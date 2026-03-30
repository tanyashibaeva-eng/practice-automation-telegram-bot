package ru.itmo.infra.handler.usecase.user.studentapplicationinput;

import lombok.SneakyThrows;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import ru.itmo.application.ContextHolder;
import ru.itmo.application.StudentService;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.domain.type.StudentStatus;
import ru.itmo.exception.BadRequestException;
import ru.itmo.infra.handler.usecase.user.UserCommand;

public class StudentFilledApplicationCommand implements UserCommand {
    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        try {
            var chatId = message.getChatId();
            var fileStreamDTO = StudentService.getApplication(chatId);

        ContextHolder.endCommand(message.getChatId());
        return MessageToUser.builder()
                .text("")
                .keyboardMarkup(new ReplyKeyboardRemove(true))
                .fileStream(fileStreamDTO.getFileStream())
                .fileName(fileStreamDTO.getFileName())
                .build();
    } catch (BadRequestException e) {
            ContextHolder.endCommand(message.getChatId());
            return MessageToUser.builder()
                    .text(e.getMessage())
                    .keyboardMarkup(new ReplyKeyboardRemove(true))
                    .needRewriting(true)
                    .build();
        }
    }

    @Override
    public boolean isNextCallNeeded() {
        return true;
    }

    @Override
    public String getName() {
        return "/download_sent_application";
    }

    @Override
    public String getDescription() {
        return "Скачать отправленную заявку";
    }

    @Override
    public String getDisplayName() {
        return "Скачать отправленную заявку";
    }

    @Override
    public boolean isAvailableForStatus(StudentStatus status) {
        return  status == StudentStatus.APPLICATION_WAITING_APPROVAL ||
                status == StudentStatus.APPLICATION_WAITING_SIGNING ;
    }
}
