package com.qiniu.pili.droid.shortvideo.demo.activity;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.qiniu.pili.droid.shortvideo.demo.BuildConfig;
import com.qiniu.pili.droid.shortvideo.demo.R;
import com.qiniu.pili.droid.shortvideo.demo.utils.PermissionChecker;
import com.qiniu.pili.droid.shortvideo.demo.utils.RecordSettings;
import com.qiniu.pili.droid.shortvideo.demo.utils.ToastUtils;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private Spinner mPreviewSizeRatioSpinner;
    private Spinner mPreviewSizeLevelSpinner;
    private Spinner mEncodingSizeLevelSpinner;
    private Spinner mEncodingBitrateLevelSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView versionInfoTextView = (TextView) findViewById(R.id.VersionInfoTextView);
        String info = "版本号：" + getVersionDescription() + "，编译时间：" + getBuildTimeDescription();
        versionInfoTextView.setText(info);

        mPreviewSizeRatioSpinner = (Spinner) findViewById(R.id.PreviewSizeRatioSpinner);
        mPreviewSizeLevelSpinner = (Spinner) findViewById(R.id.PreviewSizeLevelSpinner);
        mEncodingSizeLevelSpinner = (Spinner) findViewById(R.id.EncodingSizeLevelSpinner);
        mEncodingBitrateLevelSpinner = (Spinner) findViewById(R.id.EncodingBitrateLevelSpinner);

        ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, RecordSettings.PREVIEW_SIZE_RATIO_TIPS_ARRAY);
        mPreviewSizeRatioSpinner.setAdapter(adapter1);

        ArrayAdapter<String> adapter2 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, RecordSettings.PREVIEW_SIZE_LEVEL_TIPS_ARRAY);
        mPreviewSizeLevelSpinner.setAdapter(adapter2);
        mPreviewSizeLevelSpinner.setSelection(3);

        ArrayAdapter<String> adapter3= new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, RecordSettings.ENCODING_SIZE_LEVEL_TIPS_ARRAY);
        mEncodingSizeLevelSpinner.setAdapter(adapter3);
        mEncodingSizeLevelSpinner.setSelection(10);

        ArrayAdapter<String> adapter4 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, RecordSettings.ENCODING_BITRATE_LEVEL_TIPS_ARRAY);
        mEncodingBitrateLevelSpinner.setAdapter(adapter4);
        mEncodingBitrateLevelSpinner.setSelection(2);
    }

    public void onClickCapture(View v) {
        PermissionChecker checker = new PermissionChecker(this);
        boolean isPermissionOK = Build.VERSION.SDK_INT < Build.VERSION_CODES.M || checker.checkPermission();
        if (!isPermissionOK) {
            ToastUtils.s(this, "Some permissions is not approved !!!");
        } else {
            jumpToCaptureActivity(VideoRecordActivity.class);
        }
    }

    public void onClickImport(View v) {
        PermissionChecker checker = new PermissionChecker(this);
        boolean isPermissionOK = Build.VERSION.SDK_INT < Build.VERSION_CODES.M || checker.checkPermission();
        if (!isPermissionOK) {
            ToastUtils.s(this, "Some permissions is not approved !!!");
        } else {
            jumpToImportActivity();
        }
    }

    public void onClickTranscode(View v) {
        PermissionChecker checker = new PermissionChecker(this);
        boolean isPermissionOK = Build.VERSION.SDK_INT < Build.VERSION_CODES.M || checker.checkPermission();
        if (!isPermissionOK) {
            ToastUtils.s(this, "Some permissions is not approved !!!");
        } else {
            jumpToTranscodeActivity();
        }
    }

    public void onClickScreenRecord(View v) {
        PermissionChecker checker = new PermissionChecker(this);
        boolean isPermissionOK = Build.VERSION.SDK_INT < Build.VERSION_CODES.M || checker.checkPermission();
        if (!isPermissionOK) {
            ToastUtils.s(this, "Some permissions is not approved !!!");
        } else {
            jumpToScreenRecordActivity();
        }
    }

    private void jumpToImportActivity() {
        Intent intent = new Intent(MainActivity.this, VideoTrimActivity.class);
        startActivity(intent);
    }

    private void jumpToTranscodeActivity() {
        Intent intent = new Intent(MainActivity.this, VideoTranscodeActivity.class);
        startActivity(intent);
    }

    private void jumpToScreenRecordActivity() {
        Intent intent = new Intent(MainActivity.this, ScreenRecordActivity.class);
        startActivity(intent);
    }

    public void jumpToCaptureActivity(Class<?> cls) {
        Intent intent = new Intent(MainActivity.this, cls);
        intent.putExtra("PreviewSizeRatio", mPreviewSizeRatioSpinner.getSelectedItemPosition());
        intent.putExtra("PreviewSizeLevel", mPreviewSizeLevelSpinner.getSelectedItemPosition());
        intent.putExtra("EncodingSizeLevel", mEncodingSizeLevelSpinner.getSelectedItemPosition());
        intent.putExtra("EncodingBitrateLevel", mEncodingBitrateLevelSpinner.getSelectedItemPosition());
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
}
