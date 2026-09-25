package org.firstinspires.ftc.teamcode.Subsystems.Intake.TransferMotor;

import org.firstinspires.ftc.teamcode.HardwareInterface.Motor.MotorConstants;
import org.firstinspires.ftc.teamcode.HardwareInterface.Motor.MotorControl;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.IntakeConstants;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.IntakeStates;

public class TransferMotorControl {
    private final MotorControl motorControl;
    private TransferMotorStates prevMotorStates = TransferMotorStates.idle;

    public TransferMotorControl(MotorControl motorControl) {
        this.motorControl = motorControl;
    }

    public void update() {
        if(IntakeStates.getTransferMotorState() != prevMotorStates) {
            updateStates();
            prevMotorStates = IntakeStates.getTransferMotorState();
        }

    }

    public void updateStates() {
        switch (IntakeStates.getTransferMotorState()) {
            case forward:
                motorControl.setMotorSpeed(MotorConstants.transfer, IntakeConstants.transferSpeed);
                break;
            case backward:
                motorControl.setMotorSpeed(MotorConstants.transfer, -IntakeConstants.transferSpeed);
                break;
            case idle:
                motorControl.setMotorSpeed(MotorConstants.transfer, 0);
                break;
        }

    }
}
