package org.usfirst.frc.tm744yr18.robot.commands;

import org.usfirst.frc.tm744yr18.robot.config.TmSdKeysI.SdKeysE;
import org.usfirst.frc.tm744yr18.robot.helpers.TmDriverStation;
import org.usfirst.frc.tm744yr18.robot.helpers.TmSdMgr;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmCGrpCallbackI;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmToolsI.P;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmToolsI.P.PrtYn;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmToolsI.Tt;
import org.usfirst.frc.tm744yr18.robot.subsystems.TmSsArm;
import org.usfirst.frc.tm744yr18.robot.subsystems.TmSsDriveTrain;
import org.usfirst.frc.tm744yr18.robot.subsystems.TmSsGrabber;
import org.usfirst.frc.tm744yr18.robot.subsystems.TmSsArm.LiftPosE;
import org.usfirst.frc.tm744yr18.robot.subsystems.TmSsDriveTrain.DrvGyro;
import org.usfirst.frc.tm744yr18.robot.subsystems.TmSsDriveTrain.DrvNavX;
import org.usfirst.frc.tm744yr18.robot.subsystems.TmSsGrabber.GrabberCubeClampAssistE;
import org.usfirst.frc.tm744yr18.robot.subsystems.TmSsGrabber.GrabberPositionAndWedgeReturnCodesE;
import org.usfirst.frc.tm744yr18.robot.subsystems.TmSsGrabber.GrabberMotionAndWedgeMgmt.GrabberMotionAndWedgeRequestsE;
import org.usfirst.frc.tm744yr18.t744utils.fileIo.TmTrajectoryFileIo;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.command.Command;
import t744opts.Tm744Opts;

/**
 *
 */
public class TmACmdFollowTrajectoryLeftOrRight extends Command {
	TmSsDriveTrain ssDrive;
	TmDriverStation m_tds;
	DriverStation m_ds;
	TmTrajectoryFileIo tWorker;
	TmTrajectoryFileIo tWorkerLeft;
	TmTrajectoryFileIo tWorkerRight;
	TmTrajectoryFileIo tWorkerNeither;
	boolean runAuto = true;
	int traj_index_left;
	int traj_index_right;
	Timer autoTime;
	String m_trajFnLeft;
	String m_trajFnRight;
	TrajDest m_destination;
	LiftPosE m_liftPosition;
	boolean m_wantToEjectCube;
	boolean armIsNotSet;
	boolean clampIsNotSet;
	boolean grabberIsNotSet;
	boolean clawWheelIsNotSet;
	
	boolean wantToRelease;
	boolean wantToStopAuto;
	boolean wantToMoveArm;
	boolean wantToEjectCube;
	GrabberMotionAndWedgeRequestsE m_grabberMotion;
	GrabberPositionAndWedgeReturnCodesE m_grabberRetCode;
	
	TmCGrpCallbackI m_callbackObj = null;
	boolean m_isOkToRun = true;

	double thetaIAccumulator;
	double prevTime;
	double prevThetaError;
	
	public static enum TrajDest { 
		SWITCH(0), SCALE(1)/*, BOTTOM(-1), NONE(-1)*/;
		public final int eCharIndex;
		private TrajDest(int charNdx) { eCharIndex = charNdx; }
	}
	public static enum ShowFile { Y, N }
	
	/**
	 * 
	 * @param destination	indicates which char in gameMessage should be examined (switch or scale) to 
	 * 							determine whether need to go to the left or right side
	 * @param liftPosition	indicates where the arm should be positioned if moved before end of trajectory
	 * 							or null to not move arm
	 * @param fileLeft		the trajectory file to use if need to go to left side of switch or scale
	 * @param fileRight		the trajectory file to use if need to go to right side of switch or scale
	 * @param moveGrabber	the grabber movement to request if moving grabber before end of trajectory
	 * 							or null to not move grabber
	 * @param showLeft		do/don't show contents of 'fileleft' on console
	 * @param showRight		do/don't show contents of 'fileleft' on console
	 */
    public TmACmdFollowTrajectoryLeftOrRight(TrajDest destination, LiftPosE liftPosition, 
    		String fileLeft, String fileRight, ShowFile showLeft, ShowFile showRight) {
    	this(null, destination, liftPosition, fileLeft, fileRight, null, showLeft, showRight, false);
    }
    public TmACmdFollowTrajectoryLeftOrRight(TmCGrpCallbackI callbackObj, 
    		TrajDest destination, LiftPosE liftPosition, 
    		String fileLeft, String fileRight, ShowFile showLeft, ShowFile showRight) {
    	this(callbackObj, destination, liftPosition, fileLeft, fileRight, null, showLeft, showRight, false);
    }
    
