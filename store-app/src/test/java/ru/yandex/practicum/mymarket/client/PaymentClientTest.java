package ru.yandex.practicum.mymarket.client;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.client.dto.PaymentResponse;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentClientTest {

    @Test
    void getBalanceCallsBalanceEndpointAndMapsResponse() {
        AtomicReference<ClientRequest> captured = new AtomicReference<>();
        ExchangeFunction exchangeFunction = request -> {
            captured.set(request);
            return jsonResponse(HttpStatus.OK, "{\"balance\":4200}");
        };

        WebClient webClient = WebClient.builder().exchangeFunction(exchangeFunction).build();
        PaymentClient client = new PaymentClient(webClient);

        Long balance = client.getBalance("user1").block();

        assertThat(balance).isEqualTo(4200L);
        ClientRequest req = captured.get();
        assertThat(req.method()).isEqualTo(HttpMethod.GET);
        assertThat(req.url().getPath()).isEqualTo("/balance");
        assertThat(req.url().getQuery()).contains("username=user1");
    }

    @Test
    void payPostsPaymentsAndMapsResponse() {
        AtomicReference<ClientRequest> captured = new AtomicReference<>();

        ExchangeFunction exchangeFunction = request -> {
            captured.set(request);
            return jsonResponse(HttpStatus.OK,
                    "{\"success\":true,\"balance\":7500,\"message\":\"Payment completed\"}");
        };

        WebClient webClient = WebClient.builder().exchangeFunction(exchangeFunction).build();
        PaymentClient client = new PaymentClient(webClient);

        PaymentResponse payment = client.pay("user2", 2500L).block();

        assertThat(payment.success()).isTrue();
        assertThat(payment.balance()).isEqualTo(7500L);
        assertThat(payment.message()).isEqualTo("Payment completed");

        ClientRequest req = captured.get();
        assertThat(req.method()).isEqualTo(HttpMethod.POST);
        assertThat(req.url().getPath()).isEqualTo("/payments");
    }

    private static Mono<ClientResponse> jsonResponse(HttpStatus status, String json) {
        ClientResponse response = ClientResponse.create(status)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .body(json)
                .build();
        return Mono.just(response);
    }
}
