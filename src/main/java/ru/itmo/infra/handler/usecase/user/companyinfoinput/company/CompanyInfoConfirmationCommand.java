package ru.itmo.infra.handler.usecase.user.companyinfoinput.company;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import ru.itmo.application.ContextHolder;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.infra.handler.usecase.Command;
import ru.itmo.infra.handler.usecase.user.companyinfoinput.InfoSubmittedCommand;
import ru.itmo.infra.handler.usecase.user.studentregistration.StudentRegistrationConfirmationCommand;

public class CompanyInfoConfirmationCommand implements Command {
    @Override
    public MessageToUser execute(MessageDTO message) {
        var chatId = message.getChatId();

        switch (message.getText()) {
            case "Да":
                ContextHolder.setNextCommand(chatId, new InfoSubmittedCommand());
                return MessageToUser.builder()
                        .text("")
                        .keyboardMarkup(new ReplyKeyboardRemove(true))
                        .needRewriting(false)
                        .build();
            case "Нет":
                // TODO подумать куда лучше отсюда переходить
                ContextHolder.setNextCommand(chatId, new AskingCompanyNameCommand());
                return MessageToUser.builder()
                        .text("")
                        .keyboardMarkup(new ReplyKeyboardRemove(true))
                        .build();
            case "Вернуться в меню":
                ContextHolder.endCommand(chatId);
                return MessageToUser.builder()
                        .text("")
                        .keyboardMarkup(new ReplyKeyboardRemove(true))
                        .build();
            default:
                ContextHolder.setNextCommand(chatId, new StudentRegistrationConfirmationCommand());
                return MessageToUser.builder()
                        .text("Извините, я вас не понимаю, ответьте \"Да\", \"Нет\" или \"Вернуться в меню\" ")
                        .keyboardMarkup(getInlineKeyboard())
                        .build();
        }
    }

    @Override
    public boolean isNextCallNeeded() {
        return true;
    }
}
