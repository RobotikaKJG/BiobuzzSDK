package org.firstinspires.ftc.teamcode.Main;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.PIDFCoefficients;
import com.qualcomm.robotcore.util.RobotLog;

import org.firstinspires.ftc.teamcode.HardwareInterface.Motor.MotorConstants;
import org.firstinspires.ftc.teamcode.Subsystems.Drivebase.DrivebaseController;
import org.firstinspires.ftc.teamcode.Subsystems.Outtake.OuttakeMotor.OuttakeMotorStates;
import org.firstinspires.ftc.teamcode.Subsystems.Outtake.OuttakeStates;

import java.util.List;

/**
 * Separate TeleOp OpMode identical to GeneralBlueTeleOp, but augmented with
 * FTC Dashboard graphing for outtake motor ticks/velocity and live PIDF tuning.
 */
@Config
@TeleOp(name = "General Blue TeleOp (Outtake PIDF Tuning)", group = "TeleOp")
public class GeneralBlueTeleOpOuttakeTuning extends LinearOpMode {

    // PIDF Tuning Parameters accessible from FTC Dashboard
    public static double P = 60.0;
    public static double I = 0.0;
    public static double D = 0.0;
    public static double F = 11.75;
    public static double targetRPM = 1600.0;
    public static boolean overrideOuttakePIDF = false;

    // Drive loop target period (main thread). 2 ms => ~500 Hz cap
    private static final long DRIVE_TARGET_PERIOD_MS = 2;
    private static final long TELEMETRY_INTERVAL_MS = 50; // Faster update rate for plotting graphs

