package org.usfirst.frc.tm744yr18.robot.subsystems;

import java.util.ArrayList;
import java.util.List;

import org.usfirst.frc.tm744yr18.robot.cmdGroups.TmACGrpTrajLeftSwitchAndRelease;
import org.usfirst.frc.tm744yr18.robot.cmdGroups.TmACGrpTrajMoveToSwitchOrScaleAndRelease;
import org.usfirst.frc.tm744yr18.robot.cmdGroups.TmACGrpTrajRightStartScale1CubeAmmenable;
import org.usfirst.frc.tm744yr18.robot.cmdGroups.TmACGrpTrajRightSwitchAndRelease;
import org.usfirst.frc.tm744yr18.robot.cmdGroups.TmACGrpTrajLeftStartScaleTwoCube.TrajLeftStartScaleTwoCubeOptionsE;
import org.usfirst.frc.tm744yr18.robot.cmdGroups.TmACGrpTrajCenterSwitchAndReleaseCube;
import org.usfirst.frc.tm744yr18.robot.cmdGroups.TmACGrpTrajLeftStartScaleTwoCube;
import org.usfirst.frc.tm744yr18.robot.cmdGroups.TmACGrpTrajCenterSwitchReleaseAndAcquireCube;
import org.usfirst.frc.tm744yr18.robot.commands.TmACmdDoNothing;
import org.usfirst.frc.tm744yr18.robot.commands.TmACmdDriveByLimelight;
import org.usfirst.frc.tm744yr18.robot.commands.TmACmdDriveByVision;
import org.usfirst.frc.tm744yr18.robot.commands.TmACmdDriveStraight;
import org.usfirst.frc.tm744yr18.robot.commands.TmACmdDriveStraightWithGyroThenStop;
import org.usfirst.frc.tm744yr18.robot.commands.TmACmdFollowTrajectoryLeftOrRight;
import org.usfirst.frc.tm744yr18.robot.commands.TmACmdFollowTrajectoryLeftOrRight.ShowFile;
import org.usfirst.frc.tm744yr18.robot.commands.TmACmdFollowTrajectoryLeftOrRight.TrajDest;
import org.usfirst.frc.tm744yr18.robot.commands.TmACmdFollowTrajectorySwitchOrScale;
import org.usfirst.frc.tm744yr18.robot.config.TmSdKeysI;
import org.usfirst.frc.tm744yr18.robot.config.TmSdKeysI.SdKeysE;
import org.usfirst.frc.tm744yr18.robot.helpers.TmDriverStation;
import org.usfirst.frc.tm744yr18.robot.helpers.TmHdwrItemEnableDisableMgr;
import org.usfirst.frc.tm744yr18.robot.helpers.TmSdMgr;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmEnumWithListI;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmListBackingEnumI;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmStdSubsystemI;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmToolsI.P;
import org.usfirst.frc.tm744yr18.robot.subsystems.TmSsArm.LiftPosE;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.command.Command;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;

public class TmSsAutonomous implements TmStdSubsystemI {

	/*---------------------------------------------------------
	 * getInstance stuff                                      
	 *---------------------------------------------------------*/
	/** 
	 * handle making the singleton instance of this class and giving
	 * others access to it
	 */
	private static TmSsAutonomous m_instance;

	public static synchronized TmSsAutonomous getInstance() {
		if (m_instance == null) {
			m_instance = new TmSsAutonomous();
		}
		return m_instance;
	}

	private TmSsAutonomous() {
		if ( ! (m_instance == null)) {
			P.println("Error!!! TmSsAutonomous.m_instance is being modified!!");
			P.println("         was: " + m_instance.toString());
			P.println("         now: " + this.toString());
		}
		m_instance = this;
	}
	/*----------------end of getInstance stuff----------------*/

	/*-------------------------------------------------------------
	 *      autonomous algorithm select SmartDashboard support 
	 *-------------------------------------------------------------*/

	public static SendableChooser<Command> autoChooser = null;

	Command selectedCmdRaw = null; //used only for debugging 'chooser' problems
//	AutonomousAlgE selectedAlg = null; //AutonomousAlgE.SELECTED_ALG_NOT_FOUND;
	AutonAlgEntry selectedAlg = null; //AutonomousAlgE.SELECTED_ALG_NOT_FOUND;
	
	/**
	 * represents autonomous algorithm coding status. Provides a string to be
	 * displayed on SmartDashboard to indicate the status of the item in the
	 * 'chooser'.
	 */
	public enum AutonAlgStatusE {
		kReady("OK"),
		kTest("Test"),
		kCoded("Coded"),
		kTBD("TBD"),
		kUnused("n/a"),
		kBetaTest("Beta"),
		kGood("GOOD"),
		kBad("BAD!");

