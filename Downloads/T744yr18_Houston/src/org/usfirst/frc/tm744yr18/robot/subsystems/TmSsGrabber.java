package org.usfirst.frc.tm744yr18.robot.subsystems;

import org.usfirst.frc.tm744yr18.robot.cmdGroups.TmCCGrpGrabberDownClampedNoWedge;
import org.usfirst.frc.tm744yr18.robot.cmdGroups.TmCCGrpGrabberUpClampedNoWedge;
import org.usfirst.frc.tm744yr18.robot.cmdGroups.TmCCGrpGrabberUpClampedWithWedge;
import org.usfirst.frc.tm744yr18.robot.config.TmHdwrDsCntls.DsNamedControlsE;
import org.usfirst.frc.tm744yr18.robot.config.TmHdwrRoCntls.RoNamedControlsE;
import org.usfirst.frc.tm744yr18.robot.config.TmSdKeysI.SdKeysE;
import org.usfirst.frc.tm744yr18.robot.devices.TmFakeable_CanTalonSrx;
import org.usfirst.frc.tm744yr18.robot.devices.TmFakeable_DoubleSolenoid;
import org.usfirst.frc.tm744yr18.robot.devices.TmFakeable_RoDigitalInput;
import org.usfirst.frc.tm744yr18.robot.exceptions.TmExceptions;
import org.usfirst.frc.tm744yr18.robot.devices.TmFakeable_CanTalonSrx.CtreMotorInvertedE;
import org.usfirst.frc.tm744yr18.robot.helpers.TmDriverStation;
import org.usfirst.frc.tm744yr18.robot.helpers.TmSdMgr;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmDsControlUserI;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmRoControlUserI;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmStdSubsystemI;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmToolsI;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmToolsI.P.PrtYn;
import org.usfirst.frc.tm744yr18.robot.subsystems.TmSsGrabber.GrabberMotionAndWedgeMgmt.*;

import com.ctre.phoenix.motorcontrol.ControlMode;

import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.command.Subsystem;
import t744opts.Tm744Opts.OptDefaults;

public class TmSsGrabber extends Subsystem implements TmStdSubsystemI, TmToolsI, TmDsControlUserI, TmRoControlUserI {
	
	static TmDriverStation m_tds = TmDriverStation.getInstance();

	public enum GrabberCubeClampAssistE { 
		CLAMPED(DoubleSolenoid.Value.kForward),
		UNCLAMPED(DoubleSolenoid.Value.kReverse),
		OFF(DoubleSolenoid.Value.kOff),
		;
		private final DoubleSolenoid.Value eDirection;
		public DoubleSolenoid.Value getSolDirection() { return eDirection; }
		private GrabberCubeClampAssistE(DoubleSolenoid.Value dir) { eDirection = dir; }
	};
	private static GrabberCubeClampAssistE m_clampAssistCurrentPosition;
//	private static GrabberCubeClampAssistE m_clampPrevPosition;

	private static TmFakeable_DoubleSolenoid m_cubeClampAssistBothSides;
//	private static TmFakeable_DoubleSolenoid m_cubeClampAssistRight;

	public enum GrabberMotorStateE { //CONFIG ME //TODO
		GRABBING(0.5, -0.5),
		RELEASING(-0.5, 0.5),
		DRIVER_CONTROL(0, 0), //uses input from driver station
		OFF(0, 0),
		;
		protected double eMtrPercentOutLeft;
		protected double eMtrPercentOutRight;
		
		private GrabberMotorStateE(double leftMtrPercentOut, double rightMtrPercentOut) {
			eMtrPercentOutLeft = leftMtrPercentOut; 
			eMtrPercentOutRight = rightMtrPercentOut;
		}
	};
	static TmFakeable_CanTalonSrx m_clampMtrLeft; //see GrabberMotorStateE
	static TmFakeable_CanTalonSrx m_clampMtrRight;
	static Timer m_clampReleaseTimer;
	static double m_motorsOffTime = 0;
	//turn motors off once release has been requested, but not immediately
	static final double GRABBER_MOTOR_RELEASE_TIME_SECS = 0.250;
	static GrabberMotorStateE m_currentMotorState = GrabberMotorStateE.OFF;
		

	public enum GrabberCubeLiftE { 
		RAISED(DoubleSolenoid.Value.kForward),
		LOWERED(DoubleSolenoid.Value.kReverse),
		OFF(DoubleSolenoid.Value.kOff),
		;
		private final DoubleSolenoid.Value eDirection;
		public DoubleSolenoid.Value getSolDirection() { return eDirection; }
		private GrabberCubeLiftE(DoubleSolenoid.Value dir) { eDirection = dir; }
	};
	private static GrabberCubeLiftE m_liftCurrentPosition;
//	private static GrabberCubeLiftE m_LiftPrevPosition;

	private static TmFakeable_DoubleSolenoid m_cubeLiftBothSides;
//	private static TmFakeable_DoubleSolenoid m_cubeLiftRight;
	
	private static TmFakeable_RoDigitalInput m_grabberUpFullLimitSwitch = null; //GRABBER_UP_FULL_LIMIT_SWITCH
	
	public enum GrabberWedgeE { 
		DEPLOYED(DoubleSolenoid.Value.kForward),
		RETRACTED(DoubleSolenoid.Value.kReverse),
		OFF(DoubleSolenoid.Value.kOff),
		;
		private final DoubleSolenoid.Value eDirection;
		public DoubleSolenoid.Value getSolDirection() { return eDirection; }
		private GrabberWedgeE(DoubleSolenoid.Value dir) { eDirection = dir; }
	};
	private static GrabberWedgeE m_wedgeCurrentPosition;
//	private static GrabberWedgeE m_wedgePrevPosition;

	private static TmFakeable_DoubleSolenoid m_wedge; //see GrabberLiftWedgeE
	private static TmFakeable_RoDigitalInput m_wedgeRetractedLimitSwitch = null;
	//KEY_GRABBER_WEDGE_LIMIT_SWITCH

	
	
	
	private static TmSsGrabber m_instance;
	public static synchronized TmSsGrabber getInstance() {
		if (m_instance == null) {
			m_instance = new TmSsGrabber();
		}
		return m_instance;
	}
		

	@Override
	public boolean isFakeableItem() { return false; }

	@Override
	public void configAsFake() {}

	@Override
	public boolean isFake() { return false; }

	@Override
	public void sssDoInstantiate() {
	}

