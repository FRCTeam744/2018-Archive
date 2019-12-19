package org.usfirst.frc.tm744yr18.robot.interfaces;

import java.util.List;

import org.usfirst.frc.tm744yr18.robot.interfaces.TmToolsI.P;

public interface TmToStringI {

	/**
	 * print detailed info for an entry in an enum, list, etc.
	 * @return
	 */
	public default String toStringLog() { return toStringLog(""); }
	public String toStringLog(String prefix);
//	public String toStringLog(String firstLinePrefix, String addlLinePrefix);
	
	/**
	 * when printing toStringLog() info for all elements of an enum, list, etc.,
	 * use this method to supply a header line.
	 * 
	 * @return null if no header info
	 */
	public default String toStringHdr() { return toStringHdr(""); }
	public String toStringHdr(String prefix);// { return toStringHdr(prefix, ""); }
//	public String toStringHdr(String firstLinePrefix, String addlLinePrefix);
	
	/**
	 * when printing toStringLog() info for all elements of an enum, list, etc.,
	 * use this method to print the enum name, general notes about the enum, list, or whatever
	 * 
	 * should be printed before any toStringHdr() stuff
	 * 
	 *	to return the enum class name:	return this.getClass().getSimpleName();
	 *
	 * @return null if no notes info
	 */
	public String toStringNotes();
	
	/*
	 * this is a "generic method" where variables are used for the Types of various things
	 * <T extends TmToStringI> tells java that we're using T as one such type variable
	 * and that the only valid types are those that extend TmToStringI
	 */
	public static <T extends TmToStringI> int calcMaxNameLenForEnum(T[] values) {
		int ans = 0;
		for(T v : values) {
			String vname = v.toString();
			if(null != vname) {
				int len = vname.length();
				if(len > ans) { ans = len; }
			}
		}
		return ans;
	}
	/*
	 * this is a "generic method" where variables are used for the Types of various things
	 * <T extends TmToStringI> tells java that we're using T as one such type variable
	 * and that the only valid types are those that extend TmToStringI
	 * 
	 * show only the header and the specified number of lines of log info
	 */
	public static <T extends TmToStringI> void showEnumHeadersPlus(int logLineCnt, T[] values) {
		String prefix = "   ";
		int maxNameLen = TmToStringI.calcMaxNameLenForEnum(values);
		String classSimpleName = values[0].getClass().getSimpleName();
		P.println(classSimpleName + " -- " + values[0].getClass().getCanonicalName() +
				" (max name len=" + maxNameLen + ", class name len=" + classSimpleName.length() + ")");
		if(null != values[0].toStringNotes()) {P.println("(Note: " + values[0].toStringNotes() + ")");} //any entry works to get toStringHdr()
//		if(null != values[0].toStringHdr()) {P.println(prefix + values[0].toStringHdr());} //any entry works to get toStringHdr()
		if(null != values[0].toStringHdr("")) {P.println(values[0].toStringHdr(prefix));} //any entry works to get toStringHdr()
		int lineCnt = logLineCnt;
		for(T dt : values) {
			if(lineCnt>0) {
//				P.println(prefix + dt.toStringLog());
				P.println(dt.toStringLog(prefix));
				lineCnt--;
			} else {
				break; //exit for loop
			}
		}
	}
	/*
	 * this is a "generic method" where variables are used for the Types of various things
	 * <T extends TmToStringI> tells java that we're using T as one such type variable
	 * and that the only valid types are those that extend TmToStringI
	 */
	public static <T extends TmToStringI> void showEnumEverything(T[] values) {
		String prefix = "   ";
		int maxNameLen = TmToStringI.calcMaxNameLenForEnum(values);
		String classSimpleName = values[0].getClass().getSimpleName();
		P.println(classSimpleName + " -- " + values[0].getClass().getCanonicalName() +
				" (max name len=" + maxNameLen + ", class name len=" + classSimpleName.length() + ")");
		if(null != values[0].toStringNotes()) {P.println("(Note: " + values[0].toStringNotes() + ")");} //any entry works to get toStringHdr()
//		if(null != values[0].toStringHdr()) {P.println(prefix + values[0].toStringHdr());} //any entry works to get toStringHdr()
//		for(T dt : values) { P.println(prefix + dt.toStringLog()); }
		if(null != values[0].toStringHdr("")) {P.println(values[0].toStringHdr(prefix));} //any entry works to get toStringHdr()
		for(T dt : values) { P.println(dt.toStringLog(prefix)); }
	}

	/*
	 * this is a "generic method" where variables are used for the Types of various things
	 * <T extends TmToStringI> tells java that we're using T as one such type variable
	 * and that the only valid types are those that extend TmToStringI
	 */
	public static <T extends TmToStringI> void showListHdeadersPlus(int logLineCnt, List<T> list) {
		String prefix = "   ";
		P.println("List<" + list.get(0).getClass().getSimpleName() + "> -- " + list.get(0).getClass().getCanonicalName());
		if(null != list.get(0).toStringNotes()) {P.println("(Note: " + list.get(0).toStringNotes() + ")");} //any entry works to get toStringHdr()
//		if(null != list.get(0).toStringHdr()) {P.println(prefix + list.get(0).toStringHdr());} //any entry works to get toStringHdr()
		if(null != list.get(0).toStringHdr("")) {P.println(list.get(0).toStringHdr(prefix));} //any entry works to get toStringHdr()
		int lineCnt = logLineCnt;
		for(T dt : list) {
			if(lineCnt>0) {
//				P.println(prefix + dt.toStringLog());
				P.println(dt.toStringLog(prefix));
				lineCnt--;
			} else {
				break; //exit for loop
			}
		}
	}
	/*
	 * this is a "generic method" where variables are used for the Types of various things
	 * <T extends TmToStringI> tells java that we're using T as one such type variable
	 * and that the only valid types are those that extend TmToStringI
	 */
	public static <T extends TmToStringI> void showListEverything(List<T> list) {
		String prefix = "   ";
		P.println("List<" + list.get(0).getClass().getSimpleName() + "> -- " + list.get(0).getClass().getCanonicalName());
		if(null != list.get(0).toStringNotes()) {P.println("(Note: " + list.get(0).toStringNotes() + ")");} //any entry works to get toStringHdr()
//		if(null != list.get(0).toStringHdr()) {P.println(prefix + list.get(0).toStringHdr());} //any entry works to get toStringHdr()
//		//this is a forEach loop using a "lambda method" to display toString() info for each item in the list
//		//it's an alternative to: for(List<T> e : list) {P.println(prefix + e.toString());}
//		list.stream().forEach(e -> P.println(prefix + e.toStringLog()));
		if(null != list.get(0).toStringHdr("")) {P.println(list.get(0).toStringHdr(prefix));} //any entry works to get toStringHdr()
		//this is a forEach loop using a "lambda method" to display toString() info for each item in the list
		//it's an alternative to: for(List<T> e : list) {P.println(prefix + e.toString());}
		list.stream().forEach(e -> P.println(e.toStringLog(prefix)));
	}

}
