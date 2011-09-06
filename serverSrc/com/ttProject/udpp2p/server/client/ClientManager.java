package com.ttProject.udpp2p.server.client;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import com.ttProject.udpp2p.server.adapter.ServerAdapter;

/**
 * クライアントを管理
 * @author taktod
 */
@SuppressWarnings("unused")
public class ClientManager {
	/** クライアントIDを作成するときの種 */
	private static Long seed = System.currentTimeMillis();
	/** シングルトンインスタンス */
	private static ClientManager instance = null;

	/** 待機中の通常クライアント */
	private Map<String, Client> clients = null; // 接続に利用したパケットキー -> Client
	/** 接続中のシステムクライアント */
	private Map<String, Client> systemClients = null; // 接続に利用したパケットキー -> Client
	/** 特定クライアントと接続しようとしているユーザーのデータ */
	private Map<String, Client> waitingClients = null; // つなぎたいClientId -> Client
	/** 接続ユーザー数カウンター */
	private Long connectCount = 0L;
	/** システム接続の最大接続数予定 */
	private int systemMaxCount = 50;
	/** アダプターオブジェクト */
	private ServerAdapter adapter;
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
	 * adapter設定
	 * @param adapter
	 */
	public void setAdapter(ServerAdapter adapter) {
		this.adapter = adapter;
	}
	public DatagramSocket getSocket() {
		return adapter.getSocket();
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
		connectCount ++;
		if(connectCount == Long.MAX_VALUE) {
			connectCount = 0L;
		}
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
	public void pingJob() {
		// 通常クライアント
		Map<String, Client> nextClients = new ConcurrentHashMap<String, Client>();
		synchronized(clients) {
			// pingの処理
			for(Entry<String, Client> entry : clients.entrySet()) {
				if(entry.getValue().sendPing(adapter.getSocket())) {
					nextClients.put(entry.getKey(), entry.getValue());
				}
			}
		}
		clients = nextClients;
		// システムクライアント
		nextClients = new ConcurrentHashMap<String, Client>();
		synchronized (systemClients) {
			for(Entry<String, Client> entry : systemClients.entrySet()) {
				if(entry.getValue().sendPing(adapter.getSocket())) {
					nextClients.put(entry.getKey(), entry.getValue());
				}
			}
		}
		systemClients = nextClients;
		nextClients = new ConcurrentHashMap<String, Client>();
		synchronized(waitingClients) {
			for(Entry<String, Client> entry : waitingClients.entrySet()) {
				if(entry.getValue().sendPing(adapter.getSocket())) {
					nextClients.put(entry.getKey(), entry.getValue());
				}
			}
		}
		waitingClients = nextClients;
	}
	/**
	 * クライアントの最終設定を実行
	 * @param client
	 */
	public void setupClient(Client client) {
		// クライアントのモードを設定しておく。
		setupClientMode(client);
		Long target = client.getTarget();
		// システムクライアント
		if(target == -1) {
			doSystemClient(client);
			return;
		}
		// 接続先指定クライアント
		if(target > 0L) {
			doTargetClient(client);
			return;
		}
		// 一般クライアント
		if(target == 0L) {
			doClient(client);
			return;
		}
	}
	/**
	 * システム接続の後処理
	 * @param client
	 */
	private void doSystemClient(Client client) {
		// システムクライアントの場合はクライアントに情報を送り返しておく。
		// ここでなにかするということはなにもなし。
		client.sendMode();
	}
	/**
	 * 接続先指定の後処理
	 * @param client
	 */
	private void doTargetClient(Client client) {
		// 接続先をきめているクライアントの場合は、現在のクライアントセットに対象クライアントがあるか確認。
		String key = null;
		Client target = waitingClients.get(client.getId().toString());
		if(target != null && target.getId() == client.getTarget()) {
			// 自分とつなごうとしてる相手が自分がつなぎたい相手の場合
			// みつけた相手とつながる。
			return;
		}
		// clientsに自分がつなごうとしているユーザーがいるか確認する。
		// ここにsynchronizedしておかないとだめ？
		for(Entry<String, Client> entry : clients.entrySet()) {
			if(entry.getValue().getId() == client.getTarget()) {
				// 相手がみつかった。
				key = entry.getKey();
				// みつけた相手がほかのユーザーとかち合うとこまるので、clientsから見つけたclientを取り去る。
				break;
			}
		}
		if(key != null) {
			// 相手が見つかった場合
			target = clients.remove(key);
			// targetとやり取りする。(*)
			return;
		}
		// なければ自分をwaitingにいれて、システムクライアントにデータを要求する。
		waitingClients.put(client.getTarget().toString(), client);
		// なければシステムクライアントに接続相手から接続にくるように要求をだしておく。
		// システムクライアントにデータを送り、対象クライアントが接続しにくるように促す。
		// なお、システムメッセージやりとり動作をする上で接続先ユーザーを指定されてもあまり意味がない。(アプリケションレベルでは意味があると思うけど。)
	}
	/**
	 * 一般接続の後処理
	 * @param client
	 */
	private void doClient(Client client) {
		// 通常クライアントの場合は、適当にみつけたクライアントに接続要求を出す。
		// まず、waitingClientsに自分宛の接続が存在するか確認する。
		Client target = waitingClients.get(client.getId().toString()); // 自分と接続したいユーザーがいるか確認する。
		if(target != null) {
			// 相手がなにものであろうと自分とつなぎたい相手なのでつなぐ方向で考える。
			// みつけた相手とつなげる。
			return;
		}
		// 自分宛の接続が存在しない場合は適当にみつけたクライアントと接続する。
		for(Entry<String, Client> entry : clients.entrySet()) {
			// 一番はじめにみつけた相手とやり取りを実行する。
			// ここに処理がきたら、このユーザーと接続できるかもしれないので、処理をすすめる。
		}
		// ここまできてしまったということは接続する相手がいないということなので、clientsに自分をいれて待機しておく。
	}
	/**
	 * クライアントの状態を設定しておく
	 * @param client
	 */
	private void setupClientMode(Client client) {
		// クライアントの割り振りを決定する。
		Long target = client.getTarget();
		// このクライアントの設定がない場合は設定を作成する。
		if(target == null) {
			// システムクライアントをためしてみる。
			if(canBeSystemClient(client)) {
				client.setSystemClient();
			}
			else {
				// 接続待ちクライアントに設定する。
				client.setTarget(0L);
			}
			target = client.getTarget();
		}
		if(target == -1L) {
			// システムクライアントの場合
			clients.remove(client.getAddressKey());
			systemClients.put(client.getAddressKey(), client);
			return;
		}
		if(target > 0L) {
			// 接続先が設定されている場合はqueueに設定しておく。
			clients.remove(client.getAddressKey());
			waitingClients.put(target.toString(), client);
		}
	}
	/**
	 * システムになれるか確認
	 * @param client
	 * @return
	 */
	private boolean canBeSystemClient(Client client) {
		// システムクライアントがすでに最大接続数に達している場合
		if(systemClients.size() >= systemMaxCount) {
			return false;
		}
		// 同じクライアントからの接続がすでにシステム接続になっているか確認
		for(Entry<String, Client> element : systemClients.entrySet()) {
			if(element.getValue().getId() == client.getId()) {
				return false;
			}
		}
		// どちらでもないのでシステムクライアントになれる。
		return true;
	}
}
