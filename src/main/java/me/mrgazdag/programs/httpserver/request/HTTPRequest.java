package me.mrgazdag.programs.httpserver.request;

import me.mrgazdag.programs.httpserver.ByteCache;
import me.mrgazdag.programs.httpserver.HTTPVersion;

import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@SuppressWarnings("unused")
public class HTTPRequest {
	private final HTTPRequestMethod method;
	private final String methodAsString;
	private final String resource;
	private final HTTPVersion version;
	private final Map<String, String> headersExact;
	private final Map<String, String> headersIgnoreCase;
	private final ParameterMap parameters;
	private final ByteCache data;
	
	HTTPRequest(HTTPRequestMethod method, String methodAsString, String resource, HTTPVersion version, HashMap<String, String> headersExact, Map<String, String> headersIgnoreCase, ParameterMap parameters, ByteCache data) {
		this.method = method;
		this.methodAsString = methodAsString;
		this.resource = resource;
		this.version = version;
		this.headersExact = Collections.unmodifiableMap(headersExact);
		this.headersIgnoreCase = Collections.unmodifiableMap(headersIgnoreCase);
		this.parameters = parameters;
		this.data = data;
	}
	public HTTPRequestMethod getHTTPMethod() {
		return method;
	}
	public String getHTTPMethodAsString() {
		return methodAsString;
	}
	public String getRequestedResource() {
		return resource;
	}
	public HTTPVersion getHTTPVersion() {
		return version;
	}
	public Map<String, String> getHeadersExact() {
		return headersExact;
	}
	public Map<String, String> getHeaders() {
		return headersIgnoreCase;
	}
	public ParameterMap getParameters() {
		return parameters;
	}
	public String getHeaderExact(String key) {
		return headersExact.get(key);
	}
	public String getHeader(String key) {
		return headersIgnoreCase.get(ignoreCase(key));
	}
	public String getHeader(HTTPRequestHeader header) {
		return headersIgnoreCase.get(ignoreCase(header.getKey()));
	}
	public boolean hasHeaderExact(String key) {
		return headersExact.containsKey(key);
	}
	public boolean hasHeader(String key) {
		return headersIgnoreCase.containsKey(ignoreCase(key));
	}
	public boolean hasHeader(HTTPRequestHeader header) {
		return headersIgnoreCase.containsKey(ignoreCase(header.getKey()));
	}
	public ByteCache getData() {
		return data;
	}


	@SuppressWarnings("UnusedReturnValue")
	public static class HTTPRequestBuilder {
		private HTTPRequestMethod method;
		private String methodAsString;
		private String resource;
		private HTTPVersion version;
		private final HashMap<String,String> headersExact;
		private final HashMap<String,String> headersIgnoreCase;
		private final ParameterMap parameters;
		private ByteCache data;
		public HTTPRequestBuilder() {
			method = HTTPRequestMethod.UNKNOWN;
			resource = "/";
			version = HTTPVersion.UNKNOWN;
			headersExact = new HashMap<>();
			headersIgnoreCase = new HashMap<>();
			parameters = new ParameterMap();
		}
		public HTTPRequestBuilder method(HTTPRequestMethod method, String methodAsString) {
			this.method = method;
			this.methodAsString = methodAsString;
			return this;
		}
		public HTTPRequestBuilder resource(String resource) {
			this.resource = resource;
			return this;
		}
		public HTTPRequestBuilder parameters(String query) {
			this.parameters.parse(query);
			return this;
		}
		public HTTPRequestBuilder parameter(String key, String value) {
			this.parameters.add(key, value);
			return this;
		}
		public HTTPRequestBuilder version(HTTPVersion version) {
			this.version = version;
			return this;
		}
		public HTTPRequestBuilder header(String key, String value) {
			this.headersExact.put(key, value);
			this.headersIgnoreCase.put(ignoreCase(key), value);
			return this;
		}
		public HTTPRequestBuilder data(ByteCache data) {
			this.data = data;
			return this;
		}
		public boolean hasHeaderExact(String key) {
			return headersExact.containsKey(key);
		}
		public boolean hasHeader(HTTPRequestHeader header) {
			return headersIgnoreCase.containsKey(ignoreCase(header.getKey()));
		}
		public boolean hasHeader(String key) {
			return headersIgnoreCase.containsKey(ignoreCase(key));
		}
		public String getHeader(String key) {
			return headersIgnoreCase.get(ignoreCase(key));
		}
		public String getHeader(HTTPRequestHeader header) {
			return headersIgnoreCase.get(ignoreCase(header.getKey()));
		}
		public HTTPRequest build() {
			return new HTTPRequest(method,methodAsString,resource,version, headersExact,headersIgnoreCase, parameters, data);
		}
	}
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		String nl = System.lineSeparator();
		sb.append(methodAsString).append(" ").append(resource).append(" ").append(version).append(nl);
		for (Map.Entry<String, String> entry : headersExact.entrySet()) {
			sb.append(entry.getKey()).append(": ").append(entry.getValue()).append(nl);
		}
		return sb.toString();
	}
	private static String ignoreCase(String original) {
		return original.toLowerCase(Locale.ROOT);
	}
}
