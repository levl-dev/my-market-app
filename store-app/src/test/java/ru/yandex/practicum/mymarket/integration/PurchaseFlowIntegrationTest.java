package ru.yandex.practicum.mymarket.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.model.AppUser;
import ru.yandex.practicum.mymarket.model.CartItem;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.model.Order;
import ru.yandex.practicum.mymarket.model.OrderItem;
import ru.yandex.practicum.mymarket.repository.AppUserRepository;
import ru.yandex.practicum.mymarket.repository.CartItemRepository;
import ru.yandex.practicum.mymarket.repository.ItemRepository;
import ru.yandex.practicum.mymarket.repository.OrderItemRepository;
import ru.yandex.practicum.mymarket.repository.OrderRepository;
import ru.yandex.practicum.mymarket.client.PaymentClient;
import ru.yandex.practicum.mymarket.client.dto.PaymentResponse;
import ru.yandex.practicum.mymarket.service.OrderService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class PurchaseFlowIntegrationTest {
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
    private OrderService orderService;

    @MockBean
    private PaymentClient paymentClient;

    @BeforeEach
    void cleanDb() {
        when(paymentClient.pay(anyString(), anyLong()))
                .thenReturn(Mono.just(new PaymentResponse(true, 10_000L, "Payment completed")));
        orderItemRepository.deleteAll()
                .then(orderRepository.deleteAll())
                .then(cartItemRepository.deleteAll())
                .then(appUserRepository.deleteAll())
                .then(itemRepository.deleteAll())
                .block();
    }

    @Test
    void createOrderFromCartCreatesOrderWithSnapshotAndClearsCart() {
        AppUser user = appUserRepository.save(user("buyer")).block();
        Item ball = itemRepository.save(item("Ball", 2500L)).block();
        Item mug = itemRepository.save(item("Mug", 700L)).block();

        cartItemRepository.save(cartItem(user.getId(), ball.getId(), 2)).block();
        cartItemRepository.save(cartItem(user.getId(), mug.getId(), 1)).block();

        long id = orderService.createOrderFromCart(user.getId()).block();

        OrderSnapshot snap = snapshot(id, user.getId(), ball.getId(), mug.getId()).block();

        assertThat(snap.order().getUserId()).isEqualTo(user.getId());
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

    @Test
    void getOrdersAndGetOrderReturnOnlyUsersOrders() {
        AppUser first = appUserRepository.save(user("first")).block();
        AppUser second = appUserRepository.save(user("second")).block();

        Order firstOrder = new Order();
        firstOrder.setUserId(first.getId());
        firstOrder.setTotalSum(100L);
        firstOrder.setCreatedAt(java.time.LocalDateTime.now());
        firstOrder = orderRepository.save(firstOrder).block();

        Order secondOrder = new Order();
        secondOrder.setUserId(second.getId());
        secondOrder.setTotalSum(200L);
        secondOrder.setCreatedAt(java.time.LocalDateTime.now());
        secondOrder = orderRepository.save(secondOrder).block();

        List<Order> firstOrders = orderRepository.findAllByUserIdOrderByIdDesc(first.getId()).collectList().block();
        Order visible = orderRepository.findByIdAndUserId(firstOrder.getId(), first.getId()).block();
        Order hidden = orderRepository.findByIdAndUserId(secondOrder.getId(), first.getId()).block();

        assertThat(firstOrders).extracting(Order::getId).containsExactly(firstOrder.getId());
        assertThat(visible).isNotNull();
        assertThat(hidden).isNull();
    }

    private Mono<OrderSnapshot> snapshot(long orderId, long userId, long ballId, long mugId) {
        return Mono.zip(
                orderRepository.findById(orderId).single(),
                orderItemRepository.findByOrderId(orderId).collectList(),
                cartItemRepository.findAllByUserIdOrderByIdAsc(userId).count()
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

    private static CartItem cartItem(long userId, long itemId, int count) {
        CartItem cartItem = new CartItem();
        cartItem.setUserId(userId);
        cartItem.setItemId(itemId);
        cartItem.setCount(count);
        return cartItem;
    }

    private static AppUser user(String username) {
        AppUser appUser = new AppUser();
        appUser.setUsername(username);
        appUser.setPassword(PASSWORD);
        appUser.setEnabled(true);
        return appUser;
    }
}
