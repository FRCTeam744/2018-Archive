package org.usfirst.frc.tm744yr18.robot.devices;

import java.util.ArrayList;
import java.util.List;

import org.usfirst.frc.tm744yr18.robot.config.TmHdwrRoCntls.RoNamedControlsE;
import org.usfirst.frc.tm744yr18.robot.config.TmHdwrRoCntls.RoNamedModulesE;
import org.usfirst.frc.tm744yr18.robot.exceptions.TmExceptions;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmRoControlUserI;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmItemAvailabilityI;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmItemAvailabilityI.ItemAvailabilityE;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmToolsI.P;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmToolsI.Tt;

import edu.wpi.first.wpilibj.DoubleSolenoid;

//public class DoubleSolenoid extends SolenoidBase implements LiveWindowSendable {
public class TmFakeable_DoubleSolenoid implements TmItemAvailabilityI {
	private DoubleSolenoid m_realObj = null;
	private boolean m_beingFaked = false;

	public boolean isFake() {
		return m_beingFaked;
	}

	public boolean isReal() {
		return !m_beingFaked;
	}

	private FakeParms m_fakeParms;

	public class FakeParms {
		//any parms or methods that the 'fake' object needs to
		//emulate should be coded here.
		private DoubleSolenoid.Value f_solDirection = DoubleSolenoid.Value.kOff;
		
		public DoubleSolenoid.Value get() {
			return f_solDirection;
		}
		
		public void set(DoubleSolenoid.Value value) {
			f_solDirection = value;
		}
		
	}

	public static class TrackInstances {
		private static final TrackInstances tiInstance = new TrackInstances();
		public static TrackInstances getInstance() { return tiInstance; }
		private TrackInstances() {}
		
		static List<TrackInstEntry> tiDevInstanceList = new ArrayList<>();
		
		public class TrackInstEntry<CU extends TmRoControlUserI> {
			public CU fctCntlUser;
			public TmFakeable_DoubleSolenoid fctObj;
			public RoNamedControlsE fctNamedCntl;
			public TrackInstEntry(CU cntlUser, TmFakeable_DoubleSolenoid fct, RoNamedControlsE namedCntl) {
				fctCntlUser = cntlUser;
				fctObj = fct;
				fctNamedCntl = namedCntl;
			}
		}
		
		public static List<TrackInstEntry> getList() { return tiDevInstanceList; }
		
		//call from constructor: TrackInstances.trackUserAndDevInstances(cntlUser, this, namedCntl);
		public static <CU extends TmRoControlUserI> void trackUserAndDevInstances(CU cntlUser, 
														TmFakeable_DoubleSolenoid devInst, RoNamedControlsE namedCntl) {
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
		public static TmFakeable_DoubleSolenoid getInstanceFromNamedControl(RoNamedControlsE namedCntlToFind) {
			TmFakeable_DoubleSolenoid ans = null;
			for(TrackInstEntry t : tiDevInstanceList) {
				if(t.fctNamedCntl.equals(namedCntlToFind)) {
					ans = t.fctObj;
					break; //end the loop
				}
			}
			return ans;
		}
	}
	
	private static RoNamedControlsE m_forward;
	private static RoNamedControlsE m_reverse;
	
