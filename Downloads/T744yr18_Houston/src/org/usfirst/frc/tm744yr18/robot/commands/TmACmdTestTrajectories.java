package org.usfirst.frc.tm744yr18.robot.commands;

import org.usfirst.frc.tm744yr18.robot.helpers.TmDriverStation;
import org.usfirst.frc.tm744yr18.robot.helpers.TmTrajGenerator;
import org.usfirst.frc.tm744yr18.robot.subsystems.TmSsDriveTrain;
import org.usfirst.frc.tm744yr18.t744utils.fileIo.TmFileIoOpsysAndPaths;
import org.usfirst.frc.tm744yr18.t744utils.fileIo.TmTrajectoryFileIo;
import org.usfirst.frc.tm744yr18.t744utils.fileIo.TmTrajectoryFileIo.TrajDataCsv;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.command.Command;
import jaci.pathfinder.Pathfinder;
import jaci.pathfinder.Waypoint;

/**
* naming convention: TmACmd prefix indicates an autonomous command
* 					  TmCCmd prefix indicates a common (to auton/teleop) command
* 					  TmTCmd prefix indicates a telelop command
*/
public class TmACmdTestTrajectories extends Command {

	TmSsDriveTrain ssDrive;
	TmDriverStation m_tds;
	TmTrajGenerator m_autoTraj;
	TrajectoryTestsE m_trajTest;
	
	public static enum TrajectoryTestsE {
		kCustomAuto, kOpenloopStraightAuto, kClosedloopStraightAuto, kSwitchTrajAuto; //, kShowTrajectoryFile;
	}

 public TmACmdTestTrajectories(TrajectoryTestsE trajTest) {
 	m_tds = TmDriverStation.getInstance();
 	ssDrive = TmSsDriveTrain.getInstance();
 	requires(ssDrive);
 	m_trajTest = trajTest;
 }

 // Called just before this Command runs the first time
 protected void initialize() {
	 ssDrive.startAutonTrajectoryTest(m_trajTest);
 }

 // Called repeatedly when this Command is scheduled to run
 protected void execute() {
 	if( m_tds.isEnabledAutonomous() ) {
// 		TmSsDriveTrain.Services.stopAllMotors();
 	}
 }

 // Make this return true when this Command no longer needs to run execute()
 protected boolean isFinished() {
 	boolean ans = false;
 	if( m_tds.isEnabledAutonomous() ) {
 		ans = false;
 	} else {
 		ans = true;
 		ssDrive.stopAutonTrajectoryTest();
 	}
 	return ans;
 }

 // Called once after isFinished returns true
 protected void end() {
 	if( m_tds.isEnabledAutonomous() ) {
 		
 	}
 }

 // Called when another command which requires one or more of the same
 // subsystems is scheduled to run
 protected void interrupted() {
 	if( m_tds.isEnabledAutonomous() ) {
 		
 	}
 }
}
