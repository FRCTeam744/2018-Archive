package org.usfirst.frc.tm744yr18.robot.subsystems;

import org.usfirst.frc.tm744yr18.robot.commands.TmCCmdDriveShiftGear;
import org.usfirst.frc.tm744yr18.robot.config.TmHdwrDsCntls.DsNamedControlsE;
import org.usfirst.frc.tm744yr18.robot.config.TmHdwrRoCntls.RoNamedControlsE;
import org.usfirst.frc.tm744yr18.robot.config.TmSdKeysI;
import org.usfirst.frc.tm744yr18.robot.devices.TmFakeable_Solenoid;
import org.usfirst.frc.tm744yr18.robot.helpers.TmSdMgr;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmDsControlUserI;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmRoControlUserI;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmStdSubsystemI;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmToolsI;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmToolsI.P.PrtYn;

import edu.wpi.first.wpilibj.command.Subsystem;

/**
 *
 */
public class TmSsDrvGearShift extends Subsystem implements TmStdSubsystemI, TmToolsI, TmDsControlUserI, TmRoControlUserI {

	public enum DrvGearsE { 
		HIGH(false), //solenoid Off (default position)
		LOW(true), //solenoid 
		;
		//true=on false=off
		private final boolean eSolenoidSetting;
		private DrvGearsE(boolean isOn) { eSolenoidSetting = isOn; }
		
		
		public static DrvGearsE getGearFromReading(boolean solenoidReading) {
			DrvGearsE ans;
			if(solenoidReading==LOW.eSolenoidSetting) { ans = DrvGearsE.LOW; }
			else { ans = DrvGearsE.HIGH; }
			return ans;
		}
		
		public static DrvGearsE getDefaultGear() { 
			return DrvGearsE.HIGH;
		}
	};
	
	private static DrvGearsE m_drvShifterCurrentPosition;
	private static DrvGearsE m_drvShifterPrevPosition;

	protected static class SolenoidMgr {
		//these use true/false values
		protected static TmFakeable_Solenoid gearShiftFakeableSolenoid;
		DrvGearsE curState;
		protected static final Object lock = new Object();
		protected <CU extends TmRoControlUserI> SolenoidMgr(CU cntlUser, RoNamedControlsE gearShiftControl) {
			this(cntlUser, gearShiftControl, DrvGearsE.HIGH);
		}
		protected <CU extends TmRoControlUserI> SolenoidMgr(CU cntlUser, 
				RoNamedControlsE gearShiftControl, DrvGearsE initialState) {
			gearShiftFakeableSolenoid = new TmFakeable_Solenoid(cntlUser, gearShiftControl);
			curState = initialState;
			set(initialState);
		}
		
		public synchronized void set(DrvGearsE newState) {
			synchronized(lock) {
				switch(newState) {
				case HIGH:
				case LOW:
					gearShiftFakeableSolenoid.set(newState.eSolenoidSetting);
					break;
				}
				curState = newState;
			}
		}
		
		public synchronized DrvGearsE get() {
			DrvGearsE ans = DrvGearsE.getDefaultGear();
			synchronized(lock) {
				boolean hdwr = gearShiftFakeableSolenoid.get();
				ans = DrvGearsE.getGearFromReading(hdwr);
			}
			return ans; 
		}
		
		public synchronized void configAsFake() {
			gearShiftFakeableSolenoid.configAsFake();
		}
	}
	
	static SolenoidMgr m_gearShifter;
	
	/** 
	 * handle making the singleton instance of this class and giving
	 * others access to it
	 */
	private static final TmSsDrvGearShift m_instance = new TmSsDrvGearShift();
	public static synchronized TmSsDrvGearShift getInstance() { return m_instance; }

	public void initDefaultCommand() {
		// Set the default command for a subsystem here.
		//setDefaultCommand(new MySpecialCommand());
	}

	@Override
	public void sssDoInstantiate() {
		// stuff needed before sssDoRobotInit is called
		
	}

