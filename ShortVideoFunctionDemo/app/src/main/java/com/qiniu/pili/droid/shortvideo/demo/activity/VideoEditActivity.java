package com.qiniu.pili.droid.shortvideo.demo.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.qiniu.pili.droid.shortvideo.PLBuiltinFilter;
import com.qiniu.pili.droid.shortvideo.PLGifWatermarkSetting;
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
import com.qiniu.pili.droid.shortvideo.demo.utils.MediaStoreUtils;
import com.qiniu.pili.droid.shortvideo.demo.utils.ToastUtils;
import com.qiniu.pili.droid.shortvideo.demo.view.AudioMixSettingDialog;
import com.qiniu.pili.droid.shortvideo.demo.view.CustomProgressDialog;
import com.qiniu.pili.droid.shortvideo.demo.view.FrameListView;
import com.qiniu.pili.droid.shortvideo.demo.view.FrameSelectorView;
import com.qiniu.pili.droid.shortvideo.demo.view.GifSelectorPanel;
import com.qiniu.pili.droid.shortvideo.demo.view.ImageSelectorPanel;
import com.qiniu.pili.droid.shortvideo.demo.view.OnStickerOperateListener;
import com.qiniu.pili.droid.shortvideo.demo.view.PaintSelectorPanel;
import com.qiniu.pili.droid.shortvideo.demo.view.StrokedTextView;
import com.qiniu.pili.droid.shortvideo.demo.view.TextSelectorPanel;
import com.qiniu.pili.droid.shortvideo.demo.view.sticker.StickerImageView;
import com.qiniu.pili.droid.shortvideo.demo.view.sticker.StickerTextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static com.qiniu.pili.droid.shortvideo.demo.utils.RecordSettings.RECORD_SPEED_ARRAY;

public class VideoEditActivity extends Activity implements PLVideoSaveListener {
    private static final String TAG = "VideoEditActivity";
    private static final String MP4_PATH = "MP4_PATH";

    private static final int REQUEST_CODE_PICK_AUDIO_MIX_FILE = 0;
    private static final int REQUEST_CODE_DUB = 1;
    private static final int REQUEST_CODE_MULTI_AUDIO_MIX_FILE = 2;

    private int mRotation = 0;

    // 视频编辑器预览状态
    private enum PLShortVideoEditorStatus {
        Idle, Playing, Paused,
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
    private GifSelectorPanel mGifSelectorPanel;

    private int mFgVolumeBeforeMute = 100;
    private long mMixDuration = 5000; // ms
    private boolean mIsMuted = false;
    private boolean mIsMixAudio = false;
    private boolean mIsUseWatermark = true;

    private String mMp4path;

    private TextView mSpeedTextView;
    private View mVisibleView;

    private FrameListView mFrameListView;
    private TimerTask mScrollTimerTask;
    private Timer mScrollTimer;
    private View mCurView;
    private boolean mIsRangeSpeed;

    /**
     * Gif 动图相关
     */
    private final Map<StickerImageView, PLGifWatermarkSetting> mGifViewSettings = new HashMap<>();
    private FrameLayout mStickerViewGroup;

    // audio mixing mode:
    // 0 - single audio mixing;
    // 1 - multiple audio mixing;
    // 单混音和多重混音为互斥模式，最多只可选择其中一项。
    private int mAudioMixingMode = -1;
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

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);

        setContentView(R.layout.activity_editor);

        mSpeedTextView = findViewById(R.id.normal_speed_text);
        mMuteButton = findViewById(R.id.mute_button);
        mPausePalybackButton = findViewById(R.id.pause_playback);
        mSpeedPanel = findViewById(R.id.speed_panel);
        mFrameListView = findViewById(R.id.frame_list_view);
        mStickerViewGroup = findViewById(R.id.sticker_container_view);

        initPreviewView();
        initTextSelectorPanel();
        initPaintSelectorPanel();
        initImageSelectorPanel();
        initGifSelectorPanel();
        initProcessingDialog();
        initWatermarkSetting();
        initShortVideoEditor();
        initFiltersList();
        initAudioMixSettingDialog();
        initResources();

