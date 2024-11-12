package com.example.photo_gallery;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.SparseArray;
import android.view.MenuItem;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_MANAGE_ALL_FILES_ACCESS = 11535; //LE
    BottomNavigationView bottomNavigationView;
    ViewPager2 viewPager;
    RelativeLayout ln2;
    private static final int PHOTO_POSITION = 0;
    private static final int ALBUM_POSITION = 1;
    private static final int SECRET_POSITION = 2;
    private static final int FAVORITE_POSITION = 3;
    private static final int TRASH_POSITION = 4;
    ArrayList<Image> mylist;
    GetAllimagefromGallery gallery;
    MySharedPreferences pref;
    PhotoFragment photoFragment;
    private static final int REQUEST_STORAGE_PERMISSION = 123;
    private boolean isPermissionGranted = false;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestManageAllFilePermission();
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottom_nav);
        viewPager = findViewById(R.id.view_pager);
        ln2 = (RelativeLayout) findViewById(R.id.ln2);
        pref = new MySharedPreferences(this);
        photoFragment = new PhotoFragment();

        // Ánh xạ ID của mục và vị trí của viewPager
        final SparseArray<Integer> menuIdToViewPagerPosition = new SparseArray<>();
        menuIdToViewPagerPosition.put(R.id.photo, PHOTO_POSITION);
        menuIdToViewPagerPosition.put(R.id.album, ALBUM_POSITION);
        menuIdToViewPagerPosition.put(R.id.secret, SECRET_POSITION);
        menuIdToViewPagerPosition.put(R.id.favorite, FAVORITE_POSITION);
        menuIdToViewPagerPosition.put(R.id.trash, TRASH_POSITION);

        setUpViewPager();
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Integer viewPagerPosition = menuIdToViewPagerPosition.get(item.getItemId());
                if (viewPagerPosition != null) {
                    viewPager.setCurrentItem(viewPagerPosition);
                }
                return true;
            }
        });


    }

    private void requestManageAllFilePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                // If the app doesn't have the MANAGE_EXTERNAL_STORAGE permission, request it
                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
                isPermissionGranted = true; // Set to false since permission is not granted yet
            } else {
                // Permission already granted
                isPermissionGranted = true;
            }
        } else {
            // For devices running Android versions earlier than 11
            // Launch MainActivity and request WRITE_EXTERNAL_STORAGE permission
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    ContextCompat.checkSelfPermission(this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // Permissions already granted
                isPermissionGranted = true;
            } else {
                ActivityCompat.requestPermissions(this, new String[]{
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                }, REQUEST_STORAGE_PERMISSION);
                isPermissionGranted = true; // Set to false since permission is not granted yet
            }
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        boolean darkmode = Boolean.parseBoolean(pref.updateMeUsingSavedStateData("darkmode"));
        int mauchude = Integer.parseInt(pref.updateMeUsingSavedStateData("mauchude"));
        if (darkmode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        bottomNavigationView.setBackgroundColor(mauchude);
    }

    private void setUpViewPager() {
        ViewPagerAdapder viewPagerAdapter = new ViewPagerAdapder(getSupportFragmentManager(), getLifecycle());
        viewPagerAdapter.setContext(getApplicationContext());
        viewPager.setAdapter(viewPagerAdapter);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged(state);

            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        bottomNavigationView.getMenu().findItem(R.id.photo).setChecked(true);
                        break;
                    case 1:
                        bottomNavigationView.getMenu().findItem(R.id.album).setChecked(true);
                        break;
                    case 2:
                        bottomNavigationView.getMenu().findItem(R.id.secret).setChecked(true);
                        break;
                    case 3:
                        bottomNavigationView.getMenu().findItem(R.id.favorite).setChecked(true);
                        break;
                    case 4:
                        bottomNavigationView.getMenu().findItem(R.id.trash).setChecked(true);
                        break;
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    private void showPermissionRequestDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Yêu cầu quyền truy cập bộ nhớ");
        builder.setMessage("Ứng dụng cần quyền truy cập bộ nhớ để hoạt động.");
        builder.setPositiveButton("Cấp quyền", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                }, REQUEST_STORAGE_PERMISSION);
            }
        });
        builder.setNegativeButton("HỦY", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.show();
    }

    void setBottomNavigationViewcolor(int color) {
        bottomNavigationView.setBackgroundColor(color);
    }
}
