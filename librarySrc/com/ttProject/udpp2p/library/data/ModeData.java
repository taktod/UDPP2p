package com.ttProject.udpp2p.library.data;

import com.ttProject.udpp2p.library.json.JsonData;

/**
 * サーバーがクライアントに処理完了したときにモードを指定するときにおくってくるデータ
 * @author taktod
 */
public class ModeData implements Data {
	/** クライアントのID */
	private Long id = null;
	/** 接続状態 */
	private Long target = null;
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
		id = Long.parseLong((String)data.get("id"));
		target = Long.parseLong((String)data.get("target"));
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
	 * {@inheritDoc}
	 */
	@Override
	public String encode() {
		JsonData data = new JsonData();
		data.put("message", "mode");
		if(id != null) {
			data.put("id", id.toString());
		}
		if(target != null) {
			data.put("target", target.toString());
		}
		return data.encode();
	}
}
