package ru.itmo.infra.handler.usecase.companyinfoinput;

import lombok.SneakyThrows;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import ru.itmo.application.ContextHolder;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.infra.handler.usecase.Command;

import java.util.ArrayList;

public class ChoosePracticePlaceCommand implements Command {
    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        var chatId = message.getChatId();
        ContextHolder.setNextCommand(chatId, new PracticeConfirmationCommand());
        return MessageToUser.builder()
                .text("Выберите формат прохождения практики:")
                .keyboardMarkup(getPracticePlaceKeyboard())
                .build();
    }

    @Override
    public boolean isNextCallNeeded() {
        return false;
    }

    @Override
    public String getName() {
        return "/choose_place";
    }

    private static ReplyKeyboard getPracticePlaceKeyboard() {
        var replyKeyboardMarkupBuilder = ReplyKeyboardMarkup.builder();
        replyKeyboardMarkupBuilder.resizeKeyboard(true);
        replyKeyboardMarkupBuilder.oneTimeKeyboard(true);

        var keyboard = new ArrayList<KeyboardRow>();
        var keyboardFirstRow = new KeyboardRow();
        keyboardFirstRow.add("Практика у Маркиной Т.А");
        keyboardFirstRow.add("Практика в ИТМО");
        keyboard.add(keyboardFirstRow);

        var keyboardSecondRow = new KeyboardRow();
        keyboardSecondRow.add("В сторонней компании");
        keyboardSecondRow.add("Вернуться в меню");
        keyboard.add(keyboardSecondRow);
        replyKeyboardMarkupBuilder.keyboard(keyboard);

        return replyKeyboardMarkupBuilder.build();
    }
}
