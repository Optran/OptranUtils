package com.github.optran.utils.crypto.rc4;

public class RC4Hash {
	private RC4 rc4;
	private final int size;
	private byte[] memory;
	private int state;

	public RC4Hash(int size) {
		this.size = size;
		memory = new byte[size];
		rc4 = new RC4();
		reset();
	}

	public void reset() {
		rc4.reset();
		rc4.seed("Optran Hash");
		state = 0;
		for (int i = 0; i < memory.length; i++) {
			memory[i] = (byte) 0xFF;
		}
	}

	public void update(byte[] data) {
		if (null == data) {
			return;
		}
		if (0 == data.length) {
			return;
		}
		for (int i = 0; i < data.length; i++) {
			update(data[i]);
		}
	}

	public void update(byte data) {
		if (state < size) {
			memory[state] = data;
			state++;
		}
		if (state == size) {
			hash();
		}
	}

	private void hash() {
		rc4.seed(memory);
		state = 0;
	}

	public byte[] digest(byte[] data) {
		update(data);
		data = digest();
		return data;
	}

	public byte[] digest() {
		while (state != 0) {
			update((byte) 0);
		}
		byte[] retVal = new byte[size];
		for (int i = 0; i < retVal.length; i++) {
			retVal[i] = rc4.next();
		}
		reset();
		return retVal;
	}
}
