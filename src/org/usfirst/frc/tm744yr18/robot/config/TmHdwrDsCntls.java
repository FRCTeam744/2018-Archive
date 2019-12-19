package org.usfirst.frc.tm744yr18.robot.config;

import java.util.List;

import org.usfirst.frc.tm744yr18.bldVerInfo.TmVersionInfo;
import org.usfirst.frc.tm744yr18.robot.config.TmHdwrDsPhys;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmDsControlUserI;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmEnumWithListI;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmForcedInstantiateI;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmToStringI;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmToolsI;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmItemAvailabilityI.ItemAvailabilityE;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmItemAvailabilityI.ItemFakeableE;

import edu.wpi.first.wpilibj.command.Command;
import t744opts.Tm744Opts;

public class TmHdwrDsCntls extends TmHdwrDsPhys implements TmToolsI, TmForcedInstantiateI, TmDsControlUserI {


/*
 *                          +---+         button 1 is the trigger (digital)
 *                        4   3   5
 *                            2
 *                          |   |
 *                          |   |
 *                          |   |
 *               ---------- |   |-----------
 *             /            |   |           \
 *             |    6       |   |      11   |
 *             |            |   |           |
 *             |    7                  10   |
 *             |                            |
 *             |         8         9        |
 *             \___________+------+_________/
 *                         |  Z   |
 *                         |roller|
 *                         +------+
 * 
 * 
 *  experimentation: our joystick z-button (<joystick>.getZ() - the
 *  roller at the front bottom) is -1 when all the way up, +1 when
 *  all the way down. Convert the -1 to +1 range to a 0 to +1 range
 *  for the servos.
 *  X-axis: -1 when all the way left, +1 when all the way right
 * 
 *
 * 
 *              LT                                        RT
 *             +-+                                        +-+
 *             | |        (triggers are analog-ish)       | |
 *             | |      (combine to form 'throttle'?)     | |
 *         LB+-----+                                    +-----+RB
 *         /-+-----+-\________________________________/-+-----+-\
 *        /                                                  _   \
 *       /     __N__       +-+back      +-+start            /Y\   \
 *       |  NW/     \NE    +-+          +-+                 \_/    |
 *       | W | POV   | E     +-+mode                     _      _  |
 *       |  SW\_____/SE      +-+                        /X\    /B\ |
 *       |       S                                      \_/    \_/ |
 *       |                  _____              _____         _     |
 *       |                 / left\            /right\       /A\    |
 *       |                |  joy  |          |  joy  |      \_/    |
 *       |                 \_____/            \_____/              |
 *       |                                                         |
 *       |                                                         |
 *        \     _____________________________________________     /
 *         \---/                                             \---/
 *                   Logitech Gamepad
*/


	/*---------------------------------------------------------
	 * getInstance stuff                                      
	 *---------------------------------------------------------*/
	/** 
	 * handle making the singleton instance of this class and giving
	 * others access to it
	 */
	private static TmHdwrDsCntls m_instance;

	public static synchronized TmHdwrDsCntls getInstance() {
		if (m_instance == null) { m_instance = new TmHdwrDsCntls();	}
		return m_instance;
	}

	private TmHdwrDsCntls() {
		if ( ! (m_instance == null)) {
			P.println("Error!!! TmHdwrDsCntls.m_instance is being modified!!");
			P.println("         was: " + m_instance.toString());
			P.println("         now: " + this.toString());
		}
		m_instance = this;
	}
	/*----------------end of getInstance stuff----------------*/

