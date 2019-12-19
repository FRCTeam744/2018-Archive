package org.usfirst.frc.tm744yr18.robot.subsystems;

import org.usfirst.frc.tm744yr18.robot.commands.TmCCmdArmClawStopMotors;
import org.usfirst.frc.tm744yr18.robot.commands.TmCCmdArmRunLiftWithEncoderPositions;
import org.usfirst.frc.tm744yr18.robot.commands.TmTCmdArmClearEncoderOverride;
import org.usfirst.frc.tm744yr18.robot.commands.TmTCmdArmResetLiftEncoder;
import org.usfirst.frc.tm744yr18.robot.commands.TmTCmdArmRunLiftWithXboxJoysticks;
import org.usfirst.frc.tm744yr18.robot.commands.TmTCmdArmSetEncoderOverride;
import org.usfirst.frc.tm744yr18.robot.config.TmHdwrDsCntls.DsNamedControlsE;
import org.usfirst.frc.tm744yr18.robot.config.TmHdwrDsPhys.DsNamedControlsEntry;
import org.usfirst.frc.tm744yr18.robot.config.TmHdwrRoCntls.RoNamedControlsE;
import org.usfirst.frc.tm744yr18.robot.config.TmHdwrRoPhysBase.EncoderIncrVsThingDirE;
import org.usfirst.frc.tm744yr18.robot.config.TmHdwrRoPhysBase.MotorPosPercOutVsThingDirE;
import org.usfirst.frc.tm744yr18.robot.config.TmPrefKeys.PrefKeysE;
import org.usfirst.frc.tm744yr18.robot.config.TmSdKeysI.SdKeysE;
import org.usfirst.frc.tm744yr18.robot.devices.TmFakeable_CanTalonSrx;
import org.usfirst.frc.tm744yr18.robot.devices.TmFakeable_CanTalonSrx.CtreMotorInvertedE;
import org.usfirst.frc.tm744yr18.robot.devices.TmFakeable_CanTalonSrx.CtrePidIdxE;
import org.usfirst.frc.tm744yr18.robot.devices.TmFakeable_CanTalonSrx.CtreSensorPhaseE;
import org.usfirst.frc.tm744yr18.robot.devices.TmFakeable_CanTalonSrx.CtreSlotIdxE;
import org.usfirst.frc.tm744yr18.robot.devices.TmFakeable_CanTalonSrx.EncoderPolarityE;
import org.usfirst.frc.tm744yr18.robot.exceptions.TmExceptions;
import org.usfirst.frc.tm744yr18.robot.devices.TmFakeable_RoDigitalInput;
import org.usfirst.frc.tm744yr18.robot.helpers.TmDriverStation;
import org.usfirst.frc.tm744yr18.robot.helpers.TmSdMgr;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmDsControlUserI;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmMotorAmpsTrackingI;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmRoControlUserI;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmStdSubsystemI;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmToolsI;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmToolsI.P.PrtYn;
import org.usfirst.frc.tm744yr18.robot.subsystems.TmSsArm.ArmOperate.StagesAllowedDirectionsE;
import org.usfirst.frc.tm744yr18.robot.subsystems.TmSsArm.ArmOperate.OperatingModesE;

import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;

import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.command.Subsystem;
import t744opts.Tm744Opts;
import t744opts.Tm744Opts.OptDefaults;

