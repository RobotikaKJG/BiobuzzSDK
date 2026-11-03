package org.firstinspires.ftc.teamcode.Subsystems.Control.Buttons._Gamepad2.RightTriggerG2;

import org.firstinspires.ftc.teamcode.Subsystems.Control.ButtonStates;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.AutoIntakeTransfer.AutoIntakeTransferStates;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.IntakeStates;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.LockServo.LockServoStates;
import org.firstinspires.ftc.teamcode.Subsystems.Outtake.AutoCycleShoot.AutoCycleShootStates;
import org.firstinspires.ftc.teamcode.Subsystems.Outtake.OuttakeStates;

public class RightTriggerControlG2 {
    public void update() {
        switch (ButtonStates.getRightTriggerState()) {
            case forward:
                IntakeStates.setAutoIntakeTransferState(AutoIntakeTransferStates.activate);
                break;
            case stop:
                IntakeStates.setAutoIntakeTransferState(AutoIntakeTransferStates.stop);
                IntakeStates.setLockServoState(LockServoStates.lock);
                OuttakeStates.setAutoCycleShootState(AutoCycleShootStates.idle);
                break;
            case idle:
                break;
        }
    }
}