	@Override
	public void sssDoRobotInit() {
		m_cubeClampAssistBothSides = new TmFakeable_DoubleSolenoid(this,
				RoNamedControlsE.GRABBER_CLAMP_ASSIST_BOTH_SIDES_OPEN__PISTON_RETRACTED, 
				RoNamedControlsE.GRABBER_CLAMP_ASSIST_BOTH_SIDES_CLOSE__PISTON_EXTENDED);	
		m_clampAssistCurrentPosition = GrabberCubeClampAssistE.OFF;
		
		m_cubeLiftBothSides = new TmFakeable_DoubleSolenoid(this,
				RoNamedControlsE.GRABBER_LIFT_BOTH_SIDES_RAISE, 
				RoNamedControlsE.GRABBER_LIFT_BOTH_SIDES_LOWER);	
		m_liftCurrentPosition = GrabberCubeLiftE.OFF;
		m_grabberUpFullLimitSwitch  = new TmFakeable_RoDigitalInput(this, 
				RoNamedControlsE.GRABBER_UP_FULL_LIMIT_SWITCH, ! Cnst.LIMIT_SWITCHES_TRIPPED_STATE );
		
		m_wedge = new TmFakeable_DoubleSolenoid(this,
				RoNamedControlsE.GRABBER_WEDGE_DEPLOY,
				RoNamedControlsE.GRABBER_WEDGE_RETRACT);
		m_wedgeCurrentPosition = GrabberWedgeE.OFF;
		m_wedgeRetractedLimitSwitch = new TmFakeable_RoDigitalInput(this, 
				RoNamedControlsE.GRABBER_WEDGE_RETRACTED_LIMIT_SWITCH, ! Cnst.LIMIT_SWITCHES_TRIPPED_STATE );
		
		m_clampMtrLeft = new TmFakeable_CanTalonSrx(this, RoNamedControlsE.GRABBER_MOTOR_LEFT);
		m_clampMtrRight = new TmFakeable_CanTalonSrx(this, RoNamedControlsE.GRABBER_MOTOR_RIGHT);
		configMotor(m_clampMtrLeft);
		configMotor(m_clampMtrRight);
		
		m_clampReleaseTimer = new Timer();
		m_motorsOffTime = 0;
		
		synchronized(GrabberMotionAndWedgeMgmt.gmwmLock) {
			GrabberMotionAndWedgeMgmt.updateGrabberCubeLift(GrabberCubeLiftE.RAISED);
			m_gmwm.setGmwSteMachState(GrabberMotionAndWedgeSteMachStatesE.IDLE);
		}
		
		DsNamedControlsE.GRABBER_DOWN_CLAMP_NO_WEDGE_BTN.getEnt().whenPressed(this, 
				new TmCCGrpGrabberDownClampedNoWedge());
		DsNamedControlsE.GRABBER_FULL_UP_AND_CLAMPED_BTN.getEnt().whenPressed(this, 
				new TmCCGrpGrabberUpClampedNoWedge()); //TmCCmdGrabberSetPositionAndWedge(GrabberMotionAndWedgeRequestsE.REQ_GRABBER_UP_NO_WEDGE));
		DsNamedControlsE.GRABBER_PARTIAL_UP_AND_CLAMPED_BTN.getEnt().whenPressed(this, 
				new TmCCGrpGrabberUpClampedWithWedge()); //TmCCmdGrabberSetPositionAndWedge(GrabberMotionAndWedgeRequestsE.REQ_GRABBER_UP_WITH_WEDGE));

		DsNamedControlsE.GRABBER_MOTORS_GRABBING_INPUT.getEnt().registerAsDsCntlUser(this);
		DsNamedControlsE.GRABBER_MOTORS_RELEASING_INPUT.getEnt().registerAsDsCntlUser(this);
	}
	protected static synchronized void configMotor(TmFakeable_CanTalonSrx mtr) {
		if(mtr.m_namedCntlEnt.cMtrInversion.isInvertMotor()) { mtr.setInverted(CtreMotorInvertedE.INVERTED); }
		
        /* set the peak and nominal outputs, 12V means full */
        mtr.configNominalOutputForward(0.0, Cnst.CTRE_TIMEOUT_MS_WAIT_FOR_MOTOR_CFG);
        mtr.configNominalOutputReverse(0.0, Cnst.CTRE_TIMEOUT_MS_WAIT_FOR_MOTOR_CFG);
        mtr.configPeakOutputForward(1.0, Cnst.CTRE_TIMEOUT_MS_WAIT_FOR_MOTOR_CFG);
        mtr.configPeakOutputReverse(-1.0, Cnst.CTRE_TIMEOUT_MS_WAIT_FOR_MOTOR_CFG);
        
		mtr.configOpenloopRamp(Cnst.VOLTAGE_RAMP_TIME_SECS, Cnst.CTRE_TIMEOUT_MS_NO_WAIT_FOR_COMPLETION);
		
		mtr.configContinuousCurrentLimit(mtr.m_namedCntlEnt.cMaxContinuousAmps.value, Cnst.CTRE_TIMEOUT_MS_NO_WAIT_FOR_COMPLETION);
		mtr.configPeakCurrentLimit(mtr.m_namedCntlEnt.cMaxPeakAmps.value, Cnst.CTRE_TIMEOUT_MS_NO_WAIT_FOR_COMPLETION);
		mtr.configPeakCurrentDuration(mtr.m_namedCntlEnt.cMaxPeakAmpsDurationMs.value, Cnst.CTRE_TIMEOUT_MS_NO_WAIT_FOR_COMPLETION);
	}
	private class Cnst {
		public static final boolean LIMIT_SWITCHES_TRIPPED_STATE = false;
		public static final double VOLTAGE_RAMP_TIME_SECS = 0.100;
		public static final int CTRE_TIMEOUT_MS_NO_WAIT_FOR_COMPLETION = 0;
		public static final int CTRE_TIMEOUT_MS_WAIT_FOR_MOTOR_CFG = 20;
	}

	@Override
	public void sssDoDisabledInit() {
		updateGrabberMotorState(GrabberMotorStateE.OFF);
		updateGrabberCubeClampAssist(GrabberCubeClampAssistE.CLAMPED);
		synchronized(GrabberMotionAndWedgeMgmt.gmwmLock) {
			GrabberMotionAndWedgeMgmt.updateGrabberCubeLift(GrabberCubeLiftE.RAISED);
			m_gmwm.setGmwSteMachState(GrabberMotionAndWedgeSteMachStatesE.IDLE);
		}
		//not safe here...: updateGrabberWedge(GrabberWedgeE.RETRACTED);
	}

	@Override
	public void sssDoAutonomousInit() {
		updateGrabberMotorState(GrabberMotorStateE.OFF);
		updateGrabberCubeClampAssist(GrabberCubeClampAssistE.CLAMPED);
		synchronized(GrabberMotionAndWedgeMgmt.gmwmLock) {
			GrabberMotionAndWedgeMgmt.updateGrabberCubeLift(GrabberCubeLiftE.RAISED);
			m_gmwm.setGmwSteMachState(GrabberMotionAndWedgeSteMachStatesE.IDLE);
		}
		//not safe here...: updateGrabberWedge(GrabberWedgeE.RETRACTED);
	}

	@Override
	public void sssDoTeleopInit() {
		updateGrabberMotorState(GrabberMotorStateE.OFF);
		synchronized(m_grabberLock) {
			updateGrabberCubeClampAssist(GrabberCubeClampAssistE.CLAMPED);
			gcaSteMachState = GrabberClampAssistStateMachE.TELEOP_INIT;
		}
		synchronized(GrabberMotionAndWedgeMgmt.gmwmLock) {
			m_gmwm.setGmwSteMachState(GrabberMotionAndWedgeSteMachStatesE.IDLE);
		}
		//not safe here...: updateGrabberWedge(GrabberWedgeE.RETRACTED);
	}

	@Override
	public void sssDoLwTestInit() {}

