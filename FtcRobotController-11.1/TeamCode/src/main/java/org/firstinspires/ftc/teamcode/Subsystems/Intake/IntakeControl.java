package org.firstinspires.ftc.teamcode.Subsystems.Intake;

import org.firstinspires.ftc.teamcode.Subsystems.Intake.AutoIntakeTransfer.AutoIntakeTransferControl;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.AutoIntakeTransfer.AutoIntakeTransferLogic;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.IntakeMotor.IntakeMotorControl;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.IntakeMotor.IntakeMotorStates;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.LED.LEDControl;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.LED.LEDLogic;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.LockServo.LockServoControl;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.TransferMotor.TransferMotorControl;
import org.firstinspires.ftc.teamcode.Subsystems.SubsystemState;

public class IntakeControl {
    private final AutoIntakeTransferControl autoIntakeTransferControl;
    private final AutoIntakeTransferLogic autoIntakeTransferLogic;
    private final IntakeMotorControl intakeMotorControl;
    private final LEDControl ledControl;
    private final LEDLogic ledLogic;
    private final TransferMotorControl transferMotorControl;
    private final LockServoControl lockServoControl;

    public IntakeControl(AutoIntakeTransferControl autoIntakeTransferControl, AutoIntakeTransferLogic autoIntakeTransferLogic, IntakeMotorControl intakeMotorControl, LEDControl ledControl, LEDLogic ledLogic, TransferMotorControl transferMotorControl, LockServoControl lockServoControl) {
        this.autoIntakeTransferControl = autoIntakeTransferControl;
        this.autoIntakeTransferLogic = autoIntakeTransferLogic;
        this.intakeMotorControl = intakeMotorControl;
        this.ledControl = ledControl;
        this.ledLogic = ledLogic;
        this.transferMotorControl = transferMotorControl;
        this.lockServoControl = lockServoControl;
    }

    public void update() {
        autoIntakeTransferControl.update();
        autoIntakeTransferLogic.update();
        intakeMotorControl.update();
        ledControl.update();
        ledLogic.update();
        transferMotorControl.update();
        lockServoControl.update();

        updateIntakeState();
    }

    private void updateIntakeState(){
        if(intakeActive())
            IntakeStates.setIntakeState(SubsystemState.Run);
        else
            IntakeStates.setIntakeState(SubsystemState.Idle);
    }

    private boolean intakeActive() {
        return IntakeStates.getIntakeMotorState() != IntakeMotorStates.idle;
    }
}