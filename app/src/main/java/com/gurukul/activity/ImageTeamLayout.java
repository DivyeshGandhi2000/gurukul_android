package com.gurukul.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioButton;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;
import com.gurukul.BaseActivity;
import com.gurukul.R;
import com.gurukul.Utils.Utils;

public class ImageTeamLayout extends BaseActivity {

    private static final int PERMISSION_REQUEST_CODE = 100;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_theam);
        View fakeStatusBar = findViewById(R.id.fakeStatusBar);
        Utils.setupFakeStatusBar(this, fakeStatusBar, R.color.primary);
        prefs = this.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);

        // 1. Initialize all Cards
        MaterialCardView cardClassic = findViewById(R.id.cardClassic);
        MaterialCardView cardModern = findViewById(R.id.cardModern);
        MaterialCardView cardGold = findViewById(R.id.cardGold);
        MaterialCardView cardBlue = findViewById(R.id.cardBlue);

        // 2. Initialize all RadioButtons
        RadioButton radioClassic = findViewById(R.id.radioClassic);
        RadioButton radioModern = findViewById(R.id.radioModern);
        RadioButton radioGold = findViewById(R.id.radioGold);
        RadioButton radioBlue = findViewById(R.id.radioBlue);

        // Group them into arrays to make the logic much easier
        MaterialCardView[] cards = {cardClassic, cardModern, cardGold, cardBlue};
        RadioButton[] radios = {radioClassic, radioModern, radioGold, radioBlue};
        String[] templateKeys = {"classic", "modern", "gold", "blue"};

        // 3. Load the saved template on startup
        String currentTemplate = prefs.getString("template", "classic");

        // Uncheck everything first
        for (RadioButton r : radios) {
            r.setChecked(false);
        }

        // Check the saved one
        switch (currentTemplate) {
            case "modern": radioModern.setChecked(true); break;
            case "gold":   radioGold.setChecked(true);   break;
            case "blue":   radioBlue.setChecked(true);   break;
            default:       radioClassic.setChecked(true); break;
        }

        // 4. Set Click Listeners on the CARDS
        for (int i = 0; i < cards.length; i++) {
            final int index = i; // Needed for use inside the lambda/click listener

            cards[i].setOnClickListener(view -> {

                // Uncheck all radio buttons
                for (RadioButton r : radios) {
                    r.setChecked(false);
                }

                // Check the specific radio button inside the clicked card
                radios[index].setChecked(true);

                // Save the newly selected template to SharedPreferences immediately
                prefs.edit().putString("template", templateKeys[index]).apply();
            });
        }
    }
}