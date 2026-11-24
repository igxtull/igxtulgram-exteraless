package com.igxtul.gram.android;

import android.view.View;

import com.exteragram.messenger.preferences.BasePreferencesActivity;

import org.telegram.messenger.SharedConfig;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.Components.UItem;
import org.telegram.ui.Components.UniversalAdapter;

import java.util.ArrayList;

/**
 * Нормальный экран настроек igxtulGram в стиле Telegram (UItem + UniversalAdapter).
 * Все пункты реальные: цвета пузырей, радиус, размер текста, компактный список.
 */
public class IgxtulSettingsActivity extends BasePreferencesActivity {

    private static final int BUBBLE_IN = 1;
    private static final int BUBBLE_OUT = 2;
    private static final int RADIUS = 3;
    private static final int FONT_SIZE = 4;
    private static final int COMPACT = 5;
    private static final int APPLY = 6;
    private static final int RESET = 7;

    private static final String[] COLOR_NAMES = {
            "Цвет темы", "Фиолетовый", "Синий", "Бирюзовый", "Зелёный", "Красный", "Тёмно-серый"
    };
    private static final int[] COLOR_VALUES = {
            0, 0xFF4A3AA8, 0xFF2B6FD6, 0xFF12A5A5, 0xFF2E9E5B, 0xFFD3543C, 0xFF2B303B
    };

    @Override
    public String getTitle() {
        return "igxtulGram";
    }

    @Override
    public void fillItems(ArrayList<UItem> items, UniversalAdapter adapter) {
        items.add(UItem.asHeader("Пузыри сообщений"));
        items.add(UItem.asButton(BUBBLE_IN, "Цвет входящих", hex(IgxtulPrefs.bubbleIn())));
        items.add(UItem.asButton(BUBBLE_OUT, "Цвет исходящих", hex(IgxtulPrefs.bubbleOut())));
        items.add(UItem.asIntSlideView(0, 0, IgxtulPrefs.radius(), 24,
                value -> value == 0 ? "По умолчанию" : value + " dp",
                value -> {
                    IgxtulPrefs.setRadius(value);
                    IgxtulPrefs.applyAll();
                }));
        items.add(UItem.asShadow("Цвет «Цвет темы» использует стандартную тему Telegram. Радиус 0 — стандартный."));

        items.add(UItem.asHeader("Текст и чаты"));
        items.add(UItem.asIntSlideView(0, 12, IgxtulPrefs.fontSize(), 26,
                value -> value + " sp",
                value -> {
                    IgxtulPrefs.setFontSize(value);
                    IgxtulPrefs.applyAll();
                }));
        items.add(UItem.asButton(COMPACT, "Компактный список чатов",
                SharedConfig.compactMode ? "Вкл" : "Выкл"));
        items.add(UItem.asShadow("Размер текста и радиус применяются ко всем чатам сразу."));

        items.add(UItem.asHeader("Прочее"));
        items.add(UItem.asButton(APPLY, "Применить сейчас"));
        items.add(UItem.asButton(RESET, "Сбросить настройки"));
        items.add(UItem.asShadow("igxtulGram by igxtul"));
    }

    @Override
    public void onClick(UItem item, View view, int position, float x, float y) {
        if (item == null || item.id <= 0) return;
        switch (item.id) {
            case BUBBLE_IN:
                pickColor(false);
                break;
            case BUBBLE_OUT:
                pickColor(true);
                break;
            case COMPACT:
                SharedConfig.toggleCompactMode();
                item.textValue = SharedConfig.compactMode ? "Вкл" : "Выкл";
                break;
            case APPLY:
                IgxtulPrefs.applyAll();
                break;
            case RESET:
                IgxtulPrefs.reset();
                IgxtulPrefs.applyAll();
                break;
            default:
                break;
        }
    }

    private void pickColor(final boolean out) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getParentActivity());
        builder.setTitle(out ? "Цвет исходящих" : "Цвет входящих");
        builder.setItems(COLOR_NAMES, (dialog, which) -> {
            IgxtulPrefs.setBubble(out, COLOR_VALUES[which]);
            IgxtulPrefs.applyAll();
            if (listView != null && listView.adapter != null) listView.adapter.notifyDataSetChanged();
        });
        builder.setNegativeButton("Отмена", null);
        showDialog(builder.create());
    }

    private static String hex(int color) {
        return color == 0 ? "Цвет темы" : String.format("#%06X", color & 0xFFFFFF);
    }
}
