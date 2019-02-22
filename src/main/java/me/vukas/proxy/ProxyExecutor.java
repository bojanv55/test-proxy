package me.vukas.proxy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ProxyExecutor {

	private static final int SO_TIMEOUT = 5_000;
	private ExecutorService executor;

	public ProxyExecutor(ExecutorService executor) {
		this.executor = executor;
	}

	public void start(InetSocketAddress listenAddress, InetSocketAddress upstreamAddress) {
		executor.submit(() -> {
			while(shouldReconnect()) {
				startProxy(listenAddress, upstreamAddress);
			}
		});
	}

	private boolean shouldReconnect(){
		return true;
	}

	private void startProxy(InetSocketAddress listenAddress, InetSocketAddress upstreamAddress) {
		try(ServerSocket serverSocket = serverSocket(listenAddress);
				Socket listenSocket = listenSocket(serverSocket);
				Socket upstreamSocket = upstreamSocket(upstreamAddress)) {	//connect upstream, only after accepting local
			Proxy proxy = new Proxy(listenSocket, upstreamSocket);
			Collection<Callable<Void>> streams = proxy.start();
			executor.invokeAny(streams);
		}
		catch (IOException e) {
			log.warn("Proxy IO error.", e);
		}
		catch (InterruptedException | ExecutionException e) {
			log.warn("Execution exceptions.", e);
		}
		finally {
			log.info("Closing proxy.");
		}
	}

	private ServerSocket serverSocket(InetSocketAddress listenAddress) throws IOException {
		ServerSocket serverSocket = new ServerSocket();
		serverSocket.setReuseAddress(true);	//reuse previous port in time_wait state
		serverSocket.bind(listenAddress);
		return serverSocket;
	}

	private Socket listenSocket(ServerSocket serverSocket) throws IOException {
		return serverSocket.accept();
	}

	private Socket upstreamSocket(InetSocketAddress upstreamAddress) throws IOException {
		Socket upstreamSocket = new Socket();
		upstreamSocket.setSoTimeout(SO_TIMEOUT);
		upstreamSocket.connect(upstreamAddress);
		return upstreamSocket;
	}
}
