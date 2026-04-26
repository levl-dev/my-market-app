package ru.yandex.practicum.mymarket.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ChangeCatalogItemCountForm {
    private long id;
    private CartAction action;
    private String search;
    private SortType sort;
    private Integer pageNumber;
    private Integer pageSize;
}
