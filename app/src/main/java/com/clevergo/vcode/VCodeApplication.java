package com.clevergo.vcode;

import android.app.Application;

import com.google.android.material.color.DynamicColors;

public class VCodeApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        //DynamicColors.applyToActivitiesIfAvailable(VCodeApplication.this);
    }
}
