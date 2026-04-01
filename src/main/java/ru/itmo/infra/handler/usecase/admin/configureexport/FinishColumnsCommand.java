package ru.itmo.infra.handler.usecase.admin.configureexport;

import lombok.SneakyThrows;
import ru.itmo.application.ContextHolder;
import ru.itmo.application.EduStreamService;
import ru.itmo.application.StudentService;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.domain.model.EduStream;
import ru.itmo.exception.BadRequestException;
import ru.itmo.exception.UnknownUserException;
import ru.itmo.infra.handler.usecase.admin.AdminCommand;
import ru.itmo.infra.handler.usecase.admin.gotostream.GotoStreamCommand;

import java.util.Set;

public class FinishColumnsCommand implements AdminCommand {

    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        try {
            var streamName = getEduStreamNameOrThrow(message);
            EduStream stream = new EduStream(streamName);
            if (EduStreamService.findEduStreamByName(stream).isEmpty()) {
                throw new BadRequestException("Поток с таким именем не найден");
            }

            var chatId = message.getChatId();
            Set<StudentColumn> selected = ContextHolder.getSelectedColumns(chatId);
            if (selected.isEmpty()) {
                return MessageToUser.builder()
                        .text("Выберите хотя бы один столбец")
                        .keyboardMarkup(ConfigureExportCommand.buildKeyboard(chatId))
                        .needRewriting(true)
                        .build();
            }

            var fileStreamDTO = StudentService.customExportStudentsToExcel(stream.getName(), selected);

            ContextHolder.clearSelectedColumns(chatId);
            ContextHolder.setNextCommand(chatId, new GotoStreamCommand());
            return MessageToUser.builder()
                    .text("Сгенерированная кастомная выгрузка по потоку %s".formatted(streamName))
                    .fileStream(fileStreamDTO.getFileStream())
                    .fileName(fileStreamDTO.getFileName())
                    .build();
        } catch (BadRequestException e) {
            return MessageToUser.builder().text(e.getMessage()).build();
        } catch (UnknownUserException e) {
            return returnToMainMenuWithError(message.getChatId(),
                    "Из-за внешних обстоятельств контекст был утерян. Пожалуйста, повторите действие еще раз");
        }
    }

    @Override
    public String getName() {
        return "/columns_done";
    }

    @Override
    public boolean isNextCallNeeded() {
        return false;
    }
}