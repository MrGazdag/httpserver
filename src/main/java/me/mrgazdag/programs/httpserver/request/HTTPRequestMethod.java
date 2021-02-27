package me.mrgazdag.programs.httpserver.request;

public enum HTTPRequestMethod {
	HEAD,
	GET,
	POST,
	PUT,
	DELETE,
	TRACE,
	OPTIONS,
	CONNECT,
	UNKNOWN;
	
	
	public static HTTPRequestMethod of(String s) {
		for (HTTPRequestMethod m : values()) {
			if (m.name().equalsIgnoreCase(s)) return m;
		}
		return UNKNOWN;
	}
}
