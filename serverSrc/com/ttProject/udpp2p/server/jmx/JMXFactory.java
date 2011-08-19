package com.ttProject.udpp2p.server.jmx;

import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;

public class JMXFactory {
	private static final String domain = "com.ttProject.udpp2p.server";
	private static final MBeanServer mbeanServer;
	
	static {
		MBeanServer mbs;
		try {
			mbs = MBeanServerFactory.findMBeanServer(null).get(0);
		}
		catch (Exception e) {
			mbs = ManagementFactory.getPlatformMBeanServer();
		}
		mbeanServer = mbs;
	}
	public static String getDomain() {
		return domain;
	}
	public static MBeanServer getMBeanServer() {
		return mbeanServer;
	}
}
