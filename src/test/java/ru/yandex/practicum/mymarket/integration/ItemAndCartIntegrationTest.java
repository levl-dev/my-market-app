package ru.yandex.practicum.mymarket.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.mymarket.dto.CartAction;
import ru.yandex.practicum.mymarket.dto.ItemCard;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.repository.CartItemRepository;
import ru.yandex.practicum.mymarket.repository.ItemRepository;
import ru.yandex.practicum.mymarket.service.CartService;
import ru.yandex.practicum.mymarket.service.ItemService;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class ItemAndCartIntegrationTest {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private CartService cartService;

    @Autowired
    private ItemService itemService;

    @Test
    @Transactional
    void itemServiceReflectsCartCountAfterCartChanges() {
        cartItemRepository.deleteAll();
        itemRepository.deleteAll();

        Item item = new Item();
        item.setTitle("TestItem1");
        item.setDescription("TestDescription1");
        item.setImgPath("/img.png");
        item.setPrice(1000L);
        Item saved = itemRepository.save(item);

        ItemCard before = itemService.getItem(saved.getId());
        assertThat(before.count()).isZero();

        cartService.changeItemCount(saved.getId(), CartAction.PLUS);
        ItemCard afterFirstPlus = itemService.getItem(saved.getId());
        assertThat(afterFirstPlus.count()).isEqualTo(1);

        cartService.changeItemCount(saved.getId(), CartAction.PLUS);
        ItemCard afterSecondPlus = itemService.getItem(saved.getId());
        assertThat(afterSecondPlus.count()).isEqualTo(2);

        cartService.changeItemCount(saved.getId(), CartAction.MINUS);
        ItemCard afterMinus = itemService.getItem(saved.getId());
        assertThat(afterMinus.count()).isEqualTo(1);
    }
}
