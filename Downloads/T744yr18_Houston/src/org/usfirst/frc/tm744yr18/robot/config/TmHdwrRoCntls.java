package org.usfirst.frc.tm744yr18.robot.config;

import java.util.List;

import org.usfirst.frc.tm744yr18.bldVerInfo.TmVersionInfo;
import org.usfirst.frc.tm744yr18.robot.config.TmSdKeysI.SdKeysE;
import org.usfirst.frc.tm744yr18.robot.devices.TmFakeable_CanTalonSrx.EncoderCountsCapabilityE;
import org.usfirst.frc.tm744yr18.robot.devices.TmFakeable_CanTalonSrx.EncoderPolarityE;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmDsControlUserI;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmEnumWithListI;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmForcedInstantiateI;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmToStringI;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmToolsI;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmItemAvailabilityI.ItemAvailabilityE;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmItemAvailabilityI.ItemFakeableE;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmToolsI.P;
import org.usfirst.frc.tm744yr18.robot.subsystems.TmSsArm;
import org.usfirst.frc.tm744yr18.robot.subsystems.TmSsDriveTrain;

import edu.wpi.first.wpilibj.Relay;
import edu.wpi.first.wpilibj.command.Command;
import t744opts.Tm744Opts;
import t744opts.Tm744Opts.OptDefaults;

public class TmHdwrRoCntls extends TmHdwrRoPhys implements TmForcedInstantiateI, TmDsControlUserI {

	//used to change the configuration of devices that don't generate exceptions when they're
	//not present....  DoubleSolenoids, for example
	//RUNNING_ON_SOFTWARE_TEST_FIXTURE
	public static final boolean RUN_STF = Tm744Opts.isOptRunStf(); //Tm744Opts.OptDefaults.RUN_STF;

	/*---------------------------------------------------------
	 * getInstance stuff                                      
	 *---------------------------------------------------------*/
	/** 
	 * handle making the singleton instance of this class and giving
	 * others access to it
	 */
	private static TmHdwrRoCntls m_instance;

	public static synchronized TmHdwrRoCntls getInstance() {
		if (m_instance == null) {
			m_instance = new TmHdwrRoCntls();
		}
		return m_instance;
	}

	private TmHdwrRoCntls() {
		//calls TmHdwrRoPhysTools() automatically
		if ( ! (m_instance == null)) {
			P.println("Error!!! TmHdwrRoPhy.m_instance is being modified!!");
			P.println("         was: " + m_instance.toString());
			P.println("         now: " + this.toString());
		}
		m_instance = this;
	}
	/*----------------end of getInstance stuff----------------*/

	/**
	 * name the various electrical modules actually installed on the robot
	 * @author JudiA
	 *
	 */
	public static enum RoNamedModulesE implements TmToStringI, TmEnumWithListI<RoNamedModulesEntry> {
		RIO(ItemAvailabilityE.ACTIVE, ItemFakeableE.NOT_FAKEABLE, RoModuleTypesE.RIO_MOD, RoNamedConnectionsE.CAN_BUS_ID_00),
		
		PCM0(ItemAvailabilityE.ACTIVE, ItemFakeableE.NOT_FAKEABLE, RoModuleTypesE.PCM_MOD, RoNamedConnectionsE.CAN_BUS_ID_00),
//		PCM1(ItemAvailabilityE.ACTIVE, ItemFakeableE.NOT_FAKEABLE, RoModuleTypesE.PCM_MOD, RoNamedConnectionsE.CAN_BUS_ID_01),

		//for 2017, the transmissions are closer to the back than to the front
		//for 2017, the gear placer is at the front, the shooter is at the back
		DRV_MTRCNTL_MOD_FRONT_LEFT(ItemAvailabilityE.SEE_NAMED_CONTROL, ItemFakeableE.FAKEABLE, 
										RoModuleTypesE.CAN_TALON_SRX_MOD, RoNamedConnectionsE.CAN_BUS_ID_27),
		DRV_MTRCNTL_MOD_CENTER_LEFT(ItemAvailabilityE.SEE_NAMED_CONTROL, ItemFakeableE.FAKEABLE, 
										RoModuleTypesE.CAN_TALON_SRX_MOD, RoNamedConnectionsE.CAN_BUS_ID_28),
		DRV_MTRCNTL_MOD_REAR_LEFT(ItemAvailabilityE.SEE_NAMED_CONTROL, ItemFakeableE.FAKEABLE, 
										RoModuleTypesE.CAN_TALON_SRX_MOD, RoNamedConnectionsE.CAN_BUS_ID_25),
		DRV_MTRCNTL_MOD_FRONT_RIGHT(ItemAvailabilityE.SEE_NAMED_CONTROL, ItemFakeableE.FAKEABLE, 
										RoModuleTypesE.CAN_TALON_SRX_MOD, RoNamedConnectionsE.CAN_BUS_ID_23),
		DRV_MTRCNTL_MOD_CENTER_RIGHT(ItemAvailabilityE.SEE_NAMED_CONTROL, ItemFakeableE.FAKEABLE, 
										RoModuleTypesE.CAN_TALON_SRX_MOD, RoNamedConnectionsE.CAN_BUS_ID_24),
		DRV_MTRCNTL_MOD_REAR_RIGHT(ItemAvailabilityE.SEE_NAMED_CONTROL, ItemFakeableE.FAKEABLE, 
										RoModuleTypesE.CAN_TALON_SRX_MOD, RoNamedConnectionsE.CAN_BUS_ID_26),
		
//		ARM_MTRCNTL_MOD_CLAW_LEFT(ItemAvailabilityE.SEE_NAMED_CONTROL, ItemFakeableE.FAKEABLE, 
//				RoModuleTypesE.CAN_TALON_SRX_MOD, RoNamedConnectionsE.CAN_BUS_ID_42),
//		ARM_MTRCNTL_MOD_CLAW_RIGHT(ItemAvailabilityE.SEE_NAMED_CONTROL, ItemFakeableE.FAKEABLE, 
//				RoModuleTypesE.CAN_TALON_SRX_MOD, RoNamedConnectionsE.CAN_BUS_ID_43),
//		ARM_MTRCNTL_MOD_STAGE1_EXTENDER(ItemAvailabilityE.SEE_NAMED_CONTROL, ItemFakeableE.FAKEABLE, 
//				RoModuleTypesE.CAN_TALON_SRX_MOD, RoNamedConnectionsE.CAN_BUS_ID_44),
//		ARM_MTRCNTL_MOD_STAGE2_LIFTER(ItemAvailabilityE.SEE_NAMED_CONTROL, ItemFakeableE.FAKEABLE, 
//				RoModuleTypesE.CAN_TALON_SRX_MOD, RoNamedConnectionsE.CAN_BUS_ID_45),

		ARM_MTRCNTL_MOD_CLAW_LEFT(ItemAvailabilityE.SEE_NAMED_CONTROL, ItemFakeableE.FAKEABLE, 
										RoModuleTypesE.CAN_TALON_SRX_MOD, 
			(OptDefaults.RUN_STG1AUX_MTR_FROM_ARM_CLAW_RIGHT_TALON_ETC ? RoNamedConnectionsE.CAN_BUS_ID_44
																	   : RoNamedConnectionsE.CAN_BUS_ID_42)),
		ARM_MTRCNTL_MOD_CLAW_RIGHT(ItemAvailabilityE.SEE_NAMED_CONTROL, ItemFakeableE.FAKEABLE, 
										RoModuleTypesE.CAN_TALON_SRX_MOD, 
			(OptDefaults.RUN_STG1AUX_MTR_FROM_ARM_CLAW_RIGHT_TALON_ETC ? RoNamedConnectionsE.CAN_BUS_ID_45
												   					   : RoNamedConnectionsE.CAN_BUS_ID_43)),
		
