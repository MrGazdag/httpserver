package me.mrgazdag.programs.httpserver.handler;

import me.mrgazdag.programs.httpserver.BadRequestException;
import me.mrgazdag.programs.httpserver.HTTPVersion;
import me.mrgazdag.programs.httpserver.request.HTTPRequest;
import me.mrgazdag.programs.httpserver.resource.HTTPResource;
import me.mrgazdag.programs.httpserver.response.HTTPResponse;
import me.mrgazdag.programs.httpserver.response.HTTPStatusCode;

public class DefaultHandler implements HTTPHandler {
	private HTTPResource resource;
	public DefaultHandler(HTTPResource resource) {
		this.resource = resource;
	}
	@Override
	public HTTPResponse handle(HTTPRequest request) throws BadRequestException {
		return new HTTPResponse(HTTPStatusCode.HTTP_200_OK, HTTPVersion.VERSION_1_1).resource(resource);
	}

}
