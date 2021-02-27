package me.mrgazdag.programs.httpserver.handler;

import me.mrgazdag.programs.httpserver.HTTPVersion;
import me.mrgazdag.programs.httpserver.request.HTTPRequest;
import me.mrgazdag.programs.httpserver.resource.FileResource;
import me.mrgazdag.programs.httpserver.resource.HTTPResource;
import me.mrgazdag.programs.httpserver.response.HTTPResponse;
import me.mrgazdag.programs.httpserver.response.HTTPStatusCode;

import java.io.File;

@SuppressWarnings("unused")
public class FileHandler implements HTTPHandler {
	
	private final HTTPResource resource;
	private final File f;
	private final HTTPResource notFound;
	public FileHandler(File f) {
		this(f,null);
	}
	public FileHandler(File f, HTTPResource notFound) {
		this.f = f;
		this.resource = FileResource.create(f);
		this.notFound = notFound;
	}
	@SuppressWarnings("unused")
	public FileHandler(FileResource resource) {
		this(resource, null);
	}
	public FileHandler(FileResource resource, HTTPResource notFound) {
		this.f = resource.getFile();
		this.resource = resource;
		this.notFound = notFound;
	}

	@Override
	public HTTPResponse handle(HTTPRequest request) {
		if (!f.exists()) {
			return new HTTPResponse(HTTPStatusCode.HTTP_404_NOT_FOUND, HTTPVersion.VERSION_1_1).resource(notFound);
		} else {
			return new HTTPResponse(HTTPStatusCode.HTTP_200_OK, HTTPVersion.VERSION_1_1).resource(resource);			
		}
	}
	
}
