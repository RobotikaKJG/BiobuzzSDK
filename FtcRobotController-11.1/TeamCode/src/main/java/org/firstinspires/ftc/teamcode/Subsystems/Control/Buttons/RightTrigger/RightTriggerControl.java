package org.firstinspires.ftc.teamcode.Subsystems.Control.Buttons.RightTrigger;

import org.firstinspires.ftc.teamcode.Subsystems.Control.ButtonStates;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.AutoIntakeTransfer.AutoIntakeTransferStates;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.IntakeMotor.IntakeMotorStates;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.IntakeStates;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.LED.LEDStates;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.LockServo.LockServoStates;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.TransferMotor.TransferMotorStates;
import org.firstinspires.ftc.teamcode.Subsystems.Outtake.AutoCycleShoot.AutoCycleShootStates;
import org.firstinspires.ftc.teamcode.Subsystems.Outtake.OuttakeStates;

public class RightTriggerControl {
    public void update() {
        switch (ButtonStates.getRightTriggerState()) {
            case forward:
                IntakeStates.setManualStop(false);
                IntakeStates.setAutoIntakeTransferState(AutoIntakeTransferStates.activate);
                IntakeStates.setLEDState(LEDStates.intakeNo);
                break;
            case stop:
                IntakeStates.setManualStop(true);
                IntakeStates.setAutoIntakeTransferState(AutoIntakeTransferStates.stop);
                IntakeStates.setLockServoState(LockServoStates.lock);
                OuttakeStates.setAutoCycleShootState(AutoCycleShootStates.idle);
                break;
            case idle:
                break;
        }
    }
}
