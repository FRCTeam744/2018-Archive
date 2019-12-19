package org.usfirst.frc.tm744yr18.robot.exceptions;

import java.util.Arrays;

import org.usfirst.frc.tm744yr18.robot.interfaces.TmToolsI.P;

/**
 * Keep all our exceptions in a single file for easier reference.
 * Throw exception Aaa via: throw TmExceptions.getInstance().new Aaa(...);
 * Catch exception Aaa via: catch(TmException.Aaa t)
 * @author JudiA
 *
 */
public class TmExceptions {
	/*---------------------------------------------------------
	 * getInstance stuff                                      
	 *---------------------------------------------------------*/
	/** 
	 * handle making the singleton instance of this class and giving
	 * others access to it
	 */
	private static TmExceptions m_instance;

	public static synchronized TmExceptions getInstance() {
		if (m_instance == null) {
			m_instance = new TmExceptions();
		}
		return m_instance;
	}

	private TmExceptions() {
		if ( ! (m_instance == null)) {
			P.println("Error!!! TmExceptions.m_instance is being modified!!");
			P.println("         was: " + m_instance.toString());
			P.println("         now: " + this.toString());
		}
		m_instance = this;
	}
	/*----------------end of getInstance stuff----------------*/


	@SuppressWarnings("serial")
	public class Team744RunTimeEx extends RuntimeException {
		public Team744RunTimeEx(String msg) {
			super(msg);
		}
	}
	
	@SuppressWarnings("serial")
	public class InappropriatePreferenceRequestEx extends Team744RunTimeEx {
		public InappropriatePreferenceRequestEx(String msg) {
			super(msg);
		}
	}

