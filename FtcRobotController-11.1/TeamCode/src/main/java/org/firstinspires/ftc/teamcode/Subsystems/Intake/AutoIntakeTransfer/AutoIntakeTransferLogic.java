package org.firstinspires.ftc.teamcode.Subsystems.Intake.AutoIntakeTransfer;

import com.acmerobotics.dashboard.message.redux.ReceiveGamepadState;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.util.RobotLog;

import org.firstinspires.ftc.teamcode.HardwareInterface.Motor.MotorControl;
import org.firstinspires.ftc.teamcode.HardwareInterface.Sensor.SensorControl;
import org.firstinspires.ftc.teamcode.Main.GlobalVariables;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.IntakeConstants;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.IntakeStates;

public class AutoIntakeTransferLogic {
    private static final String TAG = "Intake"; // RobotLog tag for the loading-sequence trace
    private double currentWait = 0;
    private boolean wasIfCalled = false;
    private SensorControl sensorControl;
    private Gamepad gamepad1;

    // Intake presence latch: set the instant either intake sensor sees the artifact,
    // released only after both have been clear for intakeReleaseHoldSec. See
    // updateIntakeFilter().
    private boolean intakeLatched = false;
    private double intakeClearSinceSec = -1;

    public AutoIntakeTransferLogic(SensorControl sensorControl, Gamepad gamepad1) {
        this.sensorControl = sensorControl;
        this.gamepad1 = gamepad1;
    }

    public void update() {
        updateIntakeFilter();

        switch (IntakeStates.getAutoIntakeTransferState()) {
            case activate:
                activate();
                break;
            case checkAgainMid:
                checkAgainMid();
                break;
            case stopTransfer:
                stopTransfer();
                break;
            case checkAgainFront:
                checkAgainFront();
                break;
            case stop:
                stop();
                break;
            case idle:
                break;
        }
    }

    private void activate() {
        if (sensorControl.isTransferBall()) {
            RobotLog.ii(TAG, "activate: TRANSFER artifact -> checkAgainMid");
            IntakeStates.setAutoIntakeTransferState(AutoIntakeTransferStates.checkAgainMid);
            addWaitTime(IntakeConstants.checkAgainAfter);
        }
    }

    private void checkAgainMid() {
        if (currentWait > getSeconds()) return;
        if (sensorControl.isTransferBall()) {
            RobotLog.ii(TAG, "checkAgainMid: confirmed -> stopTransfer (transfer off)");
            IntakeStates.setAutoIntakeTransferState(AutoIntakeTransferStates.stopTransfer);
        }
        else {
            RobotLog.ii(TAG, "checkAgainMid: lost -> activate");
            IntakeStates.setAutoIntakeTransferState(AutoIntakeTransferStates.activate);
        }
    }

    private void stopTransfer() {
        if (GlobalVariables.isAutonomous) {
            IntakeStates.setAutoIntakeTransferState(AutoIntakeTransferStates.checkAgainFront);
            return;
        }
        if (intakeLatched) {
            RobotLog.ii(TAG, "stopTransfer: INTAKE artifact -> checkAgainFront");
            IntakeStates.setAutoIntakeTransferState(AutoIntakeTransferStates.checkAgainFront);
            addWaitTime(IntakeConstants.checkAgainAfter);
        }
    }

    private void checkAgainFront() {
        if (currentWait > getSeconds()) return;
        if (intakeLatched) {
            RobotLog.ii(TAG, "checkAgainFront: confirmed -> stop (3 balls loaded)");
            IntakeStates.setAutoIntakeTransferState(AutoIntakeTransferStates.stop);
            gamepad1.rumble(300);
        }
        else {
            RobotLog.ii(TAG, "checkAgainFront: lost -> stopTransfer");
            IntakeStates.setAutoIntakeTransferState(AutoIntakeTransferStates.stopTransfer);
        }
    }

    private void stop() {
        IntakeStates.setAutoIntakeTransferState(AutoIntakeTransferStates.idle);
    }

    /**
     * Asymmetric debounce for the intake pair: presence latches immediately, absence only
     * after intakeReleaseHoldSec of continuous clear. A spinning artifact chatters the raw
     * sensors (shell, hole, shell, ...), which made checkAgainFront's single re-read land on
     * a hole and bounce back to stopTransfer, leaving the intake running indefinitely.
     * <p>
     * The latch is armed only in the two states that consult it, so entering stopTransfer
     * always requires a fresh detection and a stale latch can never confirm the next cycle
     * early. Called every loop from {@link #update()} — the timing is only correct if it
     * polls in every state.
     */
    private void updateIntakeFilter() {
        AutoIntakeTransferStates state = IntakeStates.getAutoIntakeTransferState();
        if (state != AutoIntakeTransferStates.stopTransfer
                && state != AutoIntakeTransferStates.checkAgainFront) {
            intakeLatched = false;
            intakeClearSinceSec = -1;
            return;
        }

        double now = getSeconds();
        if (sensorControl.isIntakeBall()) {
            intakeLatched = true;
            intakeClearSinceSec = -1;          // ball seen -> restart the release timer
        } else if (intakeLatched) {
            if (intakeClearSinceSec < 0) {
                intakeClearSinceSec = now;     // sensors just went clear
            } else if (now - intakeClearSinceSec >= IntakeConstants.intakeReleaseHoldSec) {
                RobotLog.ii(TAG, "intake latch released after held clear");
                intakeLatched = false;
                intakeClearSinceSec = -1;
            }
        }
    }

    private void addWaitTime(double waitTime) {
        currentWait = getSeconds() + waitTime;
    }

    private double getSeconds() {
        return System.currentTimeMillis() / 1000.0;
    }
}
