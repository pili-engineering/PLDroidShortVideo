package com.qiniu.pili.droid.shortvideo.demo.activity;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;

import com.qiniu.pili.droid.shortvideo.PLAuthenticationResultCallback;
import com.qiniu.pili.droid.shortvideo.PLShortVideoEnv;
import com.qiniu.pili.droid.shortvideo.demo.BuildConfig;
import com.qiniu.pili.droid.shortvideo.demo.R;
import com.qiniu.pili.droid.shortvideo.demo.utils.PermissionChecker;
import com.qiniu.pili.droid.shortvideo.demo.utils.ToastUtils;
import com.qiniu.pili.droid.shortvideo.demo.view.ItemView;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ItemView itemVersionInfo = (ItemView) findViewById(R.id.item_version_info);
        itemVersionInfo.setValue("" + getVersionDescription());

        ItemView itemCompileInfo = (ItemView) findViewById(R.id.item_compile_info);
        itemCompileInfo.setValue("" + getBuildTimeDescription());

        PLShortVideoEnv.checkAuthentication(getApplicationContext(), new PLAuthenticationResultCallback() {
            @Override
            public void onAuthorizationResult(int result) {
                if (result == PLAuthenticationResultCallback.UnCheck) {
                    ToastUtils.showShortToast(MainActivity.this, "UnCheck");
                } else if (result == PLAuthenticationResultCallback.UnAuthorized) {
                    ToastUtils.showShortToast(MainActivity.this, "UnAuthorized");
                } else {
                    ToastUtils.showShortToast(MainActivity.this, "Authorized");
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private boolean isPermissionOK() {
        PermissionChecker checker = new PermissionChecker(this);
        boolean isPermissionOK = Build.VERSION.SDK_INT < Build.VERSION_CODES.M || checker.checkPermission();
        if (!isPermissionOK) {
            ToastUtils.showShortToast(this, "Some permissions is not approved !!!");
        }
        return isPermissionOK;
    }

    public void onClickCapture(View v) {
        if (isPermissionOK()) {
            jumpToCaptureActivity();
        }
    }

    public void onClickAudioCapture(View v) {
        if (isPermissionOK()) {
            jumpToAudioCaptureActivity();
        }
    }

    public void onClickImportAndRecord(View v) {
        if (isPermissionOK()) {
            Intent intent = new Intent(this, ImportAndEditActivity.class);
            startActivity(intent);
        }
    }

    public void onClickMixRecord(View v) {
        if (isPermissionOK()) {
            jumpToActivity(VideoMixRecordConfigActivity.class);
        }
    }

    public void onClickMakeGIF(View v) {
        if (isPermissionOK()) {
            jumpToActivity(MakeGIFActivity.class);
        }
    }

    public void onClickScreenRecord(View v) {
        if (isPermissionOK()) {
            jumpToActivity(ScreenRecordActivity.class);
        }
    }

    public void onClickImageCompose(View v) {
        if (isPermissionOK()) {
            jumpToActivity(ImageComposeActivity.class);
        }
    }

    public void onClickImageCompose2(View v) {
        if (isPermissionOK()) {
            jumpToActivity(ImageComposeWithTransitionActivity.class);
        }
    }

    public void onClickDraftBox(View v) {
        if (isPermissionOK()) {
            jumpToActivity(DraftBoxActivity.class);
        }
    }

    public void onClickVideoMix(View v) {
        if (isPermissionOK()) {
            jumpToActivity(VideoMixActivity.class);
        }
    }

    private void jumpToActivity(Class<?> cls) {
        Intent intent = new Intent(MainActivity.this, cls);
        startActivity(intent);
    }

    public void jumpToCaptureActivity() {
        Intent intent = new Intent(MainActivity.this, VideoRecordActivity.class);
        intent.putExtra(VideoRecordActivity.PREVIEW_SIZE_RATIO, ConfigActivity.PREVIEW_SIZE_RATIO_POS);
        intent.putExtra(VideoRecordActivity.PREVIEW_SIZE_LEVEL, ConfigActivity.PREVIEW_SIZE_LEVEL_POS);
        intent.putExtra(VideoRecordActivity.ENCODING_MODE, ConfigActivity.ENCODING_MODE_LEVEL_POS);
        intent.putExtra(VideoRecordActivity.ENCODING_SIZE_LEVEL, ConfigActivity.ENCODING_SIZE_LEVEL_POS);
        intent.putExtra(VideoRecordActivity.ENCODING_BITRATE_LEVEL, ConfigActivity.ENCODING_BITRATE_LEVEL_POS);
        intent.putExtra(VideoRecordActivity.AUDIO_CHANNEL_NUM, ConfigActivity.AUDIO_CHANNEL_NUM_POS);
        startActivity(intent);
    }

    public void jumpToAudioCaptureActivity() {
        Intent intent = new Intent(MainActivity.this, AudioRecordActivity.class);
        intent.putExtra(AudioRecordActivity.ENCODING_MODE, ConfigActivity.ENCODING_MODE_LEVEL_POS);
        intent.putExtra(AudioRecordActivity.AUDIO_CHANNEL_NUM, ConfigActivity.AUDIO_CHANNEL_NUM_POS);
        startActivity(intent);
    }

    private String getVersionDescription() {
        PackageManager packageManager = getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(getPackageName(), 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "未知";
    }

    protected String getBuildTimeDescription() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA).format(BuildConfig.BUILD_TIMESTAMP);
    }

    public void onClickSetting(View view) {
        jumpToActivity(ConfigActivity.class);
    }
}
