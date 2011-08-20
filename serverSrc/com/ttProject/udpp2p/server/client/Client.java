package com.ttProject.udpp2p.server.client;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.util.Random;

/**
 * 通常のクライアント
 * @author taktod
 */
@SuppressWarnings("unused")
public class Client {
	/** タイムアウト値:5分設定 */
	private final Long timeout = 300000L;
	/** クライアントのアドレス情報 */
	private SocketAddress address = null;
	/** 最終メッセージ時刻 */
	private Long lastMessageTime = null;
	/** クライアントID */
	private Long id = null;
	/** Handshake用の情報 */
	private Long handshakeToken = null;
	/** UDP check */
	private boolean udpCheck = false;
	/** UDPHolepunching check */
	private boolean udpHolePunchingCheck = false;
	/** 接続状態　-1:システム 0:接続待ち Long:接続相手 */
	private Long type = null;
	/**
	 * コンストラクタ
	 * @param packet
	 * @param id
	 */
	public Client(DatagramPacket packet, Long id) {
		lastMessageTime = System.currentTimeMillis();
		address = packet.getSocketAddress();
		this.id = id;
	}
	/**
	 * クライアントID
	 * @return
	 */
	public Long getId() {
		return id;
	}
	/**
	 * Handshake用のTokenを生成する。
	 */
	private void generateHandshakeToken() {
		Random randam = new Random(System.currentTimeMillis());
		handshakeToken = randam.nextLong();
		handshakeToken = handshakeToken < 0 ? -handshakeToken : handshakeToken;
	}
	/**
	 * pingを送信する。
	 * @param socket
	 */
	public boolean sendPing(DatagramSocket socket) {
		// クライアントから応答が長いことない場合はエラーと判断して切断する。
		if(lastMessageTime < System.currentTimeMillis() - timeout) {
			// 5分たったら強制切断
			return false;
		}
		sendData(socket, "ping");
		return true;
	}
	/**
	 * メッセージを受け取る
	 * @param socket
	 * @param packet
	 */
	public void receiveMessage(DatagramSocket socket, DatagramPacket packet) {
		// UDPチェック中はhandshakeの応答でしかlastMessageTimeを更新しない。
		// 最終メッセージ取得時刻を設定しておく。
		if(udpCheck) {
			lastMessageTime = System.currentTimeMillis();
		}
		String message = new String(packet.getData());
		if(message.startsWith("ping")) {
			// 命令がpingの場合はこれ以上なにもしない。
			return;
		}
		// TODO この部分はいつかswitchに書き直しておく。
		// その他の命令の場合・・・
		if(message.startsWith("conn")) {
			// 接続処理
			connectionTask(socket, message);
			return;
		}
		if(message.startsWith("handshake")) {
			// Handshake時処理
			handshakeTask(socket, message);
			return;
		}
		if(message.startsWith("demand")) {
			// 他のクライアントとの接続要求
			demandTask(socket, message);
		}
		// 該当処理なし。
		otherTask(socket, message);
	}
	private void otherTask(DatagramSocket socket, String message) {
		
	}
	private void demandTask(DatagramSocket socket, String message) {
		
	}
	private void connectionTask(DatagramSocket socket, String message) {
		System.out.println("connection...");
		// conn:(id):(つなぎたい状態　system or 相手のClientID)
		// 接続時の動作
		// Handshakeのキューを送っておく。
		generateHandshakeToken();
		// 送信データを作成し、送る
		sendData(socket, "handshake:" + handshakeToken.toString());
	}
	private void handshakeTask(DatagramSocket socket, String message) {
		// そもそものHandshakeと一致するか確認する。一致しなければ落とす。
		// 送られてきたHandshakeの値をHex化して一致するか確認する。
		// 一致したらそのクライアントはUDP接続は可能ということ。
		System.out.println("handshakeEvent...");
		String[] data = message.split(":");
//		if(Long.parseLong(data[1].trim(), 16) - handshakeToken == 0) {
		if(data[1].trim().equals(Long.toHexString(handshakeToken))) {
			System.out.println("handshaketoken is ok");
			// udp動作成功
			udpCheck = true;
			// mode情報をおくっておく。
			sendData(socket, "mode:");
		}
		else {
			System.out.println("handshaketoken is ng");
		}
	}
	/**
	 * データを送信する。
	 * @param socket
	 * @param data
	 */
	private void sendData(DatagramSocket socket, String data) {
		try {
			byte[] bytes = data.getBytes();
			DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address);
			socket.send(packet);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