	@Override
	public void sssDoRobotPeriodic() {
		synchronized(m_grabberLock) {
			if(m_motorsOffTime > 0) {
				if(m_clampReleaseTimer.get() >= m_motorsOffTime) {
					updateGrabberMotorState(GrabberMotorStateE.OFF);
					m_motorsOffTime = 0;
				}
			}
		}
		TmSdMgr.putBoolean(SdKeysE.KEY_GRABBER_UP_FULL_LIMIT_SWITCH, 
				TmSsGrabber.isGrabberUpFullLimitSwitchTripped());
	}

	@Override
	public void sssDoDisabledPeriodic() {
		postToSd();
	}

	@Override
	public void sssDoAutonomousPeriodic() {
		runGrabberMotors();
		synchronized(GrabberMotionAndWedgeMgmt.gmwmLock) {
			m_gmwm.runGrabberMotionAndWedgeSteMach();
		}
//		postToSd();
	}

	@Override
	public void sssDoTeleopPeriodic() {
		runGrabberMotors();
		runGrabberClampAssist();
		synchronized(GrabberMotionAndWedgeMgmt.gmwmLock) {
			m_gmwm.runGrabberMotionAndWedgeSteMach();
		}
//		postToSd();
	}

	@Override
	public void sssDoLwTestPeriodic() {}
	
		
	private static final Object m_grabberLock = new Object();
	public static void updateGrabberCubeClampAssist(GrabberCubeClampAssistE requestedAction)
	{
		synchronized(m_grabberLock) {
			if(requestedAction.equals(GrabberCubeClampAssistE.UNCLAMPED))
			{
				m_cubeClampAssistBothSides.set(GrabberCubeClampAssistE.UNCLAMPED.getSolDirection());
				m_clampAssistCurrentPosition = GrabberCubeClampAssistE.UNCLAMPED;
			}
			else if(requestedAction.equals(GrabberCubeClampAssistE.CLAMPED))
			{
				m_cubeClampAssistBothSides.set(GrabberCubeClampAssistE.CLAMPED.getSolDirection());
				m_clampAssistCurrentPosition = GrabberCubeClampAssistE.CLAMPED;
			}
			else if(requestedAction.equals(GrabberCubeClampAssistE.OFF))
			{
				m_cubeClampAssistBothSides.set(GrabberCubeClampAssistE.UNCLAMPED.getSolDirection());
				m_clampAssistCurrentPosition = GrabberCubeClampAssistE.UNCLAMPED;
			}
		}
		P.println(PrtYn.Y, "Grabber cube clamp assist set to " + m_clampAssistCurrentPosition.name());
		postToSd();
	}
	
	boolean grabberUnclampBtnPrev;
	private enum GrabberClampAssistStateMachE {
		TELEOP_INIT, UNCLAMP_BTN_PRESSED, UNCLAMP_BTN_RELEASED;
	}
	private GrabberClampAssistStateMachE gcaSteMachState = GrabberClampAssistStateMachE.TELEOP_INIT;
	
	/**
	 * state machine for teleop that keeps the clamp-assist closed except while a button is held down
	 */
	public void runGrabberClampAssist() {
		if(m_tds.isEnabledTeleop()) {
			synchronized(m_grabberLock) {
				DsNamedControlsE namedControl = null; 
//				if (OptDefaults.MAIN_XBOX_CONTROLLER_ATTACHED){
					namedControl = DsNamedControlsE.GRABBER_UNCLAMP_WHILE_HELD;
//				}
//				if(namedControl != null) {
					boolean btnCurState = namedControl.getEnt().getButton(); //DsControlsMgr.getButton(DsNamedControlsE.GRABBER_UNCLAMP.getEnt());

					switch(gcaSteMachState) {
					case TELEOP_INIT:
						if(btnCurState==true) {
							updateGrabberCubeClampAssist(GrabberCubeClampAssistE.UNCLAMPED);
							gcaSteMachState = GrabberClampAssistStateMachE.UNCLAMP_BTN_PRESSED;
						} else {
							updateGrabberCubeClampAssist(GrabberCubeClampAssistE.CLAMPED);
							gcaSteMachState = GrabberClampAssistStateMachE.UNCLAMP_BTN_RELEASED;
						}
						//					grabberUnclampBtnPrev = btnCurState;
						break;
					case UNCLAMP_BTN_PRESSED:
						if(btnCurState != grabberUnclampBtnPrev) {
							//button must have been released
							P.println(PrtYn.Y, namedControl.name() + " button (" +
									namedControl.getEnt().cNamedConn.name() + 
									") ---- released");
							updateGrabberCubeClampAssist(GrabberCubeClampAssistE.CLAMPED);
							gcaSteMachState = GrabberClampAssistStateMachE.UNCLAMP_BTN_RELEASED;
						}
						break;
					case UNCLAMP_BTN_RELEASED:
						if(btnCurState != grabberUnclampBtnPrev) {
							//button must have been pressed
							P.println(PrtYn.Y, namedControl.name() + " button (" +
									namedControl.getEnt().cNamedConn.name() + 
									") ---- pressed");
							updateGrabberCubeClampAssist(GrabberCubeClampAssistE.UNCLAMPED);
							gcaSteMachState = GrabberClampAssistStateMachE.UNCLAMP_BTN_PRESSED;
						}
						break;
						//default:
						//break;
					}
					grabberUnclampBtnPrev = btnCurState;
//				}
			}
		}
	}
	protected static boolean isWedgeRetracted() {
		boolean ans;
		synchronized(GrabberMotionAndWedgeMgmt.gmwmLock) {
			if(OptDefaults.WEDGE_LIMIT_SWITCH_INSTALLED) {
				ans = isWedgeRetractedLimitSwitchTripped();
			} else {
				ans = (m_wedgeCurrentPosition == GrabberWedgeE.RETRACTED);
			}
		}
		return ans;
	}
	
	protected static boolean isWedgeRetractedLimitSwitchTripped() {
		boolean ans;
		synchronized(GrabberMotionAndWedgeMgmt.gmwmLock) {
			if(OptDefaults.WEDGE_LIMIT_SWITCH_INSTALLED) {
				ans = m_wedgeRetractedLimitSwitch.get()==Cnst.LIMIT_SWITCHES_TRIPPED_STATE;
			} else {
				ans = (m_wedgeCurrentPosition.equals(GrabberWedgeE.RETRACTED));
			}
		}
		return ans;
	}
	
	protected static boolean isGrabberUpFullLimitSwitchTripped() {
		boolean ans;
		synchronized(GrabberMotionAndWedgeMgmt.gmwmLock) {
			//this limit switch doesn't work the way past ones did...
			ans = m_grabberUpFullLimitSwitch.get()== ! Cnst.LIMIT_SWITCHES_TRIPPED_STATE;
//			if(OptDefaults.GRABBER_UP_LIMIT_SWITCH_INSTALLED) {
//				ans = m_grabberUpFullLimitSwitch.get()==Cnst.LIMIT_SWITCHES_TRIPPED_STATE;
//			} else {
//				ans = false; //(m_liftCurrentPosition.equals(GrabberCubeLiftE.RAISED));
//			}
		}
		return ans;
	}
	
	public static boolean isGrabberUp() {
		boolean ans;
		synchronized(GrabberMotionAndWedgeMgmt.gmwmLock) {
			if(OptDefaults.GRABBER_UP_LIMIT_SWITCH_INSTALLED) {
				ans = isGrabberUpFullLimitSwitchTripped();
			} else {
				ans = (m_liftCurrentPosition.equals(GrabberCubeLiftE.RAISED));
			}
		}
		return ans;
	}
	
