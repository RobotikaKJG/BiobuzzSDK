package org.firstinspires.ftc.teamcode.Subsystems.Control.Buttons.LeftBumper;

import org.firstinspires.ftc.teamcode.HardwareInterface.Motor.MotorConstants;
import org.firstinspires.ftc.teamcode.HardwareInterface.Motor.MotorControl;
import org.firstinspires.ftc.teamcode.Main.GlobalVariables;
import org.firstinspires.ftc.teamcode.Subsystems.Control.ButtonStates;
import org.firstinspires.ftc.teamcode.Subsystems.Outtake.AutoCycleShoot.AutoCycleShootStates;
import org.firstinspires.ftc.teamcode.Subsystems.Outtake.OuttakeConstants;
import org.firstinspires.ftc.teamcode.Subsystems.Outtake.OuttakeMotor.OuttakeMotorStates;
import org.firstinspires.ftc.teamcode.Subsystems.Outtake.OuttakeStates;

public class LeftBumperLogic {
    private final LeftBumperControl leftBumperControl = new LeftBumperControl();
    private MotorControl motorControl;

    public LeftBumperLogic(MotorControl motorControl) {
        this.motorControl = motorControl;
    }


    public void update() {
        if(shoot()) return;
        stop();
        return;
    }

    private boolean shoot() {
        if(OuttakeStates.getAutoCycleShootState() !=  AutoCycleShootStates.idle || OuttakeStates.getMotorState() == OuttakeMotorStates.idle) return false;
        // Speed validation disabled: isCorrectSpeed() has a units mismatch
        // (getVelocity() returns ticks/sec but outtakeTargetSpeed is a power 0-1).
        // Also, this method is edge-triggered (called once per press) so a timeout won't work.
        // TODO: Fix by converting target power to expected ticks/sec using motor max velocity.
        ButtonStates.setLeftBumperState(LeftBumperStates.shoot);
        completeAction();
        return true;
    }

    private boolean isCorrectSpeed() {
        double currentSpeed = motorControl.getMotorVelocity(MotorConstants.outtake1);
        double speedDelta = Math.abs(currentSpeed - GlobalVariables.outtakeTargetSpeed);
        return speedDelta < OuttakeConstants.targetSpeedThreshold;
    }

    private void stop() {
        ButtonStates.setLeftBumperState(LeftBumperStates.stop);
        completeAction();
    }

    private void completeAction(){
        leftBumperControl.update();
        ButtonStates.setLeftBumperState(LeftBumperStates.idle);
    }
}
