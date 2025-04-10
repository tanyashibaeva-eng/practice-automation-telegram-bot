package ru.itmo.infra.handler.usecase;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import ru.itmo.bot.CallbackData;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;

import java.util.ArrayList;

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

    default ReplyKeyboard getInlineKeyboard() {
        var replyKeyboardMarkupBuilder = ReplyKeyboardMarkup.builder();
        replyKeyboardMarkupBuilder.resizeKeyboard(true);
        replyKeyboardMarkupBuilder.oneTimeKeyboard(true);

        var keyboard = new ArrayList<KeyboardRow>();
        var keyboardFirstRow = new KeyboardRow();
        keyboardFirstRow.add("Да");
        keyboardFirstRow.add("Нет");
        keyboard.add(keyboardFirstRow);

        var keyboardSecondRow = new KeyboardRow();
        keyboardSecondRow.add("Вернуться в меню");
        keyboard.add(keyboardSecondRow);
        replyKeyboardMarkupBuilder.keyboard(keyboard);

        return replyKeyboardMarkupBuilder.build();
    }

    default ReplyKeyboard getConfirmationKeyboard() {
        var replyKeyboardMarkupBuilder = ReplyKeyboardMarkup.builder();
        replyKeyboardMarkupBuilder.resizeKeyboard(true);
        replyKeyboardMarkupBuilder.oneTimeKeyboard(true);
        var keyboard = new ArrayList<KeyboardRow>();

        KeyboardRow row = new KeyboardRow();
        row.add("Да");
        row.add("Нет");
        keyboard.add(row);

        replyKeyboardMarkupBuilder.keyboard(keyboard);

        return replyKeyboardMarkupBuilder.build();
    }
}

