package org.firstinspires.ftc.teamcode.Main.ManualOpModes;


import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.HardwareInterface.Motor.MotorConstants;
import org.firstinspires.ftc.teamcode.Main.Dependencies;
import org.firstinspires.ftc.teamcode.Main.GlobalVariables;
import org.firstinspires.ftc.teamcode.Subsystems.Drivebase.DrivebaseController;

@TeleOp
public class DrivebaseTest extends LinearOpMode {

    @Override
    public void runOpMode() throws InterruptedException {



        GlobalVariables.isAutonomous = false;
        Dependencies dependencies = new Dependencies(hardwareMap, gamepad1,gamepad2, telemetry);
        DrivebaseController drivebaseController = dependencies.createDrivebaseController();

        waitForStart();

        if (isStopRequested()) return;

        while (opModeIsActive()) {
            drivebaseController.updateState();
            dependencies.motorControl.setMotors(MotorConstants.allDrive);
            dependencies.pedroLocalizer.update();
            telemetry.addData("Rotation",dependencies.sensorControl.getLocalizerAngle());
            telemetry.update();
            if(gamepad1.triangle)
                break;
        }
    }
}
