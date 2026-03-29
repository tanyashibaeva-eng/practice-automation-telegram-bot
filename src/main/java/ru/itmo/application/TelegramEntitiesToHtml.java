package ru.itmo.application;

import org.telegram.telegrambots.meta.api.objects.MessageEntity;
import org.telegram.telegrambots.meta.api.objects.User;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

public final class TelegramEntitiesToHtml {

    private TelegramEntitiesToHtml() {
    }

    public static String convert(String text, List<MessageEntity> entities) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        if (entities == null || entities.isEmpty()) {
            return SimpleMarkdownToTelegramHtml.escapeHtml(text);
        }
        List<MessageEntity> valid = new ArrayList<>();
        for (MessageEntity e : entities) {
            if (e == null || e.getOffset() == null || e.getLength() == null) {
                continue;
            }
            int o = e.getOffset();
            int l = e.getLength();
            if (l <= 0 || o < 0 || o + l > text.length()) {
                continue;
            }
            valid.add(e);
        }
        if (valid.isEmpty()) {
            return SimpleMarkdownToTelegramHtml.escapeHtml(text);
        }

        TreeSet<Integer> cuts = new TreeSet<>();
        cuts.add(0);
        cuts.add(text.length());
        for (MessageEntity e : valid) {
            int o = e.getOffset();
            int end = o + e.getLength();
            cuts.add(o);
            cuts.add(end);
        }

        List<Integer> points = new ArrayList<>(cuts);
        StringBuilder out = new StringBuilder();
        for (int i = 0; i + 1 < points.size(); i++) {
            int b0 = points.get(i);
            int b1 = points.get(i + 1);
            if (b0 >= b1) {
                continue;
            }
            List<MessageEntity> active = new ArrayList<>();
            for (MessageEntity e : valid) {
                int o = e.getOffset();
                int end = o + e.getLength();
                if (o <= b0 && end >= b1) {
                    active.add(e);
                }
            }
            active.sort(INNERMOST_FIRST);
            String segment = text.substring(b0, b1);
            String escaped = SimpleMarkdownToTelegramHtml.escapeHtml(segment);
            String wrapped = escaped;
            for (MessageEntity e : active) {
                wrapped = wrapWithEntity(wrapped, e, segment);
            }
            out.append(wrapped);
        }
        return out.toString();
    }

    private static final Comparator<MessageEntity> INNERMOST_FIRST = Comparator
            .comparingInt((MessageEntity e) -> e.getLength())
            .thenComparingInt(e -> -e.getOffset());

    private static String wrapWithEntity(String innerHtml, MessageEntity e, String rawSegment) {
        String type = e.getType() == null ? "" : e.getType();
        return switch (type) {
            case "bold", "strong" -> "<b>" + innerHtml + "</b>";
            case "italic", "em" -> "<i>" + innerHtml + "</i>";
            case "underline" -> "<u>" + innerHtml + "</u>";
            case "strikethrough" -> "<s>" + innerHtml + "</s>";
            case "spoiler" -> "<tg-spoiler>" + innerHtml + "</tg-spoiler>";
            case "code" -> "<code>" + innerHtml + "</code>";
            case "pre" -> wrapPre(innerHtml, e.getLanguage());
            case "text_link" -> {
                String url = e.getUrl();
                if (url == null || url.isBlank()) {
                    yield innerHtml;
                }
                yield "<a href=\"" + escapeAttr(url) + "\">" + innerHtml + "</a>";
            }
            case "text_mention" -> {
                User u = e.getUser();
                if (u == null || u.getId() == null) {
                    yield innerHtml;
                }
                yield "<a href=\"tg://user?id=" + u.getId() + "\">" + innerHtml + "</a>";
            }
            case "url" -> "<a href=\"" + escapeAttr(rawSegment) + "\">" + innerHtml + "</a>";
            case "email" -> "<a href=\"mailto:" + escapeAttr(rawSegment) + "\">" + innerHtml + "</a>";
            case "phone_number" -> {
                String tel = rawSegment.replace(" ", "").replace("-", "");
                yield "<a href=\"tel:" + escapeAttr(tel) + "\">" + innerHtml + "</a>";
            }
            case "blockquote" -> "<blockquote>" + innerHtml + "</blockquote>";
            case "expandable_blockquote" -> "<blockquote expandable>" + innerHtml + "</blockquote>";
            case "custom_emoji" -> {
                String id = e.getCustomEmojiId();
                if (id == null || id.isBlank()) {
                    yield innerHtml;
                }
                yield "<tg-emoji emoji-id=\"" + escapeAttr(id) + "\">" + innerHtml + "</tg-emoji>";
            }
            default -> innerHtml;
        };
    }

    private static String wrapPre(String innerEscaped, String language) {
        if (language != null && !language.isBlank()) {
            String cls = safeLanguageClass(language);
            if (!cls.isEmpty()) {
                return "<pre><code class=\"language-" + cls + "\">" + innerEscaped + "</code></pre>";
            }
        }
        return "<pre>" + innerEscaped + "</pre>";
    }

    private static String safeLanguageClass(String language) {
        return language.replaceAll("[^a-zA-Z0-9_-]", "");
    }

    private static String escapeAttr(String v) {
        if (v == null) {
            return "";
        }
        return v.replace("&", "&amp;")
                .replace("\"", "&quot;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
