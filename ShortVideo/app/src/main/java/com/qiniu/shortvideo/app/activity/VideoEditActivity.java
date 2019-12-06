package com.qiniu.shortvideo.app.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.qiniu.pili.droid.shortvideo.PLBuiltinFilter;
import com.qiniu.pili.droid.shortvideo.PLGifWatermarkSetting;
import com.qiniu.pili.droid.shortvideo.PLMixAudioFile;
import com.qiniu.pili.droid.shortvideo.PLTextView;
import com.qiniu.shortvideo.app.R;
import com.qiniu.shortvideo.app.adapter.FilterItemAdapter;
import com.qiniu.shortvideo.app.adapter.MVItemAdapter;
import com.qiniu.shortvideo.app.model.AudioFile;
import com.qiniu.shortvideo.app.tusdk.AdvancedFilterBottomView;
import com.qiniu.shortvideo.app.tusdk.SceneRecyclerAdapter;
import com.qiniu.shortvideo.app.tusdk.SpecialEffectBottomView;
import com.qiniu.shortvideo.app.tusdk.TuSDKManager;
import com.qiniu.shortvideo.app.utils.LoadFrameTask;
import com.qiniu.shortvideo.app.utils.ToastUtils;
import com.qiniu.shortvideo.app.utils.Utils;
import com.qiniu.shortvideo.app.utils.ViewOperator;
import com.qiniu.shortvideo.app.view.CustomProgressDialog;
import com.qiniu.shortvideo.app.utils.Config;
import com.qiniu.shortvideo.app.view.GifBottomView;
import com.qiniu.shortvideo.app.view.ListBottomView;
import com.qiniu.shortvideo.app.view.MusicSelectBottomView;
import com.qiniu.shortvideo.app.view.PaintBottomView;
import com.qiniu.pili.droid.shortvideo.PLMediaFile;
import com.qiniu.pili.droid.shortvideo.PLPaintView;
import com.qiniu.pili.droid.shortvideo.PLShortVideoEditor;
import com.qiniu.pili.droid.shortvideo.PLVideoEditSetting;
import com.qiniu.pili.droid.shortvideo.PLVideoFilterListener;
import com.qiniu.pili.droid.shortvideo.PLVideoSaveListener;
import com.qiniu.shortvideo.app.view.StickerBottomView;
import com.qiniu.shortvideo.app.view.TextBottomView;
import com.qiniu.shortvideo.app.view.VolumeSettingBottomView;
import com.qiniu.shortvideo.app.view.layer.StickerTextView;
import com.qiniu.shortvideo.app.view.layer.StickerViewGroup;
import com.qiniu.shortvideo.app.view.layer.StickerImageView;

import org.json.JSONObject;
import org.lasque.tusdk.api.video.preproc.filter.TuSDKVideoProcesser;
import org.lasque.tusdk.core.TuSdkContext;
import org.lasque.tusdk.core.utils.ThreadHelper;
import org.lasque.tusdk.video.editor.TuSdkMediaEffectData;
import org.lasque.tusdk.video.editor.TuSdkMediaFilterEffectData;
import org.lasque.tusdk.video.editor.TuSdkMediaSceneEffectData;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import static com.qiniu.shortvideo.app.activity.VideoEditActivity.EDITOR_MODE.CAPTION;
import static com.qiniu.shortvideo.app.activity.VideoEditActivity.EDITOR_MODE.GIF_STICKER;

