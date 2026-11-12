package org.firstinspires.ftc.teamcode.HardwareInterface.Servo;

import org.firstinspires.ftc.teamcode.Subsystems.Intake.IntakeConstants;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.IntakeStates;
import org.firstinspires.ftc.teamcode.Subsystems.Outtake.OuttakeConstants;

public class ServoConstants {
    // Servo indexes
    public static final int lockServo = 0;
    public static final int flowerServo = 1;

    // CR Servo indexes


    // Analog indexes
    public static final int turretAnalog = 0;


    public static final double[] servoMinPos =
            {
                    IntakeConstants.lockServoMinPos,
                    IntakeConstants.flowerServoMinPos
            };
    public static final double[] servoMaxPos =
            {
                    IntakeConstants.lockServoMaxPos,
                    IntakeConstants.flowerServoMaxPos
            };
}
