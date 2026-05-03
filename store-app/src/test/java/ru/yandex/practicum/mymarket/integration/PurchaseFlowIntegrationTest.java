package ru.yandex.practicum.mymarket.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.model.CartItem;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.model.Order;
import ru.yandex.practicum.mymarket.model.OrderItem;
import ru.yandex.practicum.mymarket.repository.CartItemRepository;
import ru.yandex.practicum.mymarket.repository.ItemRepository;
import ru.yandex.practicum.mymarket.repository.OrderItemRepository;
import ru.yandex.practicum.mymarket.repository.OrderRepository;
import ru.yandex.practicum.mymarket.client.PaymentClient;
import ru.yandex.practicum.mymarket.client.dto.PaymentResponse;
import ru.yandex.practicum.mymarket.service.OrderService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class PurchaseFlowIntegrationTest {

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private OrderService orderService;

    @MockBean
    private PaymentClient paymentClient;

    @BeforeEach
    void cleanDb() {
        when(paymentClient.pay(anyLong()))
                .thenReturn(Mono.just(new PaymentResponse(true, 10_000L, "Payment completed")));
        orderItemRepository.deleteAll()
                .then(orderRepository.deleteAll())
                .then(cartItemRepository.deleteAll())
                .then(itemRepository.deleteAll())
                .block();
    }

    @Test
    void createOrderFromCartCreatesOrderWithSnapshotAndClearsCart() {
        Item ball = itemRepository.save(item("Ball", 2500L)).block();
        Item mug = itemRepository.save(item("Mug", 700L)).block();

        cartItemRepository.save(cartItem(ball.getId(), 2)).block();
        cartItemRepository.save(cartItem(mug.getId(), 1)).block();

        long id = orderService.createOrderFromCart().block();

        OrderSnapshot snap = snapshot(id, ball.getId(), mug.getId()).block();

        assertThat(snap.order().getTotalSum()).isEqualTo(5700L);
        assertThat(snap.orderItems()).hasSize(2);
        assertThat(snap.orderItems())
                .extracting(oi -> oi.getItemId() + ":" + oi.getTitle() + ":" + oi.getPrice() + ":" + oi.getCount())
                .containsExactlyInAnyOrder(
                        snap.ballId() + ":Ball:2500:2",
                        snap.mugId() + ":Mug:700:1"
                );
        assertThat(snap.cartRows()).isZero();
    }

    private Mono<OrderSnapshot> snapshot(long orderId, long ballId, long mugId) {
        return Mono.zip(
                orderRepository.findById(orderId).single(),
                orderItemRepository.findByOrderId(orderId).collectList(),
                cartItemRepository.findAll().count()
        ).map(t -> new OrderSnapshot(t.getT1(), t.getT2(), t.getT3(), ballId, mugId));
    }

    private record OrderSnapshot(Order order, List<OrderItem> orderItems, long cartRows, long ballId, long mugId) {
    }

    private static Item item(String title, long price) {
        Item item = new Item();
        item.setTitle(title);
        item.setDescription("");
        item.setImgPath("/img.png");
        item.setPrice(price);
        return item;
    }

    private static CartItem cartItem(long itemId, int count) {
        CartItem cartItem = new CartItem();
        cartItem.setItemId(itemId);
        cartItem.setCount(count);
        return cartItem;
    }
}
