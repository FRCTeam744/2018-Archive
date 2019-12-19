package org.usfirst.frc.tm744yr18.robot.cmdGroups;

import org.usfirst.frc.tm744yr18.robot.commands.TmCCmdGrabberClampAssistOpenOrClose;
import org.usfirst.frc.tm744yr18.robot.commands.TmCCmdGrabberSetPositionAndWedge;
import org.usfirst.frc.tm744yr18.robot.subsystems.TmSsGrabber.GrabberCubeClampAssistE;
import org.usfirst.frc.tm744yr18.robot.subsystems.TmSsGrabber.GrabberMotionAndWedgeMgmt.GrabberMotionAndWedgeRequestsE;

import edu.wpi.first.wpilibj.command.CommandGroup;

@Deprecated //OK to use, just think carefully what you're doing!
public class TmCCGrpGrabberUpClampedDontCareWedge extends CommandGroup {
	public TmCCGrpGrabberUpClampedDontCareWedge(){
		addSequential (new TmCCmdGrabberClampAssistOpenOrClose(GrabberCubeClampAssistE.CLAMPED));
		addSequential (new TmCCmdGrabberSetPositionAndWedge(GrabberMotionAndWedgeRequestsE.REQ_GRABBER_UP));
		addSequential (new TmCCGrpArmRunLiftWheelsForGrabberUp());
	}
}
