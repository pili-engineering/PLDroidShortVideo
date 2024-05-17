package com.qiniu.pili.droid.shortvideo.demo.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.qiniu.pili.droid.shortvideo.demo.R;


public class IndicatorSeekBar extends RelativeLayout implements SeekBar.OnSeekBarChangeListener {
    private SeekBar seekBar;
    private TextView textView;

    public IndicatorSeekBar(Context context) {
        super(context);
        init();
    }

    public IndicatorSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public IndicatorSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public IndicatorSeekBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        inflate(getContext(), R.layout.seekbar_indicator, this);
        seekBar = findViewById(R.id.indicatorSeekbar);
        textView = findViewById(R.id.indicatorTextview);
        seekBar.setOnSeekBarChangeListener(this);
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        textView.setText(progress + "%");
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    public int getProgress() {
        return seekBar.getProgress();
    }

    public void setProgress(int progress) {
        seekBar.setProgress(progress);
        textView.setText(progress + "%");
    }
}
