package com.ttProject.udpp2p.client.event;

import com.ttProject.udpp2p.client.instance.UdpP2pServerAdapter;

public class TimerEvent extends Thread {
	/** UdpP2p動作のアダプター */
	private final UdpP2pServerAdapter adapter;
	/** タイマーの間隔 */
	private final long interval = 30000L;
	/**
	 * コンストラクタ
	 * @param adapter
	 */
	public TimerEvent(UdpP2pServerAdapter adapter) {
		this.adapter = adapter;
	}
	/**
	 * 実動作処理
	 */
	@Override
	public void run() {
		while(true) {
			try {
				Thread.sleep(interval);
				// schedule jobを実行
				adapter.scheduledJob();
			}
			catch (Exception e) {
				// タイマー中断方法は模索していない。
				// 途中で停止するには、プロセスを停止するしか方法なくなっている。
			}
		}
	}
}