	public static boolean isInState(GrabberMotionAndWedgeRequestsE targetState) {
		boolean ans = false;
		synchronized(GrabberMotionAndWedgeMgmt.gmwmLock) {
			if(targetState==null) {
				ans = false;
			} else {
				switch(targetState) {
				case REQ_GRABBER_DOWN:
					if( ! isGrabberUp()) {
						ans = true;
					}
					break;
				case REQ_GRABBER_DOWN_WEDGE_RETRACT:
					if( ( ! isGrabberUp()) && isWedgeRetracted()) {
						ans = true;
					}
					break;
				case REQ_GRABBER_UP:
					if(isGrabberUp()) {
						ans = true;
					}
					break;
				case REQ_GRABBER_UP_NO_WEDGE:
					if( isGrabberUp() && isWedgeRetracted()) {
						ans = true;
					}
					break;
				case REQ_GRABBER_UP_WITH_WEDGE:
					if( isGrabberUp() && ( ! isWedgeRetracted())) {
						ans = true;
					}
					break;
				case REQ_WEDGE_DEPLOY:
					if( ! isWedgeRetracted()) {
						ans = true;
					}
					break;
				case REQ_WEDGE_RETRACT:
					if( isWedgeRetracted()) {
						ans = true;
					}
					break;
					//default:
					//break;
				}
			}
			return ans;
		}
	}
	
	private static GrabberMotionAndWedgeMgmt m_gmwm = new GrabberMotionAndWedgeMgmt();
	
	private static int gmwmInstanceCount = 0;
	public static class GrabberMotionAndWedgeMgmt {
		private GrabberMotionAndWedgeMgmt() {
			//this class needs to be a singleton. throw an exception if it isn't.
			//because it's an inner class, the normal getInstance() approach gets
			//messy. if we try to make everything static, we wind up with a lot of
			//messy typing. this seems to be a reasonable compromise.
			if( ! (gmwmInstanceCount++ == 0)) {
				throw TmExceptions.getInstance()
						.new Team744RunTimeEx("There should only be one instance of GrabberMotionAndWedgeMgmt!!");
			}
		}

		public static final Object gmwmLock = new Object();

		public static enum GrabberMotionAndWedgeRequestsE {
			@Deprecated REQ_WEDGE_DEPLOY,
			@Deprecated REQ_WEDGE_RETRACT,
			@Deprecated REQ_GRABBER_UP,
			@Deprecated REQ_GRABBER_DOWN,
			REQ_GRABBER_DOWN_WEDGE_RETRACT,
			REQ_GRABBER_UP_WITH_WEDGE,
			REQ_GRABBER_UP_NO_WEDGE
			;
		}

//		//the position required at the start of a match
//		private static GrabberMotionAndWedgeRequestsE m_lastGmwRequest = 
//				GrabberMotionAndWedgeRequestsE.REQ_GRABBER_UP_NO_WEDGE;

		//states for state machine
		public static enum GrabberMotionAndWedgeSteMachStatesE {
			STE_SET_GRABBER_UP_WITH_WEDGE(GrabberMotionAndWedgeRequestsE.REQ_GRABBER_UP_WITH_WEDGE),
			STE_SET_GRABBER_UP_NO_WEDGE(GrabberMotionAndWedgeRequestsE.REQ_GRABBER_UP_NO_WEDGE),
			STE_SET_GRABBER_DOWN_NO_WEDGE(GrabberMotionAndWedgeRequestsE.REQ_GRABBER_DOWN_WEDGE_RETRACT),
			STE_WAIT_DOWN_THEN_RETRACT_WEDGE(),
			STE_WAIT_DOWN_THEN_DEPLOY_WEDGE(),
			STE_WAIT_DOWN_THEN_RETRACT_WEDGE_THEN_UP(),
			STE_WAIT_DOWN_THEN_DEPLOY_WEDGE_THEN_UP(),
			STE_WAIT_WEDGE_RETRACTED_THEN_UP(),
			STE_WAIT_WEDGE_DEPLOYED_THEN_UP(),
			STE_WAIT_DOWN_THEN_IDLE(),
			STE_WAIT_UP_THEN_IDLE(),
			STE_WAIT_WEDGE_RETRACTED_THEN_IDLE(),
			STE_WAIT_WEDGE_DEPLOYED_THEN_IDLE(),
			STE_FINISH(),
			IDLE()
			;
			protected GrabberMotionAndWedgeRequestsE eRelatedReq;
			private GrabberMotionAndWedgeSteMachStatesE() {
				this(null);
			}
			private GrabberMotionAndWedgeSteMachStatesE(GrabberMotionAndWedgeRequestsE relatedRequest) {
				eRelatedReq = relatedRequest;
			}
		}
		
		public void setGmwSteMachState(GrabberMotionAndWedgeSteMachStatesE newState) {
			synchronized(GrabberMotionAndWedgeMgmt.gmwmLock) {
				cCurGmwmSmSte = newState;
			}
		}

		//current state for state machine
		private static GrabberMotionAndWedgeSteMachStatesE cCurGmwmSmSte = GrabberMotionAndWedgeSteMachStatesE.IDLE;
		public boolean isStateMachineIdle() {
			synchronized(GrabberMotionAndWedgeMgmt.gmwmLock) {
				return cCurGmwmSmSte == GrabberMotionAndWedgeSteMachStatesE.IDLE;
			}
		}

