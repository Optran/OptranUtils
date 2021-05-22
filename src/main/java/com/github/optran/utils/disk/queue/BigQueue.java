package com.github.optran.utils.disk.queue;

import static com.github.optran.utils.ExternalizableSerializationUtils.deserializeExternalizable;
import static com.github.optran.utils.ExternalizableSerializationUtils.serializeExternalizable;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Optional;

import com.github.optran.utils.disk.heap.DiskHeap;

public class BigQueue {
	private long header;
	private DiskHeap heap;

	public BigQueue(DiskHeap heap) {
		this.heap = heap;
		BigQueueNodeHeader headerNode = new BigQueueNodeHeader();
		saveHeader(headerNode);
	}

	public BigQueue(long header, DiskHeap heap) {
		this.header = header;
		this.heap = heap;
	}

	protected long getHeader() {
		return header;
	}

	protected void setHeader(long header) {
		this.header = header;
	}

	private void saveHeader(BigQueueNodeHeader headerNode) {
		byte[] data = serializeExternalizable(headerNode);
		if (0 == header) {
			header = heap.malloc(data.length);
		}
		heap.write(header, data);
	}

	private BigQueueNodeHeader readHeader() {
		BigQueueNodeHeader headerNode = new BigQueueNodeHeader();
		deserializeExternalizable(heap.read(header), headerNode);
		return headerNode;
	}

	private long newBigQueueNode(byte[] data) {
		BigQueueNode node = new BigQueueNode();
		node.setData(data);
		node.setNextNode(0);
		data = serializeExternalizable(node);
		long reference = heap.malloc(data.length);
		heap.write(reference, data);
		return reference;
	}

	private BigQueueNode readBigQueueNode(long reference) {
		BigQueueNode node = new BigQueueNode();
		deserializeExternalizable(heap.read(reference), node);
		return node;
	}

	private void writeBigQueueNode(long reference, BigQueueNode node) {
		byte[] data = serializeExternalizable(node);
		heap.write(reference, data);
	}

	public void push(byte[] data) {
		BigQueueNodeHeader headerNode = readHeader();
		if (0 == headerNode.getSize()) {
			long tailRef = headerNode.getTail();
			tailRef = newBigQueueNode(data);
			headerNode.setTail(tailRef);
			headerNode.setHead(tailRef);
		} else {
			long tailRef = headerNode.getTail();
			BigQueueNode tailNode = readBigQueueNode(tailRef);
			long newTailRef = newBigQueueNode(data);
			BigQueueNode newTailNode = readBigQueueNode(newTailRef);
			tailNode.setNextNode(newTailRef);
			writeBigQueueNode(tailRef, tailNode);
			writeBigQueueNode(newTailRef, newTailNode);
			headerNode.setTail(newTailRef);
		}
		headerNode.setSize(headerNode.getSize() + 1);
		saveHeader(headerNode);
	}

	public Optional<byte[]> peek() {
		BigQueueNodeHeader headerNode = readHeader();
		if (0 == headerNode.getSize()) {
			return null;
		} else {
			long headRef = headerNode.getHead();
			BigQueueNode headNode = readBigQueueNode(headRef);
			return Optional.ofNullable(headNode.getData());
		}
	}

	public Optional<byte[]> pop() {
		BigQueueNodeHeader headerNode = readHeader();
		Optional<byte[]>retVal = null;
		if (0 == headerNode.getSize()) {
			return null;
		} else {
			long headRef = headerNode.getHead();
			BigQueueNode headNode = readBigQueueNode(headRef);
			retVal = Optional.ofNullable(headNode.getData());
			long nextHeadRef = headNode.getNextNode();
			heap.free(headRef);
			headerNode.setHead(nextHeadRef);
			headerNode.setSize(headerNode.getSize() - 1);
		}
		saveHeader(headerNode);
		return retVal;
	}

	private class BigQueueNodeHeader implements Externalizable {
		private long head;
		private long tail;
		private long size;

		protected long getHead() {
			return head;
		}

		protected void setHead(long head) {
			this.head = head;
		}

		protected long getTail() {
			return tail;
		}

		protected void setTail(long tail) {
			this.tail = tail;
		}

		protected long getSize() {
			return size;
		}

		protected void setSize(long size) {
			this.size = size;
		}

		@Override
		public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
			head = in.readLong();
			tail = in.readLong();
			size = in.readLong();
		}

		@Override
		public void writeExternal(ObjectOutput out) throws IOException {
			out.writeLong(head);
			out.writeLong(tail);
			out.writeLong(size);
		}
	}

	private class BigQueueNode implements Externalizable {
		private long nextNode;
		private byte[] data;

		protected long getNextNode() {
			return nextNode;
		}

		protected void setNextNode(long nextNode) {
			this.nextNode = nextNode;
		}

		protected byte[] getData() {
			return data;
		}

		protected void setData(byte[] data) {
			this.data = data;
		}

		@Override
		public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
			nextNode = in.readLong();
			if (in.readBoolean()) {
				int length = in.readInt();
				data = new byte[length];
				in.read(data);
			}
		}

		@Override
		public void writeExternal(ObjectOutput out) throws IOException {
			out.writeLong(nextNode);
			if (null == data) {
				out.writeBoolean(false);
			} else {
				out.writeBoolean(true);
				out.writeInt(data.length);
				out.write(data);
			}
		}
	}
}