		/**
		 * this is the string to be displayed on SmartDashboard for algorithms
		 * tagged with this status type 
		 */
		public String descr;
		public String descrRaw;

		private AutonAlgStatusE(String smartDashDescrString) {
			descrRaw = smartDashDescrString;
			//'-' means left-justify, 5 is the width, s is String
			descr = String.format("%-5s", descrRaw);
		}
		
		public String getSmartDashDescription() { return descr; }
	}

	public enum AutonAlgAvailableE {
		ALWAYS, RED, BLUE, R1, R2, R3, B1, B2, B3;
		
		public boolean isAvailableForStation() {
			boolean ans = false;
			switch(this) {
			case ALWAYS: 
				ans = true;
				break;
			case RED:
				if(DriverStation.getInstance().getAlliance().equals(DriverStation.Alliance.Red)) {
					ans = true;
				}
				break;
			case BLUE:
				if(DriverStation.getInstance().getAlliance().equals(DriverStation.Alliance.Blue)) {
					ans = true;
				}
				break;
			case R1:
				if(DriverStation.getInstance().getAlliance().equals(DriverStation.Alliance.Red) &&
						DriverStation.getInstance().getLocation() == 1) {
					ans = true;
				}
				break;
			case R2:
				if(DriverStation.getInstance().getAlliance().equals(DriverStation.Alliance.Red) &&
						DriverStation.getInstance().getLocation() == 2) {
					ans = true;
				}
				break;
			case R3:
				if(DriverStation.getInstance().getAlliance().equals(DriverStation.Alliance.Red) &&
						DriverStation.getInstance().getLocation() == 3) {
					ans = true;
				}
				break;
			case B1:
				if(DriverStation.getInstance().getAlliance().equals(DriverStation.Alliance.Blue) &&
						DriverStation.getInstance().getLocation() == 1) {
					ans = true;
				}
				break;
			case B2:
				if(DriverStation.getInstance().getAlliance().equals(DriverStation.Alliance.Blue) &&
						DriverStation.getInstance().getLocation() == 2) {
					ans = true;
				}
				break;
			case B3:
				if(DriverStation.getInstance().getAlliance().equals(DriverStation.Alliance.Blue) &&
						DriverStation.getInstance().getLocation() == 3) {
					ans = true;
				}
				break;
			default: ans = true; break;
			}
			return ans;
		}
	}

	/** if we had hardware switches, this is where we'd name the individual
	 *  settings and save related info. See 2014/2015 code for example */
	public enum AutonSwitchAlgsE {
		//if switches are available, one of the possible settings
		//should be assigned to mean "use smart dashboard". This
		//enum should be associated with that setting and should
		//also be used if there are no switches on the robot
		AUTON_SWITCHES_ALG_SMARTDASHBOARD;
	}
	static AutonSwitchAlgsE autonAlg = AutonSwitchAlgsE.AUTON_SWITCHES_ALG_SMARTDASHBOARD;

	/*------------------------------------------------------------
	 * list various autonomous algorithms and related info        
	 *------------------------------------------------------------*/
	/**
	 * this enum holds information about each autonomous algorithm and is used
	 * to set up the "chooser" on the smartdashboard
	 * @author JudiA
	 *
	 */
	public enum AutonomousAlgE implements TmEnumWithListI<AutonAlgEntry> { //CONFIG_ME
		ALG_DO_NOTHING("Do Nothing", 
				new TmACmdDoNothing(),	AutonAlgStatusE.kGood),		
//		ALG_CFG_ENGAGE_LOW_GEAR("For Pit use: Drive just enough to engage low gear", 
//				new TmACGrpCfgEngageLowGear(),	AutonAlgStatusE.kGood),
		
		
		ALG_TRAJ_CENTER_TO_SWITCH_RELEASE_AND_GET_CUBE("Traj: CENTER moves to SWITCH - RELEASES and ACQUIRES", 
				new TmACGrpTrajCenterSwitchReleaseAndAcquireCube(),
				AutonAlgStatusE.kGood),

		
		ALG_TRAJ_CENTER_TO_SWITCH_AND_RELEASE("Traj: CENTER moves to SWITCH and RELEASES", 
				new TmACGrpTrajCenterSwitchAndReleaseCube("trajCenterToLeftSwitch.csv", "trajCenterToRightSwitch.csv",
						ShowFile.N, ShowFile.N),
				AutonAlgStatusE.kGood),
		ALG_TRAJ_LEFT_TO_SWITCH_AND_RELEASE("Traj: LEFT moves to SWITCH and RELEASES",
				new TmACGrpTrajLeftSwitchAndRelease("trajLeftToLeftSwitch.csv", "trajLeftToRightSwitch.csv"),
				AutonAlgStatusE.kCoded),
		ALG_TRAJ_RIGHT_TO_SWITCH_AND_RELEASE("Traj: RIGHT moves to SWITCH and RELEASES",
				new TmACGrpTrajRightSwitchAndRelease("trajRightToRightSwitch.csv", "trajRightToLeftSwitch.csv"),
				AutonAlgStatusE.kCoded),
		
