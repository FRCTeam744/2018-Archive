package org.usfirst.frc.tm744yr18.robot.devices;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.usfirst.frc.tm744yr18.robot.config.TmHdwrDsCntls.DsNamedControlsE;
import org.usfirst.frc.tm744yr18.robot.config.TmHdwrRoCntls.RoNamedControlsE;
import org.usfirst.frc.tm744yr18.robot.config.TmSdKeysI.SdKeysE;
import org.usfirst.frc.tm744yr18.robot.helpers.TmSdMgr;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmToolsI;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmToolsI.P.PrtYn;

//import edu.wpi.first.wpilibj.ADXL345_SPI;
import edu.wpi.first.wpilibj.SPI;
import edu.wpi.first.wpilibj.SensorBase;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.smartdashboard.SendableBuilder;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import t744opts.Tm744Opts;

/*
 * see https://docs.oracle.com/javase/8/docs/api/java/nio/ByteBuffer.html
 */

/**
 * Must call this class's doPeriodic method in order to keep angle updated. Call it
 * from teleopPeriodic and autonomousPeriodic.  Reset it in the corresponding init methods
 * @author JudiA
 *
 */
public class TmGyroADXRS453SPI extends SensorBase implements TmToolsI {

	private int m_devPartId;    //chip's revision number
	private int m_devSerialNbr; //chip's serial number
	
	TimestampedRate m_prevTsRate;
	TimestampedRate m_tsRate;
	TimestampedRate m_workingTsRate; //used as a temporary holding area while checking for errors

	private RoNamedControlsE m_gyroNamedCntl;
	private SPI.Port m_gyroSpiPort;
	
	private SPI m_spi;
	private Timer m_smTimer; //sm for "state machine"
	private Timer m_angleTimer;
	private double m_angle;

	private String m_classNameId;

	private boolean m_foundSpiGyro = false; //default for when NavX gyro enabled
	private int m_gyroErrorCount;
	
	private SdKeysE m_sdKeyAngle;
	private SdKeysE m_sdKeyRate;
	private SdKeysE m_sdKeyRateRaw;
	private SdKeysE m_sdKeyTemperature;
	private SdKeysE m_sdKeyErrorCount;
	
	private double m_rateAdj = 0.0;
	
	public void postToSdAngle() {
		if(m_sdKeyAngle != null) {
			TmSdMgr.putNumber(m_sdKeyAngle, getAngle());
		}
	}
	public void postToSdRate() {
		if(m_sdKeyRate != null) {
			TmSdMgr.putNumber(m_sdKeyRate, getRate());
		}
	}
	public void postToSdRateRaw() {
		if(m_sdKeyRateRaw != null) {
			TmSdMgr.putNumber(m_sdKeyRateRaw, getRateRaw());
		}
	}
	public void postToSdTemperature() {
		if(m_sdKeyTemperature != null) {
			TmSdMgr.putNumber(m_sdKeyTemperature, getTemperature());
		}
	}
	public void postToSdErrorCount() {
		if(m_sdKeyErrorCount != null) {
			TmSdMgr.putNumber(m_sdKeyErrorCount, m_gyroErrorCount);
		}
	}
	
//	public TmGyroADXRS453SPI(RoNamedControlsE driveTrainGyro) {
//		this(driveTrainGyro, null, null, null, null, null);
//	}	
	public TmGyroADXRS453SPI(RoNamedControlsE driveTrainGyro,
			SdKeysE sdKeyAngle, SdKeysE sdKeyRate, SdKeysE sdKeyTemp, SdKeysE sdKeyErrorCount, SdKeysE sdKeyRateRaw) {
		m_sdKeyAngle = sdKeyAngle;
		m_sdKeyRate = sdKeyRate;
		m_sdKeyTemperature = sdKeyTemp;
		m_sdKeyErrorCount = sdKeyErrorCount;
		m_sdKeyRateRaw = sdKeyRateRaw;

		m_gyroNamedCntl = driveTrainGyro;
		m_gyroSpiPort = driveTrainGyro.getEnt().cNamedConn.getEnt().getConnectionSpiPort();

		if( ! Tm744Opts.isGyroNavX()) {
			m_spi = new SPI(m_gyroSpiPort);
		}
		m_smTimer = new Timer();
		m_angleTimer = new Timer();
		
		m_prevTsRate = new TimestampedRate();
		m_tsRate = new TimestampedRate();
		m_workingTsRate = new TimestampedRate();

		m_classNameId= "[" + Tt.getClassName(this) + "]";
		m_gyroErrorCount = 0;
		if( ! Tm744Opts.isGyroNavX()) {
			init();
		}
//		LiveWindow.addSensor("Gyro_ADXRS453_SPI_" + port.toString(), port.getValue(), this);
	}	
	public void free(){
		if( ! Tm744Opts.isGyroNavX()) {
			m_spi.free();
		}
	}
	

	/**
	 * Set SPI bus parameters, start state machine to do the device hardware
	 * initialization sequence.
	 */
	private void init(){
		if( ! Tm744Opts.isGyroNavX()) {
			m_spi.setClockRate(500000);
			m_spi.setMSBFirst();
			m_spi.setSampleDataOnRising();
			m_spi.setClockActiveHigh();
			m_spi.setChipSelectActiveLow();

			m_curSte = StartUpSeqSteMachStatesE.NEED_TO_START;
			m_smTimer.start();
			m_foundSpiGyro = false;
		}
	}
	
	//constants from device datasheet.  LSB is "least significant bit" and is used as a
	//shorthand for when the reading changes by 1.  Like "ticks" for an encoder.
	private static final int LSBS_PER_ANGULAR_DEGREE_PER_SEC = 80;
	private static final int LSBS_PER_DEGREE_CENTIGRADE = 5;
	private static final int DEGREES_CELCIUS_REPRESENTED_BY_A_READING_OF_0 = 45;

	/**
	 * angle is calculated by a crude integration of rate. Not 100% accurate.
	 * @return current accumulated angle in degrees.
	 */
	public double getAngle() {
		return m_angle;
	}
	
	/** used to keep a rate reading paired with its timestamp */
	private class TimestampedRate {
		public double timestamp;
		public double rate;      //as returned by calcAngularDegreesPerSecond(CmdAndResponse cr); and adjusted
		public double rateRaw;   //as returned by calcAngularDegreesPerSecond(CmdAndResponse cr); before adjustments
		public TimestampedRate() {
			rate = 0;
			rateRaw = 0;
			timestamp = 0;
		}
		public void set(TimestampedRate tsr) {
			rate = tsr.rate;
			rateRaw = tsr.rateRaw;
			timestamp = tsr.timestamp;
		}
	}
	
