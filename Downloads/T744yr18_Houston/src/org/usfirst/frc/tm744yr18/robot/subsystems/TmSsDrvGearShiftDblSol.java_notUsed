package org.usfirst.frc.tm744yr18.robot.subsystems;

import org.usfirst.frc.tm744yr18.robot.commands.TmCCmdDriveShiftGear;
import org.usfirst.frc.tm744yr18.robot.config.TmHdwrDsCntls.DsNamedControlsE;
import org.usfirst.frc.tm744yr18.robot.config.TmHdwrRoCntls.RoNamedControlsE;
import org.usfirst.frc.tm744yr18.robot.config.TmSdKeysI;
import org.usfirst.frc.tm744yr18.robot.devices.TmFakeable_DoubleSolenoid;
import org.usfirst.frc.tm744yr18.robot.helpers.TmSdMgr;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmDsControlUserI;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmRoControlUserI;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmStdSubsystemI;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmToolsI;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmToolsI.P.PrtYn;

import edu.wpi.first.wpilibj.DoubleSolenoid;
import edu.wpi.first.wpilibj.Solenoid;
import edu.wpi.first.wpilibj.command.Subsystem;

/**
 *
 */
public class TmSsDrvGearShiftDblSol extends Subsystem implements TmStdSubsystemI, TmToolsI, 
															TmDsControlUserI, TmRoControlUserI {

	public enum DrvGearsE { 
		HIGH(DoubleSolenoid.Value.kForward),
		LOW(DoubleSolenoid.Value.kReverse),
		OFF(DoubleSolenoid.Value.kOff),
		;
		private final DoubleSolenoid.Value eDirection;
		public DoubleSolenoid.Value getSolDirection() { return eDirection; }
		private DrvGearsE(DoubleSolenoid.Value dir) { eDirection = dir; }
	};
	private static DrvGearsE m_drvShifterCurrentPosition;
	private static DrvGearsE m_drvShifterPrevPosition;

	private static TmFakeable_DoubleSolenoid m_gearShifter;

	/** 
	 * handle making the singleton instance of this class and giving
	 * others access to it
	 */
	private static TmSsDrvGearShiftDblSol m_instance;
	public static synchronized TmSsDrvGearShiftDblSol getInstance() {
		if (m_instance == null) {
			m_instance = new TmSsDrvGearShiftDblSol();
		}
		return m_instance;
	}

	public void initDefaultCommand() {
		// Set the default command for a subsystem here.
		//setDefaultCommand(new MySpecialCommand());
	}

	@Override
	public void sssDoInstantiate() {
		// stuff needed before doRobotInit is called
		
	}

	@Override
	public void sssDoRobotInit() {
		m_gearShifter = new TmFakeable_DoubleSolenoid(this,
				RoNamedControlsE.DRV_SHIFTER_HIGH_GEAR, 
				RoNamedControlsE.DRV_SHIFTER_LOW_GEAR);	
		m_drvShifterPrevPosition = m_drvShifterCurrentPosition = DrvGearsE.OFF;
		
//	    public TmCCmdDriveShiftGear(TmSsDrvGearShiftDblSol.DrvGearsE requestedGear) {
		DsNamedControlsE.DRIVE_HIGH_GEAR_BTN.getEnt().whenPressed(this, 
				new TmCCmdDriveShiftGear(TmSsDrvGearShiftDblSol.DrvGearsE.HIGH));
		DsNamedControlsE.DRIVE_LOW_GEAR_BTN.getEnt().whenPressed(this, 
				new TmCCmdDriveShiftGear(TmSsDrvGearShiftDblSol.DrvGearsE.LOW));

//		m_gearShifter.addToLiveWindow(Tm16Misc.LwSubSysName.SS_DRV_GEARSHIFTER, 
//							Tm16Misc.LwItemNames.DRV_GEARSHIFTER);
//		TmSdDbgSD.dbgPutBoolean(Tm16Misc.SdKeysE.KEY_DRIVE_GEARSHIFT, isDrvShifterInHighGear());
		postToSd(); //TmPostToSd.dbgPutBoolean(TmMiscSdKeys.SdKeysE.KEY_DRIVE_GEARSHIFT_IS_HIGH, isDrvShifterInHighGear());
	}

	@Override
	public void sssDoDisabledInit() {
		m_drvShifterPrevPosition = m_drvShifterCurrentPosition = DrvGearsE.HIGH;
		postToSd(); //TmPostToSd.dbgPutBoolean(TmMiscSdKeys.SdKeysE.KEY_DRIVE_GEARSHIFT_IS_HIGH, isDrvShifterInHighGear());
	}

	@Override
	public void sssDoAutonomousInit() {
		updateDrvShifter(DrvGearsE.HIGH);
		postToSd(); //
	}

	@Override
	public void sssDoTeleopInit() {
		updateDrvShifter(DrvGearsE.HIGH);
		postToSd(); //
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
		postToSd(); //

	}

	@Override
	public void sssDoTeleopPeriodic() {
		postToSd(); //

	}

	@Override
	public void sssDoLwTestPeriodic() {
	}

	public static boolean isDrvShifterInLowGear()
	{
		return m_gearShifter.get().equals(DrvGearsE.LOW.getSolDirection());
	}
	public static boolean isDrvShifterInHighGear()
	{
		return m_gearShifter.get().equals(DrvGearsE.HIGH.getSolDirection());
	}
	
	public static DrvGearsE getCurrentGear() {
		DrvGearsE ans;
		ans = DrvGearsE.OFF;
		DoubleSolenoid.Value dir = m_gearShifter.get();
		if( dir.equals(DrvGearsE.LOW.getSolDirection()) ) {
			ans = DrvGearsE.LOW;
		} 
		else if( dir.equals(DrvGearsE.HIGH.getSolDirection()) ) {
			ans = DrvGearsE.HIGH;
		} 
		return ans;
	}

	private static final Object m_gearShifterLock = new Object();
	public static void updateDrvShifter(DrvGearsE requestedGear)
	{
		String msgInfo = "??";
		String msgSuffix = "";
		synchronized(m_gearShifterLock) {
			if(requestedGear.equals(DrvGearsE.LOW))
			{
				m_gearShifter.set(DrvGearsE.LOW.getSolDirection());
				m_drvShifterCurrentPosition = DrvGearsE.LOW;
			}
			else if(requestedGear.equals(DrvGearsE.HIGH))
			{
				m_gearShifter.set(DrvGearsE.HIGH.getSolDirection());
				m_drvShifterCurrentPosition = DrvGearsE.HIGH;
			}
			else
			{
				//nothing
			}

			msgInfo = "already";
			//print a dbg msg whenever we've actually changed positions
			if( ! m_drvShifterCurrentPosition.equals(m_drvShifterPrevPosition))
			{
				msgInfo = "now";
				m_drvShifterPrevPosition = m_drvShifterCurrentPosition;
			} 
		}

		msgSuffix = " (software expected " + m_drvShifterCurrentPosition.toString() +")";
		P.println(PrtYn.Y, "shifter hardware " + msgInfo + " in " + 
				(isDrvShifterInLowGear() ? "LOW" : 
					isDrvShifterInHighGear() ? "HIGH" : ("UNKNOWN(" + m_gearShifter.get().toString() + ")")) + 
						" gear" + msgSuffix);
		postToSd(); //TmPostToSd.dbgPutBoolean(TmMiscSdKeys.SdKeysE.KEY_DRIVE_GEARSHIFT_IS_HIGH, isDrvShifterInHighGear());
	}
	
	public static void postToSd() {
		//orlando real bot all hi/lo gear stuff correct except driver station boolean. HACK!!!
		TmSdMgr.putBoolean(TmSdKeysI.SdKeysE.KEY_DRIVE_GEARSHIFT_IS_HIGH, isDrvShifterInHighGear());
		//TmPostToSd.dbgPutBoolean(TmMiscSdKeys.SdKeysE.KEY_DRIVE_GEARSHIFT_IS_HIGH, ! isDrvShifterInHighGear());
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
	public boolean isFake() {
		// TODO Auto-generated method stub
		return false;
	}

//	/**
//	 * this was used in 2016 in autonomous...
//	 */
//    public synchronized void toggleShifter()
//    {
//        if(isDrvShifterInLowGear())
//        {
//        	TmSsDrvGearShift.updateDrvShifter(DrvGearsE.HIGH);
//        }
//        else
//        {
//        	TmSsDrvGearShift.updateDrvShifter(DrvGearsE.LOW);
//        }
//    }

}

