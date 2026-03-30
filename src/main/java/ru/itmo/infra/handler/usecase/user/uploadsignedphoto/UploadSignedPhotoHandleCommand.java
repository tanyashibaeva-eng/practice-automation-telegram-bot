package ru.itmo.infra.handler.usecase.user.uploadsignedphoto;

import lombok.SneakyThrows;
import lombok.extern.java.Log;
import org.telegram.telegrambots.meta.api.methods.GetFile;
import org.telegram.telegrambots.meta.api.objects.Document;
import org.telegram.telegrambots.meta.generics.TelegramClient;
import ru.itmo.application.ContextHolder;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.bot.PracticeAutomationBot;
import ru.itmo.exception.InternalException;
import ru.itmo.infra.handler.usecase.Command;
import ru.itmo.infra.storage.StudentRepository;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

/**
 * Обработка загрузки фото подписанной заявки.
 * Принимает фото (отправленное как фото или как документ), валидирует и сохраняет.
 */
@Log
public class UploadSignedPhotoHandleCommand implements Command {

    private static final TelegramClient telegramClient = PracticeAutomationBot.getTelegramClient();
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024;
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of("image/jpeg", "image/png", "application/pdf");
    private static final String PHOTO_STORAGE_DIR = "signed_photos";

    private static final byte[] JPEG_MAGIC = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    private static final byte[] PNG_MAGIC = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47};
    private static final byte[] PDF_MAGIC = new byte[]{0x25, 0x50, 0x44, 0x46};

    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        String eduStreamName;
        try {
            eduStreamName = ContextHolder.getEduStreamName(message.getChatId());
        } catch (Exception ex) {
            ContextHolder.endCommand(message.getChatId());
            return MessageToUser.builder()
                    .text("Не удалось определить учебный поток. Попробуйте начать сначала через /start.")
                    .build();
        }

        ContextHolder.endCommand(message.getChatId());

        String fileId = null;

        if (message.hasPhoto()) {
            fileId = message.getPhotoFileId();
        }

        if (fileId == null && message.hasDocument()) {
            Document doc = message.getDocument();
            String mimeType = doc.getMimeType();
            if (mimeType == null || !ALLOWED_MIME_TYPES.contains(mimeType.toLowerCase())) {
                return MessageToUser.builder()
                        .text("Неподдерживаемый формат файла. Пожалуйста, отправьте изображение в формате JPEG, PNG или PDF.")
                        .build();
            }
            if (doc.getFileSize() != null && doc.getFileSize() > MAX_FILE_SIZE) {
                return MessageToUser.builder()
                        .text("Файл слишком большой. Максимальный размер — 10 МБ.")
                        .build();
            }
            fileId = doc.getFileId();
        }

        if (fileId == null) {
            return MessageToUser.builder()
                    .text("Пожалуйста, отправьте фотографию подписанной заявки (как фото или как файл в формате JPEG, PNG или PDF).")
                    .build();
        }

        File downloadedFile;
        try {
            var getFile = new GetFile(fileId);
            var tgFile = telegramClient.execute(getFile);
            downloadedFile = telegramClient.downloadFile(tgFile).toPath().toFile();
        } catch (Exception ex) {
            log.severe("Ошибка скачивания файла из Telegram: " + ex.getMessage());
            return MessageToUser.builder()
                    .text("Не удалось загрузить файл из Telegram. Попробуйте ещё раз.")
                    .build();
        }

        try {
            byte[] fileBytes = Files.readAllBytes(downloadedFile.toPath());
            if (!isValidImage(fileBytes)) {
                return MessageToUser.builder()
                        .text("Файл не является допустимым изображением (JPEG, PNG или PDF). Попробуйте другой файл.")
                        .build();
            }
            if (fileBytes.length > MAX_FILE_SIZE) {
                return MessageToUser.builder()
                        .text("Файл слишком большой. Максимальный размер — 10 МБ.")
                        .build();
            }
        } catch (IOException ex) {
            log.severe("Ошибка чтения файла: " + ex.getMessage());
            return MessageToUser.builder()
                    .text("Ошибка при обработке файла. Попробуйте ещё раз.")
                    .build();
        }

        String extension = detectExtension(downloadedFile);

        String fileName = message.getChatId() + "_" + System.currentTimeMillis() + extension;
        Path storageDir = Paths.get(PHOTO_STORAGE_DIR, eduStreamName);
        try {
            Files.createDirectories(storageDir);
        } catch (IOException ex) {
            log.severe("Ошибка создания директории: " + ex.getMessage());
            return MessageToUser.builder()
                    .text("Ошибка при сохранении файла. Попробуйте ещё раз.")
                    .build();
        }

        Path targetPath = storageDir.resolve(fileName);
        try {
            Files.copy(downloadedFile.toPath(), targetPath);
        } catch (IOException ex) {
            log.severe("Ошибка копирования файла: " + ex.getMessage());
            return MessageToUser.builder()
                    .text("Ошибка при сохранении файла. Попробуйте ещё раз.")
                    .build();
        }

        try {
            StudentRepository.updateSignedPhotoPath(
                    message.getChatId(),
                    eduStreamName,
                    targetPath.toString()
            );
        } catch (InternalException ex) {
            log.severe("Ошибка обновления БД: " + ex.getMessage());
            try { Files.deleteIfExists(targetPath); } catch (IOException ignored) {}
            return MessageToUser.builder()
                    .text("Ошибка при сохранении данных. Попробуйте ещё раз.")
                    .build();
        }

        return MessageToUser.builder()
                .text("Фото подписанной заявки успешно загружено! Статус заявки обновлён.")
                .build();
    }

    @Override
    public boolean isNextCallNeeded() {
        return true;
    }

    /**
     * Проверяет magic bytes файла на соответствие JPEG, PNG или PDF.
     */
    private boolean isValidImage(byte[] fileBytes) {
        if (fileBytes.length < 4) return false;
        if (startsWith(fileBytes, JPEG_MAGIC)) return true;
        if (startsWith(fileBytes, PNG_MAGIC)) return true;
        if (startsWith(fileBytes, PDF_MAGIC)) return true;
        return false;
    }

    private boolean startsWith(byte[] data, byte[] prefix) {
        if (data.length < prefix.length) return false;
        for (int i = 0; i < prefix.length; i++) {
            if (data[i] != prefix[i]) return false;
        }
        return true;
    }

    /**
     * Определяет расширение файла по magic bytes.
     */
    private String detectExtension(File file) {
        try (InputStream is = new FileInputStream(file)) {
            byte[] header = new byte[4];
            if (is.read(header) >= 4) {
                if (startsWith(header, PNG_MAGIC)) return ".png";
                if (startsWith(header, PDF_MAGIC)) return ".pdf";
            }
        } catch (IOException ignored) {}
        return ".jpg";
    }
}