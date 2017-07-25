package com.kiwi.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.kiwi.ui.OnViewEventListener;
import com.kiwi.ui.R;
import com.kiwi.ui.SharedPreferenceManager;

import java.util.ArrayList;
import java.util.List;

import static com.kiwi.ui.widget.KwControlView.REMOVE_BLEMISHES;
import static com.kiwi.ui.widget.KwControlView.SKIN_SHINNING_TENDERNESS;
import static com.kiwi.ui.widget.KwControlView.SKIN_TONE_PERFECTION;
import static com.kiwi.ui.widget.KwControlView.SKIN_TONE_SATURATION;

public class FaceBeautyView extends FrameLayout implements View.OnClickListener{
    private SharedPreferenceManager instance;
    private ImageView mSwitchBeautySecond;
    private OnViewEventListener onEventListener;
    private List<SeekBarRow> seekBarRowList = new ArrayList<>();
    private boolean isOpen = false;

    public void setOnEventListener(OnViewEventListener onEventListener) {
        this.onEventListener = onEventListener;
    }

    public FaceBeautyView(Context context) {
        super(context);
        init(null, 0);
    }

    public FaceBeautyView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public FaceBeautyView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        this.instance = SharedPreferenceManager.getInstance();
        LayoutInflater.from(getContext()).inflate(R.layout.face_beauty_layout, this);

        //全局美颜2
        mSwitchBeautySecond = (ImageView) findViewById(R.id.switch_beauty_second);
        mSwitchBeautySecond.setOnClickListener(this);

        initSeekBarUI();

        this.isOpen = instance.isBeautyEnabled();
        updateView();
    }

    private void initSeekBarUI() {
        seekBarRowList.clear();
        seekBarRowList.add(new SeekBarRow(SKIN_TONE_PERFECTION,(SeekBar) findViewById(R.id.seekbar_skinPerfection),(TextView)findViewById(R.id.text_skinPerfection), instance.getSkinWhite()));
        seekBarRowList.add(new SeekBarRow(REMOVE_BLEMISHES,(SeekBar) findViewById(R.id.seekbar_skinRemoveBlemishes),(TextView)findViewById(R.id.text_skinRemoveBlemishes),instance.getSkinRemoveBlemishes()));
        seekBarRowList.add(new SeekBarRow(SKIN_TONE_SATURATION,(SeekBar) findViewById(R.id.seekbar_skinSaturation),(TextView)findViewById(R.id.text_skinSaturation),instance.getSkinSaturation()));
        seekBarRowList.add(new SeekBarRow(SKIN_SHINNING_TENDERNESS,(SeekBar) findViewById(R.id.seekbar_skinTenderness),(TextView)findViewById(R.id.text_skinTenderness),instance.getSkinTenderness()));

        for(final SeekBarRow row: seekBarRowList){
            int initValue = row.initValue;
            row.seekBar.setProgress(initValue);
            row.value.setText(Integer.toString(initValue));

            row.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    row.value.setText(Integer.toString(progress));
                    saveBeautyConfig(row.id,progress);
                    onEventListener.onAdjustFaceBeauty(row.id, progress);
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
        }
    }



    private void saveBeautyConfig(int id, int progress) {
        switch (id){
            case SKIN_TONE_PERFECTION :
                instance.setSkinPerfection(progress);
                break;
            case REMOVE_BLEMISHES  :
                instance.setSkinRemoveBlemishes(progress);
                break;
            case SKIN_TONE_SATURATION :
                instance.setSkinSaturation(progress);
                break;
            case SKIN_SHINNING_TENDERNESS  :
                instance.setSkinTenderness(progress);
                break;
        }
    }

    private void updateView() {
        mSwitchBeautySecond.setImageResource(isOpen ? R.drawable.kai : R.drawable.guan);
        for (SeekBarRow row : seekBarRowList) {
            SeekBar seekBar = row.seekBar;
            seekBar.setEnabled(isOpen);
        }
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.switch_beauty_second) {
            isOpen = !isOpen;
            instance.setBeautyEnabled(isOpen);
            onEventListener.onSwitchFaceBeauty(isOpen);
            updateView();
        }
    }
}
