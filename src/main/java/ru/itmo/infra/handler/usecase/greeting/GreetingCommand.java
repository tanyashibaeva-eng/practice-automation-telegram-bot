package ru.itmo.infra.handler.usecase.greeting;

import lombok.NoArgsConstructor;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import ru.itmo.bot.CallbackData;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.infra.handler.usecase.Command;

@NoArgsConstructor
public class GreetingCommand implements Command {
    @Override
    public MessageToUser execute(MessageDTO message) {
        return MessageToUser.builder().text("Привет, ты на стартовой странице, тут будут кнопочки для навигации!").keyboardMarkup(getMarkupKeyboardForStart()).build();
    }

    @Override
    public boolean isTerminal() {
        return false;
    }

    @Override
    public String getName() {
        return "/start";
    }

    private static ReplyKeyboard getMarkupKeyboardForStart() {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(
                        new InlineKeyboardRow(
                                InlineKeyboardButton.builder()
                                        .text("поток 1")
                                        .callbackData(
                                                CallbackData.builder()
                                                        .command("/showEduStreamInfo")
                                                        .key("eduStreamName")
                                                        .value("поток 1")
                                                        .build()
                                                        .toString()
                                        ).build()
                        )).build();
    }
}
