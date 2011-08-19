package com.ttProject.udpp2p.client;

import com.ttProject.udpp2p.client.instance.UdpP2pServerAdapter;

/*
 * 今回のプロジェクトの目標は、Factoryメソッドを利用してUDPホールパンチングによるP2Pを実現するプログラムをつくっておく。
 * なおサーバーとクライアントは別に動作できるようにしておく。(プログラムごとわけてもいいかも。)
 */
public class ClientEntry {
	/**
	 * CUIの導入ポイント
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		new ClientEntry();
	}
	/**
	 * コンストラクタ
	 * @throws Exception
	 */
	public ClientEntry() throws Exception {
//		System.out.println(Long.parseLong("34a36d8e8a6fb0a", 16));
//		UdpP2pServer ups = new UdpP2pServer();
//		ups.connect();
		UdpP2pServerAdapter adapter = new UdpP2pServerAdapter();
		adapter.start();
		// 試しにデータをおくってみる。
/*		final DatagramSocket socket = new DatagramSocket();
		socket.send(new DatagramPacket("conn".getBytes(), 0, "conn".getBytes().length, new InetSocketAddress("localhost", 12345)));
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(1000);
				}
				catch (Exception e) {
					// TODO: handle exception
				}
				System.out.println("ソケットクローズ");
				socket.close();
			}
		});
		t.start(); // * /
		System.out.println("socket待ち開始");
		try {
			while(true) {
				DatagramPacket packet = new DatagramPacket(new byte[1024], 1024);
				socket.receive(packet);
				System.out.println(new String(packet.getData()));
			}
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}// */
		System.out.println("end");
	}
}
