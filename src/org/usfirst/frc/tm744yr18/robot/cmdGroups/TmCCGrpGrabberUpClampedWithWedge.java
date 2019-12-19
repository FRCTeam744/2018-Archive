package org.usfirst.frc.tm744yr18.robot.cmdGroups;

import org.usfirst.frc.tm744yr18.robot.commands.TmCCmdArmClawStartGrabbing;
import org.usfirst.frc.tm744yr18.robot.commands.TmCCmdArmClawStopMotors;
import org.usfirst.frc.tm744yr18.robot.commands.TmCCmdArmRunLiftWithEncoderPositions;
import org.usfirst.frc.tm744yr18.robot.commands.TmCCmdDelay;
import org.usfirst.frc.tm744yr18.robot.commands.TmCCmdGrabberClampAssistOpenOrClose;
import org.usfirst.frc.tm744yr18.robot.commands.TmCCmdGrabberSetPositionAndWedge;
import org.usfirst.frc.tm744yr18.robot.subsystems.TmSsArm;
import org.usfirst.frc.tm744yr18.robot.subsystems.TmSsGrabber.GrabberCubeClampAssistE;
import org.usfirst.frc.tm744yr18.robot.subsystems.TmSsGrabber.GrabberMotionAndWedgeMgmt.GrabberMotionAndWedgeRequestsE;

import edu.wpi.first.wpilibj.command.CommandGroup;

public class TmCCGrpGrabberUpClampedWithWedge extends CommandGroup {
	
	private static final double SECONDS_TO_RUN_ARMLIFT_WHEELS_BEFORE = 0.050;
	private static final double SECONDS_TO_RUN_ARMLIFT_WHEELS_AFTER = 3.0;
	
	public TmCCGrpGrabberUpClampedWithWedge(){
		addSequential (new TmCCmdArmRunLiftWithEncoderPositions(TmSsArm.LiftPosE.BOTTOM));
		addSequential (new TmCCmdGrabberClampAssistOpenOrClose(GrabberCubeClampAssistE.CLAMPED));
		addSequential (new TmCCmdDelay(0.5));
//		addSequential (new TmCCmdArmClawStartGrabbing());
		addSequential (new TmCCmdDelay(SECONDS_TO_RUN_ARMLIFT_WHEELS_BEFORE));
		
		//run in parallel with following command(s); list ends with addSequential
//		addParallel (new TmCCGrpArmRunLiftWheelsForGrabberUp()); //runs for two seconds
//		addSequential(new TmCCmdArmClawStartGrabbing());
//		addSequential(new TmCCmdDelay(2.0)); //TmACmdDoNothingForXSeconds(secondsToRunWheels));
//		addSequential(new TmCCmdArmClawStopMotors());
//		addSequential(new TmCCmdDelay(0.2));
		addSequential (new TmCCmdGrabberSetPositionAndWedge(GrabberMotionAndWedgeRequestsE.REQ_GRABBER_UP_WITH_WEDGE));
	}
}
 