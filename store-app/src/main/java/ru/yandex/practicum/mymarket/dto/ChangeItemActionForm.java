package ru.yandex.practicum.mymarket.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ChangeItemActionForm {
    private CartAction action;
}
