package ru.yandex.practicum.mymarket.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.dto.CartAction;
import ru.yandex.practicum.mymarket.model.CartItem;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.cache.ItemCacheService;
import ru.yandex.practicum.mymarket.repository.CartItemRepository;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {
    private static final long USER_ID = 1L;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ItemCacheService itemCacheService;

    private CartService cartService;

    @BeforeEach
    void setUp() {
        cartService = new CartService(cartItemRepository, itemCacheService);
        lenient().when(itemCacheService.findById(anyLong())).thenReturn(Mono.empty());
    }

    @Test
    void plusAddsNewCartLineWhenItemWasNotInCart() {
        when(cartItemRepository.findByUserIdAndItemId(USER_ID, 1L)).thenReturn(Mono.empty());
        Item item = new Item();
        item.setId(1L);
        item.setTitle("T");
        item.setPrice(100L);
        when(itemCacheService.findById(1L)).thenReturn(Mono.just(item));
        doAnswer(invocation -> Mono.just(invocation.getArgument(0)))
                .when(cartItemRepository)
                .save(any(CartItem.class));

        cartService.changeItemCount(USER_ID, 1L, CartAction.PLUS).block();

        ArgumentCaptor<CartItem> captor = ArgumentCaptor.forClass(CartItem.class);
        verify(cartItemRepository).save(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo(USER_ID);
        assertThat(captor.getValue().getItemId()).isEqualTo(1L);
        assertThat(captor.getValue().getCount()).isEqualTo(1);
    }

    @Test
    void plusIncrementsCountWhenItemAlreadyInCart() {
        CartItem existing = new CartItem();
        existing.setItemId(2L);
        existing.setCount(3);
        when(cartItemRepository.findByUserIdAndItemId(USER_ID, 2L)).thenReturn(Mono.just(existing));
        doAnswer(invocation -> Mono.just(invocation.getArgument(0)))
                .when(cartItemRepository)
                .save(any(CartItem.class));

        cartService.changeItemCount(USER_ID, 2L, CartAction.PLUS).block();

        verify(cartItemRepository).save(existing);
        assertThat(existing.getCount()).isEqualTo(4);
    }

    @Test
    void minusDecrementsCountWhenCountStaysPositive() {
        CartItem existing = new CartItem();
        existing.setItemId(3L);
        existing.setCount(2);
        when(cartItemRepository.findByUserIdAndItemId(USER_ID, 3L)).thenReturn(Mono.just(existing));
        when(cartItemRepository.save(existing)).thenReturn(Mono.just(existing));

        cartService.changeItemCount(USER_ID, 3L, CartAction.MINUS).block();

        verify(cartItemRepository).save(existing);
        verify(cartItemRepository, never()).delete(any());
        assertThat(existing.getCount()).isEqualTo(1);
    }

    @Test
    void minusRemovesLineWhenCountBecomesZero() {
        CartItem existing = new CartItem();
        existing.setItemId(4L);
        existing.setCount(1);
        when(cartItemRepository.findByUserIdAndItemId(USER_ID, 4L)).thenReturn(Mono.just(existing));
        when(cartItemRepository.delete(existing)).thenReturn(Mono.empty());

        cartService.changeItemCount(USER_ID, 4L, CartAction.MINUS).block();

        verify(cartItemRepository).delete(existing);
        verify(cartItemRepository, never()).save(any());
    }

    @Test
    void deleteRemovesCartLineWhenPresent() {
        CartItem existing = new CartItem();
        when(cartItemRepository.findByUserIdAndItemId(USER_ID, 5L)).thenReturn(Mono.just(existing));
        when(cartItemRepository.delete(existing)).thenReturn(Mono.empty());

        cartService.changeItemCount(USER_ID, 5L, CartAction.DELETE).block();

        verify(cartItemRepository).delete(existing);
    }

    @Test
    void deleteDoesNothingWhenLineAbsent() {
        when(cartItemRepository.findByUserIdAndItemId(USER_ID, 6L)).thenReturn(Mono.empty());

        cartService.changeItemCount(USER_ID, 6L, CartAction.DELETE).block();

        verify(cartItemRepository, never()).delete(any());
    }

    @Test
    void plusThrowsWhenItemDoesNotExist() {
        when(cartItemRepository.findByUserIdAndItemId(USER_ID, 7L)).thenReturn(Mono.empty());
        when(itemCacheService.findById(7L)).thenReturn(Mono.empty());

        assertThatThrownBy(() -> cartService.changeItemCount(USER_ID, 7L, CartAction.PLUS).block())
                .isInstanceOf(ResponseStatusException.class);

        verify(cartItemRepository, never()).save(any());
    }

    @Test
    void getTotalSumsPriceTimesCountForAllLines() {
        Item a = new Item();
        a.setId(1L);
        a.setPrice(100L);
        CartItem ca = new CartItem();
        ca.setItemId(1L);
        ca.setCount(2);

        Item b = new Item();
        b.setId(2L);
        b.setPrice(50L);
        CartItem cb = new CartItem();
        cb.setItemId(2L);
        cb.setCount(1);

        when(cartItemRepository.findAllByUserIdOrderByIdAsc(USER_ID)).thenReturn(Flux.just(ca, cb));
        when(itemCacheService.findByIds(List.of(1L, 2L))).thenReturn(Mono.just(Map.of(1L, a, 2L, b)));

        assertThat(cartService.getTotal(USER_ID).block()).isEqualTo(100L * 2 + 50L * 1);
    }
}