	/*
	public class MotorSafetyTimeoutEx extends Team744RunTimeEx {
		public MotorSafetyTimeoutEx(String msg) {
			super(msg);
		}
	}
	public class InappropriateMappedIoDefEx extends Team744RunTimeEx {
		public InappropriateMappedIoDefEx(String msg) {
			super(msg);
		}
	}
	public class InvalidMappedIoDefEx extends Team744RunTimeEx {
		public InvalidMappedIoDefEx(String msg) {
			super(msg);
		}
	}
	public class DuplicateMappedIoDefEx extends Team744RunTimeEx {
		public DuplicateMappedIoDefEx(String msg) {
			super(msg);
		}
	}	
	public class MappedIoRegistrationErrorsEx extends Team744RunTimeEx {
		public MappedIoRegistrationErrorsEx(String msg) {
			super(msg);
		}
	}	
	public class DuplicateAssignmentOfMappedIoDefEx extends Team744RunTimeEx {
		public DuplicateAssignmentOfMappedIoDefEx(String msg) {
			super(msg);
		}
	}	
	public class MappedIoDefNoFeatureIndexEx extends Team744RunTimeEx {
		public MappedIoDefNoFeatureIndexEx(String msg) {
			super(msg);
		}
	}
	public class UnsupportedEntityIndexTypeEx extends Team744RunTimeEx {
		public UnsupportedEntityIndexTypeEx(String msg) {
			super(msg);
		}
	}
	
	public class CannotSimulateCANTalonEx extends Team744RunTimeEx {
		public CannotSimulateCANTalonEx(String msg) {
			super(msg);
		}
	}
	
	public class DuplicateInstanceOfSingletonClassEx extends Team744RunTimeEx {
		public DuplicateInstanceOfSingletonClassEx(String msg) {
			super(msg);
		}
	}
	
	public class CameraGetVideoFailedEx extends Team744RunTimeEx {
		public CameraGetVideoFailedEx(String msg) {
			super(msg);
		}
	}
	*/
	@SuppressWarnings("serial")
	public class WrongTypeObjectForCastEx extends Team744RunTimeEx {
		public WrongTypeObjectForCastEx(String msg) {
			super(msg);
		}
	}
	@SuppressWarnings("serial")
	public class MultipleUsersForRoControlEx extends Team744RunTimeEx {
		public MultipleUsersForRoControlEx(String msg) {
			super(msg);
		}
	}
	@SuppressWarnings("serial")
	public class UnsupportedFeatureSelectedEx extends Team744RunTimeEx {
		public UnsupportedFeatureSelectedEx(String msg) {
			super(msg);
		}
	}
	@SuppressWarnings("serial")
	public class ReachedCodeThatShouldNeverExecuteEx extends Team744RunTimeEx {
		public ReachedCodeThatShouldNeverExecuteEx(String msg) {
			super(msg);
		}
	}
	@SuppressWarnings("serial")
	public class MethodCalledForInvalidEntryEx extends Team744RunTimeEx {
		public MethodCalledForInvalidEntryEx(String msg) {
			super(msg);
		}
	}
	@SuppressWarnings("serial")
	public class DsAssignmentErrorsDetectedEx extends Team744RunTimeEx {
		public DsAssignmentErrorsDetectedEx(String msg) {
			super(msg);
		}
	}
	@SuppressWarnings("serial")
	public class DsUnknownConnIndexFoundDuringAssignmentEx extends Team744RunTimeEx {
		public DsUnknownConnIndexFoundDuringAssignmentEx(String msg) {
			super(msg);
		}
	}
	@SuppressWarnings("serial")
	public class PopulatingInvalidDsDevicesEntryEx extends Team744RunTimeEx {
		public PopulatingInvalidDsDevicesEntryEx(String msg) {
			super(msg);
		}
	}
	@SuppressWarnings("serial")
	public class PopulatingInvalidDsControlsEntryEx extends Team744RunTimeEx {
		public PopulatingInvalidDsControlsEntryEx(String msg) {
			super(msg);
		}
	}
	@SuppressWarnings("serial")
	public class PopulatingInvalidRoDevicesEntryEx extends Team744RunTimeEx {
		public PopulatingInvalidRoDevicesEntryEx(String msg) {
			super(msg);
		}
	}
	@SuppressWarnings("serial")
	public class PopulatingInvalidRoControlsEntryEx extends Team744RunTimeEx {
		public PopulatingInvalidRoControlsEntryEx(String msg) {
			super(msg);
		}
	}
	@SuppressWarnings("serial")
	public class InvalidDsControlPassedToMethodEx extends Team744RunTimeEx {
		public InvalidDsControlPassedToMethodEx(String msg) {
			super(msg);
		}
	}
	@SuppressWarnings("serial")
	public class InvalidMethodForDsControlEx extends Team744RunTimeEx {
		public InvalidMethodForDsControlEx(String msg) {
			super(msg);
		}
	}
	@SuppressWarnings("serial")
	public class InvalidRoConnectionEntryEx extends Team744RunTimeEx {
		public InvalidRoConnectionEntryEx(String msg) {
			super(msg);
		}
	}
//	public class InconsistentParmsForRoConnectionEntryEx extends Team744RunTimeEx {
//		public InconsistentParmsForRoConnectionEntryEx(String msg) {
//			super(msg);
//		}
//	}
	@SuppressWarnings("serial")
	public class InvalidDsConnectionsEntryEx extends Team744RunTimeEx {
		public InvalidDsConnectionsEntryEx(String msg) {
			super(msg);
		}
	}
//	public class InconsistentParmsForDsConnectionEntryEx extends Team744RunTimeEx {
//		public InconsistentParmsForDsConnectionEntryEx(String msg) {
//			super(msg);
//		}
//	}

	@SuppressWarnings("serial")
	public class InvalidRoModulesEntryEx extends Team744RunTimeEx {
		public InvalidRoModulesEntryEx(String msg) {
			super(msg);
		}
	}

	@SuppressWarnings("serial")
	public class InvalidDsDevicesEntryEx extends Team744RunTimeEx {
		public InvalidDsDevicesEntryEx(String msg) {
			super(msg);
		}
	}
	
	@SuppressWarnings("serial")
	public class InvalidRoControlsEntryEx extends Team744RunTimeEx {
		public InvalidRoControlsEntryEx(String msg) {
			super(msg);
		}
	}
	
	@SuppressWarnings("serial")
	public class InvalidDsControlsEntryEx extends Team744RunTimeEx {
		public InvalidDsControlsEntryEx(String msg) {
			super(msg);
		}
	}
	
	/*
	 * To throw exception Aaa:
	 * 		throw TmExceptions.getInstance().new Aaa(...);
	 * To catch exception Aaa:
	 * 		catch(TmException.Aaa t)
	 */

	
	public static void reportExceptionOneLine(Throwable t, String msgPrefix) {
		P.println(msgPrefix + ": " + Arrays.toString(t.getStackTrace()));
	}
	public static void reportExceptionMultiLine(Throwable t, String msgPrefix) {
		P.println(msgPrefix + ": " + t.toString());
		t.printStackTrace();
	}

}
