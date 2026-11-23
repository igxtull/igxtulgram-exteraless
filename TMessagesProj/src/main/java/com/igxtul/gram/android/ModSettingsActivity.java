package com.igxtul.gram.android;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.igxtul.gram.core.ColorUtil;
import com.igxtul.gram.core.CustomizationEngine;
import com.igxtul.gram.core.Feature;
import com.igxtul.gram.core.ModConfig;
import com.igxtul.gram.core.Theme;

import java.util.ArrayList;
import java.util.List;

/** Экран настроек мода, собранный кодом — без XML-лейаутов. */
public final class ModSettingsActivity extends Activity {
    private CustomizationEngine engine;
    private AndroidTarget target;
    private LinearLayout root;
    private Spinner themeSpinner;
    private EditText accentInput;
    private EditText jsonInput;
    private final List<String> themeIds = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
        super.onCreate(savedInstanceState);
        engine = IgxtulGram.init(this, null);
        target = new AndroidTarget(this);

        ScrollView scroll = new ScrollView(this);
        root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setTag("igxtul:bg:background");
        int pad = dp(16);
        root.setPadding(pad, pad, pad, pad);

        title("igxtulGram " + IgxtulGram.VERSION);
        caption("Кастомизация Telegram · by igxtul");

        TextView preview = new TextView(this);
        preview.setText("Привет, igxtul!");
        preview.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        preview.setPadding(dp(16), dp(16), dp(16), dp(16));
        preview.setTag("igxtul:bg:bubble_out");
        preview.setBackgroundColor(Color.parseColor("#4A3AA8"));
        root.addView(fullWidth(preview, dp(12)));

