package ru.yandex.practicum.mymarket.client.dto;

public record PaymentResponse(boolean success, long balance, String message) {
}
