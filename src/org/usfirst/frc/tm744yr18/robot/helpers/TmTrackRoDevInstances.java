package org.usfirst.frc.tm744yr18.robot.helpers;

import java.util.ArrayList;
import java.util.List;

import org.usfirst.frc.tm744yr18.robot.config.TmHdwrRoCntls.RoNamedControlsE;
//import org.usfirst.frc.tm744yr18.robot.devices.TmFakeable_CanTalonSrx;
//import org.usfirst.frc.tm744yr18.robot.devices.TmFakeable_CanTalonSrx.FakeParms;
//import org.usfirst.frc.tm744yr18.robot.devices.TmFakeable_CanTalonSrx.TrackInstances;
//import org.usfirst.frc.tm744yr18.robot.devices.TmFakeable_CanTalonSrx.TrackInstances.TrackInstEntry;
//import org.usfirst.frc.tm744yr18.robot.exceptions.TmExceptions;
import org.usfirst.frc.tm744yr18.robot.exceptions.TmExceptions.Team744RunTimeEx;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmRoControlUserI;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmToolsI.Tt;

public class TmTrackRoDevInstances {

//	public static class TrackInstances {
//		private static final TrackInstances tiInstance = new TrackInstances();
//		public static TrackInstances getInstance() { return tiInstance; }
//		private TrackInstances() {}
//		
//		static List<TrackInstEntry> tiFakeableCanTalonSrxList = new ArrayList<>();
//		
//		public class TrackInstEntry<CU extends TmRoControlUserI> {
//			public CU fctCntlUser;
//			public TmFakeable_CanTalonSrx fctObj;
//			public RoNamedControlsE fctNamedCntl;
//			public TrackInstEntry(CU cntlUser, TmFakeable_CanTalonSrx fct, RoNamedControlsE namedCntl) {
//				fctCntlUser = cntlUser;
//				fctObj = fct;
//				fctNamedCntl = namedCntl;
//			}
//		}
//		
//		public static List<TrackInstEntry> getList() { return tiFakeableCanTalonSrxList; }
//		
//		/**
//		 * helpful when tracking followers?
//		 * @param namedCntlToFind
//		 * @return
//		 */
//		public static TmFakeable_CanTalonSrx getInstanceFromNamedControl(RoNamedControlsE namedCntlToFind) {
//			TmFakeable_CanTalonSrx ans = null;
//			for(TrackInstEntry t : tiFakeableCanTalonSrxList) {
//				if(t.fctNamedCntl.equals(namedCntlToFind)) {
//					ans = t.fctObj;
//					break; //end the loop
//				}
//			}
//			return ans;
//		}
//	}
//	
////	public TmFakeable_CANTalon(RoNamedIoE namedCanTalonIoDef) {
////		this(namedCanTalonIoDef, FakeParms.DEFAULT_MAX_ENCODER_TICKS_PER_SEC);
////	}
////	private TmFakeable_CANTalon(RoNamedIoE namedCanTalonIoDef, double maxEncoderTicksPerSecond) {
//	public <CU extends TmRoControlUserI> TmFakeable_CanTalonSrx(CU cntlUser, RoNamedControlsE namedCntl) {
//		this(cntlUser, namedCntl, FakeParms.DEFAULT_MAX_ENCODER_TICKS_PER_SEC);
//	}
//	private <CU extends TmRoControlUserI> TmFakeable_CanTalonSrx(CU cntlUser, RoNamedControlsE namedCntl, double maxEncoderTicksPerSecond) {
//		for(TrackInstEntry tie : TrackInstances.tiFakeableCanTalonSrxList) {
//			if(tie.fctNamedCntl.equals(namedCntl)) {
//				throw TmExceptions.getInstance().new Team744RunTimeEx(cntlUser.getClass().getSimpleName() + " CONFIGURATION ERROR!!!: " +
//								tie.fctCntlUser.getClass().getSimpleName() + " has already allocated a " +
//								Tt.getClassName(this) + " object for " + tie.fctNamedCntl.name());
//			}
//		}
//		TrackInstances.tiFakeableCanTalonSrxList.add(TrackInstances.getInstance().new TrackInstEntry(cntlUser, this, namedCntl));
//	}	
	
}