	private int errMsgCnt = 50;
	private PrtYn errMsgPrt = PrtYn.Y;
	/**
	 * reads rate from gyro
	 * @return degrees per second. If errors, returns the last rate read.
	 */
	public double getRate() {
		double ans = m_prevTsRate.rate;
		String dbgMsgPrefix = m_classNameId + "getRate()";

		if( ! Tm744Opts.isGyroNavX()) {

			if(errMsgCnt<=0) {
				errMsgCnt = 0;
				errMsgPrt = PrtYn.N;
			}

			if(isReady()) { 
				CmdAndResponse cr;
				int errCnt;
				bldSensorDataCommand(m_crReadRate);
				doSpiTransaction(m_crReadRate); //ignore this response 
				m_workingTsRate.timestamp = m_angleTimer.get();
				doSpiTransaction(m_crReadRate); //this response should have the rate requested by the previous cmd
				cr = m_crReadRate;
				errCnt = 0;
				if(cr.respType != RespTypeE.SENSOR_DATA) {
					errCnt++;
					P.println(errMsgPrt, dbgMsgPrefix + ": unexpected response type " + cr.respType.toString() + "." +
							" errCnt=" + errCnt + " errMsgCnt=" + errMsgCnt);
				}
				else if(SensorDataRespBitMaskE.getStatus(cr.resp) != SensorDataResponseStatusE.VALID_DATA.value) {
					errCnt++;
					String prob = String.format("Response status 0x%X not the expected value of 0x%X",
							SensorDataRespBitMaskE.getStatus(cr.resp), SensorDataResponseStatusE.VALID_DATA.value);
					P.println(errMsgPrt, dbgMsgPrefix +  ": invalid data: " + 
							prob + "." +
							" errCnt=" + errCnt + " errMsgCnt=" + errMsgCnt);					
				}
				else if(SensorDataRespBitMaskE.getFaults(cr.resp) != 0) {
					errCnt++;
					P.println(errMsgPrt, dbgMsgPrefix +  ": fault bits are on." +
							" errCnt=" + errCnt + " errMsgCnt=" + errMsgCnt);					
				}
				else {
					//				m_workingTsRate.rate = calcAngularDegreesPerSecond(cr, m_tsRate.rate);
					m_workingTsRate.rateRaw = calcAngularDegreesPerSecond(cr, m_tsRate.rateRaw);
					m_workingTsRate.rate = m_workingTsRate.rateRaw - m_rateAdj;
					//move most recent info used in angle calculations into 'prev'
					//and then set new info
					m_prevTsRate.set(m_tsRate);
					m_tsRate.set(m_workingTsRate);
					ans = m_tsRate.rate;
					updateAngle();
				}
				m_gyroErrorCount += errCnt;
				if(errMsgCnt>0) {
					errMsgCnt -= errCnt;
					if(errMsgCnt==1) {
						P.println("will now stop showing error messages from gyro");
					}
					if(errMsgCnt<=0) {
						errMsgCnt = 0;
						errMsgPrt = PrtYn.N;
					}
				}
				//		P.println(PrtYn.Y, cr.toStringFull());
			}
		}
		return ans;
	}
	public double getRateRaw() {
		return m_tsRate.rateRaw;
	}
	
	
	private int dbgMsgCnt = 100;
	/** a crude integration function to calculate angle from rate */
	private void updateAngle() {
		double curRate = m_tsRate.rate;
		double prevRate = m_prevTsRate.rate;
		double elapsedTime = m_tsRate.timestamp - m_prevTsRate.timestamp;
		m_angle += (((curRate + prevRate) / 2) * elapsedTime); // /INCR_CNT_PER_DEGREE_PER_SEC;
		if(m_tsRate.rate != 0.0) {
			if(dbgMsgCnt-- > 0) {
				P.println("Gyro detected motion: current degPerSec=" + m_tsRate.rate + 
						", elapsedTime=" + elapsedTime + ", angle=" + m_angle);
			}
		}
	}
	
	public void reset() {
		getRate(); //make sure timer, angle, and rate measurements are sort of in sync
		m_angle = 0;
	}

	/**
	 * reads temperature from gyro
	 * @return degrees Centigrade
	 */
	public double getTemperature() {
		double ans = 0;
		if(! isReady()) { return ans; }
		if( ! Tm744Opts.isGyroNavX()) {
			CmdAndResponse cr = new CmdAndResponse();
			int errCnt;
			bldReadCommand(cr, RegisterE.TEM1);
			doSpiTransaction(cr); //ignore this response 
			doSpiTransaction(cr); //this response should have the temperature requested by the previous cmd
			errCnt = 0;
			if(cr.respType != RespTypeE.READ) {
				P.println(((cr.respType == RespTypeE.INVALID_DUE_TO_P1_PARITY_ERRORS) ? PrtYn.Y : PrtYn.Y), 
						m_classNameId + "getTemperature()" + ": unexpected response type " + cr.respType.toString());
				errCnt++;
			}
			else {
				ans = calcDegreesCelcius(cr.resp);
			}
		}
		return ans;
	}
	
	private double calcDegreesCelcius(int resp) {
		//assume never called when Tm744Opts.isGyroNavX() is true
		double ans = 0;
		int temperature;
		int tempData = ReadWriteRespBitMaskE.getData(resp);
		//the 10-bit temperature is returned left justified in the 16-bit data field
		//it is a signed (twos-complement) value
		temperature =  tempData << 16; //force the sign bit to be set properly
		temperature =  temperature >> (16 + 6);
		
		//temperature scaling factor is 5 LSBs per degree centigrade
		ans = DEGREES_CELCIUS_REPRESENTED_BY_A_READING_OF_0 + temperature / LSBS_PER_DEGREE_CENTIGRADE;
		return ans;
	}
	
	private double calcAngularDegreesPerSecond(CmdAndResponse cr, double defaultRate) {
		//assume never called when Tm744Opts.isGyroNavX() is true
		double ans = defaultRate;
		int errCnt = 0;
		int rate;
		int rateData = 0; // = m_lastRateReadRaw;
		if(cr.respType == RespTypeE.SENSOR_DATA) {
			rateData = SensorDataRespBitMaskE.getData(cr.resp);
		}
		else if(cr.respType == RespTypeE.READ) {	
			rateData = ReadWriteRespBitMaskE.getData(cr.resp);
		}
		else {
			errCnt++;
			P.println(PrtYn.Y, m_classNameId + "unexpected response type " + cr.respType.toString() + " in calcAngularDegreesPerSecond()");
		}
		
		if(errCnt == 0) {
			//the 16-bit rate is a signed (twos-complement) value
			rate =  rateData << 16; //force the sign bit to be set properly
			rate =  rate >> 16;
			
			//raw value scaling factor is 80 LSBs per degree per second
			ans = rate / LSBS_PER_ANGULAR_DEGREE_PER_SEC;
		}
		return ans;
	}
	
	

	/*============================== Gyro start-up sequence state machine and related stuff ============*/
	public boolean isReady() { return m_curSte == StartUpSeqSteMachStatesE.DONE; }
	
	public void doPeriodic() {
		if( ! Tm744Opts.isGyroNavX() ) {

			if(isReady()) {
				getRate();
			} else {
				runStartUpSeqSteMach();
			}
		}
	}
	