    public TmACmdFollowTrajectoryLeftOrRight(TrajDest destination, LiftPosE liftPosition,
    		String fileLeft, String fileRight, 
    		GrabberMotionAndWedgeRequestsE moveGrabber, ShowFile showLeft, ShowFile showRight) {
    	this(null, destination, liftPosition, fileLeft, fileRight, moveGrabber, showLeft, showRight, false);
    }
    public TmACmdFollowTrajectoryLeftOrRight(
    		TmCGrpCallbackI callbackObj, 
    		TrajDest destination, LiftPosE liftPosition,
        		String fileLeft, String fileRight, 
        		GrabberMotionAndWedgeRequestsE moveGrabber, ShowFile showLeft, ShowFile showRight, boolean wantToEjectCube) {
        // Use requires() here to declare subsystem dependencies
        // eg. requires(chassis);
    	m_tds = TmDriverStation.getInstance();
    	m_ds = DriverStation.getInstance();
     	ssDrive = TmSsDriveTrain.getInstance();
     	autoTime = new Timer();
     	requires(ssDrive);
     	
    	m_callbackObj = callbackObj;
     	m_destination = destination;
     	m_liftPosition = liftPosition;
     	m_trajFnLeft = fileLeft;
     	m_trajFnRight = fileRight;
     	m_grabberMotion = moveGrabber;
     	
     	
     	tWorker = null; //gameMessage may not yet be available
     	tWorkerLeft    = new TmTrajectoryFileIo(m_trajFnLeft);
     	tWorkerRight   = new TmTrajectoryFileIo(m_trajFnRight);
     	
     	tWorkerLeft.loadCsvData();
     	tWorkerRight.loadCsvData();
     	
     	armIsNotSet = true;
     	clampIsNotSet = true;
     	grabberIsNotSet = true;
     	clawWheelIsNotSet = false; //disable this feature: was true;
     	
     	wantToEjectCube = false; //disable this feature: was: = wantToEjectCubeArg;
     	
     	if(m_liftPosition.equals(LiftPosE.TOP)) {
     		wantToStopAuto = false;
         	wantToRelease = true;
         	wantToMoveArm = true;
     	}
     	else if(m_liftPosition.equals(LiftPosE.SWITCH)) {
         	//askDrivers: wantToRelease = false;
     		wantToStopAuto = false;
         	wantToRelease = true; //?? askDrivers
         	wantToMoveArm = true;
     	}
     	else if(m_liftPosition.equals(LiftPosE.USER)) {
     		wantToStopAuto = false;
     		wantToRelease = false;
     		wantToMoveArm = false;
     	}
     	else {
     		m_liftPosition = LiftPosE.BOTTOM;
     		wantToStopAuto = false;
     		wantToRelease = false; //as long as we have a cube, keep it for teleop
     		wantToMoveArm = false;
     	}
     	
     	
     	
//     	//may need to override parms set based on TrajDest
//     	if(m_liftPosition == null) {
//     		wantToMoveArm = false;
//     	}
     	
//     	if(m_destination == TrajDest.SCALE) {
////     		m_liftPosition = LiftPosE.TOP;
//     		wantToStopAuto = false;
//         	wantToRelease = true;
//         	wantToMoveArm = true;
//     	}
//     	else if(m_destination == TrajDest.SWITCH) {
////     		m_liftPosition = LiftPosE.SWITCH;
//         	//askDrivers: wantToRelease = false;
//     		wantToStopAuto = false;
//         	wantToRelease = true; //?? askDrivers
//         	wantToMoveArm = true;
//     	}
//     	else if(m_destination == TrajDest.BOTTOM) {
////     		m_liftPosition = LiftPosE.BOTTOM;
//     		wantToStopAuto = false;
//     		wantToRelease = true; //??
//     		wantToMoveArm = true;
//     	}
//     	else if(m_destination == TrajDest.NONE) {
////     		m_liftPosition = LiftPosE.USER; //will be ignored--arm won't be moved
//     		wantToStopAuto = false;
//     		wantToRelease = false;
//     		wantToMoveArm = false;
//     	}
//     	else {
////     		m_liftPosition = LiftPosE.BOTTOM;
//     		wantToStopAuto = true; //because we don't know where we're going
//     		wantToRelease = false;
//     		wantToMoveArm = false;
//     	}
     	

		if(showLeft.equals(ShowFile.Y)) { 
			TmTrajectoryFileIo tWorkerTemp = new TmTrajectoryFileIo(m_trajFnLeft);
			tWorkerTemp.loadCsvData();
			tWorkerTemp.showOnConsole(Tt.getClassName(this) + " -- " + m_destination.name() + " -- Left"); 
		}	
		if(showRight.equals(ShowFile.Y)) { 
			TmTrajectoryFileIo tWorkerTemp = new TmTrajectoryFileIo(m_trajFnRight);
			tWorkerTemp.loadCsvData();
			tWorkerTemp.showOnConsole(Tt.getClassName(this) + " -- " + m_destination.name() + " -- Right"); 
		}	
    }

