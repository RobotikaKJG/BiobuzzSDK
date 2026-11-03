package org.firstinspires.ftc.teamcode.Subsystems.Control.Buttons._Gamepad2.RightBumperG2;

import org.firstinspires.ftc.teamcode.Subsystems.Control.ButtonStates;
import org.firstinspires.ftc.teamcode.Subsystems.Outtake.OuttakeStates;

public class RightBumperControlG2 {
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
