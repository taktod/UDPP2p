package com.ttProject.udpp2p.server.client;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.util.Random;

import com.ttProject.udpp2p.library.data.ConnectionData;
import com.ttProject.udpp2p.library.data.Data;
import com.ttProject.udpp2p.library.data.HandshakeData;
import com.ttProject.udpp2p.library.data.ModeData;
import com.ttProject.udpp2p.library.data.PingData;
import com.ttProject.udpp2p.library.json.JsonData;

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
		PingData pingData = new PingData();
		sendData(socket, pingData);
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
		JsonData recvData = new JsonData(new String(packet.getData()));
		if("ping".equals(recvData.get("message"))) {
			// 命令がpingの場合はこれ以上なにもしない。
			return;
		}
		// TODO この部分はいつかswitchに書き直しておく。
		// その他の命令の場合・・・
		if("conn".equals(recvData.get("message"))) {
			// 接続処理
			connectionTask(socket, new ConnectionData(recvData));
			return;
		}
		if("handshake".equals(recvData.get("message"))) {
			// Handshake時処理
			handshakeTask(socket, new HandshakeData(recvData));
			return;
		}
		if("demand".equals(recvData.get("message"))) {
			// 他のクライアントとの接続要求
			demandTask(socket, recvData);
		}
		// 該当処理なし。
		otherTask(socket, recvData);
	}
	private void otherTask(DatagramSocket socket, JsonData data) {
		
	}
	private void demandTask(DatagramSocket socket, JsonData data) {
		
	}
	private void connectionTask(DatagramSocket socket, ConnectionData connectionData) {
		System.out.println("connection...");

		JsonData sendData = new JsonData();
		HandshakeData handshakeData = new HandshakeData();
		// 接続時の動作
		// Handshakeのキューを送っておく。
		generateHandshakeToken();
		handshakeData.setToken(handshakeToken);
		// 送信データを作成し、送る
		sendData(socket, handshakeData);
	}
	private void handshakeTask(DatagramSocket socket, HandshakeData handshakeData) {
		System.out.println("handshakeEvent...");
		JsonData sendData = new JsonData();
		// そもそものHandshakeと一致するか確認する。一致しなければ落とす。
		// 送られてきたHandshakeの値をHex化して一致するか確認する。
		// 一致したらそのクライアントはUDP接続は可能ということ。
		String token = handshakeData.getStringToken();
		if(token != null && token.equals(Long.toHexString(handshakeToken))) {
			System.out.println("handshaketoken is ok");
			// udp動作成功
			udpCheck = true;
			// mode情報をおくっておく。
			ModeData modeData = new ModeData();
			sendData(socket, modeData);
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
	private void sendData(DatagramSocket socket, Data data) {
		sendData(socket, data.encode());
	}
}
