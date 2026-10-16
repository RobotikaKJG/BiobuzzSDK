package org.firstinspires.ftc.teamcode.Main.ManualOpModes;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.PIDFCoefficients;

import org.firstinspires.ftc.teamcode.HardwareInterface.Motor.MotorConstants;
import org.firstinspires.ftc.teamcode.HardwareInterface.Servo.ServoConstants;
import org.firstinspires.ftc.teamcode.Main.Dependencies;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.IntakeConstants;

import java.util.List;

/**
 * Standalone TeleOp OpMode for running and tuning the outtake motors only.
 * Press SQUARE on the PS5 controller to start or stop outtake motor spinning.
 * Press RIGHT TRIGGER to turn ON intake and transfer motors.
 * Press LEFT BUMPER to toggle lock servo (Locked / Unlocked).
 * Press TRIANGLE to increase F by 2, press CROSS to decrease F by 2.
 * Manually set target speed (ticks/sec), target power, or PIDF values in FTC Dashboard.
 */
@Config
@TeleOp(name = "Outtake Motor Only PIDF Tuning", group = "Tuning")
public class OuttakeMotorOnlyTuning extends LinearOpMode {

    // Live tuning parameters set manually from FTC Dashboard
    public static double P = 60.0;
    public static double I = 0.0;
    public static double D = 0.0;
    public static double F = 11.75;
    public static double targetTicksPerSecond = 1600.0; // Target velocity in ticks/sec (Dashboard)
    public static double targetPower = 0.6;            // Target direct motor power (-1.0 to 1.0)
    public static boolean useDirectPowerMode = false;   // true = Direct Power Mode, false = Velocity PIDF Mode

    private static final long TELEMETRY_INTERVAL_MS = 20; // 50 Hz graph updates
    private boolean isSpinning = false;
    private boolean isLockServoLocked = true;

