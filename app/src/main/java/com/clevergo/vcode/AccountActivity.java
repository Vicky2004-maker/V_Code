/*
 * Created by Viknesh on 2022.
 * Copyright (c) 2022. All rights reserved.
 */

package com.clevergo.vcode;

import static com.clevergo.vcode.CodeViewActivity.UID;
import static com.clevergo.vcode.CodeViewActivity.auth;
import static com.clevergo.vcode.CodeViewActivity.customWorkerThread;
import static com.clevergo.vcode.Helper.uiHandler;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseUser;

import java.io.IOException;
import java.net.URL;

public class AccountActivity extends AppCompatActivity {
    private ImageView profilePicture_imageView;
    private FirebaseUser user = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        profilePicture_imageView = findViewById(R.id.profilePicture_imageView);
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;

        actionBar.setTitle(getString(R.string.account));
        actionBar.setDisplayHomeAsUpEnabled(true);
        if(auth.getCurrentUser() != null) {
            user = auth.getCurrentUser();
            actionBar.setSubtitle(user.getDisplayName());
            customWorkerThread.addWork(() -> {
                try {
                    Bitmap bitmap = BitmapFactory.decodeStream(new URL(user.getPhotoUrl().toString()).openStream());
                    uiHandler.post(() -> profilePicture_imageView.setImageBitmap(bitmap));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            ((TextInputEditText) findViewById(R.id.userName_TextInput)).setText(user.getDisplayName());
            ((TextInputEditText) findViewById(R.id.phoneNumber_TextInput)).setText(user.getPhoneNumber());
            ((TextInputEditText) findViewById(R.id.emailID_TextInput)).setText(user.getEmail());
        }
        findViewById(R.id.signOut_button).setOnClickListener(a -> {
            if(user != null) {
                auth.signOut();
                Toast.makeText(AccountActivity.this, getString(R.string.sign_out_success), Toast.LENGTH_LONG).show();
                UID = null;
                AccountActivity.this.finish();
            }
        });

        findViewById(R.id.deleteAccount_button).setOnClickListener(a -> {
            //TODO : Request for account deletion
        });
    }
}
