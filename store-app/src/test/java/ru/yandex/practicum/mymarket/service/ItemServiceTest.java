package ru.yandex.practicum.mymarket.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.dto.ItemCard;
import ru.yandex.practicum.mymarket.cache.ItemCacheService;
import ru.yandex.practicum.mymarket.model.Item;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {
    private static final long USER_ID = 1L;

    @Mock
    private ItemCacheService itemCacheService;

    @Mock
    private CartService cartService;

    @InjectMocks
    private ItemService itemService;

    @Test
    void getItemReturnsCountFromCart() {
        Item item = new Item();
        item.setId(1L);
        item.setTitle("Title");
        item.setDescription("Desc");
        item.setImgPath("/img.png");
        item.setPrice(300L);
        when(itemCacheService.findById(1L)).thenReturn(Mono.just(item));
        when(cartService.getItemCount(USER_ID, 1L)).thenReturn(Mono.just(5));

        ItemCard card = itemService.getItem(1L, USER_ID).block();

        assertThat(card.id()).isEqualTo(1L);
        assertThat(card.title()).isEqualTo("Title");
        assertThat(card.count()).isEqualTo(5);
    }

    @Test
    void getItemReturnsZeroCountWhenNotInCart() {
        Item item = new Item();
        item.setId(2L);
        item.setTitle("X");
        item.setDescription("");
        item.setImgPath("");
        item.setPrice(1L);
        when(itemCacheService.findById(2L)).thenReturn(Mono.just(item));
        when(cartService.getItemCount(USER_ID, 2L)).thenReturn(Mono.just(0));

        ItemCard card = itemService.getItem(2L, USER_ID).block();

        assertThat(card.count()).isZero();
    }
}
