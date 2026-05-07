package ru.yandex.practicum.mymarket.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.reactive.result.view.Rendering;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.dto.ChangeCatalogItemCountForm;
import ru.yandex.practicum.mymarket.dto.ChangeItemActionForm;
import ru.yandex.practicum.mymarket.service.CartService;
import ru.yandex.practicum.mymarket.service.ItemService;

@Controller
@RequiredArgsConstructor
public class ItemController {

    private final CartService cartService;
    private final ItemService itemService;

    @PostMapping("/items")
    public Mono<String> changeCountFromCatalog(@ModelAttribute ChangeCatalogItemCountForm form) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/items");

        if (form.getSearch() != null) {
            builder.queryParam("search", form.getSearch());
        }
        if (form.getSort() != null) {
            builder.queryParam("sort", form.getSort());
        }
        if (form.getPageNumber() != null) {
            builder.queryParam("pageNumber", form.getPageNumber());
        }
        if (form.getPageSize() != null) {
            builder.queryParam("pageSize", form.getPageSize());
        }

        String redirectUrl = "redirect:" + builder.build().encode().toUriString();

        return cartService.changeItemCount(form.getId(), form.getAction())
                .thenReturn(redirectUrl);
    }

    @GetMapping("/items/{id}")
    public Mono<Rendering> getItem(@PathVariable long id) {
        return itemService.getItem(id)
                .map(item -> Rendering.view("item")
                        .modelAttribute("item", item)
                        .build());
    }

    @PostMapping("/items/{id}")
    public Mono<Rendering> changeCountFromItemPage(
            @PathVariable long id,
            @ModelAttribute ChangeItemActionForm form
    ) {
        return cartService.changeItemCount(id, form.getAction())
                .then(itemService.getItem(id))
                .map(item -> Rendering.view("item")
                        .modelAttribute("item", item)
                        .build());
    }
}