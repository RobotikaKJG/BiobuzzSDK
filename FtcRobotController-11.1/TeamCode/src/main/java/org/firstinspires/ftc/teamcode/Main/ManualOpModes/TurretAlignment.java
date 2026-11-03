package org.firstinspires.ftc.teamcode.Main.ManualOpModes;

import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import org.firstinspires.ftc.teamcode.Main.Dependencies;
import org.firstinspires.ftc.teamcode.Main.GlobalVariables;

/**
 * OpMode to align the turret for gear mounting.
 * Sets all turret servos to 0.5, which corresponds to the 'forward' (0 degrees) position
 * in the robot's coordinate system.
 */
@TeleOp(name = "Turret Alignment", group = "Manual")
public class TurretAlignment extends LinearOpMode {
    @Override
    public void runOpMode() {
        GlobalVariables.isAutonomous = false;
        Dependencies dependencies = new Dependencies(hardwareMap, gamepad1, gamepad2, telemetry);
        
        telemetry.addLine("Turret Alignment OpMode");
        telemetry.addLine("This will set all turret servos to 0.5 (Center/Forward)");
        telemetry.addLine("Press START to lock the position");
        telemetry.update();

        waitForStart();

        while (opModeIsActive()) {
            // 0.5 is the 'center' or 'forward' position defined in TurretServoControl
            dependencies.servoControl.setTurretServosPos(0.5);
            
            telemetry.addLine("Turret is LOCKED at 0.5 (Forward)");
            telemetry.addLine("You can now safely mount the gears in the center position.");
            telemetry.update();
        }
    }
}
