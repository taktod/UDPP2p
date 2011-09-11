package com.ttProject.udpp2p.client.instance;

import java.util.ArrayList;
import java.util.List;

import com.ttProject.udpp2p.client.Server;
import com.ttProject.udpp2p.client.event.TimerEvent;

public class UdpP2pServerAdapter implements Server {
	private final boolean allowLocalConnection = true;
	private TimerEvent timer;
	// Threadを3つ持たせておく。
	// それぞれが接続相手を持って動作している。
	// タイマーイベントと各接続からのデータを処理する。

	/** システム用の接続 */
	private UdpP2pServer systemServer = null;
	/** クライアント用の接続 */
	private List<UdpP2pServer> clientServer = null;
	/**
	 * コンストラクタ
	 */
	public UdpP2pServerAdapter() {
		clientServer = new ArrayList<UdpP2pServer>();
	}
	public void start() {
		try {
			timer = new TimerEvent(this);
			timer.start();
			
			setupDefaultClient();
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

	// 中間処理
	// 何も考えずに初期のクライアントを作成する。
	public void setupDefaultClient() {
		// あたらしいUDPP2Pサーバーの接続をつくる。
		// この時点ではどちらの接続になるかは不明
		UdpP2pServer server = new UdpP2pServer(this);
		server.setLocalDataSend(allowLocalConnection);
		server.connect();
	}
	public void setupNextClient(UdpP2pServer server) {
		if(server.isSystemClient()) {
			systemServer = server;
			// システムクライアントの場合は次の接続を作成する必要がある
			UdpP2pServer nextServer = new UdpP2pServer(this);
			nextServer.setLocalDataSend(allowLocalConnection);
			nextServer.connect(UdpP2pServer.getClientId());
		}
		else {
			clientServer.add(server);
		}
	}
}
