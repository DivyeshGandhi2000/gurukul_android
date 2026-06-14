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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.card.MaterialCardView;
import com.gurukul.BaseActivity;
import com.gurukul.R;
import com.gurukul.Utils.Utils;

public class VideoColorActivity extends BaseActivity {

    private SharedPreferences prefs;
    private RadioButton radioVideoBlue, radioVideoBrown, radioVideoRed, radioVideoGreen;

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
        MaterialCardView cardVideoRed = findViewById(R.id.cardVideoRed);
        MaterialCardView cardVideoGreen = findViewById(R.id.cardVideoGreen);

        // 2. Initialize Radio Buttons
        radioVideoBlue = findViewById(R.id.radioVideoBlue);
        radioVideoBrown = findViewById(R.id.radioVideoBrown);
        radioVideoRed = findViewById(R.id.radioVideoRed);
        radioVideoGreen = findViewById(R.id.radioVideoGreen);

        // 3. Initialize Eye View Icons
        ImageView ivViewBlue = findViewById(R.id.ivViewBlue);
        ImageView ivViewBrown = findViewById(R.id.ivViewBrown);
        ImageView ivViewRed = findViewById(R.id.ivViewRed);
        ImageView ivViewGreen = findViewById(R.id.ivViewGreen);

        // 4. Load current preference
        String currentColor = prefs.getString("video_color", "blue");
        selectColorUI(currentColor);

        // 5. Card Click Listeners (to select the option)
        cardVideoBlue.setOnClickListener(v -> saveColor("blue"));
        cardVideoBrown.setOnClickListener(v -> saveColor("brown"));
        cardVideoRed.setOnClickListener(v -> saveColor("red"));
        cardVideoGreen.setOnClickListener(v -> saveColor("green"));

        // 6. Eye Icon Click Listeners (to view the bottom sheet)
        ivViewBlue.setOnClickListener(v -> showPreviewBottomSheet("Blue Video Format", "blue"));
        ivViewBrown.setOnClickListener(v -> showPreviewBottomSheet("Brown Video Format", "brown"));
        ivViewRed.setOnClickListener(v -> showPreviewBottomSheet("Red Video Format", "red"));
        ivViewGreen.setOnClickListener(v -> showPreviewBottomSheet("Green Video Format", "green"));
    }

    private void saveColor(String colorKey) {
        selectColorUI(colorKey);
        prefs.edit().putString("video_color", colorKey).apply();
        Toast.makeText(this, "Video Format Updated", Toast.LENGTH_SHORT).show();
    }

    private void selectColorUI(String colorKey) {
        radioVideoBlue.setChecked(colorKey.equals("blue"));
        radioVideoBrown.setChecked(colorKey.equals("brown"));
        radioVideoRed.setChecked(colorKey.equals("red"));
        radioVideoGreen.setChecked(colorKey.equals("green"));
    }

    private void showPreviewBottomSheet(String titleText, String colorKey) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View sheetView = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_preview, null);
        dialog.setContentView(sheetView);

        TextView tvTitle = sheetView.findViewById(R.id.tvPreviewTitle);
        TextView btnSelect = sheetView.findViewById(R.id.btnSelectTheme);
        FrameLayout container = sheetView.findViewById(R.id.previewContainer);

        tvTitle.setText(titleText);

        int layoutRes;
        if (colorKey.equals("blue")) {
            layoutRes = R.layout.item_video_new_xml;
        } else {
            layoutRes = R.layout.item_video2_new_xml;
        }

        View xmlView = LayoutInflater.from(this).inflate(layoutRes, container, false);
        populatePreviewData(xmlView);
        container.addView(xmlView);

        btnSelect.setOnClickListener(v -> {
            saveColor(colorKey);
            dialog.dismiss();
        });

        // Trigger the simulation animation once the dialog is shown
        dialog.setOnShowListener(dialogInterface -> startPreviewAnimation(xmlView, colorKey));

        dialog.show();
    }

    private void populatePreviewData(View view) {
        TextView tvName = view.findViewById(R.id.name);
        TextView tvDesc = view.findViewById(R.id.discription);
        TextView tvDate = view.findViewById(R.id.date);
        TextView tvType = view.findViewById(R.id.type);
        ImageView ivImage = view.findViewById(R.id.image);

        if (tvName != null)  tvName.setText("Sample Name");
        if (tvDesc != null)  tvDesc.setText("Sample description text goes here to show spacing.");
        if (tvDate != null)  tvDate.setText("01 जनवरी 2026");
        if (tvType != null)  {
            tvType.setText("Type");
            tvType.setVisibility(View.VISIBLE);
        }
        if (ivImage != null) {
            ivImage.setImageResource(R.drawable.img);
            ivImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }
    }

    /**
     * Simulates the mathematical animations from VideoGenerator using Android UI Animators.
     * This will make the components slide and fade in when the bottom sheet opens.
     */
    private void startPreviewAnimation(View view, String colorKey) {
        ImageView mainImage = view.findViewById(R.id.image);
        TextView tvType = view.findViewById(R.id.type);
        TextView tvName = view.findViewById(R.id.name);
        TextView tvDesc = view.findViewById(R.id.discription);
        LinearLayout dateBar = view.findViewById(R.id.dateBar);
        LinearLayout headerCard = view.findViewById(R.id.headerCard);

        // 1. Reset everything to an invisible/offset state (Start State)
        if (headerCard != null) {
            headerCard.setTranslationY(-200f);
            headerCard.setAlpha(0f);
        }
        if (mainImage != null) {
            mainImage.setScaleX(1.3f);
            mainImage.setScaleY(1.3f);
            mainImage.setAlpha(0f);
        }

        // Define slide offsets based on the theme style
        float textStartX = colorKey.equals("blue") || colorKey.equals("red") ? -150f : 0f;
        float textStartY = colorKey.equals("brown") || colorKey.equals("green") ? 80f : 0f;

        if (tvType != null) { tvType.setTranslationX(textStartX); tvType.setTranslationY(textStartY); tvType.setAlpha(0f); }
        if (tvName != null) { tvName.setTranslationX(textStartX); tvName.setTranslationY(textStartY); tvName.setAlpha(0f); }
        if (tvDesc != null) { tvDesc.setTranslationX(textStartX); tvDesc.setTranslationY(textStartY); tvDesc.setAlpha(0f); }
        if (dateBar != null) { dateBar.setTranslationX(textStartX == 0 ? 0 : 150f); dateBar.setTranslationY(textStartY); dateBar.setAlpha(0f); }

        // 2. Animate them into their final positions (Staggered cascade effect)
        long duration = 600;

        if (headerCard != null) {
            headerCard.animate().translationY(0f).alpha(1f).setDuration(duration).start();
        }
        if (mainImage != null) {
            mainImage.animate().scaleX(1f).scaleY(1f).alpha(1f).setDuration(duration + 200).start();
        }
        if (tvType != null) {
            tvType.animate().translationX(0f).translationY(0f).alpha(1f).setStartDelay(100).setDuration(duration).start();
        }
        if (tvName != null) {
            tvName.animate().translationX(0f).translationY(0f).alpha(1f).setStartDelay(200).setDuration(duration).start();
        }
        if (tvDesc != null) {
            tvDesc.animate().translationX(0f).translationY(0f).alpha(1f).setStartDelay(300).setDuration(duration).start();
        }
        if (dateBar != null) {
            dateBar.animate().translationX(0f).translationY(0f).alpha(1f).setStartDelay(400).setDuration(duration).start();
        }
    }
}