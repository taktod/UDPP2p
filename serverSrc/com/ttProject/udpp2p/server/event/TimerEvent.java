package com.ttProject.udpp2p.server.event;

import com.ttProject.udpp2p.server.adapter.ServerAdapter;

/**
 * 一定期間ごとの動作のタイマーイベント
 * @author taktod
 */
public class TimerEvent extends Thread {
	/** 定義アダプター */
	private final ServerAdapter adapter;
	/** 動作間隔 */
	private final long interval = 30000L;
	/**
	 * コンストラクタ
	 * @param adapter
	 */
	public TimerEvent(ServerAdapter adapter) {
		this.adapter = adapter;
	}
	@Override
	public void run() {
		while(true) {
			try {
				Thread.sleep(interval);
				adapter.scheduledJob();
			}
			catch (Exception e) {
			}
		}
	}
}
