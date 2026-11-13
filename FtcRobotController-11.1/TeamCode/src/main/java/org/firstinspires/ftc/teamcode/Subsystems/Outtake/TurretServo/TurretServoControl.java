package org.firstinspires.ftc.teamcode.Subsystems.Outtake.TurretServo;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.RobotLog;

import com.pedropathing.geometry.Pose;

import org.firstinspires.ftc.teamcode.HardwareInterface.Motor.MotorConstants;
import org.firstinspires.ftc.teamcode.HardwareInterface.Motor.MotorControl;
import org.firstinspires.ftc.teamcode.HardwareInterface.Sensor.SensorControl;
import org.firstinspires.ftc.teamcode.HardwareInterface.Servo.ServoConstants;
import org.firstinspires.ftc.teamcode.HardwareInterface.Servo.ServoControl;
import org.firstinspires.ftc.teamcode.Main.GlobalVariables;
import org.firstinspires.ftc.teamcode.Subsystems.Outtake.OuttakeConstants;
import org.firstinspires.ftc.teamcode.Subsystems.Outtake.OuttakeStates;

public class TurretServoControl {

    private static final String TAG = "TurretServo";

    private final ServoControl servoControl;
    private final SensorControl sensorControl;

    private volatile double turretAngleDeg = 0.0;   // volatile: read by main thread for telemetry
    private volatile double targetAngleDeg = 0.0;   // volatile: read by main thread for telemetry
    private double targetServoPos = 0.0;

    // Limit held while the aim target is in the unreachable dead zone behind the
    // turret (NaN = target currently reachable). See resolveTargetWithinLimits().
    private double deadZoneLimitDeg = Double.NaN;

    // Slew-limiter state: last position actually commanded to the servos.
    private double lastCommandedPos = Double.NaN;
    private long lastUpdateNs = 0;

    private double manualAngleDeg = 0.0;

    public TurretServoControl(ServoControl servoControl, SensorControl sensorControl) {
        this.servoControl = servoControl;
        this.sensorControl = sensorControl;
    }

    public void update() {
        if (!OuttakeStates.isTurretTrackingEnabled()) return;

        double target;

        if (OuttakeStates.getTurretServoState() == TurretServoStates.manual) {
            target = manualAngleDeg;
        } else {
            if (sensorControl.getLocalizerPose().getY() < -10 && !GlobalVariables.far) {
                return;
            }

            target = sensorControl.getTurretTargetAngleDegrees();
            if (isFinite(target)) {
                target = normalizeDegrees(target + OuttakeConstants.turretAimOffsetDeg);
            }
        }

        if (!isFinite(target)) {
            // Pose/velocity glitch produced a bad angle. Hold the last good aim rather
            // than writing an illegal (NaN) servo position. Root cause is guarded in the
            // Pinpoint driver; this is defense-in-depth so the turret never errors.
            return;
        }

        targetAngleDeg = resolveTargetWithinLimits(target);
        turretAngleDeg = getCurrentTurretAngleDeg();

        double pos = (targetAngleDeg * OuttakeConstants.turretGearRatio * OuttakeConstants.turretDirection)
                / OuttakeConstants.turretServoTravel + 0.5;
        // Never command past the servos' usable travel — past turretServoMax the
        // turret presses into its hard stop and stalls (even if the angle limits
        // are someday re-widened, this clamp keeps the hardware safe).
        pos = clamp(pos, OuttakeConstants.turretServoMin, OuttakeConstants.turretServoMax);
        if (!isFinite(pos)) {
            return; // never write NaN to the servo
        }
        targetServoPos = slewLimit(pos);
        servoControl.setTurretServosPos(targetServoPos);
    }

