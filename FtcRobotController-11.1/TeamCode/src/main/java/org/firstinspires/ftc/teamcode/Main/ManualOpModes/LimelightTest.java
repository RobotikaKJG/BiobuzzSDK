package org.firstinspires.ftc.teamcode.Main.ManualOpModes;

import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.robotcore.external.navigation.Pose3D;

import java.util.List;

@TeleOp(name = "Limelight Test", group = "Test")
public class LimelightTest extends LinearOpMode {

    private static final double FIELD_HALF_SIZE_M = 1.7;

    @Override
    public void runOpMode() {
        Limelight3A limelight = hardwareMap.get(Limelight3A.class, "limelight");
        limelight.pipelineSwitch(0);
        limelight.start();

        telemetry.addData("Status", "Initialized, waiting for start");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {
            LLResult result = limelight.getLatestResult();

            if (result == null) {
                telemetry.addData("Limelight", "No result (null)");
            } else if (!result.isValid()) {
                telemetry.addData("Limelight", "Result not valid");
            } else {
                Pose3D botpose = result.getBotpose();
                if (botpose != null) {
                    telemetry.addData("Bot X (m)", "%.3f", botpose.getPosition().x);
                    telemetry.addData("Bot Y (m)", "%.3f", botpose.getPosition().y);
                    telemetry.addData("Bot Z (m)", "%.3f", botpose.getPosition().z);
                }

                telemetry.addData("tx", "%.2f", result.getTx());
                telemetry.addData("ty", "%.2f", result.getTy());

                List<LLResultTypes.FiducialResult> fiducials = result.getFiducialResults();
                if (fiducials != null && !fiducials.isEmpty()) {
                    telemetry.addData("Tags seen", fiducials.size());

                    for (LLResultTypes.FiducialResult f : fiducials) {
                        int id = f.getFiducialId();
                        telemetry.addData("Tag " + id + " tx", "%.2f", f.getTargetXDegrees());

                        if (id == 24 || id == 20) {
                            if (botpose != null) {
                                double y;
                                if (id == 24)
                                    y = botpose.getPosition().y - FIELD_HALF_SIZE_M;
                                else
                                    y = botpose.getPosition().y + FIELD_HALF_SIZE_M;
                                double x = botpose.getPosition().x + FIELD_HALF_SIZE_M;

                                double distance = Math.sqrt(x * x + y * y);
                                telemetry.addData("Distance to " + id + " (m)", "%.3f", distance);
                                telemetry.addData("Distance to " + id + " (in)", "%.1f", distance * 39.3701);
                            }
                        }
                    }
                } else {
                    telemetry.addData("Tags seen", 0);
                }
            }

            telemetry.update();
        }

        limelight.stop();
    }
}