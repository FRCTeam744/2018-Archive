package t744opts;

import org.usfirst.frc.tm744yr18.robot.config.TmPrefKeys.PrefKeysE;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmToolsI.PrefCreateE;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmToolsI.Tt;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmToolsI.P.PrtYn;
import org.usfirst.frc.tm744yr18.robot.subsystems.TmSsArm.ArmServices;

/**
 * this class should reside outside of the org folder to facilitate porting code
 * from one computer/robot to another without getting software test fixture code on the
 * real robot and other similar no-no's
 * @author robotics
 *
 */
public class Tm744Opts {
	
	public static class OptDefaults {
	
		//used to change the configuration of devices that don't generate exceptions when they're
		//not present....  DoubleSolenoids, for example
		public static final boolean RUN_STF = false; //RUNNING_ON_SOFTWARE_TEST_FIXTURE = true;
		public static final boolean DRV_ENC_FAKE = false; //true=real encoders are not (yet) installed on drive motors
		public static final boolean ARM_ENC_FAKE = false; //true=real encoders are not (yet) installed on arm lift motors
		
		public static final boolean ARM_CASCADING = true;
		public static final boolean ARM_CASCADING_ENCODER_ABSOLUTE = (ARM_CASCADING ? false : false);
		
		public static final boolean WEDGE_LIMIT_SWITCH_INSTALLED = false;
		public static final boolean GRABBER_UP_LIMIT_SWITCH_INSTALLED = false;
		
		//ugh!!! - needed for smoketest for cascading arm, still in use, should never need to be 'true' again!!
		public static final boolean RUN_STG1AUX_MTR_FROM_ARM_CLAW_RIGHT_TALON_ETC = false;
		
		//for playing around in the lab/gym may have just one controller that 
		//we position at different usb slots for different testing.  These
		//options make such things possible.  Ordinarily we want the MAIN one true
		//and the XTRA one false
		public static final boolean MAIN_XBOX_CONTROLLER_ATTACHED = true;
		public static final boolean XTRA_XBOX_CONTROLLER_ATTACHED = false;

		public static final boolean USB_CAM0_INSTALLED = true;
		public static final boolean USB_CAM1_INSTALLED = false;
		
		public static final boolean POWER_COMPRESSOR_FROM_RELAY = false;
		
		public static final boolean COMPRESSOR_SENSOR_AS_ANALOG = false;
		public static final boolean COMPRESSOR_INSTALLED = true;
		
		public static final boolean PCM0_INSTALLED = true;

		public static final boolean DOUBLE_SOLENOID_FOR_DRIVE_GEAR_SHIFT = false;
		
		public static final boolean USE_NAVX_GYRO = true; //false for ADXRS453SPI
		public static final boolean USE_NO_GYROS = false;
		
	}
	
	//simulation code can call this method to change this setting. nothing else should!!!
	private static boolean m_isInSimulationMode = false;
	public static void setSimulationMode() { m_isInSimulationMode = true; }
	public static boolean isInSimulationMode() {
		return m_isInSimulationMode;
	}
	
	public static boolean isDblSolForGearShift() { return OptDefaults.DOUBLE_SOLENOID_FOR_DRIVE_GEAR_SHIFT; }
	public static boolean isOptRunStf() { return OptDefaults.RUN_STF; }
	public static boolean isOptDrvEncFake() { return (isInSimulationMode() ? true : OptDefaults.DRV_ENC_FAKE); }
	public static boolean isOptArmEncFake() { return (isInSimulationMode() ? true : OptDefaults.ARM_ENC_FAKE); }
//	public static boolean isOptArmCascading() { return OptDefaults.ARM_CASCADING; }
	public static boolean isOptWedgeLimitSwitchInstalled() { return OptDefaults.WEDGE_LIMIT_SWITCH_INSTALLED; }
	public static boolean isOptGrabberUpFullLimitSwitchInstalled() { return OptDefaults.GRABBER_UP_LIMIT_SWITCH_INSTALLED; }
	public static boolean isOptMainXboxCntlrAttached() { return OptDefaults.MAIN_XBOX_CONTROLLER_ATTACHED; }
	public static boolean isOptXtraXboxCntlrAttached() { return OptDefaults.XTRA_XBOX_CONTROLLER_ATTACHED; }
	public static boolean isUsbCam0Installed() {
		return OptDefaults.USB_CAM0_INSTALLED;
	}
	public static boolean isUsbCam1Installed() {
		return OptDefaults.USB_CAM1_INSTALLED;
	}
	
