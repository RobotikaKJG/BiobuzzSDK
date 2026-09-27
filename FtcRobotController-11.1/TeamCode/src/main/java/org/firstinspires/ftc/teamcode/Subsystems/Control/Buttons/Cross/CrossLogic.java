//package org.firstinspires.ftc.teamcode.Subsystems.Control.Buttons.Cross;
//
//import org.firstinspires.ftc.teamcode.Subsystems.Control.ButtonStates;
//import org.firstinspires.ftc.teamcode.Subsystems.Control.Buttons.RightTrigger.RightTriggerStates;
//import org.firstinspires.ftc.teamcode.Subsystems.Intake.IntakeMotor.IntakeMotorStates;
//import org.firstinspires.ftc.teamcode.Subsystems.Intake.IntakeStates;
//import org.firstinspires.ftc.teamcode.Subsystems.Intake.TransferMotor.TransferMotorStates;
//import org.firstinspires.ftc.teamcode.Subsystems.Outtake.OuttakeMotor.OuttakeMotorStates;
//import org.firstinspires.ftc.teamcode.Subsystems.Outtake.OuttakeStates;
//
//public class CrossLogic {
//    private final CrossControl crossControl = new CrossControl();
//
//    public void update() {
//        if (resetPos()) return;
//        stopTransfer();
//    }
//
//    private void completeAction(){
//        crossControl.update();
//        ButtonStates.setCrossState(CrossStates.idle);
//    }
//
//    private boolean resetPos() {
//        if(IntakeStates.getTransferMotorState() != TransferMotorStates.idle) return false;
//
//        ButtonStates.setCrossState(CrossStates.activate);
//        completeAction();
//        return true;
//    }
//
//    private void stopTransfer() {
//        ButtonStates.setCrossState(CrossStates.stop);
//        completeAction();
//    }
//
//}
