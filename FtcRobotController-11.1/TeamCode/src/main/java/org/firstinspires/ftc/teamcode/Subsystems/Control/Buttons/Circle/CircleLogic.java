package org.firstinspires.ftc.teamcode.Subsystems.Control.Buttons.Circle;

import org.firstinspires.ftc.teamcode.Subsystems.Control.ButtonStates;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.IntakeStates;
import org.firstinspires.ftc.teamcode.Subsystems.Outtake.OuttakeStates;
import org.firstinspires.ftc.teamcode.Subsystems.Outtake.TurretServo.TurretServoStates;

public class CircleLogic {
    private final CircleControl circleControl = new CircleControl();

    public void update() {
    }

    private void completeAction(){
        circleControl.update();
        ButtonStates.setCircleState(CircleStates.idle);
    }
}
