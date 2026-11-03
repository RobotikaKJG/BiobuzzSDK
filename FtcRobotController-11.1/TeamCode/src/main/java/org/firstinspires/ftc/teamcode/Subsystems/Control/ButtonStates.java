package org.firstinspires.ftc.teamcode.Subsystems.Control;

//import org.firstinspires.ftc.teamcode.Subsystems.Control.Buttons.DpadDown.DpadDownStates;
//import org.firstinspires.ftc.teamcode.Subsystems.Control.Buttons.DpadLeft.DpadLeftStates;
//import org.firstinspires.ftc.teamcode.Subsystems.Control.Buttons.DpadRight.DpadRightStates;
//import org.firstinspires.ftc.teamcode.Subsystems.Control.Buttons.DpadUp.DpadUpStates;
import org.firstinspires.ftc.teamcode.Subsystems.Control.Buttons.Circle.CircleLogic;
import org.firstinspires.ftc.teamcode.Subsystems.Control.Buttons.Circle.CircleStates;
import org.firstinspires.ftc.teamcode.Subsystems.Control.Buttons.DpadRight.DpadRightStates;
import org.firstinspires.ftc.teamcode.Subsystems.Control.Buttons.DpadUp.DpadUpStates;
import org.firstinspires.ftc.teamcode.Subsystems.Control.Buttons.LeftBumper.LeftBumperStates;
//import org.firstinspires.ftc.teamcode.Subsystems.Control.Buttons.LeftTrigger.LeftTriggerStates;
import org.firstinspires.ftc.teamcode.Subsystems.Control.Buttons.LeftTrigger.LeftTriggerStates;
import org.firstinspires.ftc.teamcode.Subsystems.Control.Buttons.RightTrigger.RightTriggerStates;
import org.firstinspires.ftc.teamcode.Subsystems.Control.Buttons.RightBumper.RightBumperStates;
import org.firstinspires.ftc.teamcode.Subsystems.Control.Buttons.Square.SquareStates;
import org.firstinspires.ftc.teamcode.Subsystems.Control.Buttons._Gamepad2.DpadDownG2.DpadDownStatesG2;
import org.firstinspires.ftc.teamcode.Subsystems.Control.Buttons._Gamepad2.DpadLeftG2.DpadLeftStatesG2;
import org.firstinspires.ftc.teamcode.Subsystems.Control.Buttons._Gamepad2.DpadRightG2.DpadRightStatesG2;
import org.firstinspires.ftc.teamcode.Subsystems.Control.Buttons._Gamepad2.DpadUpG2.DpadUpStatesG2;
import org.firstinspires.ftc.teamcode.Subsystems.Control.Buttons._Gamepad2.LeftBumperG2.LeftBumperStatesG2;
import org.firstinspires.ftc.teamcode.Subsystems.Control.Buttons._Gamepad2.LeftTriggerG2.LeftTriggerStatesG2;
import org.firstinspires.ftc.teamcode.Subsystems.Control.Buttons._Gamepad2.RightBumperG2.RightBumperStatesG2;
import org.firstinspires.ftc.teamcode.Subsystems.Control.Buttons._Gamepad2.RightTriggerG2.RightTriggerStatesG2;

public class ButtonStates {
    private static LeftBumperStates leftBumperStates = LeftBumperStates.idle;
    private static LeftTriggerStates leftTriggerStates = LeftTriggerStates.idle;
    private static RightBumperStates rightBumperStates = RightBumperStates.idle;
    private static RightTriggerStates rightTriggerStates = RightTriggerStates.idle;
    private static SquareStates squareStates = SquareStates.idle;
    private static CircleStates circleStates = CircleStates.idle;
    private static DpadRightStates dpadRightStates = DpadRightStates.idle;
//    private static DpadDownStates dpadDownStates = DpadDownStates.idle;
    private static DpadUpStates dpadUpStates = DpadUpStates.idle;
//    private static DpadLeftStates dpadLeftStates = DpadLeftStates.idle;

    private static DpadDownStatesG2 dpadDownStatesG2 = DpadDownStatesG2.idle;
    private static DpadUpStatesG2 dpadUpStatesG2 = DpadUpStatesG2.idle;
    private static DpadRightStatesG2 dpadRightStatesG2 = DpadRightStatesG2.idle;
    private static DpadLeftStatesG2 dpadLeftStatesG2 = DpadLeftStatesG2.idle;
    private static LeftBumperStatesG2 leftBumperStatesG2 = LeftBumperStatesG2.idle;
    private static LeftTriggerStatesG2 leftTriggerStatesG2 = LeftTriggerStatesG2.idle;
    private static RightBumperStatesG2 rightBumperStatesG2 = RightBumperStatesG2.idle;
    private static RightTriggerStatesG2 rightTriggerStatesG2 = RightTriggerStatesG2.idle;

