package com.faceunity.view.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.faceunity.R;

import java.util.ArrayList;

public class StickerOptionAdapter extends RecyclerView.Adapter<StickerOptionAdapter.StickerOptionViewHolder> {

    private ArrayList<StickerOptionItem> mStickerOptions;
    private OnStickerOptionsClickedListener mOnStickerOptionsClickedListener;
    private int mSelectedPosition = 0;
    private Context mContext;

    public interface OnStickerOptionsClickedListener {
        void onStickerOptionClicked(StickerOptionItem item);
    }

    public StickerOptionAdapter(Context context, ArrayList<StickerOptionItem> list) {
        mContext = context;
        mStickerOptions = list;
    }

    public void setOnStickerOptionsClickedListener(OnStickerOptionsClickedListener listener) {
        mOnStickerOptionsClickedListener = listener;
    }

    public int getSelectedPosition() {
        return mSelectedPosition;
    }

    @NonNull
    @Override
    public StickerOptionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_sticker_options_item, null);
        return new StickerOptionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final StickerOptionViewHolder holder, final int position) {
        holder.mStickerTypeTv.setText(mStickerOptions.get(position).getStickerType());
        holder.mStickerTypeTv.setTextColor(mContext.getResources().getColor(
                mSelectedPosition == position ? R.color.colorWhite : R.color.main_color_gray));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSelectedPosition = position;
                if (mOnStickerOptionsClickedListener != null) {
                    mOnStickerOptionsClickedListener.onStickerOptionClicked(mStickerOptions.get(position));
                }
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mStickerOptions == null ? 0 : mStickerOptions.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    class StickerOptionViewHolder extends RecyclerView.ViewHolder {
        TextView mStickerTypeTv;

        public StickerOptionViewHolder(View itemView) {
            super(itemView);
            mStickerTypeTv = itemView.findViewById(R.id.sticker_option_type);
        }
    }
}
