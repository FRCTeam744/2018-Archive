package org.usfirst.frc.tm744yr18.robot.devices;

import org.usfirst.frc.tm744yr18.robot.config.TmSdKeysI.SdKeysE;
import org.usfirst.frc.tm744yr18.robot.helpers.TmSdMgr;

import com.kauailabs.navx.frc.AHRS;
import com.kauailabs.navx.frc.AHRS.SerialDataType;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.I2C;
import edu.wpi.first.wpilibj.SPI;
import edu.wpi.first.wpilibj.SerialPort;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import t744opts.Tm744Opts;

public class TmNavX {
	AHRS ahrs = null;
	
	private SdKeysE m_sdKeyConnected;
	private SdKeysE m_sdKeyCalibrating;
	private SdKeysE m_sdKeyYaw;
	private SdKeysE m_sdKeyPitch;
	private SdKeysE m_sdKeyRoll;
	private SdKeysE m_sdKeyTotalYaw;
	
	private double m_localAngle = 0;
	
	public TmNavX(SdKeysE sdKeyConnected, SdKeysE sdKeyCalibrating, 
				  SdKeysE sdKeyYaw, SdKeysE sdKeyPitch, SdKeysE sdKeyRoll, SdKeysE sdKeyTotalYaw) {
		if(Tm744Opts.isGyroNavX()) {
		try {
			/***********************************************************************
			 * navX-MXP:
			 * - Communication via RoboRIO MXP (SPI, I2C, TTL UART) and USB.            
			 * - See http://navx-mxp.kauailabs.com/guidance/selecting-an-interface.
			 * 
			 * navX-Micro:
			 * - Communication via I2C (RoboRIO MXP or Onboard) and USB.
			 * - See http://navx-micro.kauailabs.com/guidance/selecting-an-interface.
			 * 
			 * Multiple navX-model devices on a single robot are supported.
			 ************************************************************************/
//            ahrs = new AHRS(SerialPort.Port.kUSB1); 
//            ahrs = new AHRS(SerialPort.Port.kMXP, SerialDataType.kProcessedData, (byte)50);
            ahrs = new AHRS(SPI.Port.kMXP); //I recommend we use SPI if mounted directly to the roboRio
//            ahrs = new AHRS(I2C.Port.kMXP);
            ahrs.enableLogging(Tm744Opts.isInSimulationMode() ? false : true);
            m_sdKeyConnected   = sdKeyConnected;
        	m_sdKeyCalibrating = sdKeyCalibrating;
        	m_sdKeyYaw		   = sdKeyYaw;
        	m_sdKeyPitch	   = sdKeyPitch;
        	m_sdKeyRoll		   = sdKeyRoll;
        	m_sdKeyTotalYaw	   = sdKeyTotalYaw;
        } catch (RuntimeException ex ) {
            DriverStation.reportError("Error instantiating navX MXP:  " + ex.getMessage(), true);
            ahrs = null;
            m_sdKeyConnected   = null;
        	m_sdKeyCalibrating = null;
        	m_sdKeyYaw         = null;
        	m_sdKeyPitch       = null;
        	m_sdKeyRoll        = null;
        	m_sdKeyTotalYaw    = null;
        }
		}
	}
	
	public double getLocalAngle() {
		if(ahrs != null) {
			if(ahrs.isConnected() && !ahrs.isCalibrating()) {
				return -ahrs.getAngle() - m_localAngle;
			}
			else {
				return 0;
			}			
		}
		return 0;
	}
	
	public double getGlobalAngle() {
		if(ahrs != null) {
			if(ahrs.isConnected() && !ahrs.isCalibrating()) {
				return -ahrs.getAngle();
			}
			else {
				return 0;
			}
		}
		return 0;
	}
	
	public void resetSoft() {
		if(ahrs != null) {
			m_localAngle = -ahrs.getAngle();
		}
	}
	
	public void resetHard() {
		if(ahrs != null) {
			ahrs.reset();			
		}
	}
	
	public void postToSdConnected() {
		if(m_sdKeyConnected != null && ahrs != null) {
			TmSdMgr.putBoolean(m_sdKeyConnected, ahrs.isConnected());			
		}
	}
	
	public void postToSdCalibrating() {
		if(m_sdKeyCalibrating != null && ahrs != null) {
			TmSdMgr.putBoolean(m_sdKeyCalibrating, ahrs.isCalibrating());
	        
		}
	}

	public void postToSdYaw() {
		if(m_sdKeyYaw != null && ahrs != null) {
			TmSdMgr.putNumber(m_sdKeyYaw, ahrs.getYaw());
		}
	}

	public void postToSdPitch() {
		if(m_sdKeyPitch != null && ahrs != null) {
			TmSdMgr.putNumber(m_sdKeyPitch, ahrs.getPitch());
		}
	}

	public void postToSdRoll() {
		if(m_sdKeyRoll != null && ahrs != null) {
	        TmSdMgr.putNumber(m_sdKeyRoll, ahrs.getRoll());
		}
	}
	
	public void postToSdTotalyaw() {
		if(m_sdKeyTotalYaw != null && ahrs != null) {
	        TmSdMgr.putNumber(m_sdKeyTotalYaw, -ahrs.getAngle());
		}
	}
	
	public void postToSdAll(){
		postToSdConnected();
		postToSdCalibrating();
		postToSdYaw();
		postToSdPitch();
		postToSdRoll();
		postToSdTotalyaw();
	}
}
