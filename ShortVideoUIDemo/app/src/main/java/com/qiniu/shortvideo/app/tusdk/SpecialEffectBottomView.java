package com.qiniu.shortvideo.app.tusdk;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.qiniu.shortvideo.app.R;
import com.qiniu.shortvideo.app.tusdk.playview.TuSdkMovieScrollView;
import com.qiniu.shortvideo.app.view.BaseBottomView;

import org.lasque.tusdk.core.TuSdkContext;

import java.util.Arrays;
import java.util.List;

public class SpecialEffectBottomView extends BaseBottomView {
    /**
     * 判断为按压的最短触摸时间
     **/
    private static final int MIN_PRESS_DURATION_MILLIS = 300;
    /**
     * 场景特效列表
     **/
    private RecyclerView mSceneRecyclerView;
    private ImageView mDeleteEffectBtn;
    /**
     * 场景特效列表适配器
     **/
    private SceneRecyclerAdapter mSceneAdapter;
    private Handler mHandler = new Handler();
    /**
     * 视频封面Bitmap列表
     **/
    private List<Bitmap> mBitmapList;
    /**
     * 当前正在应用的场景特效Code
     **/
    public volatile String mSceneCode;
    /**
     * 当前应用场景特效的开始时间
     **/
    public long mStartTimeUs;
    /**
     * 当前视频的时长
     **/
    private long mVideoDurationUs;
    /**
     * 播放控件，主界面播放进度改变时，要更新改控件；手动拖动该控件是，要通知主界面进行相应播放器操作
     **/
    private TuSdkMovieScrollPlayLineView mLineView;

    private ImageButton mConfirmBtn;

    private float prePercent = 0;
    private boolean mIsVisible = false;
    private boolean mIsPlaying;

    private long mActionDownTime;

    private OnEffectClickedListener mOnEffectClickedListener;

    public interface OnEffectClickedListener {
        // 当按住并选择特效的时候触发
        void onPressSceneEffect(int position, String sceneCode, long startTimeUs, SceneRecyclerAdapter.SceneViewHolder sceneViewHolder);
        // 当松开特效选择的时候触发
        void onEffectItemReleased(SceneRecyclerAdapter.SceneViewHolder sceneViewHolder);
        // 点击确认按钮的时候触发
        void onEffectConfirmClicked();
        // 删除上一个特效的时候触发
        void onEffectDeleteClicked();
        // 当缩略图进度改变的时候触发
        void onLineViewProgressChanged(float progress, boolean isTouching);
    }

    public SpecialEffectBottomView(@NonNull Context context) {
        super(context);
        init();
    }

    public SpecialEffectBottomView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SpecialEffectBottomView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public SpecialEffectBottomView(Context context, long videoDurationUs) {
        super(context);
        init();
        mVideoDurationUs = videoDurationUs;
    }

    public void setBitmapList(List<Bitmap> bitmaps) {
        mBitmapList = bitmaps;
        if (mBitmapList != null) {
            for (Bitmap bp : mBitmapList)
                mLineView.addBitmap(bp);
        }
    }

    public void addCoverBitmap(Bitmap bitmap) {
        if (mLineView == null) return;
        mLineView.addBitmap(bitmap);
    }

    public void setOnEffectClickedListener(OnEffectClickedListener listener) {
        mOnEffectClickedListener = listener;
    }

    public void changePlayLineViewColor(String sceneCode) {
        mLineView.endAddColorRect();
        mLineView.addColorRect(TuSdkContext.getColor("lsq_scence_effect_color_" + sceneCode));
    }

    /**
     * 删除特效的同时删除对应色块
     *
     * @param seekTimeUs 待删除特效的开始时间
     */
    public void deleteLastLineView(long seekTimeUs) {
        if (mLineView == null) {
            return;
        }
        // 删除最后一段特效段并指定到最后一段特效的结束时间处
        mLineView.endAddColorRect();
        mLineView.deletedColorRect();
        mLineView.seekTo(seekTimeUs / (float) mVideoDurationUs);
    }

