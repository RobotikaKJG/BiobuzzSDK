package org.firstinspires.ftc.teamcode.Subsystems.Intake.LED;

import org.firstinspires.ftc.teamcode.HardwareInterface.Sensor.GoBildaIndicator;
import org.firstinspires.ftc.teamcode.HardwareInterface.Sensor.SensorControl;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.IntakeStates;

public class LEDControl {
    private LEDStates prevLEDState = null;
    private final SensorControl sensorControl;

    public LEDControl(SensorControl sensorControl) {
        this.sensorControl = sensorControl;
    }

    public void update() {
        if(IntakeStates.getLEDState() != prevLEDState) {
            updateStates();
            prevLEDState = IntakeStates.getLEDState();
        }
    }

    public void updateStates() {
        switch (IntakeStates.getLEDState()) {
            case intakeNo:
                sensorControl.setLEDColor(GoBildaIndicator.Color.WHITE);
                break;
            case intakeTwo:
                sensorControl.setLEDColor(GoBildaIndicator.Color.ORANGE);
                break;
            case intakeThree:
                sensorControl.setLEDColor(GoBildaIndicator.Color.BLUE);
                break;
            case outtakeNo:
                sensorControl.setLEDColor(GoBildaIndicator.Color.RED);
                break;
            case outtakeMaybe:
                sensorControl.setLEDColor(GoBildaIndicator.Color.ORANGE);
                break;
            case outtakeYes:
                sensorControl.setLEDColor(GoBildaIndicator.Color.GREEN);
                break;
            case idle:
                sensorControl.setLEDColor(GoBildaIndicator.Color.OFF);
                break;
        }
    }
}
