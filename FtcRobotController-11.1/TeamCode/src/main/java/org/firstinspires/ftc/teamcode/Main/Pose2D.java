package org.firstinspires.ftc.teamcode.Main;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

public class Pose2D {
    private final double x; // in millimeters
    private final double y; // in millimeters
    private final double heading; // in radians

    // Constructor that converts from given units to internal units
    public Pose2D(DistanceUnit distanceUnit, double x, double y, AngleUnit headingUnit, double heading) {
        this.x = distanceUnit.toMm(x);
        this.y = distanceUnit.toMm(y);
        this.heading = headingUnit.toRadians(heading);
    }

    // Constructor with default units: millimeters and radians
    public Pose2D(double x, double y, double heading) {
        this.x = x;  // assuming x and y are in millimeters
        this.y = y;
        this.heading = Math.toRadians(heading); // assuming heading is in degrees
    }

    // Getters with unit conversions
    public double getX(DistanceUnit unit) {
        return unit.fromMm(x);
    }

    public double getY(DistanceUnit unit) {
        return unit.fromMm(y);
    }

    public double getHeading(AngleUnit unit) {
        return unit.fromRadians(heading);
    }

    @Override
    public String toString() {
        return String.format("Pose2D(x=%.2f, y=%.2f, heading=%.2f radians)", x, y, heading);
    }
}