    public void moveLineViewToPercent(float percent) {
        if (mLineView != null) {
            mLineView.seekTo(percent);
        }
    }

    public void setEffectDeleteEnable(boolean enable) {
        mDeleteEffectBtn.setAlpha(enable ? 1 : 0.3f);
        mDeleteEffectBtn.setClickable(enable);
    }

    public void clearSceneEffects() {
        if (mLineView != null) {
            mLineView.clearAllColorRect();
        }
    }

    public void setVisible(boolean visible) {
        mIsVisible = visible;
    }

    public boolean isVisible() {
        return mIsVisible;
    }

    @Override
    protected void init() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.editor_effect_view, this);
        mSceneRecyclerView = view.findViewById(R.id.lsq_editor_effect_scene_list);
        mSceneRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
        mSceneAdapter = new SceneRecyclerAdapter();
        mSceneAdapter.setOnItemTouchListener(new SceneRecyclerAdapter.OnItemTouchListener() {
            /** 当前触摸的持续时间 **/
            long duration = 0;
            boolean isTouching = false;

            @Override
            public void onItemTouch(MotionEvent event, final int position, final SceneRecyclerAdapter.SceneViewHolder sceneViewHolder) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        //按下场景特效Item
                        if (isTouching) return;
                        isTouching = true;
                        mHandler.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                onPressSceneEffect(sceneViewHolder, position);
                            }
                        }, MIN_PRESS_DURATION_MILLIS);
                        mActionDownTime = System.currentTimeMillis();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        //手抬起来之后
                        isTouching = false;
                        mHandler.removeMessages(0);
                        if (System.currentTimeMillis() - mActionDownTime < MIN_PRESS_DURATION_MILLIS) {
                            return;
                        }
                        onReleaseSceneEffect(sceneViewHolder, position);
                        break;
                }
            }

            private void onPressSceneEffect(SceneRecyclerAdapter.SceneViewHolder sceneViewHolder, int position) {
                float percent = mLineView.getCurrentPercent();
                mStartTimeUs = (long) (mVideoDurationUs * percent);
                mSceneCode = mSceneAdapter.getSceneCode(position);

                if (mOnEffectClickedListener != null) {
                    mOnEffectClickedListener.onPressSceneEffect(position, mSceneCode, mStartTimeUs, sceneViewHolder);
                }
            }

            private void onReleaseSceneEffect(SceneRecyclerAdapter.SceneViewHolder sceneViewHolder, int position) {

                mLineView.endAddColorRect();
                if (mOnEffectClickedListener != null) {
                    mOnEffectClickedListener.onEffectItemReleased(sceneViewHolder);
                }
            }
        });
        mSceneRecyclerView.setAdapter(mSceneAdapter);

        mSceneAdapter.setSceneList(Arrays.asList(TuConfig.SCENE_EFFECT_CODES));

        mLineView = view.findViewById(R.id.lsq_editor_scene_play_range);
        mLineView.setType(0);
        mLineView.setOnProgressChangedListener(new TuSdkMovieScrollView.OnProgressChangedListener() {
            @Override
            public void onProgressChanged(float progress, boolean isTouching) {
                if (mOnEffectClickedListener != null) {
                    mOnEffectClickedListener.onLineViewProgressChanged(progress, isTouching);
                }
            }

            @Override
            public void onCancelSeek() {

            }
        });
        mLineView.setOnBackListener(new TuSdkMovieScrollView.OnColorGotoBackListener() {
            @Override
            public void onGotoBack(float percent) {
                prePercent = percent;
            }
        });

        mConfirmBtn = view.findViewById(R.id.confirm_btn);
        mConfirmBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnEffectClickedListener != null) {
                    mOnEffectClickedListener.onEffectConfirmClicked();
                }
            }
        });

        mDeleteEffectBtn = view.findViewById(R.id.delete_effect_btn);
        mDeleteEffectBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnEffectClickedListener != null) {
                    mOnEffectClickedListener.onEffectDeleteClicked();
                }
            }
        });
    }

    @Override
    public boolean isPlayerNeedZoom() {
        return false;
    }
}
