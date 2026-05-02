package ru.yandex.practicum.mymarket.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.dto.ItemCard;
import ru.yandex.practicum.mymarket.dto.Paging;
import ru.yandex.practicum.mymarket.dto.SortType;
import ru.yandex.practicum.mymarket.service.CatalogService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@WebFluxTest(controllers = CatalogController.class)
@ActiveProfiles("test")
class CatalogControllerTest {

    @Autowired
    private WebTestClient webTestClient;

    @MockBean
    private CatalogService catalogService;

    @Test
    void getRootReturnsItemsViewWithModel() {
        stubCatalogPage();

        webTestClient.get().uri("/")
                .accept(MediaType.TEXT_HTML)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
                .expectBody(String.class)
                .value(body -> assertThat(body).contains("t"));
    }

    @Test
    void getItemsReturns200AndExpectedModel() {
        stubCatalogPage();

        webTestClient.get().uri("/items")
                .accept(MediaType.TEXT_HTML)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
                .expectBody(String.class)
                .value(body -> assertThat(body).contains("t"));
    }

    private void stubCatalogPage() {
        ItemCard card = new ItemCard(1L, "t", "", "/img.png", 100L, 0);
        ItemCard placeholder = new ItemCard(-1L, "", "", "", 0L, 0);
        CatalogService.CatalogPageResult result = new CatalogService.CatalogPageResult(
                List.of(List.of(card, placeholder, placeholder)),
                "",
                SortType.NO,
                new Paging(5, 1, false, false)
        );
        when(catalogService.getItems(anyString(), eq(SortType.NO), eq(1), eq(5))).thenReturn(Mono.just(result));
    }
}
