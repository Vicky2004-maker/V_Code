package com.clevergo.vcode;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CompletableFuture;

public class MainActivity extends AppCompatActivity {

    CompletableFuture<Void> settingFileTask;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        setContentView(R.layout.activity_main);

        Objects.requireNonNull(getSupportActionBar()).hide();

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            settingFileTask = CompletableFuture.runAsync(() -> Helper.initializeFile(MainActivity.this))
                    .thenRun(Helper::updateSettingsMap)
                    .thenRun(() -> {
                        if (Helper.isPrivacyPolicyAccepted() && Helper.checkPermissions(MainActivity.this)) {
                            timerToLoad(3000);
                        }
                    });
        } else {
            Helper.initializeFile(MainActivity.this);
            Helper.updateSettingsMap();
            if (Helper.isPrivacyPolicyAccepted() && Helper.checkPermissions(MainActivity.this)) {
                timerToLoad(3000);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == Helper.PERMISSION_REQ_CODE) {
            if (grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                runOnUiThread(this::privacyPolicyDialog);
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            if (!Helper.checkPermissions(MainActivity.this)) {
                Helper.launchPermission(MainActivity.this);
            }

            if (!Helper.isPrivacyPolicyAccepted() && settingFileTask.isDone()) {
                privacyPolicyDialog();
            }
        } else {
            if (!Helper.checkPermissions(MainActivity.this)) {
                Helper.launchPermission(MainActivity.this);
            }
        }
    }

    private void privacyPolicyDialog() {
        if (!Helper.settingsMap.containsKey("privacyPolicy")) {
            AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
            alertDialog.setTitle(getString(R.string.privacyPolicy));
            alertDialog.setMessage(getString(R.string.privacyPolicyMessage));
            alertDialog.setCancelable(false);
            alertDialog.setPositiveButton(getString(R.string.agree), (a, b) -> {
                Helper.writeSetting("privacyPolicy", "agree");
                if (!Helper.checkPermissions(MainActivity.this)) {
                    Helper.launchPermission(MainActivity.this);
                } else {
                    timerToLoad(1000);
                }
            });
            alertDialog.setNeutralButton(getString(R.string.disagree), (a, b) -> finish());
            alertDialog.setNegativeButton(getString(R.string.view), (a, b) -> Helper.launchUrlInBrowser(Helper.PRIVACY_POLICY_URL, MainActivity.this));

            alertDialog.create();
            alertDialog.show();
        } else if (!Helper.checkPermissions(MainActivity.this)) {
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
}