		//2018-03-06_00-46 had to swap can id's for these //Tm744Opts.OptDefaults.ARM_CASCADING
		ARM_MTRCNTL_MOD_STAGE1_EXTENDER(ItemAvailabilityE.SEE_NAMED_CONTROL, ItemFakeableE.FAKEABLE, 
										RoModuleTypesE.CAN_TALON_SRX_MOD, 
			(OptDefaults.RUN_STG1AUX_MTR_FROM_ARM_CLAW_RIGHT_TALON_ETC ? RoNamedConnectionsE.CAN_BUS_ID_43
							   					   					   : RoNamedConnectionsE.CAN_BUS_ID_45)),
		ARM_MTRCNTL_MOD_STAGE2_LIFTER(ItemAvailabilityE.SEE_NAMED_CONTROL, ItemFakeableE.FAKEABLE, 
										RoModuleTypesE.CAN_TALON_SRX_MOD, 
			(OptDefaults.RUN_STG1AUX_MTR_FROM_ARM_CLAW_RIGHT_TALON_ETC ? RoNamedConnectionsE.CAN_BUS_ID_42
																	   : RoNamedConnectionsE.CAN_BUS_ID_44)),
		
		GRABBER_MTRCNTL_MOD_LEFT(ItemAvailabilityE.SEE_NAMED_CONTROL, ItemFakeableE.FAKEABLE, 
										RoModuleTypesE.CAN_TALON_SRX_MOD, RoNamedConnectionsE.CAN_BUS_ID_40),
		GRABBER_MTRCNTL_MOD_RIGHT(ItemAvailabilityE.SEE_NAMED_CONTROL, ItemFakeableE.FAKEABLE, 
										RoModuleTypesE.CAN_TALON_SRX_MOD, RoNamedConnectionsE.CAN_BUS_ID_41),
		;

		
		@Override
		public List<RoNamedModulesEntry> getList() { return roNamedModulesList; }
		public static List<RoNamedModulesEntry> staticGetList() { return roNamedModulesList; }
		@Override
		public RoNamedModulesEntry getEnt() { return getList().get(this.ordinal()); }
		@Override
		public String getEnumClassName() { return this.getClass().getSimpleName(); }

		@Override
		public String getListEntryClassName() { return getEnt().getClass().getSimpleName(); }
		
		//should normally access these through the related list entry; exception is in RoNamedControlsE enum item parms
		protected final RoModuleTypesE eModuleType;
		protected ItemAvailabilityE eModAvail;
		protected final ItemFakeableE eModFakeable;
		protected final RoNamedConnectionsE eModNamedConn;
		
		private RoNamedModulesE(ItemAvailabilityE modAvail, ItemFakeableE modFakeable, RoModuleTypesE modType, RoNamedConnectionsE modNamedConn) {
			eModuleType = modType;
			eModAvail = modAvail;
			eModFakeable = modFakeable;
			eModNamedConn = modNamedConn;
			getList().add(this.ordinal(), TmHdwrRoPhysBase.getInstance().new RoNamedModulesEntry(this, modAvail, modFakeable, modType, modNamedConn));
		}
		
		String frmtStr = "%-31s [ListEnt[%2d]: %s]";
		String frmtHdr = "%-31s [             %s]";
		@Override
		public String toStringLog(String inpPrefix) {
			String prefix = (inpPrefix==null) ? "" : inpPrefix;
			String ans = String.format(prefix + frmtStr, this.name(), 
							this.ordinal(), getEnt().toStringLog());
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
  			return(getEnumClassName() + " - saves no info in enum (see List<" + getListEntryClassName() + "> roNamedModulesList and enum's getList() and getEnt() methods)");
		}
		
	}
	
	public static class CntlCnst {
		public static final double DEFAULT_MTR_TUNING_FACTOR = 1.0;
	}

	/**
	 * name the various controls on the robot
	 * @author JudiA
	 *
	 */
	public static enum RoNamedControlsE implements TmToStringI, TmEnumWithListI<RoNamedControlsEntry> {
		//Note: on practice bot, the encoder itself is on the rear wheel, but it's cabled
		//      to the talon for the front wheel.  Not sure what happened on real bot.'
		//      Since the front and rear wheels do exactly the same thing, this shouldn't matter.
		DRV_MTR_FRONT_LEFT_WITH_ENC(RoControlCfgStyleE.MANUAL_CFG,
				//note: x=a?b:c; means if(a){x=b;}else{x=c;}
				(RUN_STF ? 
						ItemAvailabilityE.ACTIVE : 
						ItemAvailabilityE.ACTIVE), ItemFakeableE.FAKEABLE, 
							RoControlTypesE.CAN_BUS_MOTOR_CONTROLLER, 
							RoNamedModulesE.DRV_MTRCNTL_MOD_FRONT_LEFT, 
							ConnCnst.USE_MODULE_CAN_ID, //(true ? null : RoNamedConnectionsE.CAN_BUS_ID_27), 
							FrcAndJoysticksVsRoDrvMtrDirVsRobotDirE.FOR_FRC_JS_MTR_IN_REVERSE_DIR_FOR_ROBOT_FORWARD,
							MotorPosPercOutVsThingDirE.POS_MTR_PERCENT_OUT_MOVES_THING_BACKWARD,
							RoMtrInversionE.INVERT_MOTOR, CntlCnst.DEFAULT_MTR_TUNING_FACTOR, 
							SdKeysE.KEY_DRIVE_MOTOR_FRONT_LEFT_MODE,
							SdKeysE.KEY_DRIVE_MOTOR_FRONT_LEFT_PERCENT_OUT, 
							SdKeysE.KEY_DRIVE_MOTOR_FRONT_LEFT_AMPS,
							RoMtrContinuousAmpsE.MAX_CONTINUOUS_AMPS_40,
							RoMtrPeakAmpsE.MAX_PEAK_AMPS_42,
							RoMtrPeakAmpsDurationE.MAX_PEAK_DURATION_100MS,
							RoMtrHasEncoderE.HAS_ENCODER, SdKeysE.KEY_DRIVE_ENCODER_LEFT, 
							TmSsDriveTrain.DrvTrainCnst.DRV_ENCODER_MAX_REVS_PER_SECOND, 
							TmSsDriveTrain.DrvTrainCnst.DRV_ENCODER_COUNTS_PER_REVOLUTION, 
							TmSsDriveTrain.DrvTrainCnst.DRV_ENCODER_FEET_PER_REVOLUTION,
							EncoderIncrVsThingDirE.ENC_INCREASING_WHEN_THING_MOVING_BACKWARD, //thing=robot
							EncoderPolarityE.MATCHES_MOTOR, 
							EncoderCountsCapabilityE.ABSOLUTE_USED_AS_RELATIVE),							
		//2018-02-15_20-55 -- we're swapping polarity of the connection between the talon and 
		//                    the robot so that the center motor can be treated just like the 
		//                    other two and can use Follower mode
		DRV_MTR_CENTER_LEFT(RoControlCfgStyleE.MANUAL_CFG, 
				//note: x=a?b:c; means if(a){x=b;}else{x=c;}
				(RUN_STF ? ItemAvailabilityE.USE_FAKE : ItemAvailabilityE.ACTIVE), ItemFakeableE.FAKEABLE, 
							RoControlTypesE.CAN_BUS_MOTOR_CONTROLLER, 
							RoNamedModulesE.DRV_MTRCNTL_MOD_CENTER_LEFT, 
							ConnCnst.USE_MODULE_CAN_ID, //(true ? null : RoNamedConnectionsE.CAN_BUS_ID_28/*28*/), 
							FrcAndJoysticksVsRoDrvMtrDirVsRobotDirE.FOR_FRC_JS_MTR_IN_REVERSE_DIR_FOR_ROBOT_FORWARD,
							MotorPosPercOutVsThingDirE.POS_MTR_PERCENT_OUT_MOVES_THING_BACKWARD,
							RoMtrInversionE.INVERT_MOTOR, //.NO_INVERT_MOTOR, 
							CntlCnst.DEFAULT_MTR_TUNING_FACTOR, 
							SdKeysE.KEY_DRIVE_MOTOR_CENTER_LEFT_MODE, 
							SdKeysE.KEY_DRIVE_MOTOR_CENTER_LEFT_PERCENT_OUT, 
							SdKeysE.KEY_DRIVE_MOTOR_CENTER_LEFT_AMPS,
							RoMtrContinuousAmpsE.MAX_CONTINUOUS_AMPS_40,
							RoMtrPeakAmpsE.MAX_PEAK_AMPS_42,
							RoMtrPeakAmpsDurationE.MAX_PEAK_DURATION_100MS),
		DRV_MTR_REAR_LEFT(RoControlCfgStyleE.MANUAL_CFG, 
				//note: x=a?b:c; means if(a){x=b;}else{x=c;}
				(RUN_STF ? ItemAvailabilityE.USE_FAKE : ItemAvailabilityE.ACTIVE), ItemFakeableE.FAKEABLE, 
//							ItemAvailabilityE.ACTIVE, ItemFakeableE.FAKEABLE,
							RoControlTypesE.CAN_BUS_MOTOR_CONTROLLER, 
							RoNamedModulesE.DRV_MTRCNTL_MOD_REAR_LEFT, 
							ConnCnst.USE_MODULE_CAN_ID, //(true ? null : RoNamedConnectionsE.CAN_BUS_ID_25/*25*/), 
							FrcAndJoysticksVsRoDrvMtrDirVsRobotDirE.FOR_FRC_JS_MTR_IN_REVERSE_DIR_FOR_ROBOT_FORWARD,
							MotorPosPercOutVsThingDirE.POS_MTR_PERCENT_OUT_MOVES_THING_BACKWARD,
							RoMtrInversionE.INVERT_MOTOR, CntlCnst.DEFAULT_MTR_TUNING_FACTOR, 
							SdKeysE.KEY_DRIVE_MOTOR_REAR_LEFT_MODE, 
							SdKeysE.KEY_DRIVE_MOTOR_REAR_LEFT_PERCENT_OUT, 
							SdKeysE.KEY_DRIVE_MOTOR_REAR_LEFT_AMPS,
							RoMtrContinuousAmpsE.MAX_CONTINUOUS_AMPS_40,
							RoMtrPeakAmpsE.MAX_PEAK_AMPS_42,
							RoMtrPeakAmpsDurationE.MAX_PEAK_DURATION_100MS), 
							//RoMtrHasEncoderE.HAS_ENCODER, SdKeysE.KEY_DRIVE_ENCODER_LEFT, 
							//TmSsDriveTrain.DrvTrainCnst.DRV_ENCODER_MAX_REVS_PER_SECOND, 
							//TmSsDriveTrain.DrvTrainCnst.DRV_ENCODER_COUNTS_PER_REVOLUTION, 
							//TmSsDriveTrain.DrvTrainCnst.DRV_ENCODER_FEET_PER_REVOLUTION, 
							//EncoderIncrVsThingDirE.TBD,
							//EncoderPolarityE.MATCHES_MOTOR, 
							//EncoderCountsCapabilityE.ABSOLUTE_USED_AS_RELATIVE),
		
