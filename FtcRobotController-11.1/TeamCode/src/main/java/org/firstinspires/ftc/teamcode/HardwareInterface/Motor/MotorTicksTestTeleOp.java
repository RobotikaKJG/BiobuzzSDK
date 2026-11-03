package org.firstinspires.ftc.teamcode.HardwareInterface.Motor;

import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.qualcomm.robotcore.hardware.DcMotor;

@TeleOp(name = "MotorTicksTestTeleOp", group = "TeleOp")
public class MotorTicksTestTeleOp extends OpMode {
    private DcMotor motor;

    @Override
    public void init() {
        motor = hardwareMap.get(DcMotor.class, "turretMotor");
        motor.setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
    }

    @Override
    public void loop() {
        telemetry.addData("Ticks", motor.getCurrentPosition());
        telemetry.update();
    }
}
