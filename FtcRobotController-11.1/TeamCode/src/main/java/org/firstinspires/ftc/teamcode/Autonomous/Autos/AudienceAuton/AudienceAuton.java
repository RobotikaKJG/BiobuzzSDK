package org.firstinspires.ftc.teamcode.Autonomous.Autos.AudienceAuton;

import com.pedropathing.follower.Follower;

import org.firstinspires.ftc.teamcode.Autonomous.Auton;
import org.firstinspires.ftc.teamcode.Autonomous.AutonomousConstants;
import org.firstinspires.ftc.teamcode.Autonomous.Autos.GoalAuton.GoalAutonState;
import org.firstinspires.ftc.teamcode.Autonomous.Paths.AudiencePaths.AudiencePaths;
import org.firstinspires.ftc.teamcode.Autonomous.Paths.AudiencePaths.BlueAudiencePaths;
import org.firstinspires.ftc.teamcode.Autonomous.Paths.AudiencePaths.RedAudiencePaths;
import org.firstinspires.ftc.teamcode.HardwareInterface.Sensor.SensorControl;
import org.firstinspires.ftc.teamcode.Main.GlobalVariables;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.AutoIntakeTransfer.AutoIntakeTransferStates;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.IntakeStates;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.LockServo.LockServoStates;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.TransferMotor.TransferMotorStates;
import org.firstinspires.ftc.teamcode.Subsystems.Outtake.AutoCycleShoot.AutoCycleShootStates;
import org.firstinspires.ftc.teamcode.Subsystems.Outtake.OuttakeMotor.OuttakeMotorStates;
import org.firstinspires.ftc.teamcode.Subsystems.Outtake.OuttakeStates;

public class AudienceAuton implements Auton {
    private SensorControl sensorControl;
    private Follower follower;
    private AudiencePaths paths;
    private AudienceAutonState audienceAutonState = AudienceAutonState.drive_startPos_shootPos;
    private double currentWait = 0;

    public AudienceAuton(Follower follower, SensorControl sensorControl) {
        this.follower = follower;
        this.sensorControl = sensorControl;
    }

    @Override
    public void start() {
        GlobalVariables.far = true;
        setTrajectorySide();
        follower.setStartingPose(paths.getPt_startPose());
        follower.followPath(paths.startPos_shootPos(), true);
        OuttakeStates.setMotorState(OuttakeMotorStates.autonomous);
        audienceAutonState = AudienceAutonState.drive_startPos_shootPos;
        addWaitTime(AutonomousConstants.shooterToMaxSpeed + 0.8);
    }

    @Override
    public void setTrajectorySide() {
        switch (GlobalVariables.alliance) {
            case Red:
                paths = new RedAudiencePaths(follower);
                break;
            case Blue:
                paths = new BlueAudiencePaths(follower);
                break;
        }
    }

