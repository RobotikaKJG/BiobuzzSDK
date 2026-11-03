package org.firstinspires.ftc.teamcode.Autonomous.Autos.GoalAuton;

import android.provider.Settings;

import com.pedropathing.follower.Follower;

import org.firstinspires.ftc.teamcode.Autonomous.Auton;
import org.firstinspires.ftc.teamcode.Autonomous.AutonomousConstants;
import org.firstinspires.ftc.teamcode.Autonomous.Autos.GoalAuton.GoalAutonState;
import org.firstinspires.ftc.teamcode.Autonomous.Paths.GoalPaths.BlueGoalPaths;
import org.firstinspires.ftc.teamcode.Autonomous.Paths.GoalPaths.GoalPaths;
import org.firstinspires.ftc.teamcode.Autonomous.Paths.GoalPaths.RedGoalPaths;
import org.firstinspires.ftc.teamcode.Autonomous.Paths.GoalPaths.GoalPaths;
import org.firstinspires.ftc.teamcode.Main.Alliance;
import org.firstinspires.ftc.teamcode.Main.GlobalVariables;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.AutoIntakeTransfer.AutoIntakeTransferStates;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.IntakeMotor.IntakeMotorStates;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.IntakeStates;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.LockServo.LockServoStates;
import org.firstinspires.ftc.teamcode.Subsystems.Outtake.AutoCycleShoot.AutoCycleShootStates;
import org.firstinspires.ftc.teamcode.Subsystems.Outtake.OuttakeMotor.OuttakeMotorStates;
import org.firstinspires.ftc.teamcode.Subsystems.Outtake.OuttakeStates;


public class GoalAuton implements Auton {
    private Follower follower;
    private GoalPaths paths;
    private GoalAutonState goalAutonState = GoalAutonState.drive_startPos_shootPos;
    private double currentWait = 0;
    private boolean wasIfCalled = false;
    private int gateCount = 0;
    private boolean corrected = false;

    public GoalAuton(Follower follower) {
        this.follower = follower;
    }

    @Override
    public void start() {
        GlobalVariables.far = false;
        setTrajectorySide();
        follower.setStartingPose(paths.getStartPose());
        follower.followPath(paths.startPos_shootPos(), 0.9, false);
        OuttakeStates.setMotorState(OuttakeMotorStates.forwardStart);
        System.out.println("Outtake state:" + OuttakeStates.getMotorState());
        IntakeStates.setLockServoState(LockServoStates.unlock);
        goalAutonState = GoalAutonState.drive_startPos_shootPos;
        addWaitTime(AutonomousConstants.shooterToMaxSpeed);
    }

    @Override
    public void setTrajectorySide() {
        switch (GlobalVariables.alliance) {
            case Red:
                paths = new RedGoalPaths(follower);
                break;
            case Blue:
                paths = new BlueGoalPaths(follower);
                break;
        }
    }

    @Override
    public void run() {
        System.out.println("State: " + goalAutonState);
        switch (goalAutonState)
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
            case intake_gate:
                intake_gate();
                break;
            case drive_openGatePos_shootPos:
                drive_openGatePos_shootPos();
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
        if(getSeconds() < currentWait) return;
        OuttakeStates.setAutoCycleShootState(AutoCycleShootStates.turnTransfer);
        if (follower.isBusy()) return;
        goalAutonState = GoalAutonState.shoot_preload;
//        addWaitTime(AutonomousConstants.shootTime);
    }

    private void shoot_preload() {
//        if(getSeconds() < currentWait) return;
        OuttakeStates.setMotorState(OuttakeMotorStates.forwardClose);
        OuttakeStates.setAutoCycleShootState(AutoCycleShootStates.idle);
        IntakeStates.setLockServoState(LockServoStates.lock);
        IntakeStates.setAutoIntakeTransferState(AutoIntakeTransferStates.activate);
        follower.followPath(paths.shootPos_takeFirstPos(), false);
        goalAutonState = GoalAutonState.drive_shootPos_takeFirstPos;
    }

    private void drive_shootPos_takeFirstPos() {
        if (follower.isBusy()) return;
        follower.followPath(paths.takeFirstPos_shootPos());
        goalAutonState = GoalAutonState.drive_takeFirstPos_shootPos;
    }

    private void drive_takeFirstPos_shootPos() {
        if (follower.isBusy()) return;
        goalAutonState = GoalAutonState.shoot_first;
        addWaitTime(0.2);
    }

    private void shoot_first() {
        if(getSeconds() < currentWait) return;
        if (!wasIfCalled) {
            OuttakeStates.setAutoCycleShootState(AutoCycleShootStates.activate);
            addWaitTime(AutonomousConstants.shootTime);
            wasIfCalled = true;
        }
        if (getSeconds() < currentWait) return;
        wasIfCalled = false;
        OuttakeStates.setAutoCycleShootState(AutoCycleShootStates.idle);
        IntakeStates.setLockServoState(LockServoStates.lock);
        IntakeStates.setAutoIntakeTransferState(AutoIntakeTransferStates.stop);
        follower.followPath(paths.shootPos_openGatePosBreak(), true);
        goalAutonState = GoalAutonState.drive_shootPos_openGatePos;
        gateCount = 0;
    }

    private void drive_shootPos_openGatePos() {
        if (follower.isBusy()) return;
        IntakeStates.setAutoIntakeTransferState(AutoIntakeTransferStates.activate);
        gateCount = gateCount + 1;
        if (gateCount == 1) addWaitTime(1.5);
        else addWaitTime(2);
        follower.followPath(paths.openGatePosBreak_openGatePos(), 0.7, true);
        goalAutonState = GoalAutonState.intake_gate;
    }

