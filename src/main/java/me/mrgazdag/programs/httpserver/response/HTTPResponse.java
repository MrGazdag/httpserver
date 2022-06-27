package me.mrgazdag.programs.httpserver.response;

import me.mrgazdag.programs.httpserver.HTTPVersion;
import me.mrgazdag.programs.httpserver.resource.HTTPResource;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map.Entry;


@SuppressWarnings("unused")
public class HTTPResponse {
	
	private HTTPStatusCode code;
	private HTTPVersion version;
	private final HashMap<String, String> headers;
	private HTTPResource resource;
	public HTTPResponse() {
		headers = new HashMap<>();
		code = HTTPStatusCode.HTTP_501_NOT_IMPLEMENTED;
		version = HTTPVersion.VERSION_1_1;
	}
	public HTTPResponse(HTTPStatusCode code, HTTPVersion version) {
		this();
		this.code = code;
		this.version = version;
	}
	public HTTPResponse code(HTTPStatusCode code) {
		this.code = code;
		return this;
	}
	public HTTPResponse version(HTTPVersion version) {
		this.version = version;
		return this;
	}
	public HTTPResponse header(String key, String value) {
		this.headers.put(key, value);
		return this;
	}
	@SuppressWarnings("UnusedReturnValue")
	public HTTPResponse header(HTTPResponseHeader key, String value) {
		this.headers.put(key.getKey(), value);
		return this;
	}
	public HTTPResponse resource(HTTPResource resource) {
		this.resource = resource;
		if (resource != null) header(HTTPResponseHeader.CONTENT_TYPE, resource.getMimeType());
		return this;
	}
	public HTTPStatusCode getCode() {
		return code;
	}
	public HTTPResource getResource() {
		return resource;
	}
	public HTTPVersion getVersion() {
		return version;
	}
	public boolean send(Socket s) throws IOException {
		OutputStream outStream = s.getOutputStream();
		BufferedWriter out = new BufferedWriter(new OutputStreamWriter(outStream));
		return send(out,outStream);
	}
	public boolean send(BufferedWriter out, OutputStream outStream) throws IOException {
		if (resource != null && resource.length() > 0) header(HTTPResponseHeader.CONTENT_LENGTH, ""+(resource == null ? 0 : resource.length()));
		out.write(version.getMessage());
		out.write(" ");
		out.write(code.getCode()+"");
		out.write(" ");
		out.write(code.getMessage());
		out.write("\r\n");
		for (Entry<String,String> header : headers.entrySet()) {
			out.write(header.getKey() + ": " + header.getValue());
			out.write("\r\n");
		}
		out.write("\r\n");
		out.flush();
		if (resource != null) {
			resource.write(outStream);
		}
		return resource == null ||resource.length() > -1;
	}
}
