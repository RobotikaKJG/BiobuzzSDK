//package org.firstinspires.ftc.teamcode.Main.ManualOpModes;
//
//import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
//import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
//
//import org.firstinspires.ftc.teamcode.HardwareInterface.Gamepad.EdgeDetection;
//import org.firstinspires.ftc.teamcode.HardwareInterface.Sensor.LimitSwitch;
//import org.firstinspires.ftc.teamcode.HardwareInterface.Sensor.LimitSwitches;
//import org.firstinspires.ftc.teamcode.HardwareInterface.Sensor.SensorControl;
//import org.firstinspires.ftc.teamcode.Roadrunner.StandardTrackingWheelLocalizer;
//import org.firstinspires.ftc.teamcode.Roadrunner.TwoWheelTrackingLocalizer;
//
//@TeleOp
//public class LimitSwitchTest extends LinearOpMode {
//    EdgeDetection edgeDetection = new EdgeDetection();
//
//    @Override
//    public void runOpMode() {
//        // Get the color sensor from hardwareMap
//
//        SensorControl sensorControl = new SensorControl(hardwareMap,edgeDetection, new TwoWheelTrackingLocalizer(hardwareMap));
//        LimitSwitch limitSwitch = hardwareMap.get(LimitSwitch.class, "turretRightLimitSwitch");
//
//        // Wait for the Play button to be pressed
//        waitForStart();
//
//        // While the Op Mode is running, update the telemetry values.
//        while (opModeIsActive()) {
////            telemetry.addData("LeftSlide", sensorControl.isLimitSwitchPressed(LimitSwitches.slideLeft));
////            telemetry.addData("RightSlide", sensorControl.isLimitSwitchPressed(LimitSwitches.slideRight));
////            telemetry.addData("Extendo", sensorControl.isLimitSwitchPressed(LimitSwitches.extendo));
//            telemetry.addData("Test", limitSwitch.getValue());
//
//            telemetry.update();
//        }
//    }
//}
