package ru.yandex.practicum.mymarket.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.mymarket.dto.ItemCard;
import ru.yandex.practicum.mymarket.dto.Paging;
import ru.yandex.practicum.mymarket.dto.SortType;
import ru.yandex.practicum.mymarket.service.CatalogService;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(CatalogController.class)
class CatalogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CatalogService catalogService;

    @Test
    void getRootReturnsItemsViewWithModel() throws Exception {
        stubCatalogPage();

        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("items"))
                .andExpect(model().attributeExists("items", "search", "sort", "paging"));
    }

    @Test
    void getItemsReturns200AndExpectedModel() throws Exception {
        stubCatalogPage();

        mockMvc.perform(get("/items"))
                .andExpect(status().isOk())
                .andExpect(view().name("items"))
                .andExpect(model().attributeExists("items", "search", "sort", "paging"))
                .andExpect(model().attribute("search", ""))
                .andExpect(model().attribute("sort", SortType.NO));
    }

    private void stubCatalogPage() {
        ItemCard card = new ItemCard(1L, "t", "", "/img.png", 100L, 0);
        ItemCard placeholder = new ItemCard(-1L, "", "", "", 0L, 0);
        CatalogService.CatalogPageResult result = new CatalogService.CatalogPageResult(
                List.of(List.of(card, placeholder, placeholder)),
                new Paging(5, 1, false, false)
        );
        when(catalogService.getItems(anyString(), eq(SortType.NO), eq(1), eq(5))).thenReturn(result);
    }
}
