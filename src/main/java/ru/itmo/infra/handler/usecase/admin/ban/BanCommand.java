package ru.itmo.infra.handler.usecase.admin.ban;

import lombok.SneakyThrows;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import ru.itmo.application.ContextHolder;
import ru.itmo.application.StudentService;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.domain.dto.command.BanArgs;
import ru.itmo.exception.BadRequestException;
import ru.itmo.infra.handler.usecase.Command;
import ru.itmo.infra.handler.usecase.admin.AdminCommand;
import ru.itmo.util.TextParser;

public class BanCommand implements AdminCommand {
    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        try {
            var messageText = message.getText().trim().replaceAll(" +", " ");
            var fields = messageText.split(" ");
            if (fields.length < 2) {
                throw new BadRequestException("Неверный формат команды, не указан chatId студента, формат: `/ban <studentChatId>`");
            }

            var studentChatIdStr = fields[1];
            long studentChatId;
            try {
                studentChatId = TextParser.parseDoubleToLong(studentChatIdStr);
            } catch (BadRequestException e) {
                throw new BadRequestException("Неверный тип аргумента <chatId>, ожидалось число");
            }

            var students = StudentService.getStudentsByChatId(studentChatId);
            if (students.isEmpty()) {
                throw new BadRequestException("Студент с chatId: %d не найден".formatted(studentChatId));
            }

            var textBuilder = new StringBuilder();
            textBuilder.append("Найденные записи о студенте:\n");
            for (var student : students) {
                textBuilder.append("- Поток: %s\n".formatted(student.getEduStream().getName()));
                textBuilder.append("\tФИО: %s\n".formatted(student.getFullName()));
                textBuilder.append("\tСтатус: %s\n".formatted(student.getStatus().getDisplayName()));
            }

            textBuilder.append("\nЗабанить студента с chatId %d? Все записи о нем будут удалены".formatted(studentChatId));
            ContextHolder.setCommandData(message.getChatId(), new BanArgs(studentChatId));
            ContextHolder.setNextCommand(message.getChatId(), new BanConfirmationCommand());

            return MessageToUser.builder()
                    .text(textBuilder.toString())
                    .keyboardMarkup(new ReplyKeyboardRemove(true))
                    .keyboardMarkup(getInlineKeyboard())
                    .build();
        } catch (BadRequestException e) {
            ContextHolder.endCommand(message.getChatId());
            return MessageToUser.builder()
                    .text(e.getMessage())
                    .keyboardMarkup(new ReplyKeyboardRemove(true))
                    .build();
        }
    }

    @Override
    public boolean isNextCallNeeded() {
        return true;
    }

    @Override
    public String getName() {
        return "/ban";
    }
}
