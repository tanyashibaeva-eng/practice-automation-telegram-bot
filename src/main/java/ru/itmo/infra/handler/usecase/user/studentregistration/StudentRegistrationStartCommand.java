package ru.itmo.infra.handler.usecase.user.studentregistration;

import lombok.SneakyThrows;
import ru.itmo.application.ContextHolder;
import ru.itmo.application.SimpleMarkdownToTelegramHtml;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.domain.type.StudentStatus;
import ru.itmo.infra.handler.usecase.user.UserCommand;

public class StudentRegistrationStartCommand implements UserCommand {
    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        ContextHolder.setNextCommand(message.getChatId(), new StudentRegistrationProcessStreamCommand());
        String text = SimpleMarkdownToTelegramHtml.convert(
                "При регистрации, требуется указать\n\n" +
                "* название потока  \n" +
                "* номер ИСУ\n\n" +
                "**Текущее название потока:**\n" +
                "Весна 2025/2026\n\n" +
                "Введите название вашего потока");
        return MessageToUser.builder()
                .text(text)
                .parseMode("HTML")
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
        return "/register";
    }

    @Override
    public String getDescription() {
        return "Зарегистрироваться";
    }

    @Override
    public String getDisplayName() {
        return "Зарегистрироваться";
    }

    @Override
    public boolean isAvailableForStatus(StudentStatus status) {
        return status == StudentStatus.NOT_REGISTERED;
    }
}
