package com.example.photo_gallery;

import static com.example.photo_gallery.DataLocalManager.getListImg;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class PhotoFragment extends Fragment {
    private RecyclerView recyclerView;

    MainActivity main;
    private CategoryAdapter categoryAdapter;
    private androidx.appcompat.widget.Toolbar toolbar_photo;
    private Boolean flag = false;
    private List<Category> listImg;
    private List<Image> imageList;
    private List<String> listLabel;
    private ArrayList<String> list_searchA;
    private static int REQUEST_CODE_MULTI = 40;
    final SparseArray<Integer> menuIdTotoolBarPosition = new SparseArray<>();
    private Context context;
    private static final int MENU_SEARCH_ID = 0;
    private static final int MENU_CAMERA_ID = 1;
    private static final int MENU_SEARCH_ADVANCED_ID = 2;
    private static final int MENU_DUPLICATE_ID = 3;
    private static final int MENU_CHOOSE_ID = 4;
    private static final int MENU_SETTING_ID = 5;
    MySharedPreferences pref;

    public static Set<String> imageListFavor;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo, container, false);
        Toast.makeText(getContext(),"onCreate" ,Toast.LENGTH_SHORT).show();

        context = view.getContext();
        setUpListLabel(view.getContext());
        recyclerView = view.findViewById(R.id.rcv_category);
        toolbar_photo = view.findViewById(R.id.toolbar_photo);
        menuIdTotoolBarPosition.put(R.id.menuSearch, MENU_SEARCH_ID);
        menuIdTotoolBarPosition.put(R.id.menuCamera, MENU_CAMERA_ID);
        menuIdTotoolBarPosition.put(R.id.menuSearch_Advanced, MENU_SEARCH_ADVANCED_ID);
        menuIdTotoolBarPosition.put(R.id.menuChoose, MENU_CHOOSE_ID);
        menuIdTotoolBarPosition.put(R.id.menuSettings, MENU_SETTING_ID);
        menuIdTotoolBarPosition.put(R.id.duplicateImages, MENU_DUPLICATE_ID);
        main = (MainActivity) getActivity();
        pref = new MySharedPreferences(getContext());
        toolbar_photo.inflateMenu(R.menu.menu_top);
        toolbar_photo.setTitle(getContext().getResources().getString(R.string.photo));
        toolbar_photo.setTitleTextColor(0xFFFFFFFF);
        int mauchude = Integer.parseInt(pref.updateMeUsingSavedStateData("mauchude"));
        toolbar_photo.setBackgroundColor(mauchude);
        toolBarEvents();
        setRyc();
        return view;
    }

    private void setUpListLabel(Context context) {
        list_searchA = new ArrayList<>();
        try {
            listLabel = new ArrayList<>();
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(context.getAssets().open("label.txt")));
            String line = "";
            while ((line = reader.readLine()) != null) {
                listLabel.add(line.toUpperCase());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setRyc() {
        categoryAdapter = new CategoryAdapter(getContext());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        recyclerView.setLayoutManager(linearLayoutManager);
        categoryAdapter.setData(getListCategory());
        recyclerView.setAdapter(categoryAdapter);

    }

    private void toolBarEvents() {
        toolbar_photo.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int id = item.getItemId();
                int menuConstant = -1;
                menuConstant = menuIdTotoolBarPosition.get(id);
                if (menuConstant != -1) {
                    // Sử dụng switch để xử lý chức năng tương ứng với hằng số
                    switch (menuConstant) {
                        case MENU_SEARCH_ID:

                            break;
                        case MENU_CAMERA_ID:
                            takenImg();
                            //requestCamera();
                            //openCamera();
                            break;
                        case MENU_SEARCH_ADVANCED_ID:

                            break;
                        case MENU_DUPLICATE_ID:

                            break;
                        case MENU_CHOOSE_ID:

                            break;
                        case MENU_SETTING_ID:
                            Intent intent = new Intent(getContext(), SettingsActivity.class);
                            startActivity(intent);
                            break;
                    }
                }
                return true;
            }

        });

    }

    public ArrayList<String> getListImg() {
        List<Image> imageList = GetAllimagefromGallery.getAllImageFromGallery(getContext());

        long hash = 0;
        Map<Long, ArrayList<String>> map = new HashMap<Long, ArrayList<String>>();
        for (Image img : imageList) {
            Bitmap bitmap = BitmapFactory.decodeFile(img.getPath());
            hash = hashBitmap(bitmap);
            if (map.containsKey(hash)) {
                map.get(hash).add(img.getPath());
            } else {
                ArrayList<String> list = new ArrayList<>();
                list.add(img.getPath());
                map.put(hash, list);
            }
        }
        ArrayList<String> result = new ArrayList<>();
        Set set = map.keySet();
        for (Object key : set) {
            if (map.get(key).size() >= 2) {
                result.addAll(map.get(key));
            }
        }
        return result;
    }

    public long hashBitmap(Bitmap bmp) {
        long hash = 31;
        for (int x = 1; x < bmp.getWidth(); x = x * 2) {
            for (int y = 1; y < bmp.getHeight(); y = y * 2) {
                hash *= (bmp.getPixel(x, y) + 31);
                hash = hash % 1111122233;
            }
        }
        return hash;
    }


    private void showImageByDate(String date) {
        Toast.makeText(getContext(), date, Toast.LENGTH_LONG).show();
        List<Image> imageList = GetAllimagefromGallery.getAllImageFromGallery(getContext());
        List<Image> listImageSearch = new ArrayList<>();

        for (Image image : imageList) {
            if (image.getDateTaken().contains(date)) {
                listImageSearch.add(image);
            }
        }

        if (listImageSearch.size() == 0) {
            Toast.makeText(getContext(), "Searched image not found", Toast.LENGTH_LONG).show();
        } else {
            ArrayList<String> listStringImage = new ArrayList<>();
            for (Image image : listImageSearch) {
                listStringImage.add(image.getPath());
            }
            Intent intent = new Intent(context, ItemAlbumActivity.class);
            intent.putStringArrayListExtra("data", listStringImage);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Toast.makeText(getContext(),"onResume" ,Toast.LENGTH_SHORT).show();
        MyAsyncTask myAsyncTask = new MyAsyncTask();
        myAsyncTask.execute();
        categoryAdapter.setData(listImg);
        // Đọc giá trị màu từ SharedPreferences
        int mauchude = Integer.parseInt(pref.updateMeUsingSavedStateData("mauchude"));

        // Cập nhật màu chủ đề cho toolbar_photo
        toolbar_photo.setBackgroundColor(mauchude);
        toolBarEvents();
    }

    @Override
    public void onStop() {
        super.onStop();
        flag = true;
    }

    //Camera
    private static final int CAMERA_REQUEST = 1888;
    private static final int MY_CAMERA_PERMISSION_CODE = 100;
    private static final int PICTURE_RESULT = 1;
    private Uri imageUri;
    private String imageurl;
    private Bitmap thumbnail;

    private void takenImg() {
        int permissionCheckCamera = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA);

        if (permissionCheckCamera != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
        } else {
            openCamera();
        }

    }

    void requestCamera() {
        int permissionCheckCamera = ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA);

        if (permissionCheckCamera != PackageManager.PERMISSION_GRANTED) {
            // Request camera permission
            requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_PERMISSION_CODE);
        }
    }

    private void openCamera() {
        ContentValues values = new ContentValues();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US);
        String timestamp = sdf.format(new Date());
        String imageFileName = "IMG_" + timestamp + ".jpg";

        values.put(MediaStore.Images.Media.DISPLAY_NAME, imageFileName);
        values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
        imageUri = getActivity().getApplicationContext().getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
        startActivityForResult(intent, PICTURE_RESULT);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(getActivity(), "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {

            case PICTURE_RESULT:
                if (requestCode == PICTURE_RESULT) {
                    if (resultCode == Activity.RESULT_OK) {
                        try {
                            thumbnail = MediaStore.Images.Media.getBitmap(
                                    getActivity().getApplicationContext().getContentResolver(), imageUri);

                            imageurl = getRealPathFromURI(imageUri);
                            GetAllimagefromGallery.updateAllImagesFromUris(this.context, Collections.singletonList(imageUri), true);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }


                    }
                }
        }
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_MULTI) {
            MyAsyncTask myAsyncTask = new MyAsyncTask();
            myAsyncTask.execute();
        }
    }

    public String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = getActivity().managedQuery(contentUri, proj, null, null, null);
        int column_index = cursor
                .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    @NonNull
    private List<Category> getListCategory() {
        List<Category> categoryList = new ArrayList<>();
        int categoryCount = 0;
        imageList = GetAllimagefromGallery.getAllImageFromGallery(getContext());

        try {
            categoryList.add(new Category(imageList.get(0).getDateTaken(), new ArrayList<>()));
            categoryList.get(categoryCount).addListGirl(imageList.get(0));
            for (int i = 1; i < imageList.size(); i++) {
                if (!imageList.get(i).getDateTaken().equals(imageList.get(i - 1).getDateTaken())) {
                    categoryList.add(new Category(imageList.get(i).getDateTaken(), new ArrayList<>()));
                    categoryCount++;
                }
                categoryList.get(categoryCount).addListGirl(imageList.get(i));
            }
            return categoryList;
        } catch (Exception e) {
            return null;
        }

    }

    public class MyAsyncTask extends AsyncTask<Void, Integer, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            listImg = getListCategory();
            return null;
        }

        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            categoryAdapter.setData(listImg);
        }
    }

    public class LabelAsyncTask extends AsyncTask<Void, Integer, Void> {
        private String title;
        private ProgressDialog mProgressDialog;

        public void setTitle(String title) {
            this.title = title.toUpperCase();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            List<Image> imageList = GetAllimagefromGallery.getAllImageFromGallery(context);
            list_searchA.clear();
            for (int i = 0; i < imageList.size(); i++) {
                Bitmap bitmap = getBitmap(imageList.get(i).getPath());
                Bitmap resized = Bitmap.createScaledBitmap(bitmap, 224, 224, true);
            }
            return null;
        }

        private int getMax(float[] arr) {
            int ind = 0;
            float min = 0.0f;
            for (int i = 0; i < 1001; i++) {
                if (arr[i] > min) {
                    ind = i;
                    min = arr[i];
                }
            }
            return ind;
        }

        public Bitmap getBitmap(String path) {
            Bitmap bitmap = null;
            try {
                File f = new File(path);
                BitmapFactory.Options options = new BitmapFactory.Options();
                options.inPreferredConfig = Bitmap.Config.ARGB_8888;
                bitmap = BitmapFactory.decodeStream(new FileInputStream(f), null, options);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return bitmap;
        }


        @Override
        protected void onPostExecute(Void unused) {
            super.onPostExecute(unused);
            mProgressDialog.cancel();
            Intent intent = new Intent(context, ItemAlbumActivity.class);
            intent.putStringArrayListExtra("data", list_searchA);
            intent.putExtra("name", title);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = new ProgressDialog(context);
            mProgressDialog.setMessage("Loading, please wait...");
            mProgressDialog.show();
        }
    }



}

