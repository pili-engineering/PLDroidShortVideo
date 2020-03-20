package com.qiniu.pili.droid.shortvideo.demo.view;

import android.app.ProgressDialog;
import android.content.Context;

public class CustomProgressDialog extends ProgressDialog {
    public CustomProgressDialog(Context context) {
        super(context);
        setMessage("处理中...");
        setMax(100);
        setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        setCanceledOnTouchOutside(false);
        setCancelable(true);
    }

    @Override
    public void dismiss() {
        super.dismiss();
        setProgress(0);
    }

    @Override
    public void cancel() {
        super.cancel();
        setProgress(0);
    }
}
