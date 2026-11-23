package com.igxtul.gram.android;

import android.content.Context;
import android.content.SharedPreferences;

import com.igxtul.gram.core.ConfigStore;

/** Хранит конфиг мода в SharedPreferences приложения. */
public final class AndroidConfigStore implements ConfigStore {
    private static final String PREFS = "igxtul_gram";
    private static final String KEY = "config_v1";

    private final SharedPreferences prefs;

    public AndroidConfigStore(Context context) {
        this.prefs = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    @Override
    public String load() {
        return prefs.getString(KEY, null);
    }

    @Override
    public void save(String json) {
        prefs.edit().putString(KEY, json).apply();
    }
}
