package org.usfirst.frc.tm744yr18.robot.commands;

import org.usfirst.frc.tm744yr18.robot.config.TmSdKeysI.SdKeysE;
import org.usfirst.frc.tm744yr18.robot.helpers.TmDriverStation;
import org.usfirst.frc.tm744yr18.robot.helpers.TmSdMgr;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmToolsI.P;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmToolsI.P.PrtYn;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmToolsI.Tt;
import org.usfirst.frc.tm744yr18.robot.subsystems.TmSsArm.LiftPosE;
import org.usfirst.frc.tm744yr18.robot.subsystems.TmSsDriveTrain.DrvGyro;
import org.usfirst.frc.tm744yr18.robot.subsystems.TmSsDriveTrain.DrvNavX;
import org.usfirst.frc.tm744yr18.robot.subsystems.TmSsGrabber.GrabberCubeClampAssistE;
import org.usfirst.frc.tm744yr18.robot.subsystems.TmSsArm;
import org.usfirst.frc.tm744yr18.robot.subsystems.TmSsDriveTrain;
import org.usfirst.frc.tm744yr18.robot.subsystems.TmSsGrabber;
import org.usfirst.frc.tm744yr18.t744utils.fileIo.TmTrajectoryFileIo;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.command.Command;
import t744opts.Tm744Opts;

/**
 *
 */
public class TmACmdFollowTrajectorySwitchOrScale extends Command {
	TmSsDriveTrain ssDrive;
	TmDriverStation m_tds;
	DriverStation m_ds;
	TmTrajectoryFileIo tWorker;
	TmTrajectoryFileIo tWorkerScale;
	TmTrajectoryFileIo tWorkerSwitch;
	TmTrajectoryFileIo tWorkerNeither;
	boolean runAuto = true;
	int traj_index_left;
	int traj_index_right;
	Timer autoTime;
	String m_fileSwitch;
	String m_fileScale;
	String m_fileStraight;
	char m_destLeftOrRight;
	LiftPosE m_liftPosition;
	boolean armIsNotSet;
	boolean clampIsNotSet;
	boolean wantToRelease;
	
//	char m_startLeftRightOrCenter; //only needed when starting from center....

	
	public static enum TrajDest { 
		SWITCH(0), SCALE(1);
		public final int eCharIndex;
		private TrajDest(int charNdx) { eCharIndex = charNdx; }
	}
	public static enum ShowFile { Y, N }
	
	/* Will follow the trajectory found in filename. Given that the correct switch and scale is found.
	 * filename - name of file with trajectory data
	 * switchSide - char. Should be 'L' if you only run trajectory when you own the left side switch
	 * 					  Should be 'R' if you only run trajectory when you own the right side switch
	 * 						   Can be anything else ('*' is good) if you run the trajectory regardless of switch side.
	 * scaleSide - char. Should be 'L' if you only run trajectory when you own the left side scale
	 * 					 Should be 'R' if you only run trajectory when you own the right side scale
	 * 				     Can be anything else ('*' is good) if you run the trajectory regardless of scale side.
	 * 
	 */
   
//	public static final boolean NO_SHOW_FILE = false;
//	public static final boolean SHOW_FILE = true;
	/**
	 * If the scale is on the side indicated by destLeftOrRight, use the 'fileScale' trajectory file.
	 * Else if the switch is on the side indicated by destLeftOrRight, use the 'fileSwitch' trajectory file.
	 * Else use the fileStraight trajectory file.
	 * @param destLeftOrRight should be 'L' for left or 'R' for right. anything else will be ignored
	 * @param fileSwitch
	 * @param fileScale
	 * @param fileStraight
	 */
    public TmACmdFollowTrajectorySwitchOrScale(char destLeftOrRight, String fileSwitch, String fileScale, String fileStraight){
//    	this(destLeftOrRight, destLeftOrRight, fileSwitch, fileScale, fileStraight);
//    }
//    public TmACmdFollowTrajectorySwitchOrScale(char startLeftRightOrCenter, char destLeftOrRight, String fileSwitch, String fileScale, String fileStraight){
        // Use requires() here to declare subsystem dependencies
        // eg. requires(chassis);
    	m_tds = TmDriverStation.getInstance();
    	m_ds = DriverStation.getInstance();
     	ssDrive = TmSsDriveTrain.getInstance();
     	autoTime = new Timer();
     	requires(ssDrive);
     	
     	m_fileSwitch = fileSwitch;
     	m_fileScale = fileScale;
     	m_fileStraight = fileStraight;
     	m_destLeftOrRight = destLeftOrRight;
//    	m_startLeftRightOrCenter = startLeftRightOrCenter; //only needed when starting from center....
     	
     	tWorker = null; //gameMessage may not yet be available
     	//go ahead and load these now so don't have to do it during auton
     	tWorkerScale   = new TmTrajectoryFileIo(m_fileScale);
     	tWorkerSwitch  = new TmTrajectoryFileIo(m_fileSwitch);
     	tWorkerNeither = new TmTrajectoryFileIo(m_fileStraight);
     	
     	tWorkerScale.loadCsvData();
     	tWorkerSwitch.loadCsvData();
     	tWorkerNeither.loadCsvData();
     	    	
     	armIsNotSet = true;
     	clampIsNotSet = true;
//     	wantToRelease = ?; //must wait and do in initialize()
    }

