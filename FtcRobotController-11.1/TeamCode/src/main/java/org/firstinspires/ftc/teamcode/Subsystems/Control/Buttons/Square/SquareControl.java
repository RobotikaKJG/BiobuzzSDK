package org.firstinspires.ftc.teamcode.Subsystems.Control.Buttons.Square;

import org.firstinspires.ftc.teamcode.Subsystems.Control.ButtonStates;
import org.firstinspires.ftc.teamcode.Subsystems.Outtake.AutoResetPos.AutoResetPosStates;
import org.firstinspires.ftc.teamcode.Subsystems.Outtake.OuttakeStates;

public class SquareControl {
    public void update() {
        switch (ButtonStates.getSquareState()) {
            case resetPos:
                resetPos();
                break;
            case idle:
                break;
        }
    }

    private void resetPos() {
        OuttakeStates.setAutoResetPosState(AutoResetPosStates.resetPos);
    }
}
