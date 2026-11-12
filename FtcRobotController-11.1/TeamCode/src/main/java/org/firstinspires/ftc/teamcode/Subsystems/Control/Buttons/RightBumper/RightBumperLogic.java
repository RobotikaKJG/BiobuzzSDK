package org.firstinspires.ftc.teamcode.Subsystems.Control.Buttons.RightBumper;

import org.firstinspires.ftc.teamcode.Subsystems.Control.ButtonStates;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.IntakeStates;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.FlowerServo.FlowerServoStates;


public class RightBumperLogic {
    private final RightBumperControl RightBumperControl = new RightBumperControl();

    public void update() {
        if (resetPos()) return;
        stopServoLock();
    }


    private void completeAction(){
        RightBumperControl.update();
        ButtonStates.setRightBumperState(RightBumperStates.idle);
    }

    private boolean resetPos() {
        if (IntakeStates.getFlowerServoState() == FlowerServoStates.unlock) return false;

        ButtonStates.setRightBumperState(RightBumperStates.activate);
        completeAction();
        return true;
    }

    private void stopServoLock() {
        ButtonStates.setRightBumperState(RightBumperStates.stop);
        completeAction();
    }

}
