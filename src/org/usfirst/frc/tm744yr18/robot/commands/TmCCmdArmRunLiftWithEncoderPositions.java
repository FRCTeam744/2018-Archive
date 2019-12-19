package org.usfirst.frc.tm744yr18.robot.commands;


import org.usfirst.frc.tm744yr18.robot.interfaces.TmToolsI;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmToolsI.P.PrtYn;
import org.usfirst.frc.tm744yr18.robot.subsystems.TmSsArm;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.command.Command;

/**
*
*/
public class TmCCmdArmRunLiftWithEncoderPositions extends Command implements TmToolsI {

	TmSsArm ssArm;
	DriverStation m_ds;
	int stage1Pos;
	int stage2Pos;
	TmSsArm.LiftPosE liftPosToUse;
	PrtYn flag = PrtYn.N;
	
//	public enum LiftPosE { TOP, MID, SWITCH, BOTTOM, USER }
//
	public TmCCmdArmRunLiftWithEncoderPositions(int stg1Pos, int stg2Pos) {
		m_ds = DriverStation.getInstance();
		ssArm = TmSsArm.getInstance();
		stage1Pos = stg1Pos;
		stage2Pos = stg2Pos;
		liftPosToUse = TmSsArm.LiftPosE.USER;
		requires(ssArm);
	}
	public TmCCmdArmRunLiftWithEncoderPositions(TmSsArm.LiftPosE requestedPos) {
		m_ds = DriverStation.getInstance();
		ssArm = TmSsArm.getInstance();
		liftPosToUse = requestedPos;
		stage1Pos = 0;
		stage2Pos = 0;
		requires(ssArm);
	}

	// Called just before this Command runs the first time
	protected void initialize() {
		P.println(flag, Tt.getClassName(this) + " initializing");
		switch(liftPosToUse) {
		case BOTTOM: //POV-W???
		case SWITCH: //btn A???
		case SCALE_MID: //POV-NE???
		case TOP: //btn Y???
		case SCALE_LOW: //btn X??
			stage1Pos = TmSsArm.ArmServices.getFixedEncoderPosition(liftPosToUse);
			break;
		case SCALE_HIGH:
		case MAX_HEIGHT:
			P.println(PrtYn.Y, "no DsNamedControlsE entries for fixed encoder position " + liftPosToUse.name());
			break;
		case USER:
		//default:
			break;
		}
		TmSsArm.ArmServices.requestBothStagesServo(stage1Pos, stage2Pos);
	}

	// Called repeatedly when this Command is scheduled to run
	protected void execute() {
//		if(m_ds.isEnabled() && m_ds.isOperatorControl()) {
//			double stage1joy = DsNamedControlsE.ARM_TEST_STAGE1_MTR_INPUT.getAnalog();
//			double stage2joy = DsNamedControlsE.ARM_TEST_STAGE2_MTR_INPUT.getAnalog();
//			if(stage1joy == stage2joy) {} //just a good debug breakpoint
//			TmSsArm.ArmServices.requestNudgeServos(stage1joy, stage2joy);
//		}
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
		P.println(flag, Tt.getClassName(this) + " ending");
	}

	// Called when another command which requires one or more of the same
	// subsystems is scheduled to run
	protected void interrupted() {
		P.println(flag, Tt.getClassName(this) + " interrupted");

	}
}

