package ru.yandex.practicum.paymentservice.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import ru.yandex.practicum.paymentservice.model.PaymentRequest;

import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class PaymentSecurityIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    @Test
    void getBalanceWithoutAuthReturnsUnauthorized() {
        webTestClient.get().uri("/balance?username=user")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void postPaymentsWithoutAuthReturnsUnauthorized() {
        webTestClient.post().uri("/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new PaymentRequest().username("user").amount(100L))
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void getBalanceWithJwtReturnsOk() {
        webTestClient.mutateWith(mockJwt()).get().uri("/balance?username=user")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void postPaymentsWithJwtReturnsOk() {
        webTestClient.mutateWith(mockJwt()).post().uri("/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new PaymentRequest().username("user").amount(100L))
                .exchange()
                .expectStatus().isOk();
    }
}
