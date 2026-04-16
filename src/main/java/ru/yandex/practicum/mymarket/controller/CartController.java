package ru.yandex.practicum.mymarket.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.yandex.practicum.mymarket.dto.CartAction;
import ru.yandex.practicum.mymarket.service.CartService;

@Controller
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping("/cart/items")
    public String getCart(Model model) {
        fillCartModel(model);
        return "cart";
    }

    @PostMapping("/cart/items")
    public String changeCartItem(
            @RequestParam long id,
            @RequestParam CartAction action,
            Model model
    ) {
        cartService.changeItemCount(id, action);
        fillCartModel(model);
        return "cart";
    }

    private void fillCartModel(Model model) {
        model.addAttribute("items", cartService.getCartItems());
        model.addAttribute("total", cartService.getTotal());
    }
}
