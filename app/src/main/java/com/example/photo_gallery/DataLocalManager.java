package com.example.photo_gallery;

import android.content.Context;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DataLocalManager {
    private static DataLocalManager instance;
    private MySharedPreferences mySharedPreferences;

    public static void init(Context context){
        instance = new DataLocalManager();
        instance.mySharedPreferences = new MySharedPreferences(context);
    }

    public static DataLocalManager getInstance(){
        if(instance == null){
            instance = new DataLocalManager();
        }
        return instance;
    }

    public static void setListImg(Set<String> listImg, String PREF_TYPE){
        DataLocalManager.getInstance().mySharedPreferences.deleteList(PREF_TYPE);
        DataLocalManager.getInstance().mySharedPreferences.putStringSet(PREF_TYPE, listImg);

    }
    public static void setListImgByList(List<String> listImg, String PREF_TYPE){
        Set<String> setListImg = new HashSet<>();

        for (String i: listImg) {
            setListImg.add(i);
        }
        DataLocalManager.getInstance().mySharedPreferences.deleteList(PREF_TYPE);

        DataLocalManager.getInstance().mySharedPreferences.putStringSet(PREF_TYPE, setListImg);

    }

    public static List<String> getListImg(String PREF_TYPE){
        Set<String> strJsonArray = DataLocalManager.getInstance().mySharedPreferences.getStringSet(PREF_TYPE);

        List<String> listImg = new ArrayList<>();

        for (String i: strJsonArray) {
            listImg.add(i);
        }


        return listImg;
    }

    public static Set<String> getListSet(String PREF_TYPE){
        Set<String> setImg = DataLocalManager.getInstance().mySharedPreferences.getStringSet(PREF_TYPE);
        return setImg;
    }
}
