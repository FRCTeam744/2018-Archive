package org.usfirst.frc.tm744yr18.robot.config;

import java.util.ArrayList;
import java.util.List;

import org.usfirst.frc.tm744yr18.robot.exceptions.TmExceptions;
import org.usfirst.frc.tm744yr18.robot.helpers.TmHdwrItemEnableDisableMgr;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmEnumWithListI;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmForcedInstantiateI;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmListBackingEnumI;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmToStringI;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmToolsI;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmToolsI.P.PrtYn;


public class TmHdwrDsPhysBase implements TmToolsI, TmForcedInstantiateI {
	

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
	private static TmHdwrDsPhysBase m_instance;

	public static synchronized TmHdwrDsPhysBase getInstance() {
		if (m_instance == null) {
			m_instance = new TmHdwrDsPhysBase();
		}
		return m_instance;
	}

	protected TmHdwrDsPhysBase() {
		if (!(m_instance == null)) {
			P.println("Error!!! TmHdwrDsPhysBase.m_instance is being modified!!");
			P.println("         was: " + m_instance.toString());
			P.println("         now: " + this.toString());
		}
		m_instance = this;
	}
	/*----------------end of getInstance stuff----------------*/


	public static enum DsDeviceTypesE implements TmToStringI {
		JOYSTICK_DEV, XBOX_CNTLR_DEV, COMPUTER;

		@Override
		public String toStringLog(String inpPrefix) { 
			String prefix = (inpPrefix==null) ? "" : inpPrefix;
			return prefix + toString();
		}
		@Override
		public String toStringHdr(String inpPrefix) { return null; }
		@Override
		public String toStringNotes() {
			return this.getClass().getSimpleName() + " entries have no associated data";
		}
	}

	public static enum DsControlTypesE implements TmToStringI { 
		kAnalog, kButton, kPov, kCannedAnalogButton(kButton, kAnalog), kCannedPovButton(kButton, kPov);

		private static String frmtStr = "%-19s [%-19s, %-19s]";
		private static String frmtHdr = "%-19s [%-19s, %-19s]";
		@Override
		public String toStringLog(String inpPrefix) {
			String prefix = (inpPrefix==null) ? "" : inpPrefix;
			String ans = String.format(prefix + frmtStr, 
					this.name(), eUsedAsType, ((eRequiresType==null) ? "null" : eRequiresType.name()));
			return ans;
		}
		@Override
		public String toStringHdr(String inpPrefix) {
			String prefix = (inpPrefix==null) ? "" : inpPrefix;
			String typeName = this.getClass().getSimpleName();
			String ans = String.format(prefix + frmtHdr, 
					typeName, typeName, typeName);
			return ans;
		}
		@Override
		public String toStringNotes() {
			return null; //this.getClass().getSimpleName() + " entries have no associated data";
		}
		
		final DsControlTypesE eUsedAsType;
		final DsControlTypesE eRequiresType;
		
		private DsControlTypesE() {
			this(null, null);
		}
		private DsControlTypesE(DsControlTypesE usedAs, DsControlTypesE requiresType) {
			if(usedAs==null) {
				eUsedAsType = this;
				eRequiresType = null;
			} else {
				eUsedAsType = usedAs;
				eRequiresType = requiresType;
			}
		}
		
	}
	

	public static enum DsConnectionTypesE implements TmToStringI {
		USB_PORT(DsDeviceTypesE.COMPUTER, DsCnst.kFIRST_USB_PORT_NUMBER, DsCnst.kUSB_PORT_CNT),
		JS_FRC_BUTTON(DsDeviceTypesE.JOYSTICK_DEV, DsCnst.JS_FRC_BUTTON_BASE_NBR, DsCnst.JS_FRC_BUTTON_CNT),
		JS_FRC_ANALOG(DsDeviceTypesE.JOYSTICK_DEV, DsCnst.JS_FRC_ANALOG_BASE_NBR, DsCnst.JS_FRC_ANALOG_CNT),
	 	JS_TM_CANNED_ANALOG_BUTTON(DsDeviceTypesE.JOYSTICK_DEV, DsCnst.JS_TM_ANALOG_BASE_NBR, DsCnst.JS_TM_ANALOG_BUTTON_CNT),
		XBOX_FRC_BUTTON(DsDeviceTypesE.XBOX_CNTLR_DEV, DsCnst.GC_FRC_BUTTON_BASE_NBR, DsCnst.GC_FRC_BUTTON_CNT),
		XBOX_FRC_ANALOG(DsDeviceTypesE.XBOX_CNTLR_DEV, DsCnst.GC_FRC_ANALOG_BASE_NBR, DsCnst.GC_FRC_ANALOG_CNT),
		XBOX_FRC_POV(DsDeviceTypesE.XBOX_CNTLR_DEV, DsCnst.GC_FRC_POV_BASE_NBR, DsCnst.GC_FRC_POV_CNT),
		XBOX_TM_CANNED_POV_BUTTON(DsDeviceTypesE.XBOX_CNTLR_DEV, DsCnst.GC_TM_POV_BUTTON_BASE_NBR, DsCnst.GC_TM_POV_BUTTON_CNT),
		;
		
		public final DsDeviceTypesE eDevType;
		public final int eMinNdx;
		public final int eCount;
		
		private static String frmtStr = "%-26s [%-14s, minNdx=%2d, maxNdx=%2d, cnt=%2d]";
		private static String frmtHdr = "%-26s [%-14s, ........., ........., ......]";
		@Override
		public String toStringLog(String inpPrefix) {
			String prefix = (inpPrefix==null) ? "" : inpPrefix;
			String ans = String.format(prefix + frmtStr, 
					this.name(), ((eDevType==null) ? "null" : eDevType.name()), eMinNdx, (eMinNdx + eCount -1), eCount);
			return ans;
		}		
		@Override
		public String toStringHdr(String inpPrefix) {
			String prefix = (inpPrefix==null) ? "" : inpPrefix;
			String ans = String.format(prefix + frmtHdr, ".......name.......", eDevType.getClass().getSimpleName());
			return ans;
		}
		@Override
		public String toStringNotes() { return null; }
		
		private DsConnectionTypesE() { 
			eDevType = null;
			eMinNdx = DsCnst.INVALID_DS_HDWR_CONNECTION_INDEX;
			eCount = 0;
		}
		private DsConnectionTypesE(DsDeviceTypesE devType) {
			eDevType = devType;
			eMinNdx = DsCnst.INVALID_DS_HDWR_CONNECTION_INDEX;
			eCount = 0;
		}
		private DsConnectionTypesE(DsDeviceTypesE devType, int minNdx, int count) {
			eDevType = devType;
			eMinNdx = minNdx;
			eCount = count;
		}

	}
	
	private enum PovAnglesE implements TmToStringI {
        POV_NORTH_ANGLE(0), // 0 degrees
        POV_SOUTH_ANGLE(180), // 180 degrees
        POV_EAST_ANGLE(90), // 90 degrees
        POV_WEST_ANGLE(270), // 270 degrees
        
