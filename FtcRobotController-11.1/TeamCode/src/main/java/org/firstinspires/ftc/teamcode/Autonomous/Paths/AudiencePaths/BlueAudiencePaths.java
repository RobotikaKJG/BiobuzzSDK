package org.firstinspires.ftc.teamcode.Autonomous.Paths.AudiencePaths;

import com.pedropathing.follower.Follower;
import com.pedropathing.geometry.BezierCurve;
import com.pedropathing.geometry.BezierLine;
import com.pedropathing.geometry.Pose;
import com.pedropathing.paths.PathChain;

public class BlueAudiencePaths implements AudiencePaths {
    Follower follower;

    private PathChain startPos_shootPos;
    private PathChain shootPos_takeThreePos;
    private PathChain takeThreePos_shootPos;
    private PathChain shootPos_takeBottomPos;
    private PathChain takeBottomPos_shootPos;
    private PathChain takeBottomPos_takeUpPos;
    private PathChain takeUpPos_shootPos;
    private PathChain shootPos_park;

    private final Pose pt_startPose = new Pose(57.5, 7, Math.toRadians(90));
    private final Pose pt_shootPose = new Pose(51.5, 9.53, Math.toRadians(180));

    private final Pose pt_takeThreePose = new Pose(25.94, 25.99);
    private final Pose cp_takeThreePose = new Pose(25.93, 6.7);

    private final Pose pt_takeBottomPose = new Pose(15.72, 9.26);

    private final Pose pt_takeUpPose = new Pose(14.85, 28.6);
    private final Pose cp_takeUpPose = new Pose(31.36, 28.24);

    private final Pose pt_parkPose = new Pose(41.3, 10);

    public BlueAudiencePaths(Follower follower) {
        this.follower = follower;
        buildPaths();
    }

    private void buildPaths() {
        startPos_shootPos = follower.pathBuilder()
                .addPath(new BezierLine(pt_startPose, pt_shootPose))
                .setLinearHeadingInterpolation(pt_startPose.getHeading(), pt_shootPose.getHeading())
                .build();

        shootPos_takeThreePos = follower.pathBuilder()
                .addPath(new BezierCurve(pt_shootPose, cp_takeThreePose, pt_takeThreePose))
                .setTangentHeadingInterpolation()
                .build();

        takeThreePos_shootPos = follower.pathBuilder()
                .addPath(new BezierLine(pt_takeThreePose, pt_shootPose))
                .setTangentHeadingInterpolation()
                .setReversed()
                .build();

        shootPos_takeBottomPos = follower.pathBuilder()
                .addPath(new BezierLine(pt_shootPose, pt_takeBottomPose))
                .setTangentHeadingInterpolation()
                .build();

        takeBottomPos_shootPos = follower.pathBuilder()
                .addPath(new BezierLine(pt_takeBottomPose, pt_shootPose))
                .setTangentHeadingInterpolation()
                .setReversed()
                .build();

        takeBottomPos_takeUpPos = follower.pathBuilder()
                .addPath(new BezierCurve(pt_takeBottomPose, cp_takeUpPose, pt_takeUpPose))
                .setConstantHeadingInterpolation(Math.toRadians(180))
                .build();

        takeUpPos_shootPos = follower.pathBuilder()
                .addPath(new BezierLine(pt_takeUpPose, pt_shootPose))
                .setTangentHeadingInterpolation()
                .setReversed()
                .build();

        shootPos_park = follower.pathBuilder()
                .addPath(new BezierLine(pt_shootPose, pt_parkPose))
                .setConstantHeadingInterpolation(Math.toRadians(180))
                .build();

    }

    @Override
    public Pose getPt_startPose() {
        return pt_startPose;
    }

    public PathChain startPos_shootPos() {
        return startPos_shootPos;
    }

    public PathChain shootPos_takeThreePos() {
        return shootPos_takeThreePos;
    }

    public PathChain takeThreePos_shootPos() {
        return takeThreePos_shootPos;
    }

    public PathChain shootPos_takeBottomPos() {
        return shootPos_takeBottomPos;
    }

    public PathChain takeBottomPos_shootPos() {
        return takeBottomPos_shootPos;
    }

    public PathChain takeBottomPos_takeUpPos() {
        return takeBottomPos_takeUpPos;
    }

    public PathChain takeUpPos_shootPos() {
        return takeUpPos_shootPos;
    }

    public PathChain shootPos_park() {
        return shootPos_park;
    }
}