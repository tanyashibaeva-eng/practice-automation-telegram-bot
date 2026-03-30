package ru.itmo.infra.handler.usecase.user.studentregistration;

import lombok.SneakyThrows;
import ru.itmo.application.ContextHolder;
import ru.itmo.application.EduStreamService;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.domain.model.EduStream;
import ru.itmo.exception.BadRequestException;
import ru.itmo.infra.handler.usecase.Command;

import java.util.Optional;

public class StudentRegistrationProcessStreamCommand implements Command {

    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        String eduStreamName = message.getText().trim();

        try {
            EduStream eduStream = new EduStream(eduStreamName);

            Optional<EduStream> eduStreamOpt = EduStreamService.findEduStreamByName(eduStream);

            if (eduStreamOpt.isPresent()) {
                ContextHolder.setEduStreamName(message.getChatId(), eduStreamName);
                ContextHolder.setNextCommand(message.getChatId(), new StudentRegistrationISUCommand());
                return MessageToUser.builder()
                        .text("")
                        .build();
            } else {
                ContextHolder.setNextCommand(message.getChatId(), new StudentRegistrationProcessStreamCommand());
                return MessageToUser.builder()
                        .text("Поток с таким названием не найден. Попробуйте снова.")
                        .keyboardMarkup(getReturnToStartMarkup())
                        .needRewriting(true)
                        .build();
            }
        } catch (BadRequestException e) {
            return MessageToUser.builder()
                    .text("Ошибка: " + e.getMessage())
                    .keyboardMarkup(getReturnToStartMarkup())
                    .needRewriting(true)
                    .build();
        }
    }

    @Override
    public boolean isNextCallNeeded() {
        return true;
    }
}