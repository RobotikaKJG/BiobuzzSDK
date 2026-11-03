package org.firstinspires.ftc.teamcode.Main;

import com.qualcomm.robotcore.hardware.Gamepad;
import org.firstinspires.ftc.teamcode.HardwareInterface.Gamepad.EdgeDetection;
import org.firstinspires.ftc.teamcode.HardwareInterface.Gamepad.GamepadIndexValues;
import org.firstinspires.ftc.teamcode.HardwareInterface.Motor.MotorConstants;
import org.firstinspires.ftc.teamcode.HardwareInterface.Motor.MotorControl;
import org.firstinspires.ftc.teamcode.HardwareInterface.Sensor.GoBildaIndicator;
import org.firstinspires.ftc.teamcode.HardwareInterface.Sensor.SensorControl;
import org.firstinspires.ftc.teamcode.Main.ShooterTelemetry.RemoteControl;
import org.firstinspires.ftc.teamcode.Main.ShooterTelemetry.ShooterLogger;
import org.firstinspires.ftc.teamcode.Subsystems.Control.ButtonStates;
import org.firstinspires.ftc.teamcode.Subsystems.Control.ButtonControl;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.IntakeControl;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.IntakeStates;
import org.firstinspires.ftc.teamcode.Subsystems.Outtake.OuttakeControl;
import org.firstinspires.ftc.teamcode.Subsystems.Outtake.OuttakeStates;

/**
 * Body of the CONTROL loop (runs on {@link ControlThread}). Handles every
 * non-drive, non-turret subsystem: gamepad edge detection, LED, buttons, intake,
 * outtake, and the single batched {@code setMotors(notDrive)} write (motors 4..7).
 *
 * The drivebase runs on the main OpMode thread (DrivebaseController, motors 0..3);
 * the turret + localizer run on {@link TurretThread}. This class therefore no
 * longer touches the drive motors, the turret servos, or the localizer.
 */
public class IterativeController {
    private final MotorControl motorControl;
    private final Gamepad gamepad1;
    private final Gamepad gamepad2;
    private final Gamepad currentGamepad1 = new Gamepad();
    private final Gamepad prevGamepad1 = new Gamepad();
    private final Gamepad currentGamepad2 = new Gamepad();
    private final Gamepad prevGamepad2 = new Gamepad();
    private final EdgeDetection edgeDetection;
    private final ButtonControl buttonControl;
    private final OuttakeControl outtakeControl;
    private final IntakeControl intakeControl;
    private final SensorControl sensorControl;
    private final ShooterLogger shooterLogger;
    private GoBildaIndicator.Color lastColor = null;
    private boolean isLimelightRecalibrating = false;

    public IterativeController(Dependencies dependencies) {
        gamepad1 = dependencies.gamepad1;
        gamepad2 = dependencies.gamepad2;
        edgeDetection = dependencies.edgeDetection;
        motorControl = dependencies.motorControl;
        currentGamepad1.copy(this.gamepad1);
        prevGamepad1.copy(currentGamepad1);
        currentGamepad2.copy(this.gamepad2);
        prevGamepad2.copy(currentGamepad2);
        buttonControl = dependencies.createSubsystemControl();
        outtakeControl = dependencies.createOuttakeControl();
        intakeControl = dependencies.createIntakeControl();
        sensorControl = dependencies.sensorControl;
        shooterLogger = dependencies.shooterLogger;

        sensorControl.initLimelight(0);

        IntakeStates.setInitialStates();
        OuttakeStates.setInitialStates();
        ButtonStates.setInitialStates();
    }

    public void TeleOp() {
        updateCommonValues();

        if (edgeDetection.rising(GamepadIndexValues.rightStickButton))
            GlobalVariables.far = !GlobalVariables.far;

        if (edgeDetection.rising(GamepadIndexValues.dpadLeft)) {
            isLimelightRecalibrating = true;
        }

        if (isLimelightRecalibrating) {
            if (sensorControl.resetLocalizerWithLimelight()) {
                isLimelightRecalibrating = false;
            }
        }

        buttonControl.update();
        intakeControl.update();
        outtakeControl.update();

        // Drive motors (0..3) are written by the drive loop; write everything else here.
        motorControl.setMotors(MotorConstants.notDrive);

        // Shooter telemetry: sample after all writes so velocities/states reflect this
        // iteration. Reads hit this loop's bulk-cache snapshot; non-blocking, never throws.
        shooterLogger.sample();
    }

    private void updateCommonValues() {
        prevGamepad1.copy(currentGamepad1);
        currentGamepad1.copy(gamepad1);
        RemoteControl.mergeInto(currentGamepad1);
        edgeDetection.refreshGamepad1Index(currentGamepad1, prevGamepad1);

        prevGamepad2.copy(currentGamepad2);
        currentGamepad2.copy(gamepad2);
        edgeDetection.refreshGamepad2Index(currentGamepad2, prevGamepad2);
        // localizer.update() + heading reset moved to TurretThread (owns the localizer).
    }
}
