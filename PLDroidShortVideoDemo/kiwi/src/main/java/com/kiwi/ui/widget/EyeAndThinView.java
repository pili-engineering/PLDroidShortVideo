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

import static com.kiwi.ui.widget.KwControlView.BEAUTY_BIG_EYE_TYPE;
import static com.kiwi.ui.widget.KwControlView.BEAUTY_THIN_FACE_TYPE;

public class EyeAndThinView extends FrameLayout implements View.OnClickListener {
    private SharedPreferenceManager instance;
    private OnViewEventListener onEventListener;
    private ImageView mSwitchBeauty;
    private List<SeekBarRow> seekBarRowList = new ArrayList<>();
    private boolean isOpen = false;

    public void setOnEventListener(OnViewEventListener onEventListener) {
        this.onEventListener = onEventListener;
    }

    public EyeAndThinView(Context context) {
        super(context);
        init(null, 0);
    }

    public EyeAndThinView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public EyeAndThinView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        this.instance = SharedPreferenceManager.getInstance();
        LayoutInflater.from(getContext()).inflate(R.layout.eye_thin_layout, this);

        mSwitchBeauty = (ImageView) findViewById(R.id.switch_beauty);
        mSwitchBeauty.setOnClickListener(this);

        initSeekBarUI();

        this.isOpen = instance.isLocalBeautyEnabled();
        updateView();
    }

    private void initSeekBarUI() {
        seekBarRowList.clear();
        seekBarRowList.add(new SeekBarRow(BEAUTY_BIG_EYE_TYPE,(SeekBar) findViewById(R.id.seekbar_eyemagnify),(TextView)findViewById(R.id.text_eyemagnify), instance.getBigEye()));
        seekBarRowList.add(new SeekBarRow(BEAUTY_THIN_FACE_TYPE,(SeekBar) findViewById(R.id.seekbar_faceSculpt),(TextView)findViewById(R.id.text_faceSculpt),instance.getThinFace()));

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
            case BEAUTY_BIG_EYE_TYPE :
                instance.setBigEye(progress);
                break;
            case BEAUTY_THIN_FACE_TYPE  :
                instance.setThinFace(progress);
                break;
        }
    }

    private void updateView() {
        mSwitchBeauty.setImageResource(isOpen ? R.drawable.kai : R.drawable.guan);
        for (SeekBarRow row : seekBarRowList) {
            SeekBar seekBar = row.seekBar;
            seekBar.setEnabled(isOpen);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.switch_beauty) {
            isOpen = !isOpen;

            instance.setLocalBeautyEnabled(isOpen);
            onEventListener.onSwitchEyeAndThin(isOpen);
            updateView();
        }
    }

}
