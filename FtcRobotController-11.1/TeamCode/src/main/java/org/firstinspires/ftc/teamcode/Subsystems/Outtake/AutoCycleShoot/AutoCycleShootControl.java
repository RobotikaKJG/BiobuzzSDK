package org.firstinspires.ftc.teamcode.Subsystems.Outtake.AutoCycleShoot;

import org.firstinspires.ftc.teamcode.HardwareInterface.Motor.MotorConstants;
import org.firstinspires.ftc.teamcode.HardwareInterface.Motor.MotorControl;
import org.firstinspires.ftc.teamcode.Main.GlobalVariables;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.AutoIntakeTransfer.AutoIntakeTransferStates;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.IntakeMotor.IntakeMotorStates;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.IntakeStates;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.LockServo.LockServoStates;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.TransferMotor.TransferMotorStates;
import org.firstinspires.ftc.teamcode.Subsystems.Outtake.OuttakeConstants;
import org.firstinspires.ftc.teamcode.Subsystems.Outtake.OuttakeStates;

public class AutoCycleShootControl {
    private final MotorControl motorControl;
    private AutoCycleShootStates prevAutoCycleShootState = AutoCycleShootStates.idle;

    private static final double FAR_MIN_OUTTAKE_VELOCITY = 1900.0;

    /** Mean velocity when first armed near target — the pre-1st-ball RPM baseline. */
    private double armedVelocityTicks = Double.NaN;

    /** True after flywheel has dipped from the armed level (first ball through). */
    private boolean firstDropSeen = false;

    /**
     * True once velocity has returned to pre-1st-ball (armed − margin) after that
     * first drop. Until then feed stays open so ball 2 can still go; afterward
     * further feed waits for nearly the same pre-1st RPM (3rd-ball protect).
     */
    private boolean recoveredToPreFirst = false;

    /** True while turnTransfer is holding the queue for flywheel recovery. */
    private static volatile boolean holdingForRecovery = false;

    /** When the latch unlock was first commanded this shot; NaN until then. */
    private double unlockCommandedSec = Double.NaN;

    public AutoCycleShootControl(MotorControl motorControl) {
        this.motorControl = motorControl;
    }

    /** Used by {@link AutoCycleShootLogic} so pause time doesn't end the shot. */
    public static boolean isHoldingForRecovery() {
        return holdingForRecovery;
    }

    public void update() {
        if (OuttakeStates.getAutoCycleShootState() != prevAutoCycleShootState) {
            if (OuttakeStates.getAutoCycleShootState() != AutoCycleShootStates.turnTransfer) {
                resetFeedGate();
            }
            updateStates();
            prevAutoCycleShootState = OuttakeStates.getAutoCycleShootState();
        } else if (OuttakeStates.getAutoCycleShootState() == AutoCycleShootStates.turnTransfer) {
            updateStates();
        }
    }

    public void updateStates() {
        switch (OuttakeStates.getAutoCycleShootState()) {
            case recalibrate:
                break;
            case activate:
                // Unlock only once turnTransfer is ready to feed — avoids open latch + idle transfer.
                IntakeStates.setAutoIntakeTransferState(AutoIntakeTransferStates.idle);
                break;
            case turnBack:
                IntakeStates.setTransferMotorState(TransferMotorStates.backward);
                break;
            case turnTransfer:
                if (canFeedBall()) {
                    IntakeStates.setLockServoState(LockServoStates.unlock);
                    if (Double.isNaN(unlockCommandedSec)) {
                        unlockCommandedSec = nowSec();
                    }
                    // The unlock above is only a command; the latch still has to travel.
                    // Hold the feed until it has, or the transfer jams balls into it.
                    // Reuse the recovery pause so this wait does not count against
                    // shootFeedMaxSec — otherwise a long settle aborts ball 2/3.
                    if (nowSec() - unlockCommandedSec < OuttakeConstants.latchOpenSettleSec) {
                        holdingForRecovery = true;
                        IntakeStates.setIntakeMotorState(IntakeMotorStates.idle);
                        IntakeStates.setTransferMotorState(TransferMotorStates.idle);
                        break;
                    }
                    holdingForRecovery = false;
                    IntakeStates.setIntakeMotorState(IntakeMotorStates.forward);
                    IntakeStates.setTransferMotorState(TransferMotorStates.forward);
                } else {
                    // Only after the post-1st recovery gate (or brief spin-up arm wait).
                    holdingForRecovery = true;
                    IntakeStates.setIntakeMotorState(IntakeMotorStates.idle);
                    IntakeStates.setTransferMotorState(TransferMotorStates.idle);
                }
                break;
            case stop:
                holdingForRecovery = false;
                break;
            case turnTransferBack:
                holdingForRecovery = false;
                IntakeStates.setTransferMotorState(TransferMotorStates.backward);
                break;
            case deactivate:
                resetFeedGate();
                IntakeStates.setIntakeMotorState(IntakeMotorStates.idle);
                IntakeStates.setTransferMotorState(TransferMotorStates.idle);
                IntakeStates.setLockServoState(LockServoStates.lock);
                IntakeStates.setAutoIntakeTransferState(AutoIntakeTransferStates.idle);
                break;
            case idle:
                resetFeedGate();
                break;
        }
    }

