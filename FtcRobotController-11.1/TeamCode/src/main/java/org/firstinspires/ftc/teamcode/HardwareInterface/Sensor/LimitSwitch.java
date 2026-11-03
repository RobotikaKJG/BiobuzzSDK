package org.firstinspires.ftc.teamcode.HardwareInterface.Sensor;

import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.DigitalChannelController;
import com.qualcomm.robotcore.hardware.TouchSensor;
import com.qualcomm.robotcore.hardware.configuration.annotations.DeviceProperties;
import com.qualcomm.robotcore.hardware.configuration.annotations.DigitalIoDeviceType;


@DigitalIoDeviceType
@DeviceProperties(name = "Limit Switch", description = "Physical limit switch", xmlTag = "LimitSwitch")
public class LimitSwitch implements TouchSensor  //implements ColorSensor
{

    private final DigitalChannelController digitalChannelController;
    private final int physicalPort;
    private SwitchConfig deviceMode;

    public enum SwitchConfig {
        NC,
        NO
    }

    @Override
    public Manufacturer getManufacturer() {

        return Manufacturer.Other;
    }

    @Override
    public String getDeviceName() {

        return "Limit Switch";
    }

    @Override
    public String getConnectionInfo() {
        return digitalChannelController.getConnectionInfo() + "; digital channel " + physicalPort;
    }

    @Override
    public int getVersion() {
        return 1;
    }

    @Override
    public void resetDeviceConfigurationForOpMode() {
        digitalChannelController.setDigitalChannelMode(physicalPort, DigitalChannel.Mode.INPUT);
    }

    @Override
    public double getValue() {
        if (deviceMode == SwitchConfig.NC)
            return isPressed() ? 0 : 1;
        return isPressed() ? 1 : 0;
    }

    public Boolean getIsPressed() {
        if (deviceMode == SwitchConfig.NC)
            return !isPressed();
        return isPressed();
    }

    @Override
    public boolean isPressed() {
        return !digitalChannelController.getDigitalChannelState(physicalPort);
    }

    public void setMode(SwitchConfig mode) {
        deviceMode = mode;
    }

    public LimitSwitch(final DigitalChannelController digitalChannelController, final int physicalPort) {
        this.digitalChannelController = digitalChannelController;
        this.physicalPort = physicalPort;
    }

    @Override
    public void close() {
    }
}
