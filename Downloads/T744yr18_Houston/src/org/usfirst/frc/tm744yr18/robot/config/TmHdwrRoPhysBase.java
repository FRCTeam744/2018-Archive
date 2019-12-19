package org.usfirst.frc.tm744yr18.robot.config;

import java.util.ArrayList;
import java.util.List;

import org.usfirst.frc.tm744yr18.robot.config.TmHdwrRoCntls.RoNamedControlsE;
import org.usfirst.frc.tm744yr18.robot.config.TmHdwrRoCntls.RoNamedModulesE;
import org.usfirst.frc.tm744yr18.robot.config.TmHdwrRoPhys.RoNamedConnectionsE;
import org.usfirst.frc.tm744yr18.robot.config.TmSdKeysI.SdKeysE;
import org.usfirst.frc.tm744yr18.robot.devices.TmFakeable_CanTalonSrx.EncoderCountsCapabilityE;
import org.usfirst.frc.tm744yr18.robot.devices.TmFakeable_CanTalonSrx.EncoderPolarityE;
import org.usfirst.frc.tm744yr18.robot.exceptions.TmExceptions;
import org.usfirst.frc.tm744yr18.robot.helpers.TmHdwrItemEnableDisableMgr;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmForcedInstantiateI;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmItemAvailabilityI;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmListBackingEnumI;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmToStringI;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmItemAvailabilityI.ItemAvailabilityE;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmItemAvailabilityI.ItemFakeableE;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmToolsI.P;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmToolsI.Tt;

import edu.wpi.first.wpilibj.I2C;
import edu.wpi.first.wpilibj.Relay;
import edu.wpi.first.wpilibj.SPI;

public class TmHdwrRoPhysBase implements TmForcedInstantiateI {

	/*---------------------------------------------------------
	 * getInstance stuff                                      
	 *---------------------------------------------------------*/
	/** 
	 * handle making the singleton instance of this class and giving
	 * others access to it
	 */
	private static TmHdwrRoPhysBase m_instance;

	public static synchronized TmHdwrRoPhysBase getInstance() {
		if (m_instance == null) {
			m_instance = new TmHdwrRoPhysBase();
		}
		return m_instance;
	}

	protected TmHdwrRoPhysBase() {
		if (m_instance == null) {
			m_instance = this;
		} else {
			P.println("Error!!! TmHdwrRoPhyTools.m_instance is being modified!!");
			P.println("         was: " + m_instance.toString());
			P.println("         now: " + this.toString());
			m_instance = this;
		}
	}
	/*----------------end of getInstance stuff----------------*/
	
	TmExceptions m_exceptions = TmExceptions.getInstance();

	/**
	 * 
	 * FIRST provides drive methods that make assumptions about how driver motors are wired up
	 * and adjusts what it thinks are joystick values accordingly.  Gets confusing.  We use
	 * this enum to help sort it out.  It should be used only for joystick-type values.
	 * 
	 * For use with drive motors only.  Indicates how to interpret input from joysticks, etc.
	 * Joysticks' Y-axis gives negative values when pushed away from you.  If we want that action to make a 
	 * motor move forward in order to make the robot move forward, then the code needs to negate the joystick
	 * reading before sending it to the motor as the desired motor output.
	 * If the motor has to be rotating in its reverse direction to make the robot move forward, then 
	 * the code can use the joystick reading as-is.
	 * Usually the main motors on the left side need to run in reverse to move the robot forward while
	 * the main motors on the right side need to run in their forward direction.
	 * @author JudiA
	 *
	 */
	public enum FrcAndJoysticksVsRoDrvMtrDirVsRobotDirE { 
		FOR_FRC_JS_MTR_IN_REVERSE_DIR_FOR_ROBOT_FORWARD(1.0), //mtr in reverse for robot forward -- use normal joystick readings as-is
		FOR_FRC_JS_MTR_IN_FORWARD_DIR_FOR_ROBOT_FORWARD(-1.0), //mtr in forward for robot forward -- need to invert normal joystick readings
		FOR_FRC_JS_NOT_A_DRIVE_MOTOR(0.0);
		public double eMultiplierForJoystickReadings; //probably never used....
		private FrcAndJoysticksVsRoDrvMtrDirVsRobotDirE(double multiplierForDrvMtrPolarity) {
			eMultiplierForJoystickReadings = multiplierForDrvMtrPolarity;
		}
	}
	
	public enum EncoderIncrVsThingDirE {
		ENC_INCREASING_WHEN_THING_MOVING_FORWARD,
		ENC_INCREASING_WHEN_THING_MOVING_BACKWARD,
		ENC_INCREASING_WHEN_THING_MOVING_LEFT,
		ENC_INCREASING_WHEN_THING_MOVING_RIGHT,
		ENC_INCREASING_WHEN_THING_MOVING_UP,
		ENC_INCREASING_WHEN_THING_MOVING_DOWN,
		ENC_INCREASING_WHEN_THING_MOVING_IN,
		ENC_INCREASING_WHEN_THING_MOVING_OUT,
		TBD
		;
	}
	
	public enum MotorPosPercOutVsThingDirE {
		POS_MTR_PERCENT_OUT_MOVES_THING_FORWARD,
		POS_MTR_PERCENT_OUT_MOVES_THING_BACKWARD,
		POS_MTR_PERCENT_OUT_MOVES_THING_LEFT,
		POS_MTR_PERCENT_OUT_MOVES_THING_RIGHT,
		POS_MTR_PERCENT_OUT_MOVES_THING_UP,
		POS_MTR_PERCENT_OUT_MOVES_THING_DOWN,
		POS_MTR_PERCENT_OUT_MOVES_THING_IN,
		POS_MTR_PERCENT_OUT_MOVES_THING_OUT,
		TBD
		;
	}
	
	/**
	 * motor inversion indication. smart motor controllers use this in their internal calculations.
	 * must be coordinated with sensor info used by the controller
	 * @author JudiA
	 *
	 */
	public enum RoMtrInversionE { INVERT_MOTOR, NO_INVERT_MOTOR, UNKNOWN;
	
		public boolean isInvertMotor() { return this.equals(INVERT_MOTOR); }
		
	}
	
	public enum RoMtrContinuousAmpsE {
		MAX_CONTINUOUS_AMPS_40(40),
		MAX_CONTINUOUS_AMPS_30(30),
		MAX_CONTINUOUS_AMPS_25(25),
		MAX_CONTINUOUS_AMPS_20(20),
		MAX_CONTINUOUS_AMPS_DONT_CARE(200)
		;
		public final int value;
		private RoMtrContinuousAmpsE(int val) {value = val;}
	}
	public enum RoMtrPeakAmpsE {
		MAX_PEAK_AMPS_42(42),
		MAX_PEAK_AMPS_32(32),		
		MAX_PEAK_AMPS_25(25),
		MAX_PEAK_AMPS_22(22),
		MAX_PEAK_AMPS_DONT_CARE(200)
		;
		public final int value;
		private RoMtrPeakAmpsE(int val) {value = val;}
	}
	public enum RoMtrPeakAmpsDurationE {
		MAX_PEAK_DURATION_100MS(100),
		;
		public final int value;
		private RoMtrPeakAmpsDurationE(int val) {value = val;}
	}
	
	public enum RoMtrHasEncoderE {HAS_ENCODER, NO_ENCODER, USE_FAKE_ENCODER }

	public enum RoControlCfgStyleE { 
		MANUAL_CFG, //subsystem using the control will handle all allocation, initialization, configuration
//		AUTO_CFG //RoControlsMgr will handle allocation, etc.
	}
	
