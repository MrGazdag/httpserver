package me.mrgazdag.programs.httpserver.response;

@SuppressWarnings("unused")
public enum HTTPResponseHeader {
	ACCESS_CONTROL_ALLOW_ORIGIN("Access-Control-Allow-Origin"),
	ACCESS_CONTROL_ALLOW_CREDENTIALS("Access-Control-Allow-Credentials"),
	ACCESS_CONTROL_EXPOSE_HEADERS("Access-Control-Expose-Headers"),
	ACCESS_CONTROL_MAX_AGE("Access-Control-Max-Age"),
	ACCESS_CONTROL_ALLOW_METHODS("Access-Control-Allow-Methods"),
	ACCESS_CONTROL_ALLOW_HEADERS("Access-Control-Allow-Headers"),
	ACCEPT_PATCH("Accept-Patch"),
	ACCEPT_RANGES("Accept-Ranges"),
	AGE("Age"),
	ALLOW("Allow"),
	ALT_SVC("Alt-Svc"),
	CACHE_CONTROL("Cache-Control"),
	CONNECTION("Connection"),
	CONTENT_DISPOSITION("Content-Disposition"),
	CONTENT_ENCODING("Content-Encoding"),
	CONTENT_LANGUAGE("Content-Language"),
	CONTENT_LENGTH("Content-Length"),
	CONTENT_LOCATION("Content-Location"),
	CONTENT_MD5("Content-MD5"),
	CONTENT_RANGE("Content-Range"),
	CONTENT_TYPE("Content-Type"),
	DATE("Date"),
	DELTA_BASE("Delta-Base"),
	ETAG("ETag"),
	EXPIRES("Expires"),
	INSTANCE_MANIPULATIONS("IM"),
	LAST_MODIFIED("Last-Modified"),
	LINK("Link"),
	LOCATION("Location"),
	@Deprecated
	P3P("P3P"),
	PRAGMA("Pragma"),
	PROXY_AUTHENTICATE("Proxy-Authenticate"),
	PUBLIC_KEY_PINS("Public-Key-Pins"),
	RETRY_AFTER("Retry-After"),
	SERVER("Server"),
	SET_COOKIE("Set-Cookie"),
	STRICT_TRANSPORT_SECURITY("Strict-Transport-Security"),
	TRAILER("Trailer"),
	TRANSFER_ENCODING("Transfer-Encoding"),
	TRACKING_STATUS("Tk"),
	UPGRADE("Upgrade"),
	VARY("Vary"),
	VIA("Via"),
	WARNING("Warning"),
	WWW_AUTHENTICATE("WWW-Authenticate"),
	X_FRAME_OPTIONS("X-Frame-Options")
	;
	
	private final String key;
	HTTPResponseHeader(String key) {
		this.key = key;
	}
	public String getKey() {
		return key;
	}
}