		public void runGrabberMotionAndWedgeSteMach() {
//			synchronized(m_grabberLock) {
			synchronized(GrabberMotionAndWedgeMgmt.gmwmLock) {
				if( ! cCurGmwmSmSte.equals(GrabberMotionAndWedgeSteMachStatesE.IDLE)) {
					P.println(PrtYn.Y, "runGrabberMotionAndWedgeSteMach entry: ste=" + cCurGmwmSmSte.name() +
							" g-up?" + (isGrabberUp()?"Y":"N") + 
							" g-up-lmtSw?" + (TmSsGrabber.isGrabberUpFullLimitSwitchTripped()?"Y":"N") +
							" wedge-retracted?" + ((isWedgeRetracted()?"Y":"N")));
				}
				switch(cCurGmwmSmSte) {
				case IDLE:
					break;
				case STE_SET_GRABBER_DOWN_NO_WEDGE:
					//if already in the requested state, don't waste air trying to do it again
					if(isInState(cCurGmwmSmSte.eRelatedReq)) {
						cCurGmwmSmSte = GrabberMotionAndWedgeSteMachStatesE.STE_FINISH;
					} 
					else if(isGrabberUp()) {
						updateGrabberCubeLift(GrabberCubeLiftE.LOWERED);
						cCurGmwmSmSte = GrabberMotionAndWedgeSteMachStatesE.STE_WAIT_DOWN_THEN_RETRACT_WEDGE;
					}
					else { //assume wedge needs to be retracted //if( ! isWedgeRetracted()) {
						updateGrabberWedge(GrabberWedgeE.RETRACTED);
						cCurGmwmSmSte = GrabberMotionAndWedgeSteMachStatesE.STE_WAIT_WEDGE_RETRACTED_THEN_IDLE;
					}
					break;
				case STE_WAIT_DOWN_THEN_RETRACT_WEDGE:
					if( ! isGrabberUp()) {
						if( ! isWedgeRetracted()) {	
							updateGrabberWedge(GrabberWedgeE.RETRACTED);
							cCurGmwmSmSte = GrabberMotionAndWedgeSteMachStatesE.STE_WAIT_WEDGE_RETRACTED_THEN_IDLE;
						} else {
							cCurGmwmSmSte = GrabberMotionAndWedgeSteMachStatesE.STE_FINISH;
						}
					}
					break;
				case STE_WAIT_WEDGE_RETRACTED_THEN_IDLE:
					if(isWedgeRetracted()) {	
						cCurGmwmSmSte = GrabberMotionAndWedgeSteMachStatesE.STE_FINISH;
					}
					break;
				case STE_SET_GRABBER_UP_NO_WEDGE:
					//if already in the requested state, don't waste air trying to do it again
					if(isInState(cCurGmwmSmSte.eRelatedReq)) {
						cCurGmwmSmSte = GrabberMotionAndWedgeSteMachStatesE.STE_FINISH;
					} 
					else if(isGrabberUp() && ! isWedgeRetracted()) {
						updateGrabberCubeLift(GrabberCubeLiftE.LOWERED);
						cCurGmwmSmSte = GrabberMotionAndWedgeSteMachStatesE.STE_WAIT_DOWN_THEN_RETRACT_WEDGE_THEN_UP;
					}
					else if( ! isWedgeRetracted()) {
						updateGrabberWedge(GrabberWedgeE.RETRACTED);
						cCurGmwmSmSte = GrabberMotionAndWedgeSteMachStatesE.STE_WAIT_WEDGE_RETRACTED_THEN_UP;
					}
					else {
						updateGrabberCubeLift(GrabberCubeLiftE.RAISED);
						cCurGmwmSmSte = GrabberMotionAndWedgeSteMachStatesE.STE_WAIT_UP_THEN_IDLE;
					} 
					break;
				case STE_SET_GRABBER_UP_WITH_WEDGE:
					//if already in the requested state, don't waste air trying to do it again
					if(isInState(cCurGmwmSmSte.eRelatedReq)) {
						cCurGmwmSmSte = GrabberMotionAndWedgeSteMachStatesE.STE_FINISH;
					} 
					else if(isGrabberUp() && isWedgeRetracted()) {
						updateGrabberCubeLift(GrabberCubeLiftE.LOWERED);
						cCurGmwmSmSte = GrabberMotionAndWedgeSteMachStatesE.STE_WAIT_DOWN_THEN_DEPLOY_WEDGE_THEN_UP;
					}
					else if(isWedgeRetracted()) {
						updateGrabberWedge(GrabberWedgeE.DEPLOYED);
						cCurGmwmSmSte = GrabberMotionAndWedgeSteMachStatesE.STE_WAIT_WEDGE_DEPLOYED_THEN_UP;
					}
					else {
						updateGrabberCubeLift(GrabberCubeLiftE.RAISED);
						cCurGmwmSmSte = GrabberMotionAndWedgeSteMachStatesE.STE_WAIT_UP_THEN_IDLE;
					}
					break;
				case STE_WAIT_DOWN_THEN_DEPLOY_WEDGE:
					if( ! isGrabberUp()) {
						if(isWedgeRetracted()) {	
							updateGrabberWedge(GrabberWedgeE.DEPLOYED);
							cCurGmwmSmSte = GrabberMotionAndWedgeSteMachStatesE.STE_WAIT_WEDGE_DEPLOYED_THEN_IDLE;
						} else {
							cCurGmwmSmSte = GrabberMotionAndWedgeSteMachStatesE.STE_FINISH;
						}
					}
					break;
				case STE_WAIT_DOWN_THEN_DEPLOY_WEDGE_THEN_UP:
					if( ! isGrabberUp()) {
						if(isWedgeRetracted()) {	
							updateGrabberWedge(GrabberWedgeE.DEPLOYED);
							cCurGmwmSmSte = GrabberMotionAndWedgeSteMachStatesE.STE_WAIT_WEDGE_DEPLOYED_THEN_UP;
						} else {
							updateGrabberCubeLift(GrabberCubeLiftE.RAISED);
							cCurGmwmSmSte = GrabberMotionAndWedgeSteMachStatesE.STE_WAIT_UP_THEN_IDLE;
						}
					}
					break;
				case STE_WAIT_DOWN_THEN_IDLE:
					if( ! isGrabberUp()) {
						cCurGmwmSmSte = GrabberMotionAndWedgeSteMachStatesE.STE_FINISH;
					}
					break;
				case STE_WAIT_DOWN_THEN_RETRACT_WEDGE_THEN_UP:
					if( ! isGrabberUp()) {
						if( ! isWedgeRetracted()) {	
							updateGrabberWedge(GrabberWedgeE.RETRACTED);
							cCurGmwmSmSte = GrabberMotionAndWedgeSteMachStatesE.STE_WAIT_WEDGE_RETRACTED_THEN_UP;
						} else {
							updateGrabberCubeLift(GrabberCubeLiftE.RAISED);
							cCurGmwmSmSte = GrabberMotionAndWedgeSteMachStatesE.STE_WAIT_UP_THEN_IDLE;
						}
					}
					break;
				case STE_WAIT_UP_THEN_IDLE:
					if(isGrabberUp()) {
						cCurGmwmSmSte = GrabberMotionAndWedgeSteMachStatesE.STE_FINISH;
					}
					break;
				case STE_WAIT_WEDGE_DEPLOYED_THEN_IDLE:
					if( ! isWedgeRetracted()) {	
						cCurGmwmSmSte = GrabberMotionAndWedgeSteMachStatesE.STE_FINISH;
					}
					break;
				case STE_WAIT_WEDGE_DEPLOYED_THEN_UP:
					if( ! isWedgeRetracted()) {
						updateGrabberCubeLift(GrabberCubeLiftE.RAISED);
						cCurGmwmSmSte = GrabberMotionAndWedgeSteMachStatesE.STE_WAIT_UP_THEN_IDLE;
					}
					break;
				case STE_WAIT_WEDGE_RETRACTED_THEN_UP:
					if(isWedgeRetracted()) {
						updateGrabberCubeLift(GrabberCubeLiftE.RAISED);
						cCurGmwmSmSte = GrabberMotionAndWedgeSteMachStatesE.STE_WAIT_UP_THEN_IDLE;
					}
					break;
				case STE_FINISH:
					cCurGmwmSmSte = GrabberMotionAndWedgeSteMachStatesE.IDLE;
					postToSd();
					P.println(PrtYn.Y, "runGrabberMotionAndWedgeSteMach finish: ste=" + cCurGmwmSmSte.name() +
							" g-up?" + (isGrabberUp()?"Y":"N") + 
							" g-up-lmtSw?" + (TmSsGrabber.isGrabberUpFullLimitSwitchTripped()?"Y":"N") +
							" wedge-retracted?" + ((isWedgeRetracted()?"Y":"N")));
					break;
				//default:
					//break;				
				}
				if( ! cCurGmwmSmSte.equals(GrabberMotionAndWedgeSteMachStatesE.IDLE)) {
					P.println(PrtYn.Y, "runGrabberMotionAndWedgeSteMach exit: ste=" + cCurGmwmSmSte.name() +
								" g-up?" + (isGrabberUp()?"Y":"N") + 
								" g-up-lmtSw?" + (TmSsGrabber.isGrabberUpFullLimitSwitchTripped()?"Y":"N") +
								" wedge-retracted?" + ((isWedgeRetracted()?"Y":"N")));
					postToSd();
				}
			}
//			}
		}
		
