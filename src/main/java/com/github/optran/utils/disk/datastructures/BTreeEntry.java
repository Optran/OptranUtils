package com.github.optran.utils.disk.datastructures;

public class BTreeEntry<Key, Value> {
	private Key key;
	private Value value;
	public BTreeEntry(Key key, Value value) {
		super();
		this.key = key;
		this.value = value;
	}
	public Key getKey() {
		return key;
	}
	public void setKey(Key key) {
		this.key = key;
	}
	public Value getValue() {
		return value;
	}
	public void setValue(Value value) {
		this.value = value;
	}
	@Override
	public String toString() {
		return "BTreeEntry [key=" + key + ", value=" + value + "]";
	}
}
