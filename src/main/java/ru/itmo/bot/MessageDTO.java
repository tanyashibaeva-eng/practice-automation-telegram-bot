package ru.itmo.bot;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.api.objects.MessageEntity;

import java.util.List;

@AllArgsConstructor
@Data
@Builder
public class MessageDTO {
    private Long chatId;
    private String username;
    private String text;
    private Document document;
    /**
     * Разметка входящего текста (Bot API: UTF-16 offsets). Для callback не заполняется.
     */
    private List<MessageEntity> entities;

    public boolean hasDocument() {
        return document != null;
    }

    public boolean hasText() {
        return text != null && !text.trim().isEmpty();
    }
}
