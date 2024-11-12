package com.example.photo_gallery;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder> {
    private List<Album> mListAlbums;
    private Context context;
    private BottomSheetDialog bottomSheetDialog;
    private TextView txtPath;
    public AlbumAdapter(List<Album> mListAlbums, Context context) {
        this.mListAlbums = mListAlbums;
        this.context = context;
    }

    public void setData(List<Album> mListAlbums) {
        this.mListAlbums = mListAlbums;
        notifyDataSetChanged();
    }
    public void notifyData() {
        notifyDataSetChanged();
    }
    @NonNull
    @Override
    public AlbumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_album, parent, false);
        return new AlbumViewHolder(view);
    }

    // nạp dữ liệu cho một view holder
    @Override
    public void onBindViewHolder(@NonNull AlbumViewHolder holder, int position) {
        holder.onBind(mListAlbums.get(position), position);
    }

    @Override
    public int getItemCount() {
        if (mListAlbums != null) {
            return mListAlbums.size();
        }
        return 0;
    }

    class AlbumViewHolder extends RecyclerView.ViewHolder {
        private final ImageView img_album;
        private final TextView txtName_album;
        private final TextView txtCount_item_album;
        private Context context;
        private LinearLayout layout_bottom_delete;
        private LinearLayout layout_bottom_slide_show;
        private LinearLayout layout_bottom_edit_name;

        public AlbumViewHolder(@NonNull View itemView) {
            super(itemView);
            img_album = itemView.findViewById(R.id.img_album);
            txtName_album = itemView.findViewById(R.id.txtName_album);
            txtCount_item_album = itemView.findViewById(R.id.txtCount_item_album);
            context = itemView.getContext();
        }

        public void onBind(Album ref, int pos) {
            bindData(ref);

            img_album.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(context, ItemAlbumActivity.class);
                    ArrayList<String> list = new ArrayList<>();
                    for(int i=0;i<ref.getList().size();i++) {
                        list.add(ref.getList().get(i).getThump());
                    }

                    intent.putStringArrayListExtra("data", list);
                    intent.putExtra("path_folder", ref.getPathFolder());
                    intent.putExtra("name", ref.getName());
                    intent.putExtra("ok", 1);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(intent);
                }
            });

            img_album.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    openBottomDialog();

                    txtPath.setText(ref.getPathFolder());
                    txtPath.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Toast.makeText(context, ref.getPathFolder(), Toast.LENGTH_SHORT).show();
                            bottomSheetDialog.cancel();
                        }
                    });

                    layout_bottom_slide_show.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            //slideShowEvents(ref);
                        }
                    });
                    layout_bottom_delete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            deleteEvents(ref, pos);
                        }
                    });
                    layout_bottom_edit_name.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            editNameEvents(ref, pos);
                        }
                    });
                    return true;
                }
            });

        }

        private void bindData(Album ref) {
            txtName_album.setText(ref.getName());
            txtCount_item_album.setText(String.valueOf(ref.getList().size()) + " items");
            Glide.with(context).load(ref.getImg().getThump()).into(img_album);
        }


        private void deleteEvents(Album ref, int pos) {
            // Delete images in the ref album's listImage
            for (Image image : ref.getList()) {
                File imageFile = new File(image.getPath());
                if (imageFile.exists()) {
                    imageFile.delete();
                }
            }

            // Delete the folder associated with the ref album
            File albumFolder = new File(ref.getPathFolder());
            if (albumFolder.exists() && albumFolder.isDirectory()) {
                File[] files = albumFolder.listFiles();


                if (files != null) {
                    for (File file : files) {
                        file.delete();
                    }
                }
                albumFolder.delete();
            }

            // Remove the album from the list and notify the adapter
            mListAlbums.remove(pos);
            notifyDataSetChanged();

            // Dismiss the bottom sheet dialog
            bottomSheetDialog.cancel();
        }

        private void editNameEvents(Album ref, int pos) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
            View dialogView = LayoutInflater.from(context).inflate(R.layout.layout_edit_name_album, null);
            alertDialogBuilder.setView(dialogView);

            EditText editTextAlbumName = dialogView.findViewById(R.id.editTextAlbumName);
            Button buttonSave = dialogView.findViewById(R.id.buttonSave);

            // Set the current album name in the EditText
            editTextAlbumName.setText(ref.getName());

            AlertDialog alertDialog = alertDialogBuilder.create();
            alertDialog.show();

            buttonSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    String newAlbumName = editTextAlbumName.getText().toString().trim();
                    List<Image> listImage= ref.getList();
                    if (!newAlbumName.isEmpty()) {

                        final String albumPath = Environment.getExternalStorageDirectory() + File.separator + "Pictures" + File.separator + newAlbumName;
                        File directory = new File(albumPath);
                        if (!directory.exists()) {
                            directory.mkdirs();
                            Log.e("File-no-exist", directory.getPath());
                        }

                        ArrayList<Image> newListImage = new ArrayList<>();
                        String[] paths = new String[listImage.size()];

                        for (int i = 0; i < listImage.size(); i++) {
                            Image img = listImage.get(i);
                            String imagePath = img.getPath();
                            String imageFileName = newAlbumName+"_image_" + i + ".jpg";

                            // Create a copy of the image and save it to the album directory
                            String destinationPath = albumPath + File.separator + imageFileName;
                            copyImage(imagePath, destinationPath);

                            // Add the copied image to the new list
                            Image copiedImage = new Image(destinationPath, img.getThump(), img.getDateTaken());
                            newListImage.add(copiedImage);

                            paths[i] = destinationPath;
                        }

                        MediaScannerConnection.scanFile(context.getApplicationContext(), paths, null, null);
                        Album newAlbum = new Album(newAlbumName, newListImage, albumPath);
                        deleteEvents( ref, pos);
                        mListAlbums.add(newAlbum);
                        //notifyItemChanged(pos);
                        alertDialog.dismiss();
                        //return newAlbum;

                    } else {
                        Toast.makeText(context, "Please enter a valid name", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        private void copyImage(String sourcePath, String destinationPath) {
            try {
                File sourceFile = new File(sourcePath);
                File destinationFile = new File(destinationPath);

                FileInputStream inStream = new FileInputStream(sourceFile);
                FileOutputStream outStream = new FileOutputStream(destinationFile);

                byte[] buffer = new byte[1024];
                int length;
                while ((length = inStream.read(buffer)) > 0) {
                    outStream.write(buffer, 0, length);
                }

                inStream.close();
                outStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        private void openBottomDialog() {
            View viewDialog = LayoutInflater.from(context).inflate(R.layout.layout_bottom_sheet_album, null);
            layout_bottom_slide_show = viewDialog.findViewById(R.id.layout_bottom_slide_show);
            layout_bottom_delete = viewDialog.findViewById(R.id.layout_bottom_delete);
            layout_bottom_edit_name=viewDialog.findViewById(R.id.layout_bottom_edit_name);
            txtPath = viewDialog.findViewById(R.id.txtPath);


            bottomSheetDialog = new BottomSheetDialog(context);
            bottomSheetDialog.setContentView(viewDialog);
            bottomSheetDialog.show();
        }
    }
}