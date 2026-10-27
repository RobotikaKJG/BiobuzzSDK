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

    public static double P = 60;
    public static double I = 0;
    public static double D = 0;
    public static double F = 11.75;
    public static double targetRPM = 1600;

    @Override
    public void runOpMode() throws InterruptedException {
        Dependencies dependencies = new Dependencies(hardwareMap, gamepad1, gamepad2, telemetry);
        FtcDashboard dashboard = FtcDashboard.getInstance();
        
        // Track previous state for edge detection
        Gamepad prevGamepad1 = new Gamepad();
        Gamepad currentGamepad1 = new Gamepad();


        telemetry.addLine("Use Gamepad 1 to tune values:");
        telemetry.addLine("DPAD UP/DOWN: Target RPM (Hold)");
        telemetry.addLine("Cross/Circle: P coefficient (Click)");
        telemetry.addLine("Square/Triangle: I coefficient (Click)");
        telemetry.addLine("L1/R1: D coefficient (Click)");
        telemetry.addLine("L2/R2: F coefficient (Hold)");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {
            // Update edge detection with current and previous state

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
            dashboard.sendTelemetryPacket(packet);
            telemetry.update();
        }
    }
}