    @Override
    public void runOpMode() throws InterruptedException {
        List<LynxModule> allHubs = hardwareMap.getAll(LynxModule.class);

        GlobalVariables.isAutonomous = false;
        GlobalVariables.subCycles = false;
        GlobalVariables.alliance = Alliance.Blue;

        Dependencies dependencies = new Dependencies(hardwareMap, gamepad1, gamepad2, telemetry);
        IterativeController iterativeController = new IterativeController(dependencies);
        DrivebaseController drivebaseController = dependencies.createDrivebaseController();
        FtcDashboard dashboard = FtcDashboard.getInstance();

        Gamepad currentGamepad2 = new Gamepad();
        Gamepad prevGamepad2 = new Gamepad();

        for (LynxModule hub : allHubs) {
            hub.setBulkCachingMode(LynxModule.BulkCachingMode.MANUAL);
        }

        waitForStart();

        dependencies.servoControl.setServoStartPos();

        if (isStopRequested()) return;

        ControlThread controlThread = new ControlThread(this, allHubs, iterativeController);
        controlThread.start();

        LoopTimer driveTimer = new LoopTimer(10);
        CycleTimer cycleTimer = new CycleTimer(10);
        long lastTelemetryMs = 0;
        long prevLoopNs = System.nanoTime();
        long driveErrors = 0;
        long lastDriveErrLogMs = 0;

        try {
            while (opModeIsActive()) {
                long startNs = System.nanoTime();
                driveTimer.record(startNs - prevLoopNs); // full loop PERIOD
                prevLoopNs = startNs;

                // Gamepad 2 edge detection for manual live PIDF adjustment
                prevGamepad2.copy(currentGamepad2);
                currentGamepad2.copy(gamepad2);

                if (currentGamepad2.dpad_up && !prevGamepad2.dpad_up) targetRPM += 50;
                if (currentGamepad2.dpad_down && !prevGamepad2.dpad_down) targetRPM -= 50;
                if (currentGamepad2.a && !prevGamepad2.a) P += 1.0;
                if (currentGamepad2.b && !prevGamepad2.b) P = Math.max(0, P - 1.0);
                if (currentGamepad2.x && !prevGamepad2.x) I += 0.1;
                if (currentGamepad2.y && !prevGamepad2.y) I = Math.max(0, I - 0.1);
                if (currentGamepad2.left_bumper && !prevGamepad2.left_bumper) D += 0.1;
                if (currentGamepad2.right_bumper && !prevGamepad2.right_bumper) D = Math.max(0, D - 0.1);
                if (currentGamepad2.left_trigger > 0.5 && prevGamepad2.left_trigger <= 0.5) F += 0.25;
                if (currentGamepad2.right_trigger > 0.5 && prevGamepad2.right_trigger <= 0.5) F = Math.max(0, F - 0.25);
                if (currentGamepad2.dpad_left && !prevGamepad2.dpad_left) overrideOuttakePIDF = !overrideOuttakePIDF;

                try {
                    drivebaseController.updateState();
                    dependencies.motorControl.setMotors(MotorConstants.allDrive);

                    // If override is active OR outtake is in forwardClose state, apply PIDF tuning parameters
                    PIDFCoefficients pidf = new PIDFCoefficients(P, I, D, F);
                    if (overrideOuttakePIDF) {
                        dependencies.motorControl.setMotorRPM(MotorConstants.outtake, targetRPM, pidf);
                        dependencies.motorControl.setMotors(MotorConstants.outtake);
                    } else if (OuttakeStates.getOuttakeMotorState() == OuttakeMotorStates.forwardClose) {
                        dependencies.motorControl.setMotorRPM(MotorConstants.outtake, targetRPM, pidf);
                        dependencies.motorControl.setMotors(MotorConstants.outtake);
                    }
                } catch (Exception e) {
                    driveErrors++;
                    long n = System.currentTimeMillis();
                    if (n - lastDriveErrLogMs >= 1000) {
                        RobotLog.ee("DriveLoop", e, "drive loop error #%d (continuing)", driveErrors);
                        lastDriveErrLogMs = n;
                    }
                }

                if (gamepad1.triangle) break;

                // Send telemetry & FTC Dashboard plot packet
                long nowMs = System.currentTimeMillis();
                if (nowMs - lastTelemetryMs >= TELEMETRY_INTERVAL_MS) {
                    int outtake1Pos = dependencies.motorControl.getMotorPosition(MotorConstants.outtake1);
                    int outtake2Pos = dependencies.motorControl.getMotorPosition(MotorConstants.outtake2);
                    int outtakeAvgPos = dependencies.motorControl.getMotorPosition(MotorConstants.outtake);

                    double outtake1Vel = dependencies.motorControl.getMotorVelocity(MotorConstants.outtake1);
                    double outtake2Vel = dependencies.motorControl.getMotorVelocity(MotorConstants.outtake2);
                    double outtakeAvgVel = dependencies.motorControl.getMotorVelocity(MotorConstants.outtake);
                    double lastCommanded = dependencies.motorControl.getLastCommandedVelocity(MotorConstants.outtake);
                    double activeTargetVel = overrideOuttakePIDF ? targetRPM : (Double.isNaN(lastCommanded) ? 0 : lastCommanded);

                    // FTC Dashboard Graph Packet
                    TelemetryPacket packet = new TelemetryPacket();
                    packet.put("Outtake1 Ticks", outtake1Pos);
                    packet.put("Outtake2 Ticks", outtake2Pos);
                    packet.put("Outtake Avg Ticks", outtakeAvgPos);
                    packet.put("Outtake1 Velocity", outtake1Vel);
                    packet.put("Outtake2 Velocity", outtake2Vel);
                    packet.put("Outtake Avg Velocity", outtakeAvgVel);
                    packet.put("Target Velocity", activeTargetVel);
                    packet.put("Velocity Error", activeTargetVel - outtakeAvgVel);
                    packet.put("PIDF P", P);
                    packet.put("PIDF I", I);
                    packet.put("PIDF D", D);
                    packet.put("PIDF F", F);
                    dashboard.sendTelemetryPacket(packet);

                    // Driver Station Telemetry
                    telemetry.addLine("=== Outtake PIDF Tuning TeleOp ===");
                    telemetry.addData("Outtake 1 Ticks", outtake1Pos);
                    telemetry.addData("Outtake 2 Ticks", outtake2Pos);
                    telemetry.addData("Outtake Avg Ticks", outtakeAvgPos);
                    telemetry.addData("Outtake 1 Vel", "%.1f", outtake1Vel);
                    telemetry.addData("Outtake 2 Vel", "%.1f", outtake2Vel);
                    telemetry.addData("Target Vel", "%.1f", activeTargetVel);
                    telemetry.addData("PIDF", "P=%.2f I=%.2f D=%.2f F=%.2f", P, I, D, F);
                    telemetry.addData("Override Mode", overrideOuttakePIDF ? "ENABLED (Constant Target RPM)" : "DISABLED (Normal TeleOp)");
                    telemetry.addLine("--- Performance ---");
                    telemetry.addLine(cycleTimer.toTable());
                    telemetry.addData("Drive loop",   "%.2f ms  (%.0f hz)", driveTimer.getAvgMs(), hz(driveTimer.getAvgMs()));
                    telemetry.addData("Control loop", "%.2f ms  (%.0f hz)", controlThread.getAvgLoopMs(), hz(controlThread.getAvgLoopMs()));
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
            controlThread.stopLoop();
            controlThread.interrupt();
            try { controlThread.join(500); } catch (InterruptedException ignored) {}

            dependencies.motorControl.setMotorSpeed(MotorConstants.all, 0);
            dependencies.motorControl.setMotors(MotorConstants.all);
        }
    }

    private static double hz(double ms) {
        return ms > 0 ? 1000.0 / ms : 0.0;
    }
}
