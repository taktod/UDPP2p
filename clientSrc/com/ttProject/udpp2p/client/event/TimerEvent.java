package com.ttProject.udpp2p.client.event;

import com.ttProject.udpp2p.client.instance.UdpP2pServerAdapter;

public class TimerEvent extends Thread {
	private final UdpP2pServerAdapter adapter;
	private final long interval = 30000L;
	public TimerEvent(UdpP2pServerAdapter adapter) {
		this.adapter = adapter;
	}
	@Override
	public void run() {
		while(true) {
			try {
				Thread.sleep(interval);
				// schedule jobを実行
				adapter.scheduledJob();
			}
			catch (Exception e) {
			}
		}
	}
}
