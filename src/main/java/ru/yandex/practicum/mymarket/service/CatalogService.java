package ru.yandex.practicum.mymarket.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.dto.ItemCard;
import ru.yandex.practicum.mymarket.dto.Paging;
import ru.yandex.practicum.mymarket.dto.SortType;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.repository.ItemRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CatalogService {

    private final ItemRepository itemRepository;
    private static final int ITEMS_PER_ROW = 3;

    public Mono<CatalogPageData> getCatalogPage() {
        return itemRepository.findAll()
                .collectList()
                .map(this::toPageData);
    }

    private CatalogPageData toPageData(List<Item> items) {
        List<ItemCard> cards = items.stream().map(this::toItemCard).toList();
        int pageSize = Math.max(cards.size(), 1);
        return new CatalogPageData(
                splitIntoRows(cards),
                "",
                SortType.NO,
                new Paging(pageSize, 1, false, false)
        );
    }

    private ItemCard toItemCard(Item item) {
        return new ItemCard(
                item.getId(),
                item.getTitle(),
                item.getDescription(),
                item.getImgPath(),
                item.getPrice(),
                0
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

    public record CatalogPageData(
            List<List<ItemCard>> items,
            String search,
            SortType sort,
            Paging paging
    ) {
    }
}
