package com.ttProject.udpp2p.server.jmx;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Map;

import javax.management.MBeanServer;
import javax.management.Notification;
import javax.management.NotificationListener;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;

/**
 * rmi経由JMXアクセス用
 * @author taktod
 *
 */
public class JMXAgent implements NotificationListener {
	private static JMXConnectorServer cs;
	private static boolean enableRmiAdapter = true;
	private static MBeanServer mbs = null;
	private static String rmiAdapterPort = "3698";
	private static String rmiAdapterRemotePort = "3697";
	private static String rmiAdapterHost = "localhost";
	@SuppressWarnings("unused")
	private static String user = "taktod";
	@SuppressWarnings("unused")
	private static String password = "hogehoge";
	static {
		if(mbs == null) {
			mbs = JMXFactory.getMBeanServer();
		}
	}
	/**
	 * JMXコネクションを初期化する
	 */
	public static void init() {
		Map<String, Object> env = null;
		if(enableRmiAdapter) {
			System.setProperty("java.rmi.server.hostname", rmiAdapterHost);
			try {
				Registry r = null;
				try {
					r = LocateRegistry.getRegistry(Integer.valueOf(rmiAdapterPort));
					for(String regName : r.list()) {
						if(regName.equals("udpp2p")) {
							r.unbind("udpp2p");
						}
					}
				}
				catch (Exception e) {
					r = LocateRegistry.createRegistry(Integer.valueOf(rmiAdapterPort));
				}
				
				JMXServiceURL url = null;
				url = new JMXServiceURL("service:jmx:rmi://" + rmiAdapterHost + ":" + rmiAdapterRemotePort + "/jndi/rmi://" + rmiAdapterHost + ":" + rmiAdapterPort + "/udpp2p");
				
				// 本来ならjmx.remote.x.access.fileとかを設置すべき。
				
				cs = JMXConnectorServerFactory.newJMXConnectorServer(url, env, mbs);
				cs.addNotificationListener(new JMXAgent(), null, null);
				cs.start();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	public static void shutdown() {
		
	}
	
	@Override
	public void handleNotification(Notification paramNotification,
			Object paramObject) {
		System.out.println("notification" + paramNotification.getMessage());
	}
}