    // Called just before this Command runs the first time
    protected void initialize() {
    	P.println(PrtYn.Y, "FYI: NuP2FPS=" + TmSsDriveTrain.DrvTrainCnst.NuP2FPS + 
    			" FPS_2_NuP100MS=" + TmSsDriveTrain.DrvTrainCnst.FPS_2_NuP100MS +
    			" NuP100MS_2_RPM=" + TmSsDriveTrain.DrvTrainCnst.NuP100MS_2_RPM);
    	m_isOkToRun = true;
    	runAuto = true;
    	if(m_callbackObj != null) {
    		m_isOkToRun = m_callbackObj.isOkToRunCmd();
    		runAuto = m_isOkToRun;
    	}

    	if(m_isOkToRun) {
    		//check if this is a switch or scale side-specific auto
    		//if so, skip or run accordingly
//    		runAuto = true;
    		tWorker = null;
    		String gameData = m_ds.getGameSpecificMessage();
    		armIsNotSet = true;
    		clampIsNotSet = true;
    		grabberIsNotSet = true;
    		m_grabberRetCode = null;
         	clawWheelIsNotSet = false; //disable this feature: was true;
         	
         	//wantToEjectCube = false; //disable this feature: was: = wantToEjectCubeArg;
         	
    		if(tWorker==null) {
    			//			boolean validCharIndex = m_destination.equals(TrajDest.SCALE) || m_destination.equals(TrajDest.SWITCH);
    			if(gameData.length() == 3) {
    				//				if(validCharIndex) {
    				if(gameData.charAt(m_destination.eCharIndex)=='L') {
    					if(m_trajFnLeft != null) {
    						tWorker = tWorkerLeft; //new TmTrajectoryFileIo(m_trajFnLeft);
    					}
    				} else {
    					if(m_trajFnRight != null) {
    						tWorker = tWorkerRight; //new TmTrajectoryFileIo(m_trajFnRight);
    					}
    				}
    				//				}
    				if(tWorker==null) { 
    					runAuto = false;
    				} 
    				//				else {
    				//					tWorker.loadCsvData();
    				//				}
    				if(false && m_destination.equals(TrajDest.SCALE) &&
    						gameData.charAt(m_destination.eCharIndex)=='R') {
    					wantToStopAuto = true;
    					armIsNotSet = false;
    					clampIsNotSet = false;
    					grabberIsNotSet = false;
    				}
    			}
    			else { 
    				//don't run auto if no gameData available
    				runAuto = false;
    			}
    		}
    		
    		

    		if(m_isOkToRun) {
    			//initialize things
    			traj_index_left  = 0;
    			traj_index_right = 0;
    			autoTime.reset();
    			autoTime.start();
    			ssDrive.resetEncoders();
    			if(Tm744Opts.isUseNoGyros()) {
    			} else {
    				if(Tm744Opts.isGyroNavX()) {
    					DrvNavX.resetNavXSoft();
    				} else {
    					DrvGyro.resetGyro(); //ssDrive.getAngle();
    				}  
    			}

    			thetaIAccumulator = 0;
    			prevTime = 0;
    			prevThetaError = 0;
    		}
    	}
    	if(tWorker != null) {
        	P.println(PrtYn.Y, tWorker.getFilename());   		
    	}
    	
    }
    
