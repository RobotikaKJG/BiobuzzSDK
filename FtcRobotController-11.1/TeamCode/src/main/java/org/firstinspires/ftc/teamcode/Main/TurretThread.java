package org.firstinspires.ftc.teamcode.Main;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.util.RobotLog;

import org.firstinspires.ftc.teamcode.HardwareInterface.Gamepad.EdgeDetection;
import org.firstinspires.ftc.teamcode.HardwareInterface.Gamepad.GamepadIndexValues;
import org.firstinspires.ftc.teamcode.HardwareInterface.Sensor.SensorControl;
import org.firstinspires.ftc.teamcode.Subsystems.Outtake.TurretServo.TurretServoControl;

/**
 * Turret tracking loop. Owns the Pinpoint odometry ({@code localizer.update()} +
 * the options-button heading reset) and the turret servos, so the turret aims
 * from the freshest pose, independent of the heavier control loop.
 *
 * Resilience: a transient hardware error (e.g. a Pinpoint I2C failure when the
 * Lynx bus is saturated during heavy driving) is logged and SKIPPED — it must
 * NEVER kill the loop, or the turret would stop tracking for the rest of the
 * match. Diagnostics (iterations / errorCount / lastError / alive) are exposed
 * for telemetry and logged to RobotLog (pull with `adb logcat` / `deploy.sh logs`).
 */
public class TurretThread extends Thread {
    private static final long TARGET_PERIOD_MS = 5; // ~200 Hz cap; the Pinpoint read dominates
    private static final long ERROR_LOG_THROTTLE_MS = 1000;
    private static final String TAG = "TurretThread";

    private final LinearOpMode opMode;
    private final SensorControl sensorControl;
    private final TurretServoControl turretServoControl;
    private final Gamepad gamepad1;
    private final Gamepad currentGamepad = new Gamepad();
    private final Gamepad prevGamepad = new Gamepad();
    private final EdgeDetection edgeDetection = new EdgeDetection();
    private final LoopTimer loopTimer = new LoopTimer(10);

    private volatile boolean running = true;
    private volatile boolean loopAlive = false;
    private volatile long iterations = 0;
    private volatile long errorCount = 0;
    private volatile String lastError = "none";
    private long lastErrorLogMs = 0;

    public TurretThread(LinearOpMode opMode, SensorControl sensorControl,
                        TurretServoControl turretServoControl, Gamepad gamepad1) {
        super(TAG);
        this.opMode = opMode;
        this.sensorControl = sensorControl;
        this.turretServoControl = turretServoControl;
        this.gamepad1 = gamepad1;
    }

    /** Average turret-loop period over the last 10 iterations (ms). */
    public double getAvgLoopMs() { return loopTimer.getAvgMs(); }
    /** Loop iteration count — if this stops climbing, the loop has stalled/exited. */
    public long getIterations() { return iterations; }
    /** Number of caught (non-fatal) errors this run. */
    public long getErrorCount() { return errorCount; }
    public String getLastError() { return lastError; }
    /** True while the loop is running; false once it exits. */
    public boolean isLoopAlive() { return loopAlive; }

    public void stopLoop() { running = false; }

    @Override
    public void run() {
        loopAlive = true;
        RobotLog.ii(TAG, "turret loop started");
        long prevLoopNs = System.nanoTime();
        try {
            while (running && !opMode.isStopRequested()) {
                long startNs = System.nanoTime();
                loopTimer.record(startNs - prevLoopNs); // full loop PERIOD (incl. sleep) -> true rate
                prevLoopNs = startNs;
                try {
                    // Own gamepad snapshot + edge detection (options = reset heading).
                    prevGamepad.copy(currentGamepad);
                    currentGamepad.copy(gamepad1);
                    edgeDetection.refreshGamepadIndex(currentGamepad, prevGamepad);

                    sensorControl.updateLocalizer();              // Pinpoint read + velocity calc
                    if (edgeDetection.rising(GamepadIndexValues.options)) {
                        sensorControl.resetLocalizerAngleNow();
                    }
                    turretServoControl.update();                  // aim from pose, write turret servos
                    iterations++;
                } catch (Exception e) {
                    // Transient hardware error (e.g. Pinpoint I2C failure under heavy bus
                    // load). DO NOT break — log, count, and keep tracking next iteration.
                    errorCount++;
                    lastError = e.getClass().getSimpleName() + ": " + e.getMessage();
                    long now = System.currentTimeMillis();
                    if (now - lastErrorLogMs >= ERROR_LOG_THROTTLE_MS) {
                        RobotLog.ee(TAG, e, "turret loop error #%d (continuing)", errorCount);
                        lastErrorLogMs = now;
                    }
                }

                long workMs = (System.nanoTime() - startNs) / 1_000_000L;
                long sleepMs = TARGET_PERIOD_MS - workMs;
                if (sleepMs > 0) {
                    try {
                        Thread.sleep(sleepMs);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
        } finally {
            loopAlive = false;
            RobotLog.ww(TAG, "turret loop EXITED after %d iters, %d errors (running=%b, stopRequested=%b)",
                    iterations, errorCount, running, opMode.isStopRequested());
        }
    }
}
