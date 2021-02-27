package me.mrgazdag.programs.httpserver;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class ByteCache {
	private int chunkSize;
	private volatile List<byte[]> bytes;
	private volatile byte[] lastChunk;
	private volatile int nextIndex;
	private OutputStream outputStream;
	
	public ByteCache() {
		this.bytes = new ArrayList<byte[]>();
		this.chunkSize = 2048;
		incrementList();
	}
	public ByteCache(int chunkSize) {
		this.bytes = new ArrayList<byte[]>();
		this.chunkSize = chunkSize;
		incrementList();
	}
	private void check() {
		if (nextIndex == chunkSize) {
			incrementList();
		}
	}
	public ByteCache clear() {
		nextIndex = chunkSize;
		incrementList();
		this.bytes.clear();
		return this;
	}
	private void incrementList() {
		if (lastChunk != null) bytes.add(lastChunk);
		lastChunk = new byte[chunkSize];
		nextIndex = 0;
	}
	public void copyTo(OutputStream stream) throws IOException {
		for (byte[] bs : bytes) {
			stream.write(bs);
		}
		stream.write(lastChunk, 0, nextIndex);
	}
	public void write(byte b) {
		lastChunk[nextIndex] = b;
		nextIndex++;
		check();
	}
	public void write(byte[] b) {
		write(b,0,b.length);
	}
	public void write(byte[] b, int off, int len) {
		int remaining = chunkSize-nextIndex;
		int olen = len;
		int read = 0;
		int capacity = 0;
		do {
			olen = len;
			capacity = chunkSize-nextIndex;
			remaining = Math.min(len, capacity);
			System.arraycopy(b, off+read, lastChunk, nextIndex, remaining);
			if (remaining == capacity) incrementList();
			else nextIndex +=remaining;
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
				public void write(int b) throws IOException {
					ByteCache.this.write((byte) b);
				}
				@Override
				public void write(byte[] b) throws IOException {
					ByteCache.this.write(b);
				}
				@Override
				public void write(byte[] b, int off, int len) throws IOException {
					ByteCache.this.write(b, off, len);
				}
			};
		}
		return outputStream;
	}
}
