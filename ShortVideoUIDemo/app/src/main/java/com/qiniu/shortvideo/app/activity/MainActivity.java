package com.qiniu.shortvideo.app.activity;

import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Build;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.qiniu.shortvideo.app.R;
import com.qiniu.shortvideo.app.utils.Config;
import com.qiniu.shortvideo.app.utils.GlideImageLoader;
import com.qiniu.shortvideo.app.utils.PermissionChecker;
import com.qiniu.shortvideo.app.utils.ToastUtils;
import com.youth.banner.Banner;
import com.youth.banner.BannerConfig;
import com.youth.banner.listener.OnBannerListener;

import java.util.ArrayList;
import java.util.List;

/**
 * 七牛短视频主界面
 */
public class MainActivity extends AppCompatActivity implements OnBannerListener {

    private Banner mImageBanner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setTheme(R.style.LaunchScreenTheme);
        setContentView(R.layout.activity_main);

        List<Integer> images = new ArrayList<>();
        TypedArray imgArrays = getResources().obtainTypedArray(R.array.banner_img);
        for (int i = 0; i < imgArrays.length(); i++) {
            images.add(imgArrays.getResourceId(i, 0));
        }
        imgArrays.recycle();

        mImageBanner = findViewById(R.id.image_banner);
        mImageBanner.setBannerStyle(BannerConfig.CIRCLE_INDICATOR)
                .setImageLoader(new GlideImageLoader())
                .setImages(images)
                .setDelayTime(1500)
                .setIndicatorGravity(BannerConfig.CENTER)
                .setOnBannerListener(this)
                .start();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mImageBanner.startAutoPlay();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mImageBanner.stopAutoPlay();
    }

    public void onClickVideoRecording(View v) {
        if (!isPermissionOK()) {
            return;
        }
        Intent intent = new Intent(MainActivity.this, VideoRecordActivity.class);
        intent.putExtra(VideoRecordActivity.PREVIEW_SIZE_RATIO, ConfigActivity.PREVIEW_SIZE_RATIO_POS);
        intent.putExtra(VideoRecordActivity.PREVIEW_SIZE_LEVEL, ConfigActivity.PREVIEW_SIZE_LEVEL_POS);
        intent.putExtra(VideoRecordActivity.ENCODING_MODE, ConfigActivity.ENCODING_MODE_LEVEL_POS);
        intent.putExtra(VideoRecordActivity.ENCODING_SIZE_LEVEL,ConfigActivity.ENCODING_SIZE_LEVEL_POS);
        intent.putExtra(VideoRecordActivity.ENCODING_BITRATE_LEVEL, ConfigActivity.ENCODING_BITRATE_LEVEL_POS);
        intent.putExtra(VideoRecordActivity.AUDIO_CHANNEL_NUM, ConfigActivity.AUDIO_CHANNEL_NUM_POS);
        startActivity(intent);
    }

    public void onClickVideoMix(View v) {
        if (!isPermissionOK()) {
            return;
        }
        Intent intent = new Intent(MainActivity.this, MediaSelectActivity.class);
        intent.putExtra(MediaSelectActivity.TYPE, MediaSelectActivity.TYPE_VIDEO_MIX);
        startActivity(intent);
    }

    public void onClickVideoEdit(View v) {
        if (!isPermissionOK()) {
            return;
        }
        Intent intent = new Intent(MainActivity.this, MediaSelectActivity.class);
        intent.putExtra(MediaSelectActivity.TYPE, MediaSelectActivity.TYPE_VIDEO_EDIT);
        startActivity(intent);
    }

    public void onClickVideoPuzzle(View v) {
        if (!isPermissionOK()) {
            return;
        }
        Intent intent = new Intent(MainActivity.this, VideoPuzzleConfigActivity.class);
        startActivity(intent);
    }

    public void onClickVideoSplice(View v) {
        if (!isPermissionOK()) {
            return;
        }
        Intent intent = new Intent(MainActivity.this, MediaSelectActivity.class);
        intent.putExtra(MediaSelectActivity.TYPE, MediaSelectActivity.TYPE_MEDIA_COMPOSE);
        startActivity(intent);
    }

    public void onClickFunctionList(View v) {
        jumpToWebActivity(Config.FUNCTION_LIST_PATH);
    }

    public void onClickSetting(View v) {
        Intent intent = new Intent(MainActivity.this, ConfigActivity.class);
        startActivity(intent);
    }

    private boolean isPermissionOK() {
        PermissionChecker checker = new PermissionChecker(this);
        boolean isPermissionOK = Build.VERSION.SDK_INT < Build.VERSION_CODES.M || checker.checkPermission();
        if (!isPermissionOK) {
            ToastUtils.s(this, "Some permissions is not approved !!!");
        }
        return isPermissionOK;
    }

    private void jumpToWebActivity(String url) {
        if (!isPermissionOK()) {
            return;
        }
        Intent intent = new Intent(MainActivity.this, WebDisplayActivity.class);
        intent.putExtra(WebDisplayActivity.URL, url);
        startActivity(intent);
    }

    @Override
    public void OnBannerClick(int position) {
        String[] urls = getResources().getStringArray(R.array.banner_url);
        jumpToWebActivity(urls[position]);
    }
}
