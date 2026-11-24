package com.igxtul.gram.android;

import android.content.Context;
import android.content.SharedPreferences;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.MessagesController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.SharedConfig;
import org.telegram.messenger.UserConfig;
import org.telegram.ui.ActionBar.MessageDrawable;
import org.telegram.ui.ActionBar.Theme;

/**
 * Настройки igxtulGram, привязанные к реальным механизмам Telegram.
 *
 * Раньше цвета шли через IgxtulGram.tintBubble() -> Drawable.setColorFilter(),
 * но MessageDrawable.setColorFilter() в Telegram — пустая заглушка, поэтому
 * цвета никогда не применялись. Здесь используется public-поле overrideColor,
 * которое MessageDrawable.readColor/Settings учитывает в setTop().
 */
public final class IgxtulPrefs {
    public static final String PREFS = "igxtulgram_settings";

    public static final int DEFAULT_COLOR = 0; // 0 = цвет темы Telegram
    public static final int DEFAULT_RADIUS = 17;
    public static final int DEFAULT_FONT = 16;

    private static SharedPreferences p() {
        return ApplicationLoader.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static int bubbleIn() { return p().getInt("in", DEFAULT_COLOR); }
    public static int bubbleOut() { return p().getInt("out", DEFAULT_COLOR); }
    public static int radius() { return p().getInt("radius", DEFAULT_RADIUS); }
    public static int fontSize() { return p().getInt("font", DEFAULT_FONT); }

    public static void setBubble(boolean out, int color) {
        p().edit().putInt(out ? "out" : "in", color).apply();
    }

    public static void setRadius(int value) {
        p().edit().putInt("radius", clamp(value, 0, 24)).apply();
    }

    public static void setFontSize(int value) {
        p().edit().putInt("font", clamp(value, 12, 26)).apply();
    }

    public static void reset() {
        p().edit().clear().apply();
    }

    private static int clamp(int v, int min, int max) {
        return v < min ? min : v > max ? max : v;
    }

    /** Вызывать из ChatMessageCell.drawBackground() ДО вызова setTop(). */
    public static void applyBubble(MessageDrawable drawable, boolean out) {
        if (drawable == null) return;
        int c = out ? bubbleOut() : bubbleIn();
        drawable.overrideColor = c == 0 ? null : Integer.valueOf(c);
    }

    /**
     * Применить глобальные настройки: радиус пузырей и размер текста.
     * Пишем сразу в main-настройки Telegram, откуда SharedConfig их читает.
     */
    public static void applyAll() {
        try {
            int radius = radius();
            int font = fontSize();
            SharedPreferences prefs = MessagesController.getGlobalMainSettings();
            prefs.edit()
                    .putInt("bubbleRadius", radius)
                    .putInt("fons_size", font)
                    .putInt("iv_font_size", font)
                    .apply();
            SharedConfig.bubbleRadius = radius;
            SharedConfig.fontSize = font;
            SharedConfig.fontSizeIsDefault = false;
            Theme.createChatResources(ApplicationLoader.applicationContext, true);
            NotificationCenter.getInstance(UserConfig.selectedAccount)
                    .postNotificationName(NotificationCenter.updateInterfaces, 0);
        } catch (Throwable ignored) {
        }
    }
}
