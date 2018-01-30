package com.qiniu.pili.droid.shortvideo.demo.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.qiniu.android.utils.StringUtils;
import com.qiniu.pili.droid.shortvideo.PLMediaFile;
import com.qiniu.pili.droid.shortvideo.PLShortVideoTranscoder;
import com.qiniu.pili.droid.shortvideo.PLVideoSaveListener;
import com.qiniu.pili.droid.shortvideo.demo.R;
import com.qiniu.pili.droid.shortvideo.demo.utils.Config;
import com.qiniu.pili.droid.shortvideo.demo.utils.GetPathFromUri;
import com.qiniu.pili.droid.shortvideo.demo.utils.RecordSettings;
import com.qiniu.pili.droid.shortvideo.demo.utils.ToastUtils;
import com.qiniu.pili.droid.shortvideo.demo.view.CustomProgressDialog;

import java.io.File;

public class VideoTranscodeActivity extends AppCompatActivity {
    private static final String TAG = "VideoTranscodeActivity";

    private Spinner mTranscodingBitrateLevelSpinner;
    private Spinner mTranscodingRotationSpinner;
    private EditText mTranscodingWidthEditText;
    private EditText mTranscodingHeightEditText;
    private CustomProgressDialog mProcessingDialog;

    private PLShortVideoTranscoder mShortVideoTranscoder;
    private PLMediaFile mMediaFile;
    private TextView mVideoFilePathText;
    private TextView mVideoSizeText;
    private TextView mVideoBitrateText;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle("VideoTranscode");
        setContentView(R.layout.activity_transcode);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(false);
        }

        setTitle(R.string.title_transcode);

        mVideoFilePathText = (TextView) findViewById(R.id.SrcVideoPathText);
        mVideoSizeText = (TextView) findViewById(R.id.SrcVideoSizeText);
        mVideoBitrateText = (TextView) findViewById(R.id.SrcVideoBitrateText);

        mTranscodingBitrateLevelSpinner = (Spinner) findViewById(R.id.TranscodingBitrateLevelSpinner);
        mTranscodingRotationSpinner = (Spinner) findViewById(R.id.TranscodingRotationSpinner);
        mTranscodingWidthEditText = (EditText) findViewById(R.id.TranscodingWidth);
        mTranscodingHeightEditText = (EditText) findViewById(R.id.TranscodingHeight);

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, RecordSettings.ENCODING_BITRATE_LEVEL_TIPS_ARRAY);
        mTranscodingBitrateLevelSpinner.setAdapter(adapter);
        mTranscodingBitrateLevelSpinner.setSelection(2);

        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, RecordSettings.ROTATION_LEVEL_TIPS_ARRAY);
        mTranscodingRotationSpinner.setAdapter(adapter);
        mTranscodingRotationSpinner.setSelection(0);

        mProcessingDialog = new CustomProgressDialog(this);
        mProcessingDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                mShortVideoTranscoder.cancelTranscode();
            }
        });

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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            String selectedFilepath = GetPathFromUri.getPath(this, data.getData());
            Log.i(TAG, "Select file: " + selectedFilepath);
            if (!StringUtils.isNullOrEmpty(selectedFilepath)) {
                onVideoFileSelected(selectedFilepath);
                return;
            }
        }

        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mMediaFile != null) {
            mMediaFile.release();
        }
    }

    private void onVideoFileSelected(String filepath) {
        mShortVideoTranscoder = new PLShortVideoTranscoder(this, filepath, Config.TRANSCODE_FILE_PATH);
        mMediaFile = new PLMediaFile(filepath);
        mVideoFilePathText.setText(new File(filepath).getName());
        mVideoSizeText.setText(mMediaFile.getVideoWidth() + " x " + mMediaFile.getVideoHeight());
        mTranscodingWidthEditText.setText(String.valueOf(mMediaFile.getVideoWidth()), TextView.BufferType.EDITABLE);
        mTranscodingHeightEditText.setText(String.valueOf(mMediaFile.getVideoHeight()), TextView.BufferType.EDITABLE);
        String bitrate = (mMediaFile.getVideoBitrate() / 1000) + " kbps";
        mVideoBitrateText.setText(bitrate);
    }

    public void onClickTranscode(View v) {
        doTranscode(false);
    }

    public void onClickReverse(View v) {
        doTranscode(true);
    }

    private void doTranscode(boolean isReverse) {
        if (mShortVideoTranscoder == null) {
            ToastUtils.s(this, "请先选择转码文件！");
            return;
        }

        int transcodingBitrateLevel = mTranscodingBitrateLevelSpinner.getSelectedItemPosition();
        int transcodingRotationLevel = mTranscodingRotationSpinner.getSelectedItemPosition();
        int transcodingWidth = Integer.parseInt(mTranscodingWidthEditText.getText().toString());
        int transcodingHeight = Integer.parseInt(mTranscodingHeightEditText.getText().toString());

        mProcessingDialog.show();

        mShortVideoTranscoder.transcode(
                transcodingWidth, transcodingHeight,
                RecordSettings.ENCODING_BITRATE_LEVEL_ARRAY[transcodingBitrateLevel],
                RecordSettings.ROTATION_LEVEL_ARRAY[transcodingRotationLevel],
                isReverse, new PLVideoSaveListener() {
                    @Override
                    public void onSaveVideoSuccess(final String s) {
                        Log.i(TAG, "save success: " + s);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mProcessingDialog.dismiss();
                                showChooseDialog(s);
                            }
                        });
                    }

                    @Override
                    public void onSaveVideoFailed(final int errorCode) {
                        Log.i(TAG, "save failed: " + errorCode);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mProcessingDialog.dismiss();
                                ToastUtils.toastErrorCode(VideoTranscodeActivity.this, errorCode);
                            }
                        });
                    }

                    @Override
                    public void onSaveVideoCanceled() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mProcessingDialog.dismiss();
                            }
                        });
                    }

                    @Override
                    public void onProgressUpdate(final float percentage) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mProcessingDialog.setProgress((int) (100 * percentage));
                            }
                        });
                    }
                });
    }

    private void showChooseDialog(final String filePath) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.if_edit_video));
        builder.setPositiveButton(getString(R.string.dlg_yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                VideoEditActivity.start(VideoTranscodeActivity.this, filePath);
            }
        });
        builder.setNegativeButton(getString(R.string.dlg_no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                PlaybackActivity.start(VideoTranscodeActivity.this, filePath);
            }
        });
        builder.setCancelable(false);
        builder.create().show();
    }

}
