package com.qiniu.shortvideo.app.adapter;

import android.content.Context;
import android.graphics.Paint;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.qiniu.shortvideo.app.R;
import com.qiniu.shortvideo.app.model.MediaFile;

import java.util.ArrayList;
import java.util.List;

import static com.qiniu.shortvideo.app.activity.MediaSelectActivity.GRID_ITEM_COUNT;

/**
 * 视频选择适配器，获取本地视频并显示时间等 UI
 */
public class MediaFileAdapter extends RecyclerView.Adapter<MediaFileAdapter.MediaItemViewHolder> {
    public static final int TYPE_MEDIA_SHOW = 0;
    public static final int TYPE_MEDIA_CHOSEN = 1;

    private Context mContext;
    private ArrayList<MediaFile> mDataSource;
    private RequestOptions mRequestOptions;
    private OnItemClickListener mOnItemClickListener;

    private int mCurrentSelectedPosition = -1;

    private int mType;

    public interface OnItemClickListener {
        void onMediaItemClicked(MediaFile mediaFile, int position);
    }

    public MediaFileAdapter(Context context, int type) {
        this(context, type, null);
    }

    public MediaFileAdapter(Context context, int type, List dataSource) {
        mContext = context;
        mType = type;
        mDataSource = new ArrayList<>();
        if (dataSource != null) {
            mDataSource.addAll(dataSource);
        }
        int itemWidth = calculateItemSize();
        mRequestOptions = new RequestOptions()
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .override(itemWidth, itemWidth);
        Glide.get(context).clearMemory();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    public void addMediaFile(MediaFile mediaFile) {
        if (mDataSource != null) {
            int position = mDataSource.size();
            mDataSource.add(mediaFile);
            notifyItemInserted(position);
        }
    }

    public void addMediaFiles(List<MediaFile> mediaFiles) {
        if (mDataSource != null) {
            int dataCount = mDataSource.size();
            mDataSource.addAll(mediaFiles);
            notifyItemRangeInserted(dataCount, mediaFiles.size());
        }
    }

    public ArrayList<MediaFile> getMediaFiles() {
        return mDataSource;
    }

    @NonNull
    @Override
    public MediaItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_media_file, parent, false);
        return new MediaItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MediaItemViewHolder mediaItemViewHolder, final int position) {
        final MediaFile mediaFile = mDataSource.get(position);
        Glide.with(mContext)
                .load(mediaFile.getThumbPath())
                .placeholder(R.drawable.ic_placeholder)
                .apply(mRequestOptions)
                .into(mediaItemViewHolder.mMediaThumbnail);

        if (mediaFile.getType() == MediaFile.VIDEO) {
            mediaItemViewHolder.mDurationText.setVisibility(View.VISIBLE);
            mediaItemViewHolder.mDurationText.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG); //下划线
            mediaItemViewHolder.mDurationText.getPaint().setAntiAlias(true);//抗锯齿
            mediaItemViewHolder.mDurationText.setText(
                    secToTime(Math.max((int) (mediaFile.getDuration() / 1000), 1)));
        }
        if (mType == TYPE_MEDIA_CHOSEN) {
            mediaItemViewHolder.mDeleteBtn.setVisibility(View.VISIBLE);
            mediaItemViewHolder.mDeleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = mDataSource.indexOf(mediaFile);
                    mDataSource.remove(mediaFile);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, mDataSource.size());
                }
            });
        }

        mediaItemViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurrentSelectedPosition = position;
                if (mOnItemClickListener != null) {
                    mOnItemClickListener.onMediaItemClicked(mediaFile, mCurrentSelectedPosition);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDataSource.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    class MediaItemViewHolder extends RecyclerView.ViewHolder {
        ImageView mMediaThumbnail;
        TextView mDurationText;
        ImageView mDeleteBtn;

        public MediaItemViewHolder(@NonNull View itemView) {
            super(itemView);
            mMediaThumbnail = itemView.findViewById(R.id.video_thumbnail);
            mDurationText = itemView.findViewById(R.id.duration_text);
            mDeleteBtn = itemView.findViewById(R.id.delete_btn);
        }
    }

    private int calculateItemSize() {
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        int marginSizeLeftAndRight = 10;
        int width = wm.getDefaultDisplay().getWidth() - marginSizeLeftAndRight * 2;
        int marginSizeStart = 3;
        int marginSizeMiddle = 5;
        return  (width - marginSizeStart * 2 - marginSizeMiddle * (GRID_ITEM_COUNT - 1)) / GRID_ITEM_COUNT;
    }

    private String secToTime(int time) {
        String timeStr;
        int hour;
        int minute;
        int second;
        if (time <= 0)
            return "00:00";
        else {
            minute = time / 60;
            if (minute < 60) {
                second = time % 60;
                timeStr = unitFormat(minute) + ":" + unitFormat(second);
            } else {
                hour = minute / 60;
                if (hour > 99)
                    return "99:59:59";
                minute = minute % 60;
                second = time - hour * 3600 - minute * 60;
                timeStr = unitFormat(hour) + ":" + unitFormat(minute) + ":" + unitFormat(second);
            }
        }
        return timeStr;
    }

    private String unitFormat(int i) {
        String retStr = null;
        if (i >= 0 && i < 10)
            retStr = "0" + Integer.toString(i);
        else
            retStr = "" + i;
        return retStr;
    }
}
