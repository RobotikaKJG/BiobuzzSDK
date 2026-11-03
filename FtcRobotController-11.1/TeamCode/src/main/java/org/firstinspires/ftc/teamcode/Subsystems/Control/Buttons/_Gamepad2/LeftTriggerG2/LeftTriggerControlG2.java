package org.firstinspires.ftc.teamcode.Subsystems.Control.Buttons._Gamepad2.LeftTriggerG2;

import org.firstinspires.ftc.teamcode.Subsystems.Control.ButtonStates;
import org.firstinspires.ftc.teamcode.Subsystems.Outtake.AutoCycleShoot.AutoCycleShootStates;
import org.firstinspires.ftc.teamcode.Subsystems.Outtake.AutoOuttakeFarClose.AutoOuttakeFarCloseStates;
import org.firstinspires.ftc.teamcode.Subsystems.Outtake.OuttakeMotor.OuttakeMotorStates;
import org.firstinspires.ftc.teamcode.Subsystems.Outtake.OuttakeStates;

public class LeftTriggerControlG2 {

    public LeftTriggerControlG2() {
    }

    public void update() {
        switch(ButtonStates.getLeftTriggerState()){
            case toggleOuttake:
                toggleOuttake();
                break;
            case idle:
                break;
        }
    }

    private void toggleOuttake() {
        switch (OuttakeStates.getMotorState()){
            case idle:
                OuttakeStates.setAutoOuttakeFarCloseState(AutoOuttakeFarCloseStates.cycle);
                break;
            case forwardFar:
                stopShooter();
                break;
            case forwardClose:
                stopShooter();
                break;
        }
    }

    private void stopShooter() {
        OuttakeStates.setMotorState(OuttakeMotorStates.idle);
        OuttakeStates.setAutoOuttakeFarCloseState(AutoOuttakeFarCloseStates.idle);
        if (!needToTurnOff()) return;
        OuttakeStates.setAutoCycleShootState(AutoCycleShootStates.stop);
    }

    private boolean needToTurnOff() {
        return OuttakeStates.getAutoCycleShootState() == AutoCycleShootStates.activate || OuttakeStates.getAutoCycleShootState() == AutoCycleShootStates.turnTransfer;
    }
}