        //these are hard to hit accurately and probably shouldn't be used
        //except for lab test purposes
        POV_NE_ANGLE(45),
        POV_SE_ANGLE(225),
        POV_NW_ANGLE(135),
        POV_SW_ANGLE(315),
        
        POV_BOGUS_ANGLE_FOR_NONPOV_INPUTS(400); //any obviously bogus value will do....
        ;
		
		private int ePovAngle;
		
		private PovAnglesE(int angle) {
			ePovAngle = angle;			
		}
		
		public int getAngle() {return ePovAngle;}

		String frmtStr    = "%-15s [angle=%3d]";
		String frmtHdr = "%-15s [.........]";
		@Override
		public String toStringLog(String inpPrefix) {
			String prefix = (inpPrefix==null) ? "" : inpPrefix;
			return String.format(prefix + frmtStr, this.name(), ePovAngle);
		}

		@Override
		public String toStringHdr(String inpPrefix) {
			String prefix = (inpPrefix==null) ? "" : inpPrefix;
			return String.format(prefix + frmtHdr, "....name....");
		}

		@Override
		public String toStringNotes() {
			return null;
		}

	}
	
	public enum PovInputForBtnE implements TmToStringI {
		POV0_NORTH(DsConnectionTypesE.XBOX_FRC_POV, 0 + DsConnectionTypesE.XBOX_FRC_POV.eMinNdx, PovAnglesE.POV_NORTH_ANGLE),
		POV0_SOUTH(DsConnectionTypesE.XBOX_FRC_POV, 0 + DsConnectionTypesE.XBOX_FRC_POV.eMinNdx, PovAnglesE.POV_SOUTH_ANGLE),
		POV0_EAST(DsConnectionTypesE.XBOX_FRC_POV, 0 + DsConnectionTypesE.XBOX_FRC_POV.eMinNdx, PovAnglesE.POV_EAST_ANGLE),
		POV0_WEST(DsConnectionTypesE.XBOX_FRC_POV, 0 + DsConnectionTypesE.XBOX_FRC_POV.eMinNdx, PovAnglesE.POV_WEST_ANGLE),
		POV0_NE(DsConnectionTypesE.XBOX_FRC_POV, 0 + DsConnectionTypesE.XBOX_FRC_POV.eMinNdx, PovAnglesE.POV_NE_ANGLE, 2),
		POV0_NW(DsConnectionTypesE.XBOX_FRC_POV, 0 + DsConnectionTypesE.XBOX_FRC_POV.eMinNdx, PovAnglesE.POV_NW_ANGLE, 2),
		POV0_SE(DsConnectionTypesE.XBOX_FRC_POV, 0 + DsConnectionTypesE.XBOX_FRC_POV.eMinNdx, PovAnglesE.POV_SE_ANGLE, 2),
		POV0_SW(DsConnectionTypesE.XBOX_FRC_POV, 0 + DsConnectionTypesE.XBOX_FRC_POV.eMinNdx, PovAnglesE.POV_SW_ANGLE, 2),

		//Attention!!! DsCnst.GC_TM_POV_BUTTON_CNT should match PovInputForBtnE.values().length
		
		;
		
		protected final DsConnectionTypesE eConnType;
		protected final int eFrcPovNdx;
		protected final PovAnglesE ePovAngleId; //here for reference, code should use ePovAngle directly
		protected final int ePovAngle;
		protected final int ePovAngleTolerance;
		//let DsConnectionsEntry verify sane info
		private PovInputForBtnE(DsConnectionTypesE connType, int povNdx, PovAnglesE povAngleId) {
			this(connType, povNdx, povAngleId, DsCnst.DEFAULT_POV_DEADZONE_TOLERANCE);
		}
		private PovInputForBtnE(DsConnectionTypesE connType, int povNdx, PovAnglesE povAngleId, int angleTolerance) {
			eConnType = connType;
			eFrcPovNdx = povNdx;
			if(povAngleId==null) {
				ePovAngleId = PovAnglesE.POV_BOGUS_ANGLE_FOR_NONPOV_INPUTS;
				ePovAngle = PovAnglesE.POV_BOGUS_ANGLE_FOR_NONPOV_INPUTS.ePovAngle;
			} else {
				ePovAngleId = povAngleId;
				ePovAngle = povAngleId.ePovAngle;
			}
			ePovAngleTolerance = angleTolerance;
		}
		
		String frmtStr = "%-10s [%-18s, povNdx=%2d, %-16s, angle=% 4d, tol=%d]";
		String frmtHdr = "%-10s [%-18s, ........., %-16s, .........., .....]";
		public String toStringLog(String inpPrefix) {
			String prefix = (inpPrefix==null) ? "" : inpPrefix;
			String ans = String.format(prefix + frmtStr, 
    				this.name(), eConnType.name(), eFrcPovNdx, 
    				((ePovAngleId==null) ? "n/a" : ePovAngleId.name()), ePovAngle, ePovAngleTolerance);
    		return ans;
		}
		@Override
		public String toStringHdr(String inpPrefix) {
			String prefix = (inpPrefix==null) ? "" : inpPrefix;
			return String.format(prefix + frmtHdr, "...name...", eConnType.getClass().getSimpleName(),
					ePovAngleId.getClass().getSimpleName());
		}
		@Override
		public String toStringNotes() { return null; }
	}
	
	public enum AnalogRangeForBtnE implements TmToStringI {
		MAX_NEG(-1.0, -0.6), MID(-0.57, 0.57), MAX_POS(0.6, 1.0), 
		NONE(1.0, -1.0), //min>max on purpose so can never be active!!!
		;
		
		protected final double eMin;
		protected final double eMax;
		
		private AnalogRangeForBtnE(double min, double max) {
			eMin = min;
			eMax = max;
		}
		
		public boolean isActive(double testVal) {
			boolean ans = false;
			ans = Tt.isInRange(testVal, eMin, eMax); //, Tt.EndpointHandlingE.EXCLUDE_ENDPOINTS);
			return ans;
		}

		String frmtStr = "%-7s [min=% 1.3f, max=% 1.3f]";
		String frmtHdr = "%-7s [.........., ..........]";
		@Override
		public String toStringLog(String inpPrefix) {
			String prefix = (inpPrefix==null) ? "" : inpPrefix;
			return String.format(prefix + frmtStr, this.name(), eMin, eMax);
		}
		@Override
		public String toStringHdr(String inpPrefix) {
			String prefix = (inpPrefix==null) ? "" : inpPrefix;
			return String.format(prefix + frmtHdr, "name...");
		}
		@Override
		public String toStringNotes() {
			return null;
		}
	}
	
	/**
	 * define axis and range of values that constitute "button pressed"
	 * @author JudiA
	 *
	 */
	protected enum AnalogInputForBtnE implements TmToStringI {
		JS_X_AXIS_LEFT_MAX_NEG(DsConnectionTypesE.JS_FRC_ANALOG, 0 + DsConnectionTypesE.JS_FRC_ANALOG.eMinNdx, AnalogRangeForBtnE.MAX_NEG),
		JS_X_AXIS_MID(DsConnectionTypesE.JS_FRC_ANALOG, 0 + DsConnectionTypesE.JS_FRC_ANALOG.eMinNdx, AnalogRangeForBtnE.MID),
		JS_X_AXIS_RIGHT_MAX_POS(DsConnectionTypesE.JS_FRC_ANALOG, 0 + DsConnectionTypesE.JS_FRC_ANALOG.eMinNdx, AnalogRangeForBtnE.MAX_POS),
		
