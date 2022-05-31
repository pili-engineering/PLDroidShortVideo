package com.qiniu.shortvideo.app.activity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

import com.qiniu.shortvideo.app.R;
import com.qiniu.shortvideo.app.adapter.ChooseMusicAdapter;
import com.qiniu.shortvideo.app.model.AudioFile;
import com.qiniu.shortvideo.app.utils.MediaUtils;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * 选择混音的音乐，并依据选择的时间段进行播放
 */
public class ChooseMusicActivity extends AppCompatActivity implements
        ChooseMusicAdapter.OnMusicClickListener {
    private static final String TAG = "ChooseMusicActivity";
    public static final String SELECTED_MUSIC_FILE = "selectedMusicFile";
    public static final String START_TIME = "startTime";
    public static final String END_TIME = "endTime";

    private MediaPlayer mMediaPlayer;

    private RecyclerView mMusicRecyclerView;
    private ChooseMusicAdapter mMusicAdapter;
    private ArrayList<AudioFile> mAudioFiles;
    private Handler mPlayHandler = new Handler(Looper.getMainLooper());

    private int mCurrentPos = -1;
    private long mStartTime;
    private long mEndTime;

    /**
     * 根据选择的时间实现循环播放
     */
    private Runnable mMusicRunnable = new Runnable() {
        @Override
        public void run() {
            mMediaPlayer.seekTo((int) mStartTime);
            mMediaPlayer.start();
            mPlayHandler.postDelayed(this, mEndTime - mStartTime);
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_music);

        mMusicRecyclerView = findViewById(R.id.music_list);

        mAudioFiles = MediaUtils.getLocalAudios(getApplicationContext());
        mMusicAdapter = new ChooseMusicAdapter(mAudioFiles);
        mMusicAdapter.setVideoDuration(getIntent().getLongExtra("videoDurationMs", 0L));
        mMusicAdapter.setOnMusicClickListener(this);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false);
        mMusicRecyclerView.setLayoutManager(layoutManager);
        mMusicRecyclerView.addItemDecoration(
                new DividerItemDecoration(getApplicationContext(), DividerItemDecoration.VERTICAL));
        mMusicRecyclerView.setAdapter(mMusicAdapter);

        initPlayer();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPlayHandler.removeCallbacks(mMusicRunnable);
        mPlayHandler = null;
        mMediaPlayer.release();
        mMediaPlayer = null;
    }

    public void onClickBack(View v) {
        finishActivity();
    }

    private void initPlayer() {
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setLooping(true);
    }

    private boolean startToPlay(String filePath) {
        try {
            mMediaPlayer.setDataSource(filePath);
            mMediaPlayer.prepare();
            mPlayHandler.postDelayed(mMusicRunnable, 0);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private void stopToPlay() {
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.stop();
            mMediaPlayer.reset();
        }
    }

    private void finishActivity() {
        mPlayHandler.removeCallbacks(mMusicRunnable);
        stopToPlay();
        finish();
    }

    @Override
    public boolean onMusicClicked(AudioFile audioFile, int position, boolean isPlaying) {
        if (audioFile == null) {
            if (mCurrentPos != 0) {
                stopToPlay();
                mMusicAdapter.setItemInvisible(
                        (ChooseMusicAdapter.MusicViewHolder) mMusicRecyclerView.findViewHolderForAdapterPosition(mCurrentPos));
                mCurrentPos = 0;
            }
            return true;
        }
        if (mCurrentPos != position) {
            stopToPlay();
            mMusicAdapter.setItemInvisible(
                    (ChooseMusicAdapter.MusicViewHolder) mMusicRecyclerView.findViewHolderForAdapterPosition(mCurrentPos));
            mCurrentPos = position;
            mStartTime = 0;
            mEndTime = audioFile.getDuration();
            return startToPlay(audioFile.getFilePath());
        } else {
            if (isPlaying) {
                stopToPlay();
                return true;
            } else {
                return startToPlay(audioFile.getFilePath());
            }
        }
    }

    @Override
    public void onMusicConfirmed(AudioFile audioFile) {
        Intent intent = new Intent();
        intent.putExtra(SELECTED_MUSIC_FILE, (Serializable) audioFile);
        intent.putExtra(START_TIME, mStartTime);
        intent.putExtra(END_TIME, mEndTime);
        setResult(RESULT_OK, intent);

        finishActivity();
    }

    @Override
    public void onMusicRangeChanged(AudioFile audioFile, long startTime, long endTime) {
        mPlayHandler.removeCallbacks(mMusicRunnable);
        mStartTime = startTime;
        mEndTime = endTime;
        mPlayHandler.postDelayed(mMusicRunnable, 0);
    }
}