	/* NOTE!!! The response in a CmdAndResponse is NOT the response to the command in that object.  It is 
	 * the response to whatever prior command was sent.
	 */
	CmdAndResponse crCmd1DummyResp = new CmdAndResponse(); //will get predefined dummy response
	CmdAndResponse crCmd2Resp1 = new CmdAndResponse(); //gets actual response to crSnsDataWithChk cmd - discard it
	CmdAndResponse crCmd3Resp2 = new CmdAndResponse(); //gets actual response to crSnsClrChk cmd - ck fault bits and ST bits
	CmdAndResponse crCmd4Resp3 = new CmdAndResponse(); //should have same response as in crSnsDataChkRespA
	CmdAndResponse crCmd5Resp4 = new CmdAndResponse(); //should have all fault bits cleared
	CmdAndResponse crCmd6Resp5 = new CmdAndResponse(); //the response here should be the PartId
	CmdAndResponse crCmd7Resp6 = new CmdAndResponse(); //the response here should be the SerNbrHi
	CmdAndResponse crCmd8Resp7 = new CmdAndResponse(); //the response here should be the SerNbrLo
	CmdAndResponse crCmd9Resp8 = new CmdAndResponse(); //the response here should be the gyro rate
	CmdAndResponse crCmd10Resp9 = new CmdAndResponse(); //the response here should be the temperature
	
	CmdAndResponse m_crReadRate = new CmdAndResponse(); //the response here should be the gyro rate
	CmdAndResponse crReadTemp = new CmdAndResponse(); //the response here should be the gyro temp
	
	
	
