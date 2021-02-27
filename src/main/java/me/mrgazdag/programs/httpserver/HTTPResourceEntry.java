package me.mrgazdag.programs.httpserver;

import java.util.function.Predicate;

import me.mrgazdag.programs.httpserver.handler.HTTPHandler;
import me.mrgazdag.programs.httpserver.request.HTTPRequest;

public class HTTPResourceEntry {
	private final Predicate<HTTPRequest> filter;
	private final HTTPHandler handler;
	public HTTPResourceEntry(Predicate<HTTPRequest> filter, HTTPHandler handler) {
		this.filter = filter;
		this.handler = handler;
	}
	public Predicate<HTTPRequest> getFilter() {
		return filter;
	}
	public HTTPHandler getHandler() {
		return handler;
	}
}
