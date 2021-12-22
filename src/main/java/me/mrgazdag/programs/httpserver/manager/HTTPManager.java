package me.mrgazdag.programs.httpserver.manager;

import me.mrgazdag.programs.httpserver.*;
import me.mrgazdag.programs.httpserver.request.HTTPRequest;
import me.mrgazdag.programs.httpserver.request.HTTPRequest.HTTPRequestBuilder;
import me.mrgazdag.programs.httpserver.request.HTTPRequestMethod;
import me.mrgazdag.programs.httpserver.resource.MIMEType;
import me.mrgazdag.programs.httpserver.resource.TextResource;
import me.mrgazdag.programs.httpserver.response.HTTPResponse;
import me.mrgazdag.programs.httpserver.response.HTTPStatusCode;

import java.io.*;
import java.net.Socket;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

@SuppressWarnings("unused")
public class HTTPManager {
	public enum DefaultManagerLoggingLevel {
		NOTHING, CONNECTIONS, EVERYTHING
	}
	private HTTPServer server;
	private DefaultManagerLoggingLevel loggingLevel;
	private PrintStream log;
	public HTTPManager() {setLogger(System.out, DefaultManagerLoggingLevel.CONNECTIONS);}
	public HTTPManager(HTTPServer server) {
		this();
		this.server = server;
	}
	public HTTPManager setServer(HTTPServer server) {
		this.server = server;
		return this;
	}
	public HTTPServer getServer() {
		return server;
	}
	@SuppressWarnings("unused")
	private void logIF(DefaultManagerLoggingLevel level, String string) {
		if (level.ordinal() <= loggingLevel.ordinal()) {
			log.println(string);
		}
	}
	@SuppressWarnings("SameParameterValue")
	private void logIF(DefaultManagerLoggingLevel level, Socket socket, String string) {
		if (level.ordinal() <= loggingLevel.ordinal()) {
			log.print(socket.getInetAddress().getHostAddress());
			log.print(":");
			log.print(socket.getPort());
			log.print("> ");
			log.println(string);
		}
	}
	public DefaultManagerLoggingLevel getLoggingLevel() {
		return loggingLevel;
	}
	public void setLogger(PrintStream log, DefaultManagerLoggingLevel level) {
		this.log = log;
		this.loggingLevel = level;
	}
	public void handle(Socket socket, BufferedReader in, BufferedWriter out, InputStream inStream, OutputStream outStream) throws IOException {
		HTTPRequest request = null;
		HTTPResponse response = null;
		try {
			request = buildRequest(in);
			logIF(DefaultManagerLoggingLevel.CONNECTIONS, socket, request.getHTTPMethod().name() + " [" + request.getRequestedResource() + "]");
		} catch (BadRequestFormatException e) {
			logIF(DefaultManagerLoggingLevel.CONNECTIONS, socket, "BAD REQUEST FORMAT");
			response = onBadRequestFormat(e.getCause());
		} catch (InternalException e) {
			logIF(DefaultManagerLoggingLevel.CONNECTIONS, socket, "INTENRAL ERROR");
			e.getCause().printStackTrace();
			response = onInternalError(null, e.getCause());
		}
		try {
			handlers: if (response == null) {
				for (HTTPResourceEntry handler : server.getHandlers()) {
					if (handler.getFilter().test(request)) {
						try {
							response = handler.getHandler().handle(request);
							if (response != null) break handlers;
						} catch (BadRequestException e) {
							response = onBadRequest(request, e);
							break handlers;
						} catch (Throwable e) {
							response = onInternalError(request, e);
							break handlers;
						}
					}
				}
				response = onNotFound(request);
			}
		    response.send(out, outStream);
		} catch (Throwable t) {
			System.out.println(request.toString());
		    t.printStackTrace();
			throw t;
		}
	}
	protected HTTPResponse createSimple(HTTPStatusCode code) {
		return new HTTPResponse(HTTPStatusCode.HTTP_200_OK, HTTPVersion.VERSION_1_1).resource(new TextResource(code.getCode() + " " + code.getMessage(), StandardCharsets.UTF_8, MIMEType.TEXT_PLAIN.getFullString()));
	}
	public HTTPResponse onBadRequestFormat(Throwable source) {
		return createSimple(HTTPStatusCode.HTTP_400_BAD_REQUEST);
	}
	public HTTPResponse onInternalError(HTTPRequest request, Throwable source) {
		return createSimple(HTTPStatusCode.HTTP_500_INTERNAL_SERVER_ERROR);
	}
	public HTTPResponse onBadRequest(HTTPRequest request, BadRequestException e) {
		return createSimple(HTTPStatusCode.HTTP_400_BAD_REQUEST);
	}
	public HTTPResponse onNotFound(HTTPRequest request) {
		return createSimple(HTTPStatusCode.HTTP_404_NOT_FOUND);
	}
	public HTTPRequest buildRequest(BufferedReader in) throws BadRequestFormatException,InternalException {
		HTTPRequestBuilder builder = new HTTPRequestBuilder();
		readFirstInputLine(builder, in);
		readHeaders(builder, in);
		readData(builder, in);
		return builder.build();
	}

	public void readData(HTTPRequestBuilder builder, BufferedReader in) throws InternalException {
		//TODO read data when BufferedReader has been dropped
		if (builder.hasHeader("Content-Length") && Integer.parseInt(builder.getHeader("Content-Length")) > 0) {
			try {
				ByteCache cache = new ByteCache();
				while (in.ready()) {
					String str = in.readLine();
					cache.write(str.getBytes(StandardCharsets.UTF_8));
				}
				builder.data(cache);
			} catch (Throwable t) {
				throw new InternalException(t);
			}
		}
	}

	public void readFirstInputLine(HTTPRequestBuilder builder, BufferedReader in) throws BadRequestFormatException,InternalException {
		try {
			String firstLine = in.readLine();
			if (firstLine == null) {
				throw new BadRequestFormatException("Invalid first line");
			}
			String[] firstLineParts = firstLine.split(" ");
			String methodString = firstLineParts[0];
			//ps.println("Method: \"" + methodString + "\"");
			String path = firstLineParts[1];
			//ps.println("Path: \"" + path + "\"");
			String versionString = firstLine.substring(methodString.length() + 1 + path.length() + 1);
			//ps.println("Version: \"" + versionString + "\"");
			builder.method(HTTPRequestMethod.of(methodString), methodString);
			parseResource(path, builder);//builder.resource(path);
			builder.version(HTTPVersion.of(versionString));			
		} catch (IOException e) {
			throw new BadRequestFormatException(e);
		} catch (Throwable t) {
			throw new InternalException(t);
		}
	}
	@SuppressWarnings("RedundantThrows")
	public void parseResource(String resource, HTTPRequestBuilder builder) throws BadRequestFormatException,InternalException {
		String[] parts = resource.split("\\?",2);
		try {
			builder.resource(URLDecoder.decode(parts[0], StandardCharsets.UTF_8.name()));
		} catch (UnsupportedEncodingException ignored) {
			builder.resource(parts[0]);
		}
		if (parts.length == 2) builder.parameters(parts[1]);
	}
	public void readHeaders(HTTPRequestBuilder builder, BufferedReader in) throws BadRequestFormatException,InternalException {
		try {
			while (in.ready()) {
				String line = in.readLine();
				if (line.equals("")) break;
				//ps.println(String.valueOf(line));
				String[] parts = line.split(": ", 2);
				builder.header(parts[0], parts[1]);
			}
		} catch (IOException e) {
			throw new BadRequestFormatException(e);
		} catch (Throwable t) {
			throw new InternalException(t);
		}
	}
}
