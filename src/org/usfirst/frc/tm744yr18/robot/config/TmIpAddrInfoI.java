package org.usfirst.frc.tm744yr18.robot.config;

public interface TmIpAddrInfoI {
	public static class Cnst {
		public static final String NO_URL = "";
		public static final String NO_LINUX_DEV = "";
		public static final int NO_PORT = -1;
		public static final String DEFAULT_NET_MASK = "255.255.255.0";
		public static final String DEFAULT_RADIO_IP_ADDR = "10.7.44.1";
		public static final String DEFAULT_ROBO_RIO_WIFI_IP_ADDR = "10.7.44.2";
		public static final String DEFAULT_GATEWAY = DEFAULT_RADIO_IP_ADDR;
		public static final String NO_USERID = "";
		public static final String NO_PASSWORD = "";
	}
	public class IpAddrInfo {
		
		public String url = Cnst.NO_URL;
		public String ipAddr = null;
		public int port = Cnst.NO_PORT;
		public String netMask = Cnst.DEFAULT_NET_MASK;
		public String gateway = Cnst.DEFAULT_RADIO_IP_ADDR;
		public String userid = "";
		public String password = "";
		public String linuxDev = ""; //used for USB cameras
		
		public IpAddrInfo(String ipAddrArg) {
			this(Cnst.NO_URL, ipAddrArg, Cnst.NO_PORT, Cnst.DEFAULT_NET_MASK, Cnst.DEFAULT_GATEWAY, 
					Cnst.NO_USERID, Cnst.NO_PASSWORD, Cnst.NO_LINUX_DEV);
		}
		public IpAddrInfo(String urlArg, String ipAddrArg) {
			this(urlArg, ipAddrArg, Cnst.NO_PORT, Cnst.DEFAULT_NET_MASK, Cnst.DEFAULT_GATEWAY, 
					Cnst.NO_USERID, Cnst.NO_PASSWORD, Cnst.NO_LINUX_DEV);
		}
		public IpAddrInfo(String urlArg, String ipAddrArg, int portArg) {
			this(urlArg, ipAddrArg, portArg, Cnst.DEFAULT_NET_MASK, Cnst.DEFAULT_GATEWAY, 
					Cnst.NO_USERID, Cnst.NO_PASSWORD, Cnst.NO_LINUX_DEV);
		}
//		public IpAddrInfo(String urlArg, String ipAddrArg, int portArg, String netMaskArg, String gatewayArg) {
//			this(urlArg, ipAddrArg, portArg, netMaskArg, gatewayArg, "", "", "");
//		}
		public IpAddrInfo(String urlArg, String ipAddrArg, int portArg, String netMaskArg, String gatewayArg, 
				String useridArg, String passwordArg, String linuxDevArg) {
			url = urlArg;
			ipAddr = ipAddrArg;
			port = portArg;
			netMask = netMaskArg;
			gateway = gatewayArg;
			userid = useridArg;
			password = passwordArg;
			linuxDev = linuxDevArg;
		}
	}
	
	//code only uses IP addresses and (maybe) a port.  the rest of the info here is primarily for documentation
	public static enum IpAddrInfoE {
		RADIO("", Cnst.DEFAULT_RADIO_IP_ADDR, Cnst.NO_PORT),
		ROBO_RIO_WIFI("http://roborio-744-frc.local/", Cnst.DEFAULT_ROBO_RIO_WIFI_IP_ADDR, Cnst.NO_PORT, 
				"admin", "", Cnst.NO_LINUX_DEV),
		ROBO_RIO_USB("http://roborio-744-frc.local/", "172.22.11.2", Cnst.NO_PORT, 
				"admin", "", Cnst.NO_LINUX_DEV),
		LIMELIGHT_CFG("http://limelight.local:5801", "10.7.44.11", 5801),
		LIMELIGHT_STREAM("http://limelight.local:5800", "10.7.44.11", 5800),
		RASPBERRY_PI_UDP(Cnst.NO_URL, "10.7.44.13", 5005),
		USB_CAMERA_0("http://roborio-744-frc.local:1181/?action=stream", Cnst.DEFAULT_ROBO_RIO_WIFI_IP_ADDR/*?*/, 1181, 
				Cnst.NO_USERID, Cnst.NO_PASSWORD, "/dev/video0"),
		USB_CAMERA_1("http://roborio-744-frc.local:1182/?action=stream", Cnst.DEFAULT_ROBO_RIO_WIFI_IP_ADDR/*?*/, 1182, 
				Cnst.NO_USERID, Cnst.NO_PASSWORD, "/dev/video1"),
		;
		
		public final IpAddrInfo eIpAddrInfo;
		
		private IpAddrInfoE(String url, String ipAddr, int port) {
			eIpAddrInfo = new IpAddrInfo(url, ipAddr, port, Cnst.NO_USERID, Cnst.NO_PASSWORD, Cnst.NO_LINUX_DEV,
													Cnst.DEFAULT_NET_MASK, Cnst.DEFAULT_GATEWAY);
		}
		private IpAddrInfoE(String url, String ipAddr, int port, String userid, String password, String linuxDev) {
			eIpAddrInfo = new IpAddrInfo(url, ipAddr, port, userid, password, linuxDev, 
					Cnst.DEFAULT_NET_MASK, Cnst.DEFAULT_GATEWAY);
		}
		private IpAddrInfoE(String url, String ipAddr, int port, String userid, String password, String linuxDev, 
																		String netMask, String gateway) {
			eIpAddrInfo = new IpAddrInfo(url, ipAddr, port, userid, password, linuxDev, netMask, gateway);
		}
		
		public String getIpAddr() { return eIpAddrInfo.ipAddr; }
		public int getPort() { return eIpAddrInfo.port; }
		public String getUrl() { return eIpAddrInfo.url; }
		public String getLinuxDev() { return eIpAddrInfo.linuxDev; }
	}
}
