package com.igxtul.gram.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Встроенные пресеты и пользовательские темы. */
public final class PresetManager {
    private final LinkedHashMap<String, Theme> builtin = new LinkedHashMap<String, Theme>();
    private final LinkedHashMap<String, Theme> custom = new LinkedHashMap<String, Theme>();

    public PresetManager() {
        addBuiltin(theme("midnight", "Midnight", "igxtul", "Тёмная база с фиолетовым акцентом",
                "#FF7B5CFF", "#FF0E1015", "#FF1C202B", "#FF4A3AA8",
                "#FFF2F4FF", "#FFA0A6BC", 14f, 15));
        addBuiltin(theme("amoled", "AMOLED", "igxtul", "Чистый чёрный, экономит батарею",
                "#FF00E5FF", "#FF000000", "#FF101014", "#FF08323A",
                "#FFF7F8FF", "#FF8C99A8", 10f, 15));
        addBuiltin(theme("nord", "Nord", "igxtul", "Холодные серо-голубые тона",
                "#FF88C0D0", "#FF2E3440", "#FF3B4252", "#FF5E81AC",
                "#FFECEFF4", "#FFD8DEE9", 12f, 15));
        addBuiltin(theme("paper", "Paper", "igxtul", "Светлая тема для дня",
                "#FF2D7FF9", "#FFF5F6FA", "#FFFFFFFF", "#FFDCEBFF",
                "#FF16202B", "#FF6B7686", 16f, 15));
        addBuiltin(theme("neon", "Neon", "igxtul", "Яркий розовый акцент",
                "#FFFF2E88", "#FF09090F", "#FF17131F", "#FF6A1B4D",
                "#FFFFF4FA", "#FFB9A9C4", 18f, 16));
    }

    private static Theme theme(String id, String name, String author, String description,
                               String accent, String bg, String in, String out,
                               String text, String secondary, float radius, int fontSize) {
        Map<String, Object> m = new LinkedHashMap<String, Object>();
        m.put("id", id);
        m.put("name", name);
        m.put("author", author);
        m.put("description", description);
        m.put(ModConfig.K_ACCENT, accent);
        m.put(ModConfig.K_BACKGROUND, bg);
        m.put(ModConfig.K_BUBBLE_IN, in);
        m.put(ModConfig.K_BUBBLE_OUT, out);
        m.put(ModConfig.K_BUBBLE_IN_TEXT, text);
        m.put(ModConfig.K_BUBBLE_OUT_TEXT, text);
        m.put(ModConfig.K_TEXT_PRIMARY, text);
        m.put(ModConfig.K_TEXT_SECONDARY, secondary);
        m.put(ModConfig.K_CORNER_RADIUS, radius);
        m.put(ModConfig.K_FONT_SIZE, fontSize);
        m.put(ModConfig.K_GRADIENT_BUBBLES, true);
        m.put(ModConfig.K_ROUNDED_AVATARS, true);
        return Theme.fromMap(m);
    }

    private void addBuiltin(Theme t) { builtin.put(t.id(), t); }

    public Collection<Theme> all() {
        List<Theme> list = new ArrayList<Theme>(builtin.values());
        list.addAll(custom.values());
        return list;
    }

    public Collection<Theme> builtin() { return new ArrayList<Theme>(builtin.values()); }
    public Collection<Theme> custom() { return new ArrayList<Theme>(custom.values()); }

    public Theme get(String id) {
        Theme t = custom.get(id);
        return t != null ? t : builtin.get(id);
    }

    public void add(Theme theme) {
        if (theme == null || theme.id() == null) return;
        if (builtin.containsKey(theme.id())) return;
        custom.put(theme.id(), theme);
    }

    public boolean remove(String id) { return custom.remove(id) != null; }

    /** Экспорт пользовательских тем одной JSON-строкой. */
    public String exportCustom() {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        for (Theme t : custom.values()) list.add(t.toMap());
        return Json.stringify(list);
    }

    /** Импорт пользовательских тем. Возвращает количество добавленных. */
    public int importCustom(String json) {
        int count = 0;
        for (Object item : Json.array(Json.parse(json))) {
            Theme t = Theme.fromMap(Json.object(item));
            if (!builtin.containsKey(t.id())) { custom.put(t.id(), t); count++; }
        }
        return count;
    }
}
