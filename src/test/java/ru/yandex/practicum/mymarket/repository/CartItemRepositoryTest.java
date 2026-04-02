package ru.yandex.practicum.mymarket.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import ru.yandex.practicum.mymarket.model.CartItem;
import ru.yandex.practicum.mymarket.model.Item;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CartItemRepositoryTest {

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ItemRepository itemRepository;

    @Test
    void findByItemIdReturnsCartLine() {
        Item item = itemRepository.save(item("Ball", 2500L));
        CartItem cartItem = new CartItem();
        cartItem.setItem(item);
        cartItem.setCount(2);
        cartItemRepository.save(cartItem);

        var found = cartItemRepository.findByItemId(item.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getCount()).isEqualTo(2);
        assertThat(found.get().getItem().getId()).isEqualTo(item.getId());
    }

    @Test
    void findByItemIdInReturnsOnlyRequestedItems() {
        Item first = itemRepository.save(item("One", 100L));
        Item second = itemRepository.save(item("Two", 200L));

        CartItem line1 = new CartItem();
        line1.setItem(first);
        line1.setCount(1);
        cartItemRepository.save(line1);

        CartItem line2 = new CartItem();
        line2.setItem(second);
        line2.setCount(3);
        cartItemRepository.save(line2);

        List<CartItem> found = cartItemRepository.findByItemIdIn(List.of(second.getId()));

        assertThat(found).hasSize(1);
        assertThat(found.get(0).getItem().getId()).isEqualTo(second.getId());
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
}
