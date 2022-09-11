package com.clevergo.vcode;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.AppCompatImageButton;

import com.google.firebase.storage.StorageReference;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class CloudFileBrowserAdapter implements ListAdapter {

    private final Context context;
    private List<StorageReference> cloudFiles = new ArrayList<>();
    private ImageView fileLanguageDisplay_imageView;

    public CloudFileBrowserAdapter(Context context, List<StorageReference> cloudFiles) {
        this.cloudFiles = cloudFiles;
        this.context = context;
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer) {

    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer) {

    }

    @Override
    public int getCount() {
        return cloudFiles.size();
    }

    @Override
    public Object getItem(int position) {
        return cloudFiles.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(context);
        @SuppressLint("ViewHolder") View v = inflater.inflate(R.layout.cloud_file_browser_item, null, true);

        StorageReference storageReference = cloudFiles.get(position);
        //TODO: Delete, Edit, Read Cloud File, Add a Info button to show all the details that we possibly could

        fileLanguageDisplay_imageView = v.findViewById(R.id.fileLanguageDisplay_imageView);
        TextView file_name_browser_textView = v.findViewById(R.id.file_name_browser_textView);
        TextView updatedTime_textView = v.findViewById(R.id.updatedTime_textView);
        AppCompatImageButton deleteFile_browser_imageView = v.findViewById(R.id.deleteFile_browser_imageView);
        AppCompatImageButton editFile_browser_imageView = v.findViewById(R.id.editFile_browser_imageView);
        AppCompatImageButton openFile_browser_imageView = v.findViewById(R.id.openFile_browser_imageView);

        v.findViewById(R.id.fileInfo_browser_imageView).setOnClickListener(a -> {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(context.getString(R.string.file_name)).append(" ").append(storageReference.getName());
            stringBuilder.append("\n");

            storageReference.getMetadata().addOnSuccessListener(storageMetadata -> {
                long sizeBytes = storageMetadata.getSizeBytes();
                //TODO: Optimize this calculation
                stringBuilder.append(context.getString(R.string.size)).append(" ").append((double) (sizeBytes / 1024d) > 1024d ? (double) (sizeBytes / 1024e2) : sizeBytes).append((double) (sizeBytes / 1024d) > 1024d ? "MB" : "KB");
                stringBuilder.append(storageMetadata);
            }).addOnCompleteListener(task -> new AlertDialog.Builder(context)
                    .setTitle(context.getString(R.string.file_details))
                    .setMessage(stringBuilder.toString())
                    .setIcon(AppCompatResources.getDrawable(context, R.drawable.ic_info_24))
                    .setCancelable(true)
                    .setPositiveButton(context.getString(R.string.ok), (dialog, which) -> {
                    }).create().show());
        });

        setImageViewIcon(Helper.getFileExtension(storageReference.getName()));
        file_name_browser_textView.setText(storageReference.getName());
        storageReference.getMetadata().addOnSuccessListener(storageMetadata -> {
            Date date = new Date(storageMetadata.getUpdatedTimeMillis());
            DateFormat formatter = DateFormat.getInstance();
            formatter.setTimeZone(TimeZone.getDefault());
            updatedTime_textView.setText(formatter.format(date));
        });
        return v;
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return cloudFiles.size();
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean areAllItemsEnabled() {
        return false;
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }

    private void setImageViewIcon(String fileExtension) {
        switch (fileExtension) {
            case "java":
                fileLanguageDisplay_imageView.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.java_logo));
                break;
            case "js":
                fileLanguageDisplay_imageView.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.javascript_logo));
                break;
            case "py":
                fileLanguageDisplay_imageView.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.python_logo));
                break;
            default:
                fileLanguageDisplay_imageView.setImageDrawable(AppCompatResources.getDrawable(context, R.drawable.ic__temp_fileplacer_24));
                break;
        }
    }
}
