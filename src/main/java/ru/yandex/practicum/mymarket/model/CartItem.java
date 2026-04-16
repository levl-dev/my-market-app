package ru.yandex.practicum.mymarket.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.relational.core.mapping.Column;

@Table("cart_items")
@Getter
@Setter
@NoArgsConstructor
public class CartItem {

    @Id
    private Long id;

    @Column("item_id")
    private Long itemId;

    private int count;
}