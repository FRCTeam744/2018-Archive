package org.usfirst.frc.tm744yr18.robot.subsystems;

import java.util.ArrayList;
import java.util.List;

import org.usfirst.frc.tm744yr18.robot.commands.TmACmdTestTrajectories.TrajectoryTestsE;
import org.usfirst.frc.tm744yr18.robot.commands.TmTCmdDriveWithJoysticks;
import org.usfirst.frc.tm744yr18.robot.config.TmHdwrDsCntls.DsNamedControlsE;
import org.usfirst.frc.tm744yr18.robot.config.TmHdwrRoCntls.RoNamedControlsE;
import org.usfirst.frc.tm744yr18.robot.config.TmHdwrRoPhysBase.FrcAndJoysticksVsRoDrvMtrDirVsRobotDirE;
import org.usfirst.frc.tm744yr18.robot.config.TmHdwrRoPhysBase.RoMtrInversionE;
import org.usfirst.frc.tm744yr18.robot.config.TmHdwrRoPhysBase.RoNamedControlsEntry;
import org.usfirst.frc.tm744yr18.robot.config.TmSdKeysI.SdKeysE;
//import org.usfirst.frc.tm744yr18.robot.devices.TmDriveSixMotors;
//import org.usfirst.frc.tm744yr18.robot.devices.TmDriveSixMotors.CenterDriveMotorsBehaviorE;
//import org.usfirst.frc.tm744yr18.robot.devices.TmDriveSixMotors.MotorSideAndLocation;
import org.usfirst.frc.tm744yr18.robot.devices.TmFakeable_CanTalonSrx;
import org.usfirst.frc.tm744yr18.robot.devices.TmFakeable_CanTalonSrx.CtreMotorInvertedE;
import org.usfirst.frc.tm744yr18.robot.devices.TmFakeable_CanTalonSrx.CtrePidIdxE;
import org.usfirst.frc.tm744yr18.robot.devices.TmFakeable_CanTalonSrx.CtreSensorPhaseE;
import org.usfirst.frc.tm744yr18.robot.devices.TmFakeable_CanTalonSrx.CtreSlotIdxE;
import org.usfirst.frc.tm744yr18.robot.devices.TmGyroADXRS453SPI;
import org.usfirst.frc.tm744yr18.robot.devices.TmNavX;
import org.usfirst.frc.tm744yr18.robot.exceptions.TmExceptions;
import org.usfirst.frc.tm744yr18.robot.helpers.TmDriverStation;
import org.usfirst.frc.tm744yr18.robot.helpers.TmSdMgr;
import org.usfirst.frc.tm744yr18.robot.helpers.TmTrajGenerator;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmDsControlUserI;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmRoControlUserI;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmStdSubsystemI;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmToolsI;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmToolsI.P.PrtYn;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.command.Subsystem;
import edu.wpi.first.wpilibj.hal.HAL;
import edu.wpi.first.wpilibj.hal.FRCNetComm.tInstances;
import edu.wpi.first.wpilibj.hal.FRCNetComm.tResourceType;
import jaci.pathfinder.Pathfinder;
import jaci.pathfinder.Waypoint;

public class TmSsDriveTrain extends Subsystem implements TmStdSubsystemI, TmToolsI, TmRoControlUserI, TmDsControlUserI {

	/*---------------------------------------------------------
	 * getInstance stuff                                      
	 *---------------------------------------------------------*/
	/** 
	 * handle making the singleton instance of this class and giving
	 * others access to it
	 */
	private static final TmSsDriveTrain m_instance = new TmSsDriveTrain();

	public static synchronized TmSsDriveTrain getInstance() {
		return m_instance;
	}

	private TmSsDriveTrain() {
//		if ( ! (m_instance == null)) {
//			P.println("Error!!! TmSsDriveTrain.m_instance is being modified!!");
//			P.println("         was: " + m_instance.toString());
//			P.println("         now: " + this.toString());
//		}
//		m_instance = this;
	}
	/*----------------end of getInstance stuff----------------*/

	//use these to save some typing
	static TmExceptions m_exceptions = TmExceptions.getInstance();
	static DriverStation m_ds = DriverStation.getInstance();
	static TmDriverStation m_tds = TmDriverStation.getInstance();
//	static TmDriveSixMotors m_drive;
	static DrvEncoderMgmt m_drvEncMgr;
	
	private Command m_defaultTeleopCommand = null;
//	private Command m_defaultAutonCommand = null;
//	private Command m_alternateTeleopCommand = null;
	private static final Object m_cmdLock = new Object();
	
//  private boolean m_allowLiveWindowControl = true;

	
	static TmFakeable_CanTalonSrx drvMtrFrontLAndEnc;
	static TmFakeable_CanTalonSrx drvMtrCenterL;
	static TmFakeable_CanTalonSrx drvMtrRearL;
	static TmFakeable_CanTalonSrx drvMtrFrontRAndEnc;
	static TmFakeable_CanTalonSrx drvMtrCenterR;
	static TmFakeable_CanTalonSrx drvMtrRearR;
	
	static List<TmFakeable_CanTalonSrx> drvMotorList = new ArrayList<>();
	static List<TmFakeable_CanTalonSrx> drvRightMotorList = new ArrayList<>();
	static List<TmFakeable_CanTalonSrx> drvLeftMotorList = new ArrayList<>();
	
	//these should be aliases for one of the motors above
	static TmFakeable_CanTalonSrx drvMtrAliasRightEncoder;
	static TmFakeable_CanTalonSrx drvMtrAliasLeftEncoder;

	public DrvEncoderMgmt getDriveEncoderMgmtInstance() { return m_drvEncMgr; }
	
	private int forDebugRefLeftEncSnapAtBoot; // = drvMtrAliasLeftEncoder.getSelectedSensorPosition(CtrePidIdxE.PRIMARY_CLOSED_LOOP);
	private int forDebugRefRightEncSnapAtBoot; // = drvMtrAliasRightEncoder.getSelectedSensorPosition(CtrePidIdxE.PRIMARY_CLOSED_LOOP);

	/**
	 * The location of a motor on the robot for the purpose of driving.
	 */
	public enum MotorSideAndLocationE { //see MotorType in FRC's DifferentialDrive class
		kFrontLeft(0), kFrontRight(1), kRearLeft(2), kRearRight(3), kCenterLeft(4), kCenterRight(5);

		public final int value; //TODO refactor to "index"
		private MotorSideAndLocationE(int value) { this.value = value; }
	}
	
	public int getPositionLeftNu() {
		return drvMtrAliasLeftEncoder.getSelectedSensorPosition(CtrePidIdxE.PRIMARY_CLOSED_LOOP);
	}
	
	public int getPositionRightNu() {
		return drvMtrAliasRightEncoder.getSelectedSensorPosition(CtrePidIdxE.PRIMARY_CLOSED_LOOP);
	}
	
	public void resetEncoders() {
		drvMtrAliasRightEncoder.setSelectedSensorPosition(0, CtrePidIdxE.PRIMARY_CLOSED_LOOP, 
				Cnst.CTRE_TIMEOUT_MS_NO_WAIT_FOR_COMPLETION);
		drvMtrAliasLeftEncoder.setSelectedSensorPosition(0, CtrePidIdxE.PRIMARY_CLOSED_LOOP, 
				Cnst.CTRE_TIMEOUT_MS_NO_WAIT_FOR_COMPLETION);
	}
	
	public static class DrvTrainCnst {
		public static final double DRV_ENCODER_MAX_REVS_PER_SECOND = Cnst.MAX_ENCODER_AXLE_RPS;
		public static final double DRV_ENCODER_FEET_PER_REVOLUTION = Cnst.FEET_PER_REVOLUTION;
		public static final int DRV_ENCODER_COUNTS_PER_REVOLUTION = Cnst.ENCODER_COUNTS_PER_REVOLUTION;
		public static final double DRV_POS_P_GAIN = Cnst.POS_P_GAIN;
		public static final double DRV_ANGLE_P_GAIN = Cnst.ANGLE_P_GAIN;
		public static final double DRV_ENCODER_COUNTS_2_FEET = Cnst.ENCODER_COUNTS_2_FEET;
		
    	public static final double WHEEL_DIAMETER = Cnst.WHEEL_DIAMETER; //0.5;
		public final static double RPM_2_FPS = Cnst.RPM_2_FPS; // WHEEL_DIAMETER*Math.PI/60;
		public final static double RPM_2_NuP100MS = Cnst.RPM_2_NuP100MS; //(1/60.0)*(1/10.0)*(4096.0);
		public final static double NuP100MS_2_RPM = Cnst.NuP100MS_2_RPM; // 1/RPM_2_NuP100MS;
		public final static double NuP2FPS = Cnst.NuP2FPS; //NuP100MS_2_RPM*RPM_2_FPS;
		public final static double FPS_2_NuP100MS = Cnst.FPS_2_NuP100MS; //1/NuP2FPS; //wrong: NuP100MS_2_RPM*RPM_2_FPS;
	}
    private static class Cnst {
    	//4in. wheels: (4in * pi) / 12 = 1.047
    	//6in. wheels: (6in * pi) / 12 = 1.5708
    	public static final double WHEEL_DIAMETER = 0.52559;
    	
    	public static final double FEET_PER_REVOLUTION = WHEEL_DIAMETER*Math.PI; //(6 * Math.PI)/12; //CONFIG_ME
    	
    	public static final double MAX_FEET_PER_SECOND = 16; //CONFIG_ME    	
    	public static final double MAX_FEET_PER_SECOND_LOW_GEAR = 6;
    	
    	public static final double MAX_ENCODER_AXLE_RPS = MAX_FEET_PER_SECOND/FEET_PER_REVOLUTION;
    	public static final int ENCODER_COUNTS_PER_REVOLUTION = 4096; //CONFIG_ME - this doesn't get configed, right? 	
    	public static final double ENCODER_COUNTS_2_FEET = FEET_PER_REVOLUTION / ENCODER_COUNTS_PER_REVOLUTION; //encCount * (feet/rev)*(rev/encCount)
    	
		public static final int CTRE_TIMEOUT_MS_NO_WAIT_FOR_COMPLETION = 0;
		public static final int CTRE_TIMEOUT_MS_WAIT_FOR_MOTOR_CFG = 20;

