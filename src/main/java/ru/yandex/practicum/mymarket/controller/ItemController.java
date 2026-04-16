package ru.yandex.practicum.mymarket.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.yandex.practicum.mymarket.dto.CartAction;
import ru.yandex.practicum.mymarket.service.CartService;
import ru.yandex.practicum.mymarket.service.ItemService;

@Controller
@RequiredArgsConstructor
public class ItemController {

    private final CartService cartService;
    private final ItemService itemService;

    @PostMapping("/items")
    public String changeCountFromCatalog(
            @RequestParam long id,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) Integer pageNumber,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam CartAction action,
            RedirectAttributes redirectAttributes
    ) {
        cartService.changeItemCount(id, action);

        if (search != null) {
            redirectAttributes.addAttribute("search", search);
        }
        if (sort != null) {
            redirectAttributes.addAttribute("sort", sort);
        }
        if (pageNumber != null) {
            redirectAttributes.addAttribute("pageNumber", pageNumber);
        }
        if (pageSize != null) {
            redirectAttributes.addAttribute("pageSize", pageSize);
        }

        return "redirect:/items";
    }

    @GetMapping("/items/{id}")
    public String getItem(@PathVariable long id, Model model) {
        model.addAttribute("item", itemService.getItem(id));
        return "item";
    }

    @PostMapping("/items/{id}")
    public String changeCountFromItemPage(
            @PathVariable long id,
            @RequestParam CartAction action,
            Model model
    ) {
        cartService.changeItemCount(id, action);
        model.addAttribute("item", itemService.getItem(id));
        return "item";
    }
}
