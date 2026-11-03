package org.firstinspires.ftc.teamcode.Autonomous.Paths.GoalSoloPaths;

import com.pedropathing.follower.Follower;
import com.pedropathing.geometry.BezierCurve;
import com.pedropathing.geometry.BezierLine;
import com.pedropathing.geometry.Pose;
import com.pedropathing.paths.PathChain;

public class BlueGoalSoloPaths implements GoalSoloPaths {
    Follower follower;
    private PathChain startPos_shootPos;
    private PathChain shootPos_takeFirstPos;
    private PathChain takeFirstPos_shootPos;
    private PathChain shootPos_openGatePosBreak;
    private PathChain openGatePosBreak_openGatePos;
    private PathChain openGatePos_takeGatePos;
    private PathChain takeGatePos_shootPos;
    private PathChain shootPos_takeSecondPos_shootPos;
    private PathChain shootPos_takeThirdPos;
    private PathChain takeThirdPos_shootPosPark;

    private final Pose pt_startPose = new Pose(121, 119.3, Math.toRadians(36.2));
    private final Pose pt_shootPose = new Pose(93.6, 81.9, Math.toRadians(0));

    private final Pose pt_takeFirstPose = new Pose(115.6, 58.5);//new Pose(124.4, 54.65);
    private final Pose cp_takeFirstPose = new Pose(108.1, 58.8);//new Pose(114.8, 54.5);
    private final Pose cp_takeFirstPoseBack = new Pose(95.7, 81.27);

    private final Pose pt_openGatePose = new Pose(126.5, 59.9);
    private final Pose pt_openGatePoseBreak = new Pose(121, 59.9);
    private final Pose cp_openGatePose = new Pose(98.2, 58.9);
    private final Pose pt_takeGatePose = new Pose(131.5, 53);
    private final Pose cp_takeGatePose = new Pose(125.8, 57.5);

    private final Pose pt_takeSecondPose = new Pose(110, 81.9);
    private final Pose pt_shootPoseSecond = new Pose(93.6, 85.9, Math.toRadians(0));

    private final Pose pt_takeThirdPose = new Pose(125.1, 31.9);
    private final Pose cp_takeThirdPose = new Pose(117.1, 49.8);
    private final Pose cp_takeThirdPoseBack = new Pose(95.7, 109.6);

    private final Pose pt_shootPosePark = new Pose(93.6, 85.9, Math.toRadians(0));

    private int takeGateHeading = 145;

    public BlueGoalSoloPaths(Follower follower) {
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
                .setConstantHeadingInterpolation(Math.toRadians(0))
                .build();

        openGatePos_takeGatePos = follower.pathBuilder()
                .addPath(new BezierCurve(pt_openGatePose, cp_takeGatePose, pt_takeGatePose))
                .setLinearHeadingInterpolation(Math.toRadians(0), Math.toRadians(takeGateHeading))
                .build();

        takeGatePos_shootPos = follower.pathBuilder()
                .addPath(new BezierLine(pt_takeGatePose, pt_shootPose))
                .setTangentHeadingInterpolation()
                .setReversed()
                .build();

        shootPos_takeSecondPos_shootPos = follower.pathBuilder()
                .addPath(new BezierCurve(pt_shootPose, pt_takeSecondPose, pt_shootPoseSecond))
                .setConstantHeadingInterpolation(Math.toRadians(0))
                .build();

        shootPos_takeThirdPos = follower.pathBuilder()
                .addPath(new BezierCurve(pt_shootPoseSecond, cp_takeThirdPose, pt_takeThirdPose))
                .setTangentHeadingInterpolation()
                .build();

        takeThirdPos_shootPosPark = follower.pathBuilder()
                .addPath(new BezierCurve(pt_takeThirdPose, cp_takeThirdPose, cp_takeThirdPoseBack, pt_shootPosePark))
                .setTangentHeadingInterpolation()
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

    public PathChain openGatePos_takeGatePos() {
        return openGatePos_takeGatePos;
    }

    public PathChain takeGatePos_shootPos() {
        return takeGatePos_shootPos;
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
}