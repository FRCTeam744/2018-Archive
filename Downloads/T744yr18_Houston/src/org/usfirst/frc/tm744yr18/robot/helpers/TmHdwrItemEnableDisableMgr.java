package org.usfirst.frc.tm744yr18.robot.helpers;

public class TmHdwrItemEnableDisableMgr {

	/*
	 * This class is for use with things like joysticks that may or may not be present
	 * at run time.  We use it to minimize the number of messages that appear on the
	 * console.
	 */

	private final Object enableDisableLock = new Object();
	private boolean isEnabled = true;
	private int msgCntInitializer = 10;
	private int msgCnt = msgCntInitializer;

	/**
	 * until msgCnt goes to 0, we ignore the disabled state. This allows the code to
	 * continue to access a missing joystick or other device several times before 
	 * we report 'disabled' and bypass the attempted access
	 * @return
	 */
	public boolean isEnabled() { return (isEnabled || (msgCnt>0)); }

	/**
	 * provides a way for code to re-enable checking for the joystick or whatever
	 */
	public void enable() { 
		synchronized(enableDisableLock) {
			isEnabled = true;
			msgCnt = msgCntInitializer;
		} 
	}

	public void disableAfterMsgs() { 
		if(false) { //do nothing. was causing "dead controller" problem
			synchronized(enableDisableLock) {
				if(isEnabled) {
					msgCnt = msgCntInitializer;
				} else {
					msgCnt--;
				}
				isEnabled = false;
			}
		}
	}

	public TmHdwrItemEnableDisableMgr() {

	}

}
