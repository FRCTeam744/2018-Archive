package org.usfirst.frc.tm744yr18.robot.commands;


import org.usfirst.frc.tm744yr18.robot.config.TmHdwrDsCntls.DsNamedControlsE;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmDsControlUserI;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmToolsI;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmToolsI.P.PrtYn;
import org.usfirst.frc.tm744yr18.robot.subsystems.TmSsDriveTrain;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.command.Command;

/**
*
*/
public class TmTCmdDriveWithJoysticks extends Command implements TmToolsI, TmDsControlUserI {

	TmSsDriveTrain ssDrive;
	DriverStation m_ds;
	
 public TmTCmdDriveWithJoysticks() {
 	m_ds = DriverStation.getInstance();
 	ssDrive = TmSsDriveTrain.getInstance();
    requires(ssDrive);
    DsNamedControlsE.DRIVE_LEFT_INPUT.getEnt().registerAsDsCntlUser(this);
    DsNamedControlsE.DRIVE_RIGHT_INPUT.getEnt().registerAsDsCntlUser(this);
 }

 // Called just before this Command runs the first time
 protected void initialize() {
	 P.println(Tt.getClassName(this) + " initializing");
 }

 // Called repeatedly when this Command is scheduled to run
 protected void execute() {
 	if(m_ds.isEnabled() && m_ds.isOperatorControl()) {
 		double leftJoy = DsNamedControlsE.DRIVE_LEFT_INPUT.getEnt().getAnalog();
 		double rightJoy = DsNamedControlsE.DRIVE_RIGHT_INPUT.getEnt().getAnalog();
 		if(leftJoy > rightJoy) {} //good debug breakpoint
 		P.printFrmt(PrtYn.N, "drv: l-js=% 5.3f r-js=% 5.3f", leftJoy, rightJoy);
 		if(Math.abs(leftJoy)>0.2 || Math.abs(rightJoy)>0.2) {
 			P.printFrmt(PrtYn.N, "drv: l-js=% 5.3f r-js=% 5.3f", leftJoy, rightJoy);
 		}
 		TmSsDriveTrain.Driving.tankDriveJoysticksPercentOutput(leftJoy, rightJoy);
 	}
 }

 // Make this return true when this Command no longer needs to run execute()
 protected boolean isFinished() {
     if(m_ds.isEnabled() && m_ds.isOperatorControl()) {
    	 return false;
     } else {
    	 TmSsDriveTrain.DrvServices.stopAllMotors();
    	 return false;
     }
 }

 // Called once after isFinished returns true
 protected void end() {
	 P.println(Tt.getClassName(this) + " ending");
 }

 // Called when another command which requires one or more of the same
 // subsystems is scheduled to run
 protected void interrupted() {
	 P.println(Tt.getClassName(this) + " interrupted");

 }
}

