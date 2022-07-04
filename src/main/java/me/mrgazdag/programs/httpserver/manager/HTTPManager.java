package me.mrgazdag.programs.httpserver.manager;

import me.mrgazdag.programs.httpserver.*;
import me.mrgazdag.programs.httpserver.request.HTTPRequest;
import me.mrgazdag.programs.httpserver.request.HTTPRequest.HTTPRequestBuilder;
import me.mrgazdag.programs.httpserver.request.HTTPRequestHeader;
import me.mrgazdag.programs.httpserver.request.HTTPRequestMethod;
import me.mrgazdag.programs.httpserver.resource.FileResource;
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
	public boolean handle(Socket socket, Reader in, BufferedWriter out, InputStream inStream, OutputStream outStream) throws IOException {
		HTTPRequest request = null;
		HTTPResponse response = null;
		try {
			request = buildRequest(in, inStream);
			logIF(DefaultManagerLoggingLevel.CONNECTIONS, socket, request.getHTTPMethod().name() + " [" + request.getRequestedResource() + "]");
		} catch (BadRequestFormatException e) {
			logIF(DefaultManagerLoggingLevel.CONNECTIONS, socket, "BAD REQUEST FORMAT");
			e.printStackTrace();
			response = onBadRequestFormat(e.getCause());
		} catch (InternalException e) {
			logIF(DefaultManagerLoggingLevel.CONNECTIONS, socket, "INTERNAL ERROR");
			//e.getCause().printStackTrace();
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
		    return response.send(out, outStream);
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
	public HTTPRequest buildRequest(Reader in, InputStream inStream) throws BadRequestFormatException,InternalException {
		HTTPRequestBuilder builder = new HTTPRequestBuilder();
		readFirstInputLine(builder, in, inStream);
		readHeaders(builder, in, inStream);
		readData(builder, in, inStream);
		return builder.build();
	}

	public void readData(HTTPRequestBuilder builder, Reader in, InputStream inStream) throws InternalException {
		//TODO read data when BufferedReader has been dropped
		if (builder.hasHeader("Content-Length")) {
			int contentLength = Integer.parseInt(builder.getHeader("Content-Length"));
			if (contentLength <= 0) return;
			FileResource.FileType mime = FileResource.FileType.fromMIME(builder.getHeader(HTTPRequestHeader.CONTENT_TYPE));
			if (mime == null || !mime.isText()) {
				try {
					//binary data
					ByteCache cache = new ByteCache();
					byte[] buf = new byte[4098];
					int size = 0;
					while (size < contentLength) {
						int toRead = Math.min(buf.length, contentLength-size);
						int actuallyRead = inStream.read(buf, 0, toRead);
						size+=actuallyRead;
						cache.write(buf, 0, actuallyRead);
					}
					builder.data(cache);
				} catch (Throwable t) {
					throw new InternalException(t);
				}
			} else {
				try {
					//text data
					ByteCache cache = new ByteCache();
					char[] cbuf = new char[4098];
					int size = 0;
					while (size < contentLength) {
						int toRead = Math.min(cbuf.length, contentLength-size);
						int actuallyRead = in.read(cbuf, 0, toRead);
						size+=actuallyRead;
						cache.write(new String(cbuf, 0, actuallyRead).getBytes(StandardCharsets.UTF_8));
					}
					builder.data(cache);
				} catch (Throwable t) {
					throw new InternalException(t);
				}
			}
		}
	}
	private static String readLine(Reader in, InputStream inStream) throws IOException {
		StringBuilder sb = new StringBuilder();
		while (true) {
			char c = (char) in.read();
			if (c == '\r') {
				inStream.mark(2);
				char c2 = (char) in.read();
				if (c2 != '\n') {
					inStream.reset();
				}
				break;
			} else if (c == '\n') {
				break;
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	public void readFirstInputLine(HTTPRequestBuilder builder, Reader in, InputStream inStream) throws BadRequestFormatException,InternalException {
		try {
			String firstLine = readLine(in, inStream);
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
	public void readHeaders(HTTPRequestBuilder builder, Reader in, InputStream inStream) throws BadRequestFormatException,InternalException {
		try {
			while (in.ready()) {
				String line = readLine(in, inStream);
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