		//Note: on practice bot, the encoder itself is on the rear wheel, but it's cabled
		//      to the talon for the front wheel.  Not sure what happened on real bot.'
		//      Since the front and rear wheels do exactly the same thing, this shouldn't matter.
		DRV_MTR_FRONT_RIGHT_WITH_ENC(RoControlCfgStyleE.MANUAL_CFG, 
				//note: x=a?b:c; means if(a){x=b;}else{x=c;}
				(RUN_STF ? ItemAvailabilityE.USE_FAKE : ItemAvailabilityE.ACTIVE), ItemFakeableE.FAKEABLE, 
							RoControlTypesE.CAN_BUS_MOTOR_CONTROLLER, 
							RoNamedModulesE.DRV_MTRCNTL_MOD_FRONT_RIGHT, 
							ConnCnst.USE_MODULE_CAN_ID, //(true ? null : RoNamedConnectionsE.CAN_BUS_ID_23), 
							FrcAndJoysticksVsRoDrvMtrDirVsRobotDirE.FOR_FRC_JS_MTR_IN_FORWARD_DIR_FOR_ROBOT_FORWARD,
							MotorPosPercOutVsThingDirE.POS_MTR_PERCENT_OUT_MOVES_THING_FORWARD,
							RoMtrInversionE.INVERT_MOTOR, CntlCnst.DEFAULT_MTR_TUNING_FACTOR, 
							SdKeysE.KEY_DRIVE_MOTOR_FRONT_RIGHT_MODE, 
							SdKeysE.KEY_DRIVE_MOTOR_FRONT_RIGHT_PERCENT_OUT, 
							SdKeysE.KEY_DRIVE_MOTOR_FRONT_RIGHT_AMPS,
							RoMtrContinuousAmpsE.MAX_CONTINUOUS_AMPS_40,
							RoMtrPeakAmpsE.MAX_PEAK_AMPS_42,
							RoMtrPeakAmpsDurationE.MAX_PEAK_DURATION_100MS,
							RoMtrHasEncoderE.HAS_ENCODER, SdKeysE.KEY_DRIVE_ENCODER_RIGHT, 
							TmSsDriveTrain.DrvTrainCnst.DRV_ENCODER_MAX_REVS_PER_SECOND, 
							TmSsDriveTrain.DrvTrainCnst.DRV_ENCODER_COUNTS_PER_REVOLUTION, 
							TmSsDriveTrain.DrvTrainCnst.DRV_ENCODER_FEET_PER_REVOLUTION, 
							EncoderIncrVsThingDirE.ENC_INCREASING_WHEN_THING_MOVING_FORWARD, //thing=robot
							EncoderPolarityE.MATCHES_MOTOR, 
							EncoderCountsCapabilityE.ABSOLUTE_USED_AS_RELATIVE),
		//2018-02-15_20-55 -- we're swapping polarity of the connection between the talon and 
		//                    the robot so that the center motor can be treated just like the 
		//                    other two and can use Follower mode
		DRV_MTR_CENTER_RIGHT(RoControlCfgStyleE.MANUAL_CFG, 
				//note: x=a?b:c; means if(a){x=b;}else{x=c;}
				(RUN_STF ? ItemAvailabilityE.USE_FAKE : ItemAvailabilityE.ACTIVE), ItemFakeableE.FAKEABLE,				
							RoControlTypesE.CAN_BUS_MOTOR_CONTROLLER, 
							RoNamedModulesE.DRV_MTRCNTL_MOD_CENTER_RIGHT, 
							ConnCnst.USE_MODULE_CAN_ID, //(true ? null : RoNamedConnectionsE.CAN_BUS_ID_24), 
							FrcAndJoysticksVsRoDrvMtrDirVsRobotDirE.FOR_FRC_JS_MTR_IN_FORWARD_DIR_FOR_ROBOT_FORWARD, //.MTR_IN_REVERSE_DIR_FOR_ROBOT_FORWARD,
							MotorPosPercOutVsThingDirE.POS_MTR_PERCENT_OUT_MOVES_THING_FORWARD,
							RoMtrInversionE.INVERT_MOTOR, //.NO_INVERT_MOTOR, 
							CntlCnst.DEFAULT_MTR_TUNING_FACTOR, 
							SdKeysE.KEY_DRIVE_MOTOR_CENTER_RIGHT_MODE, 
							SdKeysE.KEY_DRIVE_MOTOR_CENTER_RIGHT_PERCENT_OUT, 
							SdKeysE.KEY_DRIVE_MOTOR_CENTER_RIGHT_AMPS,
							RoMtrContinuousAmpsE.MAX_CONTINUOUS_AMPS_40,
							RoMtrPeakAmpsE.MAX_PEAK_AMPS_42,
							RoMtrPeakAmpsDurationE.MAX_PEAK_DURATION_100MS
							),
		DRV_MTR_REAR_RIGHT(RoControlCfgStyleE.MANUAL_CFG, 
				//note: x=a?b:c; means if(a){x=b;}else{x=c;}
				(RUN_STF ? ItemAvailabilityE.USE_FAKE : ItemAvailabilityE.ACTIVE), ItemFakeableE.FAKEABLE, 
							RoControlTypesE.CAN_BUS_MOTOR_CONTROLLER, 
							RoNamedModulesE.DRV_MTRCNTL_MOD_REAR_RIGHT, 
							ConnCnst.USE_MODULE_CAN_ID, //(true ? null : RoNamedConnectionsE.CAN_BUS_ID_26), 
							FrcAndJoysticksVsRoDrvMtrDirVsRobotDirE.FOR_FRC_JS_MTR_IN_FORWARD_DIR_FOR_ROBOT_FORWARD,
							MotorPosPercOutVsThingDirE.POS_MTR_PERCENT_OUT_MOVES_THING_FORWARD,
							RoMtrInversionE.INVERT_MOTOR, CntlCnst.DEFAULT_MTR_TUNING_FACTOR, 
							SdKeysE.KEY_DRIVE_MOTOR_REAR_RIGHT_MODE, 
							SdKeysE.KEY_DRIVE_MOTOR_REAR_RIGHT_PERCENT_OUT, 
							SdKeysE.KEY_DRIVE_MOTOR_REAR_RIGHT_AMPS,
							RoMtrContinuousAmpsE.MAX_CONTINUOUS_AMPS_40,
							RoMtrPeakAmpsE.MAX_PEAK_AMPS_42,
							RoMtrPeakAmpsDurationE.MAX_PEAK_DURATION_100MS), 
							//RoMtrHasEncoderE.HAS_ENCODER, SdKeysE.KEY_DRIVE_ENCODER_RIGHT, 
							//TmSsDriveTrain.DrvTrainCnst.DRV_ENCODER_MAX_REVS_PER_SECOND, 
							//TmSsDriveTrain.DrvTrainCnst.DRV_ENCODER_COUNTS_PER_REVOLUTION, 
							//TmSsDriveTrain.DrvTrainCnst.DRV_ENCODER_FEET_PER_REVOLUTION, 
							//EncoderIncrVsThingDirE.TBD,
							//EncoderPolarityE.MATCHES_MOTOR, 
							//EncoderCountsCapabilityE.ABSOLUTE_USED_AS_RELATIVE),

