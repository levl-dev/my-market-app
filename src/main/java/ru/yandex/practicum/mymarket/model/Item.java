package ru.yandex.practicum.mymarket.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.data.relational.core.mapping.Column;

@Table("items")
@Getter
@Setter
@NoArgsConstructor
public class Item {

    @Id
    private Long id;

    private String title;

    private String description;

    @Column("img_path")
    private String imgPath;

    private Long price;
}