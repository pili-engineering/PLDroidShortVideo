package com.qiniu.pili.droid.shortvideo.demo.activity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.qiniu.pili.droid.shortvideo.PLErrorCode;
import com.qiniu.pili.droid.shortvideo.PLMicrophoneSetting;
import com.qiniu.pili.droid.shortvideo.PLScreenRecordStateListener;
import com.qiniu.pili.droid.shortvideo.PLScreenRecorder;
import com.qiniu.pili.droid.shortvideo.PLScreenRecorderSetting;
import com.qiniu.pili.droid.shortvideo.demo.R;
import com.qiniu.pili.droid.shortvideo.demo.utils.Config;
import com.qiniu.pili.droid.shortvideo.demo.utils.PermissionChecker;
import com.qiniu.pili.droid.shortvideo.demo.utils.ToastUtils;

public class ScreenRecordActivity extends AppCompatActivity implements PLScreenRecordStateListener {
    private static final String TAG = "ScreenRecordActivity";

    private PLScreenRecorder mScreenRecorder;
    private TextView mTipTextView;

    private void requestScreenRecord() {
        if (mScreenRecorder == null) {
            DisplayMetrics metrics = getResources().getDisplayMetrics();
            int width = metrics.widthPixels;
            int height = metrics.heightPixels;
            int dpi = metrics.densityDpi;
            mScreenRecorder = new PLScreenRecorder(this);
            mScreenRecorder.setRecordStateListener(this);
            PLScreenRecorderSetting screenSetting = new PLScreenRecorderSetting();
            screenSetting.setRecordFile(Config.SCREEN_RECORD_FILE_PATH)
                            .setInputAudioEnabled(false)
                            .setSize(width, height)
                            .setDpi(dpi);
            PLMicrophoneSetting microphoneSetting = new PLMicrophoneSetting();
            mScreenRecorder.prepare(screenSetting, microphoneSetting);
        }
        mScreenRecorder.requestScreenRecord();
    }

    private boolean startScreenRecord(int requestCode, int resultCode, Intent data) {
        boolean isReady = mScreenRecorder.onActivityResult(requestCode, resultCode, data);
        if (isReady) {
            mScreenRecorder.start();
        }

        return isReady;
    }

    private void stopScreenRecord() {
        if (mScreenRecorder != null && mScreenRecorder.isRecording()) {
            mScreenRecorder.stop();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_record);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(false);
        }

        setTitle(R.string.title_screen_record);

        mTipTextView = (TextView) findViewById(R.id.tip);
        FloatingActionButton fab_rec = (FloatingActionButton) findViewById(R.id.fab_rec);
        fab_rec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PermissionChecker checker = new PermissionChecker(ScreenRecordActivity.this);
                boolean isPermissionOK = Build.VERSION.SDK_INT < Build.VERSION_CODES.M || checker.checkPermission();
                if (!isPermissionOK) {
                    ToastUtils.s(view.getContext(), "相关权限申请失败 !!!");
                    return;
                }

                if (mScreenRecorder != null && mScreenRecorder.isRecording()) {
                    stopScreenRecord();
                } else {
                    requestScreenRecord();
                    Snackbar.make(view, "正在申请录屏权限……", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                }
            }
        });

        FloatingActionButton fab_play = (FloatingActionButton) findViewById(R.id.fab_play);
        fab_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mScreenRecorder != null && mScreenRecorder.isRecording()) {
                    Snackbar.make(view, "正在录屏，不能播放！", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                    return;
                }

                PlaybackActivity.start(ScreenRecordActivity.this, Config.SCREEN_RECORD_FILE_PATH);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
            default:
                break;
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mScreenRecorder != null) {
            mScreenRecorder.stop();
            mScreenRecorder = null;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLScreenRecorder.REQUEST_CODE) {
            if (data == null) {
                String tip = "录屏申请启动失败！";
                Toast.makeText(this, tip, Toast.LENGTH_SHORT).show();
                ((TextView) findViewById(R.id.tip)).setText(tip);
                mScreenRecorder.stop();
                mScreenRecorder = null;
                return;
            }

            getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            if (startScreenRecord(requestCode, resultCode, data)) {
                Toast.makeText(this, "正在进行录屏...", Toast.LENGTH_SHORT).show();
                moveTaskToBack(true);
            }
        }
    }

    private void updateTip(String tip) {
        mTipTextView.setText(tip);
        Toast.makeText(this, tip, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onReady() {
        String tip = "录屏初始化成功！";
        updateTip(tip);
    }

    @Override
    public void onError(int code) {
        Log.e(TAG, "onError: code = " + code);
        if (code == PLErrorCode.ERROR_UNSUPPORTED_ANDROID_VERSION) {
            final String tip = "录屏只支持 Android 5.0 以上系统";
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    updateTip(tip);
                }
            });
        }
    }

    @Override
    public void onRecordStarted() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String tip = "正在录屏……";
                updateTip(tip);
            }
        });
    }

    @Override
    public void onRecordStopped() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String tip =  "已经停止录屏！";
                updateTip(tip);
            }
        });
    }

}
