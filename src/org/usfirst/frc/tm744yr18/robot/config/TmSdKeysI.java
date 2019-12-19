package org.usfirst.frc.tm744yr18.robot.config;

import t744opts.Tm744Opts;

public interface TmSdKeysI {
	
	public enum SdKeyStateE { ALWAYS_ENABLED, ENABLED, DISABLED, ALWAYS_DISABLED }
	
	public enum SdKeysE {
		KEY_CAMERA_FRONT_SELECTED(SdKeyStateE.DISABLED, "cam0"), 
		KEY_CAMERA_REAR_SELECTED(SdKeyStateE.DISABLED, "cam1"),

		KEY_COMPRESSOR_RUNNING(
				(Tm744Opts.isPowerCompressorFromRelay() ? SdKeyStateE.DISABLED : 
														SdKeyStateE.ENABLED),
				"compressorOn"),
		KEY_COMPRESSOR_SENSOR((Tm744Opts.isPowerCompressorFromRelay() ? SdKeyStateE.ENABLED : 
														SdKeyStateE.DISABLED),
				"compressorSensor"),
		KEY_COMPRESSOR_RELAY((Tm744Opts.isPowerCompressorFromRelay() ? SdKeyStateE.ENABLED : 
														SdKeyStateE.DISABLED),
				"compressorRelay"),
		
		KEY_VISION_UDP_MSG_COUNT(SdKeyStateE.ENABLED, "UdpMsgCnt"),
		KEY_VISION_UDP_NO_MSG_AVAIL_COUNT(SdKeyStateE.ENABLED, "UdpNoMsgAvailCnt"),
		KEY_VISION_UDP_ERROR_COUNT(SdKeyStateE.ENABLED, "UdpErrCnt"),
		KEY_VISION_UDP_QUERY_COUNT(SdKeyStateE.ENABLED, "UdpQueryCnt"),
		
		KEY_CMD_SHOW_DRVSTA_IO(SdKeyStateE.ALWAYS_DISABLED, "CmdShowDsIo"),
		KEY_CMD_SHOW_ROBOT_IO(SdKeyStateE.ALWAYS_DISABLED, "CmdShowRoIo"),

		KEY_CODE_BUILD_INFO(SdKeyStateE.ALWAYS_ENABLED, "BuildInfo"),

		KEY_DRVSTA_STATE(SdKeyStateE.ALWAYS_ENABLED, "DrvStaState"),
		
		KEY_MISC_FAKE_DIG_INPUTS(SdKeyStateE.ENABLED, "FakeDigInList"),
		KEY_MISC_FAKE_CAN_TALONS(SdKeyStateE.ENABLED, "FakeTalonList"),

		KEY_DRIVE_SPEED_ERR_RIGHT_NU(SdKeyStateE.DISABLED, "RobotSpeedErrRightNu"),
		KEY_DRIVE_SPEED_ERR_LEFT_NU(SdKeyStateE.DISABLED, "RobotSpeedErrLeftNu"),
		KEY_DRIVE_SPEED_ERR_RIGHT_RPM(SdKeyStateE.DISABLED, "RobotSpeedErrRightRpm"),
		KEY_DRIVE_SPEED_ERR_LEFT_RPM(SdKeyStateE.DISABLED, "RobotSpeedErrLeftRpm"),
		KEY_DRIVE_POS_ERR_RIGHT_FEET(SdKeyStateE.DISABLED, "RobotPositionErrRightFeet (ck units)"),
		KEY_DRIVE_POS_ERR_LEFT_FEET(SdKeyStateE.DISABLED, "RobotPositionErrLeftFeet (ck units)"),
		KEY_DRIVE_THETA_ERROR_DEGREES(SdKeyStateE.ENABLED, "RobotAngleError (deg)"),
		KEY_DRIVE_THETA_DESIRED(SdKeyStateE.ENABLED, "RobotAngleDesired (deg)"),
//		
//		KEY_DRIVE_MODE_LEFT(SdKeyStateE.DISABLED, "DrvCntlModeLeft"),
//		KEY_DRIVE_MODE_RIGHT(SdKeyStateE.DISABLED, "DrvCntlModeRight"),
//		KEY_DRIVE_OUTPUT_LEFT(SdKeyStateE.DISABLED, "DrvOutputLeft"),
//		KEY_DRIVE_OUTPUT_RIGHT(SdKeyStateE.DISABLED, "DrvOutputRight"),
		
