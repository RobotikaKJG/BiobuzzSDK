package org.firstinspires.ftc.teamcode.HardwareInterface.Servo;

import com.qualcomm.robotcore.hardware.AnalogInput;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.teamcode.Subsystems.Intake.IntakeConstants;
import org.firstinspires.ftc.teamcode.Subsystems.Outtake.OuttakeConstants;

public class ServoControl {

    private final HardwareMap hardwareMap;
    private Servo[] servos;

    private double[] lastWrittenPos;
    private static final double SERVO_POS_EPSILON = 0.001;
//    private CRServo[] crservos;
//    private AnalogInput[] analog;

    public ServoControl(HardwareMap hardwareMap) {
        this.hardwareMap = hardwareMap;
        getServos();
        lastWrittenPos = new double[servos.length];
        java.util.Arrays.fill(lastWrittenPos, Double.NaN);
    }

    private void getServos() {
//        crservos = new CRServo[]{
//                hardwareMap.get(CRServo.class, "turretServo")
//        };
        servos = new Servo[]{
                hardwareMap.get(Servo.class, "lockServo"),
                hardwareMap.get(Servo.class, "flowerServo")
        };
//        analog = new AnalogInput[]{
//                hardwareMap.get(AnalogInput.class, "turretAnalog")
//
//        };
    }

    public void setServoStartPos() {
        servos[ServoConstants.lockServo].setPosition(IntakeConstants.lockServoLockedPos);
        lastWrittenPos[ServoConstants.lockServo] = IntakeConstants.lockServoLockedPos;
        servos[ServoConstants.flowerServo].setPosition(IntakeConstants.flowerServoLockedPos);
        lastWrittenPos[ServoConstants.flowerServo] = IntakeConstants.flowerServoLockedPos;
    }

    public void setServoPos(int index, double position) {
        if (isInBounds(index, position)) {
            if (!Double.isNaN(lastWrittenPos[index])
                    && Math.abs(position - lastWrittenPos[index]) < SERVO_POS_EPSILON) {
                return;
            }
            lastWrittenPos[index] = position;
            servos[index].setPosition(position);
        }
    }

    public double getServoPos(int index) {
        double position = servos[index].getPosition();
        return position;
    }

//    public void setServoSpeed(int index, double speed) {
//        crservos[index].setPower(speed);
//    }

    private boolean isInBounds(int index, double position) {
        return position >= ServoConstants.servoMinPos[index] && position <= ServoConstants.servoMaxPos[index];
    }

    private String servoName(int index) {
        switch (index) {
            case ServoConstants.lockServo:
                return "lockServo";
            case ServoConstants.flowerServo:
                return "flowerServo";
            default:
                return "unknownServo" + index;
        }
    }

//    public double getCRSPos(int index) {
//        return analog[index].getVoltage() / analog[index].getMaxVoltage();
//    }

//    public double getCRSDegrees(int index) {
//        return getCRSPos(index) * 360.0;
//    }
}
