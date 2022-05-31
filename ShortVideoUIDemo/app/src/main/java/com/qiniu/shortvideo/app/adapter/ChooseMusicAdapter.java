package com.qiniu.shortvideo.app.adapter;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.Group;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.qiniu.shortvideo.app.R;
import com.qiniu.shortvideo.app.model.AudioFile;
import com.qiniu.shortvideo.app.view.MusicWaveView;
import com.qiniu.shortvideo.app.view.RangeSlider;

import java.util.ArrayList;
import java.util.List;

/**
 * 音乐选择的适配器
 */
public class ChooseMusicAdapter extends RecyclerView.Adapter<ChooseMusicAdapter.MusicViewHolder> {
    private List<AudioFile> mAudioFiles;
    private OnMusicClickListener mOnMusicClickListener;
    private long mVideoDurationMs;
    private int mScrollX;

    public interface OnMusicClickListener {
        boolean onMusicClicked(AudioFile audioFile, int position, boolean isPlaying);

        void onMusicConfirmed(AudioFile audioFile);

        void onMusicRangeChanged(AudioFile audioFile, long startTime, long endTime);
    }

    public ChooseMusicAdapter(ArrayList<AudioFile> audioFiles) {
        mAudioFiles = audioFiles;
    }

    public void setOnMusicClickListener(OnMusicClickListener listener) {
        mOnMusicClickListener = listener;
    }

    public void setItemInvisible(MusicViewHolder holder) {
        if (holder != null) {
            holder.mConfirmBtn.setVisibility(View.INVISIBLE);
            holder.mMusicSelectView.setVisibility(View.GONE);
            holder.mMusicSelectSlider.resetRangePos();
            holder.mPlayControlView.setImageResource(R.drawable.ic_music_play);
            holder.mIsPlaying = false;
        }
    }

    public void setVideoDuration(long videoDurationMs) {
        mVideoDurationMs = videoDurationMs;
    }

    private void setDurationTxt(MusicViewHolder holder, long startTime, long endTime) {
        long time = startTime / 1000;
        long min = time / 60;
        long sec = time % 60;
        holder.mStartTimeText.setText(String.format("%1$02d:%2$02d", min, sec));
        time = endTime / 1000;
        min = time / 60;
        sec = time % 60;
        holder.mEndTimeText.setText(String.format("%1$02d:%2$02d", min, sec));
    }

    @NonNull
    @Override
    public MusicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_music, parent, false);
        return new MusicViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MusicViewHolder musicViewHolder, final int position) {
        final AudioFile audioFile = mAudioFiles.get(position);
        musicViewHolder.mMusicSelectView.setVisibility(View.GONE);
        musicViewHolder.mMusicNameText.setText(audioFile.getTitle());
        musicViewHolder.mSingerText.setText(audioFile.getSinger());
        musicViewHolder.mMusicWaveView.setMusicDuration(audioFile.getDuration());
        musicViewHolder.mMusicWaveView.setVideoDuration(mVideoDurationMs);
        musicViewHolder.mMusicWaveView.layout();
        setDurationTxt(musicViewHolder, 0, audioFile.getDuration());
        musicViewHolder.mMusicSelectSlider.setRangeChangeListener(new RangeSlider.OnRangeChangeListener() {
            @Override
            public void onKeyDown(int type) {

            }

            @Override
            public void onKeyUp(int type, int leftPinIndex, int rightPinIndex) {
                if (mOnMusicClickListener != null) {
                    long startTime = audioFile.getDuration() * leftPinIndex / 100;
                    long endTime = audioFile.getDuration() * rightPinIndex / 100;
                    setDurationTxt(musicViewHolder, startTime, endTime);
                    mOnMusicClickListener.onMusicRangeChanged(audioFile, startTime, endTime);
                }
            }
        });

        musicViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                musicViewHolder.mConfirmBtn.setVisibility(View.VISIBLE);
                musicViewHolder.mMusicSelectView.setVisibility(View.VISIBLE);
                if (musicViewHolder.mIsPlaying) {
                    if (mOnMusicClickListener != null
                            && mOnMusicClickListener.onMusicClicked(audioFile, position, true)) {
                        musicViewHolder.mPlayControlView.setImageResource(R.drawable.ic_music_play);
                        musicViewHolder.mIsPlaying = false;
                    }
                } else {
                    if (mOnMusicClickListener != null
                            && mOnMusicClickListener.onMusicClicked(audioFile, position, false)) {
                        musicViewHolder.mPlayControlView.setImageResource(R.drawable.ic_music_pause);
                        musicViewHolder.mIsPlaying = true;
                    }
                }
            }
        });

        musicViewHolder.mConfirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnMusicClickListener != null) {
                    mOnMusicClickListener.onMusicConfirmed(audioFile);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mAudioFiles.isEmpty() ? 0 : mAudioFiles.size();
    }

    public class MusicViewHolder extends RecyclerView.ViewHolder {
        ImageView mPlayControlView;
        TextView mMusicNameText;
        TextView mSingerText;
        TextView mStartTimeText;
        TextView mEndTimeText;
        Button mConfirmBtn;
        Group mMusicSelectView;
        MusicWaveView mMusicWaveView;
        RangeSlider mMusicSelectSlider;

        boolean mIsPlaying;

        public MusicViewHolder(@NonNull View itemView) {
            super(itemView);
            mPlayControlView = itemView.findViewById(R.id.play_btn);
            mMusicNameText = itemView.findViewById(R.id.music_name_text);
            mSingerText = itemView.findViewById(R.id.music_singer_text);
            mStartTimeText = itemView.findViewById(R.id.music_start_time);
            mEndTimeText = itemView.findViewById(R.id.music_end_time);
            mConfirmBtn = itemView.findViewById(R.id.confirm_btn);
            mMusicSelectView = itemView.findViewById(R.id.music_select_views);
            mMusicWaveView = itemView.findViewById(R.id.music_wave_view);
            mMusicSelectSlider = itemView.findViewById(R.id.music_select_range_bar);
        }
    }
}
