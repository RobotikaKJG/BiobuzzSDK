package org.firstinspires.ftc.teamcode.Autonomous.Paths;

import com.pedropathing.geometry.BezierCurve;
import com.pedropathing.geometry.BezierLine;
import com.pedropathing.geometry.Pose;
import com.pedropathing.paths.Path;

public class AutoCheckPath {

    private final Pose start = new Pose(56, 8, Math.toRadians(270));
    private final Pose pickup = new Pose(8.5, 8, Math.toRadians(270));
    private final Pose pickupControl1 = new Pose(8.5, 18, Math.toRadians(0));
    private final Pose check = new Pose(56.0518, 7.5465, Math.toRadians(270));
    private final Pose driveupStart = new Pose(56.0518, 7.5465, Math.toRadians(270));
    private final Pose driveup = new Pose(57, 110, Math.toRadians(90));
    private final Pose driveupControl1 = new Pose(-20, 95, Math.toRadians(0));
    private final Pose park = new Pose(8, 110, Math.toRadians(180));

    public Path pickup() {
        Path path = new Path(new BezierCurve(start, pickupControl1, pickup));
        path.setLinearHeadingInterpolation(start.getHeading(), pickup.getHeading());
        return path;
    }

    public Path check() {
        Path path = new Path(new BezierLine(pickup, check));
        path.setConstantHeadingInterpolation(check.getHeading());
        return path;
    }

    public Path driveup() {
        Path path = new Path(new BezierCurve(driveupStart, driveupControl1, driveup));
        path.setLinearHeadingInterpolation(driveupStart.getHeading(), driveup.getHeading());
        return path;
    }

    public Path park() {
        Path path = new Path(new BezierLine(driveup, park));
        path.setConstantHeadingInterpolation(park.getHeading());
        return path;
    }
}
