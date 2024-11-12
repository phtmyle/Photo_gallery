package com.example.photo_gallery;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

public class MySharedPreferences {
    private static final String MY_SHARED_PREFERENCES = "MY_SHARED_PREFERENCES";
    private Context context;

    public MySharedPreferences(Context context) {
        this.context = context;
    }

    public Set<String> getStringSet(String key) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(MY_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        return sharedPreferences.getStringSet(key, new HashSet<String>());
    }

    //LE
    //public void deleteListFavor(String key) {
    //    SharedPreferences settings = context.getSharedPreferences(MY_SHARED_PREFERENCES, Context.MODE_PRIVATE);
    //    settings.edit().remove("PREF_IMG_FAVOR").commit();
    //}

    public void deleteList(String key) {
        SharedPreferences settings = context.getSharedPreferences(MY_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        settings.edit().remove(key).commit();
    }

    public void putStringSet(String key, Set value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(MY_SHARED_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet(key, value);
        editor.apply();
    }

    void saveStateData(int mauchude, boolean darkmode) {
        SharedPreferences myPrefContainer = context.getSharedPreferences("setting", Activity.MODE_PRIVATE);
        SharedPreferences.Editor myPrefEditor = myPrefContainer.edit();
        String key1 = "mauchude", val1 = String.valueOf(mauchude);
        String key2 = "darkmode", val2 = String.valueOf(darkmode);
        myPrefEditor.putString(key1, val1);
        myPrefEditor.putString(key2, val2);
        myPrefEditor.commit();
    }

    public String updateMeUsingSavedStateData(String key) {
        SharedPreferences myPrefContainer = context.getSharedPreferences("setting", Activity.MODE_PRIVATE);
        int mauchude = 0xFF420606;
        boolean darkmode = false;
        String defaultValue = "";
        if (key.contains("mauchude")) {
            defaultValue = String.valueOf(mauchude);
        } else if (key.contains("darkmode")) {
            defaultValue = String.valueOf(darkmode);
        }
        if ((myPrefContainer != null) && myPrefContainer.contains(key)) {
            String result = myPrefContainer.getString(key, defaultValue);
            return result;
        }
        return defaultValue;
    }
}
