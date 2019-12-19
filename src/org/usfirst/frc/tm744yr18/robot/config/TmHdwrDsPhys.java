package org.usfirst.frc.tm744yr18.robot.config;

import java.util.ArrayList;
import java.util.List;

import org.usfirst.frc.tm744yr18.robot.config.TmHdwrDsCntls.DsNamedControlsE;
import org.usfirst.frc.tm744yr18.robot.config.TmHdwrDsCntls.DsNamedDevicesE;
import org.usfirst.frc.tm744yr18.robot.exceptions.TmExceptions;
import org.usfirst.frc.tm744yr18.robot.helpers.TmHdwrItemEnableDisableMgr;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmDsControlUserI;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmForcedInstantiateI;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmItemAvailabilityI;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmListBackingEnumI;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmToStringI;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmToolsI;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmToolsI.P.PrtYn;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.XboxController;
import edu.wpi.first.wpilibj.buttons.JoystickButton;
import edu.wpi.first.wpilibj.command.Command;

public class TmHdwrDsPhys extends TmHdwrDsPhysBase implements TmToolsI, TmForcedInstantiateI {

	/*---------------------------------------------------------
	 * getInstance stuff                                      
	 *---------------------------------------------------------*/
	/** 
	 * handle making the singleton instance of this class and giving
	 * others access to it
	 */
	private static TmHdwrDsPhys m_instance;

	public static synchronized TmHdwrDsPhys getInstance() {
		if (m_instance == null) {
			m_instance = new TmHdwrDsPhys();
		}
		return m_instance;
	}

	protected TmHdwrDsPhys() {
		if (!(m_instance == null)) {
			P.println("Error!!! TmHdwrDsPhys.m_instance is being modified!!");
			P.println("         was: " + m_instance.toString());
			P.println("         now: " + this.toString());
		}
		m_instance = this;
	}
	/*----------------end of getInstance stuff----------------*/
	
	private static TmExceptions m_exceptions = TmExceptions.getInstance();
	private static DriverStation m_ds = DriverStation.getInstance();

	protected static List<DsNamedDevicesEntry> dsNamedDevicesList = new ArrayList<DsNamedDevicesEntry>();
	
	public class DsNamedDevicesEntry implements TmToStringI, TmForcedInstantiateI, TmItemAvailabilityI, 
												TmListBackingEnumI<DsNamedDevicesEntry, DsNamedDevicesE> {
		
		@Override
		public List<DsNamedDevicesEntry> getListBackingEnum() { return dsNamedDevicesList; }
		
		//c prefix for parms needed primarily for bookkeeping
		//d prefix for parms needed primarily to use devices
		
		public ItemAvailabilityE cDevAvail;
		public ItemFakeableE cDevFakeable;
		public DsNamedDevicesE cNamedDev;
		public DsNamedDevicesE cDupOf;
		public DsDeviceTypesE cDevType;
		public DsNamedConnectionsE cNamedConn;
		
		//these aren't really deprecated, but use the getters for them to do better type checking
		@Deprecated
		private Joystick dJoystickObj;
		@Deprecated
		private XboxController dXboxObj;
		
		protected boolean cIsValid;	
		public boolean isValid() { return cIsValid; }
		
		private void logValidity(boolean validity) {
			cIsValid = validity;
			if( ! validity) {
				throw m_exceptions.new InvalidDsDevicesEntryEx(this.toStringLog());
			}
		}
		
		String frmtStr = "ndx=%3d, %-21s [%-14s, %-17s, %-13s, %-26s, valid=%b]";
		String frmtHdr = "......., %-21s [%-14s, %-17s, %-13s, %-26s, ..........]";
		public String toStringLog(String inpPrefix) {
			String prefix = (inpPrefix==null) ? "" : inpPrefix;
			String ans = String.format(prefix + frmtStr, 
					((cNamedDev==null) ? -1 : cNamedDev.ordinal()), Tt.getName(cNamedDev),
					Tt.getName(cDevAvail), Tt.getName(cDevFakeable), Tt.getName(cDevType), Tt.getName(cNamedConn), cIsValid);
			return ans;
		}
		@Override
		public String toStringHdr(String inpPrefix) {
			String prefix = (inpPrefix==null) ? "" : inpPrefix;
			return String.format(prefix + frmtHdr, Tt.getClassName(cNamedDev),
					Tt.getClassName(cDevAvail), Tt.getClassName(cDevFakeable), Tt.getClassName(cDevType), Tt.getClassName(cNamedConn));
		}
		@Override
		public String toStringNotes() {
			return "enum " + cNamedDev.getClass().getSimpleName() + " initializes List<" + 
							this.getClass().getSimpleName() + "> dsNamedDevicesList, see enum's getList() and getEnt() methods too";
		}

//		public Object cEnableDisableLock;
//		private boolean cIsEnabled = true;
//		public void disable() { synchronized(cEnableDisableLock) { cIsEnabled = false; } }; //call this if get exceptions (joystick not plugged in, etc.)
//		public void enable() { synchronized(cEnableDisableLock) { cIsEnabled = true; } }; //call from a command or something to re-enable a disabled entity
//		public boolean isEnabled() { synchronized(cEnableDisableLock) { return cIsEnabled; } }
		private TmHdwrItemEnableDisableMgr cHdwrItemEnableDisableMgr = new TmHdwrItemEnableDisableMgr();

		public TmHdwrItemEnableDisableMgr getHdwrItemEnableDisableMgr() { return cHdwrItemEnableDisableMgr; }

		Joystick cJoystickObj = null;
		XboxController cXboxObj = null;	
		
		public DsNamedDevicesEntry(DsNamedDevicesE namedDev, ItemAvailabilityE devAvail, ItemFakeableE devFakeable, DsDeviceTypesE devType, 
											DsNamedConnectionsE namedConn, DsNamedDevicesE dupOf) {
			cDevAvail = devAvail;
			cDevFakeable = devFakeable;
			cNamedDev = namedDev;
			cDupOf = dupOf;
			cDevType = devType;
			cNamedConn = namedConn;
			
			cJoystickObj = null;
			cXboxObj = null;
			
			boolean valid = false;
			if( ! (cNamedDev==null || cDevType==null || cNamedConn==null)) {
				DsConnectionsEntry connEnt = cNamedConn.getEnt(); //getDsNamedConnectionsList().get(cNamedConn.ordinal());
				if(connEnt.isValid()) {
					if(connEnt.getConnectionDeviceType().equals(DsDeviceTypesE.COMPUTER) && 
							connEnt.getConnectionType().equals(DsConnectionTypesE.USB_PORT)) {
						//at present (1/24/18) we only support devices connected
						//to the USB ports on the driver station computer
						valid = true;
					} 
					else {
						P.println(PrtYn.Y, "connEnt " + connEnt.getConnectionType().name() + " not USB_PORT or " 
								+ connEnt.getConnectionDeviceType().name() + " not COMPUTER\n" +
								"connEnt: " + connEnt.toStringLog());
					}
				}
				else {
					P.println(PrtYn.Y, connEnt.getNamedConnection().name() + " connection entry invalid");
				}
				
			}
			logValidity(valid);
		}

		@Override
		public void doForcedInstantiation() {}

		@Override
		public void doPopulate() {
			if( ! this.isValid()) { throw m_exceptions.new PopulatingInvalidDsDevicesEntryEx(this.cNamedDev.name()); }
 
			switch(cDevType) {
			case COMPUTER:
				break;
			case JOYSTICK_DEV:
				dJoystickObj = new Joystick(cNamedConn.getEnt().getConnectionFrcIndex());
				break;
			case XBOX_CNTLR_DEV:
				dXboxObj = new XboxController(cNamedConn.getEnt().getConnectionFrcIndex());
				break;
			default:
				break;
			
			}			
		}
		
		public Joystick getJoystickObj() { 
			if( ! this.cDevType.equals(DsDeviceTypesE.JOYSTICK_DEV)) {
				throw m_exceptions.new WrongTypeObjectForCastEx("shouldn't be requesting a Joystick object!!");
			}
			return dJoystickObj;
		}
		
		public XboxController getXboxObj() { 
			if( ! this.cDevType.equals(DsDeviceTypesE.XBOX_CNTLR_DEV)) {
				throw m_exceptions.new WrongTypeObjectForCastEx("shouldn't be requesting a XboxController object!!");
			}
			return dXboxObj;
		}

		@Override
		public boolean isFakeableItem() { return cDevFakeable.isFakeable(); }

		@Override
		public void configAsFake() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public boolean isFake() {
			// TODO Auto-generated method stub
			return false;
		}

		
//		public GenericHID getHidObj() {
////			GenericHID obj = null;
//			switch(this.cDevType) {
//			case JOYSTICK_DEV:
//				return this.dJoystickObj;
//				//break;
//			case XBOX_CNTLR_DEV:
//				return this.dXboxObj;
//				//break;
//			default:
//				throw m_exceptions.new ReachedCodeThatShouldNeverExecuteEx("should never get here!!");
//				//break;			
//			}
////			return obj;
//		}
		
	}
	
