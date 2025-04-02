package ru.itmo.domain.model;

import lombok.Data;

@Data
public class TelegramUser {
    private long chatId;
    private boolean isAdmin;
    private boolean isBanned;
    private String username;
}
