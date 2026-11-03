package org.firstinspires.ftc.teamcode.Subsystems.Control.Buttons.DpadRight;

import org.firstinspires.ftc.teamcode.Subsystems.Control.ButtonStates;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.IntakeStates;
import org.firstinspires.ftc.teamcode.Subsystems.SubsystemState;

public class DpadRightLogic {
    private final DpadRightControl dpadRightControl = new DpadRightControl();

    public void update() {
        if(toggleServo()) return;
    }

    private void completeAction(){
        dpadRightControl.update();
        ButtonStates.setDpadRightState(DpadRightStates.idle);
    }

    private boolean toggleServo() {
        ButtonStates.setDpadRightState(DpadRightStates.toggleServo);
        completeAction();
        return true;
    }

    private boolean intakeActive() {
        return IntakeStates.getIntakeState() == SubsystemState.Run;
    }
}