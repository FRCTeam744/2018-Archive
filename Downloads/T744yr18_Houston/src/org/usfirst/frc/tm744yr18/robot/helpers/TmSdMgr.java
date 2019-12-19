package org.usfirst.frc.tm744yr18.robot.helpers;

import java.nio.ByteBuffer;

import org.usfirst.frc.tm744yr18.robot.config.TmSdKeysI.SdKeysE;

import edu.wpi.first.wpilibj.Sendable;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class TmSdMgr extends SmartDashboard { //implements TmToolsI, TmSdKeys {

	public TmSdMgr() {
		// TODO Auto-generated constructor stub
	}

	private static boolean isKeyInfoUsable(SdKeysE keyInfo) {
		boolean ans = false;
		if( ! (keyInfo==null)) {
			ans = keyInfo.isEnabled();
		}
		return ans;
	}


	/**
	 * Put a boolean in the table.
	 * @param keyInfo contains the key to be assigned to
	 * @param value the value that will be assigned
	 * @return False if the table key already exists with a different type
	 */
	public static boolean putBoolean(SdKeysE keyInfo, boolean value) {
		return isKeyInfoUsable(keyInfo) ? getEntry(keyInfo.getKey()).setBoolean(value) : value;
	}


	/**
	 * Returns the boolean the key maps to. If the key does not exist or is of
	 *     different type, it will return the default value.
	 * @param keyInfo contains  the key to look up
	 * @param defaultValue the value to be returned if no value is found
	 * @return the value associated with the given key or the given default value
	 *     if there is no value associated with the key
	 */
	public static boolean getBoolean(SdKeysE keyInfo, boolean defaultValue) {
		return isKeyInfoUsable(keyInfo) ? getEntry(keyInfo.getKey()).getBoolean(defaultValue) : defaultValue;
	}

	/**
	 * Put a number in the table.
	 * @param keyInfo contains the key to be assigned to
	 * @param value the value that will be assigned
	 * @return False if the table key already exists with a different type
	 */
	public static boolean putNumber(SdKeysE keyInfo, double value) {
		return (isKeyInfoUsable(keyInfo)) ? getEntry(keyInfo.getKey()).setDouble(value) : false /*TBD*/;
	}

	/**
	 * Returns the number the key maps to. If the key does not exist or is of
	 *     different type, it will return the default value.
	 * @param keyInfo contains the key to look up
	 * @param defaultValue the value to be returned if no value is found
	 * @return the value associated with the given key or the given default value
	 *     if there is no value associated with the key
	 */
	public static double getNumber(SdKeysE keyInfo, double defaultValue) {
		//Note: x=a?b:c; means if(a){x=b;}else{x=c;} -- conditional operator [744conditionalOp]
		return isKeyInfoUsable(keyInfo) ? getEntry(keyInfo.getKey()).getDouble(defaultValue) : defaultValue;
	}

	/**
	 * Put a string in the table.
	 * @param keyInfo contains  the key to be assigned to
	 * @param value the value that will be assigned
	 * @return False if the table key already exists with a different type
	 */
	public static boolean putString(SdKeysE keyInfo, String value) {
		return isKeyInfoUsable(keyInfo) ? getEntry(keyInfo.getKey()).setString(value) : false;
	}

	/**
	 * Returns the string the key maps to. If the key does not exist or is of
	 *     different type, it will return the default value.
	 * @param keyInfo contains  the key to look up
	 * @param defaultValue the value to be returned if no value is found
	 * @return the value associated with the given key or the given default value
	 *     if there is no value associated with the key
	 */
	public static String getString(SdKeysE keyInfo, String defaultValue) {
		return isKeyInfoUsable(keyInfo) ? getEntry(keyInfo.getKey()).getString(defaultValue) : defaultValue;
	}

	/**
	 * Put a boolean array in the table.
	 * @param keyInfo contains  the key to be assigned to
	 * @param value the value that will be assigned
	 * @return False if the table key already exists with a different type
	 */
	public static boolean putBooleanArray(SdKeysE keyInfo, boolean[] value) {
		return isKeyInfoUsable(keyInfo) ? getEntry(keyInfo.getKey()).setBooleanArray(value) : false;
	}

	/**
	 * Put a boolean array in the table.
	 * @param keyInfo contains  the key to be assigned to
	 * @param value the value that will be assigned
	 * @return False if the table key already exists with a different type
	 */
	public static boolean putBooleanArray(SdKeysE keyInfo, Boolean[] value) {
		return isKeyInfoUsable(keyInfo) ? getEntry(keyInfo.getKey()).setBooleanArray(value) : false;
	}

	/**
	 * Returns the boolean array the key maps to. If the key does not exist or is
	 *     of different type, it will return the default value.
	 * @param keyInfo contains  the key to look up
	 * @param defaultValue the value to be returned if no value is found
	 * @return the value associated with the given key or the given default value
	 *     if there is no value associated with the key
	 */
	public static boolean[] getBooleanArray(SdKeysE keyInfo, boolean[] defaultValue) {
		return isKeyInfoUsable(keyInfo) ? getEntry(keyInfo.getKey()).getBooleanArray(defaultValue) : defaultValue;
	}

	/**
	 * Returns the boolean array the key maps to. If the key does not exist or is
	 *     of different type, it will return the default value.
	 * @param keyInfo contains  the key to look up
	 * @param defaultValue the value to be returned if no value is found
	 * @return the value associated with the given key or the given default value
	 *     if there is no value associated with the key
	 */
	public static Boolean[] getBooleanArray(SdKeysE keyInfo, Boolean[] defaultValue) {
		return isKeyInfoUsable(keyInfo) ? getEntry(keyInfo.getKey()).getBooleanArray(defaultValue) : defaultValue;
	}

	/**
	 * Put a number array in the table.
	 * @param keyInfo contains  the key to be assigned to
	 * @param value the value that will be assigned
	 * @return False if the table key already exists with a different type
	 */
	public static boolean putNumberArray(SdKeysE keyInfo, double[] value) {
		return isKeyInfoUsable(keyInfo) ? getEntry(keyInfo.getKey()).setDoubleArray(value) : false;
	}

	/**
	 * Put a number array in the table.
	 * @param keyInfo contains  the key to be assigned to
	 * @param value the value that will be assigned
	 * @return False if the table key already exists with a different type
	 */
	public static boolean putNumberArray(SdKeysE keyInfo, Double[] value) {
		return isKeyInfoUsable(keyInfo) ? getEntry(keyInfo.getKey()).setNumberArray(value) : false;
	}

	/**
	 * Gets the current value in the table, setting it if it does not exist.
	 * @param keyInfo contains  the key
	 * @param defaultValue the default value to set if key does not exist.
	 * @return False if the table key exists with a different type
	 */
	public static boolean setDefaultNumberArray(SdKeysE keyInfo, double[] defaultValue) {
		return isKeyInfoUsable(keyInfo) ? getEntry(keyInfo.getKey()).setDefaultDoubleArray(defaultValue) : false;
	}

	/**
	 * Returns the number array the key maps to. If the key does not exist or is
	 *     of different type, it will return the default value.
	 * @param keyInfo contains  the key to look up
	 * @param defaultValue the value to be returned if no value is found
	 * @return the value associated with the given key or the given default value
	 *     if there is no value associated with the key
	 */
	public static double[] getNumberArray(SdKeysE keyInfo, double[] defaultValue) {
		return isKeyInfoUsable(keyInfo) ? getEntry(keyInfo.getKey()).getDoubleArray(defaultValue) : defaultValue;
	}

	/**
	 * Returns the number array the key maps to. If the key does not exist or is
	 *     of different type, it will return the default value.
	 * @param keyInfo contains  the key to look up
	 * @param defaultValue the value to be returned if no value is found
	 * @return the value associated with the given key or the given default value
	 *     if there is no value associated with the key
	 */
	public static Double[] getNumberArray(SdKeysE keyInfo, Double[] defaultValue) {
		return isKeyInfoUsable(keyInfo) ? getEntry(keyInfo.getKey()).getDoubleArray(defaultValue) : defaultValue;
	}

	/**
	 * Put a string array in the table.
	 * @param keyInfo contains  the key to be assigned to
	 * @param value the value that will be assigned
	 * @return False if the table key already exists with a different type
	 */
	public static boolean putStringArray(SdKeysE keyInfo, String[] value) {
		return isKeyInfoUsable(keyInfo) ? getEntry(keyInfo.getKey()).setStringArray(value) : false;
	}

	/**
	 * Gets the current value in the table, setting it if it does not exist.
	 * @param keyInfo contains  the key
	 * @param defaultValue the default value to set if key does not exist.
	 * @return False if the table key exists with a different type
	 */
	public static boolean setDefaultStringArray(SdKeysE keyInfo, String[] defaultValue) {
		return isKeyInfoUsable(keyInfo) ? getEntry(keyInfo.getKey()).setDefaultStringArray(defaultValue) : false;
	}

	/**
	 * Returns the string array the key maps to. If the key does not exist or is
	 *     of different type, it will return the default value.
	 * @param keyInfo contains  the key to look up
	 * @param defaultValue the value to be returned if no value is found
	 * @return the value associated with the given key or the given default value
	 *     if there is no value associated with the key
	 */
	public static String[] getStringArray(SdKeysE keyInfo, String[] defaultValue) {
		return isKeyInfoUsable(keyInfo) ? getEntry(keyInfo.getKey()).getStringArray(defaultValue) : defaultValue;
	}

	/**
	 * Put a raw value (byte array) in the table.
	 * @param keyInfo contains  the key to be assigned to
	 * @param value the value that will be assigned
	 * @return False if the table key already exists with a different type
	 */
	public static boolean putRaw(SdKeysE keyInfo, byte[] value) {
		return isKeyInfoUsable(keyInfo) ? getEntry(keyInfo.getKey()).setRaw(value) : false;
	}

	/**
	 * Put a raw value (bytes from a byte buffer) in the table.
	 * @param keyInfo contains  the key to be assigned to
	 * @param value the value that will be assigned
	 * @param len the length of the value
	 * @return False if the table key already exists with a different type
	 */
	public static boolean putRaw(SdKeysE keyInfo, ByteBuffer value, int len) {
		return isKeyInfoUsable(keyInfo) ? getEntry(keyInfo.getKey()).setRaw(value, len) : false;
	}

	/**
	 * Gets the current value in the table, setting it if it does not exist.
	 * @param keyInfo contains  the key
	 * @param defaultValue the default value to set if key does not exist.
	 * @return False if the table key exists with a different type
	 */
	public static boolean setDefaultRaw(SdKeysE keyInfo, byte[] defaultValue) {
		return isKeyInfoUsable(keyInfo) ? getEntry(keyInfo.getKey()).setDefaultRaw(defaultValue) : false;
	}

	/**
	 * Returns the raw value (byte array) the key maps to. If the key does not
	 *     exist or is of different type, it will return the default value.
	 * @param keyInfo contains  the key to look up
	 * @param defaultValue the value to be returned if no value is found
	 * @return the value associated with the given key or the given default value
	 *     if there is no value associated with the key
	 */
	public static byte[] getRaw(SdKeysE keyInfo, byte[] defaultValue) {
		return isKeyInfoUsable(keyInfo) ? getEntry(keyInfo.getKey()).getRaw(defaultValue) : defaultValue;
	}

	  /**
	   * Maps the specified key to the specified value in this table. The key can not be null. The value
	   * can be retrieved by calling the get method with a key that is equal to the original key.
	   *
	   * @param key  the key
	   * @param data the value
	   * @throws IllegalArgumentException If key is null
	   */
	  public static synchronized void putData(SdKeysE keyInfo, Sendable data) {
			if(isKeyInfoUsable(keyInfo)) { SmartDashboard.putData(keyInfo.getKey(), data); }
	  }
	
}
