package ru.itmo.infra.handler.usecase.admin.configureexport;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import ru.itmo.application.ContextHolder;
import ru.itmo.bot.CallbackData;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.infra.handler.usecase.admin.AdminCommand;

import java.util.Set;

public class ConfigureExportCommand implements AdminCommand {

    @Override
    public MessageToUser execute(MessageDTO message) {
        Long chatId = message.getChatId();
        ContextHolder.clearSelectedColumns(chatId);
        return MessageToUser.builder()
                .text("Выберите столбцы для выгрузки:")
                .keyboardMarkup(buildKeyboard(chatId))
                .needRewriting(true)
                .build();
    }

    @Override
    public boolean isNextCallNeeded() {
        return false;
    }

    @Override
    public String getName() {
        return "/configure_export";
    }

    @Override
    public String getDescription() {
        return "Получить кастомную excel выгрузку по потоку. Пример: `/configure_export Бакалавры 2025`";
    }

    public static ReplyKeyboard buildKeyboard(Long chatId) {
        Set<StudentColumn> selected = ContextHolder.getSelectedColumns(chatId);
        InlineKeyboardMarkup.InlineKeyboardMarkupBuilder builder = InlineKeyboardMarkup.builder();

        for (StudentColumn col : StudentColumn.values()) {
            boolean isSelected = selected.contains(col);
            builder.keyboardRow(new InlineKeyboardRow(
                    InlineKeyboardButton.builder()
                            .text((isSelected ? "✅ " : "❌ ") + col.getTitle())
                            .callbackData(
                                    CallbackData.builder()
                                            .command(new ToggleColumnCommand().getName())
                                            .key("column")
                                            .value(col.name())
                                            .build()
                                            .toString()
                            )
                            .build()
            ));

        }

        builder.keyboardRow(new InlineKeyboardRow(
                InlineKeyboardButton.builder()
                        .text("\uD83C\uDFC1 Готово")
                        .callbackData(
                                CallbackData.builder()
                                        .command(new FinishColumnsCommand().getName())
                                        .build()
                                        .toString()
                        )
                        .build()
        ));
        return builder.build();
    }
}