	public static enum DsNamedDevicesE implements TmToStringI, TmEnumWithListI<DsNamedDevicesEntry> {
		DRIVE_LEFT_INPUT_DEV(ItemAvailabilityE.ACTIVE, ItemFakeableE.NOT_FAKEABLE, 
				DsDeviceTypesE.JOYSTICK_DEV, DsNamedConnectionsE.DS_USB0),
		DRIVE_RIGHT_INPUT_DEV(ItemAvailabilityE.USE_FAKE, ItemFakeableE.NOT_FAKEABLE, 
				DsDeviceTypesE.JOYSTICK_DEV, DsNamedConnectionsE.DS_USB1),
		MAIN_XBOX_CNTLR_DEV(
//			(Tm744Opts.OptDefaults.MAIN_XBOX_CONTROLLER_ATTACHED ? ItemAvailabilityE.ACTIVE :
			(Tm744Opts.isOptMainXboxCntlrAttached() ? ItemAvailabilityE.ACTIVE :
															ItemAvailabilityE.DISABLED), 
				ItemFakeableE.NOT_FAKEABLE, 
				DsDeviceTypesE.XBOX_CNTLR_DEV, DsNamedConnectionsE.DS_USB2),
//		OPTIONAL_XBOX_CNTLR_DEV(
////				(Tm744Opts.OptDefaults.XTRA_XBOX_CONTROLLER_ATTACHED ? ItemAvailabilityE.ACTIVE :
//			(Tm744Opts.isOptXtraXboxCntlrAttached() ? ItemAvailabilityE.ACTIVE :
//															ItemAvailabilityE.DISABLED),  
//				ItemFakeableE.NOT_FAKEABLE, 
//				DsDeviceTypesE.XBOX_CNTLR_DEV, DsNamedConnectionsE.DS_USB3),

		//		forTestSOME_NONEXISTANT_JOYSTICK_DEV(ItemAvailabilityE.USE_FAKE, ItemFakeableE.NOT_FAKEABLE, 
//				DsDeviceTypesE.JOYSTICK_DEV, DsNamedConnectionsE.DS_USB6),
//		forTest1DRIVE_RIGHT_INPUT_DEV(DsDeviceTypesE.JOYSTICK_DEV, DsNamedConnectionsE.DS_USB1), //error for testing
//		forTest2DRIVE_RIGHT_INPUT_DEV(DsDeviceTypesE.JOYSTICK_DEV, DsNamedConnectionsE.DS_USB1, DRIVE_LEFT_INPUT_DEV),
		;
		
		private DsNamedDevicesE(ItemAvailabilityE devAvail, ItemFakeableE devFakeable, DsDeviceTypesE devType, DsNamedConnectionsE namedConn) {
			this(devAvail, devFakeable, devType, namedConn, null);
		}

		private DsNamedDevicesE(ItemAvailabilityE devAvail, ItemFakeableE devFakeable, DsDeviceTypesE devType, DsNamedConnectionsE namedConn,
							DsNamedDevicesE dupOf) {
			getList().add(this.ordinal(), 
					TmHdwrDsPhys.getInstance().new DsNamedDevicesEntry(this, devAvail, devFakeable, devType, namedConn, dupOf));
		}

		@Override
       	public List<DsNamedDevicesEntry> getList() { return dsNamedDevicesList; }
       	public static List<DsNamedDevicesEntry> staticGetList() { return dsNamedDevicesList; }
       	@Override
		public DsNamedDevicesEntry getEnt() { return getList().get(this.ordinal()); }     	

       	@Override
       	public String getEnumClassName() { return this.getClass().getSimpleName(); }
       	@Override
       	public String getListEntryClassName() { return DsNamedDevicesEntry.class.getSimpleName(); }

 		String frmtStr = "%-21s [ListEntry[%2d]: %s]";
		String frmtHdr = "%-21s [               %s]";
		public String toStringLog(String inpPrefix) {
			String prefix = (inpPrefix==null) ? "" : inpPrefix;
			DsNamedDevicesEntry devEnt = this.getEnt();
			String ans = String.format(prefix + frmtStr, 
					this.name(), this.ordinal(), devEnt.toStringLog());
			return ans;
		}
		@Override
		public String toStringHdr(String inpPrefix) {
			String prefix = (inpPrefix==null) ? "" : inpPrefix;
			String listHdr = getEnt().toStringHdr(); //any entry will do...
			return (listHdr==null ? null : String.format(prefix + frmtHdr, "name", listHdr));
		}
		@Override
		public String toStringNotes() {
			return(getEnumClassName() + " - saves no info in enum (see List<" + getListEntryClassName() + "> dsDevicesList and enum's getList() and getEnt() methods)");
		}
	}
	
