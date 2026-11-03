package org.firstinspires.ftc.teamcode.Subsystems.Control.Buttons.DpadUp;

import org.firstinspires.ftc.teamcode.Subsystems.Control.ButtonStates;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.IntakeMotor.IntakeMotorStates;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.IntakeStates;
import org.firstinspires.ftc.teamcode.Subsystems.SubsystemState;

public class DpadUpLogic {
    private final DpadUpControl dpadUpControl = new DpadUpControl();

    public void update() {
        if(toggleMotor()) return;
    }

    private void completeAction(){
        dpadUpControl.update();
        ButtonStates.setDpadUpState(DpadUpStates.idle);
    }

    private boolean toggleMotor() {
        ButtonStates.setDpadUpState(DpadUpStates.toggleMotor);
        completeAction();
        return true;
    }

    private boolean intakeActive() {
        return IntakeStates.getIntakeState() == SubsystemState.Run;
    }
}