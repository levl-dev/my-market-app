package ru.yandex.practicum.mymarket.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.security.CurrentUserService;
import ru.yandex.practicum.mymarket.service.OrderService;

@Controller
@RequiredArgsConstructor
public class PurchaseController {

    private final OrderService orderService;
    private final CurrentUserService currentUserService;

    @PostMapping("/buy")
    public Mono<String> buy() {
        return currentUserService.currentUserId()
                .flatMap(orderService::createOrderFromCart)
                .map(orderId -> "redirect:/orders/" + orderId + "?newOrder=true");
    }
}