    @Override
    public void run() {
        switch (audienceAutonState)
        {
            case drive_startPos_shootPos:
                drive_StartPos_ShootPos();
                break;
            case shoot_preload:
                shoot_preload();
                break;
            case drive_shootPos_takeThreePos:
                drive_shootPos_takeThreePos();
                break;
            case drive_takeThreePos_shootPos:
                drive_takeThreePos_shootPos();
                break;
            case shoot_first:
                shoot_first();
                break;
            case drive_shootPos_takeBottomPos:
                drive_shootPos_takeBottomPos();
                break;
            case drive_takeBottomPos_shootPos:
                drive_takeBottomPos_shootPos();
                break;
            case shoot_second:
                shoot_second();
                break;
            case drive_takeBottomPos_takeUpPos:
                drive_takeBottomPos_takeUpPos();
                break;
            case drive_takeUpPos_shootPos:
                drive_takeUpPos_shootPos();
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
        audienceAutonState = AudienceAutonState.shoot_preload;
        addWaitTime(AutonomousConstants.shootTime + 0.6);
    }

    private void shoot_preload() {
        if(getSeconds() < currentWait) return;
        OuttakeStates.setAutoCycleShootState(AutoCycleShootStates.idle);
        IntakeStates.setLockServoState(LockServoStates.lock);
        IntakeStates.setAutoIntakeTransferState(AutoIntakeTransferStates.stop);
        follower.followPath(paths.shootPos_takeThreePos(), 0.8, false);
        audienceAutonState = AudienceAutonState.drive_shootPos_takeThreePos;
        addWaitTime(0.75);
    }

    private void drive_shootPos_takeThreePos() {
        if (getSeconds() < currentWait) return;
        IntakeStates.setAutoIntakeTransferState(AutoIntakeTransferStates.activate);
        if (follower.isBusy() && IntakeStates.getAutoIntakeTransferState() != AutoIntakeTransferStates.idle) return;
        follower.followPath(paths.takeThreePos_shootPos(), false);
        audienceAutonState = AudienceAutonState.drive_takeThreePos_shootPos;
    }

    private void drive_takeThreePos_shootPos() {
        if (follower.isBusy()) return;
        OuttakeStates.setAutoCycleShootState(AutoCycleShootStates.activate);
        audienceAutonState = AudienceAutonState.shoot_first;
        addWaitTime(AutonomousConstants.shootTime + 0.6);
    }

    private void shoot_first() {
        if(getSeconds() < currentWait) return;
        OuttakeStates.setAutoCycleShootState(AutoCycleShootStates.idle);
        IntakeStates.setLockServoState(LockServoStates.lock);
        IntakeStates.setAutoIntakeTransferState(AutoIntakeTransferStates.stop);
        follower.followPath(paths.shootPos_takeBottomPos(), false);
        audienceAutonState = AudienceAutonState.drive_shootPos_takeBottomPos;
        addWaitTime(0.75);
    }

    private void drive_shootPos_takeBottomPos() {
        if (getSeconds() < currentWait) return;
        IntakeStates.setAutoIntakeTransferState(AutoIntakeTransferStates.activate);
        if (follower.isBusy() && IntakeStates.getAutoIntakeTransferState() != AutoIntakeTransferStates.idle) return;
//        if (IntakeStates.getAutoIntakeTransferState() == AutoIntakeTransferStates.checkAgainFront || IntakeStates.getAutoIntakeTransferState() == AutoIntakeTransferStates.idle || IntakeStates.getAutoIntakeTransferState() != AutoIntakeTransferStates.stop) {
        if (IntakeStates.getTransferMotorState() != TransferMotorStates.forward) {
            audienceAutonState = AudienceAutonState.drive_takeBottomPos_shootPos;
            follower.followPath(paths.takeBottomPos_shootPos(), false);
        }
        else {
            audienceAutonState = AudienceAutonState.drive_takeBottomPos_takeUpPos;
            follower.followPath(paths.takeBottomPos_takeUpPos(), false);
        }
    }

    private void drive_takeBottomPos_shootPos() {
        if (follower.isBusy()) return;
        OuttakeStates.setAutoCycleShootState(AutoCycleShootStates.activate);
        audienceAutonState = AudienceAutonState.shoot_second;
        addWaitTime(AutonomousConstants.shootTime + 0.6);
    }

    private void shoot_second() {
        if(getSeconds() < currentWait) return;
        OuttakeStates.setAutoCycleShootState(AutoCycleShootStates.idle);
        IntakeStates.setLockServoState(LockServoStates.lock);
        IntakeStates.setAutoIntakeTransferState(AutoIntakeTransferStates.stop);
        follower.followPath(paths.shootPos_takeBottomPos(), false);
        audienceAutonState = AudienceAutonState.drive_shootPos_takeBottomPos;
        addWaitTime(0.75);
    }

    private void drive_takeBottomPos_takeUpPos() {
        if (getSeconds() < currentWait) return;
        IntakeStates.setAutoIntakeTransferState(AutoIntakeTransferStates.activate);
        if (follower.isBusy() && IntakeStates.getAutoIntakeTransferState() != AutoIntakeTransferStates.idle) return;
        follower.followPath(paths.takeUpPos_shootPos(), false);
        audienceAutonState = AudienceAutonState.drive_takeUpPos_shootPos;
    }

    private void drive_takeUpPos_shootPos() {
        if (follower.isBusy()) return;
        OuttakeStates.setAutoCycleShootState(AutoCycleShootStates.activate);
        audienceAutonState = AudienceAutonState.shoot_first;
        addWaitTime(AutonomousConstants.shootTime + 0.6);
    }

    private void shoot_third() {
        if(getSeconds() < currentWait) return;
        OuttakeStates.setAutoCycleShootState(AutoCycleShootStates.idle);
        IntakeStates.setLockServoState(LockServoStates.lock);
        IntakeStates.setAutoIntakeTransferState(AutoIntakeTransferStates.stop);
        follower.followPath(paths.shootPos_park(), false);
        audienceAutonState = AudienceAutonState.stop;
    }

    private void stop() {
        if(follower.isBusy()) return;
        audienceAutonState = AudienceAutonState.idle;
    }

    private void addWaitTime(double waitTime) {
        currentWait = getSeconds() + waitTime;
    }

    private double getSeconds() {
        return System.currentTimeMillis() / 1_000.0;
    }
}