package org.firstinspires.ftc.teamcode.Autonomous.Autos.GoalAutonSolo;

public enum GoalAutonSoloState {
    drive_startPos_shootPos,
    shoot_preload,
    drive_shootPos_takeFirstPos,
    drive_takeFirstPos_shootPos,
    shoot_first,

    // Loop
    drive_shootPos_openGatePos,
    drive_openGatePos_takeGatePos,
    drive_takeGatePos_shootPos,
    shoot_gate,

    // End Loop
    drive_shootPos_takeSecondPos_shootPos,
    shoot_second,

    drive_shootPos_takeThirdPos,
    drive_takeThirdPos_shootPosPark,
    shoot_third,

    stop,
    idle
}