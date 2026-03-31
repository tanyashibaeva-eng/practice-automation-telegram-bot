package ru.itmo.application;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SimpleMarkdownToTelegramHtml {

    private static final char PH = '\uE000';
    private static final Pattern INLINE_CODE = Pattern.compile("`([^`]+?)`");
    /** Bold + italic (must run before {@link #BOLD}). */
    private static final Pattern BOLD_ITALIC = Pattern.compile("\\*\\*\\*([^*\\n]+?)\\*\\*\\*");
    private static final Pattern BOLD = Pattern.compile("\\*\\*(.+?)\\*\\*", Pattern.DOTALL);
    /** Single-asterisk emphasis; must not match {@code **} or {@code ***}. */
    private static final Pattern ITALIC = Pattern.compile("(?<!\\*)\\*(?!\\*)([^*\\n]+?)(?<!\\*)\\*(?!\\*)");
    private static final Pattern LINK = Pattern.compile("\\[([^\\]]+)]\\(([^)]+)\\)");

    private SimpleMarkdownToTelegramHtml() {
    }

    public static boolean looksLikeMarkdown(String s) {
        if (s == null || s.isBlank()) {
            return false;
        }
        return s.contains("**")
                || s.contains("```")
                || s.contains("`")
                || (s.contains("[") && s.contains("]("));
    }

    public static String convert(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        String s = input.replace("\r\n", "\n");

        List<String> fencedBodies = new ArrayList<>();
        s = extractFencedCodeBlocks(s, fencedBodies);

        s = replaceAll(INLINE_CODE, s, m -> "<code>" + escapeHtml(m.group(1)) + "</code>");
        s = replaceAll(BOLD_ITALIC, s, m -> "<b><i>" + escapeHtml(m.group(1)) + "</i></b>");
        s = replaceAll(BOLD, s, m -> "<b>" + escapeHtml(m.group(1)) + "</b>");
        s = replaceAll(ITALIC, s, m -> "<i>" + escapeHtml(m.group(1)) + "</i>");
        s = replaceAll(LINK, s, m -> {
            String label = escapeHtml(m.group(1));
            String url = m.group(2).trim().replace("\"", "%22");
            if (!url.startsWith("http://")
                    && !url.startsWith("https://")
                    && !url.startsWith("mailto:")) {
                url = "https://" + url;
            }
            String href = escapeHtmlAttribute(url);
            return "<a href=\"" + href + "\">" + label + "</a>";
        });

        s = escapePlainOutsideTokens(s);

        for (int i = 0; i < fencedBodies.size(); i++) {
            String inner = fencedBodies.get(i);
            if (inner.endsWith("\n")) {
                inner = inner.substring(0, inner.length() - 1);
            }
            s = s.replace(placeholder(i), "<pre>" + escapeHtml(inner) + "</pre>");
        }

        return s;
    }

    private static String extractFencedCodeBlocks(String input, List<String> bodies) {
        String[] rawLines = input.split("\n", -1);
        StringBuilder out = new StringBuilder();
        int i = 0;
        while (i < rawLines.length) {
            String line = rawLines[i];
            if (!line.trim().startsWith("```")) {
                if (out.length() > 0) {
                    out.append('\n');
                }
                out.append(line);
                i++;
                continue;
            }
            int openLine = i;
            i++;
            StringBuilder code = new StringBuilder();
            while (i < rawLines.length && !rawLines[i].trim().equals("```")) {
                if (code.length() > 0) {
                    code.append('\n');
                }
                code.append(rawLines[i]);
                i++;
            }
            if (i < rawLines.length) {
                bodies.add(code.toString());
                if (out.length() > 0) {
                    out.append('\n');
                }
                out.append(placeholder(bodies.size() - 1));
                i++;
            } else {
                if (out.length() > 0) {
                    out.append('\n');
                }
                out.append(rawLines[openLine]);
                if (code.length() > 0) {
                    out.append('\n').append(code);
                }
            }
        }
        return out.toString();
    }

    private static String placeholder(int index) {
        return String.valueOf(PH) + index + PH;
    }

    private static String replaceAll(Pattern p, String s, Function<Matcher, String> repl) {
        Matcher m = p.matcher(s);
        StringBuffer buf = new StringBuffer();
        while (m.find()) {
            m.appendReplacement(buf, Matcher.quoteReplacement(repl.apply(m)));
        }
        m.appendTail(buf);
        return buf.toString();
    }

    private static String escapePlainOutsideTokens(String s) {
        String ph = Pattern.quote(String.valueOf(PH));
        Pattern token = Pattern.compile(
                "<code>[^<]*</code>|<b>[\\s\\S]*?</b>|<i>[\\s\\S]*?</i>|<a href=\"[^\"]+\">[^<]*</a>|"
                        + ph + "\\d+" + ph);
        Matcher m = token.matcher(s);
        StringBuilder out = new StringBuilder();
        int last = 0;
        while (m.find()) {
            out.append(escapeHtml(s.substring(last, m.start())));
            out.append(m.group());
            last = m.end();
        }
        out.append(escapeHtml(s.substring(last)));
        return out.toString();
    }

    public static String escapeHtml(String plain) {
        if (plain == null) {
            return "";
        }
        return plain
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    private static String escapeHtmlAttribute(String url) {
        if (url == null) {
            return "";
        }
        return url
                .replace("&", "&amp;")
                .replace("\"", "&quot;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
