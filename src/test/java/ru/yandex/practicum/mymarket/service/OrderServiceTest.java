package ru.yandex.practicum.mymarket.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.yandex.practicum.mymarket.model.CartItem;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.model.Order;
import ru.yandex.practicum.mymarket.model.OrderItem;
import ru.yandex.practicum.mymarket.repository.OrderRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private CartService cartService;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    void createOrderFromCartCopiesSnapshotClearsCartAndReturnsId() {
        Item item1 = new Item();
        item1.setId(10L);
        item1.setTitle("Ball");
        item1.setPrice(200L);
        CartItem line1 = new CartItem();
        line1.setItem(item1);
        line1.setCount(2);

        Item item2 = new Item();
        item2.setId(20L);
        item2.setTitle("Mug");
        item2.setPrice(50L);
        CartItem line2 = new CartItem();
        line2.setItem(item2);
        line2.setCount(1);

        when(cartService.getCartItemsForOrder()).thenReturn(List.of(line1, line2));
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order o = invocation.getArgument(0);
            o.setId(99L);
            return o;
        });

        long id = orderService.createOrderFromCart();

        assertThat(id).isEqualTo(99L);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository).save(orderCaptor.capture());
        Order saved = orderCaptor.getValue();
        assertThat(saved.getTotalSum()).isEqualTo(200L * 2 + 50L * 1);
        assertThat(saved.getItems()).hasSize(2);

        OrderItem first = saved.getItems().get(0);
        assertThat(first.getOrder()).isSameAs(saved);
        assertThat(first.getItemId()).isEqualTo(10L);
        assertThat(first.getTitle()).isEqualTo("Ball");
        assertThat(first.getPrice()).isEqualTo(200L);
        assertThat(first.getCount()).isEqualTo(2);

        OrderItem second = saved.getItems().get(1);
        assertThat(second.getItemId()).isEqualTo(20L);
        assertThat(second.getTitle()).isEqualTo("Mug");
        assertThat(second.getPrice()).isEqualTo(50L);
        assertThat(second.getCount()).isEqualTo(1);

        verify(cartService).clearCart();
    }
}
