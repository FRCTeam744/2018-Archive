package org.usfirst.frc.tm744yr18.t744utils.fileIo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

import org.usfirst.frc.tm744yr18.robot.exceptions.TmExceptions;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmToolsI.P;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmToolsI.Tt;
import org.usfirst.frc.tm744yr18.t744utils.fileIo.TmFileIoOpsysAndPaths;
import org.usfirst.frc.tm744yr18.t744utils.fileIo.TmFileIoOpsysAndPaths.OpSysInfo;
import org.usfirst.frc.tm744yr18.t744utils.fileIo.TmFileIoOpsysAndPaths.TmSubdirsE;
import org.usfirst.frc.tm744yr18.t744utils.fileIo.TmTrajectoryFileIo.TrajDataCsv;

public class TmTrajectoryFileIo {

//	static OpSysInfo opsys;
//	TmFileIoOpsysAndPaths wFileIo;
	String m_trajFileName;
	Path m_trajFile;
	//String[] hdr; //defined near getters with nice intuitive names...
	
	public String getFilename() {
		return m_trajFileName;
	}
	//constructors
	public TmTrajectoryFileIo(String filename) {
//		wFileIo = new TmFileIoOpsysAndPaths(); //opsysSel);
//		opsys = OpSysInfo.getInstance(); //opsysSel);
		m_trajFileName = filename;
		m_trajFile = TmSubdirsE.TRAJ_SUBDIR_NAME.getFile(filename);
	}
	
	public class TrajDataCsv {
		public int fileLineNbr;
		//allocate pointer to memory for array holding data from a line in the csv file
		public double[] data; 
		public static final int NBR_OF_COLS = 17;
		//for now, just ref the column names from the CSV file
		
		//constructor(s)
		TrajDataCsv() {
			data = new double[NBR_OF_COLS]; //allocates memory for array
			for(int i=0; i<NBR_OF_COLS; i++) { //initialize memory for array
				data[i] = 0.0;
			}
			if(hdr.length != NBR_OF_COLS) {
				throw TmExceptions.getInstance().new Team744RunTimeEx(Tt.getClassName(this) + " header array is wrong size");
			}
		}
		
		//getters
		public double getColA() { return data[0]; }
		public double getColB() { return data[1]; }
		public double getColC() { return data[2]; }
		public double getColD() { return data[3]; }
		public double getColE() { return data[4]; }
		public double getColF() { return data[5]; }
		public double getColG() { return data[6]; }
		public double getColH() { return data[7]; }
		public double getColI() { return data[8]; }
		public double getColJ() { return data[9]; }
		public double getColK() { return data[10]; }
		public double getColL() { return data[11]; }
		public double getColM() { return data[12]; }
		public double getColN() { return data[13]; }
		public double getColO() { return data[14]; }
		public double getColP() { return data[15]; }
		public double getColQ() { return data[16]; }
		
		public double getLeftX()     {return getColA();}
		public double getLeftY()     {return getColB();}
		public double getRightX()    {return getColC();}
		public double getRightY()    {return getColD();}
		public double getLeftPos()   {return getColE();}
		public double getRightPos()  {return getColF();}
		public double getLeftVel()   {return getColG();}
		public double getRightVel()  {return getColH();}
		public double getLeftAcc()   {return getColI();}
		public double getRightAcc()  {return getColJ();}
		public double getLeftJerk()  {return getColK();}
		public double getRightJerk() {return getColL();}
		public double getLeftDt()    {return getColM();}
		public double getRightDt()   {return getColN();}
		public double getLeftTime()  {return getColO();}
		public double getRightTime() {return getColP();}
		public double getAngle()     {return getColQ();}		
		
	}
	
	public String[] hdr = {"l-X", "l-Y", "r-X", "r-Y", "l-Pos", "r-Pos", "l-Vel", "r-Vel",
			"l-Acc", "r-Acc", "l-Jerk", "r-Jerk", "l-Dt", "r-Dt", "l-time", "r-time", "angle"};	

	public String getHdrColA() { return hdr[0]; }
	public String getHdrColB() { return hdr[1]; }
	public String getHdrColC() { return hdr[2]; }
	public String getHdrColD() { return hdr[3]; }
	public String getHdrColE() { return hdr[4]; }
	public String getHdrColF() { return hdr[5]; }
	public String getHdrColG() { return hdr[6]; }
	public String getHdrColH() { return hdr[7]; }
	public String getHdrColI() { return hdr[8]; }
	public String getHdrColJ() { return hdr[9]; }
	public String getHdrColK() { return hdr[10]; }
	public String getHdrColL() { return hdr[11]; }
	public String getHdrColM() { return hdr[12]; }
	public String getHdrColN() { return hdr[13]; }
	public String getHdrColO() { return hdr[14]; }
	public String getHdrColP() { return hdr[15]; }
	public String getHdrColQ() { return hdr[16]; }

	//use a List to hold the lines from the CSV file
	public List<TrajDataCsv> trajDataCsvL = new ArrayList<TrajDataCsv>();
	public int linesInCsvFile = 0;
	public List<String> fileLines;
	