		ALG_TRAJ_CENTER_TO_LEFT_SCALE_OR_SWITCH_RELEASE("Traj: CENTER to LEFT Switch OR Scale And RELEASES",
				new TmACGrpTrajMoveToSwitchOrScaleAndRelease('L', "trajCenterToLeftSwitch.csv", "trajCenterToLeftScale.csv", "traj.csv"),
				AutonAlgStatusE.kCoded),
		ALG_TRAJ_CENTER_TO_RIGHT_SCALE_OR_SWITCH_RELEASE("Traj: CENTER to RIGHT Switch OR Scale And RELEASES",
				new TmACGrpTrajMoveToSwitchOrScaleAndRelease('R', "trajCenterToRightSwitch.csv", "trajCenterToRightScale.csv", "traj.csv" ),
				AutonAlgStatusE.kCoded),

		ALG_TRAJ_LEFT_TO_LEFT_SCALE_OR_SWITCH_RELEASE("Traj: LEFT to LEFT Switch OR Scale And RELEASES",
				new TmACGrpTrajMoveToSwitchOrScaleAndRelease('L', "trajLeftToLeftSwitch.csv", "trajLeftToLeftScale.csv", "traj.csv"),
				AutonAlgStatusE.kCoded),
		ALG_TRAJ_RIGHT_TO_RIGHT_SCALE_OR_SWITCH_RELEASE("Traj: RIGHT to RIGHT Switch OR Scale And RELEASES",
				new TmACGrpTrajMoveToSwitchOrScaleAndRelease('R', "trajRightToRightSwitch.csv", "trajRightToRightScale.csv", "traj.csv" ),
				AutonAlgStatusE.kCoded),

//		ALG_TRAJ_LEFT_TO_EITHER_SCALE_RELEASE("Traj: LEFT to L OR R SCALE ONLY and RELEASES",
//				new TmACmdFollowTrajectoryLeftOrRight(TrajDest.SCALE, "trajLeftToLeftScale.csv", "trajLeftToRightScale.csv", ShowFile.N, ShowFile.N), AutonAlgStatusE.kCoded),
//		ALG_TRAJ_RIGHT_TO_EITHER_SCALE_RELEASE("Traj: RIGHT to L OR R SCALE ONLY and RELEASES",
//				new TmACmdFollowTrajectoryLeftOrRight(TrajDest.SCALE, "trajRightToLeftScale.csv", "trajRightToRightScale.csv", ShowFile.N, ShowFile.N), AutonAlgStatusE.kCoded),
		ALG_TRAJ_LEFT_TO_EITHER_SCALE_NO_RELEASE("Traj: LEFT to L OR R SCALE ONLY - 2-cube trajectories only",
				new TmACGrpTrajLeftStartScaleTwoCube(TrajLeftStartScaleTwoCubeOptionsE.RIGHT_SCALE_FIRST_CUBE_AND_BACK_ONLY), 
				AutonAlgStatusE.kCoded),
		ALG_TRAJ_LEFT_TO_EITHER_SCALE_RELEASE("Traj: LEFT to L OR R SCALE ONLY and RELEASES (start 2 cube)",
				new TmACGrpTrajLeftStartScaleTwoCube(), AutonAlgStatusE.kCoded),
		ALG_TRAJ_RIGHT_TO_EITHER_SCALE_RELEASE("Traj: RIGHT to L OR R SCALE ONLY and RELEASES (start 1 cube...)",
				new TmACGrpTrajRightStartScale1CubeAmmenable(), AutonAlgStatusE.kCoded),

		

