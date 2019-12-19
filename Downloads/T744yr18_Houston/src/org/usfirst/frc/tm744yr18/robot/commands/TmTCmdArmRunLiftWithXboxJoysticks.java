package org.usfirst.frc.tm744yr18.robot.commands;


import org.usfirst.frc.tm744yr18.robot.interfaces.TmToolsI;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmToolsI.P.PrtYn;
import org.usfirst.frc.tm744yr18.robot.subsystems.TmSsArm;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.command.Command;

/**
*
*/
public class TmTCmdArmRunLiftWithXboxJoysticks extends Command implements TmToolsI {

	TmSsArm ssArm;
	DriverStation m_ds;

	public TmTCmdArmRunLiftWithXboxJoysticks() {
		m_ds = DriverStation.getInstance();
		ssArm = TmSsArm.getInstance();
		requires(ssArm);
	}

	// Called just before this Command runs the first time
	protected void initialize() {
		P.println(PrtYn.N, Tt.getClassName(this) + " initializing");
		if(m_ds.isEnabled() && m_ds.isOperatorControl()) {
			TmSsArm.ArmServices.requestRunBothStagesByJoystick();
		}
	}

	// Called repeatedly when this Command is scheduled to run
	protected void execute() {
		if(m_ds.isEnabled() && m_ds.isOperatorControl()) {
//			double stage1joy = DsNamedControlsE.ARM_TEST_STAGE1_MTR_INPUT.getAnalog();
//			double stage2joy = DsNamedControlsE.ARM_TEST_STAGE2_MTR_INPUT.getAnalog();
//			if(stage1joy == stage2joy) {} //just a good debug breakpoint
//			TmSsArm.ArmServices.requestRunBothStagesByJoystick(stage1joy, stage2joy);
		}
	}

	// Make this return true when this Command no longer needs to run execute()
	protected boolean isFinished() {
//		if(m_ds.isEnabled() && m_ds.isOperatorControl()) {
//			return false;
//		} else {
//			TmSsArm.ArmServices.stopAllLiftMotors();
//			return false;
//		}
		return true;
	}

	// Called once after isFinished returns true
	protected void end() {
		P.println(PrtYn.N, Tt.getClassName(this) + " ending");
	}

	// Called when another command which requires one or more of the same
	// subsystems is scheduled to run
	protected void interrupted() {
		P.println(PrtYn.N, Tt.getClassName(this) + " interrupted");

	}
}

