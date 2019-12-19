package org.usfirst.frc.tm744yr18.robot.commands;

import org.usfirst.frc.tm744yr18.robot.interfaces.TmToolsI.P;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmToolsI.Tt;
import org.usfirst.frc.tm744yr18.robot.subsystems.TmSsGrabber;
import org.usfirst.frc.tm744yr18.robot.subsystems.TmSsGrabber.GrabberCubeLiftE;
import org.usfirst.frc.tm744yr18.robot.subsystems.TmSsGrabber.GrabberMotionAndWedgeMgmt.GrabberMotionAndWedgeRequestsE;
import org.usfirst.frc.tm744yr18.robot.subsystems.TmSsGrabber.GrabberPositionAndWedgeReturnCodesE;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.command.Command;

public class TmCCmdGrabberSetPositionAndWedge extends Command {

	TmSsGrabber ssGrabber;
	DriverStation m_ds;
	GrabberMotionAndWedgeRequestsE m_actionToRequest;
	GrabberPositionAndWedgeReturnCodesE m_retCode;

	public TmCCmdGrabberSetPositionAndWedge(GrabberMotionAndWedgeRequestsE request) {
		m_ds = DriverStation.getInstance();
		ssGrabber = TmSsGrabber.getInstance();
		requires(ssGrabber);
		m_actionToRequest = request;
	}

	// Called just before this Command runs the first time
	protected void initialize() {
		P.println(Tt.getClassName(this) + "(" + m_actionToRequest.name() + ") initializing");
		m_retCode = TmSsGrabber.updateGrabberPositionAndWedge(m_actionToRequest);
	}

	// Called repeatedly when this Command is scheduled to run
	protected void execute() {
		if(m_retCode.equals(GrabberPositionAndWedgeReturnCodesE.BUSY) ||
				m_retCode.equals(GrabberPositionAndWedgeReturnCodesE.FAILED)) {
			m_retCode = TmSsGrabber.updateGrabberPositionAndWedge(m_actionToRequest);
		}
	}

	@Override
	protected boolean isFinished() {
		boolean ans = false;
		if(TmSsGrabber.isInState(m_actionToRequest)) {
			ans = true;
		}
		return ans;
	}
	
}
