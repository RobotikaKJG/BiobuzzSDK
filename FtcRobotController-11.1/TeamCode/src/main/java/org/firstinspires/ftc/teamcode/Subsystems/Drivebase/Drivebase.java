package org.firstinspires.ftc.teamcode.Subsystems.Drivebase;

import com.pedropathing.follower.Follower;
import com.pedropathing.geometry.BezierCurve;
import com.pedropathing.geometry.BezierLine;
import com.pedropathing.geometry.Pose;
import com.pedropathing.paths.PathChain;
import com.qualcomm.robotcore.hardware.Gamepad;

import org.firstinspires.ftc.teamcode.HardwareInterface.Motor.MotorConstants;
import org.firstinspires.ftc.teamcode.HardwareInterface.Motor.MotorControl;
import org.firstinspires.ftc.teamcode.HardwareInterface.Sensor.SensorControl;
import org.firstinspires.ftc.teamcode.Main.Alliance;
import org.firstinspires.ftc.teamcode.Main.GlobalVariables;
import org.firstinspires.ftc.teamcode.Main.ShooterTelemetry.RemoteControl;


public class Drivebase {

    private final Gamepad gamepad1;
    private final Gamepad gamepad2;
    private final Gamepad currentGamepad = new Gamepad();
    private final MotorControl motorControl;
    private final SensorControl sensorControl;
    private final Follower follower;
    private boolean isDriverOriented = false;
    private DrivebaseState drivebaseState = DrivebaseState.TeleOp;
    private PathChain firstPath;
    private PathChain secondPath;
    private boolean pathStarted = false;

    public Drivebase(Gamepad gamepad1, Gamepad gamepad2, MotorControl motorControl, SensorControl sensorControl, Follower follower) {
        this.gamepad1 = gamepad1;
        this.gamepad2 = gamepad2;
        this.motorControl = motorControl;
        this.sensorControl = sensorControl;
        this.follower = follower;
    }

    private void selectGamepad(){
        if(GlobalVariables.slowMode)
            currentGamepad.copy(gamepad2);
        else
            currentGamepad.copy(gamepad1);
        RemoteControl.mergeInto(currentGamepad);
    }

    public void togglePathFollowing() {
        if (drivebaseState == DrivebaseState.TeleOp) {
            drivebaseState = DrivebaseState.goToGate;
            pathStarted = false;
            buildPaths();
        } else {
            cancelPathFollowing();
        }
    }

    public void cancelPathFollowing() {
        drivebaseState = DrivebaseState.TeleOp;
        isDriverOriented = false; // Return to robot-oriented as requested
        follower.breakFollowing();
        follower.setMaxPower(1.0);
        pathStarted = false;
    }

    private void buildPaths() {
        if (GlobalVariables.alliance == Alliance.Red) {
            firstPath = follower.pathBuilder()
                    .addPath(new BezierLine(follower.getPose(), new Pose(48, -13.5)))
                    .setConstantHeadingInterpolation(Math.toRadians(30))
                    .build();

            secondPath = follower.pathBuilder()
                    .addPath(new BezierLine(new Pose(48, -13.5), new Pose(55.5, -13.5)))
                    .setConstantHeadingInterpolation(Math.toRadians(30))
                    .build();
        }else {
            firstPath = follower.pathBuilder()
                    .addPath(new BezierLine(follower.getPose(), new Pose(-48, -13.5)))
                    .setConstantHeadingInterpolation(Math.toRadians(150))
                    .build();

            secondPath = follower.pathBuilder()
                    .addPath(new BezierLine(new Pose(-48, -13.5), new Pose(-55.5, -13.5)))
                    .setConstantHeadingInterpolation(Math.toRadians(150))
                    .build();
        }
    }

    public void goToGate() {
        follower.setMaxPower(1.0);
        follower.followPath(firstPath, 1.0, true);
    }

    public void openGate() {
        follower.setMaxPower(0.7);
        follower.followPath(secondPath, 0.7, true);
    }

