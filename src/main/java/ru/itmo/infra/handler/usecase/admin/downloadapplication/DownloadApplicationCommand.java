package ru.itmo.infra.handler.usecase.admin.downloadapplication;

import lombok.SneakyThrows;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import ru.itmo.application.ContextHolder;
import ru.itmo.application.StudentService;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.exception.BadRequestException;
import ru.itmo.infra.handler.usecase.admin.AdminCommand;
import ru.itmo.util.TextUtils;

public class DownloadApplicationCommand implements AdminCommand {
    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        try {
            var messageText = TextUtils.removeRedundantSpaces(message.getText());
            var fields = messageText.split(" ");
            if (fields.length < 2) {
                throw new BadRequestException("Неверный формат команды, не указан chatId студента, формат: `/application <studentChatId>`");
            }

            var studentChatIdStr = fields[1];
            long studentChatId;
            try {
                studentChatId = TextUtils.parseDoubleStrToLong(studentChatIdStr);
            } catch (BadRequestException e) {
                throw new BadRequestException("Неверный тип аргумента <chatId>, ожидалось число");
            }

            var file = StudentService.getApplicationFile(studentChatId);
            ContextHolder.endCommand(message.getChatId());
            return MessageToUser.builder()
                    .text("Заявка студента %d:".formatted(studentChatId))
                    .document(file)
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
        return "/application";
    }

    @Override
    public String getDescription() {
        return "Получить заявку, загруженную студентом. Пример: `/application 123762`";
    }
}
