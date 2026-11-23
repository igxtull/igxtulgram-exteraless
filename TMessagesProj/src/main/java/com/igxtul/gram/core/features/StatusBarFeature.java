package com.igxtul.gram.core.features;

import com.igxtul.gram.core.ColorUtil;
import com.igxtul.gram.core.CustomizationTarget;
import com.igxtul.gram.core.ModConfig;
import com.igxtul.gram.core.SimpleFeature;

/** Цвет статус-бара выводится из акцента темы. */
public final class StatusBarFeature extends SimpleFeature {
    public StatusBarFeature() {
        super("status_bar", "Окрашивать статус-бар", true);
    }

    @Override
    public void apply(ModConfig cfg, CustomizationTarget target) {
        int accent = cfg.color(ModConfig.K_ACCENT, 0xFF7B5CFF);
        target.setColor("status_bar", ColorUtil.darken(accent, 0.28f));
    }
}
