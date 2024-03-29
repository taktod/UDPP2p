package com.ttProject.udpp2p.client.instance;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import com.ttProject.udpp2p.library.data.ConnectionData;
import com.ttProject.udpp2p.library.data.Data;
import com.ttProject.udpp2p.library.data.HandshakeData;
import com.ttProject.udpp2p.library.data.ModeData;
import com.ttProject.udpp2p.library.data.PingData;
import com.ttProject.udpp2p.library.json.JsonData;

/*
 * DataGramSocketを作成しコネクトを実行、そのコネクトアドレスに対してそとから接続をこころみる感じにしておく。
 * 中央サーバーに接続してシステムとしてつながり続ける。
 * 指定した接続を実行するために、中央サーバーに接続してつながりを取得する。
 * 適当な接続をもらうために中央サーバーに接続してつながりつづける？
 * 
 * 接続がうまくいかないのが長くつづいた場合(中央サーバーから応答がない場合はタイムアウトとしてエラーを応答したいところ。)
 */
/**
 * それぞれの接続用のインスタンス
 * (Junitの処理を積極的にいれていくクラス)
 * @author taktod
 */
public class UdpP2pServer implements Runnable {
	/** タイムアウト値:5分設定 */
	private final Long timeout = 300000L;
	/** 動作用ソケット */
	private DatagramSocket socket;
	/** 接続対象サーバー */
	private SocketAddress server = new InetSocketAddress("localhost", 12345);
	/** P2Pやりとり先ターゲット */
	private SocketAddress target = null;
	/** 処理アダプター */
	private UdpP2pServerAdapter adapter;
	/** 最終メッセージやりとり時刻 */
	private Long lastMessageTime = null;
	/** ID */
	private static Long id = null;
	/** ローカルネットワークのデータを送信するかどうか？ */
	private boolean localDataSend = false;
	/** 接続の種類 */
	private Byte type = null;
	
	public static Long getClientId() {
		return id;
	}
	/**
	 * システムクライアントであるかどうか
	 * @return
	 */
	public boolean isSystemClient() {
		return type == (byte)0xFF;
	}
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
	/**
	 * ローカルネットワークの情報を送信するかどうかフラグを設定する。
	 * @param flg
	 */
	public void setLocalDataSend(boolean flg) {
		localDataSend = flg;
	}
	/*
	 * することは、サーバーに接続する。connを送る。
	 * 応答データからHandshake用のトークンを送る。
	 * UDP接続を確立させる。
	 * 適当にpingデータを飛ばす。送りたいときにメッセージを飛ばし、
	 * 取得したいときにメッセージをうける。
	 */
	public void connect() {
		ConnectionData connectionData = new ConnectionData();
		addLocalData(connectionData);
		sendData(connectionData);
	}
	/**
	 * IDを指定して接続する。
	 * @param id 自分のID
	 */
	public void connect(Long id) {
		ConnectionData connectionData = new ConnectionData();
		addLocalData(connectionData);
		connectionData.setId(id);
		sendData(connectionData);
	}
	/**
	 * システム接続として立候補する。
	 * @param id 自分のID
	 */
	public void connectSystem(Long id) {
		if(id == null) {
			return;
		}
		ConnectionData connectionData = new ConnectionData();
		addLocalData(connectionData);
		connectionData.setId(id);
		connectionData.setTarget(-1L);
		sendData(connectionData);
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
		ConnectionData connectionData = new ConnectionData();
		addLocalData(connectionData);
		connectionData.setId(id);
		connectionData.setTarget(other);
		sendData(connectionData);
	}
	/**
	 * ローカル接続データを
	 * @param connectionData
	 */
	private void addLocalData(ConnectionData connectionData) {
		if(localDataSend) {
			try {
				connectionData.setLocalAddress(InetAddress.getLocalHost().getHostAddress());
				connectionData.setLocalPort(socket.getLocalPort());
				System.out.println(connectionData.encode());
			}
			catch (Exception e) {
			}
		}
	}
	/**
	 * データを送信する。
	 * @param data
	 */
	private void sendData(Data data) {
		try {
			byte[] bytes = data.encode().getBytes();
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
		PingData pingData = new PingData();
		sendData(pingData);
		return true;
	}
	/**
	 * メッセージ取得処理
	 * @param packet
	 */
	public void event(DatagramPacket packet) {
		lastMessageTime = System.currentTimeMillis();
		JsonData recvData = new JsonData(new String(packet.getData()));
		System.out.println(recvData);
		if("handshake".equals(recvData.get("message"))) {
			handshakeEvent(new HandshakeData(recvData));
			return;
		}
		if("mode".equals(recvData.get("message"))) {
			modeEvent(new ModeData(recvData));
			return;
		}
		System.out.println("soreigai");
	}
	/**
	 * Handshakeの処理
	 * @param message
	 */
	private void handshakeEvent(HandshakeData handshakeData) {
		JsonData sendData = new JsonData();
		System.out.println("handshake");
		// 与えられたHandshakeTokenをHexに変更して送り返す。
		handshakeData.setStringToken(Long.toHexString(handshakeData.getToken()));
		sendData(handshakeData);
	}
	/**
	 * サーバーから送られてくる指定状態の応答
	 * @param message
	 */
	private void modeEvent(ModeData modeData) {
		System.out.println(modeData.encode());
		// 自分のIDをサーバーから送られてきたIDでうわがきしておく。
		id = modeData.getId();
		// 接続タイプがシステムか待ち状態の場合
		type = 0x01;
		if(modeData.getTarget() <= 0) {
			switch (modeData.getTarget().intValue()) {
			case -1:
				type = (byte)0xFF; // システムクライアント
				break;
			case 0:
				type = (byte)0x00; // 接続待ち状態
				break;
			default:
				break;
			}
		}
		else {
			// 接続相手がみつかった場合
			// 相手のtargetIdと接続して問題ないか確認する。
			// すでに接続済みの相手の場合はつなげない。
			// 接続拒否中の相手の場合はつなげない。
			// これ以上接続をつくらない場合はここで処理を中断
		}
		// 自分の状態が決定したら、Adapterに次の処理を問い合わせることにする
		adapter.setupNextClient(this);
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
				event(packet);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
