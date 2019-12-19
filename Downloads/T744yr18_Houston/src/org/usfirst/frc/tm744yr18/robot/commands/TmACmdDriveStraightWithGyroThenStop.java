/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.usfirst.frc.tm744yr18.robot.commands;

import org.usfirst.frc.tm744yr18.robot.helpers.TmDriverStation;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmToolsI.P;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmToolsI.Tt;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmToolsI.P.PrtYn;
import org.usfirst.frc.tm744yr18.robot.subsystems.TmSsDriveTrain;
import org.usfirst.frc.tm744yr18.robot.subsystems.TmSsDriveTrain.Driving;
import org.usfirst.frc.tm744yr18.robot.subsystems.TmSsDriveTrain.DrvGyro;
import org.usfirst.frc.tm744yr18.robot.subsystems.TmSsDriveTrain.DrvNavX;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.command.Command;
import t744opts.Tm744Opts;

/**
 *
 * @author Alex D.
 * 
 * Command to make the robot drive straight using a p-controller on the x and y error
 * Will only be tuned to high gear.
 * Currently not tuned.
 * 
 * TUNING - see detailed comments further down (crt-F: "Constants to tune!"):
 * pKy: increase if the robot is slowing down too early, and lower if it is overshooting
 * pKx: increase if angle is not returning to 0 / decrease if angle is overshooting (oscillating)
 *      probably should >1, my guess ~10 times lower pKth to accommodate oscillation 
 * pKth: increase if angle is not returning to 0 / decrease if angle is overshooting (oscillating)
 */
public class TmACmdDriveStraightWithGyroThenStop extends Command {
    
	TmSsDriveTrain ssDrive;
	TmSsDriveTrain.DrvEncoderMgmt drvEncMgmt;
	String thisCmdName;
//    private double m_distanceDfltAndRed; //value used to seed yError in constructor
//    private double m_distanceBlue; //value used to seed yError in constructor
    private double m_distance;
    private double yError;
    private double xError;
    private double timeout;
    private Timer fyiTime;
    private final double MIN_MOTOR_SPEED = 0.12;
    private final double MAX_MOTOR_SPEED = 0.6; //was 0.8 in 2017
    private double minThetaErr = 99999;
    private double maxThetaErr = -99999;
    private String inputDescr;
    private String curState;
    FeedbackTypesE m_fbType;
    FeedbackResetsE m_fbReset;
    
    public enum FeedbackTypesE { 
    	ENCODERS_ONLY, ENCODERS_AND_GYRO;
    	private static final FeedbackTypesE DEFAULT = ENCODERS_AND_GYRO;
    	public boolean isUseEncodersAndGyro() { return this.equals(ENCODERS_AND_GYRO); }
    	public boolean isUseEncodersOnly() { return this.equals(ENCODERS_ONLY); }
    }
        
    public enum FeedbackResetsE { 
    	NONE, ENCODERS_ONLY, GYRO_ONLY, ENCODERS_AND_GYRO;
    	private static final FeedbackResetsE DEFAULT = ENCODERS_AND_GYRO;
    	public boolean isResetEncoders() { return (this.equals(ENCODERS_AND_GYRO) || this.equals(ENCODERS_ONLY)); }
    	public boolean isResetGyro() { return (this.equals(ENCODERS_AND_GYRO)  || this.equals(GYRO_ONLY)); }
    }
     //"Constants to tune!" Tuning advice:
    //
    //Sanity Check:
    //Set pKy = pKth = pKx = 0
    //Ensure robot doesn't move and ensure that there is a working timeout
    //
    //Tune pKy:
    //Start with pKth = 0. This will make pKx also irrelevant, but set pKx=0 anyway.
    //This will make the curve input equal 0 of the .drive(double speed, double curve) method
    //	Tune pKy, which controls forward and backward movement.
    //	Send a command which only drives a small distance, say ~ 1 foot.
    //	If moving backwards or forwards indefinitely, switch sign of pKy
    //	If not moving at all, increase size of kPy by a facator of 10. Repeat until it moves.
    
