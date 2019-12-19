package org.usfirst.frc.tm744yr18.robot.devices;

import java.util.ArrayList;
import java.util.List;

import org.usfirst.frc.tm744yr18.robot.config.TmHdwrRoCntls.RoNamedControlsE;
import org.usfirst.frc.tm744yr18.robot.config.TmSdKeysI.SdKeysE;
import org.usfirst.frc.tm744yr18.robot.exceptions.TmExceptions;
import org.usfirst.frc.tm744yr18.robot.helpers.TmSdMgr;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmRoControlUserI;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmToolsI.P;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmToolsI.Tt;

import edu.wpi.first.wpilibj.DigitalInput;
import t744opts.Tm744Opts;

public class TmFakeable_RoDigitalInput {
	
	private RoNamedControlsE m_namedCntl = null;
	private DigitalInput m_realObj = null;
	private boolean m_beingFaked = false;
	
	private static String m_fakeDioMsg = "";
	private static List<RoNamedControlsE> m_fakeCntlsList = new ArrayList<>();
	private void setupAsFake() {
		m_realObj = null;
		m_beingFaked = true;
		
		boolean isCntlInList = false;
		for(RoNamedControlsE rnc : m_fakeCntlsList) {
			if(rnc.equals(m_namedCntl)) {
				isCntlInList = true;
			}
		}
		if( ! isCntlInList) {
			if(m_fakeDioMsg.equals("")) {
				m_fakeDioMsg = "FAKE RoDigIn cntls: " + m_namedCntl.name();
			} else {
				m_fakeDioMsg += ", " + m_namedCntl.name();
			}
		}
		if( ! m_fakeDioMsg.equals("")) {
			TmSdMgr.putString(SdKeysE.KEY_MISC_FAKE_DIG_INPUTS, m_fakeDioMsg);
		}
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
		
		/**
		 * intended for use by simulation code only....
		 * @param newValue
		 */
		public void fake_set(boolean newValue) {
			f_thing = newValue;
		}

		public FakeParms(boolean defaultReading) {
			f_thing = defaultReading;
		}
	}

	public static class TrackInstances {
		private static final TrackInstances tiInstance = new TrackInstances();
		public static TrackInstances getInstance() { return tiInstance; }
		private TrackInstances() {}
		
		static List<TrackInstEntry> tiDevInstanceList = new ArrayList<>();
		
		public class TrackInstEntry<CU extends TmRoControlUserI> {
			public CU fctCntlUser;
			public TmFakeable_RoDigitalInput fctObj;
			public RoNamedControlsE fctNamedCntl;
			public TrackInstEntry(CU cntlUser, TmFakeable_RoDigitalInput fct, RoNamedControlsE namedCntl) {
				fctCntlUser = cntlUser;
				fctObj = fct;
				fctNamedCntl = namedCntl;
			}
		}
		
		public static List<TrackInstEntry> getList() { return tiDevInstanceList; }
		
		//call from constructor: TrackInstances.trackUserAndDevInstances(cntlUser, this, namedCntl);
		public static <CU extends TmRoControlUserI> void trackUserAndDevInstances(CU cntlUser, 
														TmFakeable_RoDigitalInput devInst, RoNamedControlsE namedCntl) {
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
		public static TmFakeable_RoDigitalInput getInstanceFromNamedControl(RoNamedControlsE namedCntlToFind) {
			TmFakeable_RoDigitalInput ans = null;
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
	public <CU extends TmRoControlUserI> TmFakeable_RoDigitalInput(CU cntlUser, RoNamedControlsE namedCntl, boolean defaultReading) {
		
		if(namedCntl.equals(RoNamedControlsE.ARM_STAGE1_BOTTOM_LIMIT_SWITCH) ||
				namedCntl.equals(RoNamedControlsE.ARM_STAGE2_TOP_LIMIT_SWITCH) ||
				namedCntl.equals(RoNamedControlsE.ARM_STAGE2_BOTTOM_LIMIT_SWITCH)) {
			int junk = 5; //debug breakpoint!!
		}
		TrackInstances.trackUserAndDevInstances(cntlUser, this, namedCntl);
		
		m_namedCntl = namedCntl;
		
		m_fakeParms = new FakeParms(defaultReading);
		String thisClassName = Tt.getClassName(this);
//		String nameForObj = Tt.extractClassName(namedIoDef) + "." + namedIoDef.name();
		
		String errMsgPrefix = thisClassName + " " + m_namedCntl.name() + " got an exception:";

		if(Tm744Opts.isInSimulationMode()) {
			setupAsFake();
		} else {
			try {
				//			int chan = namedIoDef.getNamedConnDef().getConnectionIndex();
				int chan = namedCntl.getEnt().cNamedConn.getEnt().getConnectionFrcIndex();
				m_realObj = new DigitalInput(chan);
				//set up whatever else is needed here...
			}
			catch (Throwable t) {
				TmExceptions.reportExceptionMultiLine(t, errMsgPrefix);
				setupAsFake();
			}
		}
		if (isFake()) {
			P.println(thisClassName + " " + m_namedCntl.name() + " will be a FAKE DigitalInput");
		}
	}

	//--- helper methods for methods used with real device
	public boolean get() {
		if (isFake()) {
			return m_fakeParms.get();
		} else {
			return m_realObj.get();
		}
	}

//	public void set(boolean newVal) {
//		if (isFake()) {
//			m_fakeParms.set(newVal);
//		} else {
//			m_realObj..set(newVal);
//		}
//	}
}
