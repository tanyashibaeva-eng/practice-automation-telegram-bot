package ru.itmo.infra.handler.usecase.admin.updategroup;

import lombok.SneakyThrows;
import ru.itmo.application.ContextHolder;
import ru.itmo.application.StudentService;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.domain.dto.UpdateResult;
import ru.itmo.exception.BadRequestException;
import ru.itmo.exception.UnknownUserException;
import ru.itmo.infra.handler.Handler;
import ru.itmo.infra.handler.usecase.admin.AdminCommand;

import java.io.File;

public class UpdateGroupStudentsFileCommand implements AdminCommand {

    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        try {
            long chatId = message.getChatId();

            String groupNumber = (String) ContextHolder.getCommandData(chatId);
            String eduStreamName = ContextHolder.getEduStreamName(chatId);

            if (groupNumber == null || eduStreamName == null) {
                throw new BadRequestException("Сессия устарела. Начните заново через меню потока.");
            }

            if (!message.hasDocument()) {
                ContextHolder.setNextCommand(chatId, this);
                return MessageToUser.builder()
                        .text("Пожалуйста, прикрепите Excel-файл")
                        .keyboardMarkup(getReturnToStartMarkup())
                        .needRewriting(true)
                        .build();
            }

            File file = Handler.getFileFromMessage(message);
            UpdateResult result = StudentService.updateGroupStudents(groupNumber, eduStreamName, file);

            ContextHolder.endCommand(chatId);

            String response;
            if (!result.getErrors().isEmpty()) {
                response = "⚠️ Ошибки:\n" + String.join("\n", result.getErrors());
            } else {
                response = "✅ Готово!\nДобавлено: " + result.getAdded() + "\nОбновлено: " + result.getUpdated();
            }

            return MessageToUser.builder()
                    .text(response)
                    .keyboardMarkup(getReturnToStartMarkup())
                    .needRewriting(false)
                    .build();

        } catch (BadRequestException | UnknownUserException e) {
            ContextHolder.endCommand(message.getChatId());
            return MessageToUser.builder()
                    .text("❌ " + e.getMessage())
                    .keyboardMarkup(getReturnToStartMarkup())
                    .needRewriting(false)
                    .build();
        }
    }

    @Override
    public boolean isNextCallNeeded() {
        return true;
    }
}