    //Control for pKth and pKx:
    //Test with a command that drives a longer distance, say ~10-15 feet
    //	Observe if there is any deviation in angle or sideways deviations from a straight line (x deviations).
    //	Repeat, but this time attempt to mess up the straightness somehow
    //	(e.g. slip surface under one side of drive at beginning, 
    //		  or something like tape or carpet in the gears)
    // 	Observe any deviations now, record approximate angle and sideways deviations (x) 
    
    //Tune kPth:
    //Set pKth ~= .1*pKy to start. Leave kPy where it was
    //Set pKx=0. This will always attempt to correct angle to 0, not to go towards the drive line.
    //	Test with same longer distance command that drives ~10-15 feet
    //	You will know that the sign of pKth is wrong if the robot veers and/or spins immediately
    //		This is because it is positive feedback, which
    //			increases the positive spin as the angle begins to drift from 0 in the positive direction 
    //			and increases the negative spin as the angle begins to drift from 0 in the negative direction.
    //		We want negative feedback, which
    //			increase the negative spin as the angle begins to drift from 0 in the positive direction (thus pushing it back to 0)
    //			and increases the positive spin as the angle begins to drift in the negative direction (also pushing it back to 0)
    //	With correct sign, observe if there are any deviations from the straight drive line
    //		It should be pretty straight.
    //		Repeat tests that mess up angle (e.g. slick surface or tape in gears). 
    //		Record angle and sideways deviations.
    //		Repeat these tests while increasing the magnitude of kPth 
    //			until the angle quickly returns to 0, with no overshoot or oscillations.
    //			If the kPth began with overshoots and oscillations, reduce it.
    //		Record any sideways (x) error, but do not attempt to eliminate it with this gain.
    
    //Turn on kPx:
    //Set pKx ~= 10*pKth
    //	You will know that the sign of pKx is wrong if the robot veers and/or spins immediately
    //	Repeat the previous tests, and record the deviation sideways from the straight line
    //		Increase pKx if the sideways error is still too high
    //		Decrease pKx if the robot begins overshooting the straight driveline

//---the original sample values
//    private static final double pKy  = 1.6; //Tune this!
//    private static final double pKth = 0.1; //Tune this!
//    private static final double pKx  = 1.0; //Tune this!
    
    private static final double pKyDefault  = 0.325; //0.325; //1.6; //Tune this!
    private static final double pKthDefault = 0.0325; //0.0325; //0.1; //Tune this!
    private static final double pKxDefault  = 3.25*5; //0.325; //1.0; //Tune this!
    private static final double pKxDefaultEncoders  = 3.25*5; //3.25*2.5; //0.325; //1.0; //Tune this!
    private double pKy;
    private double pKth;
    private double pKx;
    
    private static final double DEFAULT_TIMEOUT = 30;
    
    public TmACmdDriveStraightWithGyroThenStop(double distance) {
    	// timeout = (feet to move)/((max 16ft/sec)*(avg speed))
//    	this(distance, Math.abs((distance*1.1)/(16*pKy/2)));
//    	this(distance, Math.abs((distance*120.0)/(TmSsDriveTrain.getMaxFtPerSecLowGear()*pKy/2)));
    	this(distance, DEFAULT_TIMEOUT, FeedbackTypesE.DEFAULT, FeedbackResetsE.DEFAULT);
    }
    public TmACmdDriveStraightWithGyroThenStop(double distance, double timeout) {
    	this(distance, timeout, FeedbackTypesE.DEFAULT, FeedbackResetsE.DEFAULT);
    }
    public TmACmdDriveStraightWithGyroThenStop(double distance, FeedbackTypesE fbType) {
    	this(distance, DEFAULT_TIMEOUT, fbType, FeedbackResetsE.DEFAULT);
    }
    public TmACmdDriveStraightWithGyroThenStop(double distance, double timeout, FeedbackTypesE fbType) {
    	this(distance, timeout, fbType, FeedbackResetsE.DEFAULT);
    }
    public TmACmdDriveStraightWithGyroThenStop(double distance, double timeout, FeedbackTypesE fbType, FeedbackResetsE fbReset) {
    	ssDrive = TmSsDriveTrain.getInstance();
    	//oops! TmSsAutonomous calls a constructor for this cmd before TmSsDriveTrain! we'll get null here!
    	drvEncMgmt = ssDrive.getDriveEncoderMgmtInstance();
    	thisCmdName = Tt.getClassName(this) + "(" + distance + ", " + timeout + ")";
        m_fbType = fbType;
        m_fbReset = fbReset;
    	
        // Use requires() here to declare subsystem dependencies
//        this.m_distanceDfltAndRed = distanceDfltAndRed;
//        this.m_distanceBlue = distanceBlue;
        this.m_distance = distance;
        this.timeout = timeout;
//        yError = distanceDfltAndRed;
        requires(ssDrive);
        
        // (feet to move)/((max 16ft/sec)*(avg speed))
        setTimeout(timeout); //Math.abs(distance)*.5); //Tune this!
        fyiTime = new Timer();
        fyiTime.start();
        fyiTime.reset();
        pKy = pKyDefault;
        pKth = pKthDefault;
        if(m_fbType.isUseEncodersAndGyro()) {
        	pKx = pKxDefault;
        } else {
        	pKx = pKxDefaultEncoders;
        }
//        if(distance<0) {} //{ pKx = -1 * pKxDefault; }
//        if(distance<0) {} //{ pKth = -pKthDefault; }
        if(distance<0) {
        	pKth = -pKthDefault; 
        	pKx = -pKx;
        }
//        P.println("distance=" + distance + ", pKth=" + pKth);
    }

