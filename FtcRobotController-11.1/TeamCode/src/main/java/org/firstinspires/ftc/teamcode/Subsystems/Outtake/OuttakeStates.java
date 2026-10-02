package org.firstinspires.ftc.teamcode.Subsystems.Outtake;

import org.firstinspires.ftc.teamcode.Main.GlobalVariables;
import org.firstinspires.ftc.teamcode.Subsystems.Outtake.OuttakeMotor.OuttakeMotorStates;
import org.firstinspires.ftc.teamcode.Subsystems.SubsystemState;

public class OuttakeStates {
    private static SubsystemState outtakeState = SubsystemState.Idle;
    private static OuttakeMotorStates outtakeMotorState = OuttakeMotorStates.idle;
    private static volatile boolean turretTrackingEnabled = true; // read cross-thread (telemetry)

    public static void setInitialStates() {
        outtakeState = SubsystemState.Idle;
        outtakeMotorState = OuttakeMotorStates.idle;
        turretTrackingEnabled = true;
        if(!GlobalVariables.isAutonomous) outtakeMotorState = OuttakeMotorStates.forwardClose;
    }

    public static SubsystemState getOuttakeState() {
        return outtakeState;
    }

    public static void setOuttakeState(SubsystemState state) {
        outtakeState = state;
    }

    public static OuttakeMotorStates getOuttakeMotorState() {
        return outtakeMotorState;
    }

    public static void setOuttakeMotorState(OuttakeMotorStates state) {outtakeMotorState = state;}

    public static boolean isTurretTrackingEnabled() {
        return turretTrackingEnabled;
    }

    public static void toggleTurretTracking() {
        turretTrackingEnabled = !turretTrackingEnabled;
    }
}