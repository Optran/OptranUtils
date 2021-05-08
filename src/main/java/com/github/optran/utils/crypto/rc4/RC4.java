package com.github.optran.utils.crypto.rc4;

public class RC4 {
	private final int[] data;
	private int state_i;
	private int state_j;

	public RC4() {
		data = new int[256];
		reset();
	}

	public void reset() {
		for (int i = 0; i < data.length; i++) {
			data[i] = i;
		}
		state_i = 0;
		state_j = 0;
	}

	public void seed(String key) {
		if (null == key) {
			return;
		}
		if (0 == key.length()) {
			return;
		}
		seed(key.getBytes());
	}

	public void seed(byte[] key) {
		if (null == key) {
			return;
		}
		if (0 == key.length) {
			return;
		}
		int n = (data.length > key.length) ? data.length : key.length;
		int i = 0;
		int j = 0;
		for (int index = 0; index < n; index++) {
			i = index % data.length;
			j = (j + data[i] + (0xFF & key[index % key.length])) % data.length;
			swap(i, j);
		}
	}

	public byte next() {
		state_i = (state_i + 1) % data.length;
		state_j = (state_j + data[state_i]) % data.length;
		swap(state_i, state_j);
		return (byte) data[(data[state_i] + data[state_j]) % data.length];
	}

	private void swap(int i, int j) {
		if (i == j) {
			return;
		}
		int temp = data[i];
		data[i] = data[j];
		data[j] = temp;
	}
}
