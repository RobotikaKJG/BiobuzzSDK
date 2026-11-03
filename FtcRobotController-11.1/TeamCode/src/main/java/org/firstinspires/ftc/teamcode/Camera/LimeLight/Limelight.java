package org.firstinspires.ftc.teamcode.Camera.LimeLight;

import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

@TeleOp(name = "Limelight Sample Detection Test", group = "Test")
public class Limelight extends LinearOpMode {

    private Limelight3A limelight;
    private int testPipeline = 1;  // Set this to 1, 2, or 3 based on which sample color you want to test

    // Thresholds
    private static final double MIN_AREA_THRESHOLD = 1.0;   // ta must be > 0
    private static final double CENTERING_THRESHOLD = 0.1;  // tx and ty must be within ±0.1

    @Override
    public void runOpMode() throws InterruptedException {
        limelight = hardwareMap.get(Limelight3A.class, "limelight");
        limelight.pipelineSwitch(testPipeline);  // Switch to the desired sample color pipeline
        limelight.setPollRateHz(100);
        limelight.start();

        telemetry.setMsTransmissionInterval(50);
        telemetry.addLine("Waiting for start...");
        telemetry.update();
        waitForStart();

        while (opModeIsActive()) {
            LLResult result = limelight.getLatestResult();

            boolean targetVisible = false;
            boolean targetCentered = false;

            double tx = 0.0;
            double ty = 0.0;
            double ta = 0.0;

            if (result != null && result.isValid()) {
                tx = result.getTx();
                ty = result.getTy();
                ta = result.getTa();

                // Check if the target area is above the minimum threshold
                targetVisible = ta > MIN_AREA_THRESHOLD;

                // Check if it's centered within threshold
                targetCentered = Math.abs(tx) <= CENTERING_THRESHOLD && Math.abs(ty) <= CENTERING_THRESHOLD;
            }

            telemetry.addData("tx", tx);
            telemetry.addData("ty", ty);
            telemetry.addData("ta", ta);
            telemetry.addData("Target Visible (ta > 0)?", targetVisible);
            telemetry.addData("Target Centered (±0.1)?", targetCentered);
            telemetry.update();
        }
    }
}
