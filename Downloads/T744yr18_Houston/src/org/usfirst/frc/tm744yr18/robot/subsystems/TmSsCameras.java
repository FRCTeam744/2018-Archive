package org.usfirst.frc.tm744yr18.robot.subsystems;

import org.usfirst.frc.tm744yr18.robot.config.TmIpAddrInfoI.IpAddrInfoE;
//import org.opencv.core.Mat;
//import org.opencv.core.Point;
//import org.opencv.core.Scalar;
//import org.opencv.imgproc.Imgproc;
import org.usfirst.frc.tm744yr18.robot.config.TmSdKeysI.SdKeysE;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmStdSubsystemI;
import org.usfirst.frc.tm744yr18.robot.interfaces.TmToolsI;

//import edu.wpi.cscore.CvSink;
//import edu.wpi.cscore.CvSource;
//import edu.wpi.cscore.UsbCamera;
import edu.wpi.first.wpilibj.CameraServer;
//import edu.wpi.first.wpilibj.Timer;
import t744opts.Tm744Opts;

//getting errors:
//	OpenCV Error: Assertion failed (!fixedSize()) in release, file /var/lib/jenkins/workspace/OpenCV-roborio/modules/core/src/matrix.cpp, line 2570
//	terminate called after throwing an instance of 'cv::Exception'
//	  what():  /var/lib/jenkins/workspace/OpenCV-roborio/modules/core/src/matrix.cpp:2570: error: (-215) !fixedSize() in function release

public class TmSsCameras implements TmStdSubsystemI, TmToolsI {

	/*---------------------------------------------------------
	 * getInstance stuff                                      
	 *---------------------------------------------------------*/
	/** 
	 * handle making the singleton instance of this class and giving
	 * others access to it
	 */
	private static TmSsCameras m_instance;

	public static synchronized TmSsCameras getInstance() {
		if (m_instance == null) {
			m_instance = new TmSsCameras();
		}
		return m_instance;
	}

	private TmSsCameras() {
//		setCameraSelection(CameraSelectorE.FRONT_GP);
	}
	/*----------------end of getInstance stuff----------------*/

	/**
	 * used by commands, etc. to select the camera to be viewed
	 * @author JudiA
	 *
	 */
	public enum CameraSelectorE { 
//		FRONT_GP("cam0", 0, SdKeysE.KEY_CAMERA_FRONT_SELECTED, "http://roborio-744-frc.local:1181/?action=stream", "/dev/video0"), 
//		REAR_SH("cam1", 1, SdKeysE.KEY_CAMERA_REAR_SELECTED, "http://roborio-744-frc.local:1182/?action=stream", "/dev/video1"),
		FRONT_GP("cam0", 0, SdKeysE.KEY_CAMERA_FRONT_SELECTED, IpAddrInfoE.USB_CAMERA_0.getUrl(), IpAddrInfoE.USB_CAMERA_0.getLinuxDev()), 
		REAR_SH("cam1", 1, SdKeysE.KEY_CAMERA_REAR_SELECTED, IpAddrInfoE.USB_CAMERA_1.getUrl(), IpAddrInfoE.USB_CAMERA_1.getLinuxDev()),
		;
		
		public boolean isFrontCameraSelected() { return this.equals(CameraSelectorE.FRONT_GP); }
		public boolean isRearCameraSelected() { return this.equals(CameraSelectorE.REAR_SH); }
		public boolean isSelected() { return this.equals(m_cameraSelection); }
		private final String eCamName; //from roboRIO webpage
		private final int eCamDevNbr;
		private final SdKeysE eSdKey;
		private final String eCamMjpegUrl;
		private final String eCamVideoUrl;
		private CameraSelectorE(String camName, int camDevNbr, SdKeysE sdKeyForIsSelected, String mjpegUrl, String videoUrl) {
			eCamName = camName;
			eCamDevNbr = camDevNbr;
			eSdKey = sdKeyForIsSelected;
			eCamMjpegUrl = mjpegUrl;
			eCamVideoUrl = videoUrl;
		}
		public String getCameraName() { return eCamName; }
		public int getCameraDevNbr() { return eCamDevNbr; }
	}
	private static CameraSelectorE m_cameraSelection;
//	//helper methods
//	public static boolean isFrontCameraSelected() { return m_cameraSelection.isFrontCameraSelected(); }
//	public static boolean isRearCameraSelected() { return m_cameraSelection.isRearCameraSelected(); }	
//	public static void setCameraSelection(CameraSelectorE camSel) {
//		m_cameraSelection = camSel;
//		P.println("Camera selected: " + camSel.name());
//		for(CameraSelectorE c : CameraSelectorE.values()) {
//			TmSdMgr.putBoolean(c.eSdKey, c.isSelected());
//		}
//	}
//
//	private enum CameraResolutionE {
//		k320x240(320, 240),
//		k160x120(160,120),
//		;
//		public final int W;
//		public final int H;
//		private CameraResolutionE(int width, int height) {
//			W = width; H = height;
//		}		
//	}
//	
//	private static class Cnst {
//		protected static final double AUTO_CAPTURE_DELAY_SECS = 0.050;
//		protected static final double CONFIGURE_DELAY_SECS = 0.025;
//		protected static final double WHILE_LOOP_DELAY_SECS = 0.045;
//		protected static final int FRAMES_PER_SEC = 15;
//		protected static final CameraResolutionE RES = CameraResolutionE.k160x120;
//		protected static final String SWITCHER_STREAM_NAME = "Switcher"; //the name to select in Smartdashboard 
//	}
//
//
//
////	Thread visionThreadFront = null;
////	Thread visionThreadRear = null;
	
