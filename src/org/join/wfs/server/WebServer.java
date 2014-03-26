package org.join.wfs.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.impl.DefaultHttpServerConnection;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpProcessor;
import org.apache.http.protocol.HttpRequestHandlerRegistry;
import org.apache.http.protocol.HttpService;
import org.apache.http.protocol.ResponseConnControl;
import org.apache.http.protocol.ResponseContent;
import org.apache.http.protocol.ResponseDate;
import org.apache.http.protocol.ResponseServer;

public class WebServer extends Thread {

	static final String SUFFIX_ZIP = "..zip";
	static final String SUFFIX_DEL = "..del";

	private int port;
	private String webRoot;
	private boolean isLoop = false;

	public WebServer(int port, final String webRoot) {
		super();
		this.port = port;
		this.webRoot = webRoot;
	}

	@Override
	public void run() {
		ServerSocket serverSocket = null;
		try {
			// Create a server socket
			serverSocket = new ServerSocket(port);
			// Creating HTTP protocol handler
			BasicHttpProcessor httpproc = new BasicHttpProcessor();
			// Increase the HTTP protocol interceptors
			httpproc.addInterceptor(new ResponseDate());
			httpproc.addInterceptor(new ResponseServer());
			httpproc.addInterceptor(new ResponseContent());
			httpproc.addInterceptor(new ResponseConnControl());
			// Creating HTTP service
			HttpService httpService = new HttpService(httpproc,
					new DefaultConnectionReuseStrategy(),
					new DefaultHttpResponseFactory());
			// Creating HTTP parameters
			HttpParams params = new BasicHttpParams();
			params.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, 5000)
					.setIntParameter(CoreConnectionPNames.SOCKET_BUFFER_SIZE,
							8 * 1024)
					.setBooleanParameter(
							CoreConnectionPNames.STALE_CONNECTION_CHECK, false)
					.setBooleanParameter(CoreConnectionPNames.TCP_NODELAY, true)
					.setParameter(CoreProtocolPNames.ORIGIN_SERVER,
							"WebServer/1.1");
			// Set HTTP parameters
			httpService.setParams(params);
			// Creating HTTP request actuators Registry
			HttpRequestHandlerRegistry reqistry = new HttpRequestHandlerRegistry();
			// HTTP request to increase actuator
			reqistry.register("*" + SUFFIX_ZIP, new HttpZipHandler(webRoot));
			reqistry.register("*" + SUFFIX_DEL, new HttpDelHandler(webRoot));
			reqistry.register("*", new HttpFileHandler(webRoot));
			// Set HTTP request Actuators
			httpService.setHandlerResolver(reqistry);
			/* Each client receives the loop*/
			isLoop = true;
			while (isLoop && !Thread.interrupted()) {
				// Receiving client socket
				Socket socket = serverSocket.accept();
				// Binding to the server-side HTTP connection
				DefaultHttpServerConnection conn = new DefaultHttpServerConnection();
				conn.bind(socket, params);
				// Delivered to processing requests WorkerThread
				Thread t = new WorkerThread(httpService, conn);
				t.setDaemon(true); // Set daemon thread
				t.start();
			}
		} catch (IOException e) {
			isLoop = false;
			e.printStackTrace();
		} finally {
			try {
				if (serverSocket != null) {
					serverSocket.close();
				}
			} catch (IOException e) {
			}
		}
	}

	public void close() {
		isLoop = false;
	}

}
