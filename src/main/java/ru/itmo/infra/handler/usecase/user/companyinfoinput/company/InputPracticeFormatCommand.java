package ru.itmo.infra.handler.usecase.user.companyinfoinput.company;

import lombok.SneakyThrows;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import ru.itmo.application.ContextHolder;
import ru.itmo.application.PracticeFormatService;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.domain.dto.command.CompanyInfoUpdateArgs;
import ru.itmo.domain.type.PracticeFormat;
import ru.itmo.exception.InternalException;
import ru.itmo.infra.handler.usecase.user.UserCommand;

import static ru.itmo.infra.handler.usecase.user.companyinfoinput.company.AskingPracticeFormatCommand.getPracticeFormatKeyboard;

public class InputPracticeFormatCommand implements UserCommand {

    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        var chatId = message.getChatId();
        var dto = (CompanyInfoUpdateArgs) ContextHolder.getCommandData(chatId);
        
        var text = message.getText();
        if (text == null) {
            ContextHolder.setNextCommand(chatId, this);
            return MessageToUser.builder()
                    .text("Пожалуйста, выберите формат из списка")
                    .keyboardMarkup(getPracticeFormatKeyboard())
                    .build();
        }

        if (text.equals("Вернуться в меню")) {
            ContextHolder.endCommand(chatId);
            return MessageToUser.builder()
                    .text("")
                    .keyboardMarkup(new ReplyKeyboardRemove(true))
                    .build();
        }
        
        try {
            var formatOpt = PracticeFormatService.findByDisplayNameIgnoreCase(text.trim());
            if (formatOpt.isEmpty()) {
                ContextHolder.setNextCommand(chatId, this);
                return MessageToUser.builder()
                        .text("Неизвестный формат. Пожалуйста, выберите один из вариантов на клавиатуре")
                        .keyboardMarkup(getPracticeFormatKeyboard())
                        .build();
            }

            var format = formatOpt.get();
            dto.setPracticeFormatId(format.getId());
            dto.setPracticeFormatDisplayName(format.getDisplayName());

            // попытка записать легаси enum
            try {
                dto.setPracticeFormat(PracticeFormat.valueOfIgnoreCase(format.getCode()));
            } catch (Exception ignored) {
                dto.setPracticeFormat(PracticeFormat.NOT_SPECIFIED);
            }
        } catch (InternalException e) {
            ContextHolder.setNextCommand(chatId, this);
            return MessageToUser.builder()
                    .text("Не удалось получить список форматов, попробуйте позже")
                    .keyboardMarkup(getPracticeFormatKeyboard())
                    .build();
        }
        ContextHolder.setCommandData(chatId, dto);
        ContextHolder.setNextCommand(chatId, new AskingInnCommand());
        return MessageToUser.builder()
                .text("")
                .keyboardMarkup(new ReplyKeyboardRemove(true))
                .build();
    }

    @Override
    public boolean isNextCallNeeded() {
        return true;
    }
}
