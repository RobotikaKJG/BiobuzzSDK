package org.firstinspires.ftc.teamcode.Subsystems.Drivebase;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;

@com.qualcomm.robotcore.eventloop.opmode.TeleOp(name="Mecanum Drive Test", group="TeleOp")
public class DriveTrainTest extends OpMode {

    private DcMotorEx frontRightMotor, frontLeftMotor, backRightMotor, backLeftMotor;

    // Conversion parameters
    private final double TICKS_PER_REVOLUTION = 1120.0; // Update based on your motor configuration
    private final double WHEEL_DIAMETER_INCHES = 4.0;     // Update based on your wheel size
    private final double WHEEL_CIRCUMFERENCE = Math.PI * WHEEL_DIAMETER_INCHES;

    // For acceleration calculations
    private double previousTime = 0;
    private double previousLinearSpeed = 0;

    @Override
    public void init() {
        // Initialize motors
        frontRightMotor = hardwareMap.get(DcMotorEx.class, "frontRightMotor");
        frontLeftMotor = hardwareMap.get(DcMotorEx.class, "frontLeftMotor");
        backRightMotor = hardwareMap.get(DcMotorEx.class, "backRightMotor");
        backLeftMotor = hardwareMap.get(DcMotorEx.class, "backLeftMotor");

        // Reverse left side motors so they drive correctly
        frontLeftMotor.setDirection(DcMotor.Direction.REVERSE);
        backLeftMotor.setDirection(DcMotor.Direction.REVERSE);

        // Set motors to brake when power is zero
        frontRightMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        frontLeftMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backRightMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);
        backLeftMotor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.BRAKE);

        // Initialize timing variables.
        previousTime = getRuntime();  // Start the timer at initialization
    }

    @Override
    public void loop() {
        // Gamepad control
        double y = -gamepad1.left_stick_y;  // Forward/backward
        double x = gamepad1.left_stick_x * 1.1;  // Strafe (adjusted for imperfect strafing)
        double rotation = gamepad1.right_stick_x; // Rotation

        // Apply dead zones
        if (Math.abs(y) < 0.05) y = 0;
        if (Math.abs(x) < 0.05) x = 0;
        if (Math.abs(rotation) < 0.05) rotation = 0;

        // Mecanum Drive Calculation
        double frontLeftPower = y + x + rotation;
        double frontRightPower = y - x - rotation;
        double backLeftPower = y - x + rotation;
        double backRightPower = y + x - rotation;

        // Normalize power values so they don't exceed 1.0
        double max = Math.max(1.0, Math.abs(frontLeftPower));
        max = Math.max(max, Math.abs(frontRightPower));
        max = Math.max(max, Math.abs(backLeftPower));
        max = Math.max(max, Math.abs(backRightPower));

        frontLeftPower /= max;
        frontRightPower /= max;
        backLeftPower /= max;
        backRightPower /= max;

        // Set power to motors
        frontLeftMotor.setPower(frontLeftPower);
        frontRightMotor.setPower(frontRightPower);
        backLeftMotor.setPower(backLeftPower);
        backRightMotor.setPower(backRightPower);

        // --- Speed and Acceleration Computations ---

        // Use one motor (frontLeftMotor) as a reference for calculating linear speed
        double currentVelocityTicksPerSec = frontLeftMotor.getVelocity(); // ticks per second
        // Convert ticks/s to rotations/s
        double rotationsPerSec = currentVelocityTicksPerSec / TICKS_PER_REVOLUTION;
        // Convert rotations/s to linear speed in inches/s
        double currentLinearSpeed = rotationsPerSec * WHEEL_CIRCUMFERENCE;

        // Calculate acceleration in inches per second squared
        double currentTime = getRuntime(); // getRuntime() returns time in seconds
        double acceleration = 0;
        if (previousTime > 0) {
            double dt = currentTime - previousTime;  // time difference in seconds
            acceleration = (currentLinearSpeed - previousLinearSpeed) / dt;
        }

        // Update previous time and linear speed for the next loop iteration
        previousTime = currentTime;
        previousLinearSpeed = currentLinearSpeed;

        // Telemetry for debugging
        telemetry.addData("Front Left Power", frontLeftPower);
        telemetry.addData("Linear Speed (in/s)", currentLinearSpeed);
        telemetry.addData("Acceleration (in/s^2)", acceleration);
        telemetry.update();
    }
}
