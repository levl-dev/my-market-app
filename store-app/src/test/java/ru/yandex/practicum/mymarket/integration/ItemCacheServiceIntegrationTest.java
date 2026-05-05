package ru.yandex.practicum.mymarket.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.cache.ItemCacheService;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.repository.CartItemRepository;
import ru.yandex.practicum.mymarket.repository.ItemRepository;
import ru.yandex.practicum.mymarket.repository.OrderItemRepository;
import ru.yandex.practicum.mymarket.repository.OrderRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class ItemCacheServiceIntegrationTest {

    private static final String KEY_ITEM_PREFIX = "my-market:item:";
    private static final String KEY_CATALOG_LIMITED = "my-market:catalog:limited";

    @Autowired
    private ItemCacheService itemCacheService;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ReactiveRedisTemplate<String, Item> redis;

    @BeforeEach
    void cleanStorage() {
        cleanRedisCacheKeys()
                .then(orderItemRepository.deleteAll())
                .then(orderRepository.deleteAll())
                .then(cartItemRepository.deleteAll())
                .then(itemRepository.deleteAll())
                .block();
    }

    @Test
    void findByIdCachesItem() {
        Item saved = itemRepository.save(item("Cache Item", 1500L)).block();

        Item loaded = itemCacheService.findById(saved.getId()).block();

        assertThat(loaded).isNotNull();
        assertThat(loaded.getId()).isEqualTo(saved.getId());
        String key = KEY_ITEM_PREFIX + saved.getId();
        assertThat(redis.hasKey(key).block()).isTrue();
        Item cached = redis.opsForValue().get(key).block();
        assertThat(cached).isNotNull();
        assertThat(cached.getId()).isEqualTo(saved.getId());
        assertThat(cached.getTitle()).isEqualTo(saved.getTitle());
    }

    @Test
    void findAllItemsCachesCatalog() {
        itemRepository.save(item("Ball", 2500L)).block();
        itemRepository.save(item("Mug", 700L)).block();

        List<Item> loaded = itemCacheService.findAllItems().collectList().block();

        assertThat(loaded).hasSize(2);
        assertThat(redis.hasKey(KEY_CATALOG_LIMITED).block()).isTrue();
        List<Item> cached = redis.opsForList().range(KEY_CATALOG_LIMITED, 0, -1).collectList().block();
        assertThat(cached).hasSize(2);
        assertThat(cached).extracting(Item::getTitle).containsExactlyInAnyOrder("Ball", "Mug");
    }

    private Mono<Void> cleanRedisCacheKeys() {
        ScanOptions options = ScanOptions.scanOptions()
                .match(KEY_ITEM_PREFIX + "*")
                .count(1000)
                .build();
        return redis.scan(options)
                .collectList()
                .flatMap(keys -> keys.isEmpty() ? Mono.empty() : redis.delete(Flux.fromIterable(keys)).then())
                .then(redis.delete(KEY_CATALOG_LIMITED).then());
    }

    private static Item item(String title, long price) {
        Item item = new Item();
        item.setTitle(title);
        item.setDescription("");
        item.setImgPath("/img.png");
        item.setPrice(price);
        return item;
    }
}
