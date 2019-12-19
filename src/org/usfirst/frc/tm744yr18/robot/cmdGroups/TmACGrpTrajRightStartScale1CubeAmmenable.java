package org.usfirst.frc.tm744yr18.robot.cmdGroups;

import org.usfirst.frc.tm744yr18.robot.commands.TmACmdDriveStraight;
import org.usfirst.frc.tm744yr18.robot.commands.TmACmdFollowTrajectoryLeftOrRight;
import org.usfirst.frc.tm744yr18.robot.commands.TmCCmdArmRunLiftWithEncoderPositions;
import org.usfirst.frc.tm744yr18.robot.commands.TmCCmdDelay;
import org.usfirst.frc.tm744yr18.robot.commands.TmCCmdGrabberClampAssistOpenOrClose;
import org.usfirst.frc.tm744yr18.robot.commands.TmCCmdGrabberSetPositionAndWedge;
import org.usfirst.frc.tm744yr18.robot.commands.TmACmdFollowTrajectoryLeftOrRight.ShowFile;
import org.usfirst.frc.tm744yr18.robot.commands.TmACmdFollowTrajectoryLeftOrRight.TrajDest;
import org.usfirst.frc.tm744yr18.robot.commands.TmCCmdArmClawStartGrabbing;
import org.usfirst.frc.tm744yr18.robot.subsystems.TmSsArm;
import org.usfirst.frc.tm744yr18.robot.subsystems.TmSsArm.LiftPosE;
import org.usfirst.frc.tm744yr18.robot.subsystems.TmSsGrabber.GrabberCubeClampAssistE;
import org.usfirst.frc.tm744yr18.robot.subsystems.TmSsGrabber.GrabberMotionAndWedgeMgmt.GrabberMotionAndWedgeRequestsE;

import edu.wpi.first.wpilibj.command.CommandGroup;

/**
 *
 */
public class TmACGrpTrajRightStartScale1CubeAmmenable extends CommandGroup {

    public TmACGrpTrajRightStartScale1CubeAmmenable() {
    	//We have time, and we might be driving up onto a platform, so drive near the scale first
    	//Then raise the arm
    	//Then drive forward
    	//Then eject the cube
    	addSequential (new TmCCmdDelay(3.0));
    	addSequential(new TmACmdFollowTrajectoryLeftOrRight(TrajDest.SCALE, LiftPosE.USER, //USER will be ignored - no arm movement
    			"trajRightToLeftScale_ammenable.csv", "trajRightToRightScale_ammenable.csv", ShowFile.N, ShowFile.N));

    	//Raise the arm
    	addSequential (new TmCCmdGrabberClampAssistOpenOrClose(GrabberCubeClampAssistE.UNCLAMPED));
    	addSequential (new TmCCmdArmClawStartGrabbing()); //hold cube in tighter????
		addSequential (new TmCCmdDelay(0.30)); //was: (new TmCCmdDelay(0.050));
		addSequential (new TmCCmdArmRunLiftWithEncoderPositions(TmSsArm.LiftPosE.TOP));
		addSequential (new TmCCmdDelay(1.750)); //was: (new TmCCmdDelay(0.050)); //TmACmdDoNothingForXSeconds(0.050));

		//Drive Forward
		addSequential (new TmACmdDriveStraight(1.0, 0.2)); //time, speed
		addSequential (new TmCCmdDelay(0.5));
		
		//Eject
    	addSequential (new TmACGrpReleaseCube());
    	
    	//Prepare for teleop
    	addSequential (new TmACmdDriveStraight(0.5, -0.5)); //time, speed
		addSequential (new TmCCmdDelay(0.5));
		addSequential (new TmCCmdGrabberSetPositionAndWedge(GrabberMotionAndWedgeRequestsE.REQ_GRABBER_DOWN_WEDGE_RETRACT));
		addSequential (new TmCCmdArmRunLiftWithEncoderPositions(TmSsArm.LiftPosE.BOTTOM));
    	
    }
}
