package org.firstinspires.ftc.teamcode.Subsystems.Control;

import org.firstinspires.ftc.teamcode.HardwareInterface.Gamepad.GamepadIndexValues;
import org.firstinspires.ftc.teamcode.HardwareInterface.Gamepad.EdgeDetection;
import org.firstinspires.ftc.teamcode.HardwareInterface.Motor.MotorControl;
import org.firstinspires.ftc.teamcode.HardwareInterface.Sensor.SensorControl;
//import org.firstinspires.ftc.teamcode.Subsystems.Control.Buttons.DpadDown.DpadDownLogic;
//import org.firstinspires.ftc.teamcode.Subsystems.Control.Buttons.DpadLeft.DpadLeftLogic;
//import org.firstinspires.ftc.teamcode.Subsystems.Control.Buttons.DpadRight.DpadRightLogic;
//import org.firstinspires.ftc.teamcode.Subsystems.Control.Buttons.DpadUp.DpadUpLogic;
import org.firstinspires.ftc.teamcode.Subsystems.Control.Buttons.Circle.CircleLogic;
import org.firstinspires.ftc.teamcode.Subsystems.Control.Buttons.DpadRight.DpadRightLogic;
import org.firstinspires.ftc.teamcode.Subsystems.Control.Buttons.DpadUp.DpadUpLogic;
import org.firstinspires.ftc.teamcode.Subsystems.Control.Buttons.LeftBumper.LeftBumperLogic;
//import org.firstinspires.ftc.teamcode.Subsystems.Control.Buttons.LeftTrigger.LeftTriggerLogic;
import org.firstinspires.ftc.teamcode.Subsystems.Control.Buttons.LeftTrigger.LeftTriggerLogic;
import org.firstinspires.ftc.teamcode.Subsystems.Control.Buttons.RightTrigger.RightTriggerLogic;
import org.firstinspires.ftc.teamcode.Subsystems.Control.Buttons.RightBumper.RightBumperLogic;
import org.firstinspires.ftc.teamcode.Subsystems.Control.Buttons.Square.SquareLogic;
import org.firstinspires.ftc.teamcode.Subsystems.Control.Buttons._Gamepad2.DpadDownG2.DpadDownLogicG2;
import org.firstinspires.ftc.teamcode.Subsystems.Control.Buttons._Gamepad2.DpadLeftG2.DpadLeftLogicG2;
import org.firstinspires.ftc.teamcode.Subsystems.Control.Buttons._Gamepad2.DpadRightG2.DpadRightLogicG2;
import org.firstinspires.ftc.teamcode.Subsystems.Control.Buttons._Gamepad2.DpadUpG2.DpadUpLogicG2;
import org.firstinspires.ftc.teamcode.Subsystems.Control.Buttons._Gamepad2.LeftBumperG2.LeftBumperLogicG2;
import org.firstinspires.ftc.teamcode.Subsystems.Control.Buttons._Gamepad2.LeftTriggerG2.LeftTriggerLogicG2;
import org.firstinspires.ftc.teamcode.Subsystems.Control.Buttons._Gamepad2.RightBumperG2.RightBumperLogicG2;
import org.firstinspires.ftc.teamcode.Subsystems.Control.Buttons._Gamepad2.RightTriggerG2.RightTriggerLogicG2;


import org.firstinspires.ftc.teamcode.Subsystems.Outtake.TurretServo.TurretServoControl;


public class ButtonControl {
    private final EdgeDetection edgeDetection;
    private final RightTriggerLogic rightTriggerLogic = new RightTriggerLogic();
    private final RightBumperLogic rightBumperLogic = new RightBumperLogic();
    private final LeftTriggerLogic leftTriggerLogic = new LeftTriggerLogic();
    private final LeftBumperLogic leftBumperLogic;
    private final SquareLogic squareLogic = new SquareLogic();
    private final CircleLogic circleLogic = new CircleLogic();
    private final DpadRightLogic dpadRightLogic = new DpadRightLogic();
//    private final DpadDownLogic dpadDownLogic = new DpadDownLogic();
    private final DpadUpLogic dpadUpLogic = new DpadUpLogic();
//    private final DpadLeftLogic dpadLeftLogic = new DpadLeftLogic();
    
    private final DpadRightLogicG2 dpadRightLogicG2;
    private final DpadUpLogicG2 dpadUpLogicG2;
    private final DpadDownLogicG2 dpadDownLogicG2;
    private final DpadLeftLogicG2 dpadLeftLogicG2;
    private final LeftTriggerLogicG2 leftTriggerLogicG2 = new LeftTriggerLogicG2();
    private final LeftBumperLogicG2 leftBumperLogicG2 = new LeftBumperLogicG2();
    private final RightTriggerLogicG2 rightTriggerLogicG2 = new RightTriggerLogicG2();
    private final RightBumperLogicG2 rightBumperLogicG2 = new RightBumperLogicG2();

    public ButtonControl(EdgeDetection edgeDetection, SensorControl sensorControl, MotorControl motorControl, TurretServoControl turretServoControl) {
        this.edgeDetection = edgeDetection;
        this.leftBumperLogic = new LeftBumperLogic(motorControl);

        this.dpadRightLogicG2 = new DpadRightLogicG2(turretServoControl);
        this.dpadUpLogicG2 = new DpadUpLogicG2(turretServoControl);
        this.dpadDownLogicG2 = new DpadDownLogicG2(turretServoControl);
        this.dpadLeftLogicG2 = new DpadLeftLogicG2(turretServoControl);
    }

    public void update() {
        updateLogic();
    }

    private void updateLogic(){

        if(edgeDetection.rising(GamepadIndexValues.dpadRight))
            dpadRightLogic.update();
//
//        if(edgeDetection.rising(GamepadIndexValues.dpadDown))
//            dpadDownLogic.update();

        if(edgeDetection.rising(GamepadIndexValues.rightTrigger))
            rightTriggerLogic.update();

        if(edgeDetection.rising(GamepadIndexValues.rightBumper))
            rightBumperLogic.update();

        if(edgeDetection.rising(GamepadIndexValues.leftTrigger))
            leftTriggerLogic.update();

        if(edgeDetection.rising(GamepadIndexValues.leftBumper))
            leftBumperLogic.update();

        if(edgeDetection.rising(GamepadIndexValues.square))
            squareLogic.update();

        if(edgeDetection.rising(GamepadIndexValues.circle))
            circleLogic.update();

        if(edgeDetection.rising(GamepadIndexValues.dpadUp))
            dpadUpLogic.update();

//        if(edgeDetection.rising(GamepadIndexValues.dpadLeft))
//            dpadLeftLogic.update();
        
        if(edgeDetection.risingG2(GamepadIndexValues.dpadUp))
            dpadUpLogicG2.update();
        
        if(edgeDetection.risingG2(GamepadIndexValues.dpadDown))
            dpadDownLogicG2.update();
        
        if(edgeDetection.risingG2(GamepadIndexValues.dpadLeft))
            dpadLeftLogicG2.update();
        
        if(edgeDetection.risingG2(GamepadIndexValues.dpadRight))
            dpadRightLogicG2.update();
        
        if(edgeDetection.risingG2(GamepadIndexValues.leftTrigger))
            leftTriggerLogicG2.update();
        
        if(edgeDetection.risingG2(GamepadIndexValues.leftBumper))
            leftBumperLogicG2.update();
        
        if(edgeDetection.risingG2(GamepadIndexValues.rightTrigger))
            rightTriggerLogicG2.update();
        
        if(edgeDetection.risingG2(GamepadIndexValues.rightBumper))
            rightBumperLogicG2.update();
    }
}