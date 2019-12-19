package org.usfirst.frc.tm744yr18.robot.config;

import org.usfirst.frc.tm744yr18.robot.config.TmHdwrDsCntls.DsNamedDevicesE;
import org.usfirst.frc.tm744yr18.robot.config.TmHdwrDsPhysBase.DsControlTypesE;
import org.usfirst.frc.tm744yr18.robot.config.TmHdwrDsPhysBase.DsNamedConnectionsE;
import org.usfirst.frc.tm744yr18.robot.exceptions.TmExceptions;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmItemAvailabilityI.ItemAvailabilityE;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmItemAvailabilityI.ItemFakeableE;
import org.usfirst.frc.tm744yr18.robot.subsystems.TmSsArm;

import t744opts.Tm744Opts;

public class TmPrefKeys {
    public enum PrefTypeE { BOOLEAN, INT, DOUBLE, STRING; }
    public enum PrefKeysE
    {
    	//used to get data from the smartdashboard. Data is assumed to be 0xXXXX type Strings.
    	KEY_BOOT_USB_CAMERA_0_IS_INSTALLED("BootOptUsbCamera0Installed", Tm744Opts.OptDefaults.USB_CAM0_INSTALLED),
    	KEY_BOOT_USB_CAMERA_1_IS_INSTALLED("BootOptUsbCamera1Installed", Tm744Opts.OptDefaults.USB_CAM1_INSTALLED),

    	KEY_BOOT_RUN_ON_STF("BootOptRunOnSftwrTestFixture", Tm744Opts.isOptRunStf()), //Tm744Opts.OptDefaults.RUN_STF),
    	KEY_BOOT_USE_FAKE_DRV_ENC("BootOptUseFakeDrvEncoders", Tm744Opts.OptDefaults.DRV_ENC_FAKE),
    	KEY_BOOT_USE_FAKE_ARM_ENC("BootOptUseFakeArmEncoders", Tm744Opts.OptDefaults.ARM_ENC_FAKE),
    	KEY_BOOT_WEDGE_LIMIT_SW_INSTALLED("BootOptWedgeLimitSwitchInstalled", Tm744Opts.OptDefaults.WEDGE_LIMIT_SWITCH_INSTALLED),
			
		//for playing around in the lab/gym may have just one controller that 
		//we position at different usb slots for different testing.  These
		//options make such things possible.  Ordinarily we want the MAIN one true
		//and the XTRA one false
    	KEY_BOOT_USE_MAIN_XBOX_CNTLR("BootOptUseMainXboxCntlr", Tm744Opts.OptDefaults.MAIN_XBOX_CONTROLLER_ATTACHED),
    	KEY_BOOT_USE_OPTIONAL_XBOX_CNTLR("BootOptUseOptXboxCntlr", Tm744Opts.OptDefaults.XTRA_XBOX_CONTROLLER_ATTACHED),

    	KEY_FYI_ARM_LIFT_ENCODER_DEFAULTS("FYI_ArmLiftEncoder", TmSsArm.ArmServices.getDefaultArmEncoderSettings()),
    	KEY_TUNE_ARM_LIFT_ENCODER_AT_TOP("TuneArmLiftEncoderSetPt_TOP" + 
    			"", //TmHdwrDsCntls.DsNamedControlsE.ARM_LIFT_BOTH_AT_TOP_BTN.getEnt().cNamedConn.name(), 
    					TmSsArm.Cnst.STAGE1_ENCODER_AT_MAX_HEIGHT),
    	KEY_TUNE_ARM_LIFT_ENCODER_AT_MID("TuneArmLiftEncoderSetPt_SCALE_MID" + 
    			"", //TmHdwrDsCntls.DsNamedControlsE.ARM_LIFT_BOTH_SCALE_MID_BTN.getEnt().cNamedConn.name(), 
    					TmSsArm.Cnst.STAGE1_ENCODER_AT_SCALE_MID),
    	KEY_TUNE_ARM_LIFT_ENCODER_AT_LOW("TuneArmLiftEncoderSetPt_SCALE_LOW" + 
    			"", //TmHdwrDsCntls.DsNamedControlsE.ARM_LIFT_BOTH_SCALE_LOW_BTN.getEnt().cNamedConn.name(), 
				TmSsArm.Cnst.STAGE1_ENCODER_AT_SCALE_LOW),
    	KEY_TUNE_ARM_LIFT_ENCODER_AT_SWITCH("TuneArmLiftEncoderSetPt_SWITCH" + 
    			"", //TmHdwrDsCntls.DsNamedControlsE.ARM_LIFT_BOTH_SWITCH_BTN.getEnt().cNamedConn.name(), 
    					TmSsArm.Cnst.STAGE1_ENCODER_AT_SWITCH),
    	KEY_TUNE_ARM_LIFT_ENCODER_AT_BOTTOM("TuneArmLiftEncoderSetPt_BOTTOM" + 
    					"", //TmHdwrDsCntls.DsNamedControlsE.ARM_LIFT_BOTH_BOTTOM_BTN.getEnt().cNamedConn.name(), 
    					TmSsArm.Cnst.STAGE1_ENCODER_AT_BOTTOM),
    	
