package ru.itmo.infra.handler.usecase.admin.searchstudent;

import lombok.SneakyThrows;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import ru.itmo.application.ContextHolder;
import ru.itmo.application.StudentService;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.domain.model.Student;
import ru.itmo.exception.BadRequestException;
import ru.itmo.exception.UnknownUserException;
import ru.itmo.infra.handler.usecase.admin.AdminCommand;
import ru.itmo.util.TextUtils;

import java.util.List;

public class SearchStudentByGroupCommand implements AdminCommand {

    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        try {
            var messageText = TextUtils.removeRedundantSpaces(message.getText());
            var fields = messageText.split(" ", 3);

            if (fields.length < 3) {
                throw new BadRequestException("Формат: /search_by_group <группа> <ФИО или часть ФИО>");
            }

            String group = fields[1].trim();
            String fullNamePattern = fields[2].trim();

            String eduStreamName = ContextHolder.getEduStreamName(message.getChatId());

            List<Student> students = StudentService.searchByGroupAndFullName(group, fullNamePattern, eduStreamName);
            if (students.isEmpty()) {
                throw new BadRequestException(
                        "Студенты по запросу (группа: %s, ФИО: %s) не найдены в потоке %s"
                                .formatted(group, fullNamePattern, eduStreamName));
            }

            ContextHolder.endCommand(message.getChatId());
            return MessageToUser.builder()
                    .text(SearchStudentByIsuCommand.formatStudentList(students))
                    .keyboardMarkup(new ReplyKeyboardRemove(true))
                    .build();

        } catch (BadRequestException e) {
            ContextHolder.endCommand(message.getChatId());
            return MessageToUser.builder()
                    .text(e.getMessage())
                    .keyboardMarkup(new ReplyKeyboardRemove(true))
                    .build();
        } catch (UnknownUserException e) {
            return MessageToUser.builder()
                    .text("Сначала выберите поток через /start")
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
        return "/search_by_group";
    }

    @Override
    public String getDescription() {
        return "Поиск студента по группе и ФИО. Пример: /search_by_group M3100 Иванов";
    }
}
