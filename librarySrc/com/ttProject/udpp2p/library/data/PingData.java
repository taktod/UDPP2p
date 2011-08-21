package com.ttProject.udpp2p.library.data;

import com.ttProject.udpp2p.library.json.JsonData;

/**
 * Pingやりとりメッセージ
 * @author taktod
 */
public class PingData implements Data {
	private String ping;
	/**
	 * コンストラクタ
	 */
	public PingData() {
		ping = "ping";
	}
	/**
	 * コンストラクタ
	 * @param data
	 */
	public PingData(JsonData data) {
		this();
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String encode() {
		JsonData data = new JsonData();
		data.put("message", ping);
		return data.encode();
	}
}
