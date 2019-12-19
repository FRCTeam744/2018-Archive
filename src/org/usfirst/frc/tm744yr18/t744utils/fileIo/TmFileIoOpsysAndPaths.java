package org.usfirst.frc.tm744yr18.t744utils.fileIo;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;

public class TmFileIoOpsysAndPaths {

	static OpSysInfo opsys = OpSysInfo.getInstance();
	
	//used to list specific Tm744 directories used to group files
	public static enum TmSubdirsE {
		TRAJ_SUBDIR_NAME("trajectories"),
		DRIVE_TUNING_LOG_SUBDIR("driveTuning"),
		ARM_TUNING("armTuning")
		;
		private final String eSubdirName; //the directory under the root
		private final String eChildName1; //the directory under eSubdirName
		private final String eChildName2; //the directory under eChildName1
		
		private String eDirAndChildren = ""; 
		private String eFqName = ""; //fully-qualified directory name (starts at root)
		private Path ePath; //path for eFqName
		
		//Constructors
		private TmSubdirsE(String dirName) { this(dirName, "", ""); }
		private TmSubdirsE(String dirName, String childName1) { this(dirName, childName1, "");	}
		private TmSubdirsE(String dirName, String childName1, String childName2) {
			eSubdirName = dirName; //first dir under root
			eChildName1 = childName1; //second dir level
			eChildName2 = childName2; //third dir level
			eDirAndChildren = "";  //init'd in finishSetup()
			eFqName = ""; //init'd in finishSetup()
			ePath = null; //init'd in finishSetup()
		}
		
		//checks that all variables are initialized; if not, initializes them
		private void finishSetup() {
			if(opsys==null) { opsys = OpSysInfo.getInstance(); } 
			if(eFqName.equals("")) {
				String work = eSubdirName;
				if(!eChildName1.equals("")) {
					work += opsys.getFileDelim() + eChildName1;
					if(!eChildName2.equals("")) {
						work += opsys.getFileDelim() + eChildName2;
					}
				}
				eDirAndChildren = work;
				eFqName = opsys.getRootUserFiles() + opsys.getFileDelim() + work;
				ePath = Paths.get(eFqName);
			}
		}
		
		//misc. methods for this enum
		public String getSubdirStr() { finishSetup(); return eDirAndChildren; }
		public String getFullPathStr() { finishSetup(); return eFqName; }
		public Path getDirPath() { finishSetup(); return ePath; }
		
		public Path getFile(String filename) { finishSetup(); return ePath.resolve(filename); }
	}
	
	//constructors
	TmFileIoOpsysAndPaths() { 
		opsys = OpSysInfo.getInstance();
	}

	public static class OpSysInfo {
		boolean osWin = false;
		boolean osRio = false;
		String fileDelim = "/"; //default to roboRIO style
		
		public enum OpSysSelE { WINDOWS, ROBO_RIO; }
		
		//when running on Windows, assume Tm744 files are part of the eclipse project
		static final String T744_FILES_ROOT_WINDOWS = ".\\src\\t744utils\\fileIo";
		//when running on RoboRIO, assume Tm744 files are in the directory designated
		//in FRC documentation
		static final String T744_FILES_ROOT_ROBORIO = "/home/lvuser/tm744";

		String t744FilesRootPath = T744_FILES_ROOT_ROBORIO; //default??
		
		/*---------------------------------------------------------
		 * getInstance stuff                                      
		 *---------------------------------------------------------*/
		/** 
		 * handle making the singleton instance of this class and giving
		 * others access to it
		 */
		private static OpSysInfo m_instance = getInstance();

		public static synchronized TmFileIoOpsysAndPaths.OpSysInfo getInstance() {
			OpSysSelE os;
			if (m_instance == null) {
	            if (System.getProperty("os.name").startsWith("Windows")) { os = OpSysSelE.WINDOWS; }
	            else { os = OpSysSelE.ROBO_RIO; }
				m_instance = new OpSysInfo(os); //opsys);
			}
			return m_instance;
		}
		
		/*----------------end of getInstance stuff----------------*/

		
		//constructors for use by getInstance() only
		private OpSysInfo(OpSysSelE opsys) {
			switch(opsys) {
			case WINDOWS:
				osWin = true;
				fileDelim = "\\";
				t744FilesRootPath = T744_FILES_ROOT_WINDOWS;
				break;
			case ROBO_RIO:
			default:
				osRio = true;
				fileDelim = "/";
				t744FilesRootPath = T744_FILES_ROOT_ROBORIO;
				break;
			}
		}
		
		public String getFileDelim() { return fileDelim; }
				
		public boolean isWindows() { return osWin; }
		public boolean isRoboRio() { return osRio; }
		
		public String getRootUserFiles() { return t744FilesRootPath; }

	}
}
