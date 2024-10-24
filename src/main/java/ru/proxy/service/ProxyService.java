package ru.proxy.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProxyService {
    @Value("${nexus.base.url}")
    private String nexusBaseUrl;

    private final WebClient webClient;

    private static final Set<String> notCopiedHeader = Set.of("Connection", "Host");

    public Mono<byte[]> downloadPom(ServerWebExchange serverWebExchange, UUID requestID) throws IOException {
        String path = serverWebExchange.getRequest().getPath().pathWithinApplication().value();
        String url = nexusBaseUrl + path;
        ServerHttpRequest request = serverWebExchange.getRequest();

        HttpMethod method = request.getMethod();
        HttpHeaders httpHeaders = createProxyHeaders(serverWebExchange.getRequest());

        log.info("{}, Отправленые заголовки {}", requestID, httpHeaders);

        return webClient
                .method(method)
                .uri(url)
                .headers(headers -> headers.addAll(httpHeaders))
                .exchangeToMono(clientResponse -> {
                    log.info("{}, Получено сообщение {}", requestID, clientResponse);
                    int statusCode = clientResponse.statusCode().value();

                    log.info("{}, Response status code: {}", requestID, statusCode);

                    ServerHttpResponse clientResponseHeaders = serverWebExchange.getResponse();
                    clientResponseHeaders.setStatusCode(org.springframework.http.HttpStatus.valueOf(statusCode));

                    HttpHeaders responseHeaders = clientResponse.headers().asHttpHeaders();
                    log.info("{}, Получены заголовки: {}", requestID, responseHeaders);

                    responseHeaders.forEach((headerName, headerValues) -> {
                        if (!notCopiedHeader.contains(headerName)) {
                            headerValues.forEach(headerValue -> clientResponseHeaders.getHeaders().add(headerName, headerValue));
                        }
                    });

                    return clientResponse.bodyToMono(byte[].class)
                            .doOnNext(body -> clientResponseHeaders.getHeaders().setContentLength(body.length));
                });
    }

    public Mono<Void> handleHeadRequest(ServerWebExchange webExchange) {
        String path = webExchange.getRequest().getPath().pathWithinApplication().value();
        String url = nexusBaseUrl + path;
        log.info("Request URL: {}", url);
        HttpHeaders httpHeaders = createProxyHeaders(webExchange.getRequest());

        return webClient.method(HttpMethod.HEAD)
                .uri(url)
                .headers(headers -> headers.addAll(httpHeaders))
                .exchangeToMono(clientResponse -> {
                    log.debug("Response status: {}", clientResponse.statusCode());

                    if (clientResponse.statusCode().is4xxClientError()) {
                        log.warn("Authentication required for URL: {}", url);
                    }

                    return clientResponse.releaseBody().then();
                });
    }

    private HttpHeaders createProxyHeaders(ServerHttpRequest request) {
        HttpHeaders headers = new HttpHeaders();
        request.getHeaders().forEach((headerName, headerValues) -> {
            if (!notCopiedHeader.contains(headerName)) {
                headerValues.forEach(headerValue -> headers.add(headerName, headerValue));
            }
        });
        return headers;
    }

}