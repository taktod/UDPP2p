package com.ttProject.udpp2p.client.instance;

import com.ttProject.udpp2p.client.Server;
import com.ttProject.udpp2p.client.event.TimerEvent;

public class UdpP2pServerAdapter implements Server {
	private TimerEvent timer;
	// Threadを3つ持たせておく。
	// それぞれが接続相手を持って動作している。
	// タイマーイベントと各接続からのデータを処理する。
	
	/**
	 * コンストラクタ
	 */
	public UdpP2pServerAdapter() {
		
	}
	public void start() {
		try {
			timer = new TimerEvent(this);
			timer.start();
			UdpP2pServer server = new UdpP2pServer(this);
			server.setLocalDataSend(true);
			server.connect();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	public void setApplication() {
		/*
		 * システム接続とは別にアプリケーション接続を準備する。
		 * そのためのアプリケーション名
		 */
		// アプリケーション名を設定する。
	}
	public void scheduledJob() {
		// 持っているクライアントに関してpingを飛ばす？
		System.out.println("TimerEvent");
		// クライアントリスト全部にpingを送信する。
	}
}
