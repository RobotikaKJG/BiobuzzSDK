package org.firstinspires.ftc.teamcode.HardwareInterface.Motor;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.PIDFCoefficients;
import com.qualcomm.robotcore.hardware.VoltageSensor;

import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;

public class MotorControl {

    private static class MotorNames {
        public static final String frontLeft = "frontLeftMotor";
        public static final String backLeft = "backLeftMotor";
        public static final String frontRight = "frontRightMotor";
        public static final String backRight = "backRightMotor";
        public static final String outtake1 = "outtake1Motor";
        public static final String intake = "intakeMotor";
        public static final String transfer = "transferMotor";
        public static final String outtake2 = "outtake2Motor";
    }

    private final HardwareMap hardwareMap;
    private DcMotorEx[] motors;
    private final Utilities utilities = new Utilities();

    private static final PIDFCoefficients DEFAULT_VELOCITY_PIDF = new PIDFCoefficients(50, 0, 0, 12.8);

    private final double[] motorSpeeds = new double[8];
    private final double[] lastWrittenSpeeds = new double[8];
    private static final double POWER_EPSILON = 0.005;

    // Per-motor caches so RPM/velocity mode does not re-issue blocking config
    // (setMode / setPIDFCoefficients) every loop. These are the single biggest
    // per-loop I2C cost on the outtake when shooting.
    private final DcMotor.RunMode[] lastMode = new DcMotor.RunMode[8];
    private final PIDFCoefficients[] lastPidf = new PIDFCoefficients[8];
    private final double[] lastWrittenVelocity = new double[8];

    // True while a motor is velocity-controlled (setMotorRPM). setMotors() skips
    // these so a redundant setPower can't clobber the velocity target and force
    // setVelocity to be re-issued every loop (the outtake power/velocity ping-pong).
    private final boolean[] velocityControlled = new boolean[8];

    public MotorControl(HardwareMap hardwareMap) {
        this.hardwareMap = hardwareMap;
        java.util.Arrays.fill(lastWrittenSpeeds, Double.POSITIVE_INFINITY);
        java.util.Arrays.fill(lastWrittenVelocity, Double.POSITIVE_INFINITY);
        getMotors();
    }

    private void getMotors() {
        motors = new DcMotorEx[]{
                hardwareMap.get(DcMotorEx.class, MotorNames.frontLeft),
                hardwareMap.get(DcMotorEx.class, MotorNames.backLeft),
                hardwareMap.get(DcMotorEx.class, MotorNames.frontRight),
                hardwareMap.get(DcMotorEx.class, MotorNames.backRight),
                hardwareMap.get(DcMotorEx.class, MotorNames.outtake1),
                hardwareMap.get(DcMotorEx.class, MotorNames.intake),
                hardwareMap.get(DcMotorEx.class, MotorNames.transfer),
                hardwareMap.get(DcMotorEx.class, MotorNames.outtake2),
        };

        setMotorProperties();
    }

