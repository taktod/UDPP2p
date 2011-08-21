package com.ttProject.udpp2p.library.data;

import com.ttProject.udpp2p.library.json.JsonData;

/**
 * サーバーがクライアントに処理完了したときにモードを指定するときにおくってくるデータ
 * @author taktod
 */
public class ModeData implements Data {
	/**
	 * コンストラクタ
	 */
	public ModeData() {
	}
	/**
	 * コンストラクタ
	 * @param data
	 */
	public ModeData(JsonData data) {
		this();
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String encode() {
		JsonData data = new JsonData();
		data.put("message", "mode");
		return data.encode();
	}
}
