package org.usfirst.frc.tm744yr18.robot.interfaces;

public interface TmForcedInstantiateI {
	
	/**
	 * enums, even when static, don't seem to get initialized until
	 * the first time someone uses them.  There may be other things
	 * that have similar quirks.  Use this interface to provide a 
	 * standard method to call to give a class an opportunity to 
	 * force the initialization of such items.  Classes that implement
	 * it should be registered with the TmForcedInitMgr in T744Robot2018
	 */
	public void doForcedInstantiation();
	
	/**
	 * controls and devices on the robot and driver station need to be populated
	 * before they're really usable.  Need to make sure all instantiation of
	 * enums, etc. is done for all the underlying classes first.  Using
	 * doPopulate allows an easy way to ensure that it all gets done properly.
	 */
	public void doPopulate();
}
