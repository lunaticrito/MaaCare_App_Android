package com.cse2200.maacare.ui;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import com.cse2200.maacare.data.prefs.PreferenceManager;
import com.cse2200.maacare.util.LocaleHelper;

/**
 * Base activity providing uniform localization context injection and light/dark mode enforcement.
 */
public abstract class BaseActivity extends AppCompatActivity {

    protected PreferenceManager preferenceManager;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        preferenceManager = new PreferenceManager(this);
        applyThemeMode();
        super.onCreate(savedInstanceState);
    }

    protected void applyThemeMode() {
        if (preferenceManager.isDarkMode()) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    public void toggleTheme() {
        boolean isDark = preferenceManager.isDarkMode();
        preferenceManager.setDarkMode(!isDark);
        if (!isDark) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        recreate();
    }

    public void toggleLanguage() {
        String current = LocaleHelper.getLanguage(this);
        String next = current.equals(LocaleHelper.LANGUAGE_BANGLA) ? LocaleHelper.LANGUAGE_ENGLISH : LocaleHelper.LANGUAGE_BANGLA;
        preferenceManager.setAppLanguage(next);
        LocaleHelper.setLocale(this, next);
        recreate();
    }
}
