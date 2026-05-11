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
        return getItem(id, null);
    }

    public Mono<ItemCard> getItem(long id, Long userId) {
        Mono<Item> itemMono = itemCacheService.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found")));

        Mono<Integer> countMono = userId == null
                ? Mono.just(0)
                : cartService.getItemCount(userId, id);

        return itemMono.flatMap(item -> countMono
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
