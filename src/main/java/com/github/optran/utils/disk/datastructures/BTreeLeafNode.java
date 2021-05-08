package com.github.optran.utils.disk.datastructures;

public class BTreeLeafNode<Key, Value> {
	private BTreeEntry<Key, Value>[]entries;
	@SuppressWarnings("unchecked")
	public BTreeLeafNode(int capacity) {
		entries = new BTreeEntry[capacity];
	}
}
