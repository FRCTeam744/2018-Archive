package org.usfirst.frc.tm744yr18.robot.devices;

import java.util.ArrayList;
import java.util.List;

import org.usfirst.frc.tm744yr18.robot.config.TmHdwrRoCntls.RoNamedControlsE;
import org.usfirst.frc.tm744yr18.robot.config.TmHdwrRoPhys.RoNamedConnectionsE;
import org.usfirst.frc.tm744yr18.robot.config.TmHdwrRoPhysBase.RoConnectionTypesE;
import org.usfirst.frc.tm744yr18.robot.exceptions.TmExceptions;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmRoControlUserI;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmToolsI.P;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmToolsI.Tt;

import edu.wpi.first.wpilibj.MotorSafety;
import edu.wpi.first.wpilibj.MotorSafetyHelper;
import edu.wpi.first.wpilibj.Relay;
import edu.wpi.first.wpilibj.SensorBase;
import edu.wpi.first.wpilibj.smartdashboard.SendableBuilder;
import edu.wpi.first.wpilibj.Relay.Value;
//import edu.wpi.first.wpilibj.livewindow.LiveWindowSendable;
//import edu.wpi.first.wpilibj.tables.ITable;
//import edu.wpi.first.wpilibj.tables.ITableListener;

public class TmFakeable_Relay extends SensorBase implements MotorSafety { //, LiveWindowSendable {

	private Relay m_relay = null;
	
	RoNamedControlsE m_namedCntl = null;
	
	//save - useful for debug, etc.
	private Relay.Direction m_direction;
	private int m_channel;
	private int m_channelRaw; //channel number as received via a constructor
	private String m_relayName = "";
	
	private boolean m_beingFaked = false;
	private void setupAsFake() {
		m_relay = null;
		m_beingFaked = true;
	}
	
	public boolean isFake() { return m_beingFaked; }
	public boolean isReal() { return ! m_beingFaked; }
	public String getRelayName() { return m_relayName; }
	
	private FakeParms m_fakeParms;
	public class FakeParms {
		private MotorSafetyHelper c_safetyHelper;
		private long c_port;
		
		private boolean c_safetyEnabled;
		
		private Relay.Value c_relaySetting;
		
		private String c_smartDashboardType;
		
//		private ITable c_table;
//		private ITableListener c_table_listener;
		
		public Relay.Value get() {return c_relaySetting; }
		public void set(Relay.Value val) { c_relaySetting = val; }
	}
	
	public static class TrackInstances {
		private static final TrackInstances tiInstance = new TrackInstances();
		public static TrackInstances getInstance() { return tiInstance; }
		private TrackInstances() {}
		
		static List<TrackInstEntry> tiDevInstanceList = new ArrayList<>();
		
		public class TrackInstEntry<CU extends TmRoControlUserI> {
			public CU fctCntlUser;
			public TmFakeable_Relay fctObj;
			public RoNamedControlsE fctNamedCntl;
			public TrackInstEntry(CU cntlUser, TmFakeable_Relay fct, RoNamedControlsE namedCntl) {
				fctCntlUser = cntlUser;
				fctObj = fct;
				fctNamedCntl = namedCntl;
			}
		}
		
		public static List<TrackInstEntry> getList() { return tiDevInstanceList; }
	
		//call from constructor: TrackInstances.trackUserAndDevInstances(cntlUser, this, namedCntl);
		public static <CU extends TmRoControlUserI> void trackUserAndDevInstances(CU cntlUser, 
														TmFakeable_Relay devInst, RoNamedControlsE namedCntl) {
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
		 * helpful when tracking followers?
		 * @param namedCntlToFind
		 * @return
		 */
		public static TmFakeable_Relay getInstanceFromNamedControl(RoNamedControlsE namedCntlToFind) {
			TmFakeable_Relay ans = null;
			for(TrackInstEntry t : tiDevInstanceList) {
				if(t.fctNamedCntl.equals(namedCntlToFind)) {
					ans = t.fctObj;
					break; //end the loop
				}
			}
			return ans;
		}
	}
	
	private static final Relay.Direction DEFAULT_RELAY_DIRECTION = Relay.Direction.kBoth;
	
	public <CU extends TmRoControlUserI> TmFakeable_Relay(CU cntlUser, RoNamedControlsE namedCntl) {
		this(cntlUser, namedCntl, DEFAULT_RELAY_DIRECTION);
	}
	
