package com.qiniu.pili.droid.shortvideo.demo.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.qiniu.pili.droid.shortvideo.PLBuiltinFilter;
import com.qiniu.pili.droid.shortvideo.PLImageView;
import com.qiniu.pili.droid.shortvideo.PLMediaFile;
import com.qiniu.pili.droid.shortvideo.PLMixAudioFile;
import com.qiniu.pili.droid.shortvideo.PLPaintView;
import com.qiniu.pili.droid.shortvideo.PLShortVideoEditor;
import com.qiniu.pili.droid.shortvideo.PLSpeedTimeRange;
import com.qiniu.pili.droid.shortvideo.PLTextView;
import com.qiniu.pili.droid.shortvideo.PLVideoEditSetting;
import com.qiniu.pili.droid.shortvideo.PLVideoFilterListener;
import com.qiniu.pili.droid.shortvideo.PLVideoSaveListener;
import com.qiniu.pili.droid.shortvideo.PLWatermarkSetting;
import com.qiniu.pili.droid.shortvideo.demo.R;
import com.qiniu.pili.droid.shortvideo.demo.utils.Config;
import com.qiniu.pili.droid.shortvideo.demo.utils.GetPathFromUri;
import com.qiniu.pili.droid.shortvideo.demo.utils.ToastUtils;
import com.qiniu.pili.droid.shortvideo.demo.view.AudioMixSettingDialog;
import com.qiniu.pili.droid.shortvideo.demo.view.CustomProgressDialog;
import com.qiniu.pili.droid.shortvideo.demo.view.FrameListView;
import com.qiniu.pili.droid.shortvideo.demo.view.FrameSelectorView;
import com.qiniu.pili.droid.shortvideo.demo.view.ImageSelectorPanel;
import com.qiniu.pili.droid.shortvideo.demo.view.PaintSelectorPanel;
import com.qiniu.pili.droid.shortvideo.demo.view.SectionProgressBar;
import com.qiniu.pili.droid.shortvideo.demo.view.StrokedTextView;
import com.qiniu.pili.droid.shortvideo.demo.view.TextSelectorPanel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import static com.qiniu.pili.droid.shortvideo.demo.utils.RecordSettings.RECORD_SPEED_ARRAY;

public class VideoEditActivity extends Activity implements PLVideoSaveListener {
    private static final String TAG = "VideoEditActivity";
    private static final String MP4_PATH = "MP4_PATH";
    private static final String PREVIOUS_ORIENTATION = "PREVIOUS_ORIENTATION";

    private static final int REQUEST_CODE_PICK_AUDIO_MIX_FILE = 0;
    private static final int REQUEST_CODE_DUB = 1;
    private static final int REQUEST_CODE_MULTI_AUDIO_MIX_FILE = 2;

    // 视频编辑器预览状态
    private enum PLShortVideoEditorStatus {
        Idle,
        Playing,
        Paused,
    }

    private PLShortVideoEditorStatus mShortVideoEditorStatus = PLShortVideoEditorStatus.Idle;

    private GLSurfaceView mPreviewView;
    private RecyclerView mFiltersList;
    private TextSelectorPanel mTextSelectorPanel;
    private CustomProgressDialog mProcessingDialog;
    private ImageButton mMuteButton;
    private ImageButton mPausePalybackButton;
    private AudioMixSettingDialog mAudioMixSettingDialog;
    private PaintSelectorPanel mPaintSelectorPanel;
    private LinearLayout mSpeedPanel;

    private PLShortVideoEditor mShortVideoEditor;
    private String mSelectedFilter;
    private String mSelectedMV;
    private String mSelectedMask;
    private PLWatermarkSetting mWatermarkSetting;
    private PLWatermarkSetting mSaveWatermarkSetting;
    private PLWatermarkSetting mPreviewWatermarkSetting;
    private PLPaintView mPaintView;
    private ImageSelectorPanel mImageSelectorPanel;
    private SectionProgressBar mSectionProgressBar;

    private PLTextView mCurTextView;
    private PLImageView mCurImageView;

    private int mFgVolumeBeforeMute = 100;
    private long mMixDuration = 5000; // ms
    private boolean mIsMuted = false;
    private boolean mIsMixAudio = false;
    private boolean mIsUseWatermark = true;

    private String mMp4path;

    private TextView mSpeedTextView;
    private View mVisibleView;

    private volatile boolean mCancelSave;
    private volatile boolean mIsVideoPlayCompleted;
    private int mPreviousOrientation;

    private FrameListView mFrameListView;
    private TimerTask mScrollTimerTask;
    private Timer mScrollTimer;
    private View mCurView;
    private boolean mIsRangeSpeed;

    private int mAudioMixingMode = -1;       // audio mixing mode: 0 - single audio mixing; 1 - multiple audio mixing; 单混音和多重混音为互斥模式，最多只可选择其中一项。
    private boolean mMainAudioFileAdded;
    private int mAudioMixingFileCount = 0;
    private long mInputMp4FileDurationMs = 0;
    private double mSpeed = 1;
    private PLMixAudioFile mMainMixAudioFile;
    private float mMainMixAudioFileVolume = 1;

