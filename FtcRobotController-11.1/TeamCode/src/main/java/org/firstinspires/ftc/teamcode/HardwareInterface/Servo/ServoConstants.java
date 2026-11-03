package org.firstinspires.ftc.teamcode.HardwareInterface.Servo;

import org.firstinspires.ftc.teamcode.Subsystems.Intake.IntakeConstants;
import org.firstinspires.ftc.teamcode.Subsystems.Outtake.OuttakeConstants;

public class ServoConstants {
    // Servo indexes
    public static final int lockServo = 0;
    public static final int turretServo1 = 1;
    public static final int turretServo2 = 2;

    // CR Servo indexes


    // Analog indexes
    public static final int turretAnalog = 0;


    public static final double[] servoMinPos =
            {
                    IntakeConstants.lockServoMinPos,
                    OuttakeConstants.turretServo1Min,
                    OuttakeConstants.turretServo2Min
            };
    public static final double[] servoMaxPos =
            {
                    IntakeConstants.lockServoMaxPos,
                    OuttakeConstants.turretServo1Max,
                    OuttakeConstants.turretServo2Max
            };
}
