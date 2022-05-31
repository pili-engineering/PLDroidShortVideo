package com.qiniu.shortvideo.app.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.qiniu.pili.droid.shortvideo.PLMediaFile;
import com.qiniu.shortvideo.app.R;
import com.qiniu.shortvideo.app.view.layer.OnStickerOperateListener;
import com.qiniu.shortvideo.app.view.layer.StickerTextView;
import com.qiniu.shortvideo.app.view.thumbline.ThumbLineRangeBar;

import java.util.HashMap;
import java.util.Map;

/**
 * 编辑模块文字特效添加的视图
 */
public class TextBottomView extends StickerBottomView {
    private static final long DEFAULT_DURATION = 2000;
    private StickerTextView mCurTextView;
    private OnTextSelectorListener mOnTextSelectorListener;
    private Map<StickerTextView, ThumbLineRangeBar> mStickers;

    public static int[] colors = {R.color.text1, R.color.text2, R.color.text3, R.color.text4,
            R.color.text5, R.color.text6, R.color.text7, R.color.text8,
            R.color.text9, R.color.text10, R.color.text11, R.color.text12};

    public interface OnTextSelectorListener {
        /**
         * 新增文字特效时触发
         *
         * @param textView
         */
        void onTextAdded(StickerTextView textView, long startTimeMs, long durationMs);

        /**
         * 文字特效被选择时触发
         *
         * @param textView
         */
        void onTextSelected(StickerTextView textView);

        /**
         * 编辑文字特效时触发
         *
         * @return
         */
        boolean onTextEdited();

        /**
         * 删除文字特效时触发
         *
         * @return
         */
        boolean onTextDeleted();

        /**
         * 文字特效作用时间时间改变时触发
         *
         * @param textView
         * @param startTimeMs
         * @param durationMs
         */
        void onTextRangeChanged(StickerTextView textView, long startTimeMs, long durationMs);
    }

    public TextBottomView(@NonNull Context context, PLMediaFile mediaFile) {
        super(context, mediaFile);
    }

    public void setOnTextSelectorListener(OnTextSelectorListener listener) {
        mOnTextSelectorListener = listener;
    }

    @Override
    protected void init() {
        super.init();
        mBottomViewTitle.setText(R.string.text_effect_string);

        TextInfo[] infos = initTextInfos();

        mStickerRecyclerView.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false));
        mStickerRecyclerView.setAdapter(new TextEffectListAdapter(infos));
    }

    @Override
    public boolean isPlayerNeedZoom() {
        return false;
    }

    @Override
    protected void playStatusChanged(boolean isPlaying) {
    }

    private class ItemViewHolder extends RecyclerView.ViewHolder {
        public TextView mText;

        public ItemViewHolder(View itemView) {
            super(itemView);
            mText = itemView.findViewById(R.id.TextView);
            mText.setClickable(true);
            mText.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mIsPlaying) {
                        pausePlayer();
                    }
                    // 点击具体动图时，添加到滑动条上
                    ThumbLineRangeBar.ThumbLineRangeBarView thumbLineRangeBarView = new ThumbLineRangeBar.ThumbLineRangeBarView() {
                        View rootView = LayoutInflater.from(mContext).inflate(R.layout.widget_range_bar, null);
                        View headView = rootView.findViewById(R.id.head_view);
                        View tailView = rootView.findViewById(R.id.tail_view);
                        View middleView = rootView.findViewById(R.id.middle_view);

                        @Override
                        public ViewGroup getContainer() {
                            return (ViewGroup) rootView;
                        }

                        @Override
                        public View getHeadView() {
                            return headView;
                        }

                        @Override
                        public View getTailView() {
                            return tailView;
                        }

                        @Override
                        public View getMiddleView() {
                            return middleView;
                        }
                    };

                    final StickerTextView textView = (StickerTextView) View.inflate(mContext, R.layout.sticker_text_view, null);
                    textView.setTypeface(mText.getTypeface());
                    textView.setTextColor(mText.getCurrentTextColor());
                    textView.setShadowLayer(mText.getShadowRadius(), mText.getShadowDx(), mText.getShadowDy(), mText.getShadowColor());
                    textView.setAlpha(mText.getAlpha());
                    textView.setOnStickerOperateListener(new OnStickerOperateListener() {
                        @Override
                        public void onDeleteClicked() {
                            removeRangeBar(mStickers.get(textView));
                            mStickers.remove(textView);
                            if (mOnTextSelectorListener != null) {
                                mOnTextSelectorListener.onTextDeleted();
                            }
                        }

                        @Override
                        public void onEditClicked() {
                            if (mOnTextSelectorListener != null) {
                                mOnTextSelectorListener.onTextEdited();
                            }
                        }

                        @Override
                        public void onRotateClicked() {

                        }

                        @Override
                        public void onStickerSelected() {
                            pausePlayer();
                            if (mStickers != null) {
                                mVideoThumbLineView.switchRangeBarToActive(mStickers.get(textView));
                            }
                            if (mStickers != null && mCurTextView != null && mCurTextView.isEditable()) {
                                mVideoThumbLineView.switchRangeBarToActive(mStickers.get(mCurTextView));
                            }
                            mCurTextView = textView;
                            if (mOnTextSelectorListener != null) {
                                mOnTextSelectorListener.onTextSelected(textView);
                            }
                        }
                    });
                    mVideoThumbLineView.switchRangeBarToFix();

                    if (mOnTextSelectorListener != null) {
                        mOnTextSelectorListener.onTextAdded(textView, mCurrentPosition, DEFAULT_DURATION);
                    }

                    ThumbLineRangeBar thumbLineRangeBar = new ThumbLineRangeBar(mVideoThumbLineView, mCurrentPosition, 2000, 2000, mMediaFile.getDurationMs(), thumbLineRangeBarView, new ThumbLineRangeBar.OnSelectedDurationChangeListener() {
                        @Override
                        public void onRangeBarClicked(ThumbLineRangeBar rangeBar) {
                            // 只有点击贴图的时候才可以改变动图的显示时间段，单独点击不会触发
                        }

                        @Override
                        public void onDurationChange(long startTime, long duration) {
                            if (mOnTextSelectorListener != null) {
                                mOnTextSelectorListener.onTextRangeChanged(textView, startTime, duration);
                            }
                        }
                    });

                    if (mStickers == null) {
                        mStickers = new HashMap<>();
                    }

                    mStickers.put(textView, thumbLineRangeBar);

                    addRangeBar(thumbLineRangeBar);
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
            View contactView = inflater.inflate(R.layout.item_text, parent, false);

            return new ItemViewHolder(contactView);
        }

        @Override
        public void onBindViewHolder(final ItemViewHolder holder, int position) {
            // Get the data model based on position
            final TextInfo info = mInfos[position];

            // Set item views based on your views and data model
            holder.mText.setText(info.text);
            holder.mText.setTextColor(mContext.getResources().getColor(info.colorID));
            holder.mText.setTypeface(info.typeface, info.style);
            if (info.shadowRadius > 0) {
                holder.mText.setShadowLayer(info.shadowRadius, info.shadowDx, info.shadowDy, info.shadowColor);
            }
        }

        @Override
        public int getItemCount() {
            return mInfos.length;
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }
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