    private double resolveTargetWithinLimits(double targetDeg) {
        double left = OuttakeConstants.turretLimitLeft;
        double right = OuttakeConstants.turretLimitRight;

        if (targetDeg >= right && targetDeg <= left) {
            if (!Double.isNaN(deadZoneLimitDeg)) {
                logLatchEvent("RELEASE", targetDeg, targetDeg);
                deadZoneLimitDeg = Double.NaN;
            }
            return targetDeg;
        }

        // In the dead zone. distPastLeft + distBeforeRight == dead-zone width, so
        // bias > 0 means the target is angularly nearer the RIGHT limit, bias < 0
        // nearer the LEFT; bias == 0 is the dead-zone bisector (directly behind).
        double distPastLeft = normalizeDegrees(targetDeg - left);
        double distBeforeRight = normalizeDegrees(right - targetDeg);
        double bias = distPastLeft - distBeforeRight;
        double hyst = OuttakeConstants.turretDeadZoneHysteresisDeg;

        if (Double.isNaN(deadZoneLimitDeg)) {
            // Just entered the dead zone: pick the nearer limit.
            deadZoneLimitDeg = (bias <= 0) ? left : right;
            logLatchEvent("ENGAGE", targetDeg, deadZoneLimitDeg);
        } else if (deadZoneLimitDeg == left && bias > hyst) {
            // Target has crossed the bisector toward the right by the hysteresis margin.
            deadZoneLimitDeg = right;
            logLatchEvent("SWITCH->right", targetDeg, deadZoneLimitDeg);
        } else if (deadZoneLimitDeg == right && bias < -hyst) {
            deadZoneLimitDeg = left;
            logLatchEvent("SWITCH->left", targetDeg, deadZoneLimitDeg);
        }
        // else: hold the current limit (target still within the hysteresis band).
        return deadZoneLimitDeg;
    }

    private void logLatchEvent(String event, double rawTargetDeg, double resolvedDeg) {
        try {
            Pose pose = sensorControl.getLocalizerPose();
            if (pose != null) {
                RobotLog.ii(TAG,
                        "deadzone %s: rawTarget=%.1f resolved=%.1f pose=(x=%.1f y=%.1f h=%.1f)",
                        event, rawTargetDeg, resolvedDeg,
                        pose.getX(), pose.getY(), Math.toDegrees(pose.getHeading()));
            } else {
                RobotLog.ii(TAG, "deadzone %s: rawTarget=%.1f resolved=%.1f pose=null",
                        event, rawTargetDeg, resolvedDeg);
            }
        } catch (Exception ignored) {
            // Never let telemetry/logging interfere with turret control.
        }
    }

    private double slewLimit(double desiredPos) {
        long now = System.nanoTime();
        if (Double.isNaN(lastCommandedPos)) {
            lastCommandedPos = desiredPos;
            lastUpdateNs = now;
            return desiredPos;
        }
        double dt = (now - lastUpdateNs) / 1e9;
        lastUpdateNs = now;
        dt = clamp(dt, 0.0, 0.05); // bound the step if the loop ever stalls
        double maxStep = OuttakeConstants.turretServoSlewPerSec * dt;
        lastCommandedPos += clamp(desiredPos - lastCommandedPos, -maxStep, maxStep);
        return lastCommandedPos;
    }

    private static double normalizeDegrees(double angle) {
        while (angle > 180) angle -= 360;
        while (angle < -180) angle += 360;
        return angle;
    }

    private static boolean isFinite(double v) {
        return !Double.isNaN(v) && !Double.isInfinite(v);
    }

    private double getCurrentTurretAngleDeg() {
        double pos = servoControl.getServoPos(ServoConstants.turretServo1);
        return ((pos - 0.5) * OuttakeConstants.turretServoTravel)
                / (OuttakeConstants.turretGearRatio * OuttakeConstants.turretDirection);
    }

    private double clamp(double val, double min, double max) {
        return Math.max(min, Math.min(max, val));
    }

    public double getTurretAngleDeg() {
        return turretAngleDeg;
    }

    public double getTargetAngleDeg() {
        return targetAngleDeg;
    }

    public void setManualAngleDeg(double angle) {
        this.manualAngleDeg = angle;
    }
}
