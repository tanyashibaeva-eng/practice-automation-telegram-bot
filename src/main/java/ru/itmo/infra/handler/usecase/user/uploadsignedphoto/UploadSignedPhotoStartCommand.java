package ru.itmo.infra.handler.usecase.user.uploadsignedphoto;

import lombok.SneakyThrows;
import ru.itmo.application.ContextHolder;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.domain.type.StudentStatus;
import ru.itmo.infra.handler.usecase.user.UserCommand;

/**
 * Команда для начала загрузки фото подписанной заявки.
 * Доступна студентам со статусом APPLICATION_WAITING_SIGNING.
 */
public class UploadSignedPhotoStartCommand implements UserCommand {

    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        ContextHolder.setNextCommand(message.getChatId(), new UploadSignedPhotoHandleCommand());
        return MessageToUser.builder()
                .text("Отправьте фотографию подписанной заявки.\n\nВы можете отправить фото через камеру или как файл" +
                        " (JPEG, PNG, PDF). Максимальный размер — 10 МБ.")
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
        return "/upload_signed_photo";
    }

    @Override
    public String getDescription() {
        return "Загрузить фото подписанной заявки";
    }

    @Override
    public String getDisplayName() {
        return "Загрузить фото подписанной заявки";
    }

    @Override
    public boolean isAvailableForStatus(StudentStatus status) {
        return status == StudentStatus.APPLICATION_WAITING_SIGNING;
    }
}