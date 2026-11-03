package org.firstinspires.ftc.teamcode.Main.ManualOpModes;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import org.firstinspires.ftc.teamcode.HardwareInterface.Motor.MotorConstants;
import org.firstinspires.ftc.teamcode.HardwareInterface.Motor.MotorControl;
import org.firstinspires.ftc.teamcode.Main.ShooterTelemetry.ShooterLogger;

@TeleOp(name = "Outtake Manual Control", group = "Manual")
public class OuttakeManualControl extends LinearOpMode {

    private MotorControl motorControl;
    private double targetRPM = 3000;
    private boolean outtakeActive = false;
    private boolean intakeActive = false;

    private boolean lastCross = false;
    private boolean lastCircle = false;
    private boolean lastUp = false;
    private boolean lastDown = false;

    @Override
    public void runOpMode() {
        motorControl = new MotorControl(hardwareMap);

        telemetry.addLine("Initialized");
        telemetry.addLine("Cross: Toggle Outtake | Circle: Toggle Intake/Transfer");
        telemetry.addLine("Dpad Up/Down: Change RPM");
        telemetry.update();

        waitForStart();

        ShooterLogger logger = new ShooterLogger(motorControl, null, null);
        logger.start("OuttakeManualControl");
        try {
            while (opModeIsActive()) {
                // Outtake Toggle (Cross)
                if (gamepad1.cross && !lastCross) {
                    outtakeActive = !outtakeActive;
                }
                lastCross = gamepad1.cross;

                // Speed Adjustment
                if (gamepad1.dpad_up && !lastUp) {
                    targetRPM += 25;
                }
                lastUp = gamepad1.dpad_up;

                if (gamepad1.dpad_down && !lastDown) {
                    targetRPM -= 25;
                }
                lastDown = gamepad1.dpad_down;

                // Intake & Transfer Toggle (Circle)
                if (gamepad1.circle && !lastCircle) {
                    intakeActive = !intakeActive;
                }
                lastCircle = gamepad1.circle;

                // Apply Outtake Speed
                if (outtakeActive) {
                    // MotorControl's velocity setter takes encoder ticks/second.
                    double targetTicksPerSecond = targetRPM
                            * MotorConstants.OUTTAKE_TICKS_PER_REVOLUTION / 60.0;
                    motorControl.setMotorRPM(MotorConstants.outtake, targetTicksPerSecond);
                } else {
                    motorControl.setMotorRPM(MotorConstants.outtake, 0);
                }

                // Apply Intake and Transfer Power
                if (intakeActive) {
                    motorControl.setMotorSpeed(MotorConstants.intake, 1.0);
                    motorControl.setMotorSpeed(MotorConstants.transfer, 1.0);
                } else {
                    motorControl.setMotorSpeed(MotorConstants.intake, 0.0);
                    motorControl.setMotorSpeed(MotorConstants.transfer, 0.0);
                }

                // Update power-controlled motors
                motorControl.setMotors(MotorConstants.intake);
                motorControl.setMotors(MotorConstants.transfer);

                // Telemetry
                telemetry.addData("[Cross] Outtake Status", outtakeActive ? "ON" : "OFF");
                telemetry.addData("[Dpad] Target RPM", targetRPM);
                telemetry.addData("Actual RPM 1", motorControl.getMotorRPM(MotorConstants.outtake1));
                telemetry.addData("Actual RPM 2", motorControl.getMotorRPM(MotorConstants.outtake2));
                telemetry.addData("[Circle] Intake/Transfer", intakeActive ? "ON" : "OFF");
                telemetry.update();
                logger.sample();
            }
        } finally {
            logger.stop();
        }
    }
}
