package org.usfirst.frc.tm744yr18.robot.cmdGroups;

import org.usfirst.frc.tm744yr18.robot.commands.TmCCmdGrabberClampAssistOpenOrClose;
import org.usfirst.frc.tm744yr18.robot.commands.TmCCmdGrabberSetPositionAndWedge;
import org.usfirst.frc.tm744yr18.robot.commands.TmACmdDoNothingForXSeconds;
import org.usfirst.frc.tm744yr18.robot.commands.TmACmdFollowTrajectoryLeftOrRight;
import org.usfirst.frc.tm744yr18.robot.commands.TmACmdFollowTrajectoryLeftOrRight.ShowFile;
import org.usfirst.frc.tm744yr18.robot.commands.TmACmdFollowTrajectoryLeftOrRight.TrajDest;
import org.usfirst.frc.tm744yr18.robot.commands.TmCCmdArmClawStartGrabbing;
import org.usfirst.frc.tm744yr18.robot.commands.TmCCmdArmClawStartReleasing;
import org.usfirst.frc.tm744yr18.robot.commands.TmCCmdArmClawStopMotors;
import org.usfirst.frc.tm744yr18.robot.commands.TmCCmdArmRunLiftWithEncoderPositions;
import org.usfirst.frc.tm744yr18.robot.commands.TmCCmdDelay;
import org.usfirst.frc.tm744yr18.robot.subsystems.TmSsArm;
import org.usfirst.frc.tm744yr18.robot.subsystems.TmSsArm.Cnst;
import org.usfirst.frc.tm744yr18.robot.subsystems.TmSsGrabber.GrabberCubeClampAssistE;
import org.usfirst.frc.tm744yr18.robot.subsystems.TmSsGrabber.GrabberMotionAndWedgeMgmt.GrabberMotionAndWedgeRequestsE;

import edu.wpi.first.wpilibj.command.CommandGroup;

public class TmACGrpAtSwitchReleaseCube extends CommandGroup {

	public TmACGrpAtSwitchReleaseCube() { //String fileLeft, String fileRight, ShowFile showLeft, ShowFile showRight) {

		//in parallel with the following addSequential command
//		addParallel(new TmCCmdGrabberSetPositionAndWedge(GrabberMotionAndWedgeRequestsE.REQ_GRABBER_UP_NO_WEDGE));
//		addSequential (new TmACmdFollowTrajectoryLeftOrRight(TrajDest.SWITCH, fileLeft, fileRight, showLeft, showRight));
//		addSequential (new TmCCmdGrabberClampAssistOpenOrClose(GrabberCubeClampAssistE.UNCLAMPED));
//		addSequential (new TmCCmdDelay(0.20)); //was: (new TmCCmdDelay(0.050)); //TmACmdDoNothingForXSeconds(0.050));
//		addSequential (new TmCCmdArmRunLiftWithEncoderPositions(TmSsArm.LiftPosE.SWITCH)); //Cnst.STAGE1_ENCODER_AT_SWITCH, Cnst.STAGE2_ENCODER_AT_SWITCH));
//		addSequential (new TmCCmdDelay(0.30)); //750)); //was: (new TmCCmdDelay(0.050)); //TmACmdDoNothingForXSeconds(0.050));
		addSequential (new TmCCmdArmClawStartReleasing());
		addSequential (new TmCCmdDelay(0.75)); //TmACmdDoNothingForXSeconds(2));
		addSequential (new TmCCmdArmClawStopMotors());

	}
}
