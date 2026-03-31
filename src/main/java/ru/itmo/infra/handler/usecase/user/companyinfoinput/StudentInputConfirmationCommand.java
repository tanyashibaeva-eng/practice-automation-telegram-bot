package ru.itmo.infra.handler.usecase.user.companyinfoinput;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import ru.itmo.application.ContextHolder;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.infra.handler.usecase.user.UserCommand;
import ru.itmo.infra.handler.usecase.user.companyinfoinput.itmo.AskingITMOPracticeLeadFullNameCommand;

public class StudentInputConfirmationCommand implements UserCommand {
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
                ContextHolder.setNextCommand(chatId, this);
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
