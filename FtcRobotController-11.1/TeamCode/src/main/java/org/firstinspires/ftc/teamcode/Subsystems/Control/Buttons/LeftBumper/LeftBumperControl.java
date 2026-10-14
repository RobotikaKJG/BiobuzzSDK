package org.firstinspires.ftc.teamcode.Subsystems.Control.Buttons.LeftBumper;

import org.firstinspires.ftc.teamcode.Subsystems.Control.ButtonStates;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.IntakeMotor.IntakeMotorStates;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.IntakeStates;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.IntakeStates;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.LockServo.LockServoStates;

import org.firstinspires.ftc.teamcode.Subsystems.Intake.TransferMotor.TransferMotorStates;


public class LeftBumperControl {
    public void update() {
        switch (ButtonStates.getLeftBumperState()) {
            case activate:
                IntakeStates.setLockServoState(LockServoStates.unlock);
                break;
            case stop:
                IntakeStates.setLockServoState(LockServoStates.lock);
                break;
            case idle:
                IntakeStates.setLockServoState(LockServoStates.lock);
                break;
        }
    }
}
