package ru.itmo.bot;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;

import java.io.File;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class MessageToUser {
    String text;
    File document;
    ReplyKeyboard keyboardMarkup;
    boolean needRewriting;
}
