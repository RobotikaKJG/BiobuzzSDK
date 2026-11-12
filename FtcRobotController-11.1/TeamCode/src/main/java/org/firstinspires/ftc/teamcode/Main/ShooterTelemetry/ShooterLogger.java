package org.firstinspires.ftc.teamcode.Main.ShooterTelemetry;

import com.qualcomm.robotcore.util.RobotLog;

import org.firstinspires.ftc.robotcore.internal.system.AppUtil;
import org.firstinspires.ftc.teamcode.HardwareInterface.Motor.MotorConstants;
import org.firstinspires.ftc.teamcode.HardwareInterface.Motor.MotorControl;
import org.firstinspires.ftc.teamcode.HardwareInterface.Sensor.SensorControl;
import org.firstinspires.ftc.teamcode.Main.GlobalVariables;
import org.firstinspires.ftc.teamcode.Subsystems.Outtake.AutoCycleShoot.AutoCycleShootStates;
import org.firstinspires.ftc.teamcode.Subsystems.Outtake.OuttakeConstants;
import org.firstinspires.ftc.teamcode.Subsystems.Outtake.OuttakeMotor.OuttakeMotorStates;
import org.firstinspires.ftc.teamcode.Subsystems.Outtake.OuttakeStates;
import org.firstinspires.ftc.teamcode.Subsystems.Outtake.TurretServo.TurretServoControl;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.IntakeStates;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Records shooter (flywheel) telemetry to timestamped JSONL session files and feeds
 * the live-stream server ({@link ShooterTelemetryServer}).
 *
 * Threading: {@code sample()} is called from exactly one loop (the control loop in
 * TeleOp, the main loop in Autonomous) and must never block or throw — it builds a
 * sample and offers it to a bounded queue (drop-oldest on overflow). A background
 * writer thread drains the queue to disk and to the server. Same resilience
 * philosophy as ControlThread: a telemetry failure must never break robot control.
 *
 * Decimation: up to 100 Hz while the shooter is active (motor commanded, shoot
 * sequence running, or a ball in the queue) plus a 2s tail — so every shot is
 * captured with bounded logging overhead — and ~10 Hz keepalive otherwise to keep files small.
 */
public class ShooterLogger {
    private static final String TAG = "ShooterLogger";
    public static final File LOG_DIR = new File(AppUtil.ROBOT_DATA_DIR, "shooter-logs");
    public static final int TICKS_PER_REV = 28; // goBILDA bare-motor encoder, 1:1 flywheel

    private static final int QUEUE_CAPACITY = 4096;
    private static final long ACTIVE_PERIOD_NS = 10_000_000L; // at most 100 diagnostic samples/s
    private static final long ACTIVE_TAIL_MS = 2000;   // keep active rate this long after activity
    private static final long IDLE_PERIOD_MS = 100;    // ~10 Hz keepalive when idle
    private static final long ELECTRICAL_PERIOD_MS = 50; // one motor ADC per worker tick
    private static final long BATTERY_PERIOD_MS = 250; // voltage read throttle (blocking ADC)
    private static final int MAX_LOG_FILES = 40;

    private org.firstinspires.ftc.teamcode.HardwareInterface.Servo.ServoControl servoControl;
    private ShotTimingDetector shotDetector = new ShotTimingDetector();
    private long shotCount;
    private long lastShotMs = -1;
    private long previousSampleNanos;
    private long droppedSamples;
    private final MotorControl motorControl;
    private final SensorControl sensorControl;
    private final TurretServoControl turretServoControl;

    private final ArrayBlockingQueue<ShooterSample> queue = new ArrayBlockingQueue<>(QUEUE_CAPACITY);
    private volatile boolean running = false;
    private Thread writerThread;
    private Thread electricalThread;
    // Immutable after publication. Control loops only copy the latest cached readings.
    private volatile ElectricalSnapshot electrical = new ElectricalSnapshot();

    private static final class ElectricalSnapshot {
        final double[] currents;
        final double[] readEpochMs;
        final double battery;
        final long batteryEpochMs;
        final int motor;

        ElectricalSnapshot() {
            this(new double[8], new double[8], Double.NaN, 0, -1);
            Arrays.fill(currents, Double.NaN);
            Arrays.fill(readEpochMs, Double.NaN);
        }

