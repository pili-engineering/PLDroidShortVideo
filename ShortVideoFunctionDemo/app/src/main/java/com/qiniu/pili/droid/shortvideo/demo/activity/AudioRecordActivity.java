package com.qiniu.pili.droid.shortvideo.demo.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.media.AudioFormat;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.qiniu.pili.droid.shortvideo.PLAudioEncodeSetting;
import com.qiniu.pili.droid.shortvideo.PLErrorCode;
import com.qiniu.pili.droid.shortvideo.PLMicrophoneSetting;
import com.qiniu.pili.droid.shortvideo.PLRecordSetting;
import com.qiniu.pili.droid.shortvideo.PLRecordStateListener;
import com.qiniu.pili.droid.shortvideo.PLShortAudioRecorder;
import com.qiniu.pili.droid.shortvideo.PLVideoSaveListener;
import com.qiniu.pili.droid.shortvideo.demo.R;
import com.qiniu.pili.droid.shortvideo.demo.utils.Config;
import com.qiniu.pili.droid.shortvideo.demo.utils.MediaStoreUtils;
import com.qiniu.pili.droid.shortvideo.demo.utils.RecordSettings;
import com.qiniu.pili.droid.shortvideo.demo.utils.ToastUtils;
import com.qiniu.pili.droid.shortvideo.demo.view.CustomProgressDialog;
import com.qiniu.pili.droid.shortvideo.demo.view.SectionProgressBar;

import java.io.File;

public class AudioRecordActivity extends Activity implements PLRecordStateListener, PLVideoSaveListener {
    private static final String TAG = "AudioRecordActivity";

    public static final String ENCODING_MODE = "EncodingMode";
    public static final String AUDIO_CHANNEL_NUM = "AudioChannelNum";

    private PLShortAudioRecorder mShortAudioRecorder;

    private SectionProgressBar mSectionProgressBar;
    private CustomProgressDialog mProcessingDialog;
    private View mRecordBtn;
    private View mDeleteBtn;
    private View mConcatBtn;

