package org.firstinspires.ftc.teamcode.Main;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;

import org.firstinspires.ftc.teamcode.HardwareInterface.Motor.MotorConstants;
import org.firstinspires.ftc.teamcode.HardwareInterface.Motor.MotorControl;
import org.firstinspires.ftc.teamcode.HardwareInterface.Servo.ServoConstants;


@Autonomous
public class GeneralAutonomous3 extends LinearOpMode {

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

                motorControl.setMotorSpeed(MotorConstants.allDrive,0.5);
                motorControl.setMotors(MotorConstants.allDrive);
                sleep(200);
                motorControl.setMotorSpeed(MotorConstants.allDrive,0);


                break;
            }
        } finally {
//            dependencies.shooterLogger.stop();
        }
    }
}
