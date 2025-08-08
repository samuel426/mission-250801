package org.example;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JsonExample {

    private static final Pattern ID_P = Pattern.compile("\"id\"\\s*:\\s*(\\d+)");
    private static final Pattern CONTENT_P = Pattern.compile("\"content\"\\s*:\\s*\"(.*?)\"");
    private static final Pattern AUTHOR_P = Pattern.compile("\"author\"\\s*:\\s*\"(.*?)\"");

    public static void ensureDir(Path dir) {
        try {
            if (!Files.exists(dir)) Files.createDirectories(dir);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void writeWiseSaying(Path file, WiseSaying ws) {
        String json = "{\n" +
                "  \"id\": " + ws.getId() + ",\n" +
                "  \"content\": \"" + escape(ws.getContent()) + "\", \n" +
                "  \"author\": \"" + escape(ws.getAuthor()) + "\"\n" +
                "}\n";
        writeString(file, json);
    }

    public static WiseSaying readWiseSaying(Path file) {
        String s = readString(file);
        int id = extractInt(ID_P, s, -1);
        String content = unescape(extractStr(CONTENT_P, s, ""));
        String author = unescape(extractStr(AUTHOR_P, s, ""));
        if (id < 0) throw new RuntimeException("Invalid JSON: " + file);
        return new WiseSaying(id, content, author);
    }

    public static void writeLastId(Path file, int lastId) {
        writeString(file, String.valueOf(lastId));
    }

    public static int readLastId(Path file) {
        if(!Files.exists(file)) return 0;
        String s = readString(file).trim();
        if (s.isEmpty()) return 0;
        try { return Integer.parseInt(s); } catch (NumberFormatException e) { return 0; }
    }

    public static void writeDataJson(Path file, Iterable<WiseSaying> items) {
        StringBuilder sb = new StringBuilder();
        sb.append("[\n");
        boolean first = true;
        for (WiseSaying ws : items) {
            if (!first) sb.append(", \n");
            first = false;
            sb.append("  {\n");
            sb.append("    \"id\": ").append(ws.getId()).append(",\n");
            sb.append("    \"content\": \"").append(escape(ws.getContent())).append("\",\n");
            sb.append("    \"author\": \"").append(escape(ws.getAuthor())).append("\"\n");
            sb.append("  }");
        }
        sb.append("\n]\n");
        writeString(file, sb.toString());
    }

    private static void writeString(Path file, String s) {
        try {
            Files.createDirectories(file.getParent() != null ? file.getParent() : Paths.get("."));
            Files.write(file, s.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String readString(Path file) {
        try {
            return new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static int extractInt(Pattern p, String s, int def) {
        Matcher m = p.matcher(s);
        return m.find() ? Integer.parseInt(m.group(1)) : def;
    }

    private static String extractStr(Pattern p, String s, String def) {
        Matcher m = p.matcher(s);
        return m.find() ? m.group(1) : def;
    }

    private static String escape(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
    }

    private static String unescape(String s) {
        if (s == null) return "";
        return s.replace("\\n", "\n")
                .replace("\\r", "\r")
                .replace("\\\"", "\"")
                .replace("\\\\", "\\");
    }
}
