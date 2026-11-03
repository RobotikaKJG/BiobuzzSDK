package org.firstinspires.ftc.teamcode.Other;

import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Gamepad;

import org.apache.commons.math3.geometry.spherical.twod.Edge;
import org.firstinspires.ftc.teamcode.HardwareInterface.Gamepad.EdgeDetection;
import org.firstinspires.ftc.teamcode.HardwareInterface.Gamepad.GamepadIndexValues;
import org.firstinspires.ftc.teamcode.Subsystems.Outtake.OuttakeStates;

import java.util.List;


@TeleOp
public class TwoMotors extends LinearOpMode {

    private double prevTime;
    static double motor1Speed = 0;
    static double motor2Speed = 0;
    static int currentStep = 2;

    @Override
    public void runOpMode() throws InterruptedException {
        List<LynxModule> allHubs = hardwareMap.getAll(LynxModule.class);

        for (LynxModule hub : allHubs) {
            hub.setBulkCachingMode(LynxModule.BulkCachingMode.AUTO);
        }

        EdgeDetection edgeDetection = new EdgeDetection();
        Gamepad prevGamepad1 = new Gamepad();
        Gamepad currentGamepad1 = new Gamepad();


        double steps[] = {0.01,0.05,0.1,0.2,0.5,1};

        boolean active = false;

        DcMotor motor = hardwareMap.get(DcMotor.class, "outtakeMotor");
        DcMotor motor2 = hardwareMap.get(DcMotor.class, "outtakeMotor2");


        waitForStart();

        if (isStopRequested()) return;

        while (opModeIsActive()) {

            telemetry.addLine("Bumpers and triggers control motor speeds");
            telemetry.addLine("Dpad up/down controls step size");
            telemetry.addLine("Square toggles motors");
            telemetry.addData("Speed step", steps[currentStep]);
            telemetry.addData("Motor 1 speed", motor1Speed);
            telemetry.addData("Motor 2 speed", motor2Speed);

            if(edgeDetection.rising(GamepadIndexValues.leftBumper))
                motor1Speed += steps[currentStep];

            if(edgeDetection.rising(GamepadIndexValues.leftTrigger))
                motor1Speed -= steps[currentStep];

            if(edgeDetection.rising(GamepadIndexValues.rightBumper))
                motor2Speed += steps[currentStep];

            if(edgeDetection.rising(GamepadIndexValues.rightTrigger))
                motor2Speed -= steps[currentStep];

            if(edgeDetection.rising(GamepadIndexValues.dpadUp) && currentStep < 5)
                currentStep ++;

            if(edgeDetection.rising(GamepadIndexValues.dpadDown) && currentStep > 0)
                currentStep --;

            if(edgeDetection.rising(GamepadIndexValues.square))
                active = !active;

            if(active){
                motor.setPower(motor1Speed);
                motor2.setPower(motor2Speed);
            }
            else{
                motor.setPower(0);
                motor2.setPower(0);
            }


            if (gamepad1.triangle) break;
            calculateLoopTime();

            prevGamepad1.copy(currentGamepad1);
            currentGamepad1.copy(gamepad1);
            edgeDetection.refreshGamepadIndex(currentGamepad1, prevGamepad1);

            telemetry.update();
        }
    }

    private void calculateLoopTime()
    {
        double currentTime = System.nanoTime() / 1_000_000.0;
        telemetry.addData("Loop time:", currentTime - prevTime);
        prevTime = currentTime;
    }
}