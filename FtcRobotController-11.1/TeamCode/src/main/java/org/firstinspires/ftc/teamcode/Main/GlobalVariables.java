package org.firstinspires.ftc.teamcode.Main;


import com.pedropathing.geometry.Pose;
import org.firstinspires.ftc.teamcode.Autonomous.AutonomousMode;

public class GlobalVariables {
    public static volatile boolean far = false; // toggled on control loop, read by turret loop
    public static Alliance alliance;
    public static AutonomousMode autonomousMode;
    public static boolean wasAutonomous = false;
    public static boolean isAutonomous;
    public static volatile boolean slowMode = false; // read by the drive loop
    public static boolean subCycles;
    public static boolean hang = false;
    public static double lastTurretAngle = 0;
    // Both cross thread boundaries and are non-atomic as plain doubles: the control
    // loop writes outtakeTargetSpeed and reads rpmOffset, while tuning OpModes write
    // rpmOffset and read both for telemetry. Without volatile a tuning change can sit
    // in a cache and never reach the shooter (or the driver-hub readout).
    public static volatile double outtakeTargetSpeed = 0;
    public static volatile double rpmOffset = 0;
    public static Pose lastPose = new Pose(0,0,0);
    public static int gateTotal;
}