		JS_Y_AXIS_FRWD_MAX_NEG(DsConnectionTypesE.JS_FRC_ANALOG, 1 + DsConnectionTypesE.JS_FRC_ANALOG.eMinNdx, AnalogRangeForBtnE.MAX_NEG),
		JS_Y_AXIS_MID(DsConnectionTypesE.JS_FRC_ANALOG, 1 + DsConnectionTypesE.JS_FRC_ANALOG.eMinNdx, AnalogRangeForBtnE.MID),
		JS_Y_AXIS_BACK_MAX_POS(DsConnectionTypesE.JS_FRC_ANALOG, 1 + DsConnectionTypesE.JS_FRC_ANALOG.eMinNdx, AnalogRangeForBtnE.MAX_POS),
		
		JS_Z_ROLLER_AXIS_UP_MAX_NEG(DsConnectionTypesE.JS_FRC_ANALOG, 2 + DsConnectionTypesE.JS_FRC_ANALOG.eMinNdx, AnalogRangeForBtnE.MAX_NEG),
		JS_Z_ROLLER_AXIS_MID(DsConnectionTypesE.JS_FRC_ANALOG, 2 + DsConnectionTypesE.JS_FRC_ANALOG.eMinNdx, AnalogRangeForBtnE.MID),
		JS_Z_ROLLER_AXIS_DOWN_MAX_POS(DsConnectionTypesE.JS_FRC_ANALOG, 2 + DsConnectionTypesE.JS_FRC_ANALOG.eMinNdx, AnalogRangeForBtnE.MAX_POS),
		
		//Attention!!! DsCnst.JS_TM_ANALOG_BUTTON_CNT should match AnalogInputForBtnE.values().length)

		;
		
		private DsConnectionTypesE eConnType;
		private int eAnalogAxisId;
		private AnalogRangeForBtnE eRange; //here for reference only, code should use eMin/eMax directly
		private double eMin;
		private double eMax;

		private AnalogInputForBtnE(DsConnectionTypesE connType, int frcNdx, AnalogRangeForBtnE range) {
			eConnType = connType;
			eAnalogAxisId = frcNdx;
			eRange = range;
			if( ! (eRange==null)) {
				eMin = eRange.eMin;
				eMax = eRange.eMax;
			} else {
				eMin = AnalogRangeForBtnE.NONE.eMin;
				eMax = AnalogRangeForBtnE.NONE.eMax;
			}
		}
		public int getAnalogAxisId() { return eAnalogAxisId; }
		public AnalogRangeForBtnE getAnalogRangeForBtn() { return eRange; }


		String frmtStr = "%-29s [%-19s, axisNdx=%2d, min: % 1.3f, max: % 1.3f]";
		String frmtHdr = "%-29s [%-19s, .........., ..........., ...........]";
		public String toStringLog(String inpPrefix) {
			String prefix = (inpPrefix==null) ? "" : inpPrefix;
			String ans = String.format(prefix + frmtStr, 
				this.name(), ((eConnType==null) ? "null" : eConnType.name()), eAnalogAxisId, eMin, eMax);
			return ans;
		}
		@Override
		public String toStringHdr(String inpPrefix) {
			String prefix = (inpPrefix==null) ? "" : inpPrefix;
			String ans = String.format(prefix + frmtHdr, 
					".....name.....", eConnType.getClass().getSimpleName());
				return ans;
		}
		@Override
		public String toStringNotes() { return null; }
	}
	
	
	public static enum DsNamedConnectionsE implements TmToStringI, TmEnumWithListI<DsConnectionsEntry> {
    	DS_USB0(DsConnectionTypesE.USB_PORT, 0 + DsCnst.kFIRST_USB_PORT_NUMBER),
    	DS_USB1(DsConnectionTypesE.USB_PORT, 1 + DsCnst.kFIRST_USB_PORT_NUMBER),
    	DS_USB2(DsConnectionTypesE.USB_PORT, 2 + DsCnst.kFIRST_USB_PORT_NUMBER),
    	DS_USB3(DsConnectionTypesE.USB_PORT, 3 + DsCnst.kFIRST_USB_PORT_NUMBER),
    	DS_USB4(DsConnectionTypesE.USB_PORT, 4 + DsCnst.kFIRST_USB_PORT_NUMBER),
    	DS_USB5(DsConnectionTypesE.USB_PORT, 5 + DsCnst.kFIRST_USB_PORT_NUMBER),

    	//these are button numbers to feed to FRC code, paired with the applicable device type
    	TOP_BUT1_TRIGGER(DsConnectionTypesE.JS_FRC_BUTTON, 0 + DsConnectionTypesE.JS_FRC_BUTTON.eMinNdx),
//    	TOP_BUT2_FRONT(DsConnectionTypesE.JS_FRC_BUTTON,  5 + 1 + DsConnectionTypesE.JS_FRC_BUTTON.eMinNdx), //err for testing
    	TOP_BUT2_FRONT(DsConnectionTypesE.JS_FRC_BUTTON, 1 + DsConnectionTypesE.JS_FRC_BUTTON.eMinNdx), //err for testing
    	TOP_BUT3_BACK(DsConnectionTypesE.JS_FRC_BUTTON,  2 + DsConnectionTypesE.JS_FRC_BUTTON.eMinNdx),
    	TOP_BUT4_LEFT(DsConnectionTypesE.JS_FRC_BUTTON,  3 + DsConnectionTypesE.JS_FRC_BUTTON.eMinNdx),
    	TOP_BUT5_RIGHT(DsConnectionTypesE.JS_FRC_BUTTON,  4 + DsConnectionTypesE.JS_FRC_BUTTON.eMinNdx),
    	BASE_BUT6_LEFT_BACK(DsConnectionTypesE.JS_FRC_BUTTON,  5 + DsConnectionTypesE.JS_FRC_BUTTON.eMinNdx),
    	BASE_BUT7_LEFT_FRONT(DsConnectionTypesE.JS_FRC_BUTTON,  6 + DsConnectionTypesE.JS_FRC_BUTTON.eMinNdx),
    	BASE_BUT8_FRONT_LEFT(DsConnectionTypesE.JS_FRC_BUTTON,  7 + DsConnectionTypesE.JS_FRC_BUTTON.eMinNdx),
    	BASE_BUT9_FRONT_RIGHT(DsConnectionTypesE.JS_FRC_BUTTON,  8 + DsConnectionTypesE.JS_FRC_BUTTON.eMinNdx),
    	BASE_BUT10_RIGHT_FRONT(DsConnectionTypesE.JS_FRC_BUTTON,  9 + DsConnectionTypesE.JS_FRC_BUTTON.eMinNdx),
    	BASE_BUT11_RIGHT_BACK(DsConnectionTypesE.JS_FRC_BUTTON,  10 + DsConnectionTypesE.JS_FRC_BUTTON.eMinNdx),
    	X_AXIS(DsConnectionTypesE.JS_FRC_ANALOG, 0 + DsConnectionTypesE.JS_FRC_ANALOG.eMinNdx), //-1 when to left, +1 when to right??
    	Y_AXIS(DsConnectionTypesE.JS_FRC_ANALOG, 1 + DsConnectionTypesE.JS_FRC_ANALOG.eMinNdx),
    	Z_ROLLER(DsConnectionTypesE.JS_FRC_ANALOG, 2 + DsConnectionTypesE.JS_FRC_ANALOG.eMinNdx),
    	TWIST(DsConnectionTypesE.JS_FRC_ANALOG, 3 + DsConnectionTypesE.JS_FRC_ANALOG.eMinNdx),  //TODO see Joystick class kDefaultTwistAxis: twist=Z-roller?
    	THROTTLE(DsConnectionTypesE.JS_FRC_ANALOG, 4 + DsConnectionTypesE.JS_FRC_ANALOG.eMinNdx),