	/**
	 * ste is short for "state", mach is short for "machine", seq is short for "sequence"
	 * This state machine takes care of issuing the various commands required by the 
	 * start-up sequence described in the datasheet for the device.  It then reads the 
	 * device part id and serial number.
	 * @author JudiA
	 *
	 */
	private enum StartUpSeqSteMachStatesE { NEED_TO_START, WAIT_HW_INTERNAL_INIT, CMD_FORCE_FAULTS, WAIT_WHILE_FORCE_FAULTS,
												CMD_CLEAR_FAULTS, WAIT_WHILE_FAULTS_CLEAR, CMD_CLEAR_FAULT_BITS_A, 
												CMD_CLEAR_FAULT_BITS_B, WAIT_HW_CLEAR_FAULT_CONDITIONS, 
												WAIT_HW_CLEAR_FAULT_BITS, CMD_READ_PART_ID, 
												CMD_READ_SER_NBR_HIGH, CMD_READ_SER_NBR_LOW, CMD_READ_RATE, CMD_READ_TEMP,  
												START_ANGLE_CALC, GYRO_NOT_FOUND, DONE }
	private StartUpSeqSteMachStatesE m_curSte;
	private void runStartUpSeqSteMach() {
		//assume never called when Tm744Opts.isGyroNavX() is true

//		int bitMask, expectedData;
//		boolean gotExpectedData;
		CmdAndResponse cr;
		int errCnt;
		switch(m_curSte) {
		case NEED_TO_START:
			m_curSte = StartUpSeqSteMachStatesE.WAIT_HW_INTERNAL_INIT;
			m_smTimer.reset();
			break;
		case WAIT_HW_INTERNAL_INIT:
			if(m_smTimer.get() >= 0.100) {
				m_curSte = StartUpSeqSteMachStatesE.CMD_FORCE_FAULTS;
			}
			break;
		case CMD_FORCE_FAULTS:
			bldSensorDataCommand(crCmd1DummyResp, true);
			doSpiTransaction(crCmd1DummyResp);
			//the response here should be a dummy response the first time, or should
			//contain info we don't care about if it's a retry.
			if(crCmd1DummyResp.resp != VERY_FIRST_RESPONSE_FROM_DEVICE) {
				//hmm... this shouldn't have happened
				P.println(PrtYn.Y, m_classNameId + "Why didn't we get the response we expected?!? in state " + m_curSte.toString());
			}
			P.println(PrtYn.Y, crCmd1DummyResp.toStringFull());
			m_smTimer.reset();
			m_curSte = StartUpSeqSteMachStatesE.WAIT_WHILE_FORCE_FAULTS;
			break;
		case WAIT_WHILE_FORCE_FAULTS:
			if(m_smTimer.get() >= 0.050) {
				m_curSte = StartUpSeqSteMachStatesE.CMD_CLEAR_FAULTS;
			}
			break;
		case CMD_CLEAR_FAULTS:
			bldSensorDataCommand(crCmd2Resp1, false);
			doSpiTransaction(crCmd2Resp1);
			//we ignore the response here - it's from the original "force faults" cmd
			//with state before faults were forced.
			P.println(PrtYn.Y, crCmd2Resp1.toStringFull());
			m_smTimer.reset();
			m_curSte = StartUpSeqSteMachStatesE.WAIT_WHILE_FAULTS_CLEAR;
			break;
		case WAIT_WHILE_FAULTS_CLEAR:
			if(m_smTimer.get() >= 0.050) {
				m_curSte = StartUpSeqSteMachStatesE.CMD_CLEAR_FAULT_BITS_A;
			}
			break;
		case CMD_CLEAR_FAULT_BITS_A:
			bldSensorDataCommand(crCmd3Resp2, false);
			doSpiTransaction(crCmd3Resp2);
			//the response here is the response to the "clear faults" cmd we sent earlier
			//but with state from before faults were cleared
			cr = crCmd3Resp2;
			errCnt = 0;
			if(cr.respType != RespTypeE.SENSOR_DATA) {
				P.println(PrtYn.Y, m_classNameId + "ste " + m_curSte.toString() + ": unexpected response type " + cr.respType.toString());
				errCnt++;
			}
			else if(SensorDataRespBitMaskE.getStatus(cr.resp) != SensorDataResponseStatusE.SELF_TEST_DATA.value) {
				m_foundSpiGyro = true;
				P.println(PrtYn.Y, m_classNameId + "ste " + m_curSte.toString() + ": unexpected status (ST) bits.");					
				errCnt++;
			}
//			else if(SensorDataRespBitMaskE.getFaults(cr.resp) != SensorDataRespBitMaskE.FAULTS.bitMask) {
//			else if(SensorDataRespBitMaskE.isAllFaultsOn(cr.resp)) {  //TODO why didn't this work??!
//			else if((cr.resp & 0xFE) != 0xFE) {  //TODO why didn't isAllFaultsOn work??!
			else if( ! SensorDataRespBitMaskE.isAllFaultsOnWithChk(cr.resp)) {  //TODO trying for 2018
				m_foundSpiGyro = true;
				P.println(PrtYn.Y, m_classNameId + "ste " + m_curSte.toString() + ": not all fault bits are on" +
                        " (" + String.format("0x%02X", SensorDataRespBitMaskE.getFaults(cr.resp)) +
                        " not the expected " + 
                        String.format("0x%02X", (SensorDataRespBitMaskE.FAULTS.bitMask >>> SensorDataRespBitMaskE.FAULTS.shiftRightCnt)) +
                        ")");					
				errCnt++;
			}
			else {
				m_foundSpiGyro = true;
//				m_smTimer.reset();
				m_curSte = StartUpSeqSteMachStatesE.CMD_CLEAR_FAULT_BITS_B;
			}
			P.println(PrtYn.Y, cr.toStringFull());
			m_curSte = StartUpSeqSteMachStatesE.CMD_CLEAR_FAULT_BITS_B;
			break;
		case CMD_CLEAR_FAULT_BITS_B:
			bldSensorDataCommand(crCmd4Resp3, false);
			doSpiTransaction(crCmd4Resp3);
			//the response here is the response to the "clear fault bits" cmd we sent earlier
			//but with state from before bits were cleared
			cr = crCmd4Resp3;
			errCnt = 0;
			if(cr.respType != RespTypeE.SENSOR_DATA) {
				P.println(PrtYn.Y, m_classNameId + "ste " + m_curSte.toString() + ": unexpected response type " + cr.respType.toString());
				errCnt++;
			}
			else if(SensorDataRespBitMaskE.getStatus(cr.resp) != SensorDataResponseStatusE.SELF_TEST_DATA.value) {
				m_foundSpiGyro = true;
				P.println(PrtYn.Y, m_classNameId + "ste " + m_curSte.toString() + ": unexpected status (ST) bits.");					
				errCnt++;
			}
//			else if(SensorDataRespBitMaskE.getFaults(cr.resp) != SensorDataRespBitMaskE.FAULTS.bitMask) {
//			else if(SensorDataRespBitMaskE.isAllFaultsOn(cr.resp)) { //TODO why didn't this work??!
//			else if((cr.resp & 0xFE) != 0xFE) {
			else if( ! SensorDataRespBitMaskE.isAllFaultsOnWithChk(cr.resp)) {  //TODO trying for 2018
				m_foundSpiGyro = true;
				P.println(PrtYn.Y, m_classNameId + "ste " + m_curSte.toString() + ": not all fault bits are on" +
                        " (" + String.format("0x%02X", SensorDataRespBitMaskE.getFaults(cr.resp)) +
                        " not the expected " + 
                        String.format("0x%02X",SensorDataRespBitMaskE.FAULTS.bitMask) + ")");					
				errCnt++;
			}
			else {
				m_foundSpiGyro = true;
				m_curSte = StartUpSeqSteMachStatesE.CMD_READ_PART_ID;
			}
			P.println(PrtYn.Y, cr.toStringFull());
			//if there were errors, go back and issue the command again.
			//if(errCnt>0) { m_curSte = StartUpSeqSteMachStatesE.WAIT_WHILE_FORCE_FAULTS; }
			m_curSte = StartUpSeqSteMachStatesE.CMD_READ_PART_ID;
			break;
		case CMD_READ_PART_ID:
			bldReadCommand(crCmd5Resp4, RegisterE.PID1);
			doSpiTransaction(crCmd5Resp4);
			//the response here is the response to the last sensor data cmd we sent.
			//verify response has no active faults and has the ST bits set for valid data
			cr = crCmd5Resp4;
			errCnt = 0;
			if(cr.respType != RespTypeE.SENSOR_DATA) {
				P.println(PrtYn.Y, m_classNameId + "ste " + m_curSte.toString() + ": unexpected response type " + cr.respType.toString());
				errCnt++;
			}
			else if(SensorDataRespBitMaskE.getStatus(cr.resp) != SensorDataResponseStatusE.VALID_DATA.value) {
				m_foundSpiGyro = true;
				P.println(PrtYn.Y, m_classNameId + "ste " + m_curSte.toString() + ": unexpected status (ST) bits.");					
				errCnt++;
			}
			else if(SensorDataRespBitMaskE.getFaults(cr.resp) != 0) {
				m_foundSpiGyro = true;
				P.println(PrtYn.Y, m_classNameId + "ste " + m_curSte.toString() + ": still have fault bits on");					
				errCnt++;
			}
			else {
				m_foundSpiGyro = true;
				m_smTimer.reset();
				m_curSte = StartUpSeqSteMachStatesE.CMD_READ_SER_NBR_HIGH;
			}
			P.println(PrtYn.Y, cr.toStringFull());
			m_curSte = StartUpSeqSteMachStatesE.CMD_READ_SER_NBR_HIGH;
			break;
		case CMD_READ_SER_NBR_HIGH:
			bldReadCommand(crCmd6Resp5, RegisterE.SN3);
			doSpiTransaction(crCmd6Resp5);
			//the response here is the response to the read part ID.
			cr = crCmd6Resp5;
			errCnt = 0;
			if(cr.respType != RespTypeE.READ) {
				P.println(PrtYn.Y, m_classNameId + "ste " + m_curSte.toString() + ": unexpected response type " + cr.respType.toString());
				errCnt++;
			}
			else {
				m_foundSpiGyro = true;
				m_devPartId = ReadWriteRespBitMaskE.getData(cr.resp);
				m_curSte = StartUpSeqSteMachStatesE.CMD_READ_SER_NBR_LOW;
			}
			P.println(PrtYn.Y, cr.toStringFull());
			m_curSte = StartUpSeqSteMachStatesE.CMD_READ_SER_NBR_LOW;
			break;
		case CMD_READ_SER_NBR_LOW:
			bldReadCommand(crCmd7Resp6, RegisterE.SN1);
			doSpiTransaction(crCmd7Resp6);
			//the response here is the response to the read serial number high cmd.
			cr = crCmd7Resp6;
			errCnt = 0;
			if(cr.respType != RespTypeE.READ) {
				P.println(PrtYn.Y, m_classNameId + "ste " + m_curSte.toString() + ": unexpected response type " + cr.respType.toString());
				errCnt++;
			}
			else {
				m_foundSpiGyro = true;
				m_devSerialNbr = ReadWriteRespBitMaskE.getData(cr.resp) << 16;
				m_curSte = StartUpSeqSteMachStatesE.CMD_READ_RATE;
			}
			m_curSte = StartUpSeqSteMachStatesE.CMD_READ_RATE;
			P.println(PrtYn.Y, cr.toStringFull());
			break;
		case CMD_READ_RATE:
			bldReadCommand(crCmd8Resp7, RegisterE.RATE1);
			doSpiTransaction(crCmd8Resp7);
			//the response here is the response to the read serial number low cmd.
			cr = crCmd8Resp7;
			errCnt = 0;
			if(cr.respType != RespTypeE.READ) {
				P.println(PrtYn.Y, m_classNameId + 
						"ste " + m_curSte.toString() + 
						": unexpected response type " + cr.respType.toString());
				errCnt++;
			}
			else {
				m_foundSpiGyro = true;
				m_devSerialNbr |= ReadWriteRespBitMaskE.getData(cr.resp);
				m_curSte = StartUpSeqSteMachStatesE.CMD_READ_TEMP;
			}
			P.println(PrtYn.Y, cr.toStringFull());
			m_curSte = StartUpSeqSteMachStatesE.CMD_READ_TEMP;
			break;
		case CMD_READ_TEMP:
			bldReadCommand(crCmd9Resp8, RegisterE.TEM1);
			doSpiTransaction(crCmd9Resp8);
			//the response here is the response to the read rate cmd.
			cr = crCmd9Resp8;
			errCnt = 0;
			if(cr.respType != RespTypeE.READ) {
				P.println(PrtYn.Y, m_classNameId + "ste " + m_curSte.toString() + ": unexpected response type " + cr.respType.toString());
				errCnt++;
			}
			else {
				m_foundSpiGyro = true;
				m_curSte = StartUpSeqSteMachStatesE.START_ANGLE_CALC;
			}
			m_curSte = StartUpSeqSteMachStatesE.START_ANGLE_CALC;
			P.println(PrtYn.Y, cr.toStringFull());
			bldReadCommand(crCmd10Resp9, RegisterE.TEM1);
			doSpiTransaction(crCmd10Resp9);
			//the response here should be the temperature
			P.println(PrtYn.Y, crCmd10Resp9.toStringFull());
			P.println(PrtYn.Y, "SPI Gyro partId=" + String.format("0x%04X", m_devPartId) + 
					" S/N=" + m_devSerialNbr + " temperature=" + calcDegreesCelcius(crCmd10Resp9.resp) +
					" rate=" + calcAngularDegreesPerSecond(crCmd9Resp8, 0 /*default rate*/) );
			break;
		case START_ANGLE_CALC:
			m_angleTimer.start();
			m_angle = 0;
			getRate(); //get the parms needed for angle calculations to known sensible values
			getRate();
//			m_angleTimestamp = 0;
			if(m_foundSpiGyro) {
				m_curSte = StartUpSeqSteMachStatesE.DONE;
				P.println("Found GYRO. It's now ready for use.");
			} else {
				P.println(PrtYn.Y, "Never got a valid response to a command. Assume gyro not installed");
				m_curSte = StartUpSeqSteMachStatesE.GYRO_NOT_FOUND;	
				//since state never gets to 'DONE', we won't be trying to talk to a non-existent gyro.
				//dummy canned data will be returned to the rest of the code.
			}
		case GYRO_NOT_FOUND:
			break;
		case DONE:
			break;
		case WAIT_HW_CLEAR_FAULT_BITS:
			break;
		case WAIT_HW_CLEAR_FAULT_CONDITIONS:
			break;
		default:
			break;
		}
	}
	
	
	/*======================================= cmd/resp layouts and methods =========*/
	//assume never called or used when Tm744Opts.isGyroNavX() is true

