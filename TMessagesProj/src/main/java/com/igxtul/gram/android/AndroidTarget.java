package com.igxtul.gram.android;

import android.app.Activity;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.igxtul.gram.core.CustomizationTarget;
import com.igxtul.gram.core.ModConfig;

import java.util.HashMap;
import java.util.Map;

/**
 * Реализация CustomizationTarget для Android.
 *
 * Вьюхи в Telegram можно размечать тегами:
 *   igxtul:bg:bubble_out   — фон из ключа bubble_out
 *   igxtul:color:accent    — цвет текста из ключа accent
 *   igxtul:font            — шрифт из настроек
 *   igxtul:text            — размер текста из настроек
 */
public class AndroidTarget implements CustomizationTarget {
    private final Activity activity;
    private final Map<String, Integer> colors = new HashMap<String, Integer>();
    private final Map<String, Float> metrics = new HashMap<String, Float>();
    private final Map<String, Boolean> flags = new HashMap<String, Boolean>();
    private final Map<String, Typeface> typefaces = new HashMap<String, Typeface>();
    private final float density;

    private View root;
    private String fontPath = "";

    public AndroidTarget(Activity activity) {
        this.activity = activity;
        this.density = activity.getResources().getDisplayMetrics().density;
    }

    public void setRoot(View view) {
        this.root = view;
        refresh();
    }

    @Override
    public void setColor(String key, int argb) {
        colors.put(key, argb);
        if (ModConfig.K_BACKGROUND.equals(key) && root != null) root.setBackgroundColor(argb);
        if ("status_bar".equals(key) && Build.VERSION.SDK_INT >= 21 && activity != null) {
            activity.getWindow().setStatusBarColor(argb);
        }
        walk(root);
    }

    @Override
    public void setMetric(String key, float value) { metrics.put(key, value); }

    @Override
    public void setFlag(String key, boolean value) { flags.put(key, value); }

    @Override
    public void setFont(String path) {
        this.fontPath = path;
        walk(root);
    }

    @Override
    public void refresh() {
        walk(root);
        if (root != null) root.invalidate();
    }

    public int color(String key, int fallback) {
        Integer value = colors.get(key);
        return value == null ? fallback : value;
    }

    public boolean flag(String key, boolean fallback) {
        Boolean value = flags.get(key);
        return value == null ? fallback : value;
    }

    public float dp(float value) { return value * density; }

    public float radiusDp() { return metrics.containsKey(ModConfig.K_CORNER_RADIUS) ? metrics.get(ModConfig.K_CORNER_RADIUS) : 14f; }
    public float fontSizeSp() { return metrics.containsKey(ModConfig.K_FONT_SIZE) ? metrics.get(ModConfig.K_FONT_SIZE) : 15f; }

    /** Фон пузыря сообщения. Вызывать из ChatMessageCell форка. */
    public void applyBubble(View view, boolean incoming) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setCornerRadius(dp(radiusDp()));
        int base = incoming
                ? color(ModConfig.K_BUBBLE_IN, 0xFF1C202B)
                : color(ModConfig.K_BUBBLE_OUT, 0xFF4A3AA8);
        if (!incoming && flag(ModConfig.K_GRADIENT_BUBBLES, true)) {
            drawable.setOrientation(GradientDrawable.Orientation.TL_BR);
            drawable.setColors(new int[] { base, color("bubble_out_gradient", base) });
        } else {
            drawable.setColor(base);
        }
        view.setBackground(drawable);
    }

    private void walk(View view) {
        if (view == null) return;
        applyTags(view);
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) walk(group.getChildAt(i));
        }
    }

    private void applyTags(View view) {
        Object tag = view.getTag();
        if (!(tag instanceof String)) return;
        String t = (String) tag;
        String colorPrefix = "igxtul:color:";
        String bgPrefix = "igxtul:bg:";
        if (t.startsWith(colorPrefix)) {
            Integer c = colors.get(t.substring(colorPrefix.length()));
            if (c != null && view instanceof TextView) ((TextView) view).setTextColor(c);
        } else if (t.startsWith(bgPrefix)) {
            Integer c = colors.get(t.substring(bgPrefix.length()));
            if (c != null) view.setBackgroundColor(c);
        } else if ("igxtul:font".equals(t) && view instanceof TextView) {
            ((TextView) view).setTypeface(typeface(fontPath));
        } else if ("igxtul:text".equals(t) && view instanceof TextView) {
            ((TextView) view).setTextSize(TypedValue.COMPLEX_UNIT_SP, fontSizeSp());
        }
    }

    private Typeface typeface(String path) {
        if (path == null || path.trim().isEmpty()) return Typeface.DEFAULT;
        Typeface cached = typefaces.get(path);
        if (cached != null) return cached;
        Typeface loaded = Typeface.createFromAsset(activity.getAssets(), path);
        typefaces.put(path, loaded);
        return loaded;
    }
}
