package org.usfirst.frc.tm744yr18.robot.helpers;

import java.util.ArrayList;
import java.util.List;

import org.usfirst.frc.tm744yr18.robot.interfaces.TmForcedInstantiateI;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmToolsI.P;

public class TmForcedInitMgr implements TmForcedInstantiateI {
	
	public class FiEntry {
		TmForcedInstantiateI fiObject;
		String fiTrueClassName;
		boolean fiComplete = false;
		boolean fiPopulated = false;
//		KssAvailabilityE subsysAvailability;
		
		public FiEntry(TmForcedInstantiateI fiObj) {
			fiObject = fiObj;
			fiTrueClassName = fiObj.getClass().getSimpleName();
			fiComplete = false;
			fiPopulated = false;
		}
	}
	
	
	/*---------------------------------------------------------
	 * getInstance stuff                                      
	 *---------------------------------------------------------*/
	/** 
	 * handle making the singleton instance of this class and giving
	 * others access to it
	 */
	private static TmForcedInitMgr m_instance;

	public static synchronized TmForcedInitMgr getInstance() {
		if (m_instance == null) {
			m_instance = new TmForcedInitMgr();
		}
		return m_instance;
	}

	private TmForcedInitMgr() {
		if ( ! (m_instance == null)) {
			P.println("Error!!! TmForcedInitMgr.m_instance is being modified!!");
			P.println("         was: " + m_instance.toString());
			P.println("         now: " + this.toString());
		}
		m_instance = this;
	}
	/*----------------end of getInstance stuff----------------*/


	static List<FiEntry> fiList = new ArrayList<FiEntry>();
	static int fiNextNdx = 0;
	
	private void log(String msg) { System.out.println(msg); }
	
	public synchronized void addFiObject(TmForcedInstantiateI fiObj) {
		int ndx = fiNextNdx++;
		FiEntry entry = new FiEntry(fiObj);
		
		fiList.add(ndx, entry);
	}
	
	@Override
	public void doForcedInstantiation() {
		for(FiEntry e : fiList) {
			if( ! e.fiComplete) { 
				e.fiObject.doForcedInstantiation();
				e.fiComplete = true;
			}
		}
	}
	
	@Override
	public void doPopulate() {
		for(FiEntry e : fiList) {
			if( ! e.fiPopulated) { 
				e.fiObject.doPopulate();
				e.fiPopulated = true;
			}
		}
		
	}

}
