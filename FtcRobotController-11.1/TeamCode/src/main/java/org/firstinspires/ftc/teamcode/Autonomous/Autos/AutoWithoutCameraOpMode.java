package org.firstinspires.ftc.teamcode.Autonomous.Autos;

import com.pedropathing.follower.Follower;
import com.pedropathing.paths.Path;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;

import org.firstinspires.ftc.teamcode.Autonomous.Paths.AutoWithoutCamera;
import org.firstinspires.ftc.teamcode.PedroPathing.Constants;

/** Runs the exported driving sequence using the drivetrain and Pinpoint. */
@Autonomous(name = "Auto Without Camera", group = "Autonomous")
public class AutoWithoutCameraOpMode extends OpMode {
    private enum Phase { PICKUP, DRIVEBACK, ROTATE, DONE }

    private final AutoWithoutCamera paths = new AutoWithoutCamera();
    private Follower follower;
    private Path pickupPath;
    private Path drivebackPath;
    // private Path parkPath;
    private Phase phase = Phase.DONE;
    private long lastTelemetryNs;

    @Override
    public void init() {
        follower = Constants.createFollower(hardwareMap);
        follower.setStartingPose(paths.getStartPose());
        pickupPath = paths.pickup();
        drivebackPath = paths.driveback();
        // parkPath = paths.park();
        telemetry.addLine("Place robot at X=56, Y=8 inches, heading=270 degrees.");
        telemetry.addLine("Sequence: pickup, return, turn to 270 degrees.");
    }

    @Override
    public void start() {
        phase = Phase.PICKUP;
        follower.followPath(pickupPath, true);
    }

    @Override
    public void loop() {
        follower.update();
        if (!follower.isBusy()) {
            switch (phase) {
                case PICKUP:
                    follower.followPath(drivebackPath, true);
                    phase = Phase.DRIVEBACK;
                    break;
                case DRIVEBACK:
                    paths.shoot(follower);
                    phase = Phase.ROTATE;
                    break;
                case ROTATE:
                    // follower.followPath(parkPath, true);
                    follower.breakFollowing();
                    phase = Phase.DONE;
                    requestOpModeStop();
                    break;
                case DONE:
                    break;
            }
        }

        long now = System.nanoTime();
        if (now - lastTelemetryNs >= 150_000_000L) {
            telemetry.addData("Phase", phase);
            telemetry.addData("X (in)", follower.getPose().getX());
            telemetry.addData("Y (in)", follower.getPose().getY());
            telemetry.addData("Heading (deg)", Math.toDegrees(follower.getPose().getHeading()));
            lastTelemetryNs = now;
        }
    }

    @Override
    public void stop() {
        if (follower != null) follower.breakFollowing();
    }
}
