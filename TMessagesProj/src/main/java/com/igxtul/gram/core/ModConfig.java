package com.igxtul.gram.core;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Хранилище настроек мода в виде ключ -> значение.
 *
 * Ключевой момент: отдельный набор "грязных" ключей. Он позволяет отличить
 * значение, которое пользователь изменил сам, от значения по умолчанию темы.
 * Благодаря этому смена темы не затирает ручные правки.
 */
public class ModConfig {
    public static final String K_THEME = "theme";
    public static final String K_ACCENT = "accent";
    public static final String K_BACKGROUND = "background";
    public static final String K_BUBBLE_IN = "bubble_in";
    public static final String K_BUBBLE_OUT = "bubble_out";
    public static final String K_BUBBLE_IN_TEXT = "bubble_in_text";
    public static final String K_BUBBLE_OUT_TEXT = "bubble_out_text";
    public static final String K_TEXT_PRIMARY = "text_primary";
    public static final String K_TEXT_SECONDARY = "text_secondary";
    public static final String K_CORNER_RADIUS = "corner_radius_dp";
    public static final String K_BUBBLE_SPACING = "bubble_spacing_dp";
    public static final String K_FONT_SIZE = "font_size_sp";
    public static final String K_FONT_PATH = "font_path";
    public static final String K_ROUNDED_AVATARS = "rounded_avatars";
    public static final String K_SHOW_TIMESTAMPS = "show_timestamps";
    public static final String K_SHOW_READ_MARKS = "show_read_marks";
    public static final String K_COMPACT_CHAT_LIST = "compact_chat_list";
    public static final String K_GRADIENT_BUBBLES = "gradient_bubbles";
    public static final String K_BLUR_WALLPAPER = "blur_wallpaper";
    public static final String K_WALLPAPER_DIM = "wallpaper_dim";

    private static final Map<String, Object> DEFAULTS = new LinkedHashMap<String, Object>();

    static {
        DEFAULTS.put(K_THEME, "midnight");
        DEFAULTS.put(K_ACCENT, 0xFF7B5CFF);
        DEFAULTS.put(K_BACKGROUND, 0xFF0E1015);
        DEFAULTS.put(K_BUBBLE_IN, 0xFF1C202B);
        DEFAULTS.put(K_BUBBLE_OUT, 0xFF4A3AA8);
        DEFAULTS.put(K_BUBBLE_IN_TEXT, 0xFFF2F4FF);
        DEFAULTS.put(K_BUBBLE_OUT_TEXT, 0xFFF7F8FF);
        DEFAULTS.put(K_TEXT_PRIMARY, 0xFFF2F4FF);
        DEFAULTS.put(K_TEXT_SECONDARY, 0xFFA0A6BC);
        DEFAULTS.put(K_CORNER_RADIUS, 14f);
        DEFAULTS.put(K_BUBBLE_SPACING, 2f);
        DEFAULTS.put(K_FONT_SIZE, 15);
        DEFAULTS.put(K_FONT_PATH, "");
        DEFAULTS.put(K_ROUNDED_AVATARS, true);
        DEFAULTS.put(K_SHOW_TIMESTAMPS, true);
        DEFAULTS.put(K_SHOW_READ_MARKS, true);
        DEFAULTS.put(K_COMPACT_CHAT_LIST, false);
        DEFAULTS.put(K_GRADIENT_BUBBLES, true);
        DEFAULTS.put(K_BLUR_WALLPAPER, false);
        DEFAULTS.put(K_WALLPAPER_DIM, 30);
    }

    private final LinkedHashMap<String, Object> values = new LinkedHashMap<String, Object>();
    private final LinkedHashSet<String> dirty = new LinkedHashSet<String>();

    public ModConfig() {
        values.putAll(DEFAULTS);
    }

    /** Создаёт конфиг из карты; все найденные ключи считаются изменёнными. */
    public static ModConfig fromMap(Map<String, Object> map) {
        ModConfig c = new ModConfig();
        if (map != null) {
            for (Map.Entry<String, Object> e : map.entrySet()) c.set(e.getKey(), e.getValue());
        }
        return c;
    }

