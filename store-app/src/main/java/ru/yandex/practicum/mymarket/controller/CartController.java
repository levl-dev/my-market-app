package ru.yandex.practicum.mymarket.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;

import java.util.List;

import ru.yandex.practicum.mymarket.client.PaymentClient;
import ru.yandex.practicum.mymarket.dto.CartAction;
import ru.yandex.practicum.mymarket.dto.ItemCard;
import ru.yandex.practicum.mymarket.security.CurrentUserService;
import ru.yandex.practicum.mymarket.service.CartService;

@Controller
@RequiredArgsConstructor
public class CartController {

    private static final String MSG_INSUFFICIENT = "Недостаточно средств";
    private static final String MSG_UNAVAILABLE = "Сервис платежей недоступен";

    private final CartService cartService;
    private final PaymentClient paymentClient;
    private final CurrentUserService currentUserService;

    @GetMapping("/cart/items")
    public Mono<Rendering> getCart() {
        return currentUserService.currentUserId().flatMap(this::renderCart);
    }

    @PostMapping("/cart/items")
    public Mono<Rendering> changeCartItem(@RequestParam long id, @RequestParam CartAction action) {
        return currentUserService.currentUserId()
                .flatMap(userId -> cartService.changeItemCount(userId, id, action)
                        .then(renderCart(userId)));
    }

    private Mono<Rendering> renderCart(long userId) {
        return cartService.getCartItems(userId)
                .flatMap(items -> {
                    long total = cartTotal(items);
                    return paymentClient.getBalance()
                            .map(balance -> balance >= total
                                    ? new CartPaymentState(true, "")
                                    : new CartPaymentState(false, MSG_INSUFFICIENT))
                            .onErrorResume(e -> Mono.just(new CartPaymentState(false, MSG_UNAVAILABLE)))
                            .map(state -> Rendering.view("cart")
                                    .modelAttribute("items", items)
                                    .modelAttribute("total", total)
                                    .modelAttribute("canBuy", state.canBuy())
                                    .modelAttribute("paymentMessage", state.paymentMessage())
                                    .build());
                });
    }

    private static long cartTotal(List<ItemCard> items) {
        long total = 0L;
        for (ItemCard item : items) {
            total += item.price() * item.count();
        }
        return total;
    }

    private record CartPaymentState(boolean canBuy, String paymentMessage) {
    }
}
