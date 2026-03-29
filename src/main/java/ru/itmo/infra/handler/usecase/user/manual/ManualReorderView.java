package ru.itmo.infra.handler.usecase.user.manual;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import ru.itmo.application.SimpleMarkdownToTelegramHtml;
import ru.itmo.bot.CallbackData;
import ru.itmo.bot.MessageToUser;
import ru.itmo.domain.model.GuideSubsection;
import ru.itmo.exception.InternalException;
import ru.itmo.infra.handler.usecase.Command;
import ru.itmo.infra.storage.GuideRepository;

import java.util.ArrayList;
import java.util.List;

public final class ManualReorderView {

    private ManualReorderView() {
    }

    public static MessageToUser build(int sectionId) throws InternalException {
        var secOpt = GuideRepository.findSectionById(sectionId);
        if (secOpt.isEmpty()) {
            return MessageToUser.builder()
                    .text(SimpleMarkdownToTelegramHtml.escapeHtml("Раздел не найден."))
                    .parseMode("HTML")
                    .needRewriting(true)
                    .keyboardMarkup(Command.returnToStartInlineMarkup())
                    .build();
        }
        List<GuideSubsection> subs = GuideRepository.findSubsectionsBySectionOrdered(sectionId);
        StringBuilder text = new StringBuilder();
        text.append("Раздел «").append(secOpt.get().getTitle()).append("»");
        if (subs.isEmpty()) {
            text.append("\n\nПодразделов пока нет.");
        } else {
            text.append(", подразделы:\n\n");
            for (int i = 0; i < subs.size(); i++) {
                text.append(i + 1).append(". ").append(subs.get(i).getTitle()).append('\n');
            }
            text.append("\n▲ и ▼ — порядок, средняя кнопка — редактировать текст, 🗑 — удалить.");
        }

        boolean firstIsToc = !subs.isEmpty() && isTocTitle(subs.get(0).getTitle());

        var markup = InlineKeyboardMarkup.builder();
        for (int i = 0; i < subs.size(); i++) {
            GuideSubsection s = subs.get(i);
            int humanIndex = i + 1;
            boolean isToc = isTocTitle(s.getTitle());
            boolean hasUp = i > 0 && !(firstIsToc && i == 1);
            boolean hasDown = i < subs.size() - 1 && !isToc;
            List<InlineKeyboardButton> row = new ArrayList<>();
            if (hasUp) {
                row.add(moveButton(true, s.getId(), "▲"));
            } else {
                row.add(disabledArrowButton("▲", "up"));
            }
            row.add(rowTitleButton(humanIndex, s.getTitle(), s.getId()));
            if (hasDown) {
                row.add(moveButton(false, s.getId(), "▼"));
            } else {
                row.add(disabledArrowButton("▼", "down"));
            }
            row.add(deleteButton(s.getId()));
            markup.keyboardRow(new InlineKeyboardRow(row));
        }
        markup.keyboardRow(new InlineKeyboardRow(
                InlineKeyboardButton.builder()
                        .text("➕ Добавить подраздел")
                        .callbackData(CallbackData.builder()
                                .command(ManualSubsectionAddCommand.COMMAND_NAME)
                                .key("sectionId")
                                .value(String.valueOf(sectionId))
                                .build()
                                .toString())
                        .build()
        ));
        markup.keyboardRow(new InlineKeyboardRow(
                InlineKeyboardButton.builder()
                        .text(Command.returnIcon + " Вернуться в меню")
                        .callbackData(CallbackData.builder().command("/start").build().toString())
                        .build()
        ));
        String plain = text.toString();
        return MessageToUser.builder()
                .text(SimpleMarkdownToTelegramHtml.escapeHtml(plain))
                .parseMode("HTML")
                .keyboardMarkup(markup.build())
                .needRewriting(true)
                .build();
    }

    private static final int TG_BUTTON_TEXT_MAX = 64;

    private static InlineKeyboardButton moveButton(boolean up, int subsectionId, String label) {
        return InlineKeyboardButton.builder()
                .text(label)
                .callbackData(CallbackData.builder()
                        .command(up ? ManualReorderUpCommand.COMMAND_NAME : ManualReorderDownCommand.COMMAND_NAME)
                        .key("subsectionId")
                        .value(String.valueOf(subsectionId))
                        .build()
                        .toString())
                .build();
    }

    private static InlineKeyboardButton rowTitleButton(int humanIndex, String title, int subsectionId) {
        String numPart = humanIndex + ". ";
        int budget = TG_BUTTON_TEXT_MAX - numPart.length();
        String shortTitle = shortenTitle(title, Math.max(budget, 2));
        String label = numPart + shortTitle;
        if (label.length() > TG_BUTTON_TEXT_MAX) {
            label = label.substring(0, TG_BUTTON_TEXT_MAX - 1) + "…";
        }
        return InlineKeyboardButton.builder()
                .text(label)
                .callbackData(CallbackData.builder()
                        .command(ManualReorderEditBodyCommand.COMMAND_NAME)
                        .key("subsectionId")
                        .value(String.valueOf(subsectionId))
                        .build()
                        .toString())
                .build();
    }

    private static InlineKeyboardButton deleteButton(int subsectionId) {
        return InlineKeyboardButton.builder()
                .text("🗑")
                .callbackData(CallbackData.builder()
                        .command(ManualSubsectionDeleteCommand.COMMAND_NAME)
                        .key("subsectionId")
                        .value(String.valueOf(subsectionId))
                        .build()
                        .toString())
                .build();
    }

    private static InlineKeyboardButton disabledArrowButton(String arrow, String edge) {
        return InlineKeyboardButton.builder()
                .text(arrow)
                .callbackData(CallbackData.builder()
                        .command(ManualReorderNoopCommand.COMMAND_NAME)
                        .key("edge")
                        .value(edge)
                        .build()
                        .toString())
                .build();
    }

    private static boolean isTocTitle(String title) {
        return ru.itmo.application.GuideService.TOC_SUBSECTION_TITLE.equals(title);
    }

    private static String shortenTitle(String title, int maxChars) {
        if (title == null || title.isEmpty()) {
            return "…";
        }
        if (title.length() <= maxChars) {
            return title;
        }
        if (maxChars <= 1) {
            return "…";
        }
        return title.substring(0, maxChars - 1) + "…";
    }
}
