package org.firstinspires.ftc.teamcode.Autonomous.Autos.GoalAutonSolo;

import static org.firstinspires.ftc.robotcore.external.BlocksOpModeCompanion.telemetry;

import com.pedropathing.follower.Follower;

import org.firstinspires.ftc.teamcode.Autonomous.Auton;
import org.firstinspires.ftc.teamcode.Autonomous.AutonomousConstants;
import org.firstinspires.ftc.teamcode.Autonomous.Paths.GoalSoloPaths.BlueGoalSoloPaths;
import org.firstinspires.ftc.teamcode.Autonomous.Paths.GoalSoloPaths.GoalSoloPaths;
import org.firstinspires.ftc.teamcode.Autonomous.Paths.GoalSoloPaths.RedGoalSoloPaths;
import org.firstinspires.ftc.teamcode.Main.GlobalVariables;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.AutoIntakeTransfer.AutoIntakeTransferStates;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.IntakeStates;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.LockServo.LockServoStates;
import org.firstinspires.ftc.teamcode.Subsystems.Outtake.AutoCycleShoot.AutoCycleShootStates;
import org.firstinspires.ftc.teamcode.Subsystems.Outtake.OuttakeMotor.OuttakeMotorStates;
import org.firstinspires.ftc.teamcode.Subsystems.Outtake.OuttakeStates;

public class GoalAutonSolo implements Auton {
    private Follower follower;
    private GoalSoloPaths paths;
    private GoalAutonSoloState goalAutonSoloState = GoalAutonSoloState.drive_startPos_shootPos;
    private double currentWait = 0;
    private boolean wasIfCalled = false;
    private int gateCount = 0;
    private int gateTotal = 3;

    public GoalAutonSolo(Follower follower) {
        this.follower = follower;
    }

    @Override
    public void start() {
        GlobalVariables.far = false;
        setTrajectorySide();
        follower.setStartingPose(paths.getStartPose());
        follower.followPath(paths.startPos_shootPos(), true);
        OuttakeStates.setMotorState(OuttakeMotorStates.forwardClose);
        System.out.println("Outtake speed fwd close");
        goalAutonSoloState = GoalAutonSoloState.drive_startPos_shootPos;
        addWaitTime(AutonomousConstants.shooterToMaxSpeed);
    }

    @Override
    public void setTrajectorySide() {
        switch (GlobalVariables.alliance) {
            case Red:
                paths = new RedGoalSoloPaths(follower);
                break;
            case Blue:
                paths = new BlueGoalSoloPaths(follower);
                break;
        }
    }

    @Override
    public void run() {
        System.out.println("State:" + goalAutonSoloState);
        switch (goalAutonSoloState)
        {
            case drive_startPos_shootPos:
                drive_StartPos_ShootPos();
                break;
            case shoot_preload:
                shoot_preload();
                break;
            case drive_shootPos_takeFirstPos:
                drive_shootPos_takeFirstPos();
                break;
            case drive_takeFirstPos_shootPos:
                drive_takeFirstPos_shootPos();
                break;
            case shoot_first:
                shoot_first();
                break;
            case drive_shootPos_openGatePos:
                drive_shootPos_openGatePos();
                break;
            case drive_openGatePos_takeGatePos:
                drive_openGatePos_takeGatePos();
                break;
            case drive_takeGatePos_shootPos:
                drive_takeGatePos_shootPos();
                break;
            case shoot_gate:
                shoot_gate();
                break;
            case drive_shootPos_takeSecondPos_shootPos:
                drive_shootPos_takeSecondPos_shootPos();
                break;
            case shoot_second:
                shoot_second();
                break;
            case drive_shootPos_takeThirdPos:
                drive_shootPos_takeThirdPos();
                break;
            case drive_takeThirdPos_shootPosPark:
                drive_takeThirdPos_shootPosPark();
                break;
            case shoot_third:
                shoot_third();
                break;
            case stop:
                stop();
                break;
            case idle:
                break;
        }
    }

//AUTONOTE FILL IN THE LOGIC

    private void drive_StartPos_ShootPos() {
        if(follower.isBusy() || getSeconds() < currentWait) return;
        OuttakeStates.setAutoCycleShootState(AutoCycleShootStates.activate);
        goalAutonSoloState = GoalAutonSoloState.shoot_preload;
        addWaitTime(AutonomousConstants.shootTime);
    }

    private void shoot_preload() {
        if(getSeconds() < currentWait) return;
        OuttakeStates.setAutoCycleShootState(AutoCycleShootStates.idle);
        IntakeStates.setLockServoState(LockServoStates.lock);
        IntakeStates.setAutoIntakeTransferState(AutoIntakeTransferStates.activate);
        follower.followPath(paths.shootPos_takeFirstPos(), false);
        goalAutonSoloState = GoalAutonSoloState.drive_shootPos_takeFirstPos;
    }

    private void drive_shootPos_takeFirstPos() {
        if (follower.isBusy()) return;
        follower.followPath(paths.takeFirstPos_shootPos());
        goalAutonSoloState = GoalAutonSoloState.drive_takeFirstPos_shootPos;
    }

    private void drive_takeFirstPos_shootPos() {
        if (follower.isBusy()) return;
        OuttakeStates.setAutoCycleShootState(AutoCycleShootStates.activate);
        goalAutonSoloState = GoalAutonSoloState.shoot_first;
        addWaitTime(AutonomousConstants.shootTime);
    }

