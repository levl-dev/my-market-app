package ru.yandex.practicum.mymarket.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.config.SecurityConfig;
import ru.yandex.practicum.mymarket.client.PaymentClient;
import ru.yandex.practicum.mymarket.dto.CartAction;
import ru.yandex.practicum.mymarket.dto.ItemCard;
import ru.yandex.practicum.mymarket.model.AppUser;
import ru.yandex.practicum.mymarket.security.CurrentUserService;
import ru.yandex.practicum.mymarket.service.CartService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = CartController.class)
@ActiveProfiles("test")
@Import(SecurityConfig.class)
class CartControllerTest {
    private static final long USER_ID = 1L;
    private static final String USERNAME = "user";

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private CartService cartService;

    @MockBean
    private PaymentClient paymentClient;

    @MockBean
    private CurrentUserService currentUserService;

    @MockBean
    private ReactiveUserDetailsService reactiveUserDetailsService;

    private static AppUser user() {
        AppUser user = new AppUser();
        user.setId(USER_ID);
        user.setUsername(USERNAME);
        return user;
    }

    @Test
    @WithMockUser(username = "user")
    void getCartReturnsCartViewWithModel() {
        List<ItemCard> lines = List.of(new ItemCard(1L, "a", "", "/x", 10L, 2));
        when(currentUserService.currentUser()).thenReturn(Mono.just(user()));
        when(cartService.getCartItems(USER_ID)).thenReturn(Mono.just(lines));
        when(paymentClient.getBalance(USERNAME)).thenReturn(Mono.just(1_000_000L));

        webTestClient.get().uri("/cart/items")
                .accept(MediaType.TEXT_HTML)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
                .expectBody(String.class)
                .value(body -> assertThat(body).contains("a").contains("Итого: 20 руб."));
    }

    @Test
    void anonymousCannotOpenCart() {
        webTestClient.get().uri("/cart/items")
                .exchange()
                .expectStatus().is3xxRedirection();
    }

    @Test
    @WithMockUser(username = "user")
    void postCartItemsReturnsCartView() {
        List<ItemCard> lines = List.of();
        when(currentUserService.currentUser()).thenReturn(Mono.just(user()));
        when(cartService.changeItemCount(eq(USER_ID), eq(3L), eq(CartAction.DELETE))).thenReturn(Mono.empty());
        when(cartService.getCartItems(USER_ID)).thenReturn(Mono.just(lines));
        when(paymentClient.getBalance(USERNAME)).thenReturn(Mono.just(0L));

        webTestClient.post().uri("/cart/items?id=3&action=DELETE")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML);

        verify(cartService).changeItemCount(eq(USER_ID), eq(3L), eq(CartAction.DELETE));
    }
}
