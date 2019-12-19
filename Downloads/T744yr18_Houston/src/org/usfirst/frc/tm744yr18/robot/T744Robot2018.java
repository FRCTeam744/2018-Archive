package org.usfirst.frc.tm744yr18.robot;

import org.usfirst.frc.tm744yr18.bldVerInfo.TmVersionInfo;
import org.usfirst.frc.tm744yr18.robot.config.TmHdwrDsCntls;
import org.usfirst.frc.tm744yr18.robot.config.TmHdwrDsCntls.DsNamedControlsE;
import org.usfirst.frc.tm744yr18.robot.config.TmHdwrDsPhys;
import org.usfirst.frc.tm744yr18.robot.config.TmSdKeysI.SdKeysE;
import org.usfirst.frc.tm744yr18.robot.config.TmHdwrRoCntls;
import org.usfirst.frc.tm744yr18.robot.helpers.TmDriverStation;
import org.usfirst.frc.tm744yr18.robot.helpers.TmForcedInitMgr;
import org.usfirst.frc.tm744yr18.robot.helpers.TmKssMgr;
import org.usfirst.frc.tm744yr18.robot.helpers.TmSdMgr;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmDsControlUserI;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmRoControlUserI;
import org.usfirst.frc.tm744yr18.robot.subsystems.TmSsDriveTrain;
import org.usfirst.frc.tm744yr18.robot.subsystems.TmSsDrvGearShift;
import org.usfirst.frc.tm744yr18.robot.subsystems.TmSsDrvGearShiftDblSol;
//import org.usfirst.frc.tm744yr18.robot.subsystems.TmSsDrvGearShiftDblSol;
import org.usfirst.frc.tm744yr18.robot.subsystems.TmSsGrabber;
import org.usfirst.frc.tm744yr18.robot.subsystems.TmSsArm;
import org.usfirst.frc.tm744yr18.robot.subsystems.TmSsAutonomous;
import org.usfirst.frc.tm744yr18.robot.subsystems.TmSsCameras;
import org.usfirst.frc.tm744yr18.robot.subsystems.TmSsCompressor;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.PowerDistributionPanel;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.command.Scheduler;
import edu.wpi.first.wpilibj.livewindow.LiveWindow;
import t744opts.Tm744Opts;

//public class T744Robot2018 extends TimedRobot implements TmStdFrcRobotMethodsI { //implements TmFlagsI, TmToolsI {
public class T744Robot2018 extends TimedRobot implements TmDsControlUserI, TmRoControlUserI { //implements TmFlagsI, TmToolsI {
	
	public static TmForcedInitMgr m_fiMgr = TmForcedInitMgr.getInstance();
	public static TmKssMgr m_kss = TmKssMgr.getInstance();
	
//	public static TmHdwrRoPhys m_hwRoPhys = TmHdwrRoPhys.getInstance();
//	public static TmHdwrDsPhys m_hwDsPhys = TmHdwrDsPhys.getInstance();
	public static T744Robot2018 m_instance; //for simulation use
	public T744Robot2018() {
		System.out.println("T744Robot2018 constructor called!!");
		
		//this is time-consuming the first time it's called, so do it now
		//before tempermental time-sensitive code gets fired up.
		int teamNbr = TmDriverStation.getInstance().getRobotTeamNumber(); 
//		doConstructorTypeWork();
		m_instance = this;
		
	}
	
    public static Timer m_robotTime = new Timer();
    public static double getTimeSinceBoot() { return m_robotTime.get(); }
    public static String getTimeSinceBootString() {
    	return String.format("%1.4f sec. since robotInit()", m_robotTime.get());
    }
    
    private InitVsPeriodicTimeInfo m_robotInitTimings = new InitVsPeriodicTimeInfo();
    private InitVsPeriodicTimeInfo m_disabledTimings = new InitVsPeriodicTimeInfo();
    private InitVsPeriodicTimeInfo m_autonomousTimings = new InitVsPeriodicTimeInfo();
    private InitVsPeriodicTimeInfo m_teleopTimings = new InitVsPeriodicTimeInfo();

//    private static String m_buildInfoToShow = getBuildInfoToShow();
//    public static String getBuildInfoToShow() {
//    	String ans = String.format("  code built: %s\n  code project: %s", 
//    			TmVersionInfo.getDateTimeHostString(), TmVersionInfo.getProjectName());
//    	return ans;
//    }
        
	
	