        A_BUTTON(DsConnectionTypesE.XBOX_FRC_BUTTON, 0 + DsConnectionTypesE.XBOX_FRC_BUTTON.eMinNdx),
        B_BUTTON(DsConnectionTypesE.XBOX_FRC_BUTTON, 1 + DsConnectionTypesE.XBOX_FRC_BUTTON.eMinNdx),
        X_BUTTON(DsConnectionTypesE.XBOX_FRC_BUTTON, 2 + DsConnectionTypesE.XBOX_FRC_BUTTON.eMinNdx),
        Y_BUTTON(DsConnectionTypesE.XBOX_FRC_BUTTON, 3 + DsConnectionTypesE.XBOX_FRC_BUTTON.eMinNdx),
        LB_BUTTON(DsConnectionTypesE.XBOX_FRC_BUTTON, 4 + DsConnectionTypesE.XBOX_FRC_BUTTON.eMinNdx),
        RB_BUTTON(DsConnectionTypesE.XBOX_FRC_BUTTON, 5 + DsConnectionTypesE.XBOX_FRC_BUTTON.eMinNdx),
        BACK_BUTTON(DsConnectionTypesE.XBOX_FRC_BUTTON, 6 + DsConnectionTypesE.XBOX_FRC_BUTTON.eMinNdx),
        START_BUTTON(DsConnectionTypesE.XBOX_FRC_BUTTON, 7 + DsConnectionTypesE.XBOX_FRC_BUTTON.eMinNdx),
        LEFT_JOY_BUTTON(DsConnectionTypesE.XBOX_FRC_BUTTON, 8 + DsConnectionTypesE.XBOX_FRC_BUTTON.eMinNdx),
        RIGHT_JOY_BUTTON(DsConnectionTypesE.XBOX_FRC_BUTTON, 9 + DsConnectionTypesE.XBOX_FRC_BUTTON.eMinNdx),
        
        /*
         * left trigger returns a variable positive value.
         * right trigger returns a variable negative value.
         * y values are from -1.0 (pushed forward all the way) to +1.0 (pulled back all the way)
         * x values are from -1.0 (pushed forward all the way to left) to +1.0 (pushed all the way to right)
         */
    	LEFT_JOY_X(DsConnectionTypesE.XBOX_FRC_ANALOG, 0 + DsConnectionTypesE.XBOX_FRC_ANALOG.eMinNdx),
    	LEFT_JOY_Y(DsConnectionTypesE.XBOX_FRC_ANALOG, 1 + DsConnectionTypesE.XBOX_FRC_ANALOG.eMinNdx),
    	LEFT_TRIGGER(DsConnectionTypesE.XBOX_FRC_ANALOG, 2 + DsConnectionTypesE.XBOX_FRC_ANALOG.eMinNdx),
    	RIGHT_TRIGGER(DsConnectionTypesE.XBOX_FRC_ANALOG, 3 + DsConnectionTypesE.XBOX_FRC_ANALOG.eMinNdx),
    	RIGHT_JOY_X(DsConnectionTypesE.XBOX_FRC_ANALOG, 4 + DsConnectionTypesE.XBOX_FRC_ANALOG.eMinNdx),
    	RIGHT_JOY_Y(DsConnectionTypesE.XBOX_FRC_ANALOG, 5 + DsConnectionTypesE.XBOX_FRC_ANALOG.eMinNdx),
    	
    	POV(DsConnectionTypesE.XBOX_FRC_POV, 0 + DsConnectionTypesE.XBOX_FRC_POV.eMinNdx),
    	
    	//there is one POV on the Xbox or GamePad controller. When read, it returns
    	//an integer value indicating an angle.  The following all use
    	//the same POV, but will be true only if it matches the designated
    	//angle.
        POV_NORTH_BUTTON(DsConnectionTypesE.XBOX_TM_CANNED_POV_BUTTON, PovInputForBtnE.POV0_NORTH, POV), // 0 degrees
        POV_SOUTH_BUTTON(DsConnectionTypesE.XBOX_TM_CANNED_POV_BUTTON, PovInputForBtnE.POV0_SOUTH, POV), // 180 degrees
        POV_EAST_BUTTON(DsConnectionTypesE.XBOX_TM_CANNED_POV_BUTTON, PovInputForBtnE.POV0_EAST, POV), // 90 degrees
        POV_WEST_BUTTON(DsConnectionTypesE.XBOX_TM_CANNED_POV_BUTTON, PovInputForBtnE.POV0_WEST, POV), // 270 degrees
        
        //these are hard to hit accurately and probably shouldn't be used
        //except for lab test purposes
        POV_NE_BUTTON(DsConnectionTypesE.XBOX_TM_CANNED_POV_BUTTON, PovInputForBtnE.POV0_NE, POV),
        POV_SE_BUTTON(DsConnectionTypesE.XBOX_TM_CANNED_POV_BUTTON, PovInputForBtnE.POV0_SE, POV),
        POV_NW_BUTTON(DsConnectionTypesE.XBOX_TM_CANNED_POV_BUTTON, PovInputForBtnE.POV0_NW, POV),
        POV_SW_BUTTON(DsConnectionTypesE.XBOX_TM_CANNED_POV_BUTTON, PovInputForBtnE.POV0_SW, POV),
        
        JOY_Z_UP_AS_BUTTON(DsConnectionTypesE.JS_TM_CANNED_ANALOG_BUTTON, AnalogInputForBtnE.JS_Z_ROLLER_AXIS_UP_MAX_NEG, Z_ROLLER),
        JOY_Z_MID_AS_BUTTON(DsConnectionTypesE.JS_TM_CANNED_ANALOG_BUTTON, AnalogInputForBtnE.JS_Z_ROLLER_AXIS_MID, Z_ROLLER),
        JOY_Z_DOWN_AS_BUTTON(DsConnectionTypesE.JS_TM_CANNED_ANALOG_BUTTON, AnalogInputForBtnE.JS_Z_ROLLER_AXIS_DOWN_MAX_POS, Z_ROLLER),

//TBD        LEFT_JOY_Y_UP_AS_BUTTON(DsConnectionTypesE.XBOX_TM_CANNED_ANALOG_BUTTON, AnalogInputForBtnE.JS_Z_ROLLER_AXIS_UP_MAX_NEG, Z_ROLLER),

