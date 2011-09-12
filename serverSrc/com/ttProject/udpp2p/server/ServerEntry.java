package com.ttProject.udpp2p.server;

import com.ttProject.udpp2p.server.adapter.ServerAdapter;

/**
 * サーバー内部の動作
 * @author taktod
 */
public class ServerEntry {
	/**
	 * メインエントリー
	 * @param args
	 */
	public static void main(String[] args) {
		new ServerEntry();
	}
	/**
	 * 導入動作
	 */
	public ServerEntry() {
		ServerAdapter adapter = new ServerAdapter();
		adapter.start();
		System.out.println("startup end...");
	}
}
