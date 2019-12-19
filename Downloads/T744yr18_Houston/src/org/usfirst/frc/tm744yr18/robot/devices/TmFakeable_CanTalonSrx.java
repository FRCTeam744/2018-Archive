package org.usfirst.frc.tm744yr18.robot.devices;

import java.util.ArrayList;
import java.util.List;

import org.usfirst.frc.tm744yr18.robot.config.TmHdwrRoCntls.RoNamedControlsE;
import org.usfirst.frc.tm744yr18.robot.config.TmHdwrRoPhys.RoNamedConnectionsE;
import org.usfirst.frc.tm744yr18.robot.config.TmHdwrRoPhysBase.EncoderIncrVsThingDirE;
import org.usfirst.frc.tm744yr18.robot.config.TmHdwrRoPhysBase.RoConnectionEntry;
import org.usfirst.frc.tm744yr18.robot.config.TmHdwrRoPhysBase.RoMtrHasEncoderE;
import org.usfirst.frc.tm744yr18.robot.config.TmHdwrRoPhysBase.RoNamedControlsEntry;
import org.usfirst.frc.tm744yr18.robot.config.TmSdKeysI.SdKeysE;
import org.usfirst.frc.tm744yr18.robot.exceptions.TmExceptions;
import org.usfirst.frc.tm744yr18.robot.helpers.TmSdMgr;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmForcedInstantiateI;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmItemAvailabilityI;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmMotorAmpsTrackingI;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmRoControlUserI;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmToolsI.P;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmToolsI.P.PrtYn;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmToolsI.Tt;

import com.ctre.phoenix.ErrorCode;
import com.ctre.phoenix.motorcontrol.ControlMode;
import com.ctre.phoenix.motorcontrol.FeedbackDevice;
//import com.ctre.phoenix.motorcontrol.NeutralMode;
import com.ctre.phoenix.motorcontrol.RemoteFeedbackDevice;
import com.ctre.phoenix.motorcontrol.can.TalonSRX;

import edu.wpi.first.wpilibj.RobotController;

//import com.ctre.CANTalon;
//import com.ctre.CANTalon.FeedbackDevice;
//import com.ctre.CANTalon.TalonControlMode;

import edu.wpi.first.wpilibj.Timer;
import t744opts.Tm744Opts;

//public class TmFakeable_CanTalonSrx implements TmCANTalonFeaturesI, SpeedController, MotorSafety {
public class TmFakeable_CanTalonSrx implements TmMotorAmpsTrackingI, TmItemAvailabilityI, TmForcedInstantiateI {
	private TalonSRX m_realObj = null;
	private boolean m_beingFaked = false;
	public boolean m_encoderBeingFaked = false;
	private ConnectedEncoder m_connectedEncoder = null;
	
	private static String m_fakeTalonsMsg = "";
	private static List<RoNamedControlsE> m_fakeCntlsList = new ArrayList<>();
	
	private AmpsTracking<TmFakeable_CanTalonSrx> m_outputCurrentLog;
	private int m_userCurrentLimit;
	
	private boolean m_usingBatteryCompensation = false;
	private double m_batteryCompensationConfigFactor = 1.0;
//	private TalonControlMode m_controlMode = TalonControlMode.PercentVbus;
	private ControlMode m_controlMode = ControlMode.PercentOutput; // .PercentVbus;
	private double m_controlModeOutput1 = 0.0;
	private double m_controlModeOutput2 = 0.0;
	private int m_canIdOfMasterBeingFollowed = -1; //not following anything
	TmFakeable_CanTalonSrx m_masterObj = null; // = TrackInstances.getInstanceFromCanId(m_canIdOfMasterBeingFollowed);

//	final double NOMINAL_BATTERY_VOLTAGE = TmSsBattery.getNominalMaxBatteryVoltage(); //12.0;
	
	private static final int CAN_BUS_TIMEOUT_MS = 0; 
	
	public RoNamedControlsE m_namedCntl;
	public RoNamedControlsEntry m_namedCntlEnt;
	public RoNamedConnectionsE m_namedConn;
	public RoConnectionEntry m_namedConnEnt;
//	public double m_maxEncoderTicksPerSecond;

//	RoNamedIoE m_namedCanTalonIoDef;
	
//	double m_lastRequestedSpeedRaw = 0;
	double m_lastReqPercOutAdjusted = 0;
	double m_lastReqPercOutUsed = 0;
	ControlMode m_lastRequestedMode = ControlMode.PercentOutput; //default
	double m_lastRequestedModeOutput1 = 0.0;
	

	
	private TmFakeable_CanTalonSrx m_curInstance = this;

	/**
	 * note that even when a real talon is present, it may not have the encoder that the
	 * code is expecting.  Always call the 'fake' version of methods that affect the
	 * encoder so we can implement a fake encoder even on real talons
	 */
	public FakeParms m_fakeParms;
	public class FakeParms {
		
//		//any parms or methods that the 'fake' object needs to
//		//emulate should be coded here.
//		MotorSafetyHelper f_safetyHelper;
//		double f_safetyExpiration = 0;
		double f_percentOut = 0;
		double f_outputCurrent = 0;
//		int f_userCurrentLimit = 999; //40; //something obviously weird

		int f_encoderPos = 0;
		int f_encoderTargetPos = 0;
		private Timer f_encoderTimer;
		private Timer f_elapsedTimer;
		double f_maxEncoderTicksPerSec;
		int f_fakeEncoderDirection;
		double f_ticksPer100ms = 0;
		double f_targetTicksPer100ms = 0;
		private final Object f_encoderLock = new Object();
		FeedbackDevice f_feedbackDev = null;
		RemoteFeedbackDevice f_remoteFeedbackDevice = null;
		protected FakeParms(double maxEncoderTicksPerSec) {
//			f_safetyHelper = null; //new MotorSafetyHelper(m_curInstance);
			f_encoderTimer = new Timer();
			f_elapsedTimer = new Timer();
			f_encoderTimer.start();
			f_elapsedTimer.start();
			f_maxEncoderTicksPerSec = maxEncoderTicksPerSec;
		}

		
//		public void fake_setCurrentLimit(int amps) {
//			f_userCurrentLimit = amps;
//		}
		ConnectedEncoder f_fakeEncoder = null;
		
		
		//HELP!! should only be called if ConnectedEncoder detects that it's connected to a fake CanTalonSrx,
		//   but ConnectedEncoder only checks if the named control is config'd as a fake.  ConnectedEncoder
		//   has been changed to always call setFakeEncoder (which calls fake_setFakeEncoder) Needs more thought!
		//called indirectly from TmFakeable_CanTalonSrx constructor via ConnectedEncoder constructor
		public void fake_setFakeEncoder(ConnectedEncoder encoder) { 
			if(f_fakeEncoder==null) {
				f_fakeEncoder = encoder;
				f_fakeEncoderDirection = encoder.getEncoderPolarityFactor(); //.c_encPolarityRelToMotor.eDirectionFactor;
				f_maxEncoderTicksPerSec = encoder.c_countsPerRevolution * encoder.c_maxRps;
			}
		}
		
		//yes, these parms should be in ConnectedEncoder, but it hasn't yet been properly updated for 2018 code. punt
		//and see fake_setFakeEncoder above (and its comments)
		public void configFakeEncoder(boolean clamp, int min, int max) {
			f_fakeEncoder.c_hasMaxMinLimits = clamp;
			f_fakeEncoder.c_minAllowedCounts = min;
			f_fakeEncoder.c_maxAllowedCounts = max;
		}
		
		//4in. wheels: (4in * pi) / 12 = 1.0471975512
		public static final double FEET_PER_REVOLUTION = 1.047;
		public static final double MAX_FEET_PER_SECOND = 16;
		public static final int DEFAULT_ENCODER_TICKS_PER_REV = 4096; //magnetic encoder
		// (ft/sec)/(ft/rev) = (rev/sec);   (rev/sec)*(ticks/rev)= (ticks/sec)
		public static final double DEFAULT_MAX_ENCODER_TICKS_PER_SEC = 
					(MAX_FEET_PER_SECOND / FEET_PER_REVOLUTION) * DEFAULT_ENCODER_TICKS_PER_REV;
		
		/**
		 * should only be called by something that owns f_encoderLock!!
		 */
		private void updateEncoderTicks(int dbgMsgCnt) {
			synchronized(f_encoderLock) {
//			double change = f_encoderTimer.get() * (f_maxEncoderTicksPerSec * m_lastReqPercOutUsed);
			double timeRaw = f_encoderTimer.get();
			double time = (timeRaw<0.020) ? 0.020 : timeRaw;
//			double elapsedTime = f_elapsedTimer.get();
			double change = time * (f_maxEncoderTicksPerSec * f_percentOut);
			//round the value to nearest int, keeping sign
			int intChange = Tt.doubleToRoundedInt(change);
			if(true) {
				switch(m_namedCntlEnt.cMtrPosPercentOutVsThingDir) {
				case POS_MTR_PERCENT_OUT_MOVES_THING_BACKWARD:
					if(m_namedCntlEnt.cMtrEncoderIncrVsThingDir.equals(
							EncoderIncrVsThingDirE.ENC_INCREASING_WHEN_THING_MOVING_FORWARD)) {
						intChange *= -1;
					}
					break;
				case POS_MTR_PERCENT_OUT_MOVES_THING_DOWN:
					if(m_namedCntlEnt.cMtrEncoderIncrVsThingDir.equals(
							EncoderIncrVsThingDirE.ENC_INCREASING_WHEN_THING_MOVING_UP)) {
						intChange *= -1;
					}
					break;
				case POS_MTR_PERCENT_OUT_MOVES_THING_FORWARD:
					if(m_namedCntlEnt.cMtrEncoderIncrVsThingDir.equals(
							EncoderIncrVsThingDirE.ENC_INCREASING_WHEN_THING_MOVING_BACKWARD)) {
						intChange *= -1;
					}
					break;
				case POS_MTR_PERCENT_OUT_MOVES_THING_IN:
					if(m_namedCntlEnt.cMtrEncoderIncrVsThingDir.equals(
							EncoderIncrVsThingDirE.ENC_INCREASING_WHEN_THING_MOVING_OUT)) {
						intChange *= -1;
					}
					break;
				case POS_MTR_PERCENT_OUT_MOVES_THING_LEFT:
					if(m_namedCntlEnt.cMtrEncoderIncrVsThingDir.equals(
							EncoderIncrVsThingDirE.ENC_INCREASING_WHEN_THING_MOVING_RIGHT)) {
						intChange *= -1;
					}
					break;
				case POS_MTR_PERCENT_OUT_MOVES_THING_OUT:
					if(m_namedCntlEnt.cMtrEncoderIncrVsThingDir.equals(
							EncoderIncrVsThingDirE.ENC_INCREASING_WHEN_THING_MOVING_IN)) {
						intChange *= -1;
					}
					break;
				case POS_MTR_PERCENT_OUT_MOVES_THING_RIGHT:
					if(m_namedCntlEnt.cMtrEncoderIncrVsThingDir.equals(
							EncoderIncrVsThingDirE.ENC_INCREASING_WHEN_THING_MOVING_LEFT)) {
						intChange *= -1;
					}
					break;
				case POS_MTR_PERCENT_OUT_MOVES_THING_UP:
					if(m_namedCntlEnt.cMtrEncoderIncrVsThingDir.equals(
							EncoderIncrVsThingDirE.ENC_INCREASING_WHEN_THING_MOVING_DOWN)) {
						intChange *= -1;
					}
					break;
				case TBD:
					break;
					//default:
					//break;
				}
			}
			f_encoderPos += intChange;
			if(f_fakeEncoder.c_hasMaxMinLimits) {
				int clampedVal = Tt.clampToRange(f_encoderPos, 
						f_fakeEncoder.c_minAllowedCounts, f_fakeEncoder.c_maxAllowedCounts);
				f_encoderPos = clampedVal;
			}
			f_encoderTimer.reset();
			//if(m_namedCntlEnt.cMtrPosPercentOutVsThingDir)
			//if(m_namedCntlEnt.cMtrEncoderIncrVsThingDir)
//			if(dbgMsgCnt>0) {
//				P.printFrmt(-1, "fakeEncUpdTicks: timeRaw=%f, time=%f, spd=% 1.2f, maxTps=%1.2f, " + 
//						"chg=% 1.2f, iChg=% d, pos=% d", 
//						timeRaw, time, f_percentOut, f_maxEncoderTicksPerSec, change, intChange, f_encoderPos);
//			}
			}
		}
		
