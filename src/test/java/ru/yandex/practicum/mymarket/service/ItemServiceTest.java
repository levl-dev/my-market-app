package ru.yandex.practicum.mymarket.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.mymarket.dto.ItemCard;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.repository.ItemRepository;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

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
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(cartService.getItemCount(1L)).thenReturn(5);

        ItemCard card = itemService.getItem(1L);

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
        when(itemRepository.findById(2L)).thenReturn(Optional.of(item));
        when(cartService.getItemCount(2L)).thenReturn(0);

        ItemCard card = itemService.getItem(2L);

        assertThat(card.count()).isZero();
    }
}
