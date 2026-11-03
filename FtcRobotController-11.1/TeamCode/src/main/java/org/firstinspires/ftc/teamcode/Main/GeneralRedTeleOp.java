package org.firstinspires.ftc.teamcode.Main;

import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.util.RobotLog;

import org.firstinspires.ftc.teamcode.HardwareInterface.Motor.MotorConstants;
import org.firstinspires.ftc.teamcode.Subsystems.Drivebase.DrivebaseController;
import org.firstinspires.ftc.teamcode.Subsystems.Outtake.AutoCycleShoot.AutoCycleShootStates;
import org.firstinspires.ftc.teamcode.Subsystems.Outtake.OuttakeStates;

import java.util.List;


@TeleOp
public class GeneralRedTeleOp extends LinearOpMode {

    // Drive loop target period (main thread). 2 ms => ~500 Hz cap; the loop only
    // sleeps the leftover when idle (setMotors gates redundant writes) so it never
    // busy-spins. When actively driving it's bound by the Lynx bus, not this cap.
    private static final long DRIVE_TARGET_PERIOD_MS = 2;
    private static final long TELEMETRY_INTERVAL_MS = 150;

    @Override
    public void runOpMode() throws InterruptedException {
        List<LynxModule> allHubs = hardwareMap.getAll(LynxModule.class);

        GlobalVariables.isAutonomous = false;
        GlobalVariables.subCycles = false;
        GlobalVariables.hang = false;
        GlobalVariables.alliance = Alliance.Red;
        GlobalVariables.rpmOffset = 0;

        Dependencies dependencies = new Dependencies(hardwareMap, gamepad1, gamepad2, telemetry);
        IterativeController iterativeController = new IterativeController(dependencies);
        DrivebaseController drivebaseController = dependencies.createDrivebaseController();

        for (LynxModule hub : allHubs) {
            hub.setBulkCachingMode(LynxModule.BulkCachingMode.MANUAL);
        }

        waitForStart();

        dependencies.servoControl.setServoStartPos();

        if (isStopRequested()) return;

        dependencies.shooterLogger.start("GeneralRedTeleOp");

        // Background loops. Control: bulk cache + non-drive/non-turret subsystems
        // (motors 4..7). Turret: Pinpoint localizer + turret servo tracking.
        ControlThread controlThread = new ControlThread(this, allHubs, iterativeController);
        TurretThread turretThread = new TurretThread(this, dependencies.sensorControl,
                dependencies.turretServoControl, gamepad1);
        controlThread.start();
        turretThread.start();

        LoopTimer driveTimer = new LoopTimer(10);
        CycleTimer cycleTimer = new CycleTimer(10);
        AutoCycleShootStates prevShootState = AutoCycleShootStates.idle;
        long lastTelemetryMs = 0;
        long prevLoopNs = System.nanoTime();
        long driveErrors = 0;
        long lastDriveErrLogMs = 0;

        try {
            // Drive loop runs on the main thread (as fast as the bus allows). Owns motors 0..3.
            while (opModeIsActive()) {
                long startNs = System.nanoTime();
                driveTimer.record(startNs - prevLoopNs); // full loop PERIOD (incl. sleep) -> true rate
                prevLoopNs = startNs;

                try {
                    drivebaseController.updateState();
                    dependencies.motorControl.setMotors(MotorConstants.allDrive);
                } catch (Exception e) {
                    // Transient hardware error — log and keep driving (never end the match
                    // because of one bad bus transaction).
                    driveErrors++;
                    long n = System.currentTimeMillis();
                    if (n - lastDriveErrLogMs >= 1000) {
                        RobotLog.ee("DriveLoop", e, "drive loop error #%d (continuing)", driveErrors);
                        lastDriveErrLogMs = n;
                    }
                }

                // Scoring cycle time: each idle -> active transition of the shoot sequence is
                // one "shoot". recordEvent discards >20s gaps (idle stretches) as non-cycles.
                AutoCycleShootStates shootState = OuttakeStates.getAutoCycleShootState();
                if (prevShootState == AutoCycleShootStates.idle && shootState != AutoCycleShootStates.idle) {
                    long cyc = cycleTimer.recordEvent(System.currentTimeMillis());
                    if (cyc >= 0) {
                        RobotLog.ii("CycleTimer", "shoot cycle %.2fs (avg %.2fs, n=%d)",
                                cyc / 1000.0, cycleTimer.getAvgSec(), cycleTimer.getCount());
                    }
                }
                prevShootState = shootState;

                if (gamepad1.triangle) break;

                // Throttle telemetry so it never caps the fast drive loop.
                long nowMs = System.currentTimeMillis();
                if (nowMs - lastTelemetryMs >= TELEMETRY_INTERVAL_MS) {
                    telemetry.addData("Deployed", BuildInfo.DEPLOYED);
                    telemetry.addLine(cycleTimer.toTable());
                    telemetry.addData("Drive loop",   "%.2f ms  (%.0f hz)", driveTimer.getAvgMs(), hz(driveTimer.getAvgMs()));
                    telemetry.addData("Turret loop",  "%.2f ms  (%.0f hz)", turretThread.getAvgLoopMs(), hz(turretThread.getAvgLoopMs()));
                    telemetry.addData("Control loop", "%.2f ms  (%.0f hz)", controlThread.getAvgLoopMs(), hz(controlThread.getAvgLoopMs()));
                    telemetry.addData("Turret diag", "iters=%d err=%d alive=%b track=%b",
                            turretThread.getIterations(), turretThread.getErrorCount(),
                            turretThread.isLoopAlive(), OuttakeStates.isTurretTrackingEnabled());
                    telemetry.addData("Turret aim", "cur=%.0f tgt=%.0f",
                            dependencies.turretServoControl.getTurretAngleDeg(),
                            dependencies.turretServoControl.getTargetAngleDeg());
                    if (turretThread.getErrorCount() > 0)
                        telemetry.addData("Turret lastErr", turretThread.getLastError());
                    if (controlThread.getErrorCount() > 0)
                        telemetry.addData("Control lastErr", controlThread.getErrorCount() + "x " + controlThread.getLastError());
                    if (driveErrors > 0)
                        telemetry.addData("Drive errors", driveErrors);
                    telemetry.update();
                    lastTelemetryMs = nowMs;
                }

                long workMs = (System.nanoTime() - startNs) / 1_000_000L;
                long sleepMs = DRIVE_TARGET_PERIOD_MS - workMs;
                if (sleepMs > 0) sleep(sleepMs);
            }
        } finally {
            // Stop the background loops, then zero every motor from this thread.
            controlThread.stopLoop();
            turretThread.stopLoop();
            dependencies.shooterLogger.stop();
            controlThread.interrupt();
            turretThread.interrupt();
            try { controlThread.join(500); } catch (InterruptedException ignored) {}
            try { turretThread.join(500); } catch (InterruptedException ignored) {}

            dependencies.motorControl.setMotorSpeed(MotorConstants.all, 0);
            dependencies.motorControl.setMotors(MotorConstants.all);
        }
    }

    private static double hz(double ms) {
        return ms > 0 ? 1000.0 / ms : 0.0;
    }
}