	public enum RoControlTypesE { CAN_BUS_MOTOR_CONTROLLER, PWM_MOTOR_CONTROLLER, DOUBLE_SOLENOID, OTHER }
	
	public static enum RoModuleConnectionsOnCanBusE { REQUIRES_CAN_BUS_ID_CONNECTION, CAN_BUS_ID_NOT_NEEDED }

	/**
	 * types of modules that might be mounted on a robot
	 * @author JudiA
	 *
	 */
	public static enum RoModuleTypesE implements TmToStringI {
		RIO_MOD(RoModuleConnectionsOnCanBusE.CAN_BUS_ID_NOT_NEEDED), 
		PCM_MOD(RoModuleConnectionsOnCanBusE.CAN_BUS_ID_NOT_NEEDED), 
		CAN_TALON_SRX_MOD(RoModuleConnectionsOnCanBusE.REQUIRES_CAN_BUS_ID_CONNECTION), 
		PWM_TALON_SRX_MOD(RoModuleConnectionsOnCanBusE.CAN_BUS_ID_NOT_NEEDED), 
		PWM_TALON_MOD(RoModuleConnectionsOnCanBusE.CAN_BUS_ID_NOT_NEEDED),
		;
		
		public final RoModuleConnectionsOnCanBusE eCanBusRequirements;
		
		private RoModuleTypesE(RoModuleConnectionsOnCanBusE canBusRequirements) {
			eCanBusRequirements = canBusRequirements;
		}
		
		public boolean isRequiresCanBusConnection() { 
			return eCanBusRequirements.equals(RoModuleConnectionsOnCanBusE.REQUIRES_CAN_BUS_ID_CONNECTION);
		}

		String frmtStr = "%-17s [%-30s]";
		String frmtHdr = "%-17s [%-30s]"; 
		@Override
		public String toStringLog(String inpPrefix) { 
			String prefix = (inpPrefix==null) ? "" : inpPrefix;
			return String.format(prefix + frmtStr, this.name(), eCanBusRequirements.name());
		}
		@Override
		public String toStringHdr(String inpPrefix) { 
			String prefix = (inpPrefix==null) ? "" : inpPrefix;
			return String.format(prefix + frmtStr, this.getClass().getSimpleName(), eCanBusRequirements.getClass().getSimpleName());
		}
		@Override
		public String toStringNotes() { 
			return null; //this.getClass().getSimpleName() + " entries have no associated data";
		}

	}

	/**
	 * types of connections found on various modules
	 * @author JudiA
	 *
	 */
	public static enum RoConnectionTypesE implements TmToStringI {
		//		RIO_MODULE,
		CAN_BUS(Cnst.CAN_ID_MIN, Cnst.CAN_ID_MAX),
		R_AI(Cnst.FRC_RIO_MIN_INDEX_VAL, Cnst.kRIO_AI_CNT), 
//		R_AO(Cnst.FRC_RIO_MIN_INDEX_VAL, Cnst.kRIO_AO_CNT), 
		R_DIO(Cnst.FRC_RIO_MIN_INDEX_VAL, Cnst.kRIO_DIO_CNT), 
		R_PWM(Cnst.FRC_RIO_MIN_INDEX_VAL, Cnst.kRIO_PWM_CNT), 
		R_RELAY(Cnst.FRC_RIO_MIN_INDEX_VAL, Cnst.kRIO_RELAY_CNT), 
		M_AI(Cnst.kRIO_AI_CNT, Cnst.kMXP_AI_CNT), 
		M_AO(Cnst.kRIO_AO_CNT, Cnst.kMXP_AO_CNT), 
		M_DIO(Cnst.kRIO_DIO_CNT, Cnst.kMXP_DIO_CNT), 
		M_PWM(Cnst.kRIO_PWM_CNT, Cnst.kMXP_PWM_CNT), 
//		M_RELAY(Cnst.kRIO_RELAY_CNT, Cnst.kMXP_RELAY_CNT), 
		//		CAN_TALON_SRX_MODULE, CAN_PDP_MODULE, CAN_PCM_MODULE, 
		//		USB_CAMERA, IP_CAMERA, 
		//		R_PWM_TALON_MODULE, M_PWM_TALON_MODULE, //these are here so we can flag these PWM's as SpeedControllers
		R_SPI_PORT, 
		R_I2C_PORT, 
		R_USB_PORT(Cnst.FRC_RIO_MIN_INDEX_VAL, Cnst.kRIO_USB_CNT), //R_ for roboRIO
		M_SPI_PORT, 
		M_I2C_PORT, //M_ for MXP
		PCM_SOLENOID(Cnst.FRC_PCM_MIN_INDEX_VAL, Cnst.kPCM_SOL_CNT),
		//		SPI_DEV, 
		//		I2C_DEV, 
		IP_NETWORK;

		//KEEP!! template for use by code that references this enum
		//		switch(connType) {
		//		case CAN_BUS:
		//		case R_AI: 
		//		case R_AO:
		//		case R_DIO:
		//		case R_PWM:
		//		case R_RELAY: 
		//		case M_AI:
		////	case M_AO:
		//		case M_DIO:
		//		case M_PWM:
		////	case M_RELAY:
		//		case R_SPI_PORT: 
		//		case R_I2C_PORT: 
		//		case R_USB_PORT: //R_ for roboRIO
		//		case M_SPI_PORT: 
		//		case M_I2C_PORT: //M_ for MXP
		//		case PCM_SOLENOID:
		//		case IP_NETWORK:
		//		}


		private final int eMinNdx;
		private final int eCount;
		private RoConnectionTypesE() { 
			eMinNdx = Cnst.INVALID_RO_HDWR_CONNECTION_INDEX;
			eCount = 0;
		}
		private RoConnectionTypesE(int minNdx, int count) {
			eMinNdx = minNdx;
			eCount = count;
		}
		
		//use methods to avoid err about eCount not being initialized
		String getFrmtStr() { return "%-18s" + ((eCount<=0) ? "" : " [min=%2d, max=%2d, cnt=%2d]"); }
		String getFrmtHdr() { return "%-18s [......, ......, ......]"; }
		@Override
		public String toStringLog(String inpPrefix) {
			String prefix = (inpPrefix==null) ? "" : inpPrefix;
			String ans;
			if(eCount<=0) {
				ans = String.format(prefix + getFrmtStr(), this.name());
			} else {
				ans =String.format(prefix + getFrmtStr(), this.name(), eMinNdx, ((eMinNdx+eCount)-1), eCount);
			}
			return ans;
		}
		@Override
		public String toStringHdr(String inpPrefix) {
			String prefix = (inpPrefix==null) ? "" : inpPrefix;
			String ans = String.format(prefix + getFrmtHdr(), this.getClass().getSimpleName());
			return ans;
		}
		@Override
		public String toStringNotes() {
			// TODO Auto-generated method stub
			return null;
		}
	}

