package org.firstinspires.ftc.teamcode.HardwareInterface.Servo;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.Servo;

@TeleOp(name = "ServoMoveToPosition", group = "TeleOp")
public class ServoMoveToPosition extends LinearOpMode {

    private Servo myServo;
    private final double TARGET_POSITION = 0.69; // Final position
    private final double START_POSITION = 0.10;   // Initial position
    private final double INCREMENT = 0.01;       // Smooth movement step
    private final int DELAY_MS = 0;             // Delay between increments

    @Override
    public void runOpMode() {
        myServo = hardwareMap.get(Servo.class, "myServo");

        myServo.setPosition(START_POSITION); // Start at 0

        telemetry.addData("Status", "Initialized");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {
            if (gamepad1.dpad_right) {
                moveServoSmoothly(START_POSITION, TARGET_POSITION);
            }

            // Output current servo position
            telemetry.addData("Servo Position", "%.3f", myServo.getPosition());
            telemetry.update();
        }
    }

    private void moveServoSmoothly(double start, double target) {
        double position = start;
        while (opModeIsActive() && position <= target) {
            myServo.setPosition(position);
            telemetry.addData("Moving to", "%.3f", position);
            telemetry.update();
            position += INCREMENT;
            sleep(DELAY_MS);
        }
        myServo.setPosition(target); // Ensure final position is reached
    }
}