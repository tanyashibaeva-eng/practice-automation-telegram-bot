package ru.itmo.infra.handler;

import org.telegram.telegrambots.meta.api.objects.message.Message;

public class StudentRegistration {

    public static String startRegistration(Message message) {
        Handler.setNextCommandFunction(message.getChatId(), StudentRegistration::processIsuNumber);
        return "Привет, я бот по производственной практике! Чтобы зарегистрироваться, введите ваш ИСУ номер:";
    }

    // Метод для обработки введенного ису номера
    public static String processIsuNumber(Message message) {
        String isuNumber = message.getText().trim();

        if (isValidIsuNumber(isuNumber)) {
            var chatId= message.getChatId();
            String fullName = getFullNameFromIsu(isuNumber);
            Handler.setNextCommandFunction(chatId, StudentRegistration::confirmRegistration);
            return "Найден студент с ИСУ номером " + isuNumber + ". Его ФИО: " + fullName + ". Подтверждаете регистрацию? (Да/Нет)";
        } else {
            return "Неверный ИСУ номер. Пожалуйста, попробуйте снова.";
        }
    }

    // Метод для подтверждения регистрации

    public static String confirmRegistration(Message message)  {
        String response = message.getText().trim().toLowerCase();
        var chatId= message.getChatId();
        if (response.equals("да")) {
            Handler.endCommand(chatId);
            return "Регистрация завершена. Добро пожаловать, " + getFullNameFromIsu(message.getText()) + "!";
        } else if(response.equals("нет")) {
            Handler.setNextCommandFunction(chatId, StudentRegistration::startRegistration);
            return "Хорошо, давай попробуем заново.";
        } else {
            return ("Напишите Да или Нет");
        }
    }

    private static String getFullNameFromIsu(String isuNumber) {
        if (isuNumber.equals("123456")) {
            return "Иванов Иван Иванович";
        }
        return "Неизвестный студент";
    }

    private static boolean isValidIsuNumber(String isuNumber) {
        return isuNumber.length() == 6;
    }
}