    // Called just before this Command runs the first time
    protected void initialize() {
//        inputDescr = String.format("dist=%1.3f timeout=%1.2f pKy=%1.5f pKth=%1.5f pKx=%1.5f minSpeed=%1.3f max=%1.3f fbType=%s fbReset=%s",
//        		distance, timeout, pKy, pKth, pKx, MIN_MOTOR_SPEED, MAX_MOTOR_SPEED, m_fbType.name(), m_fbReset.name());
//        P.println(PrtYn.Y, thisCmdName + ": INITIALIZING: " + inputDescr);

//    	m_distance = TmDriverStation.getInstance().isRedAlliance() ? m_distanceDfltAndRed : m_distanceBlue;
 
    	//oops! TmSsAutonomous (which uses this cmd) gets set up before TmSsDriveTrain! doublecheck
    	//that we set up good values for these
    	if(ssDrive==null) {
    		ssDrive = TmSsDriveTrain.getInstance();
    	}
    	if(drvEncMgmt==null) {
    		drvEncMgmt = ssDrive.getDriveEncoderMgmtInstance();
    	}

    	//cb_robotDrive.startEncoders();
        if(m_fbReset.isResetEncoders()) { 
        	drvEncMgmt.reset(); 
            prevLfEncoderRdg = 0;
            prevRtEncoderRdg = 0;
        } //cb_robotDrive.resetEncoders(); }
        
//        if(m_fbReset.isResetGyro()) { DrvGyro.resetGyro(); } //cb_robotDrive.resetGyro(); }
        if(m_fbReset.isResetGyro()) { 
        	if(Tm744Opts.isGyroNavX()) {
        		DrvNavX.resetNavXSoft();
        	} else {
        		DrvGyro.resetGyro();
        	}
//          if(m_fbReset.isResetGyro()) { DrvNavX.resetNavXSoft(); } //done//COMMENT THIS IN WHEN NAVX IS READY! AND COMMENT OUT PREVIOUS LINE
        }
        
        yError = m_distance;
        if(m_fbType.isUseEncodersAndGyro()) {
        	xError = 0;
        } else {
        	xError = 0.0; //1.0;
        }
        fyiTime.reset();
        
        theta     = 0;
        prevTheta = 0;
        y         = 0;
        prevY     = 0;
        x         = 0; //1.0; //(m_fbType.isUseEncodersAndGyro()) ? 0.0 : 1.0;
        prevX     = x;

        inputDescr = String.format("%s dist=%1.3f timeout=%1.2f pKy=%1.5f pKth=%1.5f pKx=%1.5f" +
				" yError=%1.2f xError=%1.2f theta=%1.2f prevTheta=%1.2f y=%1.2f prevY=%1.2f x=%1.2f prevX=%1.2f" +
				" minSpeed=%1.3f max=%1.3f fbType=%s fbReset=%s",
				(TmDriverStation.getInstance().isRedAlliance() ? "RED " : "BLUE"),
				m_distance, timeout, pKy, pKth, pKx,
				yError, xError, theta, prevTheta, y, prevY, x, prevX,
				MIN_MOTOR_SPEED, MAX_MOTOR_SPEED, m_fbType.name(), m_fbReset.name());
        P.println(PrtYn.Y, thisCmdName + ": INITIALIZING: " + inputDescr);       
    }

