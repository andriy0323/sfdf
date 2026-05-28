package me.ariscore.util;

public final class TimeUtil {

    private TimeUtil() {}

    /** Parses strings like "2h", "30m", "5d", "20s" into milliseconds. */
    public static long parseDuration(String s) {
        if (s == null || s.isEmpty()) return 0L;
        char c = s.charAt(s.length() - 1);
        long num;
        try {
            num = Long.parseLong(c >= '0' && c <= '9' ? s : s.substring(0, s.length() - 1));
        } catch (NumberFormatException ex) {
            return 0L;
        }
        return switch (c) {
            case 's' -> num * 1_000L;
            case 'm' -> num * 60_000L;
            case 'h' -> num * 3_600_000L;
            case 'd' -> num * 86_400_000L;
            case 'w' -> num * 604_800_000L;
            default -> num * 1_000L;
        };
    }

    public static String formatLeft(long millis) {
        if (millis <= 0) return "0s";
        long total = millis / 1000L;
        long d = total / 86400; total -= d * 86400;
        long h = total / 3600;  total -= h * 3600;
        long m = total / 60;    total -= m * 60;
        StringBuilder b = new StringBuilder();
        if (d > 0) b.append(d).append("d ");
        if (h > 0) b.append(h).append("h ");
        if (m > 0) b.append(m).append("m ");
        if (total > 0 || b.length() == 0) b.append(total).append("s");
        return b.toString().trim();
    }
}
