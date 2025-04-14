package ru.itmo.infra.handler.usecase.user.studentapplicationinput;

import lombok.SneakyThrows;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import ru.itmo.application.ContextHolder;
import ru.itmo.application.StudentService;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.infra.handler.usecase.Command;
import ru.itmo.infra.handler.usecase.start.StartCommand;

public class StudentDownloadApplicationCommand implements Command {
    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        var chatId = message.getChatId();
        var fileResp = StudentService.generateApplicationFileByChatId(chatId);
        if (fileResp.getErrorText() != null) {
            System.out.println(fileResp.getErrorText());
        }
        ContextHolder.endCommand(message.getChatId());
        ContextHolder.setNextCommand(chatId, new StartCommand());
        return MessageToUser.builder()
                .text("Скачайте и заполните заявку, затем загрузите ее обратно в бота")
                .keyboardMarkup(new ReplyKeyboardRemove(true))
                .keyboardMarkup(getReturnToStartMarkup())
                .document(fileResp.getFile())
                .build();
}

    @Override
    public boolean isNextCallNeeded() {
        return false;
    }

    @Override
    public String getName() {
        return "/download_application";
    }

    @Override
    public String getDescription() {
        return "Скачать заявку";
    }

}