	@Override
	public void sssDoRobotInit() {
		m_gearShifter = new SolenoidMgr(this,
				RoNamedControlsE.DRV_SHIFTER_LOW_GEAR,
				DrvGearsE.HIGH);	
		m_drvShifterPrevPosition = m_drvShifterCurrentPosition = DrvGearsE.HIGH;
		m_gearShifter.set(DrvGearsE.HIGH);
		DsNamedControlsE.DRIVE_LOW_GEAR_BTN.getEnt().whenPressed(this, 
				new TmCCmdDriveShiftGear(TmSsDrvGearShift.DrvGearsE.LOW));
		DsNamedControlsE.DRIVE_HIGH_GEAR_BTN.getEnt().whenPressed(this, 
				new TmCCmdDriveShiftGear(TmSsDrvGearShift.DrvGearsE.HIGH));

		postToSd();
	}

	@Override
	public void sssDoDisabledInit() {
		m_drvShifterPrevPosition = m_drvShifterCurrentPosition = DrvGearsE.HIGH;
		m_gearShifter.set(DrvGearsE.HIGH);
		postToSd();
	}

	@Override
	public void sssDoAutonomousInit() {
		updateDrvShifter(DrvGearsE.HIGH);
		postToSd();
	}

	@Override
	public void sssDoTeleopInit() {
		updateDrvShifter(DrvGearsE.HIGH);
		postToSd();
	}

	@Override
	public void sssDoLwTestInit() {
	}

	@Override
	public void sssDoRobotPeriodic() {
	}

	@Override
	public void sssDoDisabledPeriodic() {
	}

	@Override
	public void sssDoAutonomousPeriodic() {
		postToSd();
	}

	@Override
	public void sssDoTeleopPeriodic() {
		postToSd(); 
	}

	@Override
	public void sssDoLwTestPeriodic() {
	}

	public static boolean isDrvShifterInLowGear()
	{
		return m_gearShifter.get().equals(DrvGearsE.LOW);
	}
	public static boolean isDrvShifterInHighGear()
	{
		return m_gearShifter.get().equals(DrvGearsE.HIGH);
	}
	
	public static DrvGearsE getCurrentGear() {
		DrvGearsE ans;
		ans = m_gearShifter.get();
		return ans;
	}

	private static final Object m_gearShifterLock = new Object();
	public static void updateDrvShifter(DrvGearsE requestedGear)
	{
		String msgInfo = "??";
		String msgSuffix = "";
		synchronized(m_gearShifterLock) {
			switch(requestedGear) {
			case HIGH:
			case LOW:
				m_gearShifter.set(requestedGear);
				m_drvShifterCurrentPosition = requestedGear;
				break;
			//default:
				//break;
			}

			msgInfo = "already";
			if( ! m_drvShifterCurrentPosition.equals(m_drvShifterPrevPosition))
			{
				msgInfo = "now";
				m_drvShifterPrevPosition = m_drvShifterCurrentPosition;
			} 
		}

		msgSuffix = " (software expected " + m_drvShifterCurrentPosition.toString() +")";
//		P.println(PrtYn.Y, "shifter hardware is" + msgInfo + " in " + m_gearShifter.get().name() + " gear" + msgSuffix);
		P.println(PrtYn.Y, "shifter hardware is " + msgInfo + " in " + m_gearShifter.get().name() + " gear" + msgSuffix);
		postToSd(); //TmPostToSd.dbgPutBoolean(TmMiscSdKeys.SdKeysE.KEY_DRIVE_GEARSHIFT_IS_HIGH, isDrvShifterInHighGear());
	}
	
	public static void postToSd() {
		TmSdMgr.putBoolean(TmSdKeysI.SdKeysE.KEY_DRIVE_GEARSHIFT_IS_HIGH, isDrvShifterInHighGear());
	}

	@Override
	public boolean isFakeableItem() { return true; }

	@Override
	public void configAsFake() {}

	@Override
	public boolean isFake() { return false; }

}

