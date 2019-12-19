package org.usfirst.frc.tm744yr18.robot.interfaces;

public interface TmStdSubsystemI extends TmItemAvailabilityI {
	
	public void sssDoInstantiate();
	
	public void sssDoRobotInit();
	public void sssDoDisabledInit();
	public void sssDoAutonomousInit();
	public void sssDoTeleopInit();
	public void sssDoLwTestInit(); //for LiveWindow testing
	
	public void sssDoRobotPeriodic();
	public void sssDoDisabledPeriodic();
	public void sssDoAutonomousPeriodic();
	public void sssDoTeleopPeriodic();
	public void sssDoLwTestPeriodic();
	
	/** a method required by the Subsystem class and therefore included here without the sss prefix */
	public void initDefaultCommand();

	//rely on TmItemAvailability methods instead
//	public boolean sssIsFakeableSubsys();
//	public void sssConfigAsFakeSubsys();
}