		//Find F Gain by finding max speed
		final static double RPM_2_FPS = WHEEL_DIAMETER*Math.PI/60; //Math.PI*6/60.0/12.0; //0.0262 //Rotations per minute to feet per second: 2*pi*r*(1min/60sec)*(1ft/12in)
		final static double FPS_2_RPM = 1/RPM_2_FPS; //38.1972
		final static double RPM_2_NuP100MS = (1/60.0)*(1/10.0)*(4096.0); //6.82 //native units per 100 ms to rotations per minute: (1min/60sec)*(1 sec/ 10 100ms)*(4096 Nu/rot)
		final static double NuP100MS_2_RPM = 1/RPM_2_NuP100MS; //.1464
		
		final static double NuP2FPS = NuP100MS_2_RPM*RPM_2_FPS;
		final static double FPS_2_NuP100MS = 1/NuP2FPS; //wrong: NuP100MS_2_RPM*RPM_2_FPS;
		
		final static double MAX_MEAS_SPEED_RPM = 4800*NuP100MS_2_RPM; //max measured speed in rpm //CONFIG_ME
		final static double MEAS_SPEED_RPM_AT_TESTING = 4000.0; //CONFIG_ME
		final static double PERCENT_OUTPUT_USED_TESTING = 0.7; //CONFIG_ME
		final static double MAX_MEAS_SPEED_FPS = MAX_MEAS_SPEED_RPM*RPM_2_FPS; //max measured speed in fps
		final static double F_GAIN_LEFT = 0.27;//(PERCENT_OUTPUT_USED_TESTING*1023)/(MEAS_SPEED_RPM_AT_TESTING*RPM_2_NuP100MS); //max output divided by max speed in native units/sample frame (100ms) //CONFIG_ME
		final static double F_GAIN_RIGHT = 0.27;
		
		//Find P Gain by finding max error
//		final static double MAX_ERROR_OBS_OPEN_LOOP_NU = ; //Max observed error in native units
//		final static double THROTTLE_CHANGE_NEEDED_TO_CORRECT_MAX_ERROR = ; //estimated throttle to correct the max error (tune this one)
//		final static double P_GAIN = (THROTTLE_CHANGE_NEEDED_TO_CORRECT_MAX_ERROR*1023)/MAX_ERROR_OBS_OPEN_LOOP_NU; //p gain calc
		final static double P_GAIN = 0.128 /* * 5*/; //CONFIG_ME
		
		//I and D at 0 for now
		final static double I_GAIN = 0;
		final static double D_GAIN = 0;
		
		//Voltage ramp - time from 0 to 1
		final static double VOLTAGE_RAMP_TIME_SECS = .15; //CONFIG_ME
		
		final static double POS_P_GAIN = -P_GAIN / 100.0;
		final static double ANGLE_P_GAIN = 0.5;
    
    }

    
    
    
	@Override
	public void sssDoInstantiate() {
		DrvGyro.doInstantiate();
		DrvNavX.doInstantiate();
	}

	@Override
	public void sssDoRobotInit() {
		m_exceptions = TmExceptions.getInstance();

		m_defaultTeleopCommand = new TmTCmdDriveWithJoysticks();
//		m_alternateTeleopCommand = new TmTCmdDriveWithJoysticks();
//		m_defaultAutonCommand = new TmACmdDoNothing();
		
		drvMtrFrontLAndEnc = new TmFakeable_CanTalonSrx(getInstance(), RoNamedControlsE.DRV_MTR_FRONT_LEFT_WITH_ENC);
		drvMtrCenterL = new TmFakeable_CanTalonSrx(getInstance(), RoNamedControlsE.DRV_MTR_CENTER_LEFT);
		drvMtrRearL = new TmFakeable_CanTalonSrx(getInstance(), RoNamedControlsE.DRV_MTR_REAR_LEFT);
		drvMtrFrontRAndEnc = new TmFakeable_CanTalonSrx(getInstance(), RoNamedControlsE.DRV_MTR_FRONT_RIGHT_WITH_ENC);
		drvMtrCenterR = new TmFakeable_CanTalonSrx(getInstance(), RoNamedControlsE.DRV_MTR_CENTER_RIGHT);
		drvMtrRearR = new TmFakeable_CanTalonSrx(getInstance(), RoNamedControlsE.DRV_MTR_REAR_RIGHT);
		
		DrvServices.populateDrvMotorListsAndAliases();
		
		for(TmFakeable_CanTalonSrx mtr : drvMotorList) {
			mtr.configContinuousCurrentLimit(mtr.m_namedCntlEnt.cMaxContinuousAmps.value, Cnst.CTRE_TIMEOUT_MS_NO_WAIT_FOR_COMPLETION);
			mtr.configPeakCurrentLimit(mtr.m_namedCntlEnt.cMaxPeakAmps.value, Cnst.CTRE_TIMEOUT_MS_NO_WAIT_FOR_COMPLETION);
			mtr.configPeakCurrentDuration(mtr.m_namedCntlEnt.cMaxPeakAmpsDurationMs.value, Cnst.CTRE_TIMEOUT_MS_NO_WAIT_FOR_COMPLETION);
		    mtr.enableCurrentLimit(true);
		}

		
		drvMtrAliasRightEncoder.configSelectedFeedbackSensor(FeedbackDevice.CTRE_MagEncoder_Relative, 
																			CtrePidIdxE.PRIMARY_CLOSED_LOOP, 
																			Cnst.CTRE_TIMEOUT_MS_NO_WAIT_FOR_COMPLETION);
		drvMtrAliasLeftEncoder.configSelectedFeedbackSensor(FeedbackDevice.CTRE_MagEncoder_Relative, 
																			CtrePidIdxE.PRIMARY_CLOSED_LOOP, 
																			Cnst.CTRE_TIMEOUT_MS_NO_WAIT_FOR_COMPLETION);
		//do this before doing motor inversions....
		drvMtrAliasRightEncoder.setSensorPhase(CtreSensorPhaseE.MATCHES_MOTOR);
		drvMtrAliasLeftEncoder.setSensorPhase(CtreSensorPhaseE.MATCHES_MOTOR);
		
		m_drvEncMgr = new DrvEncoderMgmt(); //drvMtrAliasLeftEncoder, drvMtrAliasRightEncoder);
		
		DrvServices.setMotorInversions(); //TalonSrx modules will do the right thing for the sensor too

		//Config nominal and peak outputs
		drvMtrAliasRightEncoder.configNominalOutputForward(0.0, Cnst.CTRE_TIMEOUT_MS_NO_WAIT_FOR_COMPLETION);
		drvMtrAliasRightEncoder.configNominalOutputReverse(0.0, Cnst.CTRE_TIMEOUT_MS_NO_WAIT_FOR_COMPLETION);
		drvMtrAliasRightEncoder.configPeakOutputForward(1.0, Cnst.CTRE_TIMEOUT_MS_NO_WAIT_FOR_COMPLETION);
		drvMtrAliasRightEncoder.configPeakOutputReverse(-1.0, Cnst.CTRE_TIMEOUT_MS_NO_WAIT_FOR_COMPLETION);
		
		drvMtrAliasLeftEncoder.configNominalOutputForward(0.0, Cnst.CTRE_TIMEOUT_MS_NO_WAIT_FOR_COMPLETION);
		drvMtrAliasLeftEncoder.configNominalOutputReverse(0.0, Cnst.CTRE_TIMEOUT_MS_NO_WAIT_FOR_COMPLETION);		
		drvMtrAliasLeftEncoder.configPeakOutputForward(1.0, Cnst.CTRE_TIMEOUT_MS_NO_WAIT_FOR_COMPLETION);
		drvMtrAliasLeftEncoder.configPeakOutputReverse(-1.0, Cnst.CTRE_TIMEOUT_MS_NO_WAIT_FOR_COMPLETION);
		
		//Set gains
		drvMtrAliasRightEncoder.config_kF(CtreSlotIdxE.PARM_SLOT_0, Cnst.F_GAIN_RIGHT, Cnst.CTRE_TIMEOUT_MS_WAIT_FOR_MOTOR_CFG);
		drvMtrAliasRightEncoder.config_kP(CtreSlotIdxE.PARM_SLOT_0, Cnst.P_GAIN, Cnst.CTRE_TIMEOUT_MS_WAIT_FOR_MOTOR_CFG);
		drvMtrAliasRightEncoder.config_kI(CtreSlotIdxE.PARM_SLOT_0, Cnst.I_GAIN, Cnst.CTRE_TIMEOUT_MS_WAIT_FOR_MOTOR_CFG);
		drvMtrAliasRightEncoder.config_kD(CtreSlotIdxE.PARM_SLOT_0, Cnst.D_GAIN, Cnst.CTRE_TIMEOUT_MS_WAIT_FOR_MOTOR_CFG);

		drvMtrAliasLeftEncoder.config_kF(CtreSlotIdxE.PARM_SLOT_0, Cnst.F_GAIN_LEFT, Cnst.CTRE_TIMEOUT_MS_WAIT_FOR_MOTOR_CFG);
		drvMtrAliasLeftEncoder.config_kP(CtreSlotIdxE.PARM_SLOT_0, Cnst.P_GAIN, Cnst.CTRE_TIMEOUT_MS_WAIT_FOR_MOTOR_CFG);
		drvMtrAliasLeftEncoder.config_kI(CtreSlotIdxE.PARM_SLOT_0, Cnst.I_GAIN, Cnst.CTRE_TIMEOUT_MS_WAIT_FOR_MOTOR_CFG);
		drvMtrAliasLeftEncoder.config_kD(CtreSlotIdxE.PARM_SLOT_0, Cnst.D_GAIN, Cnst.CTRE_TIMEOUT_MS_WAIT_FOR_MOTOR_CFG);

		//Config Voltage ramp rate
//		drvMtrAliasRightEncoder.configClosedloopRamp(Cnst.VOLTAGE_RAMP_TIME_SECS, Cnst.CTRE_TIMEOUT_MS_NO_WAIT_FOR_COMPLETION);
//		drvMtrAliasLeftEncoder.configClosedloopRamp(Cnst.VOLTAGE_RAMP_TIME_SECS, Cnst.CTRE_TIMEOUT_MS_NO_WAIT_FOR_COMPLETION);
		drvMtrAliasRightEncoder.configOpenloopRamp(Cnst.VOLTAGE_RAMP_TIME_SECS, Cnst.CTRE_TIMEOUT_MS_NO_WAIT_FOR_COMPLETION);
		drvMtrAliasLeftEncoder.configOpenloopRamp(Cnst.VOLTAGE_RAMP_TIME_SECS, Cnst.CTRE_TIMEOUT_MS_NO_WAIT_FOR_COMPLETION);
		
		DrvServices.setFollowers(); //from here on out, only need to send commands to the motors with encoders (the master), 
								 //other motors on that side should follow the master
		//TmHdwrDsCntls.DsNamedControlsE.DRIVE_VELOCITY_MODE.getEnt().whenPressed(this, cmdToRun);;
		DsNamedControlsE.DRIVE_RECALIBRATE_GYRO_RATE_BTN.getEnt().whenPressed(this, TmGyroADXRS453SPI.LocalCommands.getInstance().new 
									LocalCmd_RecalibrateRate(DrvGyro.g_gyroObj, DsNamedControlsE.DRIVE_RECALIBRATE_GYRO_RATE_BTN, 0.001));
		
		forDebugRefLeftEncSnapAtBoot = drvMtrAliasLeftEncoder.getSelectedSensorPosition(CtrePidIdxE.PRIMARY_CLOSED_LOOP);
		forDebugRefRightEncSnapAtBoot = drvMtrAliasRightEncoder.getSelectedSensorPosition(CtrePidIdxE.PRIMARY_CLOSED_LOOP);
		P.printFrmt(PrtYn.Y, "at boot, drive-train L-enc=% 6d, R-enc=% 6d", forDebugRefLeftEncSnapAtBoot, forDebugRefRightEncSnapAtBoot);
		getInstance().getDriveEncoderMgmtInstance().reset();
	}
	
