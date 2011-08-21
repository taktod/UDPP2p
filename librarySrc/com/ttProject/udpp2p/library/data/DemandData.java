package com.ttProject.udpp2p.library.data;

import com.ttProject.udpp2p.library.json.JsonData;

/**
 * クライアントが特定のクライアントと接続を要求するときに送ってくるデータ
 * @author taktod
 */
public class DemandData implements Data {
	public DemandData() {
		
	}
	public DemandData(JsonData data) {
		this();
	}
	@Override
	public String encode() {
		JsonData data = new JsonData();
		data.put("message", "demand");
		return data.encode();
	}
	public static int hash() {
		return "demand".hashCode();
	}
}
