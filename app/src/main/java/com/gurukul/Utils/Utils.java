package com.gurukul.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Insets;
import android.os.Build;
import android.view.View;

import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

public class Utils {
    public static Context appContext;
    private static final String PREF_NAME = "AppPrefs";
    public static int getIntegerSharedPreferences(Context context, String name) {
        SharedPreferences settings = context
                .getSharedPreferences(PREF_NAME, 0);
        return settings.getInt(name, 1);
    }
    public static void setIntegerSharedPreference(Context context, String name, int value) {
        appContext = context;
        SharedPreferences settings = context.getSharedPreferences(PREF_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putInt(name, value);
        editor.commit();
    }

    public static boolean getSharedPreferencesBoolean(Context context, String name) {
        SharedPreferences settings = context.getSharedPreferences(PREF_NAME, 0);
        return settings.getBoolean(name, false);
    }
    public static String getSharedPreferences(Context context, String name) {
        SharedPreferences settings = context
                .getSharedPreferences(PREF_NAME, 0);
        return settings.getString(name, "");
    }
    public static String getSharedPreferencesLanguage(Context context, String name, String defaultValue) {
        SharedPreferences settings = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return settings.getString(name, defaultValue);
    }
    public static void setSharedPreference(Context context, String name, String value) {
        appContext = context;
        SharedPreferences settings = context.getSharedPreferences(PREF_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(name, value);
        editor.commit();
    }
    public static void setSharedPreferenceBoolean(Context context, String name, boolean value) {
        appContext = context;
        SharedPreferences settings = context.getSharedPreferences(PREF_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.putBoolean(name, value);
        editor.commit();
    }
    public static void setupFakeStatusBar(Activity activity, View fakeStatusBarView, int colorResId) {
        if (Build.VERSION.SDK_INT >= 35) {
            fakeStatusBarView.setVisibility(View.VISIBLE);
            fakeStatusBarView.setOnApplyWindowInsetsListener((v, insets) -> {
                Insets statusBars = insets.getInsets(WindowInsetsCompat.Type.statusBars());
                v.getLayoutParams().height = statusBars.top;
                v.requestLayout();
                return insets;
            });

            WindowCompat.setDecorFitsSystemWindows(activity.getWindow(), false);
            activity.getWindow().setStatusBarColor(activity.getResources().getColor(colorResId, activity.getTheme()));
            WindowInsetsControllerCompat insetsController =
                    new WindowInsetsControllerCompat(activity.getWindow(), activity.getWindow().getDecorView());
            insetsController.setAppearanceLightStatusBars(false);
        }
    }

}
