package org.firstinspires.ftc.teamcode.Main.ManualOpModes;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Gamepad;

import org.firstinspires.ftc.teamcode.HardwareInterface.Gamepad.GamepadIndexValues;
import org.firstinspires.ftc.teamcode.HardwareInterface.Servo.ServoConstants;
import org.firstinspires.ftc.teamcode.Main.Dependencies;
import org.firstinspires.ftc.teamcode.Main.GlobalVariables;

@TeleOp
public class ManualTrippleServoControl extends LinearOpMode {

    @Override
    public void runOpMode() throws InterruptedException {

        GlobalVariables.isAutonomous = false;
        Dependencies dependencies = new Dependencies(hardwareMap, gamepad1,gamepad2, telemetry);
        Gamepad currentGamepad1 = new Gamepad();
        Gamepad prevGamepad1 = new Gamepad();
        prevGamepad1.copy(currentGamepad1);
        currentGamepad1.copy(gamepad1);
        int currentServo = 0;
        double currentPos = 0.5;
        double increment = 0.01;
        waitForStart();

        if (isStopRequested()) return;

        while (opModeIsActive()) {

            prevGamepad1.copy(currentGamepad1);
            currentGamepad1.copy(gamepad1);
            if(gamepad1.triangle) break;
            dependencies.edgeDetection.refreshGamepadIndex(currentGamepad1,prevGamepad1);
            telemetry.addLine("Press L1 for decrease, press R1 for increase");
            telemetry.addLine("Press square to cycle through servos");
            if(dependencies.edgeDetection.rising(GamepadIndexValues.leftBumper))
            {
                currentPos -= increment;
            }
            if(dependencies.edgeDetection.rising(GamepadIndexValues.rightBumper))
            {
                currentPos += increment;
            }

            dependencies.servoControl.setServoPos(1, ServoConstants.servoMaxPos[1] * currentPos);
            dependencies.servoControl.setServoPos(2, ServoConstants.servoMaxPos[2] * currentPos);
            dependencies.servoControl.setServoPos(3, ServoConstants.servoMaxPos[3] * currentPos);

            telemetry.addData("current position", currentPos);
            telemetry.update();

        }
    }

}