	protected static List<DsNamedControlsEntry> dsNamedControlsList = new ArrayList<DsNamedControlsEntry>();
	
	public class DsNamedControlsEntry implements TmToStringI, TmForcedInstantiateI, TmItemAvailabilityI,
												TmListBackingEnumI<DsNamedControlsEntry, DsNamedControlsE> {
		
		@Override
		public List<DsNamedControlsEntry> getListBackingEnum() { return dsNamedControlsList; }

		public DsNamedControlsE cNamedCntl = null;
		public ItemAvailabilityE cCntlAvail = null; 
		public ItemFakeableE cCntlFakeable = null;
		public DsNamedControlsE cDupOf = null;
		public DsControlTypesE cCntlType = null;
		public DsNamedDevicesE cNamedDev = null; 
		public DsNamedConnectionsE cNamedConn = null; 
		protected double cJsDeadzoneTol = DsCnst.DEFAULT_JOYSTICK_DEADZONE_TOLERANCE;
		
		protected List<TmDsControlUserI> registeredUsersList = new ArrayList<>();
		
		protected boolean cIsValid;	
		public boolean isValid() { return cIsValid; }
		
		private void logValidity(boolean validity) {
			cIsValid = validity;
			if( ! validity) {
				throw m_exceptions.new InvalidDsControlsEntryEx(this.toStringLog());
			}
		}
		
		String frmtStr = "ndx=%3d, %-25s [%-19s, %-17s, %-13s, %-21s, %-26s, valid=%b]";
		String frmtHdr = "......., %-25s [%-19s, %-17s, %-13s, %-21s, %-26s, ..........]";
		public String toStringLog(String inpPrefix) {
			String prefix = (inpPrefix==null) ? "" : inpPrefix;
			String ans = String.format(prefix + frmtStr, 
					((cNamedCntl==null) ? -1 : cNamedCntl.ordinal()), Tt.getName(cNamedCntl),
					Tt.getName(cCntlAvail), Tt.getName(cCntlFakeable), Tt.getName(cCntlType), Tt.getName(cNamedDev),
					Tt.getName(cNamedConn), cIsValid);
			return ans;
		}
		@Override
		public String toStringHdr(String inpPrefix) {
			String prefix = (inpPrefix==null) ? "" : inpPrefix;
			String ans = String.format(prefix + frmtHdr, 
					Tt.getClassName(cNamedCntl),Tt.getClassName(cCntlAvail), Tt.getClassName(cCntlFakeable), Tt.getClassName(cCntlType), 
					Tt.getClassName(cNamedDev), Tt.getClassName(cNamedConn));
			return ans;
		}
		@Override
		public String toStringNotes() {
			return "enum " + cNamedCntl.getClass().getSimpleName() + " initializes List<" + 
							this.getClass().getSimpleName() + "> dsNamedControlsList, see enum's getList() and getEnt() methods too";
		}
		
//		public Object cEnableDisableLock;
//		private boolean cIsEnabled = true;
//		public void disable() { synchronized(cEnableDisableLock) { cIsEnabled = false; } }; //call this if get exceptions (joystick not plugged in, etc.)
//		public void enable() { synchronized(cEnableDisableLock) { cIsEnabled = true; } }; //call from a command or something to re-enable a disabled entity
//		public boolean isEnabled() { synchronized(cEnableDisableLock) { return cIsEnabled; } }
		private TmHdwrItemEnableDisableMgr cHdwrItemEnableDisableMgr = new TmHdwrItemEnableDisableMgr();

		public TmHdwrItemEnableDisableMgr getHdwrItemEnableDisableMgr() { return cHdwrItemEnableDisableMgr; }

		protected DsNamedControlsEntry(DsNamedControlsE namedCntl, ItemAvailabilityE cntlAvail, ItemFakeableE cntlFakeable, 
				DsControlTypesE cntlType, DsNamedDevicesE namedDev, 
				DsNamedConnectionsE namedConn, DsNamedControlsE dupOf, double jsDeadzoneTol) {
			cCntlAvail = cntlAvail; 
			cCntlFakeable = cntlFakeable;
			cNamedCntl = namedCntl;
			cDupOf = dupOf;
			cCntlType = cntlType;
			cNamedDev = namedDev;
			cNamedConn = namedConn;
			cJsDeadzoneTol = jsDeadzoneTol;
			
			boolean valid = false;
			if( ! (cCntlAvail==null || cCntlFakeable==null || cNamedCntl==null || cCntlType==null || cNamedDev==null || cNamedConn==null) ) {
				DsNamedDevicesEntry devEnt = cNamedDev.getEnt();
				DsConnectionsEntry connEnt = cNamedConn.getEnt();
				if(false) {
					P.printFrmt("cNamedCntl %s, cCntlType %s, cNamedDev %s, cNamedConn %s", 
								Tt.getName(cNamedCntl), Tt.getName(cCntlType), Tt.getName(cNamedDev), Tt.getName(cNamedConn));
					P.println("devEnt: " + devEnt.toStringLog());
					P.println("connEnt: " + connEnt.toStringLog());
					P.printFrmt("devEnt.cDevType: %s, connEnt.connDevType: %s, connType: %s", 
								devEnt.cDevType.name(), connEnt.getConnectionDeviceType().name(), 
								connEnt.getConnectionType().name());
				}
				if(devEnt.isValid() && connEnt.isValid()) {
					if(devEnt.cDevType.equals(connEnt.getConnectionDeviceType())) { //joystick vs xbox vs computer
						DsConnectionTypesE connType = connEnt.getConnectionType();
						boolean validSoFar = true;
						switch(cCntlType) {
						case kAnalog:
							switch(connType) {
								case JS_FRC_ANALOG:
								case XBOX_FRC_ANALOG:
									break;
								default:
									validSoFar = false;
									break;
							}
							break;
						case kButton:
							switch(connType) {
								case JS_FRC_BUTTON:
								case XBOX_FRC_BUTTON:
									break;
								default:
									validSoFar = false;
									break;
							}
							break;
						case kCannedAnalogButton:
							switch(connType) {
							case JS_TM_CANNED_ANALOG_BUTTON:
								break;
							default:
								validSoFar = false;
								break;
							}
						break;
						case kCannedPovButton:
							switch(connType) {
							case XBOX_TM_CANNED_POV_BUTTON:
								break;
							default:
								validSoFar = false;
								break;
							}
						break;
						case kPov:
							switch(connType) {
							case XBOX_FRC_POV:
								break;
							default:
								validSoFar = false;
								break;
							}
						break;
						default:
							validSoFar = false;
							break;						
						}
						if(validSoFar) {
							valid = true;
						}
					}
				}
			}
			logValidity(valid);
		}
		
//		/**
//		 * the simulation code keeps an array of info to use to simulate driver station
//		 * controls.  These getSimulation.... methods convert info from the control into
//		 * a suitable index into the array.  See DriverStation_SimI.java in one of the
//		 * simulation projects for details.
//		 * @return
//		 */
//		public int getSimulationPortNbr() {
//			return this.cNamedDev.getEnt().cNamedConn.getEnt().c_frcIndex;
//		}
//		public int getSimulationButtonNbr() {
//			int ans = -1; 
//			switch(cCntlType) {
//			case kButton:
//				ans = cNamedConn.getEnt().c_frcIndex;				
//				switch(cNamedDev.getEnt().cDevType) {
//				case JOYSTICK_DEV:
//					ans += DsNamedConnectionsE.TOP_BUT1_TRIGGER.ordinal(); //the first joystick button in the enum				
//					break;
//				case XBOX_CNTLR_DEV:
//					ans += DsNamedConnectionsE.A_BUTTON.ordinal(); //the first XBOX button in the enum				
//					break;
//				case COMPUTER:
//				default:
//					throw TmExceptions.getInstance().new Team744RunTimeEx("simulation asking for button nbr on device that has no buttons");
//					//break;				
//				}
//				break;
//			case kCannedAnalogButton:
//				ans = cNamedConn.getEnt().c_frcIndex;
//				ans += DsNamedConnectionsE.JOY_Z_UP_AS_BUTTON.ordinal(); //the first analog-as-button entry in the enum
//				break;
//			case kCannedPovButton:
//				ans = cNamedConn.getEnt().c_frcIndex;
//				ans += DsNamedConnectionsE.POV_NORTH_BUTTON.ordinal(); //the first pov-as-button entry in the enum
//				break;
//			case kAnalog:
//			case kPov:
//			default:
//				throw TmExceptions.getInstance().new Team744RunTimeEx("simulation asking for button nbr for something not a button");
//				//break;			
//			}
//			return ans;
//		}
		
		public double getAnalog() { return DsControlsMgr.getAnalog(this); }
		public int getPov() { return DsControlsMgr.getPov(this); }
		public boolean getButton() { return DsControlsMgr.getButton(this); }
		public boolean getButtonPressed() { return DsControlsMgr.getButtonPressed(this); }
		public boolean getButtonReleased() { return DsControlsMgr.getButtonReleased(this); }

		@Override
		public void doForcedInstantiation() {}

		@Override
		public void doPopulate() {
			
			
		}

		@Override
		public boolean isFakeableItem() { return cCntlFakeable.isFakeable(); }

		@Override
		public void configAsFake() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public boolean isFake() {
			// TODO Auto-generated method stub
			return false;
		}

		class ButtonInfo { //<CU extends TmDsControlUserI> {
			public ButtonInfo(TmDsControlUserI namedCntlUser, String cntlUserNameArg) {
				cntlUserObj = namedCntlUser;
				cntlUserName = cntlUserNameArg;
				cntlEnt = cNamedCntl.getEnt();
				devEnt = cntlEnt.cNamedDev.getEnt();
				connEnt = cntlEnt.cNamedConn.getEnt();
				btnDeviceType = devEnt.cDevType;
				if(devEnt.getHdwrItemEnableDisableMgr().isEnabled()) {
					try {
						switch(devEnt.cDevType) {
						case JOYSTICK_DEV:
							button = new JoystickButton(devEnt.getJoystickObj(), connEnt.getConnectionFrcIndex());
							break;
						case XBOX_CNTLR_DEV:
							button = new JoystickButton(devEnt.getXboxObj(), connEnt.getConnectionFrcIndex());
							break;
						default:
							break;
						}
					} catch(RuntimeException e) {
						TmExceptions.reportExceptionOneLine(e,
								cntlEnt.cNamedCntl.name() + ":" + devEnt.cNamedDev.name() +
								" on " + devEnt.cNamedConn.name() + " [drvStaAttached=" + m_ds.isDSAttached());
						if(m_ds.isDSAttached()) {
							devEnt.getHdwrItemEnableDisableMgr().disableAfterMsgs();
						}
					}
				} else {
					P.println("can't assign a command to a button when the joystick/xbox isn't connected to the computer!! -- " + cNamedCntl.name());
				}
				
			}
			DsNamedControlsEntry cntlEnt = cNamedCntl.getEnt();
			DsNamedDevicesEntry devEnt = cntlEnt.cNamedDev.getEnt();
			DsConnectionsEntry connEnt = cntlEnt.cNamedConn.getEnt();
			TmDsControlUserI cntlUserObj = null;
			String cntlUserName = null;
			DsDeviceTypesE btnDeviceType = null;
			JoystickButton button = null;
			Command cmdWhenPressed = null; //not needed?
			
			protected void setWhenPressedCommand(Command cmd) { 
				cmdWhenPressed = cmd;
				switch(cNamedConn.getEnt().c_connType) {
				case JS_FRC_BUTTON:
				case XBOX_FRC_BUTTON:
					button.whenPressed(cmd);
					break;
				case JS_TM_CANNED_ANALOG_BUTTON:
				case XBOX_TM_CANNED_POV_BUTTON:
					DsControlsMgr.getNonDigitalButtonList().add(this);
					break;
				default:
					break;
				}
			}
			
		}
		protected ButtonInfo cBtnInfo = null;
		public synchronized <CU extends TmDsControlUserI> void whenPressed(CU namedCntlUser, Command cmdToRun) {
			String cntlUserName = namedCntlUser.getClass().getSimpleName();
			registerAsDsCntlUser(namedCntlUser);
			switch(cNamedConn.getEnt().c_connType) {
			case JS_FRC_BUTTON:
			case XBOX_FRC_BUTTON:
			case JS_TM_CANNED_ANALOG_BUTTON:
			case XBOX_TM_CANNED_POV_BUTTON:
				if(cBtnInfo==null) {
					cBtnInfo = new ButtonInfo(namedCntlUser, cntlUserName);
					cBtnInfo.setWhenPressedCommand(cmdToRun);
				}
				else if(namedCntlUser.equals(cBtnInfo.cntlUserObj)) {
					cBtnInfo.setWhenPressedCommand(cmdToRun);
				}
				else {
					throw TmExceptions.getInstance().new Team744RunTimeEx("DsNamedControlE."+cNamedCntl.name() +
							" already used by " + cBtnInfo.cntlUserObj.toString());
				}
				break;
			case XBOX_FRC_POV:
			case JS_FRC_ANALOG:
			case XBOX_FRC_ANALOG:
			case USB_PORT:
			default:
				throw TmExceptions.getInstance().new Team744RunTimeEx("DsNamedControlE."+cNamedCntl.name() +
						" is not a Button type - can't do whenPressed from " + cBtnInfo.cntlUserObj.toString() + "!!!");
				//break;			
			}
		}
		
//		Button cDsButton = null;
//	?	private Button makeButton() {
//			// TODO Auto-generated method stub
//			return null;
//		}
		
		public <CU extends TmDsControlUserI> void registerAsDsCntlUser(CU user) {
//			protected List<TmDsControlUserI> registeredUsersList = new ArrayList<>();
			boolean found = false;
			String userClassName = Tt.getClassName(user);
			for(TmDsControlUserI u : registeredUsersList) {
				if(Tt.getClassName(u).equals(userClassName)) { found = true; break; }
			}
			if( ! found) {
				registeredUsersList.add(user);
			}
		}
		public String getRegisteredUsers() {
			String ans = "";
			for(TmDsControlUserI u : registeredUsersList) {
				ans += (ans.equals("") ? "" : ", ") + Tt.getClassName(u);
			}
			return ans;
		}

	}

