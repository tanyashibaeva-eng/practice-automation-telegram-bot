package ru.itmo.infra.handler.usecase.start;

import lombok.NoArgsConstructor;
import lombok.SneakyThrows;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import ru.itmo.application.AuthorizationService;
import ru.itmo.application.ContextHolder;
import ru.itmo.application.EduStreamService;
import ru.itmo.bot.CallbackData;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.exception.InternalException;
import ru.itmo.infra.handler.Handler;
import ru.itmo.infra.handler.usecase.Command;
import ru.itmo.infra.handler.usecase.admin.initedustream.InitEduStreamCommand;

import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
public class StartCommand implements Command {
    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        ContextHolder.endCommand(message.getChatId());

        if (AuthorizationService.isBanned(message.getChatId())) {
            return bannedStartCommand(message);
        }

        if (!AuthorizationService.canDoAdminActions(message.getChatId())) {
            return adminStartCommand(message);
        }

        return MessageToUser.builder()
                .text("Привет, ты на стартовой странице, тут будут кнопочки для навигации!")
                .keyboardMarkup(getMarkupKeyboardForStart())
                .needRewriting(true)
                .build();
    }

    private MessageToUser bannedStartCommand(MessageDTO message) {
        return MessageToUser.builder()
                .text("К сожалению, вы были забанены администратором. Доступ ко всем командам запрещен. Для разбана требуется связаться с преподавателем и выяснить причину бана")
                .needRewriting(true)
                .build();
    }

    private MessageToUser adminStartCommand(MessageDTO message) throws InternalException {
        var streams = EduStreamService.findAllEduStreams();

        var streamNames = new ArrayList<String>(streams.size());
        for (var stream : streams) {
            streamNames.add(stream.getName());
        }

        return MessageToUser.builder()
                .text("Админ панель\n\nОтсюда вы можете управлять потоками студентов и отслеживать их статус!\n\nВыберите поток с которым хотите работать или введите команду (`/help` для помощи)")
                .keyboardMarkup(getAdminKeyboard(streamNames))
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

    @Override
    public String getDescription() {
        return "Главное меню";
    }

    private static ReplyKeyboard getAdminKeyboard(List<String> streamNames) {
        var markupBuilder = InlineKeyboardMarkup.builder();
        for (var streamName : streamNames) {
            var callbackData = CallbackData.builder()
                    .command("/goto_stream_menu") // TODO: replace with Command.getName
                    .key("eduStreamName")
                    .value(streamName)
                    .build();
            var keyboardRow = new InlineKeyboardRow(
                    InlineKeyboardButton.builder()
                            .text(streamName)
                            .callbackData(callbackData.toString())
                            .build()
            );
            markupBuilder.keyboardRow(keyboardRow);
        }

        var initStreamCallbackData = CallbackData.builder()
                .command(new InitEduStreamCommand().getName())
                .build();
        markupBuilder.keyboardRow(new InlineKeyboardRow(
                InlineKeyboardButton.builder()
                        .text("Добавить новый поток")
                        .callbackData(initStreamCallbackData.toString())
                        .build()
        ));

        var helpCallbackData = CallbackData.builder()
                .command("/help")
                .build();
        markupBuilder.keyboardRow(new InlineKeyboardRow(
                InlineKeyboardButton.builder()
                        .text("Справка с командами")
                        .callbackData(helpCallbackData.toString())
                        .build()
        ));

        return markupBuilder.build();
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
