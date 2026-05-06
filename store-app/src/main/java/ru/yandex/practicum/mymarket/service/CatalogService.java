package ru.yandex.practicum.mymarket.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.dto.ItemCard;
import ru.yandex.practicum.mymarket.dto.Paging;
import ru.yandex.practicum.mymarket.dto.SortType;
import ru.yandex.practicum.mymarket.cache.ItemCacheService;
import ru.yandex.practicum.mymarket.model.Item;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CatalogService {

    private final ItemCacheService itemCacheService;
    private final CartService cartService;

    @Value("${app.items.catalog.max-in-memory-items:1000}")
    private int maxInMemoryItems;

    private static final int ITEMS_PER_ROW = 3;

    public Mono<CatalogPageResult> getItems(String search, SortType sort, int pageNumber, int pageSize) {
        String safeSearch = search == null ? "" : search.trim();
        SortType safeSort = sort == null ? SortType.NO : sort;
        int safePageSize = pageSize > 0 ? pageSize : 10;

        return findItems(safeSearch)
                .collectList()
                .flatMap(items -> {
                    List<Long> itemIds = items.stream().map(Item::getId).toList();
                    return cartService.getItemCounts(itemIds)
                            .map(counts -> toPageData(items, counts, safeSearch, safeSort, pageNumber, safePageSize));
                });
    }

    private Flux<Item> findItems(String search) {
        return itemCacheService.findAllItems()
                .take(maxInMemoryItems)
                .filter(item -> search.isBlank() || matchesSearch(item, search));
    }

    private static boolean matchesSearch(Item item, String search) {
        String needle = search.toLowerCase();
        String title = item.getTitle() != null ? item.getTitle() : "";
        String description = item.getDescription() != null ? item.getDescription() : "";
        return title.toLowerCase().contains(needle) || description.toLowerCase().contains(needle);
    }

    private CatalogPageResult toPageData(
            List<Item> items,
            Map<Long, Integer> counts,
            String search,
            SortType sort,
            int pageNumber,
            int pageSize
    ) {
        List<ItemCard> cards = items.stream()
                .map(item -> toItemCard(item, counts.getOrDefault(item.getId(), 0)))
                .toList();

        List<ItemCard> sortedCards = sortCards(cards, sort);
        int pagesCount = Math.max((int) Math.ceil((double) sortedCards.size() / pageSize), 1);
        int safePageNumber = Math.min(Math.max(pageNumber, 1), pagesCount);
        int from = (safePageNumber - 1) * pageSize;
        int to = Math.min(from + pageSize, sortedCards.size());
        List<ItemCard> pageItems = from < to ? sortedCards.subList(from, to) : List.of();

        return new CatalogPageResult(
                splitIntoRows(pageItems),
                search,
                sort,
                new Paging(pageSize, safePageNumber, safePageNumber > 1, safePageNumber < pagesCount)
        );
    }

    private List<ItemCard> sortCards(List<ItemCard> cards, SortType sort) {
        if (sort == SortType.ALPHA) {
            return cards.stream()
                    .sorted(Comparator.comparing(ItemCard::title, String.CASE_INSENSITIVE_ORDER))
                    .toList();
        }
        if (sort == SortType.PRICE) {
            return cards.stream()
                    .sorted(Comparator.comparingLong(ItemCard::price))
                    .toList();
        }
        return cards;
    }

    private ItemCard toItemCard(Item item, int count) {
        return new ItemCard(
                item.getId(),
                item.getTitle(),
                item.getDescription(),
                item.getImgPath(),
                item.getPrice(),
                count
        );
    }

    private List<List<ItemCard>> splitIntoRows(List<ItemCard> items) {
        List<List<ItemCard>> rows = new ArrayList<>();

        for (int i = 0; i < items.size(); i += ITEMS_PER_ROW) {
            List<ItemCard> row = new ArrayList<>(items.subList(i, Math.min(i + ITEMS_PER_ROW, items.size())));
            while (row.size() < ITEMS_PER_ROW) {
                row.add(emptyCard());
            }
            rows.add(row);
        }

        if (rows.isEmpty()) {
            rows.add(List.of(emptyCard(), emptyCard(), emptyCard()));
        }

        return rows;
    }

    private ItemCard emptyCard() {
        return new ItemCard(-1L, "", "", "", 0L, 0);
    }

    public record CatalogPageResult(
            List<List<ItemCard>> items,
            String search,
            SortType sort,
            Paging paging
    ) {
    }
}