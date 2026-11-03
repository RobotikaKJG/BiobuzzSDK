package org.firstinspires.ftc.teamcode.Subsystems.Control.Buttons.Square;

import org.firstinspires.ftc.teamcode.Subsystems.Control.ButtonStates;
import org.firstinspires.ftc.teamcode.Subsystems.Outtake.AutoCycleShoot.AutoCycleShootStates;
import org.firstinspires.ftc.teamcode.Subsystems.Outtake.OuttakeStates;

public class SquareLogic {
    private final SquareControl squareControl = new SquareControl();

    public void update() {
        resetPos();
    }

    private void completeAction(){
        squareControl.update();
        ButtonStates.setSquareState(SquareStates.idle);
    }

    private void resetPos() {
        ButtonStates.setSquareState(SquareStates.resetPos);
        completeAction();
    }
}
