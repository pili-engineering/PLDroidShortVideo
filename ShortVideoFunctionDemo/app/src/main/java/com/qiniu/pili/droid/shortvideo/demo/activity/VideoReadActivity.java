package com.qiniu.pili.droid.shortvideo.demo.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;

import com.qiniu.android.utils.StringUtils;
import com.qiniu.pili.droid.shortvideo.PLShortVideoReader;
import com.qiniu.pili.droid.shortvideo.PLVideoReadListener;
import com.qiniu.pili.droid.shortvideo.demo.R;
import com.qiniu.pili.droid.shortvideo.demo.utils.GetPathFromUri;
import com.qiniu.pili.droid.shortvideo.demo.utils.ToastUtils;

public class VideoReadActivity extends Activity {
    private static final String TAG = "VideoReadActivity";

    private PLShortVideoReader mReader;
    private String mPath;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("VideoRead");
        setContentView(R.layout.activity_frame_read);
        mReader = new PLShortVideoReader(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK && data.getData() != null) {
            String selectedFilepath = GetPathFromUri.getRealPathFromURI(this, data.getData());
            Log.i(TAG, "Select file: " + selectedFilepath);
            if (!StringUtils.isNullOrEmpty(selectedFilepath)) {
                mPath = selectedFilepath;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mReader.cancelRead();
    }

    public void onClickChooseFile(View c) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("video/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, 0);
    }

    public void onClickStartRead(View c) {
        if (mPath == null) {
            ToastUtils.showShortToast("请选择文件");
            return;
        }
        mReader.startRead(mPath, new PLVideoReadListener() {

            @Override
            public void onStart() {
                Log.i(TAG, "start");
            }

            @Override
            public void onDrawFrame(int texId, int texWidth, int texHeight, int rotation, long timestampNs, float[] transformMatrix) {
                Log.i(TAG, "onDrawFrame: texId=" + texId + ", width=" + texWidth + ", height=" + texHeight + ",rotation=" + rotation + ", time=" + timestampNs);
            }

            @Override
            public void onFinish() {
                Log.i(TAG, "finish");
            }

            @Override
            public void onCancel() {
                Log.i(TAG, "cancel");
            }

            @Override
            public void onError(int error) {
                Log.i(TAG, "error=" + error);
            }
        });
    }

    public void onClickCancelRead(View c) {
        mReader.cancelRead();
    }

}