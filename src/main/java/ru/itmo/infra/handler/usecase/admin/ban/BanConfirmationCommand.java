package ru.itmo.infra.handler.usecase.admin.ban;

import lombok.SneakyThrows;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import ru.itmo.application.ContextHolder;
import ru.itmo.application.TelegramUserService;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.domain.dto.command.BanArgs;
import ru.itmo.infra.handler.usecase.Command;

public class BanConfirmationCommand implements Command {
    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        var chatId = message.getChatId();
        var args = (BanArgs) ContextHolder.getCommandData(chatId);

        switch (message.getText()) {
            case "Да":
                TelegramUserService.banUser(args.getChatId());
                ContextHolder.endCommand(chatId);
                return MessageToUser.builder()
                        .text("Пользователь с chatId %d был забанен, все записи о нем были удалены".formatted(args.getChatId()))
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

    @Override
    public String getName() {
        return "";
    }

    @Override
    public boolean isAdminCommand() {
        return true;
    }
}
