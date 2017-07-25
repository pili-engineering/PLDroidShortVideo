package com.qiniu.pili.droid.shortvideo.demo.utils;

import android.content.Context;
import android.widget.Toast;

public class ToastUtils {
    public static void s(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public static void l(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }
}
