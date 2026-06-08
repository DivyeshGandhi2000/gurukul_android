package com.gurukul;


import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.gurukul.Utils.Constants;
import com.gurukul.Utils.Utils;

import java.util.Locale;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(() -> {

            boolean isLoggedIn = getSharedPreferences("app_pref", MODE_PRIVATE)
                    .getBoolean("isLoggedIn", false);
            Boolean isLocaleSet;

            try {
                isLocaleSet = Utils.getSharedPreferencesBoolean(getApplicationContext(), "isLocaleSet");
            } catch (NullPointerException e) {
                isLocaleSet = false;
            }

            if(isLocaleSet) {
                setLocale(Utils.getSharedPreferences(getApplicationContext(), Constants.langCode));
            }
            Intent intent;

            if (isLoggedIn) {
                // User is already logged in → move to Main Activity
                intent = new Intent(SplashActivity.this, MainActivity.class);
            } else {
                // User not logged in → move to Login Activity
                intent = new Intent(SplashActivity.this, LoginActivity.class);
            }

            startActivity(intent);
            finish();

        }, 1500);
    }

    public void setLocale(String localeName) {
        Locale myLocale = new Locale(localeName);
        Locale.setDefault(myLocale);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
        Log.e("Status", "Locale updated!");
    }
}

