package org.firstinspires.ftc.teamcode.Subsystems.Outtake.TurretServo;

import org.firstinspires.ftc.teamcode.HardwareInterface.Sensor.SensorControl;
import org.firstinspires.ftc.teamcode.HardwareInterface.Servo.ServoControl;
import org.firstinspires.ftc.teamcode.Subsystems.Outtake.OuttakeConstants;
import org.firstinspires.ftc.teamcode.Subsystems.Outtake.OuttakeStates;

/**
 * Simple turret homing:
 *   target angle (from SensorControl, robot-relative degrees)
 *     -> clamp to the turret limits
 *     -> convert to a servo position using gear ratio + servo travel
 *     -> clamp to servo min/max, rate-limit, write to both servos.
 */
public class TurretServoControl {

    private final ServoControl servoControl;
    private final SensorControl sensorControl;

    private volatile double targetAngleDeg = 0.0;  // angle we are aiming for (after limits)
    private volatile double turretAngleDeg = 0.0;  // angle we have commanded
    private double manualAngleDeg = 0.0;

    private double lastPos = Double.NaN;           // last servo position commanded
    private long lastNs = 0;

    public TurretServoControl(ServoControl servoControl, SensorControl sensorControl) {
        this.servoControl = servoControl;
        this.sensorControl = sensorControl;
    }

    public void update() {
        if (!OuttakeStates.isTurretTrackingEnabled()) return;

        double angle = (OuttakeStates.getTurretServoState() == TurretServoStates.manual)
                ? manualAngleDeg
                : sensorControl.getTurretTargetAngleDegrees();

        // Bad pose data: keep the last good position instead of writing NaN.
        if (Double.isNaN(angle) || Double.isInfinite(angle)) return;

        // 1) Don't turn past the limits.
        angle = clamp(angle, OuttakeConstants.turretLimitRight, OuttakeConstants.turretLimitLeft);
        targetAngleDeg = angle;

        // 2) Angle -> servo position (0.5 = centered).
        // The turret turns turretGearRatio x as far as the servo, so servo degrees = turret degrees / ratio.
        double pos = 0.5 + (angle * OuttakeConstants.turretDirection)
                / (OuttakeConstants.turretGearRatio * OuttakeConstants.turretServoTravel);
        pos = clamp(pos, OuttakeConstants.turretServoMin, OuttakeConstants.turretServoMax);

        // 3) Move toward it at a limited speed so the turret sweeps smoothly.
        pos = slew(pos);

        // 4) Both servos get the same position (per-servo multipliers are applied in ServoControl).
        servoControl.setTurretServosPos(pos);

        turretAngleDeg = (pos - 0.5) * OuttakeConstants.turretServoTravel
                * OuttakeConstants.turretGearRatio * OuttakeConstants.turretDirection;
    }

    private double slew(double desired) {
        long now = System.nanoTime();
        if (Double.isNaN(lastPos)) {
            lastPos = desired;
        } else {
            double dt = clamp((now - lastNs) / 1e9, 0.0, 0.05);
            double maxStep = OuttakeConstants.turretServoSlewPerSec * dt;
            lastPos += clamp(desired - lastPos, -maxStep, maxStep);
        }
        lastNs = now;
        return lastPos;
    }

    private static double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }

    public double getTurretAngleDeg() { return turretAngleDeg; }

    public double getTargetAngleDeg() { return targetAngleDeg; }

    public void setManualAngleDeg(double angle) { this.manualAngleDeg = angle; }
}