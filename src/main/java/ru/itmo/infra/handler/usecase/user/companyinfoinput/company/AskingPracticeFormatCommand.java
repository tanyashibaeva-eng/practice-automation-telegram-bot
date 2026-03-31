package ru.itmo.infra.handler.usecase.user.companyinfoinput.company;

import lombok.SneakyThrows;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import ru.itmo.application.ContextHolder;
import ru.itmo.application.PracticeFormatService;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.infra.handler.usecase.user.UserCommand;

import java.util.ArrayList;

public class AskingPracticeFormatCommand implements UserCommand {
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        var chatId = message.getChatId();
        ContextHolder.setNextCommand(chatId, new InputPracticeFormatCommand());
        return MessageToUser.builder()
                .text("Выберите формат прохождения практики:")
                .keyboardMarkup(getPracticeFormatKeyboard())
                .build();
    }

    @Override
    public boolean isNextCallNeeded() {
        return false;
    }

    public static ReplyKeyboard getPracticeFormatKeyboard() {
        var replyKeyboardMarkupBuilder = ReplyKeyboardMarkup.builder();
        replyKeyboardMarkupBuilder.resizeKeyboard(true);
        replyKeyboardMarkupBuilder.oneTimeKeyboard(true);

        var keyboard = new ArrayList<KeyboardRow>();
        try {
            var formats = PracticeFormatService.findAllActive();
            for (int i = 0; i < formats.size(); i += 2) {
                var row = new KeyboardRow();
                row.add(formats.get(i).getDisplayName());
                if (i + 1 < formats.size()) {
                    row.add(formats.get(i + 1).getDisplayName());
                }
                keyboard.add(row);
            }
        } catch (Exception ignored) {
            var keyboardFirstRow = new KeyboardRow();
            keyboardFirstRow.add("Очно");
            keyboardFirstRow.add("Очно с применением дистанционных технологий");
            keyboard.add(keyboardFirstRow);

            var keyboardSecondRow = new KeyboardRow();
            keyboardSecondRow.add("С применением дистанционных технологий");
            keyboard.add(keyboardSecondRow);
        }

        var keyboardReturnRow = new KeyboardRow();
        keyboardReturnRow.add(returnIcon + " Вернуться в меню");
        keyboard.add(keyboardReturnRow);
        replyKeyboardMarkupBuilder.keyboard(keyboard);

        return replyKeyboardMarkupBuilder.build();
    }
}