		/**
		 * allows users to request grabber motion and wedge use. If the hardware state
		 * makes the request unsafe, it will be rejected
		 * @param requestedAction
		 * @return BUSY if the request is rejected because a previous request is still being processed, 
		 * 		   UNSAFE if the hardware is in a state that makes the requested action unsafe to attempt
		 * 				(too much wear-and-tear on the robot, etc.)
		 *         FAILED if the request was attempted immediately but is known to have failed
		 *         OK if the request is accepted for processing or was attempted and apparently succeeded
		 */
		public static GrabberPositionAndWedgeReturnCodesE handleUpdateGrabberPositionAndWedge(
												GrabberMotionAndWedgeRequestsE requestedAction) {
			synchronized(GrabberMotionAndWedgeMgmt.gmwmLock) {
				
				GrabberPositionAndWedgeReturnCodesE ans;
				ans = GrabberPositionAndWedgeReturnCodesE.BUSY;
				P.println(PrtYn.Y, "handleUpdateGrabberPositionAndWedge entry: isIdle?" + 
							(m_gmwm.isStateMachineIdle()?"Y":"N"));
				if(m_gmwm.isStateMachineIdle()) {
					//GrabberMotionAndWedgeRequestsE
					if(isInState(requestedAction)) {
						ans = GrabberPositionAndWedgeReturnCodesE.OK;
					} else {
						switch(requestedAction) {
						case REQ_GRABBER_DOWN_WEDGE_RETRACT:
							m_gmwm.setGmwSteMachState(GrabberMotionAndWedgeSteMachStatesE.STE_SET_GRABBER_DOWN_NO_WEDGE);
							break;
						case REQ_GRABBER_UP_NO_WEDGE:
							m_gmwm.setGmwSteMachState(GrabberMotionAndWedgeSteMachStatesE.STE_SET_GRABBER_UP_NO_WEDGE);
							break;
						case REQ_GRABBER_UP_WITH_WEDGE:
							m_gmwm.setGmwSteMachState(GrabberMotionAndWedgeSteMachStatesE.STE_SET_GRABBER_UP_WITH_WEDGE);
							break;
						case REQ_GRABBER_DOWN:
							updateGrabberCubeLift(GrabberCubeLiftE.LOWERED);
							ans = GrabberPositionAndWedgeReturnCodesE.OK;
							break;
						case REQ_GRABBER_UP:
							updateGrabberCubeLift(GrabberCubeLiftE.RAISED);
							ans = GrabberPositionAndWedgeReturnCodesE.OK;
							break;
						case REQ_WEDGE_DEPLOY:
							if(m_liftCurrentPosition.equals(GrabberCubeLiftE.LOWERED)) {
								//safe to move wedge
								updateGrabberWedge(GrabberWedgeE.DEPLOYED);
								if(isWedgeRetracted()) {
									ans = GrabberPositionAndWedgeReturnCodesE.FAILED;
								} else {
									ans = GrabberPositionAndWedgeReturnCodesE.OK;
								}
							} else {
								ans = GrabberPositionAndWedgeReturnCodesE.UNSAFE;
							}
							break;
						case REQ_WEDGE_RETRACT:
							if(m_liftCurrentPosition.equals(GrabberCubeLiftE.LOWERED)) {
								//safe to move wedge
								updateGrabberWedge(GrabberWedgeE.RETRACTED);
								if(isWedgeRetracted()) {
									ans = GrabberPositionAndWedgeReturnCodesE.OK;
								} else {
									ans = GrabberPositionAndWedgeReturnCodesE.FAILED;
								}
							} else {
								ans = GrabberPositionAndWedgeReturnCodesE.UNSAFE;
							}
							break;
							//default:
							//break;
						} //end switch
					}
				} //end 'if IDLE'
				P.println(PrtYn.Y, "handleUpdateGrabberPositionAndWedge exit: retCode=" + 
						ans.name() + " smSte=" + cCurGmwmSmSte.name());

				return ans;
			} //end of sync
		}//end of method

		private static void updateGrabberWedge(GrabberWedgeE requestedAction)
		{
			synchronized(gmwmLock) {
				//don't move the wedge while the lift (claw) is up
				if(m_liftCurrentPosition.equals(GrabberCubeLiftE.LOWERED)) {
					//safe to move wedge
					if(requestedAction.equals(GrabberWedgeE.DEPLOYED))
					{
						m_wedge.set(GrabberWedgeE.DEPLOYED.getSolDirection());
						if(OptDefaults.WEDGE_LIMIT_SWITCH_INSTALLED) {
							if(isWedgeRetractedLimitSwitchTripped()) {
								P.println(PrtYn.Y, "Oops! wedge didn't deploy!!");
								m_wedge.set(GrabberWedgeE.DEPLOYED.getSolDirection());
								if( ! isWedgeRetractedLimitSwitchTripped()) {
									P.println(PrtYn.Y, "wedge deployed on retry....");
									m_wedgeCurrentPosition = GrabberWedgeE.DEPLOYED;
								} else {
									m_wedgeCurrentPosition = GrabberWedgeE.RETRACTED;
								}
							}
						} else {
							m_wedgeCurrentPosition = GrabberWedgeE.DEPLOYED;
						}
					}
					else if(requestedAction.equals(GrabberWedgeE.RETRACTED))
					{
						m_wedge.set(GrabberWedgeE.RETRACTED.getSolDirection());
						m_wedgeCurrentPosition = GrabberWedgeE.RETRACTED;
						if(OptDefaults.WEDGE_LIMIT_SWITCH_INSTALLED) {
							if( ! isWedgeRetractedLimitSwitchTripped()) {
								P.println(PrtYn.Y, "Oops! Wedge didn't retract!!!!");
								m_wedge.set(GrabberWedgeE.RETRACTED.getSolDirection());
								if(isWedgeRetractedLimitSwitchTripped()) {
									P.println(PrtYn.Y, "wedge retracted on retry....");
									m_wedgeCurrentPosition = GrabberWedgeE.RETRACTED;						
								} else {
									m_wedgeCurrentPosition = GrabberWedgeE.DEPLOYED;
								}
							}
						} else {
							m_wedgeCurrentPosition = GrabberWedgeE.RETRACTED;						
						}
					}
				}
			}
			P.println(PrtYn.Y, "Grabber wedge set to " + m_wedgeCurrentPosition.name());
			postToSd();
		}	

		private static void updateGrabberCubeLift(GrabberCubeLiftE requestedAction)
		{
			synchronized(gmwmLock) {
				if(requestedAction.equals(GrabberCubeLiftE.LOWERED))
				{
					m_cubeLiftBothSides.set(GrabberCubeLiftE.LOWERED.getSolDirection());
					m_liftCurrentPosition = GrabberCubeLiftE.LOWERED;
				}
				else if(requestedAction.equals(GrabberCubeLiftE.RAISED))
				{
					m_cubeLiftBothSides.set(GrabberCubeLiftE.RAISED.getSolDirection());
					m_liftCurrentPosition = GrabberCubeLiftE.RAISED;
				}
				P.println(PrtYn.Y, "Grabber cube lift set to " + m_liftCurrentPosition.name());
				postToSd();
			}
		}
		
	} //end of class
	
