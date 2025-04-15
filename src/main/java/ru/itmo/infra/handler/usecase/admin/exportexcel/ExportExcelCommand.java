package ru.itmo.infra.handler.usecase.admin.exportexcel;

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

public class ExportExcelCommand implements AdminCommand {

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
            var file = StudentService.exportStudentsToExcel(stream.getName());
            ContextHolder.setNextCommand(chatId, new GotoStreamCommand());
            return MessageToUser.builder().text("Сгенерированная выгрузка по потоку %s".formatted(streamName)).document(file).build();
        } catch (BadRequestException e) {
            // TODO:
            return MessageToUser.builder().text(e.getMessage()).build();
        } catch (UnknownUserException e) {
            return returnToMainMenuWithError(message.getChatId(),
                    "Из-за внешних обстоятельств контекст был утерян. Пожалуйста, повторите действие еще раз");
        }
    }

    @Override
    public boolean isNextCallNeeded() {
        return true;
    }

    @Override
    public String getName() {
        return "/export";
    }

    @Override
    public String getDescription() {
        return "Получить excel выгрузку по потоку. Пример: `/export Бакалавры 2025`";
    }
}
