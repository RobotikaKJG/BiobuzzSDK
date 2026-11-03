package org.firstinspires.ftc.teamcode.HardwareInterface.Servo;

import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.HardwareInterface.Gamepad.EdgeDetection;
import org.firstinspires.ftc.teamcode.HardwareInterface.Gamepad.GamepadIndexValues;

import java.util.List;

/*
Claw: 0.1 closed, 0.3 open?
Intake pivot: 0.15 down,  clears sub, 0.7 up
claw pivot: 0.8-0.9 take, 0.63 place

 */

@TeleOp
public class SingleServo extends LinearOpMode {

    private double prevTime;
    private final Gamepad currentGamepad1 = new Gamepad();
    private final Gamepad prevGamepad1 = new Gamepad();
    private double currentWait = 0;
    private boolean wasIfCalled = false;
    private Servo servo;
    private final double servoMinPos = 0.62;
    private final double servoClosedPos = 0.6;
    private final double servoMaxPos = 1;

    @Override
    public void runOpMode() throws InterruptedException {


        waitForStart();
        EdgeDetection edgeDetection = new EdgeDetection();
        servo = hardwareMap.get(Servo.class, "servo");


        if (isStopRequested()) return;

        while (opModeIsActive()) {
            if(edgeDetection.rising(GamepadIndexValues.rightBumper)) {
                servo.setPosition(servoMaxPos);
//                addWaitTime(0.4);
//                wasIfCalled = false;
            }
            if(edgeDetection.rising(GamepadIndexValues.leftBumper))
                servo.setPosition(servoMinPos);
            if (gamepad1.triangle) break;
            calculateLoopTime();
            telemetry.update();
            prevGamepad1.copy(currentGamepad1);
            currentGamepad1.copy(gamepad1);
            edgeDetection.refreshGamepadIndex(currentGamepad1, prevGamepad1);
//            updateServoTiming();
        }
    }

    private void updateServoTiming() {
        if(currentWait > getSeconds()) return;
        if(wasIfCalled) return;
        wasIfCalled = true;
        servo.setPosition(servoClosedPos);
    }

    private void calculateLoopTime()
    {
        double currentTime = System.nanoTime() / 1_000_000.0;
        telemetry.addData("Loop time:", currentTime - prevTime);
        prevTime = currentTime;
    }

    private void addWaitTime(double waitTime) {
        currentWait = getSeconds() + waitTime;
    }

    private double getSeconds() {
        return System.currentTimeMillis() / 1_000.0;
    }
}