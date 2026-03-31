package ru.itmo.infra.handler.usecase.admin.banadmin;

import lombok.SneakyThrows;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import ru.itmo.application.ContextHolder;
import ru.itmo.application.TelegramUserService;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.domain.dto.command.BanArgs;
import ru.itmo.domain.model.TelegramUser;
import ru.itmo.exception.BadRequestException;
import ru.itmo.infra.handler.usecase.admin.AdminCommand;
import ru.itmo.util.TextUtils;

import java.util.Optional;

public class BanAdminCommand implements AdminCommand {
    private static final String UNTOUCHABLE_USERNAME = "TatianaMark";

    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        try {
            var messageText = TextUtils.removeRedundantSpaces(message.getText());
            var fields = messageText.split(" +");
            if (fields.length < 2) {
                throw new BadRequestException("Неверный формат команды, не указан chatId админа, формат: `/ban <adminChatId>`");
            }

            var adminChatIdStr = fields[1];
            long adminChatId;
            try {
                adminChatId = TextUtils.parseDoubleStrToLong(adminChatIdStr);
            } catch (BadRequestException e) {
                throw new BadRequestException("Неверный тип аргумента <chatId>, ожидалось число");
            }

            Optional<TelegramUser> adminToBanOpt = TelegramUserService.findByChatId(adminChatId);
            if (adminToBanOpt.isEmpty() || !adminToBanOpt.get().isAdmin()) {
                throw new BadRequestException("Админ с chatId %d не найден".formatted(adminChatId));
            }

            TelegramUser adminToBan = adminToBanOpt.get();

            if (message.getChatId() == adminChatId) {
                throw new BadRequestException("Не надо банить себя");
            }

            if (adminToBan.getUsername().equals(UNTOUCHABLE_USERNAME)) {
                throw new BadRequestException("Побойтесь бога...");
            }

            if (adminToBan.isBanned()) {
                throw new BadRequestException("Админ с chatId %d уже забанен".formatted(adminChatId));
            }

            String text = """
                    Найденный администратор:
                    
                    Имя пользователя: @%s
                    chatId: %s
                    
                    Забанить администратора?
                    """.formatted(adminToBan.getUsername(), adminToBan.getChatId());

            ContextHolder.setCommandData(message.getChatId(), new BanArgs(adminChatId));
            ContextHolder.setNextCommand(message.getChatId(), new BanAdminConfirmationCommand());

            return MessageToUser.builder()
                    .text(text)
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
        return "/ban_admin";
    }

    @Override
    public String getDescription() {
        return "Забанить администратора, он не сможет выполнять команды. Пример: `/ban_admin 27263272`";
    }
}
