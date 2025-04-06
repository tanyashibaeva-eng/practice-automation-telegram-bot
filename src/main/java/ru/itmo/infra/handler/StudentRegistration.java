//package ru.itmo.infra.handler;
//
//import ru.itmo.bot.MessageDTO;
//import ru.itmo.bot.MessageToUser;
//
//public class StudentRegistration {
//
//    public static MessageToUser startRegistration(MessageDTO message) {
//        Handler.setNextCommandFunction(message.getChatId(), StudentRegistration::processIsuNumber);
//        return MessageToUser.builder().text("Привет, я бот по производственной практике! Чтобы зарегистрироваться, введите ваш ИСУ номер.").build();
//    }
//
//    // Метод для обработки введенного ису номера
//    public static MessageToUser processIsuNumber(MessageDTO message) {
//        String isuNumber = message.getText().trim();
//
//        if (isValidIsuNumber(isuNumber)) {
//            var chatId= message.getChatId();
//            String fullName = getFullNameFromIsu(isuNumber);
//            Handler.setNextCommandFunction(chatId, StudentRegistration::confirmRegistration);
//            return MessageToUser.builder().text("Найден студент с ИСУ номером " + isuNumber + ". Его ФИО: " + fullName + ". Подтверждаете регистрацию? (Да/Нет)").build();
//        } else {
//            return MessageToUser.builder().text("Неверный ИСУ номер. Пожалуйста, попробуйте снова.").build();
//        }
//    }
//
//    // Метод для подтверждения регистрации
//
//    public static MessageToUser confirmRegistration(MessageDTO message)  {
//        String response = message.getText().trim().toLowerCase();
//        var chatId= message.getChatId();
//        if (response.equals("да")) {
//            Handler.endCommand(chatId);
//            return MessageToUser.builder().text("Регистрация завершена. Добро пожаловать, " + getFullNameFromIsu(message.getText()) + "!").build();
//        } else if(response.equals("нет")) {
//            Handler.setNextCommandFunction(chatId, StudentRegistration::startRegistration);
//            return MessageToUser.builder().text("Хорошо, давай попробуем заново.").build();
//        } else {
//            return MessageToUser.builder().text("Напишите Да или Нет").build();
//        }
//    }
//
//    private static String getFullNameFromIsu(String isuNumber) {
//        if (isuNumber.equals("123456")) {
//            return "Иванов Иван Иванович";
//        }
//        return "Неизвестный студент";
//    }
//
//    private static boolean isValidIsuNumber(String isuNumber) {
//        return isuNumber.length() == 6;
//    }
//}