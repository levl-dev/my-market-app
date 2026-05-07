package ru.yandex.practicum.mymarket.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.config.SecurityConfig;
import ru.yandex.practicum.mymarket.dto.OrderItemView;
import ru.yandex.practicum.mymarket.dto.OrderView;
import ru.yandex.practicum.mymarket.security.CurrentUserService;
import ru.yandex.practicum.mymarket.service.OrderService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = OrderController.class)
@ActiveProfiles("test")
@Import(SecurityConfig.class)
class OrderControllerTest {
    private static final long USER_ID = 1L;

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private OrderService orderService;

    @MockBean
    private CurrentUserService currentUserService;

    @Test
    @WithMockUser(username = "user")
    void getOrdersReturnsOrdersView() {
        List<OrderView> orders = List.of(new OrderView(1L, List.of(new OrderItemView(10L, "A", 100L, 1)), 100L));
        when(currentUserService.currentUserId()).thenReturn(Mono.just(USER_ID));
        when(orderService.getOrders(USER_ID)).thenReturn(Mono.just(orders));

        webTestClient.get().uri("/orders")
                .accept(MediaType.TEXT_HTML)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
                .expectBody(String.class)
                .value(body -> assertThat(body).contains("Заказ №1").contains("A"));
    }

    @Test
    @WithMockUser(username = "user")
    void getOrderByIdReturnsOrderViewAndNewOrderFlag() {
        OrderView order = new OrderView(7L, List.of(), 0L);
        when(currentUserService.currentUserId()).thenReturn(Mono.just(USER_ID));
        when(orderService.getOrder(USER_ID, 7L)).thenReturn(Mono.just(order));

        webTestClient.get().uri(uriBuilder -> uriBuilder.path("/orders/{id}")
                        .queryParam("newOrder", "true")
                        .build(7L))
                .accept(MediaType.TEXT_HTML)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
                .expectBody(String.class)
                .value(body -> assertThat(body)
                        .contains("Заказ №7")
                        .contains("Поздравляем! Успешная покупка!"));
    }
}
