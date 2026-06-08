package com.gurukul.activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.button.MaterialButton;
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
        MaterialCardView cardGreen = findViewById(R.id.cardGreen);
        MaterialCardView cardOrange = findViewById(R.id.cardOrange);
        MaterialCardView cardYellow = findViewById(R.id.cardYellow);

        RadioButton radioClassic = findViewById(R.id.radioClassic);
        RadioButton radioModern = findViewById(R.id.radioModern);
        RadioButton radioGold = findViewById(R.id.radioGold);
        RadioButton radioBlue = findViewById(R.id.radioBlue);
        RadioButton radioGreen = findViewById(R.id.radioGreen);
        RadioButton radioOrange = findViewById(R.id.radioOrange);
        RadioButton radioYellow = findViewById(R.id.radioYellow);

        // Preview Eye Icons
        ImageView previewClassic = findViewById(R.id.ivPreviewClassic);
        ImageView previewModern = findViewById(R.id.ivPreviewModern);
        ImageView previewGold = findViewById(R.id.ivPreviewGold);
        ImageView previewBlue = findViewById(R.id.ivPreviewBlue);
        ImageView previewGreen = findViewById(R.id.ivPreviewGreen);
        ImageView previewOrange = findViewById(R.id.ivPreviewOrange);
        ImageView previewYellow = findViewById(R.id.ivPreviewYellow);

        MaterialCardView[] cards = {cardClassic, cardModern, cardGold, cardBlue, cardGreen, cardOrange, cardYellow};
        RadioButton[] radios = {radioClassic, radioModern, radioGold, radioBlue, radioGreen, radioOrange, radioYellow};
        ImageView[] previews = {previewClassic, previewModern, previewGold, previewBlue, previewGreen, previewOrange, previewYellow};

        String[] templateKeys = {"classic", "modern", "gold", "blue", "green", "orange", "yellow"};
        String[] displayNames = {"Classic Theme", "Modern Theme", "Gold Theme", "Blue Theme", "Green Theme", "Orange Theme", "Yellow Theme"};

        // 2. Load the saved template on startup
        String currentTemplate = prefs.getString("template", "classic");

        for (RadioButton r : radios) {
            r.setChecked(false);
        }

        switch (currentTemplate) {
            case "modern": radioModern.setChecked(true); break;
            case "gold":   radioGold.setChecked(true);   break;
            case "blue":   radioBlue.setChecked(true);   break;
            case "green":  radioGreen.setChecked(true);  break;
            case "orange": radioOrange.setChecked(true); break;
            case "yellow": radioYellow.setChecked(true); break;
            default:       radioClassic.setChecked(true); break;
        }

        // 3. Set Click Listeners
        for (int i = 0; i < cards.length; i++) {
            final int index = i;

            // Click on the card to select the theme
            cards[i].setOnClickListener(view -> {
                for (RadioButton r : radios) {
                    r.setChecked(false);
                }
                radios[index].setChecked(true);
                prefs.edit().putString("template", templateKeys[index]).apply();
            });

            // Click on the eye icon to open Bottom Sheet
            previews[i].setOnClickListener(view -> {
                showPreviewBottomSheet(displayNames[index], templateKeys[index], index, radios);
            });
        }
    }

    // Opens an attractive bottom sheet preview
    // 1. Updated showPreviewBottomSheet method
    private void showPreviewBottomSheet(String titleText, String templateKey, int index, RadioButton[] radios) {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this); // or your custom style
        View bottomSheetView = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_preview, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        TextView tvPreviewTitle = bottomSheetView.findViewById(R.id.tvPreviewTitle);
        TextView btnSelectTheme = bottomSheetView.findViewById(R.id.btnSelectTheme);
        FrameLayout previewContainer = bottomSheetView.findViewById(R.id.previewContainer);

        tvPreviewTitle.setText(titleText);

        // --- INFLATE THE CORRECT LAYOUT BASED ON THEME ---
        View xmlView;
        if (templateKey.equals("blue")) {
            xmlView = LayoutInflater.from(this).inflate(R.layout.item_statusnew_xml, previewContainer, false);
        } else if (templateKey.equals("green")){
            xmlView = LayoutInflater.from(this).inflate(R.layout.item_status_green, previewContainer, false);
        } else if (templateKey.equals("orange")){
            xmlView = LayoutInflater.from(this).inflate(R.layout.item_status_orange, previewContainer, false);
        } else if (templateKey.equals("yellow")){
            xmlView = LayoutInflater.from(this).inflate(R.layout.item_status_yellow, previewContainer, false);
        } else {
            // Classic, Modern, Gold all use this base layout and change colors dynamically
            xmlView = LayoutInflater.from(this).inflate(R.layout.item_status_xml, previewContainer, false);
        }

        // Apply theme colors and mock data to the layout
        populatePreviewData(xmlView, templateKey);

        // Add the inflated view to the bottom sheet container
        previewContainer.addView(xmlView);

        // Select theme button logic
        btnSelectTheme.setOnClickListener(v -> {
            for (RadioButton r : radios) {
                r.setChecked(false);
            }
            radios[index].setChecked(true);
            prefs.edit().putString("template", templateKey).apply();
            bottomSheetDialog.dismiss();
        });

        bottomSheetDialog.show();
    }

    // 2. Adapted from your populateXmlView, but injects dummy preview data safely
    private void populatePreviewData(View view, String theme) {
        ScrollView rootScroll = view.findViewById(R.id.rootScroll);
        LinearLayout headerCard = view.findViewById(R.id.headerCard);
        LinearLayout dateBar = view.findViewById(R.id.dateBar);
        LinearLayout footerCard = view.findViewById(R.id.footerCard);
        LinearLayout isVisibleName = view.findViewById(R.id.isVisibleName);

        TextView tvName = view.findViewById(R.id.name);
        TextView tvType = view.findViewById(R.id.type);
        TextView tvDate = view.findViewById(R.id.date);
        TextView tvDescription = view.findViewById(R.id.discription);
        ImageView ivImage = view.findViewById(R.id.image);
        ImageView watermark = view.findViewById(R.id.background_watermark);

        // Apply Theme Colors (Same logic as your Adapter)
        if (theme.equals("blue") || theme.equals("green") || theme.equals("orange") || theme.equals("yellow")) {
            if (watermark != null) {
                watermark.setImageResource(R.drawable.img); // Ensure R.drawable.img exists
                watermark.setVisibility(View.VISIBLE);
                watermark.setAlpha(0f);
            }
        } else if (theme.equals("modern")) {
            if (rootScroll != null) rootScroll.setBackgroundColor(android.graphics.Color.parseColor("#0D47A1"));
            if (headerCard != null) headerCard.setBackgroundResource(R.drawable.card_modern);
            if (footerCard != null) footerCard.setBackgroundResource(R.drawable.card_modern);
            if (dateBar != null) dateBar.setBackgroundColor(android.graphics.Color.parseColor("#1565C0"));
            if (isVisibleName != null) isVisibleName.setBackgroundColor(android.graphics.Color.parseColor("#0D47A1"));
            if (tvName != null) tvName.setTextColor(android.graphics.Color.parseColor("#0D47A1"));
            if (tvType != null) tvType.setTextColor(android.graphics.Color.parseColor("#2E7D32"));
            if (tvDescription != null) tvDescription.setTextColor(android.graphics.Color.BLACK);
            if (watermark != null) {
                watermark.setImageResource(R.drawable.img);
                watermark.setVisibility(View.VISIBLE);
                watermark.setAlpha(0.2f);
            }
        } else if (theme.equals("gold")) {
            if (rootScroll != null) rootScroll.setBackgroundColor(android.graphics.Color.parseColor("#121212"));
            if (headerCard != null) headerCard.setBackgroundColor(android.graphics.Color.parseColor("#F5F5F5"));
            if (footerCard != null) footerCard.setBackgroundColor(android.graphics.Color.parseColor("#F5F5F5"));
            if (dateBar != null) dateBar.setBackgroundColor(android.graphics.Color.parseColor("#B8860B"));
            if (isVisibleName != null) isVisibleName.setBackgroundColor(android.graphics.Color.parseColor("#121212"));
            if (tvName != null) tvName.setTextColor(android.graphics.Color.parseColor("#856404"));
            if (tvType != null) tvType.setTextColor(android.graphics.Color.parseColor("#B8860B"));
            if (tvDescription != null) tvDescription.setTextColor(android.graphics.Color.parseColor("#333333"));
            if (watermark != null) {
                watermark.setImageResource(R.drawable.img);
                watermark.setVisibility(View.VISIBLE);
                watermark.setAlpha(0.2f);
            }
        } else {
            // Classic (default)
            if (rootScroll != null) rootScroll.setBackgroundColor(android.graphics.Color.parseColor("#8B0000"));
            if (headerCard != null) headerCard.setBackgroundResource(R.drawable.card_classic);
            if (footerCard != null) footerCard.setBackgroundResource(R.drawable.card_classic);
            if (dateBar != null) dateBar.setBackgroundColor(android.graphics.Color.parseColor("#8B0000"));
            if (isVisibleName != null) isVisibleName.setBackgroundColor(android.graphics.Color.parseColor("#8B0000"));
            if (tvName != null) tvName.setTextColor(android.graphics.Color.parseColor("#8B0000"));
            if (tvType != null) tvType.setTextColor(android.graphics.Color.BLACK);
            if (tvDescription != null) tvDescription.setTextColor(android.graphics.Color.parseColor("#8B0000"));
            if (watermark != null) {
                watermark.setImageResource(R.drawable.img);
                watermark.setVisibility(View.VISIBLE);
                watermark.setAlpha(0.2f);
            }
        }

        // Set Dummy Data for Preview
        if (tvName != null) tvName.setText("Name");
        if (tvDescription != null) tvDescription.setText("Discription");
        if (tvDate != null) tvDate.setText("Date"); // Mock Hindi Date
        if (tvType != null) {
            tvType.setText("Type"); // Mock occasion
            tvType.setVisibility(View.VISIBLE);
        }

        // Use a placeholder image for the preview instead of loading from File
        if (ivImage != null) {
            ivImage.setImageResource(R.drawable.img); // Change this to whatever sample image you have in res/drawable
            ivImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }
    }
}