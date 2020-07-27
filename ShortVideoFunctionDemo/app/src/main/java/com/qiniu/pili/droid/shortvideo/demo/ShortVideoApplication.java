package com.qiniu.pili.droid.shortvideo.demo;

import android.app.Application;

import com.qiniu.pili.droid.shortvideo.PLShortVideoEnv;

public class ShortVideoApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // init resources needed by short video sdk
        PLShortVideoEnv.init(getApplicationContext());
    }
}
