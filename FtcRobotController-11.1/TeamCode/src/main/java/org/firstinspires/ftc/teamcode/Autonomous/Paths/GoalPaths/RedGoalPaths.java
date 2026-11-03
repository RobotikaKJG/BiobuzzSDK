package org.firstinspires.ftc.teamcode.Autonomous.Paths.GoalPaths;

import com.pedropathing.follower.Follower;
import com.pedropathing.geometry.BezierCurve;
import com.pedropathing.geometry.BezierLine;
import com.pedropathing.geometry.Pose;
import com.pedropathing.paths.PathChain;

public class RedGoalPaths implements GoalPaths {
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

    private final Pose pt_startPose = new Pose(120.9, 119.2, Math.toRadians(36.2));
    private final Pose pt_shootPose = new Pose(92.6, 82.9, Math.toRadians(0));

    private final Pose pt_takeFirstPose = new Pose(124.4, 54.65);
    private final Pose cp_takeFirstPose = new Pose(114.8, 54.5);
    private final Pose cp_takeFirstPoseBack = new Pose(95.7, 81.27);

    private final Pose pt_openGatePose = new Pose(127.3, 58.7, Math.toRadians(26));
    private final Pose pt_openGatePoseBreak = new Pose(115, 59);
    private final Pose cp_openGatePose = new Pose(98.2, 58.9);

    private final Pose pt_retryOpenStart = new Pose(127.3, 57);
    private final Pose pt_openGatePoseNew = new Pose(127.3, 59.2);
    private final Pose cp_openGatePoseNew = new Pose(120.8, 55.6);

    private final Pose pt_takeSecondPose = new Pose(111.5, 81.9);
    private final Pose pt_shootPoseSecond = new Pose(93.6, 85.9, Math.toRadians(0));

    private final Pose pt_takeThirdPose = new Pose(114.5, 34.9);
    private final Pose cp_takeThirdPose = new Pose(114.5, 54.8);
    private final Pose cp_takeThirdPoseBack = new Pose(93.9, 87.5);

    private final Pose pt_shootPosePark = new Pose(82.4, 96.5, Math.toRadians(0));

    public RedGoalPaths(Follower follower) {
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
                .setConstantHeadingInterpolation(Math.toRadians(0))
                .build();

        openGatePosBreak_openGatePos = follower.pathBuilder()
                .addPath(new BezierLine(pt_openGatePoseBreak, pt_openGatePose))
                .setLinearHeadingInterpolation(Math.toRadians(0), pt_openGatePose.getHeading())
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
                .setConstantHeadingInterpolation(Math.toRadians(0))
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
                .setConstantHeadingInterpolation(Math.toRadians(0))
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