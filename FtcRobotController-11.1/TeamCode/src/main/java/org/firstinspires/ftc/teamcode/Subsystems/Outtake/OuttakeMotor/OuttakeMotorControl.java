package org.firstinspires.ftc.teamcode.Subsystems.Outtake.OuttakeMotor;

import org.firstinspires.ftc.teamcode.HardwareInterface.Motor.MotorConstants;
import org.firstinspires.ftc.teamcode.HardwareInterface.Motor.MotorControl;
import org.firstinspires.ftc.teamcode.Subsystems.Outtake.OuttakeConstants;
import org.firstinspires.ftc.teamcode.Subsystems.Outtake.OuttakeStates;

public class OuttakeMotorControl {
    private final MotorControl motorControl;
    private OuttakeMotorStates prevMotorStates = OuttakeMotorStates.idle;

    public OuttakeMotorControl(MotorControl motorControl) {
        this.motorControl = motorControl;
    }

    public void update() {
        if (OuttakeStates.getOuttakeMotorState() != prevMotorStates) {
            updateStates();
            prevMotorStates = OuttakeStates.getOuttakeMotorState();
        }
    }

    public void updateStates() {
        switch (OuttakeStates.getOuttakeMotorState()) {
            case forward:
                motorControl.setMotorRPM(MotorConstants.outtake1, OuttakeConstants.outtake1Speed);
                motorControl.setMotorRPM(MotorConstants.outtake2, OuttakeConstants.outtake2Speed);
                break;
            case backward:
                motorControl.setMotorRPM(MotorConstants.outtake1, -OuttakeConstants.outtake1Speed);
                motorControl.setMotorRPM(MotorConstants.outtake2, -OuttakeConstants.outtake2Speed);
                break;
            case idle:
                motorControl.setMotorRPM(MotorConstants.outtake, 0);
                break;
        }
    }
}
