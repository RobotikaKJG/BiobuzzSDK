package org.firstinspires.ftc.teamcode.Subsystems.Control.Buttons.LeftBumper;

import org.firstinspires.ftc.teamcode.Subsystems.Control.ButtonStates;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.IntakeMotor.IntakeMotorStates;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.IntakeStates;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.IntakeStates;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.LockServo.LockServoStates;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.Transfer2Motor.Transfer2MotorStates;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.TransferMotor.TransferMotorStates;


public class LeftBumperControl {
    public void update() {
        switch (ButtonStates.getLeftBumperState()) {
            case activate:
                activate();
                IntakeStates.setIntakeMotorState(IntakeMotorStates.forward);
                IntakeStates.setTransferMotorState(TransferMotorStates.forward);
                IntakeStates.setTransfer2MotorState(Transfer2MotorStates.forward);
                break;
            case stop:
                IntakeStates.setLockServoState(LockServoStates.lock);
                IntakeStates.setIntakeMotorState(IntakeMotorStates.idle);
                IntakeStates.setTransferMotorState(TransferMotorStates.idle);
                IntakeStates.setTransfer2MotorState(Transfer2MotorStates.idle);
                    break;
            case idle:
                break;
        }
    }

    private void activate() {
        IntakeStates.setLockServoState(LockServoStates.unlock);
    }
}
