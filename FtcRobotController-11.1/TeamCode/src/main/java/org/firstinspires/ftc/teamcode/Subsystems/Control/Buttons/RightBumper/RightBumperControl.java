package org.firstinspires.ftc.teamcode.Subsystems.Control.Buttons.RightBumper;

import org.firstinspires.ftc.teamcode.Subsystems.Control.ButtonStates;
import org.firstinspires.ftc.teamcode.Subsystems.Outtake.OuttakeStates;

public class RightBumperControl {
    public void update() {
        switch (ButtonStates.getRightBumperState()) {
            case toggleTurret:
                OuttakeStates.toggleTurretTracking();
                break;
            case idle:
                break;
        }
    }
}