	public static class DsDevicesMgr implements TmForcedInstantiateI {
	
		/*---------------------------------------------------------
		 * getInstance stuff                                      
		 *---------------------------------------------------------*/
		/** 
		 * handle making the singleton instance of this class and giving
		 * others access to it
		 */
		private static final DsDevicesMgr m_instance = new DsDevicesMgr();
		public static synchronized DsDevicesMgr getInstance()
		{
//			if(m_instance == null){ m_instance = new DsDevicesMgr(); }
			return m_instance;
		}
		private DsDevicesMgr() {
			if ( ! (m_instance == null)) {
				P.println("Error!!! DsDevicesMgr constructor should only be called once!!");
				P.println("         orig: " + m_instance.toString());
				P.println("         this: " + this.toString());
			}
//			m_instance = this;
		}

		/*----------------end of getInstance stuff----------------*/

		
		@Override
		public void doForcedInstantiation() {}
		@Override
		public void doPopulate() {
			for(DsNamedDevicesE e : DsNamedDevicesE.values()) {
				e.getEnt().doPopulate();
			}
		}
		
	}
		

	protected static class DsControlsMgr implements TmForcedInstantiateI {

		/*---------------------------------------------------------
		 * getInstance stuff                                      
		 *---------------------------------------------------------*/
		/** 
		 * handle making the singleton instance of this class and giving
		 * others access to it
		 */
		private static final DsControlsMgr m_instance = new DsControlsMgr();
		public static synchronized DsControlsMgr getInstance()
		{
//			if(m_instance == null){ m_instance = new DsControlsMgr(); }
			return m_instance;
		}
		private DsControlsMgr() {
			if ( ! (m_instance == null)) {
				P.println("Error!!! DsControlsMgr should only be called once!!");
				P.println("         orig: " + m_instance.toString());
				P.println("         this: " + this.toString());
			}
//			m_instance = this;
		}

