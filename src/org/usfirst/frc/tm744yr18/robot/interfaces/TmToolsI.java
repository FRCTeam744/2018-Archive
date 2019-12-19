package org.usfirst.frc.tm744yr18.robot.interfaces;

import org.usfirst.frc.tm744yr18.robot.config.TmPrefKeys.PrefKeysE;
import org.usfirst.frc.tm744yr18.robot.helpers.TmDriverStation;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmToolsI.P.PrtYn;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Preferences;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import edu.wpi.first.wpilibj.hal.AllianceStationID;

public interface TmToolsI {

	public enum PrefCreateE { CREATE_AS_NEEDED, DO_NOT_CREATE }

    public class Tt { //we can include this and then prefix these method names with Tt. to make
                      //the code easier to read and to write.

    	public enum EndpointHandlingE { INCLUDE_ENDPOINTS, EXCLUDE_ENDPOINTS }
    	
    	public static boolean isInRange(double testVal, double min, double max) {
    		return isInRange(testVal, min, max, EndpointHandlingE.INCLUDE_ENDPOINTS);
		}
    	public static boolean isInRange(double testVal, double endA, double endB, EndpointHandlingE endPtHdlr) {
			boolean ans = false;
			double min = endA;
			double max = endB;
			if(min>max) {
				min = endB;
				max = endA;
			}
			if((testVal>=min) && (testVal<=max)) { ans = true; }
			if(endPtHdlr.equals(EndpointHandlingE.EXCLUDE_ENDPOINTS)) {
				if((testVal==min) || (testVal==max)) {
					ans = false;
				}
			}
			return ans;
		}  

    	public static int clampToRange(int testVal, int endA, int endB) {
    		return (int)clampToRange((double)testVal, (double)endA, (double)endB);
    	}
    	public static double clampToRange(double testVal, double endA, double endB) {
			double ans = testVal;
			double min = endA;
			double max = endB;
			if(min>max) {
				min = endB;
				max = endA;
			}
			if(testVal < min) { ans = min; }
			else if(testVal > max) { ans = max; }
			return ans;
		}  

    	public static boolean isWithinTolerance(double testVal, double target, double tolerance) {
			boolean ans = false;
			double absTolerance = Math.abs(tolerance);
			if(testVal>=(target-absTolerance) && testVal<=(target+absTolerance)) { ans = true; }
			return ans;
		}

    	/**
    	 * round a double value to the nearest integer
    	 * 3.5 rounds to 4, -3.5 rounds to -4, etc.
    	 * @param val
    	 * @return
    	 */
    	public static int doubleToRoundedInt(double val) {
    		int ans;
    		int sign = (val >= 0) ? +1: -1;
    		double dbl;

    		dbl = Math.abs(val) + 0.5;
    		dbl -= (dbl % 1); //subtract the fractional part
    		ans = sign * (int)dbl;
    		
    		return ans;
    	}

    	/**
    	 * used when comparing the signs of two numeric values.
    	 * GE0 is "greater than or equal to 0", //NEG is "less than zero"
    	 * GT0 is "greater than 0", LT0 is "less than 0"
    	 * @author JudiA
    	 *
    	 */
//    	public static enum SignsE { BOTH_GT0, BOTH_GE0, BOTH_NEG, A_GE0_B_NEG, A_NEG_B_GE0; }
    	public static enum SignsE { BOTH_GT0, BOTH_NEG, A_GT0_B_NEG, A_NEG_B_GT0, BOTH_ZERO,
    		A_GT0_B_ZERO, A_NEG_B_ZERO, A_ZERO_B_GT0, A_ZERO_B_NEG, IMPOSSIBLE; }
    	public synchronized static SignsE compareSignsDouble(double a, double b) {
    		SignsE ans;
    		if(a>0 && b>0) { ans = SignsE.BOTH_GT0; }
    		else if(a<0 && b<0) { ans = SignsE.BOTH_NEG; }
    		else if(a>0 && b<0) { ans = SignsE.A_GT0_B_NEG; }
    		else if(a<0 && b>0) { ans = SignsE.A_NEG_B_GT0; }
    		else if(a==0 && b==0) { ans = SignsE.BOTH_ZERO; }
    		else if(a>0 && b==0) { ans = SignsE.A_GT0_B_ZERO; }
    		else if(a<0 && b==0) { ans = SignsE.A_NEG_B_ZERO; }
    		else if(a==0 && b>0) { ans = SignsE.A_ZERO_B_GT0; }
    		else if(a==0 && b<0) { ans = SignsE.A_ZERO_B_NEG; }
    		else { ans = SignsE.IMPOSSIBLE; }
    		return ans;
    	}
    	public static SignsE compareSignsInt(int a, int b) {
    		SignsE ans;
    		if(a>0 && b>0) { ans = SignsE.BOTH_GT0; }
    		else if(a<0 && b<0) { ans = SignsE.BOTH_NEG; }
    		else if(a>0 && b<0) { ans = SignsE.A_GT0_B_NEG; }
    		else if(a<0 && b>0) { ans = SignsE.A_NEG_B_GT0; }
    		else if(a==0 && b==0) { ans = SignsE.BOTH_ZERO; }
    		else if(a>0 && b==0) { ans = SignsE.A_GT0_B_ZERO; }
    		else if(a<0 && b==0) { ans = SignsE.A_NEG_B_ZERO; }
    		else if(a==0 && b>0) { ans = SignsE.A_ZERO_B_GT0; }
    		else if(a==0 && b<0) { ans = SignsE.A_ZERO_B_NEG; }
    		else { ans = SignsE.IMPOSSIBLE; }
    		return ans;
    	}

