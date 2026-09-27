package org.firstinspires.ftc.teamcode.Subsystems.Intake;

import org.firstinspires.ftc.teamcode.Subsystems.Intake.IntakeMotor.IntakeMotorStates;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.LockServo.LockServoStates;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.Transfer2Motor.Transfer2MotorStates;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.TransferMotor.TransferMotorStates;
import org.firstinspires.ftc.teamcode.Subsystems.SubsystemState;

public class IntakeStates {
    private static SubsystemState intakeState = SubsystemState.Idle;
    private static IntakeMotorStates intakeMotorState = IntakeMotorStates.idle;
    private static LockServoStates lockServoState = LockServoStates.idle;
    private static TransferMotorStates transferMotorState = TransferMotorStates.idle;
    private static Transfer2MotorStates transfer2MotorState = Transfer2MotorStates.idle;
    public static void setInitialStates() {
        intakeState = SubsystemState.Idle;
        intakeMotorState = IntakeMotorStates.idle;
        lockServoState = LockServoStates.lock;
        transferMotorState = TransferMotorStates.idle;
        transfer2MotorState = Transfer2MotorStates.idle;
    }

    public static SubsystemState getIntakeState() {
        return intakeState;
    }

    public static void setIntakeState(SubsystemState state) {
        intakeState = state;
    }




    public static IntakeMotorStates getIntakeMotorState() {
        return intakeMotorState;
    }

    public static void setIntakeMotorState(IntakeMotorStates state) {
        intakeMotorState = state;
    }

    public static LockServoStates getLockServoState() {
        return lockServoState;
    }

    public static void setLockServoState(LockServoStates state) {
        lockServoState = state;
    }

    public static void setTransferMotorState(TransferMotorStates state) {
        transferMotorState = state;
    }
    public static TransferMotorStates getTransferMotorState() {
        return transferMotorState;
    }

    public static void setTransfer2MotorState(Transfer2MotorStates state) {
        transfer2MotorState = state;
    }
    public static Transfer2MotorStates getTransfer2MotorState() {
        return transfer2MotorState;
    }
}