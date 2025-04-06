package ru.itmo.infra.handler.usecase.createedustream;

import lombok.SneakyThrows;
import ru.itmo.application.StudentService;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.infra.handler.Handler;
import ru.itmo.infra.handler.usecase.Command;

public class CreateEduStreamUploadCommand implements Command {
    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        var chatId = message.getChatId();
        var file = Handler.getFileFromMessage(message);
//        var eduStreamId = Handler.getStreamEduId(chatId);

        var res = StudentService.createStudentsFromExcel(file, "1");
        if (res.isEmpty()) {
            return MessageToUser.builder().text("Файл был успешно загружен").build();
        }
        return MessageToUser.builder()
                .text("В загруженном файле содержатся ошибки, поправьте их и попробуйте снова или вернитесь назад.")
                .document(res.get())
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
