package ru.itmo.infra.handler.usecase.user.uploadsignedphoto;

import lombok.SneakyThrows;
import ru.itmo.application.ContextHolder;
import ru.itmo.application.StudentService;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.domain.type.StudentStatus;
import ru.itmo.infra.handler.usecase.user.UserCommand;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Команда для просмотра ранее загруженного фото подписанной заявки.
 * Доступна после загрузки фото (статус APPLICATION_PHOTO_UPLOADED).
 */
public class ViewSignedPhotoCommand implements UserCommand {

    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        String eduStreamName;
        try {
            eduStreamName = ContextHolder.getEduStreamName(message.getChatId());
        } catch (Exception ex) {
            return MessageToUser.builder()
                    .text("Не удалось определить учебный поток.")
                    .build();
        }

        var studentOpt = StudentService.findStudentByChatIdAndEduStreamName(
                message.getChatId(), eduStreamName);

        if (studentOpt.isEmpty()) {
            return MessageToUser.builder()
                    .text("Студент не найден.")
                    .build();
        }

        ContextHolder.endCommand(message.getChatId());
        String photoPath = studentOpt.get().getSignedPhotoPath();
        if (photoPath == null || photoPath.isEmpty()) {
            return MessageToUser.builder()
                    .text("Фото подписанной заявки ещё не загружено.")
                    .build();
        }

        Path filePath = Paths.get(photoPath);
        if (!Files.exists(filePath)) {
            return MessageToUser.builder()
                    .text("Файл фотографии не найден на сервере. Попробуйте загрузить заново.")
                    .build();
        }

        InputStream fileStream = new FileInputStream(filePath.toFile());
        String fileName = filePath.getFileName().toString();

        return MessageToUser.builder()
                .text("Фото подписанной заявки:")
                .fileStream(fileStream)
                .fileName(fileName)
                .build();
    }

    @Override
    public boolean isNextCallNeeded() {
        return true;
    }

    @Override
    public String getName() {
        return "/view_signed_photo";
    }

    @Override
    public String getDescription() {
        return "Просмотреть фото подписанной заявки";
    }

    @Override
    public String getDisplayName() {
        return "\uD83D\uDDBC Просмотреть фото заявки";
    }

    @Override
    public boolean isAvailableForStatus(StudentStatus status) {
        return status == StudentStatus.APPLICATION_PHOTO_UPLOADED;
    }
}