	public static enum GrabberPositionAndWedgeReturnCodesE {
		OK, BUSY, UNSAFE, FAILED
	}
	
	/**
	 * allows users to request grabber motion and wedge use. If the hardware state
	 * makes the request unsafe, it will be rejected
	 * @param requestedAction
	 * @return BUSY if the request is rejected because a previous request is still being processed, 
	 * 		   UNSAFE if the hardware is in a state that makes the requested action unsafe to attempt
	 * 				(too much wear-and-tear on the robot, etc.)
	 *         FAILED if the request was attempted but was unsuccessful (not enough air pressure to 
	 *         		move things, etc.)
	 *         OK if the request is acceptable
	 */
	public static GrabberPositionAndWedgeReturnCodesE 
	updateGrabberPositionAndWedge(GrabberMotionAndWedgeRequestsE requestedAction) {
		synchronized(GrabberMotionAndWedgeMgmt.gmwmLock) {
			return GrabberMotionAndWedgeMgmt.handleUpdateGrabberPositionAndWedge(requestedAction);
		}
	}
	
//	private static void updateGrabberWedge(GrabberWedgeE requestedAction)
//	{
//		synchronized(m_grabberLock) {
//			//don't move the wedge while the lift (claw) is up
//			if(m_liftCurrentPosition.equals(GrabberCubeLiftE.LOWERED)) {
//				//safe to move wedge
//				if(requestedAction.equals(GrabberWedgeE.DEPLOYED))
//				{
//					m_wedge.set(GrabberWedgeE.DEPLOYED.getSolDirection());
//					if(Opts.WEDGE_LIMIT_SWITCH_INSTALLED) {
//						if(isWedgeRetractedLimitSwitchTripped()) {
//							P.println(PrtYn.Y, "Oops! wedge didn't deploy!!");
//							m_wedge.set(GrabberWedgeE.DEPLOYED.getSolDirection());
//							if( ! isWedgeRetractedLimitSwitchTripped()) {
//								P.println(PrtYn.Y, "wedge deployed on retry....");
//								m_wedgeCurrentPosition = GrabberWedgeE.DEPLOYED;
//							} else {
//								m_wedgeCurrentPosition = GrabberWedgeE.RETRACTED;
//							}
//						}
//					} else {
//						m_wedgeCurrentPosition = GrabberWedgeE.DEPLOYED;
//					}
//				}
//				else if(requestedAction.equals(GrabberWedgeE.RETRACTED))
//				{
//					m_wedge.set(GrabberWedgeE.RETRACTED.getSolDirection());
//					m_wedgeCurrentPosition = GrabberWedgeE.RETRACTED;
//					if(Opts.WEDGE_LIMIT_SWITCH_INSTALLED) {
//						if( ! isWedgeRetractedLimitSwitchTripped()) {
//							P.println(PrtYn.Y, "Oops! Wedge didn't retract!!!!");
//							m_wedge.set(GrabberWedgeE.RETRACTED.getSolDirection());
//							if(isWedgeRetractedLimitSwitchTripped()) {
//								P.println(PrtYn.Y, "wedge retracted on retry....");
//								m_wedgeCurrentPosition = GrabberWedgeE.RETRACTED;						
//							} else {
//								m_wedgeCurrentPosition = GrabberWedgeE.DEPLOYED;
//							}
//						}
//					} else {
//						m_wedgeCurrentPosition = GrabberWedgeE.RETRACTED;						
//					}
//				}
//			}
//		}
//		P.println(PrtYn.Y, "Grabber wedge set to " + m_wedgeCurrentPosition.name());
//		postToSd();
//	}	
//
//	private static void updateGrabberCubeLift(GrabberCubeLiftE requestedAction)
//	{
//		synchronized(m_grabberLock) {
//			if(requestedAction.equals(GrabberCubeLiftE.LOWERED))
//			{
//				m_cubeLiftBothSides.set(GrabberCubeLiftE.LOWERED.getSolDirection());
//				m_liftCurrentPosition = GrabberCubeLiftE.LOWERED;
//			}
//			else if(requestedAction.equals(GrabberCubeLiftE.RAISED))
//			{
//				m_cubeLiftBothSides.set(GrabberCubeLiftE.RAISED.getSolDirection());
//				m_liftCurrentPosition = GrabberCubeLiftE.RAISED;
//			}
//		}
//		P.println(PrtYn.Y, "Grabber cube lift set to " + m_liftCurrentPosition.name());
//		postToSd();
//	}	
	
	public static void updateGrabberMotorState(GrabberMotorStateE requestedAction)
	{
		synchronized(m_grabberLock) {
			switch(requestedAction) {
			case GRABBING:
				m_currentMotorState = requestedAction;
				m_clampMtrLeft.set(ControlMode.PercentOutput, GrabberMotorStateE.GRABBING.eMtrPercentOutLeft);
				m_clampMtrRight.set(ControlMode.PercentOutput, GrabberMotorStateE.GRABBING.eMtrPercentOutRight);
				break;
			case OFF:
				m_currentMotorState = requestedAction;
				m_clampMtrLeft.set(ControlMode.PercentOutput, GrabberMotorStateE.OFF.eMtrPercentOutLeft);
				m_clampMtrRight.set(ControlMode.PercentOutput, GrabberMotorStateE.OFF.eMtrPercentOutRight);
				break;
			case RELEASING:
				m_currentMotorState = requestedAction;
				m_clampMtrLeft.set(ControlMode.PercentOutput, GrabberMotorStateE.RELEASING.eMtrPercentOutLeft);
				m_clampMtrRight.set(ControlMode.PercentOutput, GrabberMotorStateE.RELEASING.eMtrPercentOutRight);
				m_clampReleaseTimer.start();
				m_clampReleaseTimer.reset();
				m_motorsOffTime = GRABBER_MOTOR_RELEASE_TIME_SECS;
				break;
			case DRIVER_CONTROL:
				if(m_tds.isEnabledTeleop()) {
					m_currentMotorState = requestedAction;
//					m_clampMtrLeft.set(ControlMode.PercentOutput, GrabberMotorStateE.RELEASING.eMtrPercentOutLeft);
//					m_clampMtrRight.set(ControlMode.PercentOutput, GrabberMotorStateE.RELEASING.eMtrPercentOutRight);
				} else {
					m_currentMotorState = GrabberMotorStateE.OFF;
					m_clampMtrLeft.set(ControlMode.PercentOutput, 0.0);
					m_clampMtrRight.set(ControlMode.PercentOutput, 0.0);
				}
				break;
			}
			P.println(PrtYn.Y, "Grabber motors set to " + m_currentMotorState.name());
		}
		postToSd();
	}	
	