	/**
	 * documents MXP pins, which ones have dual-use, and makes it possible to ensure
	 * that the code isn't trying to use a pin as a PWM one place and a DIO somewhere
	 * else and other such issues.  This enum should never need to change except to
	 * correct errors.
	 * @author JudiA
	 *
	 */
	public static enum MxpPinNbrsE implements TmToStringI {
		MXP_PIN_01_5VOLTS(1, "+5V"),
		MXP_PIN_02_AO0(2, "AO0"),
		MXP_PIN_03_AI0(3, "AI4 (MXP AI0)"),
		MXP_PIN_04_AO1(4, "AO1"),
		MXP_PIN_05_AI1(5, "AI5 (MXP AI1)"),
		MXP_PIN_06_AGND(6, "AGND"),
		MXP_PIN_07_AI2(7, "AI6 (MXP AI2)"),
		MXP_PIN_08_DGND(8, "DGND"),
		MXP_PIN_09_AI3(9, "AI7 (MXP AI3)"),
		MXP_PIN_10_UART_RX(10, "UART.RX"),
		MXP_PIN_11_DIO0_PWM0(11, "DIO10/PWM10 (MXP DIO0/PWM0)"),
		MXP_PIN_12_DGND(12, "DGND"),
		MXP_PIN_13_DIO1_PWM1(13, "DIO11/PWM11 (MXP DIO1/PWM1)"),
		MXP_PIN_14_UART_TX(14, "UART.TX"),
		MXP_PIN_15_DIO2_PWM2(15, "DIO12/PWM12 (MXP DIO2/PWM2)"),
		MXP_PIN_16_DGND(16, "DGND"),
		MXP_PIN_17_DIO3_PWM3(17, "DIO13/PWM13 (MXP DIO3/PWM3)"),
		MXP_PIN_18_DIO11_PWM7(18, "DIO21/PWM17 (MXP DIO11/PWM7)"),
		MXP_PIN_19_DIO4_SPI_CS(19, "DIO14 (MXP DIO4/SPI.CS)"),
		MXP_PIN_20_DGND(20, "DGND"),
		MXP_PIN_21_DIO5_SPI_CLK(21, "DIO15 (MXP DIO5/SPI.CLK)"),
		MXP_PIN_22_DIO12_PWM8(22, "DIO22/PWM18 (MXP DIO12/PWM8)"),
		MXP_PIN_23_DIO6_SPI_MISO(23, "DIO16 (MXP DIO6/SPI.MISO)"),
		MXP_PIN_24_DGND(24, "DGND"),
		MXP_PIN_25_DIO7_SPI_MOSI(25, "DIO17 (MXP DIO7/SPI.MOSI)"),
		MXP_PIN_26_DIO13_PWM9(26, "DIO23/PWM19 (MXP DIO13/PWM9)"),
		MXP_PIN_27_DIO8_PWM4(27, "DIO18/PWM14 (MXP DIO8/PWM4)"),
		MXP_PIN_28_DGND(28, "DGND"),
		MXP_PIN_29_DIO9_PWM5(29, "DIO19/PWM15 (MXP DIO9/PWM5)"),
		MXP_PIN_30_DGND(30, "DGND"),
		MXP_PIN_31_DIO10_PWM6(31, "DIO20/PWM16 (MXP DI1O/PWM6)"),
		MXP_PIN_32_DIO14_I2C_SCL(32, "DIO24 (MXP DIO14/I2C.SCL)"),
		MXP_PIN_33_3VOLTS(33, "+3.3V"),
		MXP_PIN_34_DIO15_I2C_SDA(34, "DIO25 (MXP DIO15/I2C.SDA)"),
		;
		private final int ePinNbr;
		private final String eDescription;
		private MxpPinNbrsE(int pinNbr, String description) { 
			ePinNbr = pinNbr;
			eDescription = description;
		}

		String frmtStr = "MXP pin %2d [%-24s][%-28s]";
		String frmtHdr = "           [%-24s][%-28s]";
		@Override
		public String toStringLog(String inpPrefix) {
			String prefix = (inpPrefix==null) ? "" : inpPrefix;
			String ans = String.format(prefix + frmtStr, ePinNbr, this.name(), eDescription);
			return ans;
		}

		@Override
		public String toStringHdr(String inpPrefix) {
			String prefix = (inpPrefix==null) ? "" : inpPrefix;
			String ans = String.format(prefix + frmtHdr, this.getClass().getSimpleName(), "description");
			return ans;
		}

		@Override
		public String toStringNotes() {
			// TODO Auto-generated method stub
			return null;
		}
	}


	/**
	 * just holds constants so that we can type Cnst. and see a list
	 * of what's available.
	 * @author robotics
	 *
	 */
	public static class Cnst {

		//		public static final boolean IS_MXP_INSTALLED = true;

		/*======== the rest of this class shouldn't need to change ========*/
		public static final int INVALID_RO_HDWR_CONNECTION_INDEX = -1;

		public static final int CAN_ID_MIN = 0;
		public static final int CAN_ID_MAX = 62; //per FRC description of CAN bus.

		//see I2c7BitRjAddrE for SPI and I2C addressing
		//		public static final int kSPI_MIN_ADDR = 0x00;
		//		public static final int kSPI_MAX_ADDR = 0x00;
		//		
		//		//FRC I2C class expects 7-bit address right-justified in a byte.
		//		//the address must be even to allow for the least significant byte (LSB)
		//		//  to indicate read or write
		//		public static final int kI2C_MIN_ADDR = 0x00;
		//		public static final int kI2C_MAX_ADDR = 0x7E;

		//indexes for roboRIO connections begin at this value
		public static final int FRC_RIO_MIN_INDEX_VAL = 0; 
		public static final int FRC_PCM_MIN_INDEX_VAL = 0; 

		/* numbering of the channel numbers for MXP I/O picks up where the numbering
		 * on the roboRio leaves off.  These constants show where the roboRio leaves
		 * off. Should never need to be changed.
		 * See FRC_Java_Programming document, "MXP IO Numbering" under "Java Basics"
		 */
		//the number of each type of IO available on the roboRIO
		public static final int kRIO_USB_CNT = 2; //USB ports

		public static final int kRIO_AI_CNT = 4; //analog inputs
		public static final int kRIO_AO_CNT = 0; //analog outputs
		public static final int kRIO_DIO_CNT = 10; //digital input/output
		public static final int kRIO_PWM_CNT = 10; //PWMs
		public static final int kRIO_RELAY_CNT = 4; //Relays
		public static final int kRIO_SPI_CNT = 4; //SPI ports
		public static final int kRIO_I2C_CNT = 1; //I2C ports

		//the number of each type of IO available on the MXP (MyRIO Expansion Port)
		//Note: x=a?b:c; means if(a){x=b;}else{x=c;} -- conditional operator [744conditionalOp]
		//		public static final int kMXP_AI_CNT = IS_MXP_INSTALLED ? 4 : 0; //analog inputs
		//		public static final int kMXP_AO_CNT = IS_MXP_INSTALLED ? 4 : 0; //analog outputs
		//		public static final int kMXP_DIO_CNT = IS_MXP_INSTALLED ? 16 : 0; //digital input/output
		//		public static final int kMXP_PWM_CNT = IS_MXP_INSTALLED ? 10 : 0; //PWMs
		//		public static final int kMXP_RELAY_CNT = IS_MXP_INSTALLED ? 0 : 0; //Relays
		//		public static final int kMXP_SPI_CNT = IS_MXP_INSTALLED ? 1 : 0; //SPI ports
		//		public static final int kMXP_I2C_CNT = IS_MXP_INSTALLED ? 1 : 0; //I2C ports
		public static final int kMXP_AI_CNT = 4; //analog inputs
		public static final int kMXP_AO_CNT = 4; //analog outputs
		public static final int kMXP_DIO_CNT = 16; //digital input/output
		public static final int kMXP_PWM_CNT = 10; //PWMs
		public static final int kMXP_RELAY_CNT = 0; //Relays
		public static final int kMXP_SPI_CNT = 1; //SPI ports
		public static final int kMXP_I2C_CNT = 1; //I2C ports

		//the number of solenoids available on a PCM (Pneumatics Control Module)
		public static final int kPCM_SOL_CNT = 8; //solenoid channels

