package com.clevergo.vcode;

import static com.clevergo.vcode.Helper.checkPermissions;
import static com.clevergo.vcode.Helper.cloudFileList;
import static com.clevergo.vcode.Helper.getDifference_progress;
import static com.clevergo.vcode.Helper.isPrivacyPolicyAccepted;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicReference;

public class MainActivity extends AppCompatActivity {

    public static FirebaseAuth auth;
    public static String UID;
    public static StorageReference storageRef_UserFiles;
    public static FirebaseStorage storage;
    public static SubscriptionModel subscriptionModel;
    private CircularProgressIndicator progressIndicator;
    private CustomWorkerThread workerThread;

    private int totalCloudFiles = 0, loopCount_1 = 0, loopCount_2 = 0, loopCount_3 = 0;

    @Override
    protected void onStop() {
        super.onStop();
        workerThread.stop();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        finalLogIn();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        Objects.requireNonNull(getSupportActionBar()).hide();
        progressIndicator = findViewById(R.id.progressBar);

        workerThread = new CustomWorkerThread();
        Helper.setThisIsMobile(MainActivity.this);
        Helper.initializeFile(MainActivity.this);
        Helper.updateSettingsMap();
        if (!Helper.checkPermissions(MainActivity.this)) Helper.launchPermission(MainActivity.this);
        if (checkPermissions(MainActivity.this) && !isPrivacyPolicyAccepted()) {
            privacyPolicyDialog();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == Helper.PERMISSION_REQ_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                privacyPolicyDialog();
            }
        } else {
            Helper.launchPermission(MainActivity.this);
        }
    }

    private void privacyPolicyDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
        alertDialog.setTitle(getString(R.string.privacyPolicy));
        alertDialog.setMessage(getString(R.string.privacyPolicyMessage));
        alertDialog.setCancelable(false);
        alertDialog.setPositiveButton(getString(R.string.agree), (a, b) -> {
            Helper.writeSetting("privacyPolicy", "agree");
            if (!Helper.checkPermissions(MainActivity.this)) {
                Helper.launchPermission(MainActivity.this);
            } else {
                finalLogIn();
            }
        });
        alertDialog.setNeutralButton(getString(R.string.disagree), (a, b) -> finish());
        alertDialog.setNegativeButton(getString(R.string.view), (a, b) -> Helper.launchUrlInBrowser(Helper.PRIVACY_POLICY_URL, MainActivity.this));

        alertDialog.create();
        alertDialog.show();
        if (!Helper.checkPermissions(MainActivity.this)) {
            Helper.launchPermission(MainActivity.this);
        }

    }

    private void timerToLoad(int time) {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                startActivity(new Intent(MainActivity.this, CodeViewActivity.class));
            }
        }, time);
    }

    private void finalLogIn() {
        Log.println(Log.ASSERT, "IS IT RUN", "YES");
        Log.println(Log.ASSERT, "PRIVACY POLICY  1", String.valueOf(isPrivacyPolicyAccepted()));
        workerThread.addWork(() -> {
            if (Helper.isPrivacyPolicyAccepted() && Helper.checkPermissions(MainActivity.this)) {
                auth = FirebaseAuth.getInstance();
                storage = FirebaseStorage.getInstance();
                FirebaseUser currentUser = auth.getCurrentUser();

                if (currentUser != null) {
                    UID = currentUser.getUid();
                    storageRef_UserFiles = storage.getReference().child("Users_Files/" + UID);
                    storageRef_UserFiles.listAll().addOnSuccessListener(MainActivity.this, listResult -> {
                        List<StorageReference> storageReferenceList = listResult.getItems();
                        totalCloudFiles = storageReferenceList.size();
                        int temp = totalCloudFiles * 3;
                        int step = getDifference_progress(temp);
                        progressIndicator.setMax((int) (temp * 3f));
                        progressIndicator.setProgressCompat(0, true);
                        for (StorageReference file : storageReferenceList) {
                            loopCount_1++;
                            progressIndicator.setProgress(step + progressIndicator.getProgress());
                            String fileName = file.getName();
                            AtomicReference<Uri> downloadUrl = new AtomicReference<>();
                            file.getDownloadUrl().addOnSuccessListener(MainActivity.this, downloadUrl::set).addOnCompleteListener(MainActivity.this, task -> {
                                loopCount_2++;
                                progressIndicator.setProgress(step + progressIndicator.getProgress());
                            });
                            file.getMetadata().addOnSuccessListener(MainActivity.this, storageMetadata -> {
                                CloudFile cloudFile = new CloudFile(MainActivity.this,
                                        fileName,
                                        storageMetadata.getSizeBytes(),
                                        storageMetadata.getCreationTimeMillis(),
                                        storageMetadata.getUpdatedTimeMillis(),
                                        downloadUrl.get(),
                                        5, //TODO : Set total file size limit according to subscription plan
                                        storageMetadata.getCustomMetadata("Email"));

                                cloudFileList.add(cloudFile);
                                loopCount_3++;
                                progressIndicator.setProgress(step + progressIndicator.getProgress());
                                if ((loopCount_1 == loopCount_2) && (loopCount_2 == loopCount_3)) {
                                    if (Helper.checkPermissions(MainActivity.this) && Helper.isPrivacyPolicyAccepted()) {
                                        startActivity(new Intent(MainActivity.this, CodeViewActivity.class));
                                    } else if (!Helper.checkPermissions(MainActivity.this)) {
                                        Helper.launchPermission(MainActivity.this);
                                    } else if (!Helper.isPrivacyPolicyAccepted()) {
                                        privacyPolicyDialog();
                                    }
                                }
                            });
                        }
                    });
                } else {
                    subscriptionModel = SubscriptionModel.Free;
                    Log.println(Log.ASSERT, "PRIVACY POLICY  2", String.valueOf(isPrivacyPolicyAccepted()));
                    if (Helper.checkPermissions(MainActivity.this) && Helper.isPrivacyPolicyAccepted()) {
                        timerToLoad(3000);
                    } else if (!Helper.checkPermissions(MainActivity.this)) {
                        Helper.launchPermission(MainActivity.this);
                    } else if (!Helper.isPrivacyPolicyAccepted()) {
                        privacyPolicyDialog();
                    }
                }
            }
        });
    }
}
