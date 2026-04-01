package ru.yandex.practicum.mymarket.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import ru.yandex.practicum.mymarket.service.OrderService;

@Controller
@RequiredArgsConstructor
public class PurchaseController {

    private final OrderService orderService;

    @PostMapping("/buy")
    public String buy() {
        long orderId = orderService.createOrderFromCart();
        return "redirect:/orders/" + orderId + "?newOrder=true";
    }
}