	private void optionalSimpleCameraAccess(boolean isInstalled, String camName, int camUsbNdx) {
		if(isInstalled) {
			CameraServer.getInstance().startAutomaticCapture(camName, camUsbNdx);
			P.println("CAMERA " + camName  + " connected");
		} else {
			P.println("Per Opts/Preferences, " + camName + " is NOT installed");
		}		
	}
	
//	Thread m_switcher2Thread;
////	CvSink cvSinkFront = null;
////	CvSink cvSinkRear = null;
	
	@Override
	public void sssDoRobotInit() {
		if(true) {
			optionalSimpleCameraAccess(Tm744Opts.isUsbCam0Installed(), 
					CameraSelectorE.FRONT_GP.getCameraName(), CameraSelectorE.FRONT_GP.getCameraDevNbr()); //cameraNameFront, cameraFrontNdx);
			optionalSimpleCameraAccess(Tm744Opts.isUsbCam1Installed(), 
					CameraSelectorE.REAR_SH.getCameraName(), CameraSelectorE.REAR_SH.getCameraDevNbr()); // cameraNameRear, cameraRearNdx);
		} 
//		else if(Tm17Opts.isUsbCam0Installed() || Tm17Opts.isUsbCam1Installed()) {
////  originally got the following error, not caught by our try/catch blocks:		
////		OpenCV Error: Assertion failed (!fixedSize()) in release, file /var/lib/jenkins/workspace/OpenCV-roborio/modules/core/src/matrix.cpp, line 2570
////		terminate called after throwing an instance of 'cv::Exception'
////		  what():  /var/lib/jenkins/workspace/OpenCV-roborio/modules/core/src/matrix.cpp:2570: error: (-215) !fixedSize() in function release
////  adding the delays eliminated the problem.  Can now stream Switcher + 2 M-JPEG thumbnail views (via http://roborio-744-frc.local:1181/?action=stream.)
//			m_switcher2Thread = new Thread(() -> {
//				UsbCamera frontCamera = null;
//				UsbCamera rearCamera = null;
//				CvSink cvSinkFront = null;
//				CvSink cvSinkRear = null;
//				CvSource outputStream = null;
//				Mat image = null;
//				
//				try {
//					if(Tm17Opts.isUsbCam0Installed()) {
//						frontCamera = CameraServer.getInstance().startAutomaticCapture(CameraSelectorE.FRONT_GP.getCameraName(), 
//								CameraSelectorE.FRONT_GP.getCameraDevNbr());
//						Timer.delay(Cnst.AUTO_CAPTURE_DELAY_SECS);
//						frontCamera.setResolution(Cnst.RES.W, Cnst.RES.H);
//						Timer.delay(Cnst.CONFIGURE_DELAY_SECS);
//						frontCamera.setFPS(Cnst.FRAMES_PER_SEC);
//						Timer.delay(Cnst.CONFIGURE_DELAY_SECS);
//					}
//
//					if(Tm17Opts.isUsbCam1Installed()) {
//						rearCamera = CameraServer.getInstance().startAutomaticCapture(CameraSelectorE.REAR_SH.getCameraName(), 
//								CameraSelectorE.REAR_SH.getCameraDevNbr());
//						Timer.delay(Cnst.AUTO_CAPTURE_DELAY_SECS);
//						rearCamera.setResolution(Cnst.RES.W, Cnst.RES.H);
//						Timer.delay(Cnst.CONFIGURE_DELAY_SECS);
//						rearCamera.setFPS(Cnst.FRAMES_PER_SEC);
//						Timer.delay(Cnst.CONFIGURE_DELAY_SECS);
//					}
//					if(Tm17Opts.isUsbCam0Installed()) { cvSinkFront = CameraServer.getInstance().getVideo(frontCamera); }
//					if(Tm17Opts.isUsbCam1Installed()) { cvSinkRear = CameraServer.getInstance().getVideo(rearCamera); }
//					
//					outputStream = CameraServer.getInstance().putVideo(Cnst.SWITCHER_STREAM_NAME, Cnst.RES.W, Cnst.RES.H);
//
//					if(Tm17Opts.isUsbCam0Installed()) {cvSinkFront.setEnabled(false);}
//					if(Tm17Opts.isUsbCam1Installed()) {cvSinkRear.setEnabled(false);}
//
//					image = new Mat();
//				} catch(Throwable t) {
//					//never gets here when cv exception thrown... :(
//					TmExceptions.reportExceptionMultiLine(t, "exception setting up camera thread");
////					System.exit(-3);
//				}
//
//				while(!Thread.interrupted()) {
//					boolean sel = m_cameraSelection.isFrontCameraSelected();
//					try {
//						if( sel /*switcher button logic*/){
//							if(Tm17Opts.isUsbCam0Installed()) {
//								if(Tm17Opts.isUsbCam1Installed()) { cvSinkRear.setEnabled(false); }
//								Timer.delay(Cnst.WHILE_LOOP_DELAY_SECS);
//								cvSinkFront.setEnabled(true);
//								Timer.delay(Cnst.WHILE_LOOP_DELAY_SECS);
//								cvSinkFront.grabFrame(image);
//								Timer.delay(Cnst.WHILE_LOOP_DELAY_SECS);
//								outputStream.putFrame(image);
//							}
//						} else{
//							if(Tm17Opts.isUsbCam1Installed()) {
//								if(Tm17Opts.isUsbCam0Installed()) { cvSinkFront.setEnabled(false); }
//								Timer.delay(Cnst.WHILE_LOOP_DELAY_SECS);
//								cvSinkRear.setEnabled(true);
//								Timer.delay(Cnst.WHILE_LOOP_DELAY_SECS);
//								cvSinkRear.grabFrame(image);
//								Timer.delay(Cnst.WHILE_LOOP_DELAY_SECS);
//								outputStream.putFrame(image);
//							}
//						}
//					} catch(Throwable t) {
//						//get exceptions reported from the bowels of FRC/OpenCV camera code, but they're not caught here
//						TmExceptions.reportExceptionMultiLine(t, "exception running camera thread");
//						System.exit(-4);
//					}
//				}
//			});//.start();
//			m_switcher2Thread.setDaemon(true);
//			m_switcher2Thread.start();
//		}
	}