	private static final int BYTE3_MULTIPLIER = 0x01000000;
	private static final int BYTE2_MULTIPLIER = 0x00010000;
	private static final int BYTE1_MULTIPLIER = 0x00000100;
	private static final int BYTE0_MULTIPLIER = 0x00000001;
	private static final int NO_SHIFTING = 0;
	
	/** registers available to read (or write?) */
	public enum RegisterE {
		RATE1(0x00),
		RATE0(0x01),
		TEM1(0x02),
		TEM0(0x03),
		LOCST1(0x04),
		LOCST0(0x05),
		HICST1(0x06),
		HICST0(0x07),
		QUAD1(0x08),
		QUAD0(0x09),
		FAULT1(0x0A),
		FAULT0(0x0B),
		PID1(0x0C),
		PID0(0x0D),
		SN3(0x0E),
		SN2(0x0F),
		SN1(0x10),
		SN0(0x11),
		UNKNOWN(0x1FF),
		;
		
		public int regAddr;
		
		private RegisterE(int regAddress) {
			regAddr = regAddress;
		}
	}

	/** values for the status field of the response to a Sensor Data command */
	public enum SensorDataResponseStatusE {
		INVALID_DATA(0x00),
		VALID_DATA(0x01),
		SELF_TEST_DATA(0x02),
		READ_WRITE_RESPONSE(0x03),
		;
		
		public int value;
		
		private SensorDataResponseStatusE(int statusValue) {
			value = statusValue;
		}
		
		public static SensorDataResponseStatusE getStatusType(int resp) {
			SensorDataResponseStatusE ans = null;
			int status = SensorDataRespBitMaskE.getStatus(resp);
			for(SensorDataResponseStatusE item : SensorDataResponseStatusE.values()) {
				if(status == item.value) { ans = item; break; }
			}
			return ans;
		}
	}
	
	public enum FaultsAndCHKAndErrorRespBitsE {
		SPI(0x04 * BYTE2_MULTIPLIER, "SPI (bus error)"),
		RE(0x02 * BYTE2_MULTIPLIER, "RE (request error)"),
		DU(0x01 * BYTE2_MULTIPLIER, "DU (data unavailable error)"),
		
		PLL(0x80 * BYTE0_MULTIPLIER, "phase-locked-loop fault"),
		Q(0x40 * BYTE0_MULTIPLIER, "quadrature fault"),
		NVM(0x20 * BYTE0_MULTIPLIER, "non-volatile memory fault"),
		POR(0x10 * BYTE0_MULTIPLIER, "POR test fault"),
		PWR(0x08 * BYTE0_MULTIPLIER, "power fault"),
		CST(0x04 * BYTE0_MULTIPLIER, "continuous self-test fault"),
		
		CHK(0x02 * BYTE0_MULTIPLIER, "CHK bit (testing fault bits)"),
		;
		public int bitMask;
		public String description;
		private FaultsAndCHKAndErrorRespBitsE(int bitMaskVal, String descr) {
			bitMask = bitMaskVal;
			description = descr;
		}
		public boolean isSPIOn(int resp) { return (0 != (resp & SPI.bitMask)); }
		public boolean isREOn(int resp) { return (0 != (resp & RE.bitMask)); }
		public boolean isDUOn(int resp) { return (0 != (resp & DU.bitMask)); }
		
		public boolean isPLLOn(int resp) { return (0 != (resp & PLL.bitMask)); }
		public boolean isQOn(int resp) { return (0 != (resp & Q.bitMask)); }
		public boolean isNVMOn(int resp) { return (0 != (resp & NVM.bitMask)); }
		public boolean isPOROn(int resp) { return (0 != (resp & POR.bitMask)); }
		public boolean isPWROn(int resp) { return (0 != (resp & PWR.bitMask)); }
		public boolean isCSTOn(int resp) { return (0 != (resp & CST.bitMask)); }
		
		public boolean isCHKOn(int resp) { return (0 != (resp & CHK.bitMask)); }
	}

	private static final int SENSOR_DATA_CMD_FIXED_VALUES = 0x20000000;
	private enum SensorDataCmdFieldsBitMaskE {
		SQ1(0x80 * BYTE3_MULTIPLIER),
		SQ0(0x40 * BYTE3_MULTIPLIER),
		SQ2(0x10 * BYTE3_MULTIPLIER),
		FIXED_VALUES(0x20 * BYTE3_MULTIPLIER),
		DONT_CARE(0x0FFFFFFC),
		CHK(0x02 * BYTE0_MULTIPLIER),
		P(0x01 * BYTE0_MULTIPLIER),		
		;
		public int bitMask;
		private SensorDataCmdFieldsBitMaskE(int bitMaskVal) {
			bitMask = bitMaskVal;
		}
		
		public static int getSeqNbrBitsToOR(int seqNbr) {
			byte ans = 0;
			if(0 != (seqNbr & 0x01)) { ans |= SQ0.bitMask; }
			if(0 != (seqNbr & 0x02)) { ans |= SQ1.bitMask; }
			if(0 != (seqNbr & 0x04)) { ans |= SQ2.bitMask; }
			return ans;
		}
	}
	