		DRV_NAV_GYRO(RoControlCfgStyleE.MANUAL_CFG, ItemAvailabilityE.ACTIVE, ItemFakeableE.NOT_FAKEABLE, 
							RoControlTypesE.OTHER, 
							RoNamedModulesE.RIO, RoNamedConnectionsE.SPI_CS0),

		DRV_SHIFTER_HIGH_GEAR(RoControlCfgStyleE.MANUAL_CFG, 
							//Note: x=a?b:c; means if(a){x=b;}else{x=c;} -- conditional operator [744conditionalOp]
							(RUN_STF ? ItemAvailabilityE.USE_FAKE : ItemAvailabilityE.ACTIVE), ItemFakeableE.FAKEABLE, 
							RoControlTypesE.OTHER, 
							RoNamedModulesE.PCM0, RoNamedConnectionsE.PCM_SOL1),
		DRV_SHIFTER_LOW_GEAR(RoControlCfgStyleE.MANUAL_CFG, 
							//Note: x=a?b:c; means if(a){x=b;}else{x=c;} -- conditional operator [744conditionalOp]
							((RUN_STF || (! Tm744Opts.isPcm0Installed())) ? ItemAvailabilityE.USE_FAKE : 
																			ItemAvailabilityE.ACTIVE), 
							ItemFakeableE.FAKEABLE, 
							RoControlTypesE.OTHER, 
							RoNamedModulesE.PCM0, RoNamedConnectionsE.PCM_SOL0),

		//2018-02-18 lab testing showed these were reversed.  Fix my changing open from sol2 to sol3
		//               and close from sol3 to sol2
		GRABBER_CLAMP_ASSIST_BOTH_SIDES_OPEN__PISTON_RETRACTED(RoControlCfgStyleE.MANUAL_CFG, 
				//Note: x=a?b:c; means if(a){x=b;}else{x=c;} -- conditional operator [744conditionalOp]
				((RUN_STF  || (! Tm744Opts.isPcm0Installed())) ? ItemAvailabilityE.USE_FAKE : ItemAvailabilityE.ACTIVE), ItemFakeableE.FAKEABLE, 
				RoControlTypesE.OTHER, 
				RoNamedModulesE.PCM0, RoNamedConnectionsE.PCM_SOL2),
		GRABBER_CLAMP_ASSIST_BOTH_SIDES_CLOSE__PISTON_EXTENDED(RoControlCfgStyleE.MANUAL_CFG, 
				//Note: x=a?b:c; means if(a){x=b;}else{x=c;} -- conditional operator [744conditionalOp]
				((RUN_STF  || (! Tm744Opts.isPcm0Installed())) ? ItemAvailabilityE.USE_FAKE : ItemAvailabilityE.ACTIVE), ItemFakeableE.FAKEABLE, 
				RoControlTypesE.OTHER, 
				RoNamedModulesE.PCM0, RoNamedConnectionsE.PCM_SOL3),

		GRABBER_LIFT_BOTH_SIDES_RAISE(RoControlCfgStyleE.MANUAL_CFG, 
				//Note: x=a?b:c; means if(a){x=b;}else{x=c;} -- conditional operator [744conditionalOp]
				((RUN_STF  || (! Tm744Opts.isPcm0Installed())) ? ItemAvailabilityE.USE_FAKE : ItemAvailabilityE.ACTIVE), ItemFakeableE.FAKEABLE, 
				RoControlTypesE.OTHER, 
				RoNamedModulesE.PCM0, RoNamedConnectionsE.PCM_SOL4),
		GRABBER_LIFT_BOTH_SIDES_LOWER(RoControlCfgStyleE.MANUAL_CFG, 
				//Note: x=a?b:c; means if(a){x=b;}else{x=c;} -- conditional operator [744conditionalOp]
				((RUN_STF  || (! Tm744Opts.isPcm0Installed())) ? ItemAvailabilityE.USE_FAKE : ItemAvailabilityE.ACTIVE), ItemFakeableE.FAKEABLE, 
				RoControlTypesE.OTHER, 
				RoNamedModulesE.PCM0, RoNamedConnectionsE.PCM_SOL5),
		
		GRABBER_UP_FULL_LIMIT_SWITCH(RoControlCfgStyleE.MANUAL_CFG, ItemAvailabilityE.ACTIVE, ItemFakeableE.NOT_FAKEABLE, 
				RoControlTypesE.OTHER, 
				RoNamedModulesE.RIO, RoNamedConnectionsE.RIO_DIO5),

		
		
