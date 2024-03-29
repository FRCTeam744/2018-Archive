// RobotBuilder Version: 2.0
//
// This file was generated by RobotBuilder. It contains sections of
// code that are automatically generated and assigned by robotbuilder.
// These sections will be updated in the future when you export to
// Java from RobotBuilder. Do not put any code or make any change in
// the blocks indicating autogenerated code or it will be lost on an
// update. Deleting the comments indicating the section will prevent
// it from being updated in the future.


package org.usfirst.frc.tm744yr18.robot.commands;


import org.usfirst.frc.tm744yr18.robot.helpers.TmDriverStation;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmCGrpCallbackI;
import org.usfirst.frc.tm744yr18.robot.subsystems.TmSsDriveTrain;

import edu.wpi.first.wpilibj.command.Command;

/**
 * naming convention: TmACmd prefix indicates an autonomous command
 * 					  TmCCmd prefix indicates a common (to auton/teleop) command
 * 					  TmTCmd prefix indicates a telelop command
 */
public class TmACmdDriveStraight extends Command {

	TmSsDriveTrain ssDrive;
	TmDriverStation m_tds;
	double m_speed;
	TmCGrpCallbackI m_callbackObj = null;
	boolean m_isOkToRun = true;
	
    public TmACmdDriveStraight(double time, double speed) {
    	this(null, time, speed);
    }
    public TmACmdDriveStraight(TmCGrpCallbackI callbackObj, double time, double speed) {
    	m_tds = TmDriverStation.getInstance();
    	ssDrive = TmSsDriveTrain.getInstance();
    	requires(ssDrive);
    	setTimeout(time); //scheduler will start the timeout just before calling initialize()
    	m_speed = speed;
    	m_callbackObj = callbackObj;
    }

    // Called just before this Command runs the first time
    protected void initialize() {
    	if( m_tds.isEnabledAutonomous() ) {
    		
    	}
    	
    	m_isOkToRun = true;
    	if(m_callbackObj != null) {
    		m_isOkToRun = m_callbackObj.isOkToRunCmd();
    	}
    }

    // Called repeatedly when this Command is scheduled to run
    protected void execute() {
    	if(m_isOkToRun) {
    	if( m_tds.isEnabledAutonomous() ) {
    		TmSsDriveTrain.Driving.tankDriveRawPercentOutput(m_speed, m_speed);
    	}  
    	}
    }

    // Make this return true when this Command no longer needs to run execute()
    protected boolean isFinished() {
    	boolean ans = false;
    	if(m_isOkToRun) {
    		if( ( ! m_tds.isEnabledAutonomous()) || isTimedOut()) {
    			ans = true;

    		} else {
    			ans = false;
    		}
    	} else {
    		ans = true;
    	}
    	return ans;
    }

    // Called once after isFinished returns true
    protected void end() {
    	if( m_tds.isEnabledAutonomous() ) {
    		TmSsDriveTrain.DrvServices.stopAllMotors();
	
    	}
    }

    // Called when another command which requires one or more of the same
    // subsystems is scheduled to run
    protected void interrupted() {
    	if( m_tds.isEnabledAutonomous() ) {
    		TmSsDriveTrain.DrvServices.stopAllMotors();

    	}
    }
}
