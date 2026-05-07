package ru.yandex.practicum.mymarket.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.dto.ItemCard;
import ru.yandex.practicum.mymarket.dto.SortType;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.repository.ItemRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CatalogServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private CartService cartService;

    @InjectMocks
    private CatalogService catalogService;

    @Test
    void blankSearchUsesPageQuery() {
        when(itemRepository.countBySearch("")).thenReturn(Mono.just(0L));
        when(itemRepository.findPageOrderById("", 5, 0L)).thenReturn(Flux.empty());
        when(cartService.getItemCounts(any())).thenReturn(emptyCounts());

        CatalogService.CatalogPageResult result = catalogService.getItems("", SortType.NO, 1, 5).block();

        assertThat(result).isNotNull();
        assertThat(result.items()).isNotEmpty();
        verify(itemRepository).countBySearch("");
        verify(itemRepository).findPageOrderById("", 5, 0L);
    }

    @Test
    void nonBlankSearchUsesPageQueryWithSearch() {
        when(itemRepository.countBySearch("q")).thenReturn(Mono.just(0L));
        when(itemRepository.findPageOrderById("q", 5, 0L)).thenReturn(Flux.empty());
        when(cartService.getItemCounts(any())).thenReturn(emptyCounts());

        CatalogService.CatalogPageResult result = catalogService.getItems("q", SortType.NO, 1, 5).block();

        assertThat(result).isNotNull();
        verify(itemRepository).countBySearch("q");
        verify(itemRepository).findPageOrderById("q", 5, 0L);
    }

    @Test
    void sortNoKeepsRepositoryOrder() {
        Item first = item(1L, "b", 30L);
        Item second = item(2L, "a", 10L);
        when(itemRepository.countBySearch("")).thenReturn(Mono.just(2L));
        when(itemRepository.findPageOrderById("", 10, 0L)).thenReturn(Flux.just(first, second));
        when(cartService.getItemCounts(any())).thenReturn(Mono.just(Map.of(1L, 0, 2L, 0)));

        CatalogService.CatalogPageResult result = catalogService.getItems("", SortType.NO, 1, 10).block();
        List<ItemCard> flat = flatten(result);

        assertThat(flat).extracting(ItemCard::id).containsExactly(1L, 2L);
    }

    @Test
    void sortAlphaSortsByTitleCaseInsensitive() {
        Item first = item(1L, "b", 30L);
        Item second = item(2L, "a", 10L);
        when(itemRepository.countBySearch("")).thenReturn(Mono.just(2L));
        when(itemRepository.findPageOrderByTitle("", 10, 0L)).thenReturn(Flux.just(second, first));
        when(cartService.getItemCounts(any())).thenReturn(Mono.just(Map.of(1L, 0, 2L, 0)));

        CatalogService.CatalogPageResult result = catalogService.getItems("", SortType.ALPHA, 1, 10).block();
        List<ItemCard> flat = flatten(result);

        assertThat(flat).extracting(ItemCard::title).containsExactly("a", "b");
    }

    @Test
    void sortPriceSortsByPriceAscending() {
        Item first = item(1L, "a", 30L);
        Item second = item(2L, "b", 10L);
        when(itemRepository.countBySearch("")).thenReturn(Mono.just(2L));
        when(itemRepository.findPageOrderByPrice("", 10, 0L)).thenReturn(Flux.just(second, first));
        when(cartService.getItemCounts(any())).thenReturn(Mono.just(Map.of(1L, 0, 2L, 0)));

        CatalogService.CatalogPageResult result = catalogService.getItems("", SortType.PRICE, 1, 10).block();
        List<ItemCard> flat = flatten(result);

        assertThat(flat).extracting(ItemCard::price).containsExactly(10L, 30L);
    }

    @Test
    void splitIntoRowsGroupsByThreeAndPadsWithPlaceholderCards() {
        List<Item> content = new ArrayList<>();
        for (long i = 1; i <= 4; i++) {
            Item item = new Item();
            item.setId(i);
            item.setTitle("t" + i);
            item.setDescription("");
            item.setImgPath("");
            item.setPrice(10L * i);
            content.add(item);
        }
        when(itemRepository.countBySearch("")).thenReturn(Mono.just(4L));
        when(itemRepository.findPageOrderById("", 10, 0L)).thenReturn(Flux.fromIterable(content));
        when(cartService.getItemCounts(any())).thenReturn(Mono.just(Map.of(1L, 0, 2L, 0, 3L, 0, 4L, 0)));

        CatalogService.CatalogPageResult result = catalogService.getItems("", SortType.NO, 1, 10).block();

        assertThat(result.items()).hasSize(2);
        assertThat(result.items().get(0)).hasSize(3);
        assertThat(result.items().get(0).stream().map(ItemCard::id).toList()).containsExactly(1L, 2L, 3L);
        assertThat(result.items().get(1)).hasSize(3);
        assertThat(result.items().get(1).get(0).id()).isEqualTo(4L);
        assertThat(result.items().get(1).get(1).id()).isEqualTo(-1L);
        assertThat(result.items().get(1).get(2).id()).isEqualTo(-1L);
    }

    @Test
    void emptyCatalogPageYieldsSingleRowOfPlaceholderCards() {
        when(itemRepository.countBySearch("")).thenReturn(Mono.just(0L));
        when(itemRepository.findPageOrderById("", 5, 0L)).thenReturn(Flux.empty());
        when(cartService.getItemCounts(any())).thenReturn(Mono.just(Map.of()));

        CatalogService.CatalogPageResult result = catalogService.getItems("", SortType.NO, 1, 5).block();

        assertThat(result.items()).hasSize(1);
        assertThat(result.items().get(0)).hasSize(3);
        assertThat(result.items().get(0)).allMatch(c -> c.id() == -1L);
    }

    private static Mono<Map<Long, Integer>> emptyCounts() {
        return Mono.just(Map.of());
    }

    private static Item item(long id, String title, long price) {
        Item item = new Item();
        item.setId(id);
        item.setTitle(title);
        item.setDescription("");
        item.setImgPath("");
        item.setPrice(price);
        return item;
    }

    private static List<ItemCard> flatten(CatalogService.CatalogPageResult result) {
        return result.items().stream().flatMap(List::stream).filter(c -> c.id() != -1L).toList();
    }
}
