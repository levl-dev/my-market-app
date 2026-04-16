package ru.yandex.practicum.mymarket.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.mymarket.model.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
