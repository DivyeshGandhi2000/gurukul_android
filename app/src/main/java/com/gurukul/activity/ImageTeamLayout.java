package com.gurukul.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.gurukul.BaseActivity;
import com.gurukul.R;
import com.gurukul.Utils.ThemeApplier;
import com.gurukul.Utils.Utils;
import com.gurukul.db.ThemeDatabaseHelper;
import com.gurukul.model.ThemeConfig;

import java.util.List;

public class ImageTeamLayout extends BaseActivity {

    private SharedPreferences prefs;
    private RadioButton[] radioButtons;   // parallel to themeList
    private List<ThemeConfig> themeList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_theam);
        View fakeStatusBar = findViewById(R.id.fakeStatusBar);
        Utils.setupFakeStatusBar(this, fakeStatusBar, R.color.primary);
        prefs = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);

        themeList = ThemeDatabaseHelper.getInstance(this).getAllThemes();
        radioButtons = new RadioButton[themeList.size()];
        FloatingActionButton btnAdd = findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(v -> {
            Intent intent = new Intent(ImageTeamLayout.this, AddThemeActivity.class);
            startActivity(intent);
        });
        loadThemes();

    }

    private void loadThemes() {
        RadioGroup radioGroup = findViewById(R.id.templateGroup);
        radioGroup.removeAllViews();

        String current = prefs.getString("template", "classic");

        for (int i = 0; i < themeList.size(); i++) {
            ThemeConfig theme = themeList.get(i);
            View card = buildThemeCard(theme, i, current.equals(theme.getThemeKey()));
            radioGroup.addView(card);
        }
    }

    /** Programmatically builds a card identical in style to the XML cards */
    private View buildThemeCard(ThemeConfig theme, int index, boolean isSelected) {
        // Root MaterialCardView
        MaterialCardView card = new MaterialCardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        cardParams.bottomMargin = dpToPx(16);
        card.setLayoutParams(cardParams);
        card.setRadius(dpToPx(20));
        card.setCardElevation(dpToPx(2));
        card.setCardBackgroundColor(Color.WHITE);
        card.setClickable(true);
        card.setFocusable(true);

        // Inner ConstraintLayout
        ConstraintLayout inner = new ConstraintLayout(this);
        inner.setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16));
        card.addView(inner, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        // Circle icon card
        MaterialCardView iconCard = new MaterialCardView(this);
        iconCard.setId(View.generateViewId());
        ConstraintLayout.LayoutParams iconParams = new ConstraintLayout.LayoutParams(dpToPx(56), dpToPx(56));
        iconCard.setLayoutParams(iconParams);
        iconCard.setRadius(dpToPx(28));
        iconCard.setCardElevation(0);
        try { iconCard.setCardBackgroundColor(Color.parseColor(theme.getIconBg())); }
        catch (Exception e) { iconCard.setCardBackgroundColor(Color.LTGRAY); }
        TextView iconTv = new TextView(this);
        iconTv.setLayoutParams(new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        iconTv.setGravity(android.view.Gravity.CENTER);
        iconTv.setText(theme.getIconText());
        iconTv.setTextSize(22);
        iconTv.setTypeface(null, android.graphics.Typeface.BOLD);
        try { iconTv.setTextColor(Color.parseColor(theme.getIconTextColor())); }
        catch (Exception e) { iconTv.setTextColor(Color.DKGRAY); }
        iconCard.addView(iconTv);
        inner.addView(iconCard);

        // Eye preview icon
        ImageView eyeIcon = new ImageView(this);
        eyeIcon.setId(View.generateViewId());
        eyeIcon.setImageResource(R.drawable.outline_eye_tracking_24);
        androidx.core.widget.ImageViewCompat.setImageTintList(eyeIcon,
                android.content.res.ColorStateList.valueOf(Color.parseColor("#9E9E9E")));
        eyeIcon.setPadding(dpToPx(6), dpToPx(6), dpToPx(6), dpToPx(6));
        eyeIcon.setClickable(true);
        eyeIcon.setFocusable(true);
        eyeIcon.setBackground(obtainStyledAttributes(
                new int[]{android.R.attr.selectableItemBackgroundBorderless})
                .getDrawable(0));
        ConstraintLayout.LayoutParams eyeParams = new ConstraintLayout.LayoutParams(dpToPx(36), dpToPx(36));
        eyeParams.setMarginEnd(dpToPx(8));
        eyeIcon.setLayoutParams(eyeParams);
        inner.addView(eyeIcon);

        // RadioButton
        // RadioButton
        // RadioButton
        RadioButton radio = new RadioButton(this);
        radio.setId(View.generateViewId());
        radio.setClickable(false);
        radio.setChecked(isSelected);

        // Apply primary color tint programmatically for checked state
        int primaryColor;
        try {
            primaryColor = getResources().getColor(R.color.primary, null);
        } catch (Exception e) {
            primaryColor = Color.parseColor("#6200EE"); // fallback color just in case
        }

        android.content.res.ColorStateList colorStateList = new android.content.res.ColorStateList(
                new int[][]{
                        new int[]{-android.R.attr.state_checked}, // Unchecked state
                        new int[]{android.R.attr.state_checked}   // Checked state
                },
                new int[]{
                        Color.parseColor("#9E9E9E"), // Gray when unchecked
                        primaryColor
                }
        );
        radio.setButtonTintList(colorStateList);

        ConstraintLayout.LayoutParams radioParams = new ConstraintLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        radio.setLayoutParams(radioParams);
        inner.addView(radio);
        radioButtons[index] = radio;

        // Name TextView
        TextView tvName = new TextView(this);
        tvName.setId(View.generateViewId());
        tvName.setText(theme.getDisplayName());
        tvName.setTextSize(18);
        tvName.setTypeface(null, android.graphics.Typeface.BOLD);
        try { tvName.setTextColor(getResources().getColor(R.color.primary, null)); }
        catch (Exception e) { tvName.setTextColor(Color.BLACK); }
        tvName.setMaxLines(1);
        tvName.setEllipsize(android.text.TextUtils.TruncateAt.END);
        inner.addView(tvName);

        // Description TextView
        TextView tvDesc = new TextView(this);
        tvDesc.setId(View.generateViewId());
        tvDesc.setText(theme.isBuiltIn() ? "Built-in · tap to select" : "Custom theme · tap to select");
        tvDesc.setTextSize(13);
        tvDesc.setTextColor(Color.parseColor("#757575"));
        tvDesc.setMaxLines(1);
        tvDesc.setEllipsize(android.text.TextUtils.TruncateAt.END);
        inner.addView(tvDesc);

        // ── ConstraintSet ────────────────────────────────────────────────────
        ConstraintSet cs = new ConstraintSet();
        cs.clone(inner);

        // iconCard: left | vertically centered
        cs.connect(iconCard.getId(), ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START);
        cs.connect(iconCard.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
        cs.connect(iconCard.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);

        // radio: right | vertically centered
        cs.connect(radio.getId(), ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END);
        cs.connect(radio.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
        cs.connect(radio.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);

        // eyeIcon: left of radio | vertically centered
        cs.connect(eyeIcon.getId(), ConstraintSet.END, radio.getId(), ConstraintSet.START);
        cs.connect(eyeIcon.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP);
        cs.connect(eyeIcon.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);

        // tvName: right of icon, left of eye, top aligned with icon top
        cs.connect(tvName.getId(), ConstraintSet.START, iconCard.getId(), ConstraintSet.END, dpToPx(16));
        cs.connect(tvName.getId(), ConstraintSet.END, eyeIcon.getId(), ConstraintSet.START, dpToPx(12));
        cs.connect(tvName.getId(), ConstraintSet.TOP, iconCard.getId(), ConstraintSet.TOP);
        cs.constrainWidth(tvName.getId(), ConstraintSet.MATCH_CONSTRAINT);

        // tvDesc: same horizontal as tvName, below tvName
        cs.connect(tvDesc.getId(), ConstraintSet.START, tvName.getId(), ConstraintSet.START);
        cs.connect(tvDesc.getId(), ConstraintSet.END, tvName.getId(), ConstraintSet.END);
        cs.connect(tvDesc.getId(), ConstraintSet.TOP, tvName.getId(), ConstraintSet.BOTTOM);
        cs.connect(tvDesc.getId(), ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM);
        cs.constrainWidth(tvDesc.getId(), ConstraintSet.MATCH_CONSTRAINT);

        cs.applyTo(inner);

        // ── Click listeners ──────────────────────────────────────────────────
        card.setOnClickListener(v -> selectTheme(theme.getThemeKey(), index));

        eyeIcon.setOnClickListener(v ->
                showPreviewBottomSheet(theme.getDisplayName(), theme, index));

        return card;
    }

    private void selectTheme(String themeKey, int selectedIndex) {
        for (RadioButton rb : radioButtons) rb.setChecked(false);
        radioButtons[selectedIndex].setChecked(true);
        prefs.edit().putString("template", themeKey).apply();
    }
    @Override
    protected void onResume() {
        super.onResume();
        loadThemes();
    }
    private void showPreviewBottomSheet(String titleText, ThemeConfig theme, int index) {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View sheetView = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_preview, null);
        dialog.setContentView(sheetView);

        TextView tvTitle = sheetView.findViewById(R.id.tvPreviewTitle);
        TextView btnSelect = sheetView.findViewById(R.id.btnSelectTheme);
        FrameLayout container = sheetView.findViewById(R.id.previewContainer);

        tvTitle.setText(titleText);

        // Inflate the correct layout for this theme
        int layoutRes = ThemeApplier.getLayoutResId(this, theme.getThemeKey());
        View xmlView = LayoutInflater.from(this).inflate(layoutRes, container, false);
        populatePreviewData(xmlView, theme);
        container.addView(xmlView);

        btnSelect.setOnClickListener(v -> {
            selectTheme(theme.getThemeKey(), index);
            dialog.dismiss();
        });

        dialog.show();
    }

    /** Preview uses dummy data — same ThemeApplier call */
    private void populatePreviewData(View view, ThemeConfig theme) {
        ThemeApplier.applyToStatusView(this, view, theme);

        TextView tvName = view.findViewById(R.id.name);
        TextView tvDesc = view.findViewById(R.id.discription);
        TextView tvDate = view.findViewById(R.id.date);
        TextView tvType = view.findViewById(R.id.type);
        ImageView ivImage = view.findViewById(R.id.image);

        if (tvName != null)  tvName.setText("Name");
        if (tvDesc != null)  tvDesc.setText("Description");
        if (tvDate != null)  tvDate.setText("Date");
        if (tvType != null)  { tvType.setText("Type"); tvType.setVisibility(View.VISIBLE); }
        if (ivImage != null) {
            ivImage.setImageResource(R.drawable.img);
            ivImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
        }
    }

    private int dpToPx(int dp) {
        return Math.round(dp * getResources().getDisplayMetrics().density);
    }
}