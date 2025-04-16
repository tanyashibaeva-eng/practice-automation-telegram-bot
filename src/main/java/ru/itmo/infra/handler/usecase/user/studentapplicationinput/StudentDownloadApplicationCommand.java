package ru.itmo.infra.handler.usecase.user.studentapplicationinput;

import lombok.SneakyThrows;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import ru.itmo.application.ContextHolder;
import ru.itmo.application.StudentService;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.domain.type.StudentStatus;
import ru.itmo.infra.handler.usecase.user.UserCommand;

public class StudentDownloadApplicationCommand implements UserCommand {
    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        var chatId = message.getChatId();
        var fileResp = StudentService.generateApplicationFileByChatId(chatId);
        if (fileResp.getErrorText() != null) {
            System.out.println(fileResp.getErrorText());
        }
        ContextHolder.endCommand(message.getChatId());
        return MessageToUser.builder()
                .text("Скачайте и заполните заявку, затем загрузите ее обратно в бота")
                .keyboardMarkup(new ReplyKeyboardRemove(true))
                .document(fileResp.getFile())
                .build();
}

    @Override
    public boolean isNextCallNeeded() {
        return true;
    }

    @Override
    public String getName() {
        return "/download_application";
    }

    @Override
    public String getDescription() {
        return "Скачать шаблон заявки";
    }

    @Override
    public String getDisplayName() {
        return "Скачать шаблон заявки";
    }

    @Override
    public boolean isAvailableForStatus(StudentStatus status) {
        return  status == StudentStatus.APPLICATION_WAITING_SUBMISSION ||
                status == StudentStatus.APPLICATION_WAITING_APPROVAL ||
                status == StudentStatus.APPLICATION_RETURNED;
    }
}
