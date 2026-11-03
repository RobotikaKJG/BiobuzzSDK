package org.firstinspires.ftc.teamcode.Subsystems.Intake.IntakeMotor;

import org.firstinspires.ftc.teamcode.HardwareInterface.Motor.MotorConstants;
import org.firstinspires.ftc.teamcode.HardwareInterface.Motor.MotorControl;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.IntakeStates;

public class IntakeMotorControl {
    private final MotorControl motorControl;
    private IntakeMotorStates prevMotorStates = IntakeMotorStates.idle;

    public IntakeMotorControl(MotorControl motorControl) {
        this.motorControl = motorControl;
    }

    public void update() {
        if(IntakeStates.getIntakeMotorState() != prevMotorStates) {
            updateStates();
            prevMotorStates = IntakeStates.getIntakeMotorState();
        }

    }

    public void updateStates() {
        switch (IntakeStates.getIntakeMotorState()) {
            case forward:
                motorControl.setMotorSpeed(MotorConstants.intake, 0.8);  //was 1.0
                break;
            case backward:
                motorControl.setMotorSpeed(MotorConstants.intake, -0.8);  // was -1.0
                break;
            case idle:
                motorControl.setMotorSpeed(MotorConstants.intake, 0);
                break;
        }

    }
}
