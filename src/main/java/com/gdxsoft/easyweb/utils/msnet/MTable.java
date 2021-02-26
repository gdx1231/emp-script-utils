package com.gdxsoft.easyweb.utils.msnet;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;

/**
 * 用于保存数据的Hash表，主要目的是简化使用和便于.net的生产
 * 
 * @author Administrator
 * 
 */
public class MTable implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6005653302990234891L;

	/**
	 * 从hash表中生成MTable
	 * 
	 * @param maps HashMap
	 * @return MTable
	 */
	public static MTable instanceOf(HashMap<Object, Object> maps) {
		MTable tb = new MTable();
		tb._Table = maps;
		Iterator<Object> it = maps.keySet().iterator();
		while (it.hasNext()) {
			tb._Keys.add(it.next());
		}
		return tb;
	}

	/**
	 * 连接成字符串，例如：<br>
	 * 如果表中值为<br>
	 * equlsString 为"="，splitString为 "&"<br>
	 * 结果是 name=张三&id=123&address=北京海淀区
	 * 
	 * @param equlsString key和value直接的连接字符串
	 * @param splitString 不同值之间的分隔字符串
	 * @return 连接成字符串
	 */
	public String join(String equlsString, String splitString) {
		MStr s = new MStr();
		for (int i = 0; i < this.getCount(); i++) {
			Object key = this.getKey(i);
			Object val = this.getByIndex(i);

			String key1 = key == null ? "" : key.toString();
			String val1 = val == null ? "" : val.toString();
			if (i > 0) {
				s.append(splitString);
			}
			s.append(key1);
			s.append(equlsString);
			s.append(val1);
		}
		return s.toString();
	}

	/**
	 * 连接两个hash表，如果isOverWrite = true则强制替换已有的值
	 * 
	 * @param table       MTable
	 * @param isOverWrite 是否强制替换
	 */
	public void conact(MTable table, boolean isOverWrite) {
		if (table == null)
			return;
		this.conact(table._Table, isOverWrite);
	}

	/**
	 * 连接两个hash表，如果isOverWrite = true则强制替换已有的值
	 * 
	 * @param maps        HashMap
	 * @param isOverWrite 是否强制替换
	 */
	public void conact(HashMap<Object, Object> maps, boolean isOverWrite) {
		if (maps == null)
			return;

		Iterator<Object> it = maps.keySet().iterator();
		while (it.hasNext()) {
			Object key = it.next();
			if (this._Table.containsKey(key)) {
				if (isOverWrite) {
					this.removeKey(key);
				} else {
					continue;
				}
			}
			this._Table.put(key, maps.get(key));
			this._Keys.add(key);
		}
	}

	private HashMap<Object, Object> _Table = new HashMap<Object, Object>();
	/**
	 * 放置索引的表，索引，key值
	 */
	private MList _Keys = new MList();

	/**
	 * 重新初始化
	 */
	public void reset() {
		_Table.clear();
		_Keys.clear();
	}

	/**
	 * 清空
	 */
	public void clear() {
		this.reset();
	}

	/**
	 * 排序
	 */
	public void sort() {
		this._Keys.sort();
	}

	/**
	 * 增加对象
	 * 
	 * @param key 对象的key
	 * @param val 对象
	 */
	public void put(Object key, Object val) {
		this.add(key, val);
	}

	/**
	 * 增加对象
	 * 
	 * @param key 对象的key
	 * @param val 对象
	 */
	public void add(Object key, Object val) {
		if (_Table.containsKey(key)) {
			_Table.remove(key);
			_Keys.removeValue(key);

		}
		_Table.put(key, val);
		_Keys.add(key);

	}

	/**
	 * 对象是否包含key
	 * 
	 * @param key 对象的key
	 * @return true/false
	 */
	public boolean containsKey(Object key) {
		return this._Table.containsKey(key);
	}

	/**
	 * 根据索引移除对象
	 * 
	 * @param index 索引
	 * @return 移除的对象
	 */
	public Object removeAt(int index) {
		Object key;
		if (index >= 0 && index < _Keys.size()) {
			key = _Keys.get(index);
			Object v = _Table.remove(key);
			_Keys.removeAt(index);
			return v;
		} else {
			return null;
		}
	}

	/**
	 * 根据Key值移除对象
	 * 
	 * @param key 对象的key
	 * @return 移除的对象
	 */
	public Object removeKey(Object key) {
		if (this._Table.containsKey(key)) {
			Object val = _Table.remove(key);
			int index = _Keys.indexOf(key);
			_Keys.removeAt(index);
			return val;
		} else {
			return null;
		}
	}

	/**
	 * 对象数量
	 * 
	 * @return 数量
	 */
	public int size() {
		return this._Table.size();
	}

	/**
	 * 对象数量
	 * 
	 * @return 数量
	 */
	public int getCount() {
		return this._Table.size();
	}

	/**
	 * 获取对象的key
	 * 
	 * @param index 索引
	 * @return 对象的key
	 */
	public Object getKey(int index) {
		return this._Keys.get(index);
	}

	/**
	 * 获取对象
	 * 
	 * @param index 索引
	 * @return 对象
	 */
	public Object getByIndex(int index) {
		Object key = this._Keys.get(index);
		return this._Table.get(key);
	}

	/**
	 * 获取对象
	 * 
	 * @param key 对象的key
	 * @return 对象
	 */
	public Object get(Object key) {
		return this._Table.get(key);
	}

	/**
	 * 返回map
	 * 
	 * @return the _Table
	 */
	public HashMap<Object, Object> getTable() {
		return _Table;
	}

}
