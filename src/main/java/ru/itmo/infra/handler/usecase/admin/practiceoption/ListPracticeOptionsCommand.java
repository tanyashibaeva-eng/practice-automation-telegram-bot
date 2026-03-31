package ru.itmo.infra.handler.usecase.admin.practiceoption;

import lombok.SneakyThrows;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import ru.itmo.application.ContextHolder;
import ru.itmo.application.PracticeOptionService;
import ru.itmo.bot.CallbackData;
import ru.itmo.bot.MessageDTO;
import ru.itmo.bot.MessageToUser;
import ru.itmo.exception.BadRequestException;
import ru.itmo.infra.handler.usecase.admin.AdminCommand;
import ru.itmo.util.TextUtils;

import java.util.ArrayList;

public class ListPracticeOptionsCommand implements AdminCommand {
    private static final String COMMAND_NAME = "/practice_option_list";

    @Override
    @SneakyThrows
    public MessageToUser execute(MessageDTO message) {
        var actionResponse = handleActionIfProvided(message);
        if (actionResponse != null) {
            return actionResponse;
        }
        var options = PracticeOptionService.getAllOptions();
        var text = """
                Редактирование мест практики

                Флаги:
                - ITMO ✅: после выбора запрашиваются данные по практике в ИТМО.
                - Company ✅: после выбора запрашиваются данные компании.
                - Оба ❌: после выбора доп. данные не запрашиваются.

                Для каждого варианта используйте кнопки ниже.
                """;
        if (options.isEmpty()) text += "\nСписок пока пуст.";
        return MessageToUser.builder()
                .text(text.trim())
                .keyboardMarkup(buildKeyboard())
                .needRewriting(true)
                .build();
    }

    private MessageToUser handleActionIfProvided(MessageDTO message) throws Exception {
        var text = message.getText();
        if (text == null || !text.startsWith(COMMAND_NAME + "_")) {
            return null;
        }
        var payload = text.substring((COMMAND_NAME + "_").length());
        var parts = payload.split("_");
        if (parts.length == 1 && "add".equals(parts[0])) {
            ContextHolder.setNextCommand(message.getChatId(), new PracticeOptionAddInputCommand());
            return MessageToUser.builder()
                    .text("Введите название нового места практики")
                    .keyboardMarkup(getReturnToStartMarkup())
                    .build();
        }
        if (parts.length < 2) {
            throw new BadRequestException("Некорректная кнопка действия");
        }
        var action = parts[0];
        long id = TextUtils.parseDoubleStrToLong(parts[1]);
        var option = PracticeOptionService.getAllOptions().stream()
                .filter(o -> o.getId() == id)
                .findFirst()
                .orElseThrow(() -> new BadRequestException("Вариант с id=%d не найден".formatted(id)));

        switch (action) {
            case "enabled" -> {
                if (option.isEnabled()) PracticeOptionService.disableOption(id);
                else PracticeOptionService.enableOption(id);
            }
            case "rename" -> {
                ContextHolder.setCommandData(message.getChatId(), id);
                ContextHolder.setNextCommand(message.getChatId(), new PracticeOptionRenameInputCommand());
                return MessageToUser.builder()
                        .text("Переименование варианта #%d\nТекущее название: %s\n\nВведите новое название:"
                                .formatted(id, option.getTitle()))
                        .keyboardMarkup(getReturnToStartMarkup())
                        .build();
            }
            case "itmo" -> {
                boolean newItmo = !option.isRequiresItmoInfo();
                boolean newCompany = newItmo ? false : option.isRequiresCompanyInfo();
                PracticeOptionService.updateOptionFlags(id, newItmo, newCompany);
            }
            case "company" -> {
                boolean newCompany = !option.isRequiresCompanyInfo();
                boolean newItmo = newCompany ? false : option.isRequiresItmoInfo();
                PracticeOptionService.updateOptionFlags(id, newItmo, newCompany);
            }
            case "delete" -> PracticeOptionService.deleteOption(id);
            default -> throw new BadRequestException("Неизвестное действие");
        }
        return null;
    }

    private InlineKeyboardMarkup buildKeyboard() throws Exception {
        var rows = new ArrayList<InlineKeyboardRow>();
        var options = PracticeOptionService.getAllOptions();
        for (var option : options) {
            rows.add(new InlineKeyboardRow(
                    InlineKeyboardButton.builder()
                            .text("✏️ #" + option.getId() + " " + trim(option.getTitle()))
                            .callbackData(CallbackData.builder()
                                    .command(COMMAND_NAME + "_rename_" + option.getId())
                                    .build().toString())
                            .build()
            ));
            rows.add(new InlineKeyboardRow(
                    InlineKeyboardButton.builder()
                            .text("Вкл " + (option.isEnabled() ? "✅" : "❌"))
                            .callbackData(CallbackData.builder()
                                    .command(COMMAND_NAME + "_enabled_" + option.getId())
                                    .build().toString())
                            .build(),
                    InlineKeyboardButton.builder()
                            .text("ITMO " + (option.isRequiresItmoInfo() ? "✅" : "❌"))
                            .callbackData(CallbackData.builder()
                                    .command(COMMAND_NAME + "_itmo_" + option.getId())
                                    .build().toString())
                            .build(),
                    InlineKeyboardButton.builder()
                            .text("Company " + (option.isRequiresCompanyInfo() ? "✅" : "❌"))
                            .callbackData(CallbackData.builder()
                                    .command(COMMAND_NAME + "_company_" + option.getId())
                                    .build().toString())
                            .build(),
                    InlineKeyboardButton.builder()
                            .text("🗑")
                            .callbackData(CallbackData.builder()
                                    .command(COMMAND_NAME + "_delete_" + option.getId())
                                    .build().toString())
                            .build()
            ));
        }
        rows.add(new InlineKeyboardRow(
                InlineKeyboardButton.builder()
                        .text("➕ Добавить новое место")
                        .callbackData(CallbackData.builder().command(COMMAND_NAME + "_add").build().toString())
                        .build()
        ));
        rows.add(new InlineKeyboardRow(
                InlineKeyboardButton.builder()
                        .text(returnIcon + " Вернуться в меню")
                        .callbackData(CallbackData.builder().command("/start").build().toString())
                        .build()
        ));
        return InlineKeyboardMarkup.builder().keyboard(rows).build();
    }

    private String trim(String title) {
        if (title == null) return "";
        return title.length() > 28 ? title.substring(0, 28) + "..." : title;
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
        return "Показать все места практики";
    }
}
