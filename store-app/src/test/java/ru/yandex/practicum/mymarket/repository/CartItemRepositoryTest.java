package ru.yandex.practicum.mymarket.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.r2dbc.DataR2dbcTest;
import org.springframework.test.context.ActiveProfiles;
import ru.yandex.practicum.mymarket.model.AppUser;
import ru.yandex.practicum.mymarket.model.CartItem;
import ru.yandex.practicum.mymarket.model.Item;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataR2dbcTest
@ActiveProfiles("test")
class CartItemRepositoryTest {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @Test
    void findByUserIdAndItemIdReturnsCartLine() {
        AppUser user = appUserRepository.save(user("user1")).block();
        Item item = itemRepository.save(item("Ball", 2500L)).block();
        CartItem cartItem = new CartItem();
        cartItem.setUserId(user.getId());
        cartItem.setItemId(item.getId());
        cartItem.setCount(2);
        cartItemRepository.save(cartItem).block();

        var found = cartItemRepository.findByUserIdAndItemId(user.getId(), item.getId()).block();

        assertThat(found).isNotNull();
        assertThat(found.getCount()).isEqualTo(2);
        assertThat(found.getItemId()).isEqualTo(item.getId());
    }

    @Test
    void findByUserIdAndItemIdInReturnsOnlyRequestedItems() {
        AppUser user = appUserRepository.save(user("user2")).block();
        Item first = itemRepository.save(item("One", 100L)).block();
        Item second = itemRepository.save(item("Two", 200L)).block();
        assertThat(first).isNotNull();
        assertThat(second).isNotNull();

        CartItem line1 = new CartItem();
        line1.setUserId(user.getId());
        line1.setItemId(first.getId());
        line1.setCount(1);

        CartItem line2 = new CartItem();
        line2.setUserId(user.getId());
        line2.setItemId(second.getId());
        line2.setCount(3);

        cartItemRepository.save(line1).block();
        cartItemRepository.save(line2).block();

        List<CartItem> found = cartItemRepository.findByUserIdAndItemIdIn(user.getId(), List.of(second.getId()))
                .collectList()
                .block();

        assertThat(found).hasSize(1);
        assertThat(found.get(0).getItemId()).isEqualTo(second.getId());
        assertThat(found.get(0).getCount()).isEqualTo(3);
    }

    private static Item item(String title, long price) {
        Item item = new Item();
        item.setTitle(title);
        item.setDescription("");
        item.setImgPath("/img.png");
        item.setPrice(price);
        return item;
    }

    private static AppUser user(String username) {
        AppUser user = new AppUser();
        user.setUsername(username);
        user.setPassword("unused");
        user.setEnabled(true);
        return user;
    }
}
