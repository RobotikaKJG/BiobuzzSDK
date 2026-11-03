package org.firstinspires.ftc.teamcode.Main.ShooterTelemetry;

/**
 * One telemetry sample of the shooter (flywheel) subsystem. Plain struct built on
 * the control loop and handed to the {@link ShooterLogger} writer thread via a queue.
 *
 * Units: velocities/targets are encoder ticks/sec (the viewer converts to RPM using
 * ticksPerRev from the session header); motor currents in amps; battery in volts;
 * distance in inches; turret angle in degrees.
 */
public class ShooterSample {
    public boolean inferredShot;
    public long inferredShotCount;
    public double inferredShotIntervalMs = Double.NaN;
    public double sampleIntervalMs;
    public double captureMs;
    public long droppedSamples;
    public int currentMotor;
    public final double[] motorPositions = new double[8];
    public final double[] motorVelocities = new double[8];
    public final double[] motorPowerCommands = new double[8];
    public final double[] motorVelocityTargets = new double[8];
    public final double[] currentReadEpochMs = new double[8];
    public double[] servoCommands;
    public long tMs;            // ms since session start
    public double v1;           // outtake1 measured velocity (ticks/s)
    public double v2;           // outtake2 measured velocity (ticks/s)
    public double i1;           // outtake1 current (amps)
    public double i2;           // outtake2 current (amps)
    public double idFl;         // front-left drive current (amps)
    public double idBl;         // back-left drive current (amps)
    public double idFr;         // front-right drive current (amps)
    public double idBr;         // back-right drive current (amps)
    public double iIntake;      // intake motor current (amps)
    public double iTransfer;    // transfer motor current (amps)
    public double target;       // last commanded velocity (ticks/s), NaN if power-controlled
    public String shootState;   // AutoCycleShootStates name
    public String motorState;   // OuttakeMotorStates name
    public String intakeMotorState;
    public String transferMotorState;
    public boolean turretTracking;
    public boolean intakeBall;
    public boolean transferBall;
    public double battery;      // volts, NaN if not sampled this tick
    public double distance;     // inches to goal (localizer); NaN if invalid
    public double turretDeg;    // turret angle degrees; NaN if unavailable
    public double poseX;        // localizer pose X (inches; TeleOp center or Pedro corner)
    public double poseY;        // localizer pose Y (inches)
    public double poseHeadingDeg; // localizer heading degrees

    /** Serialize as one compact JSONL line. Hand-built (no reflection) — this runs
     *  on the writer thread but keeps allocation predictable. */
    public String toJsonLine() {
        StringBuilder sb = new StringBuilder(220);
        sb.append("{\"t\":").append(tMs);
        sb.append(",\"inferredShot\":").append(inferredShot);
        sb.append(",\"inferredShotCount\":").append(inferredShotCount);
        appendNum(sb, "inferredShotIntervalMs", inferredShotIntervalMs);
        appendNum(sb, "v1", v1);
        appendNum(sb, "v2", v2);
        appendNum(sb, "i1", i1);
        appendNum(sb, "i2", i2);
        appendNum(sb, "id0", idFl);
        appendNum(sb, "id1", idBl);
        appendNum(sb, "id2", idFr);
        appendNum(sb, "id3", idBr);
        appendNum(sb, "ii", iIntake);
        appendNum(sb, "it", iTransfer);
        appendNum(sb, "tg", target);
        sb.append(",\"ss\":\"").append(shootState).append('"');
        sb.append(",\"ms\":\"").append(motorState).append('"');
        sb.append(",\"ims\":\"").append(intakeMotorState).append('"');
        sb.append(",\"tms\":\"").append(transferMotorState).append('"');
        sb.append(",\"tr\":").append(turretTracking ? 1 : 0);
        sb.append(",\"ib\":").append(intakeBall ? 1 : 0);
        sb.append(",\"tb\":").append(transferBall ? 1 : 0);
        appendNum(sb, "bat", battery);
        appendNum(sb, "d", distance);
        appendNum(sb, "ta", turretDeg);
        appendNum(sb, "px", poseX);
        appendNum(sb, "py", poseY);
        appendNum(sb, "ph", poseHeadingDeg);
        appendNum(sb, "sampleIntervalMs", sampleIntervalMs);
        appendNum(sb, "captureMs", captureMs);
        sb.append(",\"droppedSamples\":").append(droppedSamples);
        sb.append(",\"currentMotor\":").append(currentMotor);
        appendArray(sb, "motorPositions", motorPositions);
        appendArray(sb, "motorVelocitiesTicksPerSec", motorVelocities);
        appendArray(sb, "motorPowerCommands", motorPowerCommands);
        appendArray(sb, "motorVelocityTargetsTicksPerSec", motorVelocityTargets);
        appendArray(sb, "currentReadEpochMs", currentReadEpochMs);
        if (servoCommands != null) appendArray(sb, "servoCommands", servoCommands);
        sb.append('}');
        return sb.toString();
    }

    private static void appendArray(StringBuilder sb, String key, double[] values) {
        sb.append(",\"").append(key).append("\":[");
        for (int i = 0; i < values.length; i++) {
            if (i > 0) sb.append(',');
            double value = values[i];
            if (Double.isNaN(value) || Double.isInfinite(value)) sb.append("null");
            else sb.append(value);
        }
        sb.append(']');
    }

    /** Append ,"key":value rounded to 2 decimals; omit NaN/Inf entirely (JSON-safe). */
    private static void appendNum(StringBuilder sb, String key, double v) {
        if (Double.isNaN(v) || Double.isInfinite(v)) return;
        sb.append(",\"").append(key).append("\":");
        // 2-decimal rounding without String.format (locale-safe, cheaper)
        double r = Math.round(v * 100.0) / 100.0;
        if (r == Math.rint(r)) sb.append((long) r);
        else sb.append(r);
    }
}
