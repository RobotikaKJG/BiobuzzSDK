package org.firstinspires.ftc.teamcode.Subsystems.Control.Buttons.LeftTrigger;

import org.firstinspires.ftc.teamcode.Subsystems.Control.ButtonStates;

public class LeftTriggerLogic {
    private final LeftTriggerControl leftTriggerControl = new LeftTriggerControl();

    public LeftTriggerLogic() {
    }

    public void update() {
        if(toggleOuttake()) return;
//        stopOuttake();
    }

    private void completeAction(){
        leftTriggerControl.update();
        ButtonStates.setLeftTriggerState(LeftTriggerStates.idle);
    }

    private boolean toggleOuttake() {
//        if(OuttakeStates.getMotorState() != OuttakeMotorStates.idle) return false;
        ButtonStates.setLeftTriggerState(LeftTriggerStates.toggleOuttake);
        completeAction();
        return true;
    }

//    private void stopOuttake() {
//        ButtonStates.setLeftTriggerState(LeftTriggerStates.stopOuttake);
//        completeAction();
//    }
}