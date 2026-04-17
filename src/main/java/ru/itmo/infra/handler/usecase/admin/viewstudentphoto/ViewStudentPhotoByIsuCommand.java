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

public class ViewStudentPhotoByIsuCommand implements AdminCommand {

    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        try {
            var fields = TextUtils.removeRedundantSpaces(message.getText()).split(" ");

            if (fields.length < 2) {
                throw new BadRequestException("Формат: /view_photo_by_isu <ISU>");
            }

            int isu;
            try {
                isu = Integer.parseInt(fields[1].trim());
            } catch (NumberFormatException e) {
                throw new BadRequestException("Неверный формат ISU. Ожидается число.");
            }

            var studentOpt = StudentService.findStudentByIsu(isu);
            if (studentOpt.isEmpty()) {
                throw new BadRequestException("Студент с ISU %d не найден.".formatted(isu));
            }

            var student = studentOpt.get();
            String photoPath = student.getSignedPhotoPath();

            if (photoPath == null || photoPath.isBlank()) {
                throw new BadRequestException("У студента %s (ISU %d) фото подписанной заявки не загружено.".formatted(student.getFullName(), isu));
            }

            Path filePath = Paths.get(photoPath);
            if (!Files.exists(filePath)) {
                throw new BadRequestException("Файл фотографии не найден на сервере.");
            }

            InputStream fileStream = new FileInputStream(filePath.toFile());
            String fileName = filePath.getFileName().toString();

            ContextHolder.endCommand(message.getChatId());
            return MessageToUser.builder()
                    .text("Фото подписанной заявки студента " + student.getFullName() + ":")
                    .fileStream(fileStream)
                    .fileName(fileName)
                    .build();

        } catch (BadRequestException e) {
            ContextHolder.endCommand(message.getChatId());
            return MessageToUser.builder().text(e.getMessage()).build();
        }
    }

    @Override
    public boolean isNextCallNeeded() {
        return true;
    }

    @Override
    public String getName() {
        return "/view_photo_by_isu";
    }

    @Override
    public String getDescription() {
        return "Просмотреть фото заявки по ISU. Формат: /view_photo_by_isu <ISU>";
    }
}