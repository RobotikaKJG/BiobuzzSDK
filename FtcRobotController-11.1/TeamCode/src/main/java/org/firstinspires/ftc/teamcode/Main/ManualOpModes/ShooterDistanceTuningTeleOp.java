package org.firstinspires.ftc.teamcode.Main.ManualOpModes;

import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.HardwareInterface.Motor.MotorConstants;
import org.firstinspires.ftc.teamcode.Main.Alliance;
import org.firstinspires.ftc.teamcode.Main.ControlThread;
import org.firstinspires.ftc.teamcode.Main.Dependencies;
import org.firstinspires.ftc.teamcode.Main.GlobalVariables;
import org.firstinspires.ftc.teamcode.Main.IterativeController;
import org.firstinspires.ftc.teamcode.Main.LoopTimer;
import org.firstinspires.ftc.teamcode.Main.TurretThread;
import org.firstinspires.ftc.teamcode.Subsystems.Drivebase.DrivebaseController;

import java.util.List;

@TeleOp(name = "ShooterDistanceTuningTeleOp", group = "Tuning")
public class ShooterDistanceTuningTeleOp extends LinearOpMode {

    private static final long DRIVE_TARGET_PERIOD_MS = 2;
    private static final long TELEMETRY_INTERVAL_MS = 100;
    private boolean isLimelightResetting = false;

    @Override
    public void runOpMode() throws InterruptedException {
        List<LynxModule> allHubs = hardwareMap.getAll(LynxModule.class);

        // Standard Robot Setup
        GlobalVariables.isAutonomous = false;
        GlobalVariables.subCycles = false;
        GlobalVariables.hang = false;
        GlobalVariables.alliance = Alliance.Red;
        GlobalVariables.rpmOffset = 0; // Initialize tuning offset

        Dependencies dependencies = new Dependencies(hardwareMap, gamepad1, gamepad2, telemetry);
        IterativeController iterativeController = new IterativeController(dependencies);
        DrivebaseController drivebaseController = dependencies.createDrivebaseController();

        for (LynxModule hub : allHubs) {
            hub.setBulkCachingMode(LynxModule.BulkCachingMode.MANUAL);
        }

        waitForStart();

        dependencies.servoControl.setServoStartPos();

        if (isStopRequested()) return;

        dependencies.shooterLogger.start("ShooterDistanceTuningTeleOp");

        // Start background threads for normal robot functioning
        ControlThread controlThread = new ControlThread(this, allHubs, iterativeController);
        TurretThread turretThread = new TurretThread(this, dependencies.sensorControl,
                dependencies.turretServoControl, gamepad1);
        controlThread.start();
        turretThread.start();

        LoopTimer driveTimer = new LoopTimer(10);
        long lastTelemetryMs = 0;
        long prevLoopNs = System.nanoTime();

        // Local edge state. We deliberately do NOT use dependencies.edgeDetection here:
        // ControlThread owns that object and writes its EnumMaps with no memory barrier,
        // so reading it from this thread both misses presses (stale cached read) and
        // double-counts them (the rising flag stays set for a whole control cycle, which
        // is longer than this loop's period). Sampling gamepad1 directly is race-free.
        boolean prevTriangle = false;
        boolean prevCross = false;

        try {
            while (opModeIsActive()) {
                long startNs = System.nanoTime();
                driveTimer.record(startNs - prevLoopNs);
                prevLoopNs = startNs;

                // 1. Shooter velocity tuning: Triangle = +20, Cross = -20 (ticks/s).
                // NOT on the d-pad: ControlThread runs the normal TeleOp bindings, where
                // dpadUp toggles the intake motor, dpadRight toggles the latch servo, and
                // dpadLeft triggers a Limelight localizer reset — that last one would move
                // the distance reading mid-sweep and silently corrupt a calibration point.
                // Triangle/Cross are unbound in TeleOp and sit vertically like +/-.
                boolean triangleNow = gamepad1.triangle;
                boolean crossNow = gamepad1.cross;
                if (triangleNow && !prevTriangle) {
                    GlobalVariables.rpmOffset += 20;
                }
                if (crossNow && !prevCross) {
                    GlobalVariables.rpmOffset -= 20;
                }
                prevTriangle = triangleNow;
                prevCross = crossNow;

                // 3. Continuous Camera-based Position Adjustment
                dependencies.sensorControl.applyContinuousVisionFusion();

                // 4. Standard Drivebase Control (Main thread)
                drivebaseController.updateState();
                dependencies.motorControl.setMotors(MotorConstants.allDrive);

                // 5. Specialized Tuning Telemetry
                long nowMs = System.currentTimeMillis();
                if (nowMs - lastTelemetryMs >= TELEMETRY_INTERVAL_MS) {
                    telemetry.addLine("--- SHOOTER TUNING (△ +20 / ✖ -20 ticks/s) ---");
                    telemetry.addData("Distance from Target", "%.2f inches", dependencies.sensorControl.getDistanceFromLocalizer());
                    telemetry.addData("Target Base RPM", "%.0f", GlobalVariables.outtakeTargetSpeed);
                    telemetry.addData("Manual RPM Offset", "%.0f", GlobalVariables.rpmOffset);
                    telemetry.addData("Final Target RPM", "%.0f", GlobalVariables.outtakeTargetSpeed + GlobalVariables.rpmOffset);
                    telemetry.addData("Actual Shooter RPM", "%.0f", dependencies.motorControl.getMotorVelocity(MotorConstants.outtake));
                    
                    telemetry.addLine("\n--- ROBOT POSITION ---");
                    telemetry.addData("Pose", dependencies.sensorControl.getLocalizerPose().toString());
                    telemetry.addData("Turret Angle", "%.1f°", dependencies.turretServoControl.getTurretAngleDeg());
                    
                    telemetry.addLine("\n--- SYSTEM STATUS ---");
                    telemetry.addData("Drive Loop", "%.1f Hz", 1000.0 / driveTimer.getAvgMs());
                    telemetry.addData("Control Loop", "%.1f Hz", 1000.0 / controlThread.getAvgLoopMs());
                    
                    telemetry.update();
                    lastTelemetryMs = nowMs;
                }

                // Maintain loop timing
                long workMs = (System.nanoTime() - startNs) / 1_000_000L;
                long sleepMs = DRIVE_TARGET_PERIOD_MS - workMs;
                if (sleepMs > 0) sleep(sleepMs);
            }
        } finally {
            // Clean shutdown of background threads
            controlThread.stopLoop();
            turretThread.stopLoop();
            dependencies.shooterLogger.stop();
            try { controlThread.join(500); } catch (InterruptedException ignored) {}
            try { turretThread.join(500); } catch (InterruptedException ignored) {}
            
            dependencies.motorControl.setMotorSpeed(MotorConstants.all, 0);
            dependencies.motorControl.setMotors(MotorConstants.all);
        }
    }
}
