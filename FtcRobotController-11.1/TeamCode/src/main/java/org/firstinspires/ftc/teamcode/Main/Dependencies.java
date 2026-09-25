package org.firstinspires.ftc.teamcode.Main;

import com.pedropathing.follower.Follower;
import com.pedropathing.ftc.localization.localizers.PinpointLocalizer;
import com.pedropathing.geometry.Pose;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.HardwareInterface.Gamepad.EdgeDetection;
import org.firstinspires.ftc.teamcode.HardwareInterface.Motor.MotorControl;
import org.firstinspires.ftc.teamcode.HardwareInterface.Sensor.SensorControl;
import org.firstinspires.ftc.teamcode.HardwareInterface.Servo.ServoControl;
import org.firstinspires.ftc.teamcode.PedroPathing.Constants;
import org.firstinspires.ftc.teamcode.Subsystems.Control.ButtonControl;
import org.firstinspires.ftc.teamcode.Subsystems.Drivebase.Drivebase;
import org.firstinspires.ftc.teamcode.Subsystems.Drivebase.DrivebaseController;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.IntakeControl;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.IntakeMotor.IntakeMotorControl;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.LockServo.LockServoControl;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.TransferMotor.TransferMotorControl;
import org.firstinspires.ftc.teamcode.Subsystems.Outtake.OuttakeControl;
import org.firstinspires.ftc.teamcode.Subsystems.Outtake.OuttakeMotor.OuttakeMotorControl;

public class Dependencies {
    public final HardwareMap hardwareMap;
    public final Gamepad gamepad1;
    public final Gamepad gamepad2;
    public final Telemetry telemetry;
    public Follower follower;
    public PinpointLocalizer pedroLocalizer;
    public MotorControl motorControl;
    public SensorControl sensorControl;
    public ServoControl servoControl;
    public EdgeDetection edgeDetection = new EdgeDetection();

    public Dependencies(HardwareMap hardwareMap, Gamepad gamepad1, Gamepad gamepad2, Telemetry telemetry) {

        this.hardwareMap = hardwareMap;
        this.gamepad1 = gamepad1;
        this.gamepad2 = gamepad2;
        this.telemetry = telemetry;
        follower = Constants.createFollower(hardwareMap);
        pedroLocalizer = (PinpointLocalizer) follower.getPoseTracker().getLocalizer();

        // Ensure we don't accidentally reset the pose here if we're in Autonomous
        if (!GlobalVariables.isAutonomous) {
            if (GlobalVariables.wasAutonomous) {
                pedroLocalizer.setPose(GlobalVariables.lastPose);
            } else {
                pedroLocalizer.setPose(new Pose(0, 0, 0));
            }
        }
        motorControl = new MotorControl(hardwareMap);
        sensorControl = new SensorControl(hardwareMap, edgeDetection, pedroLocalizer);
        servoControl = new ServoControl(hardwareMap);
    }

    public Drivebase createDrivebase() {
        return new Drivebase(gamepad1, gamepad2, motorControl, sensorControl, follower);
    }

    public DrivebaseController createDrivebaseController() {
        return new DrivebaseController(createDrivebase(), gamepad1);
    }

    ButtonControl createSubsystemControl() {
        return new ButtonControl(edgeDetection, sensorControl, motorControl);
    }

    public OuttakeControl createOuttakeControl() {
        return new OuttakeControl(createOuttakeMotorControl());
    }

    private OuttakeMotorControl createOuttakeMotorControl() {
        return new OuttakeMotorControl(motorControl);
    }

    public IntakeControl createIntakeControl() {
        return new IntakeControl(createIntakeMotorControl(), createLockServoControl(), createTransferMotorControl());
    }

    private IntakeMotorControl createIntakeMotorControl() {
        return new IntakeMotorControl(motorControl);
    }

    private TransferMotorControl createTransferMotorControl() {
        return new TransferMotorControl(motorControl);
    }

    private LockServoControl createLockServoControl() {
        return new LockServoControl(servoControl);
    }
}
