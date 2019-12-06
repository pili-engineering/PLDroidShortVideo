package com.qiniu.pili.droid.shortvideo.demo.view;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.qiniu.pili.droid.shortvideo.demo.R;

public class PaintSelectorPanel extends LinearLayout {
    private Context mContext;
    private RecyclerView mColorListView;
    private OnPaintSelectorListener mOnPaintSelectorListener;
    private ImageView mCurColorView;
    private ImageButton mCloseBtn;
    private TextView mUndoText;
    private TextView mClearText;
    private SeekBar mSizeSeekBar;
    private ImageView mSizeImage;
    private PaintColorListAdapter mAdapter;

    private static int PAINT_MAX_SIZE = 100;

    public static int[] colors = {R.color.paint1, R.color.paint2, R.color.paint3, R.color.paint4,
            R.color.paint5, R.color.paint6, R.color.paint7, R.color.paint8,
            R.color.paint9, R.color.paint10};

    public PaintSelectorPanel(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;

        View view = LayoutInflater.from(context).inflate(R.layout.panel_paint_selector, this);
        mSizeImage = (ImageView) view.findViewById(R.id.paint_size_image);
        mSizeSeekBar = (SeekBar) view.findViewById(R.id.paint_size_seek);
        mUndoText = (TextView) view.findViewById(R.id.paint_undo_text);
        mClearText = (TextView) view.findViewById(R.id.paint_clear_text);

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
                if (mOnPaintSelectorListener != null) {
                    mOnPaintSelectorListener.onPaintSizeSelected((int) (PAINT_MAX_SIZE * scale));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        mColorListView = (RecyclerView) view.findViewById(R.id.recycler_paint_color);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);
        mColorListView.setLayoutManager(layoutManager);
        mAdapter = new PaintColorListAdapter(colors);
        mColorListView.setAdapter(mAdapter);

        mCloseBtn = (ImageButton) view.findViewById(R.id.close_btn);
        mCloseBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnPaintSelectorListener != null) {
                    mOnPaintSelectorListener.onViewClosed();
                }
            }
        });
    }

    public void setup() {
        mSizeSeekBar.setProgress(10);
        mAdapter.setPosition(1);
        mAdapter.notifyDataSetChanged();
    }

    public void setOnPaintSelectorListener(OnPaintSelectorListener listener) {
        mOnPaintSelectorListener = listener;
    }

    public interface OnPaintSelectorListener {
        void onPaintColorSelected(int color);

        void onPaintSizeSelected(int size);

        void onPaintUndoSelected();

        void onPaintClearSelected();

        void onViewClosed();
    }

    private class ItemViewHolder extends RecyclerView.ViewHolder {
        public ImageView mImageView;

        public ItemViewHolder(View itemView) {
            super(itemView);
            mImageView = (ImageView) itemView.findViewById(R.id.paint_color_view);
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
            Context context = parent.getContext();
            LayoutInflater inflater = LayoutInflater.from(context);
            View contactView = inflater.inflate(R.layout.item_paint_color, parent, false);
            ItemViewHolder viewHolder = new ItemViewHolder(contactView);
            return viewHolder;
        }

        @Override
        public void onBindViewHolder(final ItemViewHolder holder, int position) {
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
