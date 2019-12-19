package org.usfirst.frc.tm744yr18.robot.cmdGroups;

import org.usfirst.frc.tm744yr18.robot.commands.TmACmdFollowTrajectoryLeftOrRight;
import org.usfirst.frc.tm744yr18.robot.commands.TmACmdFollowTrajectoryLeftOrRight.ShowFile;
import org.usfirst.frc.tm744yr18.robot.commands.TmACmdFollowTrajectoryLeftOrRight.TrajDest;
import org.usfirst.frc.tm744yr18.robot.subsystems.TmSsArm.LiftPosE;

import edu.wpi.first.wpilibj.command.CommandGroup;

public class TmACGrpTrajRightSwitchAndRelease extends CommandGroup  {
	public TmACGrpTrajRightSwitchAndRelease(String leftFile, String rightFile){
		addSequential (new TmACmdFollowTrajectoryLeftOrRight(TrajDest.SWITCH, LiftPosE.SWITCH,
				leftFile, rightFile, ShowFile.N, ShowFile.N));
		addSequential (new TmACGrpAtSwitchReleaseCube());
		}
	}

