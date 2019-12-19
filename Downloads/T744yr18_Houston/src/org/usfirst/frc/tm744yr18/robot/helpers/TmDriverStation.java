package org.usfirst.frc.tm744yr18.robot.helpers;

import java.io.BufferedInputStream;
import java.io.IOException;

import org.usfirst.frc.tm744yr18.robot.config.TmSdKeysI.SdKeysE;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmToolsI.P;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import edu.wpi.first.wpilibj.hal.AllianceStationID;

public class TmDriverStation {
	//note: FRC DriverStation class cannot be subclassed.  Subclassing it could cause some
	//      of its initialization stuff to be done twice which really hoses things up.
	DriverStation m_ds = DriverStation.getInstance();

	/*---------------------------------------------------------
	 * getInstance stuff                                      
	 *---------------------------------------------------------*/
	/** 
	 * handle making the singleton instance of this class and giving
	 * others access to it
	 */
	private static TmDriverStation m_instance;
	public static synchronized TmDriverStation getInstance()
	{
		if(m_instance == null){ new TmDriverStation(); }
		return m_instance;
	}
	private TmDriverStation() { //change to 'protected' if needs to be subclassed
		if(m_instance == null) { m_instance = this; }
		else { System.out.println("Oops! creating an additional instance of " + this.getClass().getSimpleName() 
				+ ": original: " + m_instance.toString() + " this one: " + this.toString());}
	}
	/*----------------end of getInstance stuff----------------*/

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
		AllianceStationID ans = null;
		Alliance color = m_ds.getAlliance();
		int location = m_ds.getLocation();
		if(color.equals(Alliance.Blue)) {
			switch(location) {
			case 1: ans = AllianceStationID.Blue1; break;
			case 2: ans = AllianceStationID.Blue2; break;
			case 3: ans = AllianceStationID.Blue3; break;
			default: break;
			}
		} else {
			switch(location) {
			case 1: ans = AllianceStationID.Red1; break;
			case 2: ans = AllianceStationID.Red2; break;
			case 3: ans = AllianceStationID.Red3; break;
			default: break;
			}
		}
		if(ans==null) {
			ans = AllianceStationID.Red1;
			P.println("No AllianceID value for color " + color.name() + ", location " + location + ".  Assume " + ans.name());
		}
		return ans;
	}

	
	private static String m_roboRioHostname = null;
	private static int m_roboRioTeamNbr = 0;
	
	/**
	 * Code that will read hostname from roboRIO.  See FRC_Java_Programming document.
	 * this is a time-consuming process the first time it's called
	 * @return the hostname
	 */
	public String getRobotHostname() {
	  if(m_roboRioHostname==null) {
		Runtime run = Runtime.getRuntime();
		Process proc;
		String hostname = "????";
		try {
			proc = run.exec("hostname");
			BufferedInputStream in = new BufferedInputStream(proc.getInputStream());
			byte [] b = new byte[256];
			in.read(b, 0, 256);
			hostname = new String(b).trim();
		} catch(IOException e1) {
			System.out.println("[TmDriverStation:getRobotTeamNumber] " + e1.getMessage() +
					" - cannot read roboRIO hostname");
			e1.printStackTrace();
		}
		m_roboRioHostname = hostname;
	  }
	  return m_roboRioHostname;
	}

	
	/**
	 * Code that will read hostname from roboRIO and return the team number. See FRC_Java_Programming document.
	 * This is a time-consuming process the first time it runs....
	 * @return team number extracted from hostname or 0 or 746 if it's not a recognized hostname
	 */
	public int getRobotTeamNumber() {
		int teamNumber = 0;
		if(m_roboRioHostname==null) {
			m_roboRioHostname = getRobotHostname();
		}
		if(m_roboRioTeamNbr == 0) {
			switch(m_roboRioHostname) {
			case "roboRIO-744-FRC":
				teamNumber = 744;
				break;
			case "roboRIO-745-FRC":
				teamNumber = 745;
				break;
			default:
				teamNumber = 746;
				System.out.println("===> Unrecognized hostname from roboRIO: " + m_roboRioHostname);
				break;
			}
			m_roboRioTeamNbr = teamNumber;
		}

		return m_roboRioTeamNbr;
	}
	
	public boolean isHidDevicePluggedIn(int port) {
		boolean ans = false;
		return ans;
	}

    public static void showDsStateOnSd() {
    	String msg = DriverStation.getInstance().isDisabled() ? "Dis" : "Ena";
		msg += DriverStation.getInstance().isAutonomous() ? "A" :
			   DriverStation.getInstance().isOperatorControl() ? "T" :
			   DriverStation.getInstance().isTest() ? "L" : "?";
		msg += " " + DriverStation.getInstance().getGameSpecificMessage();
    	TmSdMgr.putString(SdKeysE.KEY_DRVSTA_STATE, msg);
    }

	/**
	 * Driver station returns a string indicating which side (left/right as viewed from the alliance driver 
	 * stations) of each mechanism is the Alliance color, beginning with the mechanism closest to the Alliance
	 * driver stations.
	 * see see http://wpilib.screenstepslive.com/s/currentCS/m/getting_started/l/826278-2018-game-data-details
	 * We use this enum to restrict code to the allowed/expected values. The enum member names should match
	 * the expected strings.
	 * @author JudiA
	 *
	 */
	public static enum GameSpecificMsgE {RRR, RLR, LLL, LRL}
	
	/**
	 * get the GameSpecificMsgE enum member corresponding to the
	 * game specific message received from FMS.
	 * @return null if not an expected string
	 */
	public GameSpecificMsgE getGameSpecificMessageEnumVal() {
		GameSpecificMsgE ans = null;
		String temp = DriverStation.getInstance().getGameSpecificMessage();
		for(GameSpecificMsgE e : GameSpecificMsgE.values()) {
			if(e.name().equals(temp)) {
				ans = e;
				break;
			}
		}
		if(ans==null) {
			System.out.println("game specific message string " + temp + 
					" is not a value expected by GameSpecificMsgE");
		}
		return ans;
	}
	
}