    	KEY_FYI_OPTS_WITH_NO_PREFS("FYI_OtherOpts", Tm744Opts.getOtherOptsStr())
    	;
//		ARM_LIFT_BOTH_SCALE_MID_BTN(ItemAvailabilityE.ACTIVE, ItemFakeableE.NOT_FAKEABLE, 
//				DsControlTypesE.kButton, 
//				DsNamedDevicesE.MAIN_XBOX_CNTLR_DEV, DsNamedConnectionsE.A_BUTTON),
//		ARM_LIFT_BOTH_SWITCH_BTN(ItemAvailabilityE.ACTIVE, ItemFakeableE.NOT_FAKEABLE, 
//				DsControlTypesE.kButton, 
//				DsNamedDevicesE.DRIVE_RIGHT_INPUT_DEV, DsNamedConnectionsE.BASE_BUT9_FRONT_RIGHT),
//		ARM_LIFT_BOTH_BOTTOM_BTN(ItemAvailabilityE.ACTIVE, ItemFakeableE.NOT_FAKEABLE, 
//				DsControlTypesE.kCannedPovButton, 
//				DsNamedDevicesE.MAIN_XBOX_CNTLR_DEV, DsNamedConnectionsE.POV_WEST_BUTTON),
//		ARM_LIFT_BOTH_AT_TOP_BTN(ItemAvailabilityE.ACTIVE, ItemFakeableE.NOT_FAKEABLE, 
//				DsControlTypesE.kButton, 
//				DsNamedDevicesE.MAIN_XBOX_CNTLR_DEV, DsNamedConnectionsE.Y_BUTTON),
       
        
        private final String eKey;
        private final PrefTypeE eType;
        private double eValDouble;
        private int eValInt;
        private String eValString;
        private boolean eValBoolean;
        
        private PrefKeysE(String key, boolean defaultVal) {	eKey = key; eType = PrefTypeE.BOOLEAN; eValBoolean = defaultVal;}
        private PrefKeysE(String key, int defaultVal) {	eKey = key; eType = PrefTypeE.INT; eValInt = defaultVal; }
        private PrefKeysE(String key, double defaultVal) {	eKey = key; eType = PrefTypeE.DOUBLE; eValDouble = defaultVal; }
        private PrefKeysE(String key, String defaultVal) {	eKey = key; eType = PrefTypeE.STRING; eValString = defaultVal; }
        
        public String getKey() { return eKey; }
        public PrefTypeE getType() { return eType; }
        
        public boolean getDefaultBoolean() {
        	PrefTypeE expectedType = PrefTypeE.BOOLEAN;
        	if( ! eType.equals(expectedType)) { 
        		throw TmExceptions.getInstance().new InappropriatePreferenceRequestEx(this.name() + " is " + eType.name() + " not " + expectedType.name());
        	} else { return eValBoolean; }
        }
        public int getDefaultInt() {
        	PrefTypeE expectedType = PrefTypeE.INT;
        	if( ! eType.equals(expectedType)) { 
        		throw TmExceptions.getInstance().new InappropriatePreferenceRequestEx(this.name() + " is " + eType.name() + " not " + expectedType.name());
        	} else { return eValInt; }
        }
        public double getDefaultDouble() {
        	PrefTypeE expectedType = PrefTypeE.DOUBLE;
        	if( ! eType.equals(expectedType)) { 
        		throw TmExceptions.getInstance().new InappropriatePreferenceRequestEx(this.name() + " is " + eType.name() + " not " + expectedType.name());
        	} else { return eValDouble; }
        }
        public String getOptDefaultstring() {
        	PrefTypeE expectedType = PrefTypeE.STRING;
        	if( ! eType.equals(expectedType)) { 
        		throw TmExceptions.getInstance().new InappropriatePreferenceRequestEx(this.name() + " is " + eType.name() + " not " + expectedType.name());
        	} else { return eValString; }
        }
    }

}