        caption("Тема");
        List<String> names = new ArrayList<String>();
        for (Theme t : engine.presets().all()) { themeIds.add(t.id()); names.add(t.name() + " · " + t.author()); }
        themeSpinner = new Spinner(this);
        themeSpinner.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, names));
        String current = engine.theme().id();
        int selected = themeIds.indexOf(current);
        themeSpinner.setSelection(selected < 0 ? 0 : selected);
        themeSpinner.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(android.widget.AdapterView<?> p, View v, int pos, long id) {
                if (pos >= 0 && pos < themeIds.size()) {
                    engine.setTheme(themeIds.get(pos));
                    if (accentInput != null) accentInput.setText(ColorUtil.toHex(engine.effective().color(ModConfig.K_ACCENT, 0)));
                }
            }
            @Override public void onNothingSelected(android.widget.AdapterView<?> p) { }
        });
        root.addView(themeSpinner);

        caption("Цвет акцента");
        LinearLayout colorRow = row();
        accentInput = new EditText(this);
        accentInput.setText(ColorUtil.toHex(engine.effective().color(ModConfig.K_ACCENT, 0)));
        accentInput.setSingleLine(true);
        Button applyColor = new Button(this);
        applyColor.setText("OK");
        applyColor.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                int parsed = ColorUtil.parse(accentInput.getText().toString(), -1);
                if (parsed == -1) Toast.makeText(ModSettingsActivity.this, "Неверный цвет", Toast.LENGTH_SHORT).show();
                else engine.setOption(ModConfig.K_ACCENT, parsed);
            }
        });
        colorRow.addView(weight(accentInput, 1));
        colorRow.addView(applyColor);
        root.addView(colorRow);

        caption("Размеры");
        seek("Радиус пузырей, dp", ModConfig.K_CORNER_RADIUS, 0, 40, 14f, true);
        seek("Размер текста, sp", ModConfig.K_FONT_SIZE, 11, 30, 15f, false);
        seek("Затемнение обоев, %", ModConfig.K_WALLPAPER_DIM, 0, 100, 30f, false);

        caption("Оформление");
        toggle("Круглые аватары", ModConfig.K_ROUNDED_AVATARS, true);
        toggle("Время в сообщениях", ModConfig.K_SHOW_TIMESTAMPS, true);
        toggle("Галочки прочтения", ModConfig.K_SHOW_READ_MARKS, true);
        toggle("Компактный список чатов", ModConfig.K_COMPACT_CHAT_LIST, false);
        toggle("Градиент пузырей", ModConfig.K_GRADIENT_BUBBLES, true);
        toggle("Размытые обои", ModConfig.K_BLUR_WALLPAPER, false);

        caption("Модули");
        for (Feature f : engine.features()) featureToggle(f);

        caption("Настройки JSON");
        jsonInput = new EditText(this);
        jsonInput.setText(engine.exportSettings());
        jsonInput.setMinLines(3);
        jsonInput.setSingleLine(false);
        root.addView(jsonInput);
        LinearLayout io = row();
        button(io, "Экспорт", new View.OnClickListener() {
            @Override public void onClick(View v) { jsonInput.setText(engine.exportSettings()); }
        });
        button(io, "Импорт", new View.OnClickListener() {
            @Override public void onClick(View v) {
                engine.importSettings(jsonInput.getText().toString());
                Toast.makeText(ModSettingsActivity.this, "Настройки импортированы", Toast.LENGTH_SHORT).show();
                recreate();
            }
        });
        button(io, "Сброс", new View.OnClickListener() {
            @Override public void onClick(View v) {
                for (String key : engine.overrides().dirtyKeys()) engine.resetOption(key);
                recreate();
            }
        });
        root.addView(io);

        scroll.addView(root);
        setContentView(scroll);

        engine.attach(target);
        target.setRoot(root);
        } catch (Throwable t) {
            String msg = t.getClass().getName() + ": " + t.getMessage();
            android.widget.TextView err = new android.widget.TextView(this);
            err.setText("igxtulGram error:\n" + msg);
            err.setTextColor(Color.RED);
            err.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
            err.setPadding(dp(16), dp(16), dp(16), dp(16));
            setContentView(err);
        }
    }

    private void title(String text) {
        TextView v = new TextView(this);
        v.setText(text);
        v.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
        v.setTextColor(Color.parseColor("#7B5CFF"));
        root.addView(v);
    }

    private void caption(String text) {
        TextView v = new TextView(this);
        v.setText(text);
        v.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        v.setTextColor(Color.parseColor("#A0A6BC"));
        root.addView(fullWidth(v, dp(12)));
    }

    private void seek(final String label, final String key, final int min, final int max, float def, final boolean floatValue) {
        TextView v = new TextView(this);
        v.setText(label);
        final TextView value = new TextView(this);
        float current = engine.effective().flt(key, def);
        value.setText(String.valueOf(Math.round(current)));
        SeekBar bar = new SeekBar(this);
        bar.setMax(max - min);
        bar.setProgress(Math.round(current) - min);
        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar s, int progress, boolean fromUser) {
                if (!fromUser) return;
                float result = progress + min;
                value.setText(String.valueOf(Math.round(result)));
                engine.setOption(key, floatValue ? Float.valueOf(result) : Integer.valueOf(Math.round(result)));
            }
            @Override public void onStartTrackingTouch(SeekBar s) { }
            @Override public void onStopTrackingTouch(SeekBar s) { }
        });
        LinearLayout head = row();
        head.addView(weight(v, 1));
        head.addView(value);
        root.addView(head);
        root.addView(bar);
    }

    private void toggle(String label, final String key, boolean def) {
        Switch s = new Switch(this);
        s.setText(label);
        s.setChecked(engine.effective().bool(key, def));
        s.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton b, boolean checked) {
                engine.setOption(key, checked);
            }
        });
        root.addView(s);
    }

    private void featureToggle(final Feature f) {
        Switch s = new Switch(this);
        s.setText(f.displayName());
        s.setChecked(engine.isFeatureEnabled(f));
        s.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override public void onCheckedChanged(CompoundButton b, boolean checked) {
                engine.setFeatureEnabled(f.id(), checked);
            }
        });
        root.addView(s);
    }

    private void button(LinearLayout parent, String text, View.OnClickListener listener) {
        Button b = new Button(this);
        b.setText(text);
        b.setOnClickListener(listener);
        parent.addView(weight(b, 1));
    }

    private LinearLayout row() {
        LinearLayout l = new LinearLayout(this);
        l.setOrientation(LinearLayout.HORIZONTAL);
        return l;
    }

    private View fullWidth(View v, int marginBottom) {
        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        p.bottomMargin = marginBottom;
        v.setLayoutParams(p);
        return v;
    }

    private View weight(View v, float w) {
        v.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, w));
        return v;
    }

    private int dp(int value) {
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, value,
                getResources().getDisplayMetrics()));
    }
}
