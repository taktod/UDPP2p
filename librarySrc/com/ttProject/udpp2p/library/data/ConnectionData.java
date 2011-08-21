package com.ttProject.udpp2p.library.data;

import com.ttProject.udpp2p.library.json.JsonData;

/**
 * クライアントがサーバーに接続するときにおくってくるデータ
 * @author taktod
 */
public class ConnectionData implements Data {
	/** ID */
	private Long id = null;
	/** 接続ターゲット */
	private Long target = null;
	/** ローカルアクセスのためのアドレス(ユーザー判断で送信するデータ) */
	private String localAddress = null;
	/** ローカルアクセスのためのポート(ユーザー判断で送信するデータ) */
	private Integer port = null;
	/**
	 * コンストラクタ
	 */
	public ConnectionData() {
	}
	/**
	 * コンストラクタ
	 * @param data
	 */
	public ConnectionData(JsonData data) {
		this();
		if(data.get("id") == null) {
			return;
		}
		id = Long.parseLong((String)data.get("id"));
		if(data.get("target") == null) {
			return;
		}
		if("system".equals(data.get("target"))) {
			target = -1L;
		}
		else {
			target = Long.parseLong((String)data.get("target"));
		}
		localAddress = (String)data.get("localAddress");
		port = (Integer)data.get("port");
	}
	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}
	/**
	 * @return the target
	 */
	public Long getTarget() {
		return target;
	}
	/**
	 * @param target the target to set
	 */
	public void setTarget(Long target) {
		this.target = target;
	}
	/**
	 * @return the localAddress
	 */
	public String getLocalAddress() {
		return localAddress;
	}
	/**
	 * @param localAddress the localAddress to set
	 */
	public void setLocalAddress(String localAddress) {
		this.localAddress = localAddress;
	}
	/**
	 * @return the port
	 */
	public Integer getPort() {
		return port;
	}
	/**
	 * @param port the port to set
	 */
	public void setPort(Integer port) {
		this.port = port;
	}
	/**
	 * {@inheritDoc}
	 */
	@Override
	public String encode() {
		// Jsonデータ化を実行し、encodeする。
		JsonData data = new JsonData();
		data.put("message", "conn");
		if(id != null) {
			data.put("id", id.toString());
		}
		if(target != null) {
			if(target == -1L) { 
				data.put("target", "system");
			}
			else {
				data.put("target", target.toString());
			}
		}
		return data.encode();
	}
}