	public static boolean isPowerCompressorFromRelay() { return OptDefaults.POWER_COMPRESSOR_FROM_RELAY; }
	public static boolean isReadCompressorSensorAsAnalog() { return OptDefaults.COMPRESSOR_SENSOR_AS_ANALOG; }
	public static boolean isCompressorInstalled() { return OptDefaults.COMPRESSOR_INSTALLED; }
	
	public static boolean isPcm0Installed() { return OptDefaults.PCM0_INSTALLED; }
	
	public static boolean isGyroNavX() { return OptDefaults.USE_NAVX_GYRO; }
	public static boolean isUseNoGyros() { return OptDefaults.USE_NO_GYROS; }
	
	//info about options with no preferences
	public static String getOtherOptsStr() {
		String ans = "PwrCmprsrFromRelay=" + OptDefaults.POWER_COMPRESSOR_FROM_RELAY;
		ans += ", CmprsrSnsAsAnalog=" + OptDefaults.COMPRESSOR_SENSOR_AS_ANALOG;
		ans += ", usingNavXGyro=" + OptDefaults.USE_NAVX_GYRO;
		ans += ", 'useNoGyro'=" + OptDefaults.USE_NO_GYROS;
		return ans;
	}
	
	public static void postPrefBootOptsToSd() {
		boolean ans;    	
		ans = Tt.getPreference(PrefKeysE.KEY_BOOT_USB_CAMERA_0_IS_INSTALLED, Tm744Opts.OptDefaults.USB_CAM0_INSTALLED, PrtYn.Y, PrefCreateE.CREATE_AS_NEEDED);
		ans = Tt.getPreference(PrefKeysE.KEY_BOOT_USB_CAMERA_1_IS_INSTALLED, Tm744Opts.OptDefaults.USB_CAM1_INSTALLED, PrtYn.Y, PrefCreateE.CREATE_AS_NEEDED);
		ans = Tt.getPreference(PrefKeysE.KEY_BOOT_RUN_ON_STF, Tm744Opts.isOptRunStf(), PrtYn.Y, PrefCreateE.CREATE_AS_NEEDED);
		ans = Tt.getPreference(PrefKeysE.KEY_BOOT_USE_FAKE_DRV_ENC, Tm744Opts.OptDefaults.DRV_ENC_FAKE, PrtYn.Y, PrefCreateE.CREATE_AS_NEEDED);
		ans = Tt.getPreference(PrefKeysE.KEY_BOOT_USE_FAKE_ARM_ENC, Tm744Opts.OptDefaults.ARM_ENC_FAKE, PrtYn.Y, PrefCreateE.CREATE_AS_NEEDED);
		ans = Tt.getPreference(PrefKeysE.KEY_BOOT_WEDGE_LIMIT_SW_INSTALLED, Tm744Opts.OptDefaults.WEDGE_LIMIT_SWITCH_INSTALLED, PrtYn.Y, PrefCreateE.CREATE_AS_NEEDED);
		ans = Tt.getPreference(PrefKeysE.KEY_BOOT_USE_MAIN_XBOX_CNTLR, Tm744Opts.OptDefaults.MAIN_XBOX_CONTROLLER_ATTACHED, PrtYn.Y, PrefCreateE.CREATE_AS_NEEDED);
		ans = Tt.getPreference(PrefKeysE.KEY_BOOT_USE_OPTIONAL_XBOX_CNTLR, Tm744Opts.OptDefaults.XTRA_XBOX_CONTROLLER_ATTACHED, PrtYn.Y, PrefCreateE.CREATE_AS_NEEDED);
		String ansStr = Tt.getPreference(PrefKeysE.KEY_FYI_OPTS_WITH_NO_PREFS, Tm744Opts.getOtherOptsStr(), PrtYn.Y, PrefCreateE.CREATE_AS_NEEDED);

	}
}
