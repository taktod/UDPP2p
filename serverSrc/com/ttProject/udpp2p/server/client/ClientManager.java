package com.ttProject.udpp2p.server.client;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * クライアントを管理
 * @author taktod
 */
public class ClientManager {
	/** クライアントIDを作成するときの種 */
	private static Long seed = System.currentTimeMillis();
	/** シングルトンインスタンス */
	private static ClientManager instance = null;

	/** 待機中の通常クライアント */
	private Map<String, Client> clients = null;
	/** 接続中のシステムクライアント */
	private Map<String, Client> systemClients = null;
	/** 特定クライアントと接続しようとしているユーザーのデータ */
	private Map<String, Client> waitingClients = null; // つなぎたいClientId -> Client
	/** 接続ユーザー数カウンター */
	private Long connectCount = 0L;
	/** 現状のシステム接続数カウンター(systemClients.size()の方が正確) */
//	private int systemCount = 0;
	/** システム接続の最大接続数予定 */
	private int systemMaxCount = 50;
	/**
	 * コンストラクタ
	 */
	private ClientManager() {
		clients = new ConcurrentHashMap<String, Client>();
		systemClients = new ConcurrentHashMap<String, Client>();
		waitingClients = new ConcurrentHashMap<String, Client>();
	}
	/**
	 * シングルトンインスタンス取得
	 * @return
	 */
	public static synchronized ClientManager getInstance() {
		if(instance == null) {
			instance = new ClientManager();
		}
		return instance;
	}
	/**
	 * 処理主体のクライアントインスタンスを取得する
	 * @param packet
	 * @return
	 */
	public Client getTargetClient(DatagramPacket packet) {
		// クライアントを取得する。
		/*
		 * 通常はいったん接続すると通常クライアント扱い、HandShake後にシステムクライアントとして認定されるとシステムクライアント扱い。
		 * システムクライアントは、ずっとサーバーと接続しており、ユーザーの管理等を実行する。
		 */
		Client client;
		String packetKey = packet.getSocketAddress().toString();
		// クライアントとして接続していたことがあるか？
		client = clients.get(packetKey);
		if(client != null) {
			return client;
		}
		client = systemClients.get(packetKey);
		// システムクライアントであるか？
		if(client != null) {
			return client;
		}
		// どっちでもなければ新しいクライアント
		client = new Client(packet, generateId(packet));
		// あたらしいクライアントIDを付加しておく。
		clients.put(packetKey, client);
		return client;
	}
	/**
	 * IDを作成する。
	 * @param packet
	 * @return
	 */
	private Long generateId(DatagramPacket packet) {
		return seed + packet.getSocketAddress().hashCode();
	}
	/**
	 * pingの送信を実行する処理
	 * @param socket
	 */
	public void pingJob(DatagramSocket socket) {
		// 通常クライアント
		Map<String, Client> nextClients = new ConcurrentHashMap<String, Client>();
		synchronized(clients) {
			// pingの処理
			for(Entry<String, Client> entry : clients.entrySet()) {
				if(entry.getValue().sendPing(socket)) {
					nextClients.put(entry.getKey(), entry.getValue());
				}
			}
		}
		clients = nextClients;
		// システムクライアント
		nextClients = new ConcurrentHashMap<String, Client>();
		synchronized (systemClients) {
			for(Entry<String, Client> entry : systemClients.entrySet()) {
				if(entry.getValue().sendPing(socket)) {
					nextClients.put(entry.getKey(), entry.getValue());
				}
			}
		}
		systemClients = nextClients;
	}

	/**
	 * 指定クライアントをシステムクライアントに登録しておく。
	 * @param client
	 */
	public void registerSystemClient(Client client) {
		// アドレスキーのクライアントを削除する。
		clients.remove(client.getAddressKey());
		systemClients.put(client.getAddressKey(), client);
		/*
		 * システム接続になる方法は、自分のクライアントIDの接続がほかにシステム接続になっていないこと。
		 */
	}
	/*
	 * クライアントと接続する場合
	 * 接続する相手が特定されていない場合は、clientsのmapにいれてあるユーザーと新しく接続してきたユーザーに接続させる。
	 * 接続する相手が特定されている場合は、システムクライアントにclientIdのユーザーに接続しにくるように設定しておき、接続がきたらつなぐようにしておく。
	 */
}
