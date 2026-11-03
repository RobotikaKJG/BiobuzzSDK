package org.firstinspires.ftc.teamcode.Subsystems.Control.Buttons._Gamepad2.LeftBumperG2;

import org.firstinspires.ftc.teamcode.Subsystems.Control.ButtonStates;
import org.firstinspires.ftc.teamcode.Subsystems.Outtake.AutoCycleShoot.AutoCycleShootStates;
import org.firstinspires.ftc.teamcode.Subsystems.Outtake.OuttakeStates;

public class LeftBumperControlG2 {
    public void update() {
        switch (ButtonStates.getLeftBumperState()) {
            case shoot:
                OuttakeStates.setAutoCycleShootState(AutoCycleShootStates.recalibrate);
                break;
            case stop:
                OuttakeStates.setAutoCycleShootState(AutoCycleShootStates.stop);
                break;
            case idle:
                break;
        }
    }
}
