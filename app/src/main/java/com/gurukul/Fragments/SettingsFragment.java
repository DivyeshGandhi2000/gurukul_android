package com.gurukul.Fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.gurukul.R;

public class SettingsFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        ViewCompat.setOnApplyWindowInsetsListener(view, (v, windowInsets) -> {
            Insets systemBars = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return windowInsets;
        });

        SharedPreferences prefs =
                requireContext().getSharedPreferences("AppPrefs", Context.MODE_PRIVATE);
        RadioGroup templateGroup = view.findViewById(R.id.templateGroup);


        String currentTemplate = prefs.getString("template", "classic");
        switch (currentTemplate) {
            case "modern": templateGroup.check(R.id.radioModern); break;
            case "gold":   templateGroup.check(R.id.radioGold);   break;
            case "blue":   templateGroup.check(R.id.radioBlue);   break;
            default:       templateGroup.check(R.id.radioClassic); break;
        }

        templateGroup.setOnCheckedChangeListener((g, checkedId) -> {
            String choice = "classic";
            if (checkedId == R.id.radioModern)  choice = "modern";
            else if (checkedId == R.id.radioGold)   choice = "gold";
            else if (checkedId == R.id.radioBlue)   choice = "blue";
            prefs.edit().putString("template", choice).apply();
            Toast.makeText(getContext(), "Template Updated", Toast.LENGTH_SHORT).show();
        });

        RadioGroup fontGroup = view.findViewById(R.id.fontGroup);

        String currentFont = prefs.getString("font_family", "default");
        switch (currentFont) {
            case "serif":      fontGroup.check(R.id.fontSerif);      break;
            case "monospace":  fontGroup.check(R.id.fontMonospace);  break;
            case "sans-serif": fontGroup.check(R.id.fontSansSerif);  break;
            default:           fontGroup.check(R.id.fontDefault);    break;
        }

        fontGroup.setOnCheckedChangeListener((g, checkedId) -> {
            String fontChoice = "default";
            if (checkedId == R.id.fontSerif)           fontChoice = "serif";
            else if (checkedId == R.id.fontMonospace)  fontChoice = "monospace";
            else if (checkedId == R.id.fontSansSerif)  fontChoice = "sans-serif";
            prefs.edit().putString("font_family", fontChoice).apply();
            Toast.makeText(getContext(), "Font Updated", Toast.LENGTH_SHORT).show();
        });
        RadioGroup videoColorGroup = view.findViewById(R.id.videoColorGroup);

        String currentVideoColor = prefs.getString("video_color", "blue");
        switch (currentVideoColor) {
            case "brown": videoColorGroup.check(R.id.radioVideoBrown); break;
            case "blue":
            default:      videoColorGroup.check(R.id.radioVideoBlue);  break;
        }

        videoColorGroup.setOnCheckedChangeListener((g, checkedId) -> {
            String colorChoice = "blue";
            if (checkedId == R.id.radioVideoBrown) colorChoice = "brown";
            prefs.edit().putString("video_color", colorChoice).apply();
            Toast.makeText(getContext(), "Video Color Updated", Toast.LENGTH_SHORT).show();
        });

        return view;

    }
}