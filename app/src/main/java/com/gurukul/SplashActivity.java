package com.gurukul;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(() -> {

            boolean isLoggedIn = getSharedPreferences("app_pref", MODE_PRIVATE)
                    .getBoolean("isLoggedIn", false);

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
}

