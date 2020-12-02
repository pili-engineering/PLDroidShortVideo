package com.qiniu.pili.droid.shortvideo.demo.view;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import android.view.View;
import android.widget.SeekBar;

import com.qiniu.pili.droid.shortvideo.demo.R;

public class AudioMixSettingDialog extends BottomSheetDialog {

    private int mFgVolume = 100;
    private int mBgVolume = 100;

    private SeekBar mSrcVolumeSeekBar;
    private SeekBar mMixVolumeSeekBar;
    private SeekBar mMixPositionSeekBar;

    private OnPositionSelectedListener mOnPositionSelectedListener;

    private OnAudioVolumeChangedListener mOnAudioVolumeChangedListener;

    public interface OnAudioVolumeChangedListener {
        void onAudioVolumeChanged(int fgVolume, int bgVolume);
    }

    public interface OnPositionSelectedListener {
        void onPositionSelected(long position);
    }

    public AudioMixSettingDialog(Context context){
        super(context);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.dialog_audio_mix);
        super.onCreate(savedInstanceState);

        mSrcVolumeSeekBar = (SeekBar) findViewById(R.id.fg_volume);
        mSrcVolumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mFgVolume = progress;
                mOnAudioVolumeChangedListener.onAudioVolumeChanged(mFgVolume, mBgVolume);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        mMixVolumeSeekBar = (SeekBar) findViewById(R.id.bg_volume);
        mMixVolumeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                mBgVolume = progress;
                mOnAudioVolumeChangedListener.onAudioVolumeChanged(mFgVolume, mBgVolume);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        mMixPositionSeekBar = (SeekBar) findViewById(R.id.mix_position);
        mMixPositionSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                long seekTime = seekBar.getProgress();
                mOnPositionSelectedListener.onPositionSelected(seekTime);
            }
        });
        setBehaviorCallback();
    }

    public void setSrcVolumeProgress(int progress) {
        mSrcVolumeSeekBar.setProgress(progress);
    }

    public int getSrcVolumeProgress() {
        return mSrcVolumeSeekBar.getProgress();
    }

    public void setMixVolumeProgress(int progress) {
        mMixVolumeSeekBar.setProgress(progress);
    }

    public void setMixAudioMaxVolume(int maxVolume) {
        mMixVolumeSeekBar.setMax(maxVolume);
    }

    public void setMixPosition(int position) {
        mMixPositionSeekBar.setProgress(position);
    }

    public void setMixMaxPosition(int maxPosition) {
        mMixPositionSeekBar.setMax(maxPosition);
    }

    public void clearMixAudio() {
        setMixPosition(0);
        setSrcVolumeProgress(100);
        setMixAudioMaxVolume(100);
    }

    public void setOnPositionSelectedListener(OnPositionSelectedListener listener) {
        mOnPositionSelectedListener = listener;
    }

    public void setOnAudioVolumeChangedListener(OnAudioVolumeChangedListener listener) {
        mOnAudioVolumeChangedListener = listener;
    }

    private void setBehaviorCallback() {
        View view = getDelegate().findViewById(R.id.design_bottom_sheet);
        final BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(view);
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    dismiss();
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            }
        });
    }

}
