package org.firstinspires.ftc.teamcode.Subsystems.Control.Buttons._Gamepad2.LeftBumperG2;

import org.firstinspires.ftc.teamcode.Main.GlobalVariables;
import org.firstinspires.ftc.teamcode.Subsystems.Control.ButtonStates;

public class LeftBumperLogicG2 {

    public void update() {
        GlobalVariables.far = true;
        completeAction();
    }

    private void completeAction(){
        ButtonStates.setLeftBumperStateG2(LeftBumperStatesG2.idle);
    }
}