	@Override
	public void sssDoInstantiate() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void sssDoDisabledInit() {
		// TODO Auto-generated method stub

	}

	@Override
	public void sssDoAutonomousInit() {
		// TODO Auto-generated method stub

	}

	@Override
	public void sssDoTeleopInit() {
		// TODO Auto-generated method stub

	}

	@Override
	public void sssDoLwTestInit() {
		// TODO Auto-generated method stub

	}

	@Override
	public void sssDoRobotPeriodic() {
		// TODO Auto-generated method stub

	}

	@Override
	public void sssDoDisabledPeriodic() {
		// TODO Auto-generated method stub

	}

	@Override
	public void sssDoAutonomousPeriodic() {
		// TODO Auto-generated method stub

	}

	@Override
	public void sssDoTeleopPeriodic() {
		// TODO Auto-generated method stub

	}

	@Override
	public void sssDoLwTestPeriodic() {
		// TODO Auto-generated method stub

	}
	
//	public enum VisionOutDefE {
//		FRONT(Cnst.RES.W, Cnst.RES.H, 10, new Point(50, 50), new Point(200, 200), new Scalar(255, 255, 255), 3),
//		REAR(Cnst.RES.W, Cnst.RES.H, 10, new Point(75, 75), new Point(175, 175), new Scalar(255, 255, 255), 3),
//		NONE(Cnst.RES.W, Cnst.RES.H, 10, null, null, null, 0),
//		;
////		Imgproc.rectangle(mat, new Point(100, 100), new Point(400, 400),
////		new Scalar(255, 255, 255), 5);
//		
//		int	eImgWidth;
//		int eImgHeight;
//		int eFPS;
//		Point eTopL;
//		Point eLowerR;
//		Scalar eColor;
//		int eLineWidth;
//		private VisionOutDefE(int width, int height, int fps, Point topL, Point lowerR, Scalar color, int lineWidth) {
//			eImgWidth = width;
//			eImgHeight = height;
//			eFPS = fps;
//			eTopL = topL;
//			eLowerR = lowerR;
//			eColor = color;
//			eLineWidth = lineWidth;			
//		}
//	}

