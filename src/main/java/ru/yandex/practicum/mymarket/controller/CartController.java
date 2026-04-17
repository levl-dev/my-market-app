package ru.yandex.practicum.mymarket.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.dto.CartAction;
import ru.yandex.practicum.mymarket.dto.ItemCard;
import ru.yandex.practicum.mymarket.service.CartService;

@Controller
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping("/cart/items")
    public Mono<Rendering> getCart() {
        return renderCart();
    }

    @PostMapping("/cart/items")
    public Mono<Rendering> changeCartItem(@RequestParam long id, @RequestParam CartAction action) {
        return cartService.changeItemCount(id, action).then(renderCart());
    }

    private Mono<Rendering> renderCart() {
        return cartService.getCartItems()
                .map(items -> {
                    long total = 0L;

                    for (ItemCard item : items) {
                        total += item.price() * item.count();
                    }

                    return Rendering.view("cart")
                            .modelAttribute("items", items)
                            .modelAttribute("total", total)
                            .build();
                });
    }
}