		public ControlMode fake_getControlMode() { return m_controlMode; }
		
		public void fake_set(ControlMode mode, double outputValue) { fake_set(mode, outputValue, 0.0); }
		public void fake_set(ControlMode mode, double outputValue, double addlOutputVal) {
			m_controlMode = mode;
			m_controlModeOutput1 = outputValue;
			m_controlModeOutput2 = addlOutputVal; //for future CTRE use
			
//			if(mode.equals(ControlMode.PercentOutput)) {
//				double spd = outputValue;
//				f_ticksPer100ms = (f_maxEncoderTicksPerSec * spd) / 10; // (DEFAULT_MAX_ENCODER_TICKS_PER_SEC * spd) / 10;			
//				f_percentOut = spd; 
//			}
//			else if(mode.equals(ControlMode.Disabled)) {
//				f_ticksPer100ms = 0;
//				f_percentOut = 0;
//			}
			switch(mode) {
			case Current:
				f_outputCurrent = outputValue;
				f_percentOut = f_outputCurrent/m_namedCntlEnt.cMaxContinuousAmps.value;
				f_ticksPer100ms = (f_maxEncoderTicksPerSec * f_percentOut) / 10;
//				if(f_motorInverted) { f_ticksPer100ms *= -1; }
				if(false && f_sensorPhase.equals(CtreSensorPhaseE.INVERTED_FROM_MOTOR)) { f_ticksPer100ms *= -1; }
				break;
			case Disabled:
				f_ticksPer100ms = 0;
				f_percentOut = 0;
				f_outputCurrent = 0;
				break;
			case Follower:
				m_canIdOfMasterBeingFollowed = (int)outputValue;
				m_masterObj = TrackInstances.getInstanceFromCanId(m_canIdOfMasterBeingFollowed);
				if( ! (m_masterObj==null)) {
					f_percentOut = m_masterObj.getMotorOutputPercent();
					f_outputCurrent = f_percentOut * m_namedCntlEnt.cMaxContinuousAmps.value;
					f_ticksPer100ms = (f_maxEncoderTicksPerSec * f_percentOut) / 10;
					if(false && f_sensorPhase.equals(CtreSensorPhaseE.INVERTED_FROM_MOTOR)) { f_ticksPer100ms *= -1; }
				}
				break;
			case MotionMagic:
//			case MotionMagicArc:
			case MotionProfile:
			case MotionProfileArc:
				//unsupported
				f_ticksPer100ms = 0;
				f_percentOut = 0;
				f_outputCurrent = 0;
				break;
			case PercentOutput:
				f_percentOut = outputValue;
				f_outputCurrent = f_percentOut * m_namedCntlEnt.cMaxContinuousAmps.value;
				f_ticksPer100ms = (f_maxEncoderTicksPerSec * f_percentOut) / 10;
				if(false && f_sensorPhase.equals(CtreSensorPhaseE.INVERTED_FROM_MOTOR)) { f_ticksPer100ms *= -1; }
				break;
			case Position:
				f_encoderTargetPos = (int)outputValue;
//				if(false && f_sensorPhase.equals(CtreSensorPhaseE.INVERTED_FROM_MOTOR)) { f_encoderTargetPos *= -1; }
				break;
			case Velocity:
				f_ticksPer100ms = outputValue;
				f_percentOut = f_ticksPer100ms/f_maxEncoderTicksPerSec;
				if(false && f_sensorPhase.equals(CtreSensorPhaseE.INVERTED_FROM_MOTOR)) { f_percentOut *= -1; }
				f_outputCurrent = f_percentOut * m_namedCntlEnt.cMaxContinuousAmps.value;
				break;
			//default:
				//no default case. forces an error if a case statement is needed but missing
				//break;			
			}
		}
		
		int f_allowableClosedloopError = 0;
		public ErrorCode fake_configAllowableClosedloopError(CtreSlotIdxE slotIdx, int allowableClosedLoopError, 
																						int timeoutMs) {
			f_allowableClosedloopError = allowableClosedLoopError;
			return ErrorCode.OK;
		}
		
		int f_closedLoopTarget = 0;
		public int fake_getClosedLoopTarget(CtrePidIdxE pidIdx) {
			return f_closedLoopTarget;
		}
		
		double f_closedLoopError = 0;
		int dbgMsgCntCle = 10;
		public double fake_getClosedLoopError(CtrePidIdxE pidIdx) {
			double ans = f_closedLoopError;
			switch(m_controlMode) {
			case Follower:
				ans = 0;
				if(m_canIdOfMasterBeingFollowed>=0 && m_masterObj==null) {
					m_masterObj = TrackInstances.getInstanceFromCanId(m_canIdOfMasterBeingFollowed);
				}
				if(m_masterObj != null) {
					ans = m_masterObj.getClosedLoopError(pidIdx);
				}
				break;
			case MotionMagic:
//			case MotionMagicArc:
			case MotionProfile:
			case MotionProfileArc:
				//ans = 0; //unsupported
				//break;
			case Current:
				//ans = f_closedLoopTarget - f_outputCurrent;
				//break;
			case Disabled:
				//ans = 0;
				//break;
			case PercentOutput:
				if(false) {
				throw TmExceptions.getInstance().new ReachedCodeThatShouldNeverExecuteEx(
						m_controlMode.name() + " is not" +
						" a valid mode for closedloop control -- " + m_namedCntl.name());
				//break;
				} else {
					if(dbgMsgCntCle-- > 0) {
					P.println(PrtYn.N, m_controlMode.name() + " is not" +
							" a valid mode for closedloop control -- " + m_namedCntl.name());
					} else {
						dbgMsgCntCle = -1;
					}
				}
			case Position:
				ans = f_encoderTargetPos - f_encoderPos;
				break;
			case Velocity:
				ans = f_targetTicksPer100ms - f_ticksPer100ms;
				break;
			//default:
				//break;
			}
			return ans;
		}
		
		public double fake_getSelectedSensorVelocity(CtrePidIdxE pidIdx) {
			double ans;
			synchronized(f_encoderLock) {
				updateEncoderTicks(3);
				ans = f_ticksPer100ms;
				if(false && f_sensorPhase.equals(CtreSensorPhaseE.INVERTED_FROM_MOTOR)) { ans *= -1; }
//				if(f_motorInverted) { ans *= -1; }
			}
			return ans;
		}
		public int fake_getSelectedSensorPosition(CtrePidIdxE pidIdx) {
			int ans;
			if(m_namedCntl.equals(RoNamedControlsE.ARM_MTR_STAGE1_EXTENDER)) {
				int junk = 5; //just a good debugger breakpoint
			}
			synchronized(f_encoderLock) {
				updateEncoderTicks(3);
				ans = f_encoderPos; //f_ticksPer100ms;
				if(false && f_sensorPhase.equals(CtreSensorPhaseE.INVERTED_FROM_MOTOR)) { ans *= -1; }
//				if(f_motorInverted) { ans *= -1; }
			}
			return ans;
		}
		public ErrorCode fake_setSelectedSensorPosition(int sensorPos, CtrePidIdxE pidIdx, int timeoutMs) {
			f_encoderPos = sensorPos;
			if(false && f_sensorPhase.equals(CtreSensorPhaseE.INVERTED_FROM_MOTOR)) { f_encoderPos *= -1; }
//			if(f_motorInverted) { f_encoderPos *= -1; }
			return ErrorCode.OK;
		}
		public double fake_getTemperature() {
			double ans;
			//use something outrageous to remind folks that this is a FAKE CANTalon
			ans = -10; //degrees C
			return ans;
		}
		public double fake_getOutputCurrent() {
			double ans;
			//use something outrageous to remind folks that this is a FAKE CANTalon
//			f_outputCurrent = f_percentOut * 0.090;
			ans = f_outputCurrent;
			return ans;
		}
		
		public double fake_getMotorOutputPercent() {
			double ans = 0.0; //TODO
			switch(m_controlMode) {
			case Current:
				ans = f_percentOut; //f_outputCurrent; //TODO
				break;
			case Disabled:
				ans = f_percentOut; //0.0;
				break;
			case Follower:
				TmFakeable_CanTalonSrx master = TrackInstances.getInstanceFromCanId(m_canIdOfMasterBeingFollowed);
				if( ! (master==null)) { ans = master.getMotorOutputPercent(); }
				break;
			case MotionMagic:
//			case MotionMagicArc:
			case MotionProfile:
			case MotionProfileArc:
				ans = 0.0;
				break;
			case PercentOutput: //TODO
				ans = f_percentOut;
				break;
			case Position: //TODO
				ans = f_percentOut; //f_encoderPos;
				break;
			case Velocity: //TODO
				ans = f_percentOut; //f_ticksPer100ms;
				break;
			default:
				ans = f_percentOut; //0.0;
				break;		
			}
			return ans;
		}
		