    private void intake_gate() {
        if (getSeconds() > currentWait - 1 && !corrected && ((follower.getPose().getHeading() < Math.toRadians(30) && GlobalVariables.alliance == Alliance.Red) || (follower.getPose().getHeading() > Math.toRadians(150) && GlobalVariables.alliance == Alliance.Blue))) {
            follower.followPath(paths.openGatePos_openGatePosBreak_openGatePosNew());
            addWaitTime(2);
            corrected = true;
        }
        if (getSeconds() < currentWait && !(IntakeStates.getAutoIntakeTransferState() == AutoIntakeTransferStates.stop || IntakeStates.getAutoIntakeTransferState() == AutoIntakeTransferStates.idle))
            return;
        follower.followPath(paths.openGatePos_shootPos(), true);
        addWaitTime(0.3);
        goalAutonState = GoalAutonState.drive_openGatePos_shootPos;
        corrected = false;
    }

    private void drive_openGatePos_shootPos() {
        if (getSeconds() < currentWait) return;
        if (!wasIfCalled) {
            IntakeStates.setAutoIntakeTransferState(AutoIntakeTransferStates.stop);
            wasIfCalled = true;
        }
        if(follower.isBusy()) return;
//        OuttakeStates.setAutoCycleShootState(AutoCycleShootStates.activate);
        goalAutonState = GoalAutonState.shoot_gate;
        addWaitTime(0.2);
        wasIfCalled = false;
    }

    private void shoot_gate() {
        if(getSeconds() < currentWait) return;
        if (!wasIfCalled) {
            OuttakeStates.setAutoCycleShootState(AutoCycleShootStates.activate);
            addWaitTime(AutonomousConstants.shootTime);
            wasIfCalled = true;
        }
        if (getSeconds() < currentWait) return;
        wasIfCalled = false;
        OuttakeStates.setAutoCycleShootState(AutoCycleShootStates.idle);
        IntakeStates.setLockServoState(LockServoStates.lock);
        if (gateCount >= GlobalVariables.gateTotal) {
            if (GlobalVariables.gateTotal > 3) follower.followPath(paths.shootPos_takeSecondPos_shootPosPark(), true);
            else follower.followPath(paths.shootPos_takeSecondPos_shootPos(), true);
            IntakeStates.setAutoIntakeTransferState(AutoIntakeTransferStates.activate);
            goalAutonState = GoalAutonState.drive_shootPos_takeSecondPos_shootPos;
        }
        else {
            follower.followPath(paths.shootPos_openGatePosBreak(), true);
            IntakeStates.setAutoIntakeTransferState(AutoIntakeTransferStates.stop);
            goalAutonState = GoalAutonState.drive_shootPos_openGatePos;
        }
    }

    private void drive_shootPos_takeSecondPos_shootPos() {
        if(follower.isBusy()) return;
//        OuttakeStates.setAutoCycleShootState(AutoCycleShootStates.activate);
        goalAutonState = GoalAutonState.shoot_second;
        addWaitTime(0.2);
    }

    private void shoot_second() {
        if(getSeconds() < currentWait) return;
        if (!wasIfCalled) {
            OuttakeStates.setAutoCycleShootState(AutoCycleShootStates.activate);
            addWaitTime(AutonomousConstants.shootTime);
            wasIfCalled = true;
        }
        if (getSeconds() < currentWait) return;
        wasIfCalled = false;
        OuttakeStates.setAutoCycleShootState(AutoCycleShootStates.idle);
        IntakeStates.setLockServoState(LockServoStates.lock);
        IntakeStates.setAutoIntakeTransferState(AutoIntakeTransferStates.stop);
        addWaitTime(0.5);
        if (GlobalVariables.gateTotal > 3) {
            goalAutonState = GoalAutonState.stop;
            return;
        }
        follower.followPath(paths.shootPos_takeThirdPos(), false);
        goalAutonState = GoalAutonState.drive_shootPos_takeThirdPos;
    }

    private void drive_shootPos_takeThirdPos() {
        if (getSeconds() < currentWait) return;
        if (!wasIfCalled) {
            IntakeStates.setAutoIntakeTransferState(AutoIntakeTransferStates.activate);
            wasIfCalled = true;
        }
        if(follower.isBusy()) return;
        follower.followPath(paths.takeThirdPos_shootPosPark(), true);
        addWaitTime(1.5);
        wasIfCalled = false;
        goalAutonState = GoalAutonState.drive_takeThirdPos_shootPosPark;
    }

    private void drive_takeThirdPos_shootPosPark() {
        if (getSeconds() > currentWait - 0.5) {
            IntakeStates.setAutoIntakeTransferState(AutoIntakeTransferStates.stop);
        }
        if (getSeconds() < currentWait) return;
//        OuttakeStates.setAutoCycleShootState(AutoCycleShootStates.activate);
        goalAutonState = GoalAutonState.shoot_third;
        addWaitTime(0.2);
    }

    private void shoot_third() {
        if(getSeconds() < currentWait) return;
        if (!wasIfCalled) {
            OuttakeStates.setAutoCycleShootState(AutoCycleShootStates.activate);
            addWaitTime(AutonomousConstants.shootTime);
            wasIfCalled = true;
        }
        if (getSeconds() < currentWait) return;
        wasIfCalled = false;
        goalAutonState = GoalAutonState.stop;
    }

    private void stop() {
        if(follower.isBusy() || getSeconds() < currentWait) return;
        OuttakeStates.setMotorState(OuttakeMotorStates.idle);
        OuttakeStates.setAutoCycleShootState(AutoCycleShootStates.stop);
        goalAutonState = GoalAutonState.idle;
    }

    private void addWaitTime(double waitTime) {
        currentWait = getSeconds() + waitTime;
    }

    private double getSeconds() {
        return System.currentTimeMillis() / 1_000.0;
    }
}