    // Called repeatedly when this Command is scheduled to run
    protected void execute() {
    	if(runAuto && m_isOkToRun) {
    		int listSize = tWorker.trajDataCsvL.size();

    		if(traj_index_left < listSize) {
    			while((traj_index_left < listSize) && 
    					tWorker.trajDataCsvL.get(traj_index_left).getLeftTime()<autoTime.get()) {
    				traj_index_left++;
    			}
    		}

    		if(traj_index_right < listSize) {
    			while((traj_index_right < listSize) && 
    					tWorker.trajDataCsvL.get(traj_index_right).getRightTime()<autoTime.get()) {
    				traj_index_right++;
    			}
    		}
    		
    		//Clamp control
    		if( wantToRelease && 
    				(tWorker.trajDataCsvL.get(listSize-1).getLeftTime() - autoTime.get()) < 2.3 &&
    				clampIsNotSet) {
    			TmSsGrabber.updateGrabberCubeClampAssist(GrabberCubeClampAssistE.UNCLAMPED);
    			clampIsNotSet = false; //clamp is set now
    		}
    		//Arm control
    		if( wantToMoveArm &&
    				(tWorker.trajDataCsvL.get(listSize-1).getLeftTime() - autoTime.get()) < 2 &&
    				armIsNotSet) {
    			setArmPosition();
    			armIsNotSet = false; //arm is set now
    		}
    		//Grabber control
    		if( (m_grabberMotion != null) && //want to move grabber
    				(tWorker.trajDataCsvL.get(listSize-1).getLeftTime() - autoTime.get()) < 1.5 &&
    				grabberIsNotSet) {
    			//retCode could be null (cmd not issued yet), BUSY, FAILED, UNSAFE, or OK
    			if( ! m_grabberRetCode.equals(GrabberPositionAndWedgeReturnCodesE.OK)) {
    				m_grabberRetCode = TmSsGrabber.updateGrabberPositionAndWedge(m_grabberMotion);
    				if(m_grabberRetCode.equals(GrabberPositionAndWedgeReturnCodesE.OK)) {
    					grabberIsNotSet = false;
    				}
    			} else {
    				grabberIsNotSet = false;
    			}
    		}
    		
    		//Arm Claw Control 
    		//(thought we wanted it, then decided we didn't so hardcoded wantToEjectCube and 
    		//	clawWheelIsNotSet to false so that the following block of code never executes)
    		if(wantToEjectCube && 
    				(tWorker.trajDataCsvL.get(listSize-1).getLeftTime() - autoTime.get()) < .3 &&
    				clawWheelIsNotSet) {
    			TmSsArm.ArmServices.requestStartClawReleasing();
    			clawWheelIsNotSet = false;
    		}
    		

    		//Nav and Control
    		if(traj_index_left < listSize && traj_index_right < listSize) {
    			double rightError = ssDrive.getPositionRightNu() 
    					- tWorker.trajDataCsvL.get(traj_index_right).getRightPos()*
    					(1.0/TmSsDriveTrain.DrvTrainCnst.DRV_ENCODER_COUNTS_2_FEET);
    			double leftError  = -1*ssDrive.getPositionLeftNu() 
    					- tWorker.trajDataCsvL.get(traj_index_left).getLeftPos()*
    					(1.0/TmSsDriveTrain.DrvTrainCnst.DRV_ENCODER_COUNTS_2_FEET);

    			double thetaError;
    			if(Tm744Opts.isUseNoGyros()) { //m_destination==TrajDest.SWITCH) {
    				thetaError = 0;
    			} else {
    				if(Tm744Opts.isGyroNavX()) {
    					thetaError = tWorker.trajDataCsvL.get(traj_index_left).getAngle() - DrvNavX.getGlobalAngle();
    				} else {
    					thetaError = tWorker.trajDataCsvL.get(traj_index_left).getAngle() - DrvGyro.getAngle();
    				}  
    			}
        		TmSdMgr.putNumber(SdKeysE.KEY_DRIVE_THETA_ERROR_DEGREES, thetaError);
        		TmSdMgr.putNumber(SdKeysE.KEY_DRIVE_THETA_DESIRED, tWorker.trajDataCsvL.get(traj_index_left).getAngle());

    			TmSdMgr.putNumber(SdKeysE.KEY_DRIVE_POS_ERR_RIGHT_FEET,
    					rightError*TmSsDriveTrain.DrvTrainCnst.DRV_ENCODER_COUNTS_2_FEET);
    			TmSdMgr.putNumber(SdKeysE.KEY_DRIVE_POS_ERR_LEFT_FEET,
    					leftError*TmSsDriveTrain.DrvTrainCnst.DRV_ENCODER_COUNTS_2_FEET);
    			
    			//somewhat decouple the position controller and the angle controller
    			//basically, the position controller only cares about average error, not the individual error
    			//no idea if this will help, hurt or do nothing, but it's worth a try before adding in the I/D-gain
    			if(false && m_destination!=TrajDest.SWITCH) {
    				double avgPosError = (rightError+leftError)/2.0;
    				leftError = avgPosError;
    				rightError = avgPosError;
    			}
    			
    			//I and D term for angle controller
    			double DRV_ANGLE_I_GAIN = 0; //set to 0 if we skip using I-gain, DON'T TUNE HERE
    			double DRV_ANGLE_D_GAIN = 0; //set to 0 if we skip using D-gain, DON'T TUNE HERE
    			double curTime = autoTime.get();
    			double dThetaError = 0; //set to 0 if we skip using D-gain
				if(false && m_destination!=TrajDest.SWITCH) {
    				double iAccRange = 10;
    				double dt = curTime-prevTime;
    				if(thetaError >= iAccRange) {
//    					thetaIAccumulator += iAccRange*dt; //taking this out bc I'm very afraid of instability
    				}
    				else if(thetaError <= -iAccRange) {
//    					thetaIAccumulator += -iAccRange*dt; //taking this out bc I'm very afraid of instability
    				}
    				else {
    					thetaIAccumulator += thetaError*dt;
    				}
    				
    				//clear accumulator when on track - did I mention I'm afraid of instability
    				double iAccClearRange = 1; //degree
    				if(-iAccClearRange <= thetaError && thetaError <= iAccClearRange) {
    					thetaIAccumulator = 0;
    				}
    				
    				
    				
    				//TUNE I and D Gain here!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    				//I have some 'rule of thumb' first guesses in the comments
    				//Only turn on 1 at a time
    				DRV_ANGLE_I_GAIN = 0; //TmSsDriveTrain.DrvTrainCnst.DRV_ANGLE_P_GAIN/100;
    				
    				DRV_ANGLE_D_GAIN = 0; //TmSsDriveTrain.DrvTrainCnst.DRV_ANGLE_P_GAIN*5;
    				dThetaError = (thetaError-prevThetaError)/dt;
    				
    				prevThetaError = thetaError;
    				prevTime = curTime;
    			}

    			double rightVelInput = tWorker.trajDataCsvL.get(traj_index_right).getRightVel() + 
    					TmSsDriveTrain.DrvTrainCnst.DRV_POS_P_GAIN * rightError + .18 * 
    					tWorker.trajDataCsvL.get(traj_index_right).getRightAcc() + 
    										   TmSsDriveTrain.DrvTrainCnst.DRV_ANGLE_P_GAIN  * thetaError +
    										   							   DRV_ANGLE_I_GAIN  * thetaIAccumulator +
    										   							   DRV_ANGLE_D_GAIN  * dThetaError;
    			double leftVelInput = tWorker.trajDataCsvL.get(traj_index_left).getLeftVel() + 
    					TmSsDriveTrain.DrvTrainCnst.DRV_POS_P_GAIN * leftError + .18 * 
    					tWorker.trajDataCsvL.get(traj_index_left).getLeftAcc() - //sign is intentionally different from above 
    							   			   TmSsDriveTrain.DrvTrainCnst.DRV_ANGLE_P_GAIN  * thetaError -
    							   			   							   DRV_ANGLE_I_GAIN  * thetaIAccumulator -
    							   			   							   DRV_ANGLE_D_GAIN  * dThetaError;
    			P.println(PrtYn.Y, "Left=" + leftVelInput + " Right=" + rightVelInput + " " + tWorker.getFilename());
    			TmSsDriveTrain.Driving.tankDriveRawVelocityFPS(leftVelInput, rightVelInput);
    		}
    	}
    }

