package org.usfirst.frc.tm744yr18.robot.commands;

import org.usfirst.frc.tm744yr18.robot.interfaces.TmToolsI.P;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmToolsI.Tt;
import org.usfirst.frc.tm744yr18.robot.subsystems.TmSsGrabber;
import org.usfirst.frc.tm744yr18.robot.subsystems.TmSsGrabber.GrabberCubeClampAssistE;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.command.Command;

public class TmCCmdGrabberClampAssistOpenOrClose extends Command {

	TmSsGrabber ssGrabber;
	DriverStation m_ds;
	GrabberCubeClampAssistE m_openOrClose;

	public TmCCmdGrabberClampAssistOpenOrClose(GrabberCubeClampAssistE openOrClose) {
		m_ds = DriverStation.getInstance();
		ssGrabber = TmSsGrabber.getInstance();
		requires(ssGrabber);
		m_openOrClose = openOrClose;
	}

	// Called just before this Command runs the first time
	protected void initialize() {
		P.println(Tt.getClassName(this) + "(" + m_openOrClose.name() + ") initializing");
		TmSsGrabber.updateGrabberCubeClampAssist(m_openOrClose);
	}

	// Called repeatedly when this Command is scheduled to run
	protected void execute() {
	}

	@Override
	protected boolean isFinished() {
		return true;
	}

}
