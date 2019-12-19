package org.usfirst.frc.tm744yr18.robot.devices;

import java.util.ArrayList;
import java.util.List;

import org.usfirst.frc.tm744yr18.robot.config.TmHdwrRoCntls.RoNamedControlsE;
import org.usfirst.frc.tm744yr18.robot.exceptions.TmExceptions;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmItemAvailabilityI;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmRoControlUserI;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmToolsI.P;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmToolsI.Tt;

import edu.wpi.first.wpilibj.Solenoid;

public class TmFakeable_Solenoid implements TmItemAvailabilityI {

	private RoNamedControlsE m_namedCntl = null;
	private Solenoid m_realObj = null;
	private boolean m_beingFaked = false;

	@Override
	public void configAsFake() {
		m_realObj = null;
		m_beingFaked = true;
	}

	public boolean isFake() {
		return m_beingFaked;
	}

	public boolean isReal() {
		return !m_beingFaked;
	}

	private FakeParms m_fakeParms;

	public class FakeParms {
		//any fields or methods that the 'fake' object needs to
		//emulate should be coded here.
		boolean f_thing;

		public boolean get() {
			return f_thing;
		}

		public void set(boolean value) {
			f_thing = value;
		}

		public FakeParms() {
			f_thing = false;
		}
	}

	public static class TrackInstances {
		private static final TrackInstances tiInstance = new TrackInstances();
		public static TrackInstances getInstance() { return tiInstance; }
		private TrackInstances() {}
		
		static List<TrackInstEntry> tiDevInstanceList = new ArrayList<>();
		
		public class TrackInstEntry<CU extends TmRoControlUserI> {
			public CU fctCntlUser;
			public TmFakeable_Solenoid fctObj;
			public RoNamedControlsE fctNamedCntl;
			public TrackInstEntry(CU cntlUser, TmFakeable_Solenoid fct, RoNamedControlsE namedCntl) {
				fctCntlUser = cntlUser;
				fctObj = fct;
				fctNamedCntl = namedCntl;
			}
		}
		
		public static List<TrackInstEntry> getList() { return tiDevInstanceList; }
		
		//call from constructor: TrackInstances.trackUserAndDevInstances(cntlUser, this, namedCntl);
		public static <CU extends TmRoControlUserI> void trackUserAndDevInstances(CU cntlUser, 
														TmFakeable_Solenoid devInst, RoNamedControlsE namedCntl) {
			for(TrackInstEntry tie : TrackInstances.tiDevInstanceList) {
				if(tie.fctNamedCntl.equals(namedCntl)) {
					throw TmExceptions.getInstance().new MultipleUsersForRoControlEx(cntlUser.getClass().getSimpleName() + " CONFIGURATION ERROR!!!: " +
							tie.fctCntlUser.getClass().getSimpleName() + " has already allocated a " +
							Tt.getClassName(devInst) + " object for " + tie.fctNamedCntl.name());
				}
			}

			TrackInstances.tiDevInstanceList.add(TrackInstances.getInstance().new TrackInstEntry(cntlUser, devInst, namedCntl));
		}

		/**
		 * is this needed?? //TODO
		 * @param namedCntlToFind
		 * @return
		 */
		public static TmFakeable_Solenoid getInstanceFromNamedControl(RoNamedControlsE namedCntlToFind) {
			TmFakeable_Solenoid ans = null;
			for(TrackInstEntry t : tiDevInstanceList) {
				if(t.fctNamedCntl.equals(namedCntlToFind)) {
					ans = t.fctObj;
					break; //end the loop
				}
			}
			return ans;
		}
	}
	
	
	
	//top-level constructor
	public <CU extends TmRoControlUserI> TmFakeable_Solenoid(CU cntlUser, RoNamedControlsE namedCntl) {
		TrackInstances.trackUserAndDevInstances(cntlUser, this, namedCntl);
		m_namedCntl = namedCntl;
		
		m_fakeParms = new FakeParms();
		String thisClassName = Tt.getClassName(this);
//		String nameForObj = Tt.extractClassName(namedIoDef) + "." + namedIoDef.name();
		
		String errMsgPrefix = thisClassName + " " + m_namedCntl.name() + " got an exception:";

		try {
//			int chan = namedIoDef.getNamedConnDef().getConnectionIndex();
			int chan = namedCntl.getEnt().cNamedConn.getEnt().getConnectionFrcIndex();
			if(namedCntl.getEnt().cCntlAvail.equals(ItemAvailabilityE.USE_FAKE)) {
				configAsFake();
			} else {
				m_realObj = new Solenoid(chan);
			}
			//set up whatever else is needed here...
		}
		//some devices don't generate errors or exceptions when they're not
		//present.  For those, need to use preferences file or some other 
		//means of telling the code to use 'fake' devices
//		catch (Fixme_ExceptionIndicatingRealDeviceNotAvailable t) {
//			TmExceptions.reportExceptionOneLine(t, errMsgPrefix);
//			setupAsFake();
//		} 
		catch (Throwable t) {
			TmExceptions.reportExceptionMultiLine(t, errMsgPrefix);
			configAsFake();
		}
//		if (Fixme_somePreferencesSettingOrOptionFlag == "use fake") {
//			Tt.println("Per ... setting, " + thisClassName + " " + nameForObj + " will be a FAKE Fixme_ClassName");
//		} else 
		if (isFake()) {
			P.println(thisClassName + " " + m_namedCntl.name() + " will be a FAKE Solenoid");
		}
	}

	//--- helper methods for methods used with real device
	public boolean get() {
		if (isFake()) {
			return m_fakeParms.get();
		} else {
		  /**
		   * Read the current value of the solenoid.
		   *
		   * @return True if the solenoid output is on or false if the solenoid output is off.
		   */
			return m_realObj.get();
		}
	}

	public void set(boolean newVal) {
		if (isFake()) {
			m_fakeParms.set(newVal);
		} else {
			  /**
			   * Set the value of a solenoid.
			   *
			   * @param on True will turn the solenoid output on. False will turn the solenoid output off.
			   */
			m_realObj.set(newVal);
		}
	}

	@Override
	public boolean isFakeableItem() { return true; }

}
