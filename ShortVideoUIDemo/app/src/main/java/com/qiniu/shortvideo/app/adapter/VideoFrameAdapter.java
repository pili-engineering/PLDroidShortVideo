package com.qiniu.shortvideo.app.adapter;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;

/**
 * 缩略图显示适配器，用于视频的剪辑模块
 */
public class VideoFrameAdapter extends RecyclerView.Adapter<VideoFrameAdapter.VideoFrameViewHolder> {

    private List<Bitmap> mVideoFrames;
    private int mFrameWidth = WRAP_CONTENT;

    public VideoFrameAdapter() {
        mVideoFrames = new ArrayList<>();
    }

    public void add(Bitmap bitmap) {
        mVideoFrames.add(bitmap);
        notifyItemInserted(mVideoFrames.size() - 1);
    }

    public void clearAllBitmap() {
        mVideoFrames.clear();
        notifyDataSetChanged();
    }

    public void setVideoFrames(List<Bitmap> bitmap) {
        mVideoFrames.clear();
        mVideoFrames.addAll(bitmap);
        notifyDataSetChanged();
    }

    public void setFrameWidth(int width) {
        mFrameWidth = width;
    }

    @NonNull
    @Override
    public VideoFrameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ImageView view = new ImageView(parent.getContext());
        ViewGroup.LayoutParams lp = view.getLayoutParams();
        if (lp == null) {
            lp = new ViewGroup.LayoutParams(mFrameWidth, MATCH_PARENT);
        } else {
            lp.width = mFrameWidth;
        }
        view.setLayoutParams(lp);
        view.setScaleType(ImageView.ScaleType.CENTER_CROP);
        return new VideoFrameViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoFrameViewHolder videoFrameViewHolder, int position) {
        videoFrameViewHolder.mVideoFrame.setImageBitmap(mVideoFrames.get(position));
    }

    @Override
    public int getItemCount() {
        return mVideoFrames.size();
    }

    public class VideoFrameViewHolder extends RecyclerView.ViewHolder {
        ImageView mVideoFrame;

        public VideoFrameViewHolder(View itemView) {
            super(itemView);
            mVideoFrame = (ImageView) itemView;
        }
    }
}