		public ErrorCode fake_configSelectedFeedbackSensor(FeedbackDevice sns, CtrePidIdxE pidIdx, 
				int canBusTimeoutMs) { 
			f_feedbackDev = sns;
			switch(sns) {
			case CTRE_MagEncoder_Absolute:
			case CTRE_MagEncoder_Relative:
				break;
			case Analog:
			case None:
			case PulseWidthEncodedPosition:
			case QuadEncoder:
			case RemoteSensor0:
			case RemoteSensor1:
			case SensorDifference:
			case SensorSum:
			case SoftwareEmulatedSensor:
			case Tachometer:
			default:
				throw TmExceptions.getInstance().new UnsupportedFeatureSelectedEx(sns.name() + " is not supported.....");
				//break;
			}
			return ErrorCode.OK;
		}
//		public ErrorCode fake_configSelectedFeedbackSensor(RemoteFeedbackDevice sns, CtrePidIdxE pidIdx, 
//				int canBusTimeoutMs) { 
//			f_remoteFeedbackDevice = sns;
//			return ErrorCode.OK;
//		}
		
		public ErrorCode fake_configContinuousCurrentLimit(int amps, int timeoutMs) {
//			int retval =  MotControllerJNI.ConfigContinuousCurrentLimit(m_handle, amps, timeoutMs);
//			return ErrorCode.valueOf(retval);
			//we use info in m_namedCntlEnt....
			return ErrorCode.OK;
		}
		public ErrorCode fake_configPeakCurrentDuration(int milliseconds, int timeoutMs) {
//			int retval = MotControllerJNI.ConfigPeakCurrentDuration(m_handle, milliseconds, timeoutMs);
//			return ErrorCode.valueOf(retval);
			return ErrorCode.OK;
		}
		public ErrorCode fake_configPeakCurrentLimit(int amps, int timeoutMs) {
//			int retval =  MotControllerJNI.ConfigPeakCurrentLimit(m_handle, amps, timeoutMs);
//			return ErrorCode.valueOf(retval);
			return ErrorCode.OK;
		}
		public void fake_enableCurrentLimit(boolean enable) {
//			MotControllerJNI.EnableCurrentLimit(m_handle, enable);
		}
		
		boolean f_motorInverted = false;
		public void fake_setInverted(CtreMotorInvertedE inversion) { //(boolean invert) {
			f_motorInverted = inversion.eIsInverted;
		}

		CtreSensorPhaseE f_sensorPhase = CtreSensorPhaseE.MATCHES_MOTOR;
		/**
		 * once this is called, inverting the motor inverts sensor readings automatically
		 * @param phaseSensor MATCHES_MOTOR if positive percent out produces a positive change in the sensor
		 */
		public void fake_setSensorPhase(CtreSensorPhaseE phaseSensor) {
			f_sensorPhase = phaseSensor;
		}

		public ErrorCode fake_configNominalOutputForward(double percentOut, int timeoutMs) { return ErrorCode.OK; }
		public ErrorCode fake_configNominalOutputReverse(double percentOut, int timeoutMs) { return ErrorCode.OK; }
		public ErrorCode fake_configPeakOutputForward(double percentOut, int timeoutMs) { return ErrorCode.OK; }
		public ErrorCode fake_configPeakOutputReverse(double percentOut, int timeoutMs) { return ErrorCode.OK; }

		public ErrorCode fake_config_kP(int slotIdx, double value, int timeoutMs) { return ErrorCode.OK; }
		public ErrorCode fake_config_kI(int slotIdx, double value, int timeoutMs) { return ErrorCode.OK; }
		public ErrorCode fake_config_kD(int slotIdx, double value, int timeoutMs) { return ErrorCode.OK; }
		public ErrorCode fake_config_kF(int slotIdx, double value, int timeoutMs) { return ErrorCode.OK; }
		
		public ErrorCode fake_configOpenloopRamp(double secondsFromNeutralToFull, int timeoutMs) { return ErrorCode.OK; }
		public ErrorCode fake_configClosedloopRamp(double secondsFromNeutralToFull, int timeoutMs) { return ErrorCode.OK; }
		
		public int fake_getDeviceID() {return m_namedConnEnt.getConnectionFrcIndex(); }
		
		public ErrorCode fake_getLastError() { return ErrorCode.OK; }

		protected class Fake_SensorCollection {
			int fscDbgMsgCnt;
			int fQuadPosition = 0;
			protected Fake_SensorCollection() { this(0); }
			protected Fake_SensorCollection(int dbgMsgCnt) {
				fscDbgMsgCnt = dbgMsgCnt;
			}
			public int fake_getQuadraturePosition(int dbgMsgCnt) {
				int ans = 0;
				ans = fQuadPosition;
				return ans;
			}
			public void fake_setQuadraturePosition(int pos, int timeoutMs) {
				fQuadPosition = pos;
			}
		}
		Fake_SensorCollection f_snsCollection = new Fake_SensorCollection();
		Fake_SensorCollection fake_getSensorCollection() { return f_snsCollection; }
	} //end of FakeParms class
	
	public static class TrackInstances {
		private static final TrackInstances tiInstance = new TrackInstances();
		public static TrackInstances getInstance() { return tiInstance; }
		private TrackInstances() {}
		
		static List<TrackInstEntry> tiDevInstanceList = new ArrayList<>();
		
		public class TrackInstEntry<CU extends TmRoControlUserI> {
			public CU fctCntlUser;
			public TmFakeable_CanTalonSrx fctObj;
			public RoNamedControlsE fctNamedCntl;
			public TrackInstEntry(CU cntlUser, TmFakeable_CanTalonSrx fct, RoNamedControlsE namedCntl) {
				fctCntlUser = cntlUser;
				fctObj = fct;
				fctNamedCntl = namedCntl;
			}
		}
		
		public static List<TrackInstEntry> getList() { return tiDevInstanceList; }
		
		public static <CU extends TmRoControlUserI> void trackUserAndDevInstances(CU cntlUser, 
													TmFakeable_CanTalonSrx devInst, RoNamedControlsE namedCntl) {
			for(TrackInstEntry tie : TrackInstances.tiDevInstanceList) {
				if(tie.fctNamedCntl.equals(namedCntl)) {
					throw TmExceptions.getInstance().new MultipleUsersForRoControlEx(cntlUser.getClass().getSimpleName() 
								+ " CONFIGURATION ERROR!!!: " +
									tie.fctCntlUser.getClass().getSimpleName() + " has already allocated a " +
									Tt.getClassName(devInst) + " object for " + tie.fctNamedCntl.name());
				}
			}
			
			TrackInstances.tiDevInstanceList.add(TrackInstances.getInstance()
					.new TrackInstEntry(cntlUser, devInst, namedCntl));
		}
		
		/**
		 * helpful when tracking followers?
		 * @param namedCntlToFind
		 * @return
		 */
		public static TmFakeable_CanTalonSrx getInstanceFromNamedControl(RoNamedControlsE namedCntlToFind) {
			TmFakeable_CanTalonSrx ans = null;
			for(TrackInstEntry t : tiDevInstanceList) {
				if(t.fctNamedCntl.equals(namedCntlToFind)) {
					ans = t.fctObj;
					break; //end the loop
				}
			}
			return ans;
		}

		/**
		 * helpful when tracking followers?
		 * @param CAN id for the instance to find
		 * @return
		 */
		public static TmFakeable_CanTalonSrx getInstanceFromCanId(int canIdToFind) {
			TmFakeable_CanTalonSrx ans = null;
			for(TrackInstEntry t : tiDevInstanceList) {
				if(canIdToFind == t.fctNamedCntl.getEnt().cNamedMod.getEnt()
						.cModNamedConn.getEnt().getConnectionFrcIndex()) {
					ans = t.fctObj;
					break; //end the loop
				}
			}
			return ans;
		}

	}

	
	public int timeoutToUse(int defaultTimeoutMs) {
		int ans;
		int toUse = -1; //use default
		switch(toUse) {
		case -1:
			ans = defaultTimeoutMs;
			//break;
		default:
			ans = toUse;
			//break;
		}
		return ans;
	}
	
	
	
//	public TmFakeable_CANTalon(RoNamedIoE namedCanTalonIoDef) {
//		this(namedCanTalonIoDef, FakeParms.DEFAULT_MAX_ENCODER_TICKS_PER_SEC);
//	}
//	private TmFakeable_CANTalon(RoNamedIoE namedCanTalonIoDef, double maxEncoderTicksPerSecond) {
	public <CU extends TmRoControlUserI> TmFakeable_CanTalonSrx(CU cntlUser, RoNamedControlsE namedCntl) {
		this(cntlUser, namedCntl, FakeParms.DEFAULT_MAX_ENCODER_TICKS_PER_SEC);
//		TrackInstances.trackUserAndDevInstances(cntlUser, this, namedCntl);
	}
	private <CU extends TmRoControlUserI> TmFakeable_CanTalonSrx(CU cntlUser, RoNamedControlsE namedCntl, 
			double defaultMaxEncoderTicksPerSecond) {
		TrackInstances.trackUserAndDevInstances(cntlUser, this, namedCntl);
		
		String thisClassName = this.getClass().getSimpleName(); //Tt.extractClassName(this);
		m_namedCntl = namedCntl;
		m_namedCntlEnt = namedCntl.getEnt();
//		m_maxEncoderTicksPerSecond = maxEncoderTicksPerSecond;

		if(m_namedCntl.equals(RoNamedControlsE.ARM_MTR_STAGE1_EXTENDER)) {
			int junk = 5; //just a good debugger breakpoint
		}

//		//do the rest of the work in doForcedInstantiate()
		
		m_namedConn = m_namedCntlEnt.cNamedConn; //returned null 2/1/18 11:52pm
		m_namedConnEnt = m_namedConn.getEnt();
		
		double maxEncoderTicksPerSecond = defaultMaxEncoderTicksPerSecond;
		if(m_namedCntlEnt.cMtrHasEncoder) {
			maxEncoderTicksPerSecond = m_namedCntlEnt.cMtrEncoderMaxRevsPerSec * 
						m_namedCntlEnt.cMtrEncoderCountsPerRevolution;
		}
		m_fakeParms = new FakeParms(maxEncoderTicksPerSecond);

		m_outputCurrentLog = new AmpsTracking<TmFakeable_CanTalonSrx>(namedCntl, this);
		m_userCurrentLimit = 40;

		
		int canId = m_namedCntl.getEnt().cNamedConn.getEnt().getConnectionFrcIndex(); //.getCanId();
		String exceptionMsgPrefix = thisClassName + " " + m_namedCntl.name() + 
				" (CAN id=" + canId + ") got exception: ";
		
		try {
			if(m_namedCntlEnt.cCntlAvail.equals(ItemAvailabilityE.USE_FAKE) || Tm744Opts.isInSimulationMode()) { // || Tm17Opts.isUseFakeCanTalons()) {
				configAsFake();
			} else {
				m_realObj = new TalonSRX(canId);
//				//set up whatever else is needed here... encoder?
//				if(m_namedCntlEnt.cMtrHasEncoder) {
//					m_connectedEncoder = new ConnectedEncoder(m_namedCntl, this, 
//							m_namedCntlEnt.cMtrEncoderSdKeyPosition, 
//							m_namedCntlEnt.cMtrEncoderMaxRevsPerSec, 
//							m_namedCntlEnt.cMtrEncoderCountsPerRevolution, m_namedCntlEnt.cMtrEncoderFeetPerRevolution, 
//							m_namedCntlEnt.cMtrEncoderPolarity, m_namedCntlEnt.cMtrEncoderCountsCap);
//				}
			}
		} catch (TmExceptions.Team744RunTimeEx t) { //.CannotSimulateCANTalonEx t) {
			TmExceptions.reportExceptionOneLine(t, exceptionMsgPrefix + t.toString());
			configAsFake();
		} catch (Throwable t) {
			//unknown error
			TmExceptions.reportExceptionMultiLine(t, exceptionMsgPrefix + t.toString());
			configAsFake();
		}
		
//		if( ! isFake()) {
//			try {
//				int firmwareVer = m_realObj.getFirmwareVersion();
//				ErrorCode errCd = m_realObj.getFaults(Faults toFill);
//				
//			} catch (Throwable t) {
//
//			}
//		}
		
		if (isFake()) {
			//use msg in configAsFake() method instead
//			System.out.println(thisClassName + " " + m_namedCntl.name() + " will be a FAKE CAN TalonSrx");
		}
		
		//set up whatever else is needed here... encoder?
		if(m_namedCntlEnt.cMtrHasEncoder) {
			m_connectedEncoder = new ConnectedEncoder(m_namedCntl, this, m_namedCntlEnt.cMtrEncoderSdKeyPosition, 
					m_namedCntlEnt.cMtrEncoderMaxRevsPerSec, 
					m_namedCntlEnt.cMtrEncoderCountsPerRevolution, m_namedCntlEnt.cMtrEncoderFeetPerRevolution, 
					m_namedCntlEnt.cMtrEncoderPolarity, m_namedCntlEnt.cMtrEncoderCountsCap);
		}
		m_encoderBeingFaked = m_namedCntlEnt.cMtrHasEncoderEnumVal.equals(RoMtrHasEncoderE.USE_FAKE_ENCODER);
		if(m_namedCntl.equals(RoNamedControlsE.ARM_MTR_STAGE1_EXTENDER)) { //.m_encoderBeingFaked) {
			int junk = 5; //debug breakpoint
		}

	} //end of constructor
	
//	@Override
//	public String getMaxMinOutputCurrentSummary() { return m_outputCurrentLog.getMaxMinOutputCurrentSummary(); }
	
//	public void enableBatteryCompensation() {
//		m_usingBatteryCompensation = true;
//	}
//	
//	public void disableBatteryCompensation() {
//		m_usingBatteryCompensation = false;
//	}
//	
//	public boolean isUsingBatteryCompensation() {
//		return m_usingBatteryCompensation;
//	}
//	
//	/**
//	 * the ratio of nominal to current battery voltages will be multiplied by
//	 * this tuning parameter before being used to adjust the actual value 
//	 * sent to the motor controller
//	 * @param tuningParm
//	 */
//	public void configBatteryCompensation(double tuningParm) {
//		m_batteryCompensationConfigFactor = tuningParm;
//	}
	
//	//a more intuitive name for the standard getSpeed() method
//	public double getCountsPer100ms() { return getSpeed(); }
	
