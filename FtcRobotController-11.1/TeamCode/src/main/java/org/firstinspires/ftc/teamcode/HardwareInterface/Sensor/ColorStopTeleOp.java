package org.firstinspires.ftc.teamcode.HardwareInterface.Sensor;

import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.HardwareMap;

@TeleOp(name = "ColorStopTeleOp", group = "TeleOp")
public class ColorStopTeleOp extends OpMode {

    private DcMotor rightMotor;
    private ColorSensor colorSensor;

    private int currentRed;
    private int currentGreen;
    private int currentBlue;

    @Override
    public void init() {
        // Initialize right motor
        rightMotor = hardwareMap.get(DcMotor.class, "rightMotor");

        // Initialize color sensor
        colorSensor = hardwareMap.get(ColorSensor.class, "ColorSensor");

        rightMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        telemetry.addData("Status", "Initialized");
        telemetry.update();
    }

    @Override
    public void loop() {
        // Update color sensor readings
        updateColor();

        boolean isYellow = isYellow();
        boolean isBlue = isBlue();

        if (isYellow || isBlue) {
            rightMotor.setPower(0);
            telemetry.addData("Motors", "Stopped - Detected " + (isYellow ? "Yellow" : "Blue"));
        } else {
            rightMotor.setPower(1.0);
            telemetry.addData("Motors", "Running");
        }

        telemetry.addData("Red", currentRed);
        telemetry.addData("Green", currentGreen);
        telemetry.addData("Blue", currentBlue);
        telemetry.update();
    }

    /** Updates the current color sensor readings */
    private void updateColor() {
        currentRed = colorSensor.red();
        currentGreen = colorSensor.green();
        currentBlue = colorSensor.blue();
    }

    /** Checks if the detected color is yellow */
    private boolean isYellow() {
        return (currentRed > 600 && currentGreen > 400 && currentBlue < 500);
    }

    /** Checks if the detected color is blue */
    private boolean isBlue() {
        return (currentBlue > 300 && currentRed < 300 && currentGreen < 700);
    }
}