		KEY_DRIVE_MOTOR_FRONT_LEFT_MODE(SdKeyStateE.ENABLED, "DrvMtrModeFrontLeft"),
		KEY_DRIVE_MOTOR_CENTER_LEFT_MODE(SdKeyStateE.ENABLED, "DrvMtrModeCenterLeft"),
        KEY_DRIVE_MOTOR_REAR_LEFT_MODE(SdKeyStateE.ENABLED, "DrvMtrModeRearLeft"),
        KEY_DRIVE_MOTOR_FRONT_RIGHT_MODE(SdKeyStateE.ENABLED, "DrvMtrModeFrontRight"),
        KEY_DRIVE_MOTOR_CENTER_RIGHT_MODE(SdKeyStateE.ENABLED, "DrvMtrModeCenterRight"),
        KEY_DRIVE_MOTOR_REAR_RIGHT_MODE(SdKeyStateE.ENABLED, "DrvMtrModeRearRight"),
        
		KEY_DRIVE_MOTOR_FRONT_LEFT_PERCENT_OUT(SdKeyStateE.ENABLED, "DrvMotorFrontLeftPo"),
		KEY_DRIVE_MOTOR_CENTER_LEFT_PERCENT_OUT(SdKeyStateE.ENABLED, "DrvMotorCenterLeftPo"),
        KEY_DRIVE_MOTOR_REAR_LEFT_PERCENT_OUT(SdKeyStateE.ENABLED, "DrvMotorRearLeftPo"),
        KEY_DRIVE_MOTOR_FRONT_RIGHT_PERCENT_OUT(SdKeyStateE.ENABLED, "DrvMotorFrontRightPo"),
        KEY_DRIVE_MOTOR_CENTER_RIGHT_PERCENT_OUT(SdKeyStateE.ENABLED, "DrvMotorCenterRightPo"),
        KEY_DRIVE_MOTOR_REAR_RIGHT_PERCENT_OUT(SdKeyStateE.ENABLED, "DrvMotorRearRightPo"),
        
		KEY_DRIVE_MOTOR_FRONT_LEFT_AMPS(SdKeyStateE.DISABLED, "DrvMotorFLAmps"),
		KEY_DRIVE_MOTOR_CENTER_LEFT_AMPS(SdKeyStateE.DISABLED, "DrvMotorCLAmps"),
        KEY_DRIVE_MOTOR_REAR_LEFT_AMPS(SdKeyStateE.DISABLED, "DrvMotorRLAmps"),
        KEY_DRIVE_MOTOR_FRONT_RIGHT_AMPS(SdKeyStateE.DISABLED, "DrvMotorFRAmps"),
        KEY_DRIVE_MOTOR_CENTER_RIGHT_AMPS(SdKeyStateE.DISABLED, "DrvMotorCRAmps"),
        KEY_DRIVE_MOTOR_REAR_RIGHT_AMPS(SdKeyStateE.DISABLED, "DrvMotorRRAmps"),
        
        KEY_DRIVE_ENCODER_LEFT(SdKeyStateE.ALWAYS_ENABLED, "DrvEncoderLeft"),
        KEY_DRIVE_ENCODER_RIGHT(SdKeyStateE.ALWAYS_ENABLED, "DrvEncoderRight"),
        
        KEY_DRIVE_GEARSHIFT_IS_HIGH(SdKeyStateE.ALWAYS_ENABLED, "DrvInHighGear"),
        
        KEY_DRIVE_GYRO_RATE(SdKeyStateE.ENABLED, "DrvGyroRate"),
        KEY_DRIVE_GYRO_TEMP(SdKeyStateE.ENABLED, "DrvGyroTemp"),
        KEY_DRIVE_GYRO_ANGLE(SdKeyStateE.ENABLED, "DrvGyroAngle"),
        KEY_DRIVE_GYRO_ERROR_CNT(SdKeyStateE.ENABLED, "DrvGyroErrors"),
        KEY_DRIVE_GYRO_RATE_RAW(SdKeyStateE.ENABLED, "DrvGyroRateRaw"),
        
//        KEY_DRIVE_GYRO_THETA_ERROR(SdKeyStateE.ENABLED, "DrvGyroThetaErr"),