    	public static <E extends Enum<E>> String getName(E obj) {
    		/* this is a "generic" method.  E represents an object type.
    		 * "<E extends Enum<E>>" specifies that the only valid types are those that
    		 * extend the Enum<E> type.
    		 */
    		return ((obj==null) ? "null" : obj.name());
    	}
    	public static <T> String getClassName(T obj) {
    		/* this is a "generic" method.  T represents an object type.
    		 */
    		return ((obj==null) ? "??" : obj.getClass().getSimpleName());
    	}

    
		static Preferences preferences = Preferences.getInstance();
		
		/**
		 * 
		 * @param key
		 * @param defaultSetting
		 * @param description
		 * @param printFlags - 0 never prints, -1 always prints, other settings may or may not print
		 * @param createAsNeeded - create an entry if the key isn't found
		 * @return
		 */
    	public static boolean getPreference(PrefKeysE keyDef, boolean defaultSetting, PrtYn printFlags, PrefCreateE createAsNeeded) {
			boolean ans;
			String key = keyDef.getKey();
			ans = defaultSetting;
			if(key != null) {
				boolean keyPresent = preferences.containsKey(key);
				if(keyPresent) {
					ans = preferences.getBoolean(key, defaultSetting);
					P.println(printFlags, "key '" + key + "' found in Preferences: " + ans);
				} else {
					P.println(printFlags, "key " + key + " not found in Preferences.");
					if(createAsNeeded.equals(PrefCreateE.CREATE_AS_NEEDED)) {
						preferences.putBoolean(key, defaultSetting);
						P.println(printFlags, "key " + key + " created in Preferences with value " + defaultSetting + ".");
					}
				}
			}
			return ans;
    	}
    	public static int getPreference(PrefKeysE keyDef, int defaultSetting, PrtYn printFlags, PrefCreateE createAsNeeded) {
			int ans;
			String key = keyDef.getKey();
			ans = defaultSetting;
			if(key != null) {
				boolean keyPresent = preferences.containsKey(key);
				if(keyPresent) {
					ans = preferences.getInt(key, defaultSetting);
					P.println(printFlags, "key '" + key + "' found in Preferences: " + ans);
				} else {
					P.println(printFlags, "key " + key + " not found in Preferences.");
					if(createAsNeeded.equals(PrefCreateE.CREATE_AS_NEEDED)) {
						preferences.putInt(key, defaultSetting);
						P.println(printFlags, "key " + key + " created in Preferences with value " + defaultSetting + ".");
					}
				}
			}
			return ans;
    	}
    	public static double getPreference(PrefKeysE keyDef, double defaultSetting, PrtYn printFlags, PrefCreateE createAsNeeded) {
			double ans;
			String key = keyDef.getKey();
			ans = defaultSetting;
			if(key != null) {
				boolean keyPresent = preferences.containsKey(key);
				if(keyPresent) {
					ans = preferences.getDouble(key, defaultSetting);
					P.println(printFlags, "key '" + key + "' found in Preferences: " + ans);
				} else {
					P.println(printFlags, "key " + key + " not found in Preferences.");
					if(createAsNeeded.equals(PrefCreateE.CREATE_AS_NEEDED)) {
						preferences.putDouble(key, defaultSetting);
						P.println(printFlags, "key " + key + " created in Preferences with value " + defaultSetting + ".");
					}
				}
			}
			return ans;
    	}
    	public static String getPreference(PrefKeysE keyDef, String defaultSetting, PrtYn printFlags, PrefCreateE createAsNeeded) {
    		String ans;
    		String key = keyDef.getKey();
			ans = defaultSetting;
			if(key != null) {
				boolean keyPresent = preferences.containsKey(key);
				if(keyPresent) {
					ans = preferences.getString(key, defaultSetting);
					P.println(printFlags, "key '" + key + "' found in Preferences: " + ans);
				} else {
					P.println(printFlags, "key " + key + " not found in Preferences.");
					if(createAsNeeded.equals(PrefCreateE.CREATE_AS_NEEDED)) {
						preferences.putString(key, defaultSetting);
						P.println(printFlags, "key " + key + " created in Preferences with value " + defaultSetting + ".");
					}
				}
			}
			return ans;
    	}

    
    
    }
	
