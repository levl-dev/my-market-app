package ru.yandex.practicum.mymarket.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.mymarket.dto.CartAction;
import ru.yandex.practicum.mymarket.model.CartItem;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.repository.CartItemRepository;
import ru.yandex.practicum.mymarket.repository.ItemRepository;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private CartService cartService;

    @Test
    void plusAddsNewCartLineWhenItemWasNotInCart() {
        when(cartItemRepository.findByItemId(1L)).thenReturn(Optional.empty());
        Item item = new Item();
        item.setId(1L);
        item.setTitle("T");
        item.setPrice(100L);
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));

        cartService.changeItemCount(1L, CartAction.PLUS);

        ArgumentCaptor<CartItem> captor = ArgumentCaptor.forClass(CartItem.class);
        verify(cartItemRepository).save(captor.capture());
        assertThat(captor.getValue().getItem()).isSameAs(item);
        assertThat(captor.getValue().getCount()).isEqualTo(1);
    }

    @Test
    void plusIncrementsCountWhenItemAlreadyInCart() {
        Item item = new Item();
        item.setId(2L);
        CartItem existing = new CartItem();
        existing.setItem(item);
        existing.setCount(3);
        when(cartItemRepository.findByItemId(2L)).thenReturn(Optional.of(existing));

        cartService.changeItemCount(2L, CartAction.PLUS);

        verify(cartItemRepository).save(existing);
        assertThat(existing.getCount()).isEqualTo(4);
    }

    @Test
    void minusDecrementsCountWhenCountStaysPositive() {
        Item item = new Item();
        item.setId(3L);
        CartItem existing = new CartItem();
        existing.setItem(item);
        existing.setCount(2);
        when(cartItemRepository.findByItemId(3L)).thenReturn(Optional.of(existing));

        cartService.changeItemCount(3L, CartAction.MINUS);

        verify(cartItemRepository).save(existing);
        verify(cartItemRepository, never()).delete(any());
        assertThat(existing.getCount()).isEqualTo(1);
    }

    @Test
    void minusRemovesLineWhenCountBecomesZero() {
        Item item = new Item();
        item.setId(4L);
        CartItem existing = new CartItem();
        existing.setItem(item);
        existing.setCount(1);
        when(cartItemRepository.findByItemId(4L)).thenReturn(Optional.of(existing));

        cartService.changeItemCount(4L, CartAction.MINUS);

        verify(cartItemRepository).delete(existing);
        verify(cartItemRepository, never()).save(any());
    }

    @Test
    void deleteRemovesCartLineWhenPresent() {
        CartItem existing = new CartItem();
        when(cartItemRepository.findByItemId(5L)).thenReturn(Optional.of(existing));

        cartService.changeItemCount(5L, CartAction.DELETE);

        verify(cartItemRepository).delete(existing);
    }

    @Test
    void deleteDoesNothingWhenLineAbsent() {
        when(cartItemRepository.findByItemId(6L)).thenReturn(Optional.empty());

        cartService.changeItemCount(6L, CartAction.DELETE);

        verify(cartItemRepository, never()).delete(any());
    }

    @Test
    void plusThrowsWhenItemDoesNotExist() {
        when(cartItemRepository.findByItemId(7L)).thenReturn(Optional.empty());
        when(itemRepository.findById(7L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.changeItemCount(7L, CartAction.PLUS))
                .isInstanceOf(ResponseStatusException.class);
        verify(cartItemRepository, never()).save(any());
    }

    @Test
    void getTotalSumsPriceTimesCountForAllLines() {
        Item a = new Item();
        a.setId(1L);
        a.setPrice(100L);
        CartItem ca = new CartItem();
        ca.setItem(a);
        ca.setCount(2);

        Item b = new Item();
        b.setId(2L);
        b.setPrice(50L);
        CartItem cb = new CartItem();
        cb.setItem(b);
        cb.setCount(1);

        when(cartItemRepository.findAllByOrderByIdAsc()).thenReturn(List.of(ca, cb));

        assertThat(cartService.getTotal()).isEqualTo(100L * 2 + 50L * 1);
    }
}
