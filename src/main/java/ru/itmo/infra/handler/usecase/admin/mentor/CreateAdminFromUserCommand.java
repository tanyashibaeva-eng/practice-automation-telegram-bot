package ru.itmo.infra.handler.usecase.admin.mentor;

import lombok.SneakyThrows;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import ru.itmo.application.TelegramUserService;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.domain.model.AdminToken;
import ru.itmo.domain.model.TelegramUser;
import ru.itmo.exception.BadRequestException;
import ru.itmo.exception.InternalException;
import ru.itmo.infra.handler.usecase.admin.AdminCommand;

import java.util.Optional;

public class CreateAdminFromUserCommand implements AdminCommand {

    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        try {
            String[] parts = message.getText().trim().split("\\s+", 2);
            if (parts.length < 2 || parts[1].isEmpty()) {
                throw new BadRequestException(
                        "Неверный формат команды. Используйте:\n" +
                                "/register_as_admin <токен>\n\n" +
                                "Пример: /register_as_admin 550e8400-e29b-41d4-a716-446655440000"
                );
            }

            String token = parts[1];
            long chatId = message.getChatId();

            Optional<TelegramUser> userOpt = TelegramUserService.findByChatId(chatId);
            TelegramUser user = userOpt.orElseGet(() ->
                    new TelegramUser(chatId, false, false, null) // username можно установить позже
            );

            if (user.isAdmin()) {
                return MessageToUser.builder()
                        .text("Вы уже являетесь администратором!")
                        .keyboardMarkup(new ReplyKeyboardRemove(true))
                        .build();
            }

            AdminToken adminToken;
            try {
                adminToken = new AdminToken(token);
            } catch (BadRequestException e) {
                throw new BadRequestException(
                        "Неверный формат токена:\n" +
                                e.getMessage() + "\n\n" +
                                "Проверьте правильность токена и попробуйте снова."
                );
            }

            try {
                TelegramUserService.registerAdmin(user, adminToken);
            } catch (BadRequestException e) {
                throw new BadRequestException(
                        "Не удалось стать администратором:\n" +
                                e.getMessage() + "\n\n" +
                                "Возможно, токен недействителен или уже был использован."
                );
            }

            return MessageToUser.builder()
                    .text("Права администратора получены!\n\n" +
                            "Теперь вам доступны специальные команды (`/start` для входа в меню)")
                    .keyboardMarkup(new ReplyKeyboardRemove(true))
                    .build();

        } catch (BadRequestException e) {
            return MessageToUser.builder()
                    .text(e.getMessage())
                    .keyboardMarkup(new ReplyKeyboardRemove(true))
                    .needRewriting(true)
                    .build();
        } catch (InternalException e) {
            return MessageToUser.builder()
                    .text("Произошла внутренняя ошибка. Пожалуйста, попробуйте позже или обратитесь к администратору.")
                    .keyboardMarkup(new ReplyKeyboardRemove(true))
                    .build();
        }
    }

    @Override
    public boolean isNextCallNeeded() {
        return false;
    }

    @Override
    public boolean isAdminCommand() {
        return false; // Доступно всем пользователям
    }

    @Override
    public String getName() {
        return "/register_as_admin";
    }

    @Override
    public String getDescription() {
        return "Активировать права администратора по токену";
    }
}