	public static enum DsNamedControlsE implements TmToStringI, TmEnumWithListI<DsNamedControlsEntry> {
		DRIVE_LEFT_INPUT(ItemAvailabilityE.ACTIVE, ItemFakeableE.NOT_FAKEABLE, DsControlTypesE.kAnalog, 
				DsNamedDevicesE.DRIVE_LEFT_INPUT_DEV, DsNamedConnectionsE.Y_AXIS, DsCnst.DEFAULT_JOYSTICK_DEADZONE_TOLERANCE),
		DRIVE_RIGHT_INPUT(ItemAvailabilityE.ACTIVE, ItemFakeableE.NOT_FAKEABLE, DsControlTypesE.kAnalog, 
				DsNamedDevicesE.DRIVE_RIGHT_INPUT_DEV, DsNamedConnectionsE.Y_AXIS),

//		DRIVE_VELOCITY_MODE(ItemAvailabilityE.ACTIVE, ItemFakeableE.NOT_FAKEABLE, DsControlTypesE.kButton, 
//								DsNamedDevicesE.DRIVE_LEFT_INPUT_DEV, DsNamedConnectionsE.TOP_BUT1_TRIGGER),
		
    	//2017 drivers wanted high gear on right joystick, low gear on left joystick, using triggers
		DRIVE_HIGH_GEAR_BTN(ItemAvailabilityE.ACTIVE, ItemFakeableE.NOT_FAKEABLE, DsControlTypesE.kButton, 
				DsNamedDevicesE.DRIVE_RIGHT_INPUT_DEV, DsNamedConnectionsE.TOP_BUT1_TRIGGER),
		DRIVE_LOW_GEAR_BTN(ItemAvailabilityE.ACTIVE, ItemFakeableE.NOT_FAKEABLE, DsControlTypesE.kButton, 
				DsNamedDevicesE.DRIVE_LEFT_INPUT_DEV, DsNamedConnectionsE.TOP_BUT1_TRIGGER),

		DRIVE_RECALIBRATE_GYRO_RATE_BTN(ItemAvailabilityE.ACTIVE, ItemFakeableE.NOT_FAKEABLE, DsControlTypesE.kButton, 
				DsNamedDevicesE.DRIVE_RIGHT_INPUT_DEV, DsNamedConnectionsE.BASE_BUT6_LEFT_BACK),
		//they now want Y to move to max height
//		ARM_LIFT_BOTH_SCALE_MID_BTN(ItemAvailabilityE.DISABLED, ItemFakeableE.NOT_FAKEABLE, 
//				DsControlTypesE.kCannedPovButton, 
//				DsNamedDevicesE.MAIN_XBOX_CNTLR_DEV, DsNamedConnectionsE.POV_NE_BUTTON),
//		ARM_LIFT_BOTH_SCALE_LOW_BTN(ItemAvailabilityE.ACTIVE, ItemFakeableE.NOT_FAKEABLE, 
//				DsControlTypesE.kButton, 
//				DsNamedDevicesE.MAIN_XBOX_CNTLR_DEV, DsNamedConnectionsE.X_BUTTON),
//
////		ARM_LIFT_BOTH_SWITCH_BTN(ItemAvailabilityE.ACTIVE, ItemFakeableE.NOT_FAKEABLE, 
////				DsControlTypesE.kButton, 
////				DsNamedDevicesE.DRIVE_RIGHT_INPUT_DEV, DsNamedConnectionsE.BASE_BUT9_FRONT_RIGHT),
//		ARM_LIFT_BOTH_SWITCH_BTN(ItemAvailabilityE.ACTIVE, ItemFakeableE.NOT_FAKEABLE, 
//				DsControlTypesE.kButton, 
//				DsNamedDevicesE.MAIN_XBOX_CNTLR_DEV, DsNamedConnectionsE.A_BUTTON),
		ARM_LIFT_BOTH_SCALE_MID_BTN(ItemAvailabilityE.ACTIVE, ItemFakeableE.NOT_FAKEABLE, 
				DsControlTypesE.kButton, 
				DsNamedDevicesE.MAIN_XBOX_CNTLR_DEV, DsNamedConnectionsE.X_BUTTON),
		ARM_LIFT_BOTH_SCALE_LOW_BTN(ItemAvailabilityE.ACTIVE, ItemFakeableE.NOT_FAKEABLE, 
				DsControlTypesE.kButton, 
				DsNamedDevicesE.MAIN_XBOX_CNTLR_DEV, DsNamedConnectionsE.A_BUTTON),

