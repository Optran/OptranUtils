package com.github.optran.utils.disk.heap;

import com.github.optran.utils.exceptions.IncorrectInitializationValueException;

public class AllocHeader {
	private boolean allocated;
	private boolean prevAllocated;
	private boolean extended;
	private int size;
	public AllocHeader() {
	}
	public boolean isAllocated() {
		return allocated;
	}
	public void setAllocated(boolean allocated) {
		this.allocated = allocated;
	}
	public boolean isPrevAllocated() {
		return prevAllocated;
	}
	public void setPrevAllocated(boolean prevAllocated) {
		this.prevAllocated = prevAllocated;
	}
	public boolean isExtended() {
		return extended;
	}
	public void setExtended(boolean extended) {
		this.extended = extended;
	}
	public int getSize() {
		return size;
	}
	public void setSize(int size) {
		if(size>268435456) {
			throw new IncorrectInitializationValueException("The size in the alloc header cannot be more than 268435456");
		}
		this.size = size;
	}
	public byte[] toBytes() {
		int flagBitmap = 0;
		if(allocated) {
			flagBitmap = flagBitmap ^ 4;
		}
		if(prevAllocated) {
			flagBitmap = flagBitmap ^ 2;
		}
		if(extended) {
			flagBitmap = flagBitmap ^ 1;
		}

		int size = this.size;
		byte[] bytes = new byte[4];
		bytes[3] = (byte)(0xFF & size);
		size = size>>>8;
		bytes[2] = (byte)(0xFF & size);
		size = size>>>8;
		bytes[1] = (byte)(0xFF & size);
		size = size>>>8;
		bytes[0] = (byte)((0x1F & size)^((flagBitmap<<5)&0xe0));

		return bytes;
	}
	public void setBytes(byte[] bytes) {
		if(null==bytes) {
			throw new NullPointerException("The alloc header expects its input to not be null.");
		}
		if(4!=bytes.length) {
			throw new ArrayIndexOutOfBoundsException("The alloc header expects its input to not be of size 4 and not "+bytes.length+".");
		}
		int flagBitmap = bytes[0]&0xe0;
		flagBitmap = flagBitmap>>>5;
		allocated = ((flagBitmap & 4)>>>2) == 1;
		prevAllocated = ((flagBitmap & 2)>>>1) == 1;
		extended = (flagBitmap & 1) == 1;
		bytes[0] = (byte)(bytes[0]&0x1f);

		size = 0xFF & bytes[0];
		size = size<<8;
		size = size ^ (0xFF & bytes[1]);
		size = size<<8;
		size = size ^ (0xFF & bytes[2]);
		size = size<<8;
		size = size ^ (0xFF & bytes[3]);
	}
}
