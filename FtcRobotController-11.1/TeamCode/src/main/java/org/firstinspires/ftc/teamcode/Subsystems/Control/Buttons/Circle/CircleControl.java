package org.firstinspires.ftc.teamcode.Subsystems.Control.Buttons.Circle;

import org.firstinspires.ftc.teamcode.Subsystems.Control.ButtonStates;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.IntakeStates;

public class CircleControl {
    public void update() {
        switch (ButtonStates.getCircleState()) {
            case intakeOn:
                break;
            case intakeOff:
                break;
            case idle:
                break;
        }
    }
}
