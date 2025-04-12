package ru.itmo.infra.handler.usecase.admin.uploadexcel;

import lombok.SneakyThrows;
import ru.itmo.application.ContextHolder;
import ru.itmo.application.StudentService;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.infra.handler.Handler;
import ru.itmo.infra.handler.usecase.Command;

public class UploadExcelUploadCommand implements Command {
    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        var file = Handler.getFileFromMessage(message);

        var res = StudentService.updateStudentsFromExcel(file, "1");
        if (res.isEmpty()) {
            ContextHolder.endCommand(message.getChatId());
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