		ALG_TRAJ_CENTER_TO_LEFT_SCALE_OR_SWITCH("Traj: CENTER to LEFT Switch OR Scale",
				new TmACmdFollowTrajectorySwitchOrScale('L', "trajCenterToLeftSwitch.csv", "trajCenterToLeftScale.csv", "traj.csv"),
				AutonAlgStatusE.kCoded),
		ALG_TRAJ_CENTER_TO_RIGHT_SCALE_OR_SWITCH("Traj: CENTER to RIGHT Switch OR Scale",
				new TmACmdFollowTrajectorySwitchOrScale('R', "trajCenterToRightSwitch.csv", "trajCenterToRightScale.csv", "traj.csv" ),
				AutonAlgStatusE.kCoded),
		
		
//		ALG_TRAJ_LEFT_TO_SCALE_AND_RELEASE("Traj: LEFT moves to SCALE and RELEASES",
//		new TmACGrpTrajRightSwitchAndRelease("trajLeftToLeftSwitch.csv", "trajLeftToRightSwitch.csv"),
//		AutonAlgStatusE.kCoded),
//		ALG_TRAJ_RIGHT_TO_SCALE_AND_RELEASE("Traj: RIGHT moves to SCALE and RELEASES",
//				new TmACGrpTrajRightSwitchAndRelease("trajLeftToLeftSwitch.csv", "trajLeftToRightSwitch.csv"),
//				AutonAlgStatusE.kCoded),

		ALG_TRAJ_CENTER_TO_SWITCH("Traj: CENTER moves to SWITCH", 
				new TmACmdFollowTrajectoryLeftOrRight(TrajDest.SWITCH, LiftPosE.SWITCH, 
						"trajCenterToLeftSwitch.csv", "trajCenterToRightSwitch.csv", ShowFile.N, ShowFile.N), AutonAlgStatusE.kGood),
		ALG_TRAJ_LEFT_TO_SWITCH("Traj: LEFT moves to SWITCH", 
				new TmACmdFollowTrajectoryLeftOrRight(TrajDest.SWITCH, LiftPosE.SWITCH, 
						"trajLeftToLeftSwitch.csv", "trajLeftToRightSwitch.csv", ShowFile.N, ShowFile.N), AutonAlgStatusE.kGood),
		ALG_TRAJ_RIGHT_TO_SWITCH("Traj: RIGHT moves to SWITCH", 
				new TmACmdFollowTrajectoryLeftOrRight(TrajDest.SWITCH, LiftPosE.SWITCH, 
						"trajRightToLeftSwitch.csv", "trajRightToRightSwitch.csv", ShowFile.N, ShowFile.N), AutonAlgStatusE.kGood),
				
		ALG_TRAJ_CENTER_TO_SCALE("Traj: CENTER moves to SCALE", 
				new TmACmdFollowTrajectoryLeftOrRight(TrajDest.SCALE, LiftPosE.TOP,
						"trajCenterToLeftScale.csv", "trajCenterToRightScale.csv", ShowFile.N, ShowFile.N), AutonAlgStatusE.kGood),
		
//		ALG_TRAJ_LEFT_TO_LEFT_SWITCH("Traj: LEFT moves to LEFT SIDE SWITCH", 
//				new TmACmdFollowTrajectory("trajLeftToLeftSwitch.csv", 'L', 'A', TmACmdFollowTrajectory.NO_SHOW_FILE), AutonAlgStatusE.kCoded),
//		ALG_TRAJ_LEFT_TO_RIGHT_SWITCH("Traj: LEFT moves to RIGHT SIDE SWITCH", 
//				new TmACmdFollowTrajectory("trajLeftToRightSwitch.csv", 'R', 'A', TmACmdFollowTrajectory.NO_SHOW_FILE), AutonAlgStatusE.kCoded),
//		ALG_TRAJ_RIGHT_TO_LEFT_SWITCH("Traj: RIGHT moves to LEFT SIDE SWITCH", 
//				new TmACmdFollowTrajectory("trajRightToLeftSwitch.csv", 'L', 'A', TmACmdFollowTrajectory.NO_SHOW_FILE), AutonAlgStatusE.kCoded),
//		ALG_TRAJ_RIGHT_TO_RIGHT_SWITCH("Traj: RIGHT moves to RIGHT SIDE SWITCH", 
//				new TmACmdFollowTrajectory("trajRightToRightSwitch.csv", 'R', 'A', TmACmdFollowTrajectory.NO_SHOW_FILE), AutonAlgStatusE.kCoded),
//		ALG_TRAJ_CENTER_TO_LEFT_SCALE("Traj: CENTER moves to LEFT SIDE SCALE", 
//				new TmACmdFollowTrajectory("trajCenterToLeftScale.csv", 'A', 'L', TmACmdFollowTrajectory.NO_SHOW_FILE), AutonAlgStatusE.kCoded),
//		ALG_TRAJ_CENTER_TO_RIGHT_SCALE("Traj: CENTER moves to RIGHT SIDE SCALE", 
//				new TmACmdFollowTrajectory("trajCenterToRightScale.csv", 'A', 'R', TmACmdFollowTrajectory.NO_SHOW_FILE), AutonAlgStatusE.kCoded),
		
