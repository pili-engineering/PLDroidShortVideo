package com.qiniu.pili.droid.shortvideo.demo.activity;

import android.app.Activity;
import android.content.Intent;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;

import com.qiniu.pili.droid.shortvideo.PLAudioEncodeSetting;
import com.qiniu.pili.droid.shortvideo.PLMicrophoneSetting;
import com.qiniu.pili.droid.shortvideo.PLRecordSetting;
import com.qiniu.pili.droid.shortvideo.PLRecordStateListener;
import com.qiniu.pili.droid.shortvideo.PLShortAudioRecorder;
import com.qiniu.pili.droid.shortvideo.PLShortVideoEditor;
import com.qiniu.pili.droid.shortvideo.PLVideoEditSetting;
import com.qiniu.pili.droid.shortvideo.PLVideoFilterListener;
import com.qiniu.pili.droid.shortvideo.PLVideoSaveListener;
import com.qiniu.pili.droid.shortvideo.demo.R;
import com.qiniu.pili.droid.shortvideo.demo.utils.Config;
import com.qiniu.pili.droid.shortvideo.demo.utils.MediaStoreUtils;
import com.qiniu.pili.droid.shortvideo.demo.utils.ToastUtils;
import com.qiniu.pili.droid.shortvideo.demo.view.CustomProgressDialog;
import com.qiniu.pili.droid.shortvideo.demo.view.SectionProgressBar;

import java.io.File;
import java.util.Stack;

public class VideoDubActivity extends Activity implements PLRecordStateListener, PLVideoSaveListener, PLVideoFilterListener {
    private static final String TAG = "VideoDubActivity";
    public static final String MP4_PATH = "MP4_PATH";
    public static final String DUB_MP4_PATH = "DUB_MP4_PATH";

    private PLShortVideoEditor mShortVideoEditor;
    private PLShortAudioRecorder mShortAudioRecorder;

    private SectionProgressBar mSectionProgressBar;
    private CustomProgressDialog mProcessingDialog;
    private View mRecordBtn;
    private View mDeleteBtn;

