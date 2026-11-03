package org.firstinspires.ftc.teamcode.Subsystems.Intake.LED;

import org.firstinspires.ftc.teamcode.HardwareInterface.Motor.MotorConstants;
import org.firstinspires.ftc.teamcode.HardwareInterface.Motor.MotorControl;
import org.firstinspires.ftc.teamcode.HardwareInterface.Sensor.SensorControl;
import org.firstinspires.ftc.teamcode.Main.GlobalVariables;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.AutoIntakeTransfer.AutoIntakeTransferStates;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.IntakeMotor.IntakeMotorStates;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.IntakeStates;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.LockServo.LockServoStates;
import org.firstinspires.ftc.teamcode.Subsystems.Intake.TransferMotor.TransferMotorStates;
import org.firstinspires.ftc.teamcode.Subsystems.Outtake.AutoCycleShoot.AutoCycleShootStates;
import org.firstinspires.ftc.teamcode.Subsystems.Outtake.OuttakeConstants;
import org.firstinspires.ftc.teamcode.Subsystems.Outtake.TurretServo.TurretServoControl;

public class LEDLogic {
    private final SensorControl sensorControl;
    private final TurretServoControl turretServoControl;
    private final MotorControl motorControl;

    private double blueFlashEndTime = -1;
    private boolean prevIntakeActive = false;

    public LEDLogic(SensorControl sensorControl, TurretServoControl turretServoControl, MotorControl motorControl) {
        this.sensorControl = sensorControl;
        this.turretServoControl = turretServoControl;
        this.motorControl = motorControl;
    }

    public void update() {
        if (GlobalVariables.isAutonomous) return;
        boolean intakeActive = IntakeStates.getAutoIntakeTransferState() != AutoIntakeTransferStates.stop && IntakeStates.getAutoIntakeTransferState() != AutoIntakeTransferStates.idle;
        boolean transferActive = IntakeStates.getAutoIntakeTransferState() != AutoIntakeTransferStates.stopTransfer && IntakeStates.getAutoIntakeTransferState() != AutoIntakeTransferStates.checkAgainFront && IntakeStates.getAutoIntakeTransferState() != AutoIntakeTransferStates.stop && IntakeStates.getAutoIntakeTransferState() != AutoIntakeTransferStates.idle;

        // Detect intake turning off
        if (prevIntakeActive && !intakeActive && IntakeStates.getLockServoState() == LockServoStates.lock) {
            // Only flash blue if it wasn't a manual stop via gamepad
            if (!IntakeStates.isManualStop()) {
                blueFlashEndTime = getSeconds() + 0.5;
            }
        }
        prevIntakeActive = intakeActive;

        if (intakeActive) {
            // Intake is active
            if (!transferActive) {
                IntakeStates.setLEDState(LEDStates.intakeTwo); // Orange (Transfer off with ball)
            } else {
                IntakeStates.setLEDState(LEDStates.intakeNo); // Off
            }
        } else if (getSeconds() < blueFlashEndTime) {
            // Blue flash for 0.5s after intake turns off
            IntakeStates.setLEDState(LEDStates.intakeThree);
        } else {
            // Outtake indication
            double turretAngle = turretServoControl.getTurretAngleDeg();
            double leftLimit = OuttakeConstants.turretLimitLeft;
            double rightLimit = OuttakeConstants.turretLimitRight;

            // Shine red if it is less than 2 degrees to the limit and if it is beyond the limit
            if (turretAngle >= leftLimit - 2.0 || turretAngle <= rightLimit + 2.0) {
                System.out.println("RED color of led");
                IntakeStates.setLEDState(LEDStates.outtakeNo); // Red
            } else if (turretAngle >= leftLimit - 5.0 || turretAngle <= rightLimit + 5.0) {
                IntakeStates.setLEDState(LEDStates.outtakeMaybe); // Orange
            } else {
                double currentRPM = motorControl.getMotorVelocity(MotorConstants.outtake);
                double targetRPM = GlobalVariables.outtakeTargetSpeed + GlobalVariables.rpmOffset;
                // Check if speed is within 100 RPM of target
                if (Math.abs(currentRPM - targetRPM) < 100 && targetRPM > 100) {
                    IntakeStates.setLEDState(LEDStates.outtakeYes); // Green
                } else {
                    System.out.println("RED color of led");
                    IntakeStates.setLEDState(LEDStates.outtakeNo); // Red
                }
            }
        }
    }

    private double getSeconds() {
        return System.currentTimeMillis() / 1000.0;
    }
}
