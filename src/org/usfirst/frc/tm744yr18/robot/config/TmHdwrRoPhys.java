package org.usfirst.frc.tm744yr18.robot.config;

//import java.util.ArrayList;
import java.util.List;

import org.usfirst.frc.tm744yr18.robot.config.TmHdwrDsPhys.DsControlsDevConnAssignments;
import org.usfirst.frc.tm744yr18.robot.config.TmHdwrDsPhys.DsDevicesDevConnAssignments;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmEnumWithListI;
//import org.usfirst.frc.tm744yr18.robot.config.TmHdwrRoCntls.RoNamedControlsE;
//import org.usfirst.frc.tm744yr18.robot.config.TmHdwrRoCntls.RoNamedModulesE;
//import org.usfirst.frc.tm744yr18.robot.config.TmHdwrRoPhysBase.Cnst;
//import org.usfirst.frc.tm744yr18.robot.config.TmHdwrRoPhysBase.MxpPinNbrsE;
//import org.usfirst.frc.tm744yr18.robot.config.TmHdwrRoPhysBase.RoConnectionEntry;
//import org.usfirst.frc.tm744yr18.robot.config.TmHdwrRoPhysBase.RoConnectionTypesE;
//import org.usfirst.frc.tm744yr18.robot.config.TmHdwrRoPhysBase.RoModuleTypesE;
////import org.usfirst.frc.tm744yr18.robot.config.TmHdwrRoPhysBase.roHwConnL;
////import org.usfirst.frc.tm744yr18.robot.config.TmHdwrRoPhysBase;
////import org.usfirst.frc.tm744yr18.robot.config.TmHdwrRoPhysBase.Cnst;
////import org.usfirst.frc.tm744yr18.robot.config.TmHdwrRoPhysBase.MxpPinNbrsE;
////import org.usfirst.frc.tm744yr18.robot.config.TmHdwrRoPhysBase.RoConnectionEntry;
////import org.usfirst.frc.tm744yr18.robot.config.TmHdwrRoPhysBase.RoConnectionTypesE;
//import org.usfirst.frc.tm744yr18.robot.config.TmSdKeys.SdKeysE;
//import org.usfirst.frc.tm744yr18.robot.devices.TmFakeable_CanTalonSrx;
//import org.usfirst.frc.tm744yr18.robot.devices.TmFakeable_CanTalonSrx.EncoderCountsCapabilityE;
//import org.usfirst.frc.tm744yr18.robot.devices.TmFakeable_CanTalonSrx.EncoderPolarityE;
//import org.usfirst.frc.tm744yr18.robot.exceptions.TmExceptions;
//import org.usfirst.frc.tm744yr18.robot.helpers.TmHdwrItemEnableDisableMgr;
//import org.usfirst.frc.tm744yr18.robot.interfaces.TmEnumWithListI;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmForcedInstantiateI;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmListBackingEnumI;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmToStringI;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmToolsI.P;

import edu.wpi.first.wpilibj.I2C;
import edu.wpi.first.wpilibj.Relay;
import edu.wpi.first.wpilibj.SPI;

public class TmHdwrRoPhys extends TmHdwrRoPhysBase implements TmForcedInstantiateI {
//public class TmHdwrRoPhys implements TmForcedInstantiateI {

	/*---------------------------------------------------------
	 * getInstance stuff                                      
	 *---------------------------------------------------------*/
	/** 
	 * handle making the singleton instance of this class and giving
	 * others access to it
	 */
	private static TmHdwrRoPhys m_instance;

	public static synchronized TmHdwrRoPhys getInstance() {
		if (m_instance == null) {
			m_instance = new TmHdwrRoPhys();
		}
		return m_instance;
	}

