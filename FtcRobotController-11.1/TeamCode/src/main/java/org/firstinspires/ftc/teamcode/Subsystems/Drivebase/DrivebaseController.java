package org.firstinspires.ftc.teamcode.Subsystems.Drivebase;

import com.qualcomm.robotcore.hardware.Gamepad;

import org.firstinspires.ftc.teamcode.HardwareInterface.Gamepad.EdgeDetection;
import org.firstinspires.ftc.teamcode.HardwareInterface.Gamepad.GamepadIndexValues;
import org.firstinspires.ftc.teamcode.Main.ShooterTelemetry.RemoteControl;

/**
 * Runs on the drive loop (the main OpMode thread). Self-contained: keeps its own
 * gamepad snapshot and edge detection so it shares no mutable state with the
 * control or turret loops. The only hardware it touches (via {@link Drivebase})
 * is the drive motors and a cached read of the localizer heading.
 */
public class DrivebaseController {
    private final Drivebase drivebase;
    private final Gamepad gamepad1;
    private final Gamepad currentGamepad = new Gamepad();
    private final Gamepad prevGamepad = new Gamepad();
    private final EdgeDetection edgeDetection = new EdgeDetection();
    private final DrivebaseTrigger drivebaseTrigger = new DrivebaseTrigger();

    public DrivebaseController(Drivebase drivebase, Gamepad gamepad1) {
        this.drivebase = drivebase;
        this.gamepad1 = gamepad1;
    }

    public void updateState() {
        prevGamepad.copy(currentGamepad);
        currentGamepad.copy(gamepad1);
        RemoteControl.mergeInto(currentGamepad);
        edgeDetection.refreshGamepadIndex(currentGamepad, prevGamepad);

        if (edgeDetection.rising(drivebaseTrigger.getTrigger()))
            drivebase.switchDrivingMode();
        if (edgeDetection.rising(GamepadIndexValues.circle))
            drivebase.togglePathFollowing();

        drivebase.drive(DrivebaseConstants.getDriveSpeed());
    }
}
