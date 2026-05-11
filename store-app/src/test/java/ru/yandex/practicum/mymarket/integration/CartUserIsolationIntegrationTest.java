package ru.yandex.practicum.mymarket.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import ru.yandex.practicum.mymarket.dto.CartAction;
import ru.yandex.practicum.mymarket.dto.ItemCard;
import ru.yandex.practicum.mymarket.model.AppUser;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.repository.AppUserRepository;
import ru.yandex.practicum.mymarket.repository.CartItemRepository;
import ru.yandex.practicum.mymarket.repository.ItemRepository;
import ru.yandex.practicum.mymarket.repository.OrderItemRepository;
import ru.yandex.practicum.mymarket.repository.OrderRepository;
import ru.yandex.practicum.mymarket.service.CartService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class CartUserIsolationIntegrationTest {
    private static final String PASSWORD = "unused";

    @Autowired
    private CartService cartService;

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
    void cartIsolatedBetweenUsers() {
        AppUser user1 = appUserRepository.save(user("user1")).block();
        AppUser user2 = appUserRepository.save(user("user2")).block();
        Item item = itemRepository.save(item("Ball")).block();

        cartService.changeItemCount(user1.getId(), item.getId(), CartAction.PLUS).block();

        List<ItemCard> user1Cart = cartService.getCartItems(user1.getId()).block();
        List<ItemCard> user2Cart = cartService.getCartItems(user2.getId()).block();

        assertThat(user1Cart).hasSize(1);
        assertThat(user1Cart.getFirst().id()).isEqualTo(item.getId());
        assertThat(user1Cart.getFirst().count()).isEqualTo(1);
        assertThat(user2Cart).isEmpty();
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
