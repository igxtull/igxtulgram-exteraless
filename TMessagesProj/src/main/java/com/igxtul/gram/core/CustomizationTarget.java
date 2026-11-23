package com.igxtul.gram.core;

/**
 * Контракт между ядром мода и UI Telegram.
 * Форк Telegram реализует его один раз; ядро только вычисляет значения.
 */
public interface CustomizationTarget {
    void setColor(String key, int argb);
    void setMetric(String key, float value);
    void setFlag(String key, boolean value);
    void setFont(String path);
    void refresh();
}