        KEY_BAT_INIT(SdKeyStateE.ALWAYS_ENABLED, "batteryInit"),
        KEY_BAT_MIN(SdKeyStateE.ALWAYS_ENABLED, "batteryMin"),
        KEY_BAT_MIN_TIME(SdKeyStateE.ALWAYS_ENABLED, "batteryMinTime"),
        KEY_BAT_CUR(SdKeyStateE.ALWAYS_ENABLED, "batteryCur"), 
        
        KEY_AUTON_CHOOSER(SdKeyStateE.ALWAYS_ENABLED, "autonSel"),
        KEY_AUTON_ALG_NBR_STRING(SdKeyStateE.ALWAYS_ENABLED, "autonAlgNbr"),
        
//        KEY_ARM_OPERATING_MODE(SdKeyStateE.ENABLED, "ArmOpMode"),
        KEY_ARM_STAGE1_OPERATING_MODE(SdKeyStateE.ENABLED, "ArmStg1OpMode"),
        KEY_ARM_STAGE2_OPERATING_MODE(SdKeyStateE.DISABLED, "ArmStg2OpMode"),
        KEY_ARM_STAGE1_JOYSTICK_RDG(SdKeyStateE.ENABLED, "ArmMtrStg1Js"),
        KEY_ARM_STAGE2_JOYSTICK_RDG(SdKeyStateE.DISABLED, "ArmMtrStg2Js"),
        KEY_ARM_STAGE1_MTR_MODE(SdKeyStateE.ENABLED, "ArmMtrStg1Mode"),
        KEY_ARM_STAGE2_MTR_MODE(SdKeyStateE.DISABLED, "ArmMtrStg2Mode"),
        KEY_ARM_STAGE1_MTR_OUTPUT_PERCENT(SdKeyStateE.ENABLED, "ArmMtrStg1Po"),
        KEY_ARM_STAGE2_MTR_OUTPUT_PERCENT(SdKeyStateE.ENABLED, "ArmMtrStg2Po"),
        KEY_ARM_STAGE1_MTR_AMPS(SdKeyStateE.ENABLED, "ArmMtrStg1Amps"),
        KEY_ARM_STAGE2_MTR_AMPS(SdKeyStateE.ENABLED, "ArmMtrStg2Amps"),
        KEY_ARM_STAGE1_ENCODER_RDG(SdKeyStateE.ENABLED, "ArmMtrStg1Enc"),
        KEY_ARM_STAGE2_ENCODER_RDG(SdKeyStateE.DISABLED, "ArmMtrStg2Enc"),
        KEY_ARM_STAGE1_ENCODER_RDG_RAW(SdKeyStateE.ENABLED, "ArmMtrStg1EncRaw"),
        KEY_ARM_STAGE1_ENCODER_RDG_SNAPSHOT(SdKeyStateE.ENABLED, "ArmMtrStg1EncSnapshot"),
        KEY_ARM_STAGE1_ENCODER_RDG_INCHES(SdKeyStateE.ENABLED, "ArmMtrStg1EncInches"),
        KEY_ARM_STAGE1_ENCODER_TALON_TARGET(SdKeyStateE.ENABLED, "ArmMtrStg1EncTalonTarg"),
        KEY_ARM_STAGE2_ENCODER_RDG_RAW(SdKeyStateE.DISABLED, "ArmMtrStg2EncRaw"),
        KEY_ARM_STAGE1_CLOSED_LOOP_ERROR(SdKeyStateE.DISABLED, "ArmStg1PosCLpErr"),
        KEY_ARM_STAGE2_CLOSED_LOOP_ERROR(SdKeyStateE.DISABLED, "ArmStg2PosCLpErr"),
        KEY_ARM_STAGE1_CLOSED_LOOP_TARGET(SdKeyStateE.DISABLED, "ArmStg1PosCLpTarg"),
        KEY_ARM_STAGE2_CLOSED_LOOP_TARGET(SdKeyStateE.DISABLED, "ArmStg2PosCLpTarg"),
        KEY_ARM_STAGE1_CLOSED_LOOP_WORKING_TARGET(SdKeyStateE.DISABLED, "ArmStg1PosWrkTarg"),
        KEY_ARM_STAGE2_CLOSED_LOOP_WORKING_TARGET(SdKeyStateE.DISABLED, "ArmStg2PosWrkTarg"),
        KEY_ARM_STAGE1_CLOSED_LOOP_REQUESTED_TARGET(SdKeyStateE.DISABLED, "ArmStg1PosReqTarg"),
        KEY_ARM_STAGE2_CLOSED_LOOP_REQUESTED_TARGET(SdKeyStateE.DISABLED, "ArmStg2PosReqTarg"),
        KEY_ARM_STAGE1_OP_MODE(SdKeyStateE.ENABLED, "ArmStg1OpMode"),
        KEY_ARM_STAGE2_OP_MODE(SdKeyStateE.DISABLED, "ArmStg2OpMode"),
        KEY_ARM_CLAW_GRABBING(SdKeyStateE.ENABLED, "ArmGrabbing"),
        KEY_ARM_CLAW_RELEASING(SdKeyStateE.ENABLED, "ArmReleasing"),
        KEY_ARM_CLAW_MTR_LEFT_PERCENT_OUT(SdKeyStateE.ENABLED, "ArmClawMtrLPo"),
        KEY_ARM_CLAW_MTR_RIGHT_PERCENT_OUT(SdKeyStateE.ENABLED, "ArmClawMtrRPo"),
        KEY_ARM_SD_CMD_SERVO_TOP(SdKeyStateE.DISABLED, "CmdArmServoTop"),
        KEY_ARM_SD_CMD_SERVO_BOT(SdKeyStateE.DISABLED, "CmdArmServoBot"),
        KEY_ARM_SD_CMD_SERVO_SWITCH(SdKeyStateE.DISABLED, "CmdArmServoSw"),
        KEY_ARM_SD_CMD_SERVO_SCALE_HIGH(SdKeyStateE.DISABLED, "CmdArmServoSclH"),
        KEY_ARM_SD_CMD_SERVO_SCALE_LOW(SdKeyStateE.DISABLED, "CmdArmServoSclL"),
        KEY_ARM_SD_CMD_RUN_WITH_XBOX_JOYSTICKS(SdKeyStateE.DISABLED, "CmdArmXboxJs"),
        KEY_ARM_STAGE1_AT_TOP(SdKeyStateE.ENABLED, "ArmStg1Top"),
        KEY_ARM_STAGE1_AT_BOTTOM(SdKeyStateE.ENABLED, "ArmStg1Bot"),
        KEY_ARM_STAGE2_AT_TOP(SdKeyStateE.DISABLED, "ArmStg2Top"),
        KEY_ARM_STAGE2_AT_BOTTOM(SdKeyStateE.DISABLED, "ArmStg2Bot"),
        KEY_ARM_LIMIT_SWITCHES(SdKeyStateE.DISABLED, "ArmLmtSw.T1.B1.T2.B2"),
        KEY_ARM_STAGE1_LIMIT_SWITCH_TOP(SdKeyStateE.DISABLED, "ArmLmtStg1Top"),
        KEY_ARM_STAGE1_LIMIT_SWITCH_BOT(SdKeyStateE.ENABLED, "ArmLmtStg1Bot"),
        KEY_ARM_STAGE2_LIMIT_SWITCH_TOP(SdKeyStateE.DISABLED, "ArmLmtStg2Top"),
        KEY_ARM_STAGE2_LIMIT_SWITCH_BOT(SdKeyStateE.DISABLED, "ArmLmtStg2Bot"),
        
