package ru.yandex.practicum.mymarket.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.service.OrderService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = PurchaseController.class)
@ActiveProfiles("test")
class PurchaseControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private OrderService orderService;

    @Test
    void postBuyRedirectsToNewOrderPage() {
        when(orderService.createOrderFromCart()).thenReturn(Mono.just(42L));

        webTestClient.post().uri("/buy")
                .exchange()
                .expectStatus().isSeeOther()
                .expectHeader().value(HttpHeaders.LOCATION, location ->
                        assertThat(location).endsWith("/orders/42?newOrder=true"));
    }
}
