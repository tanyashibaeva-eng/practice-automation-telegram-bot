package ru.itmo.infra.handler.usecase.admin.deletestream;

import lombok.SneakyThrows;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import ru.itmo.application.ContextHolder;
import ru.itmo.application.EduStreamService;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.domain.model.EduStream;
import ru.itmo.exception.BadRequestException;
import ru.itmo.infra.handler.usecase.Command;
import ru.itmo.infra.handler.usecase.admin.AdminCommand;

public class DeleteStreamCommand implements AdminCommand {
    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        try {
            var streamName = getEduStreamNameOrThrow(message);
            EduStream stream = new EduStream(streamName);

            if (EduStreamService.findEduStreamByName(stream).isEmpty()) {
                throw new BadRequestException("Поток с таким именем не найден");
            }

            ContextHolder.setNextCommand(message.getChatId(), new DeleteStreamConfirmationCommand());
            return MessageToUser.builder()
                    .text("Вы уверены, что хотите удалить поток '%s'? Это действие нельзя отменить!".formatted(streamName))
                    .keyboardMarkup(getConfirmationKeyboard())
                    .build();

        } catch (BadRequestException e) {
            ContextHolder.endCommand(message.getChatId());
            return MessageToUser.builder()
                    .text("Ошибка: " + e.getMessage())
                    .keyboardMarkup(new ReplyKeyboardRemove(true))
                    .build();
        }
    }

    @Override
    public boolean isNextCallNeeded() {
        return false;
    }

    @Override
    public String getName() {
        return "/delete_stream";
    }

    @Override
    public String getDescription() {
        return "Удалить поток поток. Пример: `/delete_stream Бакалавры 2025`";
    }
}