package ru.itmo.infra.handler.usecase.admin.initedustream;

import lombok.SneakyThrows;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import ru.itmo.application.ContextHolder;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.infra.handler.usecase.admin.AdminCommand;

public class InitEduStreamCommand implements AdminCommand {

    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        // Пользователь админ, переходим к загрузке файлов
        ContextHolder.setNextCommand(message.getChatId(), new InitEduInputStreamNameCommand());
        return MessageToUser.builder()
                .text("Введите название для нового потока:")
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
        return "/init_edu_stream";
    }

    @Override
    public String getDescription() {
        return "Начать создание нового потока";
    }
}

