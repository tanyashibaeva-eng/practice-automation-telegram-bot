package ru.itmo.infra.handler.usecase.user.companyinfoinput.company;

import lombok.SneakyThrows;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import ru.itmo.application.ContextHolder;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.domain.dto.command.CompanyInfoUpdateArgs;
import ru.itmo.domain.type.PracticeFormat;
import ru.itmo.infra.handler.usecase.Command;
import ru.itmo.infra.handler.usecase.user.studentregistration.StudentRegistrationConfirmationCommand;

public class InputPracticeFormatCommand implements Command {

    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        var chatId = message.getChatId();
        var dto = (CompanyInfoUpdateArgs) ContextHolder.getCommandData(chatId);
        switch (message.getText()) {
            case "Очная практика":
                dto.setPracticeFormat(PracticeFormat.OFFLINE);
                break;
            case "Гибридная практика":
                dto.setPracticeFormat(PracticeFormat.HYBRID);
                break;
            case "Дистанционная практика":
                dto.setPracticeFormat(PracticeFormat.ONLINE);
                break;
            case "Вернуться в меню":
                ContextHolder.endCommand(chatId);
                return MessageToUser.builder()
                        .text("")
                        .keyboardMarkup(new ReplyKeyboardRemove(true))
                        .build();
            default:
                ContextHolder.setNextCommand(chatId, new StudentRegistrationConfirmationCommand());
                return MessageToUser.builder()
                        .text("Извините, я вас не понимаю, ответьте \"Очная практика\", \"Гибридная практика\", \"Дистанционная практика\", \"Вернуться в меню\"")
                        .keyboardMarkup(getInlineKeyboard())
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

    @Override
    public String getName() {
        return "/confirm_place";
    }
}
