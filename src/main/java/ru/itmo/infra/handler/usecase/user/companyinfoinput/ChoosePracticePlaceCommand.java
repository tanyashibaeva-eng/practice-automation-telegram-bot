package ru.itmo.infra.handler.usecase.user.companyinfoinput;

import lombok.SneakyThrows;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import ru.itmo.application.PracticeOptionService;
import ru.itmo.application.ContextHolder;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.domain.type.StudentStatus;
import ru.itmo.infra.handler.usecase.user.UserCommand;

import java.util.ArrayList;
import java.util.List;

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
        List<String> titles;
        try {
            titles = PracticeOptionService.getEnabledOptions()
                    .stream()
                    .map(o -> o.getTitle())
                    .toList();
        } catch (Exception e) {
            titles = List.of();
        }

        for (int i = 0; i < titles.size(); i += 2) {
            var row = new KeyboardRow();
            row.add(titles.get(i));
            if (i + 1 < titles.size()) {
                row.add(titles.get(i + 1));
            }
            keyboard.add(row);
        }

        var returnRow = new KeyboardRow();
        returnRow.add(returnIcon + " Вернуться в меню");
        keyboard.add(returnRow);
        replyKeyboardMarkupBuilder.keyboard(keyboard);

        return replyKeyboardMarkupBuilder.build();
    }
}