		ARM_LIFT_BOTH_SWITCH_BTN(ItemAvailabilityE.ACTIVE, ItemFakeableE.NOT_FAKEABLE, 
				DsControlTypesE.kCannedPovButton, 
				DsNamedDevicesE.MAIN_XBOX_CNTLR_DEV, DsNamedConnectionsE.POV_EAST_BUTTON),
		ARM_LIFT_BOTH_BOTTOM_BTN(ItemAvailabilityE.ACTIVE, ItemFakeableE.NOT_FAKEABLE, 
				DsControlTypesE.kCannedPovButton, 
				DsNamedDevicesE.MAIN_XBOX_CNTLR_DEV, DsNamedConnectionsE.POV_WEST_BUTTON),
		ARM_LIFT_BOTH_AT_TOP_BTN(ItemAvailabilityE.ACTIVE, ItemFakeableE.NOT_FAKEABLE, 
				DsControlTypesE.kButton, 
				DsNamedDevicesE.MAIN_XBOX_CNTLR_DEV, DsNamedConnectionsE.Y_BUTTON),
		
//		ARM_CLAW_START_GRABBING_BTN(ItemAvailabilityE.ACTIVE, ItemFakeableE.NOT_FAKEABLE, 
//				DsControlTypesE.kButton, 
//				DsNamedDevicesE.MAIN_XBOX_CNTLR_DEV, DsNamedConnectionsE.X_BUTTON),		
//		ARM_CLAW_START_RELEASING_BTN(ItemAvailabilityE.ACTIVE, ItemFakeableE.NOT_FAKEABLE, 
//				DsControlTypesE.kButton, 
//				DsNamedDevicesE.MAIN_XBOX_CNTLR_DEV, DsNamedConnectionsE.B_BUTTON),		
		ARM_CLAW_STOP_MOTORS_BTN(ItemAvailabilityE.ACTIVE, ItemFakeableE.NOT_FAKEABLE, 
				DsControlTypesE.kCannedPovButton, 
				DsNamedDevicesE.MAIN_XBOX_CNTLR_DEV, DsNamedConnectionsE.POV_SOUTH_BUTTON),	
		
//		ARM_CLAW_GRAB_WHILE_HELD_BTN(ItemAvailabilityE.ACTIVE, ItemFakeableE.NOT_FAKEABLE, 
//				DsControlTypesE.kButton, 
//				DsNamedDevicesE.MAIN_XBOX_CNTLR_DEV, DsNamedConnectionsE.X_BUTTON),		
		ARM_CLAW_RELEASE_WHILE_HELD_BTN(ItemAvailabilityE.ACTIVE, ItemFakeableE.NOT_FAKEABLE, 
				DsControlTypesE.kButton, 
				DsNamedDevicesE.MAIN_XBOX_CNTLR_DEV, DsNamedConnectionsE.B_BUTTON),		

//		ARM_LIFT_SET_TO_IDLE_MODE_BTN(ItemAvailabilityE.ACTIVE, ItemFakeableE.NOT_FAKEABLE, 
//				DsControlTypesE.kCannedPovButton, 
//				DsNamedDevicesE.MAIN_XBOX_CNTLR_DEV, DsNamedConnectionsE.POV_EAST_BUTTON),		
		ARM_LIFT_RUN_WITH_JOYSTICKS_BTN(ItemAvailabilityE.ACTIVE, ItemFakeableE.NOT_FAKEABLE, 
				DsControlTypesE.kButton, 
				DsNamedDevicesE.MAIN_XBOX_CNTLR_DEV, DsNamedConnectionsE.BACK_BUTTON),	
		
//		//for double stage arm only
//		ARM_STAGE1_MTR_INPUT(ItemAvailabilityE.ACTIVE, ItemFakeableE.NOT_FAKEABLE, 
//				DsControlTypesE.kAnalog, 
//				DsNamedDevicesE.MAIN_XBOX_CNTLR_DEV, DsNamedConnectionsE.LEFT_JOY_Y),
		
		ARM_DOUBLE_STAGE_MTR_INPUT(ItemAvailabilityE.ACTIVE, ItemFakeableE.NOT_FAKEABLE, 
				DsControlTypesE.kAnalog, 
				DsNamedDevicesE.MAIN_XBOX_CNTLR_DEV, DsNamedConnectionsE.RIGHT_JOY_Y),

//		@Deprecated
//		ARM_LIFT_OVERRIDE_BOTTOM_LIMIT_WHILE_HELD_BTN(ItemAvailabilityE.DISABLED, ItemFakeableE.NOT_FAKEABLE, 
//				DsControlTypesE.kButton, 
//				DsNamedDevicesE.DRIVE_RIGHT_INPUT_DEV, DsNamedConnectionsE.BASE_BUT6_LEFT_BACK),
		ARM_LIFT_RESET_ENCODER_BTN(ItemAvailabilityE.ACTIVE, ItemFakeableE.NOT_FAKEABLE, 
				DsControlTypesE.kButton, 
				DsNamedDevicesE.DRIVE_RIGHT_INPUT_DEV, DsNamedConnectionsE.BASE_BUT7_LEFT_FRONT),
		