	//used by ConnectedEncoders that detect that they're "connected" to a fake CAN Talon
	//called from ConnectedEncoders constructor
	private void setFakeEncoder(ConnectedEncoder encoder) { m_fakeParms.fake_setFakeEncoder(encoder); }

	
	//------------available on TalonSRX (on CAN bus) and used in our code
//	@Deprecated
//	public int getEncPosition() { return getEncPosition(0); }
//	@Deprecated
//	public int getEncPosition(int dbgMsgCnt) {
//		int ans;
//		if(isFake()) {
//			ans = m_fakeParms.getEncPosition(dbgMsgCnt);
//		} else {
//			ans = m_realObj.getSensorCollection().getQuadraturePosition(); //.getEncPosition();
//		}
//		return ans;
//	}
	
	public class SensorCollection {
		int scDbgMsgCnt;
		public SensorCollection() { this(0); }
		public SensorCollection(int inpDbgMsgCount) { scDbgMsgCnt = inpDbgMsgCount; }
		
//		public int getQuadraturePosition() {
//			int ans;
//			if(isFake()) {
//				ans = m_fakeParms.fake_getSensorCollection().fake_getQuadraturePosition(scDbgMsgCnt);
//			} else {
//				ans = m_realObj.getSensorCollection().getQuadraturePosition(); //.getEncPosition();
//			}
//			return ans;			
//		}

//		public void setQuadraturePosition(int pos, int timeoutMs) {
//			if(isFake()) {
//				m_fakeParms.fake_getSensorCollection().fake_setQuadraturePosition(pos, timeoutMs);
//			} else {
//				m_realObj.getSensorCollection().setQuadraturePosition(pos, timeoutMs); //CAN_BUS_TIMEOUT_MS); 
//			}
//		}
	} //end of SensorCollection class
	int snsCollDbgCnt = 10;
	public SensorCollection m_sensorCollection = new SensorCollection(snsCollDbgCnt);
	public SensorCollection getSensorCollection() { return m_sensorCollection; }

//	@Deprecated
//	public void setEncPosition(int pos) {
//		if(isFake()) {
//			m_fakeParms.setEncPosition(pos);
//		} else {
//			m_realObj.getSensorCollection().setQuadraturePosition(pos, CAN_BUS_TIMEOUT_MS); //.setEncPosition(pos);
//		}
//	}

//	@Deprecated
//	public double getSpeed() { return getSelectedSensorVelocity(CtrePidIdxE.PRIMARY_CLOSED_LOOP); }
	public double getSelectedSensorVelocity(CtrePidIdxE pidIdx) {
		double ans;
		if(isFake() || m_encoderBeingFaked) {
			ans = m_fakeParms.fake_getSelectedSensorVelocity(pidIdx); //getSpeed();
		} else {
			ans = m_realObj.getSelectedSensorVelocity(pidIdx.ePidIdx); //.getSpeed();
		}
		return ans;
	}
	
	public int getSelectedSensorPosition(CtrePidIdxE pidIdx) {
		int ans;
		if(m_namedCntl.equals(RoNamedControlsE.ARM_MTR_STAGE1_EXTENDER)) { //.m_encoderBeingFaked) {
			int junk = 5; //debug breakpoint
		}
		if(isFake() || m_encoderBeingFaked) {
			ans = m_fakeParms.fake_getSelectedSensorPosition(pidIdx); //getSpeed();
			if(m_namedCntl.equals(RoNamedControlsE.ARM_MTR_STAGE1_EXTENDER)) {
				int junk = 5;
			}
		} else {
			ans = m_realObj.getSelectedSensorPosition(pidIdx.ePidIdx); //.getSpeed();
			if(m_namedCntl.equals(RoNamedControlsE.ARM_MTR_STAGE1_EXTENDER)) {
				int junk = 5;
			}
		}
		return ans;
	}
	/**
	 * Sets the sensor position to the given value.
	 *
	 * @param sensorPos
	 *            Position to set for the selected sensor (in raw sensor units).
	 * @param pidIdx
	 *            0 for Primary closed-loop. 1 for cascaded closed-loop.
	 * @param timeoutMs
	 *            Timeout value in ms. If nonzero, function will wait for
	 *            config success and report an error if it times out.
	 *            If zero, no blocking or checking is performed.
	 * @return Error Code generated by function. 0 indicates no error.
	 */
	public ErrorCode setSelectedSensorPosition(int sensorPos, CtrePidIdxE pidIdx, int timeoutMs) {
//		int retval = MotControllerJNI.SetSelectedSensorPosition(m_handle, sensorPos, pidIdx, timeoutMs);
//		return ErrorCode.valueOf(retval);
		ErrorCode ans;
		ans = m_fakeParms.fake_setSelectedSensorPosition(sensorPos, pidIdx, timeoutToUse(timeoutMs)); //getSpeed();
		if(isFake() || m_encoderBeingFaked) {
		} else {
			ans = m_realObj.setSelectedSensorPosition(sensorPos, pidIdx.ePidIdx, timeoutToUse(timeoutMs)); //.getSpeed();
			int retryCnt = 3;
			while( ( ! ans.equals(ErrorCode.OK)) && retryCnt-->0) {
				P.println(PrtYn.Y, "RETRY due to ErrorCode." + ans.name() + 
						" for setSelectedSensorPosition() for " + this.m_namedCntl.name());
				ans = m_realObj.setSelectedSensorPosition(sensorPos, pidIdx.ePidIdx, timeoutToUse(timeoutMs));
			}
		}
		return ans;
	}

	
	public enum CtreSlotIdxE { PARM_SLOT_0(0), PARM_SLOT_1(1), PARM_SLOT_2(2);
		public final int eSlotIdx;
		private CtreSlotIdxE(int idx) { eSlotIdx = idx; }
	}
	public enum CtrePidIdxE { PRIMARY_CLOSED_LOOP(0), CASCADED_CLOSED_LOOP(1);
		public final int ePidIdx;
		private CtrePidIdxE(int idx) { ePidIdx = idx; }
	}
//	public ErrorCode configSelectedFeedbackSensor(RemoteFeedbackDevice sns, CtrePidIdxE pidIdx, int canBusTimeoutMs) {
//		ErrorCode ans = ErrorCode.GENERAL_ERROR;
//		if(isFake()) {
//			ans = m_fakeParms.fake_configSelectedFeedbackSensor(sns, pidIdx, canBusTimeoutMs);
//		} else {
//			FeedbackDevice thing1;
//			RemoteFeedbackDevice thing2;
//			thing1 = FeedbackDevice.QuadEncoder;
//			thing2 = RemoteFeedbackDevice.None;
//			ans = m_realObj.configSelectedFeedbackSensor(sns, pidIdx.ePidIdx, canBusTimeoutMs);
//		}
//		checkErrorCode(ans);
//		return ans;
//	}

	public double getTemperature() {
		double ans;
		if(isFake()) {
			ans = m_fakeParms.fake_getTemperature();
		} else {
			ans = m_realObj.getTemperature();
		}
		return ans;
	}

	public double getOutputCurrent() {
		double ans;
		if(isFake()) {
			ans = m_fakeParms.fake_getOutputCurrent();
		} else {
			ans = m_realObj.getOutputCurrent();
		}
		m_outputCurrentLog.monitorOutputCurrent(ans);
		return ans;
	}

