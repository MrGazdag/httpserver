package me.mrgazdag.programs.httpserver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.HashSet;
import java.util.function.Predicate;

import me.mrgazdag.programs.httpserver.handler.HTTPHandler;
import me.mrgazdag.programs.httpserver.manager.HTTPManager;
import me.mrgazdag.programs.httpserver.request.HTTPRequest;
import me.mrgazdag.programs.httpserver.request.HTTPRequestHeader;
import me.mrgazdag.programs.httpserver.request.HTTPRequestMethod;
import me.mrgazdag.programs.httpserver.request.HTTPRequest.HTTPRequestBuilder;
import me.mrgazdag.programs.httpserver.response.HTTPResponse;

@SuppressWarnings("unused")
public class HTTPServer {
	private ServerSocket socket;
	private volatile boolean running;
	private final HashSet<HTTPResourceEntry> handlers;
	private int counted;
	private volatile HTTPManager manager;
	private final int port;
	private Thread serverThread;
	@SuppressWarnings("unused")
	private int count() {
		return counted++;
	}
	public HTTPServer(int port) {
		this(null, port);
	}
	public HTTPServer(HTTPManager manager, int port) {
		this.running = false;
		this.handlers = new HashSet<>();
		this.manager = manager == null ? new HTTPManager(this) : manager.setServer(this);
		this.port = port;
	}
	public HTTPManager getManager() {
		return manager;
	}
	public void setManager(HTTPManager manager) {
		this.manager = manager;
	}
	public void start() {
		running = true;
		try {
			//TODO convert to sun
			//sun = HttpServer.create(new InetSocketAddress(port), 0);
			socket = new ServerSocket(port);
			counted = 0;
			serverThread = new HTTPServerThread();
			serverThread.start();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public void stop() {
		running = false;
		try {
			socket.close();
			serverThread.join();
		} catch (IOException | InterruptedException e) {
			e.printStackTrace();
		}
	}
	@SuppressWarnings("UnusedReturnValue")
	public HTTPResourceEntry addEntry(Predicate<HTTPRequest> filter, HTTPHandler handler) {
		HTTPResourceEntry entry = new HTTPResourceEntry(filter, handler);
		handlers.add(entry);
		return entry;
	}
	@SuppressWarnings("UnusedReturnValue")
	public <T extends Predicate<HTTPRequest> & HTTPHandler> HTTPResourceEntry addEntry(T handler) {
		HTTPResourceEntry entry = new HTTPResourceEntry(handler, handler);
		handlers.add(entry);
		return entry;
	}
	public boolean removeEntry(HTTPResourceEntry entry) {
		return handlers.remove(entry);
	}
	@SuppressWarnings("unused")
	private void handle(Socket s, PrintStream ps) throws Exception {
		ps.println("Connection from " + s.getInetAddress().getHostAddress() + ":" + s.getPort());
		InputStream is = s.getInputStream();
		BufferedReader in = new BufferedReader(new InputStreamReader(is));
		//StringBuilder sb = new StringBuilder();
		HTTPRequestBuilder rb = new HTTPRequestBuilder();
		String firstLine = in.readLine();
		if (firstLine == null) {
			s.close();
			return;
		}
		String[] firstLineParts = firstLine.split(" ");
		String methodString = firstLineParts[0];
		ps.println("Method: \"" + methodString + "\"");
		String path = firstLineParts[1];
		ps.println("Path: \"" + path + "\"");
		String versionString = firstLine.substring(methodString.length() + 1 + path.length() + 1);
		ps.println("Version: \"" + versionString + "\"");
		rb.method(HTTPRequestMethod.of(methodString));
		rb.resource(path);
		rb.version(HTTPVersion.of(versionString));
		while (in.ready()) {
			String line = in.readLine();
			if (line.equals("")) break;
			ps.println(line);
			String[] parts = line.split(": ", 2);
			rb.header(parts[0], parts[1]);
		}
		if (rb.hasHeader(HTTPRequestHeader.CONTENT_LENGTH)) {
			ByteCache cache = new ByteCache();
			//SET MAX FILE SIZE !!!! TODO
			//without it, it is possible to overload the server
			//with large uploads
			while (in.ready()) {
				String line = in.readLine();
				if (line.equals("")) break;
				ps.println(line);
				cache.write(line.getBytes());
				String[] parts = line.split(": ", 2);
				rb.header(parts[0], parts[1]);
			}
		}
		HTTPRequest request = rb.build();
		for (HTTPResourceEntry entry : handlers) {
			if (entry.getFilter().test(request)) {
				ps.println("filtered");
				HTTPResponse response = entry.getHandler().handle(request);
				response.send(new Socket() {
					@Override
					public OutputStream getOutputStream() {
						return new OutputStream() {
							
							@Override
							public void write(int b) {
								ps.write(b);
							}
						};
					}
				});
				response.send(s);
				s.close();
				return;
			}
		}
		ps.println("Not found");
		s.close();
	}
	public HashSet<HTTPResourceEntry> getHandlers() {
		return handlers;
	}

	public class HTTPServerThread extends Thread {
		public HTTPServerThread() {
			super("HTTP Server thread");
		}

		@Override
		public void run() {
			while (running) {
				try {
					//File folder = new File("D:\\Tworkspaces\\Programs\\!DISCORD - DiscordBeniSzarMusicBot\\target\\instance\\editor");
					Socket s = socket.accept();
					s.setTcpNoDelay(true);
					s.setSoTimeout(5000);
					//noinspection CommentedOutCode
					new Thread(() -> {
						try {
							InputStream inStream = s.getInputStream();
							OutputStream outStream = s.getOutputStream();
							BufferedReader in = new BufferedReader(new InputStreamReader(inStream));
							BufferedWriter out = new BufferedWriter(new OutputStreamWriter(outStream));
							HTTPResponse response = manager.handle(s, in, out, inStream, outStream);
							response.send(out, outStream);
						} catch (IOException e) {
							e.printStackTrace();
						} finally {
							try {
								s.close();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
								/*
								PrintStream ps = null;
								try {
									File f = new File(folder, count()+".txt");
									if (!f.exists()) f.createNewFile();
									ps = new PrintStream(f);
									handle(s, ps);
								} catch (Throwable e) {
									if (ps != null) e.printStackTrace(ps);
									else e.printStackTrace();
								} finally {
									if (ps != null) {
										ps.close();
									}
								}
								*/
					}).start();
				} catch (Exception e) {
					if (e instanceof SocketException && !running) {
						break;
					}
					e.printStackTrace();
				}

			}
		}
	}
}