	public <CU extends TmRoControlUserI> TmFakeable_DoubleSolenoid(CU cntlUser, 
											RoNamedControlsE forward, RoNamedControlsE reverse) {

		if(forward.equals(RoNamedControlsE.ARM_STAGE1_BOTTOM_LIMIT_SWITCH)) {
			int junk = 5; //debug breakpoint
		}
		TrackInstances.trackUserAndDevInstances(cntlUser, this, forward);
		TrackInstances.trackUserAndDevInstances(cntlUser, this, reverse);
//		TrackInstances.tiDevInstanceList.add(TrackInstances.getInstance().new TrackInstEntry(cntlUser, this, forward));	
//		TrackInstances.tiDevInstanceList.add(TrackInstances.getInstance().new TrackInstEntry(cntlUser, this, reverse));	
		
		String thisClassName = Tt.getClassName(this);
		m_fakeParms = new FakeParms();
		m_forward = forward;
		m_reverse = reverse;
		if(forward==null || reverse==null) {
			String msg = thisClassName + ": " +
					"forward " + (forward==null?"null":"OK") + 
					", reverse " + (reverse==null?"null":"OK");
			throw new NullPointerException(msg);
		}
		if( ! forward.getEnt().cNamedMod.getEnt().equals(reverse.getEnt().cNamedMod.getEnt())) {
			String msg = thisClassName + ": " + forward.name() +
					" and " + reverse.name() +
					" are not on the same PCM module";
			throw TmExceptions.getInstance().new Team744RunTimeEx(msg);
		}
				
		String msgPrefix = thisClassName + " " + forward.name() + "/" + reverse.name()
		+ " got an exception";
		try {
			int modNdx = forward.getEnt().cNamedMod.getEnt().cModNamedConn.getEnt().getConnectionFrcIndex();
			int fwdNdx = forward.getEnt().cNamedConn.getEnt().getConnectionFrcIndex();
			int revNdx = reverse.getEnt().cNamedConn.getEnt().getConnectionFrcIndex();
			
			if((forward.getEnt().cNamedCntl.getEnt().cCntlAvail.equals(ItemAvailabilityE.USE_FAKE)) ||
					   (reverse.getEnt().cNamedCntl.getEnt().cCntlAvail.equals(ItemAvailabilityE.USE_FAKE)) )   {
				configAsFake();
			} else {
				m_realObj = new DoubleSolenoid(modNdx, fwdNdx, revNdx);
			}
			//set up whatever else is needed here...
		} catch (UnsatisfiedLinkError t) {
			//the exception we get when running in our simulation mode on a PC.
			//we don't need a big eye-catching message.  There were no exceptions
			//when running on a roboRIO with no PCMs connected.
			TmExceptions.reportExceptionOneLine(t, msgPrefix);
			configAsFake();
		} catch (Throwable t) {
			TmExceptions.reportExceptionMultiLine(t, msgPrefix);
			configAsFake();
		}

		
		if(forward.getEnt().cNamedMod.equals(RoNamedModulesE.PCM0) && false ) { // ! Tm17Opts.isPcm0Installed()) {
			String msg = thisClassName + " " + forward.name() + "/" + reverse.name() + ": options/preferences say PCM0 not installed.";
			P.println(msg);
			configAsFake();
		}
		
		if (isFake()) {
			P.println(thisClassName + " " + forward.name() + "/" + reverse.name() 
					+ " will be a FAKE DoubleSolenoid");
		} else {
			try {
				int allSols = m_realObj.getAll();
				DoubleSolenoid.Value seed = m_realObj.get();
				//gets ERROR 1 CTRE CAN Receive Timeout error, but no exceptions;
			} catch(Throwable t) {
				TmExceptions.reportExceptionMultiLine(t, msgPrefix);
				configAsFake();
			}
		}
		
		if (isFake()) {
			P.println(thisClassName + " " + forward.name() + "/" + reverse.name() 
					+ " will be a FAKE DoubleSolenoid");
		}
	}

	public DoubleSolenoid.Value get() {
		DoubleSolenoid.Value ans;
		if(isFake()) { ans = m_fakeParms.get(); }
		else { 
			ans = m_realObj.get();
			ans = m_realObj.get();
		}
		return ans;
	}
	
	public void set(DoubleSolenoid.Value value) {
		m_fakeParms.set(value); //an easy way to debug
		if(isReal()) { m_realObj.set(value); }
	}

	@Override
	public boolean isFakeableItem() {
		return true;
	}

	@Override
	public void configAsFake() {
		//m_realObj = null; //keep for debug purposes?
		m_beingFaked = true;
	}

}
