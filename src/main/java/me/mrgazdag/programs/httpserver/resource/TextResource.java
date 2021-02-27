package me.mrgazdag.programs.httpserver.resource;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.Supplier;

import me.mrgazdag.programs.httpserver.ByteCache;

/**
 * Represents a resource that contains a single file.
 * @author Andris
 *
 */
public class TextResource extends CachedResource {

	private Supplier<String> text;
	private Charset charset;
	private String mime;
	public TextResource(File file, Charset charset, String mime) {
		super();
		this.text = () -> {
			StringBuffer sb = new StringBuffer();
			try {
				Path path = file.toPath();
				BufferedReader br = charset == null ? Files.newBufferedReader(path) : Files.newBufferedReader(path, charset);
				while (br.ready()) {
					sb.append(br.readLine());
					sb.append("\r\n");
				}
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return sb.toString();
		};
		this.charset = charset;
		this.mime = mime;
	}
	public TextResource(File file, Charset charset, String mime, boolean cacheEnabled) {
		super(cacheEnabled);
		this.text = () -> {
			StringBuilder sb = new StringBuilder();
			try {
				Path path = file.toPath();
				BufferedReader br = charset == null ? Files.newBufferedReader(path) : Files.newBufferedReader(path, charset);
				while (br.ready()) {
					sb.append(br.readLine());
					sb.append("\r\n");
				}
				br.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return sb.toString();
		};
		this.charset = charset;
		this.mime = mime;
	}
	public TextResource(String text, Charset charset, String mime) {
		super();
		this.text = () -> text;
		this.charset = charset;
		this.mime = mime;
	}
	public TextResource(String text, Charset charset, String mime, boolean cacheEnabled) {
		super(cacheEnabled);
		this.text = () -> text;
		this.charset = charset;
		this.mime = mime;
	}

	@Override
	public String getMimeType() {
		return (mime == null ? MIMEType.TEXT_PLAIN.getFullString() : mime) + (charset != null ? "; charset=" + charset : "");
	}

	@Override
	protected boolean isCacheValid() {
		return true;
	}

	@Override
	public void writeCache(ByteCache cache) throws IOException {
		cache.write(text.get().getBytes(charset));
	}

	@Override
	public long length() {
		return text.get().getBytes(charset).length;
	}
	
}
