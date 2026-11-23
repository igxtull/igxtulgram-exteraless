package com.igxtul.gram.core;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Минимальный JSON-парсер/сериализатор без зависимостей.
 * Нужно, чтобы ядро мода компилировалось обычным javac и работало
 * внутри Telegram без добавления сторонних библиотек.
 */
public final class Json {
    private Json() { }

    public static Object parse(String text) {
        if (text == null) throw new IllegalArgumentException("json is null");
        Parser p = new Parser(text);
        Object value = p.value();
        p.ws();
        if (!p.eof()) throw p.error("trailing characters");
        return value;
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> object(Object value) {
        return value instanceof Map ? (Map<String, Object>) value : new LinkedHashMap<String, Object>();
    }

    @SuppressWarnings("unchecked")
    public static List<Object> array(Object value) {
        return value instanceof List ? (List<Object>) value : new ArrayList<Object>();
    }

    public static String str(Object value, String fallback) {
        return value instanceof String ? (String) value : fallback;
    }

    public static int integer(Object value, int fallback) {
        if (value instanceof Number) return ((Number) value).intValue();
        if (value instanceof String) {
            try { return Integer.parseInt(((String) value).trim()); } catch (NumberFormatException ignored) { }
        }
        return fallback;
    }

    public static float flt(Object value, float fallback) {
        if (value instanceof Number) return ((Number) value).floatValue();
        if (value instanceof String) {
            try { return Float.parseFloat(((String) value).trim()); } catch (NumberFormatException ignored) { }
        }
        return fallback;
    }

    public static boolean bool(Object value, boolean fallback) {
        if (value instanceof Boolean) return (Boolean) value;
        if (value instanceof String) {
            String s = ((String) value).trim().toLowerCase();
            if (s.equals("true") || s.equals("1") || s.equals("yes")) return true;
            if (s.equals("false") || s.equals("0") || s.equals("no")) return false;
        }
        if (value instanceof Number) return ((Number) value).intValue() != 0;
        return fallback;
    }

    public static String stringify(Object value) {
        StringBuilder out = new StringBuilder();
        write(value, out);
        return out.toString();
    }

    private static void write(Object value, StringBuilder out) {
        if (value == null) { out.append("null"); return; }
        if (value instanceof String) { quote((String) value, out); return; }
        if (value instanceof Boolean || value instanceof Number) { out.append(value); return; }
        if (value instanceof Map) {
            out.append('{');
            boolean first = true;
            for (Map.Entry<?, ?> e : ((Map<?, ?>) value).entrySet()) {
                if (!first) out.append(',');
                first = false;
                quote(String.valueOf(e.getKey()), out);
                out.append(':');
                write(e.getValue(), out);
            }
            out.append('}');
            return;
        }
        if (value instanceof Iterable) {
            out.append('[');
            boolean first = true;
            for (Object item : (Iterable<?>) value) {
                if (!first) out.append(',');
                first = false;
                write(item, out);
            }
            out.append(']');
            return;
        }
        quote(String.valueOf(value), out);
    }

    private static void quote(String s, StringBuilder out) {
        out.append('"');
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"': out.append("\\\""); break;
                case '\\': out.append("\\\\"); break;
                case '\b': out.append("\\b"); break;
                case '\f': out.append("\\f"); break;
                case '\n': out.append("\\n"); break;
                case '\r': out.append("\\r"); break;
                case '\t': out.append("\\t"); break;
                default:
                    if (c < 0x20) out.append(String.format("\\u%04x", (int) c));
                    else out.append(c);
            }
        }
        out.append('"');
    }

    private static final class Parser {
        private final String s;
        private int i;

        Parser(String s) { this.s = s; }

        boolean eof() { return i >= s.length(); }

        void ws() {
            while (i < s.length() && Character.isWhitespace(s.charAt(i))) i++;
        }

        RuntimeException error(String msg) {
            return new IllegalArgumentException("Invalid JSON at " + i + ": " + msg);
        }

        Object value() {
            ws();
            if (eof()) throw error("unexpected end");
            char c = s.charAt(i);
            switch (c) {
                case '{': return object();
                case '[': return array();
                case '"': return string();
                case 't': word("true"); return Boolean.TRUE;
                case 'f': word("false"); return Boolean.FALSE;
                case 'n': word("null"); return null;
                default: return number();
            }
        }

        void word(String w) {
            if (!s.startsWith(w, i)) throw error("expected " + w);
            i += w.length();
        }

        Map<String, Object> object() {
            LinkedHashMap<String, Object> map = new LinkedHashMap<String, Object>();
            i++;
            ws();
            if (!eof() && s.charAt(i) == '}') { i++; return map; }
            while (true) {
                ws();
                if (eof() || s.charAt(i) != '"') throw error("expected key");
                String key = string();
                ws();
                if (eof() || s.charAt(i) != ':') throw error("expected ':'");
                i++;
                map.put(key, value());
                ws();
                if (eof()) throw error("unterminated object");
                char c = s.charAt(i++);
                if (c == '}') return map;
                if (c != ',') throw error("expected ',' or '}'");
            }
        }

        List<Object> array() {
            List<Object> list = new ArrayList<Object>();
            i++;
            ws();
            if (!eof() && s.charAt(i) == ']') { i++; return list; }
            while (true) {
                list.add(value());
                ws();
                if (eof()) throw error("unterminated array");
                char c = s.charAt(i++);
                if (c == ']') return list;
                if (c != ',') throw error("expected ',' or ']'");
            }
        }

        String string() {
            i++;
            StringBuilder out = new StringBuilder();
            while (i < s.length()) {
                char c = s.charAt(i++);
                if (c == '"') return out.toString();
                if (c != '\\') { out.append(c); continue; }
                if (i >= s.length()) throw error("bad escape");
                char e = s.charAt(i++);
                switch (e) {
                    case '"': out.append('"'); break;
                    case '\\': out.append('\\'); break;
                    case '/': out.append('/'); break;
                    case 'b': out.append('\b'); break;
                    case 'f': out.append('\f'); break;
                    case 'n': out.append('\n'); break;
                    case 'r': out.append('\r'); break;
                    case 't': out.append('\t'); break;
                    case 'u':
                        if (i + 4 > s.length()) throw error("bad unicode escape");
                        out.append((char) Integer.parseInt(s.substring(i, i + 4), 16));
                        i += 4;
                        break;
                    default: throw error("unknown escape \\" + e);
                }
            }
            throw error("unterminated string");
        }

        Object number() {
            int start = i;
            if (!eof() && (s.charAt(i) == '-' || s.charAt(i) == '+')) i++;
            boolean real = false;
            while (!eof()) {
                char c = s.charAt(i);
                if (c >= '0' && c <= '9') { i++; continue; }
                if (c == '.' || c == 'e' || c == 'E' || c == '+' || c == '-') { real = true; i++; continue; }
                break;
            }
            if (start == i) throw error("expected value");
            String token = s.substring(start, i);
            try {
                if (real) return Double.valueOf(token);
                long l = Long.parseLong(token);
                if (l >= Integer.MIN_VALUE && l <= Integer.MAX_VALUE) return Integer.valueOf((int) l);
                return Long.valueOf(l);
            } catch (NumberFormatException ex) {
                throw error("bad number " + token);
            }
        }
    }
}
