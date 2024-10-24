package ru.proxy.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.proxy.service.ProxyService;

import java.io.IOException;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@Slf4j
public class ProxyController {

    private final ProxyService proxyService;

    @RequestMapping("/**")
    public Mono<byte[]> getData(ServerWebExchange exchange, @RequestHeader HttpHeaders headers) throws IOException {
        UUID requestID = UUID.randomUUID();
        log.info("{}, Получен запрос {}, от {}, заголовки {}", requestID, exchange.getRequest().getMethod(),
                exchange.getRequest().getPath(), headers);

        return proxyService.downloadPom(exchange, requestID);
    }

    @RequestMapping(value = "/**", method = RequestMethod.HEAD)
    public Mono<Void> handleHead(ServerWebExchange exchange) {
        return proxyService.handleHeadRequest(exchange);
    }
}