    // Called just before this Command runs the first time
    protected void initialize() {
		
		//check if this is a switch or scale side-specific auto
		//if so, skip or run accordingly
		runAuto = true;
		wantToRelease = false; //default
		tWorker = null;
		String gameData = m_ds.getGameSpecificMessage();
		P.println(PrtYn.Y, "Dest:" + m_destLeftOrRight + ", gameData:" + gameData +
					", Scale char:" + gameData.charAt(TrajDest.SCALE.eCharIndex) +
					", Switch char:" + gameData.charAt(TrajDest.SWITCH.eCharIndex) +
					", current ArmOpMode=" + TmSsArm.ArmServices.getArmStage1OpMode().name() +
					", current ArmReqServoPos=" + TmSsArm.ArmServices.getArmStage1ReqServoPos()
					);
		if(tWorker==null) {
			if(gameData.length() == 3) {
				if((gameData.charAt(TrajDest.SCALE.eCharIndex)==m_destLeftOrRight) && (m_fileScale != null)) {
					tWorker = tWorkerScale; //new TmTrajectoryFileIo(m_fileScale);
					m_liftPosition = LiftPosE.TOP;
			     	wantToRelease = true;
			    } 
				else if ((gameData.charAt(TrajDest.SWITCH.eCharIndex)==m_destLeftOrRight) && (m_fileSwitch != null)){
					tWorker = tWorkerSwitch; //new TmTrajectoryFileIo(m_fileSwitch);
					m_liftPosition = LiftPosE.SWITCH;
			     	wantToRelease = true; 
				}
				else if (m_fileStraight != null) {
						tWorker = tWorkerNeither; //new TmTrajectoryFileIo(m_fileStraight);
						m_liftPosition = null;
						wantToRelease = false;
				}
				else {
					tWorker = null;
					m_liftPosition = null;
					wantToRelease = false;
				}
				
				if(tWorker==null) { 
					runAuto = false;
				} 
//				else {
//					tWorker.loadCsvData();
//				}
			}
			else { 
				//don't run auto if no gameData available
				runAuto = false;
			}
		}
         
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
        
     	armIsNotSet = true;
     	clampIsNotSet = true;
    }
    
