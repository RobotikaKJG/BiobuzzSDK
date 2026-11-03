package org.firstinspires.ftc.teamcode.Subsystems.Control.Buttons._Gamepad2.DpadDownG2;

import org.firstinspires.ftc.teamcode.Subsystems.Control.ButtonStates;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.IntakeStates;
import org.firstinspires.ftc.teamcode.Subsystems.Outtake.OuttakeStates;
import org.firstinspires.ftc.teamcode.Subsystems.Outtake.TurretServo.TurretServoControl;
import org.firstinspires.ftc.teamcode.Subsystems.Outtake.TurretServo.TurretServoStates;
import org.firstinspires.ftc.teamcode.Subsystems.SubsystemState;

public class DpadDownLogicG2 {
    private final TurretServoControl turretServoControl;

    public DpadDownLogicG2(TurretServoControl turretServoControl) {
        this.turretServoControl = turretServoControl;
    }

    public void update() {
        if(setTurretTracking()) return;
    }

    private void completeAction(){
        ButtonStates.setDpadDownStateG2(DpadDownStatesG2.idle);
    }

    private boolean setTurretTracking() {
        OuttakeStates.setTurretServoState(TurretServoStates.tracking);
        completeAction();
        return true;
    }

    private boolean intakeActive() {
        return IntakeStates.getIntakeState() == SubsystemState.Run;
    }
}