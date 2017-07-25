package com.qiniu.pili.droid.shortvideo.demo.activity;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.qiniu.pili.droid.shortvideo.PLShortVideoTranscoder;
import com.qiniu.pili.droid.shortvideo.PLVideoSaveListener;
import com.qiniu.pili.droid.shortvideo.demo.R;
import com.qiniu.pili.droid.shortvideo.demo.utils.Config;
import com.qiniu.pili.droid.shortvideo.demo.utils.GetPathFromUri;
import com.qiniu.pili.droid.shortvideo.demo.utils.RecordSettings;
import com.qiniu.pili.droid.shortvideo.demo.utils.ToastUtils;

import java.io.File;

public class VideoTranscodeActivity extends AppCompatActivity {
    private static final String TAG = "VideoTranscodeActivity";

    private Spinner mTranscodingBitrateLevelSpinner;
    private EditText mTranscodingWidthEditText;
    private EditText mTranscodingHeightEditText;
    private ProgressDialog mProcessingDialog;

    private PLShortVideoTranscoder mShortVideoTranscoder;
    private TextView mVideoFilePathText;
    private TextView mVideoSizeText;
    private TextView mVideoBitrateText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("VideoTranscode");
        setContentView(R.layout.activity_transcode);

        mVideoFilePathText = (TextView) findViewById(R.id.SrcVideoPathText);
        mVideoSizeText = (TextView) findViewById(R.id.SrcVideoSizeText);
        mVideoBitrateText = (TextView) findViewById(R.id.SrcVideoBitrateText);

        mTranscodingBitrateLevelSpinner = (Spinner) findViewById(R.id.TranscodingBitrateLevelSpinner);
        mTranscodingWidthEditText = (EditText) findViewById(R.id.TranscodingWidth);
        mTranscodingHeightEditText = (EditText) findViewById(R.id.TranscodingHeight);

        ArrayAdapter<String> adapter4 = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, RecordSettings.ENCODING_BITRATE_LEVEL_TIPS_ARRAY);
        mTranscodingBitrateLevelSpinner.setAdapter(adapter4);
        mTranscodingBitrateLevelSpinner.setSelection(2);

        mProcessingDialog = new ProgressDialog(this);
        mProcessingDialog.setMessage("处理中...");
        mProcessingDialog.setCancelable(false);

        Intent intent = new Intent();
        if (Build.VERSION.SDK_INT < 19) {
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("video/*");
        } else {
            intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("video/*");
        }
        startActivityForResult(Intent.createChooser(intent, "选择要转码的视频"), 0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            String selectedFilepath = GetPathFromUri.getPath(this, data.getData());
            Log.i(TAG, "Select file: " + selectedFilepath);
            if (selectedFilepath != null && !"".equals(selectedFilepath)) {
                onVideoFileSelected(selectedFilepath);
            }
        }
    }

    private void onVideoFileSelected(String filepath) {
        mShortVideoTranscoder = new PLShortVideoTranscoder(this, filepath, Config.TRANSCODE_FILE_PATH);
        mVideoFilePathText.setText(new File(filepath).getName());
        mVideoSizeText.setText(mShortVideoTranscoder.getSrcWidth() + " x " + mShortVideoTranscoder.getSrcHeight());
        mTranscodingWidthEditText.setText(String.valueOf(mShortVideoTranscoder.getSrcWidth()), TextView.BufferType.EDITABLE);
        mTranscodingHeightEditText.setText(String.valueOf(mShortVideoTranscoder.getSrcHeight()), TextView.BufferType.EDITABLE);
        String bitrate = (mShortVideoTranscoder.getSrcBitrate() / 1000) + " kbps";
        mVideoBitrateText.setText(bitrate);
    }

    public void onClickTranscode(View v) {
        if (mShortVideoTranscoder == null) {
            ToastUtils.s(this, "请先选择转码文件！");
            return;
        }

        int transcodingBitrateLevel = mTranscodingBitrateLevelSpinner.getSelectedItemPosition();
        int transcodingWidth = Integer.parseInt(mTranscodingWidthEditText.getText().toString());
        int transcodingHeight = Integer.parseInt(mTranscodingHeightEditText.getText().toString());

        mProcessingDialog.show();

        mShortVideoTranscoder.transcode(transcodingWidth, transcodingHeight, getEncodingBitrateLevel(transcodingBitrateLevel), new PLVideoSaveListener() {
            @Override
            public void onSaveVideoSuccess(String s) {
                Log.i(TAG, "save success: " + s);
                mProcessingDialog.dismiss();
                PlaybackActivity.start(VideoTranscodeActivity.this, s);
            }

            @Override
            public void onSaveVideoFailed(final int errorCode) {
                Log.i(TAG, "save failed: " + errorCode);
                mProcessingDialog.dismiss();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        ToastUtils.s(VideoTranscodeActivity.this, "transcode failed: " + errorCode);
                    }
                });
            }
        });
    }

    private int getEncodingBitrateLevel(int position) {
        return RecordSettings.ENCODING_BITRATE_LEVEL_ARRAY[position];
    }

}
