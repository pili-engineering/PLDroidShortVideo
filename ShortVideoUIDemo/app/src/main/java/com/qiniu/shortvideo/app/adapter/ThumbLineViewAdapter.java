package com.qiniu.shortvideo.app.adapter;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.qiniu.shortvideo.app.R;
import com.qiniu.shortvideo.app.utils.ThumbLineViewSettings;

import java.util.ArrayList;
import java.util.List;

/**
 * 视频缩略图显示的适配器，用于视频编辑模块（添加贴纸、音乐等）
 */
public class ThumbLineViewAdapter extends RecyclerView.Adapter<ThumbLineViewAdapter.ThumbnailViewHolder> {
    private static final String TAG = "ThumbLineViewAdapter";
    private static final int VIEW_TYPE_HEADER = 1;
    private static final int VIEW_TYPE_FOOTER = 2;
    private static final int VIEW_TYPE_THUMBNAIL = 3;

    private ThumbLineViewSettings mThumbLineViewSettings;
    private List<Bitmap> mThumbnails;

    public ThumbLineViewAdapter(ThumbLineViewSettings thumbLineViewSettings) {
        mThumbLineViewSettings = thumbLineViewSettings;
        mThumbnails = new ArrayList<>();
    }

    @NonNull
    @Override
    public ThumbnailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        switch (viewType) {
            case VIEW_TYPE_HEADER:
            case VIEW_TYPE_FOOTER:
                view = new View(parent.getContext());
                view.setLayoutParams(new ViewGroup.LayoutParams(mThumbLineViewSettings.getScreenWidth() / 2, ViewGroup.LayoutParams.MATCH_PARENT));
                view.setBackgroundColor(Color.TRANSPARENT);
                return new ThumbnailViewHolder(view);
            default:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_thumbnail_view, parent, false);
                return new ThumbnailViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ThumbnailViewHolder thumbnailViewHolder, int position) {
        // 去掉第一个 View 和最后一个 View
        if (position > 0 && position < mThumbLineViewSettings.getThumbnailCount() + 1 && position < mThumbnails.size()) {
            Bitmap bitmap = mThumbnails.get(position - 1);
            if (bitmap != null && !bitmap.isRecycled()) {
                thumbnailViewHolder.mThumbnailIv.setImageBitmap(bitmap);
            }
        }
    }

    @Override
    public int getItemCount() {
        return mThumbnails == null ? 0 : mThumbLineViewSettings.getThumbnailCount() + 2;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return VIEW_TYPE_HEADER;
        } else if (position == mThumbLineViewSettings.getThumbnailCount() + 1) {
            return VIEW_TYPE_FOOTER;
        } else {
            return VIEW_TYPE_THUMBNAIL;
        }
    }

    public void addBitmap(Bitmap bitmap) {
        mThumbnails.add(bitmap);
        notifyItemChanged(mThumbnails.size() - 1);
    }

    public void addBitmap(int position, Bitmap bitmap) {
        mThumbnails.add(position, bitmap);
        notifyItemChanged(position);
    }

    class ThumbnailViewHolder extends RecyclerView.ViewHolder {
        ImageView mThumbnailIv;

        public ThumbnailViewHolder(@NonNull View itemView) {
            super(itemView);
            mThumbnailIv = itemView.findViewById(R.id.thumbnail_view);
        }
    }
}
