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
		UdpP2pServerAdapter adapter = new UdpP2pServerAdapter();
		adapter.start();
	}
}
