package org.usfirst.frc.tm744yr18.robot.cmdGroups;

import org.usfirst.frc.tm744yr18.robot.commands.TmCCmdArmClawStartReleasing;
import org.usfirst.frc.tm744yr18.robot.commands.TmCCmdArmClawStopMotors;
import org.usfirst.frc.tm744yr18.robot.commands.TmCCmdDelay;
import org.usfirst.frc.tm744yr18.robot.commands.TmCCmdGrabberClampAssistOpenOrClose;
import org.usfirst.frc.tm744yr18.robot.commands.TmCCmdGrabberSetPositionAndWedge;
import org.usfirst.frc.tm744yr18.robot.subsystems.TmSsGrabber.GrabberCubeClampAssistE;
import org.usfirst.frc.tm744yr18.robot.subsystems.TmSsGrabber.GrabberMotionAndWedgeMgmt.GrabberMotionAndWedgeRequestsE;

import edu.wpi.first.wpilibj.command.CommandGroup;

public class TmCCGrpGrabberDownClampedNoWedge extends CommandGroup {
	public TmCCGrpGrabberDownClampedNoWedge(){
		addSequential (new TmCCmdArmClawStartReleasing());
		addSequential(new TmCCmdDelay(0.025));	
		addSequential (new TmCCmdGrabberSetPositionAndWedge(GrabberMotionAndWedgeRequestsE.REQ_GRABBER_DOWN_WEDGE_RETRACT));
		addSequential (new TmCCmdGrabberClampAssistOpenOrClose(GrabberCubeClampAssistE.CLAMPED));
		
		//runs in parallel with the command(s) that follow (list ends with addSequential)
		addSequential(new TmCCmdDelay(0.500));		
		addSequential(new TmCCmdArmClawStopMotors());
	}
}
