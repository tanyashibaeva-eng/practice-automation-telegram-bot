package ru.itmo.infra.handler.usecase.user.companyleadinfo;

import lombok.SneakyThrows;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import ru.itmo.bot.CallbackData;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.domain.type.StudentStatus;
import ru.itmo.infra.handler.usecase.Command;
import ru.itmo.infra.handler.usecase.user.UserCommand;

public class ChangeLeadInfoCommand implements UserCommand {

    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        return MessageToUser.builder()
                .text("Выберите, что хотите изменить в данных руководителя практики от компании:")
                .keyboardMarkup(buildKeyboard())
                .needRewriting(true)
                .build();
    }

    private ReplyKeyboard buildKeyboard() {
        var builder = InlineKeyboardMarkup.builder();

        for (var field : LeadInfoField.values()) {
            builder.keyboardRow(new InlineKeyboardRow(
                    InlineKeyboardButton.builder()
                            .text("Изменить " + field.getDisplayName())
                            .callbackData(CallbackData.builder()
                                    .command("/change_lead_field")
                                    .key("field")
                                    .value(field.name())
                                    .build().toString())
                            .build()
            ));
        }

        builder.keyboardRow(new InlineKeyboardRow(
                InlineKeyboardButton.builder()
                        .text("Посмотреть данные руководителя")
                        .callbackData(CallbackData.builder()
                                .command("/view_lead_info")
                                .build().toString())
                        .build()
        ));

        builder.keyboardRow(new InlineKeyboardRow(
                InlineKeyboardButton.builder()
                        .text(Command.returnIcon + " Вернуться в меню")
                        .callbackData(CallbackData.builder()
                                .command("/start")
                                .build().toString())
                        .build()
        ));

        return builder.build();
    }

    @Override
    public boolean isNextCallNeeded() {
        return false;
    }

    @Override
    public String getName() {
        return "/change_lead_info";
    }

    @Override
    public String getDescription() {
        return "Изменить данные руководителя практики от компании";
    }

    @Override
    public String getDisplayName() {
        return "Данные руководителя от компании";
    }

    @Override
    public boolean isAvailableForStatus(StudentStatus status) {
        if (status == null) return false;
        return switch (status) {
            case NOT_REGISTERED, REGISTERED, PRACTICE_IN_ITMO_MARKINA, PRACTICE_APPROVED -> false;
            default -> true;
        };
    }
}
