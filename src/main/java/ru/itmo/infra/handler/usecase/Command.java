package ru.itmo.infra.handler.usecase;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import ru.itmo.bot.CallbackData;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;

public interface Command {
    MessageToUser execute(MessageDTO message);
    boolean isNextCallNeeded();
    String getName();

    default ReplyKeyboard getReturnToStartMarkup() {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(
                        new InlineKeyboardRow(
                                InlineKeyboardButton.builder()
                                        .text("Вернуться в меню")
                                        .callbackData(
                                                CallbackData.builder()
                                                        .command("/start")
                                                        .build()
                                                        .toString()
                                        ).build()
                        )).build();
    }
}

