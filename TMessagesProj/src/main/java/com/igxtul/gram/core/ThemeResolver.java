package com.igxtul.gram.core;

/** Собирает итоговый стиль: тема + поверх неё ручные правки пользователя. */
public final class ThemeResolver {
    private ThemeResolver() { }

    public static ModConfig resolve(Theme base, ModConfig user) {
        ModConfig result = base == null ? new ModConfig() : base.copy();
        if (user != null) {
            for (String key : user.dirtyKeys()) result.setSilent(key, user.raw(key));
        }
        result.setSilent(ModConfig.K_THEME, base == null ? "custom" : base.id());
        result.sanitize();
        return result;
    }
}