	public double getMotorOutputPercent() {
		double ans;
		if(isFake()) {
			ans = m_fakeParms.fake_getMotorOutputPercent();
		} else {
			ans = m_realObj.getMotorOutputPercent();
		}
		return ans;
	}

//	@Deprecated
//	public void setUserCurrentLimit(int amps) {
//		if(isFake()) {
//			m_fakeParms.setCurrentLimit(amps);
//		} else {
//			int canBusTimeoutMs = CAN_BUS_TIMEOUT_MS;
//			int peakDurationMs = 125;
//			m_realObj.configContinuousCurrentLimit(amps, canBusTimeoutMs); //.setCurrentLimit(amps);
//			m_realObj.configPeakCurrentDuration(peakDurationMs, canBusTimeoutMs);
//			m_realObj.configPeakCurrentLimit(amps, canBusTimeoutMs);
//			m_realObj.enableCurrentLimit(true);
//		}
//		m_userCurrentLimit = amps;
//	}
	public ErrorCode configContinuousCurrentLimit(int amps, int timeoutMs) {
//		int retval =  MotControllerJNI.ConfigContinuousCurrentLimit(m_handle, amps, timeoutMs);
//		return ErrorCode.valueOf(retval);
		ErrorCode ans;
		if(isFake()) {
			ans = m_fakeParms.fake_configContinuousCurrentLimit(amps, timeoutToUse(timeoutMs));
		} else {
			ans = m_realObj.configContinuousCurrentLimit(amps, timeoutToUse(timeoutMs));
			int retryCnt = 3;
			while( ( ! ans.equals(ErrorCode.OK)) && retryCnt-->0) {
				P.println(PrtYn.Y, "RETRY due to ErrorCode." + ans.name() + 
						" for configContinuousCurrentLimit() for " + this.m_namedCntl.name());
				ans = m_realObj.configContinuousCurrentLimit(amps, timeoutToUse(timeoutMs));
			}
		}
		checkErrorCode(ans);
		return ans;
	}
	public ErrorCode configPeakCurrentDuration(int milliseconds, int timeoutMs) {
//		int retval = MotControllerJNI.ConfigPeakCurrentDuration(m_handle, milliseconds, timeoutMs);
//		return ErrorCode.valueOf(retval);
		ErrorCode ans;
		if(isFake()) {
			ans = m_fakeParms.fake_configPeakCurrentDuration(milliseconds, timeoutToUse(timeoutMs));
		} else {
			ans = m_realObj.configPeakCurrentDuration(milliseconds, timeoutToUse(timeoutMs));
			int retryCnt = 3;
			while( ( ! ans.equals(ErrorCode.OK)) && retryCnt-->0) {
				P.println(PrtYn.Y, "RETRY due to ErrorCode." + ans.name() + 
						" for configPeakCurrentDuration() for " + this.m_namedCntl.name());
				ans = m_realObj.configPeakCurrentDuration(milliseconds, timeoutToUse(timeoutMs));
			}
		}
		checkErrorCode(ans);
		return ans;
	}
	public ErrorCode configPeakCurrentLimit(int amps, int timeoutMs) {
//		int retval =  MotControllerJNI.ConfigPeakCurrentLimit(m_handle, amps, timeoutMs);
//		return ErrorCode.valueOf(retval);
		ErrorCode ans;
		if(isFake()) {
			ans = m_fakeParms.fake_configPeakCurrentLimit(amps, timeoutToUse(timeoutMs));
		} else {
			ans = m_realObj.configPeakCurrentLimit(amps, timeoutToUse(timeoutMs));
			int retryCnt = 3;
			while( ( ! ans.equals(ErrorCode.OK)) && retryCnt-->0) {
				P.println(PrtYn.Y, "RETRY due to ErrorCode." + ans.name() + 
						" for configPeakCurrentLimit() for " + this.m_namedCntl.name());
				ans = m_realObj.configPeakCurrentLimit(amps, timeoutToUse(timeoutMs));
			}
		}
		checkErrorCode(ans);
		return ans;
	}
	public void enableCurrentLimit(boolean enable) {
//		MotControllerJNI.EnableCurrentLimit(m_handle, enable);
		if(isFake()) {
			m_fakeParms.fake_enableCurrentLimit(enable);
		} else {
			m_realObj.enableCurrentLimit(enable);
		}
	}

	
//	@Deprecated
//	public void setVoltageCompensationRampRate(double rampRate) {
//		if(isFake()) {
////			m_fakeParms.setVoltageCompensationRampRate(rampRate);
//		} else {
//			double nominalFullVolts = 12.0;
//			double secondsFromNeutralToFull = nominalFullVolts/rampRate;
////			m_realObj.setVoltageCompensationRampRate(rampRate);
//			m_realObj.configOpenloopRamp(secondsFromNeutralToFull, CAN_BUS_TIMEOUT_MS);
//			m_realObj.configClosedloopRamp(secondsFromNeutralToFull, CAN_BUS_TIMEOUT_MS);
//		}
//	}

//	//-------------required by PIDOutput via SpeedController
//	@Override
//	public void pidWrite(double output) {
//		// TODO Auto-generated method stub
//		
//	}
	
//    //-------------required by SpeedController
////	@Override
//	@Deprecated
//	public double get() {
//		double ans;
//		double raw;
//		if(isFake()) {
//			raw = m_fakeParms.fake_get();
//		} else {
////			raw = m_realObj.get();
//			raw = 999999.0;
//			ans = raw;
//			switch(m_controlMode) {
//			case PercentOutput: //PercentVbus:
//				ans = m_realObj.getMotorOutputPercent(); //raw;
//				break;
////			case Voltage:
////				ans = raw / NOMINAL_BATTERY_VOLTAGE;
////				break;
//			case Position:
//				break;
//			case Velocity:
////				ans = m_realObj.getSelectedSensorVelocity(pidIdx);
//				break;
//			case Current:
//			case Follower:
//			case MotionProfile:
//			case MotionMagic:
//			case MotionMagicArc:
//			//case TimedPercentOutput:
//			case MotionProfileArc:
//			
//			case Disabled:
//			default:
//				break;
//			}
//		}
//		ans = 0.0;
//		switch(m_controlMode) {
//		case PercentOutput: //PercentVbus:
//			ans = raw;
//			break;
////		case Voltage:
////			ans = raw / NOMINAL_BATTERY_VOLTAGE;
////			break;
//		default:
//			break;
//		}
//		return ans;
//	}

	public ControlMode getControlMode() {
		if(isFake()) {
			return m_fakeParms.fake_getControlMode();
		} else {
			return m_realObj.getControlMode();
		}
	}
	/**
	 * not a TalonSRX function....
	 * @return
	 */
	public double getLastControlModeOutputValue() {
		return m_lastRequestedModeOutput1;
	}
////	@Override
//  	@Deprecated
//	public void set(double speed) { set(m_lastRequestedMode, speed); }
  	
	public void set(ControlMode controlMode, double outputValue) {
//		m_lastRequestedSpeedRaw = outputValue; //for debug use
		m_lastRequestedMode = controlMode;
		m_lastRequestedModeOutput1 = outputValue;
		switch(controlMode) {
		case Follower:
			//outputValue here is the CAN Id for the TalonSRX (the master) 
			//that this one is following
			m_fakeParms.fake_set(controlMode, outputValue);
			if(isFake()) {
			} else {
				m_realObj.set(controlMode, outputValue);
			}
			break;
		case PercentOutput: //PercentVbus:
			double battCompensationSpeed = outputValue;
			if(m_usingBatteryCompensation) {
//				battCompensationSpeed = outputValue * 
//				((NOMINAL_BATTERY_VOLTAGE / TmSsBattery.getInstance().getRoboBatteryVoltage()) * 
//						m_batteryCompensationConfigFactor);
				battCompensationSpeed = outputValue * (12.0 / RobotController.getBatteryVoltage()); //.getInstance().getBatteryVoltage());
				battCompensationSpeed = Tt.clampToRange(battCompensationSpeed, -1.0, 1.0);
			}
			m_lastReqPercOutAdjusted = battCompensationSpeed;
			m_lastReqPercOutUsed = battCompensationSpeed;
			
			m_fakeParms.fake_set(controlMode, battCompensationSpeed);
			if(isFake()) {
			} else {
				m_realObj.set(controlMode, battCompensationSpeed);
			}
			break;
//		case Voltage:
//			double adjSpeed = speed * NOMINAL_BATTERY_VOLTAGE;
//			m_lastReqPercOutAdjusted = adjSpeed;
//			m_lastReqPercOutUsed = adjSpeed;
//			if(isFake()) {
//				m_fakeParms.set(adjSpeed);
//			} else {
//				m_realObj.set(adjSpeed);
//			}
//			break;
		case Velocity: //Speed:
//			double battCompensationSpeed = speed;
//			if(m_usingBatteryCompensation) {
//				battCompensationSpeed = speed * 
//						((NOMINAL_BATTERY_VOLTAGE / TmSsBattery.getInstance().getRoboBatteryVoltage()) * 
//								m_batteryCompensationConfigFactor);
//			}
//			m_lastReqPercOutAdjusted = battCompensationSpeed;
//			m_lastReqPercOutUsed = outputValue;
			m_fakeParms.fake_set(controlMode, outputValue);
			if(isFake()) {
			} else {
				m_realObj.set(controlMode, outputValue);
			}
			break;
		case Position: //Speed:
			m_fakeParms.fake_set(controlMode, outputValue);
			if(isFake()) {
			} else {
				m_realObj.set(controlMode, outputValue);
			}
			break;
		case Disabled:
			m_fakeParms.fake_set(controlMode, outputValue);
			if(isFake()) {
			} else {
				m_realObj.set(controlMode, outputValue);
			}
			break;
		case Current:
			m_fakeParms.fake_set(controlMode, outputValue);
			if(isFake()) {
			} else {
				m_realObj.set(controlMode, outputValue);
			}
			break;
		case MotionMagic:
//		case MotionMagicArc:
		case MotionProfile:
		case MotionProfileArc:
//		default:
			m_fakeParms.fake_set(controlMode, outputValue);
			if(isFake()) {
			} else {
				m_realObj.set(controlMode, outputValue);
			}
			break;
		}
	}
	
//	@Deprecated
//	public void setFeedbackDevice(FeedbackDevice device){
//		if(isFake()) {
//			m_fakeParms.fake_configSelectedFeedbackSensor(FeedbackDevice.QuadEncoder, 
//					CtrePidIdxE.PRIMARY_CLOSED_LOOP, CAN_BUS_TIMEOUT_MS);
//		}
//		else {
//			m_realObj.configSelectedFeedbackSensor(FeedbackDevice.QuadEncoder, 
//					CtrePidIdxE.PRIMARY_CLOSED_LOOP.ePidIdx, CAN_BUS_TIMEOUT_MS);
//					//.setFeedbackDevice(device);
//		}
//	}
	
//	@Deprecated
//	public void reverseSensor(boolean val) {
//		if(isFake()) {
//			m_fakeParms.fake_setSensorPhase(val);
//		}
//		else{
//			m_realObj.setSensorPhase(val); //.reverseSensor(val);
//		}
//	}
	