	public static class DrvServices {
		public static void populateDrvMotorListsAndAliases() {

			//will get exceptions if things aren't added to the list in the proper order
			drvMotorList.add(MotorSideAndLocationE.kFrontLeft.value, drvMtrFrontLAndEnc);
			drvMotorList.add(MotorSideAndLocationE.kFrontRight.value, drvMtrFrontRAndEnc);
			drvMotorList.add(MotorSideAndLocationE.kRearLeft.value, drvMtrRearL);
			drvMotorList.add(MotorSideAndLocationE.kRearRight.value, drvMtrRearR);
			drvMotorList.add(MotorSideAndLocationE.kCenterLeft.value, drvMtrCenterL);
			drvMotorList.add(MotorSideAndLocationE.kCenterRight.value, drvMtrCenterR);

			
			drvLeftMotorList.add(drvMtrFrontLAndEnc);
			drvLeftMotorList.add(drvMtrRearL);
			drvLeftMotorList.add(drvMtrCenterL);
			
			drvRightMotorList.add(drvMtrFrontRAndEnc);
			drvRightMotorList.add(drvMtrRearR);
			drvRightMotorList.add(drvMtrCenterR);
			
			//Note: x=a?b:c; means if(a){x=b;}else{x=c;} -- conditional operator [744conditionalOp]
			drvMtrAliasRightEncoder = (drvMtrFrontRAndEnc.m_namedCntlEnt.cMtrHasEncoder) ? drvMtrFrontRAndEnc : null;
			drvMtrAliasLeftEncoder =  (drvMtrFrontLAndEnc.m_namedCntlEnt.cMtrHasEncoder) ? drvMtrFrontLAndEnc : null;
			if(drvMtrAliasRightEncoder==null || drvMtrAliasLeftEncoder==null) {
				throw m_exceptions.new Team744RunTimeEx("Bad motor config in drive train subsystem -- encoder not mounted on expected motor");
			}
		}
		
		public TmFakeable_CanTalonSrx getMtrObjFromCanId(int canId) {
			TmFakeable_CanTalonSrx ans = null;
			for(TmFakeable_CanTalonSrx mtr : drvMotorList) {
				if(mtr.m_namedConnEnt.getConnectionFrcIndex() == canId) {
					ans = mtr;
					break;
				}
			}
			return ans;
		}
		
		public static void setMotorInversions() {
			//invert appropriate talons (1/31/18 expect drvMtrCenterL and drvMtrCenterR)
			for(TmFakeable_CanTalonSrx mtr : drvMotorList) {
				if(mtr.m_namedCntlEnt.cMtrInversion.isInvertMotor()) { mtr.setInverted(CtreMotorInvertedE.INVERTED); }
			}
		}
		
		public static void stopAllMotors() {
			for(TmFakeable_CanTalonSrx mtr : drvMotorList) { mtr.set(ControlMode.Disabled, 0.0); }		
		}
		
		public static void setFollowers() {
			setFollowers(drvLeftMotorList, drvMtrAliasLeftEncoder);
			setFollowers(drvRightMotorList, drvMtrAliasRightEncoder);
		}
		private static void setFollowers(List<TmFakeable_CanTalonSrx> mtrListSide, TmFakeable_CanTalonSrx masterMotor) {
			boolean foundMaster = false;
			for(TmFakeable_CanTalonSrx mtr : mtrListSide) { 
				if(mtr.equals(masterMotor)) { foundMaster = true; }
			}	
			if( ! foundMaster) {
				throw m_exceptions.new Team744RunTimeEx("Bad motor config in drive train subsystem -- " +
							"master motor " + masterMotor.m_namedCntl.name() + " not in proper motor list for followers");
			}
			
//			masterMotor.set(ControlMode.PercentOutput, 0.0);
			for(TmFakeable_CanTalonSrx mtr : mtrListSide) { 
				if( ! mtr.equals(masterMotor)) { mtr.set(ControlMode.Follower, masterMotor.getDeviceID()); }
			}		
		}
		public static enum FollowersE { FOLLOWS_ENCODER_TALON, COAST }
		
		public static String getControlModeAndFollowerId(TmFakeable_CanTalonSrx mtr) {
			String ans = null;
			ControlMode cmode = mtr.getControlMode();
			ans = cmode.name();
			if(cmode.equals(ControlMode.Follower)) { ans += " (of id " + ((int)mtr.getLastControlModeOutputValue()) + ")"; }
			return ans;
		}
		
		public static void motorsPostToSd() {
			for(TmFakeable_CanTalonSrx mtr : drvMotorList) {
				motorsPostToSd(mtr);
			}
		}
		public static void motorsPostToSd(TmFakeable_CanTalonSrx mtr) {
				TmSdMgr.putString(mtr.m_namedCntlEnt.cSdKeyMtrControlMode, 
						(mtr.isFake() ? "FAKE - " : "") +
						getControlModeAndFollowerId(mtr));
				TmSdMgr.putNumber(mtr.m_namedCntlEnt.cSdKeyMtrPercentOut, mtr.getMotorOutputPercent());
		}
	