    public static void setInitialStates(){
        leftBumperStates = LeftBumperStates.idle;
        leftTriggerStates = LeftTriggerStates.idle;
        rightBumperStates = RightBumperStates.idle;
        rightTriggerStates = RightTriggerStates.idle;
        squareStates = SquareStates.idle;
        circleStates = CircleStates.idle;
        dpadRightStates = DpadRightStates.idle;
//        dpadDownStates = DpadDownStates.idle;
        dpadUpStates = DpadUpStates.idle;
//        dpadLeftStates = DpadLeftStates.idle;

        dpadDownStatesG2 = DpadDownStatesG2.idle;
        dpadUpStatesG2 = DpadUpStatesG2.idle;
        dpadRightStatesG2 = DpadRightStatesG2.idle;
        dpadLeftStatesG2 = DpadLeftStatesG2.idle;
        leftBumperStatesG2 = LeftBumperStatesG2.idle;
        leftTriggerStatesG2 = LeftTriggerStatesG2.idle;
        rightBumperStatesG2 = RightBumperStatesG2.idle;
        rightTriggerStatesG2 = RightTriggerStatesG2.idle;
    }

    public static LeftBumperStates getLeftBumperState() {
        return  leftBumperStates;
    }

    public static void setLeftBumperState(LeftBumperStates state) {
        leftBumperStates = state;
    }

    public static LeftTriggerStates getLeftTriggerState() {
        return leftTriggerStates;
    }

    public static void setLeftTriggerState(LeftTriggerStates state) {
        leftTriggerStates = state;
    }

    public static RightBumperStates getRightBumperState() {
        return rightBumperStates;
    }

    public static void setRightBumperState(RightBumperStates state) {
        rightBumperStates = state;
    }


    public static RightTriggerStates getRightTriggerState() {
        return rightTriggerStates;
    }

    public static void setRightTriggerState(RightTriggerStates state) {
        rightTriggerStates = state;
    }

    public static SquareStates getSquareState() {
        return squareStates;
    }

    public static void setSquareState(SquareStates state) {
        squareStates = state;
    }

    public static CircleStates getCircleState() {
        return circleStates;
    }

    public static void setCircleState(CircleStates state) {
        circleStates = state;
    }

    public static DpadRightStates getDpadRightState() {
        return dpadRightStates;
    }

    public static void setDpadRightState(DpadRightStates state) {
        dpadRightStates = state;
    }

    public static DpadUpStates getDpadUpState() {
        return dpadUpStates;
    }

    public static void setDpadUpState(DpadUpStates state) {
        dpadUpStates = state;
    }

    public static DpadDownStatesG2 getDpadDownStateG2() {
        return dpadDownStatesG2;
    }

    public static void setDpadDownStateG2(DpadDownStatesG2 state) {
        dpadDownStatesG2 = state;
    }

    public static DpadUpStatesG2 getDpadUpStateG2() {
        return dpadUpStatesG2;
    }

    public static void setDpadUpStateG2(DpadUpStatesG2 state) {
        dpadUpStatesG2 = state;
    }

    public static DpadRightStatesG2 getDpadRightStateG2() {
        return dpadRightStatesG2;
    }

    public static void setDpadRightStateG2(DpadRightStatesG2 state) {
        dpadRightStatesG2 = state;
    }

    public static DpadLeftStatesG2 getDpadLeftStateG2() {
        return dpadLeftStatesG2;
    }

    public static void setDpadLeftStateG2(DpadLeftStatesG2 state) {
        dpadLeftStatesG2 = state;
    }

    public static LeftBumperStatesG2 getLeftBumperStateG2() {
        return leftBumperStatesG2;
    }

    public static void setLeftBumperStateG2(LeftBumperStatesG2 state) {
        leftBumperStatesG2 = state;
    }

    public static LeftTriggerStatesG2 getLeftTriggerStateG2() {
        return leftTriggerStatesG2;
    }

    public static void setLeftTriggerStateG2(LeftTriggerStatesG2 state) {
        leftTriggerStatesG2 = state;
    }

    public static RightBumperStatesG2 getRightBumperStateG2() {
        return rightBumperStatesG2;
    }

    public static void setRightBumperStateG2(RightBumperStatesG2 state) {
        rightBumperStatesG2 = state;
    }

    public static RightTriggerStatesG2 getRightTriggerStateG2() {
        return rightTriggerStatesG2;
    }

    public static void setRightTriggerStateG2(RightTriggerStatesG2 state) {
        rightTriggerStatesG2 = state;
    }
}