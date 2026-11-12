package org.firstinspires.ftc.teamcode.Autonomous.Paths;

import com.pedropathing.follower.Follower;
import com.pedropathing.geometry.BezierCurve;
import com.pedropathing.geometry.BezierLine;
import com.pedropathing.geometry.Pose;
import com.pedropathing.paths.Path;

/** Camera-independent path definitions for Pedro Pathing 2.1.2. */
public class AutoWithoutCamera {
    private final Pose start = new Pose(56, 8, Math.toRadians(270));
    private final Pose pickup = new Pose(8.5, 8, Math.toRadians(270));
    private final Pose pickupControl1 = new Pose(8.5, 18, 0);
    private final Pose driveback = new Pose(56, 8, 0);
    private final Pose shoot = new Pose(56, 8, Math.toRadians(270));
    // private final Pose park = new Pose(8, 110, Math.toRadians(270));
    // private final Pose parkControl1 = new Pose(8.1672, 43.7596, 0);

    public Pose getStartPose() {
        return start.copy();
    }

    public Path pickup() {
        Path path = new Path(new BezierCurve(start, pickupControl1, pickup));
        path.setLinearHeadingInterpolation(start.getHeading(), pickup.getHeading());
        return path;
    }

    public Path driveback() {
        Path path = new Path(new BezierLine(pickup, driveback));
        path.setConstantHeadingInterpolation(driveback.getHeading());
        return path;
    }

    /**
     * Call once after driveback completes to turn to the absolute shooting heading.
     * Continue calling follower.update() until follower.isBusy() becomes false.
     */
    public void shoot(Follower follower) {
        follower.turnTo(shoot.getHeading());
    }

    public Pose getShootPose() {
        return shoot.copy();
    }

    // public Path park() {
    //     Path path = new Path(new BezierCurve(shoot, parkControl1, park));
    //     path.setConstantHeadingInterpolation(park.getHeading());
    //     return path;
    // }
}