	protected TmHdwrRoPhys() {
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

	public static class ConnCnst {
		public static final RoNamedConnectionsE USE_MODULE_CAN_ID = null;		
	}
	public static enum RoNamedConnectionsE implements TmToStringI, TmEnumWithListI<RoConnectionEntry> {
		//		//we have things that expect to have non-null named connections.  These are provided to satisfy those
		//		//requirements. They represent modules, not connections, so no error messages for these!!
		//		NO_CONNECTIONS_ON_CAN_TALON_SRX(RoConnectionTypesE.CAN_TALON_SRX_MODULE, -1, ConnectionCheckingE.NO_CHECK_CONNECTION),
		RIO_AI0(RoConnectionTypesE.R_AI, 0),
		RIO_AI1(RoConnectionTypesE.R_AI, 1),
		RIO_AI2(RoConnectionTypesE.R_AI, 2),
		RIO_AI3(RoConnectionTypesE.R_AI, 3),
		RIO_DIO0(RoConnectionTypesE.R_DIO, 0),
		RIO_DIO1(RoConnectionTypesE.R_DIO, 1),
		RIO_DIO2(RoConnectionTypesE.R_DIO, 2),
		RIO_DIO3(RoConnectionTypesE.R_DIO, 3),
		RIO_DIO4(RoConnectionTypesE.R_DIO, 4),
		RIO_DIO5(RoConnectionTypesE.R_DIO, 5),
		RIO_DIO6(RoConnectionTypesE.R_DIO, 6),
		RIO_DIO7(RoConnectionTypesE.R_DIO, 7),
		RIO_DIO8(RoConnectionTypesE.R_DIO, 8),
		RIO_DIO9(RoConnectionTypesE.R_DIO, 9),
		MXP_PIN11_DIO0_PWM0(RoConnectionTypesE.M_DIO, 0 + Cnst.kRIO_DIO_CNT, MxpPinNbrsE.MXP_PIN_11_DIO0_PWM0),
		MXP_PIN13_DIO1_PWM1(RoConnectionTypesE.M_DIO, 1 + Cnst.kRIO_DIO_CNT, MxpPinNbrsE.MXP_PIN_13_DIO1_PWM1),
		MXP_PIN15_DIO2_PWM2(RoConnectionTypesE.M_DIO, 2 + Cnst.kRIO_DIO_CNT, MxpPinNbrsE.MXP_PIN_15_DIO2_PWM2),
		MXP_PIN17_DIO3_PWM3(RoConnectionTypesE.M_DIO, 3 + Cnst.kRIO_DIO_CNT, MxpPinNbrsE.MXP_PIN_17_DIO3_PWM3),
		MXP_PIN19_DIO4_SPICS(RoConnectionTypesE.M_DIO, 4 + Cnst.kRIO_DIO_CNT, MxpPinNbrsE.MXP_PIN_19_DIO4_SPI_CS),
		MXP_PIN21_DIO5_SPICLK(RoConnectionTypesE.M_DIO, 5 + Cnst.kRIO_DIO_CNT, MxpPinNbrsE.MXP_PIN_21_DIO5_SPI_CLK),
		MXP_PIN23_DIO6_SPIMISO(RoConnectionTypesE.M_DIO, 6 + Cnst.kRIO_DIO_CNT, MxpPinNbrsE.MXP_PIN_23_DIO6_SPI_MISO),
		MXP_PIN25_DIO7_SPIMOSI(RoConnectionTypesE.M_DIO, 7 + Cnst.kRIO_DIO_CNT, MxpPinNbrsE.MXP_PIN_25_DIO7_SPI_MOSI),
		MXP_PIN27_DIO8_PWM4(RoConnectionTypesE.M_DIO, 8 + Cnst.kRIO_DIO_CNT, MxpPinNbrsE.MXP_PIN_27_DIO8_PWM4),
		MXP_PIN29_DIO9_PWM5(RoConnectionTypesE.M_DIO, 9 + Cnst.kRIO_DIO_CNT, MxpPinNbrsE.MXP_PIN_29_DIO9_PWM5),
		MXP_PIN31_DIO10_PWM6(RoConnectionTypesE.M_DIO, 10 + Cnst.kRIO_DIO_CNT, MxpPinNbrsE.MXP_PIN_31_DIO10_PWM6),
		MXP_PIN18_DIO11_PWM7(RoConnectionTypesE.M_DIO, 11 + Cnst.kRIO_DIO_CNT, MxpPinNbrsE.MXP_PIN_18_DIO11_PWM7),
		MXP_PIN22_DIO12_PWM8(RoConnectionTypesE.M_DIO, 12 + Cnst.kRIO_DIO_CNT, MxpPinNbrsE.MXP_PIN_22_DIO12_PWM8),
		MXP_PIN26_DIO13_PWM9(RoConnectionTypesE.M_DIO, 13 + Cnst.kRIO_DIO_CNT, MxpPinNbrsE.MXP_PIN_26_DIO13_PWM9),
		MXP_PIN32_DIO14_I2CSCL(RoConnectionTypesE.M_DIO, 14 + Cnst.kRIO_DIO_CNT, MxpPinNbrsE.MXP_PIN_32_DIO14_I2C_SCL),
		MXP_PIN34_DIO15_I2CSDA(RoConnectionTypesE.M_DIO, 15 + Cnst.kRIO_DIO_CNT, MxpPinNbrsE.MXP_PIN_34_DIO15_I2C_SDA),

		RIO_PWM0(RoConnectionTypesE.R_PWM, 0),
		RIO_PWM1(RoConnectionTypesE.R_PWM, 1),
		RIO_PWM2(RoConnectionTypesE.R_PWM, 2),
		RIO_PWM3(RoConnectionTypesE.R_PWM, 3),
		RIO_PWM4(RoConnectionTypesE.R_PWM, 4),
		RIO_PWM5(RoConnectionTypesE.R_PWM, 5),
		RIO_PWM6(RoConnectionTypesE.R_PWM, 6),
		RIO_PWM7(RoConnectionTypesE.R_PWM, 7),
		RIO_PWM8(RoConnectionTypesE.R_PWM, 8),
		RIO_PWM9(RoConnectionTypesE.R_PWM, 9),   
		MXP_PIN11_PWM0_DIO0(RoConnectionTypesE.M_PWM, 0 + Cnst.kRIO_PWM_CNT, MxpPinNbrsE.MXP_PIN_11_DIO0_PWM0),
		MXP_PIN13_PWM1_DIO1(RoConnectionTypesE.M_PWM, 1 + Cnst.kRIO_PWM_CNT, MxpPinNbrsE.MXP_PIN_13_DIO1_PWM1),
		MXP_PIN15_PWM2_DIO2(RoConnectionTypesE.M_PWM, 2 + Cnst.kRIO_PWM_CNT, MxpPinNbrsE.MXP_PIN_15_DIO2_PWM2),
		MXP_PIN17_PWM3_DIO3(RoConnectionTypesE.M_PWM, 3 + Cnst.kRIO_PWM_CNT, MxpPinNbrsE.MXP_PIN_17_DIO3_PWM3),
		MXP_PIN27_PWM4_DIO8(RoConnectionTypesE.M_PWM, 4 + Cnst.kRIO_PWM_CNT, MxpPinNbrsE.MXP_PIN_27_DIO8_PWM4),
		MXP_PIN29_PWM5_DIO9(RoConnectionTypesE.M_PWM, 5 + Cnst.kRIO_PWM_CNT, MxpPinNbrsE.MXP_PIN_29_DIO9_PWM5),
		MXP_PIN31_PWM6_DIO10(RoConnectionTypesE.M_PWM, 6 + Cnst.kRIO_PWM_CNT, MxpPinNbrsE.MXP_PIN_31_DIO10_PWM6),
		MXP_PIN18_PWM7_DIO11(RoConnectionTypesE.M_PWM, 7 + Cnst.kRIO_PWM_CNT, MxpPinNbrsE.MXP_PIN_18_DIO11_PWM7),
		MXP_PIN22_PWM8_DIO12(RoConnectionTypesE.M_PWM, 8 + Cnst.kRIO_PWM_CNT, MxpPinNbrsE.MXP_PIN_22_DIO12_PWM8),
		MXP_PIN26_PWM9_DIO13(RoConnectionTypesE.M_PWM, 9 + Cnst.kRIO_PWM_CNT, MxpPinNbrsE.MXP_PIN_26_DIO13_PWM9),

		RIO_RELAY0(RoConnectionTypesE.R_RELAY, 0),
		RIO_RELAY1(RoConnectionTypesE.R_RELAY, 1),
		RIO_RELAY2(RoConnectionTypesE.R_RELAY, 2),
		RIO_RELAY3(RoConnectionTypesE.R_RELAY, 3),

		PCM_SOL0(RoConnectionTypesE.PCM_SOLENOID, 0),
		PCM_SOL1(RoConnectionTypesE.PCM_SOLENOID, 1),
		PCM_SOL2(RoConnectionTypesE.PCM_SOLENOID, 2),
		PCM_SOL3(RoConnectionTypesE.PCM_SOLENOID, 3),
		PCM_SOL4(RoConnectionTypesE.PCM_SOLENOID, 4),
		PCM_SOL5(RoConnectionTypesE.PCM_SOLENOID, 5),
		PCM_SOL6(RoConnectionTypesE.PCM_SOLENOID, 6),
		PCM_SOL7(RoConnectionTypesE.PCM_SOLENOID, 7),

		SPI_CS0(RoConnectionTypesE.R_SPI_PORT, SPI.Port.kOnboardCS0),
		SPI_CS1(RoConnectionTypesE.R_SPI_PORT, SPI.Port.kOnboardCS1),
		SPI_CS2(RoConnectionTypesE.R_SPI_PORT, SPI.Port.kOnboardCS2),
		SPI_CS3(RoConnectionTypesE.R_SPI_PORT, SPI.Port.kOnboardCS3),
		SPI_MXP(RoConnectionTypesE.M_SPI_PORT, SPI.Port.kMXP),

		I2C_ONBOARD(RoConnectionTypesE.R_I2C_PORT, I2C.Port.kOnboard),
		I2C_MXP(RoConnectionTypesE.M_I2C_PORT, I2C.Port.kMXP),
		
		R_USB0(RoConnectionTypesE.R_USB_PORT, 0),
		R_USB1(RoConnectionTypesE.R_USB_PORT, 1),
		
		CAN_BUS_ID_00(RoConnectionTypesE.CAN_BUS, 0),
		CAN_BUS_ID_01(RoConnectionTypesE.CAN_BUS, 1),
		CAN_BUS_ID_02(RoConnectionTypesE.CAN_BUS, 2),
		CAN_BUS_ID_03(RoConnectionTypesE.CAN_BUS, 3),
		CAN_BUS_ID_04(RoConnectionTypesE.CAN_BUS, 4),
		CAN_BUS_ID_05(RoConnectionTypesE.CAN_BUS, 5),
		CAN_BUS_ID_06(RoConnectionTypesE.CAN_BUS, 6),
		CAN_BUS_ID_07(RoConnectionTypesE.CAN_BUS, 7),
		CAN_BUS_ID_08(RoConnectionTypesE.CAN_BUS, 8),
		CAN_BUS_ID_09(RoConnectionTypesE.CAN_BUS, 9),
		CAN_BUS_ID_10(RoConnectionTypesE.CAN_BUS, 10),
		CAN_BUS_ID_11(RoConnectionTypesE.CAN_BUS, 11),
		CAN_BUS_ID_12(RoConnectionTypesE.CAN_BUS, 12),
		CAN_BUS_ID_13(RoConnectionTypesE.CAN_BUS, 13),
		CAN_BUS_ID_14(RoConnectionTypesE.CAN_BUS, 14),
		CAN_BUS_ID_15(RoConnectionTypesE.CAN_BUS, 15),
		CAN_BUS_ID_16(RoConnectionTypesE.CAN_BUS, 16),
		CAN_BUS_ID_17(RoConnectionTypesE.CAN_BUS, 17),
		CAN_BUS_ID_18(RoConnectionTypesE.CAN_BUS, 18),
		CAN_BUS_ID_19(RoConnectionTypesE.CAN_BUS, 19),
		CAN_BUS_ID_20(RoConnectionTypesE.CAN_BUS, 20),
		CAN_BUS_ID_21(RoConnectionTypesE.CAN_BUS, 21),
		CAN_BUS_ID_22(RoConnectionTypesE.CAN_BUS, 22),
		CAN_BUS_ID_23(RoConnectionTypesE.CAN_BUS, 23),
		CAN_BUS_ID_24(RoConnectionTypesE.CAN_BUS, 24),
		CAN_BUS_ID_25(RoConnectionTypesE.CAN_BUS, 25),
		CAN_BUS_ID_26(RoConnectionTypesE.CAN_BUS, 26),
		CAN_BUS_ID_27(RoConnectionTypesE.CAN_BUS, 27),
		CAN_BUS_ID_28(RoConnectionTypesE.CAN_BUS, 28),
		CAN_BUS_ID_29(RoConnectionTypesE.CAN_BUS, 29),
		CAN_BUS_ID_30(RoConnectionTypesE.CAN_BUS, 30),
		CAN_BUS_ID_31(RoConnectionTypesE.CAN_BUS, 31),
		CAN_BUS_ID_32(RoConnectionTypesE.CAN_BUS, 32),
		CAN_BUS_ID_33(RoConnectionTypesE.CAN_BUS, 33),
		CAN_BUS_ID_34(RoConnectionTypesE.CAN_BUS, 34),
		CAN_BUS_ID_35(RoConnectionTypesE.CAN_BUS, 35),
		CAN_BUS_ID_36(RoConnectionTypesE.CAN_BUS, 36),
		CAN_BUS_ID_37(RoConnectionTypesE.CAN_BUS, 37),
		CAN_BUS_ID_38(RoConnectionTypesE.CAN_BUS, 38),
		CAN_BUS_ID_39(RoConnectionTypesE.CAN_BUS, 39),
		CAN_BUS_ID_40(RoConnectionTypesE.CAN_BUS, 40),
		CAN_BUS_ID_41(RoConnectionTypesE.CAN_BUS, 41),
		CAN_BUS_ID_42(RoConnectionTypesE.CAN_BUS, 42),
		CAN_BUS_ID_43(RoConnectionTypesE.CAN_BUS, 43),
		CAN_BUS_ID_44(RoConnectionTypesE.CAN_BUS, 44),
		CAN_BUS_ID_45(RoConnectionTypesE.CAN_BUS, 45),
		CAN_BUS_ID_46(RoConnectionTypesE.CAN_BUS, 46),
		CAN_BUS_ID_47(RoConnectionTypesE.CAN_BUS, 47),
		CAN_BUS_ID_48(RoConnectionTypesE.CAN_BUS, 48),
		CAN_BUS_ID_49(RoConnectionTypesE.CAN_BUS, 49),
		CAN_BUS_ID_50(RoConnectionTypesE.CAN_BUS, 50),
		CAN_BUS_ID_51(RoConnectionTypesE.CAN_BUS, 51),
		CAN_BUS_ID_52(RoConnectionTypesE.CAN_BUS, 52),
		CAN_BUS_ID_53(RoConnectionTypesE.CAN_BUS, 53),
		CAN_BUS_ID_54(RoConnectionTypesE.CAN_BUS, 54),
		CAN_BUS_ID_55(RoConnectionTypesE.CAN_BUS, 55),
		CAN_BUS_ID_56(RoConnectionTypesE.CAN_BUS, 56),
		CAN_BUS_ID_57(RoConnectionTypesE.CAN_BUS, 57),
		CAN_BUS_ID_58(RoConnectionTypesE.CAN_BUS, 58),
		CAN_BUS_ID_59(RoConnectionTypesE.CAN_BUS, 59),
		CAN_BUS_ID_60(RoConnectionTypesE.CAN_BUS, 60),
		CAN_BUS_ID_61(RoConnectionTypesE.CAN_BUS, 61),
		
//		IPADDR_RADIO(RoConnectionTypesE.IP_NETWORK, "10.7.44.1")
		;
		
		@Override
		public List<RoConnectionEntry> getList() { return roHwConnL; }
		public static List<RoConnectionEntry> staticGetList() { return roHwConnL; }
		@Override
		public RoConnectionEntry getEnt() { return roHwConnL.get(this.ordinal()); }
		@Override
		public String getEnumClassName() { return this.getClass().getSimpleName(); }
		@Override
		public String getListEntryClassName() { return roHwConnL.get(0).getClass().getSimpleName(); }

		String frmtStr = "%-23s [%s]";
		String frmtHdr = "%-23s [%s]";
		@Override
		public String toStringLog(String inpPrefix) {
			String prefix = (inpPrefix==null) ? "" : inpPrefix;
			String ans = String.format(prefix + frmtStr, this.name(), getEnt().toStringLog());
			return ans;
		}
		@Override
		public String toStringHdr(String inpPrefix) {
			String prefix = (inpPrefix==null) ? "" : inpPrefix;
			String ans = String.format(prefix + frmtHdr, this.getClass().getSimpleName(), getEnt().toStringHdr());
			return ans;
		}
		@Override
		public String toStringNotes() { 
			return(getEnumClassName() + " - saves no info in enum (see List<" + getListEntryClassName() +
				 	"> roHwConnL and enum's getList() and getEnt() methods)");
		}
	
		private RoNamedConnectionsE(RoConnectionTypesE connType, int frcNdx) {
			getList().add(this.ordinal(), 
					TmHdwrRoPhysBase.getInstance().new RoConnectionEntry(this, connType, frcNdx));
		}
		private RoNamedConnectionsE(RoConnectionTypesE connType, SPI.Port port) {
			getList().add(this.ordinal(), 
					TmHdwrRoPhysBase.getInstance().new RoConnectionEntry(this, connType, port));
		}
		private RoNamedConnectionsE(RoConnectionTypesE connType, I2C.Port port) {
			getList().add(this.ordinal(), 
					TmHdwrRoPhysBase.getInstance().new RoConnectionEntry(this, connType, port));
		}
		private RoNamedConnectionsE(RoConnectionTypesE connType, int frcNdx, MxpPinNbrsE mxpPin) {
			getList().add(this.ordinal(), 
					TmHdwrRoPhysBase.getInstance().new RoConnectionEntry(this, connType, frcNdx, mxpPin));
		}
		
	}


	public void showEverything() {
		TmToStringI.showEnumEverything(RoModuleTypesE.values());
		TmToStringI.showEnumEverything(RoConnectionTypesE.values());
		TmToStringI.showEnumEverything(RoNamedConnectionsE.values());
		TmToStringI.showEnumEverything(MxpPinNbrsE.values());
		TmToStringI.showListEverything(RoNamedConnectionsE.staticGetList());
		
	}

	@Override
	public void doForcedInstantiation() {
		super.doForcedInstantiation();
		//Access something from each of the enums in this class to force them 
		//(and their related List<> arrays) to be initialized
		//watch that optimization in the compiler doesn't decide to skip these
		RoNamedConnectionsE junk3 = RoNamedConnectionsE.CAN_BUS_ID_00;

//		RoDevicesDevConnAssignments.getInstance().doForcedInstantiation(); //creates the needed list structures
//		RoControlsDevConnAssignments.getInstance().doForcedInstantiation();
	
	}
	
	@Override
	public void doPopulate() {
		super.doPopulate();
		//nothing to do yet
	}

}
