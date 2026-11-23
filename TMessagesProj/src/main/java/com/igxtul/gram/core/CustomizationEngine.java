package com.igxtul.gram.core;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.igxtul.gram.core.features.ContrastTextFeature;
import com.igxtul.gram.core.features.GradientFeature;
import com.igxtul.gram.core.features.StatusBarFeature;

/**
 * Facade мода: загружает конфиг, собирает стиль, прогоняет фичи
 * и отдаёт готовые значения в CustomizationTarget.
 */
public final class CustomizationEngine {
    private static final String FEATURE_PREFIX = "feature.";

    private final ConfigStore store;
    private final PresetManager presets = new PresetManager();
    private final List<Feature> features = new ArrayList<Feature>();

    private ModConfig user = new ModConfig();
    private Theme theme;
    private CustomizationTarget target;

    public CustomizationEngine(ConfigStore store) {
        this.store = store;
        features.add(new ContrastTextFeature());
        features.add(new StatusBarFeature());
        features.add(new GradientFeature());
        load(snapshotOf(store == null ? null : store.load()));
    }

    private static Map<String, Object> snapshotOf(String json) {
        if (json == null || json.trim().isEmpty()) return new LinkedHashMap<String, Object>();
        try { return Json.object(Json.parse(json)); }
        catch (RuntimeException e) { return new LinkedHashMap<String, Object>(); }
    }

    private void load(Map<String, Object> root) {
        Object customThemes = root.get("custom_themes");
        if (customThemes != null) {
            String dump = customThemes instanceof String
                    ? (String) customThemes : Json.stringify(customThemes);
            try { presets.importCustom(dump); } catch (RuntimeException ignored) { }
        }
        Object themeObj = root.get("theme_json");
        if (themeObj instanceof String) {
            try {
                Theme loaded = Theme.fromJson((String) themeObj);
                presets.add(loaded);
                theme = loaded;
            } catch (RuntimeException ignored) { }
        }
        if (theme == null) {
            String id = Json.str(root.get("theme"), "midnight");
            theme = presets.get(id);
            if (theme == null) theme = presets.get("midnight");
        }
        Object overrides = root.get("overrides");
        Map<String, Object> overrideMap;
        if (overrides instanceof String) {
            try { overrideMap = Json.object(Json.parse((String) overrides)); }
            catch (RuntimeException e) { overrideMap = new LinkedHashMap<String, Object>(); }
        } else {
            overrideMap = Json.object(overrides);
        }
        user = ModConfig.fromMap(overrideMap);
    }

    public void attach(CustomizationTarget t) {
        this.target = t;
        apply();
    }

    public void detach() { this.target = null; }

    /** Пересчитать и применить всё. Вызывать после любого изменения. */
    public void apply() {
        ModConfig effective = effective();
        if (target != null) {
            for (String key : effective.keys()) {
                if (ModConfig.isColor(key)) {
                    target.setColor(key, effective.color(key, 0xFF000000));
                } else if (ModConfig.isMetric(key)) {
                    target.setMetric(key, effective.flt(key, 0f));
                } else if (ModConfig.isFlag(key) || key.startsWith(FEATURE_PREFIX)) {
                    target.setFlag(key, effective.bool(key, false));
                } else if (ModConfig.K_FONT_PATH.equals(key)) {
                    target.setFont(effective.str(key, ""));
                }
            }
            for (Feature f : features) {
                if (isFeatureEnabled(f)) {
                    try { f.apply(effective, target); } catch (RuntimeException ignored) { }
                }
            }
            target.refresh();
        }
        persist();
    }

    /** Итоговый стиль без побочных эффектов. */
    public ModConfig effective() {
        return ThemeResolver.resolve(theme, user);
    }

    public void setOption(String key, Object value) {
        user.set(key, value);
        apply();
    }

    public void setOptions(Map<String, Object> values) {
        if (values == null) return;
        for (Map.Entry<String, Object> e : values.entrySet()) user.set(e.getKey(), e.getValue());
        apply();
    }

    public void resetOption(String key) {
        user.remove(key);
        apply();
    }

    public boolean setTheme(String id) {
        Theme next = presets.get(id);
        if (next == null) return false;
        theme = next;
        user.remove(ModConfig.K_THEME);
        apply();
        return true;
    }

    public Theme theme() { return theme; }
    public PresetManager presets() { return presets; }
    public ModConfig overrides() { return user.copy(); }
    public List<Feature> features() { return new ArrayList<Feature>(features); }

    public void registerFeature(Feature f) {
        if (f != null && !features.contains(f)) features.add(f);
    }

    public void setFeatureEnabled(String id, boolean enabled) {
        user.set(FEATURE_PREFIX + id, enabled);
        apply();
    }

    public boolean isFeatureEnabled(Feature f) {
        String key = FEATURE_PREFIX + f.id();
        return user.has(key) ? user.bool(key, f.enabledByDefault()) : f.enabledByDefault();
    }

    public void saveCustomTheme(Theme t) {
        if (t == null) return;
        presets.add(t);
        theme = t;
        apply();
    }

    /** Полный экспорт настроек: темы + правки + состояние фич. */
    public String exportSettings() { return persistString(); }

    public void importSettings(String json) {
        if (json == null || json.trim().isEmpty()) return;
        try {
            load(Json.object(Json.parse(json)));
            apply();
        } catch (RuntimeException ignored) { }
    }

    private void persist() {
        if (store != null) store.save(persistString());
    }

    private String persistString() {
        Map<String, Object> root = new LinkedHashMap<String, Object>();
        root.put("version", 1);
        root.put("app", "igxtulGram");
        root.put("theme", theme == null ? "midnight" : theme.id());
        root.put("theme_json", Json.stringify(theme.toMap()));
        root.put("custom_themes", presets.exportCustom());
        root.put("overrides", Json.stringify(user.toMap()));
        return Json.stringify(root);
    }
}