    	;
		
		//let DsConnectionsEntry log inputs, check validity, etc. This enum
		//entry is used only to get the index for corresponding DsConnectionsEntry
		//element in the master list.
    	private DsNamedConnectionsE(DsConnectionTypesE connType, int frcNdx) {
    		getList().add(this.ordinal(), getInstance().new DsConnectionsEntry(this, connType, frcNdx));
    	}
    	private DsNamedConnectionsE(DsConnectionTypesE connType, AnalogInputForBtnE analogInfo) {
    		getList().add(this.ordinal(), getInstance().new DsConnectionsEntry(this, connType, analogInfo));
    	}
       	private DsNamedConnectionsE(DsConnectionTypesE connType, PovInputForBtnE povInfo) {
       		getList().add(this.ordinal(), getInstance().new DsConnectionsEntry(this, connType, povInfo));
    	}
    	private DsNamedConnectionsE(DsConnectionTypesE connType, AnalogInputForBtnE analogInfo, DsNamedConnectionsE dupOf) {
    		getList().add(this.ordinal(), getInstance().new DsConnectionsEntry(this, connType, analogInfo, dupOf));
    	}
       	private DsNamedConnectionsE(DsConnectionTypesE connType, PovInputForBtnE povInfo, DsNamedConnectionsE dupOf) {
       		getList().add(this.ordinal(), getInstance().new DsConnectionsEntry(this, connType, povInfo, dupOf));
    	}
       	
       	@Override
       	public List<DsConnectionsEntry> getList() { return dsNamedConnectionsList; }
       	public static List<DsConnectionsEntry> staticGetList() { return dsNamedConnectionsList; }
       	@Override
       	public DsConnectionsEntry getEnt() { return dsNamedConnectionsList.get(this.ordinal()); }
       	@Override
       	public String getEnumClassName() { return this.getClass().getSimpleName(); }
       	@Override
       	public String getListEntryClassName() { return DsConnectionsEntry.class.getSimpleName(); }
       	
       	String frmtStr = "%-22s [ListEntry[%2d]: %s]";
       	String frmtHdr = "%-22s [               %s]";
       	public String toStringLog(String inpPrefix) {
       		String prefix = (inpPrefix==null) ? "" : inpPrefix;
       		String ans = String.format(prefix + frmtStr, this.name(), this.ordinal(),
       				getList().get(this.ordinal()).toStringLog());
       		return ans;
       	}
		@Override
		public String toStringHdr(String inpPrefix) {
			String prefix = (inpPrefix==null) ? "" : inpPrefix;
			String listHdr = getList().get(0).toStringHdr();
       		return (listHdr==null ? null : String.format(prefix + frmtHdr, "....name....", listHdr));
		}
		@Override
		public String toStringNotes() {
			return(getEnumClassName() + " - saves no info in enum (see List<" + getListEntryClassName() +
					"> dsNamedConnectionsList and enum's getList() and getEnt() methods)");
		}

	}
	
	protected static class DsConnAndIndexAssignments implements TmToolsI, TmForcedInstantiateI {

		/*---------------------------------------------------------
		 * getInstance stuff                                      
		 *---------------------------------------------------------*/
		/** 
		 * handle making the singleton instance of this class and giving
		 * others access to it
		 */
		private static DsConnAndIndexAssignments m_instance;
		public static synchronized DsConnAndIndexAssignments getInstance()
		{
			if(m_instance == null){ m_instance = new DsConnAndIndexAssignments(); }
			return m_instance;
		}
		private DsConnAndIndexAssignments() {
			if ( ! (m_instance == null)) {
				P.println("Error!!! DsConnAndIndexAssignments.m_instance is being modified!!");
				P.println("         was: " + m_instance.toString());
				P.println("         now: " + this.toString());
			}
			m_instance = this;
		}

		/*----------------end of getInstance stuff----------------*/

		@Override
		public void doForcedInstantiation() {
			if(perConnTypeList.size()==0) { //only want to do this one time
				for(DsConnectionTypesE e : DsConnectionTypesE.values()) {
					perConnTypeList.add(e.ordinal(), new PerConnTypeEntry(e));
				}
			}
		}

		@Override
		public void doPopulate() {
			for(DsConnectionsEntry ent : DsNamedConnectionsE.staticGetList()) {
				boolean isOk = true;
				isOk = assign(ent.c_namedConnection, ent.c_dupOf);
				if( ! isOk) {
					ent.c_isValid = false;
				}
			}
			if(getNumberOfBadAssignmentsDetected() > 0) {
				throw TmExceptions.getInstance().new DsAssignmentErrorsDetectedEx("Assignment errors found while populating " + 
                        DsNamedConnectionsE.staticGetList().get(0).getNamedConnection().getClass().getSimpleName() +
						"\n      for: " + getAssignmentTroublemakers());
			}
		}
		
		protected List<PerConnTypeEntry> perConnTypeList = new ArrayList<>();
		
		int badAssignmentsCount = 0;
		public int getNumberOfBadAssignmentsDetected() { return badAssignmentsCount; }
		List<DsNamedConnectionsE> namedConnAssignmentErrorsList = new ArrayList<>();
		protected void logBadAssignment(DsNamedConnectionsE namedConn) {
			if( ! namedConnAssignmentErrorsList.contains(namedConn)) {
				namedConnAssignmentErrorsList.add(namedConn);			
				badAssignmentsCount++;
			}
		}
		protected String getAssignmentTroublemakers() {
			String ans = "";
			for(DsNamedConnectionsE n : namedConnAssignmentErrorsList) {
				ans += n.name() + ", ";
			}
			return ans;
		}

