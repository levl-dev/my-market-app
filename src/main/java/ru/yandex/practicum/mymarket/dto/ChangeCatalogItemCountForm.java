package ru.yandex.practicum.mymarket.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ChangeCatalogItemCountForm {

    private long id;
    private String search;
    private String sort;
    private Integer pageNumber;
    private Integer pageSize;
    private CartAction action;
}