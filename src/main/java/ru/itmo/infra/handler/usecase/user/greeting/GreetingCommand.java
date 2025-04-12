package ru.itmo.infra.handler.usecase.user.greeting;

import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import ru.itmo.application.ContextHolder;
import ru.itmo.bot.CallbackData;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.infra.handler.usecase.Command;

@NoArgsConstructor
public class GreetingCommand implements Command {
    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        ContextHolder.endCommand(message.getChatId());
        return MessageToUser.builder()
                .text("Привет, ты на стартовой странице, тут будут кнопочки для навигации!")
                .keyboardMarkup(getMarkupKeyboardForStart())
                .needRewriting(true)
                .build();
    }


    @Override
    public boolean isNextCallNeeded() {
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
                                        .text("Регистрация")
                                        .callbackData(
                                                CallbackData.builder()
                                                        .command("/register")
                                                        .build()
                                                        .toString()
                                        ).build()
                        )).build();
    }
}
