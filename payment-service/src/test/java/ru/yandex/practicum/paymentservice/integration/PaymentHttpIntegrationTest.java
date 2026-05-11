package ru.yandex.practicum.paymentservice.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import ru.yandex.practicum.paymentservice.model.BalanceResponse;
import ru.yandex.practicum.paymentservice.model.PaymentRequest;
import ru.yandex.practicum.paymentservice.model.PaymentResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class PaymentHttpIntegrationTest {
    private static final String USERNAME = "buyer";

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void paymentFlowShouldUpdateBalance() {
        webTestClient.mutateWith(mockJwt()).get().uri("/balance?username={username}", USERNAME)
                .exchange()
                .expectStatus().isOk()
                .expectBody(BalanceResponse.class)
                .value(body -> assertThat(body.getBalance()).isEqualTo(10_000L));

        webTestClient.mutateWith(mockJwt()).post().uri("/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new PaymentRequest().username(USERNAME).amount(2_500L))
                .exchange()
                .expectStatus().isOk()
                .expectBody(PaymentResponse.class)
                .value(body -> {
                    assertThat(body.getSuccess()).isTrue();
                    assertThat(body.getBalance()).isEqualTo(7_500L);
                });

        webTestClient.mutateWith(mockJwt()).get().uri("/balance?username={username}", USERNAME)
                .exchange()
                .expectStatus().isOk()
                .expectBody(BalanceResponse.class)
                .value(body -> assertThat(body.getBalance()).isEqualTo(7_500L));
    }
}