package org.usfirst.frc.tm744yr18.robot.commands;

import org.usfirst.frc.tm744yr18.robot.config.AutoSpeed;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmToolsI.P;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmToolsI.P.PrtYn;
import org.usfirst.frc.tm744yr18.robot.subsystems.TmSsDriveTrain;
import org.usfirst.frc.tm744yr18.robot.subsystems.TmSsDriveTrain.DriveSpinDirectionE;
import org.usfirst.frc.tm744yr18.robot.subsystems.TmSsDriveTrain.Driving;
import org.usfirst.frc.tm744yr18.robot.subsystems.TmSsDriveTrain.DrvGyro;
import org.usfirst.frc.tm744yr18.robot.subsystems.TmSsDriveTrain.DrvNavX;
import org.usfirst.frc.tm744yr18.robot.subsystems.TmSsDriveTrain.DrvServices;

import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.command.Command;
import t744opts.Tm744Opts;

/**
 *
 */
public class TmACmdDriveSpinGyro extends Command {
	
	DriverStation m_ds = DriverStation.getInstance();
    TmSsDriveTrain ssDrive;
//    TmRobotDriveSixMotors chassis;
    private double targetAngle;
    
    private static final boolean DEFAULT_RESET_PARM = true;
    private boolean reset = DEFAULT_RESET_PARM;
    
    //Tuneables!!
    private double threshold = 1; //degree
    private static double maxMotorSpeed = 0.45; //0.4
    private static double minMotorSpeed = 0.12; //0.1;
	private static final double pK = AutoSpeed.sp; //0.06 //was 0.03; //0.06; //1.8;
    
    
    public TmACmdDriveSpinGyro(DriveSpinDirectionE dir, double angle) {
    	this(dir, angle, DEFAULT_RESET_PARM);
//        // Use requires() here to declare subsystem dependencies
//        // eg. requires(chassis);
//    	ssDrive = TmSsDriveTrain.getInstance();
//    	requires(ssDrive);
////    	targetAngle = cb_robotDrive.getSign(dir) * angle;
////    	targetAngle = TmSsRobotDrive.DriveSpinDirectionE.getSign(dir) * angle;
//    	targetAngle = dir.getSign() * angle;
//    	setTimeout(7);
////    	setTimeout(angle*10/360);
    }
    
    public TmACmdDriveSpinGyro(DriveSpinDirectionE dir, double angle, boolean reset) {
        // Use requires() here to declare subsystem dependencies
        // eg. requires(chassis);
    	ssDrive = TmSsDriveTrain.getInstance();
    	requires(ssDrive);
    	targetAngle = dir.getSign() * angle;
    	this.reset = reset;
    	setTimeout(7);
//    	setTimeout(angle*10/360);
    }

    // Called just before this Command runs the first time
    protected void initialize() {
    	if(reset){
    		if(Tm744Opts.isGyroNavX()) {
    			DrvNavX.resetNavXSoft(); //done //COMMENT THIS IN WHEN NAVX IS READY! AND COMMENT OUT PREVIOUS LINE
    		} else {
    			DrvGyro.resetGyro(); //ssDrive.resetGyro();
    		}
    	} else {
    		
    	}
    }

    // Called repeatedly when this Command is scheduled to run
    protected void execute() {
    	double error;
    	if(Tm744Opts.isGyroNavX()) {
    		error = targetAngle - DrvNavX.getLocalAngle(); //done //COMMENT THIS IN WHEN NAVX IS READY! AND COMMENT OUT PREVIOUS LINE
    	} else {
    		error = targetAngle - DrvGyro.getAngle(); //ssDrive.getAngle();
    	}
    	
    	//we want one side of the robot moving one way, and the other side moving the other way
    	//the sign of 'error' automatically takes care of the direction of the spin
//    	ssDrive.getChassisInstance().setRobotLeftRightSpeeds(clamp(pK*error), -clamp(pK*error), CenterDriveMotorsBehaviorE.COAST_DURING_SPINS);
    	Driving.tankDriveRawPercentOutput(clamp(pK*error), -clamp(pK*error));
    }

    // Make this return true when this Command no longer needs to run execute()
    protected boolean isFinished() {
    	boolean ans;
    	if(Tm744Opts.isGyroNavX()) {
    		P.println(PrtYn.Y, "targAng=" + targetAngle + ", threshold=" + threshold + ", angle=" + DrvNavX.getLocalAngle());    	
    		ans = (targetAngle - threshold <= DrvNavX.getLocalAngle() && 
    				DrvNavX.getLocalAngle() < targetAngle + threshold) ||
    				isTimedOut();
    		//done//COMMENT THIS IN WHEN NAVX IS READY! AND COMMENT OUT PREVIOUS LINE (or set of lines)
    	} else {
    		P.println(PrtYn.Y, "targAng=" + targetAngle + ", threshold=" + threshold + ", angle=" + DrvGyro.getAngle());
    		ans = (targetAngle - threshold <= DrvGyro.getAngle() && 
    				DrvGyro.getAngle() < targetAngle + threshold) ||
    				isTimedOut();
    	}
    	
    	if(ans) {
    		P.println("FINISHED: " + (isTimedOut() ? "timed out" : "reached destination"));
    	}
    	return ans;
    }

    // Called once after isFinished returns true
    protected void end() {
    	DrvServices.stopAllMotors(); //ssDrive.stopMotors();
    }

    // Called when another command which requires one or more of the same
    // subsystems is scheduled to run
    protected void interrupted() {
    	DrvServices.stopAllMotors(); //ssDrive.stopMotors();
    }
    
    private double clamp(double value) {
        if(value > maxMotorSpeed) return maxMotorSpeed;
        else if(minMotorSpeed > value && value > 0.0) return minMotorSpeed;
        else if(0.0 > value && value > -minMotorSpeed) return -minMotorSpeed;
        else if(value < -maxMotorSpeed) return -maxMotorSpeed;
        else return value;
     }
}
