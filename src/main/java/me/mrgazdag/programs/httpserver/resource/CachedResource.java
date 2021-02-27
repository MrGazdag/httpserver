package me.mrgazdag.programs.httpserver.resource;

import java.io.IOException;
import java.io.OutputStream;

import me.mrgazdag.programs.httpserver.ByteCache;

/**
 * An abstract cached implementation of {@link HTTPResource}.
 * Subclasses should implement {@link #isCacheValid()} and
 * {@link #writeCache(ByteCache)}. This implementation will
 * always call {@link #writeCache(ByteCache)} at least once.
 * @author Andris
 *
 */
public abstract class CachedResource implements HTTPResource {
	protected ByteCache cache;
	private boolean cached;
	private boolean cacheEnabled;
	public CachedResource() {
		this(new ByteCache(), true);
	}
	public CachedResource(boolean cacheEnabled) {
		this(new ByteCache(), cacheEnabled);
	}
	public CachedResource(ByteCache cache) {
		this(cache, true);
	}
	public CachedResource(ByteCache cache, boolean cacheEnabled) {
		this.cache = cache;
		this.cached = false;
		this.cacheEnabled = cacheEnabled;
	}
	public void setCacheEnabled(boolean cacheEnabled) {
		this.cacheEnabled = cacheEnabled;
	}
	public boolean isCacheEnabled() {
		return cacheEnabled;
	}
	
	/**
	 * Should check for validity of the cache.
	 * @return if the cache is valid or not
	 */
	protected abstract boolean isCacheValid();
	/**
	 * Implementations should write the cache no matter what.
	 * @param cache the cache to write in
	 */
	public abstract void writeCache(ByteCache cache) throws IOException;
	@Override
	public void write(OutputStream stream) throws IOException {
		if (!cacheEnabled || !cached || !isCacheValid()) {
			writeCache(cache.clear());
			cached = true;
		}
		cache.copyTo(stream);
		/*
		cache.position(0);
		cache.limit(cache.capacity());
		while (cache.hasRemaining()) {
			int read = Math.min(cache.remaining(), writeBuffer.length);
			cache.get(writeBuffer, 0, read);
			stream.write(writeBuffer);
		}
		*/
	}
}