    @Override
    public void runOpMode() throws InterruptedException {
        List<LynxModule> allHubs = hardwareMap.getAll(LynxModule.class);
        for (LynxModule hub : allHubs) {
            hub.setBulkCachingMode(LynxModule.BulkCachingMode.MANUAL);
        }

        Dependencies dependencies = new Dependencies(hardwareMap, gamepad1, gamepad2, telemetry);
        FtcDashboard dashboard = FtcDashboard.getInstance();

        Gamepad currentGamepad1 = new Gamepad();
        Gamepad prevGamepad1 = new Gamepad();

        telemetry.addLine("=== Standalone Outtake Motor Tuner ===");
        telemetry.addLine("SQUARE: Start/Stop Outtake Motor Spinning");
        telemetry.addLine("RIGHT TRIGGER: Turn ON Intake & Transfer Motors");
        telemetry.addLine("LEFT BUMPER: Toggle Lock Servo (Locked / Unlocked)");
        telemetry.addLine("TRIANGLE: Increase F by +2 | CROSS: Decrease F by -2");
        telemetry.addLine("Change speed and PIDF manually on FTC Dashboard.");
        telemetry.addLine("Press START to begin.");
        telemetry.update();

        waitForStart();

        // Initialize servo position
        dependencies.servoControl.setServoPos(ServoConstants.lockServo, IntakeConstants.lockServoLockedPos);

        long lastTelemetryMs = 0;

        while (opModeIsActive()) {
            // Manual bulk cache refresh for fast encoder / velocity reads
            for (LynxModule hub : allHubs) {
                hub.clearBulkCache();
            }

            // Gamepad 1 edge detection for PS5 buttons
            prevGamepad1.copy(currentGamepad1);
            currentGamepad1.copy(gamepad1);

            // Press SQUARE on PS5 controller to toggle outtake motor spinning on/off
            boolean squarePressed = (currentGamepad1.square || currentGamepad1.x) && !(prevGamepad1.square || prevGamepad1.x);
            if (squarePressed) {
                isSpinning = !isSpinning;
            }

            // Press LEFT BUMPER to toggle lock servo state (Locked vs Unlocked)
            boolean leftBumperPressed = currentGamepad1.left_bumper && !prevGamepad1.left_bumper;
            if (leftBumperPressed) {
                isLockServoLocked = !isLockServoLocked;
                double lockServoPos = isLockServoLocked ? IntakeConstants.lockServoLockedPos : IntakeConstants.lockServoUnlockedPos;
                dependencies.servoControl.setServoPos(ServoConstants.lockServo, lockServoPos);
            }

            // Press TRIANGLE to increase F constant by 2
            boolean trianglePressed = (currentGamepad1.triangle || currentGamepad1.y) && !(prevGamepad1.triangle || prevGamepad1.y);
            if (trianglePressed) {
                F += 0.25;
            }

            // Press CROSS to decrease F constant by 2
            boolean crossPressed = (currentGamepad1.cross || currentGamepad1.a) && !(prevGamepad1.cross || prevGamepad1.a);
            if (crossPressed) {
                F = Math.max(0, F - 0.25);
            }

            // Press RIGHT TRIGGER to turn ON intake and transfer motors
            boolean intakeTransferActive = gamepad1.right_trigger > 0.1;
            if (intakeTransferActive) {
                dependencies.motorControl.setMotorSpeed(MotorConstants.intake, IntakeConstants.intakeSpeed);
                dependencies.motorControl.setMotorSpeed(MotorConstants.transfer, IntakeConstants.transferSpeed);
            } else {
                dependencies.motorControl.setMotorSpeed(MotorConstants.intake, 0.0);
                dependencies.motorControl.setMotorSpeed(MotorConstants.transfer, 0.0);
            }
            dependencies.motorControl.setMotors(MotorConstants.intake);
            dependencies.motorControl.setMotors(MotorConstants.transfer);

            double activePower = isSpinning ? targetPower : 0.0;
            double activeTargetTPS = isSpinning ? targetTicksPerSecond : 0.0;

            // Apply motor output for outtake
            if (useDirectPowerMode) {
                dependencies.motorControl.setMotorSpeed(MotorConstants.outtake, activePower);
                dependencies.motorControl.setMotors(MotorConstants.outtake);
            } else {
                PIDFCoefficients pidf = new PIDFCoefficients(P, I, D, F);
                dependencies.motorControl.setMotorRPM(MotorConstants.outtake, activeTargetTPS, pidf);
                dependencies.motorControl.setMotors(MotorConstants.outtake);
            }

            // High-frequency telemetry update for FTC Dashboard plotting
            long nowMs = System.currentTimeMillis();
            if (nowMs - lastTelemetryMs >= TELEMETRY_INTERVAL_MS) {
                int outtake1Pos = dependencies.motorControl.getMotorPosition(MotorConstants.outtake1);
                int outtake2Pos = dependencies.motorControl.getMotorPosition(MotorConstants.outtake2);
                int outtakeAvgPos = dependencies.motorControl.getMotorPosition(MotorConstants.outtake);

                double outtake1Vel = dependencies.motorControl.getMotorVelocity(MotorConstants.outtake1);
                double outtake2Vel = dependencies.motorControl.getMotorVelocity(MotorConstants.outtake2);
                double outtakeAvgVel = dependencies.motorControl.getMotorVelocity(MotorConstants.outtake);

                // Send plot packet to FTC Dashboard
                TelemetryPacket packet = new TelemetryPacket();
                packet.put("Is Outtake Spinning (1=ON, 0=OFF)", isSpinning ? 1 : 0);
                packet.put("Intake & Transfer Active (1=ON, 0=OFF)", intakeTransferActive ? 1 : 0);
                packet.put("Lock Servo State (1=Locked, 0=Unlocked)", isLockServoLocked ? 1 : 0);
                packet.put("Control Mode (1=Power, 0=Velocity)", useDirectPowerMode ? 1 : 0);
                packet.put("Command Power", activePower);
                packet.put("Target Vel (Ticks per Sec)", useDirectPowerMode ? 0 : activeTargetTPS);
                packet.put("Outtake1 Vel (Ticks per Sec)", outtake1Vel);
                packet.put("Outtake2 Vel (Ticks per Sec)", outtake2Vel);
                packet.put("Outtake Avg Vel (Ticks per Sec)", outtakeAvgVel);
                packet.put("Outtake1 Encoder (Ticks)", outtake1Pos);
                packet.put("Outtake2 Encoder (Ticks)", outtake2Pos);
                packet.put("Outtake Avg Encoder (Ticks)", outtakeAvgPos);
                packet.put("Velocity Error (Ticks per Sec)", useDirectPowerMode ? 0 : (activeTargetTPS - outtakeAvgVel));
                packet.put("PIDF P", P);
                packet.put("PIDF I", I);
                packet.put("PIDF D", D);
                packet.put("PIDF F", F);
                dashboard.sendTelemetryPacket(packet);

                // Display parameters on Driver Station Telemetry
                telemetry.addLine("=== Standalone Outtake Motor Tuner ===");
                telemetry.addData("Outtake Spin State", isSpinning ? "SPINNING (ON)" : "STOPPED (OFF)");
                telemetry.addData("Intake/Transfer State", intakeTransferActive ? "RUNNING (RT HELD)" : "OFF");
                telemetry.addData("Lock Servo State", isLockServoLocked ? "LOCKED (0.8)" : "UNLOCKED (1.0)");
                telemetry.addData("Control Mode", useDirectPowerMode ? "DIRECT POWER MODE" : "VELOCITY PIDF MODE");
                if (useDirectPowerMode) {
                    telemetry.addData("Target Power", "%.2f", targetPower);
                    telemetry.addData("Active Power", "%.2f", activePower);
                } else {
                    telemetry.addData("Target Velocity", "%.1f ticks/s", targetTicksPerSecond);
                    telemetry.addData("Active Target TPS", "%.1f ticks/s", activeTargetTPS);
                    telemetry.addData("Velocity Error", "%.1f ticks/s", activeTargetTPS - outtakeAvgVel);
                }
                telemetry.addData("Outtake 1 Vel", "%.1f ticks/s", outtake1Vel);
                telemetry.addData("Outtake 2 Vel", "%.1f ticks/s", outtake2Vel);
                telemetry.addData("Outtake Avg Vel", "%.1f ticks/s", outtakeAvgVel);
                telemetry.addData("Outtake 1 Position", "%d ticks", outtake1Pos);
                telemetry.addData("Outtake 2 Position", "%d ticks", outtake2Pos);
                telemetry.addData("PIDF Coefficients", "P=%.2f  I=%.2f  D=%.2f  F=%.2f", P, I, D, F);
                telemetry.addLine("--- Controls ---");
                telemetry.addLine("SQUARE (PS5): Start / Stop Outtake Motor");
                telemetry.addLine("RIGHT TRIGGER: Run Intake & Transfer Motors");
                telemetry.addLine("LEFT BUMPER: Toggle Lock Servo (Locked/Unlocked)");
                telemetry.addLine("TRIANGLE (PS5): F = F + 2");
                telemetry.addLine("CROSS (PS5): F = F - 2");
                telemetry.addLine("Change Speed / Power / PIDF manually in FTC Dashboard");
                telemetry.update();

                lastTelemetryMs = nowMs;
            }
        }

        // Stop all motors on OpMode exit
        dependencies.motorControl.setMotorSpeed(MotorConstants.all, 0);
        dependencies.motorControl.setMotors(MotorConstants.all);
    }
}
