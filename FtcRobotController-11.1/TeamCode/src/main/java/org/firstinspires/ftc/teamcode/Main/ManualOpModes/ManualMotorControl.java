package org.firstinspires.ftc.teamcode.Main.ManualOpModes;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.Gamepad;

import org.firstinspires.ftc.teamcode.HardwareInterface.Gamepad.GamepadIndexValues;
import org.firstinspires.ftc.teamcode.HardwareInterface.Motor.MotorConstants;
import org.firstinspires.ftc.teamcode.Main.Dependencies;
import org.firstinspires.ftc.teamcode.Main.GlobalVariables;

@TeleOp
public class ManualMotorControl extends LinearOpMode {

    private static final double TEST_POWER = 0.2;
    private static final String[] MOTOR_NAMES = {
            "front left",
            "back left",
            "front right",
            "back right",
            "outtake 1",
            "intake",
            "transfer",
            "outtake 2"
    };

    @Override
    public void runOpMode() throws InterruptedException {

        GlobalVariables.isAutonomous = false;
        Dependencies dependencies = new Dependencies(hardwareMap, gamepad1, gamepad2, telemetry);
        Gamepad currentGamepad1 = new Gamepad();
        Gamepad prevGamepad1 = new Gamepad();
        prevGamepad1.copy(currentGamepad1);
        currentGamepad1.copy(gamepad1);
        int currentMotor = MotorConstants.frontLeft;

        waitForStart();

        if (isStopRequested()) return;

        while (opModeIsActive()) {

            prevGamepad1.copy(currentGamepad1);
            currentGamepad1.copy(gamepad1);
            if (gamepad1.triangle) break;

            dependencies.edgeDetection.refreshGamepadIndex(currentGamepad1, prevGamepad1);

            if (dependencies.edgeDetection.rising(GamepadIndexValues.square)) {
                currentMotor++;
                if (currentMotor >= MOTOR_NAMES.length) {
                    currentMotor = MotorConstants.frontLeft;
                }
            }

            dependencies.motorControl.setMotorSpeed(MotorConstants.all, 0.0);
            if (gamepad1.right_bumper) {
                dependencies.motorControl.setMotorSpeed(currentMotor, TEST_POWER);
            } else if (gamepad1.left_bumper) {
                dependencies.motorControl.setMotorSpeed(currentMotor, -TEST_POWER);
            }
            dependencies.motorControl.setMotors(MotorConstants.all);

            telemetry.addLine("Press square to cycle through motors");
            telemetry.addLine("Hold R1 for +0.2 power");
            telemetry.addLine("Hold L1 for -0.2 power");
            telemetry.addLine("Press triangle to stop");
            telemetry.addLine();
            telemetry.addLine("Currently selected motor:");
            telemetry.addLine(MOTOR_NAMES[currentMotor]);
            telemetry.addData("Power", gamepad1.right_bumper ? TEST_POWER : gamepad1.left_bumper ? -TEST_POWER : 0.0);
            telemetry.addData("Encoder", dependencies.motorControl.getMotorPosition(currentMotor));
            telemetry.update();
        }

        dependencies.motorControl.resetMotors();
    }
}