		ALG_TRAJ_LEFT_SCALE_OR_SWITCH("Traj: LEFT to Switch OR Scale",
				new TmACmdFollowTrajectorySwitchOrScale('L', "trajLeftToLeftSwitch.csv", "trajLeftToLeftScale.csv", "traj.csv"),
				AutonAlgStatusE.kCoded),
		ALG_TRAJ_RIGHT_SCALE_OR_SWITCH("Traj: RIGHT to Switch OR Scale",
				new TmACmdFollowTrajectorySwitchOrScale('R', "trajRightToRightSwitch.csv", "trajRightToRightScale.csv", "traj.csv" ),
				AutonAlgStatusE.kCoded),
		
		ALG_TEST_DRIVE_STRAIGHT_TEST("For Lab test: the drive straight one", 
				new TmACmdDriveStraight(4, 0.5), AutonAlgStatusE.kCoded),
		ALG_TEST_DRIVE_STRAIGHT_GYRO_TEST("For Lab test: drive straight with gyro", 
				//distance, timeout
				new TmACmdDriveStraightWithGyroThenStop(10.0, 3.0), AutonAlgStatusE.kCoded),
		
		ALG_TEST_DRIVE_BY_VISION("For Lab test: drive by VISION", 
				new TmACmdDriveByVision(), AutonAlgStatusE.kCoded),
		
		ALG_TEST_DRIVE_BY_LIMELIGHT("For Lab test: drive by LIMELIGHT", 
				new TmACmdDriveByLimelight(), AutonAlgStatusE.kCoded),
		
//		ALG_TEST_AT_SWITCH_RELEASE_CUBE("For Lab test: assume at switch, release cube", 
//				new TmACGrpAtSwitchReleaseCube(), AutonAlgStatusE.kCoded),
//		ALG_TEST_TRAJ_CUSTOM("For Lab use: test trajectory kCustomAuto", 
//				new TmACmdTestTrajectories(TrajectoryTestsE.kCustomAuto), AutonAlgStatusE.kCoded),
//		ALG_TEST_TRAJ_OPENLOOP_STRAIGHT("For Lab use: test trajectory kOpenloopStraightAuto", 
//				new TmACmdTestTrajectories(TrajectoryTestsE.kOpenloopStraightAuto), AutonAlgStatusE.kCoded),
//		ALG_TEST_TRAJ_CLOSEDLOOP_STRAIGHT("For Lab use: test trajectory kClosedloopStraightAuto", 
//				new TmACmdTestTrajectories(TrajectoryTestsE.kClosedloopStraightAuto), AutonAlgStatusE.kCoded),
//		ALG_TEST_TRAJ_SWITCH_TRAJ("For Lab use: test trajectory kSwitchTrajAuto", 
//				new TmACmdTestTrajectories(TrajectoryTestsE.kSwitchTrajAuto), AutonAlgStatusE.kCoded),
//		ALG_TEST_FOLLOW_TRAJ_CUSTOM("For Lab use: test follow trajectory command", 
//				new TmACmdFollowTrajectory("traj.csv", 'A', 'A', TmACmdFollowTrajectory.NO_SHOW_FILE), AutonAlgStatusE.kCoded),
//		ALG_TEST_FOLLOW_TRAJ_CUSTOM_ALT("For Lab use: alternate test follow trajectory command", 
//				new TmACmdFollowTrajectory_play("traj_bagNight.csv", 'A', 'A', TmACmdFollowTrajectory.NO_SHOW_FILE), AutonAlgStatusE.kCoded),
//		ALG_TEST_PREP_FOR_GET_CUBE("For Lab test: prepare tower and grabber to get a cube", 
//				new TmCGrpCubePrep(), AutonAlgStatusE.kCoded),
//		ALG_TEST_PICK_UP_CUBE("For Lab test: test pick up cube cmd grp", 
//				new TmCCGrpPickUpCube(), AutonAlgStatusE.kCoded),

		SELECTED_ALG_NOT_FOUND("(alg not found - do nothing)", 
				TmSsAutonomous.getInstance().new Local_DefaultAutonCmdDoNothing(), AutonAlgStatusE.kUnused)
		;
		
