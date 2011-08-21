package com.ttProject.udpp2p.library.data;

import com.ttProject.udpp2p.library.json.JsonData;

/**
 * クライアント、サーバーがHandshakeのデータをやり取りするときにおくってくるデータ
 * @author taktod
 */
public class HandshakeData implements Data {
	/** サーバーから送信されるトークンデータ */
	private Long token = null;
	/** クライアントから送信されるトークンデータ */
	private String stringToken = null;
	/**
	 * コンストラクタ
	 */
	public HandshakeData() {
	}
	/**
	 * コンストラクタ
	 * @param data
	 */
	public HandshakeData(JsonData data) {
		this();
		if(data.get("token") != null) {
			token = Long.parseLong((String)data.get("token"));
		}
		if(data.get("stringToken") != null) {
			stringToken = (String)data.get("stringToken");
		}
	}
	/**
	 * @return the token
	 */
	public Long getToken() {
		return token;
	}
	/**
	 * @param token the token to set
	 */
	public void setToken(Long token) {
		this.token = token;
	}
	/**
	 * @return the stringToken
	 */
	public String getStringToken() {
		return stringToken;
	}
	/**
	 * @param stringToken the stringToken to set
	 */
	public void setStringToken(String stringToken) {
		this.stringToken = stringToken;
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String encode() {
		JsonData data = new JsonData();
		data.put("message", "handshake");
		if(token != null) {
			data.put("token", token.toString());
		}
		if(stringToken != null) {
			data.put("stringToken", stringToken);
		}
		return data.encode();
	}
}
