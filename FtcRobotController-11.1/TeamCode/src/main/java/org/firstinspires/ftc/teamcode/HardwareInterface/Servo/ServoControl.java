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

    // Skip redundant turret servo writes. Each setPosition is a ~1.5ms serial
    // transaction; the turret target is unchanged on most loops. Gating on the
    // base position preserves each servo's per-servo multiplier exactly.
    private double lastTurretBasePos = Double.NaN;
    private static final double TURRET_POS_EPSILON = 0.001;

    // Per-servo write-gate: skip setPosition when the target is unchanged so a
    // redundant ~1.5ms serial write can never slip through (e.g. if a caller
    // stops gating on its own state).
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
                hardwareMap.get(Servo.class, "turretServo1"),
                hardwareMap.get(Servo.class, "turretServo2")
        };
//        analog = new AnalogInput[]{
//                hardwareMap.get(AnalogInput.class, "turretAnalog")
//
//        };
    }

    public void setServoStartPos() {
        servos[ServoConstants.lockServo].setPosition(IntakeConstants.lockServoLockedPos);
        lastWrittenPos[ServoConstants.lockServo] = IntakeConstants.lockServoLockedPos;
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

    public void setTurretServosPos(double position) {
        // Hard floor/ceiling at the turret's usable travel: past turretServoMax the
        // mechanism is against its hard stop and the servos stall. Callers should
        // stay inside this range; this clamp is the last line of defense.
        position = Math.max(OuttakeConstants.turretServoMin,
                Math.min(OuttakeConstants.turretServoMax, position));
        if (!Double.isNaN(lastTurretBasePos)
                && Math.abs(position - lastTurretBasePos) < TURRET_POS_EPSILON) {
            return;
        }
        lastTurretBasePos = position;

        servos[ServoConstants.turretServo1].setPosition(position * OuttakeConstants.turretServo1Mult);

        servos[ServoConstants.turretServo2].setPosition(position * OuttakeConstants.turretServo2Mult);
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
            case ServoConstants.turretServo1:
                return "turretServo1";
            case ServoConstants.turretServo2:
                return "turretServo2";
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