		/*----------------end of getInstance stuff----------------*/

		/**
		 * returns the analog value for the control, implementing a deadzone around 0.0
		 * @param cntlEnt
		 * @return
		 */
		public static double getAnalog(DsNamedControlsEntry cntlEnt) {
			final double STANDARD_DEFAULT_VALUE = 0.0;
			return getAnalog(cntlEnt, STANDARD_DEFAULT_VALUE);
		}
		public static double getAnalog(DsNamedControlsEntry cntlEnt, double defaultValue) {
			double ans = defaultValue;

			String methodName = "getAnalog()";
			DsNamedDevicesEntry devEnt;
			DsConnectionsEntry connEnt;
			
			inspectControl(cntlEnt, methodName, null); //throws exceptions if finds problems
			if(cntlEnt.cCntlType.equals(DsControlTypesE.kCannedAnalogButton)) {
				inspectControl(cntlEnt, methodName, DsControlTypesE.kCannedAnalogButton, DsControlTypesE.kAnalog); //throws exceptions if finds problems
			} else {
				inspectControl(cntlEnt, methodName, DsControlTypesE.kAnalog); //throws exceptions if finds problems
			}
			
			devEnt = cntlEnt.cNamedDev.getEnt();
			connEnt = cntlEnt.cNamedConn.getEnt();
//			if(cntlEnt.cNamedCntl.equals(DsNamedControlsE.ARM_STAGE1_MTR_INPUT)) {
//				int junk = 5; //debug breakpoint
//			}
			switch(devEnt.cDevType) {
			case JOYSTICK_DEV:
			case XBOX_CNTLR_DEV:
				if( (m_ds.getStickAxisCount(devEnt.cNamedConn.getEnt().getConnectionFrcIndex()) == 0) 
						&& (m_ds.getStickButtonCount(devEnt.cNamedConn.getEnt().getConnectionFrcIndex()) == 0)
						&& (m_ds.getStickPOVCount(devEnt.cNamedConn.getEnt().getConnectionFrcIndex()) == 0) )
				{
					//it appears that there's no joystick connected
					if(m_ds.isDSAttached()) {
						devEnt.getHdwrItemEnableDisableMgr().disableAfterMsgs();
					}
				}				
				if(devEnt.getHdwrItemEnableDisableMgr().isEnabled()) {
					try {
						int axisNdx;
						if(cntlEnt.cCntlType.equals(DsControlTypesE.kCannedAnalogButton)) {
							axisNdx = connEnt.c_analogBtnInfo.getAnalogAxisId();
						} else {
							axisNdx = connEnt.c_frcIndex;
						}
						if(m_ds.isDSAttached()) {
						switch(devEnt.cDevType) {
						case JOYSTICK_DEV:
							ans = devEnt.getJoystickObj().getRawAxis(axisNdx);
							break;
						case XBOX_CNTLR_DEV:
							ans = devEnt.getXboxObj().getRawAxis(axisNdx);
							break;
						default:
							break;
						}
						}
					} catch(RuntimeException e) {
						TmExceptions.reportExceptionMultiLine(e,
								cntlEnt.cNamedCntl.name() + ":" + devEnt.cNamedDev.name() + " on " + devEnt.cNamedConn.name());
						if(m_ds.isDSAttached()) {
							devEnt.getHdwrItemEnableDisableMgr().disableAfterMsgs();
						}
					}
				}
				break;
			case COMPUTER:
			default:
				throw m_exceptions.new ReachedCodeThatShouldNeverExecuteEx(methodName + " should never have gotten here!!!");
				//break;			
			}
			
			//if within certain range around 0.0, return 0.0
			if(Tt.isWithinTolerance(ans, 0.0, cntlEnt.cJsDeadzoneTol)) {
	    		ans = 0.0;
	    	}
						
			return ans;
		}

		
		/**
		 * returns the angle reported by the POV
		 * @param cntlEnt
		 * @return 0-359 degrees if POV pressed, -1 if not pressed
		 */
		public static int getPov(DsNamedControlsEntry cntlEnt) {
			int ans = FrcCnst.POV_ANGLE_POV_NOT_PRESSED;

			String methodName = "getPov()";
			DsNamedDevicesEntry devEnt;
			DsConnectionsEntry connEnt;
			
			inspectControl(cntlEnt, methodName, null); //throws exceptions if finds problems
			if(cntlEnt.cCntlType.equals(DsControlTypesE.kCannedPovButton)) {
				inspectControl(cntlEnt, methodName, DsControlTypesE.kCannedPovButton, DsControlTypesE.kPov); //throws exceptions if finds problems
			} else {
				inspectControl(cntlEnt, methodName, DsControlTypesE.kPov); //throws exceptions if finds problems
			}
			
			devEnt = cntlEnt.cNamedDev.getEnt();
			connEnt = cntlEnt.cNamedConn.getEnt();
			
			if( (m_ds.getStickAxisCount(devEnt.cNamedConn.getEnt().getConnectionFrcIndex()) == 0) 
					&& (m_ds.getStickButtonCount(devEnt.cNamedConn.getEnt().getConnectionFrcIndex()) == 0)
					&& (m_ds.getStickPOVCount(devEnt.cNamedConn.getEnt().getConnectionFrcIndex()) == 0) )
			{
				//it appears that there's no joystick connected
				if(m_ds.isDSAttached()) {
					devEnt.getHdwrItemEnableDisableMgr().disableAfterMsgs();
				}
			}				
			if(devEnt.getHdwrItemEnableDisableMgr().isEnabled()) {
				if(m_ds.isDSAttached()) {
				try {
					ans = devEnt.getXboxObj().getPOV(connEnt.c_povBtnInfo.eFrcPovNdx); //.getConnectionFrcIndex());
				} catch(RuntimeException e) {
					TmExceptions.reportExceptionOneLine(e,
							cntlEnt.cNamedCntl.name() + ":" + devEnt.cNamedDev.name() + " on " + devEnt.cNamedConn.name());
					if(m_ds.isDSAttached()) {
						devEnt.getHdwrItemEnableDisableMgr().disableAfterMsgs();
					}
				}
				}
			}
			return ans;
		}
		
