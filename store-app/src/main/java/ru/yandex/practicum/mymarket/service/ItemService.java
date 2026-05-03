package ru.yandex.practicum.mymarket.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.dto.ItemCard;
import ru.yandex.practicum.mymarket.cache.ItemCacheService;
import ru.yandex.practicum.mymarket.model.Item;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemCacheService itemCacheService;
    private final CartService cartService;

    public Mono<ItemCard> getItem(long id) {
        Mono<Item> itemMono = itemCacheService.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found")));

        return itemMono.flatMap(item -> cartService.getItemCount(item.getId())
                .map(count -> new ItemCard(
                        item.getId(),
                        item.getTitle(),
                        item.getDescription(),
                        item.getImgPath(),
                        item.getPrice(),
                        count
                )));
    }
}
