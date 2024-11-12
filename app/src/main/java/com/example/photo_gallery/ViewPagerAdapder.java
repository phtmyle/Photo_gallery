package com.example.photo_gallery;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.List;

public class ViewPagerAdapder extends FragmentStateAdapter {
    private List<Image> data;
    private Context context;

    public void setContext(Context context) {
        this.context = context;
        data = GetAllimagefromGallery.getAllImageFromGallery(context);
    }

    public ViewPagerAdapder(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
        super(fragmentManager, lifecycle);

    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new PhotoFragment();
            case 1:
                return new AlbumFragment();
            case 2:
                return new SecretFragment();
            case 3:
                return new FavoriteFragment();
            case 4:
                return new TrashFragment();
            default:
                return null;
        }
    }

    //LE
    @Override
    public int getItemCount() {
        return 5;
    }
}
