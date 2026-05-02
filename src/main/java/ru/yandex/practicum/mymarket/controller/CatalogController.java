package ru.yandex.practicum.mymarket.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.result.view.Rendering;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.mymarket.dto.SortType;
import ru.yandex.practicum.mymarket.service.CatalogService;

@Controller
@RequiredArgsConstructor
public class CatalogController {

    private final CatalogService catalogService;

    @GetMapping({"/", "/items"})
    public Mono<Rendering> getItems(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "NO") SortType sort,
            @RequestParam(defaultValue = "1") int pageNumber,
            @RequestParam(defaultValue = "5") int pageSize
    ) {
        return catalogService.getItems(search, sort, pageNumber, pageSize)
                .map(data -> Rendering.view("items")
                        .modelAttribute("items", data.items())
                        .modelAttribute("search", data.search())
                        .modelAttribute("sort", data.sort())
                        .modelAttribute("paging", data.paging())
                        .build());
    }
}