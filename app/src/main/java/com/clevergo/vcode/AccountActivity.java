/*
 * Created by Viknesh on 2022.
 * Copyright (c) 2022. All rights reserved.
 */

package com.clevergo.vcode;

import static com.clevergo.vcode.MainActivity.UID;
import static com.clevergo.vcode.MainActivity.auth;
import static com.clevergo.vcode.CodeViewActivity.customWorkerThread;
import static com.clevergo.vcode.MainActivity.storage;
import static com.clevergo.vcode.Helper.GOOGLE_SIGN_IN;
import static com.clevergo.vcode.Helper.createGoogleSignInClient;
import static com.clevergo.vcode.Helper.uiHandler;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.function.Function;

public class AccountActivity extends AppCompatActivity {
    private ActionBar actionBar;

    private SignInButton signInButton;
    private ConstraintLayout signIn_page_constraintLayout, account_page_constraintLayout;
    private ImageView profilePicture_imageView;
    private FirebaseUser user = null;

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        profilePicture_imageView = findViewById(R.id.profilePicture_imageView);
        signIn_page_constraintLayout = findViewById(R.id.signIn_page_constraintLayout);
        account_page_constraintLayout = findViewById(R.id.account_page_constraintLayout);
        signInButton = findViewById(R.id.signIn_button);

        actionBar = getSupportActionBar();
        assert actionBar != null;

        displayPages();

        actionBar.setTitle(getString(R.string.account));
        actionBar.setDisplayHomeAsUpEnabled(true);

        findViewById(R.id.signOut_button).setOnClickListener(a -> {
            if (user != null) {
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

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == GOOGLE_SIGN_IN && data != null) {
            googleSignIn(data, AccountActivity.this);
        }
    }

    private void googleSignIn(Intent data, AppCompatActivity activity) {
        {
            Task<GoogleSignInAccount> signInAccountTask = GoogleSignIn.getSignedInAccountFromIntent(data);

            if (signInAccountTask.isSuccessful()) {
                Toast.makeText(activity, activity.getString(R.string.signin_successful), Toast.LENGTH_LONG).show();

                GoogleSignInAccount signInAccount = null;
                try {
                    signInAccount = signInAccountTask.getResult(ApiException.class);
                    if (signInAccount != null) {
                        AuthCredential authCredential = GoogleAuthProvider.getCredential(signInAccount.getIdToken(), null);

                        auth.signInWithCredential(authCredential).addOnFailureListener(activity, m -> {
                        }).addOnSuccessListener(activity, m -> {
                            displayPages();
                            UID = auth.getUid();
                            MainActivity.storageRef_UserFiles = storage.getReference().child("Users_Files/" + UID);
                        });
                    }
                } catch (ApiException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void displayPages() {
        if (auth.getCurrentUser() != null) {
            account_page_constraintLayout.setVisibility(View.VISIBLE);
            signIn_page_constraintLayout.setVisibility(View.GONE);
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
        } else {
            account_page_constraintLayout.setVisibility(View.GONE);
            signIn_page_constraintLayout.setVisibility(View.VISIBLE);

            signInButton.setOnClickListener(a -> createGoogleSignInClient(AccountActivity.this));
        }
    }

    private class LoadingScreen extends AsyncTask<Method, Integer, String> {
        private ProgressDialog progressDialog;
        @Override
        protected void onPreExecute() {
            progressDialog = ProgressDialog.show(AccountActivity.this, "Loading", "Please Wait", false, false);
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected String doInBackground(Method... methods) {

            return null;
        }
    }
}
