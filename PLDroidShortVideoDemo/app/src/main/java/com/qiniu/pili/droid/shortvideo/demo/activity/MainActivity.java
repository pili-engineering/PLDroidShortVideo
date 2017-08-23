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

    private Spinner mRecordOrientationSpinner;
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

        mRecordOrientationSpinner = (Spinner) findViewById(R.id.RecordOrientationSpinner);
        mPreviewSizeRatioSpinner = (Spinner) findViewById(R.id.PreviewSizeRatioSpinner);
        mPreviewSizeLevelSpinner = (Spinner) findViewById(R.id.PreviewSizeLevelSpinner);
        mEncodingSizeLevelSpinner = (Spinner) findViewById(R.id.EncodingSizeLevelSpinner);
        mEncodingBitrateLevelSpinner = (Spinner) findViewById(R.id.EncodingBitrateLevelSpinner);

        ArrayAdapter<String> adapter0 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, RecordSettings.RECORD_ORIENTATION_TIPS_ARRAY);
        mRecordOrientationSpinner.setAdapter(adapter0);

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

    private boolean isPermissionOK() {
        PermissionChecker checker = new PermissionChecker(this);
        boolean isPermissionOK = Build.VERSION.SDK_INT < Build.VERSION_CODES.M || checker.checkPermission();
        if (!isPermissionOK) {
            ToastUtils.s(this, "Some permissions is not approved !!!");
        }
        return isPermissionOK;
    }

    public void onClickCapture(View v) {
        if (isPermissionOK()) {
            jumpToCaptureActivity();
        }
    }

    public void onClickImport(View v) {
        if (isPermissionOK()) {
            jumpToImportActivity();
        }
    }

    public void onClickTranscode(View v) {
        if (isPermissionOK()) {
            jumpToTranscodeActivity();
        }
    }

    public void onClickMakeGIF(View v) {
        if (isPermissionOK()) {
            jumpToMakeGIFActivity();
        }
    }
  
    public void onClickScreenRecord(View v) {
        if (isPermissionOK()) {
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

    private void jumpToMakeGIFActivity() {
        Intent intent = new Intent(MainActivity.this, MakeGIFActivity.class);
        startActivity(intent);
    }
  
    private void jumpToScreenRecordActivity() {
        Intent intent = new Intent(MainActivity.this, ScreenRecordActivity.class);
        startActivity(intent);
    }

    public void jumpToCaptureActivity() {
        Intent intent = new Intent(MainActivity.this, VideoRecordActivity.class);
        intent.putExtra(VideoRecordActivity.RECORD_ORIENTATION_LANDSCAPE, mRecordOrientationSpinner.getSelectedItemPosition() == 1);
        intent.putExtra(VideoRecordActivity.PREVIEW_SIZE_RATIO, mPreviewSizeRatioSpinner.getSelectedItemPosition());
        intent.putExtra(VideoRecordActivity.PREVIEW_SIZE_LEVEL, mPreviewSizeLevelSpinner.getSelectedItemPosition());
        intent.putExtra(VideoRecordActivity.ENCODING_SIZE_LEVEL, mEncodingSizeLevelSpinner.getSelectedItemPosition());
        intent.putExtra(VideoRecordActivity.ENCODING_BITRATE_LEVEL, mEncodingBitrateLevelSpinner.getSelectedItemPosition());
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
