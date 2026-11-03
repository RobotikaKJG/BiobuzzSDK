package org.firstinspires.ftc.teamcode.Autonomous.Paths.GoalPaths;

import com.pedropathing.follower.Follower;
import com.pedropathing.geometry.BezierCurve;
import com.pedropathing.geometry.BezierLine;
import com.pedropathing.geometry.Pose;
import com.pedropathing.paths.PathChain;

public class BlueGoalPaths implements GoalPaths {
    Follower follower;
    private PathChain startPos_shootPos;
    private PathChain shootPos_takeFirstPos;
    private PathChain takeFirstPos_shootPos;
    private PathChain shootPos_openGatePosBreak;
    private PathChain openGatePosBreak_openGatePos;
    private PathChain openGatePos_openGatePosBreak_openGatePosNew;
    private PathChain openGatePos_shootPos;
    private PathChain shootPos_takeSecondPos_shootPos;
    private PathChain shootPos_takeThirdPos;
    private PathChain takeThirdPos_shootPosPark;
    private PathChain shootPos_takeSecondPos_shootPosPark;

    private final Pose pt_startPose = new Pose(23.1, 119.2, Math.toRadians(143.8));
    private final Pose pt_shootPose = new Pose(53, 84, Math.toRadians(180));

    private final Pose pt_takeFirstPose = new Pose(19.6, 54.65);
    private final Pose cp_takeFirstPose = new Pose(34.2, 54.5);
    private final Pose cp_takeFirstPoseBack = new Pose(48.3, 81.27);

    private final Pose pt_openGatePose = new Pose(18, 57.7, Math.toRadians(154));
    private final Pose pt_openGatePoseBreak = new Pose(29.0, 57.7);
    private final Pose cp_openGatePose = new Pose(45.8, 58.2);

    private final Pose pt_retryOpenStart = new Pose(16.7, 57);
    private final Pose pt_openGatePoseNew = new Pose(16.7, 59.2);
    private final Pose cp_openGatePoseNew = new Pose(23.2, 55.6);

    private final Pose pt_takeSecondPose = new Pose(32.5, 81.9);
    private final Pose pt_shootPoseSecond = new Pose(50.4, 85.9, Math.toRadians(180));

    private final Pose pt_takeThirdPose = new Pose(31.5, 34.9);
    private final Pose cp_takeThirdPose = new Pose(31.5, 54.8);
    private final Pose cp_takeThirdPoseBack = new Pose(50.1, 87.5);

    private final Pose pt_shootPosePark = new Pose(61.6, 96.5, Math.toRadians(180));

    public BlueGoalPaths(Follower follower) {
        this.follower = follower;
        buildPaths();
    }

    private void buildPaths() {
        startPos_shootPos = follower.pathBuilder()
                .addPath(new BezierLine(pt_startPose, pt_shootPose))
                .setLinearHeadingInterpolation(pt_startPose.getHeading(), pt_shootPose.getHeading())
                .setBrakingStart(5)
                .build();

        shootPos_takeFirstPos = follower.pathBuilder()
                .addPath(new BezierCurve(pt_shootPose, cp_takeFirstPose, pt_takeFirstPose))
                .setTangentHeadingInterpolation()
                .build();

        takeFirstPos_shootPos = follower.pathBuilder()
                .addPath(new BezierCurve(pt_takeFirstPose, cp_takeFirstPose, cp_takeFirstPoseBack, pt_shootPose))
                .setTangentHeadingInterpolation()
                .setReversed()
                .build();

        shootPos_openGatePosBreak = follower.pathBuilder()
                .addPath(new BezierCurve(pt_shootPose, cp_openGatePose, pt_openGatePoseBreak))
                .setConstantHeadingInterpolation(Math.toRadians(180))
                .build();

        openGatePosBreak_openGatePos = follower.pathBuilder()
                .addPath(new BezierLine(pt_openGatePoseBreak, pt_openGatePose))
                .setLinearHeadingInterpolation(Math.toRadians(180), pt_openGatePose.getHeading())
                .build();

        openGatePos_openGatePosBreak_openGatePosNew = follower.pathBuilder()
                .addPath(new BezierCurve(pt_retryOpenStart, cp_openGatePoseNew, pt_openGatePoseNew))
                .setConstantHeadingInterpolation(pt_openGatePose.getHeading())
                .build();


        openGatePos_shootPos = follower.pathBuilder()
                .addPath(new BezierLine(pt_openGatePoseBreak, pt_shootPose))
                .setTangentHeadingInterpolation()
                .setReversed()
                .build();

        shootPos_takeSecondPos_shootPos = follower.pathBuilder()
                .addPath(new BezierLine(pt_shootPose, pt_takeSecondPose))
                .addPath(new BezierLine(pt_takeSecondPose, pt_shootPoseSecond))
                .setConstantHeadingInterpolation(Math.toRadians(180))
                .build();

        shootPos_takeThirdPos = follower.pathBuilder()
                .addPath(new BezierCurve(pt_shootPoseSecond, cp_takeThirdPose, pt_takeThirdPose))
                .setTangentHeadingInterpolation()
                .build();

        takeThirdPos_shootPosPark = follower.pathBuilder()
                .addPath(new BezierCurve(pt_takeThirdPose, cp_takeThirdPose, cp_takeThirdPoseBack, pt_shootPosePark))
                .setTangentHeadingInterpolation()
                .setReversed()
                .build();

        shootPos_takeSecondPos_shootPosPark = follower.pathBuilder()
                .addPath(new BezierLine(pt_shootPose, pt_takeSecondPose))
                .addPath(new BezierLine(pt_takeSecondPose, pt_shootPosePark))
                .setConstantHeadingInterpolation(Math.toRadians(180))
                .build();
    }

    public PathChain startPos_shootPos() {
        return startPos_shootPos;
    }

    @Override
    public Pose getStartPose() {
        return pt_startPose;
    }

    public PathChain shootPos_takeFirstPos() {
        return shootPos_takeFirstPos;
    }

    public PathChain takeFirstPos_shootPos() {
        return takeFirstPos_shootPos;
    }

    public PathChain shootPos_openGatePosBreak() {
        return shootPos_openGatePosBreak;
    }

    public PathChain openGatePosBreak_openGatePos() {
        return openGatePosBreak_openGatePos;
    }

    public PathChain openGatePos_openGatePosBreak_openGatePosNew() {
        return openGatePos_openGatePosBreak_openGatePosNew;
    }

    public PathChain openGatePos_shootPos() {
        return openGatePos_shootPos;
    }

    public PathChain shootPos_takeSecondPos_shootPos() {
        return shootPos_takeSecondPos_shootPos;
    }

    public PathChain shootPos_takeThirdPos() {
        return shootPos_takeThirdPos;
    }

    public PathChain takeThirdPos_shootPosPark() {
        return takeThirdPos_shootPosPark;
    }

    public PathChain shootPos_takeSecondPos_shootPosPark() {
        return shootPos_takeSecondPos_shootPosPark;
    }
}