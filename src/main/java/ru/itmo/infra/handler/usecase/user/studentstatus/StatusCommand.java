package ru.itmo.infra.handler.usecase.user.studentstatus;

import lombok.SneakyThrows;
import ru.itmo.application.ContextHolder;
import ru.itmo.application.StudentService;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.domain.model.Student;
import ru.itmo.domain.type.StudentStatus;
import ru.itmo.exception.UnknownUserException;
import ru.itmo.infra.handler.usecase.user.UserCommand;

public class StatusCommand implements UserCommand {

    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        long chatId = message.getChatId();

        try { String eduStreamName = ContextHolder.getEduStreamName(chatId);
            var studentOpt = StudentService.findStudentByChatIdAndEduStreamName(chatId, eduStreamName);

            if (studentOpt.isEmpty()) {
                return MessageToUser.builder()
                        .text("Студент не найден в потоке %s.".formatted(eduStreamName))
                        .keyboardMarkup(getReturnToStartMarkup())
                        .needRewriting(true)
                        .build();
            }

            Student student = studentOpt.get();
            StudentStatus status = student.getStatus();

            return MessageToUser.builder()
                    .text("Ваш статус: " + status.getDisplayName() + "\n")
                    .keyboardMarkup(getReturnToStartMarkup())
                    .needRewriting(true)
                    .build();
        }
        catch (UnknownUserException e) {
            return getNotRegisteredMessage();
        }
    }

    private MessageToUser getNotRegisteredMessage() {
        return MessageToUser.builder()
                .text("Вы еще не зарегистрировались. \n" +
                        "Для получения статуса необходимо сначала зарегистрироваться")
                .keyboardMarkup(getReturnToStartMarkup())
                .needRewriting(true)
                .build();
    }

    @Override
    public boolean isNextCallNeeded() {
        return false;
    }

    @Override
    public String getName() {
        return "/status";
    }

    @Override
    public String getDescription() {
        return "Узнать статус";
    }

    @Override
    public String getDisplayName() {
        return "Мой статус";
    }
}