		//2018-02-18 lab testing wedge controls reversed.  Fix by changing deploy from sol6 to
		//           sol7 and retract from sol7 to sol6.
		GRABBER_WEDGE_DEPLOY(RoControlCfgStyleE.MANUAL_CFG, 
				//Note: x=a?b:c; means if(a){x=b;}else{x=c;} -- conditional operator [744conditionalOp]
				((RUN_STF  || (! Tm744Opts.isPcm0Installed())) ? ItemAvailabilityE.USE_FAKE : ItemAvailabilityE.ACTIVE), ItemFakeableE.FAKEABLE, 
				RoControlTypesE.OTHER, 
				RoNamedModulesE.PCM0, RoNamedConnectionsE.PCM_SOL7),
		GRABBER_WEDGE_RETRACT(RoControlCfgStyleE.MANUAL_CFG, 
				//Note: x=a?b:c; means if(a){x=b;}else{x=c;} -- conditional operator [744conditionalOp]
				((RUN_STF  || (! Tm744Opts.isPcm0Installed())) ? ItemAvailabilityE.USE_FAKE : ItemAvailabilityE.ACTIVE), ItemFakeableE.FAKEABLE, 
				RoControlTypesE.OTHER, 
				RoNamedModulesE.PCM0, RoNamedConnectionsE.PCM_SOL6),

		GRABBER_WEDGE_RETRACTED_LIMIT_SWITCH(RoControlCfgStyleE.MANUAL_CFG, ItemAvailabilityE.ACTIVE, ItemFakeableE.NOT_FAKEABLE, 
				RoControlTypesE.OTHER, 
				RoNamedModulesE.RIO, RoNamedConnectionsE.RIO_DIO4),

		// pneumatics for arm replaced with motors that work like lifter's
		 // grabber now has a "wedge" that slides back and forth with pneumatics

		//2018-02-18 lab testing showed LT/RT buttons reversed. Fix by inverting the motors here.
		GRABBER_MOTOR_LEFT(RoControlCfgStyleE.MANUAL_CFG, 
				//note: x=a?b:c; means if(a){x=b;}else{x=c;}
				(RUN_STF ? ItemAvailabilityE.USE_FAKE : ItemAvailabilityE.ACTIVE), ItemFakeableE.FAKEABLE, 
				RoControlTypesE.CAN_BUS_MOTOR_CONTROLLER, 
				RoNamedModulesE.GRABBER_MTRCNTL_MOD_LEFT, 
				ConnCnst.USE_MODULE_CAN_ID, //(true ? null : RoNamedConnectionsE.CAN_BUS_ID_40), 
				FrcAndJoysticksVsRoDrvMtrDirVsRobotDirE.FOR_FRC_JS_NOT_A_DRIVE_MOTOR,
				MotorPosPercOutVsThingDirE.POS_MTR_PERCENT_OUT_MOVES_THING_IN, //thing = cube //assume joystick to left=-1, to right=1
				RoMtrInversionE.INVERT_MOTOR, CntlCnst.DEFAULT_MTR_TUNING_FACTOR, 
				null, //SdKeysE.KEY_GRABBER_, //mode (should always be Percent Output)
				SdKeysE.KEY_GRABBER_MTR_LEFT_PERCENTOUT, 
				null, //SdKeysE.KEY_GRABBER_, //amps
				RoMtrContinuousAmpsE.MAX_CONTINUOUS_AMPS_20,
				RoMtrPeakAmpsE.MAX_PEAK_AMPS_32,
				RoMtrPeakAmpsDurationE.MAX_PEAK_DURATION_100MS),
		GRABBER_MOTOR_RIGHT(RoControlCfgStyleE.MANUAL_CFG, 
				//note: x=a?b:c; means if(a){x=b;}else{x=c;}
				(RUN_STF ? ItemAvailabilityE.USE_FAKE : ItemAvailabilityE.ACTIVE), ItemFakeableE.FAKEABLE, 
				RoControlTypesE.CAN_BUS_MOTOR_CONTROLLER, 
				RoNamedModulesE.GRABBER_MTRCNTL_MOD_RIGHT, 
				ConnCnst.USE_MODULE_CAN_ID, //(true ? null : RoNamedConnectionsE.CAN_BUS_ID_41), 
				FrcAndJoysticksVsRoDrvMtrDirVsRobotDirE.FOR_FRC_JS_NOT_A_DRIVE_MOTOR,
				MotorPosPercOutVsThingDirE.POS_MTR_PERCENT_OUT_MOVES_THING_OUT, //thing = cube //assume joystick to left=-1, to right=1
				RoMtrInversionE.INVERT_MOTOR, CntlCnst.DEFAULT_MTR_TUNING_FACTOR, 
				null, //SdKeysE.KEY_GRABBER_, //mode (should always be Percent Output)
				SdKeysE.KEY_GRABBER_MTR_RIGHT_PERCENTOUT, 
				null, //SdKeysE.KEY_GRABBER_, //amps
		        RoMtrContinuousAmpsE.MAX_CONTINUOUS_AMPS_20,
				RoMtrPeakAmpsE.MAX_PEAK_AMPS_32,
				RoMtrPeakAmpsDurationE.MAX_PEAK_DURATION_100MS),
		
		FLASHLIGHT_RELAY(RoControlCfgStyleE.MANUAL_CFG, ItemAvailabilityE.ACTIVE, ItemFakeableE.NOT_FAKEABLE, 
							RoControlTypesE.OTHER, 
							RoNamedModulesE.RIO, RoNamedConnectionsE.RIO_RELAY1, Relay.Direction.kBoth),
		
		COMPRESSOR_SENSOR(RoControlCfgStyleE.MANUAL_CFG, ItemAvailabilityE.ACTIVE, ItemFakeableE.NOT_FAKEABLE, 
				RoControlTypesE.OTHER, 
				RoNamedModulesE.RIO, RoNamedConnectionsE.RIO_DIO7),

		//oops! no analog input stuff coded yet!!
		COMPRESSOR_SENSOR_ANALOG(RoControlCfgStyleE.MANUAL_CFG, ItemAvailabilityE.ACTIVE, ItemFakeableE.NOT_FAKEABLE,
				RoControlTypesE.OTHER, 
				RoNamedModulesE.RIO, RoNamedConnectionsE.RIO_AI0),

		COMPRESSOR_RELAY(RoControlCfgStyleE.MANUAL_CFG, ItemAvailabilityE.ACTIVE, ItemFakeableE.NOT_FAKEABLE, 
				RoControlTypesE.OTHER, 
				RoNamedModulesE.RIO, RoNamedConnectionsE.RIO_RELAY0, Relay.Direction.kBoth),

