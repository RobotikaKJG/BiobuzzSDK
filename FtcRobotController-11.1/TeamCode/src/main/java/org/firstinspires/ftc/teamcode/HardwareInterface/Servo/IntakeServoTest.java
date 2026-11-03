package org.firstinspires.ftc.teamcode.HardwareInterface.Servo;
//IntakeServoTest


import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.Servo;

@TeleOp(name = "ServoControl", group = "TeleOp")
public class IntakeServoTest extends LinearOpMode {

    private Servo myServo;
    private double servoPosition = 0.0;  // Start at middle position (0.5)
    private final double INCREMENT = 0.05; // Convert degrees to servo range

    @Override
    public void runOpMode() {
        myServo = hardwareMap.get(Servo.class, "myServo");

        myServo.setPosition(servoPosition); // Initialize servo position

        telemetry.addData("Status", "Initialized");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {
            if (gamepad1.dpad_up) {
                servoPosition += INCREMENT;
            } else if (gamepad1.dpad_down) {
                servoPosition -= INCREMENT;
            }

            // Ensure servo position remains within bounds
            servoPosition = Math.max(0.0, Math.min(1.0, servoPosition));

            myServo.setPosition(servoPosition);

            // Output current servo position
            telemetry.addData("Servo Position", "%.3f", servoPosition);
            telemetry.update();

            sleep(100); // Prevent button spam
        }
    }
}

