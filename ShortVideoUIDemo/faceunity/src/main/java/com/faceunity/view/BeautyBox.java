package com.faceunity.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.SoundEffectConstants;
import android.view.View;
import android.widget.Checkable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.faceunity.R;

/**
 * Created by tujh on 2018/4/17.
 */
public class BeautyBox extends LinearLayout implements Checkable {

    private boolean mIsOpen = false;
    private boolean mIsChecked = false;
    private boolean mIsDouble = false;

    private boolean mBroadcasting;
    private OnCheckedChangeListener mOnCheckedChangeListener;
    private OnOpenChangeListener mOnOpenChangeListener;
    private OnDoubleChangeListener mOnDoubleChangeListener;

    private int checkedModel;
    private Drawable drawableOpenNormal;
    private Drawable drawableOpenChecked;
    private Drawable drawableCloseNormal;
    private Drawable drawableCloseChecked;

    private String textNormalStr;
    private String textDoubleStr;

    private int textNormalColor;
    private int textCheckedColor;

    private ImageView boxImg;
    private TextView boxText;

    public BeautyBox(Context context) {
        this(context, null);
    }

    public BeautyBox(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BeautyBox(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        LayoutInflater.from(context).inflate(R.layout.layout_beauty_box, this);

        boxImg = (ImageView) findViewById(R.id.beauty_box_img);
        boxText = (TextView) findViewById(R.id.beauty_box_text);

        final TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.BeautyBox, defStyleAttr, 0);

        drawableOpenNormal = a.getDrawable(R.styleable.BeautyBox_drawable_open_normal);
        drawableOpenChecked = a.getDrawable(R.styleable.BeautyBox_drawable_open_checked);
        drawableCloseNormal = a.getDrawable(R.styleable.BeautyBox_drawable_close_normal);
        drawableCloseChecked = a.getDrawable(R.styleable.BeautyBox_drawable_close_checked);

        textNormalStr = a.getString(R.styleable.BeautyBox_text_normal);
        textDoubleStr = a.getString(R.styleable.BeautyBox_text_double);
        if (TextUtils.isEmpty(textDoubleStr))
            textDoubleStr = textNormalStr;

        textNormalColor = a.getColor(R.styleable.BeautyBox_textColor_normal, getResources().getColor(R.color.main_color_c5c5c5));
        textCheckedColor = a.getColor(R.styleable.BeautyBox_textColor_checked, getResources().getColor(R.color.main_color));

        final boolean checked = a.getBoolean(R.styleable.BeautyBox_checked, false);

        checkedModel = a.getInt(R.styleable.BeautyBox_checked_model, 1);

        boxText.setText(textNormalStr);
        boxText.setTextColor(getResources().getColor(R.color.main_color_c5c5c5));

        setChecked(checked);

        a.recycle();

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });
    }

    @Override
    public boolean performClick() {
        toggle();

        final boolean handled = super.performClick();
        if (!handled) {
            // View only makes a sound effect if the onClickListener was
            // called, so we'll need to make one here instead.
            playSoundEffect(SoundEffectConstants.CLICK);
        }

        return handled;
    }

    @Override
    public void setChecked(boolean checked) {
        if (mIsChecked == checked) return;
        updateView(mIsChecked = checked);

        // Avoid infinite recursions if setChecked() is called from a listener
        if (mBroadcasting) {
            return;
        }

        mBroadcasting = true;
        if (mOnCheckedChangeListener != null) {
            mOnCheckedChangeListener.onCheckedChanged(this, mIsChecked);
        }
        mBroadcasting = false;
    }

    private void updateView(boolean checked) {
        updateImg(checked, mIsOpen);
        boxText.setTextColor(checked ? textCheckedColor : textNormalColor);
    }

    public void updateImg(boolean checked, boolean isOpen) {
        if (isOpen) {
            boxImg.setImageDrawable(checked ? drawableOpenChecked : drawableOpenNormal);
        } else {
            boxImg.setImageDrawable(checked ? drawableCloseChecked : drawableCloseNormal);
        }
    }

    @Override
    public boolean isChecked() {
        return mIsChecked;
    }

    @Override
    public void toggle() {
        if (checkedModel == 1) setChecked(true);
        else if (checkedModel == 2) {
            if (mIsChecked) {
                setOpen(!mIsOpen);
                if (mOnOpenChangeListener != null) {
                    mOnOpenChangeListener.onOpenChanged(this, mIsOpen);
                }
            } else {
                setChecked(true);
            }
        } else if (checkedModel == 3) {
            if (mIsChecked) {
                mIsDouble = !mIsDouble;
                boxText.setText(mIsDouble ? textDoubleStr : textNormalStr);
                if (mOnDoubleChangeListener != null) {
                    mOnDoubleChangeListener.onDoubleChanged(this, mIsDouble);
                }
            } else {
                setChecked(true);
            }
        }
    }

    public void setOpen(boolean open) {
        updateImg(mIsChecked, mIsOpen = open);
    }

    public void setBackgroundImg(int resId) {
        boxImg.setBackgroundResource(resId);
    }

    public void clearBackgroundImg() {
        boxImg.setBackground(null);
    }

    public void setOnCheckedChangeListener(OnCheckedChangeListener mOnCheckedChangeListener) {
        this.mOnCheckedChangeListener = mOnCheckedChangeListener;
    }

    /**
     * Interface definition for a callback to be invoked when the checked state
     * of a BeautyBox changed.
     */
    public static interface OnCheckedChangeListener {
        /**
         * Called when the checked state of a compound button has changed.
         *
         * @param beautyBox The BeautyBox view whose state has changed.
         * @param isChecked The new checked state of buttonView.
         */
        void onCheckedChanged(BeautyBox beautyBox, boolean isChecked);
    }

    public static interface OnOpenChangeListener {
        void onOpenChanged(BeautyBox beautyBox, boolean isOpen);
    }

    public void setOnOpenChangeListener(OnOpenChangeListener onOpenChangeListener) {
        mOnOpenChangeListener = onOpenChangeListener;
    }

    public static interface OnDoubleChangeListener {
        void onDoubleChanged(BeautyBox beautyBox, boolean isDouble);
    }

    public void setOnDoubleChangeListener(OnDoubleChangeListener onDoubleChangeListener) {
        mOnDoubleChangeListener = onDoubleChangeListener;
    }
}