    // Make this return true when this Command no longer needs to run execute()
    protected boolean isFinished() {
    	boolean ans = false;
    	if(m_isOkToRun) {
     		if( m_tds.isEnabledAutonomous() 
    				&& runAuto 
    				&& 
    				(traj_index_left < tWorker.trajDataCsvL.size() 
    						|| traj_index_right < tWorker.trajDataCsvL.size())
    				//askDrivers || m_liftPosition.equals(LiftPosE.SWITCH)
    				) {
    			ans = false;
    		} else {
    			runAuto = false;
    			ans = true;
    			ssDrive.stopAutonTrajectoryTest();
    			TmSsDriveTrain.Driving.tankDriveRawPercentOutput(0.0, 0.0);
    		}
    		if( wantToStopAuto) {
    			ans = false; //don't allow cmd group to move on to 'release' step or anything else
    		}
    	} else {
    		ans = true;
    	}
     	return ans;
    }

    // Called once after isFinished returns true
    protected void end() {
		P.println(PrtYn.N, "NuP2FPS=" + TmSsDriveTrain.DrvTrainCnst.NuP2FPS + 
				" FPS_2_NuP100MS=" + TmSsDriveTrain.DrvTrainCnst.FPS_2_NuP100MS +
				" NuP100MS_2_RPM=" + TmSsDriveTrain.DrvTrainCnst.NuP100MS_2_RPM);

    	TmSsDriveTrain.Driving.tankDriveJoysticksPercentOutput(0, 0);
    }

