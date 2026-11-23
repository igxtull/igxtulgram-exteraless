package com.igxtul.gram.core.features;

import com.igxtul.gram.core.ColorUtil;
import com.igxtul.gram.core.CustomizationTarget;
import com.igxtul.gram.core.ModConfig;
import com.igxtul.gram.core.SimpleFeature;

/** Второй цвет градиента для исходящих пузырей. */
public final class GradientFeature extends SimpleFeature {
    public GradientFeature() {
        super("gradient", "Градиент пузырей", true);
    }

    @Override
    public void apply(ModConfig cfg, CustomizationTarget target) {
        int out = cfg.color(ModConfig.K_BUBBLE_OUT, 0xFF4A3AA8);
        boolean enabled = cfg.bool(ModConfig.K_GRADIENT_BUBBLES, true);
        target.setColor("bubble_out_gradient", enabled ? ColorUtil.lighten(out, 0.18f) : out);
    }
}
