package me.mrgazdag.programs.httpserver.handler;

import me.mrgazdag.programs.httpserver.BadRequestException;
import me.mrgazdag.programs.httpserver.request.HTTPRequest;
import me.mrgazdag.programs.httpserver.response.HTTPResponse;


@FunctionalInterface
public interface HTTPHandler {
	@SuppressWarnings("unused")
	public HTTPResponse handle(HTTPRequest request) throws BadRequestException;
}
