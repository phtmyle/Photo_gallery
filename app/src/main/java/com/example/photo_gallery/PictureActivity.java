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

import android.content.Context;

public class PictureActivity extends AppCompatActivity implements PictureInterface {

    String PREF_IMG_FAVOR = "PREF_IMG_FAVOR";
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
    private Bitmap imageBitmap;
    private String title, link, displayedLink, snippet;
    private RecyclerView resultsRV;
    private BottomSheetDialog bottomSheetDialog;
    private RecyclerView ryc_album;
    final SparseArray<Integer> menuIdTotoolBarPosition = new SparseArray<>();
    private static final int CHI_TIET_ID = 0;
    private static final int HINH_NEN_ID = 1;
    private static final int HINH_CHO_ID = 2;
    private static final int THEM_VAO_ALBUM = 3;
    RelativeLayout ln4;
    MySharedPreferences pref;
    public static Set<String> imageListFavor; //LE
    public static Set<String> imageListTrash; //LE

    @Override
    protected void onResume() {
        super.onResume();
        imageListFavor = DataLocalManager.getListSet("PREF_IMG_FAVOR"); // LE
        imageListTrash = DataLocalManager.getListSet("PREF_IMG_TRASH"); // LE
    }

    @Override
    public void actionShow(boolean flag) {
        showNavigation(flag);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture);
        //Fix Uri file SDK link: https://stackoverflow.com/questions/48117511/exposed-beyond-app-through-clipdata-item-geturi?answertab=oldest#tab-top
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        menuIdTotoolBarPosition.put(R.id.chitiet, CHI_TIET_ID);
        menuIdTotoolBarPosition.put(R.id.hinhnen, HINH_NEN_ID);
        menuIdTotoolBarPosition.put(R.id.hinhcho, HINH_CHO_ID);
        menuIdTotoolBarPosition.put(R.id.addalbum, THEM_VAO_ALBUM);
        ln4 = findViewById(R.id.ln4);
        bottomNavigationView = findViewById(R.id.bottom_picture);
        pref = new MySharedPreferences(this);


        DataLocalManager.init(getApplicationContext()); // LE
        imageListFavor = DataLocalManager.getListSet(PREF_IMG_FAVOR);
        imageListTrash = DataLocalManager.getListSet(PREF_IMG_TRASH);

        mappingControls();
        toolbar_picture.inflateMenu(R.menu.menu_top_picture);
        toolbar_picture.setNavigationIcon(R.drawable.ic_back);
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
        toolBarEvents();
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