		ARM_LIFT_OVERRIDE_LIMIT_SWITCHES_BTN(ItemAvailabilityE.ACTIVE, ItemFakeableE.NOT_FAKEABLE, 
				DsControlTypesE.kButton, 
				DsNamedDevicesE.DRIVE_RIGHT_INPUT_DEV, DsNamedConnectionsE.BASE_BUT10_RIGHT_FRONT),
		ARM_LIFT_USE_LIMIT_SWITCHES_BTN(ItemAvailabilityE.ACTIVE, ItemFakeableE.NOT_FAKEABLE, 
				DsControlTypesE.kButton, 
				DsNamedDevicesE.DRIVE_RIGHT_INPUT_DEV, DsNamedConnectionsE.BASE_BUT11_RIGHT_BACK),

		//reserve left drv js BUT9 for reset NavX gyro if needed
//		ARM_CLAW_RUN_WITH_JOYSTICK_BTN(ItemAvailabilityE.ACTIVE, ItemFakeableE.NOT_FAKEABLE, 
//				DsControlTypesE.kButton, 
//				DsNamedDevicesE.DRIVE_LEFT_INPUT_DEV, DsNamedConnectionsE.BASE_BUT9_FRONT_RIGHT),
		ARM_CLAW_RUN_WITH_JOYSTICK_INPUT(ItemAvailabilityE.ACTIVE, ItemFakeableE.NOT_FAKEABLE, 
				DsControlTypesE.kAnalog, 
				DsNamedDevicesE.MAIN_XBOX_CNTLR_DEV, DsNamedConnectionsE.LEFT_JOY_Y),

		GRABBER_MOTORS_RELEASING_INPUT(ItemAvailabilityE.ACTIVE, ItemFakeableE.NOT_FAKEABLE, 
				DsControlTypesE.kAnalog, 
				DsNamedDevicesE.MAIN_XBOX_CNTLR_DEV, DsNamedConnectionsE.LEFT_TRIGGER),
		GRABBER_MOTORS_GRABBING_INPUT(ItemAvailabilityE.ACTIVE, ItemFakeableE.NOT_FAKEABLE, 
				DsControlTypesE.kAnalog, 
				DsNamedDevicesE.MAIN_XBOX_CNTLR_DEV, DsNamedConnectionsE.RIGHT_TRIGGER),
		GRABBER_UNCLAMP_WHILE_HELD(ItemAvailabilityE.ACTIVE, ItemFakeableE.NOT_FAKEABLE, 
				DsControlTypesE.kButton, 
				DsNamedDevicesE.MAIN_XBOX_CNTLR_DEV, DsNamedConnectionsE.START_BUTTON),
		
		GRABBER_DOWN_CLAMP_NO_WEDGE_BTN(ItemAvailabilityE.ACTIVE, ItemFakeableE.NOT_FAKEABLE, 
				DsControlTypesE.kButton, 
				DsNamedDevicesE.MAIN_XBOX_CNTLR_DEV, DsNamedConnectionsE.RB_BUTTON),
		GRABBER_FULL_UP_AND_CLAMPED_BTN(ItemAvailabilityE.ACTIVE, ItemFakeableE.NOT_FAKEABLE, 
				DsControlTypesE.kButton, 
				DsNamedDevicesE.MAIN_XBOX_CNTLR_DEV, DsNamedConnectionsE.LB_BUTTON),
		GRABBER_PARTIAL_UP_AND_CLAMPED_BTN(ItemAvailabilityE.ACTIVE, ItemFakeableE.NOT_FAKEABLE, 
				DsControlTypesE.kCannedPovButton, 
				DsNamedDevicesE.MAIN_XBOX_CNTLR_DEV, DsNamedConnectionsE.POV_NORTH_BUTTON),
		
