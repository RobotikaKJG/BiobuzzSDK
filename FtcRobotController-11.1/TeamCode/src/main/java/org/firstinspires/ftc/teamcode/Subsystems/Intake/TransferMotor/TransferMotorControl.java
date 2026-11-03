package org.firstinspires.ftc.teamcode.Subsystems.Intake.TransferMotor;

import org.firstinspires.ftc.teamcode.HardwareInterface.Motor.MotorConstants;
import org.firstinspires.ftc.teamcode.HardwareInterface.Motor.MotorControl;
import org.firstinspires.ftc.teamcode.Main.GlobalVariables;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.IntakeStates;

public class TransferMotorControl {
    private final MotorControl motorControl;
    private TransferMotorStates prevTransferMotorStates = TransferMotorStates.idle;

    public TransferMotorControl(MotorControl motorControl) {
        this.motorControl = motorControl;
    }

    public void update() {
        if(IntakeStates.getTransferMotorState() != prevTransferMotorStates) {
            updateStates();
            prevTransferMotorStates = IntakeStates.getTransferMotorState();
        }

    }

    public void updateStates() {
        switch (IntakeStates.getTransferMotorState()) {
            case forward:
                if (GlobalVariables.far)
                    motorControl.setMotorSpeed(MotorConstants.transfer, 0.6);
                else
                    motorControl.setMotorSpeed(MotorConstants.transfer, 1);
                break;
            case backward:
                motorControl.setMotorSpeed(MotorConstants.transfer, -1.0);
                break;
            case idle:
                motorControl.setMotorSpeed(MotorConstants.transfer, 0);
                break;
        }

    }
}
