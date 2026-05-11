package ru.yandex.practicum.mymarket.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.dto.CartAction;
import ru.yandex.practicum.mymarket.dto.ItemCard;
import ru.yandex.practicum.mymarket.cache.ItemCacheService;
import ru.yandex.practicum.mymarket.model.CartItem;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.repository.CartItemRepository;

import java.util.*;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ItemCacheService itemCacheService;

    public Mono<Void> changeItemCount(long userId, long itemId, CartAction action) {
        Mono<CartItem> cartItemMono = cartItemRepository.findByUserIdAndItemId(userId, itemId);
        if (action == CartAction.DELETE) {
            return cartItemMono.flatMap(cartItemRepository::delete).then();
        }

        if (action == CartAction.PLUS) {
            return cartItemMono
                    .flatMap(existing -> {
                        existing.setCount(existing.getCount() + 1);
                        return cartItemRepository.save(existing);
                    })
                    .switchIfEmpty(
                            itemCacheService.findById(itemId)
                                    .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found")))
                                    .flatMap(item -> {
                                        CartItem created = new CartItem();
                                        created.setUserId(userId);
                                        created.setItemId(item.getId());
                                        created.setCount(1);
                                        return cartItemRepository.save(created);
                                    })
                    )
                    .then();
        }

        return cartItemMono.flatMap(cartItem -> {
            int newCount = cartItem.getCount() - 1;
            if (newCount <= 0) {
                return cartItemRepository.delete(cartItem).then();
            }
            cartItem.setCount(newCount);
            return cartItemRepository.save(cartItem).then();
        }).then();
    }

    public Mono<Integer> getItemCount(long userId, long itemId) {
        return cartItemRepository.findByUserIdAndItemId(userId, itemId).map(CartItem::getCount).defaultIfEmpty(0);
    }

    public Mono<Map<Long, Integer>> getItemCounts(long userId, Collection<Long> itemIds) {
        if (itemIds.isEmpty()) {
            return Mono.just(Map.of());
        }

        return cartItemRepository.findByUserIdAndItemIdIn(userId, itemIds)
                .collectList()
                .map(cartItems -> {
                    Map<Long, Integer> counts = new HashMap<>();

                    for (CartItem cartItem : cartItems) {
                        counts.put(cartItem.getItemId(), cartItem.getCount());
                    }

                    return counts;
                });
    }

    public Mono<List<ItemCard>> getCartItems(long userId) {
        return cartItemRepository.findAllByUserIdOrderByIdAsc(userId)
                .collectList()
                .flatMap(cartItems -> {
                    if (cartItems.isEmpty()) {
                        return Mono.just(List.of());
                    }

                    List<Long> itemIds = cartItems.stream()
                            .map(CartItem::getItemId)
                            .toList();

                    return itemCacheService.findByIds(itemIds)
                            .map(itemsById -> {
                                List<ItemCard> result = new ArrayList<>();

                                for (CartItem cartItem : cartItems) {
                                    Item item = itemsById.get(cartItem.getItemId());
                                    result.add(toItemCard(cartItem, item));
                                }

                                return result;
                            });
                });
    }

    public Mono<Long> getTotal(long userId) {
        return getCartItems(userId)
                .map(items -> items.stream()
                        .mapToLong(item -> item.price() * item.count())
                        .sum());
    }

    public Mono<List<CartItem>> getCartItemsForOrder(long userId) {
        return cartItemRepository.findAllByUserIdOrderByIdAsc(userId).collectList();
    }

    public Mono<Void> clearCart(long userId) {
        return cartItemRepository.deleteAllByUserId(userId);
    }

    private ItemCard toItemCard(CartItem cartItem, Item item) {
        return new ItemCard(
                item.getId(),
                item.getTitle(),
                item.getDescription(),
                item.getImgPath(),
                item.getPrice(),
                cartItem.getCount()
        );
    }
}
