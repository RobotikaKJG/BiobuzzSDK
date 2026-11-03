package org.firstinspires.ftc.teamcode.Subsystems.Intake;

import org.firstinspires.ftc.teamcode.Subsystems.Intake.AutoIntakeTransfer.AutoIntakeTransferStates;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.IntakeMotor.IntakeMotorStates;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.LED.LEDStates;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.LockServo.LockServoStates;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.TransferMotor.TransferMotorStates;
import org.firstinspires.ftc.teamcode.Subsystems.SubsystemState;

public class IntakeStates {
    private static SubsystemState intakeState = SubsystemState.Idle;
    private static AutoIntakeTransferStates autoIntakeTransferState = AutoIntakeTransferStates.idle;
    private static IntakeMotorStates intakeMotorState = IntakeMotorStates.idle;
    private static LEDStates ledStates = LEDStates.idle;
    private static TransferMotorStates transferMotorState = TransferMotorStates.idle;
    private static LockServoStates lockServoState = LockServoStates.idle;
    private static boolean manualStop = false;

    public static void setInitialStates() {
        autoIntakeTransferState = AutoIntakeTransferStates.idle;
        intakeState = SubsystemState.Idle;
        intakeMotorState = IntakeMotorStates.idle;
        transferMotorState = TransferMotorStates.idle;
        lockServoState = LockServoStates.lock;
        manualStop = false;
    }

    public static boolean isManualStop() {
        return manualStop;
    }

    public static void setManualStop(boolean stop) {
        manualStop = stop;
    }

    public static SubsystemState getIntakeState() {
        return intakeState;
    }

    public static void setIntakeState(SubsystemState state) {
        intakeState = state;
    }

    public static AutoIntakeTransferStates getAutoIntakeTransferState() {
        return autoIntakeTransferState;
    }

    public static void setAutoIntakeTransferState(AutoIntakeTransferStates state) {
        autoIntakeTransferState = state;
    }

    public static IntakeMotorStates getIntakeMotorState() {
        return intakeMotorState;
    }

    public static void setIntakeMotorState(IntakeMotorStates state) {
        intakeMotorState = state;
    }

    public static LEDStates getLEDState() {
        return ledStates;
    }

    public static void setLEDState(LEDStates state) {
        ledStates = state;
    }

    public static TransferMotorStates getTransferMotorState() {
        return transferMotorState;
    }

    public static void setTransferMotorState(TransferMotorStates state) {
        transferMotorState = state;
    }

    public static LockServoStates getLockServoState() {
        return lockServoState;
    }

    public static void setLockServoState(LockServoStates state) {
        lockServoState = state;
    }
}