    // Called repeatedly when this Command is scheduled to run
    protected void execute() {
    	if(runAuto) {
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

    		//Clamp and Arm control //now safe to release at switch: //(don't release if going to Switch)
    		if( wantToRelease && 
    				(tWorker.trajDataCsvL.get(listSize-1).getLeftTime() - autoTime.get()) < 2.3 &&
    				clampIsNotSet) {
    			TmSsGrabber.updateGrabberCubeClampAssist(GrabberCubeClampAssistE.UNCLAMPED);
    			clampIsNotSet = false; //clamp is set now
    		}
    		if((tWorker.trajDataCsvL.get(listSize-1).getLeftTime() - autoTime.get()) < 2 &&
    				armIsNotSet) {
    			setArmPosition();
    			armIsNotSet = false; //arm is set now
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
    			if(Tm744Opts.isUseNoGyros() || ((m_liftPosition != null) && m_liftPosition.equals(LiftPosE.SWITCH))) {
    				thetaError = 0;
    			} else {
    				if(Tm744Opts.isGyroNavX()) {
    					thetaError = tWorker.trajDataCsvL.get(traj_index_left).getAngle() - DrvNavX.getGlobalAngle();
    				} else {
    					thetaError = tWorker.trajDataCsvL.get(traj_index_left).getAngle() - DrvGyro.getAngle(); //ssDrive.getAngle();
    				} 
    			}
        		TmSdMgr.putNumber(SdKeysE.KEY_DRIVE_THETA_ERROR_DEGREES, thetaError);
        		TmSdMgr.putNumber(SdKeysE.KEY_DRIVE_THETA_DESIRED, tWorker.trajDataCsvL.get(traj_index_left).getAngle());

    			TmSdMgr.putNumber(SdKeysE.KEY_DRIVE_POS_ERR_RIGHT_FEET,
    					rightError*TmSsDriveTrain.DrvTrainCnst.DRV_ENCODER_COUNTS_2_FEET);
    			TmSdMgr.putNumber(SdKeysE.KEY_DRIVE_POS_ERR_LEFT_FEET,
    					leftError*TmSsDriveTrain.DrvTrainCnst.DRV_ENCODER_COUNTS_2_FEET);

    			double rightVelInput = tWorker.trajDataCsvL.get(traj_index_right).getRightVel() + 
    					TmSsDriveTrain.DrvTrainCnst.DRV_POS_P_GAIN * rightError + .18 * 
    					tWorker.trajDataCsvL.get(traj_index_right).getRightAcc() + 
    										   TmSsDriveTrain.DrvTrainCnst.DRV_ANGLE_P_GAIN  * thetaError;
    			double leftVelInput = tWorker.trajDataCsvL.get(traj_index_left).getLeftVel() + 
    					TmSsDriveTrain.DrvTrainCnst.DRV_POS_P_GAIN * leftError + .18 * 
    					tWorker.trajDataCsvL.get(traj_index_left).getLeftAcc() - //sign is intentionally different from above 
    							   			   TmSsDriveTrain.DrvTrainCnst.DRV_ANGLE_P_GAIN  * thetaError;
//    			P.println(PrtYn.Y, "Left=" + leftVelInput + " Right=" + rightVelInput);
    			P.println(PrtYn.Y, "Left=" + leftVelInput + " Right=" + rightVelInput +
    					" trajNdxL=" + traj_index_left + " trajNdxR=" + traj_index_right +
    					" timeL=" + tWorker.trajDataCsvL.get(traj_index_left).getLeftTime());
    			TmSsDriveTrain.Driving.tankDriveRawVelocityFPS(leftVelInput, rightVelInput);
    		}
    	}
    }

    // Make this return true when this Command no longer needs to run execute()
    protected boolean isFinished() {
    	boolean ans = false;
     	if( m_tds.isEnabledAutonomous() 
     			&& runAuto 
     			&& 
     			(traj_index_left < tWorker.trajDataCsvL.size() 
     			|| traj_index_right < tWorker.trajDataCsvL.size())
     			) {
     		ans = false;
     	} else {
     		ans = true;
     		ssDrive.stopAutonTrajectoryTest();
     		TmSsDriveTrain.Driving.tankDriveRawPercentOutput(0.0, 0.0);
     	}
     	//if driving straight, force finish to 'false' so that we never move on to the
     	//next step of the alg's cmd grp (i.e. will never try to release a cube
     	//TBD
     	if( ! wantToRelease ) { // ((m_liftPosition == null) || (m_liftPosition.equals(LiftPosE.SWITCH))) {
     		ans = false;
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
		if (m_liftPosition != null){
			switch(m_liftPosition) {
			case BOTTOM: //POV-W???
			case SWITCH: //POV-E???
			case SCALE_MID: //btn X???
			case TOP: //btn Y???
			case SCALE_LOW: //btn A??
				//			if (m_liftPosition != null){
				stage1Pos = TmSsArm.ArmServices.getFixedEncoderPosition(m_liftPosition);
				P.println(PrtYn.Y, Tt.getClassName(this) + " setting position to " + 
						m_liftPosition.name() + " (" + stage1Pos + " ticks)");
				TmSsArm.ArmServices.requestBothStagesServo(stage1Pos, 0);
				//			}
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
		} else {
			P.println(PrtYn.Y, "ignored - requested position is null");
		}
    }
    
}
