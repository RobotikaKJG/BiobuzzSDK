package org.firstinspires.ftc.teamcode.Subsystems.Intake;

public class IntakeConstants {
    public static double stopFeederAfter = 0.1;

    public static double lockServoLockedPos = 0.048;
    public static double lockServoUnlockedPos = 0.34;

    public static double lockServoMinPos = Math.min(lockServoLockedPos, lockServoUnlockedPos);
    public static double lockServoMaxPos = Math.max(lockServoLockedPos, lockServoUnlockedPos);

    public static double checkAgainAfter = 0.1;

    // A spinning artifact alternates shell and holes past the intake IR sensors, so a
    // single sample can land on a hole and read "clear" while the ball is right there.
    // The intake presence latch therefore only releases after the sensors have been
    // CONTINUOUSLY clear for this long. Must exceed the longest hole-gap at intake spin
    // speed; lower it until chatter reappears, then back off. Intake only — the transfer
    // pair sees a parked, non-spinning ball and needs no such filter.
    public static double intakeReleaseHoldSec = 0.25;
}