    private void toolBarEvents() {
        toolbar_picture.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        toolbar_picture.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                int menuConstant = -1;
                menuConstant = menuIdTotoolBarPosition.get(id);
                if (menuConstant != -1) {
                    switch (menuConstant) {
                        case CHI_TIET_ID:

                            break;
                        case HINH_NEN_ID:
                            setWallpaper(imgPath);
                            break;
                        case HINH_CHO_ID:
                            setLockScreenWallpaper(imgPath);
                            break;
                        case THEM_VAO_ALBUM:

                            break;
                    }
                }
                return true;
            }
        });
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
                if (!check(imgPath)) {
                    bottomNavigationView.getMenu().getItem(2).setIcon(R.drawable.ic_heart);
                } else {
                    bottomNavigationView.getMenu().getItem(2).setIcon(R.drawable.ic_heart_red);
                }
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
        viewPager_picture = findViewById(R.id.viewPager_picture);
        bottomNavigationView.setItemIconTintList(null); //LE: doi mau vector heart
        toolbar_picture = findViewById(R.id.toolbar_picture);
        frame_viewPager = findViewById(R.id.frame_viewPager);
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

    //LE: for share image


    private void bottomNavigationViewEvents() {
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                //Uri targetUri = Uri.parse("file://" + thumb);
                Uri targetUri = Uri.parse("file://" + imgPath);
                switch (item.getItemId()) {

                    // LE
                    case R.id.sharePic: { // LE: OK
                        if (thumb.contains("gif")) {
                            Intent share = new Intent(Intent.ACTION_SEND);
                            share.setType("image/*");
                            share.putExtra(Intent.EXTRA_STREAM, targetUri);
                            startActivity(Intent.createChooser(share, "Share this image to your friends!"));
                        } else {
                            Drawable mDrawable = Drawable.createFromPath(imgPath);
                            String fileName = thumb.substring(thumb.lastIndexOf('/') + 1);
                            File tempFile = createTempImageFile(fileName);

                            if (tempFile != null) {
                                Uri tempUri = FileProvider.getUriForFile(
                                        PictureActivity.this,
                                        PictureActivity.this.getApplicationContext().getPackageName() + ".provider",
                                        tempFile
                                );

                                // Save the image to the temporary file
                                try {
                                    Bitmap mBitmap = ((BitmapDrawable) mDrawable).getBitmap();
                                    mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, getContentResolver().openOutputStream(tempUri));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                // Share the image using the temporary FileProvider URI
                                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                                shareIntent.setType("image/*");
                                shareIntent.putExtra(Intent.EXTRA_STREAM, tempUri);
                                startActivity(Intent.createChooser(shareIntent, "Share Image"));
                            }
                        }
                        break;
                    }

                    // LE
                    case R.id.starPic: { // LE: OK

                        if (!imageListFavor.add(imgPath)) {
                            imageListFavor.remove(imgPath);
                        }

                        DataLocalManager.setListImg(imageListFavor, PREF_IMG_FAVOR);
                        if (!check(imgPath)) {
                            bottomNavigationView.getMenu().getItem(2).setIcon(R.drawable.ic_heart);
                        } else {
                            bottomNavigationView.getMenu().getItem(2).setIcon(R.drawable.ic_heart_red);
                        }
                        break;
                    }

                    case R.id.deletePic: {
                        AlertDialog.Builder builder = new AlertDialog.Builder(PictureActivity.this);

                        builder.setTitle("Confirm");
                        builder.setMessage("Do you want to delete this image?");
                        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
                            @RequiresApi(api = Build.VERSION_CODES.O)
                            public void onClick(DialogInterface dialog, int which) {
                                File file = new File(imgPath);
                                if (file.exists()) {
                                    File trashDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Trash");
                                    if (!trashDir.exists()) {
                                        trashDir.mkdirs();
                                    }

                                    File newFile = new File(trashDir, System.currentTimeMillis() + "_" + file.getName() );

                                    // Move the file to the Trash folder using Files.move
                                    Path sourcePath = file.toPath();
                                    Path destinationPath = newFile.toPath();

                                    try {
                                        Files.move(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);

                                        // If moving is successful, add the updated file path to imageListTrash
                                        imageListTrash.add(newFile.getPath());
                                        imageListFavor.remove(imgPath);
                                        DataLocalManager.setListImg(imageListTrash, PREF_IMG_TRASH);
                                        GetAllimagefromGallery.removeImagesFromAllImages(Collections.singletonList(targetUri.getPath()));
                                    } catch (IOException e) {
                                        // Handle the case where moving fails
                                        e.printStackTrace();
                                        Toast.makeText(PictureActivity.this, "Failed to move the file to Trash", Toast.LENGTH_SHORT).show();
                                    }

                                    // Finish the activity
                                    finish();
                                } else {
                                    Toast.makeText(PictureActivity.this, "File not exist: " + targetUri.getPath(), Toast.LENGTH_SHORT).show();
                                }
                                dialog.dismiss();
                            }
                        });

                        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });

                        AlertDialog alert = builder.create();
                        alert.show();
                        break;
                    }

                }
                return true;
            }
        });
    }

    private boolean check(String imgPath) {
        for (String img : imageListFavor) {
            if (img.equals(imgPath)) {
                return true;
            }
        }
        return false;
    }

    //LE: for share image
    private File createTempImageFile(String fileName) {
        try {
            File tempDir = new File(getCacheDir(), "my_temp_files");

            // Print the directory path
            String directoryPath = tempDir.getAbsolutePath();
            Log.d("TempFileLocation", "Directory Path: " + directoryPath);

            if (!tempDir.exists()) {
                tempDir.mkdirs();
            }

            File tempFile = new File(tempDir, fileName);
            tempFile.createNewFile();
            tempFile.deleteOnExit();

            return tempFile;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}