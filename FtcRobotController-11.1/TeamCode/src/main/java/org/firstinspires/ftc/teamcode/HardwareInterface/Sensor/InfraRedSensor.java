package org.firstinspires.ftc.teamcode.HardwareInterface.Sensor;

import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.DigitalChannelController;
import com.qualcomm.robotcore.hardware.HardwareDevice;
import com.qualcomm.robotcore.hardware.TouchSensor;
import com.qualcomm.robotcore.hardware.configuration.annotations.DeviceProperties;
import com.qualcomm.robotcore.hardware.configuration.annotations.DigitalIoDeviceType;


@DigitalIoDeviceType
@DeviceProperties(name = "InfraRed Sensor", description = "Physical infrared sensor", xmlTag = "InfraRedSensor")
public class InfraRedSensor implements TouchSensor {
    private final DigitalChannelController digitalChannelController;
    private final int physicalPort;
    private InfraRedSensor.SwitchConfig deviceMode;

    public enum SwitchConfig {
        NC,
        NO
    }

    @Override
    public HardwareDevice.Manufacturer getManufacturer() {

        return HardwareDevice.Manufacturer.Other;
    }

    @Override
    public String getDeviceName() {

        return "InfraRed Sensor";
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
        if (deviceMode == InfraRedSensor.SwitchConfig.NC)
            return isPressed() ? 0 : 1;
        return isPressed() ? 1 : 0;
    }

    public Boolean getIsPressed() {
        if (deviceMode == InfraRedSensor.SwitchConfig.NC)
            return !isPressed();
        return isPressed();
    }

    @Override
    public boolean isPressed() {
        return !digitalChannelController.getDigitalChannelState(physicalPort);
    }

    public boolean isObstructed() {
        return !getIsPressed();
    }

    public void setMode(InfraRedSensor.SwitchConfig mode) {
        deviceMode = mode;
    }

    public InfraRedSensor(final DigitalChannelController digitalChannelController, final int physicalPort) {
        this.digitalChannelController = digitalChannelController;
        this.physicalPort = physicalPort;
    }

    @Override
    public void close() {
    }
}
