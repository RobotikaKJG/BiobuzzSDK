package org.firstinspires.ftc.teamcode.Subsystems.Control;

import org.firstinspires.ftc.teamcode.HardwareInterface.Gamepad.GamepadIndexValues;
import org.firstinspires.ftc.teamcode.HardwareInterface.Gamepad.EdgeDetection;
import org.firstinspires.ftc.teamcode.HardwareInterface.Motor.MotorControl;
import org.firstinspires.ftc.teamcode.HardwareInterface.Sensor.SensorControl;
//import org.firstinspires.ftc.teamcode.Subsystems.Control.Buttons.DpadDown.DpadDownLogic;
//import org.firstinspires.ftc.teamcode.Subsystems.Control.Buttons.DpadLeft.DpadLeftLogic;
//import org.firstinspires.ftc.teamcode.Subsystems.Control.Buttons.DpadRight.DpadRightLogic;
//import org.firstinspires.ftc.teamcode.Subsystems.Control.Buttons.DpadUp.DpadUpLogic;
//import org.firstinspires.ftc.teamcode.Subsystems.Control.Buttons.Circle.CircleLogic;
//import org.firstinspires.ftc.teamcode.Subsystems.Control.Buttons.DpadRight.DpadRightLogic;
//import org.firstinspires.ftc.teamcode.Subsystems.Control.Buttons.DpadUp.DpadUpLogic;
//import org.firstinspires.ftc.teamcode.Subsystems.Control.Buttons.LeftBumper.LeftBumperLogic;
//import org.firstinspires.ftc.teamcode.Subsystems.Control.Buttons.LeftTrigger.LeftTriggerLogic;
//import org.firstinspires.ftc.teamcode.Subsystems.Control.Buttons.LeftTrigger.LeftTriggerLogic;
import org.firstinspires.ftc.teamcode.Subsystems.Control.Buttons.RightTrigger.RightTriggerLogic;
//import org.firstinspires.ftc.teamcode.Subsystems.Control.Buttons.RightBumper.RightBumperLogic;
import org.firstinspires.ftc.teamcode.Subsystems.Control.Buttons.Square.SquareLogic;
import org.firstinspires.ftc.teamcode.Subsystems.Control.Buttons.Cross.CrossLogic;


public class ButtonControl {
    private final EdgeDetection edgeDetection;
    private final RightTriggerLogic rightTriggerLogic = new RightTriggerLogic();
//    private final RightBumperLogic rightBumperLogic = new RightBumperLogic();
//    private final LeftTriggerLogic leftTriggerLogic = new LeftTriggerLogic();
//    private final LeftBumperLogic leftBumperLogic;
    private final SquareLogic squareLogic = new SquareLogic();
    private final CrossLogic crossLogic = new CrossLogic();
//    private final CircleLogic circleLogic = new CircleLogic();
//    private final DpadRightLogic dpadRightLogic = new DpadRightLogic();
//    private final DpadDownLogic dpadDownLogic = new DpadDownLogic();
//    private final DpadUpLogic dpadUpLogic = new DpadUpLogic();
//    private final DpadLeftLogic dpadLeftLogic = new DpadLeftLogic();

    public ButtonControl(EdgeDetection edgeDetection, SensorControl sensorControl, MotorControl motorControl) {
        this.edgeDetection = edgeDetection;
    }

    public void update() {
        updateLogic();
    }

    private void updateLogic(){

//        if(edgeDetection.rising(GamepadIndexValues.dpadRight))
//            dpadRightLogic.update();
//
//        if(edgeDetection.rising(GamepadIndexValues.dpadDown))
//            dpadDownLogic.update();

        if(edgeDetection.rising(GamepadIndexValues.rightTrigger))
            rightTriggerLogic.update();

//        if(edgeDetection.rising(GamepadIndexValues.rightBumper))
//            rightBumperLogic.update();
//
//        if(edgeDetection.rising(GamepadIndexValues.leftTrigger))
//            leftTriggerLogic.update();
//
//        if(edgeDetection.rising(GamepadIndexValues.leftBumper))
//            leftBumperLogic.update();

        if(edgeDetection.rising(GamepadIndexValues.square))
            squareLogic.update();

        if(edgeDetection.rising(GamepadIndexValues.cross))
            crossLogic.update();

//        if(edgeDetection.rising(GamepadIndexValues.circle))
//            circleLogic.update();
//
//        if(edgeDetection.rising(GamepadIndexValues.dpadUp))
//            dpadUpLogic.update();
//
//        if(edgeDetection.rising(GamepadIndexValues.dpadLeft))
//            dpadLeftLogic.update();
    }
}