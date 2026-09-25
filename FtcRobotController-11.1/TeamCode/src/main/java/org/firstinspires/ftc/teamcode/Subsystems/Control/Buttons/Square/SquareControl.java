package org.firstinspires.ftc.teamcode.Subsystems.Control.Buttons.Square;

import org.firstinspires.ftc.teamcode.Subsystems.Control.ButtonStates;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.IntakeMotor.IntakeMotorStates;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.IntakeStates;
import org.firstinspires.ftc.teamcode.Subsystems.Outtake.OuttakeMotor.OuttakeMotorStates;
import org.firstinspires.ftc.teamcode.Subsystems.Outtake.OuttakeStates;

public class SquareControl {
    public void update() {
        switch (ButtonStates.getSquareState()) {
            case activate:
                activate();
                break;
            case stop:
                OuttakeStates.setOuttakeMotorState(OuttakeMotorStates.idle);
            case idle:
                break;
        }
    }

    private void activate() {
        OuttakeStates.setOuttakeMotorState(OuttakeMotorStates.forwardClose);
    }
}
