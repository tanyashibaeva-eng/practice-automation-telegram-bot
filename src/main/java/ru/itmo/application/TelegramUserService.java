package ru.itmo.application;

import lombok.extern.java.Log;
import ru.itmo.domain.dto.command.UserRegistrationArgs;
import ru.itmo.domain.dto.command.UserRegistrationResult;
import ru.itmo.domain.model.AdminToken;
import ru.itmo.domain.model.EduStream;
import ru.itmo.domain.model.Student;
import ru.itmo.domain.model.TelegramUser;
import ru.itmo.exception.BadRequestException;
import ru.itmo.exception.InternalException;
import ru.itmo.infra.storage.*;
import ru.itmo.util.EduStreamChecker;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Log
public class TelegramUserService {

    private static final Connection transactionConnection = DatabaseManager.initializeConnection();

    static {
        try {
            transactionConnection.setAutoCommit(false);
        } catch (SQLException ex) {
            log.severe("Ошибка во время установки нового соединения с БД: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    /* Здесь мы считаем, что ИСУ уже корректный и такой студент существует */
    public static UserRegistrationResult registerUser(UserRegistrationArgs args) throws InternalException {
        boolean shouldCreateTelegramUser = false;
        boolean shouldDuplicateStudent = false;

        var resultBuilder = UserRegistrationResult.builder();
        EduStream eduStream;

        try {
            eduStream = new EduStream(args.getEduStreamName());
        } catch (BadRequestException ex) {
            return resultBuilder.errorText(ex.getMessage()).build();
        }

        Optional<TelegramUser> existingUser = TelegramUserRepository.findByChatId(args.getChatId());

        if (existingUser.isPresent()) {
            if (existingUser.get().isAdmin())
                return resultBuilder.errorText("Вы не можете зарегистрироваться как студент, так как вы администратор").build();
        } else shouldCreateTelegramUser = true;

        List<Student> studentList = StudentRepository.findAllByIsuAndEduStreamName(args.getIsu(), eduStream);
        if (studentList.isEmpty()) {
            log.severe("Нарушена консистентность данных студентов: ису не найден при регистрации");
            throw new InternalException("Что-то пошло не так");
        }
        Student student = studentList.get(0);

        if (student.getTelegramUser() != null) {
            if (student.getTelegramUser().getChatId() == args.getChatId())
                return resultBuilder.errorText("Вы уже зарегистрированы").build();
            shouldDuplicateStudent = true;
        }

        try {
            TelegramUser telegramUser = new TelegramUser(args.getChatId(), false, false, args.getUsername());

            if (shouldCreateTelegramUser)
                TelegramUserRepository.saveTransactional(telegramUser, transactionConnection);
            if (shouldDuplicateStudent)
                StudentRepository.saveTransactional(student, transactionConnection);
            student.setTelegramUser(telegramUser);
            StudentRepository.updateChatIdTransactional(student, transactionConnection);

            transactionConnection.commit();
        } catch (SQLException ex) {
            log.severe("Ошибка во время выполнения транзакции регистрации студента.\nStudent: " + student + "\nException: " + ex.getMessage());
            throw new InternalException("Что-то пошло не так");
        }

        if (shouldDuplicateStudent) {
            // TODO: somehow notify admins
        }
        return resultBuilder.errorText("").build();
    }

    public static void registerAdmin(TelegramUser telegramUser, AdminToken adminToken) throws InternalException, BadRequestException {
        Optional<TelegramUser> existingUser = TelegramUserRepository.findByChatId(telegramUser.getChatId());
        if (existingUser.isPresent() && existingUser.get().isAdmin())
            throw new BadRequestException("Пользователь %s уже админ".formatted(telegramUser.getUsername()));
        if (!AdminTokenRepository.delete(adminToken))
            throw new BadRequestException("Токен невалиден");
        telegramUser.setAdmin(true);
        TelegramUserRepository.save(telegramUser);
    }

    public static boolean deleteAdmin(TelegramUser telegramUser) throws InternalException, BadRequestException {
        doesExistOrThrow(telegramUser.getChatId());
        if (!telegramUser.isAdmin())
            throw new BadRequestException("Пользователь %s не является админом".formatted(telegramUser.getUsername()));
        return TelegramUserRepository.deleteByChatId(telegramUser.getChatId());
    }

    public static Optional<TelegramUser> findByChatId(long chatId) throws InternalException {
        return TelegramUserRepository.findByChatId(chatId);
    }

    public static boolean banUser(long chatId) throws InternalException, BadRequestException {
        doesExistOrThrow(chatId);
        var tgUserOpt = TelegramUserRepository.findByChatId(chatId);
        if (tgUserOpt.isEmpty()) {
            return true;
        }
        var telegramUser = tgUserOpt.get();
        telegramUser.setBanned(true);

        List<EduStream> activeEduStreams = EduStreamRepository.findAll().stream().filter(EduStreamChecker::isActiveStream).toList();

        Student student = null;
        EduStream eduStream = null;
        for (var es : activeEduStreams) {
            Optional<Student> studentOpt = StudentRepository.findByChatIdAndEduStreamName(telegramUser.getChatId(), es);
            if (studentOpt.isPresent()) {
                student = studentOpt.get();
                eduStream = es;
                break;
            }
        }

        // продублировать запись о студенте, если она единственная в базе
        if (student != null
                && StudentRepository.findAllByIsuAndEduStreamName(student.getIsu(), eduStream).size() == 1)
            StudentRepository.saveBaseBatch(List.of(student));

        boolean wasDeleted = true;
        if (eduStream != null)
            wasDeleted = StudentRepository.deleteByChatId(telegramUser.getChatId());

        return wasDeleted && TelegramUserRepository.updateByChatId(telegramUser);
    }

    public static boolean unbanUser(TelegramUser telegramUser) throws InternalException, BadRequestException {
        doesExistOrThrow(telegramUser.getChatId());
        telegramUser.setBanned(false);
        return TelegramUserRepository.updateByChatId(telegramUser);
    }

    private static void doesExistOrThrow(long chatId) throws InternalException, BadRequestException {
        if (!TelegramUserRepository.existsByChatId(chatId))
            throw new BadRequestException("Пользователь с chatId %d не найден".formatted(chatId));
    }

}
