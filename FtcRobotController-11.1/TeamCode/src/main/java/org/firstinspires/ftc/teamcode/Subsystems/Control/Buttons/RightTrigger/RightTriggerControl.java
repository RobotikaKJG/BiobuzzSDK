package org.firstinspires.ftc.teamcode.Subsystems.Control.Buttons.RightTrigger;

import org.firstinspires.ftc.teamcode.Subsystems.Control.ButtonStates;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.IntakeMotor.IntakeMotorStates;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.IntakeStates;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.LockServo.LockServoStates;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.Transfer2Motor.Transfer2MotorStates;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.TransferMotor.TransferMotorStates;
import org.firstinspires.ftc.teamcode.Subsystems.Outtake.OuttakeStates;

public class RightTriggerControl {
    public void update() {
        switch (ButtonStates.getRightTriggerState()) {
            case forward:
                IntakeStates.setIntakeMotorState(IntakeMotorStates.forward);
                IntakeStates.setTransferMotorState(TransferMotorStates.forward);
                IntakeStates.setTransfer2MotorState(Transfer2MotorStates.forward);
                break;
            case stop:
                IntakeStates.setIntakeMotorState(IntakeMotorStates.idle);
                IntakeStates.setTransferMotorState(TransferMotorStates.idle);
                IntakeStates.setTransfer2MotorState(Transfer2MotorStates.idle);
                  break;
            case idle:
                break;
        }
    }
}
