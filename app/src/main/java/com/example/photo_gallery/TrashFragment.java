
package com.example.photo_gallery;

import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


public class TrashFragment extends Fragment {
    String PREF_TYPE = "PREF_IMG_TRASH";
    private RecyclerView recyclerView;

    private List<String> imageListPath;

    private List<Image> imageList;
    private androidx.appcompat.widget.Toolbar toolbar_trash;
    private Context context;
    MySharedPreferences pref;

    // LE
    private final Handler handler = new Handler(Looper.getMainLooper());

    private static final long DELETE_INTERVAL = 24 * 60 * 60 * 1000L; // 24 hours in milliseconds
    //private static final long DELETE_INTERVAL = 60 * 1000L; // 2 minutes in milliseconds (just for test)
    private final Runnable deleteOldImagesRunnable = new Runnable() {
        @Override
        public void run() {
            deleteOldImages();
            // Schedule the next execution immediately
            handler.post(this);
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_trash, container, false);
        handler.post(deleteOldImagesRunnable);
        context = view.getContext();
        DataLocalManager.init(context.getApplicationContext());//LE
        recyclerView = view.findViewById(R.id.trash_category);
        toolbar_trash = view.findViewById(R.id.toolbar_trash);
        toolbar_trash.inflateMenu(R.menu.menu_top_trash);

        imageListPath = DataLocalManager.getListImg(PREF_TYPE);

        pref = new MySharedPreferences(context);
        toolbar_trash.setTitle("Trash");
        toolbar_trash.setTitleTextColor(0xFFFFFFFF);
        int mauchude = Integer.parseInt(pref.updateMeUsingSavedStateData("mauchude"));
        toolbar_trash.setBackgroundColor(mauchude);
        toolBarEvents();
        setRyc();
        return view;
    }

    private void toolBarEvents() {
        toolbar_trash.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                switch (id) {
                    case R.id.restoreAll:
                        showConfirmationDialog("Restore All", "Are you sure you want to restore all items?", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                restore(imageListPath);
                            }
                        });
                        break;
                    case R.id.emptyTrash:
                        showConfirmationDialog("Empty Trash", "Are you sure you want to empty the trash?", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                emptyTrash();
                            }
                        });
                        break;

                }
                GetAllPhotoFromGallery.updateNewImages();
                GetAllPhotoFromGallery.refreshAllImages();
                return true;
            }
        });
    }

    private void emptyTrash() {
        for (String imagePath : imageListPath) {
            File file = new File(imagePath);
            if (file.exists())
                file.delete();
        }
        imageListPath.clear();
        recyclerView.setAdapter(new ItemAlbumAdapter(new ArrayList<>(imageListPath), true));
        DataLocalManager.setListImgByList(imageListPath, PREF_TYPE);
    }

    private void restore(List<String> imageListPath) {
        List<Uri> urisToRemove = imageListPath.parallelStream()
                .map(imagePath -> {
                    Uri imageUri = Uri.fromFile(new File(imagePath));

                    // Get the source file from the image path
                    File sourceFile = new File(imagePath);

                    // Specify the destination folder relative to the "Trash" directory
                    File destinationFolder = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "");
                    File destinationFile = new File(destinationFolder, sourceFile.getName());

                    // Move the file to the destination folder if it exists
                    if (sourceFile.exists() && sourceFile.renameTo(destinationFile)) {
                        return imageUri;
                    } else {
                        return null; // Indicate that the file was not successfully moved
                    }
                })
                .filter(uri -> uri != null) // Filter out null URIs (files not successfully moved)
                .collect(Collectors.toList());

        // Remove URIs from imageListPath
        imageListPath.removeAll(urisToRemove.stream().map(Uri::getPath).collect(Collectors.toList()));

        // Update UI and allImages if needed
        recyclerView.setAdapter(new ItemAlbumAdapter(new ArrayList<>(this.imageListPath), true));
        GetAllimagefromGallery.updateAllImagesFromUris(this.context, urisToRemove, true);
        GetAllimagefromGallery.refreshAllImages();
        DataLocalManager.setListImgByList(imageListPath, PREF_TYPE);
    }

    private void setRyc() {
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 3));
        recyclerView.setAdapter(new ItemAlbumAdapter(new ArrayList<>(imageListPath), true));

    }

    @Override
    public void onResume() {
        super.onResume();
        deleteOldImages();
        imageListPath = DataLocalManager.getListImg(PREF_TYPE);
        updateImageListPath();
        DataLocalManager.setListImgByList(imageListPath, PREF_TYPE);
        recyclerView.setAdapter(new ItemAlbumAdapter(new ArrayList<>(imageListPath), true));
        //FavoriteFragment.MyAsyncTask myAsyncTask = new FavoriteFragment.MyAsyncTask();
        //myAsyncTask.execute();
    }

    //NOW
    void updateImageListPath() {
        for (int i = 0; i < imageListPath.size(); i++) {
            File file = new File(imageListPath.get(i));
            if (!file.canRead()) {
                imageListPath.remove(i);
            }
        }
    }

    // Inside TrashFragment class
    private void deleteOldImages() {
        File trashDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Trash");
        imageListPath = DataLocalManager.getListImg(PREF_TYPE);
        int oldLen = imageListPath.toArray().length;
        for (String imagePath : imageListPath) {
            File file = new File(imagePath);
            String[] fileNameParts = file.getName().split("_");
            if (fileNameParts.length > 1) {
                try {
                    long moveTimestamp = Long.parseLong(fileNameParts[0]);
                    if (System.currentTimeMillis() - moveTimestamp > DELETE_INTERVAL) {
                        //file.delete(); // Delete the file
                        //imageListPath.remove(file.getPath());
                    }
                } catch (NumberFormatException e) {
                    // Handle the case where the timestamp cannot be parsed
                    e.printStackTrace();
                }
            }
        }
        int newLen = imageListPath.toArray().length;
        boolean isChanged = oldLen != newLen;
        if (isChanged) {
            recyclerView.setAdapter(new ItemAlbumAdapter(new ArrayList<>(imageListPath), true));
            DataLocalManager.setListImgByList(imageListPath, PREF_TYPE);
        }
    }


    private List<Image> getListImgTrash(List<String> imageListUri) {
        List<Image> listImageTrash = new ArrayList<>();
        List<Image> imageList = GetAllimagefromGallery.getAllImageFromGallery(context);
        for (int i = 0; i < imageList.size(); i++) {
            for (String st : imageListUri) {
                if (imageList.get(i).getPath().equals(st)) {
                    listImageTrash.add(imageList.get(i));
                }
            }
        }

        return listImageTrash;
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
            recyclerView.setAdapter(new ItemAlbumAdapter(new ArrayList<>(imageListPath), true));
        }

    }

    private void showConfirmationDialog(String title, String message, DialogInterface.OnClickListener positiveClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(title)
                .setMessage(message)
                .setPositiveButton("Yes", positiveClickListener)
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing or add any additional logic when "No" is clicked
                        dialog.dismiss();
                    }
                })
                .show();
    }
}

