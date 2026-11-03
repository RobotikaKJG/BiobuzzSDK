
package org.firstinspires.ftc.teamcode.Subsystems.Control.Buttons._Gamepad2.DpadLeftG2;

import org.firstinspires.ftc.teamcode.Subsystems.Control.ButtonStates;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.AutoIntakeTransfer.AutoIntakeTransferStates;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.IntakeMotor.IntakeMotorStates;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.IntakeStates;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.TransferMotor.TransferMotorStates;

public class DpadLeftControlG2 {
    public void update() {
        switch (ButtonStates.getDpadUpState()) {
            case toggleMotor:
                toggleMotor();
                break;
            case idle:
                break;
        }
    }

    private void toggleMotor() {
        switch (IntakeStates.getIntakeMotorState()) {
            case forward:
                IntakeStates.setAutoIntakeTransferState(AutoIntakeTransferStates.idle);
                IntakeStates.setIntakeMotorState(IntakeMotorStates.backward);
                IntakeStates.setTransferMotorState(TransferMotorStates.backward);
                break;
            case backward:
                IntakeStates.setAutoIntakeTransferState(AutoIntakeTransferStates.activate);
                break;
            case idle:
                IntakeStates.setAutoIntakeTransferState(AutoIntakeTransferStates.idle);
                IntakeStates.setIntakeMotorState(IntakeMotorStates.backward);
                IntakeStates.setTransferMotorState(TransferMotorStates.backward);
                break;
        }
    }
}
