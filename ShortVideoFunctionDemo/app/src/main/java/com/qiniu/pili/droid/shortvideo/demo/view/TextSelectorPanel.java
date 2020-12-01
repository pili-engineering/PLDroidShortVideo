package com.qiniu.pili.droid.shortvideo.demo.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.qiniu.pili.droid.shortvideo.demo.R;

public class TextSelectorPanel extends LinearLayout {
    private RecyclerView mTextViews;
    private Context mContext;
    private OnTextSelectorListener mOnTextSelectorListener;
    private ImageButton mCloseBtn;

    public static int[] colors = {R.color.text1, R.color.text2, R.color.text3, R.color.text4,
            R.color.text5, R.color.text6, R.color.text7, R.color.text8,
            R.color.text9, R.color.text10, R.color.text11, R.color.text12};


    public TextSelectorPanel(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        View view = LayoutInflater.from(context).inflate(R.layout.panel_text_selector, this);

        mTextViews = (RecyclerView) view.findViewById(R.id.recycler_text);
        TextInfo[] infos = initTextInfos();

        mTextViews.setLayoutManager(new GridLayoutManager(getContext(), 4));
        mTextViews.setAdapter(new TextEffectListAdapter(infos));

        mCloseBtn = (ImageButton) view.findViewById(R.id.close_btn);
        mCloseBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnTextSelectorListener != null) {
                    mOnTextSelectorListener.onViewClosed();
                }
            }
        });
    }

    public void setOnTextSelectorListener(OnTextSelectorListener listener) {
        mOnTextSelectorListener = listener;
    }

    private TextInfo[] initTextInfos() {
        TextInfo[] infos = new TextInfo[colors.length];
        for (int i = 0; i < infos.length; i++) {
            TextInfo textInfo = new TextInfo();
            textInfo.text = "七牛";
            infos[i] = textInfo;
            textInfo.colorID = colors[i];
            textInfo.alpha = 0.8f;

            if (i >= 4 && i < 8) {
                textInfo.strokeColor = Color.WHITE;
                textInfo.strokeWidth = 5.0f;
            }

            if (i >= 8) {
                textInfo.colorID = R.color.white;
                textInfo.shadowRadius = 20;
                textInfo.shadowColor = mContext.getResources().getColor(colors[i]);
            }
        }
        return infos;
    }

    private class ItemViewHolder extends RecyclerView.ViewHolder {
        public StrokedTextView mText;

        public ItemViewHolder(View itemView) {
            super(itemView);
            mText = (StrokedTextView) itemView.findViewById(R.id.TextView);
            mText.setClickable(true);
            mText.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnTextSelectorListener != null) {
                        mOnTextSelectorListener.onTextSelected(mText);
                    }
                }
            });
        }
    }

    private class TextEffectListAdapter extends RecyclerView.Adapter<ItemViewHolder> {
        private TextInfo[] mInfos;

        public TextEffectListAdapter(TextInfo[] infos) {
            this.mInfos = infos;
        }

        @Override
        public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);

            // Inflate the custom layout
            View contactView = inflater.inflate(R.layout.item_text, parent, false);

            // Return a new holder instance
            ItemViewHolder viewHolder = new ItemViewHolder(contactView);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(ItemViewHolder holder, int position) {
            // Get the data model based on position
            final TextInfo info = mInfos[position];

            // Set item views based on your views and data model
            holder.mText.setText(info.text);
            holder.mText.setTextColor(mContext.getResources().getColor(info.colorID));
            holder.mText.setTypeface(info.typeface, info.style);
            holder.mText.setStrokeWidth(info.strokeWidth);
            holder.mText.setStrokeColor(info.strokeColor);
            if (info.shadowRadius > 0) {
                holder.mText.setShadowLayer(info.shadowRadius, info.shadowDx, info.shadowDy, info.shadowColor);
            }
        }

        @Override
        public int getItemCount() {
            return mInfos.length;
        }
    }

    public interface OnTextSelectorListener {
        void onTextSelected(StrokedTextView textView);

        void onViewClosed();
    }

    private class TextInfo {
        String text;
        int colorID;
        Typeface typeface = Typeface.MONOSPACE;
        int style = Typeface.BOLD;
        float alpha = 1;
        int shadowColor = Color.TRANSPARENT;
        int shadowRadius;
        int shadowDx;
        int shadowDy;
        int strokeColor;
        float strokeWidth;
    }
}
