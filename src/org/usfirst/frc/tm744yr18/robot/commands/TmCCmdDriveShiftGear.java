package org.usfirst.frc.tm744yr18.robot.commands;


import org.usfirst.frc.tm744yr18.robot.interfaces.TmToolsI.P;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmToolsI.P.PrtYn;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmToolsI.Tt;
import org.usfirst.frc.tm744yr18.robot.subsystems.TmSsDrvGearShift;
import org.usfirst.frc.tm744yr18.robot.subsystems.TmSsDrvGearShiftDblSol;

import edu.wpi.first.wpilibj.command.Command;

/**
 *
 */
public class TmCCmdDriveShiftGear extends Command {

	private TmSsDrvGearShift ssGearShift;
	private TmSsDrvGearShift.DrvGearsE m_requestedGear;
	private TmSsDrvGearShiftDblSol.DrvGearsE m_requestedGearDblSol;
	
    public TmCCmdDriveShiftGear(TmSsDrvGearShift.DrvGearsE requestedGear) {
    	ssGearShift = TmSsDrvGearShift.getInstance();
        requires(ssGearShift);
        m_requestedGear = requestedGear;
        m_requestedGearDblSol = null;
    }
    public TmCCmdDriveShiftGear(TmSsDrvGearShiftDblSol.DrvGearsE requestedGear) {
    	ssGearShift = TmSsDrvGearShift.getInstance();
        requires(ssGearShift);
        m_requestedGear = null;
        m_requestedGearDblSol = requestedGear;
    }

    // Called just before this Command runs the first time
    protected void initialize() {
    	if(m_requestedGear != null) {
    		P.println(PrtYn.Y, Tt.getClassName(this) + " -- " + m_requestedGear.name() +
    			" -- cmd initializing");
    	} else {
    		P.println(PrtYn.Y, Tt.getClassName(this) + " -- " + m_requestedGearDblSol.name() +
        			" -- cmd initializing");    		
    	}
    }

    // Called repeatedly when this Command is scheduled to run
    protected void execute() {
    	if(m_requestedGear != null) {
    		TmSsDrvGearShift.updateDrvShifter(m_requestedGear);
    	} else {
    		TmSsDrvGearShiftDblSol.updateDrvShifter(m_requestedGearDblSol);
    	}
    }

    // Make this return true when this Command no longer needs to run execute()
    protected boolean isFinished() {
        return true;
    }

    // Called once after isFinished returns true
    protected void end() {
    }

    // Called when another command which requires one or more of the same
    // subsystems is scheduled to run
    protected void interrupted() {
    }
}
