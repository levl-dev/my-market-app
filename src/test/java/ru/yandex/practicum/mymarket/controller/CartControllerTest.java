package ru.yandex.practicum.mymarket.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.mymarket.dto.CartAction;
import ru.yandex.practicum.mymarket.dto.ItemCard;
import ru.yandex.practicum.mymarket.service.CartService;

import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(CartController.class)
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CartService cartService;

    @Test
    void getCartReturnsCartViewWithModel() throws Exception {
        List<ItemCard> lines = List.of(new ItemCard(1L, "a", "", "/x", 10L, 2));
        when(cartService.getCartItems()).thenReturn(lines);
        when(cartService.getTotal()).thenReturn(20L);

        mockMvc.perform(get("/cart/items"))
                .andExpect(status().isOk())
                .andExpect(view().name("cart"))
                .andExpect(model().attributeExists("items", "total"))
                .andExpect(model().attribute("items", lines))
                .andExpect(model().attribute("total", 20L));
    }

    @Test
    void postCartItemsReturnsCartView() throws Exception {
        List<ItemCard> lines = List.of();
        when(cartService.getCartItems()).thenReturn(lines);
        when(cartService.getTotal()).thenReturn(0L);

        mockMvc.perform(post("/cart/items")
                        .param("id", "3")
                        .param("action", "DELETE"))
                .andExpect(status().isOk())
                .andExpect(view().name("cart"))
                .andExpect(model().attributeExists("items", "total"));

        verify(cartService).changeItemCount(eq(3L), eq(CartAction.DELETE));
    }
}
