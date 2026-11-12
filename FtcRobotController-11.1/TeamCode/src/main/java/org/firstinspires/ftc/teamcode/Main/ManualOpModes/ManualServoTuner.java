package org.firstinspires.ftc.teamcode;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Servo;

@TeleOp(name = "Manual Servo Tuner", group = "Test")
public class ManualServoTuner extends LinearOpMode {

    @Override
    public void runOpMode() {
        Servo lockServo = hardwareMap.get(Servo.class, "lockServo");

        double position = 0.5;
        double step = 0.01;

        // Previous button states for edge detection (one press = one step)
        boolean lastRb = false, lastLb = false;

        lockServo.setPosition(position);

        telemetry.addLine("Ready. Press START.");
        telemetry.addLine("A = 0.01 step | B = 0.05 step | Y = 0.1 step");
        telemetry.addLine("Right bumper = increase | Left bumper = decrease");
        telemetry.addLine("X = reset to 0.5");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {
            // Choose step size
            if (gamepad1.a) step = 0.01;
            if (gamepad1.b) step = 0.05;
            if (gamepad1.y) step = 0.1;

            // Increase / decrease on a fresh press only
            if (gamepad1.right_bumper && !lastRb) position += step;
            if (gamepad1.left_bumper && !lastLb) position -= step;

            // Reset
            if (gamepad1.x) position = 0.5;

            lastRb = gamepad1.right_bumper;
            lastLb = gamepad1.left_bumper;

            // Servo range is 0.0 - 1.0
            position = Math.max(0.0, Math.min(1.0, position));
            lockServo.setPosition(position);

            telemetry.addData("Position", "%.2f", position);
            telemetry.addData("Step size", "%.2f", step);
            telemetry.addLine();
            telemetry.addLine("--- CONTROLS ---");
            telemetry.addLine("Cross = 0.01 step");
            telemetry.addLine("Circle = 0.05 step");
            telemetry.addLine("Triangle = 0.1 step");
            telemetry.addLine("R1 = increase position");
            telemetry.addLine("L1 = decrease position");
            telemetry.addLine("Square = reset to 0.5");
            telemetry.update();
        }
    }
}