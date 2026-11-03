package org.firstinspires.ftc.teamcode.Subsystems.Outtake.OuttakeMotor;

import org.firstinspires.ftc.teamcode.HardwareInterface.Motor.MotorConstants;
import org.firstinspires.ftc.teamcode.HardwareInterface.Motor.MotorControl;
import org.firstinspires.ftc.teamcode.HardwareInterface.Sensor.SensorControl;
import org.firstinspires.ftc.teamcode.Main.GlobalVariables;
import org.firstinspires.ftc.teamcode.Subsystems.Outtake.OuttakeConstants;
import org.firstinspires.ftc.teamcode.Subsystems.Outtake.OuttakeStates;

public class OuttakeMotorControl {
    private final MotorControl motorControl;
    private final SensorControl sensorControl;
    private OuttakeMotorStates prevMotorStates = OuttakeMotorStates.idle;

    /** Last commanded close-mode ticks/s (kept across invalid distance samples). */
    private double speed = OuttakeConstants.closeCalTicks;
    private double prevspeed = OuttakeConstants.closeCalTicks;

    /** Latched while below target after a shot dip — forces +100% until nearly recovered. */
    private boolean recoveringFullPower = false;

    public OuttakeMotorControl(MotorControl motorControl, SensorControl sensorControl) {
        this.motorControl = motorControl;
        this.sensorControl = sensorControl;
    }

    public void update() {
        if (OuttakeStates.getMotorState() != prevMotorStates) {
            updateStates();
            prevMotorStates = OuttakeStates.getMotorState();
        } else if (OuttakeStates.getMotorState() == OuttakeMotorStates.forwardClose
                || OuttakeStates.getMotorState() == OuttakeMotorStates.forwardStart
                || OuttakeStates.getMotorState() == OuttakeMotorStates.forwardFar) {
            updateStates();
        }
    }

    public void updateStates() {
        switch (OuttakeStates.getMotorState()) {
            case autonomous:
                motorControl.setMotorRPM(
                        MotorConstants.outtake,
                        OuttakeConstants.outtakeSpeedFar - 100 + GlobalVariables.rpmOffset);
                break;
            case forwardStart:
                motorControl.setMotorRPM(
                        MotorConstants.outtake, 1750 + GlobalVariables.rpmOffset);
                break;
            case forwardFar:
                motorControl.setMotorRPM(
                        MotorConstants.outtake,
                        OuttakeConstants.outtakeSpeedFar + GlobalVariables.rpmOffset);
                break;
            case forwardClose:
                calculateSpeed();
                setMotorPowerControl(speed);
                break;
            case backward:
                motorControl.setMotorSpeed(MotorConstants.outtake, -0.5);
                break;
            case idle:
                recoveringFullPower = false;
                motorControl.setMotorRPM(MotorConstants.outtake, 0);
                break;
        }
    }

    /**
     * Close shooting: the instant RPM falls a little under target (ball through),
     * command +100% power and keep it until nearly back — then hand off to velocity PID.
     * Old threshold was 100 ticks (~214 RPM), which delayed recovery.
     */
    private void setMotorPowerControl(double targetVelocity) {
        double targetWithOffset = targetVelocity + GlobalVariables.rpmOffset;
        double currentVelocity = motorControl.getMotorVelocity(MotorConstants.outtake);
        double error = targetWithOffset - currentVelocity; // positive ⇒ under target

        if (error > OuttakeConstants.recoverEnterTicks) {
            recoveringFullPower = true;
        } else if (error < OuttakeConstants.recoverExitTicks) {
            recoveringFullPower = false;
        }

        if (recoveringFullPower && error > 0) {
            motorControl.setMotorSpeed(MotorConstants.outtake, 1.0);
            motorControl.setMotors(MotorConstants.outtake);
        } else {
            motorControl.setMotorRPM(MotorConstants.outtake, targetWithOffset);
        }
    }

    /**
     * Close-mode target from localizer distance (inches → cm):
     * {@code v = closeCalTicks * sqrt(clamp(d) / closeCalDistanceCm)}, then tick clamps.
     * Invalid distance keeps the previous command so a glitch cannot spike RPM.
     */
    private void calculateSpeed() {
        double distanceIn = sensorControl.getDistanceFromLocalizer();

        distanceIn = OuttakeConstants.correctedDistanceInches(distanceIn);
        if (distanceIn < 0) {
            speed = prevspeed;
        } else {
            double distanceCm = distanceIn * 2.54;
            double clampedCm = clamp(
                    distanceCm,
                    OuttakeConstants.closeMinDistanceCm,
                    OuttakeConstants.closeMaxDistanceCm);
            double ratio = clampedCm / OuttakeConstants.closeCalDistanceCm;
            double ticks = OuttakeConstants.closeCalTicks * Math.sqrt(ratio);
            speed = clamp(ticks, OuttakeConstants.closeMinTicks, OuttakeConstants.closeMaxTicks);
        }

        GlobalVariables.outtakeTargetSpeed = speed;
        prevspeed = speed;
    }

    private static double clamp(double v, double min, double max) {
        return Math.max(min, Math.min(max, v));
    }
}
