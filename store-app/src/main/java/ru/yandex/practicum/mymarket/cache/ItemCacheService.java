package ru.yandex.practicum.mymarket.cache;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.repository.ItemRepository;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class ItemCacheService {

    private static final String KEY_ITEM_PREFIX = "my-market:item:";

    private final ItemRepository itemRepository;
    private final ReactiveRedisTemplate<String, Item> redis;
    private final Duration ttl;

    public ItemCacheService(
            ItemRepository itemRepository,
            ReactiveRedisTemplate<String, Item> redisTemplate,
            @Value("${app.items.cache.ttl:PT2M}") Duration ttl) {
        this.itemRepository = itemRepository;
        this.redis = redisTemplate;
        this.ttl = ttl;
    }

    public Mono<Item> findById(long id) {
        String key = KEY_ITEM_PREFIX + id;
        return redis.opsForValue().get(key)
                .switchIfEmpty(itemRepository.findById(id)
                        .flatMap(item -> redis.opsForValue().set(key, item, ttl).thenReturn(item)));
    }

    public Mono<Map<Long, Item>> findByIds(Collection<Long> itemIds) {
        if (itemIds == null || itemIds.isEmpty()) {
            return Mono.just(Map.of());
        }
        List<Long> ids = new ArrayList<>(itemIds.stream().filter(Objects::nonNull).distinct().toList());
        if (ids.isEmpty()) {
            return Mono.just(Map.of());
        }
        return Flux.fromIterable(ids)
                .flatMap(id -> findById(id).map(item -> Map.entry(id, item)))
                .collectMap(Map.Entry::getKey, Map.Entry::getValue);
    }
}
