package com.example.photo_gallery;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ImageViewHolder> {
    private List<Image> listImages;
    private Context context;
    private List<Category> listCategory;
    private Intent intent;
    private ArrayList<String> listPath ;
    private ArrayList<String> listThumb ;
    int mauchude=0xFF420606;
    boolean isdarkmode=false;
    public ImageAdapter(Context context) {
        this.context = context;
    }
    public ImageAdapter() {

    }
    public void setmauchude(int newTheme) {
        // Update the theme and notify data set changed
        this.mauchude = newTheme;
        notifyDataSetChanged();
    }

    public void setDarkMode(boolean isDarkMode) {
        // Update the dark mode setting and notify data set changed
        this.isdarkmode = isDarkMode;
        notifyDataSetChanged();
    }
    public void setListCategory(List<Category> listCategory) {
        this.listCategory = listCategory;
    }

    public void setData(List<Image> listImages) {
        this.listImages = listImages;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_picture, parent, false);

        return new ImageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Image image = listImages.get(position);
        if (image == null) {
            return;
        }

        // set ảnh cho imgPhoto bằng thư viện Glide
        Glide.with(context).load(image.getThump()).into(holder.imgPhoto);

        holder.imgPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                intent = new Intent(context, PictureActivity.class);
                MyAsyncTask myAsyncTask = new MyAsyncTask();
                myAsyncTask.setPos(position);
                myAsyncTask.execute();
            }
        });

    }

    @Override
    public int getItemCount() {
        if (listImages != null)
            return listImages.size();
        return 0;
    }

    public class ImageViewHolder extends RecyclerView.ViewHolder {
        private ImageView imgPhoto;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPhoto = itemView.findViewById(R.id.imgPhoto);
        }
    }


    public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object){

        // Remove view
        container.removeView((View) object);
    }

    public class MyAsyncTask extends AsyncTask<Void, Integer, Void> {
        public int pos;

        public void setPos(int pos) {
            this.pos = pos;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            listPath = new ArrayList<>();
            listThumb = new ArrayList<>();
            for(int i = 0;i<listCategory.size();i++) {
                List<Image> listGirl = listCategory.get(i).getListGirl();
                for (int j = 0; j < listGirl.size(); j++) {
                    listPath.add(listGirl.get(j).getPath());
                    listThumb.add(listGirl.get(j).getThump());
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            intent.putStringArrayListExtra("data_list_path", listPath);
            intent.putStringArrayListExtra("data_list_thumb", listThumb);
            intent.putExtra("mauchude",mauchude);
            intent.putExtra("isdarkmode",isdarkmode);
            intent.putExtra("pos", listPath.indexOf(listImages.get(pos).getPath()));
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }
}