		public static boolean getButton(DsNamedControlsEntry cntlEnt) {
			boolean ans = false;
			String methodName = "getButton()";
			DsNamedDevicesEntry devEnt;
			DsConnectionsEntry connEnt;
			DsConnectionsEntry devConnEnt; //USB port
			
			double analogAns = 0.0;
			int povAns = 0;
			
			inspectControl(cntlEnt, methodName, DsControlTypesE.kButton); //throws exceptions if finds problems
						
			devEnt = cntlEnt.cNamedDev.getEnt();
			connEnt = cntlEnt.cNamedConn.getEnt();
			devConnEnt = devEnt.cNamedConn.getEnt();
			
			switch(cntlEnt.cCntlType) {
			case kButton:
				switch(devEnt.cDevType) {
				case JOYSTICK_DEV:
				case XBOX_CNTLR_DEV:
					if( (m_ds.getStickAxisCount(devConnEnt.getConnectionFrcIndex()) == 0) 
							&& (m_ds.getStickButtonCount(devConnEnt.getConnectionFrcIndex()) == 0)
							&& (m_ds.getStickPOVCount(devConnEnt.getConnectionFrcIndex()) == 0) )
					{
						//it appears that there's no joystick connected
						if(m_ds.isDSAttached()) {
							devEnt.getHdwrItemEnableDisableMgr().disableAfterMsgs();
						}
					}
					if(cntlEnt.cNamedCntl.equals(DsNamedControlsE.ARM_LIFT_RUN_WITH_JOYSTICKS_BTN)) {
						int junk = 5; //debug breakpoint
					}
					if(devEnt.getHdwrItemEnableDisableMgr().isEnabled()) {
						try {
							if(m_ds.isDSAttached()) {
							switch(devEnt.cDevType) {
							case JOYSTICK_DEV:
								ans = devEnt.getJoystickObj().getRawButton(connEnt.getConnectionFrcIndex());
								break;
							case XBOX_CNTLR_DEV:
								ans = devEnt.getXboxObj().getRawButton(connEnt.getConnectionFrcIndex());
								break;
							default:
								break;
							}
							}
						} catch(RuntimeException e) {
							TmExceptions.reportExceptionOneLine(e,
									cntlEnt.cNamedCntl.name() + ":" + devEnt.cNamedDev.name() + " on " + devEnt.cNamedConn.name());
							if(m_ds.isDSAttached()) {
								devEnt.getHdwrItemEnableDisableMgr().disableAfterMsgs();
							}
						}
					}
					break;
				case COMPUTER:
				default:
					throw m_exceptions.new ReachedCodeThatShouldNeverExecuteEx("should never get here!!");
					//break;
				}
				break;
			case kCannedAnalogButton:
				analogAns = getAnalog(cntlEnt);
				AnalogRangeForBtnE info = connEnt.c_analogBtnInfo.getAnalogRangeForBtn();
				ans = Tt.isInRange(analogAns, info.eMin, info.eMax);
				break;
			case kCannedPovButton:
				povAns = getPov(cntlEnt);
				ans = Tt.isWithinTolerance(povAns, connEnt.c_povBtnInfo.ePovAngle, connEnt.c_povBtnInfo.ePovAngleTolerance);
				break;
			case kAnalog:
			case kPov:
				throw m_exceptions.new ReachedCodeThatShouldNeverExecuteEx(methodName + " should have thrown an exception before ever getting here!!");
			  //break;
			default:
				break;
			
			}
			
			
			return ans;
		}
		
