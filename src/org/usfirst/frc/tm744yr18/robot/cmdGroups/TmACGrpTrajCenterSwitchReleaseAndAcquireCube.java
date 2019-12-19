package org.usfirst.frc.tm744yr18.robot.cmdGroups;

import org.usfirst.frc.tm744yr18.robot.commands.TmACmdFollowTrajectory;
import org.usfirst.frc.tm744yr18.robot.commands.TmACmdFollowTrajectoryLeftOrRight;
import org.usfirst.frc.tm744yr18.robot.commands.TmCCmdArmRunLiftWithEncoderPositions;
import org.usfirst.frc.tm744yr18.robot.commands.TmACmdFollowTrajectoryLeftOrRight.ShowFile;
import org.usfirst.frc.tm744yr18.robot.commands.TmACmdFollowTrajectoryLeftOrRight.TrajDest;
import org.usfirst.frc.tm744yr18.robot.commands.TmCCmdDelay;
import org.usfirst.frc.tm744yr18.robot.commands.TmCCmdGrabberClampAssistOpenOrClose;
import org.usfirst.frc.tm744yr18.robot.commands.TmCCmdGrabberSetMotorState;
import org.usfirst.frc.tm744yr18.robot.commands.TmCCmdGrabberSetPositionAndWedge;
import org.usfirst.frc.tm744yr18.robot.subsystems.TmSsArm;
import org.usfirst.frc.tm744yr18.robot.subsystems.TmSsArm.LiftPosE;
import org.usfirst.frc.tm744yr18.robot.subsystems.TmSsGrabber.GrabberCubeClampAssistE;
import org.usfirst.frc.tm744yr18.robot.subsystems.TmSsGrabber.GrabberMotorStateE;
import org.usfirst.frc.tm744yr18.robot.subsystems.TmSsGrabber.GrabberMotionAndWedgeMgmt.GrabberMotionAndWedgeRequestsE;

import edu.wpi.first.wpilibj.command.CommandGroup;

public class TmACGrpTrajCenterSwitchReleaseAndAcquireCube extends CommandGroup {

//	TmSsAutonomous				new alg for center switch auto: ALG_TRAJ_CENTER_TO_SWITCH_RELEASE_AND_GET_CUBE
//	reduce delay after trajectory
//	traj trajRightSwitchToCenter.csv trajLeftSwitchToCenter.csv  and arm down
//	grabber down and unclamp
//	traj trajCenterToCube1.csv and run grabber in
//	clamp (grabber still running)
//	delay (make shorter)
//	grabber up an arm claw grabbing grabber 

	public TmACGrpTrajCenterSwitchReleaseAndAcquireCube() { //String fileLeft, String fileRight, ShowFile showLeft, ShowFile showRight) {

		//in parallel with the following addSequential command
//		addParallel(new TmCCmdGrabberSetPositionAndWedge(GrabberMotionAndWedgeRequestsE.REQ_GRABBER_UP_NO_WEDGE));
		addSequential (new TmACmdFollowTrajectoryLeftOrRight(TrajDest.SWITCH, LiftPosE.SWITCH, 
				"trajCenterToLeftSwitch.csv", "trajCenterToRightSwitch.csv",
				ShowFile.N, ShowFile.N));
		addSequential (new TmACGrpAtSwitchReleaseCube());
		addSequential (new TmCCmdDelay(0.05));
		addSequential (new TmACmdFollowTrajectoryLeftOrRight(TrajDest.SWITCH, LiftPosE.BOTTOM, 
				"trajLeftSwitchToCenter.csv", "trajRightSwitchToCenter.csv", ShowFile.N, ShowFile.N));
		addSequential (new TmCCmdArmRunLiftWithEncoderPositions(TmSsArm.LiftPosE.BOTTOM)); //NEW
		addSequential(new TmCCmdGrabberSetPositionAndWedge(GrabberMotionAndWedgeRequestsE.REQ_GRABBER_DOWN_WEDGE_RETRACT) );
		addSequential(new TmCCmdGrabberSetMotorState(GrabberMotorStateE.GRABBING) );
    	addSequential(new TmCCmdGrabberClampAssistOpenOrClose(GrabberCubeClampAssistE.UNCLAMPED) );
		addSequential (new TmACmdFollowTrajectory("trajCenterToCube1.csv", 'A', 'A', TmACmdFollowTrajectory.NO_SHOW_FILE));
		addSequential (new TmCCmdDelay(0.5));
		addParallel(new TmCCGrpGrabberUpClampedNoWedge()); //runs arm claw too
		addSequential (new TmCCmdDelay(0.5));
		addSequential(new TmCCmdGrabberSetMotorState(GrabberMotorStateE.OFF));
		addSequential(new TmCCmdDelay(0.05));
		addSequential(new TmACmdFollowTrajectory("trajCube1ToCenter.csv", 'A', 'A', TmACmdFollowTrajectory.NO_SHOW_FILE));
		addSequential(new TmCCmdDelay(0.05));
		addSequential (new TmACmdFollowTrajectoryLeftOrRight(TrajDest.SWITCH, LiftPosE.SWITCH, 
				"trajCenterToLeftSwitch.csv", "trajCenterToRightSwitch.csv",
				ShowFile.N, ShowFile.N));
		addSequential (new TmACGrpAtSwitchReleaseCube());
	}
}
