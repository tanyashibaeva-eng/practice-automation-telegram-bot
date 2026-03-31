package ru.itmo.bot;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;

import java.io.InputStream;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class MessageToUser {
    String text;
    InputStream fileStream;
    String fileName;
    ReplyKeyboard keyboardMarkup;
    boolean needRewriting;
    /**
     * Telegram parse mode, e.g. {@code "HTML"}. When null, plain text is sent.
     */
    String parseMode;
    /**
     * Короткая подсказка на inline-кнопку (до 200 символов у Telegram).
     */
    String callbackAnswerText;
    /**
     * Для callback: только ответить на query, не менять текст сообщения в чате.
     */
    @Builder.Default
    boolean skipMessageEdit = false;
}
