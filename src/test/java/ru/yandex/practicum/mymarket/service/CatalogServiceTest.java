package ru.yandex.practicum.mymarket.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import ru.yandex.practicum.mymarket.dto.ItemCard;
import ru.yandex.practicum.mymarket.dto.SortType;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.repository.ItemRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
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
    void blankSearchUsesFindAll() {
        Page<Item> page = emptyPage();
        when(itemRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(cartService.getItemCounts(any())).thenReturn(Map.of());

        catalogService.getItems("", SortType.NO, 1, 5);

        verify(itemRepository).findAll(any(Pageable.class));
        verify(itemRepository, never()).findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                any(), any(), any());
    }

    @Test
    void nonBlankSearchUsesSearchMethod() {
        Page<Item> page = emptyPage();
        when(itemRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                eq("q"), eq("q"), any(Pageable.class))).thenReturn(page);
        when(cartService.getItemCounts(any())).thenReturn(Map.of());

        catalogService.getItems("q", SortType.NO, 1, 5);

        verify(itemRepository).findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                eq("q"), eq("q"), any(Pageable.class));
        verify(itemRepository, never()).findAll(any(Pageable.class));
    }

    @Test
    void sortNoUsesUnsortedPageable() {
        Page<Item> page = emptyPage();
        when(itemRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(cartService.getItemCounts(any())).thenReturn(Map.of());

        catalogService.getItems("", SortType.NO, 1, 5);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(itemRepository).findAll(captor.capture());
        assertThat(captor.getValue().getSort().isSorted()).isFalse();
    }

    @Test
    void sortAlphaUsesTitleAscending() {
        Page<Item> page = emptyPage();
        when(itemRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(cartService.getItemCounts(any())).thenReturn(Map.of());

        catalogService.getItems("", SortType.ALPHA, 1, 5);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(itemRepository).findAll(captor.capture());
        assertThat(captor.getValue().getSort().getOrderFor("title").getDirection()).isEqualTo(Sort.Direction.ASC);
    }

    @Test
    void sortPriceUsesPriceAscending() {
        Page<Item> page = emptyPage();
        when(itemRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(cartService.getItemCounts(any())).thenReturn(Map.of());

        catalogService.getItems("", SortType.PRICE, 1, 5);

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(itemRepository).findAll(captor.capture());
        assertThat(captor.getValue().getSort().getOrderFor("price").getDirection()).isEqualTo(Sort.Direction.ASC);
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
        Page<Item> page = pageWithContent(content);
        when(itemRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(cartService.getItemCounts(any())).thenReturn(Map.of());

        CatalogService.CatalogPageResult result = catalogService.getItems("", SortType.NO, 1, 10);

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
        Page<Item> page = pageWithContent(List.of());
        when(itemRepository.findAll(any(Pageable.class))).thenReturn(page);
        when(cartService.getItemCounts(any())).thenReturn(Map.of());

        CatalogService.CatalogPageResult result = catalogService.getItems("", SortType.NO, 1, 5);

        assertThat(result.items()).hasSize(1);
        assertThat(result.items().get(0)).hasSize(3);
        assertThat(result.items().get(0)).allMatch(c -> c.id() == -1L);
    }

    private static Page<Item> emptyPage() {
        return pageWithContent(List.of());
    }

    private static Page<Item> pageWithContent(List<Item> content) {
        Page<Item> page = org.mockito.Mockito.mock(Page.class);
        when(page.getContent()).thenReturn(content);
        when(page.hasPrevious()).thenReturn(false);
        when(page.hasNext()).thenReturn(false);
        return page;
    }
}