		/**
		 * 
		 * @param namedConn
		 * @return true if no problems with assignment
		 */
		public boolean assign(DsNamedConnectionsE namedConn) {
			return assign(namedConn, null);
		}
		/**
		 * 
		 * @param namedConn
		 * @param dupOf - indicates that namedConn is a legitimate duplication of dupOf
		 * @return true if no problems with assignment
		 */
		public boolean assign(DsNamedConnectionsE namedConn, DsNamedConnectionsE dupOf) {
			DsConnectionsEntry connEnt = namedConn.getEnt(); //getDsNamedConnectionsList().get(namedConn.ordinal());
			
			DsConnectionTypesE connType = connEnt.c_connType;
			
	 		int connNdx = connEnt.getConnectionFrcIndex();
	 		boolean nestedAssignOk = true;
	 		if(connNdx == DsCnst.INVALID_DS_HDWR_CONNECTION_INDEX) {
				switch(connType) {
				case JS_TM_CANNED_ANALOG_BUTTON:
					connNdx = connEnt.c_analogBtnInfo.ordinal() + connType.eMinNdx;
					nestedAssignOk = assign(namedConn, connEnt.c_analogBtnInfo.eConnType, connEnt.c_analogBtnInfo.eAnalogAxisId, dupOf);
					break;
				case XBOX_TM_CANNED_POV_BUTTON:
					connNdx = connEnt.c_povBtnInfo.ordinal() + connType.eMinNdx;
					nestedAssignOk = assign(namedConn, connEnt.c_povBtnInfo.eConnType, connEnt.c_povBtnInfo.eFrcPovNdx, dupOf);
					break;
				case JS_FRC_ANALOG:
				case JS_FRC_BUTTON:
				case USB_PORT:
				case XBOX_FRC_ANALOG:
				case XBOX_FRC_BUTTON:
				case XBOX_FRC_POV:
					break;
				default:
					throw TmExceptions.getInstance().new DsUnknownConnIndexFoundDuringAssignmentEx(namedConn.name());
					//break;				
				}

	 		}
	 		return nestedAssignOk && assign(namedConn, connType, connNdx, dupOf);
		}
		private boolean assign(DsNamedConnectionsE namedConn, DsConnectionTypesE connType, int connNdx, DsNamedConnectionsE dupOf) {
			boolean ans = true;
						
			P.println(PrtYn.N, "Logging connType/connNdx for " + namedConn.name() + " (connType " + connType.name() + ")");
			if(namedConn.equals(DsNamedConnectionsE.POV_NORTH_BUTTON)) {
				int junk = 5; //just a good debugger breakpoint
			}
			
			PerConnTypeEntry perConnTypeEnt = perConnTypeList.get(connType.ordinal());
			PerConnTypeIndexNbrEntry perConnTypeIndexNbrEnt = perConnTypeEnt.perConnTypeIndexNbrList.get(connNdx - connType.eMinNdx);
			List<DsNamedConnectionsE> assignedList = perConnTypeIndexNbrEnt.indexNbrAssignmentList; //.perConnTypeIndexNbrAssignmentsList;
			if(assignedList.size() > 0) {
				if(dupOf==null) {
					logMultipleAssignments(namedConn, connType, connNdx, dupOf, assignedList);
					ans = false;
				}
				else if( ! dupOf.equals(assignedList.get(0))) {
					logMultipleAssignments(namedConn, connType, connNdx, dupOf, assignedList);
					ans = false;
				}
			}
			assignedList.add(namedConn);
			return ans;
		}

		public void logMultipleAssignments(DsNamedConnectionsE namedConn, DsConnectionTypesE connType, 
							int connNdx, DsNamedConnectionsE dupOf, List<DsNamedConnectionsE> list) {
			logBadAssignment(namedConn);
			P.println("Warning!!: multiple assignments of " + connType.toString() + " ndx " + connNdx + ":");
			String msg = "";
			for(DsNamedConnectionsE ent : list) {
				msg += ent.toString() + ", ";
				logBadAssignment(ent);
			}
			msg += namedConn.toString();
			P.println("           " + msg);
		}

		
		protected class PerConnTypeEntry {
			List<PerConnTypeIndexNbrEntry> perConnTypeIndexNbrList = new ArrayList<>();
			
			protected PerConnTypeEntry(DsConnectionTypesE connType) {
				for(int ndx=connType.eMinNdx; ndx<(connType.eMinNdx+connType.eCount); ndx++) {
					perConnTypeIndexNbrList.add((ndx - connType.eMinNdx), new PerConnTypeIndexNbrEntry(connType, ndx));
				}
			}
		}
		
		protected class PerConnTypeIndexNbrEntry {
			public DsConnectionTypesE connType;
			public int connTypeIndexNbr;
			
			List<DsNamedConnectionsE> indexNbrAssignmentList = new ArrayList<DsNamedConnectionsE>();
			
			protected PerConnTypeIndexNbrEntry(DsConnectionTypesE aConnType, int perIndexNbrListIndex) {
				connType = aConnType;
				connTypeIndexNbr = perIndexNbrListIndex + connType.eMinNdx;
			}
		}
		
	}
	
	public static class DsCnst {
		public static final int INVALID_DS_HDWR_CONNECTION_INDEX = -1;
		public static final double DEFAULT_JOYSTICK_DEADZONE_TOLERANCE = 0.07;
		public static final int DEFAULT_POV_DEADZONE_TOLERANCE = 0;
		
		public static final int kFIRST_USB_PORT_NUMBER = 0;
		public static final int kUSB_PORT_CNT = 6;

		//JS stands for joystick, GC stands for game controller (Xbox or Gamepad)
		//FRC stands for FRC code, TM stands for Team 744 code
		private static final int	JS_FRC_BUTTON_BASE_NBR	=  1;
		public static final int		JS_FRC_BUTTON_CNT = 11;
		private static final int	JS_TM_BUTTON_BASE_NBR = JS_FRC_BUTTON_BASE_NBR;
		private static final int	JS_TM_BUTTON_MAX_NBR = JS_FRC_BUTTON_BASE_NBR + JS_FRC_BUTTON_CNT - 1;
		private static final int    GC_FRC_BUTTON_BASE_NBR = 1;
		public static final int     GC_FRC_BUTTON_CNT = 10;
		private static final int 	GC_TM_BUTTON_BASE_NBR = Math.max(20, JS_TM_BUTTON_BASE_NBR + JS_FRC_BUTTON_CNT);
		private static final int 	GC_TM_BUTTON_MAX_NBR = GC_TM_BUTTON_BASE_NBR + GC_FRC_BUTTON_CNT - 1;
		
		//analog inputs are used for axis readings, etc.
		private static final int	JS_FRC_ANALOG_BASE_NBR = 0;
		private static final int 	JS_FRC_ANALOG_CNT = 5;
		private static final int    JS_TM_ANALOG_BASE_NBR = Math.max(40, GC_TM_BUTTON_BASE_NBR + GC_FRC_BUTTON_CNT);
		private static final int    JS_TM_ANALOG_MAX_NBR = JS_TM_ANALOG_BASE_NBR + JS_FRC_ANALOG_CNT - 1;
		private static final int	GC_FRC_ANALOG_BASE_NBR = 0;
		private static final int 	GC_FRC_ANALOG_CNT = 6;
		private static final int    GC_TM_ANALOG_BASE_NBR = Math.max(50, JS_TM_ANALOG_BASE_NBR + JS_FRC_ANALOG_CNT);
		private static final int    GC_TM_ANALOG_MAX_NBR = GC_TM_ANALOG_BASE_NBR + GC_FRC_ANALOG_CNT - 1;
		
		//POV "buttons" check for particular angle readings from a POV
		private static final int 	JS_TM_POV_BUTTON_CNT = 0;
		private static final int    JS_TM_POV_BUTTON_BASE_NBR = Math.max(60, GC_TM_ANALOG_BASE_NBR + GC_FRC_ANALOG_CNT);
		private static final int 	JS_TM_POV_BUTTON_MAX_NBR = JS_TM_POV_BUTTON_BASE_NBR + 0;

