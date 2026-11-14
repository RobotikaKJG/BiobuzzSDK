package org.firstinspires.ftc.teamcode.Main;

import com.pedropathing.follower.Follower;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.teamcode.Autonomous.AutonomousControl;
import org.firstinspires.ftc.teamcode.Autonomous.SelectStartVariables;
import org.firstinspires.ftc.teamcode.HardwareInterface.Motor.MotorConstants;
import org.firstinspires.ftc.teamcode.HardwareInterface.Motor.MotorControl;
import org.firstinspires.ftc.teamcode.HardwareInterface.Servo.ServoConstants;


@Autonomous
public class GeneralAutonomous2 extends LinearOpMode {

    AutonomousDependencies dependencies;
    MotorControl motorControl;

    @Override
    public void runOpMode() {
        dependencies = new AutonomousDependencies(hardwareMap,gamepad1,gamepad2,telemetry);
        motorControl = dependencies.motorControl;


        while (!isStarted() && !isStopRequested()) {
            sleep(100);
        }


        if (isStopRequested()) return;

        LoopTimer loopTimer = new LoopTimer(10);

        try {
            while (opModeIsActive()) {

                sleep(3000);
                motorControl.setMotorSpeed(MotorConstants.intake, 0.8);
                motorControl.setMotorSpeed(MotorConstants.transfer, 0.8);
                motorControl.setMotorRPM(MotorConstants.outtake, 350);
                sleep(2000);
                dependencies.servoControl.setServoPos(ServoConstants.lockServo,0);
                motorControl.setMotors(MotorConstants.intake);
                motorControl.setMotors(MotorConstants.transfer);
                motorControl.setMotors(MotorConstants.outtake);
                sleep(2000);
                motorControl.setMotorSpeed(MotorConstants.intake, 0);
                motorControl.setMotorSpeed(MotorConstants.transfer, 0);
                motorControl.setMotorRPM(MotorConstants.outtake, 0);

                motorControl.setMotorSpeed(MotorConstants.allDrive,0.5);
                motorControl.setMotors(MotorConstants.allDrive);
                sleep(500);
                motorControl.setMotorSpeed(MotorConstants.allDrive,0);


                motorControl.setMotorSpeed(MotorConstants.frontLeftBackRight,-0.5);
                motorControl.setMotorSpeed(MotorConstants.frontRightBackLeft,0.5);
                motorControl.setMotors(MotorConstants.allDrive);
                sleep(1000);
                motorControl.setMotorSpeed(MotorConstants.allDrive,0);

                motorControl.setMotorSpeed(MotorConstants.allDrive,0.5);
                motorControl.setMotors(MotorConstants.allDrive);
                sleep(1300);
                motorControl.setMotorSpeed(MotorConstants.allDrive,0);

                motorControl.setMotorSpeed(MotorConstants.frontLeftBackRight,-0.5);
                motorControl.setMotorSpeed(MotorConstants.frontRightBackLeft,0.5);
                motorControl.setMotors(MotorConstants.allDrive);
                sleep(800);
                motorControl.setMotorSpeed(MotorConstants.allDrive,0);



                break;
            }
        } finally {
//            dependencies.shooterLogger.stop();
        }
    }
}
