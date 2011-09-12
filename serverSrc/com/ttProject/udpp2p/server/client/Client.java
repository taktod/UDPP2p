package com.ttProject.udpp2p.server.client;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
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
public class Client {
	/** タイムアウト値:5分設定 */
	private final Long timeout = 300000L;
	/** クライアントのアドレス情報 */
	private SocketAddress address = null;
	/** クライアントのローカル側でのアドレス情報(任意) */
	private SocketAddress localAddress = null;
	/** 最終メッセージ時刻 */
	private Long lastMessageTime = null;
	/** クライアントID */
	private Long id = null;
	/** Handshake用の情報 */
	private Long handshakeToken = null;
	/** UDP check(udpCheckが完了するまでは、クライアントとの接続を許可しないでおいておく。) */
	private boolean udpCheck = false;
	/** UDPHolepunching check */
	private boolean udpHolePunchingCheck = false;
	/** 接続状態　-1:システム 0:接続待ち Long:接続相手 */
	private Long target = null;
	/**
	 * UdpCheckが完了している場合はHandshakeが完了しているので、接続可能クライアント扱いにする？
	 * @return
	 */
	public boolean isReadyClient() {
		return udpCheck;
	}
	// 自分がすでに接続済みクライアントデータを送る必要がある。
	/*
	 * 先にサーバーに接続ずみクライアント情報をおくった方がいいのか、接続をとりあえず試させて、クライアントごとに、接続済みであるか確認させた方がいいのか・・・
	 * 考える必要がある。
	 */
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
	 * システムクライアントに設定しておく。
	 */
	public void setSystemClient() {
		target = -1L;
	}
	/**
	 * 接続ターゲット情報設置
	 * @param targetId
	 */
	public void setTarget(Long targetId) {
		target = targetId;
	}
	/**
	 * 接続ターゲット情報取得
	 * @return
	 */
	public Long getTarget() {
		return target;
	}
	/**
	 * クライアントID
	 * @return
	 */
	public Long getId() {
		return id;
	}
	/**
	 * アドレス
	 * @return
	 */
	public SocketAddress getAddress() {
		return address;
	}
	/**
	 * アドレスキー
	 * @return
	 */
	public String getAddressKey() {
		return address.toString();
	}
	/**
	 * Handshake用のTokenを生成する。
	 */
	private void generateHandshakeToken() {
		Random randam = new Random(System.currentTimeMillis());
		handshakeToken = randam.nextLong();
		handshakeToken = (handshakeToken < 0 ? -handshakeToken : handshakeToken);
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
		sendData(pingData);
		return true;
	}
	/**
	 * メッセージを受け取る
	 * @param socket
	 * @param packet
	 */
	public void receiveMessage(DatagramPacket packet) {
		// UDPチェック中はhandshakeの応答でしかlastMessageTimeを更新しない。
		// 最終メッセージ取得時刻を設定しておく。
		if(udpCheck) {
			lastMessageTime = System.currentTimeMillis();
		}
		JsonData recvData = new JsonData(new String(packet.getData()));
		// TODO この部分はいつかswitchに書き直しておく。
		// ping
		if("ping".equals(recvData.get("message"))) {
			// 特にする処理なし
			return;
		}
		// 接続
		if("conn".equals(recvData.get("message"))) {
			connectionEvent(new ConnectionData(recvData));
			return;
		}
		// handshake
		if("handshake".equals(recvData.get("message"))) {
			handshakeEvent(new HandshakeData(recvData));
			return;
		}
		// 接続要求
		if("demand".equals(recvData.get("message"))) {
			demandEvent(recvData);
			return;
		}
		// 該当処理なし。
		otherEvent(recvData);
	}
	/**
	 * その他のイベント
	 * @param data
	 */
	private void otherEvent(JsonData data) {
		
	}
	/**
	 * クライアントから接続の要求をうけとったときの動作
	 * @param data
	 */
	private void demandEvent(JsonData data) {
		
	}
	/**
	 * 接続イベント
	 * @param connectionData
	 */
	private void connectionEvent(ConnectionData connectionData) {
		// 接続時にIDがおくられてきている場合はIDを設定しておく。(デフォルトで新規IDが付加されているから、ない場合は新しいIDがついている。)
		if(connectionData.getId() != null) {
			id = connectionData.getId();
		}
		if(connectionData.getTarget() != null) {
			target = connectionData.getTarget();
		}
		if(connectionData.getLocalAddress() != null && connectionData.getLocalPort() != null) {
			localAddress = new InetSocketAddress(connectionData.getLocalAddress(), connectionData.getLocalPort());
		}
		sendHandshake();
	}
	/**
	 * ハンドシェークの動作
	 */
	private void sendHandshake() {
		HandshakeData handshakeData = new HandshakeData();
		// 接続時の動作
		// Handshakeのキューを送っておく。
		generateHandshakeToken();
		handshakeData.setToken(handshakeToken);
		// 送信データを作成し、送る
		sendData(handshakeData);
	}
	/**
	 * ハンドシェークのイベント
	 * @param handshakeData
	 */
	private void handshakeEvent(HandshakeData handshakeData) {
//		JsonData sendData = new JsonData();
		// そもそものHandshakeと一致するか確認する。一致しなければ落とす。
		// 送られてきたHandshakeの値をHex化して一致するか確認する。
		// 一致したらそのクライアントはUDP接続は可能ということ。
		String token = handshakeData.getStringToken();
		System.out.println(token);
		System.out.println(handshakeToken);
		if(token != null && token.equals(Long.toHexString(handshakeToken))) {
			System.out.println("handshaketoken is ok");
			// クライアントの情報を確定する。
			ClientManager clientManager = ClientManager.getInstance();
			clientManager.setupClient(this);
			// udp動作成功
			udpCheck = true;
		}
		else {
			System.out.println("handshaketoken is ng");
		}
	}
	/**
	 * できあがったクライアントのモードを応答しておく。
	 */
	public void sendMode() {
		// ClientManagerに自分がどういう立ち回りを実行するべきか問い合わせる。
		ModeData modeData = new ModeData();
		modeData.setId(id);
		modeData.setTarget(target);
		sendData(modeData);
	}
	/**
	 * クライアントに特定クライアントとの接続をたずねる。
	 */
	public void sendConnectTargetData() {
		
	}
	/**
	 * データを送信する。
	 * @param socket
	 * @param data
	 */
	private void sendData(String data) {
		try {
			byte[] bytes = data.getBytes();
			DatagramPacket packet = new DatagramPacket(bytes, bytes.length, address);
			ClientManager clientManager = ClientManager.getInstance();
			clientManager.getSocket().send(packet);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	/**
	 * データを送信する。
	 * @param socket
	 * @param data
	 */
	private void sendData(Data data) {
		sendData(data.encode());
	}
}