	public <CU extends TmRoControlUserI> TmFakeable_Relay(CU cntlUser, RoNamedControlsE namedCntl, Relay.Direction direction) {
		TrackInstances.trackUserAndDevInstances(cntlUser, this, namedCntl);

		String thisClassName = Tt.getClassName(this);
		
		RoNamedConnectionsE namedConnDef = namedCntl.getEnt().cNamedConn;
		m_namedCntl = namedCntl;
		int channel = namedCntl.getEnt().cNamedConn.getEnt().getConnectionFrcIndex();
		
		String defaultRelayName = "relay(ch="+channel+",dir="+direction+")";
		m_channel = channel;
		m_direction = direction;
		m_fakeParms = new FakeParms();
		m_fakeParms.c_smartDashboardType = thisClassName;
		
		m_relayName = namedCntl.name();
		if( ! namedCntl.getEnt().isValid()) {
			m_relayName += " invalid.";
		}
		if( ! namedConnDef.getEnt().getConnectionType().equals(RoConnectionTypesE.R_RELAY)) {
			m_relayName += " not a relay.";
		}
		if(namedCntl.getEnt().isValid() && namedConnDef.getEnt().getConnectionType().equals(RoConnectionTypesE.R_RELAY)) {
			m_relayName += " [relay(ch="+channel+",dir="+direction+")]";
		} else {
			String msg = namedCntl.name() + " is invalid or is not a relay. may cause errors.";
			P.println(msg);
		}
		
		String relayName = namedCntl.name();
		if(direction == null) {
//			System.out.println(Tm16DbgTk.extractClassName(this) + "(" + channel + ",dir) has invalid dir (null)");
			System.out.println(thisClassName + " " + relayName + " has invalid dir (null)");
//			throw new NullPointerException("Null Direction was given");
			m_channel = 998;			
			setupAsFake();
		} else {			
			String errMsgPrefix = thisClassName + " " + relayName + " got an exception:";
			try { 
				m_relay = new Relay(channel, direction);
				Value relayValue = m_relay.get();
				String thing = relayValue.toString();
			}
			catch(UnsatisfiedLinkError t) { 
				//this is the exception we get running in simulation project
				TmExceptions.reportExceptionOneLine(t, errMsgPrefix);
				setupAsFake();
			}
			catch(Throwable t) { 
				TmExceptions.reportExceptionMultiLine(t, errMsgPrefix);
				setupAsFake();
			}
		}
		if(isFake()) { System.out.println(thisClassName + " " + relayName + " will be a FAKE Relay"); }
	}
	
	public Relay.Value get() {
		if(isReal()) { return m_relay.get(); }
		else { return m_fakeParms.get(); }
	}
	public void set(Relay.Value val) {
		if(isReal()) { m_relay.set(val); }
		else { m_fakeParms.set(val); }
	}

//	@Override
//	public void initTable(ITable subtable) {
////		if(isFake()) { m_fakeRelay.initTable(subtable); }
//		if(isReal()) { m_relay.initTable(subtable); }
//		else {
//		    m_fakeParms.c_table = subtable;
//		    updateTable();
//			
//		}
//	}
//
//	@Override
//	public ITable getTable() {
//		if(isReal()) { return m_relay.getTable(); }
//		else { return m_fakeParms.c_table; }
//	}
//
//	@Override
//	public String getSmartDashboardType() {
//		if(isReal()) { return m_relay.getSmartDashboardType(); }
//		else { return m_fakeParms.c_smartDashboardType; }
//	}
//
//	@Override
//	public void updateTable() {
//		if(isReal()) { m_relay.updateTable(); }
//		else {
//			if (m_fakeParms.c_table != null) {
//				if (m_fakeParms.get() == Value.kOn) {
//					m_fakeParms.c_table.putString("ValueE", "On");
//				} else if (m_fakeParms.get() == Value.kForward) {
//					m_fakeParms.c_table.putString("ValueE", "Forward");
//				} else if (m_fakeParms.get() == Value.kReverse) {
//					m_fakeParms.c_table.putString("ValueE", "Reverse");
//				} else {
//					m_fakeParms.c_table.putString("ValueE", "Off");
//				}
//			}
//		}
//	}
//
//	@Override
//	public void startLiveWindowMode() {
//		if(isReal()) { m_relay.startLiveWindowMode(); }
//		else {
//			m_fakeParms.c_table_listener = new ITableListener() {
//				@Override
//				public void valueChanged(ITable itable, String key, Object value, boolean bln) {
//					String val = ((String) value);
//					if (val.equals("Off")) {
//						m_fakeParms.set(Value.kOff);
//					} else if (val.equals("On")) {
//						m_fakeParms.set(Value.kOn);
//					} else if (val.equals("Forward")) {
//						m_fakeParms.set(Value.kForward);
//					} else if (val.equals("Reverse")) {
//						m_fakeParms.set(Value.kReverse);
//					}
//				}
//			};
//			m_fakeParms.c_table.addTableListener("ValueE", m_fakeParms.c_table_listener, true);
//		}
//	}
//
//	@Override
//	public void stopLiveWindowMode() {
//		if(isReal()) { m_relay.stopLiveWindowMode(); }
//		else {
//		    // TODO: Broken, should only remove the listener from "ValueE" only.
//			m_fakeParms.c_table.removeTableListener(m_fakeParms.c_table_listener);
//		}
//	}

	@Override
	public void setExpiration(double timeout) {
		if(isReal()) { m_relay.setExpiration(timeout); }
		else {
		}
	}

	@Override
	public double getExpiration() {
		if(isReal()) { return m_relay.getExpiration(); }
		else { 
			return 0;
		}
	}

	@Override
	public boolean isAlive() {
		if(isReal()) { return m_relay.isAlive(); }
		else {
			return false;
		}
	}

	@Override
	public void stopMotor() {
		if(isReal()) { m_relay.stopMotor(); }
		else {
		}
	}

	@Override
	public void setSafetyEnabled(boolean enabled) {
		if(isReal()) { m_relay.setSafetyEnabled(enabled); }
		else {
			m_fakeParms.c_safetyEnabled = enabled;
		}
	}

	@Override
	public boolean isSafetyEnabled() {
		if(isReal()) { return m_relay.isSafetyEnabled(); }
		else {
			return m_fakeParms.c_safetyEnabled;
		}
	}

	@Override
	public String getDescription() {
		String ans;
		if(isReal()) { ans = m_relay.getDescription(); }
		else {
			ans = m_relayName;
		}
		if(ans==null) {
			ans = m_namedCntl.name();
		}
		return ans;
	}

	@Override //from Sendable interface
	public void initSendable(SendableBuilder builder) {
		// TODO Auto-generated method stub
		
	}
}