		private AutonomousAlgE(String cmdDescription, Command cmdToRun, 
				AutonAlgStatusE algStatus) {
			this(AutonSwitchAlgsE.AUTON_SWITCHES_ALG_SMARTDASHBOARD, cmdDescription,
					cmdToRun, AutonAlgAvailableE.ALWAYS, algStatus);
		}
		private AutonomousAlgE(String cmdDescription, Command cmdToRun, AutonAlgAvailableE available, 
				AutonAlgStatusE algStatus) {
			this(AutonSwitchAlgsE.AUTON_SWITCHES_ALG_SMARTDASHBOARD, cmdDescription,
					cmdToRun, available, algStatus);
		}
		private AutonomousAlgE(AutonSwitchAlgsE autonSwitchesAlgType, String cmdDescription, 
				Command cmdToRun, AutonAlgAvailableE available, AutonAlgStatusE algStatus) {
			getList().add(this.ordinal(), m_instance.new AutonAlgEntry(this, autonSwitchesAlgType, 
					cmdDescription, cmdToRun, available, algStatus));
		}
		
		@Override
		public List<AutonAlgEntry> getList() { return autonAlgList; }
		public static List<AutonAlgEntry> staticGetList() {return autonAlgList;}
		
		@Override
		public AutonAlgEntry getEnt() { return getList().get(this.ordinal()); }
		@Override
		public String getEnumClassName() { return this.getClass().getSimpleName(); }
		@Override
		public String getListEntryClassName() { return AutonAlgEntry.class.getSimpleName(); }

	}

	private static List<AutonAlgEntry> autonAlgList = new ArrayList<>();
	public class AutonAlgEntry implements TmListBackingEnumI<AutonAlgEntry, AutonomousAlgE> {

		@Override
		public List<AutonAlgEntry> getListBackingEnum() { return autonAlgList; }
		@Override
		public TmHdwrItemEnableDisableMgr getHdwrItemEnableDisableMgr() { return null; }

		private final AutonomousAlgE eEnumEnt;
//		private final AutonSwitchAlgsE eHwSwitchInfo;
		private final String eCmdDescription;
		private final Command eCmdToRun;
		private final AutonAlgStatusE eAlgStatus;
		private final String eNdxString;
		private final String eSmartDashboardString;
		private final String eShowString;
		private final String eShowStringFormatted;
		private final AutonAlgAvailableE eWhenAvailable;

		private AutonAlgEntry(AutonomousAlgE enumEnt, String cmdDescription, Command cmdToRun, 
				AutonAlgStatusE algStatus) {
			this(enumEnt, AutonSwitchAlgsE.AUTON_SWITCHES_ALG_SMARTDASHBOARD, cmdDescription,
					cmdToRun, AutonAlgAvailableE.ALWAYS, algStatus);
		}
		private AutonAlgEntry(AutonomousAlgE enumEnt, String cmdDescription, Command cmdToRun, AutonAlgAvailableE available, 
				AutonAlgStatusE algStatus) {
			this(enumEnt, AutonSwitchAlgsE.AUTON_SWITCHES_ALG_SMARTDASHBOARD, cmdDescription,
					cmdToRun, available, algStatus);
		}
		private AutonAlgEntry(AutonomousAlgE enumEnt, AutonSwitchAlgsE autonSwitchesAlgType, String cmdDescription, 
				/*String lcdStr,*/ Command cmdToRun, AutonAlgAvailableE available,
				AutonAlgStatusE algStatus) {
			eEnumEnt = enumEnt;
//			eHwSwitchInfo = autonSwitchesAlgType;
			eCmdDescription = cmdDescription;
			eCmdToRun = cmdToRun;
			eAlgStatus = algStatus;	
			eWhenAvailable = available;
			eNdxString = "" + enumEnt.ordinal(); //eNdx;
			eSmartDashboardString = buildSmartDashString();
			eShowString = buildShowString();
			eShowStringFormatted = buildShowStringFormatted();
		}
		
		public boolean isAlgAvailable() { return eWhenAvailable.isAvailableForStation(); }
//		public AutonSwitchAlgsE getHwSwitchInfo() { return eHwSwitchInfo; }
		public String getCmdDescription() { return eCmdDescription; }
		public Command getCommand() { return eCmdToRun; }
		public AutonAlgStatusE getStatus() { return eAlgStatus; }
		public String getAlgNbr() { return eNdxString; }
		public String getSmartDashString() { return eSmartDashboardString; }
		public String getShowString() { return eShowString; }
		public String getShowStringFormatted() { return eShowStringFormatted; }
		
