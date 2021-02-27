package me.mrgazdag.programs.httpserver.request;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import me.mrgazdag.programs.httpserver.ByteCache;
import me.mrgazdag.programs.httpserver.HTTPVersion;

public class HTTPRequest {
	private HTTPRequestMethod method;
	private String resource;
	private HTTPVersion version;
	private Map<String, String> headers;
	private ParameterMap parameters;
	private ByteCache data;
	
	HTTPRequest(HTTPRequestMethod method, String resource, HTTPVersion version, HashMap<String, String> headers, ParameterMap parameters, ByteCache data) {
		this.method = method;
		this.resource = resource;
		this.version = version;
		this.headers = Collections.unmodifiableMap(headers);
		this.parameters = parameters;
		this.data = data;
	}
	public HTTPRequestMethod getHTTPMethod() {
		return method;
	}
	public String getRequestedResource() {
		return resource;
	}
	public HTTPVersion getHTTPVersion() {
		return version;
	}
	public Map<String, String> getHeaders() {
		return headers;
	}
	public ParameterMap getParameters() {
		return parameters;
	}
	public String getHeader(String key) {
		return headers.get(key);
	}
	public String getHeader(HTTPRequestHeader header) {
		return headers.get(header.getKey());
	}
	public boolean hasHeader(String key) {
		return headers.containsKey(key);
	}
	public boolean hasHeader(HTTPRequestHeader header) {
		return headers.containsKey(header.getKey());
	}
	public ByteCache getData() {
		return data;
	}


	public static class HTTPRequestBuilder {
		private HTTPRequestMethod method;
		private String resource;
		private HTTPVersion version;
		private HashMap<String,String> headers;
		private ParameterMap parameters;
		private ByteCache data;
		public HTTPRequestBuilder() {
			method = HTTPRequestMethod.UNKNOWN;
			resource = "/";
			version = HTTPVersion.UNKNOWN;
			headers = new HashMap<String,String>();
			parameters = new ParameterMap();
		}
		public HTTPRequestBuilder method(HTTPRequestMethod method) {
			this.method = method;
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
			this.headers.put(key, value);
			return this;
		}
		public HTTPRequestBuilder data(ByteCache data) {
			this.data = data;
			return this;
		}
		public boolean hasHeader(String key) {
			return headers.containsKey(key);
		}
		public boolean hasHeader(HTTPRequestHeader header) {
			return headers.containsKey(header.getKey());
		}
		public String getHeader(String key) {
			return headers.get(key);
		}
		public String getHeader(HTTPRequestHeader header) {
			return headers.get(header.getKey());
		}
		public HTTPRequest build() {
			return new HTTPRequest(method,resource,version,headers,parameters, data);
		}
	}
}
