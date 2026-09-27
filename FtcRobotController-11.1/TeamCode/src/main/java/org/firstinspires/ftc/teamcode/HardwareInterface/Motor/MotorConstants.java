package org.firstinspires.ftc.teamcode.HardwareInterface.Motor;

public class MotorConstants {
    public static final int frontLeft = 0;
    public static final int backLeft = 1;
    public static final int frontRight = 2;
    public static final int backRight = 3;
    public static final int outtake1 = 4;
    public static final int intake = 5;
//    public static final int outtake2 = 6;
    public static final int transfer = 6;
    public static final int transfer2 = 7;

    public static final int[][] motorConfig = {
            //separate motors
            {frontLeft},
            {backLeft},
            {frontRight},
            {backRight},
            {outtake1},
            {intake},
            {transfer},
            {transfer2},
            //various combinations
            {frontLeft, backLeft, frontRight, backRight},
            {frontLeft, backLeft},
            {frontRight, backRight},
            {frontLeft, backRight},
            {backLeft, frontRight},
            {frontLeft, backLeft, frontRight, backRight, outtake1, intake, transfer, transfer2},
            {outtake1, intake, transfer, transfer2},
            {frontLeft, backLeft, frontRight, backRight, intake, transfer, transfer2},
            {outtake1}
    };
    // motorConfig combined value names
    public static final int allDrive = 8;
    public static final int leftDrive = 9;
    public static final int rightDrive = 10;
    public static final int frontLeftBackRight = 11;
    public static final int frontRightBackLeft = 12;
    public static final int all = 13;
    public static final int notDrive = 14;
    public static final int notOuttake = 15;
    public static final int outtake = 16;

}
