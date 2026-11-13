package org.firstinspires.ftc.teamcode.Subsystems.Intake.LockServo;

import org.firstinspires.ftc.teamcode.Subsystems.Outtake.OuttakeConstants;

import org.firstinspires.ftc.teamcode.HardwareInterface.Servo.ServoConstants;
import org.firstinspires.ftc.teamcode.HardwareInterface.Servo.ServoControl;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.IntakeStates;

public class LockServoControl {
    private final ServoControl servoControl;
    private LockServoStates prevLockServoStates = LockServoStates.idle;

    public LockServoControl(ServoControl servoControl) {
        this.servoControl = servoControl;
    }

    public void update() {
        if(IntakeStates.getLockServoState() != prevLockServoStates) {
            updateStates();
            prevLockServoStates = IntakeStates.getLockServoState();
        }

    }

    public void updateStates() {
        switch (IntakeStates.getLockServoState()) {
            case lock:
                servoControl.setServoPos(ServoConstants.lockServo, OuttakeConstants.lockServoLockedPos);
                break;
            case unlock:
                servoControl.setServoPos(ServoConstants.lockServo, OuttakeConstants.lockServoUnlockedPos);
                break;
            case idle:
                break;
        }

    }
}

