package com.qiniu.shortvideo.app.view;

import android.content.Context;
import android.graphics.Bitmap;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import com.qiniu.pili.droid.shortvideo.PLMediaFile;
import com.qiniu.shortvideo.app.R;
import com.qiniu.shortvideo.app.utils.ThumbLineViewSettings;
import com.qiniu.shortvideo.app.view.thumbline.ThumbLineRangeBar;
import com.qiniu.shortvideo.app.view.thumbline.ThumbLineView;

import java.util.List;

/**
 * 编辑模块贴纸相关的功能视图基类（文字特效、动态贴纸）
 */
public abstract class StickerBottomView extends BaseBottomView {

    protected Context mContext;
    private ImageButton mConfirmBtn;
    protected TextView mBottomViewTitle;
    protected RecyclerView mStickerRecyclerView;
    protected ThumbLineView mVideoThumbLineView;
    protected PLMediaFile mMediaFile;
    private List<Bitmap> mBitmapList;
    protected int mCurrentPosition;
    protected boolean mIsPlaying = true;

    private OnViewOperateListener mOnViewOperateListener;

    public interface OnViewOperateListener {
        void onConfirmClicked();
        void onPlayStatusPaused();
        void onPlayProgressChanged(int position);
    }

    protected abstract void playStatusChanged(boolean isPlaying);

    public StickerBottomView(@NonNull Context context, PLMediaFile mediaFile) {
        super(context);
        mContext = context;
        mMediaFile = mediaFile;
        init();
    }

    public void setOnViewOperateListener(OnViewOperateListener listener) {
        mOnViewOperateListener = listener;
    }

    /**
     * 设置缩略图的 bitmap 集合
     *
     * @param bitmaps 缩略图
     */
    public void setBitmapList(List<Bitmap> bitmaps) {
        mBitmapList = bitmaps;
        if (mBitmapList != null) {
            for (Bitmap bp : mBitmapList) {
                mVideoThumbLineView.addBitmap(bp);
            }
        }
    }

    /**
     * 为 ThumbLineView 添加缩略图
     *
     * @param bitmap 待添加的 Bitmap
     */
    public void addBitmap(Bitmap bitmap) {
        if (mVideoThumbLineView == null) {
            return;
        }
        mVideoThumbLineView.addBitmap(bitmap);
    }

    /**
     * 移动 ThumbLineView 到指定的 position
     *
     * @param position 指定的 position
     */
    public void moveThumbLineViewToPosition(int position) {
        if (mVideoThumbLineView == null) {
            return;
        }
        mVideoThumbLineView.seekTo(position);
        mCurrentPosition = position;
    }

    /**
     * 添加指定的 RangeBar
     *
     * @param rangeBar 指定的 RangeBar
     */
    public void addRangeBar(ThumbLineRangeBar rangeBar) {
        mVideoThumbLineView.addOverlayRangeBar(rangeBar);
    }

    /**
     * 移除指定位置 RangeBar
     *
     * @param index RangeBar 索引
     */
    public void removeRangeBar(int index) {
        mVideoThumbLineView.removeOverlayRangeBar(index);
    }

    /**
     * 移除指定的 RangeBar
     *
     * @param rangeBar 指定的 RangeBar
     */
    public void removeRangeBar(ThumbLineRangeBar rangeBar) {
        mVideoThumbLineView.removeOverlayRangeBar(rangeBar);
    }

    /**
     * 开始播放
     */
    public void startPlayer() {
        mIsPlaying = true;
        mVideoThumbLineView.switchRangeBarToFix();
    }

    /**
     * 暂停播放
     */
    protected void pausePlayer() {
        mIsPlaying = false;
        if (mOnViewOperateListener != null) {
            mOnViewOperateListener.onPlayStatusPaused();
        }
    }

    @Override
    protected void init() {
        final View view = LayoutInflater.from(mContext).inflate(R.layout.editor_gif_sticker_view, this);
        mBottomViewTitle = view.findViewById(R.id.bottom_view_title);

        mVideoThumbLineView = view.findViewById(R.id.thumb_line_view);
        // 获取屏幕宽度
        WindowManager manager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics outMetrics = new DisplayMetrics();
        manager.getDefaultDisplay().getMetrics(outMetrics);
        int width = outMetrics.widthPixels;

        ThumbLineViewSettings thumbLineViewSettings = new ThumbLineViewSettings()
                .setMediaFile(mMediaFile)
                .setThumbnailCount(20)
                .setThumbnailWidth(50)
                .setThumbnailHeight(50)
                .setVideoDuration(mMediaFile.getDurationMs())
                .setScreenWidth(width);
        mVideoThumbLineView.setup(thumbLineViewSettings, new ThumbLineView.OnThumbLineSeekListener() {
            @Override
            public void onThumbLineSeek(int duration) {
                // 如果正在播放，则暂停
                mCurrentPosition = duration;
                if (mIsPlaying) {
                    pausePlayer();
                }
            }

            @Override
            public void onThumbLineSeekFinish(int duration) {
                mCurrentPosition = duration;
                if (mOnViewOperateListener != null) {
                    mOnViewOperateListener.onPlayProgressChanged(duration);
                }
            }
        });

        mConfirmBtn = view.findViewById(R.id.confirm_btn);
        mConfirmBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mVideoThumbLineView.switchRangeBarToFix();
                if (mOnViewOperateListener != null) {
                    mOnViewOperateListener.onConfirmClicked();
                }
            }
        });

        mStickerRecyclerView = view.findViewById(R.id.gif_sticker_list_view);
    }

    @Override
    public boolean isPlayerNeedZoom() {
        return false;
    }
}
