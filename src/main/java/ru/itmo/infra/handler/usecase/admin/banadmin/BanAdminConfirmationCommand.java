package ru.itmo.infra.handler.usecase.admin.banadmin;

import lombok.SneakyThrows;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import ru.itmo.application.ContextHolder;
import ru.itmo.application.TelegramUserService;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.domain.dto.command.BanArgs;
import ru.itmo.infra.handler.usecase.admin.AdminCommand;

public class BanAdminConfirmationCommand implements AdminCommand {
    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        var chatId = message.getChatId();
        var args = (BanArgs) ContextHolder.getCommandData(chatId);

        switch (message.getText()) {
            case "Да":
                TelegramUserService.banAdmin(args.getChatId());
                ContextHolder.endCommand(chatId);
                return MessageToUser.builder()
                        .text("Администратор с chatId %d был забанен".formatted(args.getChatId()))
                        .keyboardMarkup(new ReplyKeyboardRemove(true))
                        .needRewriting(false)
                        .build();
            case "Нет":
                ContextHolder.endCommand(chatId);
                return MessageToUser.builder()
                        .text("Возврат в главное меню")
                        .keyboardMarkup(new ReplyKeyboardRemove(true))
                        .build();
            default:
                ContextHolder.setNextCommand(chatId, this);
                return MessageToUser.builder()
                        .text("Ответьте \"Да\" или \"Нет\"")
                        .keyboardMarkup(getConfirmationKeyboard())
                        .build();
        }
    }

    @Override
    public boolean isNextCallNeeded() {
        return true;
    }
}