        mStickerViewGroup.post(this::initGifViewGroup);
    }

    private void initPreviewView() {
        mPreviewView = findViewById(R.id.preview);
        mPreviewView.setOnClickListener(v -> saveViewTimeAndHideRect());
    }

    private void initTextSelectorPanel() {
        mTextSelectorPanel = findViewById(R.id.text_selector_panel);
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
        mPaintSelectorPanel = findViewById(R.id.paint_selector_panel);
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
        mImageSelectorPanel = findViewById(R.id.image_selector_panel);
        mImageSelectorPanel.setOnImageSelectedListener(this::addImageView);
    }

    private void initGifSelectorPanel() {
        mGifSelectorPanel = findViewById(R.id.gif_selector_panel);
        mGifSelectorPanel.setOnGifSelectedListener(this::addGif);
    }

    private void initProcessingDialog() {
        mProcessingDialog = new CustomProgressDialog(this);
        mProcessingDialog.setOnCancelListener(dialog -> mShortVideoEditor.cancelSave());
    }

    private void initWatermarkSetting() {
        mWatermarkSetting = createWatermarkSetting();
        // 动态水印设置
        mPreviewWatermarkSetting = createWatermarkSetting();
        mSaveWatermarkSetting = createWatermarkSetting();
    }

    private void initShortVideoEditor() {
        mMp4path = getIntent().getStringExtra(MP4_PATH);
        Log.i(TAG, "editing file: " + mMp4path);

        PLVideoEditSetting setting = new PLVideoEditSetting();
        setting.setSourceFilepath(mMp4path);
        setting.setDestFilepath(Config.EDITED_FILE_PATH);
        setting.setGifPreviewEnabled(false);

        mShortVideoEditor = new PLShortVideoEditor(mPreviewView);
        mShortVideoEditor.setVideoEditSetting(setting);
        mShortVideoEditor.setVideoSaveListener(this);

        mMixDuration = mShortVideoEditor.getDurationMs();

        mFrameListView.setVideoPath(mMp4path);
        mFrameListView.setOnVideoFrameScrollListener(timeMs -> {
            if (mShortVideoEditorStatus == PLShortVideoEditorStatus.Playing) {
                pausePlayback();
            }
            mShortVideoEditor.seekTo((int) timeMs);

            runOnUiThread(() -> changeGifVisiable(timeMs));
        });

        initTimerTask();
    }

    private void initFiltersList() {
        mFiltersList = findViewById(R.id.recycler_view);
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

    /**
     * 拷贝资源从 Assets 到 SD 卡
     */
    private void initResources() {
        SharedPreferences sharedPreferences = getSharedPreferences(Config.SP_NAME, MODE_PRIVATE);
        boolean mvPrepared = sharedPreferences.getBoolean(Config.SP_RESOURCE_PREPARED_MV, false);
        boolean gifPrepared = sharedPreferences.getBoolean(Config.SP_RESOURCE_PREPARED_GIF, false);

        try {
            // copy mv assets to sdcard
            if (!mvPrepared) {
                File dir = new File(Config.MV_DIR);
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
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(Config.SP_RESOURCE_PREPARED_MV, true);
                editor.apply();
            }


            // copy gif assets to sdcard
            File dir = new File(Config.GIF_STICKER_DIR);
            if (!gifPrepared) {
                dir.mkdirs();
                String[] fs = getAssets().list("gif");
                for (String file : fs) {
                    InputStream is = getAssets().open("gif/" + file);
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
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(Config.SP_RESOURCE_PREPARED_GIF, true);
                editor.apply();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 添加文字贴图
     */
    public void addText(StrokedTextView selectText) {
        saveViewTimeAndHideRect();

        StickerTextView stickerTextView = (StickerTextView) View.inflate(VideoEditActivity.this, R.layout.sticker_text_view, null);
        stickerTextView.setText(selectText.getText().toString());
        stickerTextView.setTextColor(selectText.getCurrentTextColor());
        stickerTextView.setTypeface(selectText.getTypeface());
        stickerTextView.setShadowLayer(selectText.getShadowRadius(), selectText.getShadowDx(), selectText.getShadowDy(), selectText.getShadowColor());
        stickerTextView.setStrokeWidth(selectText.getStrokeWidth());
        stickerTextView.setStrokeColor(selectText.getStrokeColor());
        mShortVideoEditor.addTextView(stickerTextView);

        stickerTextView.setOnStickerOperateListener(new StickerOperateListener(stickerTextView));
        addSelectorView(stickerTextView);
        showViewBorder(stickerTextView);
    }

    /**
     * 添加图片贴图
     */
    private void addImageView(final Drawable drawable) {
        if (mCurView != null) {
            final FrameSelectorView selectedView = (FrameSelectorView) mCurView.getTag(R.id.selector_view);
            selectedView.post(new Runnable() {
                @Override
                public void run() {

                    saveViewTimeAndHideRect();

                    StickerImageView stickerImageView = (StickerImageView) View.inflate(VideoEditActivity.this, R.layout.sticker_image_view, null);
                    stickerImageView.setImageDrawable(drawable);

                    mShortVideoEditor.addImageView(stickerImageView);
                    stickerImageView.setOnStickerOperateListener(new StickerOperateListener(stickerImageView));

                    addSelectorView(stickerImageView);
                    showViewBorder(stickerImageView);
                }
            });
        } else {
            StickerImageView stickerImageView = (StickerImageView) View.inflate(VideoEditActivity.this, R.layout.sticker_image_view, null);
            stickerImageView.setImageDrawable(drawable);

            mShortVideoEditor.addImageView(stickerImageView);
            stickerImageView.setOnStickerOperateListener(new StickerOperateListener(stickerImageView));

            addSelectorView(stickerImageView);
            showViewBorder(stickerImageView);
        }
    }

    /**
     * 添加 GIF 贴图
     */
    private void addGif(String gifPath) {
        saveViewTimeAndHideRect();

        StickerImageView stickerImageView = (StickerImageView) View.inflate(VideoEditActivity.this, R.layout.sticker_image_view, null);
        stickerImageView.setGifFile(gifPath);
        stickerImageView.startGifPlaying();

        addSelectorView(stickerImageView);

        mStickerViewGroup.addView(stickerImageView);
        mStickerViewGroup.setVisibility(View.VISIBLE);

        PLGifWatermarkSetting gifWatermarkSetting = getGifSettingFromSticker(stickerImageView);
        mGifViewSettings.put(stickerImageView, gifWatermarkSetting);
        mShortVideoEditor.addGifWatermark(gifWatermarkSetting);

        stickerImageView.setOnStickerOperateListener(new StickerOperateListener(stickerImageView));
        showViewBorder(stickerImageView);
    }

    /**
     * 添加贴图所绑定的时间线
     */
    private void addSelectorView(View view) {
        View selectorView = mFrameListView.addSelectorView();
        view.setTag(R.id.selector_view, selectorView);
    }

    /**
     * 根据 StickerImageView 创建对应的 PLGifWatermarkSetting
     */
    private PLGifWatermarkSetting getGifSettingFromSticker(StickerImageView stickerImageView) {
        PLGifWatermarkSetting gifWatermarkSetting = new PLGifWatermarkSetting();
        gifWatermarkSetting.setFilePath(stickerImageView.getGifPath());
        gifWatermarkSetting.setDisplayPeriod(stickerImageView.getStartTime(), stickerImageView.getEndTime() - stickerImageView.getStartTime());
        gifWatermarkSetting.setPosition(stickerImageView.getViewX() / mPreviewView.getWidth(), stickerImageView.getViewY() / mPreviewView.getHeight());
        gifWatermarkSetting.setRotation((int) stickerImageView.getImageDegree());
        gifWatermarkSetting.setAlpha(255);
        gifWatermarkSetting.setSize(stickerImageView.getImageWidth() * stickerImageView.getImageScale() / mPreviewView.getWidth(), stickerImageView.getImageHeight() * stickerImageView.getImageScale() / mPreviewView.getHeight());
        return gifWatermarkSetting;
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
                    final int time = mShortVideoEditor.getCurrentPosition();
                    //三秒之后，预览视频的水印坐标动态改变
                    if (time > 3000) {
                        mPreviewWatermarkSetting.setPosition(0.01f, 1);
                    } else {
                        mPreviewWatermarkSetting.setPosition(0.01f, 0.01f);
                    }
                    mShortVideoEditor.updatePreviewWatermark(mIsUseWatermark ? mPreviewWatermarkSetting : null);

                    runOnUiThread(() -> changeGifVisiable(time));

                    return texId;
                }
            });
            mShortVideoEditorStatus = PLShortVideoEditorStatus.Playing;
        } else if (mShortVideoEditorStatus == PLShortVideoEditorStatus.Paused) {
            mShortVideoEditor.resumePlayback();
            mShortVideoEditorStatus = PLShortVideoEditorStatus.Playing;
        }
        mPausePalybackButton.setImageResource(R.drawable.btn_pause);
    }

    /**
     * 停止预览
     */
    private void stopPlayback() {
        mShortVideoEditor.stopPlayback();
        mShortVideoEditorStatus = PLShortVideoEditorStatus.Idle;
        mPausePalybackButton.setImageResource(R.drawable.btn_play);
    }

    /**
     * 暂停预览
     */
    private void pausePlayback() {
        mShortVideoEditor.pausePlayback();
        mShortVideoEditorStatus = PLShortVideoEditorStatus.Paused;
        mPausePalybackButton.setImageResource(R.drawable.btn_play);
    }

    /**
     * 显示速度改变面板
     */
    public void onClickShowSpeed(View view) {
        mSpeedPanel.setVisibility((mSpeedPanel.getVisibility() == View.GONE) ? View.VISIBLE : View.GONE);
    }

    /**
     * 改变视频播放速度
     */
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
            default:
                break;
        }

        mSpeed = recordSpeed;
        mShortVideoEditor.setSpeed(mSpeed, true);
    }

    /**
     * 根据当前的播放时间控制 GIF 的显示
     */
    private void changeGifVisiable(final long timeMS) {
        for (final StickerImageView gifViews : mGifViewSettings.keySet()) {
            if (gifViews.getStartTime() == 0 && gifViews.getEndTime() == 0) {
                //刚刚添加，未对时间进行赋值
                gifViews.setVisibility(View.VISIBLE);
                continue;
            }
            if (timeMS >= gifViews.getStartTime() && timeMS <= gifViews.getEndTime()) {
                gifViews.setVisibility(View.VISIBLE);
            } else {
                gifViews.setVisibility(View.GONE);
            }
        }
    }

    private void initTimerTask() {
        mScrollTimerTask = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(() -> {
                    if (mShortVideoEditorStatus == PLShortVideoEditorStatus.Playing) {
                        int position = mShortVideoEditor.getCurrentPosition();
                        mFrameListView.scrollToTime(position);
                    }
                });
            }
        };
        mScrollTimer = new Timer();
        // scroll fps:20
        mScrollTimer.schedule(mScrollTimerTask, 50, 50);
    }

    /**
     * 创建水印
     */
    private PLWatermarkSetting createWatermarkSetting() {
        PLWatermarkSetting watermarkSetting = new PLWatermarkSetting();
        watermarkSetting.setResourceId(R.drawable.qiniu_logo);
        watermarkSetting.setPosition(0.01f, 0.01f);
        watermarkSetting.setAlpha(128);
        watermarkSetting.setSize(0.1f, 0.1f);
        return watermarkSetting;
    }

    /**
     * 调整 GIF 容器的宽高
     */
    private void initGifViewGroup() {
        ViewGroup.LayoutParams surfaceLayout = mStickerViewGroup.getLayoutParams();

        //根据视频所携带的角度进行调整
        PLMediaFile mediaFile = new PLMediaFile(mMp4path);
        int outputWidth = mediaFile.getVideoWidth();
        int outputHeight = mediaFile.getVideoHeight();
        int rotation = mediaFile.getVideoRotation();
        if ((rotation == 90 || rotation == 270)) {
            int temp = outputWidth;
            outputWidth = outputHeight;
            outputHeight = temp;
        }
        //根据视频的宽高进行调整
        if (outputWidth > outputHeight) {
            surfaceLayout.width = mPreviewView.getWidth();
            surfaceLayout.height = Math.round((float) outputHeight * mPreviewView.getWidth() / outputWidth);
        } else {
            surfaceLayout.height = mPreviewView.getHeight();
            surfaceLayout.width = Math.round((float) outputWidth * mPreviewView.getHeight() / outputHeight);
        }
        //移动 GIF 容器以覆盖预览画面
        mStickerViewGroup.setLayoutParams(surfaceLayout);
        mStickerViewGroup.setTranslationX(mPreviewView.getWidth() - surfaceLayout.width);
        mStickerViewGroup.setTranslationY((mPreviewView.getHeight() - surfaceLayout.height) / 2);
        mStickerViewGroup.requestLayout();
    }

    /**
     * 保存当前 View 的时间调整并隐藏编辑框
     */
    private void saveViewTimeAndHideRect() {
        if (mCurView != null) {
            View rectView = mFrameListView.addSelectedRect((View) mCurView.getTag(R.id.selector_view));
            if (rectView != null) {
                mCurView.setTag(R.id.rect_view, rectView);
                FrameListView.SectionItem sectionItem = mFrameListView.getSectionByRectView(rectView);
                if (mCurView instanceof StickerImageView && ((StickerImageView) mCurView).getGifPath() != null) {
                    ((StickerImageView) mCurView).setTime(sectionItem.getStartTime(), sectionItem.getEndTime());
                    saveGifSetting();
                } else {
                    mShortVideoEditor.setViewTimeline(mCurView, sectionItem.getStartTime(), (sectionItem.getEndTime() - sectionItem.getStartTime()));
                }
                mCurView.setSelected(false);
                mCurView = null;
            } else {
                if (mCurView instanceof StickerImageView && ((StickerImageView) mCurView).getGifPath() != null) {
                    ((StickerImageView) mCurView).setTime(0, 0);
                    saveGifSetting();
                } else {
                    mShortVideoEditor.setViewTimeline(mCurView, 0, 0);
                }
            }
        }
    }

    /**
     * 重置编辑
     */
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

    /**
     * 单混音
     */
    public void onClickMix(View v) {
        if (mAudioMixingMode == 1) {
            ToastUtils.showShortToast("已选择多重混音，无法再选择单混音！");
            return;
        }
        mAudioMixingMode = 0;
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("audio/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, REQUEST_CODE_PICK_AUDIO_MIX_FILE);
    }

    /**
     * 多重混音
     */
    public void onClickMultipleAudioMixing(View v) {
        if (mAudioMixingMode == 0) {
            ToastUtils.showShortToast("已选择单混音，无法再选择多重混音！");
            return;
        }
        mAudioMixingMode = 1;
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("audio/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, REQUEST_CODE_MULTI_AUDIO_MIX_FILE);
    }

    /**
     * 静音
     */
    public void onClickMute(View v) {
        mIsMuted = !mIsMuted;
        mShortVideoEditor.muteOriginAudio(mIsMuted);
        mMuteButton.setImageResource(mIsMuted ? R.drawable.btn_mute : R.drawable.btn_unmute);
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

    /**
     * 显示文字贴图面板
     */
    public void onClickTextSelect(View v) {
        setPanelVisibility(mTextSelectorPanel, true);
    }

    /**
     * 配音
     */
    public void onClickDubAudio(View v) {
        Intent intent = new Intent(this, VideoDubActivity.class);
        intent.putExtra(VideoDubActivity.MP4_PATH, mMp4path);
        startActivityForResult(intent, REQUEST_CODE_DUB);
    }

    /**
     * 点击混音 弹窗
     */
    public void onClickAudioMixSetting(View v) {
        if (mIsMixAudio) {
            mAudioMixSettingDialog.show();
        } else {
            ToastUtils.showShortToast("请先选择混音文件！");
        }
    }

    /**
     * 点击返回
     */
    public void onClickBack(View v) {
        finish();
    }

    /**
     * 添加水印
     */
    public void onClickToggleWatermark(View v) {
        mIsUseWatermark = !mIsUseWatermark;
        mShortVideoEditor.setWatermark(mIsUseWatermark ? mWatermarkSetting : null);
    }

    /**
     * 显示动图添加面板
     */
    public void onClickToggleGifWatermark(View v) {
        setPanelVisibility(mGifSelectorPanel, true);
    }

    /**
     * 旋转视频
     */
    public void onClickRotate(View v) {
        mRotation = (mRotation + 90) % 360;
        mShortVideoEditor.setRotation(mRotation);
        for (PLGifWatermarkSetting gifWatermarkSetting : mGifViewSettings.values()) {
            mShortVideoEditor.addGifWatermark(gifWatermarkSetting);
        }
        startPlayback();
    }

    /**
     * 创建文字编辑弹窗并显示
     */
    private void createTextDialog(final PLTextView textView) {
        final EditText edit = new EditText(VideoEditActivity.this);
        edit.setText(textView.getText());

        AlertDialog.Builder builder = new AlertDialog.Builder(VideoEditActivity.this);
        builder.setView(edit);
        builder.setTitle("请输入文字");
        builder.setPositiveButton("确定", (dialog, which) -> ((StickerTextView) textView).setText(edit.getText().toString()));
        builder.setNegativeButton("取消", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    /**
     * 显示编辑框并为其添加初始时间
     */
    private void showViewBorder(View view) {
        mCurView = view;
        mCurView.setSelected(true);

        pausePlayback();
    }

    /**
     * 贴图操作监听
     */
    private class StickerOperateListener implements OnStickerOperateListener {

        private final View mView;

        StickerOperateListener(View view) {
            mView = view;
        }

        /**
         * 当点击删除贴图
         */
        @Override
        public void onDeleteClicked() {
            if (mView instanceof StickerTextView) {
                mShortVideoEditor.removeTextView((PLTextView) mView);
            } else {
                if (((StickerImageView) mView).getGifPath() != null) {
                    mStickerViewGroup.removeView(mView);
                    mShortVideoEditor.removeGifWatermark(mGifViewSettings.get(mView));
                    mGifViewSettings.remove(mView);
                } else {
                    mShortVideoEditor.removeImageView((PLImageView) mView);
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
        }

        /**
         * 当点击贴图编辑
         */
        @Override
        public void onEditClicked() {
            if (mView instanceof StickerTextView) {
                createTextDialog((StickerTextView) mView);
            }
        }

        /**
         * 当贴图被选中
         */
        @Override
        public void onStickerSelected() {
            if (mCurView != mView) {
                saveViewTimeAndHideRect();
                mCurView = mView;
                FrameSelectorView selectorView = (FrameSelectorView) mCurView.getTag(R.id.selector_view);
                selectorView.setVisibility(View.VISIBLE);
                View rectView = (View) mCurView.getTag(R.id.rect_view);
                if (rectView != null) {
                    mFrameListView.showSelectorByRectView(selectorView, rectView);
                    mFrameListView.removeRectView(rectView);
                }
            }

        }

    }

    /**
     * 显示滤镜选择面板
     */
    public void onClickShowFilters(View v) {
        setPanelVisibility(mFiltersList, true);
        mFiltersList.setAdapter(new FilterListAdapter(mShortVideoEditor.getBuiltinFilterList()));
    }

    /**
     * 显示贴图面板
     */
    public void onClickShowImages(View v) {
        setPanelVisibility(mImageSelectorPanel, true);
    }

    /**
     * 显示涂鸦面板
     */
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

    /**
     * 控制各种面板的显示
     */
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
                mGifSelectorPanel.setVisibility(View.GONE);
            }
            panel.setVisibility(isVisible ? View.VISIBLE : View.GONE);
        }
    }

    /**
     * 显示 MV 选择面板 拷贝资源
     */
    public void onClickShowMVs(View v) {
        setPanelVisibility(mFiltersList, true);
        try {
            FileReader jsonFile = new FileReader(new File(Config.MV_DIR, "plsMVs.json"));
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

    /**
     * 改变视频播放状态
     */
    public void onClickTogglePlayback(View v) {
        if (mShortVideoEditorStatus == PLShortVideoEditorStatus.Playing) {
            saveViewTimeAndHideRect();
            pausePlayback();
        } else {
            saveViewTimeAndHideRect();
            startPlayback();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }
        if (requestCode == REQUEST_CODE_PICK_AUDIO_MIX_FILE && data.getData() != null) {
            String selectedFilepath = GetPathFromUri.getRealPathFromURI(this, data.getData());
            Log.i(TAG, "Select file: " + selectedFilepath);
            if (!TextUtils.isEmpty(selectedFilepath)) {
                mShortVideoEditor.setAudioMixFile(selectedFilepath);
                mAudioMixSettingDialog.setMixMaxPosition(mShortVideoEditor.getAudioMixFileDuration());
                mIsMixAudio = true;
            }
        } else if (requestCode == REQUEST_CODE_MULTI_AUDIO_MIX_FILE && data.getData() != null) {
            String selectedFilepath = GetPathFromUri.getRealPathFromURI(this, data.getData());
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
                    ToastUtils.showShortToast("添加第一个混音文件");
                    long firstMixingDurationMs = (mInputMp4FileDurationMs <= 5000) ? mInputMp4FileDurationMs : 5000;
                    audioFile.setDurationInVideo(firstMixingDurationMs * 1000);
                } else if (mAudioMixingFileCount == 1) {
                    ToastUtils.showShortToast("添加第二个混音文件");
                    if (mInputMp4FileDurationMs - 5000 < 1000) {
                        ToastUtils.showShortToast("视频时长过短，请选择更长的视频添加混音");
                        return;
                    }
                    audioFile.setOffsetInVideo(5000 * 1000 * mAudioMixingFileCount);
                    long secondMixingDurationMs = mInputMp4FileDurationMs - 5000;
                    audioFile.setDurationInVideo(secondMixingDurationMs * 1000);
                } else if (mAudioMixingFileCount >= 2) {
                    ToastUtils.showShortToast("最多可以添加2个混音文件");
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
    protected void onResume() {
        super.onResume();
        mShortVideoEditor.setBuiltinFilter(mSelectedFilter);
        mShortVideoEditor.setMVEffect(mSelectedMV, mSelectedMask);
        mShortVideoEditor.setWatermark(mIsUseWatermark ? mWatermarkSetting : null);
        for (PLGifWatermarkSetting gifWatermarkSetting : mGifViewSettings.values()) {
            mShortVideoEditor.addGifWatermark(gifWatermarkSetting);
        }
        startPlayback();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopPlayback();
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

    /**
     * 设置添加变速时间段
     */
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

    /**
     * 保存视频
     */
    public void onSaveEdit(View v) {
        saveViewTimeAndHideRect();
        startPlayback();
        mProcessingDialog.show();
        mProcessingDialog.setProgress(0);
        if (mIsRangeSpeed) {
            setSpeedTimeRanges();
        }
        if (mMainMixAudioFile != null) {
            mMainMixAudioFile.setSpeed(mSpeed);
            mMainMixAudioFile.setDurationInVideo((int) (mInputMp4FileDurationMs * 1000 / mSpeed));
        }
        mSaveWatermarkSetting.setZOrderOnTop(true);
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
    }

    /**
     * 更新GIF设置
     */
    private void saveGifSetting() {
        if (mCurView != null && mCurView instanceof StickerImageView && ((StickerImageView) mCurView).getGifPath() != null) {
            StickerImageView stickerImageView = (StickerImageView) mCurView;
            PLGifWatermarkSetting gifWatermarkSetting = mGifViewSettings.get(stickerImageView);
            gifWatermarkSetting.setDisplayPeriod(stickerImageView.getStartTime(), stickerImageView.getEndTime() - stickerImageView.getStartTime());
            gifWatermarkSetting.setPosition(stickerImageView.getViewX() / mStickerViewGroup.getWidth(), stickerImageView.getViewY() / mStickerViewGroup.getHeight());
            gifWatermarkSetting.setRotation((int) stickerImageView.getImageDegree());
            gifWatermarkSetting.setAlpha(255);
            gifWatermarkSetting.setSize(stickerImageView.getImageWidth() * stickerImageView.getImageScale() / mStickerViewGroup.getWidth(), stickerImageView.getImageHeight() * stickerImageView.getImageScale() / mStickerViewGroup.getHeight());
            mShortVideoEditor.updateGifWatermark(gifWatermarkSetting);
        }
    }

    /**
     * 视频保存成功回调
     */
    @Override
    public void onSaveVideoSuccess(String filePath) {
        Log.i(TAG, "save edit success filePath: " + filePath);
        MediaStoreUtils.storeVideo(VideoEditActivity.this, new File(filePath), "video/mp4");
        mProcessingDialog.dismiss();
        PlaybackActivity.start(VideoEditActivity.this, filePath);
    }

    /**
     * 视频保存失败回调
     */
    @Override
    public void onSaveVideoFailed(final int errorCode) {
        Log.e(TAG, "save edit failed errorCode:" + errorCode);
        runOnUiThread(() -> {
            mProcessingDialog.dismiss();
            ToastUtils.toastErrorCode(errorCode);
        });
    }

    /**
     * 视频保存取消回调
     */
    @Override
    public void onSaveVideoCanceled() {
        mProcessingDialog.dismiss();
    }

    /**
     * 视频保存进度更新
     */
    @Override
    public void onProgressUpdate(final float percentage) {
        runOnUiThread(() -> mProcessingDialog.setProgress((int) (100 * percentage)));
    }

    private static class FilterItemViewHolder extends RecyclerView.ViewHolder {
        public ImageView mIcon;
        public TextView mName;

        public FilterItemViewHolder(View itemView) {
            super(itemView);
            mIcon = itemView.findViewById(R.id.icon);
            mName = itemView.findViewById(R.id.name);
        }
    }

    /**
     * 滤镜选择列表
     */
    private class FilterListAdapter extends RecyclerView.Adapter<FilterItemViewHolder> {
        private final PLBuiltinFilter[] mFilters;

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
            if (position == 0) {
                holder.mName.setText("None");
                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.filter_none);
                holder.mIcon.setImageBitmap(bitmap);
                holder.mIcon.setOnClickListener(v -> {
                    mSelectedFilter = null;
                    mShortVideoEditor.setBuiltinFilter(null);
                });
                return;
            }

            try {
                final PLBuiltinFilter filter = mFilters[position - 1];
                holder.mName.setText(filter.getName());
                InputStream is = getAssets().open(filter.getAssetFilePath());
                Bitmap bitmap = BitmapFactory.decodeStream(is);
                holder.mIcon.setImageBitmap(bitmap);
                holder.mIcon.setOnClickListener(v -> {
                    mSelectedFilter = filter.getName();
                    mShortVideoEditor.setBuiltinFilter(mSelectedFilter);
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

    /**
     * MV 选择列表
     */
    private class MVListAdapter extends RecyclerView.Adapter<FilterItemViewHolder> {
        private final JSONArray mMVArray;

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
                    holder.mIcon.setOnClickListener(v -> {
                        mSelectedMV = null;
                        mSelectedMask = null;
                        mShortVideoEditor.setMVEffect(null, null);
                    });
                    return;
                }

                final JSONObject mv = mMVArray.getJSONObject(position - 1);
                holder.mName.setText(mv.getString("name"));
                Bitmap bitmap = BitmapFactory.decodeFile(mvsDir + mv.getString("coverDir") + ".png");
                holder.mIcon.setImageBitmap(bitmap);
                holder.mIcon.setOnClickListener(v -> {
                    try {
                        mSelectedMV = mvsDir + mv.getString("colorDir") + ".mp4";
                        mSelectedMask = mvsDir + mv.getString("alphaDir") + ".mp4";
                        mShortVideoEditor.setMVEffect(mSelectedMV, mSelectedMask);
                    } catch (JSONException e) {
                        e.printStackTrace();
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

    /**
     * 混音音量改变监听
     */
    private final AudioMixSettingDialog.OnAudioVolumeChangedListener mOnAudioVolumeChangedListener = new AudioMixSettingDialog.OnAudioVolumeChangedListener() {
        @Override
        public void onAudioVolumeChanged(int fgVolume, int bgVolume) {
            Log.i(TAG, "fg volume: " + fgVolume + " bg volume: " + bgVolume);
            mShortVideoEditor.setAudioMixVolume(fgVolume / 100f, bgVolume / 100f);
            mIsMuted = fgVolume == 0;
            mMuteButton.setImageResource(mIsMuted ? R.drawable.btn_mute : R.drawable.btn_unmute);
        }
    };

    /**
     * 混音位置改变监听
     */
    private final AudioMixSettingDialog.OnPositionSelectedListener mOnPositionSelectedListener = new AudioMixSettingDialog.OnPositionSelectedListener() {
        @Override
        public void onPositionSelected(long position) {
            Log.i(TAG, "selected position: " + position);
            mShortVideoEditor.setAudioMixFileRange(position, position + mMixDuration);
        }
    };
}
