package ru.yandex.practicum.mymarket.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.mymarket.dto.CartAction;
import ru.yandex.practicum.mymarket.dto.ItemCard;
import ru.yandex.practicum.mymarket.model.CartItem;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.repository.CartItemRepository;
import ru.yandex.practicum.mymarket.repository.ItemRepository;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ItemRepository itemRepository;

    @Transactional
    public void changeItemCount(long itemId, CartAction action) {
        CartItem cartItem = cartItemRepository.findByItemId(itemId).orElse(null);

        if (action == CartAction.DELETE) {
            if (cartItem != null) {
                cartItemRepository.delete(cartItem);
            }
            return;
        }

        if (action == CartAction.PLUS) {
            if (cartItem == null) {
                Item item = itemRepository.findById(itemId).orElseThrow(
                        () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found")
                );
                cartItem = new CartItem();
                cartItem.setItem(item);
                cartItem.setCount(1);
            } else {
                cartItem.setCount(cartItem.getCount() + 1);
            }
            cartItemRepository.save(cartItem);
            return;
        }

        if (cartItem == null) {
            return;
        }

        int newCount = cartItem.getCount() - 1;
        if (newCount <= 0) {
            cartItemRepository.delete(cartItem);
        } else {
            cartItem.setCount(newCount);
            cartItemRepository.save(cartItem);
        }
    }

    @Transactional(readOnly = true)
    public int getItemCount(long itemId) {
        return cartItemRepository.findByItemId(itemId).map(CartItem::getCount).orElse(0);
    }

    @Transactional(readOnly = true)
    public Map<Long, Integer> getItemCounts(Collection<Long> itemIds) {
        if (itemIds.isEmpty()) {
            return Map.of();
        }

        Map<Long, Integer> counts = new HashMap<>();
        for (CartItem cartItem : cartItemRepository.findByItemIdIn(itemIds)) {
            counts.put(cartItem.getItem().getId(), cartItem.getCount());
        }
        return counts;
    }

    @Transactional(readOnly = true)
    public List<ItemCard> getCartItems() {
        return cartItemRepository.findAllByOrderByIdAsc().stream()
                .map(this::toItemCard)
                .toList();
    }

    @Transactional(readOnly = true)
    public long getTotal() {
        return cartItemRepository.findAllByOrderByIdAsc().stream()
                .mapToLong(ci -> ci.getItem().getPrice() * ci.getCount())
                .sum();
    }

    @Transactional(readOnly = true)
    public List<CartItem> getCartItemsForOrder() {
        return cartItemRepository.findAllByOrderByIdAsc();
    }

    @Transactional
    public void clearCart() {
        cartItemRepository.deleteAllInBatch();
    }

    private ItemCard toItemCard(CartItem cartItem) {
        Item item = cartItem.getItem();
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
