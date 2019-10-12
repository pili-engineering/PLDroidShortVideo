package com.qiniu.pili.droid.shortvideo.demo.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.qiniu.pili.droid.shortvideo.demo.R;

public class ItemView extends FrameLayout {

    private TextView tvName;
    private TextView tvValue;

    public ItemView(Context context) {
        this(context, null);
    }

    public ItemView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ItemView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.v_item, this);
        tvName = (TextView) findViewById(R.id.tv_name);
        tvValue = (TextView) findViewById(R.id.tv_value);

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.ItemView);
        String text = typedArray.getString(R.styleable.ItemView_text);

        if(!TextUtils.isEmpty(text)){
            tvName.setText(text);
        }

        typedArray.recycle();
    }

    public void setValue(String v){
        tvValue.setText(v);
    }

}
