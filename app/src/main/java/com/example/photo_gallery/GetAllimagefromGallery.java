package com.example.photo_gallery;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import com.example.photo_gallery.Image;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Currency;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

public class GetAllimagefromGallery {
    public static ArrayList<Image> allImages;
    private static boolean allImagesPresent = false;
    private static boolean addNewestImagesOnly = false;

    public List<Image> getAllImages() {
        return allImages;
    }

    public static void refreshAllImages() {
        allImagesPresent = false;
    }

    public static void updateNewImages() {
        addNewestImagesOnly = true;
    }

    public static void removeImagesFromAllImages(List<String> paths) {
        for (int i = allImages.size() - 1; i >= 0; i--) {
            Image image = allImages.get(i);
            if (paths.contains(image.getPath())) {
                allImages.remove(i);
            }
        }
    }

    public GetAllimagefromGallery(Context context) {

    }

    public static boolean hasSlash(String a, String b) {
        if (a == null || b == null || a.isEmpty() || b.isEmpty()) {
            return false;
        }
        int indexOfB = a.indexOf(b);
        if (indexOfB != -1 && indexOfB + b.length() < a.length()) {
            String remainingString = a.substring(indexOfB + b.length());
            return remainingString.contains("/");
        } else {
            return false;
        }
    }

    public static ArrayList<Image> getAllImageFromGallery(Context context) {
        if (!allImagesPresent) {
            Uri uri;
            Cursor cursor;
            int columnIndexData, thumb, dateIndex;
            ArrayList<Image> listImage = new ArrayList<>();
            String absolutePathImage = null;
            String thumbnail = null;
            Long dateTaken = null;
            uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

            String[] projection = {
                    MediaStore.MediaColumns.DATA,
                    MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                    MediaStore.Images.Media.DATE_TAKEN
            };

            final String orderBy = MediaStore.Images.Media.DATE_TAKEN;
            cursor = context.getApplicationContext().getContentResolver().query(uri, projection, null, null, orderBy + " DESC");
            columnIndexData = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
            thumb = cursor.getColumnIndexOrThrow(MediaStore.Images.Thumbnails.DATA);
            dateIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN);
            Calendar myCal = Calendar.getInstance();
            SimpleDateFormat formatter = new SimpleDateFormat("EEE, dd-MM-yyyy");
            while (cursor.moveToNext()) {
                try {
                    absolutePathImage = cursor.getString(columnIndexData);
                    File file = new File(absolutePathImage);
                    if (!file.canRead()) {
                        continue;
                    }
                } catch (Exception e) {
                    continue;
                }
                thumbnail = cursor.getString(thumb);
                String fileName = thumbnail.substring(thumbnail.lastIndexOf("/") + 1);
                if (fileName.startsWith("Deleted_"))
                    continue;

                dateTaken = cursor.getLong(dateIndex);
                myCal.setTimeInMillis(dateTaken);
                String dateText = formatter.format(myCal.getTime());
                Image image = new Image();
                image.setPath(absolutePathImage);
                image.setThump(thumbnail);
                image.setDateTaken(dateText);
                if (image.getPath() == "") {
                    continue;
                }
                if (addNewestImagesOnly) {
                    boolean iscontained = false; // in the "database"
                    for (Image i : allImages) {
                        if (i.getPath().equals(image.getPath())) {
                            iscontained = true;
                            break;
                        }
                    }
                    if (iscontained) {
                        addNewestImagesOnly = false;
                        allImagesPresent = true;
                        cursor.close(); // Android Studio suggestion
                        return allImages;
                    } else {
                        if (allImages.size() > 1200) {
                            addNewestImagesOnly = false;
                            allImagesPresent = true;
                            cursor.close(); // Android Studio suggestion
                            return allImages;
                        }
                        allImages.add(0, image);
                    }
                } else {
                    listImage.add(image);
                }

                if (listImage.size() > 1000) { // Just for testing.
                    break;                  // I don't want to load 10 000 photos at once.
                }
            }
            cursor.close(); // Android Studio suggestion
            ArrayList<Image> listImage1 = new ArrayList<>();
            String cut = "Pictures/";
            for (int i = 0; i < listImage.size(); i++) {
                if (!hasSlash(listImage.get(i).getPath(), cut)) {
                    listImage1.add(listImage.get(i));
                }
            }
            allImages = listImage1;
            addNewestImagesOnly = false;
            allImagesPresent = true;
            return listImage1;
        } else {
            return allImages;
        }
    }


    public static void updateAllImagesFromUris(Context context, List<Uri> imageUris, boolean isAdd ) {
        String[] projection = {
                        MediaStore.MediaColumns.DATA,                // File path
                        MediaStore.Images.Media.DATE_TAKEN           // Date taken
                };

                ArrayList<Image> newImages = new ArrayList<>();

                for (Uri imageUri : imageUris) {
                    // Query the content resolver with the current URI
                    Cursor cursor = context.getContentResolver().query(imageUri, projection, null, null, null);

                    if (cursor != null && cursor.moveToFirst()) {
                        int columnIndexData = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
                        int dateIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN);

                        // Get the file path and date taken from the cursor
                        String absolutePathImage = cursor.getString(columnIndexData);
                        Long dateTaken = cursor.getLong(dateIndex);

                        Calendar myCal = Calendar.getInstance();
                        myCal.setTimeInMillis(dateTaken);
                        SimpleDateFormat formatter = new SimpleDateFormat("EEE, dd-MM-yyyy");
                        String dateText = formatter.format(myCal.getTime());

                        // Create a new Image object with the obtained information
                        Image newImage = new Image();
                        int thumb = cursor.getColumnIndexOrThrow(MediaStore.Images.Thumbnails.DATA);
                        String thumbnail = cursor.getString(thumb);
                        newImage.setThump(thumbnail);
                        newImage.setPath(absolutePathImage);
                        newImage.setDateTaken(dateText);

                        newImages.add(newImage);

                        // Close the cursor to avoid memory leaks
                        cursor.close();
                    }
                }

                if (isAdd) {
                    if (allImages != null) {
                        allImages.addAll(0, newImages);
                    } else {
                        allImages = new ArrayList<>(newImages);
                    }
                } else {
                    allImages.removeAll(newImages);
                }
    }

    public static Image createImageFromPath(String path) {
        Image image = new Image();
        image.setPath(path);
        // Set other properties of the Image as needed
        return image;
    }

}