    private void resetFeedGate() {
        armedVelocityTicks = Double.NaN;
        firstDropSeen = false;
        recoveredToPreFirst = false;
        holdingForRecovery = false;
        unlockCommandedSec = Double.NaN;
    }

    private static double nowSec() {
        return System.currentTimeMillis() / 1000.0;
    }

    /**
     * TeleOp feed gate (pre-1st-ball resume):
     * <ul>
     *   <li>Spin-up: wait until armed (near target, or settled cruise ≥ settle frac).</li>
     *   <li>Through first drop / before full pre-1st recovery: keep feeding (1–2 balls).</li>
     *   <li>After that recovery: only feed when velocity is back to armed − margin
     *       (nearly pre-1st-ball RPM) so the 3rd ball does not stack onto a dead wheel.</li>
     * </ul>
     */
    private boolean canFeedBall() {
        double v1 = motorControl.getMotorVelocity(MotorConstants.outtake1);
        double v2 = motorControl.getMotorVelocity(MotorConstants.outtake2);
        double velocity = (v1 + v2) / 2.0;

        if (GlobalVariables.isAutonomous && GlobalVariables.far) {
            return velocity > FAR_MIN_OUTTAKE_VELOCITY;
        }

        updateFeedGate(velocity);

        if (Double.isNaN(armedVelocityTicks)) {
            return false;
        }

        // Open until we've fully recovered once to pre-1st after the first drop.
        if (!recoveredToPreFirst) {
            return true;
        }

        return velocity >= armedVelocityTicks - OuttakeConstants.feedResumeMarginTicks;
    }

    private void updateFeedGate(double velocity) {
        maybeArm(velocity);
        if (Double.isNaN(armedVelocityTicks)) return;

        if (!firstDropSeen
                && velocity <= armedVelocityTicks - OuttakeConstants.feedFirstDropTicks) {
            firstDropSeen = true;
        }

        if (firstDropSeen
                && !recoveredToPreFirst
                && velocity >= armedVelocityTicks - OuttakeConstants.feedResumeMarginTicks) {
            recoveredToPreFirst = true;
        }
    }

    private void maybeArm(double velocity) {
        if (!Double.isNaN(armedVelocityTicks)) return;

        double target = motorControl.getLastCommandedVelocity(MotorConstants.outtake1);
        if (Double.isNaN(target) || target <= 0) {
            // Bang-bang power mode reports NaN for last commanded velocity — use UI target.
            target = GlobalVariables.outtakeTargetSpeed;
        }
        if (target <= 0) return;

        // Prefer near-target arm; fall back to settled cruise so an unreachable
        // commanded target (3600 cmd / ~3400 cruise) cannot block feed forever.
        if (velocity >= target * OuttakeConstants.feedArmTargetFrac
                || velocity >= target * OuttakeConstants.feedArmSettleFrac) {
            armedVelocityTicks = velocity;
        }
    }
}
