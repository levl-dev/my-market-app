package ru.yandex.practicum.mymarket.repository;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.model.CartItem;

import java.util.Collection;

public interface CartItemRepository extends ReactiveCrudRepository<CartItem, Long> {

    Mono<CartItem> findByUserIdAndItemId(Long userId, Long itemId);

    Flux<CartItem> findByUserIdAndItemIdIn(Long userId, Collection<Long> itemIds);

    Flux<CartItem> findAllByUserIdOrderByIdAsc(Long userId);

    Mono<Void> deleteAllByUserId(Long userId);
}