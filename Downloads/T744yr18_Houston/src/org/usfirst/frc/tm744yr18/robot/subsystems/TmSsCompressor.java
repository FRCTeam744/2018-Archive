package org.usfirst.frc.tm744yr18.robot.subsystems;

import org.usfirst.frc.tm744yr18.robot.config.TmHdwrRoCntls.RoNamedControlsE;
import org.usfirst.frc.tm744yr18.robot.config.TmSdKeysI.SdKeysE;
import org.usfirst.frc.tm744yr18.robot.devices.TmFakeable_Relay;
import org.usfirst.frc.tm744yr18.robot.devices.TmFakeable_RoDigitalInput;
import org.usfirst.frc.tm744yr18.robot.helpers.TmSdMgr;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmRoControlUserI;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmStdSubsystemI;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmToolsI;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmToolsI.P.PrtYn;

import edu.wpi.first.wpilibj.AnalogInput;
import edu.wpi.first.wpilibj.Compressor;
import edu.wpi.first.wpilibj.Relay;
import t744opts.Tm744Opts;


public class TmSsCompressor implements TmStdSubsystemI, TmToolsI, TmRoControlUserI {

	/*---------------------------------------------------------
	 * getInstance stuff                                      
	 *---------------------------------------------------------*/
	/** 
	 * handle making the singleton instance of this class and giving
	 * others access to it
	 */
	private static TmSsCompressor m_instance;

	public static synchronized TmSsCompressor getInstance() {
		if (m_instance == null) {
			m_instance = new TmSsCompressor();
		}
		return m_instance;
	}

	private TmSsCompressor() {
	}
	/*----------------end of getInstance stuff----------------*/

	Compressor m_compressor = null;
	TmFakeable_Relay m_relay = null;
	TmFakeable_RoDigitalInput m_sensor = null;
	AnalogInput m_sensorAi = null;

	@Override
	public void sssDoRobotInit() {
		if(Tm744Opts.isCompressorInstalled()) {
			if( ! (Tm744Opts.isOptRunStf() || Tm744Opts.isPowerCompressorFromRelay() ||
					Tm744Opts.isInSimulationMode())) {
				m_compressor = new Compressor(0);
				m_compressor.clearAllPCMStickyFaults();
			}
			else if (Tm744Opts.isPowerCompressorFromRelay()) {

				//			m_compressor = new Compressor(0);

				m_sensor = new TmFakeable_RoDigitalInput(this, RoNamedControlsE.COMPRESSOR_SENSOR, false);
				m_prevValue = m_sensor.get();

				//the analog sensor idea was aborted, code never tested, never finished....
				if(Tm744Opts.isReadCompressorSensorAsAnalog()) {
					m_sensorAi = new AnalogInput(RoNamedControlsE.COMPRESSOR_SENSOR_ANALOG.getEnt().cNamedConn.getEnt().getConnectionFrcIndex());
				} else {	
					m_relay = new TmFakeable_Relay(this, RoNamedControlsE.COMPRESSOR_RELAY);
					//set on here, compressor won't actually run until enabled
					//m_relay.set(Relay.Value.kForward);
				}
			}
		}
	}


	@Override
	public void sssDoInstantiate() {
		// TODO Auto-generated method stub
		
	}
	
	private void postSensorToSd() {
		if(Tm744Opts.isCompressorInstalled()) {

			if(Tm744Opts.isPowerCompressorFromRelay()) {
				P.println(PrtYn.Y, "compressorSensor: " + m_sensor.get());
			}
			else if(Tm744Opts.isReadCompressorSensorAsAnalog()) {
				//			P.println(PrtYn.Y, "compressorSensorAnalog: " + m_sensorAi.getv .getVoltage());
				//			m_sensorAi = new AnalogInput(RoNamedControlsE.COMPRESSOR_SENSOR_ANALOG.getEnt().cNamedConn.getEnt().getConnectionFrcIndex());
			}
		}
	}

	/**
	 * 
	 * @param callerName the name of the method calling this method
	 * @param clcOnOrOff	true to allow the compressor to cycle on or off, false to keep it off
	 */
	private void updateCompressorClosedloopControl(String callerName, boolean clcOnOrOff) {
		if(m_compressor != null) {
			m_compressor.setClosedLoopControl(clcOnOrOff);			
		}
		else {
			P.println(PrtYn.Y, callerName + " - attempted to change compressor closed loop control, but m_compressor has not been instantiated.");
		}
	}

