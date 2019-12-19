package org.usfirst.frc.tm744yr18.robot.commands;

import edu.wpi.first.wpilibj.command.Command;

/**
 *
 */
public class TmCCmdDelay extends Command {
    public TmCCmdDelay(double seconds) {
        // Use requires() here to declare subsystem dependencies
        // eg. requires(chassis);
    	setTimeout(seconds); //scheduler will start the timeout just before calling initialize()
    }

    // Called just before this Command runs the first time
    protected void initialize() {
    }

    // Called repeatedly when this Command is scheduled to run
    protected void execute() {
    }

    // Make this return true when this Command no longer needs to run execute()
    protected boolean isFinished() {
        return isTimedOut();
    }

    // Called once after isFinished returns true
    protected void end() {
    }

    // Called when another command which requires one or more of the same
    // subsystems is scheduled to run
    protected void interrupted() {
    }
}