    public static ModConfig fromJson(String json) {
        return fromMap(Json.object(Json.parse(json)));
    }

    public Map<String, Object> toMap() {
        return new LinkedHashMap<String, Object>(values);
    }

    public String toJson() {
        return Json.stringify(values);
    }

    public boolean has(String key) {
        return values.containsKey(key);
    }

    public Object raw(String key) {
        return values.get(key);
    }

    public Set<String> keys() {
        return Collections.unmodifiableSet(values.keySet());
    }

    public String str(String key, String fallback) {
        Object v = values.get(key);
        return v instanceof String ? (String) v : fallback;
    }

    public int integer(String key, int fallback) {
        return Json.integer(values.get(key), fallback);
    }

    public float flt(String key, float fallback) {
        return Json.flt(values.get(key), fallback);
    }

    public boolean bool(String key, boolean fallback) {
        return Json.bool(values.get(key), fallback);
    }

    /** Число или строка-цвет ('#RRGGBB' / '#AARRGGBB'). */
    public int color(String key, int fallback) {
        Object v = values.get(key);
        if (v instanceof Number) return ((Number) v).intValue();
        if (v instanceof String) return ColorUtil.parse((String) v, fallback);
        return fallback;
    }

    public void set(String key, Object value) {
        if (key == null) return;
        values.put(key, value);
        dirty.add(key);
    }

    /** Записать без пометки "изменено пользователем". */
    public void setSilent(String key, Object value) {
        if (key == null) return;
        values.put(key, value);
    }

    public void remove(String key) {
        values.remove(key);
        dirty.remove(key);
    }

    public boolean isDirty(String key) {
        return dirty.contains(key);
    }

    public Set<String> dirtyKeys() {
        return Collections.unmodifiableSet(dirty);
    }

    public void clearDirty() {
        dirty.clear();
    }

    public ModConfig copy() {
        ModConfig c = new ModConfig();
        c.values.clear();
        c.values.putAll(values);
        c.dirty.addAll(dirty);
        return c;
    }

    /** Приводит значения к безопасным диапазонам. */
    public void sanitize() {
        for (String key : new LinkedHashSet<String>(values.keySet())) {
            if (isColorKey(key)) setSilent(key, color(key, 0xFF000000));
        }
        setSilent(K_CORNER_RADIUS, ColorUtil.clamp(flt(K_CORNER_RADIUS, 14f), 0f, 40f));
        setSilent(K_BUBBLE_SPACING, ColorUtil.clamp(flt(K_BUBBLE_SPACING, 2f), 0f, 16f));
        setSilent(K_FONT_SIZE, ColorUtil.clamp(integer(K_FONT_SIZE, 15), 11, 30));
        setSilent(K_WALLPAPER_DIM, ColorUtil.clamp(integer(K_WALLPAPER_DIM, 30), 0, 100));
        setSilent(K_FONT_PATH, str(K_FONT_PATH, "").trim());
    }

    private static boolean isColorKey(String key) {
        return K_ACCENT.equals(key) || K_BACKGROUND.equals(key)
                || K_BUBBLE_IN.equals(key) || K_BUBBLE_OUT.equals(key)
                || K_BUBBLE_IN_TEXT.equals(key) || K_BUBBLE_OUT_TEXT.equals(key)
                || K_TEXT_PRIMARY.equals(key) || K_TEXT_SECONDARY.equals(key);
    }

    public static boolean isColor(String key) {
        return isColorKey(key) || "status_bar".equals(key)
                || "bubble_out_gradient".equals(key);
    }

    public static boolean isMetric(String key) {
        return K_CORNER_RADIUS.equals(key) || K_BUBBLE_SPACING.equals(key)
                || K_FONT_SIZE.equals(key) || K_WALLPAPER_DIM.equals(key);
    }

    public static boolean isFlag(String key) {
        return K_ROUNDED_AVATARS.equals(key) || K_SHOW_TIMESTAMPS.equals(key)
                || K_SHOW_READ_MARKS.equals(key) || K_COMPACT_CHAT_LIST.equals(key)
                || K_GRADIENT_BUBBLES.equals(key) || K_BLUR_WALLPAPER.equals(key);
    }
}