		SHOW_DRVSTA_CNTLS_ON_CONSOLE(ItemAvailabilityE.ACTIVE, ItemFakeableE.NOT_FAKEABLE, 
				DsControlTypesE.kButton, 
				DsNamedDevicesE.DRIVE_LEFT_INPUT_DEV, DsNamedConnectionsE.BASE_BUT11_RIGHT_BACK),
		SHOW_ROBOT_CNTLS_ON_CONSOLE(ItemAvailabilityE.ACTIVE, ItemFakeableE.NOT_FAKEABLE, 
				DsControlTypesE.kButton, 
				DsNamedDevicesE.DRIVE_LEFT_INPUT_DEV, DsNamedConnectionsE.BASE_BUT10_RIGHT_FRONT),
		
//		SOME_JS_BUTTON(ItemAvailabilityE.ACTIVE, ItemFakeableE.NOT_FAKEABLE, DsControlTypesE.kButton, 
//				DsNamedDevicesE.DRIVE_LEFT_INPUT_DEV, DsNamedConnectionsE.BASE_BUT10_RIGHT_FRONT),
//		SOME_XBOX_BUTTON(ItemAvailabilityE.ACTIVE, ItemFakeableE.NOT_FAKEABLE, DsControlTypesE.kButton, 
//				DsNamedDevicesE.OTHER_INPUT_DEV, DsNamedConnectionsE.A_BUTTON),
//		SOME_XBOX_JOYSTICK(ItemAvailabilityE.ACTIVE, ItemFakeableE.NOT_FAKEABLE, DsControlTypesE.kAnalog, 
//				DsNamedDevicesE.OTHER_INPUT_DEV, DsNamedConnectionsE.LEFT_JOY_X),
//		SOME_XBOX_POV(ItemAvailabilityE.ACTIVE, ItemFakeableE.NOT_FAKEABLE, DsControlTypesE.kPov, 
//				DsNamedDevicesE.OTHER_INPUT_DEV, DsNamedConnectionsE.POV),
//		SOME_ANALOG_BUTTON(ItemAvailabilityE.ACTIVE, ItemFakeableE.NOT_FAKEABLE, DsControlTypesE.kCannedAnalogButton, 
//				DsNamedDevicesE.DRIVE_LEFT_INPUT_DEV, DsNamedConnectionsE.JOY_Z_DOWN_AS_BUTTON),
//		SOME_POV_BUTTON(ItemAvailabilityE.ACTIVE, ItemFakeableE.NOT_FAKEABLE, DsControlTypesE.kCannedPovButton, 
//				DsNamedDevicesE.OTHER_INPUT_DEV, DsNamedConnectionsE.POV_EAST_BUTTON),

//		SOME_FAKE_JS_THING(ItemAvailabilityE.ACTIVE, ItemFakeableE.NOT_FAKEABLE, DsControlTypesE.kButton, 
//				DsNamedDevicesE.forTestSOME_NONEXISTANT_JOYSTICK_DEV, DsNamedConnectionsE.BASE_BUT10_RIGHT_FRONT)
		
//		forTest_DRIVE_LEFT_INPUT(DsControlTypesE.kAnalog, DsNamedDevicesE.DRIVE_LEFT_INPUT_DEV, DsNamedConnectionsE.Y_AXIS, DsCnst.DEFAULT_JOYSTICK_DEADZONE_TOLERANCE),
//		forTest_DRIVE_RIGHT_INPUT(DsControlTypesE.kAnalog, DsNamedDevicesE.DRIVE_RIGHT_INPUT_DEV, DsNamedConnectionsE.Y_AXIS),
		;
		
		@Override
       	public List<DsNamedControlsEntry> getList() { return dsNamedControlsList; } //getDsControlsList(); }
       	public static List<DsNamedControlsEntry> staticGetList() { return dsNamedControlsList; } //getDsControlsList(); }
       	@Override
		public DsNamedControlsEntry getEnt() { return getList().get(this.ordinal()); } //getDsControlsList().get(this.ordinal()); }

       	@Override
       	public String getEnumClassName() { return this.getClass().getSimpleName(); }
       	@Override
       	public String getListEntryClassName() { return DsNamedControlsEntry.class.getSimpleName(); }
       	

