package ru.itmo.infra.handler.usecase.admin.viewstudentphoto;

import lombok.SneakyThrows;
import ru.itmo.application.ContextHolder;
import ru.itmo.application.StudentService;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.exception.BadRequestException;
import ru.itmo.infra.handler.usecase.admin.AdminCommand;
import ru.itmo.util.TextUtils;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Команда для просмотра фото подписанной заявки студента (для администратора).
 * Формат: /view_student_photo <chatId>
 */
public class ViewStudentPhotoCommand implements AdminCommand {

    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        try {
            var messageText = TextUtils.removeRedundantSpaces(message.getText());
            var fields = messageText.split(" ");

            if (fields.length < 2) {
                throw new BadRequestException("Неверный формат команды. Формат:\n/view_student_photo <chatId>");
            }

            long studentChatId;
            try {
                studentChatId = TextUtils.parseDoubleStrToLong(fields[1]);
            } catch (BadRequestException e) {
                throw new BadRequestException("Неверный формат <chatId>. Ожидается число.");
            }

            var students = StudentService.getStudentsByChatId(studentChatId);
            if (students.isEmpty()) {
                throw new BadRequestException("Студент с chatId %d не найден.".formatted(studentChatId));
            }

            String photoPath = null;
            String studentName = null;
            for (var student : students) {
                if (student.getSignedPhotoPath() != null && !student.getSignedPhotoPath().isBlank()) {
                    photoPath = student.getSignedPhotoPath();
                    studentName = student.getFullName();
                    break;
                }
            }

            if (photoPath == null) {
                throw new BadRequestException("У студента (chatId %d) фото подписанной заявки не загружено.".formatted(studentChatId));
            }

            Path filePath = Paths.get(photoPath);
            if (!Files.exists(filePath)) {
                throw new BadRequestException("Файл фотографии не найден на сервере.");
            }

            InputStream fileStream = new FileInputStream(filePath.toFile());
            String fileName = filePath.getFileName().toString();

            ContextHolder.endCommand(message.getChatId());
            return MessageToUser.builder()
                    .text("Фото подписанной заявки студента " + studentName + ":")
                    .fileStream(fileStream)
                    .fileName(fileName)
                    .build();

        } catch (BadRequestException e) {
            ContextHolder.endCommand(message.getChatId());
            return MessageToUser.builder()
                    .text(e.getMessage())
                    .build();
        }
    }

    @Override
    public boolean isNextCallNeeded() {
        return true;
    }

    @Override
    public String getName() {
        return "/view_student_photo";
    }

    @Override
    public String getDescription() {
        return "Просмотреть фото заявки студента. Формат: /view_student_photo <chatId>";
    }
}