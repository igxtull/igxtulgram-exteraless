package com.igxtul.gram.core.features;

import com.igxtul.gram.core.ColorUtil;
import com.igxtul.gram.core.CustomizationTarget;
import com.igxtul.gram.core.ModConfig;
import com.igxtul.gram.core.SimpleFeature;

/** Автоматически подбирает читаемый цвет текста внутри пузырей. */
public final class ContrastTextFeature extends SimpleFeature {
    public ContrastTextFeature() {
        super("contrast_text", "Контрастный текст в пузырях", true);
    }

    @Override
    public void apply(ModConfig cfg, CustomizationTarget target) {
        int in = cfg.color(ModConfig.K_BUBBLE_IN, 0xFF1C202B);
        int out = cfg.color(ModConfig.K_BUBBLE_OUT, 0xFF4A3AA8);
        target.setColor(ModConfig.K_BUBBLE_IN_TEXT, ColorUtil.contrastText(in));
        target.setColor(ModConfig.K_BUBBLE_OUT_TEXT, ColorUtil.contrastText(out));
    }
}