    // Called repeatedly when this Command is scheduled to run
    protected void execute() {
    	//Helpful Print Statments, can be commented/deleted if not needed later
//        P.println(PrtYn.Y, thisCmdName + ": executing: yError=" + yError + " angle=" + ssDrive.getAngle());
        
        //Get readings from encoders and gyro
    	
    	if(m_fbType.isUseEncodersAndGyro()) {
    		double rdg;
//    		double theta;
    		double dx;
    		double dy;
    		rdg = getEncoderRdg();
    		if(Tm744Opts.isGyroNavX()) {
    			theta = Math.toRadians(DrvNavX.getLocalAngle());
    		} else {
    			theta = Math.toRadians(DrvGyro.getAngle());
    		}

    		//Calculate how far the robot has moved in x and y (x is sideways and y is forward/backwards)
    		dx = rdg * Math.sin(theta);
    		dy = rdg * Math.cos(theta);
    		//Accumulate the changes from each time step, to calculate overall error in x and y
    		yError -= dy;
    		xError -= dx;
    	} else {
    		updateNav();
            //Accumulate the changes from each time step, to calculate overall error in x and y
            yError = this.m_distance - y; //goal - current
            xError = x; //removed negative sign... TODO: might need a negative sign...
    	}
        
    	double desiredTheta;
    	if(true) {
    		//the version used in Orlando
    		//If xError is large, desired angle should point more steeply back towards the line we want to travel on
    		desiredTheta = Math.toRadians(xError * pKx); //Math.toRadians() ???
    	}
    	else if(true) {
    		desiredTheta = Math.toRadians(5); //Alex experiment 1
    	} else {
    		desiredTheta = Math.toRadians(10); //Alex experiment 2
    	}

        //thetaError is difference between our current error and desired theta
        double thetaError = desiredTheta - theta;
        if(thetaError > maxThetaErr) { maxThetaErr = thetaError; }
        if(thetaError < minThetaErr) { minThetaErr = thetaError; }
        
        //Helpful print statements, can be commented/deleted when done tuning
        //use encoder readings from last update; don't update here!!!
        double curLfEncoderDist = drvEncMgmt.getLeftDistanceAdjustedForPolarity(); //.getLeftDistance(); //cb_robotDrive.getLeftDistance();
        double curRtEncoderDist = drvEncMgmt.getRightDistanceAdjustedForPolarity(); //.getRightDistance(); //cb_robotDrive.getRightDistance();
        curState = String.format("%s: time: % 1.5f, yError: % 1.4f, xError: % 1.4f, thetaError: % 1.4f " + 
        							"theta: % 1.4f, desiredTheta: % 1.4f, gyro: % 1.4f L-encDist: % 1.4f R-encDist: % 1.4f", 
        				thisCmdName, fyiTime.get(), yError, xError, thetaError,
//        				theta, desiredTheta, ssDrive.getAngle(), curLfEncoderDist, curRtEncoderDist);
						theta, desiredTheta, 
						//note: x=a?b:c; means if(a){x=b;}else{x=c;}
						(Tm744Opts.isGyroNavX() ? DrvNavX.getLocalAngle() : DrvGyro.getAngle()), 
						curLfEncoderDist, curRtEncoderDist);
        P.println(PrtYn.Y, curState);
        //Math showing what curve input equals:
        //curve input = (desiredTheta - theta) * pKth
        //= ((xError * pKx)-theta) * pKth 
        //= 
        
        //using the built in function drive(double speed, double curve)
        //p Controller on speed based on y (forward/backward) error
        //p Controller on curve/angle based on thetaError, based on xError and gyro reading
//        drvSixMotors.drive(clampInput(yError * pKy), 
//        		clampCurve(thetaError * Math.toDegrees(pKth)), 
//        		CenterDriveMotorsBehaviorE.COAST_DURING_SPINS);  
        Driving.driveMagnitudeAndCurve(clampInput(yError * pKy), clampCurve(thetaError * Math.toDegrees(pKth)));
    }
    
