package ru.itmo.infra.handler.usecase.admin.practiceformat;

import lombok.SneakyThrows;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import ru.itmo.application.ContextHolder;
import ru.itmo.application.PracticeFormatService;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.exception.BadRequestException;
import ru.itmo.exception.InternalException;
import ru.itmo.infra.handler.usecase.admin.AdminCommand;

public class CreatePracticeFormatCommand implements AdminCommand {
    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        try {
            ContextHolder.endCommand(message.getChatId());
            String raw = message.getText() == null ? "" : message.getText().trim();
            String name = raw.replaceFirst("^/practice_format_create\\s*", "").trim();
            if (name.isBlank()) {
                throw new BadRequestException("Неверный формат. Используйте: /practice_format_create <название>");
            }

            var created = PracticeFormatService.create(name);
            return MessageToUser.builder()
                    .text("Формат создан: %s".formatted(created.getDisplayName()))
                    .keyboardMarkup(new ReplyKeyboardRemove(true))
                    .build();
        } catch (BadRequestException e) {
            return MessageToUser.builder().text(e.getMessage()).keyboardMarkup(new ReplyKeyboardRemove(true)).build();
        } catch (InternalException e) {
            return MessageToUser.builder().text("Что-то пошло не так").keyboardMarkup(new ReplyKeyboardRemove(true)).build();
        }
    }

    @Override
    public boolean isNextCallNeeded() {
        return true;
    }

    @Override
    public String getName() {
        return "/practice_format_create";
    }

    @Override
    public String getDescription() {
        return "Создать формат практики. Пример: `/practice_format_create Очно`";
    }
}

