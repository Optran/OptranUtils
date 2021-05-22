package com.github.optran.utils.disk.heap;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import com.github.optran.utils.LimitedByteArrayOutputStream;
import com.github.optran.utils.PrimitiveSerializationUtils;
import com.github.optran.utils.exceptions.RuntimeIOException;

public class FreeListBlock {
	private AllocHeader allocHeader;
	private long previousNode;
	private long nextNode;
	private int size;
	public FreeListBlock(int size) {
		this.size = size;
		allocHeader = new AllocHeader();
		allocHeader.setAllocated(false);
		allocHeader.setSize(size);
		previousNode = 0;
		nextNode = 0;
	}
	public FreeListBlock(byte[]data) {
		setBytes(data);
	}
	public AllocHeader getAllocHeader() {
		return allocHeader;
	}
	public void setAllocHeader(AllocHeader allocHeader) {
		this.allocHeader = allocHeader;
	}
	public long getPreviousNode() {
		return previousNode;
	}
	public void setPreviousNode(long previousNode) {
		this.previousNode = previousNode;
	}
	public long getNextNode() {
		return nextNode;
	}
	public void setNextNode(long nextNode) {
		this.nextNode = nextNode;
	}
	public byte[] toBytes() {
		byte[]metadata = allocHeader.toBytes();
		LimitedByteArrayOutputStream lbaos = new LimitedByteArrayOutputStream(size);
		try {
			lbaos.write(metadata);
			lbaos.write(PrimitiveSerializationUtils.longToByteArr(previousNode));
			lbaos.write(PrimitiveSerializationUtils.longToByteArr(nextNode));
			lbaos.skip(size-24);
			lbaos.write(metadata);
			lbaos.close();
		} catch (IOException e) {
			throw new RuntimeIOException(e);
		}
		return lbaos.toByteArray();
	}
	public void setBytes(byte[]block) {
		size = block.length;
		byte[]header = new byte[4];
		byte[]previousNodeBytes = new byte[8];
		byte[]nextNodeBytes = new byte[8];
		ByteArrayInputStream bais = new ByteArrayInputStream(block);
		try {
			bais.read(header);
			bais.read(previousNodeBytes);
			bais.read(nextNodeBytes);
		} catch (IOException e) {
			throw new RuntimeIOException(e);
		}
		allocHeader = new AllocHeader();
		allocHeader.setBytes(header);
		previousNode = PrimitiveSerializationUtils.byteArrToLong(previousNodeBytes);
		nextNode = PrimitiveSerializationUtils.byteArrToLong(nextNodeBytes);
	}
}
