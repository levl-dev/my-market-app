package ru.yandex.practicum.mymarket.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ChangeItemActionForm {

    private CartAction action;
}