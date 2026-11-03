package org.firstinspires.ftc.teamcode.Main.ManualOpModes;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Gamepad;

import org.firstinspires.ftc.teamcode.HardwareInterface.Gamepad.GamepadIndexValues;
import org.firstinspires.ftc.teamcode.HardwareInterface.Servo.ServoConstants;
import org.firstinspires.ftc.teamcode.Main.Dependencies;
import org.firstinspires.ftc.teamcode.Main.GlobalVariables;
import com.qualcomm.robotcore.hardware.AnalogInput;

@TeleOp
public class ManualCRAxonTest extends LinearOpMode {
    @Override
    public void runOpMode() throws InterruptedException {

        GlobalVariables.isAutonomous = false;
        Dependencies dependencies = new Dependencies(hardwareMap, gamepad1,gamepad2, telemetry);
        Gamepad currentGamepad1 = new Gamepad();
        Gamepad prevGamepad1 = new Gamepad();
        prevGamepad1.copy(currentGamepad1);
        currentGamepad1.copy(gamepad1);

        AnalogInput axonFeedback = hardwareMap.get(AnalogInput.class, "transferAnalog");

        waitForStart();

        if (isStopRequested()) return;

        while (opModeIsActive()) {

            prevGamepad1.copy(currentGamepad1);
            currentGamepad1.copy(gamepad1);
            if(gamepad1.triangle) break;
            dependencies.edgeDetection.refreshGamepadIndex(currentGamepad1,prevGamepad1);
            telemetry.addLine("Press L1 to start CR servo");

            double power = 0;

            if (gamepad1.left_bumper) {
                power = 0.5;
            } else if (gamepad1.right_bumper) {
                power = -0.5;
            }

            double voltage = axonFeedback.getVoltage();
            double position = voltage / axonFeedback.getMaxVoltage();

            telemetry.addData("Axon Servo Voltage", voltage);
            telemetry.addData("Axon Servo Position", position);

            telemetry.update();
        }
    }
}
