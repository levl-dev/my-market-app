package ru.yandex.practicum.mymarket.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
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

    public CatalogPageResult getItems(String search, SortType sort, int pageNumber, int pageSize) {
        pageNumber = Math.max(pageNumber, 1);
        pageSize = Math.max(pageSize, 1);

        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize, toSort(sort));

        Page<Item> page;
        if (search == null || search.isBlank()) {
            page = itemRepository.findAll(pageable);
        } else {
            page = itemRepository.findByTitleContainingIgnoreCaseOrDescriptionContainingIgnoreCase(search, search, pageable);
        }

        List<Item> pageItems = page.getContent();
        List<Long> itemIds = pageItems.stream().map(Item::getId).toList();
        var counts = cartService.getItemCounts(itemIds);

        List<ItemCard> cards = pageItems.stream().map(item -> toItemCard(item, counts)).toList();

        return new CatalogPageResult(
                splitIntoRows(cards),
                new Paging(pageSize, pageNumber, page.hasPrevious(), page.hasNext())
        );
    }

    private ItemCard toItemCard(Item item, Map<Long, Integer> counts) {
        return new ItemCard(
                item.getId(),
                item.getTitle(),
                item.getDescription(),
                item.getImgPath(),
                item.getPrice(),
                counts.getOrDefault(item.getId(), 0)
        );
    }

    private Sort toSort(SortType sort) {
        if (sort == null || sort == SortType.NO) {
            return Sort.unsorted();
        }

        return switch (sort) {
            case ALPHA -> Sort.by("title").ascending();
            case PRICE -> Sort.by("price").ascending();
            case NO -> Sort.unsorted();
        };
    }

    private List<List<ItemCard>> splitIntoRows(List<ItemCard> items) {
        List<List<ItemCard>> rows = new ArrayList<>();

        for (int i = 0; i < items.size(); i += ITEMS_PER_ROW) {
            List<ItemCard> row = new ArrayList<>(items.subList(i, Math.min(i + 3, items.size())));
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

    public record CatalogPageResult(List<List<ItemCard>> items, Paging paging) {
    }
}