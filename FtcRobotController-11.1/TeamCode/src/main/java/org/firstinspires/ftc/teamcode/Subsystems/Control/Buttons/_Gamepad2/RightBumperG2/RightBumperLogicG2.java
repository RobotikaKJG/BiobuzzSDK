package org.firstinspires.ftc.teamcode.Subsystems.Control.Buttons._Gamepad2.RightBumperG2;

import org.firstinspires.ftc.teamcode.Main.GlobalVariables;
import org.firstinspires.ftc.teamcode.Subsystems.Control.ButtonStates;

public class RightBumperLogicG2 {

    public void update() {
        GlobalVariables.rpmOffset += 20;
        completeAction();
    }

    private void completeAction(){
        ButtonStates.setRightBumperStateG2(RightBumperStatesG2.idle);
    }
}