    private void shoot_first() {
        if(getSeconds() < currentWait) return;
        OuttakeStates.setAutoCycleShootState(AutoCycleShootStates.idle);
        IntakeStates.setLockServoState(LockServoStates.lock);
        IntakeStates.setAutoIntakeTransferState(AutoIntakeTransferStates.stop);
        follower.followPath(paths.shootPos_openGatePosBreak(), true);
        goalAutonSoloState = GoalAutonSoloState.drive_shootPos_openGatePos;
        gateCount = 0;
    }

    private void drive_shootPos_openGatePos() {
        if (follower.isBusy()) return;
        if (!wasIfCalled) {
            IntakeStates.setAutoIntakeTransferState(AutoIntakeTransferStates.activate);
            gateCount = gateCount + 1;
            addWaitTime(0.1);
            follower.followPath(paths.openGatePosBreak_openGatePos(), 0.7, true);
            wasIfCalled = true;
        }
        if (getSeconds() < currentWait) return;
        follower.followPath(paths.openGatePos_takeGatePos(), true);
        addWaitTime(1.8);
        wasIfCalled = false;
        goalAutonSoloState = GoalAutonSoloState.drive_openGatePos_takeGatePos;
    }

    private void drive_openGatePos_takeGatePos() {
        if (follower.isBusy() || getSeconds() < currentWait) return;
        IntakeStates.setAutoIntakeTransferState(AutoIntakeTransferStates.stop);
        follower.followPath(paths.takeGatePos_shootPos());
        goalAutonSoloState = GoalAutonSoloState.drive_takeGatePos_shootPos;
        addWaitTime(0.5);
    }

    private void drive_takeGatePos_shootPos() {
        if (getSeconds() < currentWait) return;
        if (!wasIfCalled) {
            IntakeStates.setAutoIntakeTransferState(AutoIntakeTransferStates.stop);
            wasIfCalled = true;
        }
        if(follower.isBusy()) return;
        OuttakeStates.setAutoCycleShootState(AutoCycleShootStates.activate);
        goalAutonSoloState = GoalAutonSoloState.shoot_gate;
        addWaitTime(AutonomousConstants.shootTime);
        wasIfCalled = false;
    }

    private void shoot_gate() {
        if (getSeconds() < currentWait) return;
        OuttakeStates.setAutoCycleShootState(AutoCycleShootStates.idle);
        IntakeStates.setLockServoState(LockServoStates.lock);
        if (gateCount >= gateTotal) {
            follower.followPath(paths.shootPos_takeSecondPos_shootPos(), true);
            IntakeStates.setAutoIntakeTransferState(AutoIntakeTransferStates.activate);
            goalAutonSoloState = GoalAutonSoloState.drive_shootPos_takeSecondPos_shootPos;
        }
        else {
            follower.followPath(paths.shootPos_openGatePosBreak(), true);
            IntakeStates.setAutoIntakeTransferState(AutoIntakeTransferStates.stop);
            goalAutonSoloState = GoalAutonSoloState.drive_shootPos_openGatePos;
        }
    }

    private void drive_shootPos_takeSecondPos_shootPos() {
        if(follower.isBusy()) return;
        OuttakeStates.setAutoCycleShootState(AutoCycleShootStates.activate);
        goalAutonSoloState = GoalAutonSoloState.shoot_second;
        addWaitTime(AutonomousConstants.shootTime);
    }

    private void shoot_second() {
        if (getSeconds() < currentWait) return;
        OuttakeStates.setAutoCycleShootState(AutoCycleShootStates.idle);
        IntakeStates.setLockServoState(LockServoStates.lock);
        IntakeStates.setAutoIntakeTransferState(AutoIntakeTransferStates.stop);
        follower.followPath(paths.shootPos_takeThirdPos(), false);
        goalAutonSoloState = GoalAutonSoloState.drive_shootPos_takeThirdPos;
        addWaitTime(0.5);
    }

    private void drive_shootPos_takeThirdPos() {
        if (getSeconds() < currentWait) return;
        if (!wasIfCalled) {
            IntakeStates.setAutoIntakeTransferState(AutoIntakeTransferStates.activate);
            wasIfCalled = true;
        }
        if(follower.isBusy()) return;
        IntakeStates.setAutoIntakeTransferState(AutoIntakeTransferStates.stop);
        follower.followPath(paths.takeThirdPos_shootPosPark(), true);
        addWaitTime(1.5);
        wasIfCalled = false;
        goalAutonSoloState = GoalAutonSoloState.drive_takeThirdPos_shootPosPark;
    }

    private void drive_takeThirdPos_shootPosPark() {
        if (getSeconds() < currentWait) return;
        OuttakeStates.setAutoCycleShootState(AutoCycleShootStates.activate);
        goalAutonSoloState = GoalAutonSoloState.shoot_third;
        addWaitTime(AutonomousConstants.shootTime);
    }

    private void shoot_third() {
        if(getSeconds() < currentWait) return;
        goalAutonSoloState = GoalAutonSoloState.stop;
    }

    private void stop() {
        if(follower.isBusy()) return;
        OuttakeStates.setMotorState(OuttakeMotorStates.idle);
        OuttakeStates.setAutoCycleShootState(AutoCycleShootStates.stop);
        goalAutonSoloState = GoalAutonSoloState.idle;
    }

    private void addWaitTime(double waitTime) {
        currentWait = getSeconds() + waitTime;
    }

    private double getSeconds() {
        return System.currentTimeMillis() / 1_000.0;
    }
}