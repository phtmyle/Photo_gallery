package com.example.photo_gallery;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class FavoriteFragment extends Fragment {
    String PREF_TYPE = "PREF_IMG_FAVOR";
    private RecyclerView recyclerView;

    private List<String> imageListPath;

    private List<Image> imageList;
    private androidx.appcompat.widget.Toolbar toolbar_favor;
    private Context context;
    MySharedPreferences pref;
    private Set<String> imgListFavor;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorite, container, false);
        context = view.getContext();
        DataLocalManager.init(context.getApplicationContext());//LE
        recyclerView = view.findViewById(R.id.favor_category);
        toolbar_favor = view.findViewById(R.id.toolbar_favor);

        imageListPath = DataLocalManager.getListImg(PREF_TYPE);
        imgListFavor = DataLocalManager.getListSet(PREF_TYPE);

        //imageList = getListImgFavor(imageListPath);
        pref = new MySharedPreferences(context);
        toolbar_favor.setTitle("Favourite");
        toolbar_favor.setTitleTextColor(0xFFFFFFFF);
        int mauchude = Integer.parseInt(pref.updateMeUsingSavedStateData("mauchude"));
        toolbar_favor.setBackgroundColor(mauchude);
        setRyc();


        return view;
    }

    private void setRyc() {

        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));

        recyclerView.setAdapter(new ItemAlbumAdapter(new ArrayList<>(imageListPath),false));

    }

    @Override
    public void onResume() {
        super.onResume();
        imageListPath = DataLocalManager.getListImg(PREF_TYPE);
        for (int i = 0; i < imageListPath.size(); i++) {
            File file = new File(imageListPath.get(i));
            if (!file.canRead()) {
                imageListPath.remove(i);
            }
        }
//NOW
        DataLocalManager.setListImgByList(imageListPath,PREF_TYPE );
        recyclerView.setAdapter(new ItemAlbumAdapter(new ArrayList<>(imageListPath),false));
        //FavoriteFragment.MyAsyncTask myAsyncTask = new FavoriteFragment.MyAsyncTask();
        //myAsyncTask.execute();
    }

    private List<Image> getListImgFavor(List<String> imageListUri) {
        List<Image> listImageFavor = new ArrayList<>();
        List<Image> imageList = GetAllimagefromGallery.getAllImageFromGallery(context);
        for (int i = 0; i < imageList.size(); i++) {
            for (String st : imageListUri) {
                if (imageList.get(i).getPath().equals(st)) {
                    listImageFavor.add(imageList.get(i));
                }
            }
        }

        return listImageFavor;
    }

    public class MyAsyncTask extends AsyncTask<Void, Integer, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            imageListPath = DataLocalManager.getListImg(PREF_TYPE);
            for (int i = 0; i < imageListPath.size(); i++) {
                File file = new File(imageListPath.get(i));
                if (!file.exists() || !file.canRead()) {
                    imageListPath.remove(i);
                }
            }

            DataLocalManager.setListImgByList(imageListPath, PREF_TYPE);
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            recyclerView.setAdapter(new ItemAlbumAdapter(new ArrayList<>(imageListPath),false));
        }
    }

}
