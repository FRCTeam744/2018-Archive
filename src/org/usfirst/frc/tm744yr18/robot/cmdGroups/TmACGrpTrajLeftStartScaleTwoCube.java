package org.usfirst.frc.tm744yr18.robot.cmdGroups;

import org.usfirst.frc.tm744yr18.robot.commands.TmCCmdDelay;
import org.usfirst.frc.tm744yr18.robot.commands.TmACmdDriveStraight;
import org.usfirst.frc.tm744yr18.robot.commands.TmACmdFollowTrajectoryLeftOrRight;
import org.usfirst.frc.tm744yr18.robot.commands.TmACmdFollowTrajectoryLeftOrRight.ShowFile;
import org.usfirst.frc.tm744yr18.robot.commands.TmACmdFollowTrajectoryLeftOrRight.TrajDest;
import org.usfirst.frc.tm744yr18.robot.commands.TmCCmdArmClawStartGrabbing;
import org.usfirst.frc.tm744yr18.robot.commands.TmCCmdArmRunLiftWithEncoderPositions;
import org.usfirst.frc.tm744yr18.robot.commands.TmCCmdGrabberClampAssistOpenOrClose;
import org.usfirst.frc.tm744yr18.robot.commands.TmCCmdGrabberSetMotorState;
import org.usfirst.frc.tm744yr18.robot.commands.TmCCmdGrabberSetPositionAndWedge;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmCGrpCallbackI;
import org.usfirst.frc.tm744yr18.robot.subsystems.TmSsArm;
import org.usfirst.frc.tm744yr18.robot.subsystems.TmSsArm.LiftPosE;
import org.usfirst.frc.tm744yr18.robot.subsystems.TmSsAutonomous.AutonAlgStatusE;
import org.usfirst.frc.tm744yr18.robot.subsystems.TmSsGrabber.GrabberCubeClampAssistE;
import org.usfirst.frc.tm744yr18.robot.subsystems.TmSsGrabber.GrabberMotionAndWedgeMgmt.GrabberMotionAndWedgeRequestsE;
import org.usfirst.frc.tm744yr18.robot.subsystems.TmSsGrabber.GrabberMotorStateE;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.command.CommandGroup;

/**
 *
 */