		public void show() { System.out.println(eSmartDashboardString); }
		public void show(String prefix) { 
			System.out.println(prefix + eShowString);
		}
		
		private String buildShowStringFormatted() {
			String ans;
			//%-25s says to left-justify a string in a field 25 chars wide
			ans = String.format("%-65s - when %12s - [%s]", eSmartDashboardString, this.eWhenAvailable.name(), this.eCmdToRun.toString());
			return ans;
		}

		private String buildShowString() {
			String ans;
			//%-25s says to left-justify a string in a field 25 chars wide
			ans = String.format("%s - when %s - [%s]", eSmartDashboardString, this.eWhenAvailable.name(), this.eCmdToRun.toString());
			return ans;
		}

		private String buildSmartDashString() {
			String ans;
			ans = String.format("%2d - %s - %s", eEnumEnt.ordinal(), eAlgStatus.getSmartDashDescription(),
											eCmdDescription );
			return ans; 
		}
	}
	
	
	/**
	 * configure the SmartDashboard "chooser" used to select the autonomous
	 * algorithm to run by parsing the array of algorithm definition info.
	 */
	public synchronized void setUpSendable()
	{
		AutonomousAlgE defaultAlg = AutonomousAlgE.ALG_DO_NOTHING;
		
		if(autoChooser == null) {
			autoChooser = new SendableChooser<Command>();
	
			//begin by setting the default selection
	  		autoChooser.addDefault(defaultAlg.getEnt().getSmartDashString(), 
					defaultAlg.getEnt().getCommand());
	  		
	  		//add remaining algorithms from enum
//	  		for(AutonomousAlgE item : AutonomousAlgE.values()) {
//	  			autoChooser.addObject(item.getEnt().getSmartDashString(), item.getEnt().getCommand());
//	  		}
	  		for(AutonAlgEntry item : AutonomousAlgE.staticGetList()) {
	  			autoChooser.addObject(item.getSmartDashString(), item.getCommand());
	  		}
	
	  		//this is used to display additional info in the chooser
	  		//(use <n/a> instead of (n/a) so will show up after all numbered items (ASCII sort order))
			autoChooser.addObject("<n/a>(does nothing)(No hardware switches available)",
					new TmACmdDoNothing() );
		} 

		TmSdMgr.putData(TmSdKeysI.SdKeysE.KEY_AUTON_CHOOSER, autoChooser);
//		SmartDashboard.putData(TmSdKeys.SdKeysE.KEY_AUTON_CHOOSER.getKey(), autoChooser);
	}
	
	public void showAlgInfo() {
		for(AutonAlgEntry item : AutonomousAlgE.staticGetList()) { //for(AutonomousAlgE item : AutonomousAlgE.values() ) {
			System.out.println(item.getShowStringFormatted()); //getSmartDashString());
		}
		getSelectedAlg().show("Selected AUTON ALG: ");
	}
	
	public synchronized AutonAlgEntry getSelectedAlg() {
		AutonAlgEntry ans = AutonomousAlgE.SELECTED_ALG_NOT_FOUND.getEnt();
		boolean found = false;
		
		selectedCmdRaw = (Command) autoChooser.getSelected();
		if(selectedCmdRaw==null) {
			int forcedAlgNbr = 0;
			selectedCmdRaw = AutonomousAlgE.staticGetList().get(forcedAlgNbr).eCmdToRun;
			System.out.println("Auton Alg Chooser FAILED! Forcing use of specific autonomous alg # " + forcedAlgNbr);
		}
		for(AutonAlgEntry item : AutonomousAlgE.staticGetList()) { //for( AutonomousAlgE item : AutonomousAlgE.values()) {
			if( ( ! found) && (item.getCommand().equals(selectedCmdRaw)) ) {
				ans = item;
				found = true;
//				TmSdMgr.putString(SdKeysE.KEY_AUTON_ALG_NBR_STRING, 
//						(getSelectedAlg()==null ? "??" : 
//							getSelectedAlg()
//							.getAlgNbr()));
////				SmartDashboard.putString(TmSdKeys.SdKeysE.KEY_AUTON_ALG_NBR_STRING.getKey(), item.getAlgNbr());
				break;
			}
		}
		if( ! found) {
			String prefix = "";
			if(selectedCmdRaw == null) {
				prefix = "Command from SD is null.";
			} else {
				prefix = "this instance of command '" + selectedCmdRaw.toString() + "'" +
						" not found in AutonomousAlgE enum.";
			}
			System.out.println(prefix + " Using SELECTED_ALG_NOT_FOUND's command instead");
		}
		return ans;
	}