		//2018-02-06 lab testing: powered motor such that negative joystick readings (positive PercentOutput values)
		//							moved the stage1 extender up
		//						  encoder readings decreased as stage1 went up, increased as it went down
		//2018-02-18_00-54 lab testing: powered motor the opposite way
		//				not checked:  encoder readings decreased as stage1 went up, increased as it went down
		//        changed from NO_INVERT to INVERT and changed encoder from OPPOSITE_OF_MOTOR to MATCHES_MOTOR
		ARM_MTR_STAGE1_EXTENDER(RoControlCfgStyleE.MANUAL_CFG, 
				//note: x=a?b:c; means if(a){x=b;}else{x=c;}
				(RUN_STF ? ItemAvailabilityE.USE_FAKE : ItemAvailabilityE.ACTIVE), ItemFakeableE.FAKEABLE, 
				RoControlTypesE.CAN_BUS_MOTOR_CONTROLLER, 
				RoNamedModulesE.ARM_MTRCNTL_MOD_STAGE1_EXTENDER, 
				ConnCnst.USE_MODULE_CAN_ID, //(true ? null : RoNamedConnectionsE.CAN_BUS_ID_44/*31*/), 
				FrcAndJoysticksVsRoDrvMtrDirVsRobotDirE.FOR_FRC_JS_NOT_A_DRIVE_MOTOR,
				MotorPosPercOutVsThingDirE.POS_MTR_PERCENT_OUT_MOVES_THING_UP,
				RoMtrInversionE.INVERT_MOTOR, CntlCnst.DEFAULT_MTR_TUNING_FACTOR, 
				SdKeysE.KEY_ARM_STAGE1_MTR_MODE, 
				SdKeysE.KEY_ARM_STAGE1_MTR_OUTPUT_PERCENT, 
				SdKeysE.KEY_ARM_STAGE1_MTR_AMPS,
				RoMtrContinuousAmpsE.MAX_CONTINUOUS_AMPS_25,
				RoMtrPeakAmpsE.MAX_PEAK_AMPS_25,
				RoMtrPeakAmpsDurationE.MAX_PEAK_DURATION_100MS, 
			(Tm744Opts.OptDefaults.ARM_ENC_FAKE ? RoMtrHasEncoderE.USE_FAKE_ENCODER : RoMtrHasEncoderE.HAS_ENCODER), 
				SdKeysE.KEY_ARM_STAGE1_ENCODER_RDG, 
				TmSsArm.Cnst.ARM_STAGE1_ENCODER_MAX_REVS_PER_SECOND, 
				TmSsArm.Cnst.ARM_STAGE1_ENCODER_COUNTS_PER_REVOLUTION, 
				TmSsArm.Cnst.ARM_STAGE1_ENCODER_FEET_PER_REVOLUTION, 
//				EncoderIncrVsThingDirE.ENC_INCREASING_WHEN_THING_MOVING_DOWN,
				(OptDefaults.RUN_STG1AUX_MTR_FROM_ARM_CLAW_RIGHT_TALON_ETC ? 
						EncoderIncrVsThingDirE.ENC_INCREASING_WHEN_THING_MOVING_DOWN :
							(OptDefaults.ARM_CASCADING ?
									EncoderIncrVsThingDirE.ENC_INCREASING_WHEN_THING_MOVING_UP :
									EncoderIncrVsThingDirE.ENC_INCREASING_WHEN_THING_MOVING_DOWN)),
				EncoderPolarityE.MATCHES_MOTOR, //.OPPOSITE_OF_MOTOR, 
				(OptDefaults.ARM_CASCADING_ENCODER_ABSOLUTE ? 
						EncoderCountsCapabilityE.ABSOLUTE_USED_AS_ABSOLUTE :
					    EncoderCountsCapabilityE.ABSOLUTE_USED_AS_RELATIVE)),
		ARM_MTR_STAGE1_AUXILLIARY(RoControlCfgStyleE.MANUAL_CFG, 
				//note: x=a?b:c; means if(a){x=b;}else{x=c;}
				(RUN_STF ? ItemAvailabilityE.USE_FAKE : ItemAvailabilityE.ACTIVE), ItemFakeableE.FAKEABLE, 
				RoControlTypesE.CAN_BUS_MOTOR_CONTROLLER, 
				RoNamedModulesE.ARM_MTRCNTL_MOD_STAGE2_LIFTER, 
				ConnCnst.USE_MODULE_CAN_ID, //(true ? null : RoNamedConnectionsE.CAN_BUS_ID_44/*31*/), 
				FrcAndJoysticksVsRoDrvMtrDirVsRobotDirE.FOR_FRC_JS_NOT_A_DRIVE_MOTOR,
				MotorPosPercOutVsThingDirE.POS_MTR_PERCENT_OUT_MOVES_THING_DOWN,
				//keep it same as main motor and assume that the polarity between the talon
				//and the motor is physically wired the opposite way
				RoMtrInversionE.INVERT_MOTOR, CntlCnst.DEFAULT_MTR_TUNING_FACTOR, 
				SdKeysE.KEY_ARM_STAGE2_MTR_MODE, 
				SdKeysE.KEY_ARM_STAGE2_MTR_OUTPUT_PERCENT, 
				SdKeysE.KEY_ARM_STAGE2_MTR_AMPS,
				RoMtrContinuousAmpsE.MAX_CONTINUOUS_AMPS_25,
				RoMtrPeakAmpsE.MAX_PEAK_AMPS_25,
				RoMtrPeakAmpsDurationE.MAX_PEAK_DURATION_100MS  
//				, 
//			(Tm744Opts.OptDefaults.ARM_ENC_FAKE ? RoMtrHasEncoderE.USE_FAKE_ENCODER : RoMtrHasEncoderE.HAS_ENCODER), 
//				SdKeysE.KEY_ARM_STAGE1_ENCODER_RDG, 
//				TmSsArm.Cnst.ARM_STAGE1_ENCODER_MAX_REVS_PER_SECOND, 
//				TmSsArm.Cnst.ARM_STAGE1_ENCODER_COUNTS_PER_REVOLUTION, 
//				TmSsArm.Cnst.ARM_STAGE1_ENCODER_FEET_PER_REVOLUTION, 
//				EncoderIncrVsThingDirE.TBD,
//				EncoderPolarityE.MATCHES_MOTOR, //.OPPOSITE_OF_MOTOR, 
//				EncoderCountsCapabilityE.ABSOLUTE_USED_AS_RELATIVE
				),
				
//		ARM_MTR_STAGE2_LIFTER(RoControlCfgStyleE.MANUAL_CFG, 
//				//note: x=a?b:c; means if(a){x=b;}else{x=c;}
//				(RUN_STF ? ItemAvailabilityE.USE_FAKE : ItemAvailabilityE.ACTIVE), ItemFakeableE.FAKEABLE, 
//				RoControlTypesE.CAN_BUS_MOTOR_CONTROLLER, 
//				RoNamedModulesE.ARM_MTRCNTL_MOD_STAGE2_LIFTER, 
//				ConnCnst.USE_MODULE_CAN_ID, //(true ? null : RoNamedConnectionsE.CAN_BUS_ID_45/*32*/), 
//				FrcAndJoysticksVsRoDrvMtrDirVsRobotDirE.FOR_FRC_JS_NOT_A_DRIVE_MOTOR,
//				MotorPosPercOutVsThingDirE.POS_MTR_PERCENT_OUT_MOVES_THING_UP,
//				RoMtrInversionE.INVERT_MOTOR, CntlCnst.DEFAULT_MTR_TUNING_FACTOR, 
//				SdKeysE.KEY_ARM_STAGE2_MTR_MODE, 
//				SdKeysE.KEY_ARM_STAGE2_MTR_OUTPUT_PERCENT, 
//				SdKeysE.KEY_ARM_STAGE2_MTR_AMPS,
//				RoMtrContinuousAmpsE.MAX_CONTINUOUS_AMPS_40,
//				RoMtrPeakAmpsE.MAX_PEAK_AMPS_42,
//				RoMtrPeakAmpsDurationE.MAX_PEAK_DURATION_100MS,
//			(Tm744Opts.OptDefaults.ARM_ENC_FAKE ? RoMtrHasEncoderE.USE_FAKE_ENCODER : RoMtrHasEncoderE.HAS_ENCODER),  
//				SdKeysE.KEY_ARM_STAGE2_ENCODER_RDG, 
//				TmSsArm.Cnst.ARM_STAGE2_ENCODER_MAX_REVS_PER_SECOND, 
//				TmSsArm.Cnst.ARM_STAGE2_ENCODER_COUNTS_PER_REVOLUTION, 
//				TmSsArm.Cnst.ARM_STAGE2_ENCODER_FEET_PER_REVOLUTION, 
//				EncoderIncrVsThingDirE.TBD,
//				EncoderPolarityE.MATCHES_MOTOR, //.OPPOSITE_OF_MOTOR, 
//				EncoderCountsCapabilityE.ABSOLUTE_USED_AS_RELATIVE),
		
