package org.usfirst.frc.tm744yr18.robot.cmdGroups;

import org.usfirst.frc.tm744yr18.robot.commands.TmACmdDoNothingForXSeconds;
import org.usfirst.frc.tm744yr18.robot.commands.TmACmdFollowTrajectorySwitchOrScale;
import org.usfirst.frc.tm744yr18.robot.commands.TmCCmdArmClawStartReleasing;
import org.usfirst.frc.tm744yr18.robot.commands.TmCCmdArmClawStopMotors;
import org.usfirst.frc.tm744yr18.robot.commands.TmACmdFollowTrajectorySwitchOrScale.TrajDest;
import org.usfirst.frc.tm744yr18.robot.subsystems.TmSsArm.LiftPosE;
import org.usfirst.frc.tm744yr18.t744utils.fileIo.TmTrajectoryFileIo;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.command.CommandGroup;

public class TmACGrpTrajMoveToSwitchOrScaleAndRelease extends CommandGroup{

	public TmACGrpTrajMoveToSwitchOrScaleAndRelease(char destLeftOrRight, String fileSwitch, String fileScale, String fileStraight){
		addSequential (new TmACmdFollowTrajectorySwitchOrScale(destLeftOrRight,  fileSwitch,  fileScale,  fileStraight));
//		addSequential (new TmACmdDoNothingForXSeconds(figureOutDelay(destLeftOrRight, 0.5, 1.2)));
		addSequential (new TmCCmdArmClawStartReleasing());
		addSequential (new TmACmdDoNothingForXSeconds(2));
		addSequential (new TmCCmdArmClawStopMotors());
	}

//	//Good catch!:  NB!!! I DON'T THINK THIS WILL RUN WHEN GAME DATA IS AVAILABLE. SINCE THIS IS CALLED FROM A CONSTRUCTOR, 
//	//WHICH GETS BUILT AT ROBOINIT, IT IS NOT GUARENTEED THAT GAMADATA WILL BE AVAILABLE OR CURRENT
//	//regardless, tried implementing a way for the elevator to start while it's finishing driving, 
//	//there is no need for a delay after the follow trajectory
//	protected double figureOutDelay(char destLeftOrRight, double delaySwitch, double delayScale) {
//		double ans = 0.0;
//		String gameData = DriverStation.getInstance().getGameSpecificMessage();
//		if(gameData.length() == 3) {
//			if(gameData.charAt(TrajDest.SCALE.eCharIndex)==destLeftOrRight) {
//				ans = delayScale;
//			} 
//			else if (gameData.charAt(TrajDest.SWITCH.eCharIndex)==destLeftOrRight){
//				ans =(delaySwitch);
//			}
//			else {
//				ans = 17;
//				//never release, if drive straight
//			}
//		}
//		else { 
//			//don't run auto if no gameData available
//			ans= 17;
//			//never release, if drive straight
//		}
//		return ans;
//	}
}
