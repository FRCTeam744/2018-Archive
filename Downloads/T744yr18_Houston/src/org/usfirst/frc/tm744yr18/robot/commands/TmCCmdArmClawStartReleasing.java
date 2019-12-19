package org.usfirst.frc.tm744yr18.robot.commands;

import org.usfirst.frc.tm744yr18.robot.interfaces.TmToolsI.P;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmToolsI.P.PrtYn;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmToolsI.Tt;
import org.usfirst.frc.tm744yr18.robot.subsystems.TmSsArm;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.command.Command;

public class TmCCmdArmClawStartReleasing extends Command {

	TmSsArm ssArm;
	DriverStation m_ds;

	public TmCCmdArmClawStartReleasing() {
		m_ds = DriverStation.getInstance();
		ssArm = TmSsArm.getInstance();
		requires(ssArm);
	}

	// Called just before this Command runs the first time
	protected void initialize() {
		P.println(PrtYn.N, Tt.getClassName(this) + " initializing");
		TmSsArm.ArmServices.requestStartClawReleasing();
	}

	// Called repeatedly when this Command is scheduled to run
	protected void execute() {
//		if(m_ds.isEnabled() && m_ds.isOperatorControl()) {
//			TmSsArm.ArmServices.requestRunClawWithJoystick(joystickRdg);
//		}
//		TmSsArm.ArmServices.requestClawMotorsOff();
	}

	@Override
	protected boolean isFinished() {
		return true;
	}



}
