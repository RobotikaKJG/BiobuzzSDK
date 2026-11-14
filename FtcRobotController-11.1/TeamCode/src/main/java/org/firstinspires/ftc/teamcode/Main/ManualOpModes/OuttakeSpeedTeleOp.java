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
import org.firstinspires.ftc.teamcode.Main.GlobalVariables;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.IntakeConstants;
import org.firstinspires.ftc.teamcode.Subsystems.Outtake.OuttakeConstants;

import java.util.List;

/**
 * TeleOp OpMode to test and tune Outtake speed along with Intake and Lock Servo control.
 * Press TRIANGLE (Y) to increase outtake speed by 25 ticks/sec.
 * Press X (SQUARE / CROSS) to decrease outtake speed by 25 ticks/sec.
 * Press RIGHT BUMPER to toggle outtake motor spinning on/off.
 * Press RIGHT TRIGGER to run Intake & Transfer motors while held.
 * Press LEFT BUMPER to toggle Lock Servo (Locked / Unlocked).
 */
@Config
@TeleOp(name = "Outtake Speed TeleOp", group = "Tuning")
public class OuttakeSpeedTeleOp extends LinearOpMode {

    // Live tuning parameters via FTC Dashboard
    public static double P = 60.0;
    public static double I = 0.0;
    public static double D = 0.0;
    public static double F = 11.75;

    public static double outtakeSpeed = 400.0;  // Target outtake speed (ticks/sec)
    public static double speedIncrement = 25.0; // Increment step size
    public static boolean isSpinning = false;
    public static boolean isLockServoLocked = true;

    private static final long TELEMETRY_INTERVAL_MS = 20;

