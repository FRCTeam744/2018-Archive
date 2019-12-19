package org.usfirst.frc.tm744yr18.robot.devices;

import org.usfirst.frc.tm744yr18.robot.interfaces.TmToolsI.P;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmToolsI.P.PrtYn;

import edu.wpi.first.networktables.NetworkTable;
import edu.wpi.first.networktables.NetworkTableEntry;
import edu.wpi.first.networktables.NetworkTableInstance;

public class TmLimelightDev {
	NetworkTable table = null; //NetworkTableInstance.getDefault().getTable("limelight");
	NetworkTableEntry m_entryTv = null;
	boolean m_tableEntriesRdy = false;
	private static final TmLimelightDev m_instance = new TmLimelightDev();
	public static TmLimelightDev getInstance() { return m_instance; }
	private TmLimelightDev() {
//		NetworkTable table = NetworkTableInstance.getDefault().getTable("limelight");
	}
	
	public boolean isRdy() { return isRdy(null); }
	public boolean isRdy(String option) {
		boolean ans;
		PrtYn	flag = PrtYn.N;
		if(option != null) {
			switch(option) {
			case "verbose":
				flag = PrtYn.Y;
				break;
			}
		}
		if( ! m_tableEntriesRdy) {
			if(table==null) {
				table = NetworkTableInstance.getDefault().getTable("limelight");
			}
			if(table != null) {
				m_entryTv = table.getEntry("tv");
				m_tableEntriesRdy = (null != m_entryTv);
			}
		}
		P.println(flag, "LimeLight: rdy?" + (m_tableEntriesRdy?"Y":"N") + " hasTable?" + ((table==null)?"N":"Y") + " tvEntry=" + m_entryTv);
		ans = m_tableEntriesRdy;
		return ans;
	}
	
	private double getTableValue(String key, double dflt) {
		double ans = dflt;
		if(isRdy()) { 
			NetworkTableEntry entry = table.getEntry(key);
			if(entry != null) {
				ans = entry.getDouble(dflt);
			}
		}
		return ans;
	}

	/**
	 * 
	 * @param key
	 * @param value
	 * @return false if value associated with 'key' is not same type as 'value'
	 *               or if no entry available for 'key'
	 */
	private boolean setTableValue(String key, double value) {
		boolean ans = false; //assume key's value is not of type 'double'
		if(isRdy()) { 
			NetworkTableEntry entry = table.getEntry(key);
			if(entry != null) {
				ans = entry.setDouble(value);
			}
		}
		return ans;
	}

	public boolean hasValidTarget() { return (1==getTableValue("tv", 0)); } //1 or 0 for yes or no
	
	public double getHorizontalOffsetDegrees() { return getTableValue("tx", 0); } //-27 to +27 degrees
	public double getVerticalOffsetDegrees() { return getTableValue("ty", 0); } //-20.5 to 20.5 degrees
	public double getTargetAreaPercent() { return getTableValue("ta", 0); } // 0% to 100%
	public double getTargetSkewDegrees() { return getTableValue("ts", 0); } // -90 to 0 degrees
	public double getPipelineLatencyMs() { return getTableValue("tl", 0); } // ms of latency in pipeline itself
	public double getMinImageCaptureLatencyMs() { return 11; } // ms of latency for image capture
	public double getTotalLatency() { return getMinImageCaptureLatencyMs() + getPipelineLatencyMs(); }
	
	public enum LedModeE {
		ON(0), OFF(1), BLINK(2);		
		public final double eLimelightValue;		
		private LedModeE(double llValue) { eLimelightValue = llValue; }
	}
	public boolean setLedMode(LedModeE newMode) { return setTableValue("ledMode", newMode.eLimelightValue); }
	
	public enum CamModeE { 
		VISION_PROCESSING(0), 
		DRIVER(1) //higher exposure, no vision processing
		;		
		public final double eLimelightValue;		
		private CamModeE(double llValue) { eLimelightValue = llValue; }
	}
	public boolean setCamMode(CamModeE newMode) { return setTableValue("camMode", newMode.eLimelightValue); }
	
	public enum PipelineSelE { //pipeline selection
		PIPELINE_0(0), 
		PIPELINE_1(1), 
		PIPELINE_2(2), 
		PIPELINE_3(3), 
		PIPELINE_4(4), 
		PIPELINE_5(5), 
		PIPELINE_6(6), 
		PIPELINE_7(7), 
		PIPELINE_8(8), 
		PIPELINE_9(9), 
		;		
		public final double eLimelightValue;		
		private PipelineSelE(double llValue) { eLimelightValue = llValue; }
	}
	public boolean setCurPipeline(PipelineSelE newPipeline) { return setTableValue("pipeline", newPipeline.eLimelightValue); }
	
}
