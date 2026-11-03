package org.firstinspires.ftc.teamcode.Autonomous.Paths.GoalPaths;

import com.pedropathing.geometry.Pose;
import com.pedropathing.paths.PathChain;

public interface GoalPaths {

    Pose getStartPose();
    PathChain startPos_shootPos();
    PathChain shootPos_takeFirstPos();
    PathChain takeFirstPos_shootPos();
    PathChain shootPos_openGatePosBreak();
    PathChain openGatePosBreak_openGatePos();
    PathChain openGatePos_openGatePosBreak_openGatePosNew();
    PathChain openGatePos_shootPos();
    PathChain shootPos_takeSecondPos_shootPos();
    PathChain shootPos_takeThirdPos();
    PathChain takeThirdPos_shootPosPark();
    PathChain shootPos_takeSecondPos_shootPosPark();
}