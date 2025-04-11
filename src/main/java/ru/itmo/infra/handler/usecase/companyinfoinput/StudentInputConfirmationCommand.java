package ru.itmo.infra.handler.usecase.companyinfoinput;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import ru.itmo.application.ContextHolder;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.infra.handler.usecase.Command;
import ru.itmo.infra.handler.usecase.companyinfoinput.itmo.AskingITMOPracticeLeadFullNameCommand;
import ru.itmo.infra.handler.usecase.studentregistration.StudentRegistrationConfirmationCommand;

public class StudentInputConfirmationCommand implements Command {
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
                ContextHolder.setNextCommand(chatId, new AskingITMOPracticeLeadFullNameCommand());
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

    @Override
    public String getName() {
        return "/student_input_confirmation";
    }
}
