package ru.yandex.practicum.mymarket.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.security.CurrentUserService;
import ru.yandex.practicum.mymarket.config.SecurityConfig;
import ru.yandex.practicum.mymarket.service.OrderService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = PurchaseController.class)
@ActiveProfiles("test")
@Import(SecurityConfig.class)
class PurchaseControllerTest {
    private static final long USER_ID = 1L;

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private OrderService orderService;

    @MockBean
    private CurrentUserService currentUserService;

    @MockBean
    private ReactiveUserDetailsService reactiveUserDetailsService;

    @Test
    @WithMockUser(username = "user")
    void postBuyRedirectsToNewOrderPage() {
        when(currentUserService.currentUserId()).thenReturn(Mono.just(USER_ID));
        when(orderService.createOrderFromCart(USER_ID)).thenReturn(Mono.just(42L));

        webTestClient.post().uri("/buy")
                .exchange()
                .expectStatus().isSeeOther()
                .expectHeader().value(HttpHeaders.LOCATION, location ->
                        assertThat(location).endsWith("/orders/42?newOrder=true"));
    }
}