		ARM_CLAW_MTR_LEFT(RoControlCfgStyleE.MANUAL_CFG, 
				//note: x=a?b:c; means if(a){x=b;}else{x=c;}
				(RUN_STF ? ItemAvailabilityE.USE_FAKE : ItemAvailabilityE.ACTIVE), ItemFakeableE.FAKEABLE, 
				RoControlTypesE.CAN_BUS_MOTOR_CONTROLLER, 
				RoNamedModulesE.ARM_MTRCNTL_MOD_CLAW_LEFT, ConnCnst.USE_MODULE_CAN_ID,
				FrcAndJoysticksVsRoDrvMtrDirVsRobotDirE.FOR_FRC_JS_NOT_A_DRIVE_MOTOR,
				MotorPosPercOutVsThingDirE.POS_MTR_PERCENT_OUT_MOVES_THING_IN, //thing=cube //assume joystick to left=-1, to right=1)
				RoMtrInversionE.INVERT_MOTOR, CntlCnst.DEFAULT_MTR_TUNING_FACTOR, 
				null, //SdKeysE.KEY_ARM_, //mode (should always be percent out
				SdKeysE.KEY_ARM_CLAW_MTR_LEFT_PERCENT_OUT, 
				null, //SdKeysE.KEY_ARM_, //amps
		        RoMtrContinuousAmpsE.MAX_CONTINUOUS_AMPS_20,
				RoMtrPeakAmpsE.MAX_PEAK_AMPS_32,
				RoMtrPeakAmpsDurationE.MAX_PEAK_DURATION_100MS),
		ARM_CLAW_MTR_RIGHT(RoControlCfgStyleE.MANUAL_CFG, 
				//note: x=a?b:c; means if(a){x=b;}else{x=c;}
				(RUN_STF ? ItemAvailabilityE.USE_FAKE : ItemAvailabilityE.ACTIVE), ItemFakeableE.FAKEABLE, 
				RoControlTypesE.CAN_BUS_MOTOR_CONTROLLER, 
				RoNamedModulesE.ARM_MTRCNTL_MOD_CLAW_RIGHT, ConnCnst.USE_MODULE_CAN_ID,
				FrcAndJoysticksVsRoDrvMtrDirVsRobotDirE.FOR_FRC_JS_NOT_A_DRIVE_MOTOR,
				MotorPosPercOutVsThingDirE.POS_MTR_PERCENT_OUT_MOVES_THING_OUT, //thing=cube
				RoMtrInversionE.INVERT_MOTOR, CntlCnst.DEFAULT_MTR_TUNING_FACTOR, 
				null, //SdKeysE.KEY_ARM_, //mode (should always be percent out
				SdKeysE.KEY_ARM_CLAW_MTR_RIGHT_PERCENT_OUT, 
				null, //SdKeysE.KEY_ARM_, //amps
				RoMtrContinuousAmpsE.MAX_CONTINUOUS_AMPS_20,
				RoMtrPeakAmpsE.MAX_PEAK_AMPS_32,
				RoMtrPeakAmpsDurationE.MAX_PEAK_DURATION_100MS),

		ARM_STAGE1_TOP_LIMIT_SWITCH(RoControlCfgStyleE.MANUAL_CFG, ItemAvailabilityE.ACTIVE, ItemFakeableE.NOT_FAKEABLE, 
				RoControlTypesE.OTHER, 
				RoNamedModulesE.RIO, RoNamedConnectionsE.RIO_DIO0),
		ARM_STAGE1_BOTTOM_LIMIT_SWITCH(RoControlCfgStyleE.MANUAL_CFG, ItemAvailabilityE.ACTIVE, ItemFakeableE.NOT_FAKEABLE, 
				RoControlTypesE.OTHER, 
				RoNamedModulesE.RIO, RoNamedConnectionsE.RIO_DIO1),
		
		ARM_STAGE2_TOP_LIMIT_SWITCH(RoControlCfgStyleE.MANUAL_CFG, ItemAvailabilityE.ACTIVE, ItemFakeableE.NOT_FAKEABLE, 
				RoControlTypesE.OTHER, 
				RoNamedModulesE.RIO, RoNamedConnectionsE.RIO_DIO2),
		ARM_STAGE2_BOTTOM_LIMIT_SWITCH(RoControlCfgStyleE.MANUAL_CFG, ItemAvailabilityE.ACTIVE, ItemFakeableE.NOT_FAKEABLE, 
				RoControlTypesE.OTHER, 
				RoNamedModulesE.RIO, RoNamedConnectionsE.RIO_DIO3),

		//using motors instead
//		ARM_CLAMP(RoControlCfgStyleE.MANUAL_CFG, ItemAvailabilityE.ACTIVE, ItemFakeableE.NOT_FAKEABLE, 
//				RoControlTypesE.OTHER, 
//				RoNamedModulesE.PCM0, RoNamedConnectionsE.PCM_SOL2),
//		ARM_UNCLAMP(RoControlCfgStyleE.MANUAL_CFG, ItemAvailabilityE.ACTIVE, ItemFakeableE.NOT_FAKEABLE, 
//				RoControlTypesE.OTHER, 
//				RoNamedModulesE.PCM0, RoNamedConnectionsE.PCM_SOL3),

		
		
//		SOME_PWM_TALON(RoControlCfgStyleE.MANUAL_CFG, ItemAvailabilityE.ACTIVE, ItemFakeableE.NOT_FAKEABLE, 
//				RoControlTypesE.PWM_MOTOR_CONTROLLER, 
//				RoNamedModulesE.RIO, RoNamedConnectionsE.RIO_PWM0),
//		SOME_OTHER_PWM_TALON(RoControlCfgStyleE.MANUAL_CFG, ItemAvailabilityE.ACTIVE, ItemFakeableE.NOT_FAKEABLE, 
//				RoControlTypesE.PWM_MOTOR_CONTROLLER, 
//				RoNamedModulesE.RIO, RoNamedConnectionsE.MXP_PIN11_PWM0_DIO0),

		;

		@Override
		public List<RoNamedControlsEntry> getList() { return roNamedControlsList; }
		public List<RoNamedControlsEntry> staticGetList() { return roNamedControlsList; }
		@Override
		public RoNamedControlsEntry getEnt() { return getList().get(this.ordinal()); }
		@Override
		public String getEnumClassName() { return this.getClass().getSimpleName(); }
		@Override
		public String getListEntryClassName() { return this.getList().get(0).getClass().getSimpleName(); }

