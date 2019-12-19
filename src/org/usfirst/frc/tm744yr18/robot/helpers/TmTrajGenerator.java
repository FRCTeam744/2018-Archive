package org.usfirst.frc.tm744yr18.robot.helpers;

import jaci.pathfinder.Pathfinder;
import jaci.pathfinder.Trajectory;
import jaci.pathfinder.Waypoint;
import jaci.pathfinder.modifiers.TankModifier;


public class TmTrajGenerator {

	private TmTrajGenerator() {}


	//Trajectory Config Parms
	private final double TIME_STEP = 0.05; // Seconds
	private final double MAX_VEL = 4;    // ft/s
	private final double MAX_ACC = 10.0;    // ft/s/s
	private final double MAX_JERK = 60.0;  // ft/s/s/s
	private Trajectory.Config config = new Trajectory.Config(Trajectory.FitMethod.HERMITE_CUBIC, Trajectory.Config.SAMPLES_HIGH, 
											TIME_STEP, MAX_VEL, MAX_ACC, MAX_JERK);
	private final double WHEELBASE_IN = 27; //in
	private final double WHEELBASE_FT  = WHEELBASE_IN/12;
    
    private Trajectory m_left;
    private Trajectory m_right;
    private double[] m_left_time;
    private double[] m_right_time;
    
	
	public TmTrajGenerator(Waypoint[] points) {
		
		Trajectory trajectory = Pathfinder.generate(points, config);
		TankModifier modifier = new TankModifier(trajectory).modify(WHEELBASE_FT);
		
		m_left = modifier.getLeftTrajectory();
        m_right = modifier.getRightTrajectory();

        //Create time arrays
        m_left_time = new double[m_left.length()];
        m_right_time = new double[m_right.length()];

        m_left_time[0] = m_left.get(0).dt;
        for(int i = 0; i < m_left.length();i++) {
        	m_left_time[i] = m_left_time[i-1]+m_left.get(i).dt;
        }
        
        m_right_time[0] = m_right.get(0).dt;
        for(int i = 0; i < m_left.length();i++) {
        	m_right_time[i] = m_right_time[i-1]+m_right.get(i).dt;
        }
        
	}
	
	public Trajectory getLeftTraj() {
		return m_left;
	}
	
	public Trajectory getRightTraj() {
		return m_right;
	}
	
	public double getLeftTime(int index) {
		return m_left_time[index];
	}

	public double getRightTime(int index) {
		return m_right_time[index];
	}

	
}
