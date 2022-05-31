package com.qiniu.shortvideo.app.activity;

import android.content.DialogInterface;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.media.AudioFormat;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Group;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.qiniu.pili.droid.shortvideo.PLBuiltinFilter;
import com.qiniu.shortvideo.app.R;
import com.qiniu.shortvideo.app.adapter.FilterItemAdapter;
import com.qiniu.shortvideo.app.utils.MediaUtils;
import com.qiniu.shortvideo.app.utils.Utils;
import com.qiniu.shortvideo.app.utils.ViewOperator;
import com.qiniu.shortvideo.app.view.CustomProgressDialog;
import com.qiniu.shortvideo.app.view.ListBottomView;
import com.qiniu.shortvideo.app.view.SectionProgressBar;
import com.qiniu.shortvideo.app.utils.Config;
import com.qiniu.shortvideo.app.utils.ToastUtils;
import com.qiniu.pili.droid.shortvideo.PLAudioEncodeSetting;
import com.qiniu.pili.droid.shortvideo.PLCameraSetting;
import com.qiniu.pili.droid.shortvideo.PLDisplayMode;
import com.qiniu.pili.droid.shortvideo.PLFaceBeautySetting;
import com.qiniu.pili.droid.shortvideo.PLMediaFile;
import com.qiniu.pili.droid.shortvideo.PLMicrophoneSetting;
import com.qiniu.pili.droid.shortvideo.PLRecordSetting;
import com.qiniu.pili.droid.shortvideo.PLRecordStateListener;
import com.qiniu.pili.droid.shortvideo.PLShortVideoMixRecorder;
import com.qiniu.pili.droid.shortvideo.PLVideoEncodeSetting;
import com.qiniu.pili.droid.shortvideo.PLVideoMixSetting;
import com.qiniu.pili.droid.shortvideo.PLVideoSaveListener;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Stack;

import static com.qiniu.shortvideo.app.utils.Config.CAMERA_RECORD_CACHE_PATH;

/**
 * 视频合拍模块
 */
