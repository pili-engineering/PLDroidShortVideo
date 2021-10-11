package com.qiniu.shortvideo.app;

import android.app.Application;

import com.qiniu.pili.droid.shortvideo.PLShortVideoEnv;
import com.qiniu.shortvideo.app.utils.Config;

import org.lasque.tusdk.core.TuSdk;

public class ShortVideoApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        PLShortVideoEnv.init(getApplicationContext());
        Config.init(getApplicationContext());
        TuSdk.enableDebugLog(false);
        TuSdk.init(this.getApplicationContext(), "5be4d67909e1d60d-03-bshmr1");
    }
}
