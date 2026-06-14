package com.gurukul.activity;

import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.gurukul.BaseActivity;
import com.gurukul.R;
import com.gurukul.db.ThemeDatabaseHelper;
import com.gurukul.model.ThemeConfig;

import java.util.UUID;

public class AddThemeActivity extends BaseActivity {

    private TextInputEditText etThemeName, etIconText;

    // Store selected hex values
    private String hexBg = "#FFFFFF";
    private String hexPrimary = "#8B0000";
    private String hexSecondary = "#000000";
    private String hexIconBg = "#EEEEEE";
    private String hexIconText = "#424242";

    // Standard Material Color Palette
    private final String[] colors = {
            "#F44336", "#E91E63", "#9C27B0", "#673AB7", "#3F51B5", "#2196F3",
            "#03A9F4", "#00BCD4", "#009688", "#4CAF50", "#8BC34A", "#CDDC39",
            "#FFEB3B", "#FFC107", "#FF9800", "#FF5722", "#795548", "#9E9E9E",
            "#607D8B", "#000000", "#FFFFFF"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_theme);

        etThemeName = findViewById(R.id.etThemeName);
        etIconText = findViewById(R.id.etIconText);

        // Setup color picker rows
        setupColorRow(findViewById(R.id.colorBg), "Background Color", hexBg, color -> hexBg = color);
        setupColorRow(findViewById(R.id.colorPrimary), "Primary Text Color", hexPrimary, color -> hexPrimary = color);
        setupColorRow(findViewById(R.id.colorSecondary), "Secondary Text Color", hexSecondary, color -> hexSecondary = color);
        setupColorRow(findViewById(R.id.colorIconBg), "Icon Background", hexIconBg, color -> hexIconBg = color);
        setupColorRow(findViewById(R.id.colorIconText), "Icon Text Color", hexIconText, color -> hexIconText = color);

        findViewById(R.id.btnSaveTheme).setOnClickListener(v -> saveTheme());
    }

    private void setupColorRow(View row, String label, String defaultColor, ColorSelectionListener listener) {
        TextView tvLabel = row.findViewById(R.id.tvColorLabel);
        View preview = row.findViewById(R.id.viewColorPreview);

        tvLabel.setText(label);
        preview.setBackgroundColor(Color.parseColor(defaultColor));

        row.setOnClickListener(v -> showColorPickerDialog(label, selectedHex -> {
            preview.setBackgroundColor(Color.parseColor(selectedHex));
            listener.onColorSelected(selectedHex);
        }));
    }

    private void showColorPickerDialog(String title, ColorSelectionListener listener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);

        GridLayout gridLayout = new GridLayout(this);
        gridLayout.setColumnCount(5);
        gridLayout.setPadding(32, 32, 32, 32);

        AlertDialog dialog = builder.setView(gridLayout).create();

        for (String hexColor : colors) {
            MaterialCardView colorCard = new MaterialCardView(this);
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.width = dpToPx(50);
            params.height = dpToPx(50);
            params.setMargins(16, 16, 16, 16);
            colorCard.setLayoutParams(params);
            colorCard.setRadius(dpToPx(25));
            colorCard.setCardBackgroundColor(Color.parseColor(hexColor));

            // Add border so white is visible
            colorCard.setStrokeColor(Color.parseColor("#DDDDDD"));
            colorCard.setStrokeWidth(dpToPx(1));

            colorCard.setOnClickListener(v -> {
                listener.onColorSelected(hexColor);
                dialog.dismiss();
            });

            gridLayout.addView(colorCard);
        }

        dialog.show();
    }

    private void saveTheme() {
        String name = etThemeName.getText().toString().trim();
        String icon = etIconText.getText().toString().trim();

        if (name.isEmpty() || icon.isEmpty()) {
            Toast.makeText(this, "Please enter Theme Name and Icon Letters", Toast.LENGTH_SHORT).show();
            return;
        }

        ThemeConfig newTheme = new ThemeConfig();
        // Generate a unique key for the database
        newTheme.setThemeKey("custom_" + UUID.randomUUID().toString().substring(0, 8));
        newTheme.setDisplayName(name);

        // Colors
        newTheme.setBgColor(hexBg);
        newTheme.setPrimaryColor(hexPrimary);
        newTheme.setSecondaryColor(hexSecondary);
        newTheme.setDateBarColor(hexPrimary); // Match date bar to primary color by default
        newTheme.setIconBg(hexIconBg);
        newTheme.setIconText(icon);
        newTheme.setIconTextColor(hexIconText);

        // Use default drawables for custom layouts so it doesn't crash ThemeApplier
        newTheme.setBorderDrawable("rounded_border");
        newTheme.setWatermarkAlpha(0.2f);

        newTheme.setBuiltIn(false); // Flags it as a custom theme
        newTheme.setSortOrder(100); // Push to the bottom of the list

        long result = ThemeDatabaseHelper.getInstance(this).addTheme(newTheme);

        if (result != -1) {
            Toast.makeText(this, "Theme saved successfully!", Toast.LENGTH_SHORT).show();
            finish(); // Returns to ImageTeamLayout which will reload the list
        } else {
            Toast.makeText(this, "Error saving theme.", Toast.LENGTH_SHORT).show();
        }
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }

    interface ColorSelectionListener {
        void onColorSelected(String hexColor);
    }
}