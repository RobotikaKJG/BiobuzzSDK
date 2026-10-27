package org.firstinspires.ftc.teamcode.Main.ManualOpModes;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Gamepad;

import org.firstinspires.ftc.teamcode.HardwareInterface.Gamepad.GamepadIndexValues;
import org.firstinspires.ftc.teamcode.HardwareInterface.Servo.ServoConstants;
import org.firstinspires.ftc.teamcode.Main.Dependencies;
import org.firstinspires.ftc.teamcode.Main.GlobalVariables;

@TeleOp
public class ManualServoControl extends LinearOpMode {

    @Override
    public void runOpMode() throws InterruptedException {

        GlobalVariables.isAutonomous = false;
        Dependencies dependencies = new Dependencies(hardwareMap, gamepad1,gamepad2, telemetry);
        Gamepad currentGamepad1 = new Gamepad();
        Gamepad prevGamepad1 = new Gamepad();
        prevGamepad1.copy(currentGamepad1);
        currentGamepad1.copy(gamepad1);
        int currentServo = 1;
        waitForStart();

        if (isStopRequested()) return;

        while (opModeIsActive()) {

            prevGamepad1.copy(currentGamepad1);
            currentGamepad1.copy(gamepad1);
            if(gamepad1.triangle) break;
            dependencies.edgeDetection.refreshGamepadIndex(currentGamepad1,prevGamepad1);
            telemetry.addLine("Press L1 for min pos, press R1 for max pos");
            telemetry.addLine("Press square to cycle through servos");
            if(dependencies.edgeDetection.rising(GamepadIndexValues.leftBumper))
            {
                dependencies.servoControl.setServoPos(currentServo, ServoConstants.servoMinPos[currentServo]);
            }
            if(dependencies.edgeDetection.rising(GamepadIndexValues.rightBumper))
            {
                dependencies.servoControl.setServoPos(currentServo, ServoConstants.servoMaxPos[currentServo]);
            }
            if(dependencies.edgeDetection.rising(GamepadIndexValues.square))
            {
                if(currentServo < 2)
                    currentServo ++;
                else currentServo = 0;
            }

            telemetry.addLine("Currently selected servo:");
            switch (currentServo) {
                case 0:
                    telemetry.addLine("lock servo");
                    break;
                case 1:
                    telemetry.addLine("turret 1");
                    break;
                case 2:
                    telemetry.addLine("turret 2");
                    break;
            }
            telemetry.update();
        }
    }

}