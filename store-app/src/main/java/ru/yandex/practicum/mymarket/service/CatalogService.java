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

    private final ItemRepository itemRepository;
    private final CartService cartService;

    private static final int ITEMS_PER_ROW = 3;

    public Mono<CatalogPageResult> getItems(String search, SortType sort, int pageNumber, int pageSize) {
        return getItems(search, sort, pageNumber, pageSize, null);
    }

    public Mono<CatalogPageResult> getItems(String search, SortType sort, int pageNumber, int pageSize, Long userId) {
        String safeSearch = search == null ? "" : search.trim();
        SortType safeSort = sort == null ? SortType.NO : sort;
        int safePageSize = pageSize > 0 ? pageSize : 10;

        return itemRepository.countBySearch(safeSearch)
                .flatMap(totalItems -> {
                    int pagesCount = Math.max((int) Math.ceil((double) totalItems / safePageSize), 1);
                    int safePageNumber = Math.min(Math.max(pageNumber, 1), pagesCount);
                    long offset = (long) (safePageNumber - 1) * safePageSize;

                    return findPageItems(safeSearch, safeSort, safePageSize, offset)
                            .collectList()
                            .flatMap(items -> {
                                List<Long> itemIds = items.stream().map(Item::getId).toList();
                                Mono<Map<Long, Integer>> countsMono = userId == null
                                        ? Mono.just(Map.of())
                                        : cartService.getItemCounts(userId, itemIds);
                                return countsMono
                                        .map(counts -> toPageData(items, counts, safeSearch, safeSort, safePageSize, safePageNumber, pagesCount));
                            });
                });
    }

    private Flux<Item> findPageItems(String search, SortType sort, int limit, long offset) {
        if (sort == SortType.ALPHA) {
            return itemRepository.findPageOrderByTitle(search, limit, offset);
        }
        if (sort == SortType.PRICE) {
            return itemRepository.findPageOrderByPrice(search, limit, offset);
        }
        return itemRepository.findPageOrderById(search, limit, offset);
    }

    private CatalogPageResult toPageData(
            List<Item> items,
            Map<Long, Integer> counts,
            String search,
            SortType sort,
            int pageSize,
            int pageNumber,
            int pagesCount
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