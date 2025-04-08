package ru.itmo.application;

import lombok.extern.java.Log;
import ru.itmo.domain.model.AdminToken;
import ru.itmo.domain.model.EduStream;
import ru.itmo.domain.model.Student;
import ru.itmo.domain.model.TelegramUser;
import ru.itmo.exception.BadRequestException;
import ru.itmo.exception.InternalException;
import ru.itmo.infra.storage.AdminTokenRepository;
import ru.itmo.infra.storage.DatabaseManager;
import ru.itmo.infra.storage.StudentRepository;
import ru.itmo.infra.storage.TelegramUserRepository;

import java.sql.Connection;
import java.sql.SQLException;
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

    public static void registerUser(TelegramUser telegramUser, Student student) throws InternalException {
        try {
            student.setTelegramUser(telegramUser);
            TelegramUserRepository.saveTransactional(telegramUser, transactionConnection);
            StudentRepository.updateChatIdTransactional(student, transactionConnection);

            transactionConnection.commit();
        } catch (SQLException ex) {
            log.severe("Ошибка во время выполнения транзакции регистрации студента.\nStudent: " + student + "\nException: " + ex.getMessage());
            throw new InternalException("Что-то пошло не так");
        }
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

    public static boolean banUser(TelegramUser telegramUser, EduStream eduStream) throws InternalException, BadRequestException {
        doesExistOrThrow(telegramUser.getChatId());
        telegramUser.setBanned(true);
        boolean wasUpdated = TelegramUserRepository.updateByChatId(telegramUser);
        return wasUpdated && StudentRepository.deleteByChatIdAndEduStreamName(telegramUser.getChatId(), eduStream);
    }

    public static boolean unbanUser(TelegramUser telegramUser) throws InternalException, BadRequestException {
        doesExistOrThrow(telegramUser.getChatId());
        telegramUser.setBanned(false);
        return TelegramUserRepository.updateByChatId(telegramUser);
    }

    private static void doesExistOrThrow(long chatId) throws InternalException, BadRequestException {
        if (!TelegramUserRepository.existsByChatId(chatId))
            throw new BadRequestException("Пользователь с таким chatId не найден");
    }

}