		private RoNamedControlsE(RoControlCfgStyleE cntlCfgStyle, ItemAvailabilityE cntlAvail, ItemFakeableE cntlFakeable, RoControlTypesE cntlType, 
				RoNamedModulesE namedMod, RoNamedConnectionsE namedConn) {
			//note: if namedConn is null, RoNamedControlsEntry will use the module's connection as the control connection.
			//      namedConn should be null only for motor controllers that are listed as both modules and controls
			getList().add(this.ordinal(), TmHdwrRoPhysBase.getInstance().new RoNamedControlsEntry(this, cntlCfgStyle, cntlAvail, cntlFakeable,
					cntlType, namedMod, namedConn, null));
		}
		private RoNamedControlsE(RoControlCfgStyleE cntlCfgStyle, ItemAvailabilityE cntlAvail, ItemFakeableE cntlFakeable, RoControlTypesE cntlType, 
				RoNamedModulesE namedMod, RoNamedConnectionsE namedConn, Relay.Direction relayDir) {
			getList().add(this.ordinal(), TmHdwrRoPhysBase.getInstance().new RoNamedControlsEntry(this, cntlCfgStyle, cntlAvail, cntlFakeable,
					cntlType, namedMod, namedConn, relayDir));
		}
		private RoNamedControlsE(RoControlCfgStyleE cntlCfgStyle, ItemAvailabilityE cntlAvail, ItemFakeableE cntlFakeable, RoControlTypesE cntlType, 
				RoNamedModulesE namedMod, RoNamedConnectionsE namedConn, 
				FrcAndJoysticksVsRoDrvMtrDirVsRobotDirE mtrDirVsRobotDir, MotorPosPercOutVsThingDirE mtrVsThingDir,
				RoMtrInversionE mtrInversion, double mtrTuningFactor, 
				SdKeysE sdKeyMtrMode, SdKeysE sdKeyMtrPercentOut, SdKeysE sdKeyMtrAmps,
				RoMtrContinuousAmpsE maxContinuousAmps,
				RoMtrPeakAmpsE maxPeakAmps,
				RoMtrPeakAmpsDurationE maxPeakAmpsDurationMs
				) { //, boolean mtrHasEncoder) {
			RoNamedControlsEntry thing2 = TmHdwrRoPhysBase.getInstance().new RoNamedControlsEntry(this, cntlCfgStyle, cntlAvail, cntlFakeable, 
					cntlType, namedMod, namedConn, null, 
					mtrDirVsRobotDir, mtrVsThingDir, mtrInversion, mtrTuningFactor, 
					sdKeyMtrMode, sdKeyMtrPercentOut, sdKeyMtrAmps,
					maxContinuousAmps, maxPeakAmps, maxPeakAmpsDurationMs
					);
			getList().add(this.ordinal(), thing2);
		}
		private RoNamedControlsE(RoControlCfgStyleE cntlCfgStyle, ItemAvailabilityE cntlAvail, ItemFakeableE cntlFakeable, RoControlTypesE cntlType, 
				RoNamedModulesE namedMod, RoNamedConnectionsE namedConn, 
				FrcAndJoysticksVsRoDrvMtrDirVsRobotDirE mtrDirVsRobotDir, MotorPosPercOutVsThingDirE mtrVsThingDir,
				RoMtrInversionE mtrInversion, double mtrTuningFactor, 
				SdKeysE sdKeyMtrMode, SdKeysE sdKeyMtrPercentOut, SdKeysE sdKeyMtrAmps,
				RoMtrContinuousAmpsE maxContinuousAmps,
				RoMtrPeakAmpsE maxPeakAmps,
				RoMtrPeakAmpsDurationE maxPeakAmpsDurationMs, 
				RoMtrHasEncoderE mtrHasEncoder, SdKeysE mtrEncoderSdKeyPosition, 
				double mtrEncoderMaxRevsPerSec, int mtrEncoderCountsPerRevolution,
				double mtrEncoderFeetPerRevolution,
				EncoderIncrVsThingDirE mtrEncoderIncrVsThingDir,
				EncoderPolarityE mtrEncoderPolarity, EncoderCountsCapabilityE mtrEncoderCountsCap ) {
			getList().add(this.ordinal(), TmHdwrRoPhysBase.getInstance().new RoNamedControlsEntry(this, cntlCfgStyle, cntlAvail, cntlFakeable,
					cntlType, namedMod, namedConn, null, 
					mtrDirVsRobotDir, mtrVsThingDir, mtrInversion, mtrTuningFactor, 
					sdKeyMtrMode, sdKeyMtrPercentOut, sdKeyMtrAmps,
					maxContinuousAmps, maxPeakAmps, maxPeakAmpsDurationMs,
					mtrHasEncoder, mtrEncoderSdKeyPosition, mtrEncoderMaxRevsPerSec, mtrEncoderCountsPerRevolution,
					mtrEncoderFeetPerRevolution, mtrEncoderIncrVsThingDir,
					mtrEncoderPolarity, mtrEncoderCountsCap));
		}
		
		String frmtStr = "%-40s [ListEnt(%2d): %s]";
		String frmtHdr = "%-40s [            %s]";
		@Override
		public String toStringLog(String inpPrefix) {
			String prefix = (inpPrefix==null) ? "" : inpPrefix;
			String ans = String.format(prefix + frmtStr, this.name(), this.ordinal(), this.getEnt().toStringLog());
			return ans;
		}
		@Override
		public String toStringHdr(String inpPrefix) {
			String prefix = (inpPrefix==null) ? "" : inpPrefix;
			String ans = String.format(prefix + frmtHdr, this.getClass().getSimpleName(), this.getEnt().toStringHdr());
			
			return ans;
		}
		@Override
		public String toStringNotes() {
			return(getEnumClassName() + " - saves no info in enum (see List<" + getListEntryClassName() + 
					"> roNamedControlsList and enum's getList() and getEnt() methods)");
		}
	
	}

	public static enum ShowRoConnectionsSortingE { ENUM_ORDER, GROUP_BY_MODULE }
	public static void showConnections() { showConnections(ShowRoConnectionsSortingE.GROUP_BY_MODULE); }
	public static void showConnections(ShowRoConnectionsSortingE order) {
//		super.showConnections();
		String frmtHdr = "%-37s, %-15s, %-31s";
		String frmtStr = "%-37s, %-15s, %-31s";
		P.println("Robot controls -- sort order: " + order.name());
		P.println("code build: " + TmVersionInfo.getProjectName());
		P.println("            " + TmVersionInfo.getDateTimeHostString());
		P.printFrmt(frmtHdr, "control", "connection", "module");
		switch(order) {
		case ENUM_ORDER:
			for(RoNamedControlsE c : RoNamedControlsE.values()) {
				P.printFrmt(frmtStr, c.name(),
						c.getEnt().cNamedConn.name(),
						c.getEnt().cNamedMod.name()
						);
			}		
			break;
		case GROUP_BY_MODULE:
			for(RoNamedModulesE m : RoNamedModulesE.values()) {
				for(RoNamedControlsE c : RoNamedControlsE.values()) {
					if(c.getEnt().cNamedMod.equals(m)) {
						P.printFrmt(frmtStr, c.name(),
								c.getEnt().cNamedConn.name(),
								c.getEnt().cNamedMod.name()
								);
					}
				}	
			}
			break;
		}
	}

	
	public void showEverything() {
		super.showEverything();
//		P.printFrmt("TmHdwrRoPhys instance: %s", m_instance.toString());
//		P.println("TmHdwrRoPhys.RoNamedModulesE:");
//		for(RoNamedModulesE nm : RoNamedModulesE.values()) {
//			P.println("  " + nm.toString());
//		}
		TmToStringI.showEnumEverything(RoNamedModulesE.values());
		TmToStringI.showEnumEverything(RoNamedControlsE.values());
//		TmToStringI.showListEverything(RoNamedModulesE.staticGetList());
	}

	@Override
	public void doForcedInstantiation() {
		//Access something from each of the enums in this class to force them 
		//(and their related List<> arrays) to be initialized
		super.doForcedInstantiation();
		RoNamedModulesE junk = RoNamedModulesE.RIO;
		RoNamedControlsE junk1 = RoNamedControlsE.DRV_MTR_CENTER_LEFT;
	}

	@Override
	public void doPopulate() {
		super.doPopulate();
//		TmSdMgr.putData(SdKeysE.KEY_CMD_SHOW_ROBOT_IO, 
//				LocalCommands.getInstance().new LocalCmd_ShowAllRoIoOnConsole());
		
		//done from T744Robot2018, probably to avoid initialization conflicts
//		DsNamedControlsE.SHOW_ROBOT_CNTLS_ON_CONSOLE.getEnt().whenPressed(this, 
//				LocalCommands.getInstance().new LocalCmd_ShowAllDsIoOnConsole());
	}
	
	
	public static class LocalCommands {
		private final static LocalCommands lcInstance = new LocalCommands();
		public static LocalCommands getInstance() { return lcInstance; }
		private LocalCommands() {}
		
		public class LocalCmd_ShowAllRoIoOnConsole extends Command implements TmToolsI {
			public LocalCmd_ShowAllRoIoOnConsole() {
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
