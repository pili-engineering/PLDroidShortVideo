package com.qiniu.pili.droid.shortvideo.demo.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatCheckBox;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.qiniu.android.utils.StringUtils;
import com.qiniu.pili.droid.shortvideo.PLMediaFile;
import com.qiniu.pili.droid.shortvideo.PLMixAudioFile;
import com.qiniu.pili.droid.shortvideo.PLShortVideoTranscoder;
import com.qiniu.pili.droid.shortvideo.PLVideoSaveListener;
import com.qiniu.pili.droid.shortvideo.PLWatermarkSetting;
import com.qiniu.pili.droid.shortvideo.demo.R;
import com.qiniu.pili.droid.shortvideo.demo.utils.Config;
import com.qiniu.pili.droid.shortvideo.demo.utils.GetPathFromUri;
import com.qiniu.pili.droid.shortvideo.demo.utils.RecordSettings;
import com.qiniu.pili.droid.shortvideo.demo.utils.ToastUtils;
import com.qiniu.pili.droid.shortvideo.demo.view.CustomProgressDialog;
import java.io.File;

public class VideoTranscodeActivity extends AppCompatActivity {
    private static final String TAG = "VideoTranscodeActivity";

    private static final int REQUEST_MIX_AUDIO = 100;

    private CustomProgressDialog mProcessingDialog;

    private PLShortVideoTranscoder mShortVideoTranscoder;
    private PLMediaFile mMediaFile;
    private TextView mVideoFilePathText;
    private TextView mVideoSizeText;
    private TextView mVideoRotationText;
    private TextView mVideoSizeRotatedText;
    private TextView mVideoBitrateText;

    private EditText mTranscodingWidthEditText;
    private EditText mTranscodingHeightEditText;

    private EditText mTranscodingClipXText;
    private EditText mTranscodingClipYText;
    private EditText mTranscodingClipWidthText;
    private EditText mTranscodingClipHeightText;

    private EditText mTranscodingBitrateText;
    private Spinner mTranscodingRotationSpinner;
    private EditText mTranscodingMaxFPSEditText;

    private TextView mMixAudioFileText;
    private PLMixAudioFile mMixAudioFile;

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

        mMixAudioFileText = (TextView) findViewById(R.id.tv_mix_audio_file);
        mVideoFilePathText = (TextView) findViewById(R.id.SrcVideoPathText);
        mVideoSizeText = (TextView) findViewById(R.id.SrcVideoSizeText);
        mVideoRotationText = (TextView) findViewById(R.id.SrcVideoRotationText);
        mVideoSizeRotatedText = (TextView) findViewById(R.id.SrcVideoSizeRotatedText);
        mVideoBitrateText = (TextView) findViewById(R.id.SrcVideoBitrateText);

        mTranscodingWidthEditText = (EditText) findViewById(R.id.TranscodingWidth);
        mTranscodingHeightEditText = (EditText) findViewById(R.id.TranscodingHeight);
        mTranscodingMaxFPSEditText = (EditText) findViewById(R.id.TranscodingMaxFPS);

        mTranscodingClipXText = (EditText) findViewById(R.id.TranscodingClipX);
        mTranscodingClipYText = (EditText) findViewById(R.id.TranscodingClipY);
        mTranscodingClipWidthText = (EditText) findViewById(R.id.TranscodingClipWidth);
        mTranscodingClipHeightText = (EditText) findViewById(R.id.TranscodingClipHeight);

        mTranscodingBitrateText = (EditText) findViewById(R.id.TranscodingBitrate);
        mTranscodingRotationSpinner = (Spinner) findViewById(R.id.TranscodingRotationSpinner);

        ArrayAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, RecordSettings.ROTATION_LEVEL_TIPS_ARRAY);
        mTranscodingRotationSpinner.setAdapter(adapter);
        mTranscodingRotationSpinner.setSelection(0);

        mProcessingDialog = new CustomProgressDialog(this);
        mProcessingDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                mShortVideoTranscoder.cancelTranscode();
            }
        });

        ((AppCompatCheckBox)findViewById(R.id.cb_add_watermark)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    mShortVideoTranscoder.setWatermark(createWatermarkSetting());
                }else {
                    mShortVideoTranscoder.setWatermark(null);
                }
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
            if(requestCode == REQUEST_MIX_AUDIO){
                //增加混音文件
                String selectedFilepath = GetPathFromUri.getPath(this, data.getData());
                Log.i(TAG, "Select mix audio file: " + selectedFilepath);
                if (!StringUtils.isNullOrEmpty(selectedFilepath)) {
                    onMixAudioFileSelected(selectedFilepath);
                    return;
                }
            }else {
                String selectedFilepath = GetPathFromUri.getPath(this, data.getData());
                Log.i(TAG, "Select file: " + selectedFilepath);
                if (!StringUtils.isNullOrEmpty(selectedFilepath)) {
                    onVideoFileSelected(selectedFilepath);
                    return;
                }
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

    private void onMixAudioFileSelected(String filepath){
        mMixAudioFileText.setText(filepath);
        PLMediaFile mediaFile = new PLMediaFile(filepath);
        mShortVideoTranscoder.setMixAudioFile(filepath, 0, mediaFile.getDurationMs(), true);
    }

    private void onVideoFileSelected(String filepath) {
        mShortVideoTranscoder = new PLShortVideoTranscoder(this, filepath, Config.TRANSCODE_FILE_PATH);
        mMediaFile = new PLMediaFile(filepath);
        int bitrateInKbps = mMediaFile.getVideoBitrate() / 1000;
        int videoWidthRaw = mMediaFile.getVideoWidth();
        int videoHeightRaw = mMediaFile.getVideoHeight();
        int videoRotation = mMediaFile.getVideoRotation();
        int videoWidthRotated = videoRotation == 0 || videoRotation == 180 ? mMediaFile.getVideoWidth() : mMediaFile.getVideoHeight();
        int videoHeightRotated = videoRotation == 0 || videoRotation == 180 ? mMediaFile.getVideoHeight() : mMediaFile.getVideoWidth();
        int videoFrameRate = mMediaFile.getVideoFrameRate();

        mVideoFilePathText.setText(new File(filepath).getName());
        mVideoSizeText.setText(videoWidthRaw + " x " + videoHeightRaw);
        mVideoRotationText.setText("" + videoRotation);
        mVideoSizeRotatedText.setText(videoWidthRotated + " x " + videoHeightRotated);
        mVideoBitrateText.setText(bitrateInKbps + " kbps");

        mTranscodingWidthEditText.setText(String.valueOf(videoWidthRaw));
        mTranscodingHeightEditText.setText(String.valueOf(videoHeightRaw));
        mTranscodingMaxFPSEditText.setText(String.valueOf(videoFrameRate));
        mTranscodingClipWidthText.setText(String.valueOf(videoWidthRotated));
        mTranscodingClipHeightText.setText(String.valueOf(videoHeightRotated));
        mTranscodingBitrateText.setText(String.valueOf(bitrateInKbps));
    }

    public void onClickAddMixAudio(View c){
        chooseMixAudioFile();
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

        int transcodingBitrate = Integer.parseInt(mTranscodingBitrateText.getText().toString()) * 1000;
        int transcodingRotationLevel = mTranscodingRotationSpinner.getSelectedItemPosition();
        int transcodingWidth = Integer.parseInt(mTranscodingWidthEditText.getText().toString());
        int transcodingHeight = Integer.parseInt(mTranscodingHeightEditText.getText().toString());
        int transcodingMaxFPS = Integer.parseInt(mTranscodingMaxFPSEditText.getText().toString());
        if (transcodingMaxFPS > 0) {
            mShortVideoTranscoder.setMaxFrameRate(transcodingMaxFPS);
        }

        int clipWidth = Integer.parseInt(mTranscodingClipWidthText.getText().toString());
        int clipHeight = Integer.parseInt(mTranscodingClipHeightText.getText().toString());
        if (clipWidth > 0 && clipHeight > 0) {
            int clipX = Integer.parseInt(mTranscodingClipXText.getText().toString());
            int clipY = Integer.parseInt(mTranscodingClipYText.getText().toString());
            mShortVideoTranscoder.setClipArea(clipX, clipY, clipWidth, clipHeight);
        }

        boolean startResult = mShortVideoTranscoder.transcode(
                transcodingWidth, transcodingHeight, transcodingBitrate,
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

        if (startResult) {
            mProcessingDialog.show();
        } else {
            ToastUtils.s(this, "开始转码失败！");
        }
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

    private PLWatermarkSetting createWatermarkSetting() {
        PLWatermarkSetting watermarkSetting = new PLWatermarkSetting();
        watermarkSetting.setResourceId(R.drawable.qiniu_logo);
        watermarkSetting.setPosition(0.01f, 0.01f);
        watermarkSetting.setAlpha(128);
        return watermarkSetting;
    }

    private void chooseMixAudioFile(){
        Intent intent = new Intent();
        if (Build.VERSION.SDK_INT < 19) {
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("audio/*");
        } else {
            intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("audio/*");
        }
        startActivityForResult(Intent.createChooser(intent, "选择要的增加的混音文件"), REQUEST_MIX_AUDIO);
    }
}
