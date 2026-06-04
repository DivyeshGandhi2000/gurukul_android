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

public class VideoColorActivity extends BaseActivity {

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_color);
        View fakeStatusBar = findViewById(R.id.fakeStatusBar);
        Utils.setupFakeStatusBar(this, fakeStatusBar, R.color.primary);
        prefs = this.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);

        // 1. Initialize Cards
        MaterialCardView cardVideoBlue = findViewById(R.id.cardVideoBlue);
        MaterialCardView cardVideoBrown = findViewById(R.id.cardVideoBrown);

        // 2. Initialize RadioButtons
        RadioButton radioVideoBlue = findViewById(R.id.radioVideoBlue);
        RadioButton radioVideoBrown = findViewById(R.id.radioVideoBrown);

        // Group into arrays
        MaterialCardView[] cards = {cardVideoBlue, cardVideoBrown};
        RadioButton[] radios = {radioVideoBlue, radioVideoBrown};
        String[] colorKeys = {"blue", "brown"};

        // 3. Load the saved video color on startup
        String currentColor = prefs.getString("video_color", "blue");

        // Uncheck all first
        for (RadioButton r : radios) {
            r.setChecked(false);
        }

        // Check the previously saved one
        if (currentColor.equals("brown")) {
            radioVideoBrown.setChecked(true);
        } else {
            radioVideoBlue.setChecked(true);
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

                // Save the choice to SharedPreferences
                prefs.edit().putString("video_color", colorKeys[index]).apply();

                // Show visual feedback
                Toast.makeText(VideoColorActivity.this, "Video Color Updated", Toast.LENGTH_SHORT).show();
            });
        }
    }
}