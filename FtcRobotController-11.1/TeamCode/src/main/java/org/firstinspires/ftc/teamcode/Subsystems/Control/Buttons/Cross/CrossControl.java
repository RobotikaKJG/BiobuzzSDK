package org.firstinspires.ftc.teamcode.Subsystems.Control.Buttons.Cross;

import org.firstinspires.ftc.teamcode.Subsystems.Control.ButtonStates;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.IntakeMotor.IntakeMotorStates;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.IntakeStates;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.TransferMotor.TransferMotorStates;
import org.firstinspires.ftc.teamcode.Subsystems.Outtake.OuttakeMotor.OuttakeMotorStates;
import org.firstinspires.ftc.teamcode.Subsystems.Outtake.OuttakeStates;

public class CrossControl {
    public void update() {
        switch (ButtonStates.getCrossState()) {
            case activate:
                activate();
                break;
            case stop:
                IntakeStates.setTransferMotorState(TransferMotorStates.idle);
            case idle:
                break;
        }
    }

    private void activate() {
        IntakeStates.setTransferMotorState(TransferMotorStates.forward);
    }
}
