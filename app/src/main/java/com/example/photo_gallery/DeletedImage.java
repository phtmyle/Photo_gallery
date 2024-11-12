package com.example.photo_gallery;

import static android.app.PendingIntent.getActivity;

import android.Manifest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.app.RecoverableSecurityException;
import android.app.WallpaperManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.navigation.NavigationBarView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import android.content.Context;

public class DeletedImage extends AppCompatActivity implements PictureInterface {

    private static final String PREF_TYPE = "PREF_IMG_TRASH";
    String PREF_IMG_TRASH = "PREF_IMG_TRASH";
    //LE
    private static final String DCIM_BUCKET_NAME = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).toString();
    private static final String DCIM_BUCKET_ID = String.valueOf(DCIM_BUCKET_NAME.toLowerCase().hashCode());

    private static final int REQUEST_PERMISSION_CODE = 123;
    private ViewPager viewPager_picture;
    private Toolbar toolbar_picture;
    private BottomNavigationView bottomNavigationView;
    private FrameLayout frame_viewPager;
    private ArrayList<String> imageListThumb;
    private ArrayList<String> imageListPath;
    private Intent intent, intentsetting;
    private int pos;
    private SlideImageAdapter slideImageAdapter;
    private PictureInterface activityPicture;
    private String imgPath;
    private String imageName;
    private String thumb;
    private String title, link, displayedLink, snippet;
    private RecyclerView resultsRV;
    private BottomSheetDialog bottomSheetDialog;
    RelativeLayout ln4;
    MySharedPreferences pref;
    public static Set<String> imageListTrash; //LE

    @Override
    protected void onResume() {
        super.onResume();
        imageListTrash = DataLocalManager.getListSet("PREF_IMG_TRASH"); // LE
    }

    @Override
    public void actionShow(boolean flag) {
        showNavigation(flag);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture_deleted);
        //Fix Uri file SDK link: https://stackoverflow.com/questions/48117511/exposed-beyond-app-through-clipdata-item-geturi?answertab=oldest#tab-top
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        ln4 = findViewById(R.id.trash_layout);
        bottomNavigationView = findViewById(R.id.bottom_picture_deleted);

        pref = new MySharedPreferences(this);


        DataLocalManager.init(getApplicationContext()); // LE
        imageListTrash = DataLocalManager.getListSet(PREF_IMG_TRASH);

        mappingControls();
        toolbar_picture.setNavigationIcon(R.drawable.ic_back); //LE

        toolbar_picture.setTitleTextColor(0xFFFFFFFF);
        int mauchude = Integer.parseInt(pref.updateMeUsingSavedStateData("mauchude"));
        toolbar_picture.setBackgroundColor(mauchude);
        boolean darkmode = Boolean.parseBoolean(pref.updateMeUsingSavedStateData("darkmode"));
        if (darkmode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        bottomNavigationView.setBackgroundColor(mauchude);
        events();
    }

    private void events() {
        setDataIntent();
        setUpSilder();
        bottomNavigationViewEvents(); //LE
    }


    private void showNavigation(boolean flag) {
        if (!flag) {
            bottomNavigationView.setVisibility(View.INVISIBLE);
            toolbar_picture.setVisibility(View.INVISIBLE);
        } else {
            bottomNavigationView.setVisibility(View.VISIBLE);
            toolbar_picture.setVisibility(View.VISIBLE);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private void setWallpaper(final String imagePath) {
        new AsyncTask<Void, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Void... params) {
                // Decode the image from the file path with reduced size
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 2;  // Adjust this value based on your requirements

                // Attempt to reuse the Bitmap
                options.inMutable = true;
                Bitmap reusableBitmap = BitmapFactory.decodeFile(imagePath, options);

                if (reusableBitmap == null) {
                    options.inMutable = false; // Create a new Bitmap if reusing fails
                    return BitmapFactory.decodeFile(imagePath, options);
                } else {
                    return reusableBitmap;
                }
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                if (bitmap != null) {
                    WallpaperManager wallpaperManager = WallpaperManager.getInstance(getApplicationContext());
                    try {
                        // Set the image as the wallpaper
                        wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_SYSTEM);
                        Toast.makeText(getApplicationContext(), "Wallpaper set successfully", Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Failed to set wallpaper", Toast.LENGTH_SHORT).show();
                    } finally {
                        // Ensure that you recycle the bitmap to free up memory
                        bitmap.recycle();
                    }
                }
            }
        }.execute();
    }


    @SuppressLint("StaticFieldLeak")
    private void setLockScreenWallpaper(final String imagePath) {
        new AsyncTask<Void, Void, Bitmap>() {
            @Override
            protected Bitmap doInBackground(Void... params) {
                // Decode the image from the file path with reduced size
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inSampleSize = 2;  // Adjust this value based on your requirements

                // Attempt to reuse the Bitmap
                options.inMutable = true;
                Bitmap reusableBitmap = BitmapFactory.decodeFile(imagePath, options);

                if (reusableBitmap == null) {
                    options.inMutable = false; // Create a new Bitmap if reusing fails
                    return BitmapFactory.decodeFile(imagePath, options);
                } else {
                    return reusableBitmap;
                }
            }

            @Override
            protected void onPostExecute(Bitmap bitmap) {
                if (bitmap != null) {
                    WallpaperManager wallpaperManager = WallpaperManager.getInstance(getApplicationContext());
                    try {
                        // Set the image as the lock screen wallpaper
                        wallpaperManager.setBitmap(bitmap, null, true, WallpaperManager.FLAG_LOCK);
                        Toast.makeText(getApplicationContext(), "Lock screen wallpaper set successfully", Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(getApplicationContext(), "Failed to set lock screen wallpaper", Toast.LENGTH_SHORT).show();
                    } finally {
                        // Ensure that you recycle the bitmap to free up memory
                        bitmap.recycle();
                    }
                }
            }
        }.execute();
    }

    private void setUpSilder() {

        slideImageAdapter = new SlideImageAdapter();
        slideImageAdapter.setData(imageListThumb, imageListPath);
        slideImageAdapter.setContext(getApplicationContext());
        slideImageAdapter.setPictureInterface(activityPicture);
        viewPager_picture.setAdapter(slideImageAdapter);
        viewPager_picture.setCurrentItem(pos);

        viewPager_picture.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                thumb = imageListThumb.get(position);
                imgPath = imageListPath.get(position);
                setTitleToolbar(thumb.substring(thumb.lastIndexOf('/') + 1));

            }

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

    }

    private void setDataIntent() {
        intent = getIntent();
        imageListPath = intent.getStringArrayListExtra("data_list_path");
        imageListThumb = intent.getStringArrayListExtra("data_list_thumb");
        pos = intent.getIntExtra("pos", 0);
        activityPicture = (PictureInterface) this;
    }

    private void mappingControls() {
        viewPager_picture = findViewById(R.id.viewPager_picture_deleted);
        toolbar_picture = findViewById(R.id.toolbar_picture_deleted);
        frame_viewPager = findViewById(R.id.frame_viewPager_deleted);
    }

    public void setTitleToolbar(String imageName) {
        this.imageName = imageName;
        toolbar_picture.setTitle(imageName);
    }

    public void showDialog(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setTitle(title);
        builder.setMessage(message);

        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                // Do nothing but close the dialog

                dialog.dismiss();
            }
        });

        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                // Do nothing
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    private void bottomNavigationViewEvents() {
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()) {
                    case R.id.restore:
                        restore(Collections.singletonList(imgPath));
                        finish();
                        break;

                    case R.id.delete:
                        AlertDialog.Builder builder = new AlertDialog.Builder(DeletedImage.this);
                        builder.setTitle("Delete Image");
                        builder.setMessage("Are you sure you want to delete this image?");

                        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                File file = new File(imgPath);
                                if (file.exists()) {
                                    file.delete();
                                }
                                finish();
                            }
                        });

                        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Do nothing
                                dialog.dismiss();
                            }
                        });

                        AlertDialog alert = builder.create();
                        alert.show();
                        break;
                }
                return true;
            }
        });
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
        GetAllimagefromGallery.updateAllImagesFromUris(DeletedImage.this, urisToRemove, true);
        GetAllimagefromGallery.refreshAllImages();
        DataLocalManager.setListImgByList(imageListPath, PREF_TYPE);
    }

}