		/**
		 * these methods expect the actual values to be sent to the motors.  Any funky negations of joystick values
		 * and other such stuff should have already been done.
		 * @param cntlMode
		 * @param mtrValLeft
		 * @param mtrValRight
		 */
		public static void setRearMotors(ControlMode cntlMode, double mtrValLeft, double mtrValRight) {
			drvMtrRearL.set(cntlMode, mtrValLeft);
			drvMtrRearR.set(cntlMode, mtrValRight);
			motorsPostToSd(drvMtrRearL);
			motorsPostToSd(drvMtrRearR);
//			TmSdMgr.putString(SdKeysE.KEY_DRIVE_MODE_LEFT, 
//					(drvMtrRearL.isFake() ? "FAKE - " : "") +
//					getControlModeAndFollowerId(drvMtrRearL)); //.getControlMode().name());
//			TmSdMgr.putString(SdKeysE.KEY_DRIVE_MODE_RIGHT, 
//					(drvMtrRearR.isFake() ? "FAKE - " : "") +
//					getControlModeAndFollowerId(drvMtrRearR)); //.getControlMode().name());
//			TmSdMgr.putNumber(SdKeysE.KEY_DRIVE_OUTPUT_LEFT, drvMtrRearL.getMotorOutputPercent());
//			TmSdMgr.putNumber(SdKeysE.KEY_DRIVE_OUTPUT_RIGHT, drvMtrRearR.getMotorOutputPercent());
		}
		public static void setFrontMotors(ControlMode cntlMode, double mtrValLeft, double mtrValRight) {
			drvMtrFrontLAndEnc.set(cntlMode, mtrValLeft);
			drvMtrFrontRAndEnc.set(cntlMode, mtrValRight);
			motorsPostToSd(drvMtrFrontLAndEnc);
			motorsPostToSd(drvMtrFrontRAndEnc);
//			TmSdMgr.putString(SdKeysE.KEY_DRIVE_MOTOR_FRONT_LEFT_MODE, 
//					(drvMtrFrontLAndEnc.isFake() ? "FAKE - " : "") +
//					getControlModeAndFollowerId(drvMtrFrontLAndEnc)); //.getControlMode().name());
//			TmSdMgr.putString(SdKeysE.KEY_DRIVE_MOTOR_FRONT_RIGHT_MODE, 
//					(drvMtrFrontRAndEnc.isFake() ? "FAKE - " : "") +
//					getControlModeAndFollowerId(drvMtrFrontRAndEnc)); //.getControlMode().name());
//			TmSdMgr.putNumber(SdKeysE.KEY_DRIVE_MOTOR_FRONT_LEFT_PERCENT_OUT, drvMtrFrontLAndEnc.getMotorOutputPercent());
//			TmSdMgr.putNumber(SdKeysE.KEY_DRIVE_MOTOR_FRONT_RIGHT_PERCENT_OUT, drvMtrFrontRAndEnc.getMotorOutputPercent());
		}
		public static void setCenterMotors(ControlMode cntlMode, double mtrValLeft, double mtrValRight) {
			drvMtrCenterL.set(cntlMode, mtrValLeft);
			drvMtrCenterR.set(cntlMode, mtrValRight);
			motorsPostToSd(drvMtrCenterL);
			motorsPostToSd(drvMtrCenterR);
//			TmSdMgr.putString(SdKeysE.KEY_DRIVE_MOTOR_CENTER_LEFT_MODE, 
//					(drvMtrCenterL.isFake() ? "FAKE - " : "") +
//					getControlModeAndFollowerId(drvMtrCenterL)); //.getControlMode().name());
//			TmSdMgr.putString(SdKeysE.KEY_DRIVE_MOTOR_CENTER_RIGHT_MODE, 
//					(drvMtrCenterR.isFake() ? "FAKE - " : "") +
//					getControlModeAndFollowerId(drvMtrCenterR)); //.getControlMode().name());
//			TmSdMgr.putNumber(SdKeysE.KEY_DRIVE_MOTOR_CENTER_LEFT_PERCENT_OUT, drvMtrCenterL.getMotorOutputPercent());
//			TmSdMgr.putNumber(SdKeysE.KEY_DRIVE_MOTOR_CENTER_RIGHT_PERCENT_OUT, drvMtrCenterR.getMotorOutputPercent());
		}
		public static void setRearMotors(FollowersE follower) {
			if( ! follower.equals(FollowersE.FOLLOWS_ENCODER_TALON)) { throw m_exceptions.new Team744RunTimeEx("rear motor can only follow talon with encoder"); }
			drvMtrRearL.set(ControlMode.Follower, drvMtrAliasLeftEncoder.getDeviceID());
			drvMtrRearR.set(ControlMode.Follower, drvMtrAliasRightEncoder.getDeviceID());
			motorsPostToSd(drvMtrRearL);
			motorsPostToSd(drvMtrRearR);
//			TmSdMgr.putString(SdKeysE.KEY_DRIVE_MODE_LEFT, getControlModeAndFollowerId(drvMtrRearL)); //.getControlMode().name());
//			TmSdMgr.putString(SdKeysE.KEY_DRIVE_MODE_RIGHT, getControlModeAndFollowerId(drvMtrRearR)); //.getControlMode().name());
//			TmSdMgr.putNumber(SdKeysE.KEY_DRIVE_OUTPUT_LEFT, drvMtrRearL.getMotorOutputPercent());
//			TmSdMgr.putNumber(SdKeysE.KEY_DRIVE_OUTPUT_RIGHT, drvMtrRearR.getMotorOutputPercent());
////			TmSdMgr.putString(SdKeysE.KEY_DRIVE_MOTOR_FRONT_LEFT_MODE, getControlModeAndFollowerId(drvMtrFrontLAndEnc));
////			TmSdMgr.putString(SdKeysE.KEY_DRIVE_MOTOR_FRONT_RIGHT_MODE, getControlModeAndFollowerId(drvMtrFrontRAndEnc));
////			TmSdMgr.putNumber(SdKeysE.KEY_DRIVE_MOTOR_FRONT_LEFT_PERCENT_OUT, drvMtrFrontLAndEnc.getMotorOutputPercent());
////			TmSdMgr.putNumber(SdKeysE.KEY_DRIVE_MOTOR_FRONT_RIGHT_PERCENT_OUT, drvMtrFrontRAndEnc.getMotorOutputPercent());
		}
		//2018-02-15_20-55 we're swapping the polarity of the connections from the talon to the motor so that we can treat all
		//                 three talons the same way.  Let's us use center motor in follower mode too.
		public static void setCenterMotors(FollowersE follower) {
			switch(follower) {
			case COAST:
				drvMtrCenterL.set(ControlMode.PercentOutput, 0.0);
				drvMtrCenterR.set(ControlMode.PercentOutput, 0.0);
				break;
			case FOLLOWS_ENCODER_TALON:
//				throw m_exceptions.new Team744RunTimeEx("center motor cannot follow talon with encoder, needs to be inverted");
////				//apparently not!! works as long as setInverted methods were called properly earlier?, else the center
////				//motor will be fighting the other two?
////				drvMtrCenterL.set(ControlMode.Follower, drvMtrAliasLeftEncoder.getDeviceID());
////				drvMtrCenterR.set(ControlMode.Follower, drvMtrAliasRightEncoder.getDeviceID());
				drvMtrCenterL.set(ControlMode.Follower, drvMtrAliasLeftEncoder.getDeviceID());
				drvMtrCenterR.set(ControlMode.Follower, drvMtrAliasRightEncoder.getDeviceID());
//				TmSdMgr.putString(SdKeysE.KEY_DRIVE_MOTOR_CENTER_LEFT_MODE, getControlModeAndFollowerId(drvMtrCenterL));
//				TmSdMgr.putString(SdKeysE.KEY_DRIVE_MOTOR_CENTER_RIGHT_MODE, getControlModeAndFollowerId(drvMtrCenterR));
//				TmSdMgr.putNumber(SdKeysE.KEY_DRIVE_MOTOR_CENTER_LEFT_PERCENT_OUT, drvMtrCenterL.getMotorOutputPercent());
//				TmSdMgr.putNumber(SdKeysE.KEY_DRIVE_MOTOR_CENTER_RIGHT_PERCENT_OUT, drvMtrCenterR.getMotorOutputPercent());
				break;
			default:
				throw m_exceptions.new ReachedCodeThatShouldNeverExecuteEx("case statements missing??");
				//break;
			}
			motorsPostToSd(drvMtrCenterL);
			motorsPostToSd(drvMtrCenterR);
//			TmSdMgr.putString(SdKeysE.KEY_DRIVE_MOTOR_CENTER_LEFT_MODE, getControlModeAndFollowerId(drvMtrCenterL));
//			TmSdMgr.putString(SdKeysE.KEY_DRIVE_MOTOR_CENTER_RIGHT_MODE, getControlModeAndFollowerId(drvMtrCenterR));
//			TmSdMgr.putNumber(SdKeysE.KEY_DRIVE_MOTOR_CENTER_LEFT_PERCENT_OUT, drvMtrCenterL.getMotorOutputPercent());
//			TmSdMgr.putNumber(SdKeysE.KEY_DRIVE_MOTOR_CENTER_RIGHT_PERCENT_OUT, drvMtrCenterR.getMotorOutputPercent());
		}
		
		public static void simulationConsMsgCurrentDriveInfo() {
			for(TmFakeable_CanTalonSrx mtr : drvMotorList) {
				P.println("DrvMtr " + mtr.m_namedCntl.name() + ": " +
							mtr.getControlMode().name() + ", %out=" + mtr.getMotorOutputPercent());
			}		
		}
	}
	public static void postToSd(){
		TmSdMgr.putNumber(SdKeysE.KEY_LEFT_ENCODER_RAW_SPEED, drvMtrAliasLeftEncoder.getSelectedSensorVelocity(CtrePidIdxE.PRIMARY_CLOSED_LOOP));
		TmSdMgr.putNumber(SdKeysE.KEY_RIGHT_ENCODER_RAW_SPEED, drvMtrAliasRightEncoder.getSelectedSensorVelocity(CtrePidIdxE.PRIMARY_CLOSED_LOOP));
	
		TmSdMgr.putNumber(SdKeysE.KEY_LEFT_ENCODER_SPEED_FEET_PER_SECOND, drvMtrAliasLeftEncoder.getSelectedSensorVelocity(CtrePidIdxE.PRIMARY_CLOSED_LOOP)* Cnst.NuP2FPS);
		TmSdMgr.putNumber(SdKeysE.KEY_RIGHT_ENCODER_SPEED_FEET_PER_SECOND, drvMtrAliasRightEncoder.getSelectedSensorVelocity(CtrePidIdxE.PRIMARY_CLOSED_LOOP)* Cnst.NuP2FPS);
		
		TmSdMgr.putNumber(SdKeysE.KEY_LEFT_ENCODER_DISTANCE_RAW, drvMtrAliasLeftEncoder.getSelectedSensorPosition(CtrePidIdxE.PRIMARY_CLOSED_LOOP));
		TmSdMgr.putNumber(SdKeysE.KEY_RIGHT_ENCODER_DISTANCE_RAW, drvMtrAliasRightEncoder.getSelectedSensorPosition(CtrePidIdxE.PRIMARY_CLOSED_LOOP));
		
		TmSdMgr.putNumber(SdKeysE.KEY_LEFT_ENCODER_DISTANCE_FT, drvMtrAliasLeftEncoder.getSelectedSensorPosition(CtrePidIdxE.PRIMARY_CLOSED_LOOP)* Cnst.ENCODER_COUNTS_2_FEET);
		TmSdMgr.putNumber(SdKeysE.KEY_RIGHT_ENCODER_DISTANCE_FT, drvMtrAliasRightEncoder.getSelectedSensorPosition(CtrePidIdxE.PRIMARY_CLOSED_LOOP)*Cnst.ENCODER_COUNTS_2_FEET);
	}
	public static void postToSd2(){
		TmSdMgr.putNumber(SdKeysE.KEY_LEFT_ENCODER_CLOSED_LOOP_ERROR_RAW, drvMtrAliasLeftEncoder.getClosedLoopError(CtrePidIdxE.PRIMARY_CLOSED_LOOP));
		TmSdMgr.putNumber(SdKeysE.KEY_RIGHT_ENCODER_CLOSED_LOOP_ERROR_RAW, drvMtrAliasRightEncoder.getClosedLoopError(CtrePidIdxE.PRIMARY_CLOSED_LOOP));
		TmSdMgr.putNumber(SdKeysE.KEY_LEFT_ENCODER_CLOSED_LOOP_ERROR_FT_PER_SECOND, drvMtrAliasLeftEncoder.getClosedLoopError(CtrePidIdxE.PRIMARY_CLOSED_LOOP)* Cnst.NuP2FPS);
		TmSdMgr.putNumber(SdKeysE.KEY_RIGHT_ENCODER_CLOSED_LOOP_ERROR_FT_PER_SECOND, drvMtrAliasRightEncoder.getClosedLoopError(CtrePidIdxE.PRIMARY_CLOSED_LOOP)* Cnst.NuP2FPS);
	}
	
//	public static Driving m_driving = TmSsDriveTrain.getInstance().new Driving();
	public static class Driving {
		private Driving() {} //force constructor private so no one can ever call it outside of TmSsDriveTrain
		
		static boolean m_inspected_tankDriveJoysticksPercentOutput = false;
		static boolean m_inspected_tankDriveJoysticksVelocity = false;
		private static final Object m_motorOutputLock = new Object(); 
		
