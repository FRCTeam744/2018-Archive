package org.usfirst.frc.tm744yr18.robot.commands;

import org.usfirst.frc.tm744yr18.robot.interfaces.TmToolsI.P;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmToolsI.P.PrtYn;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmToolsI.Tt;
import org.usfirst.frc.tm744yr18.robot.subsystems.TmSsArm;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.command.Command;

public class TmTCmdArmClearEncoderOverride extends Command {

	TmSsArm ssArm;
	DriverStation m_ds;

	public TmTCmdArmClearEncoderOverride() {
		m_ds = DriverStation.getInstance();
		ssArm = TmSsArm.getInstance();
		requires(ssArm);
	}

	// Called just before this Command runs the first time
	protected void initialize() {
		if(m_ds.isEnabled() && m_ds.isOperatorControl()) {
			P.println(PrtYn.N, Tt.getClassName(this) + " initializing/completing");
			TmSsArm.ArmServices.requestClearEncoderOverride();
		}
	}

	// Called repeatedly when this Command is scheduled to run
	protected void execute() {
	}

	@Override
	protected boolean isFinished() {
		return true;
	}

}