        ElectricalSnapshot(double[] currents, double[] times, double battery,
                           long batteryEpochMs, int motor) {
            this.currents = currents;
            this.readEpochMs = times;
            this.battery = battery;
            this.batteryEpochMs = batteryEpochMs;
            this.motor = motor;
        }
    }
    private long sessionStartMs;

    // sample()-thread state (single caller thread; no sync needed)
    private long lastActiveMs = 0;
    private long lastIdleSampleMs = 0;
    private long lastPublishedBatteryMs = 0;
    public ShooterLogger(
            MotorControl motorControl,
            SensorControl sensorControl,
            TurretServoControl turretServoControl) {
        this.motorControl = motorControl;
        this.sensorControl = sensorControl;
        this.turretServoControl = turretServoControl;
    }

    public ShooterLogger(MotorControl motors, SensorControl sensors,
                         TurretServoControl turret,
                         org.firstinspires.ftc.teamcode.HardwareInterface.Servo.ServoControl servos) {
        this(motors, sensors, turret);
        servoControl = servos;
    }

    /** Start a logging session. Call after waitForStart(). Never throws. */
    public void start(String opModeName) {
        if (running || (writerThread != null && writerThread.isAlive())
                || (electricalThread != null && electricalThread.isAlive())) return;
        try {
            //noinspection ResultOfMethodCallIgnored
            LOG_DIR.mkdirs();
            pruneOldLogs();

            electrical = new ElectricalSnapshot();
            lastPublishedBatteryMs = 0;
            previousSampleNanos = 0;
            shotDetector = new ShotTimingDetector();
            shotCount = 0;
            lastShotMs = -1;
            droppedSamples = 0;
            lastActiveMs = lastIdleSampleMs = 0;
            sessionStartMs = System.currentTimeMillis();
            String stamp = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US)
                    .format(new Date(sessionStartMs));
            File file = new File(LOG_DIR, stamp + "_" + opModeName + ".jsonl");

            String headerJson = "{\"type\":\"header\",\"version\":4"
                    + ",\"sensorsAvailable\":" + (sensorControl != null)
                    + ",\"servosAvailable\":" + (servoControl != null)
                    + ",\"epochMs\":" + sessionStartMs
                    + ",\"opMode\":\"" + opModeName + "\""
                    + ",\"alliance\":\"" + GlobalVariables.alliance + "\""
                    + ",\"motorOrder\":[\"frontLeft\",\"backLeft\",\"frontRight\",\"backRight\",\"outtake1\",\"intake\",\"transfer\",\"outtake2\"]"
                    + ",\"servoOrder\":[\"lockServo\",\"turretServo1\",\"turretServo2\"]"
                    + ",\"ticksPerRev\":" + TICKS_PER_REV
                    + "}";

            queue.clear();
            running = true;
            writerThread = new Thread(() -> writerLoop(file, headerJson), TAG + "-writer");
            writerThread.setDaemon(true);
            writerThread.start();
            electricalThread = new Thread(this::electricalLoop, TAG + "-electrical");
            electricalThread.setDaemon(true);
            electricalThread.start();

            ShooterTelemetryServer server = ShooterTelemetryServer.getInstanceOrNull();
            if (server != null) server.onSessionStart(opModeName, headerJson);

            RobotLog.ii(TAG, "session started: %s", file.getName());
        } catch (Throwable t) {
            running = false;
            RobotLog.ee(TAG, t, "failed to start session (continuing without logging)");
        }
    }

    /** End the session: flush + close the file, notify the server. Never throws. */
    public void stop() {
        if (!running) return;
        running = false;
        if (electricalThread != null) electricalThread.interrupt();
        try {
            if (electricalThread != null) electricalThread.join(1000);
            if (writerThread != null) writerThread.join(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        ShooterTelemetryServer server = ShooterTelemetryServer.getInstanceOrNull();
        if (server != null) server.onSessionEnd();
        RobotLog.ii(TAG, "session stopped");
    }

    /**
     * Record one sample. Called once per loop iteration by exactly one thread.
     * Never throws. Call on the hardware owner thread with a fresh bulk cache.
     * Electrical ADC reads run on a separate, rate-limited worker.
     */
    public void sample() {
        if (!running) return;
        try {
            long captureStart = System.nanoTime();
            if (previousSampleNanos != 0 && captureStart - previousSampleNanos < ACTIVE_PERIOD_NS) return;
            long now = System.currentTimeMillis();

            OuttakeMotorStates motorState = OuttakeStates.getMotorState();
            boolean intakeBall = sensorControl != null && sensorControl.isIntakeBall();
            boolean transferBall = sensorControl != null && sensorControl.isTransferBall();

            boolean active = (Double.isFinite(motorControl.getLastCommandedVelocity(MotorConstants.outtake1))
                    && motorControl.getLastCommandedVelocity(MotorConstants.outtake1) != 0)
                    || motorState != OuttakeMotorStates.idle
                    || OuttakeStates.getAutoCycleShootState() != AutoCycleShootStates.idle
                    || intakeBall || transferBall
                    || RemoteControl.isActive();
            if (active) lastActiveMs = now;

            boolean fullRate = (now - lastActiveMs) < ACTIVE_TAIL_MS;
            if (!fullRate) {
                if (now - lastIdleSampleMs < IDLE_PERIOD_MS) return;
                lastIdleSampleMs = now;
            }

            ShooterSample s = new ShooterSample();
            s.tMs = now - sessionStartMs;
            s.sampleIntervalMs = previousSampleNanos == 0 ? Double.NaN
                    : (captureStart - previousSampleNanos) / 1e6;
            previousSampleNanos = captureStart;
            s.v1 = motorControl.getMotorVelocity(MotorConstants.outtake1);
            s.v2 = motorControl.getMotorVelocity(MotorConstants.outtake2);
            ElectricalSnapshot readings = electrical;
            double[] cachedMotorCurrent = readings.currents;
            s.currentMotor = readings.motor;
            s.i1 = cachedMotorCurrent[MotorConstants.outtake1];
            s.i2 = cachedMotorCurrent[MotorConstants.outtake2];
            s.idFl = cachedMotorCurrent[MotorConstants.frontLeft];
            s.idBl = cachedMotorCurrent[MotorConstants.backLeft];
            s.idFr = cachedMotorCurrent[MotorConstants.frontRight];
            s.idBr = cachedMotorCurrent[MotorConstants.backRight];
            s.iIntake = cachedMotorCurrent[MotorConstants.intake];
            s.iTransfer = cachedMotorCurrent[MotorConstants.transfer];
            s.target = motorControl.getLastCommandedVelocity(MotorConstants.outtake1);
            // Only infer shots when both motors share a known velocity target.
            double target2 = motorControl.getLastCommandedVelocity(MotorConstants.outtake2);
            double detectorTarget = s.target == target2 ? s.target * 60.0 / TICKS_PER_REV : Double.NaN;
            s.inferredShot = shotDetector.update(s.tMs,
                    (s.v1 + s.v2) * 0.5 * 60.0 / TICKS_PER_REV, detectorTarget);
            if (s.inferredShot) {
                shotCount++;
                if (lastShotMs >= 0) s.inferredShotIntervalMs = s.tMs - lastShotMs;
                lastShotMs = s.tMs;
            }
            s.inferredShotCount = shotCount;
            s.shootState = OuttakeStates.getAutoCycleShootState().name();
            s.motorState = motorState.name();
            s.intakeMotorState = IntakeStates.getIntakeMotorState().name();
            s.transferMotorState = IntakeStates.getTransferMotorState().name();
            s.turretTracking = OuttakeStates.isTurretTrackingEnabled();
            s.intakeBall = intakeBall;
            s.transferBall = transferBall;

            s.battery = readings.batteryEpochMs != lastPublishedBatteryMs
                    ? readings.battery : Double.NaN;
            s.batteryReadEpochMs = readings.batteryEpochMs;
            lastPublishedBatteryMs = readings.batteryEpochMs;

            // Localizer goal range (inches), with goalDistanceOffsetCm so the
            // dashboard matches tape. Invalid pose returns negative → omit.
            double distIn = OuttakeConstants.correctedDistanceInches(
                    sensorControl == null ? Double.NaN : sensorControl.getDistanceFromLocalizer());
            s.distance = distIn >= 0 ? distIn : Double.NaN;

            try {
                s.turretDeg = turretServoControl == null
                        ? Double.NaN : turretServoControl.getTurretAngleDeg();
            } catch (Exception ignored) {
                s.turretDeg = Double.NaN;
            }

            try {
                com.pedropathing.geometry.Pose pose = sensorControl == null
                        ? null : sensorControl.getLocalizerPose();
                if (pose == null) {
                    s.poseX = s.poseY = s.poseHeadingDeg = Double.NaN;
                } else {
                    s.poseX = pose.getX();
                    s.poseY = pose.getY();
                    s.poseHeadingDeg = Math.toDegrees(pose.getHeading());
                }
            } catch (Exception ignored) {
                s.poseX = Double.NaN;
                s.poseY = Double.NaN;
                s.poseHeadingDeg = Double.NaN;
            }

            motorControl.captureDiagnostics(s.motorPositions, s.motorVelocities,
                    s.motorPowerCommands, s.motorVelocityTargets);
            System.arraycopy(readings.readEpochMs, 0, s.currentReadEpochMs, 0, 8);
            if (servoControl != null) {
                // SDK position is a command, not physical position feedback.
                s.servoCommands = new double[]{servoControl.getServoPos(0),
                        servoControl.getServoPos(1), servoControl.getServoPos(2)};
            }
            s.captureMs = (System.nanoTime() - captureStart) / 1e6;
            s.droppedSamples = droppedSamples;
            if (!queue.offer(s)) {
                if (queue.poll() != null) droppedSamples++;
                s.droppedSamples = droppedSamples; // drop oldest, keep newest
                queue.offer(s);
            }
        } catch (Throwable t) {
            // Never let telemetry break the control loop. Rare, so log unthrottled.
            RobotLog.ee(TAG, t, "sample failed (continuing)");
        }
    }

    /** Electrical diagnostics share hub transport, so bound their total request rate. */
    private void electricalLoop() {
        int motor = 0;
        long lastErrorMs = 0;
        while (running && !Thread.currentThread().isInterrupted()) {
            long started = System.nanoTime();
            try {
                ElectricalSnapshot previous = electrical;
                double[] currents = previous.currents.clone();
                double[] times = previous.readEpochMs.clone();
                currents[motor] = motorControl.getMotorCurrent(motor);
                times[motor] = motorControl.getMotorCurrentReadEpochMs(motor);
                double battery = previous.battery;
                long batteryTime = previous.batteryEpochMs;
                if (System.currentTimeMillis() - batteryTime >= BATTERY_PERIOD_MS) {
                    battery = motorControl.getBatteryVoltage();
                    batteryTime = System.currentTimeMillis();
                }
                electrical = new ElectricalSnapshot(currents, times, battery, batteryTime, motor);
                motor = (motor + 1) % currents.length;
            } catch (Exception e) {
                long now = System.currentTimeMillis();
                if (now - lastErrorMs >= 1000) {
                    RobotLog.ee(TAG, e, "electrical diagnostics failed");
                    lastErrorMs = now;
                }
            }
            long remaining = ELECTRICAL_PERIOD_MS * 1_000_000L - (System.nanoTime() - started);
            try {
                // Always yield even if the hardware transaction exceeded the period.
                Thread.sleep(Math.max(1, remaining / 1_000_000L));
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    // ---------------- writer thread ----------------

    private void writerLoop(File file, String headerJson) {
        ShooterTelemetryServer server = ShooterTelemetryServer.getInstanceOrNull();
        try (BufferedWriter out = new BufferedWriter(new FileWriter(file), 1 << 16)) {
            out.write(headerJson);
            out.newLine();
            long lastFlushMs = System.currentTimeMillis();
            while (running || !queue.isEmpty()) {
                ShooterSample s = queue.poll(50, java.util.concurrent.TimeUnit.MILLISECONDS);
                if (s != null) {
                    String line = s.toJsonLine();
                    out.write(line);
                    out.newLine();
                    if (server != null) server.publishSample(line);
                }
                long now = System.currentTimeMillis();
                if (now - lastFlushMs >= 1000) { // survive app kill with ≤1s data loss
                    out.flush();
                    lastFlushMs = now;
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Throwable t) {
            RobotLog.ee(TAG, t, "writer thread failed; session file may be incomplete");
        }
    }

    private void pruneOldLogs() {
        File[] files = LOG_DIR.listFiles((d, n) -> n.endsWith(".jsonl"));
        if (files == null || files.length < MAX_LOG_FILES) return;
        Arrays.sort(files, Comparator.comparingLong(File::lastModified));
        for (int i = 0; i <= files.length - MAX_LOG_FILES; i++) {
            //noinspection ResultOfMethodCallIgnored
            files[i].delete();
        }
    }
}
