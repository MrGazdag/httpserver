package me.mrgazdag.programs.httpserver;

import me.mrgazdag.programs.httpserver.handler.HTTPHandler;
import me.mrgazdag.programs.httpserver.manager.HTTPManager;
import me.mrgazdag.programs.httpserver.request.HTTPRequest;
import me.mrgazdag.programs.httpserver.request.HTTPRequest.HTTPRequestBuilder;
import me.mrgazdag.programs.httpserver.request.HTTPRequestHeader;
import me.mrgazdag.programs.httpserver.request.HTTPRequestMethod;
import me.mrgazdag.programs.httpserver.response.HTTPResponse;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.security.*;
import java.security.cert.CertificateException;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Predicate;

@SuppressWarnings("unused")
public class HTTPServer {
	private File keystorePath;
	private char[] keystorePass;
	private ServerSocket socket;
	private volatile boolean running;
	private final Set<HTTPResourceEntry> handlers;
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
		this.handlers = new LinkedHashSet<>();
		this.manager = manager == null ? new HTTPManager(this) : manager.setServer(this);
		this.port = port;
	}
	public HTTPManager getManager() {
		return manager;
	}

	public void enableHTTPS(File keystorePath, char[] keystorePass) {
		this.keystorePath = keystorePath;
		this.keystorePass = keystorePass;
	}

	public void setManager(HTTPManager manager) {
		this.manager = manager;
	}
	public void start() {
		running = true;
		try {
			//TODO convert to sun
			//sun = HttpServer.create(new InetSocketAddress(port), 0);
			socket = createSocket(port);
			counted = 0;
			serverThread = new HTTPServerThread();
			serverThread.start();
		} catch (KeyStoreException | IOException | CertificateException | NoSuchAlgorithmException | UnrecoverableKeyException | KeyManagementException e) {
			e.printStackTrace();
		}
	}
	private ServerSocket createSocket(int port) throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException, UnrecoverableKeyException, KeyManagementException {
		if (keystorePath == null || keystorePass == null) return new ServerSocket(port);

		char[] keyStorePassword = "pass_for_self_signed_cert".toCharArray();

		KeyStore keyStore = KeyStore.getInstance("JKS");
		keyStore.load(new FileInputStream(keystorePath), keystorePass);

		KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance("SunX509");
		keyManagerFactory.init(keyStore, keystorePass);

		SSLContext sslContext = SSLContext.getInstance("TLSv1.2");

		// We don't need the password anymore → Overwrite it
		Arrays.fill(keyStorePassword, '0');

		// Null means using default implementations for TrustManager and SecureRandom
		sslContext.init(keyManagerFactory.getKeyManagers(), null, null);

		// Bind the socket to the given port and address
		return sslContext
				.getServerSocketFactory()
				.createServerSocket(port, 0/* backlog*/);
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
		rb.method(HTTPRequestMethod.of(methodString), methodString);
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
	public Set<HTTPResourceEntry> getHandlers() {
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
						boolean completed = false;
						try {
							InputStream inStream = new BufferedInputStream(s.getInputStream());
							OutputStream outStream = s.getOutputStream();
							Reader in = new InputStreamReader(inStream);
							BufferedWriter out = new BufferedWriter(new OutputStreamWriter(outStream));
							completed = manager.handle(s, in, out, inStream, outStream);
						} catch (IOException e) {
							e.printStackTrace();
						} finally {
							try {
								if (completed) s.close();
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