    @Override
    public void runOpMode() throws InterruptedException {
        GlobalVariables.isAutonomous = false;

        List<LynxModule> allHubs = hardwareMap.getAll(LynxModule.class);
        for (LynxModule hub : allHubs) {
            hub.setBulkCachingMode(LynxModule.BulkCachingMode.MANUAL);
        }

        Dependencies dependencies = new Dependencies(hardwareMap, gamepad1, gamepad2, telemetry);
        FtcDashboard dashboard = FtcDashboard.getInstance();

        Gamepad currentGamepad1 = new Gamepad();
        Gamepad prevGamepad1 = new Gamepad();

        telemetry.addLine("=== Outtake Speed TeleOp ===");
        telemetry.addLine("TRIANGLE / Y : Increase outtake speed (+25)");
        telemetry.addLine("X / SQUARE / CROSS : Decrease outtake speed (-25)");
        telemetry.addLine("RIGHT BUMPER : Toggle Outtake Motor Spinning");
        telemetry.addLine("RIGHT TRIGGER : Hold to run Intake & Transfer Motors");
        telemetry.addLine("LEFT BUMPER : Toggle Lock Servo (Locked / Unlocked)");
        telemetry.addLine("Press START to begin.");
        telemetry.update();

        waitForStart();

        // Set initial Lock Servo position
        dependencies.servoControl.setServoPos(ServoConstants.lockServo, IntakeConstants.lockServoLockedPos);

        long lastTelemetryMs = 0;

        while (opModeIsActive()) {
            for (LynxModule hub : allHubs) {
                hub.clearBulkCache();
            }

            prevGamepad1.copy(currentGamepad1);
            currentGamepad1.copy(gamepad1);

            // Button edge detection
            boolean trianglePressed = (currentGamepad1.triangle || currentGamepad1.y) && !(prevGamepad1.triangle || prevGamepad1.y);
            boolean xPressed = (currentGamepad1.x || currentGamepad1.square || currentGamepad1.cross || currentGamepad1.a) && !(prevGamepad1.x || prevGamepad1.square || prevGamepad1.cross || prevGamepad1.a);
            boolean rbPressed = currentGamepad1.right_bumper && !prevGamepad1.right_bumper;
            boolean lbPressed = currentGamepad1.left_bumper && !prevGamepad1.left_bumper;

            // Increase outtake speed by 25 with Triangle
            if (trianglePressed) {
                outtakeSpeed += speedIncrement;
            }

            // Decrease outtake speed by 25 with X
            if (xPressed) {
                outtakeSpeed = Math.max(0, outtakeSpeed - speedIncrement);
            }

            // Toggle outtake motor spinning on/off
            if (rbPressed) {
                isSpinning = !isSpinning;
            }

            // Toggle Lock Servo position with Left Bumper
            if (lbPressed) {
                isLockServoLocked = !isLockServoLocked;
                double lockServoPos = isLockServoLocked ? IntakeConstants.lockServoLockedPos : IntakeConstants.lockServoUnlockedPos;
                dependencies.servoControl.setServoPos(ServoConstants.lockServo, lockServoPos);
            }

            // Intake & Transfer Motor control with Right Trigger (held)
            boolean intakeActive = currentGamepad1.right_trigger > 0.1;
            if (intakeActive) {
                dependencies.motorControl.setMotorSpeed(MotorConstants.intake, IntakeConstants.intakeSpeed);
                dependencies.motorControl.setMotorSpeed(MotorConstants.transfer, IntakeConstants.transferSpeed);
            } else {
                dependencies.motorControl.setMotorSpeed(MotorConstants.intake, 0.0);
                dependencies.motorControl.setMotorSpeed(MotorConstants.transfer, 0.0);
            }
            dependencies.motorControl.setMotors(MotorConstants.intake);
            dependencies.motorControl.setMotors(MotorConstants.transfer);

            // Sync with global OuttakeConstants
            OuttakeConstants.outtakeSpeed = outtakeSpeed;

            double activeTargetTPS = isSpinning ? outtakeSpeed : 0.0;

            PIDFCoefficients pidf = new PIDFCoefficients(P, I, D, F);
            dependencies.motorControl.setMotorRPM(MotorConstants.outtake, activeTargetTPS, pidf);
            dependencies.motorControl.setMotors(MotorConstants.outtake);

            long nowMs = System.currentTimeMillis();
            if (nowMs - lastTelemetryMs >= TELEMETRY_INTERVAL_MS) {
                double outtake1Vel = dependencies.motorControl.getMotorVelocity(MotorConstants.outtake1);
                double outtake2Vel = dependencies.motorControl.getMotorVelocity(MotorConstants.outtake2);
                double outtakeAvgVel = dependencies.motorControl.getMotorVelocity(MotorConstants.outtake);

                TelemetryPacket packet = new TelemetryPacket();
                packet.put("Is Outtake Spinning (1=ON, 0=OFF)", isSpinning ? 1 : 0);
                packet.put("Intake Active (1=ON, 0=OFF)", intakeActive ? 1 : 0);
                packet.put("Lock Servo (1=Locked, 0=Unlocked)", isLockServoLocked ? 1 : 0);
                packet.put("Target Velocity (ticks/s)", activeTargetTPS);
                packet.put("Outtake Avg Velocity (ticks/s)", outtakeAvgVel);
                packet.put("Outtake1 Vel", outtake1Vel);
                packet.put("Outtake2 Vel", outtake2Vel);
                packet.put("Velocity Error", isSpinning ? (activeTargetTPS - outtakeAvgVel) : 0);
                dashboard.sendTelemetryPacket(packet);

                telemetry.addLine("=== Outtake Speed TeleOp ===");
                telemetry.addData("Outtake Spin State", isSpinning ? "SPINNING (ON)" : "STOPPED (OFF)");
                telemetry.addData("Target Outtake Speed", "%.1f ticks/s", outtakeSpeed);
                telemetry.addData("Active Target Velocity", "%.1f ticks/s", activeTargetTPS);
                telemetry.addData("Outtake Avg Velocity", "%.1f ticks/s", outtakeAvgVel);
                telemetry.addData("Outtake 1 Velocity", "%.1f ticks/s", outtake1Vel);
                telemetry.addData("Outtake 2 Velocity", "%.1f ticks/s", outtake2Vel);
                telemetry.addData("Velocity Error", "%.1f ticks/s", activeTargetTPS - outtakeAvgVel);
                telemetry.addData("Intake & Transfer", intakeActive ? "RUNNING (RT HELD)" : "OFF");
                telemetry.addData("Lock Servo State", isLockServoLocked ? "LOCKED (0.25)" : "UNLOCKED (0.0)");
                telemetry.addLine("--- Controls ---");
                telemetry.addLine("TRIANGLE (Y): Increase speed (+25 ticks/s)");
                telemetry.addLine("X / SQUARE / CROSS: Decrease speed (-25 ticks/s)");
                telemetry.addLine("RIGHT BUMPER: Toggle outtake spinning on/off");
                telemetry.addLine("RIGHT TRIGGER: Hold to run intake & transfer");
                telemetry.addLine("LEFT BUMPER: Toggle lock servo");
                telemetry.update();

                lastTelemetryMs = nowMs;
            }
        }

        dependencies.motorControl.setMotorSpeed(MotorConstants.all, 0);
        dependencies.motorControl.setMotors(MotorConstants.all);
    }
}
