//package ru.itmo.infra.handler.usecase.menu;
//
//import lombok.SneakyThrows;
//import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
//import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
//import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
//import ru.itmo.bot.CallbackData;
//import ru.itmo.bot.MessageDTO;
//import ru.itmo.bot.MessageToUser;
//import ru.itmo.infra.handler.usecase.Command;
//
//
//public class StudentMenuCommand implements Command {
//
//    @Override
//    @SneakyThrows
//    public MessageToUser execute(MessageDTO message) {
//        long chatId = message.getChatId();
//
//        Command.sendOrEditMessage(
//                chatId,
//                "Главное меню:",
//                createMainMenuKeyboard()
//        );
//
//        return null;
//    }
//
//    private InlineKeyboardMarkup createMainMenuKeyboard() {
//        return InlineKeyboardMarkup.builder()
//                .keyboardRow(
//                        new InlineKeyboardRow(
//                                InlineKeyboardButton.builder()
//                                        .text("Данные о компании")
//                                        .callbackData(
//                                                CallbackData.builder()
//                                                        .command("/company_info")
//                                                        .build()
//                                                        .toString()
//                                        ).build()
//                        ))
//                .keyboardRow(
//                        new InlineKeyboardRow(
//                                InlineKeyboardButton.builder()
//                                        .text("Заполнить заявку")
//                                        .callbackData(
//                                                CallbackData.builder()
//                                                        .command("/create_application")
//                                                        .build()
//                                                        .toString()
//                                        ).build()
//                        ))
//                .keyboardRow(
//                        new InlineKeyboardRow(
//                                InlineKeyboardButton.builder()
//                                        .text("Мой статус")
//                                        .callbackData(
//                                                CallbackData.builder()
//                                                        .command("/my_status")
//                                                        .build()
//                                                        .toString()
//                                        ).build()
//                        ))
//                .build();
//    }
//
//    @Override
//    public boolean isNextCallNeeded() {
//        return false;
//    }
//
//    @Override
//    public String getName() {
//        return "/main_menu";
//    }
//}