		//these were added to facilitate simulation in SensorBase.java
		public static final int kPDP_MOD_CNT = 1;
		public static final int kPDP_CHAN_CNT = 16; //8 40A, 8 30A
		public static final int kPCM_MAX_MOD_CNT = 2;
	}
	
	/**
	 * a List of all named robot connections, whether actually used on 
	 * the robot or not
	 * An enum (RoNamedConnectionE) is used to provide the names, but
	 * entries in this list provide the smarts.  The enum entries should
	 * be the only entity adding things to this list.
	 */
	public static List<RoConnectionEntry> roHwConnL = new ArrayList<RoConnectionEntry>();

	public class RoConnectionEntry implements TmToStringI, TmListBackingEnumI<RoConnectionEntry, RoNamedConnectionsE> {

		@Override
		public List<RoConnectionEntry> getListBackingEnum() { return roHwConnL; }
		
		private RoNamedConnectionsE c_namedConnEe = null; //...Ee for "enum entry"
		private RoConnectionTypesE c_connType = null;

		private int c_connFrcNdx = Cnst.INVALID_RO_HDWR_CONNECTION_INDEX;
		private SPI.Port c_connSpiPort = null;
		private I2C.Port c_connI2cPort = null;
		private MxpPinNbrsE c_connMxpPin = null;
		private String c_connIpAddr = null; //string containing IP address or hostname
		
//		//intended for use with DoubleSolenoids
//		private RoNamedConnectionsE c_namedAuxConnEe = null; //...Ee for "enum entry"
//		private RoConnectionTypesE c_auxConnType = null;
//		private int c_auxConnFrcNdx = Cnst.INVALID_RO_HDWR_CONNECTION_INDEX;

		public RoNamedConnectionsE getNamedConnectionEe() { return c_namedConnEe; } //...Ee for "enum entry"
		public RoConnectionTypesE getConnectionType() { return c_connType; }
		
		public int getConnectionFrcIndex() { return c_connFrcNdx; }
		public SPI.Port getConnectionSpiPort() { return c_connSpiPort; }
		public I2C.Port getConnectionI2cPort() { return c_connI2cPort; }
		public MxpPinNbrsE getConnectionMxpPin() { return c_connMxpPin; }
		public String getConnectionIpAddr() { return c_connIpAddr; }

		private boolean c_isValid = false;
		public boolean isValid() { return c_isValid; }
		
		private void logValidity(boolean validity) {
			c_isValid = validity;
			if( ! validity) {
				throw TmExceptions.getInstance().new InvalidRoConnectionEntryEx(this.toStringLog());
			}
		}

		String frmtStr = "%-22s, %-12s, ndx=%2d, spi=%-11s, i2c=%-8s, mxp=%-24s, ip=%-11s";
		String frmtHdr = "%-22s, %-12s, ......, %-11s, %-8s, %-24s, %-11s";
		@Override
		public String toStringLog(String inpPrefix) {
			String prefix = (inpPrefix==null) ? "" : inpPrefix;
			String ans = String.format(prefix + frmtStr, ((getNamedConnectionEe()==null) ? "n/a" : getNamedConnectionEe().name()),
					((getConnectionType()==null) ? "n/a" : getConnectionType().name()),
					getConnectionFrcIndex(),
					((getConnectionSpiPort()==null) ? "n/a" : getConnectionSpiPort().name()),
					((getConnectionI2cPort()==null) ? "n/a" : getConnectionI2cPort().name()),
					((getConnectionMxpPin()==null) ? "n/a" : getConnectionMxpPin().name()),
					((getConnectionIpAddr()==null) ? "n/a" : getConnectionIpAddr()));
			return ans;
		}
		@Override
		public String toStringHdr(String inpPrefix) {
			String prefix = (inpPrefix==null) ? "" : inpPrefix;
			String ans = String.format(prefix + frmtHdr, getNamedConnectionEe().getClass().getSimpleName(),
					getConnectionType().getClass().getSimpleName(),
					"SPI.Port", //.kOnboardCS0.getClass().getSimpleName(),
					"I2C.Port", //.kOnboard.getClass().getSimpleName(),
					MxpPinNbrsE.MXP_PIN_01_5VOLTS.getClass().getSimpleName(), //any enum value will do, avoids null pointer problems
					"IP addr");
			return ans;
		}
		@Override
		public String toStringNotes() {
			// TODO Auto-generated method stub
			return null;
		}

		protected boolean checkIfValidFrcIndex(RoConnectionTypesE connType, int frcNdx) {
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

		@SuppressWarnings("unused")
		private RoConnectionEntry() {} //private so can never be called

		public RoConnectionEntry(RoNamedConnectionsE namedConn, RoConnectionTypesE connType, int frcNdx) {
			c_namedConnEe = namedConn;
			c_connType = connType;
			c_connFrcNdx = frcNdx;
			if(namedConn.name().equals("R_USB0")) {
				int junk = 5; //good debugger breakpoint
			}
			boolean validity = false;
			if(( ! (namedConn==null)) && ( ! (connType==null))) {
				switch(connType) {
				case CAN_BUS:
				case R_AI: 
//				case R_AO:
				case R_DIO:
				case R_PWM:
				case R_RELAY: 
				case R_USB_PORT:
				case PCM_SOLENOID:
					validity = true;
					break;
				case M_AI:
				case M_AO:
				case M_DIO:
				case M_PWM:
//				case M_RELAY:
				case R_SPI_PORT: 
				case R_I2C_PORT: 
				case M_SPI_PORT: 
				case M_I2C_PORT:
				case IP_NETWORK:
				default:
					validity = false;
					break;
				}
			}
			logValidity(validity && checkIfValidFrcIndex(connType, frcNdx));			
		}
		public RoConnectionEntry(RoNamedConnectionsE namedConn, RoConnectionTypesE connType, SPI.Port port) {
			boolean valid = false;
			c_namedConnEe = namedConn;
			c_connType = connType;
			c_connSpiPort = port;
			if( ( ! (namedConn==null)) && ( ! (connType==null)) && ( ! (port==null)) ) {
				switch(connType) {
				case R_SPI_PORT:
					//Note: x=a?b:c; means if(a){x=b;}else{x=c;} -- conditional operator [744conditionalOp]
					valid = (port.equals(SPI.Port.kMXP)) ? false : true;
					break;
				case M_SPI_PORT:
					valid = (port.equals(SPI.Port.kMXP)) ? true : false;
					break;
				default:
					valid = false;
				}
			}
			logValidity(valid);			
		}
		public RoConnectionEntry(RoNamedConnectionsE namedConn, RoConnectionTypesE connType, I2C.Port port) {
			boolean valid = false;
			c_namedConnEe = namedConn;
			c_connType = connType;
			c_connI2cPort = port;
			if( ( ! (namedConn==null)) && ( ! (connType==null)) && ( ! (port==null)) ) {
				switch(connType) {
				case R_I2C_PORT:
					//Note: x=a?b:c; means if(a){x=b;}else{x=c;} -- conditional operator [744conditionalOp]
					valid = (port.equals(I2C.Port.kMXP)) ? false : true;
					break;
				case M_I2C_PORT:
					valid = (port.equals(I2C.Port.kMXP)) ? true : false;
					break;
				default:
					valid = false;
				}
			}
			logValidity(valid);			
		}
		public RoConnectionEntry(RoNamedConnectionsE namedConn, RoConnectionTypesE connType, int frcNdx, MxpPinNbrsE mxpPin) {
			//			private int c_connFrcNdx = Cnst.INVALID_RO_HDWR_CONNECTION_INDEX;
			//			private SPI.Port c_connSpiPort = null;
			//			private I2C.Port c_connI2cPort = null;
			//			private MxpPinNbrsE c_connMxpPin = null;
			//			private String c_connIpAddr = null; //string containing IP address or hostname
			c_namedConnEe = namedConn;
			c_connType = connType;
			c_connFrcNdx = frcNdx;
			c_connMxpPin = mxpPin;

			boolean valid = false;
			if( ( ! (namedConn==null)) && ( ! (connType==null)) && ( ! (mxpPin==null)) ) {
				switch(connType) {
				case CAN_BUS:
				case R_AI: 
//				case R_AO:
				case R_DIO:
				case R_PWM:
				case R_RELAY: 
					valid = false;
					break;
				case M_AI:
				case M_AO:
				case M_DIO:
				case M_PWM:
//				case M_RELAY:
					valid = true; //valid so far, still need to check frcNdx
					break;
				case R_SPI_PORT: 
				case R_I2C_PORT: 
				case R_USB_PORT: //R_ for roboRIO
				case M_SPI_PORT: 
				case M_I2C_PORT: //M_ for MXP
				case PCM_SOLENOID:
				case IP_NETWORK:
				default:
					valid = false;
					break;
				}
			}
			logValidity(valid && checkIfValidFrcIndex(connType, frcNdx));
		}
		public RoConnectionEntry(RoNamedConnectionsE namedConn, RoConnectionTypesE connType, String ipAddrOrHost) {
			c_namedConnEe = namedConn;
			c_connType = connType;
			c_connIpAddr = ipAddrOrHost;
			boolean valid = false;
			if( ( ! (namedConn==null)) && ( ! (connType==null)) && ( ! (ipAddrOrHost==null)) ) {
				switch(connType) {
				case IP_NETWORK:
					valid = true;
					break;
				default:
					valid = false;
					break;
				}
			}
			logValidity(valid);						
		}
		
		TmHdwrItemEnableDisableMgr cConnEnableDisableMgr = new TmHdwrItemEnableDisableMgr();
		@Override
		public TmHdwrItemEnableDisableMgr getHdwrItemEnableDisableMgr() { return cConnEnableDisableMgr; }
	}
	
