package com.qiniu.shortvideo.app.view;

import android.content.Context;
import android.graphics.Bitmap;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;

import com.qiniu.pili.droid.shortvideo.PLMediaFile;
import com.qiniu.shortvideo.app.R;
import com.qiniu.shortvideo.app.model.AudioFile;
import com.qiniu.shortvideo.app.utils.ThumbLineViewSettings;
import com.qiniu.shortvideo.app.view.thumbline.ThumbLineRangeBar;
import com.qiniu.shortvideo.app.view.thumbline.ThumbLineView;

import java.util.ArrayList;
import java.util.List;

/**
 * 编辑模块添加音乐的视图
 */
public class MusicSelectBottomView extends BaseBottomView {
    private Context mContext;
    private RecyclerView mSelectedMusicRv;
    private SelectedMusicAdapter mSelectedMusicAdapter;
    // 滑动条相关
    protected ThumbLineView mVideoThumbLineView;
    private List<Bitmap> mBitmapList;
    protected int mCurrentPosition;
    protected PLMediaFile mMediaFile;
    protected boolean mIsPlaying = true;

    private OnMusicSelectOperationListener mOnMusicSelectOperationListener;
    private ArrayList<AudioFile> mDataSource;

    public interface OnMusicSelectOperationListener {
        void onMusicAddClicked();
        void onMusicRemoveClicked(AudioFile audioFile);
        void onMusicMixPositionChanged(int position);
        void onPlayStatusPaused();
        void onConfirmClicked();
    }

    public MusicSelectBottomView(Context context, PLMediaFile mediaFile, ArrayList<AudioFile> dataSource) {
        super(context);
        mContext = context;
        mMediaFile = mediaFile;
        mDataSource = dataSource;
        init();
    }

    public MusicSelectBottomView(@NonNull Context context) {
        super(context);
    }

    public void setOnMusicSelectOperationListener(OnMusicSelectOperationListener listener) {
        mOnMusicSelectOperationListener = listener;
    }

    public void addAudioFile(AudioFile audioFile) {
        mSelectedMusicAdapter.addAudioFile(audioFile);
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
     * 在音乐添加时添加相应的标识 RangeBar
     *
     * @param startTime 开始时间
     * @param duration 时长
     */
    public void addMusicBar(long startTime, long duration) {
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

        ThumbLineRangeBar thumbLineRangeBar = new ThumbLineRangeBar(mVideoThumbLineView, startTime, duration, 0, mMediaFile.getDurationMs(), thumbLineRangeBarView, null);
        mVideoThumbLineView.addOverlayRangeBar(thumbLineRangeBar);
        mVideoThumbLineView.switchRangeBarToFix();
    }

    /**
     * 移除音乐时要移除对应的 RangeBar
     *
     * @param index 指定 RangeBar 索引
     */
    public void removeMusicBar(int index) {
        mVideoThumbLineView.removeOverlayRangeBar(index);
    }

    /**
     * 开始播放
     */
    public void startPlayer() {
        mIsPlaying = true;
    }

    /**
     * 暂停播放
     */
    protected void pausePlayer() {
        mIsPlaying = false;
        if (mOnMusicSelectOperationListener != null) {
            mOnMusicSelectOperationListener.onPlayStatusPaused();
        }
    }

    @Override
    protected void init() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.editor_music_select_view, this);
        mSelectedMusicRv = view.findViewById(R.id.selected_music_rv);
        mSelectedMusicRv.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));
        mSelectedMusicAdapter = new SelectedMusicAdapter(mDataSource);
        mSelectedMusicRv.setAdapter(mSelectedMusicAdapter);

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
                if (mIsPlaying) {
                    pausePlayer();
                }
                mCurrentPosition = duration;
            }

            @Override
            public void onThumbLineSeekFinish(int duration) {
                mCurrentPosition = duration;
                if (mOnMusicSelectOperationListener != null) {
                    mOnMusicSelectOperationListener.onMusicMixPositionChanged(duration);
                }
            }
        });
        mVideoThumbLineView.setOnTouchListener(null);

        ImageButton musicAddBtn = view.findViewById(R.id.music_add_btn);
        musicAddBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnMusicSelectOperationListener != null) {
                    mOnMusicSelectOperationListener.onMusicAddClicked();
                }
            }
        });

        ImageButton confirmBtn = view.findViewById(R.id.music_confirm_btn);
        confirmBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnMusicSelectOperationListener != null) {
                    mOnMusicSelectOperationListener.onConfirmClicked();
                }
            }
        });
    }

    @Override
    public boolean isPlayerNeedZoom() {
        return false;
    }

    class SelectedMusicAdapter extends RecyclerView.Adapter<SelectedMusicViewHolder> {
        private ArrayList<AudioFile> mDataSource;

        public SelectedMusicAdapter(ArrayList dataSource) {
            mDataSource = dataSource;
        }

        public void addAudioFile(AudioFile audioFile) {
            mDataSource.add(audioFile);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public SelectedMusicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_selected_music, parent, false);
            return new SelectedMusicViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull SelectedMusicViewHolder holder, int position) {
            final AudioFile audioFile = mDataSource.get(position);
            holder.mMusicName.setText(audioFile.getTitle());
            holder.mDeleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnMusicSelectOperationListener != null) {
                        removeMusicBar(mDataSource.indexOf(audioFile));
                        mDataSource.remove(audioFile);
                        notifyDataSetChanged();
                        mOnMusicSelectOperationListener.onMusicRemoveClicked(audioFile);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mDataSource == null ? 0 : mDataSource.size();
        }
    }

    class SelectedMusicViewHolder extends RecyclerView.ViewHolder {
        TextView mMusicName;
        ImageButton mDeleteBtn;

        public SelectedMusicViewHolder(@NonNull View itemView) {
            super(itemView);
            mMusicName = itemView.findViewById(R.id.selected_music_name_tv);
            mDeleteBtn = itemView.findViewById(R.id.selected_music_remove_btn);
        }
    }
}
