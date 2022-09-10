/*
 * Created by Viknesh on 2022.
 * Copyright (c) 2022. All rights reserved.
 */

package com.clevergo.vcode;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

public class CloudFileBrowser extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloud_file_browser);

        //TODO: Search Interface in Cloud File Browser

        getSupportActionBar().setTitle(getString(R.string.cloud_browser));
        getSupportActionBar().setSubtitle(getString(R.string.uploaded_files));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ListView lv = findViewById(R.id.cloud_files_listView);
        CodeViewActivity.storageRef_UserFiles.listAll().addOnSuccessListener(CloudFileBrowser.this, listResult -> {
            CloudFileBrowserAdapter adapter = new CloudFileBrowserAdapter(CloudFileBrowser.this, listResult.getItems());
            lv.setAdapter(adapter);
        });
    }
}
