package org.usfirst.frc.tm744yr18.robot.interfaces;

/**
 * 
 * Requires the methods called from loop() in IterativeRobotBase class with "stdfrc_" prefixed
 * to them.  
 * Intended as an editing aid rather than a runtime aid.  
 * Make the main class (e.g. T744Robot2018) implement this interface, let eclipse add the 
 * "missing" methods, edit the method names to remove the "stdfrc_" prefix, then remove this 
 * interface from the class's "implements" list.
 * Only T744Robot2018 needs to call this, other classes should use TmKss instead.
 * Using the "stdfrc_" prefix ensures that the @Override annotation will be provided even
 * if the class has already implemented one or more of the methods required by loop()
 * @author JudiA
 *
 */
public interface TmStdFrcRobotMethodsI {
	// see the header info for this class for details
	public void stdfrc_robotInit();
	public void stdfrc_disabledInit();
	public void stdfrc_autonomousInit();
	public void stdfrc_teleopInit();
	
	public void stdfrc_disabledPeriodic();
	public void stdfrc_autonomousPeriodic();
	public void stdfrc_teleopPeriodic();
	
	public void stdfrc_testInit(); //for LiveWindow testing
	public void stdfrc_testPeriodic();
	
	public void stdfrc_robotPeriodic();
}
