package ru.itmo.infra.handler.usecase.user.practiceformat;

import lombok.SneakyThrows;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import ru.itmo.application.ContextHolder;
import ru.itmo.application.PracticeFormatService;
import ru.itmo.application.StudentService;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.domain.type.PracticeFormat;
import ru.itmo.exception.BadRequestException;
import ru.itmo.exception.InternalException;
import ru.itmo.infra.handler.usecase.Command;
import ru.itmo.infra.handler.usecase.user.companyinfoinput.company.AskingPracticeFormatCommand;

public class ChangePracticeFormatHandleCommand implements Command {

    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        var chatId = message.getChatId();
        var text = message.getText() == null ? "" : message.getText().trim();

        if (text.equals("Вернуться в меню")) {
            ContextHolder.endCommand(chatId);
            return MessageToUser.builder()
                    .text("")
                    .keyboardMarkup(new ReplyKeyboardRemove(true))
                    .build();
        }

        try {
            var formatOpt = PracticeFormatService.findByDisplayNameIgnoreCase(text);
            if (formatOpt.isEmpty()) {
                ContextHolder.setNextCommand(chatId, this);
                return MessageToUser.builder()
                        .text("Неизвестный формат. Пожалуйста, выберите один из вариантов на клавиатуре")
                        .keyboardMarkup(AskingPracticeFormatCommand.getPracticeFormatKeyboard())
                        .needRewriting(true)
                        .build();
            }

            var format = formatOpt.get();
            Long practiceFormatId = format.getId();
            PracticeFormat legacy;
            try {
                legacy = PracticeFormat.valueOfIgnoreCase(format.getCode());
            } catch (Exception ignored) {
                legacy = PracticeFormat.NOT_SPECIFIED;
            }

            StudentService.changePracticeFormatForCurrentStream(chatId, legacy, practiceFormatId);

            ContextHolder.endCommand(chatId);
            return MessageToUser.builder()
                    .text("Формат практики обновлен. Если вы уже загружали заявку, её нужно будет загрузить заново.")
                    .keyboardMarkup(new ReplyKeyboardRemove(true))
                    .build();
        } catch (BadRequestException e) {
            ContextHolder.endCommand(chatId);
            return MessageToUser.builder()
                    .text(e.getMessage())
                    .keyboardMarkup(new ReplyKeyboardRemove(true))
                    .needRewriting(true)
                    .build();
        } catch (InternalException e) {
            ContextHolder.endCommand(chatId);
            return MessageToUser.builder()
                    .text("Не удалось обновить формат практики из-за технической ошибки")
                    .keyboardMarkup(new ReplyKeyboardRemove(true))
                    .needRewriting(true)
                    .build();
        }
    }

    @Override
    public boolean isNextCallNeeded() {
        return true;
    }
}

