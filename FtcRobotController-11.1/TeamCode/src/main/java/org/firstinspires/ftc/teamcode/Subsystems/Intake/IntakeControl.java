package org.firstinspires.ftc.teamcode.Subsystems.Intake;

import org.firstinspires.ftc.teamcode.Subsystems.Intake.IntakeMotor.IntakeMotorControl;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.IntakeMotor.IntakeMotorStates;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.LockServo.LockServoControl;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.FlowerServo.FlowerServoControl;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.TransferMotor.TransferMotorControl;
import org.firstinspires.ftc.teamcode.Subsystems.SubsystemState;

public class IntakeControl {
    private final IntakeMotorControl intakeMotorControl;
    private final LockServoControl lockServoControl;
    private final TransferMotorControl transferMotorControl;

    private final FlowerServoControl flowerServoControl;


    public IntakeControl(IntakeMotorControl intakeMotorControl, LockServoControl lockServoControl, TransferMotorControl transferMotorControl, FlowerServoControl flowerServoControl) {
        this.intakeMotorControl = intakeMotorControl;
        this.lockServoControl = lockServoControl;
        this.transferMotorControl = transferMotorControl;
        this.flowerServoControl = flowerServoControl;
    }

    public void update() {
        intakeMotorControl.update();
        lockServoControl.update();
        transferMotorControl.update();
        flowerServoControl.update();

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