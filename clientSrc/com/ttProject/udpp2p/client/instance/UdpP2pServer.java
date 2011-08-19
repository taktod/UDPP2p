package com.ttProject.udpp2p.client.instance;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

/*
 * DataGramSocketを作成しコネクトを実行、そのコネクトアドレスに対してそとから接続をこころみる感じにしておく。
 * 中央サーバーに接続してシステムとしてつながり続ける。
 * 指定した接続を実行するために、中央サーバーに接続してつながりを取得する。
 * 適当な接続をもらうために中央サーバーに接続してつながりつづける？
 */
/**
 * それぞれの接続用のインスタンス
 * @author taktod
 */
@SuppressWarnings("unused")
public class UdpP2pServer implements Runnable {
	/** タイムアウト値:5分設定 */
	private final Long timeout = 300000L;
	/** 動作用ソケット */
	private DatagramSocket socket;
	/** 接続対象サーバー */
	private SocketAddress server = new InetSocketAddress("localhost", 12345);
	/** P2Pやりとり先ターゲット */
	private SocketAddress target = null;
	UdpP2pServerAdapter adapter;
	/** 最終メッセージやりとり時刻 */
	private Long lastMessageTime = null;
	/** ID */
	private Long id = null;
	/**
	 * コンストラクタ
	 */
	public UdpP2pServer(UdpP2pServerAdapter adapter) {
		this.adapter = adapter;
		try {
			socket = new DatagramSocket();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		Thread t = new Thread(this);
		t.setName("UdpP2p receive thread");
		t.start();
	}
	/**
	 * コンストラクタ(仲介サーバー指定)
	 * @param server
	 */
	public UdpP2pServer(SocketAddress server) {
		super();
		this.server = server;
	}
	/*
	 * することは、サーバーに接続する。connを送る。
	 * 応答データからHandshake用のトークンを送る。
	 * UDP接続を確立させる。
	 * 適当にpingデータを飛ばす。送りたいときにメッセージを飛ばし、
	 * 取得したいときにメッセージをうける。
	 */
	public void connect() {
		// conn+適当な文字列をサーバーに送る。
		// サーバーからhandshake用のトークンを応答してもらう。
		sendData("conn" + System.currentTimeMillis());
	}
	/**
	 * IDを指定して接続する。
	 * @param id 自分のID
	 */
	public void connect(Long id) {
		sendData("conn:" + id);
	}
	/**
	 * システム接続として立候補する。
	 * @param id 自分のID
	 */
	public void connectSystem(Long id) {
		if(id == null) {
			return;
		}
		sendData("conn:" + id + ":system");
	}
	/**
	 * クライアントを指定して接続する。
	 * @param id 自分のID
	 * @param other 相手のID
	 */
	public void connectClient(Long id, Long other) {
		if(id == null || other == null) {
			return;
		}
		sendData("conn:" + id + ":" + other);
	}
	/**
	 * データを送信する。
	 * @param data
	 */
	private void sendData(String data) {
		try {
			byte[] bytes = data.getBytes();
			DatagramPacket packet = new DatagramPacket(bytes, bytes.length, server);
			socket.send(packet);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * Pingを送信する。
	 * @return
	 */
	public boolean sendPing() {
		if(lastMessageTime < System.currentTimeMillis() - timeout) {
			return false;
		}
		sendData("ping");
		return true;
	}
	/**
	 * メッセージ取得処理
	 * @param packet
	 */
	public void task(DatagramPacket packet) {
		lastMessageTime = System.currentTimeMillis();
		String message = new String(packet.getData());
		if(message.startsWith("handshake")) {
			handshakeTask(message);
			return;
		}
		if(message.startsWith("mode")) {
			modeTask(message);
			return;
		}
		System.out.println("soreigai");
	}
	/**
	 * Handshakeの処理
	 * @param message
	 */
	private void handshakeTask(String message) {
		System.out.println("handshake");
		String[] data = message.split(":");
		System.out.println(data[1]);
		// 与えられたHandshakeTokenをHexに変更して送り返す。
		sendData("handshake:" + Long.toHexString(Long.parseLong(data[1].trim())));
	}
	/**
	 * サーバーから送られてくる指定状態の応答
	 * @param message
	 */
	private void modeTask(String message) {
		System.out.println(message);
	}
	/**
	 * メッセージ受け取り
	 */
	@Override
	public void run() {
		// socketをpoolingして待機する。
		try {
			while(true) {
				DatagramPacket packet = new DatagramPacket(new byte[1024], 1024);
				socket.receive(packet);
				task(packet);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