	public void doConstructorTypeWork() {
		m_fiMgr.addFiObject(TmHdwrRoCntls.getInstance());
		m_fiMgr.addFiObject(TmHdwrDsCntls.getInstance());
		
		m_kss.addKnownSubsystem(TmSsAutonomous.getInstance(), TmKssMgr.ItemAvailabilityE.ACTIVE);
		m_kss.addKnownSubsystem(TmSsDriveTrain.getInstance(), TmKssMgr.ItemAvailabilityE.ACTIVE);
		m_kss.addKnownSubsystem(TmSsArm.getInstance(), TmKssMgr.ItemAvailabilityE.ACTIVE);
		
		if(Tm744Opts.isDblSolForGearShift()) {
			m_kss.addKnownSubsystem(TmSsDrvGearShiftDblSol.getInstance(), TmKssMgr.ItemAvailabilityE.ACTIVE);
		} else {
			m_kss.addKnownSubsystem(TmSsDrvGearShift.getInstance(), TmKssMgr.ItemAvailabilityE.ACTIVE);
		}
		
		m_kss.addKnownSubsystem(TmSsGrabber.getInstance(), TmKssMgr.ItemAvailabilityE.ACTIVE);
		m_kss.addKnownSubsystem(TmSsCameras.getInstance(), TmKssMgr.ItemAvailabilityE.ACTIVE);
		m_kss.addKnownSubsystem(TmSsCompressor.getInstance(), TmKssMgr.ItemAvailabilityE.ACTIVE);

		m_fiMgr.doForcedInstantiation();		
		m_fiMgr.doPopulate();
		
		m_kss.sssDoInstantiate();
	}

	@Override
	public void robotInit() {
		LiveWindow.disableAllTelemetry();
		
    	System.out.println("====TEAM 744 ROBOT - entered robotInit() ");//at time " + getTimeSinceBootString() + "!");
    	System.out.println("-----code built " + TmVersionInfo.getDateTimeHostString());
    	System.out.println("-----code project " + TmVersionInfo.getProjectName());
    	if(TmHdwrRoCntls.RUN_STF) {
        	System.out.println("        ====================================================");
        	System.out.println("        ====================================================");
        	System.out.println("        =====                                          =====");
        	System.out.println("        =====                                          =====");
        	System.out.println("        =====                 !!!!!!                   =====");
        	System.out.println("        =====                                          =====");
        	System.out.println("        =====                                          =====");
        	System.out.println("        =====          CODE for use only on            =====");
        	System.out.println("        =====                                          =====");
        	System.out.println("        =====         SOFTWARE TEST FIXTURE            =====");
        	System.out.println("        =====                                          =====");
        	System.out.println("        =====                                          =====");
        	System.out.println("        =====                 !!!!!!                   =====");
        	System.out.println("        =====                                          =====");
        	System.out.println("        =====                                          =====");
        	System.out.println("        ====================================================");
        	System.out.println("        ====================================================");
    	}
    	System.out.flush();
    	System.out.println("Actual team number is: " + TmDriverStation.getInstance().getRobotTeamNumber());
    	System.out.println("GameSpecificMessage: " + DriverStation.getInstance().getGameSpecificMessage());
    	//details about interpretation of the game specific data are available at
    	//  http://wpilib.screenstepslive.com/s/currentCS/m/getting_started/l/826278-2018-game-data-details
    	System.out.flush();
    	
		doConstructorTypeWork();

		//time-consuming....Driver Station complains...
		if(false) {
			TmHdwrRoCntls.getInstance().showEverything();
			TmHdwrDsCntls.getInstance().showEverything();
		}
		
		m_kss.sssDoRobotInit();
		
		TmSdMgr.putString(SdKeysE.KEY_CODE_BUILD_INFO, 
				(TmHdwrRoCntls.RUN_STF ? "For SW Test Fixture only!!!! -- " : "") +
				TmVersionInfo.getProjectName() + " -- " + TmVersionInfo.getDateTimeHostString());

		//drv sta control command handled in TmHdwrDsCntls.
//		DsNamedControlsE.SHOW_DRVSTA_CNTLS_ON_CONSOLE.getEnt().whenPressed(this, 
//							TmHdwrDsCntls.LocalCommands.getInstance().new LocalCmd_ShowAllDsIoOnConsole());
		//hmm... this may cause problems if try to do it from TmHdwrRoCntls.doPopulate(), so do it here instead
		DsNamedControlsE.SHOW_ROBOT_CNTLS_ON_CONSOLE.getEnt().whenPressed(this, 
				TmHdwrRoCntls.LocalCommands.getInstance().new LocalCmd_ShowAllRoIoOnConsole());

		Tm744Opts.postPrefBootOptsToSd();
		
		if( ! Tm744Opts.isInSimulationMode()) {
			PowerDistributionPanel pdp = new PowerDistributionPanel(0);
			pdp.clearStickyFaults();
		}
		
		
	}
	
