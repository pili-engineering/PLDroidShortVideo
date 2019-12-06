package com.qiniu.shortvideo.app.view;

import android.content.Context;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.qiniu.shortvideo.app.R;

/**
 * 编辑模块音量调节功能视图
 */
public class VolumeSettingBottomView extends BaseBottomView {

    private Context mContext;
    private OnAudioVolumeChangedListener mOnAudioVolumeChangedListener;

    private int mSrcVolume = 100;
    private int mMusicVolume = 100;

    private SeekBar mSrcVolumeSeekBar;
    private SeekBar mMusicVolumeSeekBar;
    private TextView mSrcVolumeText;
    private TextView mMusicVolumeText;

    public interface OnAudioVolumeChangedListener {
        void onAudioVolumeChanged(float srcVolume, float musicVolume);
    }

    public VolumeSettingBottomView(@NonNull Context context) {
        super(context);
        mContext = context;
        init();
    }

    public void setOnAudioVolumeChangedListener(OnAudioVolumeChangedListener listener) {
        mOnAudioVolumeChangedListener = listener;
    }

    /**
     * 设置音乐 SeekBar 是否可以点击
     *
     * @param enabled true or false
     */
    public void setMusicVolumeSettingEnabled(boolean enabled) {
        if (!enabled) {
            mMusicVolumeSeekBar.setProgress(100);
        }
        mMusicVolumeSeekBar.setEnabled(enabled);
    }

    @Override
    protected void init() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.editor_volume_setting, this);
        mSrcVolumeSeekBar = view.findViewById(R.id.src_volume_seekbar);
        mSrcVolumeText = view.findViewById(R.id.src_volume_value);
        mSrcVolumeText.setText(String.format(mContext.getResources().getString(R.string.volume_value), mSrcVolume));
        mSrcVolumeSeekBar.getThumb().setColorFilter(mContext.getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        mSrcVolumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mSrcVolume = progress;
                mSrcVolumeText.setText(String.format(mContext.getResources().getString(R.string.volume_value), progress));
                mOnAudioVolumeChangedListener.onAudioVolumeChanged(mSrcVolume / 100f, mMusicVolume / 100f);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        mMusicVolumeSeekBar = view.findViewById(R.id.music_volume_seekbar);
        mMusicVolumeText = view.findViewById(R.id.music_volume_value);
        mMusicVolumeText.setText(String.format(mContext.getResources().getString(R.string.volume_value), mMusicVolume));
        mMusicVolumeSeekBar.getThumb().setColorFilter(mContext.getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);
        mMusicVolumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mMusicVolume = progress;
                mMusicVolumeText.setText(String.format(mContext.getResources().getString(R.string.volume_value), progress));
                mOnAudioVolumeChangedListener.onAudioVolumeChanged(mSrcVolume / 100f, mMusicVolume / 100f);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        mMusicVolumeSeekBar.setEnabled(false);
    }

    @Override
    public boolean isPlayerNeedZoom() {
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 消耗点击事件以防止 bottom view 的自动隐藏
        return true;
    }
}