public class VideoEditActivity extends AppCompatActivity implements
        PLVideoSaveListener,
        PaintBottomView.OnPaintSelectorListener,
        StickerViewGroup.OnItemClickListener,
        View.OnClickListener {
    private static final String TAG = "VideoEditActivity";

    private static final String MP4_PATH = "MP4_PATH";
    private static final int CHOOSE_MUSIC_REQUEST_CODE = 0;
    private static final int CHOOSE_MUSIC_RESULT_CODE = 1;

    private enum PLShortVideoEditorStatus {
        IDLE,
        PLAYING,
        PAUSED,
    }

    @IntDef({GIF_STICKER, CAPTION})
    @Retention(RetentionPolicy.SOURCE)
    public @interface EDITOR_MODE {
        int GIF_STICKER = 0;
        int CAPTION = 1;
    }
    private int mCurrentEditorMode = -1;

    private ViewOperator mViewOperator;

    private RelativeLayout mRootView;
    private FrameLayout mPreviewLayout;
    private GLSurfaceView mPreviewView;
    private ImageView mPlayControlIv;
    // 贴纸父控件
    private StickerViewGroup mStickerViewGroup;
    private TextView mAudioMixVolumeSettingBtn;
    private HorizontalScrollView mEditBtns;
    private CustomProgressDialog mProcessingDialog;
    private FrameLayout mTitleBar;

    private PaintBottomView mPaintBottomView;
    private AdvancedFilterBottomView mAdvancedFilterBottomView;
    private SpecialEffectBottomView mSpecialEffectBottomView;
    private GifBottomView mGifBottomView;

    private PLShortVideoEditorStatus mShortVideoEditorStatus = PLShortVideoEditorStatus.IDLE;
    private PLShortVideoEditor mShortVideoEditor;

    private VolumeSettingBottomView mVolumeSettingBottomView;
    private boolean mIsVolumeSetting;

    private String mMp4path;
    private long mMixDuration = 5000; // ms
    private boolean isSaving = false;
    private boolean mIsPlaying = true;
    private volatile boolean mCancelSave;
    private long mVideoDurationMs;

    /**
     * 多重混音相关
     */
    private MusicSelectBottomView mMusicSelectBottomView;
    private PLMixAudioFile mMainMixAudioFile;
    private Map<AudioFile, PLMixAudioFile> mMixAudioFileMap;
    private boolean mIsMixAudio = false;
    private boolean mMainAudioFileAdded;
    private int mMixPosition;

    private PLMediaFile mMediaFile;

    private PLPaintView mPaintView;
    private ViewGroup.MarginLayoutParams mPaintViewLayoutParams;

    private LoadFrameTask mLoadFramesTask;
    private List<Bitmap> mVideoFrames;

    private Point mScreenSize;

    /**
     * 滤镜相关
     */
    private ListBottomView mFilterBottomView;
    private boolean mIsSelectingFilter;

    /**
     * MV 相关
     */
    private ListBottomView mMvBottomView;
    private boolean mIsOperatingMv;

    /**
     * Gif 动图相关
     */
    private boolean mIsGifEditing = false;
    private Map<StickerImageView, PLGifWatermarkSetting> mStickerImageViews;
    private StickerImageView mCurrentSticker;

    private TextBottomView mTextBottomView;
    private boolean mIsTextEffectEditing = false;
    private StickerTextView mCurTextView;
    private List<StickerTextView> mStickerTextViews;

    /**
     * 涂图 SDK 处理
     */
    private TuSDKManager mTuSDKManager;
    private boolean mSceneMagicEditing = false;
    private TuSdkMediaSceneEffectData mMediaSceneEffectData;
    private TimerTask mScrollTimerTask;
    private Timer mScrollTimer;

    public static void start(Activity activity, String mp4Path) {
        Intent intent = new Intent(activity, VideoEditActivity.class);
        intent.putExtra(MP4_PATH, mp4Path);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_edit);

        mScreenSize = Utils.getScreenSize(this);

        mRootView = findViewById(R.id.editor_layout);
        mPreviewLayout = findViewById(R.id.preview_layout);
        mPreviewView = findViewById(R.id.preview);
        mPlayControlIv = findViewById(R.id.play_control_iv);
        mAudioMixVolumeSettingBtn = findViewById(R.id.volume_btn);
        mEditBtns = findViewById(R.id.edit_bottom_view);
        mTitleBar = findViewById(R.id.title_bar);

        mRootView.setOnClickListener(this);

        mStickerViewGroup = findViewById(R.id.sticker_container_view);
        mStickerViewGroup.setOnItemClickListener(this);

        mViewOperator = new ViewOperator(mRootView, mTitleBar, mEditBtns, mPreviewLayout);

        initShortVideoEditor();
        initGlSurfaceView();
        initGifResources();
        initProcessingDialog();
        initTuSDK();

        mVideoFrames = new ArrayList<>();
        loadVideoThumbnails(mMediaFile, 20, 50, 50);
    }

    @Override
    protected void onResume() {
        super.onResume();
        startTimerTask();
        updatePlayStatus(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        updatePlayStatus(false);
        cancelTimerTask();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mLoadFramesTask != null && mLoadFramesTask.getStatus() == AsyncTask.Status.RUNNING) {
            mLoadFramesTask.cancel(true);
            mLoadFramesTask = null;
        }
        mTuSDKManager.reset();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == CHOOSE_MUSIC_REQUEST_CODE && data != null) {
            AudioFile audioFile = (AudioFile) data.getSerializableExtra(ChooseMusicActivity.SELECTED_MUSIC_FILE);
            long startTime = data.getLongExtra(ChooseMusicActivity.START_TIME, 0);
            long endTime = data.getLongExtra(ChooseMusicActivity.END_TIME, mVideoDurationMs);
            if (audioFile != null) {
                try {
                    if (!mMainAudioFileAdded) {
                        mMainMixAudioFile = new PLMixAudioFile(mMp4path);
                        mShortVideoEditor.addMixAudioFile(mMainMixAudioFile);
                        mMainAudioFileAdded = true;
                    }

                    PLMixAudioFile mixAudioFile = new PLMixAudioFile(audioFile.getFilePath());
                    mixAudioFile.setOffsetInVideo(mMixPosition * 1000);
                    mixAudioFile.setStartTime(startTime * 1000);
                    long durationMs = (endTime - startTime) > (mVideoDurationMs - mMixPosition) ? (mVideoDurationMs - mMixPosition) : (endTime - startTime);
                    mixAudioFile.setDurationInVideo(durationMs * 1000);
                    mShortVideoEditor.addMixAudioFile(mixAudioFile);
                    if (mMixAudioFileMap == null) {
                        mMixAudioFileMap = new HashMap<>();
                    }
                    mMixAudioFileMap.put(audioFile, mixAudioFile);
                    mMusicSelectBottomView.addAudioFile(audioFile);
                    mMusicSelectBottomView.addMusicBar(mMixPosition, durationMs);
                    mShortVideoEditor.seekTo((int) startTime);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void onClickBack(View v) {
        finish();
    }

    public void onClickSaveEdit(View v) {
        updatePlayStatus(false);
        cancelTimerTask();

        if (mLoadFramesTask.getStatus() == AsyncTask.Status.RUNNING) {
            mLoadFramesTask.cancel(true);
        }
        isSaving = true;
        mProcessingDialog.show();
        mShortVideoEditor.save(mVideoSaveFilterListener);
    }

    public void onClickVolumeSetting(View v) {
        if (mVolumeSettingBottomView == null) {
            mVolumeSettingBottomView = new VolumeSettingBottomView(this);
            mVolumeSettingBottomView.setOnAudioVolumeChangedListener(new VolumeSettingBottomView.OnAudioVolumeChangedListener() {
                @Override
                public void onAudioVolumeChanged(float srcVolume, float musicVolume) {
                    if (!mMainAudioFileAdded) {
                        // 未添加多重混音，调节音量
                        mShortVideoEditor.setAudioMixVolume(srcVolume, 1.0f);
                    } else {
                        // 添加多重混音，调节音量
                        mMainMixAudioFile.setVolume(srcVolume);
                        if (mMixAudioFileMap != null) {
                            Iterator<PLMixAudioFile> it = mMixAudioFileMap.values().iterator();
                            while (it.hasNext()) {
                                PLMixAudioFile audioFile = it.next();
                                audioFile.setVolume(musicVolume);
                            }
                        }
                    }
                }
            });
        }
        mVolumeSettingBottomView.setMusicVolumeSettingEnabled(mMixAudioFileMap != null && mMixAudioFileMap.size() > 0);
        mViewOperator.showBottomView(mVolumeSettingBottomView);
        mIsVolumeSetting = true;
    }

    public void onClickFilterSelect(View v) {
        if (mFilterBottomView == null) {
            mFilterBottomView = new ListBottomView(this);
            FilterItemAdapter filterItemAdapter = new FilterItemAdapter(this,
                    new ArrayList<PLBuiltinFilter>(Arrays.asList(mShortVideoEditor.getBuiltinFilterList())),
                    new ArrayList<String>(Arrays.asList(getResources().getStringArray(R.array.built_in_filters))));
            filterItemAdapter.setOnFilterSelectListener(new FilterItemAdapter.OnFilterSelectListener() {
                @Override
                public void onFilterSelected(String filterName, String description) {
                    mShortVideoEditor.setBuiltinFilter(filterName);
                }
            });
            mFilterBottomView.init(filterItemAdapter);
        }
        mViewOperator.showBottomView(mFilterBottomView);
        mIsSelectingFilter = true;
    }

    public void onClickChooseMusic(View v) {
        if (mMusicSelectBottomView == null) {
            if (mMixAudioFileMap == null) {
                mMixAudioFileMap = new HashMap<>();
            }
            mMusicSelectBottomView = new MusicSelectBottomView(this, mMediaFile, new ArrayList(mMixAudioFileMap.values()));
            mMusicSelectBottomView.setBitmapList(mVideoFrames);
            mMusicSelectBottomView.setOnMusicSelectOperationListener(new MusicSelectBottomView.OnMusicSelectOperationListener() {
                @Override
                public void onMusicAddClicked() {
                    mMixPosition = mShortVideoEditor.getCurrentPosition();
                    Intent intent = new Intent(VideoEditActivity.this, ChooseMusicActivity.class);
                    intent.putExtra("videoDurationMs", mMediaFile.getDurationMs());
                    startActivityForResult(intent, CHOOSE_MUSIC_REQUEST_CODE);
                }

                @Override
                public void onMusicRemoveClicked(AudioFile audioFile) {
                    mShortVideoEditor.removeMixAudioFile(mMixAudioFileMap.remove(audioFile));
                    if (mMixAudioFileMap.isEmpty()) {
                        mShortVideoEditor.removeMixAudioFile(mMainMixAudioFile);
                        mMainMixAudioFile = null;
                        mMainAudioFileAdded = false;
                    }
                }

                @Override
                public void onMusicMixPositionChanged(int position) {
                    updatePlayStatus(false);
                    mShortVideoEditor.seekTo(position);
                }

                @Override
                public void onPlayStatusPaused() {
                    updatePlayStatus(false);
                }

                @Override
                public void onConfirmClicked() {
                    mViewOperator.hideBottomView();
                    mIsMixAudio = false;
                }
            });
        }
        mViewOperator.showBottomView(mMusicSelectBottomView);
        mIsMixAudio = true;
    }

    public void onClickPaint(View v) {
        if (mPaintView == null) {
            mPaintView = new PLPaintView(this, mPreviewView.getWidth(), mPreviewView.getHeight());
            mPaintView.setLayoutParams(mPaintViewLayoutParams);
            mShortVideoEditor.addPaintView(mPaintView);
        }
        if (mPaintBottomView == null) {
            mPaintBottomView = new PaintBottomView(this);
            mPaintBottomView.setOnPaintSelectorListener(this);
        }
        mPaintView.setPaintEnable(true);
        mViewOperator.showBottomView(mPaintBottomView);
    }

    public void onClickAdvancedFilter(View v) {
        if (mAdvancedFilterBottomView == null) {
            mAdvancedFilterBottomView = new AdvancedFilterBottomView(this);
            mAdvancedFilterBottomView.setOnFilterClickedListener(new AdvancedFilterBottomView.OnFilterClickedListener() {
                @Override
                public void onFilterClicked(int position, String filterCode) {
                    // 移除不可叠加的特效
                    mTuSDKManager.getPreviewFilterEngine().removeMediaEffectsWithType(TuSdkMediaEffectData.TuSdkMediaEffectDataType.TuSdkMediaEffectDataTypeFilter);

                    if ("None".equals(filterCode)) {
                        mTuSDKManager.getPreviewFilterEngine().removeMediaEffectsWithType(TuSdkMediaEffectData.TuSdkMediaEffectDataType.TuSdkMediaEffectDataTypeFilter);
                        mAdvancedFilterBottomView.getFilterConfigView().setVisibility(View.GONE);
                    } else {
                        if (filterCode != null && !filterCode.isEmpty()) {
                            TuSdkMediaFilterEffectData mediaFilterEffectData = mTuSDKManager.createFilterEffectData(filterCode);
                            mTuSDKManager.addMagicModel(mediaFilterEffectData);
                            mAdvancedFilterBottomView.getFilterConfigView().setVisibility(View.VISIBLE);
                        }
                    }
                }

                @Override
                public void onFilterConfirmClicked() {
                    mTuSDKManager.saveState();
                    mViewOperator.hideBottomView();
                }
            });
        }
        mViewOperator.showBottomView(mAdvancedFilterBottomView);
    }

    public void onClickEffect(View v) {
        if (mSpecialEffectBottomView == null) {
            mSpecialEffectBottomView = new SpecialEffectBottomView(this, mMediaFile.getDurationMs() * 1000);
            mSpecialEffectBottomView.setBitmapList(mVideoFrames);
            mSpecialEffectBottomView.setOnEffectClickedListener(new SpecialEffectBottomView.OnEffectClickedListener() {
                @Override
                public void onPressSceneEffect(int position, String sceneCode, long startTimeUs, SceneRecyclerAdapter.SceneViewHolder sceneViewHolder) {
                    if (mShortVideoEditor.getCurrentPosition() + 100 >= mVideoDurationMs) {
                        mShortVideoEditor.seekTo(0);
                        updatePlayStatus(false);
                    }

                    if (mShortVideoEditorStatus == PLShortVideoEditorStatus.PLAYING) {
                        updatePlayStatus(false);
                        return;
                    }

                    if (sceneCode != null && !sceneCode.isEmpty()) {
                        mMediaSceneEffectData = mTuSDKManager.createSceneEffectData(sceneCode, startTimeUs);
                        mTuSDKManager.addMagicModel(mMediaSceneEffectData);
                    }
                    updatePlayStatus(true);
                    mSpecialEffectBottomView.changePlayLineViewColor(sceneCode);
                    sceneViewHolder.mSelectLayout.setImageResource(TuSdkContext.getColorResId("lsq_scence_effect_color_" + sceneCode));
                    mSpecialEffectBottomView.setEffectDeleteEnable(true);
                }

                @Override
                public void onEffectItemReleased(SceneRecyclerAdapter.SceneViewHolder sceneViewHolder) {
                    if (mMediaSceneEffectData != null) {
                        updatePlayStatus(false);
                        long endPosition = mShortVideoEditor.getCurrentPosition();
                        mMediaSceneEffectData.getAtTimeRange().setEndTimeUs(endPosition * 1000L);
                        sceneViewHolder.mSelectLayout.setImageResource(TuSdkContext.getColorResId("lsq_color_transparent"));
                    }
                }

                @Override
                public void onEffectConfirmClicked() {
                    mViewOperator.hideBottomView();
                    mSceneMagicEditing = false;
                    mShortVideoEditor.seekTo(0);
                    updatePlayStatus(true);
                    mTuSDKManager.saveState();
                    mSceneMagicEditing = false;
                }

                @Override
                public void onEffectDeleteClicked() {
                    // 获取最后一个特效对象
                    TuSdkMediaEffectData mediaEffectData = mTuSDKManager.getLastMagicModel();
                    // 删除特效对象
                    mTuSDKManager.removeMagicModel(mediaEffectData);
                    // 更新播放器以及 PlayLineView
                    mShortVideoEditor.seekTo((int) mediaEffectData.getAtTimeRange().getStartTimeUS() / 1000);
                    mSpecialEffectBottomView.deleteLastLineView(mediaEffectData.getAtTimeRange().getStartTimeUS());
                    // 如果仅添加了一个特效，那么删除后不允许继续删除
                    if (mTuSDKManager.getAllMediaEffects().isEmpty()) {
                        mSpecialEffectBottomView.setEffectDeleteEnable(false);
                    }
                }

                @Override
                public void onLineViewProgressChanged(float progress, boolean isTouching) {
                    if (!isTouching) {
                        return;
                    } else {
                        updatePlayStatus(false);
                    }
                    mShortVideoEditor.seekTo((int) (mMediaFile.getDurationMs() * progress));
                }
            });
        }
        mShortVideoEditor.seekTo(0);
        updatePlayStatus(false);
        mViewOperator.showBottomView(mSpecialEffectBottomView);
        mSceneMagicEditing = true;
    }

    public void onClickGifStickers(View v) {
        if (mGifBottomView == null) {
            mGifBottomView = new GifBottomView(this, mMediaFile);
            mGifBottomView.setBitmapList(mVideoFrames);
            mGifBottomView.setOnViewOperateListener(new StickerBottomView.OnViewOperateListener() {
                @Override
                public void onConfirmClicked() {
                    mViewOperator.hideBottomView();
                    mIsGifEditing = false;
                    mShortVideoEditor.seekTo(0);
                    mCurrentEditorMode = -1;
                    setGifStickersClickable(false);
                    if (mStickerImageViews != null) {
                        for (Map.Entry<StickerImageView, PLGifWatermarkSetting> entry : mStickerImageViews.entrySet()) {
                            StickerImageView stickerImageView = entry.getKey();
                            stickerImageView.setEditable(false);
                            PLGifWatermarkSetting gifWatermarkSetting = entry.getValue();
                            gifWatermarkSetting.setDisplayPeriod(stickerImageView.getStartTime(), stickerImageView.getEndTime() - stickerImageView.getStartTime());
                            gifWatermarkSetting.setPosition((float) stickerImageView.getViewX() / mStickerViewGroup.getWidth(), (float) stickerImageView.getViewY() / mStickerViewGroup.getHeight());
                            gifWatermarkSetting.setRotation((int) stickerImageView.getImageDegree());
                            gifWatermarkSetting.setAlpha(255);
                            gifWatermarkSetting.setSize(stickerImageView.getImageWidth() * stickerImageView.getImageScale() / mStickerViewGroup.getWidth(),
                                    stickerImageView.getImageHeight() * stickerImageView.getImageScale() / mStickerViewGroup.getHeight());
                            mShortVideoEditor.updateGifWatermark(gifWatermarkSetting);
                        }
                    }
                }

                @Override
                public void onPlayStatusPaused() {
                    updatePlayStatus(false);
                }

                @Override
                public void onPlayProgressChanged(int position) {
                    mShortVideoEditor.seekTo(position);
                }
            });
            mGifBottomView.setOnGifItemClickListener(new GifBottomView.OnGifItemClickListener() {
                @Override
                public void onGifItemClicked(StickerImageView stickerImageView) {
                    if (mStickerImageViews == null) {
                        mStickerImageViews = new HashMap<>();
                    }

                    mStickerViewGroup.addOperationView(stickerImageView);
                    mStickerViewGroup.setVisibility(View.VISIBLE);
                    PLGifWatermarkSetting gifWatermarkSetting = getGifSettingFromSticker(stickerImageView);
                    mStickerImageViews.put(stickerImageView, gifWatermarkSetting);
                    mShortVideoEditor.addGifWatermark(gifWatermarkSetting);
                    mCurrentSticker = stickerImageView;
                }

                @Override
                public void onGifItemDeleted(StickerImageView stickerImageView) {
                    int index = mStickerViewGroup.getSelectedViewIndex();
                    mStickerViewGroup.removeOperationView(mStickerViewGroup.getOperationView(index));
                    mShortVideoEditor.removeGifWatermark(mStickerImageViews.remove(stickerImageView));
                }
            });
        }
        mViewOperator.showBottomView(mGifBottomView);
        mIsGifEditing = true;
        mShortVideoEditor.seekTo(0);
        mCurrentEditorMode = GIF_STICKER;
        // 在编辑界面下可以点击贴纸
        setGifStickersClickable(true);
    }

    public void onClickTextSticker(View v) {
        if (mTextBottomView == null) {
            mTextBottomView = new TextBottomView(this, mMediaFile);
            mTextBottomView.setBitmapList(mVideoFrames);
            mTextBottomView.setOnViewOperateListener(new StickerBottomView.OnViewOperateListener() {
                @Override
                public void onConfirmClicked() {
                    mViewOperator.hideBottomView();
                    if (mCurTextView != null && mCurTextView.isEditable()) {
                        mCurTextView.setEditable(false);
                    }
                    mIsTextEffectEditing = false;
                    setTextStickersSelectable(false);
                }

                @Override
                public void onPlayStatusPaused() {
                    updatePlayStatus(false);
                }

                @Override
                public void onPlayProgressChanged(int position) {
                    mShortVideoEditor.seekTo(position);
                }
            });
            mTextBottomView.setOnTextSelectorListener(new TextBottomView.OnTextSelectorListener() {
                @Override
                public void onTextAdded(StickerTextView textView, long startTimeMs, long durationMs) {
                    if (mStickerTextViews == null) {
                        mStickerTextViews = new ArrayList<>();
                    }
                    if (mCurTextView != null && mCurTextView.isEditable()) {
                        mCurTextView.setEditable(false);
                    }
                    mCurTextView = textView;
                    mStickerTextViews.add(textView);
                    mShortVideoEditor.addTextView(textView);
                    mShortVideoEditor.setViewTimeline(textView, startTimeMs, durationMs);
                }

                @Override
                public void onTextSelected(StickerTextView textView) {
                    if (mCurTextView != null && mCurTextView.isEditable()) {
                        mCurTextView.setEditable(false);
                    }
                    mCurTextView = textView;
                    mCurTextView.setEditable(true);
                }

                @Override
                public boolean onTextEdited() {
                    createTextDialog(mCurTextView);
                    return true;
                }

                @Override
                public boolean onTextDeleted() {
                    mShortVideoEditor.removeTextView(mCurTextView);
                    mStickerTextViews.remove(mCurTextView);
                    if (mCurTextView != null) {
                        mCurTextView = null;
                    }
                    return true;
                }

                @Override
                public void onTextRangeChanged(StickerTextView textView, long startTimeMs, long durationMs) {
                    mShortVideoEditor.setViewTimeline(mCurTextView, startTimeMs, durationMs);
                }
            });
        }
        mViewOperator.showBottomView(mTextBottomView);
        mIsTextEffectEditing = true;
        mShortVideoEditor.seekTo(0);
        setTextStickersSelectable(true);
    }

    public void onClickMv(View v) {
        if (mMvBottomView == null) {
            try {
                File dir = new File(Config.VIDEO_STORAGE_DIR + "mvs");
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

                mMvBottomView = new ListBottomView(this);
                MVItemAdapter mvItemAdapter = new MVItemAdapter(json.getJSONArray("MVs"));
                mvItemAdapter.setOnMvSelectListener(new MVItemAdapter.OnMvSelectListener() {
                    @Override
                    public void onMvSelected(String mvFilePath, String maskFilePath) {
                        mShortVideoEditor.setMVEffect(mvFilePath, maskFilePath);
                    }
                });
                mMvBottomView.init(mvItemAdapter);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        mViewOperator.showBottomView(mMvBottomView);
        mIsOperatingMv = true;
    }

    private void initShortVideoEditor() {
        mMp4path = getIntent().getStringExtra(MP4_PATH);
        Log.i(TAG, "editing file: " + mMp4path);
        mMediaFile = new PLMediaFile(mMp4path);
        mVideoDurationMs = mMediaFile.getDurationMs();

        PLVideoEditSetting setting = new PLVideoEditSetting();
        setting.setSourceFilepath(mMp4path);
        setting.setDestFilepath(Config.EDITED_FILE_PATH);
        setting.setGifPreviewEnabled(false);

        mShortVideoEditor = new PLShortVideoEditor(mPreviewView);
        mShortVideoEditor.setVideoEditSetting(setting);
        mShortVideoEditor.setVideoSaveListener(this);

        mMixDuration = mShortVideoEditor.getDurationMs();
    }

    private void initGlSurfaceView() {
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) mPreviewLayout.getLayoutParams();
        FrameLayout.LayoutParams surfaceLayout = (FrameLayout.LayoutParams) mPreviewView.getLayoutParams();
        int outputWidth = mMediaFile.getVideoWidth();
        int outputHeight = mMediaFile.getVideoHeight();
        int rotation = mMediaFile.getVideoRotation();
        if ((rotation == 90 || rotation == 270)) {
            int temp = outputWidth;
            outputWidth = outputHeight;
            outputHeight = temp;
        }

        surfaceLayout.width = mScreenSize.x;
        surfaceLayout.height = Math.round((float)outputHeight * mScreenSize.x / outputWidth);
        ViewGroup.MarginLayoutParams marginParams = null;
        if (layoutParams instanceof ViewGroup.MarginLayoutParams) {
            marginParams = (ViewGroup.MarginLayoutParams)surfaceLayout;
        } else {
            marginParams = new ViewGroup.MarginLayoutParams(surfaceLayout);
        }
        mPaintViewLayoutParams = marginParams;
        mPreviewLayout.setLayoutParams(layoutParams);
        mStickerViewGroup.setLayoutParams(marginParams);
        mPreviewView.setLayoutParams(marginParams);
    }

    private void initGifResources() {
        try {
            File dir = new File(Config.GIF_STICKER_DIR);
            // copy mv assets to sdcard
            if (!dir.exists()) {
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
            }
        } catch (IOException e) {
            e.printStackTrace();
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

    /**
     * 在播放过程中获取视频时间戳并以此进行贴图播放的控制
     */
    private void startTimerTask() {
        if (mScrollTimerTask == null) {
            mScrollTimerTask = new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (mShortVideoEditorStatus != PLShortVideoEditorStatus.PLAYING) {
                                return;
                            }
                            int position = mShortVideoEditor.getCurrentPosition();
                            if (mStickerImageViews != null){
                                for (StickerImageView stickerImageView : mStickerImageViews.keySet()) {
                                    if (position >= stickerImageView.getStartTime()
                                            && position <= stickerImageView.getEndTime()
                                            && !stickerImageView.isRunning()) {
                                        stickerImageView.startGifPlaying();
                                        stickerImageView.setVisibility(View.VISIBLE);
                                    } else if ((position < stickerImageView.getStartTime() || position > stickerImageView.getEndTime())
                                            && stickerImageView.isRunning()) {
                                        stickerImageView.stopGifPlaying();
                                        stickerImageView.setVisibility(View.GONE);
                                    }
                                }
                            }
                            if (mSceneMagicEditing) {
                                mSpecialEffectBottomView.moveLineViewToPercent(position / (float) mVideoDurationMs);
                            }
                            if (mIsGifEditing) {
                                mGifBottomView.moveThumbLineViewToPosition(position);
                            }
                            if (mIsTextEffectEditing) {
                                mTextBottomView.moveThumbLineViewToPosition(position);
                            }
                            if (mIsMixAudio) {
                                mMusicSelectBottomView.moveThumbLineViewToPosition(position);
                            }
                        }
                    });
                }
            };
            mScrollTimer = new Timer();
        }
        // scroll fps:20
        mScrollTimer.schedule(mScrollTimerTask, 0, 50);
    }

    private void cancelTimerTask() {
        mScrollTimer.cancel();
        mScrollTimerTask = null;
    }

    /**
     * 开始预览
     */
    private void startPlayback() {
        if (mShortVideoEditorStatus == PLShortVideoEditorStatus.IDLE) {
            mShortVideoEditor.startPlayback(mVideoPlayFilterListener);
            mShortVideoEditorStatus = PLShortVideoEditorStatus.PLAYING;
        } else if (mShortVideoEditorStatus == PLShortVideoEditorStatus.PAUSED) {
            mShortVideoEditor.resumePlayback();
            mShortVideoEditorStatus = PLShortVideoEditorStatus.PLAYING;
        }
        mShortVideoEditorStatus = PLShortVideoEditorStatus.PLAYING;
    }

    /**
     * 停止预览
     */
    private void stopPlayback() {
        mShortVideoEditor.stopPlayback();
        mShortVideoEditorStatus = PLShortVideoEditorStatus.IDLE;
    }

    /**
     * 暂停预览
     */
    private void pausePlayback() {
        mShortVideoEditor.pausePlayback();
        mShortVideoEditorStatus = PLShortVideoEditorStatus.PAUSED;
    }

    /**
     * 加载视频帧数据
     *
     * @param mediaFile 视频媒体文件
     * @param frameCount 帧数
     * @param frameWidth 缩略图的宽度
     * @param frameHeight 缩略图的高度
     */
    private void loadVideoThumbnails(final PLMediaFile mediaFile, int frameCount, int frameWidth, int frameHeight) {
        if (mLoadFramesTask == null && mVideoFrames.isEmpty()) {
            mLoadFramesTask = new LoadFrameTask(this, mediaFile, frameCount, frameWidth, frameHeight, new LoadFrameTask.OnLoadFrameListener() {
                @Override
                public void onFrameReady(Bitmap bitmap) {
                    if (bitmap != null) {
                        mVideoFrames.add(bitmap);
                        if (mSceneMagicEditing) {
                            mSpecialEffectBottomView.addCoverBitmap(bitmap);
                        }
                        if (mIsGifEditing) {
                            mGifBottomView.addBitmap(bitmap);
                        }
                        if (mIsTextEffectEditing) {
                            mTextBottomView.addBitmap(bitmap);
                        }
                        if (mIsMixAudio) {
                            mMusicSelectBottomView.addBitmap(bitmap);
                        }
                    }
                }
            });
            mLoadFramesTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    private PLGifWatermarkSetting getGifSettingFromSticker(StickerImageView stickerImageView) {
        PLGifWatermarkSetting gifWatermarkSetting = new PLGifWatermarkSetting();
        gifWatermarkSetting.setFilePath(stickerImageView.getFilePath());
        gifWatermarkSetting.setDisplayPeriod(stickerImageView.getStartTime(), stickerImageView.getEndTime() - stickerImageView.getStartTime());
        gifWatermarkSetting.setPosition((float) stickerImageView.getViewX() / mStickerViewGroup.getWidth(), (float) stickerImageView.getViewY() / mStickerViewGroup.getHeight());
        gifWatermarkSetting.setRotation((int) stickerImageView.getImageDegree());
        gifWatermarkSetting.setAlpha(255);
        gifWatermarkSetting.setSize(stickerImageView.getImageWidth() * stickerImageView.getImageScale() / mStickerViewGroup.getWidth(),
                stickerImageView.getImageHeight() * stickerImageView.getImageScale() / mStickerViewGroup.getHeight());
        return gifWatermarkSetting;
    }

    private void setGifStickersClickable(boolean canClick) {
        if (mStickerImageViews == null || mStickerImageViews.isEmpty()) {
            return;
        }
        for (StickerImageView stickerImageView : mStickerImageViews.keySet()) {
            stickerImageView.setClickable(canClick);
        }
    }

    private void setTextStickersSelectable(boolean selectable) {
        if (mStickerTextViews == null || mStickerTextViews.isEmpty()) {
            return;
        }
        for (StickerTextView stickerTextView : mStickerTextViews) {
            stickerTextView.setTextIsSelectable(selectable);
        }
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
                ((StickerTextView) textView).setText(edit.getText().toString());
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

    private void updatePlayStatus(boolean isPlaying) {
        if (isPlaying) {
            startPlayback();
            mPlayControlIv.setVisibility(View.GONE);
        } else {
            pausePlayback();
            mPlayControlIv.setVisibility(View.VISIBLE);
        }
        mIsPlaying = isPlaying;
    }

    @Override
    public void onClick(View v) {
        if (mIsGifEditing) {
            if (mCurrentSticker != null) {
                mCurrentSticker.setEditable(false);
            }
            mGifBottomView.startPlayer();
        }
        if (mIsTextEffectEditing) {
            if (mCurTextView != null) {
                mCurTextView.setEditable(false);
            }
            mTextBottomView.startPlayer();
        }
        if (mIsOperatingMv) {
            mViewOperator.hideBottomView();
            mIsOperatingMv = false;
            return;
        }
        if (mIsSelectingFilter) {
            mViewOperator.hideBottomView();
            mIsSelectingFilter = false;
            return;
        }
        if (mIsVolumeSetting) {
            mViewOperator.hideBottomView();
            mIsVolumeSetting = false;
            return;
        }
        if (mIsMixAudio && !mIsPlaying) {
            mMusicSelectBottomView.startPlayer();
        }
        updatePlayStatus(!mIsPlaying);
    }

    @Override
    public void onSaveVideoSuccess(String filePath) {
        Log.i(TAG, "save edit success filePath: " + filePath);
        mProcessingDialog.dismiss();
        PlaybackActivity.start(VideoEditActivity.this, filePath);
    }

    @Override
    public void onSaveVideoFailed(final int errorCode) {
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
        mCancelSave = true;
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

    @Override
    public void onViewClosed() {
        mPaintView.setPaintEnable(false);
        mViewOperator.hideBottomView();
    }

    @Override
    public void onStickerItemClicked(StickerImageView view, int lastSelectedPos, int currentSelectedPos) {
        // GIF 贴纸和文字贴纸一样
        if (mIsGifEditing) {
            mCurrentSticker = view;
            mGifBottomView.selectSticker(view);
        }
    }

    // ------    涂图特效相关    ------ //

    private void initTuSDK() {
        mTuSDKManager = new TuSDKManager(getBaseContext());
    }

    /**
     * 预览时为视频添加场景特效
     */
    private PLVideoFilterListener mVideoPlayFilterListener = new PLVideoFilterListener() {
        @Override
        public void onSurfaceCreated() {
            mTuSDKManager.setupPreviewFilterEngine();
            mTuSDKManager.getPreviewFilterEngine().setMediaEffectDelegate(mMediaEffectDelegate);
            mTuSDKManager.getPreviewFilterEngine().onSurfaceCreated();
            mTuSDKManager.resumeState();
        }

        @Override
        public void onSurfaceChanged(int width, int height) {
            if (mTuSDKManager.getPreviewFilterEngine() != null) {
                mTuSDKManager.getPreviewFilterEngine().onSurfaceChanged(width, height);
            }
        }

        @Override
        public void onSurfaceDestroy() {
            synchronized (VideoEditActivity.this) {
                mTuSDKManager.destroyPreviewFilterEngine();
            }
        }

        @Override
        public int onDrawFrame(int texId, int texWidth, int texHeight, long timestampNs, float[] transformMatrix) {
            if (mCancelSave && mTuSDKManager.getPreviewFilterEngine() == null) {
                mTuSDKManager.setupPreviewFilterEngine();
                mTuSDKManager.getPreviewFilterEngine().onSurfaceCreated();
                mCancelSave = false;
                updatePlayStatus(false);
            }

            synchronized (VideoEditActivity.this) {
                if (mTuSDKManager.getPreviewFilterEngine() != null && !isSaving && mShortVideoEditorStatus == PLShortVideoEditorStatus.PLAYING) {
                    return mTuSDKManager.getPreviewFilterEngine().processFrame(texId, texWidth, texHeight,  mShortVideoEditor.getCurrentPosition() * 1000000L);
                }
            }
            return texId;
        }
    };

    /**
     * 保存时为视频添加场景特效
     */
    private PLVideoFilterListener mVideoSaveFilterListener = new PLVideoFilterListener() {

        @Override
        public void onSurfaceCreated() {
            mTuSDKManager.setupSaveFilterEngine();
            mTuSDKManager.getSaveFilterEngine().onSurfaceCreated();
            mTuSDKManager.setMagicModelsForSaving();
        }

        @Override
        public void onSurfaceChanged(int width, int height) {
            if (mTuSDKManager.getSaveFilterEngine() != null) {
                mTuSDKManager.getSaveFilterEngine().onSurfaceChanged(width, height);
            }
        }

        @Override
        public void onSurfaceDestroy() {
            mTuSDKManager.destroySaveFilterEngine();
            isSaving = false;
        }

        @Override
        public int onDrawFrame(int texId, int texWidth, int texHeight, long timestampNs, float[] transformMatrix) {
            int outTexId = texId;
            if (mTuSDKManager.getSaveFilterEngine() != null) {
                 outTexId = mTuSDKManager.getSaveFilterEngine().processFrame(texId, texWidth, texHeight, timestampNs);
                 if (outTexId == 0) {
                     outTexId = texId;
                 }
            }
            return outTexId;
        }
    };

    /**
     * 涂图特效相关代理
     */
    private TuSDKVideoProcesser.TuSDKVideoProcessorMediaEffectDelegate mMediaEffectDelegate = new TuSDKVideoProcesser.TuSDKVideoProcessorMediaEffectDelegate() {
        /**
         * 当前被应用的特效
         *
         * @param mediaEffectData 特效数据
         */
        @Override
        public void didApplyingMediaEffect(final TuSdkMediaEffectData mediaEffectData) {
            ThreadHelper.post(new Runnable() {
                @Override
                public void run() {
                    switch (mediaEffectData.getMediaEffectType()) {
                        case TuSdkMediaEffectDataTypeFilter:
                            mAdvancedFilterBottomView.getFilterConfigView().setFilterArgs(mediaEffectData, mediaEffectData.getFilterArgs());
                            break;
                        default:
                            break;
                    }
                }
            });
        }

        @Override
        public void didRemoveMediaEffect(List<TuSdkMediaEffectData> list) {

        }
    };
}
