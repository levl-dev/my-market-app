package ru.yandex.practicum.mymarket.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import ru.yandex.practicum.mymarket.dto.CartAction;
import ru.yandex.practicum.mymarket.dto.ItemCard;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.repository.CartItemRepository;
import ru.yandex.practicum.mymarket.repository.ItemRepository;
import ru.yandex.practicum.mymarket.repository.OrderItemRepository;
import ru.yandex.practicum.mymarket.repository.OrderRepository;
import ru.yandex.practicum.mymarket.service.CartService;
import ru.yandex.practicum.mymarket.service.ItemService;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class ItemAndCartIntegrationTest {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private CartService cartService;

    @Autowired
    private ItemService itemService;

    @BeforeEach
    void cleanDb() {
        orderItemRepository.deleteAll()
                .then(orderRepository.deleteAll())
                .then(cartItemRepository.deleteAll())
                .then(itemRepository.deleteAll())
                .block();
    }

    @Test
    void itemServiceReflectsCartCountAfterCartChanges() {
        Item item = new Item();
        item.setTitle("TestItem1");
        item.setDescription("TestDescription1");
        item.setImgPath("/img.png");
        item.setPrice(1000L);

        Item saved = itemRepository.save(item).block();

        ItemCard before = itemService.getItem(saved.getId()).block();
        assertThat(before.count()).isZero();

        cartService.changeItemCount(saved.getId(), CartAction.PLUS).block();
        ItemCard afterFirstPlus = itemService.getItem(saved.getId()).block();
        assertThat(afterFirstPlus.count()).isEqualTo(1);

        cartService.changeItemCount(saved.getId(), CartAction.PLUS).block();
        ItemCard afterSecondPlus = itemService.getItem(saved.getId()).block();
        assertThat(afterSecondPlus.count()).isEqualTo(2);

        cartService.changeItemCount(saved.getId(), CartAction.MINUS).block();
        ItemCard afterMinus = itemService.getItem(saved.getId()).block();
        assertThat(afterMinus.count()).isEqualTo(1);
    }
}
