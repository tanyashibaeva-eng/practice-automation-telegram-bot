package ru.itmo.application;

import ru.itmo.domain.model.Student;
import ru.itmo.domain.model.TelegramUser;
import ru.itmo.domain.type.StudentStatus;
import ru.itmo.exception.InternalException;
import ru.itmo.infra.storage.StudentRepository;
import ru.itmo.infra.storage.TelegramUserRepository;
import ru.itmo.util.EduStreamChecker;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class AuthorizationService {

    public static boolean canRegisterAsAdmin(long chatId) throws InternalException {
        return true;
    }

    public static boolean canDoAdminActions(long chatId) throws InternalException {
        Optional<TelegramUser> telegramUserOpt = TelegramUserRepository.findByChatId(chatId);
        return telegramUserOpt.isPresent()
                && telegramUserOpt.get().isAdmin()
                && !telegramUserOpt.get().isBanned();
    }

    public static boolean canRegisterAsStudent(long chatId) throws InternalException {
        Optional<TelegramUser> telegramUserOpt = TelegramUserRepository.findByChatId(chatId);
        if (telegramUserOpt.isEmpty())
            return true;

        TelegramUser telegramUser = telegramUserOpt.get();
        if (telegramUser.isBanned() || telegramUser.isAdmin())
            return false;

        /* находим все записи по этому студенту */
        List<Student> studentList = StudentRepository.findAllByChatId(chatId);
        for (var student : studentList) {
            /* если нашлась хотя бы одна запись, в которой практика еще не завершена, значит это активная практика,
               следовательно студент уже зарегистрирован на текущий поток */
            if (EduStreamChecker.isActiveStream(student.getEduStream()))
                return false;
        }

        return true;
    }

    public static boolean canStudentUpdateCompanyInfo(long chatId) throws InternalException {
        return canStudentDoActionByStatus(chatId, Set.of(StudentStatus.REGISTERED, StudentStatus.COMPANY_INFO_RETURNED));
    }

    public static boolean canStudentSubmitApplication(long chatId) throws InternalException {
        return canStudentDoActionByStatus(chatId, Set.of(StudentStatus.APPLICATION_WAITING_SUBMISSION, StudentStatus.APPLICATION_RETURNED, StudentStatus.APPLICATION_WAITING_SIGNING));
    }

    public static boolean canStudentDownloadApplication(long chatId) throws InternalException {
        return canStudentDoActionByStatus(chatId, Set.of(StudentStatus.APPLICATION_WAITING_SUBMISSION, StudentStatus.APPLICATION_RETURNED));
    }

    private static boolean canStudentDoActionByStatus(long chatId, Set<StudentStatus> requiredStatusSet) throws InternalException {
        Optional<TelegramUser> telegramUserOpt = TelegramUserRepository.findByChatId(chatId);
        if (telegramUserOpt.isEmpty())
            return false;

        TelegramUser telegramUser = telegramUserOpt.get();
        if (telegramUser.isBanned() || telegramUser.isAdmin())
            return false;

        List<Student> studentList = StudentRepository.findAllByChatId(chatId);
        for (var student : studentList) {
            if (!EduStreamChecker.isActiveStream(student.getEduStream()))
                continue;
            if (requiredStatusSet.contains(student.getStatus()))
                return true;
        }
        return false;
    }
}
