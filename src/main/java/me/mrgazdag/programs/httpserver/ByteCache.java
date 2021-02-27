package me.mrgazdag.programs.httpserver;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ByteCache {
	private final int chunkSize;
	private final List<byte[]> bytes;
	private volatile byte[] lastChunk;
	private final AtomicInteger nextIndex = new AtomicInteger(0);
	private OutputStream outputStream;
	
	public ByteCache() {
		this.bytes = new ArrayList<>();
		this.chunkSize = 2048;
		incrementList();
	}
	@SuppressWarnings("unused")
	public ByteCache(int chunkSize) {
		this.bytes = new ArrayList<>();
		this.chunkSize = chunkSize;
		incrementList();
	}
	private void check() {
		if (nextIndex.get() == chunkSize) {
			incrementList();
		}
	}
	public ByteCache clear() {
		nextIndex.set(chunkSize);
		incrementList();
		this.bytes.clear();
		return this;
	}
	private void incrementList() {
		if (lastChunk != null) bytes.add(lastChunk);
		lastChunk = new byte[chunkSize];
		nextIndex.set(0);
	}
	public void copyTo(OutputStream stream) throws IOException {
		for (byte[] bs : bytes) {
			stream.write(bs);
		}
		stream.write(lastChunk, 0, nextIndex.get());
	}
	public void write(byte b) {
		lastChunk[nextIndex.get()] = b;
		nextIndex.incrementAndGet();
		check();
	}
	public void write(byte[] b) {
		write(b,0,b.length);
	}
	@SuppressWarnings("CommentedOutCode")
	public void write(byte[] b, int off, int len) {
		int remaining;
		int olen;
		int read = 0;
		int capacity;
		do {
			olen = len;
			capacity = chunkSize-nextIndex.get();
			remaining = Math.min(len, capacity);
			System.arraycopy(b, off+read, lastChunk, nextIndex.get(), remaining);
			if (remaining == capacity) incrementList();
			else nextIndex.addAndGet(remaining);
			read+=remaining;
			len-=remaining;
		} while (olen > remaining);
		/*
		System.arraycopy(b, off, lastChunk, nextIndex, chunkSize-nextIndex);
		nextIndex+=len;
		check();
		*/
	}
	
	public OutputStream getOutputStream() {
		if (outputStream == null) {
			outputStream = new OutputStream() {
				@Override
				public void write(int b) {
					ByteCache.this.write((byte) b);
				}
				@Override
				public void write(byte[] b) {
					ByteCache.this.write(b);
				}
				@Override
				public void write(byte[] b, int off, int len) {
					ByteCache.this.write(b, off, len);
				}
			};
		}
		return outputStream;
	}
}
