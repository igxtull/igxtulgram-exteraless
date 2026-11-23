package com.igxtul.gram.core;

/** Работа с цветами в формате Android ARGB (0xAARRGGBB). */
public final class ColorUtil {
    private ColorUtil() { }

    /** Принимает #RGB, #RRGGBB и #AARRGGBB. При ошибке возвращает fallback. */
    public static int parse(String hex, int fallback) {
        if (hex == null) return fallback;
        String s = hex.trim();
        if (s.startsWith("#")) s = s.substring(1);
        if (s.length() == 3) {
            s = String.valueOf(s.charAt(0)) + s.charAt(0)
                    + s.charAt(1) + s.charAt(1)
                    + s.charAt(2) + s.charAt(2);
        }
        if (s.length() == 6) s = "FF" + s;
        if (s.length() != 8) return fallback;
        try { return (int) Long.parseLong(s, 16); } catch (NumberFormatException e) { return fallback; }
    }

    public static String toHex(int argb) {
        return String.format("#%08X", argb);
    }

    public static int withAlpha(int argb, int alpha) {
        return (clamp(alpha, 0, 255) << 24) | (argb & 0x00FFFFFF);
    }

    public static int darken(int argb, float amount) {
        float f = 1f - clamp(amount, 0f, 1f);
        int r = clamp((int) (((argb >> 16) & 0xFF) * f), 0, 255);
        int g = clamp((int) (((argb >> 8) & 0xFF) * f), 0, 255);
        int b = clamp((int) ((argb & 0xFF) * f), 0, 255);
        return (argb & 0xFF000000) | (r << 16) | (g << 8) | b;
    }

    public static int lighten(int argb, float amount) {
        float t = clamp(amount, 0f, 1f);
        int r = mix((argb >> 16) & 0xFF, 255, t);
        int g = mix((argb >> 8) & 0xFF, 255, t);
        int b = mix(argb & 0xFF, 255, t);
        return (argb & 0xFF000000) | (r << 16) | (g << 8) | b;
    }

    public static float luminance(int argb) {
        float r = (argb >> 16) & 0xFF;
        float g = (argb >> 8) & 0xFF;
        float b = argb & 0xFF;
        return (0.299f * r + 0.587f * g + 0.114f * b) / 255f;
    }

    /** Тёмный текст на светлом пузыре, светлый на тёмном. */
    public static int contrastText(int background) {
        return luminance(background) > 0.55f ? 0xFF101217 : 0xFFF7F8FF;
    }

    private static int mix(int a, int b, float t) {
        return clamp((int) (a + (b - a) * t), 0, 255);
    }

    public static int clamp(int v, int lo, int hi) {
        return v < lo ? lo : (v > hi ? hi : v);
    }

    public static float clamp(float v, float lo, float hi) {
        return v < lo ? lo : (v > hi ? hi : v);
    }
}