    private Stack<Integer> mSectionTimestamps;
    private boolean mIsRecordCompleted = false;
    private boolean mSectionBegan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_video_dub);

        GLSurfaceView previewView = findViewById(R.id.preview);

        mSectionProgressBar = findViewById(R.id.record_progressbar);
        mRecordBtn = findViewById(R.id.record);
        mDeleteBtn = findViewById(R.id.delete);

        mSectionTimestamps = new Stack<>();

        mProcessingDialog = new CustomProgressDialog(this);
        mProcessingDialog.setOnCancelListener(dialog -> mShortAudioRecorder.cancelConcat());

        String originFilePath = getIntent().getStringExtra(MP4_PATH);
        PLVideoEditSetting setting = new PLVideoEditSetting();
        setting.setSourceFilepath(originFilePath);
        String dubFilePath = getExternalFilesDir(Environment.DIRECTORY_MOVIES) + "/ShortVideo/dub_" + System.currentTimeMillis() + ".mp4";
        setting.setDestFilepath(dubFilePath);

        mShortVideoEditor = new PLShortVideoEditor(previewView, setting);
        mShortVideoEditor.setVideoSaveListener(this);
        mShortVideoEditor.startPlayback(this);
        mShortVideoEditor.setAudioMixLooping(false);
        mShortVideoEditor.muteOriginAudio(true);

        mShortAudioRecorder = new PLShortAudioRecorder();
        mShortAudioRecorder.setRecordStateListener(this);

        PLMicrophoneSetting microphoneSetting = new PLMicrophoneSetting();
        PLAudioEncodeSetting audioEncodeSetting = new PLAudioEncodeSetting();

        PLRecordSetting recordSetting = new PLRecordSetting();
        recordSetting.setMaxRecordDuration(mShortVideoEditor.getDurationMs());
        recordSetting.setVideoCacheDir(Config.VIDEO_STORAGE_DIR);
        String audioRecordFilePath = getExternalFilesDir(Environment.DIRECTORY_MOVIES) + "/ShortVideo/audio_record_" + System.currentTimeMillis() + ".m4a";
        recordSetting.setVideoFilepath(audioRecordFilePath);

        mShortAudioRecorder.prepare(this, microphoneSetting, audioEncodeSetting, recordSetting);

        mSectionProgressBar.setFirstPointTime(0);
        mSectionProgressBar.setTotalTime(this, mShortVideoEditor.getDurationMs());

        mRecordBtn.setOnClickListener(v -> {
            if (mIsRecordCompleted) {
                ToastUtils.showShortToast("已达到拍摄总时长");
                return;
            }
            if (mSectionBegan) {
                mShortAudioRecorder.endSection();
                mShortVideoEditor.pausePlayback();
                updateRecordingBtns(false);
            } else {
                if (mShortAudioRecorder.beginSection()) {
                    mShortVideoEditor.startPlayback();
                    updateRecordingBtns(true);
                } else {
                    ToastUtils.showShortToast("无法开始音频段录制");
                }
            }
        });

        onSectionCountChanged(0, 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mRecordBtn.setEnabled(false);
        mShortAudioRecorder.resume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        updateRecordingBtns(false);
        mShortAudioRecorder.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mShortAudioRecorder.destroy();
    }

    private void updateRecordingBtns(boolean isRecording) {
        mRecordBtn.setActivated(isRecording);
    }

    private void onSectionCountChanged(final int count, final long totalTime) {
        runOnUiThread(() -> mDeleteBtn.setEnabled(count > 0));
    }

    public void onClickDelete(View v) {
        if (!mShortAudioRecorder.deleteLastSection()) {
            ToastUtils.showShortToast("回删视频段失败");
        } else {
            mIsRecordCompleted = false;
            if (!mSectionTimestamps.isEmpty()) {
                mShortVideoEditor.seekTo(mSectionTimestamps.pop());
            }
        }
    }

    public void onClickBack(View v) {
        finish();
    }

    public void onClickSaveDubbing(View v) {
        mShortAudioRecorder.concatSections(new PLVideoSaveListener() {
            @Override
            public void onSaveVideoSuccess(String filePath) {
                MediaStoreUtils.storeVideo(VideoDubActivity.this, new File(filePath), "video/mp4");
                runOnUiThread(() -> mProcessingDialog.show());
                mShortVideoEditor.setAudioMixFile(filePath);
                mShortVideoEditor.save();
            }

            @Override
            public void onSaveVideoFailed(final int errorCode) {
                ToastUtils.showShortToast("拼接音频段失败，错误码：" + errorCode);
            }

            @Override
            public void onSaveVideoCanceled() {
            }

            @Override
            public void onProgressUpdate(float percentage) {
            }
        });
    }

    @Override
    public void onSaveVideoSuccess(String filePath) {
        mProcessingDialog.dismiss();
        Intent result = new Intent();
        result.putExtra(DUB_MP4_PATH, filePath);
        setResult(Activity.RESULT_OK, result);
        finish();
    }

    @Override
    public void onSaveVideoFailed(final int errorCode) {
        runOnUiThread(() -> {
            mProcessingDialog.dismiss();
            ToastUtils.showShortToast("保存视频失败: " + errorCode);
        });
    }

    @Override
    public void onSaveVideoCanceled() {
        mProcessingDialog.dismiss();
    }

    @Override
    public void onProgressUpdate(final float percentage) {
        runOnUiThread(() -> mProcessingDialog.setProgress((int) (100 * percentage)));
    }

    @Override
    public void onSurfaceCreated() {
        mShortVideoEditor.pausePlayback();
    }

    @Override
    public void onSurfaceChanged(int i, int i1) {

    }

    @Override
    public void onSurfaceDestroy() {

    }

    @Override
    public int onDrawFrame(int i, int i1, int i2, long l, float[] floats) {
        return 0;
    }

    @Override
    public void onReady() {
        runOnUiThread(() -> {
            mRecordBtn.setEnabled(true);
            ToastUtils.showShortToast("可以开始录音咯");
        });
    }

    @Override
    public void onError(int i) {

    }

    @Override
    public void onDurationTooShort() {

    }

    @Override
    public void onRecordStarted() {
        mSectionBegan = true;
        mSectionTimestamps.push(mShortVideoEditor.getCurrentPosition());
        mSectionProgressBar.setCurrentState(SectionProgressBar.State.START);
    }

    @Override
    public void onRecordStopped() {
        mSectionBegan = false;
        mSectionProgressBar.setCurrentState(SectionProgressBar.State.PAUSE);
    }

    @Override
    public void onSectionRecording(long sectionDurationMs, long videoDurationMs, int sectionCount) {

    }

    @Override
    public void onSectionIncreased(long incDuration, long totalDuration, int sectionCount) {
        onSectionCountChanged(sectionCount, totalDuration);
        mSectionProgressBar.addBreakPointTime(totalDuration);
    }

    @Override
    public void onSectionDecreased(long decDuration, long totalDuration, int sectionCount) {
        onSectionCountChanged(sectionCount, totalDuration);
        mSectionProgressBar.removeLastBreakPoint();
    }

    @Override
    public void onRecordCompleted() {
        mIsRecordCompleted = true;
        mShortVideoEditor.pausePlayback();
        ToastUtils.showShortToast("已达到拍摄总时长");
    }
}