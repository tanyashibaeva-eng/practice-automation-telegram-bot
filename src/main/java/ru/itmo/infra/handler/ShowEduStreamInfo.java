//package ru.itmo.infra.handler;
//
//import lombok.SneakyThrows;
//import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
//import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
//import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
//import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
//import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
//import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
//import ru.itmo.bot.CallbackData;
//import ru.itmo.bot.MessageDTO;
//import ru.itmo.bot.MessageToUser;
//
//import java.util.ArrayList;
//
//public class ShowEduStreamInfo {
//    @SneakyThrows
//    public static MessageToUser start(MessageDTO message) {
//        var chatId = message.getChatId();
//        System.out.println(Handler.getEduStreamName(chatId));
//        Handler.setNextCommandFunction(chatId, ShowEduStreamInfo::handleAsk);
//        return MessageToUser.builder().text("Тут инфа про потоки").keyboardMarkup(getInlineKeyboardForStart()).build();
//    }
//
//    public static MessageToUser handleAsk(MessageDTO message) {
//        var chatId = message.getChatId();
//        var text = message.getText();
//
//        if (text.equals("Да")) {
//            Handler.endCommand(chatId);
//            return MessageToUser.builder().text("Да так да").build();
//        }
//        if (text.equals("Нет")) {
//            Handler.endCommand(chatId);
//            return MessageToUser.builder().text("Нет так нет").build();
//        }
//
//        Handler.endCommand(chatId);
//        return MessageToUser.builder().text("Хорошо давайте загрузим файл! Кидайте его!").keyboardMarkup(getMarkupKeyboardForStart()).build();
//    }
//
//    private static ReplyKeyboard getInlineKeyboardForStart() {
//        var replyKeyboardMarkupBuilder = ReplyKeyboardMarkup.builder();
//        replyKeyboardMarkupBuilder.resizeKeyboard(true);
//        replyKeyboardMarkupBuilder.oneTimeKeyboard(true);
//
//        var keyboard = new ArrayList<KeyboardRow>();
//        var keyboardFirstRow = new KeyboardRow();
//        keyboardFirstRow.add("Да");
//        keyboardFirstRow.add("Нет");
//        keyboard.add(keyboardFirstRow);
//        replyKeyboardMarkupBuilder.keyboard(keyboard);
//
//        return replyKeyboardMarkupBuilder.build();
//    }
//
//    private static ReplyKeyboard getMarkupKeyboardForStart() {
//        return InlineKeyboardMarkup.builder()
//                .keyboardRow(
//                        new InlineKeyboardRow(
//                                InlineKeyboardButton.builder()
//                                        .text("поток 1")
//                                        .callbackData(
//                                                CallbackData.builder()
//                                                        .command("/showEduStreamInfo")
//                                                        .key("eduStreamName")
//                                                        .value("поток 1")
//                                                        .build()
//                                                        .toString()
//                                        ).build()
//                        )).build();
//    }
//}
