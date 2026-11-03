
package org.firstinspires.ftc.teamcode.Subsystems.Control.Buttons.DpadRight;

import org.firstinspires.ftc.teamcode.Subsystems.Control.ButtonStates;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.IntakeMotor.IntakeMotorStates;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.IntakeStates;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.LockServo.LockServoStates;

public class DpadRightControl {
    public void update() {
        switch (ButtonStates.getDpadRightState()) {
            case toggleServo:
                toggleServo();
                break;
            case idle:
                break;
        }
    }

    private void toggleServo() {
        switch (IntakeStates.getLockServoState()) {
            case lock:
                IntakeStates.setLockServoState(LockServoStates.unlock);
                break;
            case unlock:
                IntakeStates.setLockServoState(LockServoStates.lock);
                break;
        }
    }
}
