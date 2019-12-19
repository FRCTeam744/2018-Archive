package org.usfirst.frc.tm744yr18.robot.commands;

import org.usfirst.frc.tm744yr18.robot.interfaces.TmToolsI.P;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmToolsI.Tt;
import org.usfirst.frc.tm744yr18.robot.subsystems.TmSsGrabber;
import org.usfirst.frc.tm744yr18.robot.subsystems.TmSsGrabber.GrabberMotorStateE;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.command.Command;

public class TmCCmdGrabberSetMotorState extends Command {

	TmSsGrabber ssGrabber;
	DriverStation m_ds;
	GrabberMotorStateE m_newGrabberMotorState;

	public TmCCmdGrabberSetMotorState(GrabberMotorStateE newState) {
		m_ds = DriverStation.getInstance();
		ssGrabber = TmSsGrabber.getInstance();
		m_newGrabberMotorState = newState;
		requires(ssGrabber);
	}

	// Called just before this Command runs the first time
	protected void initialize() {
		P.println(Tt.getClassName(this) + "(" + m_newGrabberMotorState.getClass().getSimpleName() + "."
							+ m_newGrabberMotorState.name() + ") initializing");
		TmSsGrabber.updateGrabberMotorState(m_newGrabberMotorState);
	}

	// Called repeatedly when this Command is scheduled to run
	protected void execute() {
//		if(m_ds.isEnabled() && m_ds.isOperatorControl()) {
//			TmSsGrabber.ArmServices.requestRunClawWithJoystick(joystickRdg);
//		}
//		TmSsGrabber.ArmServices.requestClawMotorsOff();
	}

	@Override
	protected boolean isFinished() {
		return true;
	}

}