	/**
	 * when a command is sent, the response won't come until the next transaction. So, 
	 * for the first command sent, there is nothing for the device to be responding to.
	 * To prevent trouble, the device will always send a known value as the response.
	 * This is it.  It should just be ignored for all practical purposes.
	 */
	private static final int VERY_FIRST_RESPONSE_FROM_DEVICE = 0x00000001;
	private enum SensorDataRespBitMaskE {
		SEQ(0xE0 * BYTE3_MULTIPLIER, 29),
		P0(0x10 * BYTE3_MULTIPLIER, 28),
		STATUS(0x0C * BYTE3_MULTIPLIER, 26),
		DATA(0x03FFFC00, 10),
		DONT_CARE(0x03 * BYTE1_MULTIPLIER, 8),
		FAULTS(0xFC * BYTE0_MULTIPLIER, 2),
		CHK(0x02 * BYTE0_MULTIPLIER, 1),
		P1(0x01 * BYTE0_MULTIPLIER, 0),
		;
		public int bitMask;
		public int shiftRightCnt;
		private SensorDataRespBitMaskE(int bitMaskVal, int shiftRightBitCount) {
			bitMask = bitMaskVal;
			shiftRightCnt = shiftRightBitCount;
		}
		
		public static int getSeqNbr(int resp) {
			return ((resp & SEQ.bitMask) >>> SEQ.shiftRightCnt); //unsigned right shift
		}
		public static int getStatus(int resp) {
			return ((resp & STATUS.bitMask) >>> STATUS.shiftRightCnt); //unsigned right shift
		}
		//SensorDataResponseStatusE
		public static int getData(int resp) {
			return ((resp & DATA.bitMask) >>> DATA.shiftRightCnt); //unsigned right shift
		}
		public static int getFaults(int resp) {
//			return ((resp & FAULTS.bitMask) /*>>> FAULTS.shiftRightCnt*/); //unsigned right shift
			return ((resp & FAULTS.bitMask) >>> FAULTS.shiftRightCnt); //unsigned right shift
		}
		public static int getChk(int resp) {
			return ((resp & CHK.bitMask) >>> CHK.shiftRightCnt); //unsigned right shift
		}
		
		public static boolean isCHKOn(int resp) {
			return (0 != (resp & CHK.bitMask));
		}
		public static boolean isAllFaultsOn(int resp) {
			return ((resp & FAULTS.bitMask) == FAULTS.bitMask);
		}
		public static boolean isAllFaultsOnWithChk(int resp) {
			boolean ans;
			ans = (isAllFaultsOn(resp) && isCHKOn(resp));
			return ans;
		}
	}

	private static final int READ_CMD_FIXED_VALUES = 0x80000000;
	private static final int WRITE_CMD_FIXED_VALUES = 0x40000000;
	private enum ReadWriteCmdBitMaskE {
		FIXED_VALUES_MASK(0xFC * BYTE3_MULTIPLIER, NO_SHIFTING),
		REG_ADDR(0x03FE * BYTE2_MULTIPLIER, 17),
		DATA(0x0001FFFE, 1), //for write commands only
		P(0x01 * BYTE0_MULTIPLIER, NO_SHIFTING),
		;
		public int bitMask;
		public int shiftLeftCnt;
		private ReadWriteCmdBitMaskE(int bitMaskVal, int shiftLeftBitCount) {
			bitMask = bitMaskVal;
			shiftLeftCnt = shiftLeftBitCount;
		}
		public static int getAddrBitsToOR(RegisterE regAddrDef) {
			int regAddr = regAddrDef.regAddr;
			int shiftCnt = REG_ADDR.shiftLeftCnt;
			int ans = regAddr << shiftCnt;
			return (regAddrDef.regAddr << REG_ADDR.shiftLeftCnt);
		}
		//for write commands only
		public static int getDataBitsToOR(int data) {
			return ((data << DATA.shiftLeftCnt) & DATA.bitMask);
		}
		
		public static int getRegister(int cmd) {
			return ((cmd & REG_ADDR.bitMask) >>> REG_ADDR.shiftLeftCnt);
		}
		
		public static RegisterE getRegisterType(int resp) {
			RegisterE ans = RegisterE.UNKNOWN; //REG_ADDR.bitMask >>> REG_ADDR.shiftLeftCnt;
			int regAddr = ReadWriteCmdBitMaskE.getRegister(resp);
			for(RegisterE item : RegisterE.values()) {
				if(regAddr == item.regAddr) { ans = item; break; }
			}
			return ans;
		}
	}

	private static final int READ_RESP_FIXED_VALUES = 0x4E000000;
	private static final int WRITE_RESP_FIXED_VALUES = 0x2E000000;
	private static final int READ_WRITE_ERROR_RESP_FIXED_VALUES = 0x0E000000;
	private enum ReadWriteRespBitMaskE {
		FIXED_VALUES_MASK(0xEFE00000, NO_SHIFTING),
		P0(0x10 * BYTE3_MULTIPLIER, NO_SHIFTING),
		DATA(0x001FFFE0, 5),
		P1(0x01 * BYTE0_MULTIPLIER, NO_SHIFTING),
		;
		public int bitMask;
		public int shiftRightCnt;
		private ReadWriteRespBitMaskE(int bitMaskVal, int shiftRightBitCnt) {
			bitMask = bitMaskVal;
			shiftRightCnt = shiftRightBitCnt;
		}
		public static int getData(int resp) {
			return ((resp & DATA.bitMask) >>> DATA.shiftRightCnt);
		}
		public static int getFixedValuesField(int resp) {
			return (resp & FIXED_VALUES_MASK.bitMask);
		}
	}
	//the fixed values field is bigger here than in ReadWriteRespBitMaskE, but
	//the expected value turns out to be the same....
	private static final int READ_WRITE_ERROR_RESP_FULL_FIXED_VALUES = 0x0E000000;
	private enum ReadWriteErrorRespBitMaskE {
		FIXED_VALUES_MASK(0xEFF80000),
		P0(0x10 * BYTE3_MULTIPLIER),		
		FAULTS_AND_CHK_AND_ERRORS(0x000700FE), //see FaultsAndCHKAndErrorRespBitsE for details
		DONT_CARE(0xFF * BYTE1_MULTIPLIER),
		P1(0x01 * BYTE0_MULTIPLIER),
		;
		public int bitMask;
		private ReadWriteErrorRespBitMaskE(int bitMaskVal) {
			bitMask = bitMaskVal;
		}
		public static int getFixedValuesField(int resp) {
			return (resp & FIXED_VALUES_MASK.bitMask);
		}
	}

	
	/*======================================== send cmds, get responses ============*/
	//assume never called or used when Tm744Opts.isGyroNavX() is true