	protected static List<RoNamedModulesEntry> roNamedModulesList = new ArrayList<>();
	public class RoNamedModulesEntry implements TmToStringI, TmForcedInstantiateI, TmListBackingEnumI<RoNamedModulesEntry, RoNamedModulesE>{

		@Override
		public List<RoNamedModulesEntry> getListBackingEnum() { return roNamedModulesList; }
//		public static List<RoNamedModulesEntry> staticGetListBackingEnum() { return roNamedModulesList; }

		private TmHdwrItemEnableDisableMgr nmEnableDisableMgr = null; //new TmHdwrItemEnableDisableMgr();
		@Override
		public TmHdwrItemEnableDisableMgr getHdwrItemEnableDisableMgr() { return null; } //nmEnableDisableMgr; }
		
		public RoNamedModulesE cNamedMod;
		public ItemAvailabilityE cModAvail;
		public ItemFakeableE cModFakeable;
		public RoModuleTypesE cModType;
		public RoNamedConnectionsE cModNamedConn;
		
//		TmFakeable_CanTalonSrx cFakeableCanTalonSrxObj = null;

		boolean cIsValid;
		public boolean isValid() { return cIsValid; }
		
		private void logValidity(boolean validity) {
			cIsValid = validity;
			if( ! validity) {
				throw m_exceptions.new InvalidRoModulesEntryEx(this.toStringLog());
			}
		}
		
		RoNamedModulesEntry(RoNamedModulesE namedMod, ItemAvailabilityE modAvail, ItemFakeableE modFakeable, 
				RoModuleTypesE modType, RoNamedConnectionsE modNamedConn) {
			cNamedMod = namedMod;
			cModAvail = modAvail;
			cModFakeable = modFakeable;
			cModType = modType;
			cModNamedConn = modNamedConn;
			boolean valid = false;
			if( ! (cNamedMod==null) || (cModAvail==null) || (cModFakeable==null) || (cModType==null) || (cModNamedConn==null)) {
				switch(cModType) {
				case CAN_TALON_SRX_MOD:
				case PCM_MOD:
				case RIO_MOD:
					if(cModNamedConn.getEnt().c_connType.equals(RoConnectionTypesE.CAN_BUS)) {
						valid = true;
					}
					break;
				case PWM_TALON_MOD:
				case PWM_TALON_SRX_MOD:
					if(cModNamedConn.getEnt().c_connType.equals(RoConnectionTypesE.R_PWM) ||
					   cModNamedConn.getEnt().c_connType.equals(RoConnectionTypesE.M_PWM)    ) {
						valid = true;
					}
					break;
				default:
					valid = false;
					break;
				}
			}
			cIsValid = valid;
		}
		
		String frmtStr = "ndx=%3d, %-31s, %-17s, %-13s, %-17s, %-26s, valid=%b";
		String frmtHdr = "......., %-31s, %-17s, %-13s, %-17s, %-26s, ..........";
		public String toStringLog(String inpPrefix) {
			String prefix = (inpPrefix==null) ? "" : inpPrefix;
			String ans = String.format(prefix + frmtStr, 
					((cNamedMod==null) ? -1 : cNamedMod.ordinal()), Tt.getName(cNamedMod),
					Tt.getName(cModAvail), Tt.getName(cModFakeable), Tt.getName(cModType), Tt.getName(cModNamedConn), cIsValid);
			return ans;
		}
		@Override
		public String toStringHdr(String inpPrefix) {
			String prefix = (inpPrefix==null) ? "" : inpPrefix;
			return String.format(prefix + frmtHdr, 
//					cNamedMod.getClass().getSimpleName(), 
//					cDevType.getClass().getSimpleName(), cNamedConn.getClass().getSimpleName());
					Tt.getClassName(cNamedMod),
					Tt.getClassName(cModAvail), Tt.getClassName(cModFakeable), Tt.getClassName(cModType), Tt.getClassName(cModNamedConn));
		}
		@Override
		public String toStringNotes() {
			return "enum " + Tt.getClassName(cNamedMod) + " initializes List<" + 
							this.getClass().getSimpleName() + "> roNamedModulesList, see enum's getList() and getEnt() methods too";
		}
		
		
		@Override
		public void doForcedInstantiation() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void doPopulate() {
			// TODO Auto-generated method stub
			
		}

	}
	
