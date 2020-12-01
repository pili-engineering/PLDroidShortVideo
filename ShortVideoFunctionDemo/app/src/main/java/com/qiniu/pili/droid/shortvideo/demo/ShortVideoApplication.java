package com.qiniu.pili.droid.shortvideo.demo;

import androidx.multidex.MultiDexApplication;

import com.qiniu.pili.droid.shortvideo.PLShortVideoEnv;

public class ShortVideoApplication extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        // init resources needed by short video sdk
        PLShortVideoEnv.init(getApplicationContext());
    }
}