        KEY_ARM_FAKE_STUFF_MSG(SdKeyStateE.ENABLED, "ArmFakeStuff"),
        
        KEY_ARM_ENCODER_STATS_MSG(SdKeyStateE.ENABLED, "ArmEncoderStats"),
        KEY_ARM_ENCODER_OVERRIDE(SdKeyStateE.ENABLED, "ArmEncoderOverride"),
        
        KEY_ARM_STAGE1_ALLOWED_DIR(SdKeyStateE.ENABLED, "ArmStg1AllowedDir"),
        KEY_ARM_STAGE2_ALLOWED_DIR(SdKeyStateE.DISABLED, "ArmStg2AllowedDir"),
        
        KEY_GRABBER_GRABBING(SdKeyStateE.ENABLED, "GrabberGrabbing"),
        KEY_GRABBER_RELEASING(SdKeyStateE.ENABLED, "GrabberReleasing"),
        KEY_GRABBER_CLAMPED(SdKeyStateE.ENABLED, "GrabberClamped"),
        KEY_GRABBER_UNCLAMPED(SdKeyStateE.ENABLED, "GrabberUnclamped"),
        KEY_GRABBER_LIFT_UP(SdKeyStateE.ENABLED, "GrabberUp"),
        KEY_GRABBER_UP_FULL_LIMIT_SWITCH(SdKeyStateE.ENABLED, "GrabberUpFullLmtSw"),
        KEY_GRABBER_WEDGE_DEPLOYED(SdKeyStateE.ENABLED, "WedgeDeployed"),
        KEY_GRABBER_WEDGE_LIMIT_SWITCH(SdKeyStateE.ENABLED, "NoWedgeLmtSw"),
        KEY_GRABBER_MTR_LEFT_PERCENTOUT(SdKeyStateE.ENABLED, "GrabberMtrLtPo"),
        KEY_GRABBER_MTR_RIGHT_PERCENTOUT(SdKeyStateE.ENABLED, "GrabberMtrRtPo"),
        
        
        KEY_LEFT_ENCODER_RAW_SPEED(SdKeyStateE.DISABLED, "LeftEncoderRawSpeed"),
        KEY_RIGHT_ENCODER_RAW_SPEED(SdKeyStateE.DISABLED, "RightEncoderRawSpeed"),
        KEY_LEFT_ENCODER_SPEED_FEET_PER_SECOND(SdKeyStateE.DISABLED, "LeftEncoderFPS"),
        KEY_RIGHT_ENCODER_SPEED_FEET_PER_SECOND(SdKeyStateE.DISABLED, "RightEncoderFPS"),
        KEY_LEFT_ENCODER_DISTANCE_RAW(SdKeyStateE.DISABLED, "LeftEncoderRawDistance"),
        KEY_RIGHT_ENCODER_DISTANCE_RAW(SdKeyStateE.DISABLED, "RightEncoderRawDistance"),
        KEY_LEFT_ENCODER_DISTANCE_FT(SdKeyStateE.DISABLED, "LeftEncoderFtDistance"),
        KEY_RIGHT_ENCODER_DISTANCE_FT(SdKeyStateE.DISABLED, "RightEncoderFtDistance"),
        
//duplicate        KEY_GYRO_READING(SdKeyStateE.ENABLED, "GyroInDeg"),
        
