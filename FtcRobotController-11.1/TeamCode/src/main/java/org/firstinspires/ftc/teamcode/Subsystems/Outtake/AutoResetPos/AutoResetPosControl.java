package org.firstinspires.ftc.teamcode.Subsystems.Outtake.AutoResetPos;

import org.firstinspires.ftc.teamcode.HardwareInterface.Sensor.SensorControl;
import org.firstinspires.ftc.teamcode.Subsystems.Outtake.OuttakeStates;

public class AutoResetPosControl {
    private double currentWait = 0;
    private boolean reseted = false;
    private AutoResetPosStates prevAutoResetPosState = AutoResetPosStates.idle;
    private SensorControl sensorControl;

    public AutoResetPosControl(SensorControl sensorControl) {
        this.sensorControl = sensorControl;
    }

    public void update() {
        if(OuttakeStates.getAutoResetPosState() != prevAutoResetPosState) {
            updateStates();
            prevAutoResetPosState = OuttakeStates.getAutoResetPosState();
        }
        if (OuttakeStates.getAutoResetPosState() == AutoResetPosStates.resetPos) {
            updateStates();
        }
    }

    public void updateStates() {
        switch (OuttakeStates.getAutoResetPosState()) {
            case resetPos:
                resetPos();
                break;
            case waitForReset:
                waitForReset();
                break;
            case idle:
                break;
        }
    }

    private void resetPos() {
        sensorControl.setResetting(true);
        if (!sensorControl.resetLocalizerWithLimelight()) return;
        OuttakeStates.setAutoResetPosState(AutoResetPosStates.waitForReset);
    }

    private void waitForReset() {
        sensorControl.setResetting(false);
    }

    private void addWaitTime(double waitTime) {
        currentWait = getSeconds() + waitTime;
    }

    private double getSeconds() {
        return System.currentTimeMillis() / 1000.0;
    }
}