    public double clampInput(double input) {
        if(input > MAX_MOTOR_SPEED) return MAX_MOTOR_SPEED;
        else if(input < -MAX_MOTOR_SPEED) return -MAX_MOTOR_SPEED;
        else if(input>0 && input < MIN_MOTOR_SPEED) return MIN_MOTOR_SPEED;
        else if(input<0 && input > -MIN_MOTOR_SPEED) return -MIN_MOTOR_SPEED;
        else return input;
    }
    public double clampCurve(double input){
    	if(input > 0.8) return 0.8;
    	else if (input < -0.8)return -0.8;
    	else return input;
    }
    // Make this return true when this Command no longer needs to run execute()
    protected boolean isFinished() {
    	boolean ans;
    	boolean distanceOk = isInRange(yError); //&& isInRange(xError);
        ans = (distanceOk || isTimedOut() || ! DriverStation.getInstance().isAutonomous());
        if(ans) {
        	P.println(thisCmdName + "FINISHED: " + (distanceOk ? "at destination" : (isTimedOut()? "timedOut" : "autonEnded")) + " time: " + fyiTime.get());
            P.println(PrtYn.Y, thisCmdName + " inputDescr: " + inputDescr);
            P.println(PrtYn.Y, thisCmdName + " final state: " + curState);
            P.println(PrtYn.Y, thisCmdName + "              minThetaErr=" + minThetaErr + " maxThetaErr=" + maxThetaErr);
        }
        return ans;
    }

    //Ends when x and y error are both less than .5 inches
    //make sure to have a time out though!!! 
    //If y gets too small while x is correcting, 
    //the robot will slow down and not be able to turn back toward y
    public boolean isInRange(double error) {
    	boolean ans;
    	//be careful to use double constants here (using 1/12 did integer arithmetic
    	//and set the range to 0.0)
        ans = (Math.abs(error) < (0.5/12.0)); //(.5 / 12));
        return ans;
    }
    // Called once after isFinished returns true
    protected void end() {
    	boolean distanceOk = isInRange(yError); //&& isInRange(xError);
        P.println(thisCmdName + ": ENDING: " + (distanceOk ? "at destination" : (isTimedOut()? "timedOut" : "autonEnded")) + " time: " + fyiTime.get());
        Driving.driveMagnitudeAndCurve(0.0, 0.0);
        Driving.tankDriveRawPercentOutput(0.0, 0.0);
        P.println(PrtYn.Y, thisCmdName + " inputDescr: " + inputDescr);
        P.println(PrtYn.Y, thisCmdName + " final state: " + curState);
        P.println(PrtYn.Y, thisCmdName + "              minThetaErr=" + minThetaErr + " maxThetaErr=" + maxThetaErr);
    }

    // Called when another command which requires one or more of the same
    // subsystems is scheduled to run
    protected void interrupted() {
    	P.println(PrtYn.Y, thisCmdName + ": INTERRUPTED" + " time: " + fyiTime.get());
        Driving.driveMagnitudeAndCurve(0.0, 0.0);
        Driving.tankDriveRawPercentOutput(0.0, 0.0);
        P.println(PrtYn.Y, thisCmdName + " inputDescr: " + inputDescr);
        P.println(PrtYn.Y, thisCmdName + " final state: " + curState);
        P.println(PrtYn.Y, thisCmdName + "              minThetaErr=" + minThetaErr + " maxThetaErr=" + maxThetaErr);
    }
    
    
    //Initializes variables used to track previous encoder distance
    private double prevLfEncoderRdg = 0;
    private double prevRtEncoderRdg = 0;
    //Calculates average movement since last time step
    public double getEncoderRdg() {
    	//Get left and right total distance so far
    	drvEncMgmt.update();
        double curLfEncoderRdg = drvEncMgmt.getLeftDistanceAdjustedForPolarity(); //.getLeftDistance(); //cb_robotDrive.getLeftDistance();
        double curRtEncoderRdg = drvEncMgmt.getRightDistanceAdjustedForPolarity(); //.getRightDistance(); //cb_robotDrive.getRightDistance();
        
        //Subtract the total distance from last time to get change since last time
        //Add and divide by 2 to get average
        double rdg = .5 * ((curLfEncoderRdg - prevLfEncoderRdg) + (curRtEncoderRdg - prevRtEncoderRdg));
        
        //Log previous readings
        prevLfEncoderRdg = curLfEncoderRdg;
        prevRtEncoderRdg = curRtEncoderRdg;
        
        //Potentially helpful prints
//        P.println(PrtYn.Y, thisCmdName + ": Average Encoder Rdg: " + rdg);
        
        return rdg;
    }
    
