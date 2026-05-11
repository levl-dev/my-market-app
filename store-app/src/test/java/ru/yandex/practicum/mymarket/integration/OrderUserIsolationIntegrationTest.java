package ru.yandex.practicum.mymarket.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.mymarket.dto.OrderView;
import ru.yandex.practicum.mymarket.model.AppUser;
import ru.yandex.practicum.mymarket.model.Order;
import ru.yandex.practicum.mymarket.repository.AppUserRepository;
import ru.yandex.practicum.mymarket.repository.CartItemRepository;
import ru.yandex.practicum.mymarket.repository.ItemRepository;
import ru.yandex.practicum.mymarket.repository.OrderItemRepository;
import ru.yandex.practicum.mymarket.repository.OrderRepository;
import ru.yandex.practicum.mymarket.service.OrderService;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class OrderUserIsolationIntegrationTest {
    private static final String PASSWORD = "unused";

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private ItemRepository itemRepository;

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
    void usersSeeOnlyTheirOrders() {
        AppUser user1 = appUserRepository.save(user("user1")).block();
        AppUser user2 = appUserRepository.save(user("user2")).block();

        Order order1 = orderRepository.save(order(user1.getId(), 100L)).block();
        Order order2 = orderRepository.save(order(user2.getId(), 200L)).block();

        List<OrderView> user1Orders = orderService.getOrders(user1.getId()).block();
        List<OrderView> user2Orders = orderService.getOrders(user2.getId()).block();

        assertThat(user1Orders).extracting(OrderView::id).containsExactly(order1.getId());
        assertThat(user2Orders).extracting(OrderView::id).containsExactly(order2.getId());

        assertThatThrownBy(() -> orderService.getOrder(user1.getId(), order2.getId()).block())
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining(HttpStatus.NOT_FOUND.toString());
    }

    private static AppUser user(String username) {
        AppUser appUser = new AppUser();
        appUser.setUsername(username);
        appUser.setPassword(PASSWORD);
        appUser.setEnabled(true);
        return appUser;
    }

    private static Order order(long userId, long total) {
        Order order = new Order();
        order.setUserId(userId);
        order.setTotalSum(total);
        order.setCreatedAt(LocalDateTime.now());
        return order;
    }
}
