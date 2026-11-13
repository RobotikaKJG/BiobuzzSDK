package org.firstinspires.ftc.teamcode.Main.ManualOpModes;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.dashboard.telemetry.TelemetryPacket;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.PIDFCoefficients;

import org.firstinspires.ftc.teamcode.HardwareInterface.Gamepad.GamepadIndexValues;
import org.firstinspires.ftc.teamcode.HardwareInterface.Motor.MotorConstants;
import org.firstinspires.ftc.teamcode.Main.Dependencies;

@Config
@TeleOp(name = "Outtake PIDF Tuner", group = "Tuning")
public class OuttakeTuningOpMode extends LinearOpMode {

    public static double P = 50;
    public static double I = 0;
    public static double D = 0;
    public static double F = 12.8;
    public static double targetRPM = 1900;

    @Override
    public void runOpMode() throws InterruptedException {
        Dependencies dependencies = new Dependencies(hardwareMap, gamepad1, gamepad2, telemetry);
        FtcDashboard dashboard = FtcDashboard.getInstance();

        // Track previous state for edge detection
        Gamepad prevGamepad1 = new Gamepad();
        Gamepad currentGamepad1 = new Gamepad();


        telemetry.addLine("Use Gamepad 1 to tune values:");
        telemetry.addLine("DPAD UP/DOWN: Target RPM (Hold)");
        telemetry.addLine("Cross(A)/Circle(B): P coefficient (Click)");
        telemetry.addLine("Square(X)/Triangle(Y): I coefficient (Click)");
        telemetry.addLine("LB/RB: D coefficient (Click)");
        telemetry.addLine("LT/RT: F coefficient (Hold)");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {
            // Update edge detection with current and previous state
            prevGamepad1.copy(currentGamepad1);
            currentGamepad1.copy(gamepad1);
            dependencies.edgeDetection.refreshGamepadIndex(currentGamepad1, prevGamepad1);

            if (dependencies.edgeDetection.rising(GamepadIndexValues.dpadLeft)) {
                dependencies.motorControl.setMotorSpeed(MotorConstants.transfer, 0.7);
                dependencies.motorControl.setMotorSpeed(MotorConstants.intake, 1.0);
                dependencies.motorControl.setMotors(MotorConstants.transfer);
                dependencies.motorControl.setMotors(MotorConstants.intake);
            }
            if (dependencies.edgeDetection.rising(GamepadIndexValues.dpadRight)) {
                dependencies.motorControl.setMotorSpeed(MotorConstants.transfer, 0);
                dependencies.motorControl.setMotorSpeed(MotorConstants.intake, 0);
                dependencies.motorControl.setMotors(MotorConstants.transfer);
                dependencies.motorControl.setMotors(MotorConstants.intake);
            }

            // Tune Target RPM
            if (dependencies.edgeDetection.rising(GamepadIndexValues.dpadUp)) targetRPM = 1900;
            if (dependencies.edgeDetection.rising(GamepadIndexValues.dpadDown)) targetRPM = 0;

            // Tune P
            if (dependencies.edgeDetection.rising(GamepadIndexValues.cross)) P += 1;
            if (dependencies.edgeDetection.rising(GamepadIndexValues.circle)) P -= 1;

            // Tune I
            if (dependencies.edgeDetection.rising(GamepadIndexValues.square)) I += 0.01;
            if (dependencies.edgeDetection.rising(GamepadIndexValues.triangle)) I -= 0.01;

            // Tune D
            if (dependencies.edgeDetection.rising(GamepadIndexValues.leftBumper)) D += 0.1;
            if (dependencies.edgeDetection.rising(GamepadIndexValues.rightBumper)) D -= 0.1;

            // Tune F
            if (dependencies.edgeDetection.rising(GamepadIndexValues.leftTrigger)) F -= 0.05;
            if (dependencies.edgeDetection.rising(GamepadIndexValues.rightTrigger)) F += 0.05;

            PIDFCoefficients pidf = new PIDFCoefficients(P, I, D, F);
            dependencies.motorControl.setMotorRPM(MotorConstants.outtake, targetRPM, pidf);
//            dependencies.motorControl.setMotorSpeed(MotorConstants.outtake, 0.5);
            double currentVelocity = dependencies.motorControl.getMotorVelocity(MotorConstants.outtake);
//            if (currentVelocity < 1900) {
//                dependencies.motorControl.setMotorSpeed(MotorConstants.outtake, 1);
//            }
//            dependencies.motorControl.setMotors(MotorConstants.outtake);

            TelemetryPacket packet = new TelemetryPacket();
            packet.put("Target Velocity", targetRPM);
            packet.put("Actual Velocity", currentVelocity);
            packet.put("P", P);
            packet.put("I", I);
            packet.put("D", D);
            packet.put("F", F);
            dashboard.sendTelemetryPacket(packet);


            telemetry.addData("Actual Velocity", currentVelocity);
            telemetry.addData("DPAD UP/DOWN: Target RPM", targetRPM);
            telemetry.addData("Cross(A)/Circle(B): P", P);
            telemetry.addData("Square(X)/Triangle(Y): I", I);
            telemetry.addData("LB/RB: D", D);
            telemetry.addData("LT/RT: F", F);
            telemetry.update();
        }
    }
}