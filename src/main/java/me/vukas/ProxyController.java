package me.vukas;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;

import lombok.extern.slf4j.Slf4j;
import me.vukas.dto.ProxyDto;
import me.vukas.proxy.ProxyExecutor;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class ProxyController {

	private ProxyExecutor proxyExecutor;

	public ProxyController(ProxyExecutor proxyExecutor){
		this.proxyExecutor = proxyExecutor;
	}

	@PostMapping(path = "/proxy")
	private void proxy(@RequestBody ProxyDto proxyDto) throws URISyntaxException {
		URI listenUri = new URI(proxyDto.getListen());
		URI upstreamUri = new URI(proxyDto.getUpstream());
		InetSocketAddress listenAddress = new InetSocketAddress(listenUri.getHost(), listenUri.getPort());
		InetSocketAddress upstreamAddress = new InetSocketAddress(upstreamUri.getHost(), upstreamUri.getPort());
		proxyExecutor.start(listenAddress, upstreamAddress);
	}
}