		public static int tbdMsgCntAnalogBtnPressed = 25;
		public static int tbdMsgCntPovBtnPressed = 25;
		public static boolean getButtonPressed(DsNamedControlsEntry cntlEnt) {
			boolean ans = false;
			String methodName = "getButtonPressed()";
			DsNamedDevicesEntry devEnt;
			DsConnectionsEntry connEnt;
			
			double analogAns = 0.0;
			int povAns = 0;
			
			inspectControl(cntlEnt, methodName, DsControlTypesE.kButton); //throws exceptions if finds problems
						
			devEnt = cntlEnt.cNamedDev.getEnt();
			connEnt = cntlEnt.cNamedConn.getEnt();
			
//			GenericHID hidObj = devEnt.getHidObj();
			
			switch(cntlEnt.cCntlType) {
			case kButton:
				if( (m_ds.getStickAxisCount(devEnt.cNamedConn.getEnt().getConnectionFrcIndex()) == 0) 
						&& (m_ds.getStickButtonCount(devEnt.cNamedConn.getEnt().getConnectionFrcIndex()) == 0)
						&& (m_ds.getStickPOVCount(devEnt.cNamedConn.getEnt().getConnectionFrcIndex()) == 0) )
				{
					//it appears that there's no joystick connected
					if(m_ds.isDSAttached()) {
						devEnt.getHdwrItemEnableDisableMgr().disableAfterMsgs();
					}
				}				
				if(devEnt.getHdwrItemEnableDisableMgr().isEnabled()) {
					try {
						if(m_ds.isDSAttached()) {
						switch(devEnt.cDevType) {
						case JOYSTICK_DEV:
							ans = devEnt.getJoystickObj().getRawButtonPressed(connEnt.getConnectionFrcIndex());
							break;
						case XBOX_CNTLR_DEV:
							ans = devEnt.getXboxObj().getRawButtonPressed(connEnt.getConnectionFrcIndex());
							break;
						case COMPUTER:
						default:
							throw m_exceptions.new ReachedCodeThatShouldNeverExecuteEx("should never get here!!");
							//break;
						}
						}
					} catch(RuntimeException e) {
						TmExceptions.reportExceptionOneLine(e,
								cntlEnt.cNamedCntl.name() + ":" + devEnt.cNamedDev.name() + " on " + devEnt.cNamedConn.name());
						if(m_ds.isDSAttached()) {
							devEnt.getHdwrItemEnableDisableMgr().disableAfterMsgs();
						}
					}
				}
				break;
			case kCannedAnalogButton:
				if(tbdMsgCntAnalogBtnPressed > 0) { //TODO
					P.println(PrtYn.Y, "'Button Pressed' not yet implemented for kCannedAnalogButton, using getButton() instead");
					tbdMsgCntAnalogBtnPressed--;
				}
				ans = getButton(cntlEnt);
				break;
			case kCannedPovButton:
				if(tbdMsgCntPovBtnPressed > 0) { //TODO
					P.println(PrtYn.Y, "'Button Pressed' not yet implemented for kCannedPovButton, returning ( ! getButton()) instead");
					tbdMsgCntPovBtnPressed--;
				}
				ans = getButton(cntlEnt);
				break;
			case kAnalog:
			case kPov:
			default:
				throw m_exceptions.new ReachedCodeThatShouldNeverExecuteEx(methodName + " should have thrown an exception before ever getting here!!");
			  //break;			
			}
			
			
			return ans;
		}

		public static int tbdMsgCntAnalogBtnReleased = 25;
		public static int tbdMsgCntPovBtnReleased = 25;
		public static boolean getButtonReleased(DsNamedControlsEntry cntlEnt) {
			boolean ans = false;
			String methodName = "getButton()";
			DsNamedDevicesEntry devEnt;
			DsConnectionsEntry connEnt;
			
			double analogAns = 0.0;
			int povAns = 0;
			
			inspectControl(cntlEnt, methodName, DsControlTypesE.kButton); //throws exceptions if finds problems
						
			devEnt = cntlEnt.cNamedDev.getEnt();
			connEnt = cntlEnt.cNamedConn.getEnt();
			
			switch(cntlEnt.cCntlType) {
			case kButton:
				if( (m_ds.getStickAxisCount(devEnt.cNamedConn.getEnt().getConnectionFrcIndex()) == 0) 
						&& (m_ds.getStickButtonCount(devEnt.cNamedConn.getEnt().getConnectionFrcIndex()) == 0)
						&& (m_ds.getStickPOVCount(devEnt.cNamedConn.getEnt().getConnectionFrcIndex()) == 0) )
				{
					//it appears that there's no joystick connected
					if(m_ds.isDSAttached()) {
						devEnt.getHdwrItemEnableDisableMgr().disableAfterMsgs();
					}
				}				
				if(devEnt.getHdwrItemEnableDisableMgr().isEnabled()) {
					try {
						if(m_ds.isDSAttached()) {
						switch(devEnt.cDevType) {
						case JOYSTICK_DEV:
							ans = devEnt.getJoystickObj().getRawButtonReleased(connEnt.getConnectionFrcIndex());
							break;
						case XBOX_CNTLR_DEV:
							ans = devEnt.getXboxObj().getRawButtonReleased(connEnt.getConnectionFrcIndex());
							break;
						case COMPUTER:
						default:
							throw m_exceptions.new ReachedCodeThatShouldNeverExecuteEx("should never get here!!");
							//break;
						}
						}
					} catch(RuntimeException e) {
						TmExceptions.reportExceptionOneLine(e,
								cntlEnt.cNamedCntl.name() + ":" + devEnt.cNamedDev.name() + " on " + devEnt.cNamedConn.name());
						if(m_ds.isDSAttached()) {
							devEnt.getHdwrItemEnableDisableMgr().disableAfterMsgs();
						}
					}
				}
				break;
			case kCannedAnalogButton:
				if(tbdMsgCntAnalogBtnReleased > 0) { //TODO
					P.println(PrtYn.Y, "'Button Released' not yet implemented for kCannedAnalogButton, returning ( ! getButton()) instead");
					tbdMsgCntAnalogBtnReleased--;
				}
				ans = ! getButton(cntlEnt);
				break;
			case kCannedPovButton:
				if(tbdMsgCntPovBtnReleased > 0) { //TODO
					P.println(PrtYn.Y, "'Button Released' not yet implemented for kCannedPovButton, returning ( ! getButton()) instead");
					tbdMsgCntPovBtnReleased--;
				}
				ans = ! getButton(cntlEnt);
				break;
			case kAnalog:
			case kPov:
				throw m_exceptions.new ReachedCodeThatShouldNeverExecuteEx(methodName + " should have thrown an exception before ever getting here!!");
			  //break;
			default:
				break;
			
			}
			
			
			return ans;
		}

		
		/**
		 * throw exceptions if cntlEnt or related objects have configuration issues
		 * @param cntlEnt
		 * @param methodName
		 */
		public static void inspectControl(DsNamedControlsEntry cntlEnt, String methodName, DsControlTypesE expectedCntlType) {
			inspectControl(cntlEnt, methodName, expectedCntlType, null);
		}
		public static void inspectControl(DsNamedControlsEntry cntlEnt, String methodName, 
				DsControlTypesE expectedCntlType, DsControlTypesE requiredType) {
			
			if( ! cntlEnt.isValid()) { throw m_exceptions.new 
				MethodCalledForInvalidEntryEx(methodName + " called for mis-configured control " + cntlEnt.toString()); }
			
			DsNamedDevicesEntry devEnt = cntlEnt.cNamedDev.getEnt();
			if( ! devEnt.isValid()) { throw m_exceptions.new
				MethodCalledForInvalidEntryEx(methodName + "called for mis-configured device " + devEnt.toString()); }
			
			DsConnectionsEntry connEnt = cntlEnt.cNamedConn.getEnt();
			if( ! devEnt.isValid()) { throw m_exceptions.new
				MethodCalledForInvalidEntryEx(methodName + "called for mis-configured connection " + connEnt.toString()); }
			
			//don't want exceptions when getAnalog() is called for kCannedAnalogButton type control, etc.
			if(expectedCntlType==null) {
			}
			else if(requiredType==null) {
				if( ! cntlEnt.cCntlType.eUsedAsType.equals(expectedCntlType)) {
					throw m_exceptions.new InvalidMethodForDsControlEx(methodName + " invalid for " + cntlEnt.cNamedCntl.name());
				}
			}
			else {
				if( ( ! cntlEnt.cCntlType.equals(expectedCntlType)) || ( ! cntlEnt.cCntlType.eRequiresType.equals(requiredType)) )
				{
					throw m_exceptions.new InvalidMethodForDsControlEx(methodName + " invalid for " + cntlEnt.toString());
				}				
			}
		}
		
