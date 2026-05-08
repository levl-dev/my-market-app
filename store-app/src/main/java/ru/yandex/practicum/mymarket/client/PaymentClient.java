package ru.yandex.practicum.mymarket.client;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.client.dto.BalanceResponse;
import ru.yandex.practicum.mymarket.client.dto.PaymentRequest;
import ru.yandex.practicum.mymarket.client.dto.PaymentResponse;

@Component
public class PaymentClient {

    private final WebClient paymentWebClient;

    public PaymentClient(WebClient paymentWebClient) {
        this.paymentWebClient = paymentWebClient;
    }

    public Mono<Long> getBalance(String username) {
        return paymentWebClient.get()
                .uri(uriBuilder -> uriBuilder.path("/balance")
                        .queryParam("username", username)
                        .build())
                .retrieve()
                .bodyToMono(BalanceResponse.class)
                .map(BalanceResponse::balance);
    }

    public Mono<PaymentResponse> pay(String username, long amount) {
        return paymentWebClient.post()
                .uri("/payments")
                .bodyValue(new PaymentRequest(username, amount))
                .retrieve()
                .bodyToMono(PaymentResponse.class);
    }
}
