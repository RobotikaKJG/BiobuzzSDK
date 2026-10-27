package org.firstinspires.ftc.teamcode.HardwareInterface.Sensor;

import com.pedropathing.ftc.localization.localizers.PinpointLocalizer;
import com.pedropathing.geometry.Pose;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.hardware.limelightvision.LLResult;
import com.qualcomm.hardware.limelightvision.Limelight3A;
import com.qualcomm.hardware.limelightvision.LLResultTypes;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.Pose3D;
import org.firstinspires.ftc.teamcode.Main.Alliance;
import org.firstinspires.ftc.teamcode.HardwareInterface.Gamepad.GamepadIndexValues;
import org.firstinspires.ftc.teamcode.HardwareInterface.Gamepad.EdgeDetection;
import org.firstinspires.ftc.teamcode.Main.GlobalVariables;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class SensorControl {
    private static final String PINPOINT_NAME = "pinpointIMU";
    private static final String LIMELIGHT_NAME = "limelight";

    private final Limelight3A limelight;
    private final EdgeDetection edgeDetection;
    private final PinpointLocalizer pedroLocalizer;
    public GoBildaIndicator indicator1;
    public GoBildaIndicator indicator2;

    private final InfraRedSensor[] infraRedSensors;

    // Set on the control loop (AutoResetPosControl, i.e. the square button) and read on
    // the turret loop, which owns updateLocalizer()/applyContinuousVisionFusion(). Without
    // volatile the turret loop can miss the flag entirely and keep fusing vision over the
    // pose the reset just wrote, so the reset silently does nothing.
    private volatile boolean isResetting = false;

    private double flywheelOffset = 0.0;

    // Goal coordinates in INCHES
    public static final double RedXInches = 62.0;
    public static final double RedYInches = 61.5;
    public static final double BlueXInches = -62.0;
    public static final double BlueYInches = 61.0;

    private static final double FieldHalfInches = 66.93;

    // --- CONTINUOUS INJECTION STORAGE & CONFIG ---
    private static final int LimelightFrames = 7;
    // Continuous sliding windows (instead of one-shot buffers)
    private final List<Double> rollingX = new ArrayList<>();
    private final List<Double> rollingY = new ArrayList<>();
    private final List<Double> rollingYaw = new ArrayList<>();

    // Safety Thresholds
    private static final double MAX_ALLOWED_DISTANCE_IN = 120.0;  // Don't trust far away tags
    private static final double MIN_ALLOWED_DISTANCE_IN = 10.0;  // Don't trust if too close to lens
    private static final double LINEAR_VELOCITY_THRESHOLD = 6.0; // Inches per second max
    private static final double ANGULAR_VELOCITY_THRESHOLD = Math.toRadians(10); // Max rad/s rotation
    private static final double CONTINUOUS_FUSION_ALPHA = 0.02;

    // Velocity Tracking variables
    private Pose lastPose = new Pose(0, 0, 0);
    private long lastVelocityUpdateTimeMs = System.currentTimeMillis();
    public volatile double robotVelocityXInPerSec = 0.0;
    public volatile double robotVelocityYInPerSec = 0.0;
    private volatile double robotLinearVelocityInPerSec = 0.0;
    private volatile double robotAngularVelocityRadPerSec = 0.0;

    private static final long LIMELIGHT_RESET_TIMEOUT_MS = 3000;
    private long resetStartTimeMs = -1;
    private final List<Double> limelightXReadingsIn = new ArrayList<>();
    private final List<Double> limelightYReadingsIn = new ArrayList<>();
    private final List<Double> limelightYawReadingsRad = new ArrayList<>();

    // Fusion tuning: how much we nudge towards vision per frame (0.02 = 2% vision, 98% odometry)

    public SensorControl(HardwareMap hardwareMap, EdgeDetection edgeDetection, PinpointLocalizer pedroLocalizer) {
        this.pedroLocalizer = pedroLocalizer;
        this.edgeDetection = edgeDetection;

        infraRedSensors = getInfraRedSensors(hardwareMap);

        limelight = hardwareMap.get(Limelight3A.class, LIMELIGHT_NAME);
        indicator1 = new GoBildaIndicator(hardwareMap.get(Servo.class, "led1"));
        indicator2 = new GoBildaIndicator(hardwareMap.get(Servo.class, "led2"));

        setInitialLocalisationAngle();
    }

    /**
     * Indexed by {@link InfraRedSensors} ordinal — keep this array in the same order as
     * that enum. Config names must match the Robot Controller configuration; the digital
     * port each one occupies is set there, not here.
     */
    private InfraRedSensor[] getInfraRedSensors(HardwareMap hardwareMap) {
        final InfraRedSensor[] infraRedSensors = new InfraRedSensor[]{
                hardwareMap.get(InfraRedSensor.class, "infraTransfer1"),  // digital port 3
                hardwareMap.get(InfraRedSensor.class, "infraTransfer2"),  // digital port 5
                hardwareMap.get(InfraRedSensor.class, "infraIntake1"),    // digital port 6
                hardwareMap.get(InfraRedSensor.class, "infraIntake2")     // digital port 7
        };

        for (InfraRedSensor sensor : infraRedSensors) {
            sensor.setMode(InfraRedSensor.SwitchConfig.NC);
        }
        return infraRedSensors;
    }

    private void setInitialLocalisationAngle() {
        if (!GlobalVariables.wasAutonomous) {
            pedroLocalizer.setPose(new Pose(0, 0, 0));
        } else {
            // GlobalVariables.wasAutonomous = false; // MOVED: Handled in GeneralAutonomous begin()
            pedroLocalizer.setPose(GlobalVariables.lastPose);
        }
        lastPose = pedroLocalizer.getPose();
    }

    public void setResetting(boolean reset) {
        isResetting = reset;
    }

    public boolean isInfraRedObstructed(InfraRedSensors sensor) {
        return infraRedSensors[sensor.ordinal()].isObstructed();
    }

    public void setLEDColor(GoBildaIndicator.Color color) {
        indicator1.setColor(color);
        indicator2.setColor(color);
    }

    //
    //  pedroLocalizer Loop Updates
    //

    public void updateLocalizer() {
        pedroLocalizer.update();
        GlobalVariables.lastPose = pedroLocalizer.getPose();

        calculateRobotVelocity();
        applyContinuousVisionFusion();
    }

    /**
     * Calculates current velocities from the odometry pedroLocalizer to ensure
     * we are stationary before injecting vision data.
     */
    public void calculateRobotVelocity() {
        long currentTime = System.currentTimeMillis();
        double dt = (currentTime - lastVelocityUpdateTimeMs) / 1000.0;

        if (dt > 0.005) { // Protect against divide-by-zero
            Pose currentPose = pedroLocalizer.getPose();

            Pose poseVelocity = pedroLocalizer.getVelocity();
            double dx = currentPose.getX() - lastPose.getX();
            double dy = currentPose.getY() - lastPose.getY();
            double dHeading = normalizeRadians(currentPose.getHeading() - lastPose.getHeading());

            if (poseVelocity != null) {
                robotVelocityXInPerSec = poseVelocity.getX();
                robotVelocityYInPerSec = poseVelocity.getY();
                robotLinearVelocityInPerSec = Math.hypot(robotVelocityXInPerSec, robotVelocityYInPerSec);
                robotAngularVelocityRadPerSec = Math.abs(poseVelocity.getHeading());
            } else {
                robotVelocityXInPerSec = dy / dt;
                robotVelocityYInPerSec = dy / dt;
                robotLinearVelocityInPerSec = Math.hypot(dx, dy) / dt;
                robotAngularVelocityRadPerSec = Math.abs(dHeading) / dt;
            }

            lastPose = currentPose;
            lastVelocityUpdateTimeMs = currentTime;
        }
    }

    /**
     * Continuous Odometry Injection Pipeline
     * Filters noise based on velocity, distance, orientation, and a rolling trimmed average.
     */
    public void applyContinuousVisionFusion() {
        if (isResetting) return;

        // GATE 1: Speed check
        if (robotLinearVelocityInPerSec > LINEAR_VELOCITY_THRESHOLD ||
                robotAngularVelocityRadPerSec > ANGULAR_VELOCITY_THRESHOLD) {
            return;
        }

        // GATE 2: Cache Result
        LLResult result = limelight.getLatestResult();
        if (result == null || !result.isValid()) {
            System.out.println("No limelight");
            return;
        }

        // GATE 3: Distance check with cached result
        double distance = getTagDistance(result);
        if (distance < MIN_ALLOWED_DISTANCE_IN || distance > MAX_ALLOWED_DISTANCE_IN) {
            return;
        }

        Pose3D botpose = result.getBotpose();
        if (botpose == null) return;

        double xIn = botpose.getPosition().x * 40.00;
        double yIn = botpose.getPosition().y * 39.37;
        double headingRad = botpose.getOrientation().getYaw(AngleUnit.RADIANS);

        double visionCalculatedX;
        double visionCalculatedY;
        double visionCalculatedHeading;

//        if (GlobalVariables.alliance == Alliance.Red) {
        visionCalculatedX = -xIn;
        visionCalculatedY = yIn;
        visionCalculatedHeading = normalizeRadians(headingRad - Math.toRadians(90));
//        } else {
//            visionCalculatedX = xIn;
//            visionCalculatedY = -yIn;
//            visionCalculatedHeading = normalizeRadians(headingRad + Math.toRadians(90));
//        }

        double visionRR_X = visionCalculatedY;
        double visionRR_Y = visionCalculatedX;

        Pose currentPose = pedroLocalizer.getPose();

        rollingX.add(visionRR_X);
        rollingY.add(visionRR_Y);
        rollingYaw.add(visionCalculatedHeading);

        if (rollingX.size() > LimelightFrames) {
            rollingX.remove(0);
            rollingY.remove(0);
            rollingYaw.remove(0);
        }

        if (rollingX.size() < LimelightFrames) return;

        double filteredVisionX = getTrimmedAverage(rollingX);
        double filteredVisionY = getTrimmedAverage(rollingY);
        double filteredVisionHeading = getTrimmedAverage(rollingYaw);

        double fusedX = currentPose.getX() + CONTINUOUS_FUSION_ALPHA * (filteredVisionX - currentPose.getX());
        double fusedY = currentPose.getY() + CONTINUOUS_FUSION_ALPHA * (filteredVisionY - currentPose.getY());
        double angleDiff = normalizeRadians(filteredVisionHeading - currentPose.getHeading());
        double fusedHeading = normalizeRadians(currentPose.getHeading() + CONTINUOUS_FUSION_ALPHA * angleDiff);

        Pose fusedPose = new Pose(fusedX, fusedY, fusedHeading);
        
        pedroLocalizer.setPose(fusedPose);
        lastPose = fusedPose; // SYNC: Prevent velocity spike
    }

    public void initLocalizerPose() {
        pedroLocalizer.setPose(new Pose(0, 0, 0));
        lastPose = new Pose(0, 0, 0);
    }

    public double getLocalizerAngle() {
        double heading = pedroLocalizer.getPose().getHeading();
        return heading;
    }

    public double getDistanceFromLocalizer() {
        Pose currentPose = pedroLocalizer.getPose();
        double robotX = currentPose.getY();
        double robotY = currentPose.getX();

        double targetX = (GlobalVariables.alliance == Alliance.Blue) ? RedXInches : BlueXInches;
        double targetY = (GlobalVariables.alliance == Alliance.Red) ? RedYInches : BlueYInches;

        double dx = targetX - robotX;
        double dy = targetY - robotY;

        return Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * Reset heading to 0 (keeping X/Y). Called by the turret loop, which owns the
     * pedroLocalizer, gated by its own options-button edge detection.
     */
    public void resetLocalizerAngleNow() {
        Pose current = pedroLocalizer.getPose();
        Pose newPose = new Pose(current.getX(), current.getY(), 0);
        pedroLocalizer.setPose(newPose);
    }

    public Pose getLocalizerPose() {
        Pose pose = pedroLocalizer.getPose();
        return pose;
    }

    //
    //  Limelight
    //

    public void initLimelight(int pipelineNr) {
        limelight.start();
        limelight.pipelineSwitch(pipelineNr);
    }

    public LLResult limelightResult() {
        LLResult result = limelight.getLatestResult();
        return result;
    }

    public boolean resetLocalizerWithLimelight() {
        if (resetStartTimeMs < 0) resetStartTimeMs = System.currentTimeMillis();

        if (System.currentTimeMillis() - resetStartTimeMs > LIMELIGHT_RESET_TIMEOUT_MS) {
            clearLimelightBuffers();
            return true;
        }

        LLResult result = limelight.getLatestResult();
        if (result != null && result.isValid()) {
            Pose3D botpose = result.getBotpose();
            if (botpose != null) {
                double xIn = botpose.getPosition().x * 39.37;
                double yIn = botpose.getPosition().y * 39.37;
                double headingRad = botpose.getOrientation().getYaw(AngleUnit.RADIANS);

//                if (GlobalVariables.alliance == Alliance.Red) {
                limelightXReadingsIn.add(-xIn);
                limelightYReadingsIn.add(yIn);
                limelightYawReadingsRad.add(headingRad - Math.toRadians(90));
//                } else {
//                    limelightXReadingsIn.add(xIn);
//                    limelightYReadingsIn.add(-yIn);
//                    limelightYawReadingsRad.add(headingRad + Math.toRadians(90));
//                }
            }
        }

        if (limelightXReadingsIn.size() < LimelightFrames) return false;

        double avgX = getTrimmedAverage(limelightXReadingsIn);
        double avgY = getTrimmedAverage(limelightYReadingsIn);
        double avgHeading = getTrimmedAverage(limelightYawReadingsRad);

        clearLimelightBuffers();

        Pose newPose = new Pose(avgY, avgX, avgHeading);
        pedroLocalizer.setPose(newPose);

        return true;
    }

    private void clearLimelightBuffers() {
        limelightXReadingsIn.clear();
        limelightYReadingsIn.clear();
        limelightYawReadingsRad.clear();
        resetStartTimeMs = -1;
    }

    public double getTagDistance() {
        LLResult result = limelight.getLatestResult();
        return getTagDistance(result);
    }

    public double getTagDistance(LLResult result) {
        double y = 0;
        int targetID = (GlobalVariables.alliance == Alliance.Red) ? 24 : 20;

        if (result != null && result.isValid()) {
            List<LLResultTypes.FiducialResult> fiducials = result.getFiducialResults();
            if (fiducials != null) {
                for (LLResultTypes.FiducialResult f : fiducials) {
                    if (f.getFiducialId() == targetID) {
                        Pose3D botpose = result.getBotpose();

                        // Coordinate mapping fix: ensure we use meters correctly then convert to inches
                        // Botpose coordinates are in meters
                        double botX_m = botpose.getPosition().x;
                        double botY_m = botpose.getPosition().y;
                        
                        // FieldHalfInches is in inches, convert to meters for calculation
                        double fieldHalf_m = FieldHalfInches * 0.0254;

                        double dx_m, dy_m;
                        if (GlobalVariables.alliance == Alliance.Red) {
                            dx_m = botX_m + fieldHalf_m;
                            dy_m = botY_m - fieldHalf_m;
                        } else {
                            dx_m = botX_m + fieldHalf_m;
                            dy_m = botY_m + fieldHalf_m;
                        }

                        return Math.sqrt(dx_m * dx_m + dy_m * dy_m) * 39.37; // Convert meters back to inches
                    }
                }
            }
        }
        return -1;
    }

    //
    //  Predictive shooting
    //

    public double getHoodTicksFromDegrees(double degrees) {
        return 0.02 * degrees - 0.7;
    }

    public double getFlywheelTicksFromVelocity(double velocity) {
        return 94.501 * velocity / 12 - 187.96 + flywheelOffset;
    }

    //
    //  Other Utilities
    //

    public double getTurretTargetAngleDegrees() {
        Pose currentPose = pedroLocalizer.getPose();
        double robotX = currentPose.getX();
        double robotY = currentPose.getY();
        double robotHeading = currentPose.getHeading();

        double targetX;
        double targetY;

        if (GlobalVariables.isAutonomous) {
            targetX = (GlobalVariables.alliance == Alliance.Red) ? RedXInches + 72 : BlueXInches + 72;
            targetY = (GlobalVariables.alliance == Alliance.Red) ? RedYInches + 72 : BlueYInches + 72;
        }
        else {
            targetX = (GlobalVariables.alliance == Alliance.Red) ? RedXInches : BlueXInches;
            targetY = (GlobalVariables.alliance == Alliance.Red) ? RedYInches : BlueYInches;
        }

        double dx = targetX - robotX;
        double dy = targetY - robotY;

        double angleToTargetRad = Math.atan2(dy, dx);

        double turretAngleRad = angleToTargetRad - robotHeading + getTurretTargetAngleVelocityModifier();

        return normalizeDegrees(Math.toDegrees(turretAngleRad));
    }

    public double getTurretTargetAngleVelocityModifier(){
        Pose currentVelocity = pedroLocalizer.getVelocity();
        if (currentVelocity == null) return 0.0; // no velocity estimate yet -> no lead modifier (avoids NPE)
        double velocityX = currentVelocity.getX();
        double velocityY = currentVelocity.getY();
        double velocityH = currentVelocity.getHeading();
        if (Double.isNaN(velocityX) || Double.isNaN(velocityY) || Double.isNaN(velocityH)
                || Double.isInfinite(velocityX) || Double.isInfinite(velocityY) || Double.isInfinite(velocityH)) {
            return 0.0; // bad velocity estimate -> no lead modifier
        }

        double weightedX = velocityX * 0.005;
        double weightedY = velocityY * 0.005;
        double weightedH = velocityH * 0.1;

        if (GlobalVariables.alliance == Alliance.Red) return weightedX - weightedY - weightedH;
        return weightedX + weightedY - weightedH;
    }

    /** True when either transfer-path IR sensor (digital port 3 or 5) sees an artifact. */
    public boolean isTransferBall() {
        return isInfraRedObstructed(InfraRedSensors.infraTransfer1)
                || isInfraRedObstructed(InfraRedSensors.infraTransfer2);
    }

    /** True when either intake IR sensor (digital port 6 or 7) sees an artifact. */
    public boolean isIntakeBall() {
        return isInfraRedObstructed(InfraRedSensors.infraIntake1)
                || isInfraRedObstructed(InfraRedSensors.infraIntake2);
    }

    private double getTrimmedAverage(List<Double> data) {
        if (data.isEmpty()) return 0;
        List<Double> copy = new ArrayList<>(data);
        Collections.sort(copy);
        if (copy.size() <= 2) {
            double sum = 0;
            for (double d : copy) sum += d;
            return sum / copy.size();
        }
        double sum = 0;
        for (int i = 1; i < copy.size() - 1; i++) {
            sum += copy.get(i);
        }
        return sum / (copy.size() - 2);
    }

    private double normalizeDegrees(double angle) {
        while (angle > 180) angle -= 360;
        while (angle < -180) angle += 360;
        return angle;
    }

    private double normalizeRadians(double radians) {
        while (radians > Math.PI) radians -= 2 * Math.PI;
        while (radians < -Math.PI) radians += 2 * Math.PI;
        return radians;
    }

}
