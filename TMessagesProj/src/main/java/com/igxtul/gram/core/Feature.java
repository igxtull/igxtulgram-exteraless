package com.igxtul.gram.core;

/** Дополнительный модуль кастомизации, который можно включать/выключать. */
public interface Feature {
    String id();
    String displayName();
    boolean enabledByDefault();
    void apply(ModConfig effective, CustomizationTarget target);
}
