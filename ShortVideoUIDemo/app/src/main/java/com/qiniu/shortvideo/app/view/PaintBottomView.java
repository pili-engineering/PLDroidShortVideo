package com.qiniu.shortvideo.app.view;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.qiniu.shortvideo.app.R;

/**
 * 编辑模块涂鸦功能视图
 */
public class PaintBottomView extends BaseBottomView {
    private static int PAINT_MAX_SIZE = 100;

    private RecyclerView mColorListView;
    private OnPaintSelectorListener mOnPaintSelectorListener;
    private ImageView mCurColorView;
    private ImageButton mCloseBtn;
    private TextView mUndoText;
    private TextView mClearText;
    private SeekBar mSizeSeekBar;
    private ImageView mSizeImage;
    private PaintColorListAdapter mAdapter;

    private int mCurrentPaintSize;

    public static int[] colors = {R.color.paint1, R.color.paint2, R.color.paint3, R.color.paint4,
            R.color.paint5, R.color.paint6, R.color.paint7, R.color.paint8,
            R.color.paint9, R.color.paint10};

    public interface OnPaintSelectorListener {
        void onPaintColorSelected(int color);

        void onPaintSizeSelected(int size);

        void onPaintUndoSelected();

        void onPaintClearSelected();

        void onViewClosed();
    }

    public PaintBottomView(@NonNull Context context) {
        this(context, null);
    }

    public PaintBottomView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PaintBottomView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setOnPaintSelectorListener(OnPaintSelectorListener listener) {
        mOnPaintSelectorListener = listener;
    }

    @Override
    protected void init() {
        View view = LayoutInflater.from(mContext).inflate(R.layout.editor_paint_view, this);
        mSizeImage = view.findViewById(R.id.paint_size_image);
        mSizeSeekBar = view.findViewById(R.id.paint_size_seek);
        mUndoText = view.findViewById(R.id.paint_undo_text);
        mClearText = view.findViewById(R.id.paint_clear_text);

        mUndoText.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnPaintSelectorListener != null) {
                    mOnPaintSelectorListener.onPaintUndoSelected();
                }
            }
        });

        mClearText.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnPaintSelectorListener != null) {
                    mOnPaintSelectorListener.onPaintClearSelected();
                }
            }
        });

        final int step = 1;
        final int max = 100;
        final int min = 3;

        mSizeSeekBar.setMax((max - min) / step);
        mSizeSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float scale = ((float) (min + (progress * step))) / 100;

                mSizeImage.setScaleX(scale);
                mSizeImage.setScaleY(scale);
                mCurrentPaintSize = (int) (PAINT_MAX_SIZE * scale);
                if (mOnPaintSelectorListener != null) {
                    mOnPaintSelectorListener.onPaintSizeSelected(mCurrentPaintSize);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        mColorListView = view.findViewById(R.id.recycler_paint_color);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
        mColorListView.setLayoutManager(layoutManager);
        mAdapter = new PaintColorListAdapter(colors);
        mColorListView.setAdapter(mAdapter);

        mCloseBtn = view.findViewById(R.id.close_btn);
        mCloseBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnPaintSelectorListener != null) {
                    mOnPaintSelectorListener.onViewClosed();
                }
            }
        });

        mCurrentPaintSize = 10;
        mSizeSeekBar.setProgress(mCurrentPaintSize);
    }

    @Override
    public boolean isPlayerNeedZoom() {
        return false;
    }

    private class ItemViewHolder extends RecyclerView.ViewHolder {
        ImageView mImageView;

        public ItemViewHolder(View itemView) {
            super(itemView);
            mImageView = itemView.findViewById(R.id.paint_color_view);
        }
    }

    private class PaintColorListAdapter extends RecyclerView.Adapter<ItemViewHolder> {
        private int[] mColors;
        private int mPosition = 0;

        public PaintColorListAdapter(int[] colors) {
            this.mColors = colors;
        }

        public void setPosition(int position) {
            mPosition = position;
        }

        @Override
        public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View paintColorView = inflater.inflate(R.layout.item_paint_color, parent, false);
            return new ItemViewHolder(paintColorView);
        }

        @Override
        public void onBindViewHolder(final ItemViewHolder holder, final int position) {
            final int color = mColors[position];
            holder.mImageView.setColorFilter(mContext.getResources().getColor(color));
            holder.mImageView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mCurColorView != null) {
                        mCurColorView.setSelected(false);
                    }

                    mCurColorView = holder.mImageView;
                    mCurColorView.setSelected(true);
                    mSizeImage.setColorFilter(mContext.getResources().getColor(color));
                    if (mOnPaintSelectorListener != null) {
                        mOnPaintSelectorListener.onPaintColorSelected(mContext.getResources().getColor(color));
                    }
                }
            });
            if (mPosition == position) {
                if (mCurColorView != null) {
                    mCurColorView.setSelected(false);
                }

                mCurColorView = holder.mImageView;
                mCurColorView.setSelected(true);
                mSizeImage.setColorFilter(mContext.getResources().getColor(color));
                if (mOnPaintSelectorListener != null) {
                    mOnPaintSelectorListener.onPaintColorSelected(mContext.getResources().getColor(color));
                }
            }
        }

        @Override
        public int getItemCount() {
            return mColors.length;
        }
    }
}