    // Called when another command which requires one or more of the same
    // subsystems is scheduled to run
    protected void interrupted() {
 		ssDrive.stopAutonTrajectoryTest();
    	TmSsDriveTrain.Driving.tankDriveJoysticksPercentOutput(0, 0);
    }
    
    private void setArmPosition() {
    	int stage1Pos;
//    	P.println(PrtYn.Y, Tt.getClassName(this) + " setting position to " + m_liftPosition.name());
		switch(m_liftPosition) {
		case BOTTOM: //POV-W???
		case SWITCH: //POV-E???
		case SCALE_MID: //btn X???
		case TOP: //btn Y???
		case SCALE_LOW: //btn A??
			if (m_liftPosition != null){
			stage1Pos = TmSsArm.ArmServices.getFixedEncoderPosition(m_liftPosition);
	    	P.println(PrtYn.Y, Tt.getClassName(this) + " setting position to " + 
	    			m_liftPosition.name() + " (" + stage1Pos + " ticks)");
    		TmSsArm.ArmServices.requestBothStagesServo(stage1Pos, 0);
			}
			break;
		case SCALE_HIGH:
		case MAX_HEIGHT:
			P.println(PrtYn.Y, "ignored - FYI, no DsNamedControlsE entries for fixed encoder position " + m_liftPosition.name());
			break;
		case USER:
			P.println(PrtYn.Y, "ignored - requested position: " + m_liftPosition.name());
		//default:
			break;
		}
    }
}
