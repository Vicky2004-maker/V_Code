package com.clevergo.vcode;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

public class CloudFileBrowser extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloud_file_browser);

        getSupportActionBar().setTitle(getString(R.string.cloud_browser));
        getSupportActionBar().setSubtitle(getString(R.string.uploaded_files));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ListView lv = findViewById(R.id.cloud_files_listView);
        CodeViewActivity.storageRef_UserFiles.listAll().addOnSuccessListener(CloudFileBrowser.this, listResult -> {
            ArrayList<String> fileName = new ArrayList<>();
            for (StorageReference file : listResult.getItems()) {
                fileName.add(file.getName());
            }
            lv.setAdapter(new ArrayAdapter<>(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, fileName));
        });
    }
}
