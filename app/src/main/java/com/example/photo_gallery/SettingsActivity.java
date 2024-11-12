package com.example.photo_gallery;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

public class SettingsActivity extends AppCompatActivity {
    private Toolbar toolbar_settings;
    MySharedPreferences pref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pref=new MySharedPreferences(this);
        setContentView(R.layout.activity_settings2);
        setContentView(R.layout.activity_settings2);
        toolbarEvents();
        getFragmentManager().beginTransaction()
                .replace(R.id.settingsFragment, new SettingsFragment())
                .commit();
    }

    @Override
    protected void onStart() {
        super.onStart();
        int mauchude= Integer.parseInt(pref.updateMeUsingSavedStateData("mauchude"));
        changemauchude(mauchude);
    }

    private void toolbarEvents(){
        toolbar_settings = (Toolbar)findViewById(R.id.toolbar_settings);
        toolbar_settings.setNavigationIcon(R.drawable.ic_back);
        toolbar_settings.setTitle(getBaseContext().getResources().getString(R.string.settings));
        toolbar_settings.setTitleTextColor(0xFFFFFFFF);
        toolbar_settings.inflateMenu(R.menu.menu_top_settings);
        toolbar_settings.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    public void changemauchude(int mauchude){
        toolbar_settings.setBackgroundColor(mauchude);
    }
}