    public void gamepadDrive(double maxSpeed) {
        selectGamepad();
        double y = -currentGamepad.left_stick_y;// * yGain;
        double x = -currentGamepad.left_stick_x;// * xGain;
        double rotation = currentGamepad.right_stick_x;// * rotationGain;

        // Snap to full power straight if within 5 degrees of vertical (top/bottom)
        if (Math.abs(y) > 0.7) {
            double angleFromVertical = Math.atan2(Math.abs(x), Math.abs(y));
            if (angleFromVertical < Math.toRadians(5)) {
                y = Math.signum(y);
                x = 0;
            }
        }

        robotOrientedGamepadDrive(y, x, rotation, maxSpeed);
    }

    public void driverOrientedGamepadDrive(double maxSpeed) {
        selectGamepad();
        double y, x, rotation;
        if (GlobalVariables.alliance == Alliance.Red) {
            y = currentGamepad.left_stick_y;
            x = currentGamepad.left_stick_x;
            rotation = currentGamepad.right_stick_x;
        }
        else {
            y = -currentGamepad.left_stick_y;
            x = -currentGamepad.left_stick_x;
            rotation = currentGamepad.right_stick_x;
        }
        driverOrientedGamepadDrive(y, x, rotation, maxSpeed);
    }

    private void robotOrientedGamepadDrive(double y, double x, double rotation, double maxSpeed) {

        double denominator = Math.max(Math.abs(y) + Math.abs(x) + Math.abs(rotation), 1);
        motorControl.setMotorSpeed(MotorConstants.allDrive, y);

        motorControl.addMotorSpeed(MotorConstants.leftDrive, rotation);
        motorControl.addMotorSpeed(MotorConstants.rightDrive, -rotation);

        motorControl.addMotorSpeed(MotorConstants.frontRightBackLeft, x);
        motorControl.addMotorSpeed(MotorConstants.frontLeftBackRight, -x);

        motorControl.divideMotorSpeed(MotorConstants.allDrive, denominator);
        motorControl.multiplyMotorSpeed(MotorConstants.allDrive, maxSpeed);
    }

    private void driverOrientedGamepadDrive(double y, double x, double rotation, double maxSpeed) {
        double botHeading = sensorControl.getLocalizerAngle();
        double rotX = x * Math.cos(botHeading) - y * Math.sin(botHeading);
        double rotY = x * Math.sin(botHeading) + y * Math.cos(botHeading);
        double denominator = Math.max(Math.abs(rotY) + Math.abs(rotX) + Math.abs(rotation), 1);

        motorControl.setMotorSpeed(MotorConstants.allDrive, -rotY);

        motorControl.addMotorSpeed(MotorConstants.leftDrive, rotation);
        motorControl.addMotorSpeed(MotorConstants.rightDrive, -rotation);

        motorControl.addMotorSpeed(MotorConstants.frontRightBackLeft, -rotX);
        motorControl.addMotorSpeed(MotorConstants.frontLeftBackRight, rotX);

        motorControl.divideMotorSpeed(MotorConstants.allDrive, denominator);
        motorControl.multiplyMotorSpeed(MotorConstants.allDrive, maxSpeed);
    }

    public void switchDrivingMode() {
        isDriverOriented = !isDriverOriented;
    }

    public boolean isPathFollowing() {
        return drivebaseState != DrivebaseState.TeleOp;
    }

    public void drive(double maxSpeed) {
        selectGamepad();
        if (isPathFollowing() && (Math.abs(currentGamepad.left_stick_y) > 0.1 ||
                Math.abs(currentGamepad.left_stick_x) > 0.1 ||
                Math.abs(currentGamepad.right_stick_x) > 0.1)) {
            cancelPathFollowing();
        }

        if (isPathFollowing()) {
            follower.update();
            if (drivebaseState == DrivebaseState.goToGate) {
                goToGate();
                drivebaseState = DrivebaseState.openGate;
                pathStarted = false;
            } else if (drivebaseState == DrivebaseState.openGate) {
                if (follower.isBusy()) pathStarted = true;
                if (pathStarted && !follower.isBusy()) {
                    openGate();
                    drivebaseState = DrivebaseState.goTeleOp;
                    pathStarted = false;
                }
            } else if (drivebaseState == DrivebaseState.goTeleOp) {
                if (follower.isBusy()) pathStarted = true;
                if (pathStarted && !follower.isBusy()) {
                    cancelPathFollowing();
                }
            }
            return;
        }

        if (isDriverOriented)
            driverOrientedGamepadDrive(maxSpeed);
        else
            gamepadDrive(maxSpeed);
    }
}