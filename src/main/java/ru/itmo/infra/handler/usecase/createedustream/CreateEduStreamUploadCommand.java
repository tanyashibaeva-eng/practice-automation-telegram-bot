package ru.itmo.infra.handler.usecase.createedustream;

import lombok.SneakyThrows;
import ru.itmo.application.ContextHolder;
import ru.itmo.application.StudentService;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.domain.model.EduStream;
import ru.itmo.infra.handler.Handler;
import ru.itmo.infra.handler.usecase.Command;

public class CreateEduStreamUploadCommand implements Command {
    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        var chatId = message.getChatId();
        var file = Handler.getFileFromMessage(message);

//        var eduStreamId = Handler.getStreamEduId(chatId);
        EduStream eduStream = new EduStream("1");

        var res = StudentService.createStudentsFromExcel(file, eduStream.getName());
        if (res.isEmpty()) {
            ContextHolder.endCommand(chatId);
            return MessageToUser.builder().text("Файл был успешно загружен").build();
        }
        return MessageToUser.builder()
                .text("В загруженном файле содержатся ошибки, поправьте их и попробуйте снова или вернитесь назад\nСписок ошибок:\n" + res)
                .build();
    }

    @Override
    public boolean isNextCallNeeded() {
        return true;
    }

    @Override
    public String getName() {
        return "";
    }
}
