package com.gurukul.Utils;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.gurukul.R;
import com.gurukul.model.ThemeConfig;

public class ThemeApplier {

    /**
     * Resolves a drawable resource name → resource id.
     * Returns 0 if name is null/empty so callers can skip setBackgroundResource.
     */
    public static int drawableId(Context ctx, String name) {
        if (name == null || name.isEmpty()) return 0;
        return ctx.getResources().getIdentifier(name, "drawable", ctx.getPackageName());
    }

    /**
     * Core method: applies a ThemeConfig to an inflated status card view.
     * Themes with dedicated XML layouts (blue/green/orange/yellow) already carry
     * their colors, so we only need to handle watermark + the "classic layout" themes.
     */
    public static void applyToStatusView(Context ctx, View view, ThemeConfig theme) {
        if (theme == null) return;

        ImageView watermark = view.findViewById(R.id.background_watermark);

        // Themes that use their own fully-colored XML (no Java color overrides needed)
        boolean isXmlColored = isXmlColoredTheme(theme.getThemeKey());

        if (isXmlColored) {
            // Just control the watermark
            applyWatermark(ctx, watermark, theme);
            return;
        }

        // For classic / modern / gold and any future "classic-base" theme:
        ScrollView rootScroll = view.findViewById(R.id.rootScroll);
        LinearLayout headerCard = view.findViewById(R.id.headerCard);
        LinearLayout dateBar = view.findViewById(R.id.dateBar);
        LinearLayout footerCard = view.findViewById(R.id.footerCard);
        LinearLayout isVisibleName = view.findViewById(R.id.isVisibleName);
        TextView tvName = view.findViewById(R.id.name);
        TextView tvType = view.findViewById(R.id.type);
        TextView tvDesc = view.findViewById(R.id.discription);

        // Background
        if (rootScroll != null) {
            if (theme.getBgColor() != null)
                rootScroll.setBackgroundColor(Color.parseColor(theme.getBgColor()));
        }

        // Header
        if (headerCard != null) {
            int hId = drawableId(ctx, theme.getHeaderDrawable());
            if (hId != 0) headerCard.setBackgroundResource(hId);
            else if (theme.getBgColor() != null)
                headerCard.setBackgroundColor(Color.parseColor(theme.getBgColor()));
        }

        // Footer
        if (footerCard != null) {
            int fId = drawableId(ctx, theme.getFooterDrawable());
            if (fId != 0) footerCard.setBackgroundResource(fId);
            else if (theme.getPrimaryColor() != null)
                footerCard.setBackgroundColor(Color.parseColor(theme.getPrimaryColor()));
        }

        // Date bar
        if (dateBar != null) {
            int dId = drawableId(ctx, theme.getDateBarDrawable());
            if (dId != 0) dateBar.setBackgroundResource(dId);
            else if (theme.getDateBarColor() != null)
                dateBar.setBackgroundColor(Color.parseColor(theme.getDateBarColor()));
        }

        // isVisibleName strip
        if (isVisibleName != null && theme.getBgColor() != null)
            isVisibleName.setBackgroundColor(Color.parseColor(theme.getBgColor()));

        // Text colors
        if (tvName != null && theme.getPrimaryColor() != null)
            tvName.setTextColor(Color.parseColor(theme.getPrimaryColor()));
        if (tvType != null && theme.getSecondaryColor() != null)
            tvType.setTextColor(Color.parseColor(theme.getSecondaryColor()));
        if (tvDesc != null && theme.getPrimaryColor() != null)
            tvDesc.setTextColor(Color.parseColor(theme.getPrimaryColor()));

        applyWatermark(ctx, watermark, theme);
    }

    private static void applyWatermark(Context ctx, ImageView watermark, ThemeConfig theme) {
        if (watermark == null) return;
        watermark.setImageResource(R.drawable.img);
        watermark.setVisibility(View.VISIBLE);
        watermark.setAlpha(theme.getWatermarkAlpha());
    }

    /**
     * Themes whose XML layout already has all colors baked in
     */
    public static boolean isXmlColoredTheme(String key) {
        return key != null && (key.equals("blue") || key.equals("green")
                || key.equals("orange") || key.equals("yellow"));
    }

    /**
     * Returns the correct layout resource id for a given theme
     */
    public static int getLayoutResId(Context ctx, String themeKey) {
        switch (themeKey == null ? "" : themeKey) {
            case "blue":
                return R.layout.item_statusnew_xml;
            case "green":
                return R.layout.item_status_green;
            case "orange":
                return R.layout.item_status_orange;
            case "yellow":
                return R.layout.item_status_yellow;
            default:
                return R.layout.item_status_xml;   // classic/modern/gold/…
        }
    }
}