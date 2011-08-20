package com.ttProject.udpp2p.library.json;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Jsonデータ
 * [a,b,c]
 * {"key":val,"key":val}
 * 複数ネストさせない。
 * @author taktod
 */
public class JsonData {
	/** データ保持用マップ */
	private Map<String, Object> map = null;
	/** データ保持用リスト */
	private List<Object> list = null;
	/**
	 * コンストラクタ
	 * @param jsonString
	 */
	public JsonData(String jsonString) {
		decode(jsonString);
	}
	/**
	 * 文字列を利用してJsonDataを初期化する。
	 * @param jsonString
	 */
	public void decode(String jsonString) {
		// 文字列を解析する
		if(jsonString == null) {
			return;
		}
		String dat = jsonString.trim();
		char first, last;
		first = dat.charAt(0);
		last = dat.charAt(dat.length()-1);
		if(first == '[' && last == ']') {
			// list
			setupList(dat.substring(1, dat.length()-1));
		}
		else if(first == '{' && last == '}') {
			// map
			setupMap(dat.substring(1, dat.length()-1));
		}
	}
	/**
	 * 配列データの初期化
	 * @param data
	 */
	private void setupList(String data) {
		list = new ArrayList<Object>();
		map = null;
		for(String dat : data.split(",")) {
			list.add(getElement(dat));
		}
	}
	/**
	 * オブジェクトデータの初期化
	 * @param data
	 */
	private void setupMap(String data) {
		map = new HashMap<String, Object>();
		list = null;
		for(String dat : data.split(",")) {
			String[] element = dat.split(":", 2);
			String key = getString(element[0]);
			if(key == null) {
				continue;
			}
			map.put(key, getElement(element[1]));
		}
	}
	/**
	 * 要素のコンバート
	 * @param dat
	 * @return
	 */
	private Object getElement(String dat) {
		if(dat == null) {
			return null;
		}
		String element = dat.trim();
		// 文字列
		if(element.startsWith("\"")) {
			return getString(element);
		}
		element = element.toLowerCase();
		// boolean
		if(element.indexOf("true") != -1) {
			return new Boolean(true);
		}
		if(element.indexOf("false") != -1) {
			return new Boolean(false);
		}
		// 数値(指数を無視している)
		try {
			return Integer.parseInt(element);
		}
		catch (Exception e) {
			return null;
		}
	}
	/**
	 * 文字列を解析する。
	 * @param element
	 * @return
	 */
	public String getString(String element) {
		// 文字列
		char first, last;
		try {
			first = element.charAt(0);
			last = element.charAt(element.length()-1);
			// "で囲まれているか確認
			if(first != '"' || last != '"') {
				return null;
			}
			// 本来ならエンコードデータを処理する必要あり。\nとか
			element = element.substring(1, element.length()-1);
			return element;
		}
		catch (Exception e) {
			return null;
		}
	}
	/**
	 * クラスをJson文字列に変換する。
	 * @return
	 */
	public String encode() {
		if(list != null) {
			return encodeList();
		}
		if(map != null) {
			return encodeMap();
		}
		return null;
	}
	public String encodeList() {
		/*
		 * [135,163,TRUE,"aiueo"]とかにする。
		 */
		StringBuilder str = new StringBuilder();
		str.append("[");
		Iterator<Object>iter = list.iterator();
		while(iter.hasNext()) {
			Object obj = iter.next();
			if(obj instanceof String) {
				str.append('"');
				// stringはエンコードしてやる必要あり。
				str.append(encodeString((String)obj));
				str.append('"');
			}
			else {
				str.append(obj);
			}
			if(iter.hasNext()) {
				str.append(",");
			}
		}
		str.append("]");
		return str.toString();
	}
	private String encodeString(String data) {
		return data;
	}
	public String encodeMap() {
		/*
		 * {"a":123,"b":456,"c":true}
		 */
		StringBuilder str = new StringBuilder();
		str.append("{");
		Iterator<Entry<String, Object>> iter = map.entrySet().iterator();
		while(iter.hasNext()) {
			Entry<String, Object> element = iter.next();
			// keyを書き込む
			str.append('"');
			str.append(encodeString(element.getKey()));
			str.append('"');
			str.append(':');
			// elementを書き込む
			Object obj = element.getValue();
			if(obj instanceof String) {
				str.append('"');
				// stringはエンコードしてやる必要あり。
				str.append(encodeString((String)obj));
				str.append('"');
			}
			else {
				str.append(obj);
			}
			if(iter.hasNext()) {
				str.append(",");
			}
		}
		str.append("}");
		return str.toString();
	}
	/**
	 * データのクリア
	 */
	public void clear() {
		if(list != null) {
			list.clear();
		}
		if(map != null) {
			map.clear();
		}
	}
	/**
	 * 内容表示
	 */
	@Override
	public String toString() {
		if(list != null) {
			return list.toString();
		}
		if(map != null) {
			return map.toString();
		}
		return null;
	}
/*	// Map
	public boolean containsKey(Object key) {
		return false;
	}
	public boolean containsValue(Object value) {
		return false;
	}
	public Set<java.util.Map.Entry<String, Object>> entrySet() {
		return null;
	}
	public Object get(Object key) {
		return null;
	}
	public boolean isEmpty() {
		return false;
	}
	public Set<String> keySet() {
		return null;
	}
	public Object put(String key, Object value) {
		return null;
	}
	public void putAll(Map<? extends String, ? extends Object> m) {
	}
	public Object remove(Object key) {
		return null;
	}
	public int size() {
		return 0;
	}
	public Collection<Object> values() {
		return null;
	}// */
	// list
	public boolean add(Object e) {
		return false;
	}
	public void add(int index, Object element) {
	}
	public boolean addAll(Collection<? extends Object> c) {
		return false;
	}
	public boolean addAll(int index, Collection<? extends Object> c) {
		return false;
	}
	public boolean contains(Object o) {
		return false;
	}
	public boolean containsAll(Collection<?> c) {
		return false;
	}
	public Object get(int index) {
		return null;
	}
	public int indexOf(Object o) {
		return 0;
	}
	public boolean isEmpty() {
		return false;
	}
	public Iterator<Object> iterator() {
		return null;
	}
	public int lastIndexOf(Object o) {
		return 0;
	}
	public ListIterator<Object> listIterator() {
		return null;
	}
	public ListIterator<Object> listIterator(int index) {
		return null;
	}
	public boolean remove(Object o) {
		return false;
	}
	public Object remove(int index) {
		return null;
	}
	public boolean removeAll(Collection<?> c) {
		return false;
	}
	public boolean retainAll(Collection<?> c) {
		return false;
	}
	public Object set(int index, Object element) {
		return null;
	}
	public int size() {
		return 0;
	}
	public List<Object> subList(int fromIndex, int toIndex) {
		return null;
	}
	public Object[] toArray() {
		return null;
	}
	public <T> T[] toArray(T[] a) {
		return null;
	}
}