public class TmSsArm extends Subsystem implements TmStdSubsystemI, TmToolsI, TmMotorAmpsTrackingI, 
																TmDsControlUserI, TmRoControlUserI {

	/*
	 * Notes:
	 * Encoder readings SdKey set into ArmLiftMotorInfo mtrCfg.sdKeyEncoderRdg and posted from
	 *  runLiftStageFromJoystick and runStageInServoMode
	 *  
	 *  uses 775Pro motors (VEX)
	 */
	
	
	/*---------------------------------------------------
	 * getInstance stuff
	 *--------------------------------------------------*/
	/** 
	 * handle making the singleton instance of this class and giving
	 * others access to it
	 */
	private static TmSsArm m_instance;

	public static synchronized TmSsArm getInstance() {
		if (m_instance == null) {
			m_instance = new TmSsArm();
		}
		return m_instance;
	}

	private TmSsArm() {}

	private static TmDriverStation m_tds = TmDriverStation.getInstance();
	static ArmClawInfo armClaw;

	static ArmLiftMotorInfo armStage1;
//	static ArmLiftMotorInfo armStage2;
	
	private static Command m_defaultTeleopCommand = null;

//	//we'll try creating a list of supported teleop commands so don't have to do "new" every time we want one
//	public static enum TeleopCmdListE { DO_NOTHING, AT_TOP, AT_BOT, AT_SWITCH, AT_SCALE_HIGH, AT_SCALE_LOW, 
//									RUN_LIFT_WITH_XBOX_JOYSTICKS, SET_LIFT_TO_IDLE_MODE,
//									START_CLAW_GRABBING, START_CLAW_RELEASING, STOP_CLAW, RUN_CLAW_WITH_JOYSTICK,
//									AT_SCALE_MID; }
//	List<Command> teleopCmdsList = new ArrayList<>();

	public static enum LiftPosE { MAX_HEIGHT, TOP, SCALE_HIGH, SCALE_MID, SCALE_LOW, SWITCH, BOTTOM, USER }
	public static class Cnst {
		public static final double STAGE1_MOTOR_PERCENT_OUT_FOR_TWEAKING = 0.3;
		public static final double STAGE2_MOTOR_PERCENT_OUT_FOR_TWEAKING = 0.3; //TODO
		
		public static final double STAGE1_MOTOR_PERCENT_OUT_RAPID = 0.20; //used to control motion at very top and very bottom
		
		//2018-02-18 gym testing: stg2 stayed in place with PercOut -0.265625, stg1 with PercOut 0.1016618
		public static final double STAGE1_MOTOR_PERCENT_OUT_TO_MAINTAIN_POSITION = 0.060; //CONFIG //TODO
		public static final double STAGE2_MOTOR_PERCENT_OUT_TO_MAINTAIN_POSITION = -0.265625; //CONFIG //TODO

		public static final double ARM_CASCADING_SECONDS_TO_MOVE_UP = 0.6;
		public static final double ARM_CASCADING_MAX_INCHES_OF_MOVEMENT = 34.7;
		public static final double ARM_CASCADING_INCHES_PER_REVOLUTION = 4.0;
		public static final double ARM_CASCADING_MAX_REVOLUTIONS_PER_SEC =
				(ARM_CASCADING_MAX_INCHES_OF_MOVEMENT/ARM_CASCADING_INCHES_PER_REVOLUTION) /
				ARM_CASCADING_SECONDS_TO_MOVE_UP; //(34.7/4.0)/0.6 = 14.46 max RPS

		public static final double ARM_STAGE1_ENCODER_MAX_REVS_PER_SECOND = 14.46; //???   //TODO - see spreadsheet
		public static final double ARM_STAGE1_ENCODER_INCHES_PER_REVOLUTION = 4.5; //per Josh S. 4.5in/rev
		public static final double ARM_STAGE1_ENCODER_REVOLUTIONS_PER_INCH = 1/ARM_STAGE1_ENCODER_INCHES_PER_REVOLUTION;
		public static final double ARM_STAGE1_ENCODER_FEET_PER_REVOLUTION = (ARM_STAGE1_ENCODER_INCHES_PER_REVOLUTION/12.0);
		public static final int ARM_STAGE1_ENCODER_COUNTS_PER_REVOLUTION = 4096;
		public static final double ARM_STAGE1_ENCODER_COUNTS_PER_INCH = 
							ARM_STAGE1_ENCODER_COUNTS_PER_REVOLUTION * ARM_STAGE1_ENCODER_REVOLUTIONS_PER_INCH; //910.2
		public static final int ARM_STAGE1_ENCODER_ADJUSTMENT_COUNT = (int)(ARM_STAGE1_ENCODER_COUNTS_PER_INCH/2); 
		
		//c.f. 	armStage1.motorObj.m_namedCntlEnt.cMtrEncoderCountsPerRevolution

		public static final double ARM_STAGE2_ENCODER_MAX_REVS_PER_SECOND = 10.0; //???   //TODO
		public static final double ARM_STAGE2_ENCODER_INCHES_PER_REVOLUTION = 4.0; //per Josh S. 4.0in/rev
		public static final double ARM_STAGE2_ENCODER_REVOLUTIONS_PER_INCH = 1/ARM_STAGE2_ENCODER_INCHES_PER_REVOLUTION;
		public static final double ARM_STAGE2_ENCODER_FEET_PER_REVOLUTION = (ARM_STAGE2_ENCODER_INCHES_PER_REVOLUTION/12.0);
		public static final int ARM_STAGE2_ENCODER_COUNTS_PER_REVOLUTION = 4096;
		public static final double ARM_STAGE2_ENCODER_COUNTS_PER_INCH = 
				ARM_STAGE2_ENCODER_COUNTS_PER_REVOLUTION * ARM_STAGE2_ENCODER_REVOLUTIONS_PER_INCH; //910.2
		public static final int ARM_STAGE2_ENCODER_ADJUSTMENT_COUNT = (int)(ARM_STAGE2_ENCODER_COUNTS_PER_INCH/2); //TODO
		
		public static final int CTRE_TIMEOUT_MS_NO_WAIT_FOR_COMPLETION = 0;
		public static final int CTRE_TIMEOUT_MS_WAIT_FOR_ENCODER_CFG = 20;
		public static final double VOLTAGE_RAMP_TIME_SECS = 0.100; //0.0; //TODO
		
		public static final int STAGE1_ENCODER_MULTIPLIER_FOR_MOVING_UP = 
				(Tm744Opts.OptDefaults.ARM_CASCADING ? +1 : -1);
		public static final int STAGE1_ENCODER_AT_BOTTOM = 0;
		public static final int STAGE1_ENCODER_AT_MAX_HEIGHT = 32191 * STAGE1_ENCODER_MULTIPLIER_FOR_MOVING_UP; //3/5/18 test cascading arm  // * 31050; //gym test 2/20/18: -31050-->34.11in
//		public static final int STAGE1_ENCODER_MAX_HEIGHT_SETPOINT = STAGE1_ENCODER_AT_MAX_HEIGHT; //-2000+STAGE1_ENCODER_AT_MAX_HEIGHT; // = 32191 * STAGE1_ENCODER_MULTIPLIER_FOR_MOVING_UP; //3/5/18 test cascading arm  // * 31050; //gym test 2/20/18: -31050-->34.11in
		
		public static final double STAGE1_ENCODER_MOTOR_MOMENTUM_DISTANCE_UP_INCHES = 1.5;
		public static final double STAGE1_ENCODER_MOTOR_MOMENTUM_DISTANCE_DOWN_INCHES = 2.5;

		public static final int STAGE1_ENCODER_MAX_SAFE_HEIGHT_FULL_SPEED = 26000 * STAGE1_ENCODER_MULTIPLIER_FOR_MOVING_UP;
		public static final int STAGE1_ENCODER_MIN_SAFE_HEIGHT_FULL_SPEED = 6000 * STAGE1_ENCODER_MULTIPLIER_FOR_MOVING_UP;

//		public static final int STAGE1_ENCODER_PRESET_FOR_BUTTON_A = ;
//		public static final int STAGE1_ENCODER_PRESET_FOR_BUTTON_Y = STAGE1_ENCODER_MAX_HEIGHT_SETPOINT;
		
		public static final int STAGE1_ENCODER_AT_TOP = STAGE1_ENCODER_AT_MAX_HEIGHT;
//		public static final int STAGE1_ENCODER_AT_TOP = (int)(STAGE1_ENCODER_AT_BOTTOM +
//													28 * ARM_STAGE1_ENCODER_COUNTS_PER_INCH * 
//													STAGE1_ENCODER_MULTIPLIER_FOR_MOVING_UP); //28in: -25486
		public static final int STAGE1_ENCODER_AT_SWITCH = (int)(STAGE1_ENCODER_AT_BOTTOM +
													13 * ARM_STAGE1_ENCODER_COUNTS_PER_INCH * 
													STAGE1_ENCODER_MULTIPLIER_FOR_MOVING_UP); //12in: -10922
		public static final int STAGE1_ENCODER_AT_SCALE_HIGH = (int)(STAGE1_ENCODER_AT_BOTTOM +
													26 * ARM_STAGE1_ENCODER_COUNTS_PER_INCH * 
													STAGE1_ENCODER_MULTIPLIER_FOR_MOVING_UP); //24in: -21845
		public static final int STAGE1_ENCODER_AT_SCALE_LOW = (int)(STAGE1_ENCODER_AT_BOTTOM +
													24 * ARM_STAGE1_ENCODER_COUNTS_PER_INCH * 
													STAGE1_ENCODER_MULTIPLIER_FOR_MOVING_UP); //18in: -16384
		public static final int STAGE1_ENCODER_AT_SCALE_MID = (int)(STAGE1_ENCODER_AT_BOTTOM +
													29 * ARM_STAGE1_ENCODER_COUNTS_PER_INCH * 
													STAGE1_ENCODER_MULTIPLIER_FOR_MOVING_UP); //22in: ??

		//these still show up in code, but will be ignored
		public static final int STAGE2_ENCODER_MULTIPLIER_FOR_MOVING_UP = 1;
		public static final int STAGE2_ENCODER_AT_BOTTOM = 0;
		public static final int STAGE2_ENCODER_AT_MAX_HEIGHT = STAGE2_ENCODER_MULTIPLIER_FOR_MOVING_UP * 34100; //gym test 2/20/18: 34100-->33.3in
		
		public static final int STAGE2_ENCODER_AT_TOP = (int)(STAGE2_ENCODER_AT_BOTTOM + 
													34 * ARM_STAGE2_ENCODER_COUNTS_PER_INCH * STAGE2_ENCODER_MULTIPLIER_FOR_MOVING_UP); //28in: 28672
		public static final int STAGE2_ENCODER_AT_SWITCH = (int)(STAGE2_ENCODER_AT_BOTTOM + 
													13 * ARM_STAGE2_ENCODER_COUNTS_PER_INCH * STAGE2_ENCODER_MULTIPLIER_FOR_MOVING_UP); //12in: 12288
		public static final int STAGE2_ENCODER_AT_SCALE_HIGH = (int)(STAGE2_ENCODER_AT_BOTTOM + 
													24 * ARM_STAGE2_ENCODER_COUNTS_PER_INCH * STAGE2_ENCODER_MULTIPLIER_FOR_MOVING_UP); //24in: 24576
		public static final int STAGE2_ENCODER_AT_SCALE_LOW = (int)(STAGE2_ENCODER_AT_BOTTOM + 
													18 * ARM_STAGE2_ENCODER_COUNTS_PER_INCH * STAGE2_ENCODER_MULTIPLIER_FOR_MOVING_UP); //18in: 18432
		public static final int STAGE2_ENCODER_AT_SCALE_MID = (int)(STAGE2_ENCODER_AT_BOTTOM + 
													22 * ARM_STAGE2_ENCODER_COUNTS_PER_INCH * STAGE2_ENCODER_MULTIPLIER_FOR_MOVING_UP); //22in: ??
		
		public static final boolean LIMIT_SWITCHES_NORMAL_STATE = false; //per Louis 3/9/18 //true;
		public static final boolean LIMIT_SWITCHES_TRIPPED_STATE = true; //per Louis 3/9/18 //false;
		
	}
	
	
	/*
	 * stage 1 is the extension part of the arm
	 * stage 2 is the piece that runs up and down on the extension
	 */
	
	public enum MotorPercOutVsMotorDirE { POS_PERCENT_OUT_MOVES_MECH_UP, POS_PERCENT_OUT_MOVES_MECH_DOWN; }

	protected static class ArmClawInfo {
		
		public enum ClawStateE { //CONFIG ME //TODO
//			GRABBING(0.5, -0.5),
//			RELEASING(-0.5, 0.5),
			GRABBING(0.20, -0.20),
			RELEASING(-1.0, 1.0),
			BY_JOYSTICK(0, 0),
			OFF(0, 0),
			;
			protected double eMtrPercentOutLeft;
			protected double eMtrPercentOutRight;
			
			private ClawStateE(double leftMtrPercentOut, double rightMtrPercentOut) {
				eMtrPercentOutLeft = leftMtrPercentOut; 
				eMtrPercentOutRight = rightMtrPercentOut;
			}
			
		};
		
		private static ClawStateE clawCurrentState = ClawStateE.OFF;
//		private static ClawStateE clawPrevState = ClawStateE.OFF;

		private static TmFakeable_CanTalonSrx clawMtrLeft;
		private static TmFakeable_CanTalonSrx clawMtrRight;
		protected static double clawJoystickRdgToUse;
		
		private static final Object clawLock = new Object();
		
		ArmClawInfo() {}
		
		public void doRobotInit() {
			clawMtrLeft = new TmFakeable_CanTalonSrx(TmSsArm.getInstance(), RoNamedControlsE.ARM_CLAW_MTR_LEFT);
			clawMtrRight = new TmFakeable_CanTalonSrx(TmSsArm.getInstance(), RoNamedControlsE.ARM_CLAW_MTR_RIGHT);
			configMotor(clawMtrLeft);
			configMotor(clawMtrRight);
			clawMtrLeft.set(ControlMode.PercentOutput, 0.0);
			clawMtrRight.set(ControlMode.PercentOutput, 0.0);
		}	
		
		protected static synchronized void configMotor(TmFakeable_CanTalonSrx mtr) {
			if(mtr.m_namedCntlEnt.cMtrInversion.isInvertMotor()) { mtr.setInverted(CtreMotorInvertedE.INVERTED); }
			
	        /* set the peak and nominal outputs, 12V means full */
	        mtr.configNominalOutputForward(0.0, Cnst.CTRE_TIMEOUT_MS_WAIT_FOR_ENCODER_CFG);
	        mtr.configNominalOutputReverse(0.0, Cnst.CTRE_TIMEOUT_MS_WAIT_FOR_ENCODER_CFG);
	        mtr.configPeakOutputForward(1.0, Cnst.CTRE_TIMEOUT_MS_WAIT_FOR_ENCODER_CFG);
	        mtr.configPeakOutputReverse(-1.0, Cnst.CTRE_TIMEOUT_MS_WAIT_FOR_ENCODER_CFG);
	        
			mtr.configOpenloopRamp(Cnst.VOLTAGE_RAMP_TIME_SECS, Cnst.CTRE_TIMEOUT_MS_NO_WAIT_FOR_COMPLETION);
			
			mtr.configContinuousCurrentLimit(mtr.m_namedCntlEnt.cMaxContinuousAmps.value, Cnst.CTRE_TIMEOUT_MS_NO_WAIT_FOR_COMPLETION);
			mtr.configPeakCurrentLimit(mtr.m_namedCntlEnt.cMaxPeakAmps.value, Cnst.CTRE_TIMEOUT_MS_NO_WAIT_FOR_COMPLETION);
		 	mtr.configPeakCurrentDuration(mtr.m_namedCntlEnt.cMaxPeakAmpsDurationMs.value, Cnst.CTRE_TIMEOUT_MS_NO_WAIT_FOR_COMPLETION);

		}
		
		public static boolean isGrabbing() {
			boolean ans = false;
			synchronized(clawLock) {
//				switch(clawCurrentState) {
//				case BY_JOYSTICK:
//					if(clawMtrLeft.getMotorOutputPercent() > 0) {
//						ans = true;
//					}
//					break;
//				case GRABBING:
//					ans = true;
//					break;
//				case OFF:
//				case RELEASING:
//				default:
//					break;	
//				}
				switch(clawCurrentState) {
				case BY_JOYSTICK:
				case GRABBING:
				case RELEASING:
					if(clawMtrLeft.getMotorOutputPercent() > 0) {
						ans = true;
					}
					break;
				case OFF:
				default:
					break;	
				}
			}
			return ans;
		}
		
		public static boolean isReleasing() {
			boolean ans = false;
			synchronized(clawLock) {
//				switch(clawCurrentState) {
//				case BY_JOYSTICK:
//					if(clawMtrLeft.getMotorOutputPercent() < 0) {
//						ans = true;
//					}
//					break;
//				case RELEASING:
//					ans = true;
//					break;
//				case GRABBING:
//				case OFF:
//				default:
//					break;	
//				}
				switch(clawCurrentState) {
				case BY_JOYSTICK:
				case GRABBING:
				case RELEASING:
					if(clawMtrLeft.getMotorOutputPercent() < 0) {
						ans = true;
					}
					break;
				case OFF:
				default:
					break;	
				}
			}
			return ans;
		}
		
		public static void setState(ClawStateE newState) {
			synchronized(ArmClawInfo.clawLock) {
				if( ! newState.equals(clawCurrentState)) {
					P.println(PrtYn.Y, "setState changing arm claw state from " +
							clawCurrentState.name() + " to " + newState.name());
				}
				switch(newState) {
				case BY_JOYSTICK:
					if(m_tds.isEnabledTeleop()) {
//						clawPrevState = clawCurrentState;
						clawMtrLeft.set(ControlMode.PercentOutput, newState.eMtrPercentOutLeft);
						clawMtrRight.set(ControlMode.PercentOutput, newState.eMtrPercentOutRight);
						clawCurrentState = newState;
					} else {
//						clawPrevState = clawCurrentState;
						clawMtrLeft.set(ControlMode.PercentOutput, 0.0);
						clawMtrRight.set(ControlMode.PercentOutput, 0.0);
						clawCurrentState = newState;
					}
					break;
				case GRABBING:
				case OFF:
				case RELEASING:
//					clawPrevState = clawCurrentState;
					clawMtrLeft.set(ControlMode.PercentOutput, newState.eMtrPercentOutLeft);
					clawMtrRight.set(ControlMode.PercentOutput, newState.eMtrPercentOutRight);
					clawCurrentState = newState;
					break;
				//default:
					//break;
				}
				P.println(PrtYn.Y, "Arm claw motor state set to " + clawCurrentState.name());
//				TmSdMgr.putBoolean(SdKeysE.KEY_ARM_CLAW_GRABBING, ArmClawInfo.isGrabbing());
//				TmSdMgr.putBoolean(SdKeysE.KEY_ARM_CLAW_RELEASING, ArmClawInfo.isReleasing());
			}
		}

//		public enum ClawStateE { 
//			CLAMPED(DoubleSolenoid.Value.kForward),
//			UNCLAMPED(DoubleSolenoid.Value.kReverse),
//			OFF(DoubleSolenoid.Value.kOff),
//			;
//			private final DoubleSolenoid.Value eDirection;
//			public DoubleSolenoid.Value getSolDirection() { return eDirection; }
//			private ClawStateE(DoubleSolenoid.Value dir) { eDirection = dir; }
//		};
//		private static ClawStateE grabberCurrentPosition = ClawStateE.UNCLAMPED;
//		private static ClawStateE grabberPrevPosition = ClawStateE.UNCLAMPED;
//
//		private static TmFakeable_DoubleSolenoid grabberObj;
//		
//		private static final Object grabberLock = new Object();
//		
//		ArmClawInfo() {}
//
//		public static boolean isClamping() {
//			boolean ans;
//			ans = grabberObj.get().equals(ClawStateE.CLAMPED.getSolDirection());
//			return ans;
//		}

	}
	
	protected class ArmLiftMotorInfo {
		ArmLiftMotorInfo() {}
				
		private final Object motorLock = new Object();
		
		OperatingModesE operatingMode = OperatingModesE.IDLE;
		
		StagesAllowedDirectionsE motorDirMode = StagesAllowedDirectionsE.EITHER;
		
		int requestedServoPos; //servo position as requested
		int workingServoPos; //servo position adjusted for limit switches, driver tweaks
		int prevWorkingServoPos; //servo position adjusted for limit switches, driver tweaks
		
		int encoderSnapshot = 0; //actual value from Talon last time encoder was reset
		int encoderReading = 0; //value to be used in code
		int encoderReadingRaw = 0; //value actually read from Talon last time encoder reading requested
		FeedbackDevice encoderType = null; //FeedbackDevice.CTRE_MagEncoder_Relative;
		boolean useEncoder = true;
		
		double maxEncoderCountsPer100ms = 0.0;
		double minEncoderCountsPer100ms = 0.0;
		
//		double joystickReadingToUse;
		
		TmFakeable_CanTalonSrx motorObj = null;
		TmFakeable_CanTalonSrx auxMotorObj = null;
		MotorPercOutVsMotorDirE motorDir = null;
		
		double percentOutToMaintainPosition;
		
		TmFakeable_RoDigitalInput topLimitSwitch = null;
		TmFakeable_RoDigitalInput bottomLimitSwitch = null;
		
		CtreSensorPhaseE sensorPhase;

		SdKeysE sdKeyJoystickReading;
		SdKeysE sdKeyMtrControlMode;
		SdKeysE sdKeyMtrOuputPercent;
		SdKeysE sdKeyAuxMtrControlMode;
		SdKeysE sdKeyAuxMtrOuputPercent;
		SdKeysE sdKeyEncoderRdg;
		SdKeysE sdKeyAllowedDir;
		
		//these are relevant only in servo mode
		SdKeysE sdKeyClosedLoopTarget;
		SdKeysE sdKeyClosedLoopError;
		SdKeysE sdKeyClosedLoopRequestedTarget;
		SdKeysE sdKeyClosedLoopWorkingTarget;
		
		SdKeysE sdKeyOperatingMode;
		
		//joystick to use to run or nudge
		DsNamedControlsE jsNamedControl; //.ARM_TEST_STAGE1_MTR_INPUT
		
		
		public static final int DEFAULT_NUDGE_THROTTLE = 2; //10; //CONFIG ME
		int nudgeThrottle = DEFAULT_NUDGE_THROTTLE; //nudge code runs only once this many calls
		
		int multiplierToMoveServoTargDown;
		int multiplierToMoveServoTargUp;
		
		//check these in limit switch check methods
		int encoderTargMaxHeightAllowed;
		
		int encoderTargAtTop;
		int encoderTargAtBottom;
		int encoderTargAtSwitch;
		int encoderTargAtScaleHigh;
		int encoderTargAtScaleLow;
		int encoderTargAtScaleMid;
		
		int encoderMinTargEitherMotionSafe;
		int encoderMaxTargEitherMotionSafe;
		
		double encoderPidF;
		double encoderPidP;
		double encoderPidI;
		double encoderPidD;
		
		//also see ArmServices.configLiftMotorAndEncoder()
		
		public void configEncoderPositions(int maxHeight, int atTop, int atBottom, int atSwitch, 
				int atScaleHigh, int atScaleLow, int atScaleMid) {
			//check these in limit switch check methods
			encoderTargMaxHeightAllowed = maxHeight;
			
			encoderTargAtTop = atTop;
			encoderTargAtBottom = atBottom;
			encoderTargAtSwitch = atSwitch;
			encoderTargAtScaleHigh = atScaleHigh;
			encoderTargAtScaleLow = atScaleLow;
			encoderTargAtScaleMid = atScaleMid;
		}
		public void configEncoderPid(double kF, double kP, double kI, double kD) {
			encoderPidF = kF;
			encoderPidP = kP;
			encoderPidI = kI;
			encoderPidD = kD;
		}
		
		public int getEncoderReading() {
			int ans = 0;
			synchronized(motorLock) {
				int enc = motorObj.getSelectedSensorPosition(CtrePidIdxE.PRIMARY_CLOSED_LOOP);
				//maxEncoderCountsPer100ms
				double cntsPer100ms = motorObj.getSelectedSensorVelocity(CtrePidIdxE.PRIMARY_CLOSED_LOOP);
				if(cntsPer100ms > maxEncoderCountsPer100ms) { maxEncoderCountsPer100ms = cntsPer100ms; } 
				if(cntsPer100ms < minEncoderCountsPer100ms) { minEncoderCountsPer100ms = cntsPer100ms; } 
				
				encoderReadingRaw = enc;
				encoderReading = enc - encoderSnapshot;
				ans = encoderReading;
				if(true) {
					switch(motorObj.m_namedCntlEnt.cMtrEncoderCountsCap) {
					case ABSOLUTE_USED_AS_RELATIVE:
						ans = enc - encoderSnapshot;
						break;
					case ABSOLUTE_USED_AS_ABSOLUTE:
					case RELATIVE:
					case TBD:
						//untested
						ans = enc;
						break;
						//default:
						//break;		
					}
				}
				return ans;
			}
		}
		
		public void resetEncoder() {
			int ans = 0;
			synchronized(motorLock) {

				int enc = motorObj.getSelectedSensorPosition(CtrePidIdxE.PRIMARY_CLOSED_LOOP);

				//			mtrCfg.encoderType = OptDefaults.ARM_CASCADING_ENCODER_ABSOLUTE ? 
				//									FeedbackDevice.CTRE_MagEncoder_Absolute:
				//									FeedbackDevice.CTRE_MagEncoder_Relative;
				//    		mtrCfg.motorObj.configSelectedFeedbackSensor(mtrCfg.encoderType, CtrePidIdxE.PRIMARY_CLOSED_LOOP, 
				//    												Cnst.CTRE_TIMEOUT_MS_WAIT_FOR_ENCODER_CFG);

				if(true) {
					switch(motorObj.m_namedCntlEnt.cMtrEncoderCountsCap) {
					case ABSOLUTE_USED_AS_RELATIVE:
						encoderSnapshot = enc;
						break;
					case RELATIVE:
						//untested
						motorObj.setSelectedSensorPosition(0, CtrePidIdxE.PRIMARY_CLOSED_LOOP, 
								Cnst.CTRE_TIMEOUT_MS_NO_WAIT_FOR_COMPLETION);
						break;
					case ABSOLUTE_USED_AS_ABSOLUTE:
						//untested
						break;
					case TBD:
						//untested
						break;
						//default:
						//break;		
					}
				}
				//return ans;
			}
		}
		
		public boolean isMovingDown() {
			boolean ans = false;
			switch(motorDir) {
			case POS_PERCENT_OUT_MOVES_MECH_DOWN:
				ans = motorObj.getMotorOutputPercent()>=0;
				break;
			case POS_PERCENT_OUT_MOVES_MECH_UP:
				ans = motorObj.getMotorOutputPercent()<=0;
				break;
			default:
				break;			
			}
			return ans;
		}
		
		public boolean isMovingUp() {
			boolean ans = false;
			ans = willMoveUp(motorObj.getMotorOutputPercent());
			return ans;
		}
		
		public boolean willMoveDown(double percentOut) {
			boolean ans = false;
			switch(motorDir) {
			case POS_PERCENT_OUT_MOVES_MECH_DOWN:
				ans = percentOut>=0;
				break;
			case POS_PERCENT_OUT_MOVES_MECH_UP:
				ans = percentOut<=0;
				break;
			default:
				break;			
			}
			return ans;
		}
		
		public boolean willMoveUp(double percentOut) {
			boolean ans = false;
			switch(motorDir) {
			case POS_PERCENT_OUT_MOVES_MECH_DOWN:
				ans = percentOut<=0;
				break;
			case POS_PERCENT_OUT_MOVES_MECH_UP:
				ans = percentOut>=0;
				break;
			default:
				break;			
			}
			return ans;
		}
		
		public boolean willMoveUp(int posAdjAmt) {
			boolean ans = false;
			if(this.multiplierToMoveServoTargUp < 0) {
				ans = posAdjAmt < 0;
			} else {
				ans = posAdjAmt > 0;
			}
			return ans;
		}
		
		public boolean willMoveDown(int posAdjAmt) {
			boolean ans = false;
			if(this.multiplierToMoveServoTargDown < 0) {
				ans = posAdjAmt < 0;
			} else {
				ans = posAdjAmt > 0;
			}
			return ans;
		}

		public boolean isMotorPercentOutMovingDownRapidly() {
			return isMotorPercentOutMovingDownRapidly(Math.abs(Cnst.STAGE1_MOTOR_PERCENT_OUT_RAPID));
		}
		public boolean isMotorPercentOutMovingDownRapidly(double rapidPercentOut) {
			boolean ans = false;
			switch(motorDir) {
			case POS_PERCENT_OUT_MOVES_MECH_DOWN:
//				if(motorObj.getMotorOutputPercent()>0) {
//					ans = true;
//				}
				if(motorObj.getMotorOutputPercent()>Math.abs(rapidPercentOut)) {
					ans = true;
				}
				break;
			case POS_PERCENT_OUT_MOVES_MECH_UP:
//				if(motorObj.getMotorOutputPercent()<0) {
//					ans = true;
//				}
				if(motorObj.getMotorOutputPercent()<-Math.abs(rapidPercentOut)) {
					ans = true;
				}
				break;
			default:
				break;			
			}
			return ans;
		}
		
		public boolean isMotorPercentOutMovingUpRapidly() {
			return isMotorPercentOutMovingUpRapidly(Math.abs(Cnst.STAGE1_MOTOR_PERCENT_OUT_RAPID));
		}
		public boolean isMotorPercentOutMovingUpRapidly(double rapidPercentOut) {
			boolean ans = false;
			switch(motorDir) {
			case POS_PERCENT_OUT_MOVES_MECH_DOWN:
//				if(motorObj.getMotorOutputPercent()<0) {
//					ans = true;
//				}
				if(motorObj.getMotorOutputPercent()<-Math.abs(rapidPercentOut)) {
					ans = true;
				}
				break;
			case POS_PERCENT_OUT_MOVES_MECH_UP:
//				if(motorObj.getMotorOutputPercent()>0) {
//					ans = true;
//				}
				if(motorObj.getMotorOutputPercent()>Math.abs(rapidPercentOut)) {
					ans = true;
				}
				break;
			default:
				break;			
			}
			return ans;
		}

		public void disableEncoderLimits() {
			synchronized(motorLock) { 
				useEncoder = false;
				P.println(PrtYn.Y, "arm/lift encoder limits - OVERRIDE CANCELLED");
			}
		}
		public void enableEncoderLimits() {
			synchronized(motorLock) { 
				useEncoder = true;
				P.println(PrtYn.Y, "arm/lift encoder limits - OVERRIDE ENABLED");
			}
		}
		public boolean isEnableEncoderLimits() { return useEncoder; }

		public boolean isEncoderNearTop() { 
			return isEncoderNearTop(Cnst.STAGE1_ENCODER_MOTOR_MOMENTUM_DISTANCE_UP_INCHES);
		}
		public boolean isEncoderNearTop(double inches) {
			boolean ans = false;
			double ticks = inches * Cnst.ARM_STAGE1_ENCODER_COUNTS_PER_INCH;
			int enc = getEncoderReading();
			double js = jsNamedControl.getEnt().getAnalog(); //for debug
			int encSs = encoderSnapshot; //for debug
			ans = Tt.isWithinTolerance(enc, Cnst.STAGE1_ENCODER_AT_MAX_HEIGHT, ticks);
			if( ! isEnableEncoderLimits()) { ans = false; }
			return ans;
		}
		
		public boolean isEncoderNearBottom() { 
			return isEncoderNearTop(Cnst.STAGE1_ENCODER_MOTOR_MOMENTUM_DISTANCE_DOWN_INCHES);
		}
		public boolean isEncoderNearBottom(double inches) {
			boolean ans = false;
			double ticks = inches * Cnst.ARM_STAGE1_ENCODER_COUNTS_PER_INCH;
			int enc = getEncoderReading();
			double js = jsNamedControl.getEnt().getAnalog(); //for debug
			int encSs = encoderSnapshot; //for debug
			ans = Tt.isWithinTolerance(enc, Cnst.STAGE1_ENCODER_AT_BOTTOM, ticks);
			if( ! isEnableEncoderLimits()) { ans = false; }
			return ans;
		}		

		
		public boolean checkMotionTopLimitSwitch() {
			boolean ans = false;
			boolean topRaw = topLimitSwitch.get();
			int enc = getEncoderReading();
			double js = jsNamedControl.getEnt().getAnalog(); //for debug
			int encSs = encoderSnapshot; //for debug
			switch(motorDir) {
			case POS_PERCENT_OUT_MOVES_MECH_DOWN:
				if(motorObj.getMotorOutputPercent()<=0) {
					//moving up, check limit switch
					//use joystick name to bypass this stuff for stage2 until it has sane encoder, etc.
					if(//jsNamedControl.equals(DsNamedControlsE.ARM_TEST_STAGE2_MTR_INPUT) && 
							jsNamedControl.getEnt().getAnalog() < -0.5) { //joystick trying to move up (for debug/testing)
						boolean goodbreakpt = true;
					}
					if(topLimitSwitch.get()==Cnst.LIMIT_SWITCHES_TRIPPED_STATE) {
						if(false) { //3/10/18 just want to monitor, don't want to act on
							ans = true;
						}
					}
					if(true) {
						if(multiplierToMoveServoTargUp < 0 ) { //&& jsNamedControl.equals(DsNamedControlsE.ARM_TEST_STAGE1_MTR_INPUT)) {
							if(enc <= encoderTargMaxHeightAllowed) {
								ans = true;
							}
						} else { //if(jsNamedControl.equals(DsNamedControlsE.ARM_TEST_STAGE1_MTR_INPUT)) {
							if(enc >= encoderTargMaxHeightAllowed) {
								ans = true;
							}
						}
					}
				}
				break;
			case POS_PERCENT_OUT_MOVES_MECH_UP:
				if(motorObj.getMotorOutputPercent()>=0) {
					//moving up, check limit switch
					//use joystick name to bypass this stuff for stage2 until it has sane encoder, etc.
					if(//jsNamedControl.equals(DsNamedControlsE.ARM_TEST_STAGE1_MTR_INPUT) && 
							jsNamedControl.getEnt().getAnalog() < -0.5) { //joystick trying to move it up (for debug/testing)
						boolean goodbreakpt = true;
					}
					if(topLimitSwitch.get()==Cnst.LIMIT_SWITCHES_TRIPPED_STATE) {
						if(false) { //3/10/18 just want to monitor, don't want to act on
							ans = true;
						}
					}
					if(true) { //false && motorObj.getMotorOutputPercent() != 0) {
						if(multiplierToMoveServoTargUp < 0 ) {//&& jsNamedControl.equals(DsNamedControlsE.ARM_TEST_STAGE1_MTR_INPUT)) {
							if(enc <= encoderTargMaxHeightAllowed) {
								ans = true;
							}
						} else { //if(jsNamedControl.equals(DsNamedControlsE.ARM_TEST_STAGE1_MTR_INPUT)) {
							if(enc >= encoderTargMaxHeightAllowed) {
								ans = true;
							}
						}
					}
				}
				break;
			default:
				break;			
			}
			if(ans) {
				int junk = 5; //debug breakpoint
				if(loggedTopLimitSw==false) {
					P.println(PrtYn.Y, "Hit top limit switch related to " + this.motorObj.m_namedCntl.name() +
							" lmtSwRaw=" + topRaw + " lmtSwTripped?" + 
							(topRaw==Cnst.LIMIT_SWITCHES_TRIPPED_STATE ? "Y" : "N") +
							" lmtSwFake?" + (topLimitSwitch.isFake()?"Y":"N") +
							" encFake?" + (motorObj.m_encoderBeingFaked?"Y":"N") + 
							" enc=" + enc + " mtrPo=" + motorObj.getMotorOutputPercent() + 
							" encSnap=" + encoderSnapshot);
					if( ! isEnableEncoderLimits()) {
						P.println(PrtYn.Y, "Ignoring top limit switch stuff due to OVERRIDE button");
						ans = false;
					}
				}
				if( ! isEnableEncoderLimits()) { ans = false; }
				loggedTopLimitSw = true;
			} else {
				loggedTopLimitSw = false;
			}
			return ans;
		}
		
		boolean loggedTopLimitSw = false;
		boolean loggedBottomLimitSw = false;

		public double lastJoystickReading;
		
		public boolean checkMotionBottomLimitSwitch() {
			boolean ans = false;
			boolean bot = bottomLimitSwitch.get();
			int enc = getEncoderReading();
			double js = jsNamedControl.getEnt().getAnalog(); //for debug
			int encSs = encoderSnapshot; //for debug
			String msg = "";
			//use joystick name to bypass this stuff for stage2 until it has sane encoder, etc.
			if(//jsNamedControl.equals(DsNamedControlsE.ARM_TEST_STAGE1_MTR_INPUT) && 
					jsNamedControl.getEnt().getAnalog() > 0.5) { //joystick trying to move it down
				boolean goodbreakpt = true;
			}
			switch(motorDir) {
			case POS_PERCENT_OUT_MOVES_MECH_DOWN:
				msg += "posPoMvsDown, ";
				if(motorObj.getMotorOutputPercent()>=0) {
					msg += "curPo>=0, ";
					//moving down, check limit switch
					if(bottomLimitSwitch.get()==Cnst.LIMIT_SWITCHES_TRIPPED_STATE) {
						if(false) { //3/10/18 we will just want to monitor the limit switch...
							msg += "physLmtSwTripped (resetEnc), ";
							ans = true;
							resetEncoder();
						} else {
							msg += "physLmtSwTripped, ";							
						}
					}
					if(true) { //false && motorObj.getMotorOutputPercent()<0) {
						//use joystick name to bypass this stuff for stage2 until it has sane encoder, etc.
						if(multiplierToMoveServoTargDown < 0 ) { //&& jsNamedControl.equals(DsNamedControlsE.ARM_TEST_STAGE1_MTR_INPUT)) {
							msg += "mult4Dn<0, curEnc=" + enc + ", encBot=" + encoderTargAtBottom;
							if(enc <= encoderTargAtBottom) {
								msg += ", enc<=encBot";
								ans = true;
							}
						} else { //if(jsNamedControl.equals(DsNamedControlsE.ARM_TEST_STAGE1_MTR_INPUT)) {
							msg += "mult4Dn>=0, curEnc=" + enc + ", encBot=" + encoderTargAtBottom;
							if(enc >= encoderTargAtBottom) {
								msg += ", enc>=encBot";
								ans = true;
							}
						}
					}
				}
				break;
			case POS_PERCENT_OUT_MOVES_MECH_UP:
				msg += "posPoMvsUp, ";
				if(motorObj.getMotorOutputPercent()<=0) {
					msg += "curPo<=0, ";
					//moving down, check limit switch
					if(bottomLimitSwitch.get()==Cnst.LIMIT_SWITCHES_TRIPPED_STATE) {
//						msg += "physLmtSwTripped";
						if(false) {
							msg += "physLmtSwTripped (resetEnc), ";
							ans = true;
							resetEncoder();
						} else {
							msg += "physLmtSwTripped, ";							
						}
					}
					if(true) { //false && motorObj.getMotorOutputPercent()<0) {
						//use joystick name to bypass this stuff for stage2 until it has sane encoder, etc.
						if(multiplierToMoveServoTargDown < 0 ) { //&& jsNamedControl.equals(DsNamedControlsE.ARM_TEST_STAGE1_MTR_INPUT)) {
							msg += "mult4Dn<0, curEnc=" + enc + ", encBot=" + encoderTargAtBottom;
							if(enc <= encoderTargAtBottom) {
								msg += ", enc<=encBot";
								ans = true;
							}
						} else { //if(jsNamedControl.equals(DsNamedControlsE.ARM_TEST_STAGE1_MTR_INPUT)) {
							msg += "mult4Dn>=0, curEnc=" + enc + ", encBot=" + encoderTargAtBottom;
							if(enc >= encoderTargAtBottom) {
								msg += ", enc>=encBot";
								ans = true;
							}
						}
					}
				}
//				if(ans) { P.println(PrtYn.Y, msg); }
				break;
			default:
				break;			
			}
			if(ans) {
				int junk = 5; //debug breakpoint
				msg += " encSnap=" + encoderSnapshot +
						" encRaw=" + encoderReadingRaw;
				if(loggedBottomLimitSw==false) {
					P.println(PrtYn.Y, msg);
					P.println(PrtYn.Y, "Hit bottom limit switch related to " + this.motorObj.m_namedCntl.name() +
							" lmtSwRaw=" + bot + " lmtSwTripped?" + 
							(bot==Cnst.LIMIT_SWITCHES_TRIPPED_STATE ? "Y" : "N") +
							" lmtSwFake?" + (bottomLimitSwitch.isFake()?"Y":"N") +
							" encFake?" + (motorObj.m_encoderBeingFaked?"Y":"N") + 
							" enc=" + enc + " mtrPo=" + motorObj.getMotorOutputPercent()
//							+ 
//							" encSnap=" + encoderSnapshot +
//							" encRaw=" + encoderReadingRaw
							);
					if( ! isEnableEncoderLimits()) {
						P.println(PrtYn.Y, "Ignoring bottom limit switch stuff due to OVERRIDE button");
						ans = false;
					}
					loggedBottomLimitSw = true;
				}
				if( ! isEnableEncoderLimits()) { ans = false; }
			} else {
				loggedBottomLimitSw = false;
			}
			return ans;
		}
	
		/**
		 * 
		 * @param signedNudgeAmt - positive to nudge arm up, negative to nudge arm down
		 */
		public void nudgeServoSetpoint(int nudgeAmt) {
			synchronized(armStage1.motorLock) {
//				synchronized(armStage2.motorLock) {
					if( operatingMode.equals(OperatingModesE.SERVO) && isEnableEncoderLimits()) {
						int signedNudgeAmt = nudgeAmt;
						if(motorObj.m_namedCntlEnt.cMtrEncoderIncrVsThingDir.equals(
								EncoderIncrVsThingDirE.ENC_INCREASING_WHEN_THING_MOVING_DOWN)) {
							signedNudgeAmt = -nudgeAmt;
						}
						
						if(Tt.isInRange(workingServoPos + signedNudgeAmt,
								encoderTargAtBottom,  encoderTargMaxHeightAllowed)) {
							switch(motorDirMode) {
							case DOWN_ONLY:
								if(willMoveDown(signedNudgeAmt)) {
									workingServoPos += signedNudgeAmt;
								}
								break;
							case EITHER:
								workingServoPos += signedNudgeAmt;
								break;
							case UP_ONLY:
								if(willMoveUp(signedNudgeAmt)) {
									workingServoPos += signedNudgeAmt;
								}
								break;
							default:
								break;
							}
						}
					} //end ifSERVO
//				} //end sync
			} //end sync
		} //end method


		public void handleJoystickPercentOut(double percentOut) {
			synchronized(armStage1.motorLock) {
//				synchronized(armStage2.motorLock) {
					if( operatingMode.equals(OperatingModesE.JOYSTICK) ) {
						if(armStage1.isEnableEncoderLimits()) { //.useEncoder) {
							if(checkMotionTopLimitSwitch()) {
								//moving up and top limit switch tripped
								motorDirMode = StagesAllowedDirectionsE.DOWN_ONLY;
							}
							else if(checkMotionBottomLimitSwitch()) {
								//moving down and bottom limit switch tripped
								motorDirMode = StagesAllowedDirectionsE.UP_ONLY;
							}
							else {
								int min, max;
								int enc = getEncoderReading();
								if(Tt.isInRange(enc, encoderMinTargEitherMotionSafe, encoderMaxTargEitherMotionSafe)) {
									motorDirMode = StagesAllowedDirectionsE.EITHER;
								}
							}
						} else {
							motorDirMode = StagesAllowedDirectionsE.EITHER;
						}
						
						switch(motorDirMode) {
						case DOWN_ONLY:
							if(willMoveDown(percentOut)) {
								motorObj.set(ControlMode.PercentOutput, percentOut);
								//auxMotorObj.set(ControlMode.PercentOutput, percentOut);
							} else {
								if(Tm744Opts.isOptRunStf() || motorObj.m_encoderBeingFaked) {
									motorObj.set(ControlMode.PercentOutput, 
											((Tm744Opts.isOptRunStf() || motorObj.m_encoderBeingFaked) ?
													0.0 : percentOutToMaintainPosition));
									//auxMotorObj.set(ControlMode.PercentOutput, percentOutToMaintainPosition);
								} else {
									//hold to this position until driver or another command moves it down
									motorObj.set(ControlMode.Position, Cnst.STAGE1_ENCODER_AT_MAX_HEIGHT);
								}
							}
							break;
						case EITHER:
							int enc = getEncoderReading();
							if(isEnableEncoderLimits()) {
								if(Tt.isInRange(enc, Cnst.STAGE1_ENCODER_MIN_SAFE_HEIGHT_FULL_SPEED, 
										Cnst.STAGE1_ENCODER_MAX_SAFE_HEIGHT_FULL_SPEED)) {
									motorObj.set(ControlMode.PercentOutput, percentOut);
								} else {
									//getting real close to max top or max bottom; change to half-speed
									motorObj.set(ControlMode.PercentOutput, percentOut * 0.25);
								}
							} else {
								percentOut = Tt.clampToRange(percentOut, -0.75, 0.75);
								motorObj.set(ControlMode.PercentOutput, percentOut);
							}

//							auxMotorObj.set(ControlMode.PercentOutput, percentOut);
							break;
						case UP_ONLY:
							if(willMoveUp(percentOut)) {
								motorObj.set(ControlMode.PercentOutput, percentOut);
//								auxMotorObj.set(ControlMode.PercentOutput, percentOut);
							} else {
								if(Tm744Opts.isOptRunStf() || motorObj.m_encoderBeingFaked) {
									motorObj.set(ControlMode.PercentOutput, 
											((Tm744Opts.isOptRunStf() || motorObj.m_encoderBeingFaked) ?
											0.0 : percentOutToMaintainPosition));
	//								auxMotorObj.set(ControlMode.PercentOutput, percentOutToMaintainPosition);
								} else {
									//hold to this position until driver or another command moves it down
									motorObj.set(ControlMode.Position, Cnst.STAGE1_ENCODER_AT_BOTTOM);
								}
							}
							break;
						default:
							break;						
						}
					}
//				}
			}

		}
		
		public void postFakeListMsgToSd() {
			String msg = "";
			ArmLiftMotorInfo mtrInfo = this;
			if(mtrInfo.motorObj.m_encoderBeingFaked) { msg += "enc=FAKE "; }
			if(mtrInfo.topLimitSwitch.isFake()) { msg += "topLmtSw=FAKE "; }
			if(mtrInfo.bottomLimitSwitch.isFake()) { msg += "botLmtSw=FAKE "; }
			if(mtrInfo.motorObj.isFake()) { msg += "mainMtr=FAKE "; }
			if(mtrInfo.auxMotorObj.isFake()) { msg += "auxMtr=FAKE "; }
			TmSdMgr.putString(SdKeysE.KEY_ARM_FAKE_STUFF_MSG, msg);
		}
		
		private void postLiftInfoToSd() {
			ArmLiftMotorInfo mtrInfo = this;
//			if(cntsPer100ms > maxEncoderCountsPer100ms) { maxEncoderCountsPer100ms = cntsPer100ms; } 
//			if(cntsPer100ms < minEncoderCountsPer100ms) { minEncoderCountsPer100ms = cntsPer100ms; } 
			String maxMinEncVel = String.format("cntsPer100ms: max=% 9.2f, min=% 9.2f", 
					mtrInfo.maxEncoderCountsPer100ms, mtrInfo.minEncoderCountsPer100ms);
			TmSdMgr.putString(SdKeysE.KEY_ARM_ENCODER_STATS_MSG, maxMinEncVel);
			TmSdMgr.putBoolean(SdKeysE.KEY_ARM_ENCODER_OVERRIDE, ! mtrInfo.isEnableEncoderLimits());
			TmSdMgr.putString(mtrInfo.sdKeyOperatingMode, mtrInfo.operatingMode.name());
			TmSdMgr.putString(mtrInfo.sdKeyMtrControlMode, 
					(mtrInfo.motorObj.isFake() ? "FAKE - " : "") +
					mtrInfo.motorObj.getControlMode().name());
			TmSdMgr.putNumber(mtrInfo.sdKeyMtrOuputPercent, mtrInfo.motorObj.getMotorOutputPercent());
			TmSdMgr.putString(mtrInfo.sdKeyAuxMtrControlMode, mtrInfo.auxMotorObj.getControlMode().name());
			TmSdMgr.putNumber(mtrInfo.sdKeyAuxMtrOuputPercent, mtrInfo.auxMotorObj.getMotorOutputPercent());
			int enc = mtrInfo.getEncoderReading();
			TmSdMgr.putNumber(mtrInfo.sdKeyEncoderRdg, enc);
			if(mtrInfo.equals(armStage1)) {
				TmSdMgr.putNumber(SdKeysE.KEY_ARM_STAGE1_ENCODER_RDG_INCHES, 
						enc/Cnst.ARM_STAGE1_ENCODER_COUNTS_PER_INCH);
				TmSdMgr.putNumber(SdKeysE.KEY_ARM_STAGE1_ENCODER_RDG_RAW, mtrInfo.encoderReadingRaw);
				TmSdMgr.putNumber(SdKeysE.KEY_ARM_STAGE1_ENCODER_RDG_SNAPSHOT, mtrInfo.encoderSnapshot);
			}
			TmSdMgr.putString(mtrInfo.sdKeyAllowedDir, mtrInfo.motorDirMode.name());
			if(mtrInfo.operatingMode.equals(OperatingModesE.SERVO)) {
				//not relevant in SERVO mode
				TmSdMgr.putNumber(mtrInfo.sdKeyJoystickReading, 0.0);
				//Talons do all the hard work, we just monitor and display info
				TmSdMgr.putNumber(mtrInfo.sdKeyClosedLoopError, mtrInfo.motorObj.getClosedLoopError(CtrePidIdxE.PRIMARY_CLOSED_LOOP));
				TmSdMgr.putNumber(mtrInfo.sdKeyClosedLoopTarget, mtrInfo.motorObj.getClosedLoopTarget(CtrePidIdxE.PRIMARY_CLOSED_LOOP));
				TmSdMgr.putNumber(mtrInfo.sdKeyClosedLoopWorkingTarget, mtrInfo.workingServoPos);
				TmSdMgr.putNumber(mtrInfo.sdKeyClosedLoopRequestedTarget, mtrInfo.requestedServoPos);		
			} else {
				if(mtrInfo.operatingMode.equals(OperatingModesE.JOYSTICK)) {
					TmSdMgr.putNumber(mtrInfo.sdKeyJoystickReading, mtrInfo.lastJoystickReading);
				} else {
					//not relevant when not in JOYSTICK mode
					TmSdMgr.putNumber(mtrInfo.sdKeyJoystickReading, 0.0);
				}
				//these aren't relevant when not running in SERVO mode
				TmSdMgr.putNumber(mtrInfo.sdKeyClosedLoopTarget, 0); // mtrInfo.motorObj.getClosedLoopTarget(CtrePidIdxE.PRIMARY_CLOSED_LOOP));
				TmSdMgr.putNumber(mtrInfo.sdKeyClosedLoopError, 0); // mtrInfo.motorObj.getClosedLoopError(CtrePidIdxE.PRIMARY_CLOSED_LOOP));
				TmSdMgr.putNumber(mtrInfo.sdKeyClosedLoopRequestedTarget, 0); // mtrInfo.requestedServoPos);
				TmSdMgr.putNumber(mtrInfo.sdKeyClosedLoopWorkingTarget, 0); // mtrInfo.workingServoPos);
			}
//			postLimitSwitchRdgsToSd();
		}


	}
	

	/**
	 * methods, etc. intended to be called from commands, etc., but not from periodic routines
	 * @author JudiA
	 *
	 */
	public static class ArmServices {
		private static final ArmServices sInstance = new ArmServices();
		private ArmServices() {}
		public static ArmServices getInstance() { return sInstance; }
		
//		public static void setClawState(ArmClawInfo.ClawStateE newState) {
//			ArmClawInfo.setState(newState);
//		}
		
		public static OperatingModesE getArmStage1OpMode() { return armStage1.operatingMode; }
		public static int getArmStage1ReqServoPos() { return armStage1.requestedServoPos; }
		public static double getArmStage1MotorPercentOut() { return armStage1.motorObj.getMotorOutputPercent(); }
		
		public static synchronized void stopAllLiftMotors() {
			synchronized(armStage1.motorLock) {
//				synchronized(armStage2.motorLock) {
					armStage1.motorObj.set(ControlMode.PercentOutput, 0.0);
//					armStage1.auxMotorObj.set(ControlMode.PercentOutput, 0.0);
//					armStage2.motorObj.set(ControlMode.PercentOutput, 0.0);
					
					armStage1.operatingMode = OperatingModesE.STOPPED;
//					armStage2.operatingMode = OperatingModesE.STOPPED;
//				}
			}
		}
		
		/**
		 * release motors from STOPPED state without actually running them
		 */
		public static synchronized void idleAllLiftMotors() {
			synchronized(armStage1.motorLock) {
//				synchronized(armStage2.motorLock) {
					armStage1.motorObj.set(ControlMode.PercentOutput, 0.0);
//					armStage1.auxMotorObj.set(ControlMode.PercentOutput, 0.0);
//					armStage2.motorObj.set(ControlMode.PercentOutput, 0.0);
					armStage1.operatingMode = OperatingModesE.IDLE;
//					armStage2.operatingMode = OperatingModesE.IDLE;
//				}
			}
		}

		public static synchronized void requestRunClawWithJoystick(double joystickReading) {
			synchronized(ArmClawInfo.clawLock) {
				double percOut;
				if(m_tds.isEnabledAutonomous()) {
					ArmClawInfo.clawCurrentState = ArmClawInfo.ClawStateE.OFF;
					percOut = 0.0;
					ArmClawInfo.clawMtrLeft.set(ControlMode.PercentOutput, percOut);
					ArmClawInfo.clawMtrRight.set(ControlMode.PercentOutput, -percOut);
				}  
				else if(m_tds.isEnabledTeleop()) {
					if( ! ArmClawInfo.clawCurrentState.equals(ArmClawInfo.ClawStateE.BY_JOYSTICK)) {
						P.println(PrtYn.Y, "arm claw switching from " + ArmClawInfo.clawCurrentState.name() +
								" to " + ArmClawInfo.ClawStateE.BY_JOYSTICK);
					}
					//squaring makes joysticks easier to use
					percOut = - Math.copySign(joystickReading*joystickReading, joystickReading);
					ArmClawInfo.clawCurrentState = ArmClawInfo.ClawStateE.BY_JOYSTICK;
					ArmClawInfo.clawJoystickRdgToUse = joystickReading;
					ArmClawInfo.clawMtrLeft.set(ControlMode.PercentOutput, percOut);
					ArmClawInfo.clawMtrRight.set(ControlMode.PercentOutput, -percOut);
					P.printFrmt(PrtYn.Y, "tower/arm claw: Js=% 5.2f, L-po=% 5.2f, R-po=% 5.2f",
										ArmClawInfo.clawJoystickRdgToUse,
										ArmClawInfo.clawMtrLeft.getMotorOutputPercent(),
										ArmClawInfo.clawMtrRight.getMotorOutputPercent() );
				}
			}
		}
		
		public static synchronized void requestStartClawGrabbing() { 
			synchronized(ArmClawInfo.clawLock) {
				if( ! ArmClawInfo.clawCurrentState.equals(ArmClawInfo.ClawStateE.GRABBING)) {
					P.println(PrtYn.Y, "arm claw switching from " + ArmClawInfo.clawCurrentState.name() +
							" to " + ArmClawInfo.ClawStateE.GRABBING);
				}

				ArmClawInfo.clawCurrentState = ArmClawInfo.ClawStateE.GRABBING;
				ArmClawInfo.clawMtrLeft.set(ControlMode.PercentOutput, ArmClawInfo.ClawStateE.GRABBING.eMtrPercentOutLeft);
				ArmClawInfo.clawMtrRight.set(ControlMode.PercentOutput, ArmClawInfo.ClawStateE.GRABBING.eMtrPercentOutRight);
			}
		}
		
		private static synchronized void requestStartClawGrabbing(double percentOutMagnitude) {
			synchronized(ArmClawInfo.clawLock) {
				if( ! ArmClawInfo.clawCurrentState.equals(ArmClawInfo.ClawStateE.GRABBING)) {
					P.println(PrtYn.Y, "arm claw switching from " + ArmClawInfo.clawCurrentState.name() +
							" to " + ArmClawInfo.ClawStateE.GRABBING + " at |Po|=" + percentOutMagnitude);
				}
				ArmClawInfo.clawCurrentState = ArmClawInfo.ClawStateE.GRABBING;
				ArmClawInfo.clawMtrLeft.set(ControlMode.PercentOutput, percentOutMagnitude);
				ArmClawInfo.clawMtrRight.set(ControlMode.PercentOutput, -percentOutMagnitude); 
			}
		}
		
		public static synchronized void requestStartClawReleasing() { 
			synchronized(ArmClawInfo.clawLock) {
				if( ! ArmClawInfo.clawCurrentState.equals(ArmClawInfo.ClawStateE.RELEASING)) {
					P.println(PrtYn.Y, "arm claw switching from " + ArmClawInfo.clawCurrentState.name() +
							" to " + ArmClawInfo.ClawStateE.RELEASING);
				}
				ArmClawInfo.clawCurrentState = ArmClawInfo.ClawStateE.RELEASING;
				ArmClawInfo.clawMtrLeft.set(ControlMode.PercentOutput, ArmClawInfo.ClawStateE.RELEASING.eMtrPercentOutLeft);
				ArmClawInfo.clawMtrRight.set(ControlMode.PercentOutput, ArmClawInfo.ClawStateE.RELEASING.eMtrPercentOutRight);
			}
		}
		
		private static synchronized void requestStartClawReleasing(double percentOutMagnitude) {//, double timeLimit) {
			synchronized(ArmClawInfo.clawLock) {
				if( ! ArmClawInfo.clawCurrentState.equals(ArmClawInfo.ClawStateE.RELEASING)) {
					P.println(PrtYn.Y, "arm claw switching from " + ArmClawInfo.clawCurrentState.name() +
							" to " + ArmClawInfo.ClawStateE.RELEASING + " at |Po|=" + percentOutMagnitude);
				}
				ArmClawInfo.clawCurrentState = ArmClawInfo.ClawStateE.RELEASING;
				ArmClawInfo.clawMtrLeft.set(ControlMode.PercentOutput, -percentOutMagnitude);
				ArmClawInfo.clawMtrRight.set(ControlMode.PercentOutput, percentOutMagnitude); 
			}
		}
		
		public static synchronized void requestClawMotorsOff() {//, double timeLimit) {
			synchronized(ArmClawInfo.clawLock) {
				if( ! ArmClawInfo.clawCurrentState.equals(ArmClawInfo.ClawStateE.OFF)) {
					P.println(PrtYn.Y, "arm claw switching from " + ArmClawInfo.clawCurrentState.name() +
							" to " + ArmClawInfo.ClawStateE.OFF);
				}
				ArmClawInfo.clawCurrentState = ArmClawInfo.ClawStateE.OFF;
				ArmClawInfo.clawMtrLeft.set(ControlMode.PercentOutput, 0);
				ArmClawInfo.clawMtrRight.set(ControlMode.PercentOutput, 0); 
			}
		}

		public static synchronized void requestResetLiftEncoder() {
			synchronized(armStage1.motorLock) {
				armStage1.resetEncoder();
			}
		}
		
		public static synchronized void requestSetEncoderOverride() {
			synchronized(armStage1.motorLock) {
				armStage1.disableEncoderLimits(); //.useEncoder = false;
			}
		}
		public static synchronized void requestClearEncoderOverride() {
			synchronized(armStage1.motorLock) {
				armStage1.enableEncoderLimits(); //.useEncoder = true;
			}
		}
		
		public static synchronized void requestStage1Servo(int position) {
			synchronized(armStage1.motorLock) {
//				synchronized(armStage2.motorLock) {
					requestServoForSingleStage(armStage1, position);
//				}
			}
		}
//		public static synchronized void requestStage2Servo(int position) {
//			synchronized(armStage1.motorLock) {
//				synchronized(armStage2.motorLock) {
//					requestServoForSingleStage(armStage2, position);
//				}
//			}
//		}
		private static synchronized void requestServoForSingleStage(ArmLiftMotorInfo mtrInfo, int position) {
			synchronized(armStage1.motorLock) {
//				synchronized(armStage2.motorLock) {
				if(Tt.isInRange(position, mtrInfo.encoderTargAtBottom, mtrInfo.encoderTargAtTop)) {
					if( ! mtrInfo.operatingMode.equals(ArmOperate.OperatingModesE.STOPPED))
					{
						mtrInfo.operatingMode = ArmOperate.OperatingModesE.SERVO;
						mtrInfo.motorObj.set(ControlMode.Position, position); /* 50 rotations in either direction */			
						mtrInfo.requestedServoPos = position; //servo position as requested
						mtrInfo.workingServoPos = position; //servo position adjusted for limit switches, driver tweaks
						mtrInfo.prevWorkingServoPos = position - 5; //force != so code will start servo mode

						mtrInfo.operatingMode = ArmOperate.OperatingModesE.SERVO;
					}
				} else {
					throw TmExceptions.getInstance().new Team744RunTimeEx("servo mode requested for illegal servo position: "+
							(mtrInfo.equals(armStage1) ? "stg1: " : "stg2:") + position + " max/min: " +
							mtrInfo.encoderTargAtBottom + "/" + mtrInfo.encoderTargAtTop );

				}
//				}
			}
		}
		public static synchronized void requestBothStagesServo(int stage1Pos, int stage2Pos) {
			synchronized(armStage1.motorLock) {
//				synchronized(armStage2.motorLock) {
				if(armStage1.useEncoder) {
					//isInRange will sort out max/min issues automatically....
					if(Tt.isInRange(stage1Pos, armStage1.encoderTargAtBottom, armStage1.encoderTargAtTop) &&
							true) { //Tt.isInRange(stage2Pos, armStage2.encoderTargAtBottom, armStage2.encoderTargAtTop)) {
						if(armStage1.operatingMode.equals(OperatingModesE.SERVO) && armStage1.requestedServoPos==stage1Pos) {
							//duplicate request, do nothing
						} else {
							requestServoForSingleStage(armStage1, stage1Pos);
						}
//						if(armStage2.operatingMode.equals(OperatingModesE.SERVO) && armStage2.requestedServoPos==stage2Pos) {
//							//duplicate request, do nothing
//						} else {
//							requestServoForSingleStage(armStage2, stage2Pos);
//						}
					} else {
						throw TmExceptions.getInstance().new Team744RunTimeEx("servo mode requested for illegal servo positions: "+
								"stg1: " + stage1Pos + " max/min: " + armStage1.encoderTargAtBottom + "/" + armStage1.encoderTargAtTop +
								"" //" stg2: " + stage2Pos + " max/min: " + armStage2.encoderTargAtBottom + "/" + armStage2.encoderTargAtTop
								);
					}
				} else {
					P.println(PrtYn.Y, "SERVO request REJECTED due to encoder limits OVERRIDE state");
				}
//				}
			}
		}
		public static synchronized void requestRunBothStagesByJoystick() { //double stage1JoystickReading, double stage2JoystickReading) {
			synchronized(armStage1.motorLock) {
//				synchronized(armStage2.motorLock) {
					if( ! (armStage1.operatingMode.equals(OperatingModesE.STOPPED) || 
							false )) { //armStage2.operatingMode.equals(OperatingModesE.STOPPED)   ) ) {
						armStage1.operatingMode = OperatingModesE.JOYSTICK;
//						armStage2.operatingMode = OperatingModesE.JOYSTICK;
					}
					TmSdMgr.putString(armStage1.sdKeyOperatingMode, armStage1.operatingMode.name());
//					TmSdMgr.putString(armStage2.sdKeyOperatingMode, armStage2.operatingMode.name());
//				}
			}
		}

		
		protected static synchronized void configLiftMotorAndEncoder(ArmLiftMotorInfo mtrCfg) {
			
			//also see config methods in ArmLiftMotorInfo class
			
			if(mtrCfg.equals(TmSsArm.armStage1)) {
				mtrCfg.sdKeyJoystickReading =SdKeysE.KEY_ARM_STAGE1_JOYSTICK_RDG;
				mtrCfg.sdKeyMtrControlMode = mtrCfg.motorObj.m_namedCntlEnt.cSdKeyMtrControlMode; //SdKeysE.KEY_ARM_STAGE1_MTR_MODE;
				mtrCfg.sdKeyMtrOuputPercent = mtrCfg.motorObj.m_namedCntlEnt.cSdKeyMtrPercentOut; //SdKeysE.KEY_ARM_STAGE1_MTR_OUTPUT_PERCENT;
				mtrCfg.sdKeyAuxMtrControlMode = mtrCfg.auxMotorObj.m_namedCntlEnt.cSdKeyMtrControlMode; //SdKeysE.KEY_ARM_STAGE1_MTR_MODE;
				mtrCfg.sdKeyAuxMtrOuputPercent = mtrCfg.auxMotorObj.m_namedCntlEnt.cSdKeyMtrPercentOut; //SdKeysE.KEY_ARM_STAGE1_MTR_OUTPUT_PERCENT;
				mtrCfg.sdKeyEncoderRdg =SdKeysE.KEY_ARM_STAGE1_ENCODER_RDG;
				mtrCfg.sdKeyAllowedDir =SdKeysE.KEY_ARM_STAGE1_ALLOWED_DIR;

				//FYI, these aren't relevant when running from joysticks
				mtrCfg.sdKeyClosedLoopTarget =SdKeysE.KEY_ARM_STAGE1_CLOSED_LOOP_TARGET;
				mtrCfg.sdKeyClosedLoopError =SdKeysE.KEY_ARM_STAGE1_CLOSED_LOOP_ERROR;
				mtrCfg.sdKeyClosedLoopRequestedTarget =SdKeysE.KEY_ARM_STAGE1_CLOSED_LOOP_REQUESTED_TARGET;
				mtrCfg.sdKeyClosedLoopWorkingTarget = SdKeysE.KEY_ARM_STAGE1_CLOSED_LOOP_WORKING_TARGET;
				mtrCfg.sdKeyOperatingMode = SdKeysE.KEY_ARM_STAGE1_OP_MODE;
				
				mtrCfg.percentOutToMaintainPosition = Cnst.STAGE1_MOTOR_PERCENT_OUT_TO_MAINTAIN_POSITION;
				
				mtrCfg.jsNamedControl = DsNamedControlsE.ARM_DOUBLE_STAGE_MTR_INPUT; //.ALT_ARM_TEST_STAGE1_MTR_INPUT;
////			  DsNamedControlsE.ALT_ARM_TEST_STAGE1_MTR_INPUT.getEnt().registerAsDsCntlUser(TmSsArm.getInstance());
//				DsNamedControlsE.ARM_DOUBLE_STAGE_MTR_INPUT.getEnt().registerAsDsCntlUser(TmSsArm.getInstance());
			}

			
	        /* choose the sensor and sensor direction */
			//2018-02-06 lab testing: powered motor such that negative joystick readings (positive PercentOutput values) moved the stage1 extender up
			//						  encoder readings decreased as stage1 went up, increased as it went down
			mtrCfg.encoderType = OptDefaults.ARM_CASCADING_ENCODER_ABSOLUTE ? 
					FeedbackDevice.CTRE_MagEncoder_Absolute:
						FeedbackDevice.CTRE_MagEncoder_Relative;
	        mtrCfg.motorObj.configSelectedFeedbackSensor(mtrCfg.encoderType, CtrePidIdxE.PRIMARY_CLOSED_LOOP, Cnst.CTRE_TIMEOUT_MS_WAIT_FOR_ENCODER_CFG);
	        
	        //set sensor phase before doing motor inversion....
			//2018-02-06 lab testing: powered stage1 motor such that negative joystick readings (positive PercentOutput values) moved the 
			//							stage1 extender up
			//						  encoder readings decreased as stage1 went up, increased as it went down
	        mtrCfg.motorObj.setSensorPhase(mtrCfg.sensorPhase);
	        
			/* lets grab the 360 degree position of the MagEncoder's absolute position */
			int absolutePosition = mtrCfg.getEncoderReading() & 0xFFF; /* keep only the bottom12 bits, we don't care about the wrap arounds */
	        /* use the low level API to set the quad encoder signal */
			mtrCfg.motorObj.setSelectedSensorPosition(absolutePosition, CtrePidIdxE.PRIMARY_CLOSED_LOOP, Cnst.CTRE_TIMEOUT_MS_WAIT_FOR_ENCODER_CFG);

			mtrCfg.encoderSnapshot = mtrCfg.getEncoderReading();
			
			/* set sensor phase before doing motor inversions! */
			if(mtrCfg.motorObj.m_namedCntlEnt.cMtrInversion.isInvertMotor()) { 
				mtrCfg.motorObj.setInverted(CtreMotorInvertedE.INVERTED);
			}
			if(mtrCfg.auxMotorObj.m_namedCntlEnt.cMtrInversion.isInvertMotor()) { 
				mtrCfg.auxMotorObj.setInverted(CtreMotorInvertedE.INVERTED);
			}
			
	        /* set the peak and nominal outputs, 12V means full */
	        mtrCfg.motorObj.configNominalOutputForward(0.0, Cnst.CTRE_TIMEOUT_MS_WAIT_FOR_ENCODER_CFG);
	        mtrCfg.motorObj.configNominalOutputReverse(0.0, Cnst.CTRE_TIMEOUT_MS_WAIT_FOR_ENCODER_CFG);
	        mtrCfg.motorObj.configPeakOutputForward(1.0, Cnst.CTRE_TIMEOUT_MS_WAIT_FOR_ENCODER_CFG);
	        mtrCfg.motorObj.configPeakOutputReverse(-1.0, Cnst.CTRE_TIMEOUT_MS_WAIT_FOR_ENCODER_CFG);
	        			
	        mtrCfg.motorObj.configContinuousCurrentLimit(mtrCfg.motorObj.m_namedCntlEnt.cMaxContinuousAmps.value, Cnst.CTRE_TIMEOUT_MS_NO_WAIT_FOR_COMPLETION);
	        mtrCfg.motorObj.configPeakCurrentLimit(mtrCfg.motorObj.m_namedCntlEnt.cMaxPeakAmps.value, Cnst.CTRE_TIMEOUT_MS_NO_WAIT_FOR_COMPLETION);
	        mtrCfg.motorObj.configPeakCurrentDuration(mtrCfg.motorObj.m_namedCntlEnt.cMaxPeakAmpsDurationMs.value, Cnst.CTRE_TIMEOUT_MS_NO_WAIT_FOR_COMPLETION);

			mtrCfg.motorObj.configClosedloopRamp(Cnst.VOLTAGE_RAMP_TIME_SECS, Cnst.CTRE_TIMEOUT_MS_NO_WAIT_FOR_COMPLETION);
			mtrCfg.motorObj.configOpenloopRamp(Cnst.VOLTAGE_RAMP_TIME_SECS, Cnst.CTRE_TIMEOUT_MS_NO_WAIT_FOR_COMPLETION);
	        
	        mtrCfg.auxMotorObj.configNominalOutputForward(0.0, Cnst.CTRE_TIMEOUT_MS_WAIT_FOR_ENCODER_CFG);
	        mtrCfg.auxMotorObj.configNominalOutputReverse(0.0, Cnst.CTRE_TIMEOUT_MS_WAIT_FOR_ENCODER_CFG);
	        mtrCfg.auxMotorObj.configPeakOutputForward(1.0, Cnst.CTRE_TIMEOUT_MS_WAIT_FOR_ENCODER_CFG);
	        mtrCfg.auxMotorObj.configPeakOutputReverse(-1.0, Cnst.CTRE_TIMEOUT_MS_WAIT_FOR_ENCODER_CFG);
	        			
	        mtrCfg.auxMotorObj.configContinuousCurrentLimit(mtrCfg.auxMotorObj.m_namedCntlEnt.cMaxContinuousAmps.value, Cnst.CTRE_TIMEOUT_MS_NO_WAIT_FOR_COMPLETION);
	        mtrCfg.auxMotorObj.configPeakCurrentLimit(mtrCfg.auxMotorObj.m_namedCntlEnt.cMaxPeakAmps.value, Cnst.CTRE_TIMEOUT_MS_NO_WAIT_FOR_COMPLETION);
	        mtrCfg.auxMotorObj.configPeakCurrentDuration(mtrCfg.auxMotorObj.m_namedCntlEnt.cMaxPeakAmpsDurationMs.value, Cnst.CTRE_TIMEOUT_MS_NO_WAIT_FOR_COMPLETION);

			mtrCfg.auxMotorObj.configClosedloopRamp(Cnst.VOLTAGE_RAMP_TIME_SECS, Cnst.CTRE_TIMEOUT_MS_NO_WAIT_FOR_COMPLETION);
			mtrCfg.auxMotorObj.configOpenloopRamp(Cnst.VOLTAGE_RAMP_TIME_SECS, Cnst.CTRE_TIMEOUT_MS_NO_WAIT_FOR_COMPLETION);
	        
	        /* set the allowable closed-loop error,
	         * Closed-Loop output will be neutral within this range.
	         * See Table in Section 17.2.1 for native units per rotation. 
	         */
	        mtrCfg.motorObj.configAllowableClosedloopError(CtreSlotIdxE.PARM_SLOT_0, 0, Cnst.CTRE_TIMEOUT_MS_WAIT_FOR_ENCODER_CFG); /* always servo */
	        /* set closed loop gains in slot0 */
	        mtrCfg.motorObj.config_kF(CtreSlotIdxE.PARM_SLOT_0, mtrCfg.encoderPidF, Cnst.CTRE_TIMEOUT_MS_WAIT_FOR_ENCODER_CFG);
	        mtrCfg.motorObj.config_kP(CtreSlotIdxE.PARM_SLOT_0, mtrCfg.encoderPidP, Cnst.CTRE_TIMEOUT_MS_WAIT_FOR_ENCODER_CFG);
	        mtrCfg.motorObj.config_kI(CtreSlotIdxE.PARM_SLOT_0, mtrCfg.encoderPidI, Cnst.CTRE_TIMEOUT_MS_WAIT_FOR_ENCODER_CFG);
	        mtrCfg.motorObj.config_kD(CtreSlotIdxE.PARM_SLOT_0, mtrCfg.encoderPidD, Cnst.CTRE_TIMEOUT_MS_WAIT_FOR_ENCODER_CFG);

	        //assume the motor is wired to the talon in such a way that the talons can 
	        //be treated as if both motors are running the same direction
	        mtrCfg.auxMotorObj.set(ControlMode.Follower, mtrCfg.motorObj.getDeviceID());
		}
		
		public static String getDefaultArmEncoderSettings() {
			String ans = "";
			ans += "(maxHeight=" + Cnst.STAGE1_ENCODER_AT_MAX_HEIGHT + ")";
			ans += ", top=" + Cnst.STAGE1_ENCODER_AT_TOP;
			ans += ", (scaleHigh=" + Cnst.STAGE1_ENCODER_AT_SCALE_HIGH + ")";
			ans += ", scaleMid=" + Cnst.STAGE1_ENCODER_AT_SCALE_MID;
			ans += ", (scaleLow=" + Cnst.STAGE1_ENCODER_AT_SCALE_LOW + ")";
			ans += ", switch=" + Cnst.STAGE1_ENCODER_AT_SWITCH;
			ans += ", bot=" + Cnst.STAGE1_ENCODER_AT_BOTTOM;
			return ans;
		}

		public static void configDsCntls() {
			DsNamedControlsE.ARM_CLAW_RUN_WITH_JOYSTICK_INPUT.getEnt().registerAsDsCntlUser(TmSsArm.getInstance());
//			DsNamedControlsE.ALT_ARM_TEST_STAGE1_MTR_INPUT.getEnt().registerAsDsCntlUser(TmSsArm.getInstance());
			DsNamedControlsE.ARM_DOUBLE_STAGE_MTR_INPUT.getEnt().registerAsDsCntlUser(TmSsArm.getInstance());
			
//			DsNamedControlsE.ARM_LIFT_OVERRIDE_BOTTOM_LIMIT_WHILE_HELD_BTN.getEnt().registerAsDsCntlUser(TmSsArm.getInstance());

			DsNamedControlsE.ARM_LIFT_OVERRIDE_LIMIT_SWITCHES_BTN.getEnt().whenPressed(TmSsArm.getInstance(), 
					new TmTCmdArmSetEncoderOverride());
			DsNamedControlsE.ARM_LIFT_USE_LIMIT_SWITCHES_BTN.getEnt().whenPressed(TmSsArm.getInstance(), 
					new TmTCmdArmClearEncoderOverride());		
			
			DsNamedControlsE.ARM_LIFT_RESET_ENCODER_BTN.getEnt().whenPressed(TmSsArm.getInstance(), new TmTCmdArmResetLiftEncoder());

			//		DsNamedControlsE.ARM_CLAW_RUN_WITH_JOYSTICK_BTN.getEnt().whenPressed(this, new TmTCmdArmRunClawWithXboxJoystick());
			//		DsNamedControlsE.ARM_LIFT_SET_TO_IDLE_MODE_BTN.getEnt().whenPressed(this, new TmCCmdArmLiftSetToIdleMode());

			DsNamedControlsE.ARM_LIFT_RUN_WITH_JOYSTICKS_BTN.getEnt().whenPressed(TmSsArm.getInstance(), new TmTCmdArmRunLiftWithXboxJoysticks());
			DsNamedControlsE.ARM_LIFT_BOTH_SWITCH_BTN.getEnt().whenPressed(TmSsArm.getInstance(), 
					new TmCCmdArmRunLiftWithEncoderPositions(TmSsArm.LiftPosE.SWITCH));
			DsNamedControlsE.ARM_LIFT_BOTH_SCALE_MID_BTN.getEnt().whenPressed(TmSsArm.getInstance(), 
					new TmCCmdArmRunLiftWithEncoderPositions(TmSsArm.LiftPosE.SCALE_MID));
			DsNamedControlsE.ARM_LIFT_BOTH_SCALE_LOW_BTN.getEnt().whenPressed(TmSsArm.getInstance(), 
					new TmCCmdArmRunLiftWithEncoderPositions(TmSsArm.LiftPosE.SCALE_LOW));
			//		DsNamedControlsE.ARM_CLAW_START_GRABBING_BTN.getEnt().whenPressed(TmSsArm.getInstance(), new TmCCmdArmClawStartGrabbing());
			//		DsNamedControlsE.ARM_CLAW_START_RELEASING_BTN.getEnt().whenPressed(TmSsArm.getInstance(), new TmCCmdArmClawStartReleasing());
			DsNamedControlsE.ARM_CLAW_STOP_MOTORS_BTN.getEnt().whenPressed(TmSsArm.getInstance(), 
					new TmCCmdArmClawStopMotors());
			DsNamedControlsE.ARM_LIFT_BOTH_BOTTOM_BTN.getEnt().whenPressed(TmSsArm.getInstance(), 
					new TmCCmdArmRunLiftWithEncoderPositions(TmSsArm.LiftPosE.BOTTOM)); //Cnst.STAGE1_ENCODER_AT_BOTTOM, Cnst.STAGE2_ENCODER_AT_BOTTOM));
			DsNamedControlsE.ARM_LIFT_BOTH_AT_TOP_BTN.getEnt().whenPressed(TmSsArm.getInstance(), 
					new TmCCmdArmRunLiftWithEncoderPositions(TmSsArm.LiftPosE.TOP)); //Cnst.STAGE1_ENCODER_AT_TOP, Cnst.STAGE2_ENCODER_AT_MAX_HEIGHT));
		}

		public static int getFixedEncoderPosition(LiftPosE liftPosToUse) {
			int ans = 0;
			PrtYn flag = PrtYn.Y;
			switch(liftPosToUse) {
			case BOTTOM:
				ans = Tt.getPreference(PrefKeysE.KEY_TUNE_ARM_LIFT_ENCODER_AT_BOTTOM, Cnst.STAGE1_ENCODER_AT_BOTTOM, flag, PrefCreateE.CREATE_AS_NEEDED);
				ans = Cnst.STAGE1_ENCODER_AT_BOTTOM;
				break;
			case SWITCH:
				ans = Tt.getPreference(PrefKeysE.KEY_TUNE_ARM_LIFT_ENCODER_AT_SWITCH, Cnst.STAGE1_ENCODER_AT_SWITCH, flag, PrefCreateE.CREATE_AS_NEEDED);
				ans = Cnst.STAGE1_ENCODER_AT_SWITCH;
				break;
			case SCALE_LOW:
				ans = Cnst.STAGE1_ENCODER_AT_SCALE_LOW;
				break;
			case SCALE_MID:
				ans = Tt.getPreference(PrefKeysE.KEY_TUNE_ARM_LIFT_ENCODER_AT_MID, Cnst.STAGE1_ENCODER_AT_SCALE_MID, flag, PrefCreateE.CREATE_AS_NEEDED);
				ans = Cnst.STAGE1_ENCODER_AT_SCALE_MID;
				break;
			case SCALE_HIGH:
				ans = Cnst.STAGE1_ENCODER_AT_SCALE_HIGH;
				break;
			case TOP:
				ans = Tt.getPreference(PrefKeysE.KEY_TUNE_ARM_LIFT_ENCODER_AT_TOP, Cnst.STAGE1_ENCODER_AT_TOP, flag, PrefCreateE.CREATE_AS_NEEDED);
				ans = Cnst.STAGE1_ENCODER_AT_TOP;
				break;
			case MAX_HEIGHT:
				ans = Cnst.STAGE1_ENCODER_AT_MAX_HEIGHT;
				break;
			case USER:
				break;
			//default:
				//break;
			}
			P.println(flag, "(ckg prefs) encCur=" + armStage1.getEncoderReading() + " encRaw=" + armStage1.encoderReadingRaw
					+ " encSnap=" + armStage1.encoderSnapshot);
			return ans;
		}

		
	} //end ArmServices

	/**
	 * methods intended to run from periodic routines
	 * @author JudiA
	 *
	 */
	public static class ArmOperate {
		
		public enum OperatingModesE { IDLE, JOYSTICK, SERVO, STOPPED }
		public enum StagesAllowedDirectionsE { EITHER, DOWN_ONLY, UP_ONLY }

		/**
		 * This method is intended for lab testing and early development.  At competitions, should only be 
		 * doing servo mode
		 * @param stage1JoystickReading
		 * @param stage2JoystickReading
		 */
		private static synchronized void runLiftStageFromJoystick(ArmLiftMotorInfo mtrInfo) { //double stage1JoystickReading, double stage2JoystickReading) {
			double percentOut;
			synchronized(armStage1.motorLock) {
//				synchronized(armStage2.motorLock) {					
					//joysticks give negative readings when pushed away from you, but we want that action to run
					//the motors in their forward direction (postive Percent Output values)
					double jsRdg = mtrInfo.jsNamedControl.getEnt().getAnalog();
					mtrInfo.lastJoystickReading = jsRdg;
					//squaring makes joysticks easier to use
					percentOut = -Math.copySign(jsRdg*jsRdg, jsRdg);
					if(mtrInfo.operatingMode.equals(OperatingModesE.IDLE)) {mtrInfo.operatingMode = OperatingModesE.JOYSTICK;}
					
					//check limit switches, encoders, 'useEncoder' flag, etc. and allow/forbid motion accordingly
					mtrInfo.handleJoystickPercentOut(percentOut);
					
//				}
			}
		}
		
		
		/**
		 * use the joysticks on the xbox controller to nudge the setpoints up or down from
		 * their official servo settings.  Use the nudgeThrottle parm to keep "nudges" from being
		 * too drastic.
		 */
	 	private static synchronized void nudgeServo(ArmLiftMotorInfo mtrInfo, double encoderAdjustmentRatio) {
			synchronized(armStage1.motorLock) {
//				synchronized(armStage2.motorLock) { //CONFIG ME
					int nudgeAmt = 0;
					
					//use squaring to make the joysticks easier to use
					double jsReadingRaw = mtrInfo.jsNamedControl.getEnt().getAnalog();
					double jsReading = Math.copySign(jsReadingRaw*jsReadingRaw, jsReadingRaw);
//					//could square again if necessary....
//					//don't need copySign for cubing....
//					double jsReading = (jsReadingRaw*jsReadingRaw*jsReadingRaw); //cube for even easier joystick control

					if(mtrInfo.nudgeThrottle--==0) {
						nudgeAmt = -(int)(jsReading * encoderAdjustmentRatio);// / 25);
						if(nudgeAmt != 0) {
							P.println(PrtYn.Y, "Arm SERVO nudgeAmt=" + nudgeAmt + " for JSrdg % 5.1f");
						}
						mtrInfo.nudgeThrottle = ArmLiftMotorInfo.DEFAULT_NUDGE_THROTTLE;
					}
					
					if( mtrInfo.operatingMode.equals(OperatingModesE.SERVO)) {
						mtrInfo.nudgeServoSetpoint(nudgeAmt);
					}
//				}
			}
		}
		
		private static void runStageInServoMode(ArmLiftMotorInfo mtrInfo, int encoderAdjustmentCount) {
			synchronized(armStage1.motorLock) {
//				synchronized(armStage2.motorLock) {
					//if moving up and top limit switch trips, move servo target down
					//if moving down and bottom limit switch trips, move servo target up
					if(mtrInfo.checkMotionTopLimitSwitch()) {
						//moving up and top limit switch tripped
						mtrInfo.motorDirMode = StagesAllowedDirectionsE.DOWN_ONLY;
						int setptAdj = encoderAdjustmentCount*mtrInfo.multiplierToMoveServoTargDown;
						P.println(PrtYn.Y, "SERVO hit top limit switch: setpoint adj adding " + 
								setptAdj + " po=" + mtrInfo.motorObj.getMotorOutputPercent());
						mtrInfo.workingServoPos += setptAdj;
					}
					else if(mtrInfo.checkMotionBottomLimitSwitch()) {
						//moving down and bottom limit switch tripped
						mtrInfo.motorDirMode = StagesAllowedDirectionsE.UP_ONLY;
						int setptAdj = encoderAdjustmentCount*mtrInfo.multiplierToMoveServoTargUp;
						P.println(PrtYn.Y, "SERVO hit bottom limit switch: setpoint adj adding " + 
								setptAdj + " po=" + mtrInfo.motorObj.getMotorOutputPercent()
								+ " enc=" + mtrInfo.encoderReading
								+ " encRaw=" + mtrInfo.encoderReadingRaw
								+ " encSnap=" + mtrInfo.encoderSnapshot
								);
						mtrInfo.workingServoPos += setptAdj;
					}
					else {
						int enc = mtrInfo.getEncoderReading();
						if(Tt.isInRange(enc, mtrInfo.encoderMinTargEitherMotionSafe, 
								mtrInfo.encoderMaxTargEitherMotionSafe)) {
							mtrInfo.motorDirMode = StagesAllowedDirectionsE.EITHER;
						}
					}
					
					nudgeServo(mtrInfo, encoderAdjustmentCount/10.0);
					
					//don't do the set() unless we really need to change the setpoint 'cause it'll probably
					//mess up the talon's calculations
					if(mtrInfo.prevWorkingServoPos != mtrInfo.workingServoPos) {
						mtrInfo.motorObj.set(ControlMode.Position, 
								mtrInfo.workingServoPos + mtrInfo.encoderSnapshot);
						TmSdMgr.putNumber(SdKeysE.KEY_ARM_STAGE1_ENCODER_TALON_TARGET,
								mtrInfo.workingServoPos + mtrInfo.encoderSnapshot);
					}
					
					//set up for next pass through this routine
					mtrInfo.prevWorkingServoPos = mtrInfo.workingServoPos;
					
					//Talons do all the hard work, we just monitor and display info

//				}
			}
		}
		
		private static String stageStatusStr(ArmLiftMotorInfo stage) {
			String frmtStr = "%-10s, enc=% 7d, encRaw=% 7d, encSnap=% 7d, Po=% 8.5f, " + 
					"ReqTarg=% 7d, WrkTarg=% 7d, ClTarg=% 7d, ClErr= % 8.5f, %s, %s";
			String ans = String.format(frmtStr,
					stage.operatingMode.name(),
					stage.getEncoderReading(),
					stage.encoderReadingRaw, //stage.motorObj.getSelectedSensorPosition(CtrePidIdxE.PRIMARY_CLOSED_LOOP),
					stage.encoderSnapshot,
					stage.motorObj.getMotorOutputPercent(),
					stage.requestedServoPos,
					stage.workingServoPos,
					(stage.motorObj.getControlMode().equals(ControlMode.Position) ?
							stage.motorObj.getClosedLoopTarget(CtrePidIdxE.PRIMARY_CLOSED_LOOP) : 0),
					(stage.motorObj.getControlMode().equals(ControlMode.Position) ?
							stage.motorObj.getClosedLoopError(CtrePidIdxE.PRIMARY_CLOSED_LOOP) : 0),
					stage.motorObj.m_namedCntlEnt.cMtrEncoderCountsCap.name(),
					stage.encoderType.name()
					);
			return ans;
		}
		
		private static void runArmClawLiftWheelsTeleop() {
			if(m_tds.isEnabledTeleop()) {
				synchronized(ArmClawInfo.clawLock) {
					synchronized(armStage1.motorLock) {
						double joystickRdg = DsNamedControlsE.ARM_CLAW_RUN_WITH_JOYSTICK_INPUT.getEnt().getAnalog();
						//grab-while-held takes priority over releasing and joystick
//						if(DsNamedControlsE.ARM_CLAW_GRAB_WHILE_HELD_BTN.getEnt().getButton()) {
						if(DsNamedControlsE.ARM_CLAW_RELEASE_WHILE_HELD_BTN.getEnt().getButton()) {
							//ArmClawInfo.clawCurrentState = ArmClawInfo.ClawStateE.RELEASING;
							ArmClawInfo.clawMtrLeft.set(ControlMode.PercentOutput, ArmClawInfo.ClawStateE.RELEASING.eMtrPercentOutLeft);
							ArmClawInfo.clawMtrRight.set(ControlMode.PercentOutput, ArmClawInfo.ClawStateE.RELEASING.eMtrPercentOutRight);
						}
						else if(Math.abs(joystickRdg)>0.08) {
							//joystick is active
							ArmClawInfo.clawCurrentState = ArmClawInfo.ClawStateE.BY_JOYSTICK;
							//squaring makes joysticks easier to use
							double percOut = - Math.copySign(joystickRdg*joystickRdg, joystickRdg);
							ArmClawInfo.clawMtrLeft.set(ControlMode.PercentOutput, percOut);
							ArmClawInfo.clawMtrRight.set(ControlMode.PercentOutput, -percOut);
						} 
						else if(ArmClawInfo.clawCurrentState.equals(ArmClawInfo.ClawStateE.GRABBING)) {
							ArmClawInfo.clawCurrentState = ArmClawInfo.ClawStateE.GRABBING;
							ArmClawInfo.clawMtrLeft.set(ControlMode.PercentOutput, ArmClawInfo.ClawStateE.GRABBING.eMtrPercentOutLeft);
							ArmClawInfo.clawMtrRight.set(ControlMode.PercentOutput, ArmClawInfo.ClawStateE.GRABBING.eMtrPercentOutRight);
						}
						else if(ArmClawInfo.clawCurrentState.equals(ArmClawInfo.ClawStateE.RELEASING)) {
							ArmClawInfo.clawCurrentState = ArmClawInfo.ClawStateE.RELEASING;
							ArmClawInfo.clawMtrLeft.set(ControlMode.PercentOutput, ArmClawInfo.ClawStateE.RELEASING.eMtrPercentOutLeft);
							ArmClawInfo.clawMtrRight.set(ControlMode.PercentOutput, ArmClawInfo.ClawStateE.RELEASING.eMtrPercentOutRight);
						}
						//releasing takes priority over joystick
////						else if(DsNamedControlsE.ARM_CLAW_RELEASE_WHILE_HELD_BTN.getEnt().getButton()) {
////							ArmClawInfo.clawCurrentState = ArmClawInfo.ClawStateE.RELEASING;
////							ArmClawInfo.clawMtrLeft.set(ControlMode.PercentOutput, ArmClawInfo.ClawStateE.RELEASING.eMtrPercentOutLeft);
////							ArmClawInfo.clawMtrRight.set(ControlMode.PercentOutput, ArmClawInfo.ClawStateE.RELEASING.eMtrPercentOutRight);
////						}
//						else if(Math.abs(joystickRdg)>0.08) {
//							//joystick is active
//							ArmClawInfo.clawCurrentState = ArmClawInfo.ClawStateE.BY_JOYSTICK;
//							//squaring makes joysticks easier to use
//							double percOut = - Math.copySign(joystickRdg*joystickRdg, joystickRdg);
//							ArmClawInfo.clawMtrLeft.set(ControlMode.PercentOutput, percOut);
//							ArmClawInfo.clawMtrRight.set(ControlMode.PercentOutput, -percOut);
//						} 
						else if(armStage1.motorObj.getControlMode().equals(ControlMode.Position) &&
								armStage1.motorObj.getClosedLoopTarget(CtrePidIdxE.PRIMARY_CLOSED_LOOP) == 
										Cnst.STAGE1_ENCODER_AT_BOTTOM) {
							//assume button was pressed to go to bottom. need to stop claw motors.
							ArmClawInfo.clawCurrentState = ArmClawInfo.ClawStateE.OFF;
							ArmClawInfo.clawMtrLeft.set(ControlMode.PercentOutput, 0.0);
							ArmClawInfo.clawMtrRight.set(ControlMode.PercentOutput, 0.0);
						}
						else if(Math.abs(armStage1.motorObj.getMotorOutputPercent()) > 
									Math.abs(armStage1.percentOutToMaintainPosition)) {
							//something is moving the lift. assume we have a cube and want to keep it
							ArmClawInfo.clawCurrentState = ArmClawInfo.ClawStateE.GRABBING;
							ArmClawInfo.clawMtrLeft.set(ControlMode.PercentOutput, ArmClawInfo.ClawStateE.GRABBING.eMtrPercentOutLeft);
							ArmClawInfo.clawMtrRight.set(ControlMode.PercentOutput, ArmClawInfo.ClawStateE.GRABBING.eMtrPercentOutRight);
						}
						else {
							ArmClawInfo.clawCurrentState = ArmClawInfo.ClawStateE.OFF;
							ArmClawInfo.clawMtrLeft.set(ControlMode.PercentOutput, 0.0);
							ArmClawInfo.clawMtrRight.set(ControlMode.PercentOutput, 0.0);
						}
					}//end sync
				}//end sync
			}//end if teleop
		}
		
		private static String prevStg1StatusStr = "";
//		private static String prevStg2StatusStr = "";
		public static void runArm() {
			synchronized(ArmClawInfo.clawLock) {
				synchronized(armStage1.motorLock) {
//					synchronized(armStage2.motorLock) {
						PrtYn flag = PrtYn.N;
						if(Tm744Opts.isInSimulationMode()) { flag = PrtYn.Y; }
						String stg1StatusStr;
//						String stg2StatusStr;
						DsNamedControlsEntry ent = armStage1.jsNamedControl.getEnt();
						double jsRdg = ent.getAnalog();
						if(Math.abs(jsRdg)>0.1) {
							int junk = 5; //good breakpoint
						}
						stg1StatusStr = stageStatusStr(armStage1);
//						stg2StatusStr = stageStatusStr(armStage2);
						if( ! (stg1StatusStr.equals(prevStg1StatusStr) )) { //&& stg2StatusStr.equals(prevStg2StatusStr))) {
							P.println(flag, "(A)stg1: " + stg1StatusStr);
//							P.println(flag, "(A)stg2: " + stg2StatusStr);
							prevStg1StatusStr = stg1StatusStr;
//							prevStg2StatusStr = stg2StatusStr;
						}
//						TmSdMgr.putString(SdKeysE.KEY_ARM_STAGE1_OPERATING_MODE, armStage1.operatingMode.name());
						switch(armStage1.operatingMode) {
						case JOYSTICK:
							runLiftStageFromJoystick(armStage1);
							if(true) {
								//nearTop and nearBottom methods check useEncoder flag
								if(armStage1.isMotorPercentOutMovingUpRapidly() && armStage1.isEncoderNearTop()) {
									ArmServices.idleAllLiftMotors();
								}
								if(armStage1.isMotorPercentOutMovingDownRapidly() && armStage1.isEncoderNearBottom()) {
									ArmServices.idleAllLiftMotors();
								}
							}
							break;
						case SERVO:
							if(armStage1.useEncoder || m_tds.isEnabledAutonomous()) {
								runStageInServoMode(armStage1, Cnst.ARM_STAGE1_ENCODER_ADJUSTMENT_COUNT);
							} else {
								armStage1.operatingMode = ArmOperate.OperatingModesE.JOYSTICK;
							}
							break;
						case IDLE:
						case STOPPED:
						default:
							//assume 'request' methods set the motors to the appropriate values
							break;					
						}	

						stg1StatusStr = stageStatusStr(armStage1);
						//						stg2StatusStr = stageStatusStr(armStage2);
						if( ! (stg1StatusStr.equals(prevStg1StatusStr) )) { //&& stg2StatusStr.equals(prevStg2StatusStr))) {
							P.println(flag, "(B)stg1: " + stg1StatusStr);
							//							P.println(flag, "(B)stg2: " + stg2StatusStr);
							prevStg1StatusStr = stg1StatusStr;
							//							prevStg2StatusStr = stg2StatusStr;
						}

						if(m_tds.isEnabledAutonomous()) {
							switch(ArmClawInfo.clawCurrentState) {
							case BY_JOYSTICK:
								if(m_tds.isEnabledAutonomous()) {
									double percOut = 0.0;
									ArmClawInfo.clawMtrLeft.set(ControlMode.PercentOutput, percOut);
									ArmClawInfo.clawMtrRight.set(ControlMode.PercentOutput, -percOut);
								}  
								else if(m_tds.isEnabledTeleop()) {
									double joystickRdg = DsNamedControlsE.ARM_CLAW_RUN_WITH_JOYSTICK_INPUT.getEnt().getAnalog();
									if(joystickRdg == 0.0) {} //just a good debug breakpoint

									//squaring makes joysticks easier to use
									double percOut = - Math.copySign(joystickRdg*joystickRdg, joystickRdg);
									ArmClawInfo.clawMtrLeft.set(ControlMode.PercentOutput, percOut);
									ArmClawInfo.clawMtrRight.set(ControlMode.PercentOutput, -percOut);
								}
								break;
							case GRABBING:
							case RELEASING:
							case OFF:
								//assume 'request' methods set the motors to the appropriate values
								break;
							default:
								break;
							}
						} else {
							runArmClawLiftWheelsTeleop();
						}
						postLimitSwitchRdgsToSd();
						//					} //sync for stg2
				} //sync for stg1
			} // sync claw
		}

	} //end ArmOperate class
	
	private static void postLiftInfoToSd() {
		armStage1.postLiftInfoToSd();
//		armStage2.postLiftInfoToSd();
		postLimitSwitchRdgsToSd();
	}
	private static void postClawInfoToSd() {
		TmSdMgr.putBoolean(SdKeysE.KEY_ARM_CLAW_GRABBING, ArmClawInfo.isGrabbing());
		TmSdMgr.putBoolean(SdKeysE.KEY_ARM_CLAW_RELEASING, ArmClawInfo.isReleasing());
		TmSdMgr.putNumber(ArmClawInfo.clawMtrLeft.m_namedCntlEnt.cSdKeyMtrPercentOut, //SdKeysE.KEY_ARM_CLAW_MTR_LEFT_PERCENT_OUT, 
				ArmClawInfo.clawMtrLeft.getMotorOutputPercent());
		TmSdMgr.putNumber(SdKeysE.KEY_ARM_CLAW_MTR_RIGHT_PERCENT_OUT, 
				ArmClawInfo.clawMtrRight.getMotorOutputPercent());
	}
	private static void postLimitSwitchRdgsToSd() {
		TmSdMgr.putBoolean(SdKeysE.KEY_ARM_STAGE1_AT_TOP, armStage1.checkMotionTopLimitSwitch());
		TmSdMgr.putBoolean(SdKeysE.KEY_ARM_STAGE1_AT_BOTTOM, armStage1.checkMotionBottomLimitSwitch());
//		TmSdMgr.putBoolean(SdKeysE.KEY_ARM_STAGE2_AT_TOP, armStage2.checkMotionTopLimitSwitch());
//		TmSdMgr.putBoolean(SdKeysE.KEY_ARM_STAGE2_AT_BOTTOM, armStage2.checkMotionBottomLimitSwitch());
		if(true) {
			//doesn't show up on SD but might on Shuffleboard?
			boolean[] limitSw = { 
					armStage1.topLimitSwitch.get(), armStage1.bottomLimitSwitch.get(),
//					armStage2.topLimitSwitch.get(), armStage2.bottomLimitSwitch.get(),
			};
			TmSdMgr.putBooleanArray(SdKeysE.KEY_ARM_LIMIT_SWITCHES, limitSw); //doesn't show up on SD
		}
		TmSdMgr.putBoolean(SdKeysE.KEY_ARM_STAGE1_LIMIT_SWITCH_TOP, 
						armStage1.topLimitSwitch.get()==Cnst.LIMIT_SWITCHES_TRIPPED_STATE);
		TmSdMgr.putBoolean(SdKeysE.KEY_ARM_STAGE1_LIMIT_SWITCH_BOT, 
						armStage1.bottomLimitSwitch.get()==Cnst.LIMIT_SWITCHES_TRIPPED_STATE);
//		TmSdMgr.putBoolean(SdKeysE.KEY_ARM_STAGE2_LIMIT_SWITCH_TOP, 
//						armStage2.topLimitSwitch.get()==Cnst.LIMIT_SWITCHES_TRIPPED_STATE);
//		TmSdMgr.putBoolean(SdKeysE.KEY_ARM_STAGE2_LIMIT_SWITCH_BOT, 
//						armStage2.bottomLimitSwitch.get()==Cnst.LIMIT_SWITCHES_TRIPPED_STATE);
		
		TmSdMgr.putNumber(SdKeysE.KEY_ARM_STAGE1_ENCODER_RDG, armStage1.getEncoderReading());
//		TmSdMgr.putNumber(SdKeysE.KEY_ARM_STAGE2_ENCODER_RDG, armStage2.getEncoderReading());
		TmSdMgr.putNumber(SdKeysE.KEY_ARM_STAGE1_ENCODER_RDG_RAW, 
				armStage1.motorObj.getSelectedSensorPosition(CtrePidIdxE.PRIMARY_CLOSED_LOOP));
//		TmSdMgr.putNumber(SdKeysE.KEY_ARM_STAGE2_ENCODER_RDG_RAW, 
//				armStage2.motorObj.getSelectedSensorPosition(CtrePidIdxE.PRIMARY_CLOSED_LOOP));
	}

	@Override
	public boolean isFakeableItem() { return false; }

	@Override
	public void configAsFake() {}

	@Override
	public boolean isFake() { return false; }

	@Override
	public void sssDoInstantiate() {
		
		armClaw = new ArmClawInfo();
		
		armStage1 = new ArmLiftMotorInfo();
//		armStage2 = new ArmLiftMotorInfo();
		
	}

	@Override
	public void sssDoRobotInit() {

		armStage1.motorObj = new TmFakeable_CanTalonSrx(TmSsArm.getInstance(), RoNamedControlsE.ARM_MTR_STAGE1_EXTENDER);
		armStage1.auxMotorObj = new TmFakeable_CanTalonSrx(TmSsArm.getInstance(), RoNamedControlsE.ARM_MTR_STAGE1_AUXILLIARY);
//		armStage2.motorObj = new TmFakeable_CanTalonSrx(TmSsArm.getInstance(), RoNamedControlsE.ARM_MTR_STAGE2_LIFTER);
//		armStage2.auxMotorObj = null;
		
		armStage1.topLimitSwitch = new TmFakeable_RoDigitalInput(TmSsArm.getInstance(), 
				RoNamedControlsE.ARM_STAGE1_TOP_LIMIT_SWITCH, Cnst.LIMIT_SWITCHES_NORMAL_STATE);
		armStage1.bottomLimitSwitch = new TmFakeable_RoDigitalInput(TmSsArm.getInstance(), 
				RoNamedControlsE.ARM_STAGE1_BOTTOM_LIMIT_SWITCH, Cnst.LIMIT_SWITCHES_NORMAL_STATE);
//		armStage2.topLimitSwitch = new TmFakeable_RoDigitalInput(TmSsArm.getInstance(), 
//				RoNamedControlsE.ARM_STAGE2_TOP_LIMIT_SWITCH, Cnst.LIMIT_SWITCHES_NORMAL_STATE);
//		armStage2.bottomLimitSwitch = new TmFakeable_RoDigitalInput(TmSsArm.getInstance(), 
//				RoNamedControlsE.ARM_STAGE2_BOTTOM_LIMIT_SWITCH, Cnst.LIMIT_SWITCHES_NORMAL_STATE);
		
		armClaw.doRobotInit();
		
		m_logArmInfoThrottle = -1; //<0 disables messages to console, >=0 controls the frequency of the messages

//now using motors instead of pneumatics
//		ArmClawInfo.grabberObj = new TmFakeable_DoubleSolenoid(TmSsArm.getInstance(), RoNamedControlsE.ARM_CLAMP, RoNamedControlsE.ARM_UNCLAMP);
//		ArmClawInfo.grabberObj.set(ArmClawInfo.ClawStateE.UNCLAMPED.getSolDirection());
		
		//2018-02-23 per notes from 2/18/18, stg2 percent output -0.265625 to hold in position
//		armStage1.motorDir = MotorPercOutVsMotorDirE.POS_PERCENT_OUT_MOVES_MECH_UP;
//		armStage2.motorDir = MotorPercOutVsMotorDirE.POS_PERCENT_OUT_MOVES_MECH_DOWN;
		if(armStage1.motorObj.m_namedCntl.getEnt().cMtrPosPercentOutVsThingDir.equals(
				MotorPosPercOutVsThingDirE.POS_MTR_PERCENT_OUT_MOVES_THING_UP)) {
			armStage1.motorDir = MotorPercOutVsMotorDirE.POS_PERCENT_OUT_MOVES_MECH_UP;
		} else {
			armStage1.motorDir = MotorPercOutVsMotorDirE.POS_PERCENT_OUT_MOVES_MECH_DOWN;
		}
//		if(armStage2.motorObj.m_namedCntl.getEnt().cMtrPosPercentOutVsThingDir.equals(
//				MotorPosPercOutVsThingDirE.POS_MTR_PERCENT_OUT_MOVES_THING_UP)) {
//			armStage2.motorDir = MotorPercOutVsMotorDirE.POS_PERCENT_OUT_MOVES_MECH_UP;
//		} else {
//			armStage2.motorDir = MotorPercOutVsMotorDirE.POS_PERCENT_OUT_MOVES_MECH_DOWN;
//		}

//		armStage1.sensorPhase = CtreSensorPhaseE.INVERTED_FROM_MOTOR;
//		armStage2.sensorPhase = CtreSensorPhaseE.INVERTED_FROM_MOTOR;
		if(armStage1.motorObj.m_namedCntlEnt.cMtrEncoderPolarity.equals(EncoderPolarityE.MATCHES_MOTOR)) {
			armStage1.sensorPhase = CtreSensorPhaseE.MATCHES_MOTOR;			
		} else {
			armStage1.sensorPhase = CtreSensorPhaseE.INVERTED_FROM_MOTOR;			
		}
//		if(armStage2.motorObj.m_namedCntlEnt.cMtrEncoderPolarity.equals(EncoderPolarityE.MATCHES_MOTOR)) {
//			armStage2.sensorPhase = CtreSensorPhaseE.MATCHES_MOTOR;			
//		} else {
//			armStage2.sensorPhase = CtreSensorPhaseE.INVERTED_FROM_MOTOR;			
//		}
		
		//when we know we want to change the servo position by some fixed amount (an absolute value), we'll
		//use these multipliers to control the direction of the move
		armStage1.multiplierToMoveServoTargDown = 1;
		armStage1.multiplierToMoveServoTargUp = -1;
//		armStage2.multiplierToMoveServoTargDown = -1;
//		armStage2.multiplierToMoveServoTargUp = 1;
		
		if(armStage1.motorObj.m_namedCntlEnt.cMtrEncoderIncrVsThingDir.equals(
				EncoderIncrVsThingDirE.ENC_INCREASING_WHEN_THING_MOVING_UP)) { //TODO
			armStage1.multiplierToMoveServoTargDown = -1;
			armStage1.multiplierToMoveServoTargUp = 1;
		} else {
			armStage1.multiplierToMoveServoTargDown = 1;
			armStage1.multiplierToMoveServoTargUp = -1;
		}

		//create the hysteresis used to allow "either" motion again after one of the limit
		//switches (or corresponding encoder positions) has been tripped
		if(armStage1.multiplierToMoveServoTargUp > 0) {
			armStage1.encoderMinTargEitherMotionSafe = (int)(Cnst.STAGE1_ENCODER_AT_BOTTOM + 
					2 * Cnst.ARM_STAGE1_ENCODER_COUNTS_PER_INCH);
			armStage1.encoderMaxTargEitherMotionSafe = (int)(Cnst.STAGE1_ENCODER_AT_MAX_HEIGHT - 
					2 * Cnst.ARM_STAGE1_ENCODER_COUNTS_PER_INCH);
		} else {
			armStage1.encoderMaxTargEitherMotionSafe = (int)(Cnst.STAGE1_ENCODER_AT_BOTTOM - 
					2 * Cnst.ARM_STAGE1_ENCODER_COUNTS_PER_INCH);
			armStage1.encoderMinTargEitherMotionSafe = (int)(Cnst.STAGE1_ENCODER_AT_MAX_HEIGHT + 
					2 * Cnst.ARM_STAGE1_ENCODER_COUNTS_PER_INCH);
		}

//		if(armStage2.multiplierToMoveServoTargUp > 0) {
//			armStage2.encoderMinTargEitherMotionSafe = (int)(Cnst.STAGE2_ENCODER_AT_BOTTOM + 
//					2 * Cnst.ARM_STAGE2_ENCODER_COUNTS_PER_INCH);
//			armStage2.encoderMaxTargEitherMotionSafe = (int)(Cnst.STAGE2_ENCODER_AT_MAX_HEIGHT - 
//					2 * Cnst.ARM_STAGE2_ENCODER_COUNTS_PER_INCH);
//		} else {
//			armStage2.encoderMaxTargEitherMotionSafe = (int)(Cnst.STAGE2_ENCODER_AT_BOTTOM - 
//					2 * Cnst.ARM_STAGE2_ENCODER_COUNTS_PER_INCH);
//			armStage2.encoderMinTargEitherMotionSafe = (int)(Cnst.STAGE2_ENCODER_AT_MAX_HEIGHT + 
//					2 * Cnst.ARM_STAGE2_ENCODER_COUNTS_PER_INCH);
//		}

		
		armStage1.configEncoderPositions(
				Cnst.STAGE1_ENCODER_AT_MAX_HEIGHT,
				Cnst.STAGE1_ENCODER_AT_TOP,
				Cnst.STAGE1_ENCODER_AT_BOTTOM,
				Cnst.STAGE1_ENCODER_AT_SWITCH,
				Cnst.STAGE1_ENCODER_AT_SCALE_HIGH,
				Cnst.STAGE1_ENCODER_AT_SCALE_LOW,
				Cnst.STAGE1_ENCODER_AT_SCALE_MID);
//		armStage2.configEncoderPositions(
//				Cnst.STAGE2_ENCODER_AT_MAX_HEIGHT,
//				Cnst.STAGE2_ENCODER_AT_TOP,
//				Cnst.STAGE2_ENCODER_AT_BOTTOM,
//				Cnst.STAGE2_ENCODER_AT_SWITCH,
//				Cnst.STAGE2_ENCODER_AT_SCALE_HIGH,
//				Cnst.STAGE2_ENCODER_AT_SCALE_LOW,
//				Cnst.STAGE2_ENCODER_AT_SCALE_MID);
		
		armStage1.configEncoderPid(0.0, 0.1, 0.0, 0.0); //F, P, I, D
//		armStage2.configEncoderPid(0.0, 0.1, 0.0, 0.0); //F, P, I, D
		
		ArmServices.configLiftMotorAndEncoder(armStage1);
//		ArmServices.configLiftMotorAndEncoder(armStage2);
		
//		P.printFrmt(PrtYn.Y, "at boot, arm/tower encoders (raw snapshots): arm-L-enc=% 6d, arm-R-enc=% 6d\n", 
//				armStage1.encoderSnapshot, armStage2.encoderSnapshot);
		P.printFrmt(PrtYn.Y, "at boot, arm/tower encoder (raw snapshot): enc=% 6d\n", 
				armStage1.encoderSnapshot);
		
		armStage1.motorObj.m_fakeParms.configFakeEncoder(true, armStage1.encoderTargAtBottom, 
				armStage1.encoderTargMaxHeightAllowed);
		armStage1.resetEncoder();
		
		ArmServices.configDsCntls();
		
	 	ArmServices.idleAllLiftMotors();
	 	
		String ans = Tt.getPreference(PrefKeysE.KEY_FYI_ARM_LIFT_ENCODER_DEFAULTS, ArmServices.getDefaultArmEncoderSettings(), PrtYn.Y, PrefCreateE.CREATE_AS_NEEDED);
	}
	
	@Override
	public void sssDoDisabledInit() {
		logArmInfo(true);
		ArmServices.idleAllLiftMotors();
		ArmServices.requestClawMotorsOff();
		armStage1.postFakeListMsgToSd();
		postLiftInfoToSd();
		postClawInfoToSd();
//		armStage1.resetEncoder();
//		armStage2.resetEncoder();

	}

	@Override
	public void sssDoAutonomousInit() {
		logArmInfo(true);
		ArmServices.idleAllLiftMotors();
		ArmServices.requestClawMotorsOff();
		postLiftInfoToSd();
		postClawInfoToSd();
//		armStage1.resetEncoder();
//		armStage2.resetEncoder();
	}

	@Override
	public void sssDoTeleopInit() {
		logArmInfo(true);
		ArmServices.idleAllLiftMotors();
		ArmServices.requestClawMotorsOff();
//		ArmClawInfo.grabberObj.set(ArmClawInfo.ClawStateE.UNCLAMPED.getSolDirection());
//		TmSdMgr.putBoolean(SdKeysE.KEY_ARM_CLAW_GRABBING, ArmClawInfo.isGrabbing());
//		TmSdMgr.putBoolean(SdKeysE.KEY_ARM_CLAW_RELEASING, ArmClawInfo.isReleasing());
		postLiftInfoToSd();
		postClawInfoToSd();
		if(m_defaultTeleopCommand != null) { m_defaultTeleopCommand.start(); }
//		armStage1.resetEncoder();
//		armStage2.resetEncoder();
	}

	@Override
	public void sssDoLwTestInit() {
		// TODO Auto-generated method stub

	}

	@Override
	public void sssDoRobotPeriodic() {
		
	}

	@Override
	public void sssDoDisabledPeriodic() {
		postLiftInfoToSd();
		postClawInfoToSd();
//		TmSdMgr.putBoolean(SdKeysE.KEY_ARM_CLAW_GRABBING, ArmClawInfo.isGrabbing());
//		TmSdMgr.putBoolean(SdKeysE.KEY_ARM_CLAW_RELEASING, ArmClawInfo.isReleasing());
//		TmSdMgr.putNumber(SdKeysE.KEY_ARM_CLAW_MTR_LEFT_PERCENT_OUT, 
//							ArmClawInfo.clawMtrLeft.getMotorOutputPercent());
//		TmSdMgr.putNumber(SdKeysE.KEY_ARM_CLAW_MTR_RIGHT_PERCENT_OUT, 
//							ArmClawInfo.clawMtrRight.getMotorOutputPercent());
	}

	@Override
	public void sssDoAutonomousPeriodic() {
		ArmOperate.runArm();
		logArmInfo();
		postLiftInfoToSd();
		postClawInfoToSd();
//		TmSdMgr.putBoolean(SdKeysE.KEY_ARM_CLAW_GRABBING, ArmClawInfo.isGrabbing());
//		TmSdMgr.putBoolean(SdKeysE.KEY_ARM_CLAW_RELEASING, ArmClawInfo.isReleasing());
//		TmSdMgr.putNumber(SdKeysE.KEY_ARM_CLAW_MTR_LEFT_PERCENT_OUT, 
//				ArmClawInfo.clawMtrLeft.getMotorOutputPercent());
//		TmSdMgr.putNumber(SdKeysE.KEY_ARM_CLAW_MTR_RIGHT_PERCENT_OUT, 
//				ArmClawInfo.clawMtrRight.getMotorOutputPercent());
	}

	@Override
	public void sssDoTeleopPeriodic() {
		ArmOperate.runArm();
		logArmInfo();
		postLiftInfoToSd();
		postClawInfoToSd();
//		TmSdMgr.putBoolean(SdKeysE.KEY_ARM_CLAW_GRABBING, ArmClawInfo.isGrabbing());
//		TmSdMgr.putBoolean(SdKeysE.KEY_ARM_CLAW_RELEASING, ArmClawInfo.isReleasing());
//		TmSdMgr.putNumber(SdKeysE.KEY_ARM_CLAW_MTR_LEFT_PERCENT_OUT, 
//				ArmClawInfo.clawMtrLeft.getMotorOutputPercent());
//		TmSdMgr.putNumber(SdKeysE.KEY_ARM_CLAW_MTR_RIGHT_PERCENT_OUT, 
//				ArmClawInfo.clawMtrRight.getMotorOutputPercent());
	}

	@Override
	public void sssDoLwTestPeriodic() {
		// TODO Auto-generated method stub

	}

	@Override
	public void initDefaultCommand() {
	}

	int m_logArmInfoThrottle = 0;
	String m_prevLogArmInfoStr = "";
	public void logArmInfo() { logArmInfo(false); }
	public void logArmInfo(boolean forcePrint) {
		String ans;
		if(m_logArmInfoThrottle--==0 || forcePrint) {
			ans = String.format("Arm: %-10s: s1=%-10s, s1Po=% 1.4f, s1Aux=%-10s, s1AuxPo=% 1.4f, s1Enc=% 4d, " + 
									"s1Targ=% 4d, s1top=%s, s1bot=%s, encRaw=% 7d, encSnap=% 7d",
					armStage1.operatingMode.name(), 
					armStage1.motorObj.getControlMode().name(),	
					armStage1.motorObj.getMotorOutputPercent(),	 
					armStage1.auxMotorObj.getControlMode().name(),	
					armStage1.auxMotorObj.getMotorOutputPercent(),	 
					armStage1.getEncoderReading(),
					armStage1.workingServoPos, 
					(armStage1.topLimitSwitch.get()==Cnst.LIMIT_SWITCHES_TRIPPED_STATE ? "T" : "F"),
					(armStage1.bottomLimitSwitch.get()==Cnst.LIMIT_SWITCHES_TRIPPED_STATE ? 
							"Tripped" : "notTripped"),
					armStage1.encoderReadingRaw,
					armStage1.encoderSnapshot
					);
			if(forcePrint || ! ans.equals(m_prevLogArmInfoStr)) {
				P.println(PrtYn.Y, ans);
				m_prevLogArmInfoStr = ans;
			}
			m_logArmInfoThrottle = 5;
		} 
		else if(m_logArmInfoThrottle < 0) { m_logArmInfoThrottle = -1; } //so doesn't wrap around and become positive
	}
	
	public static class LocalCommands {
		private final static LocalCommands lcInstance = new LocalCommands();
		private static LocalCommands getInstance() { return lcInstance; }
		private LocalCommands() {}
		
		
		public class LocalCmdDoNothing extends Command {

			TmSsArm ssArm;
			TmDriverStation m_tds;
			
		    public LocalCmdDoNothing() {
		    	m_tds = TmDriverStation.getInstance();
		    	ssArm = TmSsArm.getInstance();
		    	requires(ssArm);
		    }

		    // Called just before this Command runs the first time
		    protected void initialize() {
		    	P.println(Tt.getClassName(this) + " initializing");
		    }

		    // Called repeatedly when this Command is scheduled to run
		    protected void execute() {}

		    // Make this return true when this Command no longer needs to run execute()
		    protected boolean isFinished() {
		    	boolean ans = false;
		    	return ans;
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
		
	}
}
