package com.qiniu.shortvideo.app.view;

import android.content.Context;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.qiniu.pili.droid.shortvideo.PLMediaFile;
import com.qiniu.shortvideo.app.R;
import com.qiniu.shortvideo.app.adapter.GifStickerAdapter;
import com.qiniu.shortvideo.app.view.layer.OnStickerOperateListener;
import com.qiniu.shortvideo.app.view.layer.StickerImageView;
import com.qiniu.shortvideo.app.view.thumbline.ThumbLineRangeBar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 编辑模块 GIF 动图添加视图
 */
public class GifBottomView extends StickerBottomView {

    private GifStickerAdapter mGifStickerAdapter;
    private OnGifItemClickListener mOnGifItemClickListener;
    private Map<StickerImageView, ThumbLineRangeBar> mStickers;

    public interface OnGifItemClickListener {
        void onGifItemClicked(StickerImageView stickerImageView);
        void onGifItemDeleted(StickerImageView stickerImageView);
    }

    public GifBottomView(Context context, PLMediaFile mediaFile) {
        super(context, mediaFile);
    }

    public void setOnGifItemClickListener(OnGifItemClickListener listener) {
        mOnGifItemClickListener = listener;
    }

    /**
     * 点击贴图时使得贴图可编辑
     *
     * @param stickerImageView 选中的贴图
     */
    public void selectSticker(StickerImageView stickerImageView) {
        stickerImageView.setEditable(true);
        mVideoThumbLineView.switchRangeBarToActive(mStickers.get(stickerImageView));
        pausePlayer();
    }

    @Override
    protected void init() {
        super.init();
        mBottomViewTitle.setText(R.string.gif_sticker_string);

        mStickerRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
        mGifStickerAdapter = new GifStickerAdapter(mContext, new ArrayList<>(Arrays.asList(getResources().getStringArray(R.array.gif_name))));
        mGifStickerAdapter.setOnGifStickerClickListener(new GifStickerAdapter.OnGifStickerClickListener() {
            @Override
            public void onGifClicked(final String filePath) {
                if (mIsPlaying) {
                    pausePlayer();
                }
                // 点击具体动图时，添加到滑动条上
                ThumbLineRangeBar.ThumbLineRangeBarView thumbLineRangeBarView = new ThumbLineRangeBar.ThumbLineRangeBarView() {
                    View rootView = LayoutInflater.from(mContext).inflate(R.layout.widget_range_bar, null);
                    View headView = rootView.findViewById(R.id.head_view);
                    View tailView = rootView.findViewById(R.id.tail_view);
                    View middleView = rootView.findViewById(R.id.middle_view);

                    @Override
                    public ViewGroup getContainer() {
                        return (ViewGroup) rootView;
                    }

                    @Override
                    public View getHeadView() {
                        return headView;
                    }

                    @Override
                    public View getTailView() {
                        return tailView;
                    }

                    @Override
                    public View getMiddleView() {
                        return middleView;
                    }
                };

                final StickerImageView stickerImage = (StickerImageView) View.inflate(mContext, R.layout.sticker_image_view, null);
                stickerImage.setGifFile(filePath);
                stickerImage.showDelete(true);
                stickerImage.showEdit(false);
                stickerImage.setEditable(true);
                stickerImage.startGifPlaying();
                stickerImage.setOnStickerOperateListener(new OnStickerOperateListener() {
                    @Override
                    public void onDeleteClicked() {
                        removeRangeBar(mStickers.get(stickerImage));
                        mStickers.remove(stickerImage);
                        if (mOnGifItemClickListener != null) {
                            mOnGifItemClickListener.onGifItemDeleted(stickerImage);
                        }
                    }

                    @Override
                    public void onEditClicked() {

                    }

                    @Override
                    public void onRotateClicked() {

                    }

                    @Override
                    public void onStickerSelected() {

                    }
                });

                ThumbLineRangeBar thumbLineRangeBar = new ThumbLineRangeBar(mVideoThumbLineView, mCurrentPosition, 2000, 2000, mMediaFile.getDurationMs(), thumbLineRangeBarView, new ThumbLineRangeBar.OnSelectedDurationChangeListener() {
                    @Override
                    public void onRangeBarClicked(ThumbLineRangeBar rangeBar) {
                        // 只有点击贴图的时候才可以改变动图的显示时间段，单独点击不会触发
                    }

                    @Override
                    public void onDurationChange(long startTime, long duration) {
                        stickerImage.setStartTime(startTime, startTime + duration);
                    }
                });

                if (mStickers == null) {
                    mStickers = new HashMap<>();
                }

                mStickers.put(stickerImage, thumbLineRangeBar);

                addRangeBar(thumbLineRangeBar);

                if (mOnGifItemClickListener != null) {
                    mOnGifItemClickListener.onGifItemClicked(stickerImage);
                }
            }
        });
        mStickerRecyclerView.setAdapter(mGifStickerAdapter);
    }

    @Override
    public boolean isPlayerNeedZoom() {
        return false;
    }

    @Override
    protected void playStatusChanged(boolean isPlaying) {

    }
}
