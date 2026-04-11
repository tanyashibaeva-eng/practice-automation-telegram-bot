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
import ru.itmo.infra.handler.usecase.admin.AdminCommand;
import ru.itmo.util.TextUtils;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ListPracticeOptionsCommand implements AdminCommand {
    private static final String COMMAND_NAME = "/practice_option_list";
    private static final Pattern ACTION_AND_ID_PATTERN = Pattern.compile("(enabled|rename|itmo|company|delete)\\D*(\\d+)");

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
        if (text == null) return null;
        var hashIdx = text.indexOf('#');
        if (hashIdx >= 0) {
            text = text.substring(0, hashIdx);
        }
        text = text.trim();

        if (text.equals(COMMAND_NAME + " add") || text.equals(COMMAND_NAME + "_add")) {
            ContextHolder.setNextCommand(message.getChatId(), new PracticeOptionAddInputCommand());
            return MessageToUser.builder()
                    .text("Введите название нового места практики")
                    .keyboardMarkup(getReturnToStartMarkup())
                    .build();
        }

        if (!text.startsWith(COMMAND_NAME)) {
            return null;
        }
        var payload = text.substring(COMMAND_NAME.length());
        Matcher matcher = ACTION_AND_ID_PATTERN.matcher(payload);
        if (!matcher.find()) {
            return null;
        }
        var action = matcher.group(1);
        long id = TextUtils.parseDoubleStrToLong(matcher.group(2));
        var option = PracticeOptionService.getAllOptions().stream().filter(o -> o.getId() == id).findFirst().orElse(null);
        if (option == null) return null;

        switch (action) {
            case "enabled" -> {
                if (option.isEnabled()) PracticeOptionService.disableOption(id);
                else PracticeOptionService.enableOption(id);
            }
            case "rename" -> {
                ContextHolder.setCommandData(message.getChatId(), id);
                ContextHolder.setNextCommand(message.getChatId(), new PracticeOptionRenameInputCommand());
                return MessageToUser.builder()
                        .text("Переименование варианта #%d\nТекущее название: %s\n\nВведите новое название:".formatted(id, option.getTitle()))
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
            default -> {
                return null;
            }
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
                            .callbackData(CallbackData.builder().command(COMMAND_NAME + " rename " + option.getId()).build().toString())
                            .build()
            ));
            rows.add(new InlineKeyboardRow(
                    InlineKeyboardButton.builder()
                            .text("Вкл " + (option.isEnabled() ? "✅" : "❌"))
                            .callbackData(CallbackData.builder().command(COMMAND_NAME + " enabled " + option.getId()).build().toString())
                            .build(),
                    InlineKeyboardButton.builder()
                            .text("ITMO " + (option.isRequiresItmoInfo() ? "✅" : "❌"))
                            .callbackData(CallbackData.builder().command(COMMAND_NAME + " itmo " + option.getId()).build().toString())
                            .build(),
                    InlineKeyboardButton.builder()
                            .text("Company " + (option.isRequiresCompanyInfo() ? "✅" : "❌"))
                            .callbackData(CallbackData.builder().command(COMMAND_NAME + " company " + option.getId()).build().toString())
                            .build(),
                    InlineKeyboardButton.builder()
                            .text("🗑")
                            .callbackData(CallbackData.builder().command(COMMAND_NAME + " delete " + option.getId()).build().toString())
                            .build()
            ));
        }
        rows.add(new InlineKeyboardRow(
                InlineKeyboardButton.builder()
                        .text("➕ Добавить новое место")
                        .callbackData(CallbackData.builder().command(COMMAND_NAME + " add").build().toString())
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
        return title.length() > 28 ? title.substring(0, 28) + "...": title;
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
