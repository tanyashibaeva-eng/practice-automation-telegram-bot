package ru.itmo.infra.handler.usecase.user.studentregistration;

import lombok.SneakyThrows;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import ru.itmo.application.ContextHolder;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.infra.handler.usecase.Command;

public class StudentRegistrationStartCommand implements Command {
    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        ContextHolder.setNextCommand(message.getChatId(), new StudentRegistrationISUCommand());
        return MessageToUser.builder()
                .text("Введите название вашего потока")
                .keyboardMarkup(new ReplyKeyboardRemove(true))
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
}
