package ru.itmo.infra.handler.usecase.admin.unban;

import lombok.SneakyThrows;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import ru.itmo.application.ContextHolder;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.domain.dto.command.BanArgs;
import ru.itmo.exception.BadRequestException;
import ru.itmo.infra.handler.usecase.admin.AdminCommand;
import ru.itmo.util.TextUtils;

public class UnbanCommand implements AdminCommand {
    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        try {
            var messageText = TextUtils.removeRedundantSpaces(message.getText());
            var fields = messageText.split(" ");
            if (fields.length < 2) {
                throw new BadRequestException("Неверный формат команды, не указан chatId студента, формат: `/unban <studentChatId>`");
            }

            var userChatIdStr = fields[1];
            long userChatId;
            try {
                userChatId = TextUtils.parseDoubleStrToLong(userChatIdStr);
            } catch (BadRequestException e) {
                throw new BadRequestException("Неверный тип аргумента <chatId>, ожидалось число");
            }

            String textBuilder = "\nРазбанить пользователя с chatId %d?".formatted(userChatId);
            ContextHolder.setCommandData(message.getChatId(), new BanArgs(userChatId));
            ContextHolder.setNextCommand(message.getChatId(), new UnbanConfirmationCommand());

            return MessageToUser.builder()
                    .text(textBuilder)
                    .keyboardMarkup(new ReplyKeyboardRemove(true))
                    .keyboardMarkup(getInlineKeyboard())
                    .build();
        } catch (BadRequestException e) {
            ContextHolder.endCommand(message.getChatId());
            return MessageToUser.builder()
                    .text(e.getMessage())
                    .keyboardMarkup(new ReplyKeyboardRemove(true))
                    .build();
        }
    }

    @Override
    public boolean isNextCallNeeded() {
        return true;
    }

    @Override
    public String getName() {
        return "/unban";
    }

    @Override
    public String getDescription() {
        return "Разбанить пользователя, ему снова станут доступны все команды. Пример: `/unban 27263272`";
    }
}
