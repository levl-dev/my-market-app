package ru.yandex.practicum.mymarket.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.client.PaymentClient;
import ru.yandex.practicum.mymarket.client.dto.PaymentResponse;
import ru.yandex.practicum.mymarket.model.CartItem;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.model.Order;
import ru.yandex.practicum.mymarket.model.OrderItem;
import ru.yandex.practicum.mymarket.cache.ItemCacheService;
import ru.yandex.practicum.mymarket.repository.OrderItemRepository;
import ru.yandex.practicum.mymarket.repository.OrderRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {
    private static final long USER_ID = 1L;

    @Mock
    private CartService cartService;

    @Mock
    private ItemCacheService itemCacheService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private PaymentClient paymentClient;

    @InjectMocks
    private OrderService orderService;

    @Test
    void createOrderFromCartCopiesSnapshotClearsCartAndReturnsId() {
        Item item1 = new Item();
        item1.setId(10L);
        item1.setTitle("Ball");
        item1.setPrice(200L);
        CartItem line1 = new CartItem();
        line1.setItemId(10L);
        line1.setCount(2);

        Item item2 = new Item();
        item2.setId(20L);
        item2.setTitle("Mug");
        item2.setPrice(50L);
        CartItem line2 = new CartItem();
        line2.setItemId(20L);
        line2.setCount(1);

        long totalPrice = 200L * 2 + 50L * 1;

        when(cartService.getCartItemsForOrder(USER_ID)).thenReturn(Mono.just(List.of(line1, line2)));
        when(itemCacheService.findByIds(List.of(10L, 20L))).thenReturn(Mono.just(Map.of(10L, item1, 20L, item2)));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            o.setId(99L);
            return Mono.just(o);
        });
        when(orderItemRepository.saveAll(anyIterable())).thenReturn(Flux.empty());
        when(cartService.clearCart(USER_ID)).thenReturn(Mono.empty());
        when(paymentClient.pay(eq(totalPrice)))
                .thenReturn(Mono.just(new PaymentResponse(true, 9_000L, "Payment completed")));

        long id = orderService.createOrderFromCart(USER_ID).block();
        assertThat(id).isEqualTo(99L);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        Order saved = orderCaptor.getValue();
        assertThat(saved.getUserId()).isEqualTo(USER_ID);
        assertThat(saved.getTotalSum()).isEqualTo(totalPrice);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<Iterable<OrderItem>> itemsCaptor = ArgumentCaptor.forClass(Iterable.class);
        verify(orderItemRepository).saveAll(itemsCaptor.capture());

        List<OrderItem> savedItems = new ArrayList<>();
        itemsCaptor.getValue().forEach(savedItems::add);
        assertThat(savedItems).hasSize(2);
        assertThat(savedItems).allMatch(oi -> oi.getOrderId() == 99L);

        OrderItem first = savedItems.stream().filter(oi -> oi.getItemId() == 10L).findFirst().orElseThrow();
        assertThat(first.getTitle()).isEqualTo("Ball");
        assertThat(first.getPrice()).isEqualTo(200L);
        assertThat(first.getCount()).isEqualTo(2);

        OrderItem second = savedItems.stream().filter(oi -> oi.getItemId() == 20L).findFirst().orElseThrow();
        assertThat(second.getTitle()).isEqualTo("Mug");
        assertThat(second.getPrice()).isEqualTo(50L);
        assertThat(second.getCount()).isEqualTo(1);

        verify(cartService).clearCart(USER_ID);
        verify(paymentClient).pay(eq(totalPrice));
    }
}
