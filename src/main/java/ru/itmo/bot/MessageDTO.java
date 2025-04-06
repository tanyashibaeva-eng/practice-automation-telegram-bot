package ru.itmo.bot;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.telegram.telegrambots.meta.api.objects.Document;

@AllArgsConstructor
@Data
@Builder
public class MessageDTO {
    private Long chatId;
    private String text;
    private Document document;

    public boolean hasDocument() {
        return document != null;
    }

    public boolean hasText() {
        return text != null && !text.trim().isEmpty();
    }
}