    private void setMotorProperties() {
        motors[MotorConstants.frontLeft].setDirection(DcMotorSimple.Direction.REVERSE);
        motors[MotorConstants.backLeft].setDirection(DcMotorSimple.Direction.REVERSE);
        motors[MotorConstants.outtake1].setDirection(DcMotorSimple.Direction.REVERSE);
        motors[MotorConstants.outtake2].setDirection(DcMotorSimple.Direction.FORWARD);
        setZeroPowerBehavior(MotorConstants.all, DcMotor.ZeroPowerBehavior.BRAKE);
        setZeroPowerBehavior(MotorConstants.outtake2, DcMotor.ZeroPowerBehavior.FLOAT);
        setZeroPowerBehavior(MotorConstants.outtake1, DcMotor.ZeroPowerBehavior.FLOAT);
        setMotorMode(MotorConstants.all, DcMotor.RunMode.STOP_AND_RESET_ENCODER);
        setMotorMode(MotorConstants.all, DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        setMotorMode(MotorConstants.outtake1, DcMotor.RunMode.RUN_USING_ENCODER);
        setMotorMode(MotorConstants.outtake2, DcMotor.RunMode.RUN_USING_ENCODER);
    }

    public void setZeroPowerBehavior(int index, DcMotor.ZeroPowerBehavior zeroPowerBehavior) {
        for (int i = 0; i < Utilities.configLength(index); i++)
            motors[Utilities.motorIndex(index, i)].setZeroPowerBehavior(zeroPowerBehavior);
    }

    public void setMotorSpeed(int index, double speed) {
        for (int i = 0; i < Utilities.configLength(index); i++) {
            int mi = Utilities.motorIndex(index, i);
            motorSpeeds[mi] = speed;
            velocityControlled[mi] = false;
        }
    }

    public void addMotorSpeed(int index, double speed) {
        for (int i = 0; i < Utilities.configLength(index); i++) {
            int mi = Utilities.motorIndex(index, i);
            motorSpeeds[mi] += speed;
        }
    }

    public void multiplyMotorSpeed(int index, double multiplier) {
        for (int i = 0; i < Utilities.configLength(index); i++) {
            int mi = Utilities.motorIndex(index, i);
            motorSpeeds[mi] *= multiplier;
        }
    }

    public void divideMotorSpeed(int index, double divisor) {
        for (int i = 0; i < Utilities.configLength(index); i++) {
            int mi = Utilities.motorIndex(index, i);
            motorSpeeds[mi] /= divisor;
        }
    }

    public void setMotors(int index) {
        for (int i = 0; i < Utilities.configLength(index); i++) {
            int mi = Utilities.motorIndex(index, i);
            if (velocityControlled[mi]) {
                continue;
            }
            double speed = motorSpeeds[mi];
            if (Math.abs(speed - lastWrittenSpeeds[mi]) > POWER_EPSILON) {
                motors[mi].setPower(speed);
                lastWrittenSpeeds[mi] = speed;
                // A power write overrides any velocity target; force the next
                // setMotorRPM to re-issue setVelocity for this motor.
                lastWrittenVelocity[mi] = Double.POSITIVE_INFINITY;
            }
        }
    }

    public void setMotorMode(int index, DcMotor.RunMode mode)
    {
        for (int i = 0; i < Utilities.configLength(index); i++) {
            int mi = Utilities.motorIndex(index, i);
            motors[mi].setMode(mode);
            lastMode[mi] = mode;
        }
    }

    public void setMotorPos(int index, int position){
        for (int i = 0; i < Utilities.configLength(index); i++) {
            int mi = Utilities.motorIndex(index, i);
            motors[mi].setTargetPosition(position);
        }
    }

    // getCurrent() is a blocking ADC read that is NOT covered by bulk caching.
    // For telemetry/monitoring use this throttled version so it costs an I2C
    // round-trip only every CURRENT_REFRESH_MS instead of every loop.
    private static final long CURRENT_REFRESH_MS = 150;
    private final long[] lastCurrentReadMs = new long[8];
    private final double[] cachedCurrent = new double[8];

    public double getMotorCurrent(int index)
    {
        int mi = Utilities.motorIndex(index, 0);
        long now = System.currentTimeMillis();
        if (now - lastCurrentReadMs[mi] >= CURRENT_REFRESH_MS) {
            cachedCurrent[mi] = motors[mi].getCurrent(CurrentUnit.AMPS);
            lastCurrentReadMs[mi] = now;
        }
        return cachedCurrent[mi];
    }

    /** Timestamp of the actual ADC read, for the diagnostics worker. */
    public long getMotorCurrentReadEpochMs(int index) {
        return lastCurrentReadMs[Utilities.motorIndex(index, 0)];
    }

    public void setMotorCurrentAlert(int index, double current)
    {
        for (int i = 0; i < Utilities.configLength(index); i++)
            motors[Utilities.motorIndex(index, i)].setCurrentAlert(current, CurrentUnit.AMPS);
    }

    public boolean isOverCurrent(int index)
    {
        boolean overCurrent = false;
        for (int i = 0; i < Utilities.configLength(index); i++) {
            int mi = Utilities.motorIndex(index, i);
            boolean motorOverCurrent = motors[mi].isOverCurrent();
            if(motorOverCurrent)
                overCurrent = true;
        }
        return overCurrent;
    }

    /**
     * @noinspection unused
     */
    public void resetMotors() {
        for (DcMotor i : motors)
            i.setPower(0);
    }

    public int getMotorPosition(int index) {
        int position = Utilities.getMotorPosition(motors, index);
        return position;
    }

    public double getMotorVelocity(int index) {
        int mi = Utilities.motorIndex(index, 0);
        double velocity = motors[mi].getVelocity();
        return velocity;
    }

    /** Measured output-shaft RPM; outtake uses its confirmed 1:1 encoder ratio. */
    public double getMotorRPM(int index) {
        int mi = Utilities.motorIndex(index, 0);
        double ticksPerRevolution = (mi == MotorConstants.outtake1 || mi == MotorConstants.outtake2)
                ? MotorConstants.OUTTAKE_TICKS_PER_REVOLUTION
                : motors[mi].getMotorType().getTicksPerRev();
        return ticksPerRevolution > 0
                ? motors[mi].getVelocity() * 60.0 / ticksPerRevolution
                : Double.NaN;
    }

    /** Copies diagnostic values without issuing extra motor configuration writes. */
    public void captureDiagnostics(double[] positions, double[] velocities,
                                   double[] powerCommands, double[] velocityTargets) {
        for (int i = 0; i < motors.length; i++) {
            positions[i] = motors[i].getCurrentPosition();
            velocities[i] = motors[i].getVelocity();
            powerCommands[i] = velocityControlled[i] ? Double.NaN : lastWrittenSpeeds[i];
            velocityTargets[i] = getLastCommandedVelocity(i);
        }
    }

    public void resetMotorEncoders(int index) {
        utilities.resetMotorEncoders(motors, index);
    }

    public double getBatteryVoltage() {
        double result = Double.POSITIVE_INFINITY;
        for (VoltageSensor sensor : hardwareMap.voltageSensor) {
            double voltage = sensor.getVoltage();
            if (voltage > 0) {
                result = Math.min(result, voltage);
            }
        }
        return result;
    }

    /**
     * Last velocity target written via setMotorRPM (ticks/s), or NaN if the motor is
     * currently power-controlled / no target has been written. Read-only, for telemetry.
     */
    public double getLastCommandedVelocity(int index) {
        int mi = Utilities.motorIndex(index, 0);
        double v = lastWrittenVelocity[mi];
        return (v == Double.POSITIVE_INFINITY || !velocityControlled[mi]) ? Double.NaN : v;
    }

    public void setMotorRPM(int index, double velocityTicksPerSecond) {
        setMotorRPM(index, velocityTicksPerSecond, DEFAULT_VELOCITY_PIDF);
    }

    public void setMotorRPM(int index, double velocityTicksPerSecond, PIDFCoefficients pidf) {
        for (int i = 0; i < Utilities.configLength(index); i++) {
            int mi = Utilities.motorIndex(index, i);
            DcMotorEx motor = motors[mi];
            velocityControlled[mi] = true;

            // setMode and setPIDFCoefficients are blocking config writes; only
            // issue them when they actually change instead of every loop.
            if (lastMode[mi] != DcMotor.RunMode.RUN_USING_ENCODER) {
                motor.setMode(DcMotor.RunMode.RUN_USING_ENCODER);
                lastMode[mi] = DcMotor.RunMode.RUN_USING_ENCODER;
            }
            if (!pidfEquals(lastPidf[mi], pidf)) {
                motor.setPIDFCoefficients(DcMotor.RunMode.RUN_USING_ENCODER, pidf);
                lastPidf[mi] = pidf;
            }
            if (lastWrittenVelocity[mi] != velocityTicksPerSecond) {
                motor.setVelocity(velocityTicksPerSecond);
                lastWrittenVelocity[mi] = velocityTicksPerSecond;
                // A velocity write overrides any power; force the next setMotors
                // to re-issue setPower for this motor.
                lastWrittenSpeeds[mi] = Double.POSITIVE_INFINITY;
            }
        }
    }

    private static boolean pidfEquals(PIDFCoefficients a, PIDFCoefficients b) {
        if (a == null || b == null) return false;
        return a.p == b.p && a.i == b.i && a.d == b.d && a.f == b.f;
    }

    private String motorName(int motorIndex) {
        switch (motorIndex) {
            case MotorConstants.frontLeft:
                return MotorNames.frontLeft;
            case MotorConstants.backLeft:
                return MotorNames.backLeft;
            case MotorConstants.frontRight:
                return MotorNames.frontRight;
            case MotorConstants.backRight:
                return MotorNames.backRight;
            case MotorConstants.outtake1:
                return MotorNames.outtake1;
            case MotorConstants.intake:
                return MotorNames.intake;
            case MotorConstants.transfer:
                return MotorNames.transfer;
            case MotorConstants.outtake2:
                return MotorNames.outtake2;
            default:
                return "unknownMotor" + motorIndex;
        }
    }
}