        KEY_LEFT_ENCODER_CLOSED_LOOP_ERROR_RAW(SdKeyStateE.DISABLED, "LeftEncoderCLE-Raw"),
        KEY_RIGHT_ENCODER_CLOSED_LOOP_ERROR_RAW(SdKeyStateE.DISABLED, "RightEncoderCLE-Raw"),
        KEY_LEFT_ENCODER_CLOSED_LOOP_ERROR_FT_PER_SECOND(SdKeyStateE.DISABLED, "LeftEncoderCLE-FPS"),
        KEY_RIGHT_ENCODER_CLOSED_LOOP_ERROR_FT_PER_SECOND(SdKeyStateE.DISABLED, "RightEncoderCLE-FPS"),

        KEY_NAVX_IMU_IS_CONNECTED(SdKeyStateE.ENABLED, "IMU_Connected"),
        KEY_NAVX_IMU_IS_CALIBRAATING(SdKeyStateE.ENABLED, "IMU_IsCalibrating"),
        KEY_NAVX_IMU_YAW(SdKeyStateE.ENABLED, "IMU_Yaw"),
        KEY_NAVX_IMU_PITCH(SdKeyStateE.ENABLED, "IMU_Pitch"),
        KEY_NAVX_IMU_ROLL(SdKeyStateE.ENABLED, "IMU_Roll"),
        KEY_NAVX_IMU_TOTAL_YAW(SdKeyStateE.ENABLED, "IMU_TotalYaw-WPIs-getAngle()"),
        

