package com.kiwi.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioGroup;

import com.blankj.utilcode.utils.Utils;
import com.kiwi.ui.OnViewEventListener;
import com.kiwi.ui.R;

public class KwControlView extends FrameLayout implements View.OnClickListener {

    public static final int BEAUTY_BIG_EYE_TYPE = 0;  //大眼
    public static final int BEAUTY_THIN_FACE_TYPE = 1;//瘦脸

    public static final int SKIN_TONE_PERFECTION = 2; //美白  全局美颜2
    public static final int REMOVE_BLEMISHES = 3;//磨皮
    public static final int SKIN_TONE_SATURATION = 4;//饱和
    public static final int SKIN_SHINNING_TENDERNESS = 5;  //粉嫩

    private LinearLayout mToolBarLayout;

    //特效面板
    private View mEffectView;
    private StickerView mStickerView;
    private EyeAndThinView mEyeAndThinView;
    private FaceBeautyView mFaceBeautyView;

    private OnViewEventListener onEventListener;

    public KwControlView(Context context) {
        super(context);
        init(null, 0);
    }

    public KwControlView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public KwControlView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    /**
     * 初始化加载布局
     *
     * @param attrs
     * @param defStyle
     */
    private void init(AttributeSet attrs, int defStyle) {
        //初始化
        Utils.init(getContext());

        // 加载布局
        LayoutInflater.from(getContext()).inflate(getContentLayoutId(), this);
        mToolBarLayout = (LinearLayout) findViewById(R.id.layout_toolbar);
        findViewById(R.id.btn_camera_effect).setOnClickListener(this);
        findViewById(R.id.close_effect).setOnClickListener(this);

        initEffectView();
    }


    protected int getContentLayoutId() {
        return R.layout.control_layout;
    }

    /**
     * @param v
     */
    @Override
    public void onClick(View v) {
        /*贴纸以及美颜、瘦脸*/
        if (v.getId() == R.id.btn_camera_effect) {
            mToolBarLayout.setVisibility(GONE);
            mEffectView.setVisibility(VISIBLE);
            findViewById(R.id.btn_bar_sticker).performClick();
        }

        if (v.getId() == R.id.close_effect) {
            mEffectView.setVisibility(GONE);
            mToolBarLayout.setVisibility(View.VISIBLE);
        }
    }

    public void setOnEventListener(OnViewEventListener onEventListener) {
        this.onEventListener = onEventListener;
    }

    private void initEffectView() {
        mEffectView = findViewById(R.id.layout_effect);

        mStickerView = (StickerView) findViewById(R.id.sticker_view);
        mEyeAndThinView = (EyeAndThinView) findViewById(R.id.eye_thin_view);
        mFaceBeautyView = (FaceBeautyView) findViewById(R.id.face_beauty_view);

        ((RadioGroup) findViewById(R.id.bottom_view)).setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                int id = group.getCheckedRadioButtonId();

                if (id == R.id.btn_bar_sticker) {
                    mStickerView.setOnEventListener(onEventListener);
                    mStickerView.initStickerListView();
                    setViewVisual(true, false, false);
                }

                if (id == R.id.btn_bar_eye_and_thin) {
                    mEyeAndThinView.setOnEventListener(onEventListener);
                    mEyeAndThinView.setVisibility(VISIBLE);
                    setViewVisual(false, true, false);
                }

                if (id == R.id.btn_bar_face_beauty) {
                    mFaceBeautyView.setVisibility(VISIBLE);
                    mFaceBeautyView.setOnEventListener(onEventListener);
                    setViewVisual(false, false, true);
                }
            }
        });
    }

    private void setViewVisual(boolean stickerVisual, boolean eyeAndThinVisual,
                               boolean faceBeautyVisual) {
        mStickerView.setVisibility(stickerVisual ? VISIBLE : GONE);
        mEyeAndThinView.setVisibility(eyeAndThinVisual ? VISIBLE : GONE);
        mFaceBeautyView.setVisibility(faceBeautyVisual ? VISIBLE : GONE);
    }
}
