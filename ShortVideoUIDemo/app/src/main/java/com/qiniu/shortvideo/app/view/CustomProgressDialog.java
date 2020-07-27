package com.qiniu.shortvideo.app.view;

import android.app.ProgressDialog;
import android.content.Context;

/**
 * 自定义进度条对话框
 */
public class CustomProgressDialog extends ProgressDialog {
    public CustomProgressDialog(Context context) {
        super(context);
        setMessage("处理中...");
        setMax(100);
        setProgressStyle(ProgressDialog.STYLE_SPINNER);
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
