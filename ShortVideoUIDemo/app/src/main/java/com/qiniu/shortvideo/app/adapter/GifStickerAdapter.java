package com.qiniu.shortvideo.app.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.qiniu.shortvideo.app.R;
import com.qiniu.shortvideo.app.utils.Config;

import java.util.ArrayList;
import java.util.List;

/**
 * 动态贴纸适配器
 */
public class GifStickerAdapter extends RecyclerView.Adapter<GifStickerAdapter.GifViewHolder> {
    private Context mContext;
    private List<String> mGifDataSource;
    private OnGifStickerClickListener mOnGifStickerClickListener;

    public interface OnGifStickerClickListener {
        void onGifClicked(String filePath);
    }

    public GifStickerAdapter(Context context, ArrayList<String> dataSource) {
        mContext = context;
        mGifDataSource = dataSource;
    }

    public void setOnGifStickerClickListener(OnGifStickerClickListener listener) {
        mOnGifStickerClickListener = listener;
    }

    @NonNull
    @Override
    public GifViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_gif_sticker, viewGroup, false);
        return new GifViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GifViewHolder gifViewHolder, int position) {
        final String filePath = Config.GIF_STICKER_DIR + mGifDataSource.get(position);
        Glide.with(mContext)
                .asGif()
                .load(filePath)
                .into(gifViewHolder.mGifFrameView);
        gifViewHolder.mGifFrameView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnGifStickerClickListener != null) {
                    mOnGifStickerClickListener.onGifClicked(filePath);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mGifDataSource == null ? 0 : mGifDataSource.size();
    }

    class GifViewHolder extends RecyclerView.ViewHolder {
        ImageView mGifFrameView;

        public GifViewHolder(@NonNull View itemView) {
            super(itemView);
            mGifFrameView = itemView.findViewById(R.id.gif_frame_view);
        }

    }
}