    public static void start(Activity activity, String mp4Path) {
        Intent intent = new Intent(activity, VideoEditActivity.class);
        intent.putExtra(MP4_PATH, mp4Path);
        activity.startActivity(intent);
    }

    public static void start(Activity activity, String mp4Path, int previousOrientation) {
        Intent intent = new Intent(activity, VideoEditActivity.class);
        intent.putExtra(MP4_PATH, mp4Path);
        intent.putExtra(PREVIOUS_ORIENTATION, previousOrientation);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);

        setContentView(R.layout.activity_editor);

        mSpeedTextView = (TextView) findViewById(R.id.normal_speed_text);
        mMuteButton = (ImageButton) findViewById(R.id.mute_button);
        mMuteButton.setImageResource(R.mipmap.btn_unmute);
        mPausePalybackButton = (ImageButton) findViewById(R.id.pause_playback);
        mSpeedPanel = (LinearLayout) findViewById(R.id.speed_panel);

        mFrameListView = (FrameListView) findViewById(R.id.frame_list_view);

        mPreviousOrientation = getIntent().getIntExtra(PREVIOUS_ORIENTATION, 1);

        initPreviewView();
        initTextSelectorPanel();
        initPaintSelectorPanel();
        initImageSelectorPanel();
        initProcessingDialog();
        initWatermarkSetting();
        initShortVideoEditor();
        initFiltersList();
        initAudioMixSettingDialog();
    }

    @Override
    public void finish() {
        if (0 == mPreviousOrientation) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        super.finish();
    }

    /**
     * 启动预览
     */
    private void startPlayback() {
        if (mShortVideoEditorStatus == PLShortVideoEditorStatus.Idle) {
            mShortVideoEditor.startPlayback(new PLVideoFilterListener() {
                @Override
                public void onSurfaceCreated() {

                }

                @Override
                public void onSurfaceChanged(int width, int height) {

                }

                @Override
                public void onSurfaceDestroy() {

                }

                @Override
                public int onDrawFrame(int texId, int texWidth, int texHeight, long timestampNs, float[] transformMatrix) {
                    int time = mShortVideoEditor.getCurrentPosition();
                    //三秒之后，预览视频的水印坐标动态改变
                    if (time > 3000) {
                        mPreviewWatermarkSetting.setPosition(0.01f, 1);
                    } else {
                        mPreviewWatermarkSetting.setPosition(0.01f, 0.01f);
                    }
                    mShortVideoEditor.updatePreviewWatermark(mIsUseWatermark ? mPreviewWatermarkSetting : null);
                    return texId;
                }
            });
            mShortVideoEditorStatus = PLShortVideoEditorStatus.Playing;
        } else if (mShortVideoEditorStatus == PLShortVideoEditorStatus.Paused) {
            mShortVideoEditor.resumePlayback();
            mShortVideoEditorStatus = PLShortVideoEditorStatus.Playing;
        }
        mPausePalybackButton.setImageResource(R.mipmap.btn_pause);
    }

    /**
     * 停止预览
     */
    private void stopPlayback() {
        mShortVideoEditor.stopPlayback();
        mShortVideoEditorStatus = PLShortVideoEditorStatus.Idle;
        mPausePalybackButton.setImageResource(R.mipmap.btn_play);
    }

    /**
     * 暂停预览
     */
    private void pausePlayback() {
        mShortVideoEditor.pausePlayback();
        mShortVideoEditorStatus = PLShortVideoEditorStatus.Paused;
        mPausePalybackButton.setImageResource(R.mipmap.btn_play);
    }

    public void onClickShowSpeed(View view) {
        mSpeedPanel.setVisibility((mSpeedPanel.getVisibility() == View.GONE) ? View.VISIBLE : View.GONE);
    }

    public void onSpeedClicked(View view) {
        mSpeedTextView.setTextColor(getResources().getColor(R.color.speedTextNormal));

        TextView textView = (TextView) view;
        textView.setTextColor(getResources().getColor(R.color.colorAccent));
        mSpeedTextView = textView;

        double recordSpeed = 1.0;
        switch (view.getId()) {
            case R.id.super_slow_speed_text:
                recordSpeed = RECORD_SPEED_ARRAY[0];
                mIsRangeSpeed = false;
                break;
            case R.id.slow_speed_text:
                recordSpeed = RECORD_SPEED_ARRAY[1];
                mIsRangeSpeed = false;
                break;
            case R.id.normal_speed_text:
                recordSpeed = RECORD_SPEED_ARRAY[2];
                mIsRangeSpeed = false;
                break;
            case R.id.fast_speed_text:
                recordSpeed = RECORD_SPEED_ARRAY[3];
                mIsRangeSpeed = false;
                break;
            case R.id.super_fast_speed_text:
                recordSpeed = RECORD_SPEED_ARRAY[4];
                mIsRangeSpeed = false;
                break;
            case R.id.range_speed_text:
                mIsRangeSpeed = true;
                break;
        }

        mSpeed = recordSpeed;
        mShortVideoEditor.setSpeed(mSpeed, true);
    }

    private void addImageView(String imagePath) {
        checkToAddRectView();

        final PLImageView imageView = new PLImageView(VideoEditActivity.this);
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeStream(getAssets().open(imagePath));
        } catch (IOException e) {
            e.printStackTrace();
        }

        imageView.setImageBitmap(bitmap);
        mShortVideoEditor.addImageView(imageView);

        addSelectorView(imageView);

        showImageViewBorder(imageView);
        imageView.setOnTouchListener(new ViewTouchListener(imageView));
    }

    private void addSelectorView(View view) {
        View selectorView = mFrameListView.addSelectorView();
        view.setTag(R.id.selector_view, selectorView);
    }

    private void initFiltersList() {
        mFiltersList = (RecyclerView) findViewById(R.id.recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        mFiltersList.setLayoutManager(layoutManager);
        mFiltersList.setAdapter(new FilterListAdapter(mShortVideoEditor.getBuiltinFilterList()));
        setPanelVisibility(mFiltersList, true);
    }

    private void initAudioMixSettingDialog() {
        mAudioMixSettingDialog = new AudioMixSettingDialog(this);
        // make dialog create +
        mAudioMixSettingDialog.show();
        mAudioMixSettingDialog.dismiss();
        // make dialog create -
        mAudioMixSettingDialog.setOnAudioVolumeChangedListener(mOnAudioVolumeChangedListener);
        mAudioMixSettingDialog.setOnPositionSelectedListener(mOnPositionSelectedListener);
    }

    private void initShortVideoEditor() {
        mMp4path = getIntent().getStringExtra(MP4_PATH);
        Log.i(TAG, "editing file: " + mMp4path);

        PLVideoEditSetting setting = new PLVideoEditSetting();
        setting.setSourceFilepath(mMp4path);
        setting.setDestFilepath(Config.EDITED_FILE_PATH);

        mShortVideoEditor = new PLShortVideoEditor(mPreviewView);
        mShortVideoEditor.setVideoEditSetting(setting);
        mShortVideoEditor.setVideoSaveListener(this);

        mMixDuration = mShortVideoEditor.getDurationMs();

        mFrameListView.setVideoPath(mMp4path);
        mFrameListView.setOnVideoFrameScrollListener(new FrameListView.OnVideoFrameScrollListener() {
            @Override
            public void onVideoFrameScrollChanged(long timeMs) {
                if (mShortVideoEditorStatus == PLShortVideoEditorStatus.Playing) {
                    pausePlayback();
                }
                mShortVideoEditor.seekTo((int) timeMs);
            }
        });

        initTimerTask();
    }

    private void initTimerTask() {
        mScrollTimerTask = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (mShortVideoEditorStatus == PLShortVideoEditorStatus.Playing) {
                            int position = mShortVideoEditor.getCurrentPosition();
                            mFrameListView.scrollToTime(position);
                        }
                    }
                });
            }
        };
        mScrollTimer = new Timer();
        // scroll fps:20
        mScrollTimer.schedule(mScrollTimerTask, 50, 50);
    }

    private void initWatermarkSetting() {
        mWatermarkSetting = createWatermarkSetting();
        //动态水印设置
        mPreviewWatermarkSetting = createWatermarkSetting();
        mSaveWatermarkSetting = createWatermarkSetting();
    }

    private PLWatermarkSetting createWatermarkSetting() {
        PLWatermarkSetting watermarkSetting = new PLWatermarkSetting();
        watermarkSetting.setResourceId(R.drawable.qiniu_logo);
        watermarkSetting.setPosition(0.01f, 0.01f);
        watermarkSetting.setAlpha(128);
        return watermarkSetting;
    }

    private void initTextSelectorPanel() {
        mTextSelectorPanel = (TextSelectorPanel) findViewById(R.id.text_selector_panel);
        mTextSelectorPanel.setOnTextSelectorListener(new TextSelectorPanel.OnTextSelectorListener() {
            @Override
            public void onTextSelected(StrokedTextView textView) {
                addText(textView);
            }

            @Override
            public void onViewClosed() {
                setPanelVisibility(mTextSelectorPanel, false);
            }
        });
    }

    private void initPaintSelectorPanel() {
        mPaintSelectorPanel = (PaintSelectorPanel) findViewById(R.id.paint_selector_panel);
        mPaintSelectorPanel.setOnPaintSelectorListener(new PaintSelectorPanel.OnPaintSelectorListener() {
            @Override
            public void onViewClosed() {
                setPanelVisibility(mPaintSelectorPanel, false);

                mPaintView.setPaintEnable(false);
            }

            @Override
            public void onPaintColorSelected(int color) {
                mPaintView.setPaintColor(color);
            }

            @Override
            public void onPaintSizeSelected(int size) {
                mPaintView.setPaintSize(size);
            }

            @Override
            public void onPaintUndoSelected() {
                mPaintView.undo();
            }

            @Override
            public void onPaintClearSelected() {
                mPaintView.clear();
            }
        });
    }

    private void initImageSelectorPanel() {
        mImageSelectorPanel = (ImageSelectorPanel) findViewById(R.id.image_selector_panel);
        mImageSelectorPanel.setOnImageSelectedListener(new ImageSelectorPanel.OnImageSelectedListener() {
            @Override
            public void onImageSelected(String imagePath) {
                addImageView(imagePath);
            }
        });
    }

    private void initPreviewView() {
        mPreviewView = (GLSurfaceView) findViewById(R.id.preview);
        mPreviewView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideViewBorder();
                checkToAddRectView();
            }
        });
    }

    private void checkToAddRectView() {
        if (mCurView != null) {
            View rectView = mFrameListView.addSelectedRect((View) mCurView.getTag(R.id.selector_view));
            mCurView.setTag(R.id.rect_view, rectView);
            FrameListView.SectionItem sectionItem = mFrameListView.getSectionByRectView(rectView);
            mShortVideoEditor.setViewTimeline(mCurView, sectionItem.getStartTime(), (sectionItem.getEndTime() - sectionItem.getStartTime()));
            mCurView = null;
        }
    }

    private void initProcessingDialog() {
        mProcessingDialog = new CustomProgressDialog(this);
        mProcessingDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                mShortVideoEditor.cancelSave();
            }
        });
    }

    public void onClickReset(View v) {
        mSelectedFilter = null;
        mSelectedMV = null;
        mSelectedMask = null;
        mShortVideoEditor.setBuiltinFilter(null);
        mShortVideoEditor.setMVEffect(null, null);
        mShortVideoEditor.setAudioMixFile(null);
        mIsMixAudio = false;
        mAudioMixSettingDialog.clearMixAudio();
    }

    public void onClickMix(View v) {
        if (mAudioMixingMode == 1) {
            ToastUtils.s(this, "已选择多重混音，无法再选择单混音！");
            return;
        }
        Intent intent = new Intent();
        if (Build.VERSION.SDK_INT < 19) {
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("audio/*");
        } else {
            intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("audio/*");
        }
        mAudioMixingMode = 0;
        startActivityForResult(Intent.createChooser(intent, "请选择混音文件："), REQUEST_CODE_PICK_AUDIO_MIX_FILE);
    }

    public void onClickMultipleAudioMixing(View v) {
        PLMediaFile mediaFile = new PLMediaFile(mMp4path);
        boolean isPureVideo = !mediaFile.hasAudio();
        mediaFile.release();

        if (mAudioMixingMode == 0) {
            ToastUtils.s(this, "已选择单混音，无法再选择多重混音！");
            return;
        }
        if (isPureVideo) {
            ToastUtils.s(this, "该视频没有音频信息，无法进行多重混音！");
            return;
        }

        Intent intent = new Intent();
        if (Build.VERSION.SDK_INT < 19) {
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("audio/*");
        } else {
            intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("audio/*");
        }
        mAudioMixingMode = 1;
        startActivityForResult(Intent.createChooser(intent, "请选择混音文件："), REQUEST_CODE_MULTI_AUDIO_MIX_FILE);
    }

    public void onClickMute(View v) {
        mIsMuted = !mIsMuted;
        mShortVideoEditor.muteOriginAudio(mIsMuted);
        mMuteButton.setImageResource(mIsMuted ? R.mipmap.btn_mute : R.mipmap.btn_unmute);
        if (mIsMuted) {
            mFgVolumeBeforeMute = mAudioMixSettingDialog.getSrcVolumeProgress();
        }
        mAudioMixSettingDialog.setSrcVolumeProgress(mIsMuted ? 0 : mFgVolumeBeforeMute);
        if (mMainMixAudioFile != null) {
            if (mIsMuted) {
                mMainMixAudioFileVolume = mMainMixAudioFile.getVolume();
                mMainMixAudioFile.setVolume(0);
            } else {
                mMainMixAudioFile.setVolume(mMainMixAudioFileVolume);
            }
        }
    }

    public void onClickTextSelect(View v) {
        setPanelVisibility(mTextSelectorPanel, true);
    }

    public void onClickDubAudio(View v) {
        Intent intent = new Intent(this, VideoDubActivity.class);
        intent.putExtra(VideoDubActivity.MP4_PATH, mMp4path);
        startActivityForResult(intent, REQUEST_CODE_DUB);
    }

    public void onClickAudioMixSetting(View v) {
        if (mIsMixAudio) {
            mAudioMixSettingDialog.show();
        } else {
            ToastUtils.s(this, "请先选择混音文件！");
        }
    }

    public void onClickBack(View v) {
        finish();
    }

    public void onClickToggleWatermark(View v) {
        mIsUseWatermark = !mIsUseWatermark;
        mShortVideoEditor.setWatermark(mIsUseWatermark ? mWatermarkSetting : null);
    }

    public void addText(StrokedTextView selectText) {
        checkToAddRectView();

        final StrokedTextView textView = new StrokedTextView(this);
        textView.setText("点击输入文字");
        textView.setTextSize(40);
        textView.setTypeface(selectText.getTypeface());
        textView.setTextColor(selectText.getTextColors());
        textView.setShadowLayer(selectText.getShadowRadius(), selectText.getShadowDx(), selectText.getShadowDy(), selectText.getShadowColor());
        textView.setAlpha(selectText.getAlpha());
        textView.setStrokeWidth(selectText.getStrokeWidth());
        textView.setStrokeColor(selectText.getStrokeColor());

        mShortVideoEditor.addTextView(textView);

        addSelectorView(textView);

        showTextViewBorder(textView);
        textView.setOnTouchListener(new ViewTouchListener(textView));

        ToastUtils.s(this, "触摸文字右下角控制缩放与旋转，双击移除。");
    }

    private void createTextDialog(final PLTextView textView) {
        final EditText edit = new EditText(VideoEditActivity.this);
        edit.setText(textView.getText());

        AlertDialog.Builder builder = new AlertDialog.Builder(VideoEditActivity.this);
        builder.setView(edit);
        builder.setTitle("请输入文字");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                textView.setText(edit.getText());
            }
        });
        builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    private void hideViewBorder() {
        if (mCurTextView != null) {
            mCurTextView.setBackgroundResource(0);
            mCurTextView = null;
        }

        if (mCurImageView != null) {
            mCurImageView.setBackgroundResource(0);
            mCurImageView = null;
        }
    }

    private void showTextViewBorder(PLTextView textView) {
        hideViewBorder();
        mCurTextView = textView;
        mCurTextView.setBackgroundResource(R.drawable.border_text_view);

        mCurView = textView;

        FrameSelectorView selectorView = (FrameSelectorView) mCurView.getTag(R.id.selector_view);
        selectorView.setVisibility(View.VISIBLE);

        View rectView = (View) mCurView.getTag(R.id.rect_view);
        if (rectView != null) {
            mFrameListView.showSelectorByRectView(selectorView, rectView);
            mFrameListView.removeRectView(rectView);
            mShortVideoEditor.setViewTimeline(mCurView, 0, mShortVideoEditor.getDurationMs());
        }

        pausePlayback();
    }

    private void showImageViewBorder(PLImageView imageView) {
        hideViewBorder();
        mCurImageView = imageView;
        mCurImageView.setBackgroundResource(R.drawable.border_text_view);

        mCurView = imageView;

        FrameSelectorView selectorView = (FrameSelectorView) mCurView.getTag(R.id.selector_view);
        selectorView.setVisibility(View.VISIBLE);

        View rectView = (View) mCurView.getTag(R.id.rect_view);
        if (rectView != null) {
            mFrameListView.showSelectorByRectView(selectorView, rectView);
            mFrameListView.removeRectView(rectView);
            mShortVideoEditor.setViewTimeline(mCurView, 0, mShortVideoEditor.getDurationMs());
        }

        pausePlayback();
    }

    private class ViewTouchListener implements View.OnTouchListener {
        private float lastTouchRawX;
        private float lastTouchRawY;
        private boolean scale;
        private boolean isViewMoved;
        private View mView;

        public ViewTouchListener(View view) {
            mView = view;
        }

        GestureDetector.SimpleOnGestureListener simpleOnGestureListener = new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if (mView instanceof PLTextView) {
                    mShortVideoEditor.removeTextView((PLTextView) mView);
                    if (mCurTextView != null) {
                        mCurTextView = null;
                    }
                } else if (mView instanceof PLImageView) {
                    mShortVideoEditor.removeImageView((PLImageView) mView);
                    if (mCurImageView != null) {
                        mCurImageView = null;
                    }
                }

                View rectView = (View) mView.getTag(R.id.rect_view);
                if (rectView != null) {
                    mFrameListView.removeRectView((View) mView.getTag(R.id.rect_view));
                }
                FrameSelectorView selectorView = (FrameSelectorView) mView.getTag(R.id.selector_view);
                if (selectorView != null) {
                    mFrameListView.removeSelectorView(selectorView);
                }
                mCurView = null;
                return true;
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                if (isViewMoved) {
                    return true;
                }
                if (mView instanceof PLTextView) {
                    createTextDialog((PLTextView) mView);
                }
                return true;
            }
        };
        final GestureDetector gestureDetector = new GestureDetector(VideoEditActivity.this, simpleOnGestureListener);

        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if (gestureDetector.onTouchEvent(event)) {
                return true;
            }

            int action = event.getAction();
            float touchRawX = event.getRawX();
            float touchRawY = event.getRawY();
            float touchX = event.getX();
            float touchY = event.getY();

            if (action == MotionEvent.ACTION_DOWN) {
                boolean xOK = touchX >= v.getWidth() * 3 / 4 && touchX <= v.getWidth();
                boolean yOK = touchY >= v.getHeight() * 2 / 4 && touchY <= v.getHeight();
                scale = xOK && yOK;

                if (mCurView != v) {
                    checkToAddRectView();
                }
                if (v instanceof PLTextView) {
                    showTextViewBorder((PLTextView) v);
                } else if (v instanceof PLImageView) {
                    showImageViewBorder((PLImageView) v);
                }
            }

            if (action == MotionEvent.ACTION_MOVE) {
                float deltaRawX = touchRawX - lastTouchRawX;
                float deltaRawY = touchRawY - lastTouchRawY;

                if (scale) {
                    // rotate
                    float centerX = v.getX() + (float) v.getWidth() / 2;
                    float centerY = v.getY() + (float) v.getHeight() / 2;
                    double angle = Math.atan2(touchRawY - centerY, touchRawX - centerX) * 180 / Math.PI;
                    v.setRotation((float) angle - 45);

                    // scale
                    float xx = (touchRawX >= centerX ? deltaRawX : -deltaRawX);
                    float yy = (touchRawY >= centerY ? deltaRawY : -deltaRawY);
                    float sf = (v.getScaleX() + xx / v.getWidth() + v.getScaleY() + yy / v.getHeight()) / 2;
                    v.setScaleX(sf);
                    v.setScaleY(sf);
                } else {
                    // translate
                    v.setTranslationX(v.getTranslationX() + deltaRawX);
                    v.setTranslationY(v.getTranslationY() + deltaRawY);
                }
                isViewMoved = true;
            }

            if (action == MotionEvent.ACTION_UP) {
                isViewMoved = false;
            }

            lastTouchRawX = touchRawX;
            lastTouchRawY = touchRawY;
            return true;
        }
    }

    public void onClickShowFilters(View v) {
        setPanelVisibility(mFiltersList, true);

        mFiltersList.setAdapter(new FilterListAdapter(mShortVideoEditor.getBuiltinFilterList()));
    }

    public void onClickShowImages(View v) {
        setPanelVisibility(mImageSelectorPanel, true);
    }

    public void onClickShowPaint(View v) {
        setPanelVisibility(mPaintSelectorPanel, true);

        if (mPaintView == null) {
            mPaintView = new PLPaintView(this, mPreviewView.getWidth(), mPreviewView.getHeight());
            mShortVideoEditor.addPaintView(mPaintView);
        }
        mPaintView.setPaintEnable(true);
        mPaintSelectorPanel.setup();
    }

    private void setPanelVisibility(View panel, boolean isVisible) {
        setPanelVisibility(panel, isVisible, false);
    }

    private void setPanelVisibility(View panel, boolean isVisible, boolean isEffect) {
        if (panel instanceof TextSelectorPanel || panel instanceof PaintSelectorPanel) {
            if (isVisible) {
                panel.setVisibility(View.VISIBLE);
                mVisibleView = mImageSelectorPanel.getVisibility() == View.VISIBLE ? mImageSelectorPanel : mFiltersList;
                mVisibleView.setVisibility(View.GONE);
            } else {
                panel.setVisibility(View.GONE);
                mVisibleView.setVisibility(View.VISIBLE);
            }
        } else {
            if (isVisible) {
                mImageSelectorPanel.setVisibility(View.GONE);
                mFiltersList.setVisibility(View.GONE);
            }
            panel.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        }
    }

    public void onClickShowMVs(View v) {
        setPanelVisibility(mFiltersList, true);
        try {
            File dir = new File(Environment.getExternalStorageDirectory() + "/ShortVideo/mvs");
            // copy mv assets to sdcard
            if (!dir.exists()) {
                dir.mkdirs();
                String[] fs = getAssets().list("mvs");
                for (String file : fs) {
                    InputStream is = getAssets().open("mvs/" + file);
                    FileOutputStream fos = new FileOutputStream(new File(dir, file));
                    byte[] buffer = new byte[1024];
                    int byteCount;
                    while ((byteCount = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, byteCount);
                    }
                    fos.flush();
                    is.close();
                    fos.close();
                }
            }

            FileReader jsonFile = new FileReader(new File(dir, "plsMVs.json"));
            StringBuilder sb = new StringBuilder();
            int read;
            char[] buf = new char[2048];
            while ((read = jsonFile.read(buf, 0, 2048)) != -1) {
                sb.append(buf, 0, read);
            }
            Log.i(TAG, sb.toString());
            JSONObject json = new JSONObject(sb.toString());
            mFiltersList.setAdapter(new MVListAdapter(json.getJSONArray("MVs")));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onClickTogglePlayback(View v) {
        if (mShortVideoEditorStatus == PLShortVideoEditorStatus.Playing) {
            pausePlayback();
        } else {
            startPlayback();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == REQUEST_CODE_PICK_AUDIO_MIX_FILE) {
            String selectedFilepath = GetPathFromUri.getPath(this, data.getData());
            Log.i(TAG, "Select file: " + selectedFilepath);
            if (!TextUtils.isEmpty(selectedFilepath)) {
                mShortVideoEditor.setAudioMixFile(selectedFilepath);
                mAudioMixSettingDialog.setMixMaxPosition(mShortVideoEditor.getAudioMixFileDuration());
                mIsMixAudio = true;
            }
        } else if (requestCode == REQUEST_CODE_MULTI_AUDIO_MIX_FILE) {
            String selectedFilepath = GetPathFromUri.getPath(this, data.getData());
            try {
                if (!mMainAudioFileAdded) {
                    mMainMixAudioFile = new PLMixAudioFile(mMp4path);
                    PLMediaFile mp4File = new PLMediaFile(mMp4path);
                    mInputMp4FileDurationMs = mp4File.getDurationMs();
                    mp4File.release();
                    mShortVideoEditor.addMixAudioFile(mMainMixAudioFile);
                    mMainAudioFileAdded = true;
                }

                PLMixAudioFile audioFile = new PLMixAudioFile(selectedFilepath);
                if (mAudioMixingFileCount == 0) {
                    ToastUtils.s(this, "添加第一个混音文件");
                    long firstMixingDurationMs = (mInputMp4FileDurationMs <= 5000) ? mInputMp4FileDurationMs : 5000;
                    audioFile.setDurationInVideo(firstMixingDurationMs * 1000);
                } else if (mAudioMixingFileCount == 1) {
                    ToastUtils.s(this, "添加第二个混音文件");
                    if (mInputMp4FileDurationMs - 5000 < 1000) {
                        ToastUtils.s(this, "视频时长过短，请选择更长的视频添加混音");
                        return;
                    }
                    audioFile.setOffsetInVideo(5000 * 1000 * mAudioMixingFileCount);
                    long secondMixingDurationMs = mInputMp4FileDurationMs - 5000;
                    audioFile.setDurationInVideo(secondMixingDurationMs * 1000);
                } else if (mAudioMixingFileCount >= 2) {
                    ToastUtils.s(this, "最多可以添加2个混音文件");
                    return;
                }
                audioFile.setVolume(0.5f);
                mShortVideoEditor.addMixAudioFile(audioFile);

                mAudioMixingFileCount++;
            } catch (IOException e) {
                e.printStackTrace();
            }

        } else if (requestCode == REQUEST_CODE_DUB) {
            String dubMp4Path = data.getStringExtra(VideoDubActivity.DUB_MP4_PATH);
            if (!TextUtils.isEmpty(dubMp4Path)) {
                finish();
                VideoEditActivity.start(this, dubMp4Path);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopPlayback();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mShortVideoEditor.setBuiltinFilter(mSelectedFilter);
        mShortVideoEditor.setMVEffect(mSelectedMV, mSelectedMask);
        mShortVideoEditor.setWatermark(mIsUseWatermark ? mWatermarkSetting : null);
        startPlayback();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mScrollTimer != null) {
            mScrollTimer.cancel();
            mScrollTimer = null;
        }
        if (mScrollTimerTask != null) {
            mScrollTimerTask.cancel();
            mScrollTimerTask = null;
        }
    }

    private void setSpeedTimeRanges() {
        PLMediaFile mediaFile = new PLMediaFile(mMp4path);
        long durationMs = mediaFile.getDurationMs();
        mediaFile.release();

        PLSpeedTimeRange plSpeedTimeRange1 = new PLSpeedTimeRange(0.5, 0, durationMs / 3);
        PLSpeedTimeRange plSpeedTimeRange2 = new PLSpeedTimeRange(1, durationMs / 3, durationMs * 2 / 3);
        PLSpeedTimeRange plSpeedTimeRange3 = new PLSpeedTimeRange(2, durationMs * 2 / 3, durationMs);

        ArrayList<PLSpeedTimeRange> speedTimeRanges = new ArrayList<>();
        speedTimeRanges.add(plSpeedTimeRange1);
        speedTimeRanges.add(plSpeedTimeRange2);
        speedTimeRanges.add(plSpeedTimeRange3);

        mShortVideoEditor.setSpeedTimeRanges(speedTimeRanges);
    }

    public void onSaveEdit(View v) {
        checkToAddRectView();
        mProcessingDialog.show();
        if (mIsRangeSpeed) {
            setSpeedTimeRanges();
        }
        if (mMainMixAudioFile != null) {
            mMainMixAudioFile.setSpeed(mSpeed);
            mMainMixAudioFile.setDurationInVideo((int) (mInputMp4FileDurationMs * 1000 / mSpeed));
        }
        mShortVideoEditor.save(new PLVideoFilterListener() {
            @Override
            public void onSurfaceCreated() {

            }

            @Override
            public void onSurfaceChanged(int width, int height) {

            }

            @Override
            public void onSurfaceDestroy() {

            }

            @Override
            public int onDrawFrame(int texId, int texWidth, int texHeight, long timestampNs, float[] transformMatrix) {
                long time = timestampNs / 1000000L;
                //三秒之后，保存的视频中水印坐标动态改变
                if (time > 3000) {
                    mSaveWatermarkSetting.setPosition(0.01f, 1);
                } else {
                    mSaveWatermarkSetting.setPosition(0.01f, 0.01f);
                }
                mShortVideoEditor.updateSaveWatermark(mIsUseWatermark ? mSaveWatermarkSetting : null);
                return texId;
            }
        });
        hideViewBorder();
    }

    @Override
    public void onSaveVideoSuccess(String filePath) {
        Log.i(TAG, "save edit success filePath: " + filePath);
        mProcessingDialog.dismiss();
        PlaybackActivity.start(VideoEditActivity.this, filePath);
    }

    @Override
    public void onSaveVideoFailed(final int errorCode) {
        Log.e(TAG, "save edit failed errorCode:" + errorCode);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mProcessingDialog.dismiss();
                ToastUtils.toastErrorCode(VideoEditActivity.this, errorCode);
            }
        });
    }

    @Override
    public void onSaveVideoCanceled() {
        mProcessingDialog.dismiss();
        mCancelSave = true;
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

    private class FilterItemViewHolder extends RecyclerView.ViewHolder {
        public ImageView mIcon;
        public TextView mName;

        public FilterItemViewHolder(View itemView) {
            super(itemView);
            mIcon = (ImageView) itemView.findViewById(R.id.icon);
            mName = (TextView) itemView.findViewById(R.id.name);
        }
    }

    private class FilterListAdapter extends RecyclerView.Adapter<FilterItemViewHolder> {
        private PLBuiltinFilter[] mFilters;

        public FilterListAdapter(PLBuiltinFilter[] filters) {
            this.mFilters = filters;
        }

        @Override
        public FilterItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);
            View contactView = inflater.inflate(R.layout.filter_item, parent, false);
            FilterItemViewHolder viewHolder = new FilterItemViewHolder(contactView);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(FilterItemViewHolder holder, int position) {
            try {
                if (position == 0) {
                    holder.mName.setText("None");
                    Bitmap bitmap = BitmapFactory.decodeStream(getAssets().open("filters/none.png"));
                    holder.mIcon.setImageBitmap(bitmap);
                    holder.mIcon.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mSelectedFilter = null;
                            mShortVideoEditor.setBuiltinFilter(null);
                        }
                    });
                    return;
                }

                final PLBuiltinFilter filter = mFilters[position - 1];
                holder.mName.setText(filter.getName());
                InputStream is = getAssets().open(filter.getAssetFilePath());
                Bitmap bitmap = BitmapFactory.decodeStream(is);
                holder.mIcon.setImageBitmap(bitmap);
                holder.mIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mSelectedFilter = filter.getName();
                        mShortVideoEditor.setBuiltinFilter(mSelectedFilter);
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            return mFilters != null ? mFilters.length + 1 : 0;
        }
    }

    private class MVListAdapter extends RecyclerView.Adapter<FilterItemViewHolder> {
        private JSONArray mMVArray;

        public MVListAdapter(JSONArray mvArray) {
            this.mMVArray = mvArray;
        }

        @Override
        public FilterItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);
            View contactView = inflater.inflate(R.layout.filter_item, parent, false);
            FilterItemViewHolder viewHolder = new FilterItemViewHolder(contactView);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(FilterItemViewHolder holder, int position) {
            final String mvsDir = Config.VIDEO_STORAGE_DIR + "mvs/";

            try {
                if (position == 0) {
                    holder.mName.setText("None");
                    Bitmap bitmap = BitmapFactory.decodeFile(mvsDir + "none.png");
                    holder.mIcon.setImageBitmap(bitmap);
                    holder.mIcon.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mSelectedMV = null;
                            mSelectedMask = null;
                            mShortVideoEditor.setMVEffect(null, null);
                        }
                    });
                    return;
                }

                final JSONObject mv = mMVArray.getJSONObject(position - 1);
                holder.mName.setText(mv.getString("name"));
                Bitmap bitmap = BitmapFactory.decodeFile(mvsDir + mv.getString("coverDir") + ".png");
                holder.mIcon.setImageBitmap(bitmap);
                holder.mIcon.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            mSelectedMV = mvsDir + mv.getString("colorDir") + ".mp4";
                            mSelectedMask = mvsDir + mv.getString("alphaDir") + ".mp4";
                            mShortVideoEditor.setMVEffect(mSelectedMV, mSelectedMask);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        public int getItemCount() {
            return mMVArray != null ? mMVArray.length() + 1 : 0;
        }
    }

    private AudioMixSettingDialog.OnAudioVolumeChangedListener mOnAudioVolumeChangedListener = new AudioMixSettingDialog.OnAudioVolumeChangedListener() {
        @Override
        public void onAudioVolumeChanged(int fgVolume, int bgVolume) {
            Log.i(TAG, "fg volume: " + fgVolume + " bg volume: " + bgVolume);
            mShortVideoEditor.setAudioMixVolume(fgVolume / 100f, bgVolume / 100f);
            mIsMuted = fgVolume == 0;
            mMuteButton.setImageResource(mIsMuted ? R.mipmap.btn_mute : R.mipmap.btn_unmute);
        }
    };

    private AudioMixSettingDialog.OnPositionSelectedListener mOnPositionSelectedListener = new AudioMixSettingDialog.OnPositionSelectedListener() {
        @Override
        public void onPositionSelected(long position) {
            Log.i(TAG, "selected position: " + position);
            mShortVideoEditor.setAudioMixFileRange(position, position + mMixDuration);
        }
    };
}
