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
import org.firstinspires.ftc.teamcode.Subsystems.Intake.AutoIntakeTransfer.AutoIntakeTransferControl;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.AutoIntakeTransfer.AutoIntakeTransferLogic;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.IntakeControl;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.IntakeMotor.IntakeMotorControl;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.LED.LEDControl;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.LED.LEDLogic;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.LockServo.LockServoControl;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.TransferMotor.TransferMotorControl;
import org.firstinspires.ftc.teamcode.Subsystems.Outtake.AutoCycleShoot.AutoCycleShootLogic;
import org.firstinspires.ftc.teamcode.Subsystems.Outtake.AutoOuttakeFarClose.AutoOuttakeFarCloseControl;
import org.firstinspires.ftc.teamcode.Subsystems.Outtake.AutoResetPos.AutoResetPosControl;
import org.firstinspires.ftc.teamcode.Main.ShooterTelemetry.ShooterLogger;
import org.firstinspires.ftc.teamcode.Main.ShooterTelemetry.ShooterTelemetryServer;
import org.firstinspires.ftc.teamcode.Subsystems.Outtake.OuttakeControl;
import org.firstinspires.ftc.teamcode.Subsystems.Outtake.OuttakeMotor.OuttakeMotorControl;
import org.firstinspires.ftc.teamcode.Subsystems.Outtake.TurretServo.TurretServoControl;

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
    public TurretServoControl turretServoControl;
    public ShooterLogger shooterLogger;
    public EdgeDetection edgeDetection = new EdgeDetection();

    public Dependencies(HardwareMap hardwareMap, Gamepad gamepad1, Gamepad gamepad2, Telemetry telemetry) {

        this.hardwareMap = hardwareMap;
        this.gamepad1 = gamepad1;
        this.gamepad2 = gamepad2;
        this.telemetry = telemetry;
//        localizer = new StandardTrackingWheelLocalizer(hardwareMap);
//        imu = hardwareMap.get(GoBildaPinpointDriver.class, "pinpointIMU");
        follower = Constants.createFollower(hardwareMap);
        pedroLocalizer = (PinpointLocalizer) follower.getPoseTracker().getLocalizer();

        // Ensure we don't accidentally reset the pose here if we're in Autonomous
        if (!GlobalVariables.isAutonomous) {
            if (GlobalVariables.wasAutonomous) {
                pedroLocalizer.setPose(GlobalVariables.lastPose);
            } else {
                pedroLocalizer.setPose(new Pose(-13, -66, 0));
            }
        }
        motorControl = new MotorControl(hardwareMap);
        sensorControl = new SensorControl(hardwareMap, edgeDetection, pedroLocalizer);
        servoControl = new ServoControl(hardwareMap);
        turretServoControl = new TurretServoControl(servoControl, sensorControl);
        shooterLogger = new ShooterLogger(motorControl, sensorControl, turretServoControl, servoControl);
        // Persistent singleton (no-op after the first OpMode); serves recorded
        // sessions + live stream to the laptop viewer on port 8765.
        ShooterTelemetryServer.ensureStarted();
    }

    public Drivebase createDrivebase() {
        return new Drivebase(gamepad1, gamepad2, motorControl, sensorControl, follower);
    }

    public DrivebaseController createDrivebaseController() {
        return new DrivebaseController(createDrivebase(), gamepad1);
    }

    ButtonControl createSubsystemControl() {
        return new ButtonControl(edgeDetection, sensorControl, motorControl, turretServoControl);
    }

    public OuttakeControl createOuttakeControl() {
        return new OuttakeControl(createOuttakeMotorControl(), createAutoCycleShootLogic(), createAutoOuttakeFarCloseControl(), createAutoResetPosControl(), motorControl);
    }

    private OuttakeMotorControl createOuttakeMotorControl() {
        return new OuttakeMotorControl(motorControl, sensorControl);
    }

    public IntakeControl createIntakeControl() {
        return new IntakeControl(createAutoIntakeTransferControl(), createAutoIntakeTransferLogic(), createIntakeMotorControl(), createLEDControl(), createLEDLogic(), createTransferMotorControl(), createLockServoControl());
    }

    private AutoIntakeTransferControl createAutoIntakeTransferControl() {
        return new AutoIntakeTransferControl();
    }

    public AutoIntakeTransferLogic createAutoIntakeTransferLogic() {
        return new AutoIntakeTransferLogic(sensorControl, gamepad1, motorControl);
    }

    private IntakeMotorControl createIntakeMotorControl() {
        return new IntakeMotorControl(motorControl);
    }

    private TransferMotorControl createTransferMotorControl() {
        return new TransferMotorControl(motorControl);
    }

    private TurretServoControl createTurretServoControl() {
        return turretServoControl;
    }

    private AutoCycleShootLogic createAutoCycleShootLogic() {
        return new AutoCycleShootLogic(sensorControl, motorControl);
    }

    private AutoOuttakeFarCloseControl createAutoOuttakeFarCloseControl() {
        return new AutoOuttakeFarCloseControl();
    }

    private LockServoControl createLockServoControl() {
        return new LockServoControl(servoControl);
    }

    private AutoResetPosControl createAutoResetPosControl() {
        return new AutoResetPosControl(sensorControl);
    }
    
    private LEDControl createLEDControl() {
        return new LEDControl(sensorControl);
    }
    
    private LEDLogic createLEDLogic() {
        return new LEDLogic(sensorControl, turretServoControl, motorControl);
    }
}
