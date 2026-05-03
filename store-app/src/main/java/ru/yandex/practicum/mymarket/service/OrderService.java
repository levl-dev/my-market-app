package ru.yandex.practicum.mymarket.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.client.PaymentClient;
import ru.yandex.practicum.mymarket.client.dto.PaymentResponse;
import ru.yandex.practicum.mymarket.dto.OrderItemView;
import ru.yandex.practicum.mymarket.dto.OrderView;
import ru.yandex.practicum.mymarket.model.CartItem;
import ru.yandex.practicum.mymarket.model.Item;
import ru.yandex.practicum.mymarket.model.Order;
import ru.yandex.practicum.mymarket.model.OrderItem;
import ru.yandex.practicum.mymarket.cache.ItemCacheService;
import ru.yandex.practicum.mymarket.repository.OrderItemRepository;
import ru.yandex.practicum.mymarket.repository.OrderRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final CartService cartService;
    private final ItemCacheService itemCacheService;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentClient paymentClient;

    public Mono<Long> createOrderFromCart() {
        return cartService.getCartItemsForOrder()
                .flatMap(cartItems -> loadItemsById(cartItems)
                        .flatMap(itemsById -> {
                            long totalAmount = calculateTotal(cartItems, itemsById);
                            return paymentClient.pay(totalAmount)
                                    .flatMap(this::requirePaymentSuccess)
                                    .then(saveOrder(cartItems, itemsById));
                        }));
    }

    private Mono<PaymentResponse> requirePaymentSuccess(PaymentResponse response) {
        if (response.success()) {
            return Mono.just(response);
        }
        return Mono.error(new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                response.message()));
    }

    private Mono<Map<Long, Item>> loadItemsById(List<CartItem> cartItems) {
        List<Long> itemIds = cartItems.stream()
                .map(CartItem::getItemId)
                .toList();

        return itemCacheService.findByIds(itemIds);
    }

    private Mono<Long> saveOrder(List<CartItem> cartItems, Map<Long, Item> itemsById) {
        Order order = new Order();
        order.setCreatedAt(LocalDateTime.now());
        order.setTotalSum(calculateTotal(cartItems, itemsById));

        List<OrderItem> orderItems = buildOrderItems(cartItems, itemsById);

        return orderRepository.save(order)
                .flatMap(savedOrder -> {
                    for (OrderItem orderItem : orderItems) {
                        orderItem.setOrderId(savedOrder.getId());
                    }

                    return orderItemRepository.saveAll(orderItems)
                            .then(cartService.clearCart())
                            .thenReturn(savedOrder.getId());
                });
    }

    public Mono<List<OrderView>> getOrders() {
        return orderRepository.findAllByOrderByIdDesc()
                .concatMap(this::toOrderView)
                .collectList();
    }

    public Mono<OrderView> getOrder(long id) {
        return orderRepository.findById(id)
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found")))
                .flatMap(this::toOrderView);
    }

    private Mono<OrderView> toOrderView(Order order) {
        return orderItemRepository.findByOrderIdOrderByIdAsc(order.getId())
                .map(item -> new OrderItemView(
                        item.getItemId(),
                        item.getTitle(),
                        item.getPrice(),
                        item.getCount()
                ))
                .collectList()
                .map(items -> new OrderView(order.getId(), items, order.getTotalSum()));
    }

    private long calculateTotal(List<CartItem> cartItems, Map<Long, Item> itemsById) {
        long total = 0L;

        for (CartItem cartItem : cartItems) {
            Item item = itemsById.get(cartItem.getItemId());
            if (item == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found");
            }

            total += item.getPrice() * cartItem.getCount();
        }

        return total;
    }

    private List<OrderItem> buildOrderItems(List<CartItem> cartItems, Map<Long, Item> itemsById) {
        List<OrderItem> result = new ArrayList<>();

        for (CartItem cartItem : cartItems) {
            Item item = itemsById.get(cartItem.getItemId());
            if (item == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Item not found");
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setItemId(item.getId());
            orderItem.setTitle(item.getTitle());
            orderItem.setPrice(item.getPrice());
            orderItem.setCount(cartItem.getCount());

            result.add(orderItem);
        }

        return result;
    }
}