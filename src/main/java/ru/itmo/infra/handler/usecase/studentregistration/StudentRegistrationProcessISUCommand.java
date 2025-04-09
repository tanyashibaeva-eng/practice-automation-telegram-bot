package ru.itmo.infra.handler.usecase.studentregistration;

import lombok.SneakyThrows;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import ru.itmo.application.ContextHolder;
import ru.itmo.application.StudentService;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.bot.PracticeAutomationBot;
import ru.itmo.domain.dto.command.StudentRegistrationArgs;
import ru.itmo.infra.handler.usecase.Command;
import ru.itmo.util.TextParser;

import java.util.ArrayList;

public class StudentRegistrationProcessISUCommand implements Command {
    public static final TextParser textParser = new TextParser();

    protected static ReplyKeyboard getInlineKeyboard() {
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

    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        var isuNumber = message.getText().trim();
        var isuResp = StudentService.validateIsu(isuNumber, "1");
        if (isuResp.getErrorText() != null) {
            PracticeAutomationBot.sendToUser(MessageToUser.builder().text(isuResp.getErrorText()).build(), message.getChatId(), false);
            return new StudentRegistrationStartCommand().execute(message);
        }

        if (isuResp.isAlreadyRegistered()) {
            // TODO: спросить уверены ли что хотите зарегаться
        }

        var student = isuResp.getStudent();
        var chatId = message.getChatId();
        var dto = StudentRegistrationArgs.builder().isu(isuResp.getIsu()).build(); // TODO: подумать возможно класть просто студента
        ContextHolder.setCommandData(chatId, dto);
        ContextHolder.setNextCommand(chatId, new StudentRegistrationConfirmationCommand());
        return MessageToUser.builder()
                .text("Найден студент с ИСУ номером %d. Его ФИО: %s. Это вы?".formatted(student.getIsu(), student.getFullName()))
                .keyboardMarkup(getInlineKeyboard()).build();
    }

    @Override
    public boolean isNextCallNeeded() {
        return false;
    }

    @Override
    public String getName() {
        return "";
    }
}
