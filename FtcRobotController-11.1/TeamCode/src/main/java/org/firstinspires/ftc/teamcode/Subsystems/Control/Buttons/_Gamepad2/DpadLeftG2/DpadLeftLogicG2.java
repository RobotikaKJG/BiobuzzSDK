package org.firstinspires.ftc.teamcode.Subsystems.Control.Buttons._Gamepad2.DpadLeftG2;

import org.firstinspires.ftc.teamcode.Subsystems.Control.ButtonStates;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.IntakeStates;
import org.firstinspires.ftc.teamcode.Subsystems.Outtake.OuttakeStates;
import org.firstinspires.ftc.teamcode.Subsystems.Outtake.TurretServo.TurretServoControl;
import org.firstinspires.ftc.teamcode.Subsystems.Outtake.TurretServo.TurretServoStates;
import org.firstinspires.ftc.teamcode.Subsystems.SubsystemState;

public class DpadLeftLogicG2 {
    private final TurretServoControl turretServoControl;

    public DpadLeftLogicG2(TurretServoControl turretServoControl) {
        this.turretServoControl = turretServoControl;
    }

    public void update() {
        if(setTurretManual()) return;
    }

    private void completeAction(){
        ButtonStates.setDpadLeftStateG2(DpadLeftStatesG2.idle);
    }

    private boolean setTurretManual() {
        turretServoControl.setManualAngleDeg(90);
        OuttakeStates.setTurretServoState(TurretServoStates.manual);
        completeAction();
        return true;
    }

    private boolean intakeActive() {
        return IntakeStates.getIntakeState() == SubsystemState.Run;
    }
}