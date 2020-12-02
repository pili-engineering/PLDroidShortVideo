package com.qiniu.pili.droid.shortvideo.demo.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;

import com.qiniu.pili.droid.shortvideo.demo.R;
import com.qiniu.pili.droid.shortvideo.demo.utils.PermissionChecker;
import com.qiniu.pili.droid.shortvideo.demo.utils.ToastUtils;

public class ImportAndEditActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import_and_edit);
    }

    public void onClickImport(View v) {
        if (isPermissionOK()) {
            jumpToActivity(VideoTrimActivity.class);
        }
    }

    public void onClickTransitionMake(View v) {
        if (isPermissionOK()) {
            jumpToActivity(VideoDivideActivity.class);
        }
    }

    public void onClickVideoCompose(View v) {
        if (isPermissionOK()) {
            jumpToActivity(VideoComposeActivity.class);
        }
    }

    public void onClickTranscode(View v) {
        if (isPermissionOK()) {
            jumpToActivity(VideoTranscodeActivity.class);
        }
    }

    public void onClickMultipleCompose(View v) {
        if (isPermissionOK()) {
            jumpToActivity(MultipleComposeActivity.class);
        }
    }

    public void onClickExternalMediaRecord(View v) {
        if (isPermissionOK()) {
            jumpToActivity(ExternalMediaRecordActivity.class);
        }
    }

    private boolean isPermissionOK() {
        PermissionChecker checker = new PermissionChecker(this);
        boolean isPermissionOK = Build.VERSION.SDK_INT < Build.VERSION_CODES.M || checker.checkPermission();
        if (!isPermissionOK) {
            ToastUtils.s(this, "Some permissions is not approved !!!");
        }
        return isPermissionOK;
    }

    private void jumpToActivity(Class<?> cls) {
        Intent intent = new Intent(ImportAndEditActivity.this, cls);
        startActivity(intent);
    }

    public void onClickBack(View view) {
        finish();
    }
}
