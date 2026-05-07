package ru.yandex.practicum.mymarket.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.dto.ItemCard;
import ru.yandex.practicum.mymarket.dto.Paging;
import ru.yandex.practicum.mymarket.dto.SortType;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.repository.ItemRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CatalogService {

    private final CartService cartService;
    private final ItemRepository itemRepository;

    private static final int ITEMS_PER_ROW = 3;

    public Mono<CatalogPageResult> getItems(String search, SortType sort, int pageNumber, int pageSize) {
        String safeSearch = search == null ? "" : search.trim();
        SortType safeSort = sort == null ? SortType.NO : sort;
        int safePageSize = pageSize > 0 ? pageSize : 10;

        return itemRepository.countBySearch(safeSearch)
                .flatMap(totalItems -> {
                    int pagesCount = pagesCount(totalItems, safePageSize);
                    int safePageNumber = safePageNumber(pageNumber, pagesCount);
                    long offset = (long) (safePageNumber - 1) * safePageSize;
                    return findItemsPage(safeSearch, safeSort, safePageSize, offset).collectList()
                            .map(items -> new CatalogQueryResult(items, totalItems, pagesCount, safePageNumber));
                })
                .flatMap(result -> {
                    List<Item> items = result.items();
                    List<Long> itemIds = items.stream().map(Item::getId).toList();
                    return cartService.getItemCounts(itemIds)
                            .map(counts -> toPageData(
                                    items,
                                    counts,
                                    safeSearch,
                                    safeSort,
                                    safePageSize,
                                    result.pagesCount(),
                                    result.pageNumber()));
                });
    }

    private Flux<Item> findItemsPage(String search, SortType sort, int pageSize, long offset) {
        if (sort == SortType.ALPHA) {
            return itemRepository.findPageOrderByTitle(search, pageSize, offset);
        }
        if (sort == SortType.PRICE) {
            return itemRepository.findPageOrderByPrice(search, pageSize, offset);
        }
        return itemRepository.findPageOrderById(search, pageSize, offset);
    }

    private CatalogPageResult toPageData(
            List<Item> items,
            Map<Long, Integer> counts,
            String search,
            SortType sort,
            int pageSize,
            int pagesCount,
            int pageNumber
    ) {
        List<ItemCard> cards = items.stream()
                .map(item -> toItemCard(item, counts.getOrDefault(item.getId(), 0)))
                .toList();

        return new CatalogPageResult(
                splitIntoRows(cards),
                search,
                sort,
                new Paging(pageSize, pageNumber, pageNumber > 1, pageNumber < pagesCount)
        );
    }

    private int pagesCount(long totalItems, int pageSize) {
        return Math.max((int) Math.ceil((double) totalItems / pageSize), 1);
    }

    private int safePageNumber(int pageNumber, int pagesCount) {
        return Math.min(Math.max(pageNumber, 1), pagesCount);
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

    private record CatalogQueryResult(
            List<Item> items,
            long totalItems,
            int pagesCount,
            int pageNumber
    ) {
    }
}