       	//using these may add overhead. keep them so users can see how to access info, but keep them private		
		private double getAnalog() { return DsControlsMgr.getAnalog(this.getEnt()); }
		private int getPov() { return DsControlsMgr.getPov(this.getEnt()); }
		private boolean getButton() { return DsControlsMgr.getButton(this.getEnt()); }
		private boolean getButtonPressed() { return DsControlsMgr.getButtonPressed(this.getEnt()); }
		private boolean getButtonReleased() { return DsControlsMgr.getButtonReleased(this.getEnt()); }

		
		private DsNamedControlsE(ItemAvailabilityE cntlAvail, ItemFakeableE cntlFakeable, DsControlTypesE cntlType, DsNamedDevicesE namedDev, DsNamedConnectionsE namedConn) {
			this(cntlAvail, cntlFakeable, cntlType, namedDev, namedConn, null, DsCnst.DEFAULT_JOYSTICK_DEADZONE_TOLERANCE);
		}
		private DsNamedControlsE(ItemAvailabilityE cntlAvail, ItemFakeableE cntlFakeable, DsControlTypesE cntlType, DsNamedDevicesE namedDev, 
				DsNamedConnectionsE namedConn, double jsDeadzoneTol) {
			this(cntlAvail, cntlFakeable, cntlType, namedDev, namedConn, null, jsDeadzoneTol);
		}
		private DsNamedControlsE(ItemAvailabilityE cntlAvail, ItemFakeableE cntlFakeable, DsControlTypesE cntlType, DsNamedDevicesE namedDev, 
									DsNamedConnectionsE namedConn, DsNamedControlsE dupOf, double jsDeadzoneTol) {
			getList().add(this.ordinal(), 
					TmHdwrDsPhys.getInstance().new DsNamedControlsEntry(this, cntlAvail, cntlFakeable, cntlType, namedDev, 
							namedConn, dupOf, jsDeadzoneTol));
		}
		
		String frmtStr = "%-25s [ListEntry[%2d]: %s]";
		String frmtHdr = "%-25s [               %s]";
		public String toStringLog(String inpPrefix) {
			String prefix = (inpPrefix==null) ? "" : inpPrefix;
			DsNamedControlsEntry devEnt = getList().get(this.ordinal());
			String ans = String.format(frmtStr, this.name(), this.ordinal(), devEnt.toStringLog());
			return ans;
		}
		@Override
 		public String toStringHdr(String inpPrefix) {
			String prefix = (inpPrefix==null) ? "" : inpPrefix;
			String listHdr = getList().get(0).toStringHdr();
			return (listHdr==null ? null : String.format(prefix + frmtHdr, "name", listHdr));
		}
		@Override
		public String toStringNotes() {
			return(getEnumClassName() + " - saves no info in enum (see List<" + getListEntryClassName() + "> dsDevicesList and enum's getList() and getEnt() methods)");
		}
	}
	
	
	public void showEverything() {
		super.showEverything();
		if(false) { //keep so can quickly turn this stuff on or off
			TmToStringI.showEnumEverything(DsNamedDevicesE.values());
			TmToStringI.showEnumEverything(DsNamedControlsE.values());
		}
	}
	
	@Override
	public void doForcedInstantiation() {
		//Access something from each of the enums in this class to force them 
		//(and their related List<> arrays) to be initialized
		//watch that optimization in the compiler doesn't decide to skip these
		super.doForcedInstantiation();
		DsNamedDevicesE junk0 = DsNamedDevicesE.DRIVE_LEFT_INPUT_DEV;
		DsNamedControlsE junk1 = DsNamedControlsE.DRIVE_LEFT_INPUT;
	}
	@Override
	public void doPopulate() {
		super.doPopulate();
//		TmSdMgr.putData(SdKeysE.KEY_CMD_SHOW_DRVSTA_IO, 
//				LocalCommands.getInstance().new LocalCmd_ShowAllDsIoOnConsole());
		DsNamedControlsE.SHOW_DRVSTA_CNTLS_ON_CONSOLE.getEnt().whenPressed(this, 
				LocalCommands.getInstance().new LocalCmd_ShowAllDsIoOnConsole());
	}
	
