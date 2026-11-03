package org.firstinspires.ftc.teamcode.Autonomous.Paths.AudiencePaths;

import com.pedropathing.geometry.Pose;
import com.pedropathing.paths.Path;
import com.pedropathing.paths.PathChain;

public interface AudiencePaths {

    Pose getPt_startPose();
    PathChain startPos_shootPos();
    PathChain shootPos_takeThreePos();
    PathChain takeThreePos_shootPos();
    PathChain shootPos_takeBottomPos();
    PathChain takeBottomPos_shootPos();
    PathChain takeBottomPos_takeUpPos();
    PathChain takeUpPos_shootPos();
    PathChain shootPos_park();
}