package org.firstinspires.ftc.teamcode.Subsystems.Intake.FlowerServo;

import static org.firstinspires.ftc.teamcode.Subsystems.Intake.FlowerServo.FlowerServoStates.idle;


import org.firstinspires.ftc.teamcode.HardwareInterface.Servo.ServoConstants;
import org.firstinspires.ftc.teamcode.HardwareInterface.Servo.ServoControl;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.IntakeConstants;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.IntakeStates;

public class FlowerServoControl {
    private final ServoControl servoControl;
    private FlowerServoStates prevFlowerServoStates = idle;

    public FlowerServoControl(ServoControl servoControl) {
        this.servoControl = servoControl;
    }

    public void update() {
        if(IntakeStates.getFlowerServoState() != prevFlowerServoStates) {
            updateStates();
            prevFlowerServoStates = IntakeStates.getFlowerServoState();
        }

    }

    public void updateStates() {
        switch (IntakeStates.getFlowerServoState()) {
            case lock:
                servoControl.setServoPos(ServoConstants.flowerServo, IntakeConstants.flowerServoLockedPos);
                break;
            case unlock:
                servoControl.setServoPos(ServoConstants.flowerServo, IntakeConstants.flowerServoUnlockedPos);
                break;
            case idle:
                break;
        }

    }
}

