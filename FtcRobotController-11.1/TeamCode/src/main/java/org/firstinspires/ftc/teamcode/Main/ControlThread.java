package org.firstinspires.ftc.teamcode.Main;

import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.util.RobotLog;

import java.util.List;

/**
 * Background "control" loop. Owns the Lynx bulk cache and every non-drive,
 * non-turret subsystem: gamepad edge detection, LED, buttons, intake, outtake,
 * color sensors, and the single batched {@code setMotors(notDrive)} write.
 *
 * Threading: this is the ONLY thread that clears/reads the Lynx bulk cache and
 * the only writer of motors 4..7 ({@code notDrive}) and the lock servo. The drive
 * loop (main thread) owns motors 0..3; the turret loop owns the Pinpoint + turret
 * servos. The three loops touch disjoint hardware, so no locks are needed.
 *
 * Resilience: a transient hardware error is logged and skipped, never breaking
 * the loop (that would stop every subsystem for the rest of the match).
 */
public class ControlThread extends Thread {
    // Idle-rate cap (ms). The control loop is I/O-bound when busy; this only kicks
    // in when idle, to stop it busy-spinning a core (~10kHz) — a busy-spin starves
    // the turret/drive loops' sleep wakeups and makes their measured periods spike.
    private static final long TARGET_PERIOD_MS = 3;
    private static final long ERROR_LOG_THROTTLE_MS = 1000;
    private static final String TAG = "ControlThread";

    private final LinearOpMode opMode;
    private final List<LynxModule> allHubs;
    private final IterativeController iterativeController;
    private final LoopTimer loopTimer = new LoopTimer(10);

    private volatile boolean running = true;
    private volatile boolean loopAlive = false;
    private volatile long iterations = 0;
    private volatile long errorCount = 0;
    private volatile String lastError = "none";
    private long lastErrorLogMs = 0;

    public ControlThread(LinearOpMode opMode, List<LynxModule> allHubs, IterativeController iterativeController) {
        super(TAG);
        this.opMode = opMode;
        this.allHubs = allHubs;
        this.iterativeController = iterativeController;
    }

    /** Average control-loop period over the last 10 iterations (ms). */
    public double getAvgLoopMs() { return loopTimer.getAvgMs(); }
    public long getIterations() { return iterations; }
    public long getErrorCount() { return errorCount; }
    public String getLastError() { return lastError; }
    public boolean isLoopAlive() { return loopAlive; }

    public void stopLoop() { running = false; }

    @Override
    public void run() {
        loopAlive = true;
        RobotLog.ii(TAG, "control loop started");
        long prevLoopNs = System.nanoTime();
        try {
            while (running && !opMode.isStopRequested()) {
                long startNs = System.nanoTime();
                loopTimer.record(startNs - prevLoopNs); // full loop PERIOD (incl. sleep) -> true rate
                prevLoopNs = startNs;
                try {
                    // Clear bulk cache once per loop — all hub reads this iteration use one snapshot.
                    for (LynxModule hub : allHubs) {
                        hub.clearBulkCache();
                    }
                    iterativeController.TeleOp();
                    iterations++;
                } catch (Exception e) {
                    // Transient hardware error — log, count, keep running (never kill every
                    // subsystem because of one bad I2C/serial transaction).
                    errorCount++;
                    lastError = e.getClass().getSimpleName() + ": " + e.getMessage();
                    long now = System.currentTimeMillis();
                    if (now - lastErrorLogMs >= ERROR_LOG_THROTTLE_MS) {
                        RobotLog.ee(TAG, e, "control loop error #%d (continuing)", errorCount);
                        lastErrorLogMs = now;
                    }
                }

                // Cap the idle rate so the loop doesn't busy-spin a core when there's no
                // hardware I/O this iteration (reads throttled/cached, writes gated). When
                // actually busy it's I/O-bound and work exceeds the cap, so this won't fire.
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
            RobotLog.ww(TAG, "control loop EXITED after %d iters, %d errors (running=%b, stopRequested=%b)",
                    iterations, errorCount, running, opMode.isStopRequested());
        }
    }
}