	public static enum ShowDsConnectionsSortingE { ENUM_ORDER, GROUP_BY_DEVICE }
	public static void showConnections() { showConnections(ShowDsConnectionsSortingE.GROUP_BY_DEVICE); }
	public static void showConnections(ShowDsConnectionsSortingE order) {
		showConnections(order, true);
	}
	public static void showConnections(ShowDsConnectionsSortingE order, boolean shortForm) {
//		super.showConnections();
		String frmtHdr;
		String frmtStr;
		if(shortForm) {
			frmtHdr = "%-36s, %-25s, %-28s";
			frmtStr = "%-36s, %-25s, %-6s:%-21s";			
		} else {
			frmtHdr = "%-36s, %-25s, %-28s,\n%35s%s\n%35s%s\n";
			frmtStr = "%-36s, %-25s, %-6s:%-21s,\n%35scmd: %s\n%35susers: %s\n";			
		}
		P.println("Driver Station controls -- sort order: " + order.name());
		P.println("code build: " + TmVersionInfo.getProjectName());
		P.println("            " + TmVersionInfo.getDateTimeHostString());
		if(shortForm) {
			P.printFrmt(frmtHdr, "control", "connection",  "module");
		} else {
			P.printFrmt(frmtHdr, "control", "connection",  "module","", "command (buttons only)",
					"", "other users");
		}
		switch(order) {
		case ENUM_ORDER:
			for(DsNamedControlsE c : DsNamedControlsE.values()) {
				if(shortForm) {
					P.printFrmt(frmtStr, c.name(),
							c.getEnt().cNamedConn.name(),
							c.getEnt().cNamedDev.getEnt().cNamedConn.name(),
							c.getEnt().cNamedDev.name()
							);
				} else {
					P.printFrmt(frmtStr, c.name(),
							c.getEnt().cNamedConn.name(),
							c.getEnt().cNamedDev.getEnt().cNamedConn.name(),
							c.getEnt().cNamedDev.name(), "",
							((c.getEnt().cBtnInfo==null) ? "n/a" :
								(c.getEnt().cBtnInfo.cmdWhenPressed==null) ? "n/a" : 
									c.getEnt().cBtnInfo.cmdWhenPressed.toString()),
							"", ( (c.getEnt().getRegisteredUsers().equals("")) ? "n/a" :
								c.getEnt().getRegisteredUsers())
							);
				}
			}	
			break;
		case GROUP_BY_DEVICE:
			for(DsNamedDevicesE d : DsNamedDevicesE.values()) {
				for(DsNamedControlsE c : DsNamedControlsE.values()) {
					if(c.getEnt().cNamedDev.equals(d)) {
						if(shortForm) {
							//String frmtStr = "%-33s, %-25s, %-6s:%-21s,\n%35scmd: %s\n%35susers: %s\n";
							P.printFrmt(frmtStr, c.name(),
									c.getEnt().cNamedConn.name(),
									c.getEnt().cNamedDev.getEnt().cNamedConn.name(),
									c.getEnt().cNamedDev.name()
									);
						} else {
							P.printFrmt(frmtStr, c.name(),
									c.getEnt().cNamedConn.name(),
									c.getEnt().cNamedDev.getEnt().cNamedConn.name(),
									c.getEnt().cNamedDev.name(), "",
									((c.getEnt().cBtnInfo==null) ? "n/a" :
										(c.getEnt().cBtnInfo.cmdWhenPressed==null) ? "n/a" : 
											c.getEnt().cBtnInfo.cmdWhenPressed.toString()),
									"", ( (c.getEnt().getRegisteredUsers().equals("")) ? "n/a" :
										c.getEnt().getRegisteredUsers())
									);
						}
					}	
				}				
			}
			break;
		}
	}
	
	public static class LocalCommands {
		private final static LocalCommands lcInstance = new LocalCommands();
		public static LocalCommands getInstance() { return lcInstance; }
		private LocalCommands() {}
		
		public class LocalCmd_ShowAllDsIoOnConsole extends Command implements TmToolsI {
			public LocalCmd_ShowAllDsIoOnConsole() {
				// Use requires() here to declare subsystem dependencies
				// eg. requires(chassis);
			}

			// Called just before this Command runs the first time
			protected void initialize() {
				P.println(Tt.getClassName(this) + " running");
				//	    	TmHdwrDsCntls.showAllDsIo();
				showConnections(); //TmToStringI.showListEverything(DsNamedControlsE.staticGetList());
			}

			// Called repeatedly when this Command is scheduled to run
			protected void execute() {
			}

			// Make this return true when this Command no longer needs to run execute()
			protected boolean isFinished() {
				return true;
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
	
}
