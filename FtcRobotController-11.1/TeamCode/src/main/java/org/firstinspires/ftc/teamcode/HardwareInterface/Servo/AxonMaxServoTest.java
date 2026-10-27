package org.firstinspires.ftc.teamcode.HardwareInterface.Servo;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Servo;

@TeleOp(name = "AxonMaxServoTest", group = "TeleOp")
public class AxonMaxServoTest extends LinearOpMode {

    private Servo turretServo1;
    private Servo turretServo2;

    @Override
    public void runOpMode() {
        turretServo1 = hardwareMap.get(Servo.class, "turretServo1");
        turretServo2 = hardwareMap.get(Servo.class, "turretServo2");

        telemetry.addLine("Servo Control Ready (PS5)");
        telemetry.addLine("Square: 0.0 | Circle: 1.0 | Cross: 0.5");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {
            if (gamepad1.square) {
                turretServo1.setPosition(0.0);
                turretServo2.setPosition(1.0);
            } else if (gamepad1.circle) {
                turretServo1.setPosition(1.0);
                turretServo2.setPosition(0.0);
            } else if (gamepad1.cross) {
                turretServo1.setPosition(0.5);
                turretServo2.setPosition(0.5);
            }

            telemetry.addData("Servo 1 Position", turretServo1.getPosition());
            telemetry.addData("Servo 2 Position", turretServo2.getPosition());
            telemetry.update();
        }
    }
}
