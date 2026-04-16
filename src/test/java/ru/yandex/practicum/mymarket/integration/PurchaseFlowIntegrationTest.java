package ru.yandex.practicum.mymarket.integration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.mymarket.model.CartItem;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.model.Order;
import ru.yandex.practicum.mymarket.repository.CartItemRepository;
import ru.yandex.practicum.mymarket.repository.ItemRepository;
import ru.yandex.practicum.mymarket.repository.OrderRepository;
import ru.yandex.practicum.mymarket.service.OrderService;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
class PurchaseFlowIntegrationTest {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderService orderService;

    @Test
    @Transactional
    void createOrderFromCartCreatesOrderWithSnapshotAndClearsCart() {
        Item ball = itemRepository.save(item("Ball", 2500L));
        Item mug = itemRepository.save(item("Mug", 700L));

        cartItemRepository.save(cartItem(ball, 2));
        cartItemRepository.save(cartItem(mug, 1));

        long orderId = orderService.createOrderFromCart();

        Order order = orderRepository.findById(orderId).orElseThrow();
        assertThat(order.getTotalSum()).isEqualTo(5700L);
        assertThat(order.getItems()).hasSize(2);
        assertThat(order.getItems())
                .extracting(oi -> oi.getItemId() + ":" + oi.getTitle() + ":" + oi.getPrice() + ":" + oi.getCount())
                .containsExactlyInAnyOrder(
                        ball.getId() + ":Ball:2500:2",
                        mug.getId() + ":Mug:700:1"
                );
        assertThat(cartItemRepository.findAll()).isEmpty();
    }

    private static Item item(String title, long price) {
        Item item = new Item();
        item.setTitle(title);
        item.setDescription("");
        item.setImgPath("/img.png");
        item.setPrice(price);
        return item;
    }

    private static CartItem cartItem(Item item, int count) {
        CartItem cartItem = new CartItem();
        cartItem.setItem(item);
        cartItem.setCount(count);
        return cartItem;
    }

}
