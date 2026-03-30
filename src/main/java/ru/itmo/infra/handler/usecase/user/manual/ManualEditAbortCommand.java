package ru.itmo.infra.handler.usecase.user.manual;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import lombok.NoArgsConstructor;
import ru.itmo.application.ContextHolder;
import ru.itmo.bot.CallbackData;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.domain.type.StudentStatus;
import ru.itmo.infra.handler.usecase.Command;
import ru.itmo.infra.handler.usecase.user.UserCommand;

@NoArgsConstructor
public class ManualEditAbortCommand implements UserCommand {

    public static final String COMMAND_NAME = "/manual_ed_abort";

    public static InlineKeyboardMarkup awaitInputMarkup() {
        return InlineKeyboardMarkup.builder()
                .keyboardRow(new InlineKeyboardRow(
                        InlineKeyboardButton.builder()
                                .text("❌ Отменить редактирование")
                                .callbackData(CallbackData.builder()
                                        .command(COMMAND_NAME)
                                        .key("x")
                                        .value("1")
                                        .build()
                                        .toString())
                                .build()
                ))
                .build();
    }

    @Override
    public MessageToUser execute(MessageDTO message) {
        ContextHolder.endCommand(message.getChatId());
        return MessageToUser.builder()
                .text("Редактирование отменено. Снова: /manual_edit (средняя кнопка в списке подразделов).")
                .parseMode("HTML")
                .needRewriting(true)
                .keyboardMarkup(Command.returnToStartInlineMarkup())
                .build();
    }

    @Override
    public boolean isNextCallNeeded() {
        return false;
    }

    @Override
    public String getName() {
        return COMMAND_NAME;
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public boolean isAvailableForStatus(StudentStatus status) {
        return true;
    }
}
