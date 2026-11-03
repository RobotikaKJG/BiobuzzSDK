package org.firstinspires.ftc.teamcode.Main;

import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.Autonomous.AutonomousControl;
import org.firstinspires.ftc.teamcode.Autonomous.Autos.AudienceAuton.AudienceAuton;
import org.firstinspires.ftc.teamcode.Autonomous.Autos.GoalAuton.GoalAuton;
import org.firstinspires.ftc.teamcode.Autonomous.Autos.GoalAutonSolo.GoalAutonSolo;

public class AutonomousDependencies extends Dependencies {

    public final AutonomousControl autonomousControl;

    public AutonomousDependencies(HardwareMap hardwareMap, Gamepad gamepad1, Gamepad gamepad2, Telemetry telemetry) {
        super(hardwareMap, gamepad1, gamepad2, telemetry);
        autonomousControl = createAutonomousControl();
    }

    public GoalAuton createGoalAuton() {
        return new GoalAuton(follower);
    }
    public GoalAutonSolo createGoalAutonSolo() {
        return new GoalAutonSolo(follower);
    }
    public AudienceAuton createAudienceAuton() {
        return new AudienceAuton(follower, sensorControl);
    }

    public AutonomousControl createAutonomousControl() {
        return new AutonomousControl(motorControl, createGoalAuton(), createGoalAutonSolo(), createAudienceAuton(),createIntakeControl(),createOuttakeControl(), turretServoControl, sensorControl);
    }
}