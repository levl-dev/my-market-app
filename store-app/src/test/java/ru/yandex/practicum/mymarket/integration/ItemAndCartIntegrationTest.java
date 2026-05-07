package ru.yandex.practicum.mymarket.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import ru.yandex.practicum.mymarket.model.AppUser;
import ru.yandex.practicum.mymarket.dto.CartAction;
import ru.yandex.practicum.mymarket.dto.ItemCard;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.repository.AppUserRepository;
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
    private static final String PASSWORD = "unused";

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private AppUserRepository appUserRepository;

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
                .then(appUserRepository.deleteAll())
                .then(itemRepository.deleteAll())
                .block();
    }

    @Test
    void itemServiceReflectsCartCountAfterCartChanges() {
        AppUser user = appUserRepository.save(user("u1")).block();
        Item item = new Item();
        item.setTitle("TestItem1");
        item.setDescription("TestDescription1");
        item.setImgPath("/img.png");
        item.setPrice(1000L);

        Item saved = itemRepository.save(item).block();

        ItemCard before = itemService.getItem(saved.getId(), user.getId()).block();
        assertThat(before.count()).isZero();

        cartService.changeItemCount(user.getId(), saved.getId(), CartAction.PLUS).block();
        ItemCard afterFirstPlus = itemService.getItem(saved.getId(), user.getId()).block();
        assertThat(afterFirstPlus.count()).isEqualTo(1);

        cartService.changeItemCount(user.getId(), saved.getId(), CartAction.PLUS).block();
        ItemCard afterSecondPlus = itemService.getItem(saved.getId(), user.getId()).block();
        assertThat(afterSecondPlus.count()).isEqualTo(2);

        cartService.changeItemCount(user.getId(), saved.getId(), CartAction.MINUS).block();
        ItemCard afterMinus = itemService.getItem(saved.getId(), user.getId()).block();
        assertThat(afterMinus.count()).isEqualTo(1);
    }

    @Test
    void cartIsIsolatedBetweenUsers() {
        AppUser firstUser = appUserRepository.save(user("first")).block();
        AppUser secondUser = appUserRepository.save(user("second")).block();
        Item saved = itemRepository.save(item("SharedItem")).block();

        cartService.changeItemCount(firstUser.getId(), saved.getId(), CartAction.PLUS).block();
        cartService.changeItemCount(firstUser.getId(), saved.getId(), CartAction.PLUS).block();
        cartService.changeItemCount(secondUser.getId(), saved.getId(), CartAction.PLUS).block();

        ItemCard firstCard = itemService.getItem(saved.getId(), firstUser.getId()).block();
        ItemCard secondCard = itemService.getItem(saved.getId(), secondUser.getId()).block();

        assertThat(firstCard.count()).isEqualTo(2);
        assertThat(secondCard.count()).isEqualTo(1);
    }

    private static AppUser user(String username) {
        AppUser appUser = new AppUser();
        appUser.setUsername(username);
        appUser.setPassword(PASSWORD);
        appUser.setEnabled(true);
        return appUser;
    }

    private static Item item(String title) {
        Item item = new Item();
        item.setTitle(title);
        item.setDescription("");
        item.setImgPath("/img.png");
        item.setPrice(100L);
        return item;
    }
}
