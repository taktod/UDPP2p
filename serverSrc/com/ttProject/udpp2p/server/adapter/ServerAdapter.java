package com.ttProject.udpp2p.server.adapter;

import java.net.DatagramPacket;

import com.ttProject.udpp2p.server.client.Client;
import com.ttProject.udpp2p.server.client.ClientManager;
import com.ttProject.udpp2p.server.event.SocketEvent;
import com.ttProject.udpp2p.server.event.TimerEvent;
import com.ttProject.udpp2p.server.jmx.JMXAgent;

/*
 * サーバーの仕事
 * すべてのユーザーが接続し合うシステム接続と
 * 特定のユーザーが接続し合うアプリ接続の２つがある。
 * ・システム接続の実行
 * 一番はじめにネットワークにログインする際に、ネットワーク接続用のP2P接続を構築するための橋渡しを行う。
 * ・アプリ接続の実行
 * 途中でAさんがBさんと接続をしたいとする。
 * AさんやBさんは直接相手のIPアドレスを知ることはできないことにしておくので(ただし、共有空間にプログラムがIPアドレス情報を送った場合は別。)
 * Aさんは中央サーバーにBさんとの接続を実行する依頼をだす。
 * AさんはBさん宛にどこの中央サーバーで接続を実行するか連絡をいれておく。
 * Bさんは中央サーバーに接続。
 * サーバーはAさんとBさんが互いに接続用のHandshakeを実行できるように取りはからう。
 */
/**
 * SocketEventとTimerEventが永続的に保持しつづけるServerAdapter
 * よってここに必要なデータをひもづけておいて問題なさそう。
 * @author taktod
 */
public class ServerAdapter {
	/** タイマーイベントのインスタンス */
	private TimerEvent timer;
	/** ソケットイベントのインスタンス */
	private SocketEvent socket;
	/**
	 * サーバーを起動しておく。
	 */
	public void start() {
		JMXAgent.init();
		try {
			timer = new TimerEvent(this);
			timer.start();
			socket = new SocketEvent(this);
			socket.start();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * 一定時間ごとに実行されるタスク
	 */
	public void scheduledJob() {
		// ping処理を実行しておく。
		ClientManager clientManager = ClientManager.getInstance();
		clientManager.pingJob(socket.getSocket());
	}
	/**
	 * データを受信した場合の処理
	 * @param packet 取得データ
	 */
	public void dataJob(DatagramPacket packet) {
		ClientManager clientManager = ClientManager.getInstance();
		Client client = clientManager.getTargetClient(packet);
		client.receiveMessage(socket.getSocket(), packet);
	}
}