public class TmACGrpTrajLeftStartScaleTwoCube extends CommandGroup 
								implements TmCGrpCallbackI {

	public static enum TrajLeftStartScaleTwoCubeOptionsE {
		DRIVE_ONLY,
		RIGHT_SCALE_FIRST_CUBE_AND_BACK_ONLY,
		DO_EVERYTHING_ALWAYS
	}
	TrajLeftStartScaleTwoCubeOptionsE m_options;
	
	public static final double DELAY_FOR_TESTING = 0.02; //3.0; //Use me for testing! But delete me or set me to 0.05 later
    public TmACGrpTrajLeftStartScaleTwoCube() {
    	this(TrajLeftStartScaleTwoCubeOptionsE.DO_EVERYTHING_ALWAYS); //do everything, don't just drive
    }
    public TmACGrpTrajLeftStartScaleTwoCube(TrajLeftStartScaleTwoCubeOptionsE options) { // driveOnly) {
    	boolean driveOnly = options.equals(TrajLeftStartScaleTwoCubeOptionsE.DRIVE_ONLY);
    	boolean rightScale1CubeAndBack = false; //options.equals(TrajLeftStartScaleTwoCubeOptionsE.RIGHT_SCALE_FIRST_CUBE_AND_BACK_ONLY);
    	m_options = options;
    	
//    	boolean drvOnlyOrRtScl1CBack = driveOnly || rightScale1CubeAndBack;
    	//Cube 1
    	addSequential(new TmACmdFollowTrajectoryLeftOrRight(TrajDest.SCALE, getLiftPosToUse(driveOnly, LiftPosE.TOP),
    			"trajLeftToLeftScale.csv", "trajLeftToRightScale.csv", ShowFile.N, ShowFile.N));
    	//note: x=a?b:c; means if(a){x=b;}else{x=c;}
    	addSequential (driveOnly ? (new TmCCmdDelay(0.01)) : (new TmACGrpReleaseCube()) );
    	
//    	//right-side scale: back up a little -- parms are time, motor speed
////    	addSequential(isRightScale1CubeAndBack(options) ? (new TmACmdDriveStraight(1, -0.5)) : (new TmCCmdDelay(0.01)) );
//    	addSequential(new TmACmdDriveStraight(this, 1, -0.5));
////		new TmACmdDriveStraight(4, 0.5), AutonAlgStatusE.kCoded),
    	
    	addSequential(new TmCCmdDelay(DELAY_FOR_TESTING)); //Use me for testing! But delete me or set me to 0.05 later
    	
    	//Cube 2 - acquire
    	
    	//maybe do this instead of the addParallel & addSequential below???
    // 	addSequential(new TmACmdFollowTrajectoryLeftOrRight(TrajDest.SCALE, LiftPosE.BOTTOM, 
	//				"trajLeftScaleAcquireCube2Part1.csv", "trajRightScaleAcquireCube2Part1.csv", 
    //				GrabberMotionAndWedgeRequestsE.REQ_GRABBER_DOWN_WEDGE_RETRACT, ShowFile.N, ShowFile.N));
//    	addParallel( (driveOnly || rightScale1CubeAndBack) ? (new TmCCmdDelay(0.01))  : 
    	addParallel( (driveOnly ) ? (new TmCCmdDelay(0.01))  : 
    		(new TmCCmdGrabberSetPositionAndWedge(GrabberMotionAndWedgeRequestsE.REQ_GRABBER_DOWN_WEDGE_RETRACT)) );
    	addSequential(new TmACmdFollowTrajectoryLeftOrRight(this, TrajDest.SCALE, getLiftPosToUse(driveOnly, LiftPosE.BOTTOM), 
    			"trajLeftScaleAcquireCube2Part1.csv", "trajRightScaleAcquireCube2Part1.csv", ShowFile.N, ShowFile.N) );
    	addSequential(new TmCCmdArmRunLiftWithEncoderPositions(TmSsArm.LiftPosE.BOTTOM));
    	
    	addSequential(new TmCCmdDelay(DELAY_FOR_TESTING)); //Use me for testing! But delete me or set me to 0.05 later
    	
    	addParallel((driveOnly || rightScale1CubeAndBack) ? (new TmCCmdDelay(0.01))  : 
    		(new TmCCmdGrabberSetMotorState(GrabberMotorStateE.GRABBING)) );
    	addSequential(new TmACmdFollowTrajectoryLeftOrRight(this, TrajDest.SCALE, getLiftPosToUse(driveOnly, LiftPosE.BOTTOM), 
    			"trajLeftScaleAcquireCube2Part2.csv", "trajRightScaleAcquireCube2Part2.csv", ShowFile.N, ShowFile.N) );

    	addSequential(new TmCCmdDelay(DELAY_FOR_TESTING)); //Use me for testing! But delete me or set me to 0.05 later
    	
//    	addSequential((driveOnly || rightScale1CubeAndBack) ? (new TmCCmdDelay(0.01))  : 
//    		(new TmCCmdGrabberClampAssistOpenOrClose(GrabberCubeClampAssistE.CLAMPED)) );
//    	addSequential(new TmCCmdDelay(0.2));
//    	addSequential((driveOnly || rightScale1CubeAndBack) ? (new TmCCmdDelay(0.01))  : 
//    		(new TmCCmdGrabberClampAssistOpenOrClose(GrabberCubeClampAssistE.UNCLAMPED)) );
//    	addSequential(new TmCCmdDelay(0.2));
    	addSequential((driveOnly || rightScale1CubeAndBack) ? (new TmCCmdDelay(0.01))  : 
    		(new TmCCmdGrabberClampAssistOpenOrClose(GrabberCubeClampAssistE.CLAMPED)) );
    	addSequential(new TmCCmdDelay(0.5));
    	
    	addParallel((driveOnly || rightScale1CubeAndBack) ? (new TmCCmdDelay(0.01))  : 
    		(new TmCCmdGrabberSetMotorState(GrabberMotorStateE.OFF)) );
    	

    	addSequential(new TmCCmdDelay(DELAY_FOR_TESTING)); //Use me for testing! But delete me or set me to 0.05 later
    	
    	//Cube 2 - reverse back to Scale
    	addParallel((driveOnly || rightScale1CubeAndBack) ? (new TmCCmdDelay(0.01))  : 
    		(new TmCCmdGrabberSetPositionAndWedge(GrabberMotionAndWedgeRequestsE.REQ_GRABBER_UP_NO_WEDGE)) );
    	addParallel((driveOnly || rightScale1CubeAndBack) ? (new TmCCmdDelay(0.01))  :  (new TmCCmdArmClawStartGrabbing()) );
    	addSequential(new TmACmdFollowTrajectoryLeftOrRight(this, TrajDest.SCALE, getLiftPosToUse(driveOnly, LiftPosE.USER), //USER will be ignored - no arm movement
    			"trajLeftScaleBackToScaleFromCube2_part1.csv", "trajRightScaleBackToScaleFromCube2_part1.csv",
    			ShowFile.N, ShowFile.N) );
    	
    	addSequential(new TmCCmdDelay(DELAY_FOR_TESTING)); //Use me for testing! But delete me or set me to a much lower delay later
    	
    	addSequential((driveOnly || rightScale1CubeAndBack) ? (new TmCCmdDelay(0.01))  : 
    		(new TmCCmdGrabberClampAssistOpenOrClose(GrabberCubeClampAssistE.UNCLAMPED)) );
    	addSequential(new TmCCmdDelay(0.1)); //This might need ever so small a head start if the following trajectory is less than 2 seconds long
//    	addSequential((driveOnly || rightScale1CubeAndBack) ? (new TmCCmdDelay(0.01))  : 
//    		(new TmCCmdArmRunLiftWithEncoderPositions(TmSsArm.LiftPosE.TOP)) );
//    	addSequential (new TmCCmdDelay(0.5));
    	
    	//-------
//    	addSequential(new TmACmdFollowTrajectoryLeftOrRight(this, TrajDest.SCALE, getLiftPosToUse(driveOnly, LiftPosE.TOP), 
//    			"trajLeftScaleBackToScaleFromCube2_part2.csv", "trajRightScaleBackToScaleFromCube2_part2.csv", 
//    			ShowFile.N, ShowFile.N) );
    	addSequential(new TmACmdFollowTrajectoryLeftOrRight(this, TrajDest.SCALE, getLiftPosToUse(driveOnly, LiftPosE.TOP), 
    			"trajLeftScaleBackToScaleFromCube2_part2.csv", "trajRightScaleBackToScaleFromCube2_part2.csv", 
    			null, ShowFile.N, ShowFile.N, false) ); //true=wantToEjectCube
//        public TmACmdFollowTrajectoryLeftOrRight(
//        		TmCGrpCallbackI callbackObj, 
//        		TrajDest destination, LiftPosE liftPosition,
//            		String fileLeft, String fileRight, 
//            		GrabberMotionAndWedgeRequestsE moveGrabber, ShowFile showLeft, ShowFile showRight, boolean wantToEjectCube) {
    	//-------
    	
    	//Release cube and prepare for teleop
    	//addParallel((driveOnly || rightScale1CubeAndBack) ? (new TmCCmdDelay(0.01))  : 
    		//(new TmCCmdGrabberSetPositionAndWedge(GrabberMotionAndWedgeRequestsE.REQ_GRABBER_DOWN_WEDGE_RETRACT)) );
    	addSequential ((driveOnly || rightScale1CubeAndBack) ? (new TmCCmdDelay(0.01)) : (new TmACGrpReleaseCube()) );
    	
    	addSequential (new TmCCmdDelay(DELAY_FOR_TESTING));
    	
    	addSequential(new TmACmdDriveStraight(1, -0.35)); //parms: time, speed
    	addSequential((driveOnly || rightScale1CubeAndBack) ? (new TmCCmdDelay(0.01))  : 
    		(new TmCCmdArmRunLiftWithEncoderPositions(TmSsArm.LiftPosE.BOTTOM)) );
    }
    
    private LiftPosE getLiftPosToUse(boolean driveOnly, LiftPosE defaultPos) {
    	LiftPosE ans = defaultPos;
    	if(driveOnly) { ans = LiftPosE.USER; } //USER gets ignored -- arm doesn't move
    	return ans;
    }
    
//    isRightScale1CubeAndBack()
//    private boolean isRightScale1CubeAndBack() { //TrajLeftStartScaleTwoCubeOptionsE opts) {
//    	boolean ans;
//    	boolean rightScale1CubeAndBack = m_options.equals(TrajLeftStartScaleTwoCubeOptionsE.RIGHT_SCALE_FIRST_CUBE_AND_BACK_ONLY);
//		String gameData = DriverStation.getInstance().getGameSpecificMessage();
//		boolean isRightScale = ('R' == gameData.charAt(TrajDest.SCALE.eCharIndex));
//		ans = rightScale1CubeAndBack && isRightScale;
//    	return ans;
//    }
	@Override
	public boolean isOkToRunCmd() {
    	boolean ans;
    	boolean rightScale1CubeAndBack = false; //m_options.equals(TrajLeftStartScaleTwoCubeOptionsE.RIGHT_SCALE_FIRST_CUBE_AND_BACK_ONLY);
		String gameData = DriverStation.getInstance().getGameSpecificMessage();
		boolean isRightScale = ('R' == gameData.charAt(TrajDest.SCALE.eCharIndex));
		ans = rightScale1CubeAndBack && isRightScale;
    	return true;
	}
}
