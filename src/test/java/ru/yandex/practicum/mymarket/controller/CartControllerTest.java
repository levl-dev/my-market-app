package ru.yandex.practicum.mymarket.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.dto.CartAction;
import ru.yandex.practicum.mymarket.dto.ItemCard;
import ru.yandex.practicum.mymarket.service.CartService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = CartController.class)
@ActiveProfiles("test")
class CartControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private CartService cartService;

    @Test
    void getCartReturnsCartViewWithModel() {
        List<ItemCard> lines = List.of(new ItemCard(1L, "a", "", "/x", 10L, 2));
        when(cartService.getCartItems()).thenReturn(Mono.just(lines));

        webTestClient.get().uri("/cart/items")
                .accept(MediaType.TEXT_HTML)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
                .expectBody(String.class)
                .value(body -> assertThat(body).contains("a").contains("Итого: 20 руб."));
    }

    @Test
    void postCartItemsReturnsCartView() {
        List<ItemCard> lines = List.of();
        when(cartService.changeItemCount(eq(3L), eq(CartAction.DELETE))).thenReturn(Mono.empty());
        when(cartService.getCartItems()).thenReturn(Mono.just(lines));

        webTestClient.post().uri("/cart/items?id=3&action=DELETE")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML);

        verify(cartService).changeItemCount(eq(3L), eq(CartAction.DELETE));
    }
}
