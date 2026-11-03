package org.firstinspires.ftc.teamcode.Camera.LimeLight;

import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.robotcore.external.navigation.Pose3D;

@TeleOp(name = "Limelight AprilTag Pose Test", group = "Test")
public class LimelightAprilTags extends LinearOpMode {

    private Limelight3A limelight;
    private int aprilTagPipeline = 1;  // Make sure this pipeline is configured for AprilTags

    @Override
    public void runOpMode() throws InterruptedException {
        limelight = hardwareMap.get(Limelight3A.class, "limelight");
        limelight.start();

        telemetry.addLine("Booting Limelight...");
        telemetry.update();

        // Wait for limelight to be ready
//        while (!isStopRequested()) {
//            LLResult result = limelight.getLatestResult();
//            if (result != null && result.isValid()) {
//                telemetry.addLine("Limelight Ready ✅");
//                telemetry.update();
//                break;
//            } else {
//                telemetry.addLine("Waiting for Limelight...");
//                telemetry.update();
//                sleep(200);
//            }
//        }
        waitForStart();

        while (opModeIsActive()) {
            LLResult result = limelight.getLatestResult();

            if (result != null && result.isValid()) {
                // Get botpose relative to field (make sure your Limelight is configured to Field mode)
                Pose3D botpose = result.getBotpose();

                double x = botpose.getPosition().x + 1.7;
                double y = botpose.getPosition().y - 1.7;

                // Calculate distance to tag (in meters)
                double distance = Math.sqrt(x * x + y * y);

                telemetry.addData("X (m)", x);
                telemetry.addData("Y (m)", y);
                telemetry.addData("Distance to Tag (m)", distance);
            } else {
                telemetry.addLine("No valid AprilTag detected.");
            }

            telemetry.update();
        }
    }
}