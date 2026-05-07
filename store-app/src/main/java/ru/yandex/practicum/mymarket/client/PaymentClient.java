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

    public Mono<Long> getBalance() {
        return paymentWebClient.get()
                .uri("/balance")
                .retrieve()
                .bodyToMono(BalanceResponse.class)
                .map(BalanceResponse::balance);
    }

    public Mono<PaymentResponse> pay(long amount) {
        return paymentWebClient.post()
                .uri("/payments")
                .bodyValue(new PaymentRequest(amount))
                .retrieve()
                .bodyToMono(PaymentResponse.class);
    }
}
