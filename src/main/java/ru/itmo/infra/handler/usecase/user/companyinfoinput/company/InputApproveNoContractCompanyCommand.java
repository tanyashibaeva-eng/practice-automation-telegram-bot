package ru.itmo.infra.handler.usecase.user.companyinfoinput.company;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import ru.itmo.application.ContextHolder;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.infra.handler.usecase.Command;
import ru.itmo.infra.handler.usecase.user.companyinfoinput.ChoosePracticePlaceCommand;
import ru.itmo.infra.handler.usecase.user.companyinfoinput.itmo.AskingITMOPracticeLeadFullNameCommand;
import ru.itmo.infra.handler.usecase.user.studentregistration.StudentRegistrationConfirmationCommand;

public class InputApproveNoContractCompanyCommand implements Command {
    @Override
    public MessageToUser execute(MessageDTO message) {
        var chatId = message.getChatId();

        switch (message.getText()) {
            case "Да":
                ContextHolder.setNextCommand(chatId, new AskingITMOPracticeLeadFullNameCommand());
                return MessageToUser.builder()
                        .text("")
                        .keyboardMarkup(new ReplyKeyboardRemove(true))
                        .needRewriting(false)
                        .build();
            case "Нет":
                ContextHolder.setNextCommand(chatId, new ChoosePracticePlaceCommand());
                return MessageToUser.builder()
                        .text("")
                        .keyboardMarkup(new ReplyKeyboardRemove(true))
                        .build();
            default:
                ContextHolder.setNextCommand(chatId, new StudentRegistrationConfirmationCommand());
                return MessageToUser.builder()
                        .text("Извините, я вас не понимаю, ответьте \"Да\" или \"Нет\" ")
                        .keyboardMarkup(getInlineKeyboard())
                        .build();
        }
    }

    @Override
    public boolean isNextCallNeeded() {
        return true;
    }
}
