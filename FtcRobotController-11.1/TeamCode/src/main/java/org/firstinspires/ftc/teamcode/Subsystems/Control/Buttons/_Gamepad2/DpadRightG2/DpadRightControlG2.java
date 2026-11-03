
package org.firstinspires.ftc.teamcode.Subsystems.Control.Buttons._Gamepad2.DpadRightG2;

import org.firstinspires.ftc.teamcode.Subsystems.Control.ButtonStates;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.IntakeStates;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.LockServo.LockServoStates;

public class DpadRightControlG2 {
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