    /**
     * wrappers for System.out.println and optional System.out.flush, etc.
     * @author JudiA
     *
     */
    public class P {
        
		/** "print? yes or no" enum */
		public enum PrtYn {Y, N}
		/** "format? yes or no" enum */
		public enum FrmtYn {Y, N}
		/** "flush? yes or no" enum */
		public enum FlushYn {Y, N}
		
	    public static void println(String strToPrtAndFlush) {
	    	println(PrtYn.Y, FlushYn.Y, strToPrtAndFlush);
	    }
	    public static void println(PrtYn prtYorN, String strToPrtAndFlush) {
	    	println(prtYorN, FlushYn.Y, strToPrtAndFlush);
	    }
	    public static void println(FlushYn flushYorN, String strToPrt) {
	    	println(PrtYn.Y, flushYorN, strToPrt);
	    }
	    public static void println(PrtYn prtYorN, FlushYn flushYorN, String strToPrt) {
	    	if(prtYorN.equals(PrtYn.Y)) {
	    		System.out.println(strToPrt);
	    		if(flushYorN.equals(FlushYn.Y)) { System.out.flush(); }
	    	}
	    }
	    
	    public static void printFrmt(String formatStr, Object... args) {
	    	printFrmt(PrtYn.Y, FrmtYn.Y, formatStr, args);
	    }
	    public static void printFrmt(PrtYn prtYesOrNo, String formatStr, Object... args) {
	    	printFrmt(prtYesOrNo, FrmtYn.Y, formatStr, args);
	    }
		/**
		 * String gets formatted only if it's going to get displayed.  Performance should
		 * improve if the message is disabled, but that can sometimes complicate debug of
		 * timing-related problems, so we have a special flag that forces the formatting
		 * even when the message is not going to be displayed. The compiler may recognize 
		 * that the string won't be used and skip the formatting even though we've coded it....
		 * @param flagBits
		 * @param stringToPrint
		 */
		public static void printFrmt(PrtYn prtYesOrNo, FrmtYn frmtYesOrNo, String formatStr, Object... args ) {
			String str;
			if(prtYesOrNo.equals(PrtYn.N)) {
				if(frmtYesOrNo.equals(FrmtYn.Y)) {
					str = String.format(formatStr, args);
				} else {}
			} else {
				System.out.println(String.format(formatStr, args));
				System.out.flush();
			}
		}
		/*
		 *	//minus sign here means "left justify" (pad on right)
		 *	ans = String.format("%-23s", this.name());
		*/

    }

    
    public class DsT {
    	DriverStation m_ds = DriverStation.getInstance();
    	TmDriverStation m_tds = TmDriverStation.getInstance();

//    	//calls to this from doPeriodic caused messages from DS to stop,
//    	//  causing periodic code to never get called again.
//    	public String getStateDescr() {
//    		String ans = String.format("%s, %s", (m_ds.isEnabled() ? "enabled" : "disabled"), 
//    				( (m_ds.isAutonomous() ? "autonomous" : (m_ds.isOperatorControl() ? "teleop" : "liveWindow test") ) ) );
//    		return ans;
//    	}

    	public boolean isLiveWindowTest() {
    		return (m_ds.isTest());
    	}
    	
    	public boolean isDisabled() {
    		return (m_ds.isDisabled());
    	}
    	
    	public boolean isTeleop() {
    		return m_ds.isOperatorControl();
    	}

    	public boolean isAutonomous() {
    		return m_ds.isAutonomous();
    	}

    	public boolean isEnabledLiveWindowTest() {
    		return (m_ds.isEnabled() && m_ds.isTest());
    	}

    	public boolean isEnabledAutonomous() {
    		return (m_ds.isEnabled() && m_ds.isAutonomous());
    	}

    	public boolean isEnabledTeleop() {
    		return (m_ds.isEnabled() && m_ds.isOperatorControl());
    	}
    	
    	public boolean isEnabledAutonomousOrTeleop() { //i.e. not live window....
    		return (m_ds.isEnabled() && (m_ds.isAutonomous() || m_ds.isOperatorControl()));
    	}
    	
    	public boolean isBlueAlliance() {
    		return (m_ds.getAlliance().equals(Alliance.Blue));
    	}
    	
    	public boolean isRedAlliance() {
    		return (m_ds.getAlliance().equals(Alliance.Red));
    	}
    	
    	public AllianceStationID getDsAllianceStation() {
    		return m_tds.getDsAllianceStation();
    	}
    	    	    	
    }
}
