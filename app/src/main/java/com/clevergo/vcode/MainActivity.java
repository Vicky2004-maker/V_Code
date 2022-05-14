package com.clevergo.vcode;

import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        Objects.requireNonNull(getSupportActionBar()).hide();

        Helper.initializeFile(MainActivity.this);
        Helper.updateSettingsMap();

        if(Objects.equals(Helper.settingsMap.get("privacyPolicy"), "agree") && Helper.checkPermissions(MainActivity.this)) {
            timerToLoad(3000);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

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

    @Override
    protected void onResume() {
        super.onResume();

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
}
