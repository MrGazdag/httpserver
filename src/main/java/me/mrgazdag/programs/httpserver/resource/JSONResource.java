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
	private byte[] bytes;
	public JSONResource(JSONObject obj) {
		super();
		object = obj;
	}
	public JSONResource(JSONObject obj, boolean cacheEnabled) {
		//super(cacheEnabled);
		object = obj;
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
			bytes = object.toString(0).getBytes(StandardCharsets.UTF_8);
		}
		return bytes;
	}
	@Override
	public long length() {
		return getText().length;
	}
}
