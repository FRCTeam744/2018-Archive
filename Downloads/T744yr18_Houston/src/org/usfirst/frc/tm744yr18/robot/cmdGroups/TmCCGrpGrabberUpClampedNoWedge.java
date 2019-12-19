package org.usfirst.frc.tm744yr18.robot.cmdGroups;

import org.usfirst.frc.tm744yr18.robot.commands.TmCCmdArmClawStartGrabbing;
import org.usfirst.frc.tm744yr18.robot.commands.TmCCmdArmRunLiftWithEncoderPositions;
import org.usfirst.frc.tm744yr18.robot.commands.TmCCmdDelay;
import org.usfirst.frc.tm744yr18.robot.commands.TmCCmdGrabberClampAssistOpenOrClose;
import org.usfirst.frc.tm744yr18.robot.commands.TmCCmdGrabberSetPositionAndWedge;
import org.usfirst.frc.tm744yr18.robot.subsystems.TmSsArm;
import org.usfirst.frc.tm744yr18.robot.subsystems.TmSsGrabber.GrabberCubeClampAssistE;
import org.usfirst.frc.tm744yr18.robot.subsystems.TmSsGrabber.GrabberMotionAndWedgeMgmt.GrabberMotionAndWedgeRequestsE;

import edu.wpi.first.wpilibj.command.CommandGroup;

public class TmCCGrpGrabberUpClampedNoWedge extends CommandGroup {
	
	private static final double SECONDS_TO_RUN_ARMLIFT_WHEELS_BEFORE = 0.050;
	private static final double SECONDS_TO_RUN_ARMLIFT_WHEELS_AFTER = 3.0;
	
	public TmCCGrpGrabberUpClampedNoWedge(){
		addSequential (new TmCCmdArmRunLiftWithEncoderPositions(TmSsArm.LiftPosE.BOTTOM));
		addSequential (new TmCCmdGrabberClampAssistOpenOrClose(GrabberCubeClampAssistE.CLAMPED));

		addSequential (new TmCCmdArmClawStartGrabbing());
		addSequential (new TmCCmdDelay(SECONDS_TO_RUN_ARMLIFT_WHEELS_BEFORE));
		
		//run in parallel with following command(s); list ends with addSequential
		addParallel (new TmCCGrpArmRunLiftWheelsForGrabberUp(SECONDS_TO_RUN_ARMLIFT_WHEELS_AFTER));
		addSequential (new TmCCmdGrabberSetPositionAndWedge(GrabberMotionAndWedgeRequestsE.REQ_GRABBER_UP_NO_WEDGE));
	}
}
