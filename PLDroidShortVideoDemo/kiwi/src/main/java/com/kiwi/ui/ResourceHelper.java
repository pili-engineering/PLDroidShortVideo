package com.kiwi.ui;

import android.content.Context;
import android.util.Log;

import com.kiwi.tracker.common.Config;
import com.kiwi.tracker.utils.FileCache;

import java.io.File;

import static com.bumptech.glide.gifdecoder.GifHeaderParser.TAG;

public class ResourceHelper {

    public static void copyResource2SD(final Context context) {
        copyConfigs2SD(context);

        new Thread(new Runnable() {
            @Override
            public void run() {
                copyFilters2SD(context);
                copyStickers2SD(context);
            }
        }).start();
    }

    private static void copyConfigs2SD(Context context) {

        if(!new File(Config.getStickerConfigPath()).exists())
        FileCache.copyFilesFassets(context, Config.STICKERS_JSON, Config.getStickerConfigPath());

        if(!new File(Config.getFilterConfigPath()).exists())
        FileCache.copyFilesFassets(context, Config.FILTER_TYPE_JSON, Config.getFilterConfigPath());

    }

    private static void copyStickers2SD(final Context context) {
        final String stickerPath = Config.getStickerPath();
        new File(stickerPath).mkdirs();

        long start = System.currentTimeMillis();
        FileCache.copyFilesFassets(context, "sticker", stickerPath);
        Log.i(TAG, "copy stickers to sdcard path:" + stickerPath +
                ",cost:" + (System.currentTimeMillis() - start));
    }

    private static void copyFilters2SD(final Context context) {
        final String filterPath = Config.getFilterPath();
        new File(filterPath).mkdirs();
        long start = System.currentTimeMillis();
        FileCache.copyFilesFassets(context, "filter", filterPath);
        Log.i(TAG, "copy filters to sdcard path:" + filterPath +
                ",cost:" + (System.currentTimeMillis() - start));
    }

}
