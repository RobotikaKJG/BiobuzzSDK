package org.firstinspires.ftc.teamcode.Autonomous;

import com.qualcomm.robotcore.hardware.Gamepad;

import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.teamcode.HardwareInterface.Gamepad.EdgeDetection;
import org.firstinspires.ftc.teamcode.Main.Alliance;
import org.firstinspires.ftc.teamcode.HardwareInterface.Gamepad.GamepadIndexValues;
import org.firstinspires.ftc.teamcode.Main.GlobalVariables;

public class SelectStartVariables {
    Gamepad gamepad1;
    Gamepad currentGamepad1 = new Gamepad();
    Gamepad prevGamepad1 = new Gamepad();
    Telemetry telemetry;
    EdgeDetection edgeDetection;
    private boolean risingTriangleEdge;
    private boolean risingSquareEdge;
    private boolean risingDpadUpEdge;
    private boolean risingDpadLeftEdge;
    private boolean risingDpadRightEdge;

    public SelectStartVariables(Gamepad gamepad1, Telemetry telemetry) {
        this.gamepad1 = gamepad1;
        this.telemetry = telemetry;
        currentGamepad1.copy(this.gamepad1);
        edgeDetection = new EdgeDetection();
        selectAuton();
        selectAlliance();
        selectGateTotal();
    }

    private void selectAuton() {
        while (!risingDpadUpEdge && !risingDpadLeftEdge && !risingDpadRightEdge) {
            calculateGamepadValues();

            telemetry.addLine("Press dpad up for audience side, dpad left for goal side, dpad right for goal side solo");
            telemetry.update();
            if (risingDpadUpEdge)
                GlobalVariables.autonomousMode = AutonomousMode.audienceSide;
            if (risingDpadLeftEdge)
                GlobalVariables.autonomousMode = AutonomousMode.goalSide;
            if (risingDpadRightEdge)
                GlobalVariables.autonomousMode = AutonomousMode.goalSideSolo;

        }
    }

    private void selectAlliance() {
        risingTriangleEdge = false;
        risingSquareEdge = false;
        while (!risingTriangleEdge && !risingSquareEdge) {
            calculateGamepadValues();

            telemetry.addLine("Press triangle for RED, press square for BLUE");
            telemetry.update();
            if (risingTriangleEdge)
                GlobalVariables.alliance = Alliance.Red;
            if (risingSquareEdge)
                GlobalVariables.alliance = Alliance.Blue;

        }
    }

    private void selectGateTotal() {
        risingTriangleEdge = false;
        risingSquareEdge = false;
        while (!risingTriangleEdge && !risingSquareEdge) {
            calculateGamepadValues();

            telemetry.addLine("Press triangle for 3 GATES, press square for 4 GATES");
            telemetry.update();
            if (risingTriangleEdge)
                GlobalVariables.gateTotal = 3;
            if (risingSquareEdge)
                GlobalVariables.gateTotal = 4;
        }
    }

    private void calculateGamepadValues() {
        prevGamepad1.copy(currentGamepad1);
        currentGamepad1.copy(gamepad1);
        edgeDetection.refreshGamepadIndex(gamepad1, prevGamepad1);
        risingTriangleEdge = edgeDetection.rising(GamepadIndexValues.triangle);
        risingSquareEdge = edgeDetection.rising(GamepadIndexValues.square);
        risingDpadUpEdge = edgeDetection.rising(GamepadIndexValues.dpadUp);
        risingDpadLeftEdge = edgeDetection.rising(GamepadIndexValues.dpadLeft);
        risingDpadRightEdge = edgeDetection.rising(GamepadIndexValues.dpadRight);
    }
}