		protected static List<DsNamedControlsEntry.ButtonInfo> cNonButtonButtonList = new ArrayList<>();
		public static List<DsNamedControlsEntry.ButtonInfo> getNonDigitalButtonList() { return cNonButtonButtonList; }
		public void doTeleopPeriodic() {
			for(DsNamedControlsEntry.ButtonInfo btn : getNonDigitalButtonList()) {
				if(btn.cmdWhenPressed != null) {
					if(btn.cntlEnt.getButton()) {
						btn.cmdWhenPressed.start();
					}
				}
			}
		}

		@Override
		public void doForcedInstantiation() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void doPopulate() {
			for(DsNamedControlsE e : DsNamedControlsE.values()) {
				e.getEnt().doPopulate();
			}
		}

	}
	

	protected static class DsControlsDevConnAssignments implements TmToolsI, TmForcedInstantiateI {

		/*---------------------------------------------------------
		 * getInstance stuff                                      
		 *---------------------------------------------------------*/
		/** 
		 * handle making the singleton instance of this class and giving
		 * others access to it
		 */
		private static DsControlsDevConnAssignments m_instance;
		public static synchronized DsControlsDevConnAssignments getInstance()
		{
			if(m_instance == null){ m_instance = new DsControlsDevConnAssignments(); }
			return m_instance;
		}
		private DsControlsDevConnAssignments() {
			if ( ! (m_instance == null)) {
				P.println("Error!!! DsDevConnMappingsAssignments.m_instance is being modified!!");
				P.println("         was: " + m_instance.toString());
				P.println("         now: " + this.toString());
			}
			m_instance = this;
		}

		/*----------------end of getInstance stuff----------------*/

		
		@Override
		public void doForcedInstantiation() {
			if(perNamedDevList.size()==0) { //only want to do this one time
				for(DsNamedDevicesE e : DsNamedDevicesE.values()) {
					perNamedDevList.add(e.ordinal(), new PerNamedDevicesEntry(e));
				}
			}
		}

		@Override
		public void doPopulate() {
			for(DsNamedControlsEntry ent : DsNamedControlsE.staticGetList()) {
				boolean isOk = true;
				isOk = assign(ent.cNamedCntl, ent.cDupOf);
				if( ! isOk) {
					ent.cIsValid = false;
				}
			}
			if(getNumberOfBadAssignmentsDetected() > 0) {
				throw m_exceptions.new DsAssignmentErrorsDetectedEx("Assignment errors found for " + 
						DsNamedControlsE.staticGetList().get(0).cNamedCntl.getClass().getSimpleName() +
						"\n      for: " + getAssignmentTroublemakers());
			}
		}

		private boolean assign(DsNamedControlsE namedCntl) {
			return assign(namedCntl, null);
		}
		private boolean assign(DsNamedControlsE namedCntl, DsNamedControlsE dupOf) {
			DsNamedControlsEntry cntlEnt = namedCntl.getEnt();
			DsNamedConnectionsE namedConn = cntlEnt.cNamedConn;
			DsNamedDevicesE namedDev = cntlEnt.cNamedDev;
			
			boolean nestedAssignOk = true;
			//handle any special cases for namedConn or namedDev here....
			if(false) {
				//  :
				//  :
				//nestedAssignOk = assign(....);
			}
			
			return nestedAssignOk && assign(namedCntl, namedConn, namedDev, dupOf);
		}
		private boolean assign(DsNamedControlsE namedCntl, DsNamedConnectionsE namedConn, 
							DsNamedDevicesE namedDev, DsNamedControlsE dupOf) {
			boolean ans = true;
			
			P.println(PrtYn.N, "Logging conn/dev mapping for " + namedCntl.name() + 
									" (" + namedConn.name() + "/" + namedDev.name() + ")");
			if(namedConn.equals(DsNamedConnectionsE.A_BUTTON)) {
				int junk = 5; //just a good debugger breakpoint
			}
			
			PerNamedDevicesEntry perNamedDevEnt = perNamedDevList.get(namedDev.ordinal());
			PerNamedConnectionsEntry perNamedConnEnt = perNamedDevEnt.perNamedConnList.get(namedConn.ordinal());
			List<DsNamedControlsE> assignedList = perNamedConnEnt.namedCntlAssignmentsList;
			
			if(assignedList.size() > 0) {
				if(dupOf==null) {
					logMultipleAssignments(namedCntl, namedConn, namedDev, dupOf, assignedList);
					ans = false;
				} else if( ! dupOf.equals(assignedList.get(0))) {
					logMultipleAssignments(namedCntl, namedConn, namedDev, dupOf, assignedList);
					ans = false;
				}
			}
			assignedList.add(namedCntl);
			return ans;
		}
		
		private void logMultipleAssignments(DsNamedControlsE namedCntl, DsNamedConnectionsE namedConn, 
						DsNamedDevicesE namedDev, DsNamedControlsE dupOf, List<DsNamedControlsE> assignedList) {
			logBadAssignment(namedCntl);
			P.println("Warning!!: multiple assignments of " + namedConn.name() + " and " + namedDev.name() + ":");
			
			String msg = "";
			for(DsNamedControlsE ent : assignedList) {
				msg += ent.name() + ", ";
				logBadAssignment(ent);
			}
			msg += namedCntl.name();
			P.println("          " + msg);		
		}

		protected List<PerNamedDevicesEntry> perNamedDevList = new ArrayList<>();

		int badAssignmentsCount = 0;
		public int getNumberOfBadAssignmentsDetected() { return badAssignmentsCount; }
		List<DsNamedControlsE> namedCntlAssignmentErrorsList = new ArrayList<>();
		protected void logBadAssignment(DsNamedControlsE namedCntl) {
			if( ! namedCntlAssignmentErrorsList.contains(namedCntl)) {
				namedCntlAssignmentErrorsList.add(namedCntl);			
				badAssignmentsCount++;
			}
		}
		protected String getAssignmentTroublemakers() {
			String ans = "";
			for(DsNamedControlsE n : namedCntlAssignmentErrorsList) {
				ans += n.name() + ", ";
			}
			return ans;
		}


		protected class PerNamedDevicesEntry {

			protected List<PerNamedConnectionsEntry> perNamedConnList = new ArrayList<>();

