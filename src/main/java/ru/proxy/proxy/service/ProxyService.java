package ru.proxy.proxy.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class ProxyService {
    private final WebClient webClient;

    @Value("${nexus.base.url}")
    private String nexusBaseUrl;

    public Mono<byte[]> proxyRequest(ServerWebExchange exchange) {
        String path = exchange.getRequest().getPath().pathWithinApplication().value();
        String url = nexusBaseUrl + path;

        return webClient
                .method(HttpMethod.GET)
                .uri(url)
                .headers(headers -> headers.addAll(exchange.getRequest().getHeaders()))
                .accept(MediaType.APPLICATION_OCTET_STREAM)
                .retrieve()
                .bodyToMono(byte[].class);
    }
}