		//GC_TM_POV_BUTTON_CNT should match PovInputForBtnE.values().length
		private static final int 	GC_TM_POV_BUTTON_CNT = 8;
		private static final int    GC_TM_POV_BUTTON_BASE_NBR = Math.max(70, JS_TM_POV_BUTTON_BASE_NBR + JS_TM_POV_BUTTON_CNT);
		private static final int    GC_TM_POV_BUTTON_MAX_NBR = GC_TM_POV_BUTTON_BASE_NBR + GC_TM_POV_BUTTON_CNT - 1;

        //a POV returns an analog value representing the angle of the edge that's being pressed (or -1 if not pressed)
		private static final int    JS_FRC_POV_BASE_NBR = 0;
		private static final int    JS_FRC_POV_CNT = 0;
		private static final int    JS_TM_POV_BASE_NBR = Math.max(80, GC_TM_POV_BUTTON_BASE_NBR + GC_TM_POV_BUTTON_CNT);
		private static final int    JS_TM_POV_MAX_NBR = JS_TM_POV_BASE_NBR + 0;
		private static final int    GC_FRC_POV_BASE_NBR = 0;
		private static final int    GC_FRC_POV_CNT = 1;
		private static final int    GC_TM_POV_BASE_NBR = Math.max(88, JS_TM_POV_BASE_NBR + JS_FRC_POV_CNT);
		private static final int    GC_TM_POV_MAX_NBR = GC_TM_POV_BASE_NBR + GC_FRC_POV_CNT - 1;
		
		//analog "buttons" return TRUE if the analog value read is within a specified range, FALSE else.
		//they're a Team 744 thing, not something FRC implements
		//JS_TM_ANALOG_BUTTON_CNT should match AnalogInputForBtnE.values().length)
		private static final int	JS_TM_ANALOG_BUTTON_CNT = 9; //Use Z-axis, X-axis, and Y-axis for three different "buttons" each //old: //X, Y, Z only axes only //for now, assume one button per analog, though could have more...
		private static final int	JS_TM_ANALOG_BUTTON_BASE_NBR = Math.max(90, GC_TM_POV_BASE_NBR + GC_FRC_POV_CNT);
		private static final int    JS_TM_ANALOG_BUTTON_MAX_NBR = JS_TM_ANALOG_BUTTON_BASE_NBR + JS_TM_ANALOG_BUTTON_CNT - 1;
		private static final int	GC_TM_ANALOG_BUTTON_CNT = 0; //4; //two joysticks each with X and Y axes; //for now, assume one button per analog, though could have more...
		private static final int	GC_TM_ANALOG_BUTTON_BASE_NBR = Math.max(100, JS_TM_ANALOG_BUTTON_BASE_NBR + JS_TM_ANALOG_BUTTON_CNT);
		private static final int    GC_TM_ANALOG_BUTTON_MAX_NBR = GC_TM_ANALOG_BUTTON_BASE_NBR + GC_TM_ANALOG_BUTTON_CNT - 1;

	}
	
	public static class FrcCnst { //constants used in FRC code
		public static final int POV_ANGLE_POV_NOT_PRESSED = -1;
		public static final boolean BUTTON_READING_FOR_INVALID_BUTTONS = false;
	}
	
	


	protected static List<DsConnectionsEntry> dsNamedConnectionsList = new ArrayList<DsConnectionsEntry>();
	
	public class DsConnectionsEntry implements TmToStringI, TmListBackingEnumI<DsConnectionsEntry, DsNamedConnectionsE> {
		
		@Override
		public List<DsConnectionsEntry> getListBackingEnum() { return dsNamedConnectionsList; }
		
		protected DsNamedConnectionsE c_namedConnection = null;
		protected DsNamedConnectionsE c_dupOf = null;
		
		protected DsConnectionTypesE c_connType = null;
		protected int c_frcIndex = DsCnst.INVALID_DS_HDWR_CONNECTION_INDEX;
		protected AnalogInputForBtnE c_analogBtnInfo = null;
		protected PovInputForBtnE c_povBtnInfo = null;
		protected boolean c_isValid = false;
		
		public DsNamedConnectionsE getNamedConnection() { return c_namedConnection; }
		public DsNamedConnectionsE getConnectionDupOf() { return c_dupOf; }
		public DsDeviceTypesE getConnectionDeviceType() { 
			return ((c_connType==null) ? null : c_connType.eDevType);
		}
		public DsConnectionTypesE getConnectionType() { return c_connType; }
		public int getConnectionFrcIndex() { return c_frcIndex; }
		public boolean isValid() { return c_isValid; }
		
		private void logValidity(boolean validity) {
			c_isValid = validity;
			if( ! validity) { //we'll check for assignment problems later
				throw TmExceptions.getInstance().new InvalidDsConnectionsEntryEx(this.toStringLog());
			}
		}
		
		protected boolean checkIfValidFrcIndex(DsConnectionTypesE connType, int frcNdx) {
			boolean ans = false;
			if( ! (connType==null)) {
				if(connType.eCount > 0) {
					if(frcNdx >= connType.eMinNdx && frcNdx < (connType.eMinNdx + connType.eCount)) {
						ans = true;
					}
				}
			}
			return ans;
		}

		private TmHdwrItemEnableDisableMgr cHdwrItemEnableDisableMgr = new TmHdwrItemEnableDisableMgr();

		public TmHdwrItemEnableDisableMgr getHdwrItemEnableDisableMgr() { return cHdwrItemEnableDisableMgr; }

		
		@SuppressWarnings("unused")
		private DsConnectionsEntry() {} //private so can never be called

		public DsConnectionsEntry(DsNamedConnectionsE namedConn, DsConnectionTypesE connType, int frcNdx) {
    		c_namedConnection = namedConn;
    		c_connType = connType;
    		c_frcIndex = frcNdx;
    		boolean valid = false;
    		if(( ! (c_namedConnection==null)) && ( ! (c_connType==null))) {
    			valid = checkIfValidFrcIndex(c_connType, c_frcIndex);
    		}
    		logValidity(valid);
    	}
		public DsConnectionsEntry(DsNamedConnectionsE namedConn, DsConnectionTypesE connType,
				AnalogInputForBtnE analogInfo) {
			this(namedConn, connType, analogInfo, null);
		}
		public DsConnectionsEntry(DsNamedConnectionsE namedConn, DsConnectionTypesE connType,
										AnalogInputForBtnE analogInfo, DsNamedConnectionsE dupOf) {
    		c_namedConnection = namedConn;
    	 	c_dupOf = dupOf;
    		c_connType = connType;
    		c_analogBtnInfo = analogInfo;
    		boolean valid = false;
    		if(( ! (c_namedConnection==null)) && ( ! (c_connType==null)) && ( ! (c_analogBtnInfo==null))) {
    			switch(c_connType) {
    			case JS_TM_CANNED_ANALOG_BUTTON:
    				switch(c_analogBtnInfo.eConnType) {
    				case JS_FRC_ANALOG:
        				valid = checkIfValidFrcIndex(c_analogBtnInfo.eConnType, c_analogBtnInfo.eAnalogAxisId);
        				break;
    				default:
    					valid = false;
    					break;
    				}
    				break;
    			default:
    				valid = false;
    				break;
    			}

    		}
    		logValidity(valid);
		}
		public DsConnectionsEntry(DsNamedConnectionsE namedConn, DsConnectionTypesE connType,
				PovInputForBtnE povInfo) {
			this(namedConn, connType, povInfo, null);
		}
		public DsConnectionsEntry(DsNamedConnectionsE namedConn, DsConnectionTypesE connType,
									PovInputForBtnE povInfo, DsNamedConnectionsE dupOf) {
    		c_namedConnection = namedConn;
    	 	c_dupOf = dupOf;
    		c_connType = connType;
    		c_povBtnInfo = povInfo;
    		boolean valid = false;
    		if(( ! (c_namedConnection==null)) && ( ! (c_connType==null)) && ( ! (c_povBtnInfo==null))) {
    			switch(c_connType) {
    			case XBOX_TM_CANNED_POV_BUTTON:
    				switch(c_povBtnInfo.eConnType) {
    				case XBOX_FRC_POV:
        				valid = checkIfValidFrcIndex(c_povBtnInfo.eConnType, c_povBtnInfo.eFrcPovNdx);
        				break;
    				default:
    					valid = false;
    					break;
    				}
    				break;
    			default:
    				valid = false;
    				break;
    			}
    		}
    		logValidity(valid);
		}
		
		
		