    private double theta     = 0;
    private double prevTheta = 0;
    private double y         = 0;
    private double prevY     = 0;
    private double x         = 0; //1.0; //(m_fbType.isUseEncodersAndGyro()) ? 0.0 : 1.0;
    private double prevX     = x;
    private final double ROBOT_WIDTH = 2.333; //feet
    public void updateNav() {
    	//Get left and right total distance so far
    	drvEncMgmt.update();
        double curLfEncoderRdg = drvEncMgmt.getLeftDistanceAdjustedForPolarity(); //.getLeftDistance(); //cb_robotDrive.getLeftDistance();
        double curRtEncoderRdg = drvEncMgmt.getRightDistanceAdjustedForPolarity(); //.getRightDistance(); //cb_robotDrive.getRightDistance();
        
        double dL = curLfEncoderRdg - prevLfEncoderRdg;
        double dR = curRtEncoderRdg - prevRtEncoderRdg;
        
        double rotTh = prevTheta;
        
        double ALs; //Arc Length small
        double ALb; //Arc Length big
        double xSign;
        
        //no change in angle
        if(dL == dR) {
        	// Changes to x and y in robot frame
        	double dy_robotFrame = dL;
        	double dx_robotFrame = 0;
        	
        	// Implementing a rotation matrix multiplication to convert to the "start" frame
        	double dx_startFrame = Math.cos(rotTh)*dx_robotFrame - Math.sin(rotTh)*dy_robotFrame;
        	double dy_startFrame = Math.sin(rotTh)*dx_robotFrame + Math.cos(rotTh)*dy_robotFrame;
        	
        	// perform update of x, y, th
        	y = prevY + dy_startFrame;
        	x = prevX + dx_startFrame;
        	theta = prevTheta;
        	
        	//store x, y, th for next round
        	prevTheta = theta;
        	prevY = y;
        	prevX = x;
        	prevLfEncoderRdg = curLfEncoderRdg;
        	prevRtEncoderRdg = curRtEncoderRdg;
        	return;
        }
        else if(Math.abs(dL) < Math.abs(dR)) {
        	ALs = dL;
        	ALb = dR;
        	xSign = -1;
        }
        else { //dR < dL
        	ALs = dR;
        	ALb = dL;
        	xSign = 1;
        }
        
        // calculate the circle the robot is spinning about
        double th_i    = (ALb-ALs)/ROBOT_WIDTH; //Angle change at this timestep in radians
        double radius  = ROBOT_WIDTH/2 + ALs/th_i; //Distance from center of "turning circle" to middle of robot
        
        // Calculate dy and dx in the robots reference frame
        double dy_robotFrame = radius*Math.sin(th_i);
        double dx_robotFrame = xSign*radius*(1-Math.cos(th_i));
        
        // Implementing a rotation matrix multiplication to convert to the "start" frame
    	double dx_startFrame = Math.cos(rotTh)*dx_robotFrame - Math.sin(rotTh)*dy_robotFrame;
    	double dy_startFrame = Math.sin(rotTh)*dx_robotFrame + Math.cos(rotTh)*dy_robotFrame;
    	
    	//update x, y, th 
    	y     = prevY + dy_startFrame;
    	x     = prevX + dx_startFrame;
    	theta = prevTheta + xSign*th_i;
    	
    	//store prev x, y, theta for next iteration
    	prevTheta = theta;
    	prevY = y;
    	prevX = x;
    	prevLfEncoderRdg = curLfEncoderRdg;
    	prevRtEncoderRdg = curRtEncoderRdg;
    	return;
        
    }
    
}
