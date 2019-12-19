package org.usfirst.frc.tm744yr18.robot.cmdGroups;

import org.usfirst.frc.tm744yr18.robot.commands.TmACmdFollowTrajectoryLeftOrRight;
import org.usfirst.frc.tm744yr18.robot.commands.TmACmdFollowTrajectoryLeftOrRight.ShowFile;
import org.usfirst.frc.tm744yr18.robot.commands.TmACmdFollowTrajectoryLeftOrRight.TrajDest;
import org.usfirst.frc.tm744yr18.robot.subsystems.TmSsArm.LiftPosE;

import edu.wpi.first.wpilibj.command.CommandGroup;

public class TmACGrpTrajCenterSwitchAndReleaseCube extends CommandGroup {

	public TmACGrpTrajCenterSwitchAndReleaseCube(String fileLeft, String fileRight) {
		this(fileLeft, fileRight, ShowFile.N, ShowFile.N);
	}

	public TmACGrpTrajCenterSwitchAndReleaseCube(String fileLeft, String fileRight, ShowFile showLeft, ShowFile showRight) {

		//in parallel with the following addSequential command
//		addParallel(new TmCCmdGrabberSetPositionAndWedge(GrabberMotionAndWedgeRequestsE.REQ_GRABBER_UP_NO_WEDGE));
		addSequential (new TmACmdFollowTrajectoryLeftOrRight(TrajDest.SWITCH, LiftPosE.SWITCH, fileLeft, fileRight, showLeft, showRight));
//		addSequential (new TmCCmdGrabberClampAssistOpenOrClose(GrabberCubeClampAssistE.UNCLAMPED));
//		addSequential (new TmCCmdArmRunLiftWithEncoderPositions(TmSsArm.LiftPosE.SWITCH)); //Cnst.STAGE1_ENCODER_AT_SWITCH, Cnst.STAGE2_ENCODER_AT_SWITCH));
//		addSequential (new TmACmdDoNothingForXSeconds(0.050));
//		addSequential (new TmCCmdArmClawStartReleasing());
//		addSequential (new TmACmdDoNothingForXSeconds(2));
//		addSequential (new TmCCmdArmClawStopMotors());
		addSequential (new TmACGrpAtSwitchReleaseCube());
	}
}