	protected static List<RoNamedControlsEntry> roNamedControlsList = new ArrayList<>();
	public class RoNamedControlsEntry implements TmToStringI, TmForcedInstantiateI, TmItemAvailabilityI,
											TmListBackingEnumI<RoNamedControlsEntry, RoNamedControlsE>{

		@Override
		public List<RoNamedControlsEntry> getListBackingEnum() { return roNamedControlsList; }

		TmHdwrItemEnableDisableMgr cEnableDisableMgr = null;
		@Override
		public TmHdwrItemEnableDisableMgr getHdwrItemEnableDisableMgr() { return cEnableDisableMgr; }

		public RoNamedControlsE cNamedCntl = null;
		public ItemAvailabilityE cCntlAvail = null;
		public ItemFakeableE cCntlFakeable = null; 
		public RoControlTypesE cCntlType = null;
		public RoNamedModulesE cNamedMod = null;
		public RoNamedConnectionsE cNamedConn = null;
		public RoControlCfgStyleE cCntlCfgStyle;
		
		public Relay.Direction cRelayDir = null;
		
		public FrcAndJoysticksVsRoDrvMtrDirVsRobotDirE cMtrForFrcJsDirVsRobotDir = null;
		public MotorPosPercOutVsThingDirE cMtrPosPercentOutVsThingDir = null;
		public RoMtrInversionE cMtrInversion = null;
		public double cMtrTuningFactor = 0.0;
		public SdKeysE cSdKeyMtrControlMode = null;
		public SdKeysE cSdKeyMtrPercentOut = null; //was: cSdKeyMtrPercentOut
		public SdKeysE cSdKeyMtrAmps = null;
		
		public RoMtrContinuousAmpsE cMaxContinuousAmps = null;
		public RoMtrPeakAmpsE cMaxPeakAmps = null;
		public RoMtrPeakAmpsDurationE cMaxPeakAmpsDurationMs = null;
		
		public RoMtrHasEncoderE cMtrHasEncoderEnumVal = RoMtrHasEncoderE.NO_ENCODER;
		public boolean cMtrHasEncoder = false;
		public SdKeysE cMtrEncoderSdKeyPosition = null;
		public double cMtrEncoderMaxRevsPerSec = 0;
		public int cMtrEncoderCountsPerRevolution = 0;
		public double cMtrEncoderFeetPerRevolution = 0.0;
		public EncoderIncrVsThingDirE cMtrEncoderIncrVsThingDir = null;
		public EncoderPolarityE cMtrEncoderPolarity = null;
		public EncoderCountsCapabilityE cMtrEncoderCountsCap = null;
		
		public boolean cIsValid = false;

		public boolean isValid() { return cIsValid; }
		
		private void logValidity(boolean validity) {
			cIsValid = validity;
			if( ! validity) {
				throw m_exceptions.new InvalidDsControlsEntryEx(this.toStringLog());
			}
		}
		
//		//things to populate from doPopulate(); use d prefix to make it easier to find these in 
//		//eclipse drop-down lists
//		public TmFakeable_CanTalonSrx dFakeableCanTalonSrxObj = null;
////		public PWMTalonSRX dPwmTalonSrx = null;
//		public AnalogInput dAnalogInputObj = null;
//		public TmFakeable_RoDigitalInput dFakeableDigitalInputObj = null;
//		public TmFakeable_Relay dFakeableRelayObj = null;
//		public TmFakeable_DoubleSolenoid dFakeableDblSolenoid = null;
//		public TmFakeable_Solenoid dFakeableSolenoid = null;
//				
//		public 


		String frmtStr = "%-37s, %-18s, %-17s, %-13s, %-25s, %-31s, %-25s, %-10s";
		String frmtStrMtr = "%s [%-47s, %-36s, %-30s, %1.6f, %-33s, %-40s, %-33s, %-30s, %-25s, %-30s]";
		String frmtStrMtrEnc = "%s [%-30s, %7.1f , %4d,  %8.3f , %-22s, %-17s, %-25s]";
		String frmtHdr = " %-37s, %-18s, %-17s, %-13s, %-25s, %-31s, %-25s, %-10s";
		String frmtHdrMtr = "%s [%-47s, %-36s, %-30s, tuning  , %-33s, %-40s, %-33s, %-30s, %-25s, %-30s]";
		String frmtHdrMtrEnc = "%s [%-30s,  maxRPS ,  CPR,  ftPerRev , %-22s, %-17s, %-25s]";
		
		@Override
		public String toStringLog(String inpPrefix) {
			String prefix = (inpPrefix==null) ? "" : inpPrefix;
			String ans = String.format(prefix + frmtStr, Tt.getName(cNamedCntl), 
					Tt.getName(cCntlCfgStyle), Tt.getName(cCntlAvail), Tt.getName(cCntlFakeable),
					Tt.getName(cCntlType), Tt.getName(cNamedMod), Tt.getName(cNamedConn), Tt.getName(cRelayDir) );
			        if( ! (cMtrForFrcJsDirVsRobotDir==null)) {
			        	ans = String.format(frmtStrMtr, ans, Tt.getName(cMtrForFrcJsDirVsRobotDir), Tt.getName(cMtrPosPercentOutVsThingDir), Tt.getName(cMtrInversion),
			        			cMtrTuningFactor, Tt.getName(cSdKeyMtrControlMode), Tt.getName(cSdKeyMtrPercentOut), Tt.getName(cSdKeyMtrAmps),
			        			Tt.getName(cMaxContinuousAmps), Tt.getName(cMaxPeakAmps), Tt.getName(cMaxPeakAmpsDurationMs));
			        	if(cMtrHasEncoder) {
				        	ans = String.format(frmtStrMtrEnc, ans, Tt.getName(cMtrEncoderSdKeyPosition), cMtrEncoderMaxRevsPerSec,
				        			cMtrEncoderCountsPerRevolution, cMtrEncoderFeetPerRevolution,
				        			Tt.getName(cMtrEncoderIncrVsThingDir),
				        			Tt.getName(cMtrEncoderPolarity), Tt.getName(cMtrEncoderCountsCap));
			        	}
			        }        
			return ans;
		}
		@Override
		public String toStringHdr(String inpPrefix) {
			String prefix = (inpPrefix==null) ? "" : inpPrefix;
			String ans = String.format(prefix + frmtHdr, Tt.getClassName(cNamedCntl), 
					Tt.getClassName(cCntlCfgStyle), Tt.getClassName(cCntlAvail), Tt.getClassName(cCntlFakeable),
					Tt.getClassName(cCntlType), Tt.getClassName(cNamedMod), Tt.getClassName(cNamedConn), Tt.getClassName(cRelayDir) );
			        if( ! (cMtrForFrcJsDirVsRobotDir==null)) {
			        	ans = String.format(frmtHdrMtr, ans, Tt.getClassName(cMtrForFrcJsDirVsRobotDir), Tt.getClassName(cMtrPosPercentOutVsThingDir), 
			        			Tt.getClassName(cMtrInversion),
			        			Tt.getClassName(cSdKeyMtrControlMode), Tt.getClassName(cSdKeyMtrPercentOut), Tt.getClassName(cSdKeyMtrAmps),
			        			Tt.getClassName(cMaxContinuousAmps), Tt.getClassName(cMaxPeakAmps), Tt.getClassName(cMaxPeakAmpsDurationMs));
			        	if(cMtrHasEncoder) {
				        	ans = String.format(frmtHdrMtrEnc, ans, Tt.getClassName(cMtrEncoderSdKeyPosition),
				        			Tt.getClassName(cMtrEncoderIncrVsThingDir),
				        			Tt.getClassName(cMtrEncoderPolarity), Tt.getClassName(cMtrEncoderCountsCap));
			        	}
			        }        
			return ans;
		}
		@Override
		public String toStringNotes() {
			return "enum " + cNamedCntl.getClass().getSimpleName() + " initializes List<" + 
					this.getClass().getSimpleName() + "> roNamedControlsList, see getList() and getEnt() methods in enum";
		}

		public RoNamedControlsEntry(RoNamedControlsE namedCntl, RoControlCfgStyleE cntlCfgStyle, ItemAvailabilityE cntlAvail, ItemFakeableE cntlFakeable, 
				RoControlTypesE cntlType, RoNamedModulesE namedMod, RoNamedConnectionsE namedConn, 
				Relay.Direction relayDir) {
			this(namedCntl, cntlCfgStyle, cntlAvail, cntlFakeable, cntlType, namedMod, namedConn, relayDir,   
					null, null, null, 0.0, null, null, null, 
					null, null, null, 
					RoMtrHasEncoderE.NO_ENCODER, null, 0.0, 0, 0.0, null, null, null);
		}
		public RoNamedControlsEntry(RoNamedControlsE namedCntl, RoControlCfgStyleE cntlCfgStyle, ItemAvailabilityE cntlAvail, ItemFakeableE cntlFakeable, 
				RoControlTypesE cntlType, RoNamedModulesE namedMod, RoNamedConnectionsE namedConn, 
				Relay.Direction relayDir, 
				FrcAndJoysticksVsRoDrvMtrDirVsRobotDirE mtrDirVsRobotDir, MotorPosPercOutVsThingDirE mtrVsThingDir,
				RoMtrInversionE mtrInversion, 
				double mtrTuningFactor, SdKeysE sdKeyMtrMode, SdKeysE sdKeyMtrPercentOut, SdKeysE sdKeyMtrAmps,
				RoMtrContinuousAmpsE maxContinuousAmps,
				RoMtrPeakAmpsE maxPeakAmps,
				RoMtrPeakAmpsDurationE maxPeakAmpsDurationMs) {
			this(namedCntl, cntlCfgStyle, cntlAvail, cntlFakeable, cntlType, namedMod, namedConn, relayDir,   
					mtrDirVsRobotDir, mtrVsThingDir, mtrInversion, mtrTuningFactor, 
					sdKeyMtrMode, sdKeyMtrPercentOut, sdKeyMtrAmps, 
					maxContinuousAmps, maxPeakAmps, maxPeakAmpsDurationMs,
					RoMtrHasEncoderE.NO_ENCODER, null, 0.0, 0, 0.0, null, null, null);			
		}
		public RoNamedControlsEntry(RoNamedControlsE namedCntl, RoControlCfgStyleE cntlCfgStyle, ItemAvailabilityE cntlAvail, ItemFakeableE cntlFakeable, 
					RoControlTypesE cntlType, RoNamedModulesE namedMod, RoNamedConnectionsE namedConn, 
					Relay.Direction relayDir, 
					FrcAndJoysticksVsRoDrvMtrDirVsRobotDirE mtrDirVsRobotDir, MotorPosPercOutVsThingDirE mtrVsThingDir,
					RoMtrInversionE mtrInversion, 
					double mtrTuningFactor, SdKeysE sdKeyMtrMode, SdKeysE sdKeyMtrPercentOut, SdKeysE sdKeyMtrAmps,
					RoMtrContinuousAmpsE maxContinuousAmps,
					RoMtrPeakAmpsE maxPeakAmps,
					RoMtrPeakAmpsDurationE maxPeakAmpsDurationMs,
					RoMtrHasEncoderE mtrHasEncoder, SdKeysE mtrEncoderSdKeyPosition, double mtrEncoderMaxRevsPerSec, 
					int mtrEncoderCountsPerRevolution, double mtrEncoderFeetPerRevolution,
					EncoderIncrVsThingDirE mtrEncoderIncrVsThingDir,
					EncoderPolarityE mtrEncoderPolarity, EncoderCountsCapabilityE mtrEncoderCountsCap) {
			cNamedCntl = namedCntl;
			cCntlCfgStyle = cntlCfgStyle;
			cCntlAvail = cntlAvail;
			cCntlFakeable = cntlFakeable; 
			cCntlType = cntlType;
			cNamedMod = namedMod;
			cNamedConn = namedConn;
			
			cRelayDir = relayDir;
			
			/**
			 * used only in TmSsDriveTrain's tankDriveJoysticksPercentOutput() and 
			 * tankDriveJoysticksVelocity() methods
			 */
			cMtrForFrcJsDirVsRobotDir = mtrDirVsRobotDir;
			
			cMtrPosPercentOutVsThingDir = mtrVsThingDir;
			cMtrInversion = mtrInversion;
			cMtrTuningFactor = mtrTuningFactor;
			cSdKeyMtrControlMode = sdKeyMtrMode;
			cSdKeyMtrPercentOut = sdKeyMtrPercentOut;
			cSdKeyMtrAmps = sdKeyMtrAmps;
			
			cMaxContinuousAmps = maxContinuousAmps;
			cMaxPeakAmps = maxPeakAmps;
			cMaxPeakAmpsDurationMs = maxPeakAmpsDurationMs;
			
			cMtrHasEncoderEnumVal = mtrHasEncoder;
			cMtrHasEncoder = ! mtrHasEncoder.equals(RoMtrHasEncoderE.NO_ENCODER);
			cMtrEncoderSdKeyPosition = mtrEncoderSdKeyPosition;
			cMtrEncoderMaxRevsPerSec = mtrEncoderMaxRevsPerSec;
			cMtrEncoderCountsPerRevolution = mtrEncoderCountsPerRevolution;
			cMtrEncoderIncrVsThingDir = mtrEncoderIncrVsThingDir;
			cMtrEncoderPolarity = mtrEncoderPolarity;
			cMtrEncoderCountsCap = mtrEncoderCountsCap;

			cIsValid = false;
			
			//check validity from doForcedInstantiation() so that we can safely access list entries, etc.
		}
		
		private boolean checkInitialValidity() {
			boolean valid = true;
			if( ! (cNamedCntl==null || cCntlType==null || cNamedMod==null )) { //|| cNamedConn==null) ) {
				RoNamedControlsEntry cntlEnt = cNamedCntl.getEnt();
				
				if(cNamedConn==null) {
					cNamedConn = cNamedMod.getEnt().cModNamedConn;

					switch(cCntlType) {
					case CAN_BUS_MOTOR_CONTROLLER:
						switch(cNamedMod.getEnt().cModType) {
						case CAN_TALON_SRX_MOD:
							break;
						case PWM_TALON_MOD:
						case PWM_TALON_SRX_MOD:
						case PCM_MOD:
						case RIO_MOD:
						default:
							valid = false;
							break;
						}
						break;

					case PWM_MOTOR_CONTROLLER:
						switch(cNamedMod.getEnt().cModType) {
						case PWM_TALON_MOD:
						case PWM_TALON_SRX_MOD:
							break;
						case CAN_TALON_SRX_MOD:
						case PCM_MOD:
						case RIO_MOD:
						default:
							valid = false;
							break;
						}
						break;

					case OTHER:
					default:
						valid = false;
						break;

					}
				}

				if(valid) {

					RoConnectionEntry connEnt = cNamedConn.getEnt();
					switch(connEnt.c_connType) {
					case CAN_BUS:
						if( ! cNamedMod.eModuleType.isRequiresCanBusConnection()) {
							valid = false;
						}
						else if(cntlEnt.cCntlType.equals(RoControlTypesE.CAN_BUS_MOTOR_CONTROLLER)) {
							if( ! (cMtrForFrcJsDirVsRobotDir==null || cMtrInversion==null || cMtrPosPercentOutVsThingDir==null) ) {
								if(cMtrHasEncoder) {
									if( ! (cMtrEncoderPolarity==null || cMtrEncoderCountsCap==null || cMtrEncoderIncrVsThingDir==null) ) {
									} else {
										valid = false;
									}
								} 
							}
						}
						break;
					case IP_NETWORK:
					case M_AI:
					case M_AO:
					case M_DIO:
					case M_I2C_PORT:
					case M_PWM:
					case M_SPI_PORT:
						if(cNamedMod.eModuleType.equals(RoModuleTypesE.RIO_MOD)) {
							valid = true;
						} else { valid = false; }
						break;
					case PCM_SOLENOID:
						if(cNamedMod.eModuleType.equals(RoModuleTypesE.PCM_MOD)) {
							valid = true;
						} else { valid = false; }
						break;
					case R_AI:
					case R_DIO:
					case R_PWM:
						if(cNamedMod.eModuleType.equals(RoModuleTypesE.RIO_MOD)) {
							valid = true;
						} else {valid = false; }
						break;
					case R_RELAY:
						if(cNamedMod.eModuleType.equals(RoModuleTypesE.RIO_MOD)) {
							if(cRelayDir==null) {
								valid = false;
							} else {
								valid = true;
							}
						}
						break;
					case R_I2C_PORT:
					case R_SPI_PORT:
					case R_USB_PORT:
						if( ! cNamedMod.eModuleType.equals(RoModuleTypesE.RIO_MOD)) {
							valid = false;
						}
						break;
					default:
						valid = false;
						break;
					}
				}
			}
			logValidity(valid);
			return valid;
		}

		@Override
		public void doForcedInstantiation() {
			if(cNamedConn==null) {
				if(cNamedCntl.getEnt().cCntlType.equals(RoControlTypesE.CAN_BUS_MOTOR_CONTROLLER)) {
					if(cNamedMod.getEnt().cModType.equals(RoModuleTypesE.CAN_TALON_SRX_MOD)) {
						cNamedConn = cNamedMod.getEnt().cModNamedConn;
					}
				}
			}
			
			if( ! checkInitialValidity()) {
				//an exception should have already been thrown....
				throw TmExceptions.getInstance().new InvalidRoControlsEntryEx(this.toStringLog());
			}
		}
		
//		TmFakeable_CanTalonSrx dFakeableCanTalonSrxObj = null;
//		public TmFakeable_CanTalonSrx getFakeableCanTalonSrxObj() { return dFakeableCanTalonSrxObj; }

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
			return cCntlAvail.equals(ItemAvailabilityE.USE_FAKE);
			//return false;
		}

	}
	
	
//	public static class RoModulesMgr implements TmForcedInstantiateI {
//
//		@Override
//		public void doForcedInstantiation() {
//			// TODO Auto-generated method stub
//			
//		}
//
//		@Override
//		public void doPopulate() {
//			for(RoNamedModulesE e : RoNamedModulesE.values()) {
//				e.getEnt().doPopulate();
//			}
//		}
//		
//	}


