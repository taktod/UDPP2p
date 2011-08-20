package com.ttProject.udpp2p.library;

import com.ttProject.udpp2p.library.json.JsonData;

public class TestEntry {
	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		String data = "{\"a\":123,\"b\":234,\"c\":\"test\"}";
//		String data = "[123,456,true,\"hello!!\"]";
//		System.out.println(data);
		JsonData jdata = new JsonData();
		jdata.add("test");
		jdata.add(135);
//		jdata.put("a", "test");
//		jdata.put("b", true);
		System.out.println(jdata.toString());
		System.out.println(jdata.encode());
	}
}
