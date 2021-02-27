package me.mrgazdag.programs.httpserver.handler;

import me.mrgazdag.programs.httpserver.HTTPVersion;
import me.mrgazdag.programs.httpserver.request.HTTPRequest;
import me.mrgazdag.programs.httpserver.resource.HTTPResource;
import me.mrgazdag.programs.httpserver.response.HTTPResponse;
import me.mrgazdag.programs.httpserver.response.HTTPStatusCode;

@SuppressWarnings("unused")
public class DefaultHandler implements HTTPHandler {
	private final HTTPResource resource;
	@SuppressWarnings("unused")
	public DefaultHandler(HTTPResource resource) {
		this.resource = resource;
	}
	@Override
	public HTTPResponse handle(HTTPRequest request) {
		return new HTTPResponse(HTTPStatusCode.HTTP_200_OK, HTTPVersion.VERSION_1_1).resource(resource);
	}

}
