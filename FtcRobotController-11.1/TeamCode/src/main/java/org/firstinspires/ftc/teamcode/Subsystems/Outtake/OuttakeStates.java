package org.firstinspires.ftc.teamcode.Subsystems.Outtake;

import org.firstinspires.ftc.teamcode.Subsystems.Outtake.AutoOuttakeFarClose.AutoOuttakeFarCloseStates;
import org.firstinspires.ftc.teamcode.Subsystems.Outtake.AutoResetPos.AutoResetPosStates;
import org.firstinspires.ftc.teamcode.Subsystems.Outtake.TurretServo.TurretServoStates;
import org.firstinspires.ftc.teamcode.Subsystems.Outtake.AutoCycleShoot.AutoCycleShootStates;
import org.firstinspires.ftc.teamcode.Subsystems.Outtake.OuttakeMotor.OuttakeMotorStates;
import org.firstinspires.ftc.teamcode.Subsystems.SubsystemState;

public class OuttakeStates {
    private static SubsystemState outtakeState = SubsystemState.Idle;
    private static OuttakeMotorStates outtakeMotorState = OuttakeMotorStates.idle;
    private static volatile AutoCycleShootStates autoCycleShootStates = AutoCycleShootStates.idle; // read cross-thread (cycle timer)
    private static TurretServoStates turretServoState = TurretServoStates.idle;
    private static AutoOuttakeFarCloseStates autoOuttakeFarCloseState = AutoOuttakeFarCloseStates.idle;
    private static AutoResetPosStates autoResetPosState = AutoResetPosStates.idle;
    private static volatile boolean turretTrackingEnabled = true; // read cross-thread (telemetry)

    public static void setInitialStates() {
        outtakeState = SubsystemState.Idle;
        outtakeMotorState = OuttakeMotorStates.idle;
        autoCycleShootStates = AutoCycleShootStates.idle;
        turretServoState = TurretServoStates.tracking;
        autoOuttakeFarCloseState = AutoOuttakeFarCloseStates.idle;
        autoResetPosState = AutoResetPosStates.idle;
        turretTrackingEnabled = true;
    }

    public static SubsystemState getOuttakeState() {
        return outtakeState;
    }

    public static void setOuttakeState(SubsystemState state) {
        outtakeState = state;
    }

    public static OuttakeMotorStates getMotorState() {
        return outtakeMotorState;
    }

    public static void setMotorState(OuttakeMotorStates state) {outtakeMotorState = state;}

    public static AutoCycleShootStates getAutoCycleShootState() {
        return autoCycleShootStates;
    }

    public static void setAutoCycleShootState(AutoCycleShootStates state) {autoCycleShootStates = state;}

    public static TurretServoStates getTurretServoState() {
        return turretServoState;
    }

    public static void setTurretServoState(TurretServoStates state) {
        turretServoState = state;
    }

    public static void setAutoOuttakeFarCloseState(AutoOuttakeFarCloseStates state) {
        autoOuttakeFarCloseState = state;
    }

    public static AutoOuttakeFarCloseStates getAutoOuttakeFarCloseState() {
        return autoOuttakeFarCloseState;
    }

    public static void setAutoResetPosState(AutoResetPosStates state) {
        autoResetPosState = state;
    }

    public static AutoResetPosStates getAutoResetPosState() {
        return autoResetPosState;
    }

    public static boolean isTurretTrackingEnabled() {
        return turretTrackingEnabled;
    }

    public static void toggleTurretTracking() {
        turretTrackingEnabled = !turretTrackingEnabled;
    }
}