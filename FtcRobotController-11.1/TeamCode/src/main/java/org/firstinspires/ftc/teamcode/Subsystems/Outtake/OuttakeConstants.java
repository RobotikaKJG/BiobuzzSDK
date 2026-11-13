package org.firstinspires.ftc.teamcode.Subsystems.Outtake;

public class OuttakeConstants {
    // Turret servo position commanded during INIT, before tracking starts.
    public static double turretStartPos = 0.5;
    // Midpoint of the observed red/blue positions (about 23 turret degrees from blue).
    public static double blueTurretStartPos = 0.2014;
    // Added to the calculated turret aiming angle while tracking after START.
    public static double turretAimOffsetDeg = 45.0;
    // Gate servo positions used during startup and ball feeding.
    public static double lockServoLockedPos = 0.048; // 
    public static double lockServoUnlockedPos = 0.34;

    public static double lockServoMinPos = Math.min(lockServoLockedPos, lockServoUnlockedPos);
    public static double lockServoMaxPos = Math.max(lockServoLockedPos, lockServoUnlockedPos);

    public static double turretServo1Mult = 1.0;
    public static double turretServo2Mult = 1.0;

    public static double turretServoMax = 0.9;
    public static double turretServoMin = 0.1;

    // These bound the same physical travel the turret has always had; because
    // turretDirection is -1, the RIGHT limit is the one near turretServoMax:
    // -100 -> pos 0.81. Anything past that presses the turret into its hard stop
    // and stalls it (the old limit-to-limit jam during fast drivetrain rotation).
    public static double turretLimitRight = -198.0;
    public static double turretLimitLeft = 198.0;
    public static double turretGearRatio = 20.0 / 13.0;
    public static double turretServoTravel = 349.0;

    // Sign of the angle -> servo-position mapping. -1 because increasing servo
    // position rotates the turret toward negative (right) angles; flip this if
    // the turret ever aims to the side opposite the target.
    public static double turretDirection = -1.0;

    public static double turretServoSlewPerSec = 1.8;

    public static double turretDeadZoneHysteresisDeg = 20.0;

    public static double turretServo1Max = turretServoMax * turretServo1Mult;
    public static double turretServo2Max = turretServoMax * turretServo2Mult;

    public static double turretServo1Min = turretServoMin * turretServo1Mult;
    public static double turretServo2Min = turretServoMin * turretServo2Mult;

    public static double maxDistance = 98.43;
    public static double minDistance = 53.94;

    public static double oneBallWait = 0.15;
    // Delay in AutoCycleShootLogic.activate(), BEFORE the state machine reaches
    // turnTransfer. The latch is still locked here, so this is not latch travel time.
    public static double servoOpenWait = 0.15;

    // Latch travel time: AutoCycleShootControl.turnTransfer unlocks the latch and starts
    // the transfer in the same tick, so the transfer used to push balls into a latch that
    // had not moved yet. Hold the intake/transfer this long after the unlock command.
    // Diagnostic value — far longer than servo travel should need. If it still jams at
    // 2 s the cause is not timing (check latch command / position limits / binding).
    public static double latchOpenSettleSec = 2.0;
    public static double deactivateAfter = 0.3;

    // TeleOp auto-finish for a shot: feed until both ball sensors have read empty
    // CONTINUOUSLY for shootClearHoldSec (so the brief gaps while a ball is in transit
    // between the sensors don't end the shot early — that cut the 3rd ball — and the
    // last ball has time to launch), or until the max feed time as a safety fallback,
    // then stop and close the gate. Biased toward firing all 3 (over-feed a hair rather
    // than cut a ball): raise shootClearHoldSec if a ball is ever left unfired.
    public static double shootFeedMinSec = 0.15;    // ignore "empty" in the first moments
    public static double shootClearHoldSec = 0.5;   // queue must stay empty this long to stop
    public static double shootFeedMaxSec = 3.5;     // hard cap (jam / sensor failure)

    // Transfer feed gating (TeleOp), ticks/s:
    // 1) Arm near target OR settled cruise (= pre-1st-ball RPM), feed through 1 / 2.
    // 2) After first drop has recovered to that pre-1st level once, further feed
    //    waits until velocity is back to armed − feedResumeMarginTicks (3rd ball).
    // Commanded target is often unreachable (e.g. 3600 cmd / ~3400 cruise); settle
    // frac lets us arm on real cruise instead of blocking forever at 98% of target.
    public static double feedArmTargetFrac = 0.98;      // preferred: this close to target to arm
    public static double feedArmSettleFrac = 0.90;      // fallback: arm on actual cruise ≥ this × target
    public static double feedFirstDropTicks = 80;       // ~170 RPM dip counts as first ball
    public static double feedResumeMarginTicks = 40;    // ~85 RPM under pre-1st-ball baseline

    // Close-mode recovery: slam to +100% power as soon as we're a little under
    // target (shot dip), and stay there until nearly back — not wait for ~214 RPM.
    public static double recoverEnterTicks = 25;        // ~54 RPM under → full power
    public static double recoverExitTicks = 35;         // stay at 100% until within ~75 RPM

    public static double resetWait = 100.0;

    // Close family +1% vs prior tune (1680 / 1600 / 1800 / 3400 RPM floor).
    public static double outtakeSpeedCloseClose = 1616;
    public static double outtakeSpeedFar = 2190;
    public static double outtakeSpeedClose = 1697;
    public static double outtakeSpeedCloseFar = 1818;

    // Close-mode distance→velocity (ticks/s). Anchored at the empirically tuned
    // 275 cm / outtakeSpeedClose shot; closer uses v = v0 * sqrt(d/d0), clamped.
    // Beyond closeMaxDistanceCm, forwardFar / far toggle owns long shots.
    public static double closeCalDistanceCm = 275.0;
    public static double closeCalTicks = outtakeSpeedClose; // 1697
    public static double closeMinDistanceCm = 140.0;
    public static double closeMaxDistanceCm = 290.0; // small margin past calibration
    // Floor at ~3434 RPM (3400×1.01; 28 PPR → ticks/s = RPM * 28/60).
    public static double closeMinTicks = 1603; // 3434 * 28 / 60
    public static double closeMaxTicks = outtakeSpeedClose;

    // Localizer under-reads goal range vs tape (~260 shown when ~270 true).
    // Applied to close RPM curve and shooter-log `d` (dashboard).
    public static double goalDistanceOffsetCm = 10.0;

    /** Raw localizer inches → corrected inches (negative / invalid unchanged). */
    public static double correctedDistanceInches(double rawInches) {
        if (rawInches < 0 || Double.isNaN(rawInches)) return rawInches;
        return rawInches + goalDistanceOffsetCm / 2.54;
    }

    public static double farShootingThreshold = 2300;
    public static double targetSpeedThreshold = 0.02;

}