public class VideoMixRecordActivity extends AppCompatActivity implements
        PLRecordStateListener, PLVideoSaveListener {
    private static final String TAG = "VideoMixRecordActivity";
    public static final String VIDEO_PATH = "videoPath";
    private static final int FLING_MIN_DISTANCE = 350;

    private PLShortVideoMixRecorder mMixRecorder;

    private View mDecorView;
    private ConstraintLayout mRootView;
    private GLSurfaceView mCameraSurfaceView;
    private GLSurfaceView mSampleSurfaceView;
    private SectionProgressBar mSectionProgressBar;
    private Button mRecordBtn;
    private Button mConcatBtn;
    private TextView mSwitchCameraBtn;
    private TextView mSwitchFlashBtn;
    private TextView mSwitchBeautyBtn;
    private ImageButton mDeleteBtn;
    private CustomProgressDialog mProcessingDialog;
    private Group mBtnGroup;

    private GestureDetector mGestureDetector;

    private PLCameraSetting mCameraSetting;
    private PLMicrophoneSetting mMicrophoneSetting;
    private PLRecordSetting mRecordSetting;
    private PLVideoEncodeSetting mVideoEncodeSetting;
    private PLAudioEncodeSetting mAudioEncodeSetting;
    private PLFaceBeautySetting mFaceBeautySetting;

    private TextView mFilterDescription;
    private ListBottomView mFilterBottomView;
    private FilterItemAdapter mFilterItemAdapter;
    private boolean mIsSelectingFilter;

    private boolean mSectionBegan;
    private long mMinRecordTime;
    private boolean mFlashEnabled = false;
    private boolean mFaceBeautyEnabled = true;

    private Stack<Long> mDurationRecordStack = new Stack();
    private Stack<Double> mDurationVideoStack = new Stack();

    private boolean mIsEditVideo = false;
    private String mSampleVideoPath;
    private String mCurrentFilterName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_mix);

        mDecorView = getWindow().getDecorView();
        mRootView = findViewById(R.id.video_mix_layout);
        mCameraSurfaceView = findViewById(R.id.camera_preview);
        mSampleSurfaceView = findViewById(R.id.sample_preview);
        mSectionProgressBar = findViewById(R.id.section_progress_bar);
        mRecordBtn = findViewById(R.id.record_btn);
        mConcatBtn = findViewById(R.id.next_btn);
        mSwitchCameraBtn = findViewById(R.id.switch_camera_btn);
        mSwitchFlashBtn = findViewById(R.id.flash_light_btn);
        mSwitchBeautyBtn = findViewById(R.id.beauty_btn);
        mDeleteBtn = findViewById(R.id.delete_section_btn);
        mBtnGroup = findViewById(R.id.mix_btn_group);
        mFilterDescription = findViewById(R.id.filter_description_text);

        mGestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (e1 == null || e2 == null) {
                    return false;
                }
                if (e1.getX() - e2.getX() > FLING_MIN_DISTANCE) {
                    mFilterItemAdapter.changeToNextFilter();
                    return true;
                } else if (e1.getX() - e2.getX() < -FLING_MIN_DISTANCE) {
                    mFilterItemAdapter.changeToLastFilter();
                    return true;
                }
                return false;
            }
        });

        mRootView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mIsSelectingFilter) {
                    ViewOperator.startDisappearAnimY(mFilterBottomView);
                    mFilterBottomView.setVisibility(View.GONE);
                    mBtnGroup.setVisibility(View.VISIBLE);
                    mIsSelectingFilter = false;
                }
                mGestureDetector.onTouchEvent(event);
                return true;
            }
        });

        mRecordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mSectionBegan) {
                    mMixRecorder.endSection();
                    mSectionBegan = false;
                } else {
                    if (mMixRecorder.beginSection()) {
                        mSectionBegan = true;
                        mSectionProgressBar.setCurrentState(SectionProgressBar.State.START);
                        updateRecordingBtns(true);
                    } else {
                        ToastUtils.s(VideoMixRecordActivity.this, "无法开始视频段录制");
                    }
                }
            }
        });

        mProcessingDialog = new CustomProgressDialog(this);
        mProcessingDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                mMixRecorder.cancel();
            }
        });

        mSampleVideoPath = getIntent().getStringExtra(VIDEO_PATH);
        init(mSampleVideoPath);
        initBuiltInFilters();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (Build.VERSION.SDK_INT >= 19 && Utils.checkDeviceHasNavigationBar(this)) {
            int flag = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide
                    | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            mDecorView.setSystemUiVisibility(flag);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mMixRecorder != null) {
            mMixRecorder.resume();
            if (mCurrentFilterName != null) {
                mMixRecorder.setBuiltinFilter(mCurrentFilterName);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mMixRecorder != null) {
            mMixRecorder.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mMixRecorder != null) {
            mMixRecorder.destroy();
        }
    }

    public void onClickSwitchFlash(View v) {
        if (!mMixRecorder.isFlashSupport()) {
            return;
        }
        mFlashEnabled = !mFlashEnabled;
        mMixRecorder.setFlashEnabled(mFlashEnabled);
        mSwitchFlashBtn.setActivated(mFlashEnabled);
        Drawable drawable= getResources().getDrawable(mFlashEnabled ? R.mipmap.qn_flash_on : R.mipmap.qn_flash_off);
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        mSwitchFlashBtn.setCompoundDrawables(null, drawable, null, null);
    }

    public void onClickSwitchCamera(View v) {
        mMixRecorder.switchCamera();
    }

    public void onClickBack(View v) {
        finish();
    }

    public void onClickDeleteLastSection(View v) {
        if (!mMixRecorder.deleteLastSection()) {
            ToastUtils.s(this, "回删视频段失败");
        }
    }

    public void onClickSaveVideos(View v) {
        Log.i(TAG, "save videos");
        if (mSectionBegan) {
            ToastUtils.s(VideoMixRecordActivity.this, "当前正在拍摄，无法拼接！");
            return;
        }
        mProcessingDialog.show();
        showChooseDialog();
    }

    public void onClickFilterSelect(View v) {
        mBtnGroup.setVisibility(View.INVISIBLE);
        mFilterBottomView.setVisibility(View.VISIBLE);
        ViewOperator.startAppearAnimY(mFilterBottomView);
        mIsSelectingFilter = true;
    }

    public void onClickBeautyEnabled(View v) {
        mFaceBeautyEnabled = !mFaceBeautyEnabled;
        mFaceBeautySetting.setEnable(mFaceBeautyEnabled);
        mMixRecorder.updateFaceBeautySetting(mFaceBeautySetting);
        Drawable drawable= getResources().getDrawable(mFaceBeautyEnabled ? R.mipmap.qn_record_beauty_on : R.mipmap.qn_record_beauty_off);
        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        mSwitchBeautyBtn.setCompoundDrawables(null, drawable, null, null);
    }

    private void init(String sampleVideoPath) {
        mMixRecorder = new PLShortVideoMixRecorder(this);
        mMixRecorder.setRecordStateListener(this);

        mCameraSetting = new PLCameraSetting();
        PLCameraSetting.CAMERA_FACING_ID facingId = chooseCameraFacingId();
        mCameraSetting.setCameraId(facingId);
        mCameraSetting.setCameraPreviewSizeRatio(PLCameraSetting.CAMERA_PREVIEW_SIZE_RATIO.RATIO_16_9);
        mCameraSetting.setCameraPreviewSizeLevel(PLCameraSetting.CAMERA_PREVIEW_SIZE_LEVEL.PREVIEW_SIZE_LEVEL_720P);

        mMicrophoneSetting = new PLMicrophoneSetting();
        mMicrophoneSetting.setChannelConfig(AudioFormat.CHANNEL_IN_MONO);

        int VIDEO_MIX_ENCODE_WIDTH = 720;
        int VIDEO_MIX_ENCODE_HEIGHT = 720;

        mVideoEncodeSetting = new PLVideoEncodeSetting(this);
        mVideoEncodeSetting.setPreferredEncodingSize(VIDEO_MIX_ENCODE_WIDTH, VIDEO_MIX_ENCODE_HEIGHT);
        mVideoEncodeSetting.setEncodingBitrate(2000 * 1000);
        mVideoEncodeSetting.setEncodingFps(30);
        mVideoEncodeSetting.setDisplayMode(null);

        mAudioEncodeSetting = new PLAudioEncodeSetting();
        mAudioEncodeSetting.setChannels(1);

        PLMediaFile mediaFile = new PLMediaFile(sampleVideoPath);
        long maxDuration = mediaFile.getDurationMs();
        float sampleRotation = mediaFile.getVideoRotation();
        float sampleWidth = (sampleRotation == 90 || sampleRotation == 270) ? mediaFile.getVideoHeight() : mediaFile.getVideoWidth();
        float sampleHeight = (sampleRotation == 90 || sampleRotation == 270) ? mediaFile.getVideoWidth() : mediaFile.getVideoHeight();
        mediaFile.release();
        mRecordSetting = new PLRecordSetting();
        mRecordSetting.setMaxRecordDuration(maxDuration);
        mRecordSetting.setVideoCacheDir(Config.VIDEO_STORAGE_DIR);
        mRecordSetting.setVideoFilepath(Config.MIX_RECORD_FILE_PATH);
        mRecordSetting.setDisplayMode(PLDisplayMode.FIT);
        mRecordSetting.setRecordSpeedVariable(true);

        mMinRecordTime = maxDuration / 5;
        mSectionProgressBar.setFirstPointTime(mMinRecordTime);
        onSectionCountChanged(0, 0);

        mFaceBeautySetting = new PLFaceBeautySetting(1.0f, 0.5f, 0.5f);

        float sampleRatio = sampleWidth / sampleHeight;
        //固定宽度
        int sampleRectWidth = VIDEO_MIX_ENCODE_WIDTH / 2;
        //高度根据长宽比来计算，防止拉伸
        int sampleRectHeight = (int) (sampleRectWidth / sampleRatio);
        //顶部坐标
        int sampleRectTop = (VIDEO_MIX_ENCODE_HEIGHT - sampleRectHeight) / 2;
        //底部坐标
        int sampleRectBottom = sampleRectTop + sampleRectHeight;

        float cameraRatio = (float) PLCameraSetting.calcCameraPreviewSizeRatio(mCameraSetting.getCameraPreviewSizeRatio());
        //固定宽度
        int cameraRectWidth = VIDEO_MIX_ENCODE_WIDTH / 2;
        //高度根据长宽比来计算，防止拉伸
        int cameraRectHeight = (int) (cameraRectWidth * cameraRatio);
        //顶部坐标
        int cameraRectTop = (VIDEO_MIX_ENCODE_HEIGHT - cameraRectHeight) / 2;
        //底部坐标
        int cameraRectBottom = cameraRectTop + cameraRectHeight;

        //摄像机录制的视频在最后画面的矩形区域
        Rect cameraVideoRect = new Rect(0, cameraRectTop, VIDEO_MIX_ENCODE_WIDTH / 2, cameraRectBottom);
        //样本视频在最后画面的矩形区域
        Rect sampleVideoRect = new Rect(VIDEO_MIX_ENCODE_WIDTH / 2, sampleRectTop, VIDEO_MIX_ENCODE_WIDTH, sampleRectBottom);

        //其中：CAMERA_RECORD_CACHE_PATH 为摄像机录制之后的片段最后拼接的缓存视频的路径，它用于和样本视频最后进行合成
        PLVideoMixSetting mixSetting = new PLVideoMixSetting(cameraVideoRect, sampleVideoRect, sampleVideoPath, CAMERA_RECORD_CACHE_PATH);
        mMixRecorder.prepare(mCameraSurfaceView, mSampleSurfaceView, mixSetting, mCameraSetting, mMicrophoneSetting, mVideoEncodeSetting,
                mAudioEncodeSetting, mFaceBeautySetting, mRecordSetting);
        mMixRecorder.muteMicrophone(true);
        mMixRecorder.muteSampleVideo(false);

        mSectionProgressBar.setTotalTime(this, mRecordSetting.getMaxRecordDuration());
    }

    private void initBuiltInFilters() {
        mFilterBottomView = findViewById(R.id.filter_select_view);
        mFilterItemAdapter = new FilterItemAdapter(this,
                new ArrayList<PLBuiltinFilter>(Arrays.asList(mMixRecorder.getBuiltinFilterList())),
                new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.built_in_filters))));
        mFilterItemAdapter.setOnFilterSelectListener(new FilterItemAdapter.OnFilterSelectListener() {
            @Override
            public void onFilterSelected(String filterName, String description) {
                mCurrentFilterName = filterName;
                mMixRecorder.setBuiltinFilter(filterName);
                showDescription(description, 1500);
            }
        });
        mFilterBottomView.init(mFilterItemAdapter);
    }

    private PLCameraSetting.CAMERA_FACING_ID chooseCameraFacingId() {
        if (PLCameraSetting.hasCameraFacing(PLCameraSetting.CAMERA_FACING_ID.CAMERA_FACING_3RD)) {
            return PLCameraSetting.CAMERA_FACING_ID.CAMERA_FACING_3RD;
        } else if (PLCameraSetting.hasCameraFacing(PLCameraSetting.CAMERA_FACING_ID.CAMERA_FACING_FRONT)) {
            return PLCameraSetting.CAMERA_FACING_ID.CAMERA_FACING_FRONT;
        } else {
            return PLCameraSetting.CAMERA_FACING_ID.CAMERA_FACING_BACK;
        }
    }

    private void onSectionCountChanged(final int count, final long totalTime) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mDeleteBtn.setEnabled(count > 0);
                mDeleteBtn.setImageResource(count > 0 ? R.mipmap.qn_delete_section_active : R.mipmap.qn_delete_section_inactive);
                mConcatBtn.setEnabled(totalTime >= mMinRecordTime);
                Log.i(TAG, "onSectionCountChanged : " + mConcatBtn.isEnabled());
            }
        });
    }

    private void updateRecordingBtns(boolean isRecording) {
        mSwitchCameraBtn.setEnabled(!isRecording);
        mRecordBtn.setBackgroundResource(isRecording ? R.mipmap.qn_shooting : R.mipmap.qn_video);
    }

    private void showChooseDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.if_edit_video));
        builder.setPositiveButton(getString(R.string.dlg_yes), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mIsEditVideo = true;
                mMixRecorder.save(VideoMixRecordActivity.this);
            }
        });
        builder.setNegativeButton(getString(R.string.dlg_no), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mIsEditVideo = false;
                mMixRecorder.save(VideoMixRecordActivity.this);
            }
        });
        builder.setCancelable(false);
        builder.create().show();
    }

    private Runnable effectDescriptionHide = new Runnable() {
        @Override
        public void run() {
            mFilterDescription.setText("");
            mFilterDescription.setVisibility(View.INVISIBLE);
        }
    };

    private void showDescription(int str, int time) {
        if (str == 0) {
            return;
        }
        String filterName = getString(str);
        showDescription(filterName, time);
    }

    private void showDescription(String filterName, int time) {
        SpannableString description = new SpannableString(String.format("%s%n<<左右滑动切换滤镜>>", filterName));
        description.setSpan(new AbsoluteSizeSpan(30, true), 0, filterName.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        description.setSpan(new AbsoluteSizeSpan(14, true), filterName.length(), description.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        mFilterDescription.removeCallbacks(effectDescriptionHide);
        mFilterDescription.setVisibility(View.VISIBLE);
        mFilterDescription.setText(description);
        mFilterDescription.postDelayed(effectDescriptionHide, time);
    }

    @Override
    public void onReady() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mRecordBtn.setEnabled(true);
                ToastUtils.s(VideoMixRecordActivity.this, "可以开始拍摄咯");
            }
        });
    }

    @Override
    public void onError(int code) {
        Log.i(TAG, "errorCode = " + code);
    }

    @Override
    public void onDurationTooShort() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ToastUtils.s(VideoMixRecordActivity.this, "该视频段太短了");
            }
        });
    }

    @Override
    public void onRecordStarted() {

    }

    @Override
    public void onSectionRecording(long sectionDurationMs, long videoDurationMs, int sectionCount) {

    }

    @Override
    public void onRecordStopped() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mSectionBegan = false;
                updateRecordingBtns(false);
            }
        });
    }

    @Override
    public void onSectionIncreased(long incDuration, long totalDuration, int sectionCount) {
        long totalRecordDurationMs = incDuration + (mDurationRecordStack.isEmpty() ? 0 : mDurationRecordStack.peek());
        double totalVideoDurationMs = incDuration + (mDurationVideoStack.isEmpty() ? 0 : mDurationVideoStack.peek());
        if (totalVideoDurationMs >= mRecordSetting.getMaxRecordDuration()) {
            totalVideoDurationMs = mRecordSetting.getMaxRecordDuration();
        }
        mDurationRecordStack.push(totalRecordDurationMs);
        mDurationVideoStack.push(totalVideoDurationMs);
        if (mRecordSetting.IsRecordSpeedVariable()) {
            mSectionProgressBar.addBreakPointTime((long) totalVideoDurationMs);
        } else {
            mSectionProgressBar.addBreakPointTime(totalRecordDurationMs);
        }

        mSectionProgressBar.setCurrentState(SectionProgressBar.State.PAUSE);

        onSectionCountChanged(sectionCount, (long) totalVideoDurationMs);
    }

    @Override
    public void onSectionDecreased(long decDuration, long totalDuration, int sectionCount) {
        mSectionProgressBar.removeLastBreakPoint();
        if (!mDurationVideoStack.isEmpty()) {
            mDurationVideoStack.pop();
        }
        if (!mDurationRecordStack.isEmpty()) {
            mDurationRecordStack.pop();
        }
        double currentDuration = mDurationVideoStack.isEmpty() ? 0 : mDurationVideoStack.peek();
        onSectionCountChanged(sectionCount, (long) currentDuration);
    }

    @Override
    public void onRecordCompleted() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ToastUtils.s(VideoMixRecordActivity.this, "已达到拍摄总时长");
            }
        });
    }

    @Override
    public void onSaveVideoSuccess(final String filePath) {
        MediaUtils.storeVideo(this, new File(filePath), Config.MIME_TYPE_VIDEO);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mProcessingDialog.dismiss();
                if (mIsEditVideo) {
                    VideoEditActivity.start(VideoMixRecordActivity.this, filePath);
                } else {
                    PlaybackActivity.start(VideoMixRecordActivity.this, filePath);
                }
            }
        });
    }

    @Override
    public void onSaveVideoFailed(final int errorCode) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mProcessingDialog.dismiss();
                ToastUtils.s(VideoMixRecordActivity.this, "拼接视频段失败: " + errorCode);
            }
        });
    }

    @Override
    public void onSaveVideoCanceled() {
        mProcessingDialog.dismiss();
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
}