	public static class RoControlsMgr implements TmForcedInstantiateI {

		/*---------------------------------------------------------
		 * getInstance stuff                                      
		 *---------------------------------------------------------*/
		/** 
		 * handle making the singleton instance of this class and giving
		 * others access to it
		 */
		private static RoControlsMgr m_instance;
		public static synchronized RoControlsMgr getInstance()
		{
			if(m_instance == null){ m_instance = new RoControlsMgr(); }
			return m_instance;
		}
		private RoControlsMgr() {}
		/*----------------end of getInstance stuff----------------*/

		
		@Override
		public void doForcedInstantiation() {
			for(RoNamedControlsE e : RoNamedControlsE.values()) {
				e.getEnt().doForcedInstantiation();
			}
		}

		private class ModConnAssignmentsEntry {
			List<RoNamedControlsE> assignedList = new ArrayList<>();
		}
		
		@Override
		public void doPopulate() {
			for(RoNamedControlsE e : RoNamedControlsE.values()) {
				e.getEnt().doPopulate();
			}
			
			//now inspect to catch multiple assignments
			final int ARRAY_LEN = RoNamedModulesE.values().length*RoNamedConnectionsE.values().length;
			ModConnAssignmentsEntry[] everythingList = new ModConnAssignmentsEntry[ARRAY_LEN];
			for(int i=0; i<ARRAY_LEN; i++) {
				everythingList[i] = new ModConnAssignmentsEntry();
			}
			for(RoNamedControlsE nc : RoNamedControlsE.values()) {
				int connCnt = RoNamedConnectionsE.values().length;
				int modNdx = nc.getEnt().cNamedMod.ordinal();
				int connNdx = nc.getEnt().cNamedConn.ordinal();
				int ndx = modNdx * connCnt + connNdx;
				everythingList[ndx].assignedList.add(nc);
			}
			boolean foundMult = false;
			for(int i=0; i<ARRAY_LEN; i++) {
				if(everythingList[i].assignedList.size() > 1) {
					foundMult = true;
					String ncList = "";
					for(RoNamedControlsE nc : everythingList[i].assignedList) {
						ncList += nc.name() + ", ";
					}
					RoNamedControlsE nc = everythingList[i].assignedList.get(0);
					P.println("Multiple assignments for mod " + nc.getEnt().cNamedMod.name() + ":" +
										"conn " + nc.getEnt().cNamedConn.name() + ": " + ncList);
				}
			}
			if(foundMult) {
				throw TmExceptions.getInstance().new Team744RunTimeEx("multiple assignments detected for RoNamedControlsE");
			}
		}
		
	}


