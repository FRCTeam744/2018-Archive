package org.usfirst.frc.tm744yr18.robot.interfaces;

import java.util.List;

public interface TmEnumWithListI<LE> {
	
	/*
	 * this interface flags enums that use external lists to hold and manage
	 * related data.  The enum ordinal value is the index into the list.
	 * 
	 * Note that this is a "generic" interface.  The <LE> after the interface
	 * name indicates that LE will be used as a placeholder for whatever 
	 * class is actually being used for entries in the list.
	 * 
	 */
	
	/**
	 * get the list used to hold and manage the data related to this enum.
	 * The enum ordinals are the indexes into the list.
	 *  <E> is the type of the class used for the list entries
	 * @return
	 */
	public List<LE> getList();
	/**
	 * get the entry in the list that corresponds to this enum
	 * @return
	 */
	public LE getEnt();
	public String getEnumClassName(); // {return this.getClass.getSimpleName(); }
	public String getListEntryClassName(); // {return LE.class.getSimpleName(); }
	
	//should also have the following, but can't use static in an interface
	//unless actually providing a body for the method.
//	public static List<LE> staticGetList();
	
}
