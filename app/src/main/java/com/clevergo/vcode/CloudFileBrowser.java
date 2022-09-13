/*
 * Created by Viknesh on 2022.
 * Copyright (c) 2022. All rights reserved.
 */

package com.clevergo.vcode;

import static com.clevergo.vcode.Helper.cloudFileList;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.storage.StorageReference;

import java.text.DateFormat;
import java.util.Date;
import java.util.TimeZone;

public class CloudFileBrowser extends AppCompatActivity {

    private long sizeUploaded;
    private ActionBar actionBar;
    private int loopCount = 0, totalFiles = 0;

    @SuppressLint("DefaultLocale")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloud_file_browser);

        //TODO: Search Interface in Cloud File Browser
        actionBar = getSupportActionBar();
        assert actionBar != null;

        actionBar.setTitle(getString(R.string.cloud_browser));
        actionBar.setSubtitle(String.format("%4.2f MB/ 5 MB", Helper.getTotalCloudFilesSize_MB()));
        actionBar.setDisplayHomeAsUpEnabled(true);

        ListView lv = findViewById(R.id.cloud_files_listView);
        CloudFileBrowserAdapter adapter = new CloudFileBrowserAdapter(CloudFileBrowser.this, cloudFileList);
        lv.setAdapter(adapter);
    }

    @SuppressLint("DefaultLocale")
    private void getSizeStored(StorageReference file) {
        file.getMetadata().addOnSuccessListener(CloudFileBrowser.this, storageMetadata -> {
            sizeUploaded += storageMetadata.getSizeBytes();
            long timeMillis = storageMetadata.getUpdatedTimeMillis();
            Date date = new Date(timeMillis);
            DateFormat dateFormat = DateFormat.getInstance();
            dateFormat.setTimeZone(TimeZone.getDefault());

            Log.e("FORMATTED TIME", dateFormat.format(date));
        }).addOnCompleteListener(CloudFileBrowser.this, task -> {
            loopCount++;
            if (loopCount == totalFiles) {
            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