		/**
		 * Provide traditional tank steering using the stored robot configuration. This function 
		 * lets you directly provide joystick-style values from any source.
		 * 
		 * Note that joysticks return negative values when pushed away from you or to the left.
		 * They return positive values when pulled toward you or to the right.
		 *
		 * @param leftValue  The value of the left stick. (negative to move robot forward)
		 * @param rightValue The value of the right stick. (negative to move robot forward)
		 */
		public static synchronized void tankDriveJoysticksPercentOutput(double leftJoyReading, double rightJoyReading) {
			tankDriveJoysticksPercentOutput(leftJoyReading, rightJoyReading, true, true); //TODO chg 'inspect' parm to false once tested and working
		}
		public static synchronized void tankDriveJoysticksPercentOutput(double leftJoyReading, double rightJoyReading, 
																			boolean useSquaredMagnitudes, boolean inspect) { 
			if(inspect && !m_inspected_tankDriveJoysticksPercentOutput) {
				int foundErrors = 0;
				FrcAndJoysticksVsRoDrvMtrDirVsRobotDirE mtrRevForRobotFrwd = FrcAndJoysticksVsRoDrvMtrDirVsRobotDirE.FOR_FRC_JS_MTR_IN_REVERSE_DIR_FOR_ROBOT_FORWARD; //use joystick rdgs as-is
				FrcAndJoysticksVsRoDrvMtrDirVsRobotDirE mtrFrwdForRobotFrwd = FrcAndJoysticksVsRoDrvMtrDirVsRobotDirE.FOR_FRC_JS_MTR_IN_FORWARD_DIR_FOR_ROBOT_FORWARD; //invert joystick rdgs
				
				if( ! drvMtrFrontLAndEnc.m_namedCntlEnt.cMtrForFrcJsDirVsRobotDir.equals(mtrRevForRobotFrwd)) { foundErrors++; }
				if( ! drvMtrRearL.m_namedCntlEnt.cMtrForFrcJsDirVsRobotDir.equals(mtrRevForRobotFrwd)) { foundErrors++; }

//2018-02-15_20-55 - center motor connections from talon swapped
//				if( ! drvMtrCenterR.m_namedCntlEnt.cMtrDirVsRobotDir.equals(mtrRevForRobotFrwd)) { foundErrors++; }
				if( ! drvMtrCenterL.m_namedCntlEnt.cMtrForFrcJsDirVsRobotDir.equals(mtrRevForRobotFrwd)) { foundErrors++; }
				
				if( ! drvMtrFrontRAndEnc.m_namedCntlEnt.cMtrForFrcJsDirVsRobotDir.equals(mtrFrwdForRobotFrwd)) { foundErrors++; }
				if( ! drvMtrRearR.m_namedCntlEnt.cMtrForFrcJsDirVsRobotDir.equals(mtrFrwdForRobotFrwd)) { foundErrors++; }
				
//2018-02-15_20-55 - center motor connections from talon swapped
//				if( ! drvMtrCenterL.m_namedCntlEnt.cMtrDirVsRobotDir.equals(mtrFrwdForRobotFrwd)) { foundErrors++; }
				if( ! drvMtrCenterR.m_namedCntlEnt.cMtrForFrcJsDirVsRobotDir.equals(mtrFrwdForRobotFrwd)) { foundErrors++; }

				if(foundErrors>0) {
					throw m_exceptions.new Team744RunTimeEx("check motor direction vs robot direction settings");
				}
				m_inspected_tankDriveJoysticksPercentOutput = true;
			}
			if(useSquaredMagnitudes) {
				//makes the joysticks easier to use....
				leftJoyReading = Math.copySign(leftJoyReading*leftJoyReading, leftJoyReading);
				rightJoyReading = Math.copySign(rightJoyReading*rightJoyReading, rightJoyReading);
			}
			if( 0.15 < Math.abs(TmSsArm.ArmServices.getArmStage1MotorPercentOut()) ) {
				leftJoyReading = Tt.clampToRange(leftJoyReading, -0.5, 0.5);
				rightJoyReading = Tt.clampToRange(leftJoyReading, -0.5, 0.5);
			}
			synchronized(m_motorOutputLock) {
				switch(Tt.compareSignsDouble(leftJoyReading, rightJoyReading)) {
				case A_GT0_B_NEG:
				case A_NEG_B_GT0:
					DrvServices.setFrontMotors(ControlMode.PercentOutput, leftJoyReading, -rightJoyReading);
					DrvServices.setCenterMotors(DrvServices.FollowersE.COAST); //we're spinning; center motors need to coast
					DrvServices.setRearMotors(DrvServices.FollowersE.FOLLOWS_ENCODER_TALON);
					break;
				case BOTH_NEG:
				case BOTH_GT0:
				case BOTH_ZERO:
				case A_GT0_B_ZERO:
				case A_NEG_B_ZERO:
				case A_ZERO_B_GT0:
				case A_ZERO_B_NEG:
				case IMPOSSIBLE:
				default:
					DrvServices.setFrontMotors(ControlMode.PercentOutput, leftJoyReading, -rightJoyReading);
//2018-02-15_20-55 - center motor connections from talon swapped
//					//apparently not!: can set center motors to follow rear as long as the setInverted() method was properly called earlier?
//					//DrvServices.setCenterMotors(Services.FollowersE.FOLLOWS_ENCODER_TALON);
//					DrvServices.setCenterMotors(ControlMode.PercentOutput, -leftJoyReading, rightJoyReading);
					DrvServices.setCenterMotors(DrvServices.FollowersE.FOLLOWS_ENCODER_TALON);
					DrvServices.setRearMotors(DrvServices.FollowersE.FOLLOWS_ENCODER_TALON);
					break;
				}
			}
			postToSd();
			postToSd2();
		}

		/**
		 * Joysticks return negative values when pushed away from you or to the left.
		 * They return positive values when pulled toward you or to the right. So, when
		 * we drive from joysticks, negative values move the robot forward, positive ones
		 * move it in reverse.
		 * 
		 * We provide tankDriveRawPercentOutput for code to use when it's calculating the actual
		 * speed rather than a joystick reading.  This method will do the negation required to
		 * convert 'speed' as a human being conceives of it into the values expected
		 * by the traditional tankDrive() methods.  It also bypasses the default
		 * "Square but keep sign" work normally done to make joysticks easier to use
		 * 
		 * @param leftSpeed  (positive to move robot forward)
		 * @param rightSpeed  (positive to move robot forward)
		 */
		public static void tankDriveRawPercentOutput(double leftSpeed, double rightSpeed) {
			tankDriveJoysticksPercentOutput(-leftSpeed, -rightSpeed, false, true);
		}
		public static void tankDriveRawVelocity(double leftSpeed, double rightSpeed) {
			synchronized(m_motorOutputLock) {
				switch(Tt.compareSignsDouble(leftSpeed, rightSpeed)) {
				case A_GT0_B_NEG:
				case A_NEG_B_GT0:
					DrvServices.setFrontMotors(ControlMode.Velocity, -leftSpeed, rightSpeed);
					DrvServices.setCenterMotors(DrvServices.FollowersE.COAST); //spinning. let center motors coast
					DrvServices.setRearMotors(DrvServices.FollowersE.FOLLOWS_ENCODER_TALON);
					break;
				case A_GT0_B_ZERO:
				case A_NEG_B_ZERO:
				case A_ZERO_B_GT0:
				case A_ZERO_B_NEG:
				case BOTH_GT0:
				case BOTH_NEG:
				case BOTH_ZERO:
				case IMPOSSIBLE:
				default:
					DrvServices.setFrontMotors(ControlMode.Velocity, -leftSpeed, rightSpeed);
//2018-02-15_20-55 - center motor connections from talon swapped				
//					//apparently not!: can set center motors to follow rear as long as the setInverted() method was properly called earlier?
//					//DrvServices.setCenterMotors(ControlMode.Velocity, -targetSpeedLeft, targetSpeedRight);
//					//DrvServices.setCenterMotors(Services.FollowersE.FOLLOWS_REAR); //ControlMode.Follower, drvMtrRearLAndEnc.getDeviceID(), drvMtrRearRAndEnc.getDeviceID());
//					DrvServices.setCenterMotors(DrvServices.FollowersE.COAST);
					DrvServices.setCenterMotors(DrvServices.FollowersE.FOLLOWS_ENCODER_TALON);
					DrvServices.setRearMotors(DrvServices.FollowersE.FOLLOWS_ENCODER_TALON);
					break;
				}
			}
			postToSd();
			postToSd2();
		}
		public static void tankDriveRawVelocityFPS(double leftSpeedFPS, double rightSpeedFPS) {
			tankDriveRawVelocity(leftSpeedFPS*Cnst.FPS_2_NuP100MS, rightSpeedFPS*Cnst.FPS_2_NuP100MS);
//			tankDriveJoysticksVelocity(leftSpeedFPS/Cnst.MAX_MEAS_SPEED_FPS, rightSpeedFPS/Cnst.MAX_MEAS_SPEED_FPS, false, true);
		}
		
		/**
		 * Provide traditional tank steering for velocity-driven robot configuration. This function 
		 * lets you directly provide joystick-style values from any source.
		 * 
		 * Note that joysticks return negative values when pushed away from you or to the left.
		 * They return positive values when pulled toward you or to the right.
		 *
		 * @param leftValue  The value of the left stick. (negative to move robot forward)
		 * @param rightValue The value of the right stick. (negative to move robot forward)
		 */
		public static synchronized void tankDriveJoysticksVelocity(double leftJoyReading, double rightJoyReading) {
			tankDriveJoysticksVelocity(leftJoyReading, rightJoyReading, true, true); //TODO change 'inspect' to false once tested and working
		}
		public static synchronized void tankDriveJoysticksVelocity(double leftJoyReading, double rightJoyReading, 
																			boolean useSquaredMagnitudes, boolean inspect) {
			if(useSquaredMagnitudes) {
				//makes the joysticks easier to use....
				leftJoyReading = Math.copySign(leftJoyReading*leftJoyReading, leftJoyReading);
				rightJoyReading = Math.copySign(rightJoyReading*rightJoyReading, rightJoyReading);
			}
			double targetSpeedRight = rightJoyReading * -1*Cnst.MAX_MEAS_SPEED_RPM*Cnst.RPM_2_NuP100MS;
			double targetSpeedLeft = leftJoyReading * Cnst.MAX_MEAS_SPEED_RPM*Cnst.RPM_2_NuP100MS;
			if(inspect && !m_inspected_tankDriveJoysticksVelocity) {
				int foundErrors = 0;
				FrcAndJoysticksVsRoDrvMtrDirVsRobotDirE mtrRevForRobotFrwd = FrcAndJoysticksVsRoDrvMtrDirVsRobotDirE.FOR_FRC_JS_MTR_IN_REVERSE_DIR_FOR_ROBOT_FORWARD; //use joystick rdgs as-is
				FrcAndJoysticksVsRoDrvMtrDirVsRobotDirE mtrFrwdForRobotFrwd = FrcAndJoysticksVsRoDrvMtrDirVsRobotDirE.FOR_FRC_JS_MTR_IN_FORWARD_DIR_FOR_ROBOT_FORWARD; //invert joystick rdgs
				
				if( ! drvMtrFrontLAndEnc.m_namedCntlEnt.cMtrForFrcJsDirVsRobotDir.equals(mtrRevForRobotFrwd)) { foundErrors++; }
				if( ! drvMtrRearL.m_namedCntlEnt.cMtrForFrcJsDirVsRobotDir.equals(mtrRevForRobotFrwd)) { foundErrors++; }

//2018-02-15_20-55 - center motor connections from talon swapped				
//				if( ! drvMtrCenterR.m_namedCntlEnt.cMtrDirVsRobotDir.equals(mtrRevForRobotFrwd)) { foundErrors++; }
				if( ! drvMtrCenterL.m_namedCntlEnt.cMtrForFrcJsDirVsRobotDir.equals(mtrRevForRobotFrwd)) { foundErrors++; }
				
				if( ! drvMtrFrontRAndEnc.m_namedCntlEnt.cMtrForFrcJsDirVsRobotDir.equals(mtrFrwdForRobotFrwd)) { foundErrors++; }
				if( ! drvMtrRearR.m_namedCntlEnt.cMtrForFrcJsDirVsRobotDir.equals(mtrFrwdForRobotFrwd)) { foundErrors++; }
				
//2018-02-15_20-55 - center motor connections from talon swapped				
//				if( ! drvMtrCenterL.m_namedCntlEnt.cMtrDirVsRobotDir.equals(mtrFrwdForRobotFrwd)) { foundErrors++; }
				if( ! drvMtrCenterR.m_namedCntlEnt.cMtrForFrcJsDirVsRobotDir.equals(mtrFrwdForRobotFrwd)) { foundErrors++; }

				if(foundErrors>0) {
					throw m_exceptions.new Team744RunTimeEx("check motor direction vs robot direction settings");
				}
			}
			tankDriveRawVelocity(targetSpeedLeft, targetSpeedRight);
//			tankDriveJoysticksVelocity(-leftSpeed, -rightSpeed, false, true);

		}
		

