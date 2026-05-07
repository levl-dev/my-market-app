package ru.yandex.practicum.paymentservice.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.paymentservice.model.BalanceResponse;
import ru.yandex.practicum.paymentservice.model.PaymentRequest;
import ru.yandex.practicum.paymentservice.model.PaymentResponse;
import ru.yandex.practicum.paymentservice.service.PaymentService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = PaymentController.class)
@ActiveProfiles("test")
class PaymentControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private PaymentService paymentService;

    @Test
    void getBalanceReturnsPayloadFromService() {
        when(paymentService.getBalance()).thenReturn(Mono.just(new BalanceResponse(42L)));

        webTestClient.get().uri("/balance")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody(BalanceResponse.class)
                .value(body -> assertThat(body.getBalance()).isEqualTo(42L));

        verify(paymentService).getBalance();
    }

    @Test
    void postPaymentDelegatesToServiceAndReturnsJson() {
        when(paymentService.makePayment(eq(100L))).thenReturn(Mono.just(new PaymentResponse(true, 9_900L, "ok")));

        webTestClient.post().uri("/payments")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(new PaymentRequest(100L))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectBody(PaymentResponse.class)
                .value(body -> {
                    assertThat(body.getSuccess()).isTrue();
                    assertThat(body.getBalance()).isEqualTo(9_900L);
                    assertThat(body.getMessage()).isEqualTo("ok");
                });

        verify(paymentService).makePayment(eq(100L));
    }
}
