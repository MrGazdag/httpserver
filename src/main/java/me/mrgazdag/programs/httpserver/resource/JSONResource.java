package me.mrgazdag.programs.httpserver.resource;

import java.nio.charset.StandardCharsets;

import me.mrgazdag.programs.httpserver.ByteCache;
import org.json.JSONObject;

/**
 * Represents a resource that contains a {@link JSONObject}.
 * @author Andris
 *
 */
@SuppressWarnings("unused")
public class JSONResource extends CachedResource {

	private final JSONObject object;
	private final String stringData;
	private byte[] bytes;
	public JSONResource(JSONObject obj) {
		super();
		this.object = obj;
		this.stringData = null;
	}
	public JSONResource(String stringData) {
		super();
		this.object = null;
		this.stringData = stringData;
	}
	public JSONResource(JSONObject obj, boolean cacheEnabled) {
		//super(cacheEnabled);
		this.object = obj;
		this.stringData = null;
	}
	public JSONResource(String stringData, boolean cacheEnabled) {
		//super(cacheEnabled);
		this.object = null;
		this.stringData = stringData;
	}

	@Override
	public String getMimeType() {
		return MIMEType.APPLICATION_JSON.getFullString() + ", charset=UTF-8";
	}
	@Override
	protected boolean isCacheValid() {
		return true;
	}
	@Override
	public void writeCache(ByteCache cache) {
		cache.write(getText());
	}
	private byte[] getText() {
		if (bytes == null) {
			//noinspection ConstantConditions
			bytes = (object == null ? stringData : object.toString(0)).getBytes(StandardCharsets.UTF_8);
		}
		return bytes;
	}
	@Override
	public long length() {
		return getText().length;
	}
}
