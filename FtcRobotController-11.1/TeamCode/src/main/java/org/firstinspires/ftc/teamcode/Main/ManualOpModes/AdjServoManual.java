package org.firstinspires.ftc.teamcode.Main.ManualOpModes;

import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Servo;

/**
 * A simple OpMode to control a single servo named "adjServo".
 * The position can be adjusted via FTC Dashboard during runtime.
 */
@Config
@TeleOp(name = "Adj Servo Manual", group = "Manual")
public class AdjServoManual extends LinearOpMode {

    public static double targetPosition = 0.5;

    @Override
    public void runOpMode() {
        Servo adjServo = hardwareMap.get(Servo.class, "adjServo");

        telemetry.addLine("Initialized");
        telemetry.addLine("Adjust 'targetPosition' in FTC Dashboard to move the servo.");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {
            adjServo.setPosition(targetPosition);

            telemetry.addData("Target Position", targetPosition);
            telemetry.addData("Servo Name", "adjServo");
            telemetry.update();
        }
    }
}
