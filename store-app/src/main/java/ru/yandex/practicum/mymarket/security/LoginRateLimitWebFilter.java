package ru.yandex.practicum.mymarket.security;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class LoginRateLimitWebFilter implements WebFilter {

    private static final int MAX_ATTEMPTS_PER_MINUTE = 5;
    private static final long WINDOW_MILLIS = 60_000L;

    private final ConcurrentHashMap<String, WindowCounter> countsByIp = new ConcurrentHashMap<>();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        var request = exchange.getRequest();
        if (!HttpMethod.POST.equals(request.getMethod())) {
            return chain.filter(exchange);
        }
        if (!"/login".equals(request.getPath().pathWithinApplication().value())) {
            return chain.filter(exchange);
        }

        String ip = clientIp(request.getRemoteAddress());
        long windowStart = System.currentTimeMillis() / WINDOW_MILLIS;
        WindowCounter counter = countsByIp.computeIfAbsent(ip, k -> new WindowCounter());

        int attemptNo;
        synchronized (counter) {
            if (counter.windowStart != windowStart) {
                counter.windowStart = windowStart;
                counter.attempts.set(0);
            }
            attemptNo = counter.attempts.incrementAndGet();
        }

        if (attemptNo > MAX_ATTEMPTS_PER_MINUTE) {
            exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
            return exchange.getResponse().setComplete();
        }

        return chain.filter(exchange);
    }

    private static String clientIp(InetSocketAddress remote) {
        if (remote == null) {
            return "unknown";
        }
        String host = remote.getAddress() != null ? remote.getAddress().getHostAddress() : null;
        return host != null ? host : remote.getHostString();
    }

    private static final class WindowCounter {
        volatile long windowStart;
        final AtomicInteger attempts = new AtomicInteger();
    }
}