	@Override
	public boolean isFakeableItem() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void configAsFake() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean isFake() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void initDefaultCommand() {
		// TODO Auto-generated method stub
		
	}

//	Thread makeVisionThread(String cameraName, int usbPort) {
////		Imgproc.rectangle(mat, new Point(50, 50), new Point(200, 200),
////				new Scalar(255, 255, 255), 3);
//		return makeVisionThread(cameraName, usbPort, VisionOutDefE.NONE); //null, null, null, 0);
//	}
//	Thread makeVisionThread(String cameraName, int usbPort, VisionOutDefE visionOutputDef) { //Point topL, Point lowerR, Scalar color, int width) {
//		VisionOutDefE l_vOutDef = visionOutputDef;
////		Point l_topL = topL;
////		Point l_lowerR = lowerR;
////		Scalar l_color = color;
////		int l_width = width;
//		
//		Thread visionThread = new Thread(() -> {
//			// Get the UsbCamera from CameraServer
//			UsbCamera camera = CameraServer.getInstance().startAutomaticCapture(cameraName, usbPort);
//			// Set the resolution
//			camera.setResolution(l_vOutDef.eImgWidth, l_vOutDef.eImgHeight); //320, 240); //640, 480);
//			camera.setFPS(l_vOutDef.eFPS); //10);
//
//			// Get a CvSink. This will capture Mats from the camera
//			CvSink cvSink = CameraServer.getInstance().getVideo();
//			// Setup a CvSource. This will send images back to the Dashboard
//			String outputName = cameraName + ((l_vOutDef.eTopL==null) ? "Plain" : "Rectangle"); //l_topL==null) ? "Plain" : "Rectangle");
//			CvSource outputStream = CameraServer.getInstance().putVideo(outputName, l_vOutDef.eImgWidth, l_vOutDef.eImgHeight); //320, 240); //640, 480);
//
//			// Mats are very memory expensive. Lets reuse this Mat.
//			Mat mat = new Mat();
//
//			// This cannot be 'true'. The program will never exit if it is. This
//			// lets the robot stop this thread when restarting robot code or
//			// deploying.
//			while (!Thread.interrupted()) {
//				// Tell the CvSink to grab a frame from the camera and put it
//				// in the source mat.  If there is an error notify the output.
//				if (cvSink.grabFrame(mat) == 0) {
//					// Send the output the error.
//					outputStream.notifyError(cvSink.getError());
//					// skip the rest of the current iteration
//					continue;
//				}
//				// Put a rectangle on the image
////				Imgproc.rectangle(mat, new Point(100, 100), new Point(400, 400),
////						new Scalar(255, 255, 255), 5);
//				if( ! (l_vOutDef.eTopL==null)) { //l_topL==null)) {
//					Imgproc.rectangle(mat, l_vOutDef.eTopL, l_vOutDef.eLowerR, l_vOutDef.eColor, l_vOutDef.eLineWidth); //l_topL, l_lowerR, l_color, l_width);
//				}
//				// Give the output stream a new image to display
//				outputStream.putFrame(mat);
//			}
//		});
//		visionThread.setDaemon(true);
//		visionThread.start();
//
//		return visionThread;
//	}
//
//	Thread makeVisionThreadExample(String cameraName, int usbPort) {
//
//		Thread visionThread = new Thread(() -> {
//			// Get the UsbCamera from CameraServer
//			UsbCamera camera = CameraServer.getInstance().startAutomaticCapture(cameraName, usbPort);
//			// Set the resolution
//			camera.setResolution(640, 480);
//
//			// Get a CvSink. This will capture Mats from the camera
//			CvSink cvSink = CameraServer.getInstance().getVideo();
//			// Setup a CvSource. This will send images back to the Dashboard
//			CvSource outputStream = CameraServer.getInstance().putVideo("Rectangle", 640, 480);
//
//			// Mats are very memory expensive. Lets reuse this Mat.
//			Mat mat = new Mat();
//
//			// This cannot be 'true'. The program will never exit if it is. This
//			// lets the robot stop this thread when restarting robot code or
//			// deploying.
//			while (!Thread.interrupted()) {
//				// Tell the CvSink to grab a frame from the camera and put it
//				// in the source mat.  If there is an error notify the output.
//				if (cvSink.grabFrame(mat) == 0) {
//					// Send the output the error.
//					outputStream.notifyError(cvSink.getError());
//					// skip the rest of the current iteration
//					continue;
//				}
//				// Put a rectangle on the image
//				Imgproc.rectangle(mat, new Point(100, 100), new Point(400, 400),
//						new Scalar(255, 255, 255), 5);
//				// Give the output stream a new image to display
//				outputStream.putFrame(mat);
//			}
//		});
//		visionThread.setDaemon(true);
//		visionThread.start();
//
//		return visionThread;
//	}

}