	/*-------------------------------------------------------------
	 *      misc. service routine(s)
	 *-------------------------------------------------------------*/

	/**
	 * if the command selected on SD is running, cancel it
	 */
	private synchronized void cancelCmdIfRunning() {
		if(selectedAlg.getCommand() != null) {
			if(selectedAlg.getCommand().isRunning()) {
				selectedAlg.getCommand().cancel();
			}
		}
	}


	
	@Override
	public void sssDoInstantiate() {
		AutonomousAlgE junk = AutonomousAlgE.ALG_DO_NOTHING; //just access something to make sure enum and list get set up
		selectedAlg = AutonomousAlgE.SELECTED_ALG_NOT_FOUND.getEnt();
	}

	@Override
	public void sssDoRobotInit() {
		setUpSendable(); //wait and make sure we're talking to the dashboard first??
	}

	private static boolean algInfoDisplayed = false;
	@Override
	public void sssDoDisabledInit() {
		cancelCmdIfRunning();
		setUpSendable();
		selectedAlg = getSelectedAlg();
		if( ! selectedAlg.equals(AutonomousAlgE.SELECTED_ALG_NOT_FOUND.getEnt())) {
			selectedAlg.show("DisabledInit - SELECTED ALG: ");
		}
		if( ! algInfoDisplayed) { showAlgInfo(); algInfoDisplayed = true; }
		cancelCmdIfRunning();
	}

	@Override
	public void sssDoAutonomousInit() {
		setUpSendable();
		//showAlgInfo();
		selectedAlg = getSelectedAlg();
		selectedAlg.show("AutonInit - SELECTED ALG: ");
		if(selectedAlg.getCommand() != null) {
			selectedAlg.getCommand().start();
		}
	}

	@Override
	public void sssDoTeleopInit() {
		cancelCmdIfRunning();
		setUpSendable();
		//showAlgInfo();
		selectedAlg = getSelectedAlg(); //done to try to get SD updated
	}

	@Override
	public void sssDoLwTestInit() {
		cancelCmdIfRunning();
	}

	@Override
	public void sssDoRobotPeriodic() {
		TmSdMgr.putString(SdKeysE.KEY_AUTON_ALG_NBR_STRING, 
				(getSelectedAlg()==null ? "??" : getSelectedAlg().getAlgNbr()));
//		SmartDashboard.putString(TmSdKeys.SdKeysE.KEY_AUTON_ALG_NBR_STRING.getKey(), getSelectedAlg().getAlgNbr()); //("" + selectedAlgNbr)); //item.getAlgNbr());
	}

	@Override
	public void sssDoDisabledPeriodic() {
	}

	@Override
	public void sssDoAutonomousPeriodic() {
	}

	@Override
	public void sssDoTeleopPeriodic() {
	}

	@Override
	public void sssDoLwTestPeriodic() {
		// TODO Auto-generated method stub

	}

	@Override
	public void initDefaultCommand() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isFakeableItem() { return false; }

	@Override
	public void configAsFake() {}

	@Override
	public boolean isFake() { return false; }
	
	
	public class Local_DefaultAutonCmdDoNothing extends Command {

		TmSsDriveTrain ssDrive;
		TmDriverStation m_tds;

	    public Local_DefaultAutonCmdDoNothing() {
	    	m_tds = TmDriverStation.getInstance();
	    	ssDrive = TmSsDriveTrain.getInstance();
	    	requires(ssDrive);
	    }

	    // Called just before this Command runs the first time
	    protected void initialize() {
	    	if( m_tds.isEnabledAutonomous() ) {
	    		
	    	}
	    }

	    // Called repeatedly when this Command is scheduled to run
	    protected void execute() {
	    	if( m_tds.isEnabledAutonomous() ) {
	    		TmSsDriveTrain.DrvServices.stopAllMotors();
	    	}
	    }

	    // Make this return true when this Command no longer needs to run execute()
	    protected boolean isFinished() {
	    	boolean ans = false;
	    	if( m_tds.isEnabledAutonomous() ) {
	    		ans = false;
	    	} else {
	    		ans = true;
	    	}
	    	return ans;
	    }

	    // Called once after isFinished returns true
	    protected void end() {
	    	if( m_tds.isEnabledAutonomous() ) {
	    		
	    	}
	    }

	    // Called when another command which requires one or more of the same
	    // subsystems is scheduled to run
	    protected void interrupted() {
	    	if( m_tds.isEnabledAutonomous() ) {
	    		
	    	}
	    }
	}
	
	
}
