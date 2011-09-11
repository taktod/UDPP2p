package com.ttProject.udpp2p.library.data;

import com.ttProject.udpp2p.library.json.JsonData;

/**
 * クライアント同士の接続をするためにサーバーから送られてくる自分とつながる対象のデータ
 * @author taktod
 */
public class ConnectTargetData implements Data {
	/** 外部からのアクセス用のアドレス */
	private String address;
	/** 外部からのアクセス用のポート */
	private Integer port;
	/** LAN内部の場合のアクセス用アドレス */
	private String localAddress;
	/** LAN内部の場合のアクセス用ポート */
	private Integer localPort;
	/** クライアントに与えられているID番号 */
	private Long id;
	/**
	 * コンストラクタ
	 */
	public ConnectTargetData() {
	}
	/**
	 * コンストラクタ
	 * @param data
	 */
	public ConnectTargetData(JsonData data) {
		this();
		address = (String)data.get("address");
		port = Integer.parseInt((String)data.get("port"));
		id = Long.parseLong((String)data.get("id"));
		localAddress = (String)data.get("localAddress");
		localPort = Integer.parseInt((String)data.get("localPort"));
	}
	/**
	 * @return the address
	 */
	public String getAddress() {
		return address;
	}
	/**
	 * @param address the address to set
	 */
	public void setAddress(String address) {
		this.address = address;
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
	 * @return the localPort
	 */
	public Integer getLocalPort() {
		return localPort;
	}
	/**
	 * @param localPort the localPort to set
	 */
	public void setLocalPort(Integer localPort) {
		this.localPort = localPort;
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
	 * エンコード動作
	 */
	@Override
	public String encode() {
		JsonData data = new JsonData();
		data.put("address", address);
		data.put("port", port.toString());
		data.put("id", id.toString());
		if(localAddress != null && localPort != null) {
			data.put("localAddress", localAddress);
			data.put("localPort", localPort.toString());
		}
		return null;
	}
}
