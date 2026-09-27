package org.firstinspires.ftc.teamcode.Subsystems.Control.Buttons.LeftBumper;

import org.firstinspires.ftc.teamcode.Subsystems.Control.ButtonStates;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.IntakeStates;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.LockServo.LockServoStates;
import org.firstinspires.ftc.teamcode.Subsystems.Outtake.OuttakeMotor.OuttakeMotorStates;
import org.firstinspires.ftc.teamcode.Subsystems.Outtake.OuttakeStates;

public class LeftBumperLogic {
    private final LeftBumperControl LeftBumperControl = new LeftBumperControl();

    public void update() {
        if (resetPos()) return;
        stopServoLock();
    }

    private void setPos(){
        LeftBumperControl.update();
        ButtonStates.setLeftBumperState(LeftBumperStates.idle);
    }

    private boolean resetPos() {
        if (IntakeStates.getLockServoState() != LockServoStates.idle) return false;

        ButtonStates.setLeftBumperState(LeftBumperStates.activate);
        setPos();
        return true;
    }

    private void stopServoLock() {
        ButtonStates.setLeftBumperState(LeftBumperStates.stop);
        setPos();
    }

}
