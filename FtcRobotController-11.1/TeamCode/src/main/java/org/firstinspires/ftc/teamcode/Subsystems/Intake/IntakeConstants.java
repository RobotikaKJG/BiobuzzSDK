package org.firstinspires.ftc.teamcode.Subsystems.Intake;

public class IntakeConstants {
    public static double intakeSpeed = 0.8;

    public static double transferSpeed = 0.8;

    public static double transfer2Speed = 0.8;

    public static double lockServoLockedPos = 0;
    public static double lockServoUnlockedPos = 0.25;

    public static double lockServoMinPos = Math.min(lockServoLockedPos, lockServoUnlockedPos);
    public static double lockServoMaxPos = Math.max(lockServoLockedPos, lockServoUnlockedPos);
}
