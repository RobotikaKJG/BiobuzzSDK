package org.firstinspires.ftc.teamcode.Subsystems.Intake.AutoIntakeTransfer;

import org.firstinspires.ftc.teamcode.HardwareInterface.Motor.MotorConstants;
import org.firstinspires.ftc.teamcode.HardwareInterface.Motor.MotorControl;
import org.firstinspires.ftc.teamcode.Main.GlobalVariables;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.IntakeMotor.IntakeMotorStates;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.IntakeStates;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.LockServo.LockServoStates;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.TransferMotor.TransferMotorStates;
import org.firstinspires.ftc.teamcode.Subsystems.Outtake.OuttakeMotor.OuttakeMotorStates;
import org.firstinspires.ftc.teamcode.Subsystems.Outtake.OuttakeStates;

public class AutoIntakeTransferControl {
    private AutoIntakeTransferStates prevAutoIntakeTransferState = AutoIntakeTransferStates.idle;

    private static final double FAR_MIN_OUTTAKE_VELOCITY = 2080.0;

    public AutoIntakeTransferControl() {
    }

    public void update() {
        if(IntakeStates.getAutoIntakeTransferState() != prevAutoIntakeTransferState) {
            updateStates();
            prevAutoIntakeTransferState = IntakeStates.getAutoIntakeTransferState();
        }
    }

    public void updateStates() {
        switch (IntakeStates.getAutoIntakeTransferState()) {
            case activate:
                IntakeStates.setIntakeMotorState(IntakeMotorStates.forward);
                IntakeStates.setTransferMotorState(TransferMotorStates.forward);
                break;
            case stopTransfer:
                IntakeStates.setTransferMotorState(TransferMotorStates.idle);
                break;
            case stop:
                IntakeStates.setIntakeMotorState(IntakeMotorStates.idle);
                IntakeStates.setTransferMotorState(TransferMotorStates.idle);
                break;
            case idle:
                break;
        }
    }
}