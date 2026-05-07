package ru.yandex.practicum.mymarket.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.config.SecurityConfig;
import ru.yandex.practicum.mymarket.dto.CartAction;
import ru.yandex.practicum.mymarket.dto.ItemCard;
import ru.yandex.practicum.mymarket.service.CartService;
import ru.yandex.practicum.mymarket.service.ItemService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = ItemController.class)
@ActiveProfiles("test")
@Import(SecurityConfig.class)
class ItemControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private CartService cartService;

    @MockBean
    private ItemService itemService;

    @Test
    void getItemReturnsItemViewAndModel() {
        ItemCard card = new ItemCard(1L, "Title", "Desc", "/i.png", 200L, 3);
        when(itemService.getItem(1L)).thenReturn(Mono.just(card));

        webTestClient.get().uri("/items/{id}", 1L)
                .accept(MediaType.TEXT_HTML)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
                .expectBody(String.class)
                .value(body -> assertThat(body).contains("Title").contains("Desc"));
    }

    @Test
    @WithMockUser(username = "user")
    void postItemsRedirectsToItemsWithQueryParams() {
        when(cartService.changeItemCount(eq(5L), eq(CartAction.PLUS))).thenReturn(Mono.empty());

        webTestClient.post().uri("/items")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("id", "5")
                        .with("action", "PLUS")
                        .with("search", "foo")
                        .with("sort", "NO")
                        .with("pageNumber", "2")
                        .with("pageSize", "10"))
                .exchange()
                .expectStatus().isSeeOther()
                .expectHeader().value(HttpHeaders.LOCATION, location ->
                        assertThat(location).endsWith("/items?search=foo&sort=NO&pageNumber=2&pageSize=10"));

        verify(cartService).changeItemCount(eq(5L), eq(CartAction.PLUS));
    }

    @Test
    @WithMockUser(username = "user")
    void postItemByIdReturnsItemView() {
        ItemCard after = new ItemCard(2L, "X", "", "", 1L, 1);
        when(itemService.getItem(2L)).thenReturn(Mono.just(after));
        when(cartService.changeItemCount(eq(2L), eq(CartAction.MINUS))).thenReturn(Mono.empty());

        webTestClient.post().uri("/items/{id}", 2L)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("action", "MINUS"))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
                .expectBody(String.class)
                .value(body -> assertThat(body).contains("X"));

        verify(cartService).changeItemCount(eq(2L), eq(CartAction.MINUS));
    }
}
