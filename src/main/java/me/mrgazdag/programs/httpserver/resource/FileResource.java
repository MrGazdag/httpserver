package me.mrgazdag.programs.httpserver.resource;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;

import me.mrgazdag.programs.httpserver.ByteCache;

/**
 * Represents a resource that contains a single file.
 * @author Andris
 *
 */
public class FileResource extends CachedResource {

	private File f;
	private FileType type;
	/*
	private FileResource(File f) {
		this(f,StandardCharsets.UTF_8, getFileType(f));
	}
	private FileResource(File f, boolean cacheEnabled) {
		this(f, StandardCharsets.UTF_8, getFileType(f), cacheEnabled);
	}
	*/
	private FileResource(File f, FileType type) {
		super(true);
		this.f = f;
		this.type = type;
	}
	private FileResource(File f, FileType type, boolean cacheEnabled) {
		super(cacheEnabled);
		this.f = f;
		this.type = type;
	}
	public File getFile() {
		return f;
	}
	@Override
	public String getMimeType() {
		return type.mime;
	}

	@Override
	protected boolean isCacheValid() {
		return true;
	}

	@Override
	public void writeCache(ByteCache cache) throws IOException {
		FileInputStream fis = new FileInputStream(f);
		OutputStream out = cache.getOutputStream();
		byte[] buffer = new byte[8192];
		int read;
		while ((read = fis.read(buffer, 0, 8192)) >= 0) {
			out.write(buffer, 0, read);
		}
		fis.close();
	}
	public static enum FileType {
		GENERIC(MIMEType.APPLICATION_OCTET_STREAM, false),
		HTML(MIMEType.TEXT_HTML, true, "html", "htm", "shtml", "shtm", "xhtml", "xht", "hta"),
		CSS(MIMEType.TEXT_CSS, true, "css"),
		PLAINTEXT(MIMEType.TEXT_PLAIN, true, "txt"),
		JAVASCRIPT(MIMEType.TEXT_JAVASCRIPT, true, "js", "jsm"),
		JSON(MIMEType.APPLICATION_JSON, true, "json"),
		ZIP(MIMEType.APPLICATION_ZIP, false, "zip"),
		RAR(MIMEType.APPLICATION_X_RAR_COMPRESSED, false, "rar"),
		PDF(MIMEType.APPLICATION_PDF, false, "pdf"),
		;
		private String mime;
		private boolean text;
		private String[] extensions;
		private FileType(String mime, boolean text, String...extensions) {
			this.mime = mime;
			this.text = text;
			this.extensions = extensions;
		}
		private FileType(MIMEType mime, boolean text, String...extensions) {
			this.mime = mime.getFullString();
			this.text = text;
			this.extensions = extensions;
		}
	}
	public static FileType getFileType(File f) {
		String[] parts = f.getName().split("\\.");
		if (parts.length < 2) return FileType.GENERIC;
		String last = parts[parts.length-1];
		for (FileType ft : FileType.values()) {
			if (ft == FileType.GENERIC) continue;
			for (String ext : ft.extensions) {
				if (last.equals(ext)) return ft;
			}
		}
		return FileType.GENERIC;
		
	}
	public static HTTPResource create(File f) {
		return create(f, null);
	}
	public static HTTPResource create(File f, Charset charset) {
		FileType type = getFileType(f);
		if (type.text) return new TextResource(f, charset, type.mime);
		else return new FileResource(f, type);
	}
	public static HTTPResource create(File f, boolean cacheEnabled) {
		FileType type = getFileType(f);
		if (type.text) return new TextResource(f, null, type.mime, cacheEnabled);
		else return new FileResource(f, type, cacheEnabled);
	}
	public static HTTPResource create(File f, Charset charset, boolean cacheEnabled) {
		FileType type = getFileType(f);
		if (type.text) return new TextResource(f, charset, type.mime, cacheEnabled);
		else return new FileResource(f, type, cacheEnabled);
	}
	@Override
	public long length() {
		return f.length();
	}
}
