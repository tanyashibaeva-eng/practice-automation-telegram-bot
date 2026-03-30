package ru.itmo.application;

import ru.itmo.domain.model.Student;
import ru.itmo.domain.model.TelegramUser;
import ru.itmo.domain.type.StudentStatus;
import ru.itmo.exception.InternalException;
import ru.itmo.infra.notification.Notification;
import ru.itmo.infra.notification.Notifier;
import ru.itmo.infra.storage.Filter;
import ru.itmo.infra.storage.StudentRepository;
import ru.itmo.infra.storage.TelegramUserRepository;

import java.util.List;
import java.util.Set;

import static ru.itmo.domain.type.StudentStatus.*;

public class NotificationService {

    private static final Set<StudentStatus> statusRequiresPingSet = Set.of(
            REGISTERED,
            COMPANY_INFO_RETURNED,
            APPLICATION_RETURNED,
            APPLICATION_WAITING_SUBMISSION,
            APPLICATION_WAITING_SIGNING
    );

    private static final Filter statusFilter = Filter.builder().stStatuses(statusRequiresPingSet.stream().toList()).build();

    public static void pingStudents() throws InternalException {
        List<Student> studentsToPing = StudentRepository.findAll(statusFilter);
        studentsToPing.forEach(NotificationService::pingStudent);
    }

    public static void pingStudent(Student student) {
        if (!statusRequiresPingSet.contains(student.getStatus()))
            return;

        Notification notification = Notification.builder()
                .chatId(student.getTelegramUser().getChatId())
                .text(getMessageForStatus(student.getStatus()))
                .build();
        Notifier.notifyAsync(notification);
    }

    public static void notifyAdmins(String message) throws InternalException {
        List<TelegramUser> adminList = TelegramUserService.getAllNotBannedAdmins();
        for (var admin : adminList) {
            Notifier.notifyAsync(
                    Notification.builder()
                            .chatId(admin.getChatId())
                            .text(message)
                            .build()
            );
        }
    }

    private static String getMessageForStatus(StudentStatus status) {
        return switch (status) {
            case REGISTERED -> """
                    !!! Уведомление от администратора !!!
                    Вам необходимо выбрать место прохождения практики, в случае прохождения практики в сторонней компании загрузить данные о ней для дальнейшей проверки
                    """;
            case COMPANY_INFO_RETURNED -> """
                    !!! Уведомление от администратора !!!
                    Преподаватель вернул данные о компании на доработку, необходимо заново их заполнить
                    """;
            case APPLICATION_RETURNED -> """
                    !!! Уведомление от администратора !!!
                    Заявка о прохождении практики в сторонней компании была возвращена преподавателем на доработку, необходимо заново ее заполнить
                    """;
            case APPLICATION_WAITING_SUBMISSION -> """
                    !!! Уведомление от администратора !!!
                    Данные о компании были утверждены преподавателем. Теперь вам необходимо заполнить и загрузить заявку о прохождении практики в сторонней компании
                    """;
            case APPLICATION_WAITING_SIGNING -> """
                    !!! Уведомление от администратора !!!
                    Заявка о прохождении практики в сторонней компании была утверждена преподавателем. Вам необходимо отнести ее на подписание
                    """;
            case APPLICATION_PHOTO_UPLOADED -> """
                    !!! Уведомление от администратора !!!
                    Фото подписанной заявки загружено. Ожидайте подтверждения от преподавателя.
                    """;
            default -> null;
        };
    }
}
