package org.usfirst.frc.tm744yr18.robot.cmdGroups;

import org.usfirst.frc.tm744yr18.robot.commands.TmCCmdArmClawStartGrabbing;
import org.usfirst.frc.tm744yr18.robot.commands.TmCCmdArmClawStopMotors;
import org.usfirst.frc.tm744yr18.robot.commands.TmCCmdDelay;

import edu.wpi.first.wpilibj.command.CommandGroup;

public class TmCCGrpArmRunLiftWheelsForGrabberUp extends CommandGroup {
	public TmCCGrpArmRunLiftWheelsForGrabberUp(){
		this(2.0); //default time
	}
	public TmCCGrpArmRunLiftWheelsForGrabberUp(double secondsToRunWheels){
		
		//when bringing grabber up, want to run the arm claw (a.k.a. lift wheels)
		//to ensure cube is firmly held

		//runs in parallel with the command(s) that follow (list ends with addSequential)
		addSequential(new TmCCmdArmClawStartGrabbing());
		addSequential(new TmCCmdDelay(secondsToRunWheels)); //TmACmdDoNothingForXSeconds(secondsToRunWheels));
		addSequential(new TmCCmdArmClawStopMotors());
	}
}
