package org.usfirst.frc.tm744yr18.robot.helpers;

import java.util.ArrayList;
import java.util.List;

import org.usfirst.frc.tm744yr18.robot.interfaces.TmItemAvailabilityI;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmStdSubsystemI;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmToolsI.P;

public class TmKssMgr implements TmStdSubsystemI, TmItemAvailabilityI {

	/**
	 * if USE_FAKE requested for subsystem that indicates it is not fakeable, subsystem
	 * will be DISABLED (its methods won't be called), else the subsystem's configAsFakeSubsys()
	 * method will be called.
	 * @author JudiA
	 *
	 */
	public class KssEntry {
		TmStdSubsystemI subsystem;
		String subsysClassName;
		ItemAvailabilityE subsysAvailability;

		public KssEntry(TmStdSubsystemI subsys, ItemAvailabilityE availability) {
			subsystem = subsys;
			subsysClassName = subsys.getClass().getSimpleName();
			subsysAvailability = availability;
		}

		public boolean isRunable() {
			return subsysAvailability.isRunable();
		}
	}

	/*---------------------------------------------------------
	 * getInstance stuff
	 *---------------------------------------------------------*/
	/**
	 * handle making the singleton instance of this class and giving
	 * others access to it
	 */
	private static TmKssMgr m_instance;

	public static synchronized TmKssMgr getInstance() {
		if (m_instance == null) {
			m_instance = new TmKssMgr();
		}
		return m_instance;
	}

	private TmKssMgr() {
		if ( ! (m_instance == null)) {
			P.println("Error!!! TmKss.m_instance is being modified!!");
			P.println("         was: " + m_instance.toString());
			P.println("         now: " + this.toString());
		}
		m_instance = this;
	}
	/*----------------end of getInstance stuff----------------*/


	static List<KssEntry> kssList = new ArrayList<KssEntry>();
	static int kssNextNdx = 0;

	private void log(String msg) { System.out.println(msg); }

	public synchronized void addKnownSubsystem(TmStdSubsystemI subsystem, ItemAvailabilityE presence) {
		int ndx = kssNextNdx++;
		KssEntry entry = new KssEntry(subsystem, presence);
		if(presence.equals(ItemAvailabilityE.USE_FAKE)) {
			if(subsystem.isFakeableItem()) {
				log(entry.subsysClassName + " being config'd as a fake subsystem.");
				subsystem.configAsFake();
			} else {
				log(presence.name() + " requested for " + entry.subsysClassName +
						", but it's not fakeable.  Changed to " + ItemAvailabilityE.DISABLED.name());
				entry.subsysAvailability = ItemAvailabilityE.DISABLED;
			}
		}

		kssList.add(ndx, entry);
	}

	@Override
	public void sssDoInstantiate() {
		for(KssEntry kss : kssList) {
			if(kss.isRunable()) {
				kss.subsystem.sssDoInstantiate();
				log(kss.subsysClassName + " -- sssDoInstantiate() called");
			}
		}
	}

	@Override
	public void sssDoRobotInit() {
		for(KssEntry kss : kssList) {
			if(kss.isRunable()) {
				kss.subsystem.sssDoRobotInit();
				log(kss.subsysClassName + " -- sssDoRoboInit() called");
			}
		}
	}

	@Override
	public void sssDoDisabledInit() {
		log("TmKssMgr calling sssDoDisabledInit() methods");
		for(KssEntry kss : kssList) {
			if(kss.isRunable()) {
				kss.subsystem.sssDoDisabledInit();
//				log(kss.subsysClassName + " -- sssDoDisabledInit() called");
			}
		}
	}

	@Override
	public void sssDoAutonomousInit() {
		log("TmKssMgr calling sssDoAutonomousInit() methods");
		for(KssEntry kss : kssList) {
			if(kss.isRunable()) {
				kss.subsystem.sssDoAutonomousInit();
//				log(kss.subsysClassName + " -- sssDoAutonomousInit() called");
			}
		}
	}

	@Override
	public void sssDoTeleopInit() {
		log("TmKssMgr calling sssDoTeleopInit() methods");
		for(KssEntry kss : kssList) {
			if(kss.isRunable()) {
				kss.subsystem.sssDoTeleopInit();
//				log(kss.subsysClassName + " -- sssDoTeleopInit() called");
			}
		}
	}

	@Override
	public void sssDoLwTestInit() {
		log("TmKssMgr calling sssDoLwTestInit() methods");
		for(KssEntry kss : kssList) {
			if(kss.isRunable()) {
				kss.subsystem.sssDoLwTestInit();
//				log(kss.subsysClassName + " -- sssDoLwTestInit() called");
			}
		}
	}

	@Override
	public void sssDoRobotPeriodic() {
		for(KssEntry kss : kssList) {
			if(kss.isRunable()) {
				kss.subsystem.sssDoRobotPeriodic();
//				log(kss.subsysClassName + " -- sssDoRobotPeriodic() called");
			}
		}
	}

	@Override
	public void sssDoDisabledPeriodic() {
		for(KssEntry kss : kssList) {
			if(kss.isRunable()) {
				kss.subsystem.sssDoDisabledPeriodic();
//				log(kss.subsysClassName + " -- sssDoDisabledPeriodic() called");
			}
		}
	}

	@Override
	public void sssDoAutonomousPeriodic() {
		for(KssEntry kss : kssList) {
			if(kss.isRunable()) {
				kss.subsystem.sssDoAutonomousPeriodic();
//				log(kss.subsysClassName + " -- sssDoAutonomousPeriodic() called");
			}
		}
	}

	@Override
	public void sssDoTeleopPeriodic() {
		for(KssEntry kss : kssList) {
			if(kss.isRunable()) {
				kss.subsystem.sssDoTeleopPeriodic();
//				log(kss.subsysClassName + " -- sssDoTeleopPeriodic() called");
			}
		}
	}

	@Override
	public void sssDoLwTestPeriodic() {
		for(KssEntry kss : kssList) {
			if(kss.isRunable()) {
				kss.subsystem.sssDoLwTestPeriodic();
//				log(kss.subsysClassName + " -- sssDoLwTestPeriodic() called");
			}
		}
	}

	@Override
	public void initDefaultCommand() {
		log("TmKssMgr calling initDefaultCommand() methods");
		for(KssEntry kss : kssList) {
			if(kss.isRunable()) {
				kss.subsystem.initDefaultCommand();
//				log(kss.subsysClassName + " -- initDefaultCommand() called");
			}
		}
	}

	@Override
	public boolean isFakeableItem() { return false; }
	@Override
	public void configAsFake() {}
	@Override
	public boolean isFake() { return false; }

}
