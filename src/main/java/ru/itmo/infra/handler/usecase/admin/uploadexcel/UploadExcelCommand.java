package ru.itmo.infra.handler.usecase.admin.uploadexcel;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import ru.itmo.application.ContextHolder;
import ru.itmo.bot.CallbackData;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.infra.handler.usecase.admin.AdminCommand;
import ru.itmo.infra.handler.usecase.admin.gotostream.GotoStreamCommand;

public class UploadExcelCommand implements AdminCommand {
    @Override
    public MessageToUser execute(MessageDTO message) {
        var chatId = message.getChatId();
        ContextHolder.setNextCommand(chatId, new UploadExcelHandleCommand());
        return MessageToUser.builder().text("Хорошо, давайте загрузим файл! Кидайте его!").keyboardMarkup(getReturnToStartMarkup()).build(); // TODO
    }

    @Override
    public ReplyKeyboard getReturnToStartMarkup() {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(
                        new InlineKeyboardRow(
                                InlineKeyboardButton.builder()
                                        .text(returnIcon + " Вернуться в меню")
                                        .callbackData(
                                                CallbackData.builder()
                                                        .command(new GotoStreamCommand().getName())
                                                        .build()
                                                        .toString()
                                        ).build()
                        )).build();
    }

    @Override
    public boolean isNextCallNeeded() {
        return false;
    }

    @Override
    public String getName() {
        return "/upload";
    }

    @Override
    public String getDescription() {
        return "Загрузить excel файл для обновления студентов в потоке. Пример: `/upload Бакалавры 2025`";
    }
}
