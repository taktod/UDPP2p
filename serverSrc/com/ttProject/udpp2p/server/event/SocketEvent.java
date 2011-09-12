package com.ttProject.udpp2p.server.event;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.ttProject.udpp2p.server.adapter.ServerAdapter;

/**
 * ソケット命令やりとり用クラス
 * @author taktod
 */
public class SocketEvent extends Thread {
	/** 関連づけられているアダプター */
	private final ServerAdapter adapter;
	private final int port = 12345;
	private DatagramSocket socket = null;
	/**
	 * コンストラクタ
	 * @param adapter
	 * @throws SocketException 
	 */
	public SocketEvent(ServerAdapter adapter) throws SocketException {
		this.adapter = adapter;
		socket = new DatagramSocket(port);
	}
	/**
	 * Socket待ち受け動作
	 */
	@Override
	public void run() {
		DatagramPacket recvPacket;
		ExecutorService execService = Executors.newCachedThreadPool();
		while(true) {
			try {
				recvPacket = new DatagramPacket(new byte[1024], 1024);
				socket.receive(recvPacket); // 次の命令を受け取るまで止まっている。
				execService.execute(new SocketJob(recvPacket));
			}
			catch (IOException e) {
			}
		}
	}
	/**
	 * 処理実体
	 * @author taktod
	 */
	private class SocketJob implements Runnable {
		private DatagramPacket packet;
		public SocketJob(DatagramPacket packet) {
			this.packet = packet;
		}
		@Override
		public void run() {
			// データを取得したらadapterで処理を実行する。
			adapter.dataJob(packet);
		}
	}
	/**
	 * ソケットデータの取得
	 * @return
	 */
	public DatagramSocket getSocket() {
		return socket;
	}
}
