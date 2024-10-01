package ru.proxy.proxy.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.proxy.proxy.service.ProxyService;

@RequiredArgsConstructor
@RestController
public class ProxyController {

    private final ProxyService proxyService;

    @GetMapping("/**")
    public Mono<byte[]> getData(ServerWebExchange exchange) {
        return proxyService.proxyRequest(exchange);
    }
}