	@Override
	public void sssDoDisabledInit() {
		updateCompressorClosedloopControl("sssDoDisabledInit()", true);
		postSensorToSd();
	}

	@Override
	public void sssDoAutonomousInit() {
		updateCompressorClosedloopControl("sssDoAutonomousInit()", false);
		postSensorToSd();
	}

	@Override
	public void sssDoTeleopInit() {
		updateCompressorClosedloopControl("sssDoTeleopInit()", true);
		postSensorToSd();
	}

	@Override
	public void sssDoLwTestInit() {
		// TODO Auto-generated method stub

	}

	//methods provided for use in simulation code
	private boolean m_compressorEnabled = false;
	public void fake_setCompressorEnabledState(boolean newState) {
		m_compressorEnabled = newState;
	}
	
	private boolean m_prevValue;
	@Override
	public void sssDoRobotPeriodic() {
		boolean currentRdg;
		if(Tm744Opts.isCompressorInstalled()) {

		if(m_compressor==null) {
			if(m_sensor != null) {
				currentRdg = m_sensor.get();
			} else {
				currentRdg = false;
			}
			if(currentRdg != m_prevValue) {
				P.println(PrtYn.Y, "compressorSensor changing from " + m_prevValue + " to " + currentRdg);
				m_prevValue = currentRdg;
			}
			TmSdMgr.putBoolean(SdKeysE.KEY_COMPRESSOR_SENSOR, currentRdg);
			if(m_relay != null) {
				if(currentRdg) {
					//pressure low, run compressor
					m_relay.set(Relay.Value.kForward);
				} else {
					//reached desired pressure
					m_relay.set(Relay.Value.kOff);
				}
			}
			TmSdMgr.putBoolean(SdKeysE.KEY_COMPRESSOR_SENSOR, currentRdg);
			if(m_relay != null) {
				TmSdMgr.putString(SdKeysE.KEY_COMPRESSOR_RELAY, m_relay.get().name());
			}
		} else {
			TmSdMgr.putBoolean(SdKeysE.KEY_COMPRESSOR_RUNNING, 
				(Tm744Opts.isOptRunStf() ? m_compressorEnabled : m_compressor.enabled()));
		}
//		if(Tm744Opts.isReadCompressorSensorAsAnalog()) {
//			m_sensorAi = new AnalogInput(RoNamedControlsE.COMPRESSOR_SENSOR_ANALOG.getEnt().cNamedConn.getEnt().getConnectionFrcIndex());
//		}
		
		}

	}

	@Override
	public void sssDoDisabledPeriodic() {
		// TODO Auto-generated method stub

	}

	@Override
	public void sssDoAutonomousPeriodic() {
		// TODO Auto-generated method stub

	}

	@Override
	public void sssDoTeleopPeriodic() {
		// TODO Auto-generated method stub

	}

	@Override
	public void sssDoLwTestPeriodic() {
		// TODO Auto-generated method stub

	}
	
//	public enum VisionOutDefE {
//		FRONT(Cnst.RES.W, Cnst.RES.H, 10, new Point(50, 50), new Point(200, 200), new Scalar(255, 255, 255), 3),
//		REAR(Cnst.RES.W, Cnst.RES.H, 10, new Point(75, 75), new Point(175, 175), new Scalar(255, 255, 255), 3),
//		NONE(Cnst.RES.W, Cnst.RES.H, 10, null, null, null, 0),
//		;
////		Imgproc.rectangle(mat, new Point(100, 100), new Point(400, 400),
////		new Scalar(255, 255, 255), 5);
//		
//		int	eImgWidth;
//		int eImgHeight;
//		int eFPS;
//		Point eTopL;
//		Point eLowerR;
//		Scalar eColor;
//		int eLineWidth;
//		private VisionOutDefE(int width, int height, int fps, Point topL, Point lowerR, Scalar color, int lineWidth) {
//			eImgWidth = width;
//			eImgHeight = height;
//			eFPS = fps;
//			eTopL = topL;
//			eLowerR = lowerR;
//			eColor = color;
//			eLineWidth = lineWidth;			
//		}
//	}

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
	public boolean isFake() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void initDefaultCommand() {
		// TODO Auto-generated method stub
		
	}

}
