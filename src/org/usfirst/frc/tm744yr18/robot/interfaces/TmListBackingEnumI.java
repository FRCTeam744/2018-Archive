package org.usfirst.frc.tm744yr18.robot.interfaces;

import java.util.List;

import org.usfirst.frc.tm744yr18.robot.helpers.TmHdwrItemEnableDisableMgr;

public interface TmListBackingEnumI<T, E extends Enum<E>> {
	
	/**
	 * get the full list that holds the info and methods for the enmum entries
	 * so that it can be inspected, iterated, etc.  Handy for initialization 
	 * code that needs to populate things in each entry
	 * @return
	 */
	public List<T> getListBackingEnum();
	
	public TmHdwrItemEnableDisableMgr getHdwrItemEnableDisableMgr(); // = new TmHdwrItemEnableDisableMgr();
	
//	/* an example implementation:
//	public Object cEnableDisableLock;
//	private boolean cIsEnabled = true;
//	public void disable() { synchronized(enableDisableLock) { cIsEnabled = false; } }; //call this if get exceptions (joystick not plugged in, etc.)
//	public void enable() { synchronized(enableDisableLock) { cIsEnabled = true; } }; //call from a command or something to re-enable a disabled entity
//	public boolean isEnabled() { synchronized(enableDisableLock) { return cIsEnabled; } }
//
//	//and in the constructor:
//	enableDisableLock = new Object();
//	cIsEnabled = true;
//    *
//    */
//
//	/**
//	 * a synchronization lock to help keep pieces of code from
//	 * stepping on each other's toes
//	 */
//	public Object cEnableDisableLock = null; // = new Object();
//	
//	/**
//	 * call this method if a device or control gets an exception (e.g.
//	 * a joystick that's not plugged in, or a motor controller that's 
//	 * not installed)  It helps cut down on the complaint messages that
//	 * FRC code generates in such circumstances.
//	 */
//	public void disable();
//	
//	/**
//	 * call this method to re-enable a device, e.g. to see if a joystick has been plugged
//	 * back in or some such thing.
//	 */
//	public void enable();
//	
//	/**
//	 * provided so that the relevant code can check if the device is enabled before
//	 * trying to talk to it.
//	 * @return
//	 */
//	public boolean isEnabled();
	
}
