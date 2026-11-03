package org.firstinspires.ftc.teamcode.Subsystems.Outtake;
import org.firstinspires.ftc.teamcode.HardwareInterface.Motor.MotorControl;
import org.firstinspires.ftc.teamcode.Subsystems.Outtake.AutoCycleShoot.AutoCycleShootControl;
import org.firstinspires.ftc.teamcode.Subsystems.Outtake.AutoCycleShoot.AutoCycleShootLogic;
import org.firstinspires.ftc.teamcode.Subsystems.Outtake.AutoOuttakeFarClose.AutoOuttakeFarCloseControl;
import org.firstinspires.ftc.teamcode.Subsystems.Outtake.AutoResetPos.AutoResetPosControl;
import org.firstinspires.ftc.teamcode.Subsystems.Outtake.OuttakeMotor.OuttakeMotorControl;
import org.firstinspires.ftc.teamcode.Subsystems.SubsystemState;

public class OuttakeControl {
    private final OuttakeMotorControl outtakeMotorControl;
    private final AutoCycleShootControl autoCycleShootControl;
    private final AutoCycleShootLogic autoCycleShootLogic;
    private final AutoOuttakeFarCloseControl autoOuttakeFarCloseControl;
    private final AutoResetPosControl autoResetPosControl;

    // NOTE: the turret is intentionally NOT updated here. In TeleOp it runs on the
    // dedicated TurretThread; in Autonomous it is updated by AutonomousControl.
    public OuttakeControl(OuttakeMotorControl outtakeMotorControl, AutoCycleShootLogic autoCycleShootLogic, AutoOuttakeFarCloseControl autoOuttakeFarCloseControl, AutoResetPosControl autoResetPosControl, MotorControl motorControl) {
        this.outtakeMotorControl = outtakeMotorControl;
        this.autoCycleShootLogic = autoCycleShootLogic;
        this.autoOuttakeFarCloseControl = autoOuttakeFarCloseControl;
        this.autoResetPosControl = autoResetPosControl;
        this.autoCycleShootControl = new AutoCycleShootControl(motorControl);
    }

    public void update() {
        outtakeMotorControl.update();
        autoCycleShootControl.update();
        autoCycleShootLogic.update();
        autoOuttakeFarCloseControl.update();
        autoResetPosControl.update();

        updateOuttakeState();
    }

    private void updateOuttakeState(){
        if(outtakeActive())
            OuttakeStates.setOuttakeState(SubsystemState.Run);
        else
            OuttakeStates.setOuttakeState(SubsystemState.Idle);
    }

    private boolean outtakeActive() {
        return false;
    }
}