	public void testThingsInCode() {
		double analogData;
		boolean booleanData;
		int intData;
//		analogData = DsNamedControlsE.DRIVE_LEFT_INPUT.getAnalog();
//		booleanData = DsNamedControlsE.SOME_JS_BUTTON.getButton();
		
//		intData = DsNamedControlsE.SOME_XBOX_POV.getPov();
//		booleanData = DsNamedControlsE.SOME_XBOX_BUTTON.getButton();
//		analogData = DsNamedControlsE.SOME_XBOX_JOYSTICK.getAnalog();
//		booleanData = DsNamedControlsE.SOME_ANALOG_BUTTON.getButton();
//		booleanData = DsControlsMgr.getButton(DsNamedControlsE.SOME_ANALOG_BUTTON.getEnt());
//		booleanData = DsNamedControlsE.SOME_ANALOG_BUTTON.getEnt().getButton();
//		booleanData = DsNamedControlsE.SOME_POV_BUTTON.getButton(); -- gets exception when controller not connected
		
	  //booleanData = DsNamedControlsE.DRIVE_LEFT_INPUT.getButton(); //should get error -- it did!!
		
		
//		TmHdwrDsCntls.showConnections(ShowDsConnectionsSortingE.GROUP_BY_DEVICE);
//		TmHdwrRoCntls.showConnections(ShowRoConnectionsSortingE.GROUP_BY_MODULE);
//		P.println("\n\n\n");
//		TmHdwrDsCntls.showConnections(ShowDsConnectionsSortingE.ENUM_ORDER);
//		TmHdwrRoCntls.showConnections(ShowRoConnectionsSortingE.ENUM_ORDER);
	}

	@Override
	public void disabledInit() {
		System.out.println("====TEAM 744 ROBOT - entered disabledInit()!");
//    	System.out.println("-----code built " + TmVersionInfo.getDateTimeHostString());
//    	System.out.println("-----code project " + TmVersionInfo.getProjectName());
    	System.out.println("-----GameSpecificMessage: " + DriverStation.getInstance().getGameSpecificMessage());
    	System.out.flush();
		m_kss.sssDoDisabledInit();
	}

	@Override
	public void autonomousInit() {
    	System.out.println("====TEAM 744 ROBOT - entered autonomousInit()!");
		m_kss.sssDoAutonomousInit();
	}

	@Override
	public void teleopInit() {
    	System.out.println("====TEAM 744 ROBOT - entered teleopInit()!");
		m_kss.sssDoTeleopInit();
		
		testThingsInCode();
	}

	@Override
	public void disabledPeriodic() {
		m_kss.sssDoDisabledPeriodic();
		Scheduler.getInstance().run();
	}

	@Override
	public void autonomousPeriodic() {
		m_kss.sssDoAutonomousPeriodic();
		Scheduler.getInstance().run();
	}

