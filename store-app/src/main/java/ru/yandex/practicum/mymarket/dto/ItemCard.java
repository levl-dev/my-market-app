package ru.yandex.practicum.mymarket.dto;

public record ItemCard(
        long id,
        String title,
        String description,
        String imgPath,
        long price,
        int count
) {
}