	public static boolean runGrabberMotorsWithDrvStaControls() {
		boolean ans_driverTakingControl = false;
		synchronized(m_grabberLock) {
			if(m_tds.isEnabledAutonomous()) {
				//do nothing in autonomous
			}
			else if(m_tds.isEnabledTeleop()) {
				double inputValForReleasing = 0; 
				double inputValForGrabbing = 0;
				 inputValForGrabbing = DsNamedControlsE.GRABBER_MOTORS_GRABBING_INPUT.getEnt().getAnalog();
				 inputValForReleasing = DsNamedControlsE.GRABBER_MOTORS_RELEASING_INPUT.getEnt().getAnalog();
//				if (OptDefaults.MAIN_XBOX_CONTROLLER_ATTACHED){
//					 inputValForGrabbing = DsNamedControlsE.GRABBER_MOTORS_GRABBING_INPUT.getEnt().getAnalog();
//					 inputValForReleasing = DsNamedControlsE.GRABBER_MOTORS_RELEASING_INPUT.getEnt().getAnalog();
//				}
//				if(OptDefaults.XTRA_XBOX_CONTROLLER_ATTACHED && inputValForReleasing==0 && inputValForGrabbing==0) {
//					 inputValForGrabbing = DsNamedControlsE.ALT_GRABBER_MOTORS_GRABBING_INPUT.getEnt().getAnalog();
//					 inputValForReleasing = DsNamedControlsE.ALT_GRABBER_MOTORS_RELEASING_INPUT.getEnt().getAnalog();
//				}
				//grabbing takes priority over releasing
				if(inputValForGrabbing != 0.0) {
					ans_driverTakingControl = true;
					if(GrabberMotorStateE.GRABBING.eMtrPercentOutLeft > 0) {
						m_clampMtrLeft.set(ControlMode.PercentOutput, Math.abs(inputValForGrabbing));
						m_clampMtrRight.set(ControlMode.PercentOutput, -Math.abs(inputValForGrabbing));							
					}
					else {
						m_clampMtrLeft.set(ControlMode.PercentOutput, -Math.abs(inputValForGrabbing));
						m_clampMtrRight.set(ControlMode.PercentOutput, Math.abs(inputValForGrabbing));							
					}
				}
				else if(inputValForReleasing != 0.0) {
					ans_driverTakingControl = true;
					if(GrabberMotorStateE.RELEASING.eMtrPercentOutLeft > 0) {
						m_clampMtrLeft.set(ControlMode.PercentOutput, Math.abs(inputValForReleasing));
						m_clampMtrRight.set(ControlMode.PercentOutput, -Math.abs(inputValForReleasing));							
					}
					else {
						m_clampMtrLeft.set(ControlMode.PercentOutput, -Math.abs(inputValForReleasing));
						m_clampMtrRight.set(ControlMode.PercentOutput, Math.abs(inputValForReleasing));							
					}
				}
			}
		}
		return ans_driverTakingControl;
	}
	public void runGrabberMotors() {
		synchronized(m_grabberLock) {
			switch(m_currentMotorState) {
			case DRIVER_CONTROL:
				if(m_tds.isEnabledAutonomous()) {
					//do nothing in autonomous
				}
				else if(m_tds.isEnabledTeleop()) {
					if(! runGrabberMotorsWithDrvStaControls()) {
						m_clampMtrLeft.set(ControlMode.PercentOutput, 0.0);
						m_clampMtrRight.set(ControlMode.PercentOutput, 0.0);
					}
				}
				else { //disabled or in LW test mode or ...
					m_clampMtrLeft.set(ControlMode.PercentOutput, 0.0);
					m_clampMtrRight.set(ControlMode.PercentOutput, 0.0);
				}
				break;
			case GRABBING:
				if(! runGrabberMotorsWithDrvStaControls()) {
					m_clampMtrLeft.set(ControlMode.PercentOutput, GrabberMotorStateE.GRABBING.eMtrPercentOutLeft);
					m_clampMtrRight.set(ControlMode.PercentOutput, GrabberMotorStateE.GRABBING.eMtrPercentOutRight);
				}
				break;
			case OFF:
				if(! runGrabberMotorsWithDrvStaControls()) {
					m_clampMtrLeft.set(ControlMode.PercentOutput, GrabberMotorStateE.OFF.eMtrPercentOutLeft);
					m_clampMtrRight.set(ControlMode.PercentOutput, GrabberMotorStateE.OFF.eMtrPercentOutRight);
				}
				break;
			case RELEASING:
				if(! runGrabberMotorsWithDrvStaControls()) {
					m_clampMtrLeft.set(ControlMode.PercentOutput, GrabberMotorStateE.RELEASING.eMtrPercentOutLeft);
					m_clampMtrRight.set(ControlMode.PercentOutput, GrabberMotorStateE.RELEASING.eMtrPercentOutRight);
				}
				if(m_clampReleaseTimer.get() > m_motorsOffTime) {
					m_currentMotorState = GrabberMotorStateE.OFF;
				}
				break;
				//default:
				//	break;
			}
		}
	}


	public static void postToSd() { //TODO use clamp/unclamp for clampAssist, grabbing/releasing for motors
		synchronized(m_grabberLock) {
			synchronized(TmSsGrabber.GrabberMotionAndWedgeMgmt.gmwmLock) {
				TmSdMgr.putBoolean(SdKeysE.KEY_GRABBER_GRABBING, 
						((GrabberMotorStateE.GRABBING.eMtrPercentOutLeft > 0) ?
								(m_clampMtrLeft.getMotorOutputPercent() > 0) :
									(m_clampMtrLeft.getMotorOutputPercent() < 0)));
				TmSdMgr.putBoolean(SdKeysE.KEY_GRABBER_RELEASING, 
						((GrabberMotorStateE.GRABBING.eMtrPercentOutLeft > 0) ?
								(m_clampMtrLeft.getMotorOutputPercent() < 0) :
									(m_clampMtrLeft.getMotorOutputPercent() > 0)));
				TmSdMgr.putBoolean(SdKeysE.KEY_GRABBER_CLAMPED, 
						m_clampAssistCurrentPosition.equals(GrabberCubeClampAssistE.CLAMPED));
				TmSdMgr.putBoolean(SdKeysE.KEY_GRABBER_UNCLAMPED, 
						m_clampAssistCurrentPosition.equals(GrabberCubeClampAssistE.UNCLAMPED));
				TmSdMgr.putBoolean(SdKeysE.KEY_GRABBER_LIFT_UP, m_liftCurrentPosition.equals(GrabberCubeLiftE.RAISED));
				TmSdMgr.putBoolean(SdKeysE.KEY_GRABBER_WEDGE_DEPLOYED, m_wedgeCurrentPosition.equals(GrabberWedgeE.DEPLOYED));
				TmSdMgr.putNumber(m_clampMtrLeft.m_namedCntlEnt.cSdKeyMtrPercentOut, m_clampMtrLeft.getMotorOutputPercent());
				TmSdMgr.putNumber(m_clampMtrRight.m_namedCntlEnt.cSdKeyMtrPercentOut, m_clampMtrRight.getMotorOutputPercent());
				TmSdMgr.putBoolean(SdKeysE.KEY_GRABBER_WEDGE_LIMIT_SWITCH, isWedgeRetractedLimitSwitchTripped());
				TmSdMgr.putBoolean(SdKeysE.KEY_GRABBER_UP_FULL_LIMIT_SWITCH, TmSsGrabber.isGrabberUpFullLimitSwitchTripped());
			}
		}
	}
		
	@Override
	public void initDefaultCommand() {}

}
