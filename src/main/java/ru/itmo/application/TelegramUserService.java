package ru.itmo.application;

import lombok.extern.java.Log;
import ru.itmo.domain.model.Student;
import ru.itmo.domain.model.TelegramUser;
import ru.itmo.exception.InternalException;
import ru.itmo.infra.storage.DatabaseManager;
import ru.itmo.infra.storage.StudentRepository;
import ru.itmo.infra.storage.TelegramUserRepository;

import java.sql.Connection;
import java.sql.SQLException;

@Log
public class TelegramUserService {

    private static final Connection transactionalConnection = DatabaseManager.initializeConnection();

    static {
        try {
            transactionalConnection.setAutoCommit(false);
        } catch (SQLException ex) {
            log.severe("Ошибка во время установки нового соединения с БД: " + ex.getMessage());
            throw new RuntimeException(ex);
        }
    }

    public static void registerUser(TelegramUser telegramUser, Student student) throws InternalException {
        try {
            student.setTelegramUser(telegramUser);
            TelegramUserRepository.saveTransactional(telegramUser, transactionalConnection);
            StudentRepository.updateChatIdTransactional(student, transactionalConnection);

            transactionalConnection.commit();
        } catch (SQLException ex) {
            log.severe("Ошибка во время выполнения транзакции регистрации студента.\nStudent: " + student + "\nException: " + ex.getMessage());
            throw new InternalException("Что-то пошло не так");
        }
    }

}
