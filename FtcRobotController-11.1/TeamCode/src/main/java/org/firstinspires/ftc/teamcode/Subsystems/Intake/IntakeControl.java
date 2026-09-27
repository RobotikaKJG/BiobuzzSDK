package org.firstinspires.ftc.teamcode.Subsystems.Intake;

import org.firstinspires.ftc.teamcode.Subsystems.Intake.IntakeMotor.IntakeMotorControl;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.IntakeMotor.IntakeMotorStates;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.LockServo.LockServoControl;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.Transfer2Motor.Transfer2MotorControl;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.TransferMotor.TransferMotorControl;
import org.firstinspires.ftc.teamcode.Subsystems.SubsystemState;

public class IntakeControl {
    private final IntakeMotorControl intakeMotorControl;
    private final LockServoControl lockServoControl;
    private final TransferMotorControl transferMotorControl;
    private final Transfer2MotorControl transfer2MotorControl;

    public IntakeControl(IntakeMotorControl intakeMotorControl, LockServoControl lockServoControl, TransferMotorControl transferMotorControl, Transfer2MotorControl transfer2MotorControl) {
        this.intakeMotorControl = intakeMotorControl;
        this.lockServoControl = lockServoControl;
        this.transferMotorControl = transferMotorControl;
        this.transfer2MotorControl = transfer2MotorControl;
    }

    public void update() {
        intakeMotorControl.update();
        lockServoControl.update();
        transferMotorControl.update();
        transfer2MotorControl.update();

        updateIntakeState();
    }

    private void updateIntakeState(){
        if(intakeActive())
            IntakeStates.setIntakeState(SubsystemState.Run);
        else
            IntakeStates.setIntakeState(SubsystemState.Idle);
    }

    private boolean intakeActive() {
        return IntakeStates.getIntakeState() != SubsystemState.Idle;
    }
}