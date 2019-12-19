package org.usfirst.frc.tm744yr18.robot.cmdGroups;

import org.usfirst.frc.tm744yr18.robot.commands.TmCCmdArmClawStartReleasing;
import org.usfirst.frc.tm744yr18.robot.commands.TmCCmdArmClawStopMotors;
import org.usfirst.frc.tm744yr18.robot.commands.TmCCmdDelay;

import edu.wpi.first.wpilibj.command.CommandGroup;

/**
 *
 */
public class TmACGrpReleaseCube extends CommandGroup {

    public TmACGrpReleaseCube() {
    	addSequential (new TmCCmdArmClawStartReleasing());
		addSequential (new TmCCmdDelay(0.4)); //TmACmdDoNothingForXSeconds(2));
		addSequential (new TmCCmdArmClawStopMotors());
    }
}