	public double getClosedLoopError(CtrePidIdxE pidIdx){
		double ans = 0.0;
		if(isFake()) {
			ans = m_fakeParms.fake_getClosedLoopError(CtrePidIdxE.PRIMARY_CLOSED_LOOP);
		}
		else {
			ans = m_realObj.getClosedLoopError(CtrePidIdxE.PRIMARY_CLOSED_LOOP.ePidIdx);
		}
		return ans;
	}
	
	public int getClosedLoopTarget(CtrePidIdxE pidIdx){
		int ans = 0;
		if(isFake()) {
			ans = m_fakeParms.fake_getClosedLoopTarget(CtrePidIdxE.PRIMARY_CLOSED_LOOP);
		}
		else {
			ans = m_realObj.getClosedLoopTarget(CtrePidIdxE.PRIMARY_CLOSED_LOOP.ePidIdx);
		}
		return ans;
	}
	
//	@Deprecated
//	public void configVoltageOutputs(){
//		if(isFake()) {
//			//nothing
//		}
//		else {
////			m_realObj.configNominalOutputVoltage(0.0f, 0.0f);
////			m_realObj.configPeakOutputVoltage(+12.0f,  0.0f);
//			m_realObj.configNominalOutputForward(1.0, CAN_BUS_TIMEOUT_MS);
//			m_realObj.configNominalOutputReverse(-1.0, CAN_BUS_TIMEOUT_MS);
//			m_realObj.configPeakOutputForward(1.0, CAN_BUS_TIMEOUT_MS);
//			m_realObj.configPeakOutputReverse(-1.0, CAN_BUS_TIMEOUT_MS);
//		}
//	}
	
//	@Deprecated
//	public void setPidGains(double F, double P, double I, double D) {
//		if(isFake()) {
//			//nothing
//		}
//		else {
//			int profileSlot = 0;
//			
//			//integral accumulator cleared automatically if closed-loop error is outside 
//			//of this zone
//			int iZone = 50; //closed loop error units X 1ms; 
//			
//			m_realObj.selectProfileSlot(profileSlot, CtrePidIdxE.PRIMARY_CLOSED_LOOP.ePidIdx); //.setProfile(0);
//			m_realObj.config_kF(profileSlot, F, CAN_BUS_TIMEOUT_MS); //.setF(F);
//			m_realObj.config_kP(profileSlot, P, CAN_BUS_TIMEOUT_MS); //.setP(P);
//			m_realObj.config_kI(profileSlot, I, CAN_BUS_TIMEOUT_MS); //.setI(I);
//			m_realObj.config_kD(profileSlot, D, CAN_BUS_TIMEOUT_MS); //.setD(D);
////			m_realObj.config_IntegralZone(profileSlot, iZone, CAN_BUS_TIMEOUT_MS); //new for 2018??
//		}
//	}

	public enum CtreMotorInvertedE { 
		INVERTED(true), //motor runs in reverse when positive inputs applied
		NOT_INVERTED(false); // motor runs forward when positive inputs applied
		private final boolean eIsInverted;
		private CtreMotorInvertedE(boolean isInverted) {
			eIsInverted = isInverted;
		}
	}
	public void setInverted(CtreMotorInvertedE inversion) { //boolean invert) {
//		_invert = invert; /* cache for getter */
//		MotControllerJNI.SetInverted(m_handle, invert);
		m_fakeParms.fake_setInverted(inversion); 
		if(isFake()) {
		} else {
			m_realObj.setInverted(inversion.eIsInverted); 
		}
	}
//	public void setInverted(CtreMotorInvertedE isInverted) {
//		if(false) { //original code did nothing here, a BUG, but don't change for 2018!!
//		if(isFake()) {
////			m_fakeParms.setInverted();
//		} else {
//			m_realObj.setInverted(isInverted.eIsInverted);
//		}
//		}
//	}

//	@Deprecated
////	@Override
//	public boolean getInverted() {
//		// TODO Auto-generated method stub
//		return false;
//	}

////	@Override
//	@Deprecated //not really, but it's not a standard TalonSRX method
//	public void disable() { //not available on TalonSRX, but here for kicks
//		if(isFake()) {
//			m_fakeParms.fake_set(ControlMode.Disabled, 0.0); //.stopMotor();
//		} else {
//			//second parm will be ignored....
//			m_realObj.set(ControlMode.Disabled, 0.0); //.disable();
//		}
//	}

////	@Override
//	@Deprecated //not really, but it's not a standard TalonSRX method
//	public void stopMotor() {
//		if(isFake()) {
//			m_fakeParms.fake_set(ControlMode.Disabled, 0.0);
//		} else {
//			m_realObj.set(ControlMode.Disabled, 0.0);  //.setNeutralMode(neutralMode); .setNeutralOutput(); //stopMotor();
//		}
//	}
	
//	public ErrorCode configSelectedFeedbackSensor(FeedbackDevice sns, CtrePidIdxE pidIdx, int canBusTimeoutMs) {
//		ErrorCode ans = ErrorCode.GENERAL_ERROR;
//		if(isFake()) {
//			ans = m_fakeParms.configSelectedFeedbackSensor(sns, pidIdx, canBusTimeoutMs);
//		} else {
//			FeedbackDevice thing1;
//			RemoteFeedbackDevice thing2;
//			thing1 = FeedbackDevice.QuadEncoder;
//			thing2 = RemoteFeedbackDevice.None;
//			ans = m_realObj.configSelectedFeedbackSensor(sns, pidIdx.ePidIdx, canBusTimeoutMs);
//		}
//		return ans;
//	}
	public ErrorCode configSelectedFeedbackSensor(FeedbackDevice feedbackDevice, CtrePidIdxE pidIdx, int timeoutMs) {
		ErrorCode ans;
		ans = m_fakeParms.fake_configSelectedFeedbackSensor(feedbackDevice, pidIdx, timeoutToUse(timeoutMs));
		if(isFake()) {
		} else {
			ans = m_realObj.configSelectedFeedbackSensor(feedbackDevice, pidIdx.ePidIdx, timeoutToUse(timeoutMs));
			int retryCnt = 3;
			while( ( ! ans.equals(ErrorCode.OK)) && retryCnt-->0) {
				P.println(PrtYn.Y, "RETRY due to ErrorCode." + ans.name() + 
						" for configSelectedFeedbackSensor() for " + this.m_namedCntl.name());
				ans = m_realObj.configSelectedFeedbackSensor(feedbackDevice, pidIdx.ePidIdx, timeoutToUse(timeoutMs));
			}
		}
		checkErrorCode(ans);
		return ans;
	}
//	private ErrorCode configSelectedFeedbackSensor(FeedbackDevice feedbackDevice, int pidIdx, int timeoutMs) {
////		int retval = MotControllerJNI.ConfigSelectedFeedbackSensor(m_handle, feedbackDevice.value, pidIdx, timeoutMs);
////		return ErrorCode.valueOf(retval);
//		if(isFake()) {
//			return m_fakeParms.configSelectedFeedbackSensor(feedbackDevice, CtrePidIdxE.findCtrePidIdx(pidIdx), timeoutMs);
//		} else {
//			return m_realObj.configSelectedFeedbackSensor(feedbackDevice, pidIdx, timeoutMs);
//		}
//	}
	public enum CtreSensorPhaseE { MATCHES_MOTOR(true), INVERTED_FROM_MOTOR(false);
		public final boolean eParmForSensorPhaseMethod;
		CtreSensorPhaseE(boolean valueForMethod) { eParmForSensorPhaseMethod = valueForMethod; }
	}
	/**
	 * Sets the phase of the sensor. Use when controller forward/reverse output
	 * doesn't correlate to appropriate forward/reverse reading of sensor.
	 * Pick a value so that positive PercentOutput yields a positive change in sensor.
	 * After setting this, user can freely call SetInvert() with any value.
	 *
	 * @param PhaseSensor
	 *            Indicates whether to invert the phase of the sensor.
	 */
	public void setSensorPhase(CtreSensorPhaseE phase) {
//		MotControllerJNI.SetSensorPhase(m_handle, PhaseSensor);
		m_fakeParms.fake_setSensorPhase(phase);//
		if(isFake()) {
		} else {
			m_realObj.setSensorPhase(phase.eParmForSensorPhaseMethod);
		}
	}
//	private void setSensorPhase(boolean phaseSensor) { //use setSensorPhase(CtreSensorPhase phase) instead
////		MotControllerJNI.SetSensorPhase(m_handle, PhaseSensor);
//		if(isFake()) {
//			m_fakeParms.fake_setSensorPhase(phaseSensor);
//		} else {
//			m_realObj.setSensorPhase(phaseSensor);
//		}
//	}
	/**
	 * Configures the forward nominal output percentage.
	 *
	 * @param percentOut
	 *            Nominal (minimum) percent output. [0,+1]
	 * @param timeoutMs
	 *            Timeout value in ms. If nonzero, function will wait for
	 *            config success and report an error if it times out.
	 *            If zero, no blocking or checking is performed.
	 * @return Error Code generated by function. 0 indicates no error.
	 */
	public ErrorCode configNominalOutputForward(double percentOut, int timeoutMs) {
//		int retval = MotControllerJNI.ConfigNominalOutputForward(m_handle, percentOut, timeoutMs);
//		return ErrorCode.valueOf(retval);
		ErrorCode ans;
		if(isFake()) {
			ans = m_fakeParms.fake_configNominalOutputForward(percentOut, timeoutToUse(timeoutMs));
		} else {
			ans = m_realObj.configNominalOutputForward(percentOut, timeoutMs);
//			//doesn't work? the JNI method returns 0 no matter what??
//			if(ans.equals(ErrorCode.SigNotUpdated) || 
//					ans.equals(ErrorCode.SIG_NOT_UPDATED)) {
//				//assume talon not present
//				P.println(this.m_namedCntl.name() + "got error " + ans.name() + 
//						".  Assuming talon not installed, switching to FAKE talon");
//				configAsFake();
//			}
			int retryCnt = 3;
			while( ( ! ans.equals(ErrorCode.OK)) && retryCnt-->0) {
				P.println(PrtYn.Y, "RETRY due to ErrorCode." + ans.name() + 
						" for configNominalOutputForward() for " + this.m_namedCntl.name());
				ans = m_realObj.configNominalOutputForward(percentOut, timeoutToUse(timeoutMs));
			}
		}
		checkErrorCode(ans);
		return ans;
	}
	public ErrorCode configNominalOutputReverse(double percentOut, int timeoutMs) {
//		int retval = MotControllerJNI.ConfigNominalOutputReverse(m_handle, percentOut, timeoutMs);
//		return ErrorCode.valueOf(retval);
		ErrorCode ans;
		if(isFake()) {
			ans = m_fakeParms.fake_configNominalOutputReverse(percentOut, timeoutToUse(timeoutMs));
		} else {
			ans = m_realObj.configNominalOutputReverse(percentOut, timeoutToUse(timeoutMs));
			int retryCnt = 3;
			while( ( ! ans.equals(ErrorCode.OK)) && retryCnt-->0) {
				P.println(PrtYn.Y, "RETRY due to ErrorCode." + ans.name() + 
						" for configNominalOutputReverse() for " + this.m_namedCntl.name());
				ans = m_realObj.configNominalOutputReverse(percentOut, timeoutToUse(timeoutMs));
			}
		}
		checkErrorCode(ans);
		return ans;
	}
	public ErrorCode configPeakOutputForward(double percentOut, int timeoutMs) {
//		int retval = MotControllerJNI.ConfigPeakOutputForward(m_handle, percentOut, timeoutMs);
//		return ErrorCode.valueOf(retval);
		ErrorCode ans;
		if(isFake()) {
			ans = m_fakeParms.fake_configPeakOutputForward(percentOut, timeoutToUse(timeoutMs));
		} else {
			ans = m_realObj.configPeakOutputForward(percentOut, timeoutToUse(timeoutMs));
			int retryCnt = 3;
			while( ( ! ans.equals(ErrorCode.OK)) && retryCnt-->0) {
				P.println(PrtYn.Y, "RETRY due to ErrorCode." + ans.name() + 
						" for configPeakOutputForward() for " + this.m_namedCntl.name());
				ans = m_realObj.configPeakOutputForward(percentOut, timeoutToUse(timeoutMs));
			}
		}
		checkErrorCode(ans);
		return ans;
	}
	public ErrorCode configPeakOutputReverse(double percentOut, int timeoutMs) {
//		int retval = MotControllerJNI.ConfigPeakOutputReverse(m_handle, percentOut, timeoutMs);
//		return ErrorCode.valueOf(retval);
		ErrorCode ans;
		if(isFake()) {
			ans = m_fakeParms.fake_configPeakOutputReverse(percentOut, timeoutToUse(timeoutMs));
		} else {
			ans = m_realObj.configPeakOutputReverse(percentOut, timeoutToUse(timeoutMs));
			int retryCnt = 3;
			while( ( ! ans.equals(ErrorCode.OK)) && retryCnt-->0) {
				P.println(PrtYn.Y, "RETRY due to ErrorCode." + ans.name() + 
						" for configPeakOutputReverse() for " + this.m_namedCntl.name());
				ans = m_realObj.configPeakOutputReverse(percentOut, timeoutToUse(timeoutMs));
			}
		}
		checkErrorCode(ans);
		return ans;
	}
	
