package com.gurukul.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;
import com.gurukul.BaseActivity;
import com.gurukul.R;
import com.gurukul.Utils.Utils;

public class FontGroupLayout extends BaseActivity {

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_font_theam);
        View fakeStatusBar = findViewById(R.id.fakeStatusBar);
        Utils.setupFakeStatusBar(this, fakeStatusBar, R.color.primary);
        prefs = this.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);

        // 1. Initialize all Cards
        MaterialCardView cardDefault = findViewById(R.id.cardDefault);
        MaterialCardView cardSerif = findViewById(R.id.cardSerif);
        MaterialCardView cardMonospace = findViewById(R.id.cardMonospace);
        MaterialCardView cardSansSerif = findViewById(R.id.cardSansSerif);

        // 2. Initialize all RadioButtons
        RadioButton fontDefault = findViewById(R.id.fontDefault);
        RadioButton fontSerif = findViewById(R.id.fontSerif);
        RadioButton fontMonospace = findViewById(R.id.fontMonospace);
        RadioButton fontSansSerif = findViewById(R.id.fontSansSerif);

        // Group them into arrays
        MaterialCardView[] cards = {cardDefault, cardSerif, cardMonospace, cardSansSerif};
        RadioButton[] radios = {fontDefault, fontSerif, fontMonospace, fontSansSerif};
        String[] fontKeys = {"default", "serif", "monospace", "sans-serif"};

        // 3. Load the saved font on startup
        String currentFont = prefs.getString("font_family", "default");

        // Uncheck all first
        for (RadioButton r : radios) {
            r.setChecked(false);
        }

        // Check the previously saved one
        switch (currentFont) {
            case "serif":      fontSerif.setChecked(true);      break;
            case "monospace":  fontMonospace.setChecked(true);  break;
            case "sans-serif": fontSansSerif.setChecked(true);  break;
            default:           fontDefault.setChecked(true);    break;
        }

        // 4. Set Click Listeners on the CARDS
        for (int i = 0; i < cards.length; i++) {
            final int index = i;

            cards[i].setOnClickListener(view -> {

                // Uncheck all radio buttons
                for (RadioButton r : radios) {
                    r.setChecked(false);
                }

                // Check the specific radio button inside the clicked card
                radios[index].setChecked(true);

                // Save the choice
                prefs.edit().putString("font_family", fontKeys[index]).apply();

                // Show the Toast just like you requested!
                Toast.makeText(FontGroupLayout.this, "Font Updated", Toast.LENGTH_SHORT).show();
            });
        }
    }
}