		//things from RobotDrive class used by its drive() method
		@Deprecated //per 2018 RobotDrive class
        public static final double kDefaultSensitivity = 0.5;
		@Deprecated //per 2018 RobotDrive class, made it static for use here
        protected static double m_sensitivity = kDefaultSensitivity;
		@Deprecated //per 2018 RobotDrive class
		protected static boolean kArcadeRatioCurve_Reported = false;
		@Deprecated //per 2018 RobotDrive class
		protected static int getNumMotors() { return 6; }
		/**
		 * Drive the motors at "outputMagnitude" and "curve". Both outputMagnitude and curve are -1.0 to
		 * +1.0 values, where 0.0 represents stopped and not turning. {@literal curve < 0 will turn left
		 * and curve > 0} will turn right.
		 *
		 * <p>The algorithm for steering provides a constant turn radius for any normal speed range, both
		 * forward and backward. Increasing sensitivity causes sharper turns for fixed values of curve.
		 *
		 * <p>This function will most likely be used in an autonomous routine.
		 * 
		 * <p>This function is a modified version of the drive() method provided in wpilibj's RobotDrive class.
		 *
		 * @param outputMagnitude The speed setting for the outside wheel in a turn, forward or backwards,
		 *                        +1 to -1.
		 * @param curve           The rate of turn, constant for different forward speeds. Set {@literal
		 *                        curve < 0 for left turn or curve > 0 for right turn.} Set curve =
		 *                        e^(-r/w) to get a turn radius r for wheelbase w of your robot.
		 *                        Conversely, turn radius r = -ln(curve)*w for a given value of curve and
		 *                        wheelbase w.
		 */
		public synchronized static void driveMagnitudeAndCurve(double outputMagnitude, double curve) {
			final double leftOutput;
			final double rightOutput;
			synchronized(m_motorOutputLock) {
				if (!kArcadeRatioCurve_Reported) {
					HAL.report(tResourceType.kResourceType_RobotDrive, getNumMotors(),
							tInstances.kRobotDrive_ArcadeRatioCurve);
					kArcadeRatioCurve_Reported = true;
				}
				if (curve < 0) {
					double value = Math.log(-curve);
					double ratio = (value - m_sensitivity) / (value + m_sensitivity);
					if (ratio == 0) {
						ratio = .0000000001;
					}
					leftOutput = outputMagnitude / ratio;
					rightOutput = outputMagnitude;
				} else if (curve > 0) {
					double value = Math.log(curve);
					double ratio = (value - m_sensitivity) / (value + m_sensitivity);
					if (ratio == 0) {
						ratio = .0000000001;
					}
					leftOutput = outputMagnitude;
					rightOutput = outputMagnitude / ratio;
				} else {
					leftOutput = outputMagnitude;
					rightOutput = outputMagnitude;
				}
//				setRobotLeftRightSpeeds(leftOutput, rightOutput, centerBehavior);
				tankDriveRawPercentOutput(leftOutput, rightOutput);
			}
		}
//		  public void drive(double outputMagnitude, double curve) {
//			    final double leftOutput;
//			    final double rightOutput;
//
//			    if (!kArcadeRatioCurve_Reported) {
//			      HAL.report(tResourceType.kResourceType_RobotDrive, getNumMotors(),
//			          tInstances.kRobotDrive_ArcadeRatioCurve);
//			      kArcadeRatioCurve_Reported = true;
//			    }
//			    if (curve < 0) {
//			      double value = Math.log(-curve);
//			      double ratio = (value - m_sensitivity) / (value + m_sensitivity);
//			      if (ratio == 0) {
//			        ratio = .0000000001;
//			      }
//			      leftOutput = outputMagnitude / ratio;
//			      rightOutput = outputMagnitude;
//			    } else if (curve > 0) {
//			      double value = Math.log(curve);
//			      double ratio = (value - m_sensitivity) / (value + m_sensitivity);
//			      if (ratio == 0) {
//			        ratio = .0000000001;
//			      }
//			      leftOutput = outputMagnitude;
//			      rightOutput = outputMagnitude / ratio;
//			    } else {
//			      leftOutput = outputMagnitude;
//			      rightOutput = outputMagnitude;
//			    }
//			    setLeftRightMotorOutputs(leftOutput, rightOutput);
//			  }

		
		
	} //end Driving class

	public class DrvEncoderMgmt {

//		TmFakeable_CANTalon.ConnectedEncoder dem_leftEnc;
//		TmFakeable_CANTalon.ConnectedEncoder dem_rightEnc;
//		TmFakeable_CanTalonSrx dem_leftMotorObj;
//		TmFakeable_CanTalonSrx dem_rightMotorObj;
//		MotorConfigE dem_leftMtrCfg;
//		MotorConfigE dem_rightMtrCfg;
		RoNamedControlsEntry dem_leftMtrCntlEnt;
		RoNamedControlsEntry dem_rightMtrCntlEnt;
		
		int dem_encoderCountsSnapshotLeft = 0;
		int dem_encoderCountsSnapshotRight = 0;
		
		public double dem_leftMotorPolarityFactor;
		public double dem_rightMotorPolarityFactor;
		
		//for 2018, talons should handle this automatically as long as mtr.sensorPhase()
		//  is called before mtr.setInverted()
//		public int dem_leftEncoderPolarityFactor;
//		public int dem_rightEncoderPolarityFactor;
		
		public double dem_leftDistanceAdjustedForPolarity;
		public double dem_rightDistanceAdjustedForPolarity;
		private double dem_leftDistance;
		private double dem_rightDistance;
		public int dem_leftPosition;
		public int dem_rightPosition;
		
		private final Object dem_lock = new Object();
		
		
		/**
		 * much of this stuff should be moved to TmFakeable_CanTalonSrx's ConnectedEncoder - //TODO
		 * @param leftMtr
		 * @param rightMtr
		 */
//		private static final DrvEncoderMgmt dem_instance = new DrvEncoderMgmt();
		protected DrvEncoderMgmt() { //TmFakeable_CanTalonSrx leftMtr, TmFakeable_CanTalonSrx rightMtr) {
//			dem_leftMotorObj = leftMtr;
//			dem_rightMotorObj = rightMtr;
//			drvMtrAliasRightEncoder.setSensorPhase(CtreSensorPhase.MATCHES_MOTOR);
//			drvMtrAliasLeftEncoder.setSensorPhase(CtreSensorPhase.MATCHES_MOTOR);
			dem_leftMtrCntlEnt = drvMtrAliasLeftEncoder.m_namedCntlEnt;
			dem_rightMtrCntlEnt = drvMtrAliasRightEncoder.m_namedCntlEnt;
			if( ! dem_leftMtrCntlEnt.cMtrHasEncoder || ! dem_rightMtrCntlEnt.cMtrHasEncoder) {
				throw TmExceptions.getInstance().new 
							Team744RunTimeEx("motor controllers for DrvEncodeMgmt don't have encoders");
			}
			//Note: x=a?b:c; means if(a){x=b;}else{x=c;} -- conditional operator [744conditionalOp]
			dem_leftMotorPolarityFactor = //dem_leftMtrCfg.getNamedMotorDef().getMultiplierForDrvMtrPolarity();
					(dem_leftMtrCntlEnt.cMtrInversion.equals(RoMtrInversionE.INVERT_MOTOR)) ? -1 : 1;
			dem_rightMotorPolarityFactor = 
					(dem_rightMtrCntlEnt.cMtrInversion.equals(RoMtrInversionE.INVERT_MOTOR)) ? -1 : 1;

			//shouldn't be needed as long as mtr.sensorPhase() is called before mtr.setInverted()
//			dem_leftEncoderPolarityFactor = //leftEnc.getEncoderPolarityFactor();
//					(dem_leftMtrCntlEnt.cMtrEncoderPolarity.equals(EncoderPolarityE.OPPOSITE_OF_MOTOR)) ? -1 : 1;
//			dem_rightEncoderPolarityFactor = //rightEnc.getEncoderPolarityFactor();
//					(dem_rightMtrCntlEnt.cMtrEncoderPolarity.equals(EncoderPolarityE.OPPOSITE_OF_MOTOR)) ? -1 : 1;
		}
		
		public synchronized void update() {
			update(0);
		}
		
		public synchronized void update(int dbgMsgCnt) {
			synchronized(dem_lock) {
				if(dbgMsgCnt>0) {
					String msg="debug";
				}
				//do these in quick succession to keep them in sync with each other
				//these method handle adjusting the reading from the motor object as needed before returning it
				//   (see EncoderCountsCapabilityE enum)			
				dem_leftPosition = getLeftEncoderRdg();
				dem_rightPosition = getRightEncoderRdg();
				
				//we can do the remaining calcs at our leisure
				dem_leftDistance = toDistanceLeft(dem_leftPosition);
				dem_rightDistance = toDistanceRight(dem_rightPosition);
	
				
				//The encoders give negative values when moving forward. the encoder polarity factors handle that for us
				//for 2018, as long as mtr.sensorPhase() is called before mtr.setInverted() the motor inversion
				//  ripples down into the sensor readings.  We no longer need to include encoderPolarityFactor here
//				dem_leftDistanceAdjustedForPolarity = dem_leftDistance * dem_leftEncoderPolarityFactor * dem_leftMotorPolarityFactor;
//				dem_rightDistanceAdjustedForPolarity = dem_rightDistance * dem_rightEncoderPolarityFactor * dem_rightMotorPolarityFactor;
				dem_leftDistanceAdjustedForPolarity = dem_leftDistance * dem_leftMotorPolarityFactor;
				dem_rightDistanceAdjustedForPolarity = dem_rightDistance * dem_rightMotorPolarityFactor;
			}
		}
		
