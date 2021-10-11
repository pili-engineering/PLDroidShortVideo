package com.qiniu.pili.droid.shortvideo.demo.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;

import androidx.annotation.Nullable;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.qiniu.pili.droid.shortvideo.demo.R;
import com.qiniu.pili.droid.shortvideo.demo.transition.TransitionBase;
import com.qiniu.pili.droid.shortvideo.demo.utils.ToastUtils;

public class TransitionEditView extends LinearLayout {
    public static final String[] TEXT_COLOR_TIPS_ARRAY = {
            "#FFFFFF", "#FF3D49", "#FFEE00", "#578BFF", "#00C6FF", "#EACFD4", "#F8EADA", "#CEFFC6", "#C3CADA", "#000000"
    };
    public static final String[] TEXT_SIZE_TIPS_ARRAY = {
            "46", "50", "54", "58", "62", "66", "70", "74", "78", "82", "86", "90"
    };
    public static final String[] TEXT_TYPEFFACE_TIPS_ARRAY = {
            "Sans_Serif", "Default_Bold", "zcool-gdh", "HappyZcool-2016"
    };

    private TransitionBase mTransition;
    private final Button mBackBtn;
    private final EditText mTitleEditText;
    private final EditText mSubtitleEditText;

    private TransitionTextView mTransitionTitle;
    private TransitionTextView mTransitionSubtitle;
    private EditText mCurFocusText;
    private final NumberPicker mNumberPicker;
    private final ViewGroup mNumberPickerGroup;
    private final TextView mConfirmText;

    private Typeface[] mTypefaces;

    private final ViewGroup mColorGroup;
    private final ViewGroup mSizeGroup;
    private final ViewGroup mTypefaceGroup;

    private final Context mContext;

    public TransitionEditView(Context context) {
        this(context, null);
    }

    public TransitionEditView(final Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        View view = LayoutInflater.from(context).inflate(R.layout.transition_edit_view, this);
        mBackBtn = view.findViewById(R.id.back_button);
        mTitleEditText = view.findViewById(R.id.title_edit_text);
        mSubtitleEditText = view.findViewById(R.id.subtitle_edit_text);
        mNumberPicker = view.findViewById(R.id.number_picker);
        mColorGroup = view.findViewById(R.id.color_group);
        mSizeGroup = view.findViewById(R.id.size_group);
        mTypefaceGroup = view.findViewById(R.id.typeface_group);
        mNumberPickerGroup = view.findViewById(R.id.number_picker_group);
        mConfirmText = view.findViewById(R.id.text_confirm);

        mConfirmText.setOnClickListener(v -> {
            mNumberPickerGroup.setVisibility(GONE);

            if (mCurFocusText == mTitleEditText && null != mTransitionTitle) {
                cloneEditText(mTransitionTitle, mCurFocusText);
            }
            if (mCurFocusText == mSubtitleEditText && null != mTransitionSubtitle) {
                cloneEditText(mTransitionSubtitle, mCurFocusText);
            }

            mTransition.updateTransitions();
        });

        mColorGroup.setOnClickListener(mOnClickListener);
        mSizeGroup.setOnClickListener(mOnClickListener);
        mTypefaceGroup.setOnClickListener(mOnClickListener);
        mTitleEditText.setOnFocusChangeListener(mOnFocusChangeListener);
        mSubtitleEditText.setOnFocusChangeListener(mOnFocusChangeListener);

        mBackBtn.setOnClickListener(v -> {
            hideSoftInput();
            setVisibility(GONE);
        });

        initTypeFaces();
    }

    private void initTypeFaces() {
        mTypefaces = new Typeface[4];
        mTypefaces[0] = Typeface.SANS_SERIF;
        mTypefaces[1] = Typeface.DEFAULT_BOLD;
        mTypefaces[2] = Typeface.createFromAsset(mContext.getAssets(), "fonts/zcool-gdh.ttf");
        mTypefaces[3] = Typeface.createFromAsset(mContext.getAssets(), "fonts/HappyZcool-2016.ttf");
    }

    private final OnClickListener mOnClickListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            showNumberPicker(v);
        }
    };

    private void hideSoftInput() {
        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(mCurFocusText.getWindowToken(), 0);
        }
    }

    private void showNumberPicker(final View view) {
        if (null == mCurFocusText) {
            ToastUtils.showShortToast("请先选中需要修改的文字");
            return;
        }

        hideSoftInput();

        mNumberPickerGroup.setVisibility(VISIBLE);
        String[] array;
        switch (view.getId()) {
            case R.id.color_group:
                array = TEXT_COLOR_TIPS_ARRAY;
                break;
            case R.id.size_group:
                array = TEXT_SIZE_TIPS_ARRAY;
                break;
            default:
                array = TEXT_TYPEFFACE_TIPS_ARRAY;
                break;
        }
        mNumberPicker.setDisplayedValues(null);
        mNumberPicker.setMinValue(0);
        mNumberPicker.setMaxValue(array.length - 1);
        mNumberPicker.setDisplayedValues(array);
        mNumberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                switch (view.getId()) {
                    case R.id.color_group:
                        mCurFocusText.setTextColor(Color.parseColor(TEXT_COLOR_TIPS_ARRAY[newVal]));
                        break;
                    case R.id.size_group:
                        mCurFocusText.setTextSize(TypedValue.COMPLEX_UNIT_PX, Integer.parseInt(TEXT_SIZE_TIPS_ARRAY[newVal]));
                        break;
                    default:
                        mCurFocusText.setTypeface(mTypefaces[newVal]);
                        break;
                }
            }
        });
    }

    private final OnFocusChangeListener mOnFocusChangeListener = new OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                mCurFocusText = (EditText) v;
            }
        }
    };

    public void setTransition(TransitionBase transition) {
        mTransition = transition;
        mTransitionTitle = mTransition.getTitle();
        mTransitionSubtitle = mTransition.getSubtitle();

        mTitleEditText.setVisibility(GONE);
        mSubtitleEditText.setVisibility(GONE);

        if (mTransitionTitle != null) {
            mTitleEditText.setVisibility(VISIBLE);
            mCurFocusText = mTitleEditText;
            cloneEditText(mTitleEditText, mTransitionTitle);
            mTitleEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (getVisibility() == View.VISIBLE) {
                        cloneEditText(mTransitionTitle, mTitleEditText);
                        mTransition.updateTransitions();
                    }
                }
            });
        }

        if (mTransitionSubtitle != null) {
            mSubtitleEditText.setVisibility(VISIBLE);
            cloneEditText(mSubtitleEditText, mTransitionSubtitle);
            mSubtitleEditText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if (getVisibility() == View.VISIBLE) {
                        cloneEditText(mTransitionSubtitle, mSubtitleEditText);
                        mTransition.updateTransitions();
                    }
                }
            });
        }
    }

    private void cloneEditText(EditText dstText, EditText srcText) {
        dstText.setText(srcText.getText());
        dstText.setTextColor(srcText.getTextColors());
        dstText.setTypeface(srcText.getTypeface());
        dstText.setTextSize(TypedValue.COMPLEX_UNIT_PX, srcText.getTextSize());
    }
}