	private enum ParitySelectE {P, P0, P1}
	private static boolean	isOddParity(ParitySelectE typeOfParity, int data) {
		boolean ans;
		int cnt, startNdx, endNdx;
		int mask;
		
		cnt = 0;
		switch(typeOfParity) {
		case P:
		case P1:
			startNdx = 0;
			endNdx = 32;
			break;
		case P0:
			startNdx = 16;
			endNdx = 32;
			break;
		default:
			startNdx = 0;
			endNdx = 0;
			break;
		}
		for(int i=startNdx; i<endNdx; i++) {
			mask = 1 << i;
			if(0 != (data & mask)) { cnt++; }
		}
		ans = (1 == (cnt & 0x01));
		return ans;
	}
	
	public enum CmdTypeE {SENSOR_DATA, SENSOR_DATA_WITH_CHK, READ, WRITE, NONE}
	public enum RespTypeE {SENSOR_DATA, READ, WRITE, READ_WRITE_ERROR, IGNORABLE_FIRST_RESPONSE, 
							INVALID_DUE_TO_P1_PARITY_ERRORS, INVALID_DUE_TO_P0_PARITY_ERRORS }
	public class CmdAndResponse {
		public CmdTypeE cmdType;
		public int cmd;
		public int resp;
		public CmdTypeE prevCmdType;
		public RespTypeE respType;
		public int retCode;	
		public int seqNbr; //used only for SensorData commands
		public int respSeqNbr; //used only for SensorData responses
		
		public static final int BUFFER_SIZE = 4;
		
		public boolean isRespParityOk() {
			boolean ans = true;
			if(respType == RespTypeE.INVALID_DUE_TO_P0_PARITY_ERRORS || respType == RespTypeE.INVALID_DUE_TO_P1_PARITY_ERRORS) {
				ans = false;
			}
			return ans;
		}
		
		public String toStringFull() {
			//assume never called when Tm744Opts.isGyroNavX() is true

			String ans = "";
			ans += "cmd=" + String.format("0x%08X", cmd) + " (" + cmdType.toString() + ")";
			if(cmdType==CmdTypeE.SENSOR_DATA || cmdType==CmdTypeE.SENSOR_DATA_WITH_CHK) {
				ans += " seqNbr=" + seqNbr;
			}
			else if(cmdType==CmdTypeE.READ) {
				ans += " (read reg " + ReadWriteCmdBitMaskE.getRegisterType(cmd).toString() + ")";
			}
			ans += " retCode=" + retCode;
			ans += " prevCmdType=" + prevCmdType.toString() + 
					" resp=" + String.format("0x%08X", resp) + " (" + respType.toString() + ")";
			ans += " ST:" + SensorDataResponseStatusE.getStatusType(resp).toString();
			if(respType==RespTypeE.SENSOR_DATA) {
				ans += " respSeqNbr=" + respSeqNbr + " data=" + String.format("0x%04X", SensorDataRespBitMaskE.getData(resp));
			}
			else if(respType == RespTypeE.READ || respType == RespTypeE.WRITE) {
				ans += " data=" + String.format("0x%04X", ReadWriteRespBitMaskE.getData(resp));
			}
			
			return ans;
		}
	}
	
	private static CmdTypeE m_prevCmdType = CmdTypeE.NONE;
	protected void doSpiTransaction(CmdAndResponse cr) {
		//assume never called when Tm744Opts.isGyroNavX() is true

		cr.prevCmdType = m_prevCmdType;
		m_prevCmdType = cr.cmdType;
		
		if( ! isOddParity(ParitySelectE.P, cr.cmd)) {
			cr.cmd |= SensorDataCmdFieldsBitMaskE.P.bitMask;
		}
		
		//---- convert command to bytes and send
		byte[] cmdBuffer = new byte[CmdAndResponse.BUFFER_SIZE];
		byte[] respBuffer = new byte[CmdAndResponse.BUFFER_SIZE];
		
		//ByteBuffer has methods that convert from int to byte[] and vice-versa, specifying endianness
		//changes made in the ByteBuffer are reflected in the byte[] too.
		ByteBuffer cmdBb = ByteBuffer.wrap(cmdBuffer);
		ByteBuffer respBb = ByteBuffer.wrap(respBuffer);
		cmdBb.order(ByteOrder.BIG_ENDIAN);
		respBb.order(ByteOrder.BIG_ENDIAN);
		
		//set parity bit in command for odd parity
		switch(cr.cmdType) {
		case SENSOR_DATA:
			if( ! isOddParity(ParitySelectE.P, cr.cmd)) { cr.cmd |= SensorDataCmdFieldsBitMaskE.P.bitMask; }
			break;
		case READ:
		case WRITE:
			if( ! isOddParity(ParitySelectE.P, cr.cmd)) { cr.cmd |= ReadWriteCmdBitMaskE.P.bitMask; }
			break;
		default:
			break;
		}
		
		//put the command in the buffer
		cmdBb.putInt(cr.cmd);
		
		//there's no documentation for the return code, but we'll save it anyway
		cr.retCode = m_spi.transaction(cmdBuffer, respBuffer, CmdAndResponse.BUFFER_SIZE);
		
		//get the response out of the buffer
		cr.resp = respBb.getInt();
		
		if(cr.prevCmdType.equals(CmdTypeE.NONE)) { 
			if(cr.resp != VERY_FIRST_RESPONSE_FROM_DEVICE) {
				P.println(PrtYn.Y, m_classNameId + 
							" It looks like we just sent the very first cmd to the gyro, but didn't get the expected response.");
			}
			cr.respType = RespTypeE.IGNORABLE_FIRST_RESPONSE; 
		}
		else if(! isOddParity(ParitySelectE.P1, cr.resp)) { 
			cr.respType = RespTypeE.INVALID_DUE_TO_P1_PARITY_ERRORS;			
		}
		else if(! isOddParity(ParitySelectE.P0, cr.resp)) { 
			cr.respType = RespTypeE.INVALID_DUE_TO_P0_PARITY_ERRORS;			
		}
		else if(SensorDataRespBitMaskE.getStatus(cr.resp) != SensorDataResponseStatusE.READ_WRITE_RESPONSE.value) {
			cr.respType = RespTypeE.SENSOR_DATA;
		}
		//the status bits of the sensor read response map into the fixed values field of read/write responses
		else if(ReadWriteRespBitMaskE.getFixedValuesField(cr.resp) == READ_RESP_FIXED_VALUES) {
			cr.respType = RespTypeE.READ;
		}
		else if(ReadWriteRespBitMaskE.getFixedValuesField(cr.resp) == WRITE_RESP_FIXED_VALUES) {
			cr.respType = RespTypeE.WRITE;
		}
		else if(ReadWriteRespBitMaskE.getFixedValuesField(cr.resp) == READ_WRITE_ERROR_RESP_FIXED_VALUES) {
			if(ReadWriteErrorRespBitMaskE.getFixedValuesField(cr.resp) != READ_WRITE_ERROR_RESP_FULL_FIXED_VALUES) {
				P.println(PrtYn.Y, m_classNameId + 
							" some of the expected hardcoded bit values in this R/W Error response are wrong.");
			}
			cr.respType = RespTypeE.READ_WRITE_ERROR;
		}
	}
	
	private int m_nextSeqNbr = 0;
	private int getNewSeqNbr() { 
		int ans;
		if(isReady()) { ans = m_nextSeqNbr++ % 8; }
		else { ans = 0; }
		return ans;
	}