		public synchronized void reset() {
			synchronized(dem_lock) {
				//do these in quick succession to keep them in sync with each other
				drvMtrAliasLeftEncoder.setSelectedSensorPosition(0, CtrePidIdxE.PRIMARY_CLOSED_LOOP,
						Cnst.CTRE_TIMEOUT_MS_NO_WAIT_FOR_COMPLETION);
				drvMtrAliasRightEncoder.setSelectedSensorPosition(0, CtrePidIdxE.PRIMARY_CLOSED_LOOP,
						Cnst.CTRE_TIMEOUT_MS_NO_WAIT_FOR_COMPLETION);
				//assume set is instantaneous? change timeout to wait for completion? //TODO
				dem_encoderCountsSnapshotLeft = drvMtrAliasLeftEncoder.getSelectedSensorPosition(CtrePidIdxE.PRIMARY_CLOSED_LOOP);
				dem_encoderCountsSnapshotRight = drvMtrAliasRightEncoder.getSelectedSensorPosition(CtrePidIdxE.PRIMARY_CLOSED_LOOP);
				if(dem_encoderCountsSnapshotLeft!=0 || dem_encoderCountsSnapshotRight!=0) {
					P.printFrmt(PrtYn.Y, "OOPS!!! - drive train encoders didn't reset to 0!! L-enc=% 6d, R-enc=% 6d",
								dem_encoderCountsSnapshotLeft, dem_encoderCountsSnapshotRight);
					//read again and see if there was just a delay in updating
					int lEnc = drvMtrAliasLeftEncoder.getSelectedSensorPosition(CtrePidIdxE.PRIMARY_CLOSED_LOOP);
					int rEnc = drvMtrAliasRightEncoder.getSelectedSensorPosition(CtrePidIdxE.PRIMARY_CLOSED_LOOP);
					if(lEnc==0 && rEnc==0) {
						P.println(PrtYn.Y, "        - drive train encoders OK after slight delay (are now reset to 0)");
						dem_encoderCountsSnapshotLeft = lEnc;
						dem_encoderCountsSnapshotRight = rEnc;
					}
				}
				this.update();		
				if(dem_encoderCountsSnapshotLeft!=0 || dem_encoderCountsSnapshotRight!=0) {
					//read again and see if there was just a delay in updating
					int lEnc = drvMtrAliasLeftEncoder.getSelectedSensorPosition(CtrePidIdxE.PRIMARY_CLOSED_LOOP);
					int rEnc = drvMtrAliasRightEncoder.getSelectedSensorPosition(CtrePidIdxE.PRIMARY_CLOSED_LOOP);
					if(lEnc==0 && rEnc==0) {
						P.println(PrtYn.Y, "DriveTrain encoders OK after slight delay (are now reset to 0)");
						dem_encoderCountsSnapshotLeft = lEnc;
						dem_encoderCountsSnapshotRight = rEnc;
					}
				}
			}
		}
		public int getLeftEncoderRdg() { //(TmFakeable_CanTalonSrx motorObj) {
			int ans = drvMtrAliasLeftEncoder.getSelectedSensorPosition(CtrePidIdxE.PRIMARY_CLOSED_LOOP);
			if(dem_leftMtrCntlEnt.cMtrEncoderCountsCap.isAbsoluteUsedAsRelative()) { ans -= dem_encoderCountsSnapshotLeft; }
			return ans;
		}
		public int getRightEncoderRdg() { //(TmFakeable_CanTalonSrx motorObj) {
			int ans = drvMtrAliasRightEncoder.getSelectedSensorPosition(CtrePidIdxE.PRIMARY_CLOSED_LOOP);
			if(dem_rightMtrCntlEnt.cMtrEncoderCountsCap.isAbsoluteUsedAsRelative()) { ans -= dem_encoderCountsSnapshotRight; }
			return ans;
		}
		
		public double toDistanceLeft(int count) {
			double ans = 0;
			double ticks = count;
			ans = ticks / dem_leftMtrCntlEnt.cMtrEncoderCountsPerRevolution * 
					dem_leftMtrCntlEnt.cMtrEncoderFeetPerRevolution;
			return ans;
		}
		public double toDistanceRight(int count) {
			double ans = 0;
			double ticks = count;
			ans = ticks / dem_rightMtrCntlEnt.cMtrEncoderCountsPerRevolution * 
								dem_rightMtrCntlEnt.cMtrEncoderFeetPerRevolution;			
			return ans;
		}
		
		public double getLeftDistanceAdjustedForPolarity() {
			return dem_leftDistanceAdjustedForPolarity; //dem_leftPolarityFactor * dem_leftDistance;
		}
		public double getRightDistanceAdjustedForPolarity() {
			return dem_rightDistanceAdjustedForPolarity; //dem_rightPolarityFactor * dem_rightDistance;
		}
		
		public double getAverageDistanceAdjustedForPolarity() {
			double ans;
			synchronized(dem_lock) {
				ans = ((dem_leftDistanceAdjustedForPolarity+dem_rightDistanceAdjustedForPolarity)/2);
			}
			return ans;
		}

	}
	
	public enum DriveSpinDirectionE {
		CLOCKWISE(1.0), COUNTER_CLOCKWISE(-1.0);
		
		private double eDirMultiplier;		
		private DriveSpinDirectionE(double directionMultiplier) {
			eDirMultiplier = directionMultiplier;
		}
		
		public double getSign() { return eDirMultiplier; }
		
	}
	
    public static class DrvNavX {
		static TmNavX m_navX;
		
		public static void doInstantiate() {
			m_navX = new TmNavX(SdKeysE.KEY_NAVX_IMU_IS_CONNECTED, 
								SdKeysE.KEY_NAVX_IMU_IS_CALIBRAATING, 
								SdKeysE.KEY_NAVX_IMU_YAW,
								SdKeysE.KEY_NAVX_IMU_PITCH,
								SdKeysE.KEY_NAVX_IMU_ROLL,
								SdKeysE.KEY_NAVX_IMU_TOTAL_YAW);
		}
		
		public static void doPeriodic() {
			m_navX.postToSdAll();
		}
		
		public static void doCommonInit() {
			m_navX.resetHard();
		}
		
		/*
		 * returns totalYaw angle minus the angle of the last soft reset.
		 * if navX is not connected and/or calibrating returns 0.
		 */
		public static double getLocalAngle() {
			return m_navX.getLocalAngle();
		}
		
		/*
		 * returns totalYaw angle of the navX.
		 * if navX is not connected and/or calibrating returns 0.
		 */
		public static double getGlobalAngle() {
			return m_navX.getGlobalAngle();
		}
		
		/*
		 * resets the local angle. For use before spinning a certain number of degrees.
		 */
		public static void resetNavXSoft() {
			m_navX.resetSoft();
		}
		
		/*
		 * resets the actual angle returned by the NavX. For use at start of auto. 
		 * (ok at start of teleop/disabled since we don't use the NavX in teleop)
		 */
		public static void resetNavXHard() {
			m_navX.resetHard();
		}
		
	}
    	
    public static class DrvGyro {
	    static TmGyroADXRS453SPI g_gyroObj;
	    
	    public static void doInstantiate() {
	        g_gyroObj = new TmGyroADXRS453SPI(RoNamedControlsE.DRV_NAV_GYRO,
	        		SdKeysE.KEY_DRIVE_GYRO_ANGLE,
	        		SdKeysE.KEY_DRIVE_GYRO_RATE,
	        		SdKeysE.KEY_DRIVE_GYRO_TEMP,
	        		SdKeysE.KEY_DRIVE_GYRO_ERROR_CNT,
	        		SdKeysE.KEY_DRIVE_GYRO_RATE_RAW);
	    }
	    public static void doPeriodic() {
	    	g_gyroObj.doPeriodic();
	    	
	    	g_gyroObj.postToSdAngle();
	    	g_gyroObj.postToSdRate();
	    	g_gyroObj.postToSdRateRaw();
	    	g_gyroObj.postToSdTemperature();
	    	g_gyroObj.postToSdErrorCount();
	    	
	    }
	    public static void doCommonInit() {
	    	//if(g_gyroObj != null)
		    g_gyroObj.reset();
	    }
	    
//	    public void postToSdAngle() {
//	    	g_gyroObj.postToSdAngle();
//	    }
	    
    	//these are service methods for use by commands, etc.
    	private static double g_angle = 0; //a debug aid
    	public static double getAngle() {
    		g_angle = g_gyroObj.getAngle();
    		return g_angle;
    	}

    	public static void resetGyro() {
    		g_gyroObj.reset();
    		g_angle = 0;
    	}

    }

	@Override
	public void sssDoDisabledInit() {
		DrvServices.stopAllMotors();
		synchronized(m_cmdLock) {
			setDefaultCommand(null);
//			m_defaultAutonCommand.cancel();
			m_defaultTeleopCommand.cancel();
//			m_alternateTeleopCommand.cancel();
		}
		DrvServices.stopAllMotors();
		DrvServices.motorsPostToSd();
		P.println(PrtYn.N, "ENCODER_COUNTS_2_FEET=" + Cnst.ENCODER_COUNTS_2_FEET);
		getInstance().getDriveEncoderMgmtInstance().reset();
		DrvGyro.doCommonInit();
		DrvNavX.doCommonInit();
	}

	@Override
	public void sssDoAutonomousInit() {
//		dbgDisabledPeriodicMsgCnt = 5;
//		dbgAutonPeriodicMsgCnt = 5;
//		dbgTeleopPeriodicMsgCnt = 5;
		DrvServices.stopAllMotors();
		DrvServices.motorsPostToSd();
		getInstance().getDriveEncoderMgmtInstance().reset();
		DrvGyro.doCommonInit();
		DrvNavX.doCommonInit();
	}

	@Override
	public void sssDoTeleopInit() {
//		dbgDisabledPeriodicMsgCnt = 5;
//		dbgAutonPeriodicMsgCnt = 5;
//		dbgTeleopPeriodicMsgCnt = 5;
		DrvServices.stopAllMotors();
		DrvServices.motorsPostToSd();
		getInstance().getDriveEncoderMgmtInstance().reset();
		 synchronized(m_cmdLock) {
			 setDefaultCommand(null);
//			 m_defaultAutonCommand.cancel();
//			 m_alternateTeleopCommand.cancel();
			 m_defaultTeleopCommand.cancel();
			 setDefaultCommand(m_defaultTeleopCommand);
		 }
		DrvGyro.doCommonInit();
		DrvNavX.doCommonInit();
	}