	@Override
	public void teleopPeriodic() {
		m_kss.sssDoTeleopPeriodic();
		TmHdwrDsPhys.doTeleopPeriodic();
		Scheduler.getInstance().run();
//		SmartDashboard.putNumber("Ds"+DsNamedControlsE.DRIVE_LEFT_INPUT.name(), DsNamedControlsE.DRIVE_LEFT_INPUT.getAnalog());
////		SmartDashboard.putBoolean("Ds"+DsNamedControlsE.SOME_FAKE_JS_THING.name(), DsNamedControlsE.SOME_FAKE_JS_THING.getButton());
	}

	@Override
	public void testInit() {
    	System.out.println("====TEAM 744 ROBOT - entered Live Window testInit()!");
		m_kss.sssDoLwTestInit();
	}

	@Override
	public void testPeriodic() {
		m_kss.sssDoLwTestPeriodic();
	}

	@Override
	public void robotPeriodic() {
		TmDriverStation.showDsStateOnSd();
		m_kss.sssDoRobotPeriodic();
	}
	
    private class InitVsPeriodicTimeInfo {
    	double timeInitEntered;
    	double timeInitExited;
    	double timePeriodicEntered;
    	double timePeriodicExited;
    	double minTimeInInit;
    	double maxTimeInInit;
    	double minTimeInitToPeriodic;
    	double maxTimeInitToPeriodic;
    	double minTimeInPeriodic;
    	double maxTimeInPeriodic;
    	boolean firstTimeInPeriodicAfterInit;
    	InitVsPeriodicTimeInfo() { 
    		timeInitEntered = 0;
    		timeInitExited = 0;
    		timePeriodicEntered = 0;
    		timePeriodicExited = 0;
    		minTimeInInit = 9999;
        	maxTimeInInit = 0;
        	minTimeInitToPeriodic = 9999;
        	maxTimeInitToPeriodic = 0;
        	minTimeInPeriodic = 9999;
        	maxTimeInPeriodic = 0;
    		firstTimeInPeriodicAfterInit = true;
    	}
    	
    	public String getMinMaxInfoString() {
    		return String.format("(min/max seconds) init: [%1.5f, %1.5f] between: [%1.5f, %1.5f] " + 
    						"periodic: [%1.5f, %1.5f]",
    						minTimeInInit, maxTimeInInit, minTimeInitToPeriodic, maxTimeInitToPeriodic,
    						minTimeInPeriodic, maxTimeInPeriodic);
    	}
    	
    	public void enterInit() {
    		timeInitEntered = getTimeSinceBoot();
    		timeInitExited = 0;
    		timePeriodicEntered = 0;
    		timePeriodicExited = 0;
    		firstTimeInPeriodicAfterInit = true;
    	}
    	
    	public void exitInit() {
    		timeInitExited = getTimeSinceBoot();
    		double delta = timeInitExited - timeInitEntered;
    		if(delta < minTimeInInit) { minTimeInInit = delta; }
    		else if(delta > maxTimeInInit) { maxTimeInInit = delta; }
    		else {}
    		timePeriodicEntered = 0;
    		timePeriodicExited = 0;
    		firstTimeInPeriodicAfterInit = true;
    	}
    	
    	public void enterPeriodic() {
    		timePeriodicEntered = getTimeSinceBoot();
    		if(firstTimeInPeriodicAfterInit) {
    			timePeriodicExited = 0;
    			double delta = timePeriodicEntered - timeInitExited;
    			if(delta < minTimeInitToPeriodic) { minTimeInitToPeriodic = delta; }
    			else if(delta > maxTimeInitToPeriodic) { maxTimeInitToPeriodic = delta; }
    			else {}
    		}
			firstTimeInPeriodicAfterInit = false;
    	}

    	public void exitPeriodic() {
    		timePeriodicExited = getTimeSinceBoot();
    		double delta = timePeriodicExited - timePeriodicEntered;
    		if(delta < minTimeInPeriodic) { minTimeInPeriodic = delta; }
    		else if(delta > maxTimeInPeriodic) { maxTimeInPeriodic = delta; }
    		else {}
    	}
    }

    /**
     * code anywhere can call this to indicate that it needs configuration, tuning,
     * or just general changes to code.  Can then set a breakpoint here to find
     * all those places so that none of them get forgotten.
     */
    public void fixMePlease() {
    	int junk = 5; //just a good debugger breakpoint
    }

}
