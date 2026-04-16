package ru.yandex.practicum.mymarket.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.mymarket.dto.OrderItemView;
import ru.yandex.practicum.mymarket.dto.OrderView;
import ru.yandex.practicum.mymarket.model.CartItem;
import ru.yandex.practicum.mymarket.model.Order;
import ru.yandex.practicum.mymarket.model.OrderItem;
import ru.yandex.practicum.mymarket.repository.OrderRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final CartService cartService;
    private final OrderRepository orderRepository;

    @Transactional
    public long createOrderFromCart() {
        List<CartItem> cartItems = cartService.getCartItemsForOrder();

        Order order = new Order();
        order.setItems(new ArrayList<>());

        long total = 0L;
        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setItemId(cartItem.getItem().getId());
            orderItem.setTitle(cartItem.getItem().getTitle());
            orderItem.setPrice(cartItem.getItem().getPrice());
            orderItem.setCount(cartItem.getCount());
            order.getItems().add(orderItem);

            total += cartItem.getItem().getPrice() * cartItem.getCount();
        }
        order.setTotalSum(total);

        Order savedOrder = orderRepository.save(order);
        cartService.clearCart();
        return savedOrder.getId();
    }

    @Transactional(readOnly = true)
    public List<OrderView> getOrders() {
        return orderRepository.findAllByOrderByIdDesc().stream()
                .map(this::toOrderView)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrderView getOrder(long id) {
        Order order = orderRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found")
        );
        return toOrderView(order);
    }

    private OrderView toOrderView(Order order) {
        List<OrderItemView> items = order.getItems().stream()
                .map(item -> new OrderItemView(item.getItemId(), item.getTitle(), item.getPrice(), item.getCount()))
                .toList();
        return new OrderView(order.getId(), items, order.getTotalSum());
    }
}