			PerNamedDevicesEntry(DsNamedDevicesE namedDev) {
				if(perNamedConnList.size()==0) { //only want to do this one time
					for(DsNamedConnectionsE e : DsNamedConnectionsE.values()) {
						perNamedConnList.add(e.ordinal(), new PerNamedConnectionsEntry(e));
					}
				}				
			}
			
		}

		protected class PerNamedConnectionsEntry {
			
			List<DsNamedControlsE> namedCntlAssignmentsList = new ArrayList<>();
			
			PerNamedConnectionsEntry(DsNamedConnectionsE namedConn) {
				
			}
			
		}

	}
	
	protected static class DsDevicesDevConnAssignments implements TmToolsI, TmForcedInstantiateI {

		/*---------------------------------------------------------
		 * getInstance stuff                                      
		 *---------------------------------------------------------*/
		/** 
		 * handle making the singleton instance of this class and giving
		 * others access to it
		 */
		private static DsDevicesDevConnAssignments m_instance;
		public static synchronized DsDevicesDevConnAssignments getInstance()
		{
			if(m_instance == null){ m_instance = new DsDevicesDevConnAssignments(); }
			return m_instance;
		}
		private DsDevicesDevConnAssignments() {
			if ( ! (m_instance == null)) {
				P.println("Error!!! " + m_instance.getClass().getSimpleName() + ".m_instance is being modified!!");
				P.println("         was: " + m_instance.toString());
				P.println("         now: " + this.toString());
			}
			m_instance = this;
		}

		/*----------------end of getInstance stuff----------------*/

		
		@Override
		public void doForcedInstantiation() {
			if(perNamedConnList.size()==0) { //only want to do this one time
				for(DsNamedConnectionsE e : DsNamedConnectionsE.values()) {
					perNamedConnList.add(e.ordinal(), new PerNamedConnectionsEntry(e));
					//PerNamedDeviceEntry will call other methods to set up the other lists
				}
			}
		}

		@Override
		public void doPopulate() {
			for(DsNamedDevicesEntry ent : DsNamedDevicesE.staticGetList()) {
				boolean isOk = true;
				isOk = assign(ent.cNamedDev, ent.cDupOf);
				if( ! isOk) {
					ent.cIsValid = false;
				}
			}
			if(getNumberOfBadAssignmentsDetected() > 0) {
				throw m_exceptions.new DsAssignmentErrorsDetectedEx("Assignment errors found for " + 
						DsNamedDevicesE.staticGetList().get(0).cNamedDev.getClass().getSimpleName() +
						"\n      for: " + getAssignmentTroublemakers());
			}
		}

		private boolean assign(DsNamedDevicesE namedDev) {
			return assign(namedDev, null);
		}
		private boolean assign(DsNamedDevicesE namedDev, DsNamedDevicesE dupOf) {
			DsNamedDevicesEntry devEnt = namedDev.getEnt();
			DsNamedConnectionsE namedConn = devEnt.cNamedConn;
			
			boolean nestedAssignOk = true;
			//handle any special cases for namedConn or namedDev here....
			if(false) {
				//  :
				//  :
				//nestedAssignOk = assign(....);
			}
			
			return nestedAssignOk && assign(namedDev, namedConn, dupOf);
		}
		private boolean assign(DsNamedDevicesE namedDev, DsNamedConnectionsE namedConn, DsNamedDevicesE dupOf) {
			boolean ans = true;
			
			P.println(PrtYn.N, "Logging conn/dev mapping for " + namedDev.name() + 
									" (" + namedConn.name() + ")");
			if(namedDev.equals(DsNamedDevicesE.DRIVE_LEFT_INPUT_DEV)) {
				int junk = 5; //just a good debugger breakpoint
			}
			
			PerNamedConnectionsEntry perNamedConnEnt = perNamedConnList.get(namedConn.ordinal());
			List<DsNamedDevicesE> assignedList = perNamedConnEnt.namedDevAssignmentsList;
			
			if(assignedList.size() > 0) {
				if(dupOf==null) {
					logMultipleAssignments(namedDev, namedConn, dupOf, assignedList);
					ans = false;
				} else if( ! dupOf.equals(assignedList.get(0))) {
					logMultipleAssignments(namedDev, namedConn, dupOf, assignedList);
					ans = false;
				}
			}
			assignedList.add(namedDev);
			return ans;
		}
		
		private void logMultipleAssignments(DsNamedDevicesE namedDev, DsNamedConnectionsE namedConn, 
										DsNamedDevicesE dupOf, List<DsNamedDevicesE> assignedList) {
			logBadAssignment(namedDev);
			P.println("Warning!!: multiple assignments to " + namedConn.name() + ":");
			
			String msg = "";
			for(DsNamedDevicesE ent : assignedList) {
				msg += ent.name() + ", ";
				logBadAssignment(ent);
			}
			msg += namedDev.name();
			P.println("          " + msg);		
		}

		protected List<PerNamedConnectionsEntry> perNamedConnList = new ArrayList<>();

		int badAssignmentsCount = 0;
		public int getNumberOfBadAssignmentsDetected() { return badAssignmentsCount; }
		List<DsNamedDevicesE> namedDevAssignmentErrorsList = new ArrayList<>();
		protected void logBadAssignment(DsNamedDevicesE namedDev) {
			if( ! namedDevAssignmentErrorsList.contains(namedDev)) {
				namedDevAssignmentErrorsList.add(namedDev);			
				badAssignmentsCount++;
			}
		}
		protected String getAssignmentTroublemakers() {
			String ans = "";
			for(DsNamedDevicesE n : namedDevAssignmentErrorsList) {
				ans += n.name() + ", ";
			}
			return ans;
		}


		protected class PerNamedConnectionsEntry {
			
			protected List<DsNamedDevicesE> namedDevAssignmentsList = new ArrayList<>();
			
			PerNamedConnectionsEntry(DsNamedConnectionsE namedConn) {
				//all we need is the list
			}			
		}
		
	}	
	

	@Override
	public void showEverything() {
		super.showEverything();
		if(false) {
		TmToStringI.showListEverything(dsNamedDevicesList);
		TmToStringI.showListEverything(dsNamedControlsList);
		}
	}
	

	@Override
	public void doForcedInstantiation() {
		//Access something from each of the enums in this class to force them 
		//(and their related List<> arrays) to be initialized
		//watch that optimization in the compiler doesn't decide to skip these
		super.doForcedInstantiation();

		DsDevicesDevConnAssignments.getInstance().doForcedInstantiation(); //creates the needed list structures
		DsControlsDevConnAssignments.getInstance().doForcedInstantiation();
	}
	@Override
	public void doPopulate() {
		super.doPopulate();
		
		DsDevicesDevConnAssignments.getInstance().doPopulate(); //logs all assignments, etc.
		DsControlsDevConnAssignments.getInstance().doPopulate(); //logs all assignments, etc.
		
		DsDevicesMgr.getInstance().doPopulate(); //set up joystick objects, etc.
		DsControlsMgr.getInstance().doPopulate();
		
	}

	public static void doTeleopPeriodic() {
		DsControlsMgr.getInstance().doTeleopPeriodic();
	}


}