	@Override
	public void sssDoLwTestInit() {
		// TODO Auto-generated method stub

	}

	@Override
	public void sssDoRobotPeriodic() {
		TmSdMgr.putNumber(SdKeysE.KEY_DRIVE_ENCODER_LEFT, 
				drvMtrAliasLeftEncoder.getSelectedSensorPosition(CtrePidIdxE.PRIMARY_CLOSED_LOOP));
		TmSdMgr.putNumber(SdKeysE.KEY_DRIVE_ENCODER_RIGHT, 
				drvMtrAliasRightEncoder.getSelectedSensorPosition(CtrePidIdxE.PRIMARY_CLOSED_LOOP));
		DrvGyro.doPeriodic();
		DrvNavX.doPeriodic();
	}

	@Override
	public void sssDoDisabledPeriodic() {
//		if(dbgDisabledPeriodicMsgCnt-- > 0) {
//			P.println(this.getClass().getSimpleName() + " - in sssDoDisabledPeriodic()");
//		}

	}

	@Override
	public void sssDoAutonomousPeriodic() {
//		if(dbgAutonPeriodicMsgCnt-- > 0) {
//			P.println(this.getClass().getSimpleName() + " - in sssDoAutonomousPeriodic()");
//		}
		if(true && (m_testTrajectoriesInAuton == true)) {
			localTrajTestMgr.doAutonPeriodic();
		}

	}

	@Override
	public void sssDoTeleopPeriodic() {
//		if(dbgTeleopPeriodicMsgCnt-- > 0) {
//			P.println(this.getClass().getSimpleName() + " - in sssDoTeleopPeriodic()");
//		}
		int thing = 5;
		if(thing>0) {
			thing = 10; //good debugger breakpoint
		}

	}

	@Override
	public void sssDoLwTestPeriodic() {
		// TODO Auto-generated method stub

	}

	@Override
	public void initDefaultCommand() {
		//do in teleopInit instead
//		setDefaultCommand(new TmTCmdDriveWithJoysticks());
	}

	@Override
	public boolean isFakeableItem() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void configAsFake() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isFake() { return false; }

	private boolean m_testTrajectoriesInAuton = false;
	TrajectoryTestsE m_autoSelected = null;
	Local_TestTrajectories localTrajTestMgr = new Local_TestTrajectories();
	
	public void startAutonTrajectoryTest(TrajectoryTestsE trajTest) {
		m_testTrajectoriesInAuton = true;
		m_autoSelected = trajTest;
//		m_testInitDone = false;
		if(true) {
		localTrajTestMgr.doInit();
		}
	}
	public synchronized void stopAutonTrajectoryTest() {
		if(m_testTrajectoriesInAuton) { DrvServices.stopAllMotors(); }
		m_testTrajectoriesInAuton = false;
	}
//	public boolean m_testInitDone = false;
	
	public class Local_TestTrajectories {
		TmTrajGenerator m_autoTraj;
		Timer autoTime;
		int traj_index_left;
		int traj_index_right;
		
		public void doInit() {
			traj_index_left = 0;
			traj_index_right = 0;
			// m_autoSelected = SmartDashboard.getString("Auto Selector",
			// 		kDefaultAuto);
			Waypoint[] points;
			switch (m_autoSelected) {
			case kClosedloopStraightAuto:
				points = new Waypoint[] {
						new Waypoint(0, 0, 0),
						new Waypoint(10, 0, 0)
				};
				m_autoTraj = new TmTrajGenerator(points);
				break;
			case kSwitchTrajAuto:
				points = new Waypoint[] {
						new Waypoint(0, 0, 0),
						new Waypoint(7.8, -3.15, Pathfinder.d2r(-10))
				};
				m_autoTraj = new TmTrajGenerator(points);
				break;
			case kCustomAuto:
				break;
			case kOpenloopStraightAuto:
				break;
			default:
				break;
			}//end switch
			autoTime = new Timer();
			autoTime.reset();
			autoTime.start();
		}

		
		public void doAutonPeriodic() {
			if(true) {
			switch (m_autoSelected) {
			case kCustomAuto:
				if(autoTime.get() < 6) {
					drvMtrRearR.set(ControlMode.Velocity, -1*autoTime.get()*Cnst.FPS_2_RPM*Cnst.RPM_2_NuP100MS);
					drvMtrCenterR.set(ControlMode.Follower, drvMtrRearR.getDeviceID());
					drvMtrFrontRAndEnc.set(ControlMode.Follower, drvMtrRearR.getDeviceID());

					drvMtrRearL.set(ControlMode.Velocity, autoTime.get()*Cnst.FPS_2_RPM*Cnst.RPM_2_NuP100MS);
					drvMtrCenterL.set(ControlMode.Follower, drvMtrRearL.getDeviceID());
					drvMtrFrontLAndEnc.set(ControlMode.Follower, drvMtrRearL.getDeviceID());

//					SmartDashboard.putNumber("Speed Error - Right (native units/100ms)", 
//							drvMtrRearRAndEnc.getClosedLoopError(CtrePidIdxE.PRIMARY_CLOSED_LOOP));
//					SmartDashboard.putNumber("Speed Error - Left (native units/100ms)", 
//							drvMtrRearLAndEnc.getClosedLoopError(CtrePidIdxE.PRIMARY_CLOSED_LOOP));
//					SmartDashboard.putNumber("Speed Error - Right (rpm)", 
//							drvMtrRearRAndEnc.getClosedLoopError(CtrePidIdxE.PRIMARY_CLOSED_LOOP)*Cnst.NuP100MS_2_RPM);
//					SmartDashboard.putNumber("Speed Error - Left (rpm)", 
//							drvMtrRearLAndEnc.getClosedLoopError(CtrePidIdxE.PRIMARY_CLOSED_LOOP)*Cnst.NuP100MS_2_RPM);
					TmSdMgr.putNumber(SdKeysE.KEY_DRIVE_SPEED_ERR_RIGHT_NU,  
							drvMtrRearR.getClosedLoopError(CtrePidIdxE.PRIMARY_CLOSED_LOOP));
					TmSdMgr.putNumber(SdKeysE.KEY_DRIVE_SPEED_ERR_LEFT_NU,
							drvMtrRearL.getClosedLoopError(CtrePidIdxE.PRIMARY_CLOSED_LOOP));
					TmSdMgr.putNumber(SdKeysE.KEY_DRIVE_SPEED_ERR_RIGHT_RPM,
							drvMtrRearR.getClosedLoopError(CtrePidIdxE.PRIMARY_CLOSED_LOOP)*Cnst.NuP100MS_2_RPM);
					TmSdMgr.putNumber(SdKeysE.KEY_DRIVE_SPEED_ERR_LEFT_RPM,
							drvMtrRearL.getClosedLoopError(CtrePidIdxE.PRIMARY_CLOSED_LOOP)*Cnst.NuP100MS_2_RPM);
				}
				else {
					drvMtrRearR.set(ControlMode.PercentOutput,   0);
					drvMtrRearL.set(ControlMode.PercentOutput, 0);
					drvMtrCenterR.set(ControlMode.Follower, drvMtrRearR.getDeviceID());
					drvMtrFrontRAndEnc.set(ControlMode.Follower, drvMtrRearR.getDeviceID());			
					drvMtrCenterL.set(ControlMode.Follower, drvMtrRearL.getDeviceID());
					drvMtrFrontLAndEnc.set(ControlMode.Follower, drvMtrRearL.getDeviceID());
				}
				break;
			case kOpenloopStraightAuto:
				if(autoTime.get() < 3) {
					drvMtrRearR.set(ControlMode.PercentOutput,   -1*Cnst.PERCENT_OUTPUT_USED_TESTING);
					drvMtrRearL.set(ControlMode.PercentOutput, Cnst.PERCENT_OUTPUT_USED_TESTING);
					drvMtrCenterR.set(ControlMode.Follower, drvMtrRearR.getDeviceID());
					drvMtrFrontRAndEnc.set(ControlMode.Follower, drvMtrRearR.getDeviceID());			
					drvMtrCenterL.set(ControlMode.Follower, drvMtrRearL.getDeviceID());
					drvMtrFrontLAndEnc.set(ControlMode.Follower, drvMtrRearL.getDeviceID());					
				}
				else {
					drvMtrRearR.set(ControlMode.PercentOutput,   0);
					drvMtrRearL.set(ControlMode.PercentOutput, 0);
					drvMtrCenterR.set(ControlMode.Follower, drvMtrRearR.getDeviceID());
					drvMtrFrontRAndEnc.set(ControlMode.Follower, drvMtrRearR.getDeviceID());			
					drvMtrCenterL.set(ControlMode.Follower, drvMtrRearL.getDeviceID());
					drvMtrFrontLAndEnc.set(ControlMode.Follower, drvMtrRearL.getDeviceID());
				}

				break;
			case kClosedloopStraightAuto:
				//if before end of trajectory, update to current time in traj
				if(traj_index_left < m_autoTraj.getLeftTraj().length()) {
					while(autoTime.get()>m_autoTraj.getLeftTraj().get(traj_index_left).dt) {
						traj_index_left++;
					}					
				}
				if(traj_index_right < m_autoTraj.getRightTraj().length()) {
					while(autoTime.get()>m_autoTraj.getRightTraj().get(traj_index_right).dt) {
						traj_index_right++;
					}
				}


				drvMtrRearR.set(ControlMode.Velocity, m_autoTraj.getRightTraj().get(traj_index_right).velocity);
				drvMtrCenterR.set(ControlMode.Follower, drvMtrRearR.getDeviceID());
				drvMtrFrontRAndEnc.set(ControlMode.Follower, drvMtrRearR.getDeviceID());

				drvMtrRearL.set(ControlMode.Velocity, m_autoTraj.getLeftTraj().get(traj_index_left).velocity);
				drvMtrCenterL.set(ControlMode.Follower, drvMtrRearL.getDeviceID());
				drvMtrFrontLAndEnc.set(ControlMode.Follower, drvMtrRearL.getDeviceID());

				break;
			case kSwitchTrajAuto:

				break;
//			case kShowTrajectoryFile:
//				break;
				//			case kDefaultAuto:
			default:
				// Put default auto code here
				break;
			}
			}
		}

	}

}
