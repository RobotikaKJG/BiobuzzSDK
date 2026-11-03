package org.firstinspires.ftc.teamcode.Subsystems.Control.Buttons._Gamepad2.LeftTriggerG2;

import org.firstinspires.ftc.teamcode.Main.GlobalVariables;
import org.firstinspires.ftc.teamcode.Subsystems.Control.ButtonStates;

public class LeftTriggerLogicG2 {

    public void update() {
        GlobalVariables.far = false;
        completeAction();
    }

    private void completeAction(){
        ButtonStates.setLeftTriggerStateG2(LeftTriggerStatesG2.idle);
    }
}
