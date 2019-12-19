package org.usfirst.frc.tm744yr18.robot.cmdGroups;

import org.usfirst.frc.tm744yr18.robot.commands.TmACmdFollowTrajectoryLeftOrRight;
import org.usfirst.frc.tm744yr18.robot.commands.TmACmdFollowTrajectoryLeftOrRight.ShowFile;
import org.usfirst.frc.tm744yr18.robot.commands.TmACmdFollowTrajectoryLeftOrRight.TrajDest;
import org.usfirst.frc.tm744yr18.robot.subsystems.TmSsArm.LiftPosE;

import edu.wpi.first.wpilibj.command.CommandGroup;

public class TmACGrpTrajLeftSwitchAndRelease extends CommandGroup {
	public TmACGrpTrajLeftSwitchAndRelease(String leftFile, String rightFile){
		//LeftFile = trajLeftToLeftSwitch.csv
		//RightFile = "trajLeftToRightSwitch.csv"
	addSequential (new TmACmdFollowTrajectoryLeftOrRight(TrajDest.SWITCH, LiftPosE.SWITCH, 
			leftFile, rightFile, ShowFile.N, ShowFile.N));
	addSequential (new TmACGrpAtSwitchReleaseCube());
	}
}