	/**
	 * builds a sensor data command to send to the gyro
	 */
	public void bldSensorDataCommand(CmdAndResponse cr) {
		bldSensorDataCommand(cr, false);
	}
	/**
	 * builds a sensor data command to send to the gyro
	 * @param chkBit - true to force the ADXRS453 to generate faults. Done to 
	 *                 verify that faults can be reported when they actually occur.
	 */
	private void bldSensorDataCommand(CmdAndResponse cr, boolean chkBit) {
		//x=a?b:c; means if(a){x=b;}else{x=c;}
		cr.cmdType = chkBit ? CmdTypeE.SENSOR_DATA_WITH_CHK : CmdTypeE.SENSOR_DATA;
		cr.seqNbr = getNewSeqNbr();
		cr.cmd = SENSOR_DATA_CMD_FIXED_VALUES |
				SensorDataCmdFieldsBitMaskE.getSeqNbrBitsToOR(cr.seqNbr);
		if(chkBit) { cr.cmd |= SensorDataCmdFieldsBitMaskE.CHK.bitMask; }
	}
	
	/**
	 * builds a read command to send to the gyro
	 * @param cr - CmdAndResponse object to use to hold the command (and later the response)
	 * @param regToRead - indicates which gyro register to read
	 */
	private void bldReadCommand(CmdAndResponse cr, RegisterE regToRead) {
		//x=a?b:c; means if(a){x=b;}else{x=c;}
		if((regToRead.regAddr & 0x01) != 0) {
			//see device datasheet for details
			P.println(PrtYn.Y, m_classNameId + "should only be reading from even-numbered registers, not from " + regToRead.regAddr); 
		}
		cr.cmdType = CmdTypeE.READ;
		cr.cmd = READ_CMD_FIXED_VALUES | ReadWriteCmdBitMaskE.getAddrBitsToOR(regToRead);
	}
	
	/**
	 * builds a read command to send to the gyro
	 * @param cr - CmdAndResponse object to use to hold the command (and later the response)
	 * @param regToRead - indicates which gyro register to read
	 * @param data - 16 bits of data to write
	 */
	private void bldWriteCommand(CmdAndResponse cr, RegisterE regToRead, short data) {
		//x=a?b:c; means if(a){x=b;}else{x=c;}
		cr.cmdType = CmdTypeE.WRITE;
		cr.cmd = WRITE_CMD_FIXED_VALUES | ReadWriteCmdBitMaskE.getAddrBitsToOR(regToRead);
		cr.cmd |= ReadWriteCmdBitMaskE.getDataBitsToOR(data);
	}
	
	
	@Override
	public void initSendable(SendableBuilder builder) {
		// TODO Auto-generated method stub
		
	}
	
	public static class LocalCommands {
		private final static LocalCommands lcInstance = new LocalCommands();
		public static LocalCommands getInstance() { return lcInstance; }
		private LocalCommands() {}
		
		public class LocalCmd_RecalibrateRate extends Command implements TmToolsI {
			TmGyroADXRS453SPI l_gyro;
			DsNamedControlsE l_namedCntl;
			double l_accumRate;
			double l_minRate;
			double l_maxRate;
//			double l_accumTime;
//			double l_avgRate;
//			double l_maxError;
			double l_maxErrorAllowed = 0.01;
//			Timer l_timer = new Timer();
			boolean l_firstPassThruExecute;
			double l_initialRate;
			int l_sampleCount;
			int l_minSampleCount = 20;
			public LocalCmd_RecalibrateRate(TmGyroADXRS453SPI gyroObj, DsNamedControlsE namedCntl,
					double maxErrorAllowed) {
				// Use requires() here to declare subsystem dependencies
				// eg. requires(chassis);
				l_gyro = gyroObj;
				l_namedCntl = namedCntl;
				l_maxErrorAllowed = maxErrorAllowed;
			}

			// Called just before this Command runs the first time
			protected void initialize() {
				P.println(Tt.getClassName(this) + " running -- assume robot is idle and compressor NOT running!!");
				l_accumRate = 0;
//				l_accumTime = 0;
//				l_avgRate = 0;
//				l_maxError = 0;
//				l_timer.reset();
				l_firstPassThruExecute = true;
				l_sampleCount = 0;
				l_initialRate = 0;
				l_minRate = 0;
				l_maxRate = 0;
			}

			// Called repeatedly when this Command is scheduled to run
			protected void execute() {
				
				//note: x=a?b:c; means if(a){x=b;}else{x=c;}
				double newRate = Tm744Opts.isGyroNavX() ? 0.0 : l_gyro.getRate();
				
				if(l_firstPassThruExecute) {
					l_initialRate = newRate;
					l_accumRate = l_initialRate;
				} else {
					l_accumRate += newRate;
				}
				l_sampleCount++;
				if(newRate<l_minRate) { l_minRate = newRate; }
				else if(newRate>l_maxRate) { l_maxRate = newRate; }
				
			}

			// Make this return true when this Command no longer needs to run execute()
			protected boolean isFinished() {
				boolean ans = false;
				if( ! l_namedCntl.getEnt().getButton()) {
					//button has been released
					ans = true;
					if(l_sampleCount>l_minSampleCount && (l_maxRate - l_minRate) < l_maxErrorAllowed) {
						l_gyro.m_rateAdj = l_accumRate/l_sampleCount;
						P.println(PrtYn.Y, "gyro rate will be adjusted by l_avgRate based on " +
								l_sampleCount + " samples within " + l_maxErrorAllowed +
								" of one other");
					} else {
						P.println(PrtYn.Y, "no new gyro rate adjustment value. " +
								" sampleCount=" + l_sampleCount + " (min is " +
								l_minSampleCount + "), range=" + (l_maxRate - l_minRate) +
								" (maxAllowed=" + l_maxErrorAllowed + ")");
					}
				}
				return ans;
			}

			// Called once after isFinished returns true
			protected void end() {
			}

			// Called when another command which requires one or more of the same
			// subsystems is scheduled to run
			protected void interrupted() {
			}
		}

	}
	
	
	

	/*============================== Live Window support ============*/

//	@Override
//	public void initTable(ITable subtable) {
//		m_table = subtable;
//		updateTable();
//	}
//
//	private ITable m_table;
//
//	@Override
//	public ITable getTable() {
//		return m_table;
//	}
//
//	@Override
//	public String getSmartDashboardType() {
////		return "Tm744SPIGyroADXRS453";
//		return "Tm744-SPIGyro-ADXRS453-Ver1";
//	}
//
//	@Override
//	public void updateTable() {
//		if (m_table != null) {
//			m_table.putNumber("angle", getAngle());
////			m_table.putNumber("X", getX());
////			m_table.putNumber("Y", getY());
////			m_table.putNumber("Z", getZ());
//		}
//	}
//
//	@Override
//	public void startLiveWindowMode() {
//		// TODO Auto-generated method stub
//		
//	}
//
//	@Override
//	public void stopLiveWindowMode() {
//		// TODO Auto-generated method stub
//		
//	}

}