    private String mRecordErrorMsg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_audio_record);

        int encodingModePos = getIntent().getIntExtra(ENCODING_MODE, 0);
        int audioChannelNumPos = getIntent().getIntExtra(AUDIO_CHANNEL_NUM, 0);

        mSectionProgressBar = (SectionProgressBar) findViewById(R.id.record_progressbar);
        mRecordBtn = findViewById(R.id.record);
        mDeleteBtn = findViewById(R.id.delete);
        mConcatBtn = findViewById(R.id.concat);

        mProcessingDialog = new CustomProgressDialog(this);
        mProcessingDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                mShortAudioRecorder.cancelConcat();
            }
        });

        mShortAudioRecorder = new PLShortAudioRecorder();
        mShortAudioRecorder.setRecordStateListener(this);

        PLMicrophoneSetting microphoneSetting = new PLMicrophoneSetting();
        microphoneSetting.setChannelConfig(RecordSettings.AUDIO_CHANNEL_NUM_ARRAY[audioChannelNumPos] == 1 ?
                AudioFormat.CHANNEL_IN_MONO : AudioFormat.CHANNEL_IN_STEREO);

        PLAudioEncodeSetting audioEncodeSetting = new PLAudioEncodeSetting();
        audioEncodeSetting.setHWCodecEnabled(encodingModePos == 0);
        audioEncodeSetting.setChannels(RecordSettings.AUDIO_CHANNEL_NUM_ARRAY[audioChannelNumPos]);

        PLRecordSetting recordSetting = new PLRecordSetting();
        recordSetting.setMaxRecordDuration(RecordSettings.DEFAULT_MAX_RECORD_DURATION);
        recordSetting.setVideoCacheDir(Config.VIDEO_STORAGE_DIR);
        recordSetting.setVideoFilepath(Config.AUDIO_RECORD_FILE_PATH);

        mShortAudioRecorder.prepare(this, microphoneSetting, audioEncodeSetting, recordSetting);

        mSectionProgressBar.setFirstPointTime(RecordSettings.DEFAULT_MIN_RECORD_DURATION);
        mSectionProgressBar.setTotalTime(this, recordSetting.getMaxRecordDuration());

        mRecordBtn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();
                if (action == MotionEvent.ACTION_DOWN) {
                    if (mShortAudioRecorder.beginSection()) {
                        updateRecordingBtns(true);
                    } else {
                        ToastUtils.showShortToast(AudioRecordActivity.this, "无法开始视频段录制");
                    }
                } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                    mShortAudioRecorder.endSection();
                    updateRecordingBtns(false);
                }

                return false;
            }
        });
        onSectionCountChanged(0, 0);
    }

    private void updateRecordingBtns(boolean isRecording) {
        mRecordBtn.setActivated(isRecording);
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

    public void onClickDelete(View v) {
        if (!mShortAudioRecorder.deleteLastSection()) {
            ToastUtils.showShortToast(this, "回删视频段失败");
        }
    }

    public void onClickConcat(View v) {
        mProcessingDialog.show();
        mShortAudioRecorder.concatSections(AudioRecordActivity.this);
    }

    @Override
    public void onReady() {
        runOnUiThread(() -> {
            mRecordBtn.setEnabled(true);
            ToastUtils.showShortToast(AudioRecordActivity.this, "可以开始录音咯");
        });
    }

    @Override
    public void onError(int code) {
        if (code == PLErrorCode.ERROR_SETUP_MICROPHONE_FAILED) {
            mRecordErrorMsg = "麦克风配置错误";
        }
        runOnUiThread(() -> ToastUtils.showShortToast(AudioRecordActivity.this, mRecordErrorMsg));
    }

    @Override
    public void onDurationTooShort() {
        runOnUiThread(() -> ToastUtils.showShortToast(AudioRecordActivity.this, "该视频段太短了"));
    }

    @Override
    public void onRecordStarted() {
        Log.i(TAG, "record start time: " + System.currentTimeMillis());
        mSectionProgressBar.setCurrentState(SectionProgressBar.State.START);
    }

    @Override
    public void onRecordStopped() {
        Log.i(TAG, "record stop time: " + System.currentTimeMillis());
        mSectionProgressBar.setCurrentState(SectionProgressBar.State.PAUSE);
    }


    @Override
    public void onSectionRecording(long sectionDurationMs, long videoDurationMs, int sectionCount) {
        Log.d(TAG, "sectionDurationMs: " + sectionDurationMs + "; videoDurationMs: " + videoDurationMs + "; sectionCount: " + sectionCount);
    }

    @Override
    public void onSectionIncreased(long incDuration, long totalDuration, int sectionCount) {
        Log.i(TAG, "section increased incDuration: " + incDuration + " totalDuration: " + totalDuration + " sectionCount: " + sectionCount);
        onSectionCountChanged(sectionCount, totalDuration);
        mSectionProgressBar.addBreakPointTime(totalDuration);
    }

    @Override
    public void onSectionDecreased(long decDuration, long totalDuration, int sectionCount) {
        Log.i(TAG, "section decreased decDuration: " + decDuration + " totalDuration: " + totalDuration + " sectionCount: " + sectionCount);
        onSectionCountChanged(sectionCount, totalDuration);
        mSectionProgressBar.removeLastBreakPoint();
    }

    @Override
    public void onRecordCompleted() {
        runOnUiThread(() -> ToastUtils.showShortToast(AudioRecordActivity.this, "已达到拍摄总时长"));
    }

    @Override
    public void onProgressUpdate(final float percentage) {
        runOnUiThread(() -> mProcessingDialog.setProgress((int) (100 * percentage)));
    }

    @Override
    public void onSaveVideoFailed(final int errorCode) {
        runOnUiThread(() -> {
            mProcessingDialog.dismiss();
            ToastUtils.showShortToast(AudioRecordActivity.this, "拼接视频段失败: " + errorCode);
        });
    }

    @Override
    public void onSaveVideoCanceled() {
        mProcessingDialog.dismiss();
    }

    @Override
    public void onSaveVideoSuccess(final String filePath) {
        Log.i(TAG, "concat sections success filePath: " + filePath);
        runOnUiThread(() -> {
            MediaStoreUtils.storeAudio(AudioRecordActivity.this, new File(filePath), "audio/mp3");
            mProcessingDialog.dismiss();
            PlaybackActivity.start(AudioRecordActivity.this, filePath);
        });
    }

    private void onSectionCountChanged(final int count, final long totalTime) {
        runOnUiThread(() -> {
            mDeleteBtn.setEnabled(count > 0);
            mConcatBtn.setEnabled(totalTime >= RecordSettings.DEFAULT_MIN_RECORD_DURATION);
        });
    }
}
