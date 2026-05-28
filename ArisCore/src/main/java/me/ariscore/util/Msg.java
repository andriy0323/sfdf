package me.ariscore.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * RGB gradient + tag parser used by every ArisCore module.
 *
 * Supported tokens (inside any string):
 *   &amp;a..&amp;f   legacy colour codes
 *   #RRGGBB     single hex colour applied to the next run
 *   &lt;grad:#start:#end&gt;text&lt;/grad&gt;    RGB gradient
 *   &lt;b&gt;text&lt;/b&gt;   bold (also &lt;i&gt; italic, &lt;u&gt; underline)
 *   &lt;click:run:/foo&gt;text&lt;/click&gt;        clickable command/suggest/url
 *   &lt;hover:hint&gt;text&lt;/hover&gt;             hover tooltip
 */
public final class Msg {

    private static final Pattern HEX_PATTERN  = Pattern.compile("#([0-9a-fA-F]{6})");
    private static final Pattern GRAD_PATTERN = Pattern.compile("<grad:#?([0-9a-fA-F]{6}):#?([0-9a-fA-F]{6})>(.*?)</grad>", Pattern.DOTALL);
    private static final Pattern CLICK_PATTERN = Pattern.compile("<click:(run|suggest|url):([^>]+)>(.*?)</click>", Pattern.DOTALL);
    private static final Pattern HOVER_PATTERN = Pattern.compile("<hover:([^>]+)>(.*?)</hover>", Pattern.DOTALL);

    private Msg() {}

    /** Parse a string with our colour/tag mini-language to an Adventure Component. */
    public static Component mm(String raw) {
        if (raw == null || raw.isEmpty()) return Component.empty();
        return parseAll(raw);
    }

    /** Convenience: parse list of strings and return as joined component with newlines. */
    public static Component mmLines(List<String> lines) {
        if (lines == null || lines.isEmpty()) return Component.empty();
        Component out = Component.empty();
        for (int i = 0; i < lines.size(); i++) {
            out = out.append(parseAll(lines.get(i)));
            if (i < lines.size() - 1) out = out.append(Component.newline());
        }
        return out;
    }

    /** Render a simple gradient over a single line of text. */
    public static Component gradient(String text, int from, int to) {
        if (text == null || text.isEmpty()) return Component.empty();
        int len = text.length();
        if (len == 1) return Component.text(text, TextColor.color(from));

        TextComponent.Builder b = Component.text();
        for (int i = 0; i < len; i++) {
            float t = (float) i / Math.max(1, len - 1);
            int r = (int) (((from >> 16) & 0xFF) * (1 - t) + ((to >> 16) & 0xFF) * t);
            int g = (int) (((from >> 8) & 0xFF) * (1 - t) + ((to >> 8) & 0xFF) * t);
            int bl = (int) ((from & 0xFF) * (1 - t) + (to & 0xFF) * t);
            b.append(Component.text(String.valueOf(text.charAt(i)), TextColor.color(r, g, bl)));
        }
        return b.build();
    }

    /** Plain Component without italic. */
    public static Component plain(String text) {
        return Component.text(text == null ? "" : text)
                .decoration(TextDecoration.ITALIC, false);
    }

    public static int parseHex(String hex, int fallback) {
        if (hex == null) return fallback;
        String h = hex.startsWith("#") ? hex.substring(1) : hex;
        try { return Integer.parseInt(h, 16); } catch (Exception e) { return fallback; }
    }

    private static Component parseAll(String raw) {
        String s = raw;

        s = replaceAmpersandColors(s);

        Component out = Component.empty();
        out = parseGradients(s, out);
        return out;
    }

    private static String replaceAmpersandColors(String s) {
        return s.replace('&', '\u00a7');
    }

    private static Component parseGradients(String s, Component acc) {
        Matcher gm = GRAD_PATTERN.matcher(s);
        int last = 0;
        Component out = acc;
        while (gm.find()) {
            String before = s.substring(last, gm.start());
            if (!before.isEmpty()) out = out.append(parseClicks(before));
            int from = parseHex(gm.group(1), 0xFFFFFF);
            int to   = parseHex(gm.group(2), 0xFFFFFF);
            out = out.append(gradient(stripDecor(gm.group(3)), from, to));
            last = gm.end();
        }
        if (last < s.length()) out = out.append(parseClicks(s.substring(last)));
        return out;
    }

    private static String stripDecor(String s) {
        return s.replaceAll("<[^>]+>", "");
    }

    private static Component parseClicks(String s) {
        Matcher cm = CLICK_PATTERN.matcher(s);
        if (cm.find()) {
            int from = cm.start();
            int to = cm.end();
            String before = s.substring(0, from);
            String type = cm.group(1);
            String value = cm.group(2);
            String text = cm.group(3);
            String after = s.substring(to);
            ClickEvent.Action action = switch (type) {
                case "suggest" -> ClickEvent.Action.SUGGEST_COMMAND;
                case "url"     -> ClickEvent.Action.OPEN_URL;
                default        -> ClickEvent.Action.RUN_COMMAND;
            };
            Component inner = parseHovers(text).clickEvent(ClickEvent.clickEvent(action, value));
            return parseHovers(before).append(inner).append(parseClicks(after));
        }
        return parseHovers(s);
    }

    private static Component parseHovers(String s) {
        Matcher hm = HOVER_PATTERN.matcher(s);
        if (hm.find()) {
            int from = hm.start();
            int to = hm.end();
            String before = s.substring(0, from);
            String hint = hm.group(1);
            String text = hm.group(2);
            String after = s.substring(to);
            Component inner = parseHexAndLegacy(text).hoverEvent(HoverEvent.showText(parseHexAndLegacy(hint)));
            return parseHexAndLegacy(before).append(inner).append(parseHovers(after));
        }
        return parseHexAndLegacy(s);
    }

    private static Component parseHexAndLegacy(String s) {
        if (s == null || s.isEmpty()) return Component.empty();
        Matcher hm = HEX_PATTERN.matcher(s);
        TextComponent.Builder b = Component.text();
        int last = 0;
        TextColor cur = null;
        boolean bold = false, italic = false, underline = false;

        while (hm.find()) {
            String pre = s.substring(last, hm.start());
            if (!pre.isEmpty()) appendWithCurrent(b, pre, cur, bold, italic, underline);
            cur = TextColor.fromHexString("#" + hm.group(1));
            last = hm.end();
        }
        if (last < s.length()) appendWithCurrent(b, s.substring(last), cur, bold, italic, underline);
        return b.build();
    }

    private static void appendWithCurrent(TextComponent.Builder b, String text, TextColor cur, boolean bold, boolean italic, boolean underline) {
        Component c = Component.text(text);
        if (cur != null) c = c.color(cur);
        if (bold)      c = c.decoration(TextDecoration.BOLD, true);
        if (italic)    c = c.decoration(TextDecoration.ITALIC, true);
        if (underline) c = c.decoration(TextDecoration.UNDERLINED, true);
        c = c.decoration(TextDecoration.ITALIC, italic);
        b.append(c);
    }

    public static List<Component> mmList(List<String> lines) {
        List<Component> out = new ArrayList<>();
        if (lines == null) return out;
        for (String s : lines) out.add(mm(s).decoration(TextDecoration.ITALIC, false));
        return out;
    }

    public static Component prefix() {
        return gradient("ArisWorld", 0xFFB300, 0xFF4500)
                .decoration(TextDecoration.BOLD, true)
                .append(Component.text(" » ", NamedTextColor.GRAY));
    }
}
