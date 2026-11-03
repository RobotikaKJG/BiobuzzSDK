package org.firstinspires.ftc.teamcode.Subsystems.Control.Buttons._Gamepad2.RightTriggerG2;

import org.firstinspires.ftc.teamcode.Main.GlobalVariables;
import org.firstinspires.ftc.teamcode.Subsystems.Control.ButtonStates;

public class RightTriggerLogicG2 {

    public void update() {
        GlobalVariables.rpmOffset -= 20;
        completeAction();
    }

    private void completeAction(){
        ButtonStates.setRightTriggerStateG2(RightTriggerStatesG2.idle);
    }
}