	public void loadCsvData() { loadCsvData(m_trajFile); }
	public void loadCsvData(Path fileToRead) { loadCsvData(fileToRead, 0); }
	public void loadCsvData(Path fileToRead, int nbrOfHdrLines) {
		linesInCsvFile = 0;
		try {
			fileLines = Files.readAllLines(fileToRead);
			linesInCsvFile = fileLines.size();
			for(int ln=nbrOfHdrLines; ln<linesInCsvFile; ln++) { //for(int ln=0; ln<linesInCsvFile; ln++) {
				String exceptionMsgPrefix = "CSV trajectory file " + m_trajFileName + 
						" line " + (ln+1) + ": ";
				trajDataCsvL.add(parseLine(ln, fileLines.get(ln),exceptionMsgPrefix));
			}
		} catch(NoSuchFileException e) {
//			TmExceptions.reportExceptionOneLine(e, "CSV trajectory file " + m_trajFileName + " not found");
			P.println("Oops!!! Trajectory file not found!!!! Using default data!! --- filename: " + m_trajFileName);
			TrajDataCsv dummyLine = new TrajDataCsv();
			for(int i = 0; i<TrajDataCsv.NBR_OF_COLS; i++) { 
				dummyLine.data[i] = 0.0; 
			}
			trajDataCsvL.add(dummyLine); //so there's something safe for callers to process
		} catch(IOException e) {
			TmExceptions.reportExceptionMultiLine(e, "CSV trajectory file " + m_trajFileName + ": ");
		}	
	}
	
	public TrajDataCsv parseLine(int fileLineIndex, String csvLine, String exceptionPrefix) {
		TrajDataCsv ans = new TrajDataCsv();
		Scanner s = null;
		s = new Scanner(csvLine);
		s.useDelimiter(",");
		int cnt = 0;
		ans.fileLineNbr = fileLineIndex + 1;
		while(s.hasNext() && cnt<TrajDataCsv.NBR_OF_COLS) {
			try {
				ans.data[cnt] = s.nextDouble();
				cnt++;
			} catch(InputMismatchException e) {
				TmExceptions.reportExceptionOneLine(e, exceptionPrefix);
			}
		}
		s.close();
		return ans;
	}
	
	public String formatHdrForDisplay() {
		String ans = String.format("            %-10s  %-10s  %-10s  %-10s  %-10s"
				+ "     %-10s  %-10s  %-10s  %-10s  %-10s      %-10s  %-10s  %-10s  %-10s"
				+ "     %-10s  %-10s  %-10s\n",
	                        getHdrColA(), getHdrColB(), getHdrColC(), getHdrColD(), getHdrColE(),
	                        getHdrColF(), getHdrColG(), getHdrColH(), getHdrColI(), getHdrColJ(),
	                        getHdrColK(), getHdrColL(), getHdrColM(), getHdrColN(), getHdrColO(),
	                        getHdrColP(), getHdrColQ()
	                        );
		return ans;
	}
	public String formatLineForDisplay(int dataLineIndex) {
		TrajDataCsv l = trajDataCsvL.get(dataLineIndex);
//		String ans = String.format("line %3d: %0 2.6f  %0 2.6f  %0 2.6f  %0 2.6f  %0 2.6f"
//				+ "     %0 2.6f  %0 2.6f  %0 2.6f  %0 2.6f  %0 2.6f      %0 2.6f  %0 2.6f  %0 2.6f  %0 2.6f"
//				+ "     %0 2.6f  %0 2.6f  %0 2.6f\n",
		String ans = String.format("line %3d: % 10.6f  % 10.6f  % 10.6f  % 10.6f  % 10.6f"
				+ "     % 10.6f  % 10.6f  % 10.6f  % 10.6f  % 10.6f      % 10.6f  % 10.6f  % 10.6f  % 10.6f"
				+ "     % 10.6f  % 10.6f  % 10.6f\n",
	                        l.fileLineNbr, //(++cnt),
	                        l.getColA(), l.getColB(), l.getColC(), l.getColD(), l.getColE(),
	                        l.getColF(), l.getColG(), l.getColH(), l.getColI(), l.getColJ(),
	                        l.getColK(), l.getColL(), l.getColM(), l.getColN(), l.getColO(),
	                        l.getColP(), l.getColQ()
	                        );
		return ans;
	}
	
	public void showOnConsole() { showOnConsole(null); }
	public void showOnConsole(String callerHeaderLinePrefix) {
		String callerHdr = (callerHeaderLinePrefix==null) ? "" : (callerHeaderLinePrefix + " ");
		System.out.println(callerHdr + "trajectory file " + m_trajFileName); //Tt.getClassName(this) + " showing trajectory file " + filename);
		System.out.println(formatHdrForDisplay());
		for(int ndx=0; ndx<trajDataCsvL.size(); ndx++) { //TrajDataCsv l : trajDataCsvL) {
			System.out.println(formatLineForDisplay(ndx));	
		}
	}
	
//	//should be called from a try-with-resources statement
//	public Path openTrajFile() { return openTrajFile(DEFAULT_TRAJ_FILE_NAME); }
//	public Path openTrajFile(String trajFileName) {
//		Path ans = null;
//		return ans;
//	}
//	
//	public void closeTrajFile(Path fileToClose) {
//	
//	}
	
}
