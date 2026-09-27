package org.firstinspires.ftc.teamcode.Subsystems.Intake.Transfer2Motor;

import org.firstinspires.ftc.teamcode.HardwareInterface.Motor.MotorConstants;
import org.firstinspires.ftc.teamcode.HardwareInterface.Motor.MotorControl;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.IntakeConstants;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.IntakeStates;


public class Transfer2MotorControl {
    private final MotorControl motorControl;
    private Transfer2MotorStates prevMotorStates = Transfer2MotorStates.idle;

    public Transfer2MotorControl(MotorControl motorControl) {
        this.motorControl = motorControl;
    }

    public void update() {
        if(IntakeStates.getTransfer2MotorState() != prevMotorStates) {
            updateStates();
            prevMotorStates = IntakeStates.getTransfer2MotorState();
        }

    }

    public void updateStates() {
        switch (IntakeStates.getTransfer2MotorState()) {
            case forward:
                motorControl.setMotorSpeed(MotorConstants.transfer2, IntakeConstants.transfer2Speed);
                break;
            case backward:
                motorControl.setMotorSpeed(MotorConstants.transfer2, -IntakeConstants.transfer2Speed);
                break;
            case idle:
                motorControl.setMotorSpeed(MotorConstants.transfer2, 0);
                break;
        }

    }
}

