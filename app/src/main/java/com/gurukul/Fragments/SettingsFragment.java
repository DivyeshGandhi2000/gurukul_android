package com.gurukul.Fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.LocaleList;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.os.LocaleListCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.gurukul.LoginActivity;
import com.gurukul.R;
import com.gurukul.Utils.Constants;
import com.gurukul.Utils.Utils;
import com.gurukul.activity.FontGroupLayout;
import com.gurukul.activity.ImageTeamLayout;
import com.gurukul.activity.VideoColorActivity;

import java.util.Locale;

public class SettingsFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        View selectTemplateTheme = view.findViewById(R.id.SelectTemplateThime);
        View SelectFontFamily = view.findViewById(R.id.SelectFontFamily);
        View SelectVideoTheme = view.findViewById(R.id.SelectVideoTheme);
        View ChangelanguageBtn = view.findViewById(R.id.ChangelanguageBtn);
        View logoutBtn = view.findViewById(R.id.logoutBtn);

        selectTemplateTheme.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), ImageTeamLayout.class);
            startActivity(intent);
        });
        SelectFontFamily.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), FontGroupLayout.class);
            startActivity(intent);
        });
        SelectVideoTheme.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), VideoColorActivity.class);
            startActivity(intent);
        });
        ChangelanguageBtn.setOnClickListener(v -> changeLanguage());
        logoutBtn.setOnClickListener(v -> LogoutDialog());
        return view;

    }
    private void changeLanguage() {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View dialogView = inflater.inflate(R.layout.dialog_change_language, null);

        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setView(dialogView)
                .setCancelable(true)
                .create();

        TextView titleTv = dialogView.findViewById(R.id.deliteTitleTv);
        TextView buttonCancel = dialogView.findViewById(R.id.button_cancel);
        TextView buttonOk = dialogView.findViewById(R.id.button_ok);
        RadioGroup radioGroupLanguage = dialogView.findViewById(R.id.radioGroupLanguage);

        titleTv.setText(getString(R.string.choose_language));
        int selectedLang = Utils.getIntegerSharedPreferences(getActivity(), Constants.default_code);
        radioGroupLanguage.check(selectedLang == 0 ? R.id.radioHindi : R.id.radioEnglish);

        buttonOk.setOnClickListener(v -> {
            FragmentActivity activity = getActivity();
            if (activity == null) return;

            int checkedId = radioGroupLanguage.getCheckedRadioButtonId();
            String languageCode = (checkedId == R.id.radioHindi) ? "hi" : "en";
            int langCode = (checkedId == R.id.radioHindi) ? 0 : 1;
            Utils.setIntegerSharedPreference(activity, Constants.default_code, langCode);
            Utils.setSharedPreference(activity, "LANG_CHANGED", "yes");
            Utils.setSharedPreference(activity, Constants.langCode, languageCode);
            Utils.setSharedPreferenceBoolean(activity, Constants.isLocaleSet, true);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                LocaleListCompat localeList = LocaleListCompat.forLanguageTags(languageCode);
                AppCompatDelegate.setApplicationLocales(localeList);
            } else {
                setLocaleTraditional(languageCode);
            }

            dialog.dismiss();
        });

        buttonCancel.setOnClickListener(v -> dialog.dismiss());

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            View dialogRootView = dialog.getWindow().getDecorView();
            int paddingInPixels = (int) (10 * getResources().getDisplayMetrics().density);

            dialogRootView.setPadding(paddingInPixels, dialogRootView.getPaddingTop(),
                    paddingInPixels, dialogRootView.getPaddingBottom());
        }
        dialog.show();
    }
    private void LogoutDialog() {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View dialogView = inflater.inflate(R.layout.dialog_logout, null);

        AlertDialog dialog = new AlertDialog.Builder(getActivity())
                .setView(dialogView)
                .setCancelable(true)
                .create();

        // Assuming you have these IDs in your dialog_logout.xml
        TextView titleTv = dialogView.findViewById(R.id.deliteTitleTv);
        TextView buttonCancel = dialogView.findViewById(R.id.button_cancel);
        TextView buttonOk = dialogView.findViewById(R.id.button_ok);

        buttonOk.setOnClickListener(v -> {
            FragmentActivity activity = getActivity();
            if (activity == null) return;

            // 1. Update SharedPreferences to log the user out
            activity.getSharedPreferences("app_pref", Context.MODE_PRIVATE)
                    .edit()
                    .putBoolean("isLoggedIn", false)
                    .apply();

            // 2. Intent to navigate back to LoginActivity
            Intent intent = new Intent(activity, LoginActivity.class);

            // 3. Clear the back stack so the user cannot use the back button to re-enter
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);

            // 4. Dismiss dialog and finish the current hosting activity
            dialog.dismiss();
            activity.finish();
        });

        buttonCancel.setOnClickListener(v -> dialog.dismiss());

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            View dialogRootView = dialog.getWindow().getDecorView();
            int paddingInPixels = (int) (10 * getResources().getDisplayMetrics().density);

            dialogRootView.setPadding(paddingInPixels, dialogRootView.getPaddingTop(),
                    paddingInPixels, dialogRootView.getPaddingBottom());
        }

        dialog.show();
    }

    private void setLocaleTraditional(String languageCode) {
        FragmentActivity activity = getActivity();
        if (activity == null) return;

        try {
            Locale locale = new Locale(languageCode);
            Locale.setDefault(locale);

            Resources resources = activity.getResources();
            Configuration config = new Configuration(resources.getConfiguration());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                config.setLocale(locale);
                config.setLocales(new LocaleList(locale));
            } else {

                config.locale = locale;
            }


            resources.updateConfiguration(config, resources.getDisplayMetrics());

            try {
                Resources appResources = activity.getApplicationContext().getResources();
                Configuration appConfig = new Configuration(appResources.getConfiguration());

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    appConfig.setLocale(locale);
                    appConfig.setLocales(new LocaleList(locale));
                } else {
                    appConfig.locale = locale;
                }

                appResources.updateConfiguration(appConfig, appResources.getDisplayMetrics());
            } catch (Exception e) {
                Log.w("ProfileFragment", "Could not update app context resources: " + e.getMessage());
            }

            Log.d("ProfileFragment", "Locale updated traditionally to: " + languageCode);
            if (!activity.isFinishing() && !activity.isDestroyed()) {
                activity.runOnUiThread(() -> {
                    try {
                        activity.recreate();
                    } catch (Exception e) {
                        Log.e("ProfileFragment", "Error recreating activity: " + e.getMessage());
                    }
                });
            }

        } catch (Exception e) {
            Log.e("ProfileFragment", "Error setting traditional locale: " + e.getMessage());
        }
    }

}