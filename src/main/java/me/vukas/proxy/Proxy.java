package me.vukas.proxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.Callable;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class Proxy {

	private Socket listenSocket;
	private Socket upstreamSocket;

	Proxy(Socket listenSocket, Socket upstreamSocket) {
		this.listenSocket = listenSocket;
		this.upstreamSocket = upstreamSocket;
	}

	Collection<Callable<Void>> start() {
		Callable<Void> downstreamCallable = () -> {
			byte[] buffer = new byte[1024];
			int len;
			try(InputStream upstreamSocketInput = upstreamSocket.getInputStream();
					OutputStream listenSocketOutput = listenSocket.getOutputStream()) {
				while ((len = upstreamSocketInput.read(buffer)) > 0) {
					log.info("DOWN");
					listenSocketOutput.write(buffer, 0, len);
				}
			}
			catch (IOException e) {
				log.warn("Error sending data downstream.", e);
			}
			finally {
				log.info("Closing downstream.");
			}
			return null;
		};

		Callable<Void> upstreamCallable = () -> {
			byte[] buffer = new byte[1024];
			int len;
			try(InputStream listenSocketInput = listenSocket.getInputStream();
					OutputStream upstreamSocketOutput = upstreamSocket.getOutputStream()) {
				while ((len = listenSocketInput.read(buffer)) > 0) {
					log.info("UP");
					upstreamSocketOutput.write(buffer, 0, len);
				}
			}
			catch (IOException e) {
				log.warn("Error sending data upstream.", e);
			}
			finally {
				log.info("Closing upstream.");
			}
			return null;
		};

		return Set.of(downstreamCallable, upstreamCallable);
	}
}
