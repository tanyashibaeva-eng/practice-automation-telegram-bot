package ru.itmo.application;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardRow;
import ru.itmo.bot.CallbackData;
import ru.itmo.bot.MessageToUser;
import ru.itmo.domain.model.GuideSubsection;
import ru.itmo.exception.InternalException;
import ru.itmo.infra.handler.usecase.Command;
import ru.itmo.infra.handler.usecase.user.guide.GuideNavigateCommand;
import ru.itmo.infra.handler.usecase.user.manual.ManualEditAbortCommand;
import ru.itmo.infra.storage.GuideRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class GuideService {

    private static final int TELEGRAM_TEXT_LIMIT = 4096;

    // Подраздел с этим названием становится содержанием раздела: всегда стоит первым,
    // клавиатура строится динамически из остальных подразделов.
    // Чтобы создать содержание, достаточно добавить подраздел с названием «Содержание».
    public static final String TOC_SUBSECTION_TITLE = "Содержание";

    private GuideService() {
    }

    public static MessageToUser openSectionByCommand(String command) throws InternalException {
        var section = GuideRepository.findActiveSectionByCommand(command);
        if (section.isEmpty()) {
            return MessageToUser.builder()
                    .text("Раздел не найден.")
                    .needRewriting(true)
                    .build();
        }
        var first = GuideRepository.findFirstSubsectionBySectionId(section.get().getId());
        if (first.isEmpty()) {
            return MessageToUser.builder()
                    .text("В этом разделе пока нет материалов.")
                    .needRewriting(true)
                    .build();
        }
        return buildSubsectionMessage(first.get());
    }

    public static MessageToUser openSubsectionById(int subsectionId) throws InternalException {
        var sub = GuideRepository.findSubsectionById(subsectionId);
        if (sub.isEmpty()) {
            return MessageToUser.builder()
                    .text("Подраздел не найден.")
                    .needRewriting(true)
                    .build();
        }
        return buildSubsectionMessage(sub.get());
    }

    private static MessageToUser buildSubsectionMessage(GuideSubsection sub) throws InternalException {
        String text = truncateForTelegram(previewSubsectionHtml(sub, sub.getBody() == null ? "" : sub.getBody()));

        return MessageToUser.builder()
                .text(text)
                .parseMode("HTML")
                .keyboardMarkup(buildNavKeyboard(sub))
                .needRewriting(true)
                .build();
    }

    public static String previewSubsectionHtml(GuideSubsection sub, String bodyRaw) {
        String titlePart = "<b>" + escapeHtml(sub.getTitle()) + "</b>";
        String bodyPart = formatBodyToHtml(bodyRaw == null ? "" : bodyRaw);
        bodyPart = bodyPart.stripLeading();
        return titlePart + "\n\n" + bodyPart;
    }

    public static MessageToUser manualEditPromptWithCurrentView(GuideSubsection sub) {
        String preview = previewSubsectionHtml(sub, sub.getBody() == null ? "" : sub.getBody());
        String header = "<b>Подраздел «" + escapeHtml(sub.getTitle()) + "»</b>\n\n<b>Сейчас отображается так:</b>\n\n";
        String footer = "\n\n<i>Отправьте одним следующим сообщением новый текст — он заменит содержимое в базе. "
                + "Дальше будет предпросмотр с кнопками «Подтвердить» / «Отменить».</i>\n\n"
                + "<i>Отмена: кнопка ниже или строка, начинающаяся с / (например /start).</i>";
        String full = capHtmlPreviewThreePart(header, preview, footer);
        return MessageToUser.builder()
                .text(full)
                .parseMode("HTML")
                .keyboardMarkup(ManualEditAbortCommand.awaitInputMarkup())
                .needRewriting(true)
                .build();
    }

    public static MessageToUser manualEditRepromptAfterDraftCancel(GuideSubsection sub) {
        String preview = previewSubsectionHtml(sub, sub.getBody() == null ? "" : sub.getBody());
        String header = "<i>Черновик отменён.</i>\n\n<b>Подраздел «" + escapeHtml(sub.getTitle()) + "»</b>\n\n"
                + "<b>Сейчас в базе отображается так:</b>\n\n";
        String footer = "\n\n<i>Отправьте одним сообщением новый текст. Дальше снова будет предпросмотр.</i>\n\n"
                + "<i>Отмена: кнопка ниже или строка, начинающаяся с /.</i>";
        String full = capHtmlPreviewThreePart(header, preview, footer);
        return MessageToUser.builder()
                .text(full)
                .parseMode("HTML")
                .keyboardMarkup(ManualEditAbortCommand.awaitInputMarkup())
                .needRewriting(true)
                .build();
    }

    private static String formatBodyToHtml(String bodyRaw) {
        if (isLikelyHtmlBody(bodyRaw)) {
            return bodyRaw;
        }
        if (SimpleMarkdownToTelegramHtml.looksLikeMarkdown(bodyRaw)) {
            return SimpleMarkdownToTelegramHtml.convert(bodyRaw);
        }
        return escapeHtml(bodyRaw);
    }

    private static boolean isLikelyHtmlBody(String body) {
        if (body == null || body.isBlank()) {
            return false;
        }
        String t = body.trim();
        return t.startsWith("<")
                || body.contains("<b>")
                || body.contains("<i>")
                || body.contains("<code>")
                || body.contains("<pre>")
                || body.contains("<a href")
                || body.contains("<blockquote")
                || body.contains("<tg-spoiler")
                || body.contains("<tg-emoji");
    }

    private static String escapeHtml(String plain) {
        if (plain == null) {
            return "";
        }
        return plain
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    public static String truncateForTelegram(String text) {
        if (text.length() <= TELEGRAM_TEXT_LIMIT) {
            return text;
        }
        return text.substring(0, TELEGRAM_TEXT_LIMIT - 20) + "\n\n… (текст обрезан)";
    }

    public static String capHtmlPreviewThreePart(String headerHtml, String coreHtml, String footerHtml) {
        if (headerHtml == null) {
            headerHtml = "";
        }
        if (coreHtml == null) {
            coreHtml = "";
        }
        if (footerHtml == null) {
            footerHtml = "";
        }
        String noticeTooLong = "\n\n<i>… обрезано: в одном сообщении Telegram не больше "
                + TELEGRAM_TEXT_LIMIT
                + " символов. При редактировании справки в базу по-прежнему сохраняется весь присланный текст.</i>";
        long total = (long) headerHtml.length() + coreHtml.length() + footerHtml.length();
        if (total <= TELEGRAM_TEXT_LIMIT) {
            return headerHtml + coreHtml + footerHtml;
        }
        int budget = TELEGRAM_TEXT_LIMIT - headerHtml.length() - footerHtml.length() - noticeTooLong.length();
        if (budget < 80) {
            budget = TELEGRAM_TEXT_LIMIT - headerHtml.length() - footerHtml.length() - 40;
        }
        if (budget <= 0) {
            return headerHtml + noticeTooLong + footerHtml;
        }
        String truncated = coreHtml.length() <= budget ? coreHtml : coreHtml.substring(0, budget);
        return headerHtml + truncated + noticeTooLong + footerHtml;
    }

    public static String wrapManualEditPreviewHtml(String previewCoreHtml) {
        String intro = "<b>Предпросмотр:</b>\n\n";
        String footer = "\n\n<i>Подтвердите замену текста в базе или отмените (вернётесь к вводу нового текста).</i>";
        return capHtmlPreviewThreePart(intro, previewCoreHtml, footer);
    }

    private static boolean isTableOfContents(GuideSubsection sub) {
        return TOC_SUBSECTION_TITLE.equals(sub.getTitle());
    }

    private static InlineKeyboardMarkup buildNavKeyboard(GuideSubsection sub) throws InternalException {
        if (isTableOfContents(sub)) {
            return buildTocKeyboard(sub);
        }

        var markup = InlineKeyboardMarkup.builder();
        List<InlineKeyboardButton> navRow = new ArrayList<>();

        if (sub.getPrevSubsectionId() != null) {
            Optional<GuideSubsection> prev = GuideRepository.findSubsectionById(sub.getPrevSubsectionId());
            if (prev.isPresent()) {
                navRow.add(InlineKeyboardButton.builder()
                        .text(trimButtonLabel("◀ " + prev.get().getTitle()))
                        .callbackData(CallbackData.builder()
                                .command(GuideNavigateCommand.COMMAND_NAME)
                                .key("subsectionId")
                                .value(String.valueOf(prev.get().getId()))
                                .build()
                                .toString())
                        .build());
            }
        }
        if (sub.getNextSubsectionId() != null) {
            Optional<GuideSubsection> next = GuideRepository.findSubsectionById(sub.getNextSubsectionId());
            if (next.isPresent()) {
                navRow.add(InlineKeyboardButton.builder()
                        .text(trimButtonLabel(next.get().getTitle() + " ▶"))
                        .callbackData(CallbackData.builder()
                                .command(GuideNavigateCommand.COMMAND_NAME)
                                .key("subsectionId")
                                .value(String.valueOf(next.get().getId()))
                                .build()
                                .toString())
                        .build());
            }
        }
        if (!navRow.isEmpty()) {
            markup.keyboardRow(new InlineKeyboardRow(navRow));
        }
        markup.keyboardRow(new InlineKeyboardRow(
                InlineKeyboardButton.builder()
                        .text(Command.returnIcon + " Вернуться в меню")
                        .callbackData(CallbackData.builder()
                                .command("/start")
                                .build()
                                .toString())
                        .build()
        ));
        return markup.build();
    }

    private static InlineKeyboardMarkup buildTocKeyboard(GuideSubsection tocSubsection) throws InternalException {
        var markup = InlineKeyboardMarkup.builder();
        List<GuideSubsection> entries = GuideRepository.findSubsectionsBySectionIdExcept(
                tocSubsection.getSectionId(),
                tocSubsection.getId());
        for (GuideSubsection entry : entries) {
            markup.keyboardRow(new InlineKeyboardRow(
                    InlineKeyboardButton.builder()
                            .text(trimButtonLabel(entry.getTitle()))
                            .callbackData(CallbackData.builder()
                                    .command(GuideNavigateCommand.COMMAND_NAME)
                                    .key("subsectionId")
                                    .value(String.valueOf(entry.getId()))
                                    .build()
                                    .toString())
                            .build()
            ));
        }
        markup.keyboardRow(new InlineKeyboardRow(
                InlineKeyboardButton.builder()
                        .text(Command.returnIcon + " Вернуться в меню")
                        .callbackData(CallbackData.builder()
                                .command("/start")
                                .build()
                                .toString())
                        .build()
        ));
        return markup.build();
    }

    private static String trimButtonLabel(String label) {
        if (label.length() <= 64) {
            return label;
        }
        return label.substring(0, 61) + "...";
    }

}
