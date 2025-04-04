package ru.itmo.domain.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TelegramUser {
    private long chatId;
    private boolean isAdmin;
    private boolean isBanned;
    private String username;
}
