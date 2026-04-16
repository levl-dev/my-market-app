package ru.yandex.practicum.mymarket.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.mymarket.dto.CartAction;
import ru.yandex.practicum.mymarket.dto.ItemCard;
import ru.yandex.practicum.mymarket.service.CartService;
import ru.yandex.practicum.mymarket.service.ItemService;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(ItemController.class)
class ItemControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CartService cartService;

    @MockBean
    private ItemService itemService;

    @Test
    void getItemReturnsItemViewAndModel() throws Exception {
        ItemCard card = new ItemCard(1L, "Title", "Desc", "/i.png", 200L, 3);
        when(itemService.getItem(1L)).thenReturn(card);

        mockMvc.perform(get("/items/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(view().name("item"))
                .andExpect(model().attributeExists("item"))
                .andExpect(model().attribute("item", card));
    }

    @Test
    void postItemsRedirectsToItemsWithQueryParams() throws Exception {
        mockMvc.perform(post("/items")
                        .param("id", "5")
                        .param("action", "PLUS")
                        .param("search", "foo")
                        .param("sort", "NO")
                        .param("pageNumber", "2")
                        .param("pageSize", "10"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/items?search=foo&sort=NO&pageNumber=2&pageSize=10"));

        verify(cartService).changeItemCount(eq(5L), eq(CartAction.PLUS));
    }

    @Test
    void postItemByIdReturnsItemView() throws Exception {
        ItemCard after = new ItemCard(2L, "X", "", "", 1L, 1);
        when(itemService.getItem(2L)).thenReturn(after);

        mockMvc.perform(post("/items/{id}", 2L).param("action", "MINUS"))
                .andExpect(status().isOk())
                .andExpect(view().name("item"))
                .andExpect(model().attributeExists("item"))
                .andExpect(model().attribute("item", after));

        verify(cartService).changeItemCount(eq(2L), eq(CartAction.MINUS));
    }
}