	public void showEverything() {
//		TmToStringI.showEnumEverything(RoDrvMtrInputPolarityE.values());
//		TmToStringI.showEnumEverything(RoMtrInversionE.values());
//		TmToStringI.showEnumEverything(RoControlTypesE.values());
//		TmToStringI.showEnumEverything(RoModuleConnectionsOnCanBusE.values());
		TmToStringI.showEnumEverything(RoModuleTypesE.values());
		TmToStringI.showEnumEverything(RoConnectionTypesE.values());
//		TmToStringI.showEnumEverything(RoNamedConnectionsE.values());
		TmToStringI.showEnumEverything(MxpPinNbrsE.values());
		
		TmToStringI.showListEverything(RoNamedConnectionsE.staticGetList());
		
	}

	@Override
	public void doForcedInstantiation() {
		//Access something from each of the enums in this class to force them 
		//(and their related List<> arrays) to be initialized
		//watch that optimization in the compiler doesn't decide to skip these
		FrcAndJoysticksVsRoDrvMtrDirVsRobotDirE junk1 = FrcAndJoysticksVsRoDrvMtrDirVsRobotDirE.FOR_FRC_JS_NOT_A_DRIVE_MOTOR;
		RoMtrInversionE junk2 = RoMtrInversionE.INVERT_MOTOR;
		RoControlTypesE junk3 = RoControlTypesE.CAN_BUS_MOTOR_CONTROLLER;
		RoModuleConnectionsOnCanBusE junk4 = RoModuleConnectionsOnCanBusE.CAN_BUS_ID_NOT_NEEDED;
		RoModuleTypesE junk5 = RoModuleTypesE.CAN_TALON_SRX_MOD;
		RoConnectionTypesE junk6 = RoConnectionTypesE.CAN_BUS;
//		RoNamedConnectionsE junk7 = RoNamedConnectionsE.I2C_MXP;
		MxpPinNbrsE junk8 = MxpPinNbrsE.MXP_PIN_01_5VOLTS;

//		RoConnAndIndexAssignments.getInstance().doForcedInstantiation();
		
//		RoDevicesMgr.getInstance().doForcedInstantiation(); //set up joystick objects, etc.
		RoControlsMgr.getInstance().doForcedInstantiation();

	}
	
	@Override
	public void doPopulate() {
		
//		RoConnAndIndexAssignments.getInstance().doPopulate(); //logs assignments
		
//		RoDevicesDevConnAssignments.getInstance().doPopulate(); //logs all assignments, etc.
//		RoControlsDevConnAssignments.getInstance().doPopulate(); //logs all assignments, etc.
//		
//		RoDevicesMgr.getInstance().doPopulate(); //set up joystick objects, etc.
		RoControlsMgr.getInstance().doPopulate();
		
		
	}

}
