package ru.itmo.infra.handler.usecase.user.companyinfoinput.company;

import lombok.SneakyThrows;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import ru.itmo.application.ContextHolder;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.infra.handler.usecase.Command;

import java.util.ArrayList;

public class AskingPracticeFormatCommand implements Command {
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        var chatId = message.getChatId();
        ContextHolder.setNextCommand(chatId, new InputPracticeFormatCommand());
        return MessageToUser.builder()
                .text("Выберите формат прохождения практики:")
                .keyboardMarkup(getPracticePlaceKeyboard())
                .build();
    }

    @Override
    public boolean isNextCallNeeded() {
        return false;
    }

    public static ReplyKeyboard getPracticePlaceKeyboard() {
        var replyKeyboardMarkupBuilder = ReplyKeyboardMarkup.builder();
        replyKeyboardMarkupBuilder.resizeKeyboard(true);
        replyKeyboardMarkupBuilder.oneTimeKeyboard(true);

        var keyboard = new ArrayList<KeyboardRow>();
        var keyboardFirstRow = new KeyboardRow();
        keyboardFirstRow.add("Очная практика");
        keyboardFirstRow.add("Гибридная практика");
        keyboard.add(keyboardFirstRow);

        var keyboardSecondRow = new KeyboardRow();
        keyboardSecondRow.add("Дистанционная практика");
        keyboardSecondRow.add("Вернуться в меню");
        keyboard.add(keyboardSecondRow);
        replyKeyboardMarkupBuilder.keyboard(keyboard);

        return replyKeyboardMarkupBuilder.build();
    }
}