	public ErrorCode config_kF(CtreSlotIdxE slotIdx, double value, int timeoutMs) {
//		int retval = MotControllerJNI.Config_kF(m_handle, slotIdx,  value, timeoutMs);
//		return ErrorCode.valueOf(retval);
		ErrorCode ans;
		if(isFake()) {
			ans = m_fakeParms.fake_config_kF(slotIdx.eSlotIdx, value, timeoutToUse(timeoutMs));
		} else {
			ans = m_realObj.config_kF(slotIdx.eSlotIdx, value, timeoutToUse(timeoutMs));
			int retryCnt = 3;
			while( ( ! ans.equals(ErrorCode.OK)) && retryCnt-->0) {
				P.println(PrtYn.Y, "RETRY due to ErrorCode." + ans.name() + 
						" for config_kF() for " + this.m_namedCntl.name());
				ans = m_realObj.config_kF(slotIdx.eSlotIdx, value, timeoutToUse(timeoutMs));
			}
		}
		checkErrorCode(ans);
		return ans;
	}
	public ErrorCode config_kP(CtreSlotIdxE slotIdx, double value, int timeoutMs) {
//		int retval = MotControllerJNI.Config_kP(m_handle, slotIdx,  value, timeoutMs);
//		return ErrorCode.valueOf(retval);
		ErrorCode ans;
		if(isFake()) {
			ans = m_fakeParms.fake_config_kP(slotIdx.eSlotIdx, value, timeoutToUse(timeoutMs));
		} else {
			ans = m_realObj.config_kP(slotIdx.eSlotIdx, value, timeoutToUse(timeoutMs));
			int retryCnt = 3;
			while( ( ! ans.equals(ErrorCode.OK)) && retryCnt-->0) {
				P.println(PrtYn.Y, "RETRY due to ErrorCode." + ans.name() + 
						" for config_kP() for " + this.m_namedCntl.name());
				ans = m_realObj.config_kP(slotIdx.eSlotIdx, value, timeoutToUse(timeoutMs));
			}
		}
		checkErrorCode(ans);
		return ans;
	}
	public ErrorCode config_kI(CtreSlotIdxE slotIdx, double value, int timeoutMs) {
//		int retval = MotControllerJNI.Config_kI(m_handle, slotIdx,  value, timeoutMs);
//		return ErrorCode.valueOf(retval);
		ErrorCode ans;
		if(isFake()) {
			ans = m_fakeParms.fake_config_kI(slotIdx.eSlotIdx, value, timeoutToUse(timeoutMs));
		} else {
			ans = m_realObj.config_kI(slotIdx.eSlotIdx, value, timeoutToUse(timeoutMs));
			int retryCnt = 3;
			while( ( ! ans.equals(ErrorCode.OK)) && retryCnt-->0) {
				P.println(PrtYn.Y, "RETRY due to ErrorCode." + ans.name() + 
						" for config_kI() for " + this.m_namedCntl.name());
				ans = m_realObj.config_kI(slotIdx.eSlotIdx, value, timeoutToUse(timeoutMs));
			}
		}
		checkErrorCode(ans);
		return ans;
	}
	public ErrorCode config_kD(CtreSlotIdxE slotIdx, double value, int timeoutMs) {
//		int retval = MotControllerJNI.Config_kD(m_handle, slotIdx,  value, timeoutMs);
//		return ErrorCode.valueOf(retval);
		ErrorCode ans;
		if(isFake()) {
			ans = m_fakeParms.fake_config_kD(slotIdx.eSlotIdx, value, timeoutToUse(timeoutMs));
		} else {
			ans = m_realObj.config_kD(slotIdx.eSlotIdx, value, timeoutToUse(timeoutMs));
			int retryCnt = 3;
			while( ( ! ans.equals(ErrorCode.OK)) && retryCnt-->0) {
				P.println(PrtYn.Y, "RETRY due to ErrorCode." + ans.name() + 
						" for config_kD() for " + this.m_namedCntl.name());
				ans = m_realObj.config_kD(slotIdx.eSlotIdx, value, timeoutToUse(timeoutMs));
			}
		}
		checkErrorCode(ans);
		return ans;
	}
	/**
	 * Sets the allowable closed-loop error in the given parameter slot.
	 *
	 * @param slotIdx
	 *            Parameter slot for the constant.
	 * @param allowableClosedLoopError
	 *            Value of the allowable closed-loop error.
	 * @param timeoutMs
	 *            Timeout value in ms. If nonzero, function will wait for
	 *            config success and report an error if it times out.
	 *            If zero, no blocking or checking is performed.
	 * @return Error Code generated by function. 0 indicates no error.
	 */
	public ErrorCode configAllowableClosedloopError(CtreSlotIdxE slotIdx, int allowableClosedLoopError, int timeoutMs) {
//		int retval = MotControllerJNI.ConfigAllowableClosedloopError(m_handle, slotIdx, allowableClosedLoopError,
//				timeoutMs);
//		return ErrorCode.valueOf(retval);
		ErrorCode ans;
		if(isFake()) {
			ans = m_fakeParms.fake_configAllowableClosedloopError(slotIdx, allowableClosedLoopError, timeoutToUse(timeoutMs));
		} else {
			ans = m_realObj.configAllowableClosedloopError(slotIdx.eSlotIdx, allowableClosedLoopError, timeoutToUse(timeoutMs));
			int retryCnt = 3;
			while( ( ! ans.equals(ErrorCode.OK)) && retryCnt-->0) {
				P.println(PrtYn.Y, "RETRY due to ErrorCode." + ans.name() + 
						" for configAllowableClosedloopError() for " + this.m_namedCntl.name());
				ans = m_realObj.configAllowableClosedloopError(slotIdx.eSlotIdx, allowableClosedLoopError, timeoutToUse(timeoutMs));
			}
		}
		checkErrorCode(ans);
		return ans;
	}

	public ErrorCode configOpenloopRamp(double secondsFromNeutralToFull, int timeoutMs) {
//		int retval = MotControllerJNI.ConfigOpenLoopRamp(m_handle, secondsFromNeutralToFull, timeoutMs);
//		return ErrorCode.valueOf(retval);
		ErrorCode ans;
		if(isFake()) {
			ans = m_fakeParms.fake_configOpenloopRamp(secondsFromNeutralToFull, timeoutToUse(timeoutMs));
		} else {
			ans = m_realObj.configOpenloopRamp(secondsFromNeutralToFull, timeoutToUse(timeoutMs));
			int retryCnt = 3;
			while( ( ! ans.equals(ErrorCode.OK)) && retryCnt-->0) {
				P.println(PrtYn.Y, "RETRY due to ErrorCode." + ans.name() + 
						" for configOpenloopRamp() for " + this.m_namedCntl.name());
				ans = m_realObj.configOpenloopRamp(secondsFromNeutralToFull, timeoutToUse(timeoutMs));
			}
		}
		checkErrorCode(ans);
		return ans;
	}
	public ErrorCode configClosedloopRamp(double secondsFromNeutralToFull, int timeoutMs) {
//		int retval = MotControllerJNI.ConfigClosedLoopRamp(m_handle, secondsFromNeutralToFull, timeoutMs);
//		return ErrorCode.valueOf(retval);
		ErrorCode ans;
		if(isFake()) {
			ans = m_fakeParms.fake_configClosedloopRamp(secondsFromNeutralToFull, timeoutToUse(timeoutMs));
		} else {
			ans = m_realObj.configClosedloopRamp(secondsFromNeutralToFull, timeoutToUse(timeoutMs));
			int retryCnt = 3;
			while( ( ! ans.equals(ErrorCode.OK)) && retryCnt-->0) {
				P.println(PrtYn.Y, "RETRY due to ErrorCode." + ans.name() + 
						" for configClosedloopRamp() for " + this.m_namedCntl.name());
				ans = m_realObj.configClosedloopRamp(secondsFromNeutralToFull, timeoutToUse(timeoutMs));
			}
		}
		checkErrorCode(ans);
		return ans;
	}
	
