package org.firstinspires.ftc.teamcode.Subsystems.Control.Buttons.RightBumper;

import org.firstinspires.ftc.teamcode.Subsystems.Control.ButtonStates;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.IntakeStates;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.FlowerServo.FlowerServoStates;



public class RightBumperControl {
    public void update() {
        switch (ButtonStates.getRightBumperState()) {
            case activate:
                IntakeStates.setFlowerServoState(FlowerServoStates.unlock);
                break;
            case stop:
                IntakeStates.setFlowerServoState(FlowerServoStates.lock);
                break;
            case idle:
                IntakeStates.setFlowerServoState(FlowerServoStates.lock);
                break;
        }
    }
}
