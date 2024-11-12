package com.example.photo_gallery;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;

public class SettingsFragment extends PreferenceFragment {
    public SwitchPreference switchNight;
    private Preference colorPreference;
    public MySharedPreferences prefs;
    int mauchude=0xFF420606;
    boolean darkmode=false;
    int color[]={
            0xFF4C0202,0xFF7512A6,0xFFED5B9D,0xFF4EB10F,
            0xFFD86A18, 0xFFF44336,0xFF3567F1,0xFFE3CD5A
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.setting);
        prefs=new MySharedPreferences(getContext());
        mauchude= Integer.parseInt(prefs.updateMeUsingSavedStateData("mauchude"));
        darkmode=Boolean.parseBoolean(prefs.updateMeUsingSavedStateData("darkmode"));
        switchNight = (SwitchPreference) findPreference("nightMode");
        switchNight.setChecked(darkmode);
        switchNight.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                boolean checked = ((SwitchPreference) preference)
                        .isChecked();
                darkmode=checked;
                if(checked){
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                }
                else{
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                }
                return true;
            }
        });
        colorPreference = findPreference("themeColor");
        colorPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                showColorPickerDialog();
                return true;
            }
        });
    }

    private void showColorPickerDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Chọn màu chủ đề");
        // Tạo danh sách màu chủ đề từ array mauchude
        String[] colors = getResources().getStringArray(R.array.mauchude);

        builder.setItems(colors, new DialogInterface.OnClickListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mauchude=color[which];
                SettingsActivity settingsActivity= (SettingsActivity) getActivity();
                settingsActivity.changemauchude(color[which]);
            }
        });

        builder.create().show();
    }
    @Override
    public void onResume() {
        super.onResume();
        switchNight = (SwitchPreference) findPreference("nightMode");
        boolean nightMode = Boolean.parseBoolean(prefs.updateMeUsingSavedStateData("darkmode"));
        if (nightMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        prefs.saveStateData(mauchude,darkmode);
    }
}