	public int getDeviceID() {
//		return MotControllerJNI.GetDeviceNumber(m_handle);
		if(isFake()) {
			return m_fakeParms.fake_getDeviceID();
		} else {
			return m_realObj.getDeviceID();
		}
	}
	
	public ErrorCode getLastError() {
		if(isFake()) {
			return m_fakeParms.fake_getLastError();
		} else {
			return m_realObj.getLastError();
		}
	}
	
	/**
	 * not a standard TalonSRX function
	 * @param errCode
	 */
	protected void checkErrorCode(ErrorCode errCode) {
		checkErrorCode(errCode, (Tm744Opts.isOptRunStf() ? true : false));
	}
	protected void checkErrorCode(ErrorCode errCode, boolean becomeFakeIfError) {
		if( ! errCode.equals(ErrorCode.OK)) {
			P.println(Tt.getClassName(this) + " saw ErrorCode " + errCode.value + 
					" (" + errCode.name() + ") for " + this.m_namedCntl + 
					" (CAN id " + this.m_namedConnEnt.getConnectionFrcIndex() + ")");
			if(becomeFakeIfError && (errCode.equals(ErrorCode.SIG_NOT_UPDATED) || 
										errCode.equals(ErrorCode.SigNotUpdated))) {
				configAsFake();
			}
		}
		if( ! getLastError().equals(ErrorCode.OK)) {
			P.println("CTRE code saw ErrorCode " + errCode.value + 
					" (" + errCode.name() + ") for " + this.m_namedCntl + 
					" (CAN id " + this.m_namedConnEnt.getConnectionFrcIndex() + ")");
			if(becomeFakeIfError && (errCode.equals(ErrorCode.SIG_NOT_UPDATED) || errCode.equals(ErrorCode.SigNotUpdated)
//CTR code reports these for drive motors, but they don't get propagated to Java code :(
//					|| errCode.equals(ErrorCode.RxTimeout) || errCode.equals(ErrorCode.CAN_MSG_NOT_FOUND) 
//					|| errCode.equals(ErrorCode.FirmVersionCouldNotBeRetrieved)
					)) {
				configAsFake();
			}
		}
	}
	

	//implemented so Fake CAN Talons can properly imitate the behavior of the real Talon and Encoder
	public enum EncoderPolarityE {
		MATCHES_MOTOR(1), OPPOSITE_OF_MOTOR(-1), TBD(0);
		private final int eDirectionFactor;
		private EncoderPolarityE(int polarityFactor) {
			eDirectionFactor = polarityFactor;
		}
	}
	public enum EncoderCountsCapabilityE {
		ABSOLUTE_USED_AS_ABSOLUTE, //absolute encoder, ignore requests to reset or set value
		ABSOLUTE_USED_AS_RELATIVE, //absolute encoder, implement resets and sets using c_countSnapshot
		RELATIVE, //relative encoder, hardware processes resets and sets appropriately
		TBD;
		public boolean isAbsoluteUsedAsAbsolute() { return this.equals(ABSOLUTE_USED_AS_ABSOLUTE); }
		public boolean isAbsoluteUsedAsRelative() { return this.equals(ABSOLUTE_USED_AS_RELATIVE); }
		public boolean isRelative() { return this.equals(RELATIVE); }
	}
	
	/**
	 * this class is an attempt to make encoder connected to CAN TalonSRX look like a standard
	 * FRC Encoder.
	 * @author JudiA
	 *
	 */
	@Deprecated //better to use CTRE's interfaces to access the encoder info
	private class ConnectedEncoder {
		private static final int DEFAULT_COUNTS_PER_REVOLUTION = 4096;
		private static final double DEFAULT_FEET_PER_REVOLUTION = 1.0;
		private static final double DEFAULT_MAX_RPS = 5200;
		
		private RoNamedControlsE c_mtrNamedCntl;
		private SdKeysE c_sdKeyPosition;
		private double c_feetPerRevolution = DEFAULT_FEET_PER_REVOLUTION;
		private int c_countsPerRevolution = DEFAULT_COUNTS_PER_REVOLUTION;
		private double c_maxRps = DEFAULT_MAX_RPS;
		private EncoderPolarityE c_encPolarityRelToMotor;
		private boolean c_isOnFakeTalon = false;
		private EncoderCountsCapabilityE c_encCountsCapability;
		private int c_countsSnapshot;
		
		//for use by fake encoder code
		private boolean c_hasMaxMinLimits = false;
		private int c_minAllowedCounts = 0;
		private int c_maxAllowedCounts = 0;
		
		TmFakeable_CanTalonSrx c_fakeableCanTalonSrxInstance = null;
		
		//magnetic encoder is connected to a CAN TalonSRX controller and accessed through it.
		
		/**
		 * 
		 * @param mtrCfg - allows a Fake CAN Talon to properly emulate an encoder attached to the real CAN Talon
		 * @param encoderSdKeyPosition - used to post the encoder "position" on smartdashboard
		 * @param maxRevPerSec - allows a Fake CAN Talon to properly emulate an encoder attached to the real CAN Talon
		 * @param countsPerRevolution - used to calculate distances from encoder position
		 * @param feetPerRevolution - used to calculate distances from encoder position
		 * @param encPolarityRelToMotor - allows a Fake CAN Talon to properly emulate an encoder "attached" to it,
		 *                                available for use by subsystems that use the encoder values
		 * @param encCountsCapability - indicates whether this is an "absolute" encoder that can't be reset to 0                              
		 */
		public ConnectedEncoder(RoNamedControlsE mtrNamedCntl, TmFakeable_CanTalonSrx talonInstance, 
								SdKeysE encoderSdKeyPosition, 
								double maxRevPerSec,
								int countsPerRevolution, double feetPerRevolution, 
								EncoderPolarityE encPolarityRelToMotor,
								EncoderCountsCapabilityE encCountsCapability) {
			c_mtrNamedCntl = mtrNamedCntl;
			c_sdKeyPosition = encoderSdKeyPosition;
			c_maxRps = maxRevPerSec;
			c_feetPerRevolution = feetPerRevolution;
			c_countsPerRevolution = countsPerRevolution;
			c_encPolarityRelToMotor = encPolarityRelToMotor;
			c_encCountsCapability = encCountsCapability;
			c_countsSnapshot = 0;
			c_fakeableCanTalonSrxInstance = talonInstance;
			c_isOnFakeTalon = c_mtrNamedCntl.getEnt().isFake(); //if control config'd as fake //isOnFakeCanTalonSrx();
			if(true || c_isOnFakeTalon) {
				c_fakeableCanTalonSrxInstance.setFakeEncoder(this);
			}
		}
		

		private int getEncoderPolarityFactor() { return c_encPolarityRelToMotor.eDirectionFactor; }
		
//		/**
//		 * get the instance of TmFakeable_CanTalonSrx to which this encoder is connected
//		 * @return
//		 */
//		private TmFakeable_CanTalonSrx getCanTalonSrxMotorObj() {
//			return this.c_fakeableCanTalonSrxInstance;
//		}
		
//		public MotorConfigE getMotorCfg() { return c_mtrCfg; }
		
//		private boolean isOnFakeCanTalonSrx() {
//			boolean ans = false;
//			if(c_mtrNamedCntl.getEnt().isFake()) {
//				getCanTalonSrxMotorObj().setFakeEncoder(this);
//				ans = true;
//			}
//			return ans;
//		}
		
//		/**
//		 * @return counts per revolution for the installed encoder
//		 */
//		private int getCpr() {
//			return c_countsPerRevolution;
//		}
//
//		private double toDistance(int encoderCounts) {
//			double ans = 0;
//			double ticks = encoderCounts;
//			ans = ticks / c_countsPerRevolution * c_feetPerRevolution;
//			
//			return ans;
//		}
//
//		private double getDistance() {
//			int get = get();
//			return toDistance(get);
//		}
//		
//		private double getDistanceAdjustedForPolarity() {
//			double ans;
//			double motorPolarityFactor = 
//					c_mtrNamedCntl.getEnt().cMtrInversion.isInvertMotor() ? -1.0 : 1.0; //.getMultiplierForDrvMtrPolarity();
//			ans = motorPolarityFactor * getDistance();
//			return ans;
//		}

		
//		private synchronized int get() {
//			int ans;
//			ans = (getCanTalonSrxMotorObj()==null) ? 0 : 
//						getCanTalonSrxMotorObj().getSensorCollection().getQuadraturePosition();
//			if(c_encCountsCapability.isAbsoluteUsedAsRelative()) { ans -= c_countsSnapshot; }
//			return ans;
//		}
//		
//		private void reset() {
//		}
//
//		private void postToSdPosition() {
//			if(c_sdKeyPosition != null) {
////				TmPostToSd.dbgPutNumber(c_sdKeyPosition, get());
//				TmSdMgr.putNumber(c_sdKeyPosition, get());
//			}
//		}
	} //end ConnectedEncoder class

	@Override
	public boolean isFakeableItem() { return true; }
	
	public static int m_fakeCount = 0;
	@Override
	public void configAsFake() {
		P.println(Tt.getClassName(this) + " " + m_namedCntl.name() +
				" is being configured as a FAKE TalonSRX -- CAN id " + 
				m_namedCntlEnt.cNamedMod.getEnt().cModNamedConn.getEnt().getConnectionFrcIndex());
//		m_realObj = null; //keep in case need to make active again
		m_beingFaked = true;
		m_fakeCount++;

		boolean isCntlInList = false;
		for(RoNamedControlsE rnc : m_fakeCntlsList) {
			if(rnc.equals(m_namedCntl)) {
				isCntlInList = true;
			}
		}
		if( ! isCntlInList) {
			if(m_fakeTalonsMsg.equals("")) {
				m_fakeTalonsMsg = "FAKE TalonSrx cntls: " + m_namedCntl.name();
			} else {
				m_fakeTalonsMsg += ", " + m_namedCntl.name();
			}
		}
		if( ! m_fakeTalonsMsg.equals("")) {
			TmSdMgr.putString(SdKeysE.KEY_MISC_FAKE_CAN_TALONS, m_fakeTalonsMsg);
		}
	}
	@Override
	public boolean isFake() {
		boolean ans;
		if(m_beingFaked) {
			ans = true; //good debug breakpoint
		} else {
			ans = false; //good debug breakpoint
		}
		return ans; //m_beingFaked;
	}
	@Override
	public void doForcedInstantiation() {
	}
	@Override
	public void doPopulate() {
	}
	
}
