package com.igxtul.gram.android;

import android.content.Context;
import android.content.Intent;

import com.igxtul.gram.core.CustomizationTarget;
import com.igxtul.gram.core.CustomizationEngine;

/**
 * Точка входа мода. Вызывать из Application/LaunchActivity форка Telegram.
 */
public final class IgxtulGram {
    public static final String VERSION = "1.0.0";
    public static final String BRAND = "igxtulGram";

    private static CustomizationEngine engine;
    private static CustomizationTarget activeTarget;

    private IgxtulGram() { }

    public static synchronized CustomizationEngine init(Context context, CustomizationTarget target) {
        if (engine == null) engine = new CustomizationEngine(new AndroidConfigStore(context));
        if (target != null) {
            activeTarget = target;
            engine.attach(target);
        }
        return engine;
    }

    public static CustomizationEngine get() { return engine; }

    public static void openSettings(Context context) {
        context.startActivity(new Intent(context, ModSettingsActivity.class));
    }

    /** Вызвать из ChatMessageCell, чтобы покрасить пузырь сообщения. */
    public static void applyBubble(android.view.View view, boolean outgoing) {
        if (activeTarget instanceof AndroidTarget) ((AndroidTarget) activeTarget).applyBubble(view, outgoing);
    }

    /** Перепривязать рут-вью после создания экрана. */
    public static void setRoot(android.view.View view) {
        if (activeTarget instanceof AndroidTarget) ((AndroidTarget) activeTarget).setRoot(view);
    }

    /** Тонировка пузыря из ChatMessageCell.drawBackground(). */
    public static void tintBubble(android.graphics.drawable.Drawable drawable, boolean outgoing) {
        if (drawable == null || engine == null || !(activeTarget instanceof AndroidTarget)) return;
        AndroidTarget t = (AndroidTarget) activeTarget;
        String key = outgoing ? com.igxtul.gram.core.ModConfig.K_BUBBLE_OUT
                : com.igxtul.gram.core.ModConfig.K_BUBBLE_IN;
        drawable.setColorFilter(t.color(key, 0), android.graphics.PorterDuff.Mode.SRC_IN);
    }
}