		String frmtStr = "%-22s, %-14s, %-26s, ndx=%2d, %-22s, valid=%-5s"; //, spi=%-11s, i2c=%-8s, mxp=%-24s, ip=%-10s, valid=%-5s",

		String frmtHdr = "%-22s, %-14s, %-26s, ......, %-22s, .........."; //, %-15s, %-12s, %-28s, %-10s, .........";
		String frmtJABStr = "[%s]";
		String frmtJABHdr = "[%s]";
		String frmtXPBStr = "[%s]";
		String frmtXPBHdr = "[%s]";
		@Override
		public String toStringLog(String inpPrefix) {
			String prefix = (inpPrefix==null) ? "" : inpPrefix;
			String ans = String.format(prefix + frmtStr,
					((getNamedConnection()==null) ? " n/a" : getNamedConnection().name()),
					((getConnectionDeviceType()==null) ? " n/a" : getConnectionDeviceType().name()),
					((getConnectionType()==null) ? " n/a" : getConnectionType().name()),
					getConnectionFrcIndex(),
					((getConnectionDupOf()==null) ? "" : getConnectionDupOf().name()),
//					((getConnectionSpiPort()==null) ? "n/a" : getConnectionSpiPort().name()),
//					((getConnectionI2cPort()==null) ? "n/a" : getConnectionI2cPort().name()),
//					((getConnectionMxpPin()==null) ? "n/a" : getConnectionMxpPin().name()),
//					((getConnectionIpAddr()==null) ? "n/a" : getConnectionIpAddr())
					isValid()
					);
			        if( ! (getConnectionType()==null)) {
			        	//DsConnectionTypesE.XBOX_TM_CANNED_POV_BUTTON
			        	switch(getConnectionType()) {
			        	case JS_TM_CANNED_ANALOG_BUTTON:
			        		String addA = String.format(frmtJABStr, c_analogBtnInfo.toStringLog());
			        		ans += addA;
			        		break;
			        	case XBOX_TM_CANNED_POV_BUTTON:
			        		String addP = String.format(frmtXPBStr, c_povBtnInfo.toStringLog());
			        		ans += addP;
			        		break;
			        	default:
			        		break;
			        	}
			        }
			return ans;
		}
		@Override
		public String toStringHdr(String inpPrefix) {
			String prefix = (inpPrefix==null) ? "" : inpPrefix;
			String ans = String.format(prefix + frmtHdr,
					getNamedConnection().getClass().getSimpleName(),
					getConnectionDeviceType().getClass().getSimpleName(),
					getConnectionType().getClass().getSimpleName(),
					getNamedConnection().getClass().getSimpleName() //same type as getConnectionDupOf().getClass().getSimpleName()
//					getConnectionSpiPort().getClass().getSimpleName(),
//					getConnectionI2cPort().getClass().getSimpleName(),
//					getConnectionMxpPin().getClass().getSimpleName(),
//					getConnectionIpAddr().getClass().getSimpleName(),
					);
	        if( ! (getConnectionType()==null)) {
	        	switch(getConnectionType()) {
	        	case JS_TM_CANNED_ANALOG_BUTTON:
	        		String addA = String.format(frmtJABHdr, c_analogBtnInfo.toStringHdr());
	        		ans += addA;
	        		break;
	        	case XBOX_TM_CANNED_POV_BUTTON:
	        		String addP = String.format(frmtXPBHdr, c_povBtnInfo.toStringHdr());
	        		ans += addP;
	        		break;
	        	default:
	        		break;
	        	}
	        }
	        return ans;
		}
		@Override
		public String toStringNotes() {
			return "enum " + getNamedConnection().getClass().getSimpleName() + " initializes List<" + 
					this.getClass().getSimpleName() + "> dsNamedConnectionsList, see getList() and getEnt() methods in enum";
		}
	}


	public void showEverything() {
		if(false) {
		TmToStringI.showEnumEverything(DsDeviceTypesE.values());
		TmToStringI.showEnumEverything(DsControlTypesE.values());
		TmToStringI.showEnumEverything(DsConnectionTypesE.values());
		TmToStringI.showEnumEverything(PovAnglesE.values());
		TmToStringI.showEnumEverything(PovInputForBtnE.values());
		TmToStringI.showEnumEverything(AnalogRangeForBtnE.values());
		TmToStringI.showEnumEverything(AnalogInputForBtnE.values());
		TmToStringI.showEnumEverything(DsNamedConnectionsE.values());
		}
		if(false) { TmToStringI.showListEverything(dsNamedConnectionsList); }
	}

	@Override
	public void doForcedInstantiation() {
		//Access something from each of the enums in this class to force them 
		//(and their related List<> arrays) to be initialized
		//watch that optimization in the compiler doesn't decide to skip these
		DsControlTypesE j1 = DsControlTypesE.kAnalog;
		DsDeviceTypesE j2 = DsDeviceTypesE.COMPUTER;
		DsConnectionTypesE j3 = DsConnectionTypesE.JS_FRC_ANALOG;
		PovAnglesE j4 = PovAnglesE.POV_BOGUS_ANGLE_FOR_NONPOV_INPUTS;
		PovInputForBtnE j5 = PovInputForBtnE.POV0_EAST;
		AnalogRangeForBtnE j6 = AnalogRangeForBtnE.MAX_NEG;
		AnalogInputForBtnE j7 = AnalogInputForBtnE.JS_X_AXIS_LEFT_MAX_NEG;		
		DsNamedConnectionsE j8 = DsNamedConnectionsE.DS_USB0;
		
		DsConnAndIndexAssignments.getInstance().doForcedInstantiation();
		
	}
	
	@Override
	public void doPopulate() {
		DsConnAndIndexAssignments.getInstance().doPopulate(); //logs assignments
	}
}
