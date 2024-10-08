package ru.proxy.proxy.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.proxy.proxy.service.ProxyService;

@RequiredArgsConstructor
@RestController
@Slf4j
public class ProxyController {

    private final ProxyService proxyService;

    @GetMapping("/**")
    public Mono<byte[]> getData(ServerWebExchange exchange) {
        log.info("Получен запрос от {}", exchange.getRequest().getPath() );
        return proxyService.proxyRequest(exchange);
    }
}
