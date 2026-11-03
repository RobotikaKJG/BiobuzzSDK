package org.firstinspires.ftc.teamcode.Autonomous.Paths.GoalSoloPaths;

import com.pedropathing.geometry.Pose;
import com.pedropathing.paths.PathChain;

public interface GoalSoloPaths {

    Pose getStartPose();
    PathChain startPos_shootPos();
    PathChain shootPos_takeFirstPos();
    PathChain takeFirstPos_shootPos();
    PathChain shootPos_openGatePosBreak();
    PathChain openGatePosBreak_openGatePos();
    PathChain openGatePos_takeGatePos();
    PathChain takeGatePos_shootPos();
    PathChain shootPos_takeSecondPos_shootPos();
    PathChain shootPos_takeThirdPos();
    PathChain takeThirdPos_shootPosPark();
}