        NO_KEY(SdKeyStateE.ALWAYS_DISABLED, "noKey")
        ;
        
//        private F eFlags; //flags to pass to TmSdDbgSD methods to control when/whether to send to SmartDashboard
		private SdKeyStateE eKeyState;
        private String eKey; //key to use when sending to SmartDashboard
        
        private SdKeysE(SdKeyStateE initialState, String key) {
        	eKeyState = initialState;
        	eKey = key;
        }
        public SdKeyStateE getState() { return eKeyState; }
        public String getKey() { return eKey; }
        public boolean isEnabled() { 
        	boolean ans;
        	switch(eKeyState) {
			case ALWAYS_ENABLED:
			case ENABLED:
				ans = true;
				break;
			case DISABLED:
			case ALWAYS_DISABLED:
			default:
				ans = false;
				break;        	
        	}
        	return ans;
        }
        
        /**
         * allow code to enable/disable keys on the fly, from commands, etc.
         * @param newState
         */
        public synchronized void setState(SdKeyStateE newState) {
        	switch(eKeyState) {
			case DISABLED:
			case ENABLED:
				eKeyState = newState;
				break;
			case ALWAYS_ENABLED:
			case ALWAYS_DISABLED:
			default:
				break;
        	
        	}
        }
	}
	
	
    public class LwSubSysName
    {
        public static final String SS_ROBOT_DRIVE = "RobotDrive";
        public static final String SS_DRV_GEARSHIFTER = "DrvGearShifter";
        public static final String SS_BATTERY = "Battery";
        public static final String SS_CAMERA_LEDS = "CameraLeds";
        public static final String SS_SHOOTER = "Shooter";
        public static final String SS_CAMERA = "Camera";
//        public static final String SS_ARM = "Arm";
        public static final String SS_INTAKE = "Intake";
    }
    
    public class LwItemNames
    {
        public static final String CAMERA_LEDS = "cameraLeds";

        public static final String CAMERA_ = "camera__";
        
        public static final String DRIVE_MOTOR_LEFT_FRONT = "L Fr motor";
        public static final String DRIVE_MOTOR_LEFT_REAR = "L Rr motor";
        public static final String DRIVE_MOTOR_RIGHT_FRONT = "R Fr motor";
        public static final String DRIVE_MOTOR_RIGHT_REAR = "R Rr motor";
        
        public static final String DRIVE_ENCODER_RIGHT = "Right encoder";
        public static final String DRIVE_ENCODER_LEFT = "Left encoder";
        public static final String DRIVE_ENCODER_RIGHT_INPUT_A = "Rt enc. input A";
        public static final String DRIVE_ENCODER_RIGHT_INPUT_B = "Rt enc. input B";
        public static final String DRIVE_ENCODER_LEFT_INPUT_A = "Lft enc. input A";
        public static final String DRIVE_ENCODER_LEFT_INPUT_B = "Lft enc. input B";
        
        public static final String DRV_GEARSHIFTER = "DrvGear";
        
        public static final String SHOOTER_MOTOR = "ShooterMtr";
        public static final String SHOOTER_LAUNCHER = "ShooterLnchr";
        public static final String SHOOTER_SAIL = "ShooterSail";
        public static final String SHOOTER_ENCODER = "ShooterEnc";
        public static final String SHOOTER_BALL_SENSOR = "ShooterBallSns";
        
//        public static final String ARM_MOTOR = "ArmMtr";
//        public static final String ARM_ENCODER = "ArmMagEnc";
//        public static final String ARM_VERTICAL_LIMIT_SWITCH = "ArmVertSwitch";
//        public static final String ARM_OPTICAL_ENCODER = "ArmOptEnc";
//        public static final String ARM_OPTICAL_ENCODER_INPUT_A = "ArmOptEncInpA";
//        public static final String ARM_OPTICAL_ENCODER_INPUT_B = "ArmOptEncInpB";
        
        public static final String INTAKE_MOTOR = "IntakeMtr";
        public static final String INTAKE_MOTOR_AUX = "IntakeMtrAux";
    }	

}
