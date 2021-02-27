package me.mrgazdag.programs.httpserver.request;

@SuppressWarnings("unused")
public enum HTTPRequestHeader {
	A_IM("A-IM"),
	ACCEPT("Accept"),
	ACCEPT_CHARSET("Accept-Charset"),
	ACCEPT_DATETIME("Accept-Datetime"),
	ACCEPT_ENCODING("Accept-Encoding"),
	ACCEPT_LANGUAGE("Accept-Language"),
	ACCESS_CONTROL_REQUEST_METHOD("Access-Control-Request-Method"),
	ACCESS_CONTROL_REQUEST_HEADERS("Access-Control-Request-Headers"),
	AUTHORIZATION("Authorization"),
	CACHE_CONTROL("Cache-Control"),
	CONNECTION("Connection"),
	CONTENT_ENCODING("Content-Encoding"),
	CONTENT_LENGTH("Content-Length"),
	CONTENT_MD5("Content-MD5"),
	CONTENT_TYPE("Content-Type"),
	COOKIE("Cookie"),
	DATE("Date"),
	EXPECT("Expect"),
	FORWARDED("Forwarded"),
	FROM("From"),
	HOST("Host"),
	HTTP2_SETTINGS("HTTP2-Settings"),
	IF_MATCH("If-Match"),
	IF_MODIFIED_SINCE("If-Modified-Since"),
	IF_NONE_MATCH("If-None-Match"),
	IF_RANGE("If-Range"),
	IF_UNMODIFIED_SINCE("If-Unmodified-Since"),
	MAX_FORWARDS("Max-Forwards"),
	ORIGIN("Origin"),
	PRAGMA("Pragma"),
	PROXY_AUTHORIZATION("Proxy-Authorization"),
	RANGE("Range"),
	REFERER("Referer"),
	TRANSFER_ENCODINGS("TE"),
	TRAILER("Trailer"),
	TRANSFER_ENCODING("Transfer-Encoding"),
	USER_AGENT("User-Agent"),
	UPGRADE("Upgrade"),
	VIA("Via"),
	WARNING("Warning")
	;
	
	private final String key;
	HTTPRequestHeader(String key) {
		this.key = key;
	}
	public String getKey() {
		return key;
	}
}
