package ru.itmo.infra.handler.usecase.user.companyinfoinput;

import lombok.SneakyThrows;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import ru.itmo.application.ContextHolder;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.domain.type.StudentStatus;
import ru.itmo.infra.handler.usecase.user.UserCommand;

import java.util.ArrayList;

public class ChoosePracticePlaceCommand implements UserCommand {
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
    public String getDisplayName() {
        return "Выбор места практики";
    }

    @Override
    public boolean isAvailableForStatus(StudentStatus status) {
        return status == StudentStatus.REGISTERED ||
                status == StudentStatus.COMPANY_INFO_RETURNED;
    }

    @Override
    public boolean isNextCallNeeded() {
        return false;
    }

    @Override
    public String getName() {
        return "/choose_place";
    }

    @Override
    public String getDescription() {
        return "Выбрать место прохождения практики";
    }

    public static ReplyKeyboard getPracticePlaceKeyboard() {
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
        keyboardSecondRow.add(returnIcon + " Вернуться в меню");
        keyboard.add(keyboardSecondRow);
        replyKeyboardMarkupBuilder.keyboard(keyboard);

        return replyKeyboardMarkupBuilder.build();
    }
}